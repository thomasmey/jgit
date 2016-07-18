package org.eclipse.jgit.internal.storage.jdbc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.internal.storage.file.PackLock;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.transport.PackParser;
import org.eclipse.jgit.transport.PackedObjectInfo;

public class JdbcPackParser extends PackParser {

	private JdbcObjectInserter jdbcObjectInserter;

	private File tmpPack;

	private RandomAccessFile out;

	private long currentObjectStart;

	private long currentObjectEnd;

	private Map<Long, Long> objects;

	public JdbcPackParser(JdbcObjectDatabase jdbcObjectDatabase,
			JdbcObjectInserter jdbcObjectInserter, InputStream in) {
		super(jdbcObjectDatabase, in);
		this.jdbcObjectInserter = jdbcObjectInserter;
		this.objects = new HashMap<>();
	}

	@Override
	public PackLock parse(ProgressMonitor receiving, ProgressMonitor resolving)
			throws IOException {
		tmpPack = File.createTempFile("incoming_", ".pack"); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			out = new RandomAccessFile(tmpPack, "rw"); //$NON-NLS-1$

			super.parse(receiving, resolving);

			// out.seek(packEnd);
			// out.write(packHash);
			out.getChannel().force(true);

			tmpPack.setReadOnly();

			// transfer all objects into database
			byte[] baseObjArray = new byte[Constants.OBJECT_ID_LENGTH];
			for (int i = 0, n = getObjectCount(); i < n; i++) {
				PackedObjectInfo obj = getObject(i);
				long start = obj.getOffset();
				out.seek(start);
				ObjectTypeAndSize ots = readTypeAndSize(out);
				ObjectId baseObj = null;
				switch (ots.type) {
				case Constants.OBJ_OFS_DELTA:
					int c = out.readByte();
					long ofs = c & 0x7f;
					while ((c & 0x80) != 0) {
						ofs += 1;
						c = out.readByte();
						ofs <<= 7;
						ofs += (c & 0x7f);
					}
					final long base = start - ofs;
					baseObj = getBaseObject(base, i);
					break;

				case Constants.OBJ_REF_DELTA:
					out.read(baseObjArray);
					baseObj = ObjectId.fromRaw(baseObjArray);
					break;
				}
				Long end = objects.get(start);
				long packedSize = end - out.getFilePointer() - 1;
				InputStream is = Channels.newInputStream(out.getChannel());
				jdbcObjectInserter.insertObject(obj, ots, is,
						packedSize, baseObj);
			}
			return null;
		} finally {
			try {
				if (out != null && out.getChannel().isOpen())
					out.close();
			} catch (IOException closeError) {
				// Ignored. We want to delete the file.
			}
			if (tmpPack != null && !tmpPack.delete() && tmpPack.exists())
				tmpPack.deleteOnExit();
		}
	}

	private PackedObjectInfo getBaseObject(long base, int n) {
		for (int i = n; i >= 0; i--) {
			PackedObjectInfo obj = getObject(i);
			if (obj.getOffset() == base)
				return obj;
		}
		return null;
	}

	private ObjectTypeAndSize readTypeAndSize(RandomAccessFile in)
			throws IOException {
		ObjectTypeAndSize info = new ObjectTypeAndSize();
		int c = in.readByte();

		info.type = (c >> 4) & 7;
		long sz = c & 15;
		int shift = 4;
		while ((c & 0x80) != 0) {
			c = in.readByte();
			sz += ((long) (c & 0x7f)) << shift;
			shift += 7;
		}
		info.size = sz;
		return info;
	}

	@Override
	protected void onStoreStream(byte[] raw, int pos, int len)
			throws IOException {
		// copy received pack into tempoary pack file for random access
		out.write(raw, pos, len);
	}

	@Override
	protected void onObjectHeader(Source src, byte[] raw, int pos, int len)
			throws IOException {
	}

	@Override
	protected void onObjectData(Source src, byte[] raw, int pos, int len)
			throws IOException {
	}

	@Override
	protected void onInflatedObjectData(PackedObjectInfo obj, int typeCode,
			byte[] data) throws IOException {
	}

	@Override
	protected void onPackHeader(long objCnt) throws IOException {
	}

	@Override
	protected void onPackFooter(byte[] hash) throws IOException {
	}

	@Override
	protected void onBeginWholeObject(long streamPosition, int type,
			long inflatedSize) throws IOException {
		currentObjectStart = streamPosition;
	}

	@Override
	protected void onEndWholeObject(PackedObjectInfo info, long streamPosition) throws IOException {
		currentObjectEnd = streamPosition;
		objects.put(currentObjectStart, currentObjectEnd);
	}

	@Override
	protected void onBeginOfsDelta(long deltaStreamPosition,
			long baseStreamPosition, long inflatedSize) throws IOException {
		currentObjectStart = deltaStreamPosition;
	}

	@Override
	protected void onBeginRefDelta(long deltaStreamPosition, AnyObjectId baseId,
			long inflatedSize) throws IOException {
		currentObjectStart = deltaStreamPosition;
	}

	@Override
	protected UnresolvedDelta onEndDelta(long streamPosition) throws IOException {
		currentObjectEnd = streamPosition;
		objects.put(currentObjectStart, currentObjectEnd);
		return super.onEndDelta(streamPosition);
	}

	@Override
	protected boolean onAppendBase(int typeCode, byte[] data,
			PackedObjectInfo info) throws IOException {
		return false;
	}

	@Override
	protected void onEndThinPack() throws IOException {
	}

	@Override
	protected ObjectTypeAndSize seekDatabase(PackedObjectInfo obj,
			ObjectTypeAndSize info) throws IOException {
		out.seek(obj.getOffset());
		return readObjectHeader(info);
	}

	@Override
	protected ObjectTypeAndSize seekDatabase(UnresolvedDelta delta,
			ObjectTypeAndSize info) throws IOException {
		out.seek(delta.getOffset());
		return readObjectHeader(info);
	}

	@Override
	protected int readDatabase(byte[] dst, int pos, int cnt)
			throws IOException {
		return out.read(dst, pos, cnt);
	}

	@Override
	protected boolean checkCRC(int oldCRC) {
		return true;
	}
}

package org.eclipse.jgit.internal.storage.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.transport.PackParser;
import org.eclipse.jgit.transport.PackParser.ObjectTypeAndSize;
import org.eclipse.jgit.util.IO;

public class JdbcObjectInserter extends ObjectInserter {

	private JdbcObjectDatabase jdbcObjectDatabase;

	private Connection connection;
	private PreparedStatement psInsertObject;

	public JdbcObjectInserter(JdbcObjectDatabase jdbcObjectDatabase) {
		this.jdbcObjectDatabase = jdbcObjectDatabase;

		DataSource ds = jdbcObjectDatabase.getJdbcRepository().getDatasource();
		String sqlInsertObject = "insert into objects (id, object_type, object_size, content, base_id) values (?, ?, ?, ?, ?)"; //$NON-NLS-1$
		try {
			connection = ds.getConnection();
			connection.setAutoCommit(false);
			psInsertObject = connection.prepareStatement(sqlInsertObject);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ObjectId insert(int objectType, long length, InputStream in)
			throws IOException {

		MessageDigest md = digest();
		md.update(Constants.encodedTypeString(objectType));
		md.update((byte) ' ');
		md.update(Constants.encodeASCII(length));
		md.update((byte) 0);
		byte[] id = null; // md.digest();

		try {
			// psInsertObject.setBytes(1, id);
			// psInsertObject.setInt(2, objectType);
			// psInsertObject.setBinaryStream(3, in, length);
			psInsertObject.execute();
			return ObjectId.fromRaw(id);
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public PackParser newPackParser(InputStream in) throws IOException {
		return new JdbcPackParser(jdbcObjectDatabase, this, in);
	}

	@Override
	public ObjectReader newReader() {
		return new JdbcObjectReader(jdbcObjectDatabase);
	}

	@Override
	public void flush() throws IOException {
		try {
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void insertObject(ObjectId obj, ObjectTypeAndSize ots,
			InputStream in, long packedSize, ObjectId baseObj)
			throws IOException {

		byte[] id = new byte[Constants.OBJECT_ID_LENGTH];
		byte[] baseId = null;
		if (baseObj != null) {
			baseId = new byte[Constants.OBJECT_ID_LENGTH];
			baseObj.copyRawTo(baseId, 0);
		}
		obj.copyRawTo(id, 0);
		try {
			psInsertObject.setBytes(1, id);
			psInsertObject.setInt(2, ots.type);
			psInsertObject.setLong(3, ots.size);
			psInsertObject.setBinaryStream(4, in, packedSize);
			psInsertObject.setBytes(5, baseId);
			psInsertObject.execute();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}
}

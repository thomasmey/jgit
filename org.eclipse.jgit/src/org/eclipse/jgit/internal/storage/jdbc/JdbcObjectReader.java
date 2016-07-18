package org.eclipse.jgit.internal.storage.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.sql.DataSource;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;

public class JdbcObjectReader extends ObjectReader {

	private JdbcObjectDatabase jdbcObjectDatabase;

	private Connection connection;
	private PreparedStatement psSelectObject;

	public JdbcObjectReader(JdbcObjectDatabase jdbcObjectDatabase) {
		this.jdbcObjectDatabase = jdbcObjectDatabase;

		DataSource ds = jdbcObjectDatabase.getJdbcRepository().getDatasource();
		String sqlSelectObject = "select id, object_type, object_size, content from objects where id = ?"; //$NON-NLS-1$
		try {
			connection = ds.getConnection();
			psSelectObject = connection.prepareStatement(sqlSelectObject);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ObjectReader newReader() {
		return new JdbcObjectReader(jdbcObjectDatabase);
	}

	@Override
	public Collection<ObjectId> resolve(AbbreviatedObjectId id)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ObjectLoader open(AnyObjectId objectId, int typeHint)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		byte[] id = new byte[Constants.OBJECT_ID_LENGTH];
		objectId.copyRawTo(id, 0);

		try {
			psSelectObject.setBytes(1, id);
			ResultSet r = psSelectObject.executeQuery();
			if(r.next()) {
				return new ObjectLoader.SmallObject(r.getInt(2), r.getBytes(4));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (typeHint == OBJ_ANY)
			throw new MissingObjectException(objectId.copy(),
					JGitText.get().unknownObjectType2);
		throw new MissingObjectException(objectId.copy(), typeHint);
	}

	@Override
	public Set<ObjectId> getShallowCommits() throws IOException {
		// FIXME:
		return Collections.emptySet();
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
}

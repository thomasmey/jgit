package org.eclipse.jgit.internal.storage.jdbc;

import org.eclipse.jgit.lib.ObjectDatabase;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.ObjectReader;

/**
 * Access to individual git objects
 *
 * @author thomas
 *
 */
public class JdbcObjectDatabase extends ObjectDatabase {

	private JdbcRepository jdbcRepository;

	public JdbcObjectDatabase(JdbcRepository jdbcRepository) {
		this.jdbcRepository = jdbcRepository;
	}

	@Override
	public ObjectInserter newInserter() {
		return new JdbcObjectInserter(this);
	}

	@Override
	public ObjectReader newReader() {
		return new JdbcObjectReader(this);
	}

	@Override
	public void close() {
		throw new UnsupportedOperationException();
	}

	public JdbcRepository getJdbcRepository() {
		return jdbcRepository;
	}

}

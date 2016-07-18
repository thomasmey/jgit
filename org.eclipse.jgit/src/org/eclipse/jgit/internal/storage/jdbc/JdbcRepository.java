package org.eclipse.jgit.internal.storage.jdbc;

import java.io.IOException;

import javax.sql.DataSource;

import org.eclipse.jgit.attributes.AttributesNodeProvider;
import org.eclipse.jgit.lib.BaseRepositoryBuilder;
import org.eclipse.jgit.lib.ObjectDatabase;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.lib.ReflogReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;

/**
 * @author thomas A Repository that stores all objects in a database
 */
public class JdbcRepository extends Repository {

	private DataSource datasource;

	private JdbcStoredConfig repoConfig;

	@Override
	public boolean isBare() {
		return true;
	}

	/**
	 * @param datasource
	 */
	public JdbcRepository(
			JdbcRepositoryBuilder repoBuilder) {
		super(repoBuilder);
		this.datasource = repoBuilder.getDataSource();
		this.repoConfig = new JdbcStoredConfig(this);
	}

	@Override
	public void create(boolean bare) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ObjectDatabase getObjectDatabase() {
		return new JdbcObjectDatabase(this);
	}

	@Override
	public RefDatabase getRefDatabase() {
		return new JdbcRefDatabase(this);
	}

	@Override
	public StoredConfig getConfig() {
		return repoConfig;
	}

	@Override
	public AttributesNodeProvider createAttributesNodeProvider() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void scanForRepoChanges() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void notifyIndexChanged() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ReflogReader getReflogReader(String refName) throws IOException {
		return new JdbcReflogReader(refName);
	}

	public DataSource getDatasource() {
		return datasource;
	}

}

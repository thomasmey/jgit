package org.eclipse.jgit.internal.storage.jdbc;

import javax.sql.DataSource;

import org.eclipse.jgit.lib.BaseRepositoryBuilder;

public class JdbcRepositoryBuilder extends BaseRepositoryBuilder<JdbcRepositoryBuilder, JdbcRepository> {

	private DataSource dataSource;

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

}

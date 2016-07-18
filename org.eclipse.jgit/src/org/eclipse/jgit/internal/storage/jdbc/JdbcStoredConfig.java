package org.eclipse.jgit.internal.storage.jdbc;

import java.io.IOException;

import javax.sql.DataSource;

import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.StoredConfig;

/**
 * structure is: section, subsection, name -> value
 *
 * @author thomas
 *
 */
public class JdbcStoredConfig extends StoredConfig {

	private JdbcRepository jdbcRepository;

	public JdbcStoredConfig(JdbcRepository jdbcRepository) {
		this.jdbcRepository = jdbcRepository;
	}

	@Override
	public void load() throws IOException, ConfigInvalidException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void save() throws IOException {
		for (String sec : super.getSections()) {
			for (String subSec : super.getSubsections(sec)) {
				for (String name : super.getNames(sec, subSec)) {
					System.out.println("sec=" + sec + " subSec=" + subSec
							+ " name=" + name);
				}
			}
		}
	}
}

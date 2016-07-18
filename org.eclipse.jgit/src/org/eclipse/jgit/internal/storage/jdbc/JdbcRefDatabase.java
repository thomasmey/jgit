package org.eclipse.jgit.internal.storage.jdbc;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.lib.RefRename;
import org.eclipse.jgit.lib.RefUpdate;

/**
 * All refs in this git repo
 *
 * @author thomas
 *
 */
public class JdbcRefDatabase extends RefDatabase {

	private JdbcRepository jdbcRepository;

	public JdbcRefDatabase(JdbcRepository jdbcRepository) {
		this.jdbcRepository = jdbcRepository;
	}

	@Override
	public void create() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isNameConflicting(String name) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public RefUpdate newUpdate(String name, boolean detach) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public RefRename newRename(String fromName, String toName)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Ref getRef(String name) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Ref> getRefs(String prefix) throws IOException {
		// throw new UnsupportedOperationException();
		return Collections.emptyMap();
	}

	@Override
	public List<Ref> getAdditionalRefs() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Ref peel(Ref ref) throws IOException {
		throw new UnsupportedOperationException();
	}
}

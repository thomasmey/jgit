package org.eclipse.jgit.internal.storage.jdbc;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.lib.ReflogEntry;
import org.eclipse.jgit.lib.ReflogReader;

public class JdbcReflogReader implements ReflogReader {

	public JdbcReflogReader(String refName) {
	}

	@Override
	public ReflogEntry getLastEntry() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ReflogEntry> getReverseEntries() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ReflogEntry getReverseEntry(int number) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ReflogEntry> getReverseEntries(int max) throws IOException {
		throw new UnsupportedOperationException();
	}
}

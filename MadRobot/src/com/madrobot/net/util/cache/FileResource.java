package com.madrobot.net.util.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.madrobot.net.util.cache.annotation.ThreadSafe;

/**
 * Cache resource backed by a file.
 * 
 * @since 4.1
 */
@ThreadSafe
public class FileResource implements Resource {

	private static final long serialVersionUID = 4132244415919043397L;

	private final File file;

	private volatile boolean disposed;

	public FileResource(final File file) {
		super();
		this.file = file;
		this.disposed = false;
	}

	synchronized File getFile() {
		return this.file;
	}

	@Override
	public synchronized InputStream getInputStream() throws IOException {
		return new FileInputStream(this.file);
	}

	@Override
	public synchronized long length() {
		return this.file.length();
	}

	@Override
	public synchronized void dispose() {
		if (this.disposed) {
			return;
		}
		this.disposed = true;
		this.file.delete();
	}

}

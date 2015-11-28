package com.madrobot.net.util.cache;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.madrobot.net.util.cache.annotation.Immutable;

/**
 * Cache resource backed by a byte array on the heap.
 * 
 * @since 4.1
 */
@Immutable
public class HeapResource implements Resource {

	private static final long serialVersionUID = -2078599905620463394L;

	private final byte[] b;

	public HeapResource(final byte[] b) {
		super();
		this.b = b;
	}

	byte[] getByteArray() {
		return this.b;
	}

	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(this.b);
	}

	@Override
	public long length() {
		return this.b.length;
	}

	@Override
	public void dispose() {
	}

}

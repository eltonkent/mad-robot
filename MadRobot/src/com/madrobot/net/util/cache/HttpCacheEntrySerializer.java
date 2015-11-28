package com.madrobot.net.util.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Used by some {@link HttpCacheStorage} implementations to serialize
 * {@link HttpCacheEntry} instances to a byte representation before storage.
 */
public interface HttpCacheEntrySerializer {

	/**
	 * Serializes the given entry to a byte representation on the given
	 * {@link OutputStream}.
	 * 
	 * @throws IOException
	 */
	void writeTo(HttpCacheEntry entry, OutputStream os) throws IOException;

	/**
	 * Deserializes a byte representation of a cache entry by reading from the
	 * given {@link InputStream}.
	 * 
	 * @throws IOException
	 */
	HttpCacheEntry readFrom(InputStream is) throws IOException;

}

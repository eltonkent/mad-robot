package com.madrobot.net.util.cache;

import java.io.IOException;

/**
 * New storage backends should implement this {@link HttpCacheStorage}
 * interface. They can then be plugged into the existing
 * {@link com.madrobot.net.util.cache.CachingHttpClient} implementation.
 * 
 */
public interface HttpCacheStorage {

	/**
	 * Store a given cache entry under the given key.
	 * 
	 * @param key
	 *            where in the cache to store the entry
	 * @param entry
	 *            cached response to store
	 * @throws IOException
	 */
	void putEntry(String key, HttpCacheEntry entry) throws IOException;

	/**
	 * Retrieves the cache entry stored under the given key or null if no entry
	 * exists under that key.
	 * 
	 * @param key
	 *            cache key
	 * @return an {@link HttpCacheEntry} or {@code null} if no entry exists
	 * @throws IOException
	 */
	HttpCacheEntry getEntry(String key) throws IOException;

	/**
	 * Deletes/invalidates/removes any cache entries currently stored under the
	 * given key.
	 * 
	 * @param key
	 * @throws IOException
	 */
	void removeEntry(String key) throws IOException;

	/**
	 * Atomically applies the given callback to update an existing cache entry
	 * under a given key.
	 * 
	 * @param key
	 *            indicates which entry to modify
	 * @param callback
	 *            performs the update; see {@link HttpCacheUpdateCallback} for
	 *            details, but roughly the callback expects to be handed the
	 *            current entry and will return the new value for the entry.
	 * @throws IOException
	 * @throws HttpCacheUpdateException
	 */
	void updateEntry(String key, HttpCacheUpdateCallback callback) throws IOException,
			HttpCacheUpdateException;

}

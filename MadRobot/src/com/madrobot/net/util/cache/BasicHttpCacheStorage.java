package com.madrobot.net.util.cache;

import java.io.IOException;
import java.util.LinkedHashMap;

import com.madrobot.net.util.cache.annotation.ThreadSafe;

/**
 * Basic {@link HttpCacheStorage} implementation backed by an instance of
 * {@link LinkedHashMap}. In other words, cache entries and the cached response
 * bodies are held in-memory. This cache does NOT deallocate resources
 * associated with the cache entries; it is intended for use with
 * {@link HeapResource} and similar. This is the default cache storage backend
 * used by {@link CachingHttpClient}.
 * 
 * @since 4.1
 */
@ThreadSafe
public class BasicHttpCacheStorage implements HttpCacheStorage {

	private final CacheMap entries;

	public BasicHttpCacheStorage(CacheConfig config) {
		super();
		this.entries = new CacheMap(config.getMaxCacheEntries());
	}

	/**
	 * Places a HttpCacheEntry in the cache
	 * 
	 * @param url
	 *            Url to use as the cache key
	 * @param entry
	 *            HttpCacheEntry to place in the cache
	 */
	@Override
	public synchronized void putEntry(String url, HttpCacheEntry entry) throws IOException {
		entries.put(url, entry);
	}

	/**
	 * Gets an entry from the cache, if it exists
	 * 
	 * @param url
	 *            Url that is the cache key
	 * @return HttpCacheEntry if one exists, or null for cache miss
	 */
	@Override
	public synchronized HttpCacheEntry getEntry(String url) throws IOException {
		return entries.get(url);
	}

	/**
	 * Removes a HttpCacheEntry from the cache
	 * 
	 * @param url
	 *            Url that is the cache key
	 */
	@Override
	public synchronized void removeEntry(String url) throws IOException {
		entries.remove(url);
	}

	@Override
	public synchronized void updateEntry(String url, HttpCacheUpdateCallback callback)
			throws IOException {
		HttpCacheEntry existingEntry = entries.get(url);
		entries.put(url, callback.update(existingEntry));
	}

}

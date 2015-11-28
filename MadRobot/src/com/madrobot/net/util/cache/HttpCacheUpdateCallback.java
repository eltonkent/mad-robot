package com.madrobot.net.util.cache;

import java.io.IOException;

/**
 * Used for atomically updating entries in a {@link HttpCacheStorage}
 * implementation. The current entry (if any) is fed into an implementation of
 * this interface, and the new, possibly updated entry (if any) should be
 * returned.
 */
public interface HttpCacheUpdateCallback {

	/**
	 * Returns the new cache entry that should replace an existing one.
	 * 
	 * @param existing
	 *            the cache entry currently in-place in the cache, possibly
	 *            <code>null</code> if nonexistent
	 * @return the cache entry that should replace it, again, possibly
	 *         <code>null</code> if the entry should be deleted
	 * 
	 * @since 4.1
	 */
	HttpCacheEntry update(HttpCacheEntry existing) throws IOException;

}

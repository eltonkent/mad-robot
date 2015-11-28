package com.madrobot.net.util.cache;

import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.HashSet;
import java.util.Set;

import com.madrobot.net.util.cache.annotation.ThreadSafe;

/**
 * {@link HttpCacheStorage} implementation capable of deallocating resources
 * associated with the cache entries. This cache keeps track of cache entries
 * using {@link PhantomReference} and maintains a collection of all resources
 * that are no longer in use. The cache, however, does not automatically
 * deallocates associated resources by invoking {@link Resource#dispose()}
 * method. The consumer MUST periodically call {@link #cleanResources()} method
 * to trigger resource deallocation. The cache can be permanently shut down
 * using {@link #shutdown()} method. All resources associated with the entries
 * used by the cache will be deallocated.
 * 
 * This {@link HttpCacheStorage} implementation is intended for use with
 * {@link FileResource} and similar.
 * 
 */
@ThreadSafe
public class ManagedHttpCacheStorage implements HttpCacheStorage {

	private final CacheMap entries;
	private final ReferenceQueue<HttpCacheEntry> morque;
	private final Set<ResourceReference> resources;

	private volatile boolean shutdown;

	public ManagedHttpCacheStorage(final CacheConfig config) {
		super();
		this.entries = new CacheMap(config.getMaxCacheEntries());
		this.morque = new ReferenceQueue<HttpCacheEntry>();
		this.resources = new HashSet<ResourceReference>();
	}

	private void ensureValidState() throws IllegalStateException {
		if (this.shutdown) {
			throw new IllegalStateException("Cache has been shut down");
		}
	}

	private void keepResourceReference(final HttpCacheEntry entry) {
		Resource resource = entry.getResource();
		if (resource != null) {
			// Must deallocate the resource when the entry is no longer in used
			ResourceReference ref = new ResourceReference(entry, this.morque);
			this.resources.add(ref);
		}
	}

	@Override
	public void putEntry(final String url, final HttpCacheEntry entry) throws IOException {
		if (url == null) {
			throw new IllegalArgumentException("URL may not be null");
		}
		if (entry == null) {
			throw new IllegalArgumentException("Cache entry may not be null");
		}
		ensureValidState();
		synchronized (this) {
			this.entries.put(url, entry);
			keepResourceReference(entry);
		}
	}

	@Override
	public HttpCacheEntry getEntry(final String url) throws IOException {
		if (url == null) {
			throw new IllegalArgumentException("URL may not be null");
		}
		ensureValidState();
		synchronized (this) {
			return this.entries.get(url);
		}
	}

	@Override
	public void removeEntry(String url) throws IOException {
		if (url == null) {
			throw new IllegalArgumentException("URL may not be null");
		}
		ensureValidState();
		synchronized (this) {
			// Cannot deallocate the associated resources immediately as the
			// cache entry may still be in use
			this.entries.remove(url);
		}
	}

	@Override
	public void updateEntry(final String url, final HttpCacheUpdateCallback callback)
			throws IOException {
		if (url == null) {
			throw new IllegalArgumentException("URL may not be null");
		}
		if (callback == null) {
			throw new IllegalArgumentException("Callback may not be null");
		}
		ensureValidState();
		synchronized (this) {
			HttpCacheEntry existing = this.entries.get(url);
			HttpCacheEntry updated = callback.update(existing);
			this.entries.put(url, updated);
			if (existing != updated) {
				keepResourceReference(updated);
			}
		}
	}

	public void cleanResources() {
		if (this.shutdown) {
			return;
		}
		ResourceReference ref;
		while ((ref = (ResourceReference) this.morque.poll()) != null) {
			synchronized (this) {
				this.resources.remove(ref);
			}
			ref.getResource().dispose();
		}
	}

	public void shutdown() {
		if (this.shutdown) {
			return;
		}
		this.shutdown = true;
		synchronized (this) {
			this.entries.clear();
			for (ResourceReference ref : this.resources) {
				ref.getResource().dispose();
			}
			this.resources.clear();
			while (this.morque.poll() != null) {
			}
		}
	}

}

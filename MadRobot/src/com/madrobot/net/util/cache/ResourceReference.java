package com.madrobot.net.util.cache;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

import com.madrobot.net.util.cache.annotation.Immutable;

@Immutable
class ResourceReference extends PhantomReference<HttpCacheEntry> {

	private final Resource resource;

	public ResourceReference(final HttpCacheEntry entry, final ReferenceQueue<HttpCacheEntry> q) {
		super(entry, q);
		if (entry.getResource() == null) {
			throw new IllegalArgumentException("Resource may not be null");
		}
		this.resource = entry.getResource();
	}

	public Resource getResource() {
		return this.resource;
	}

	@Override
	public int hashCode() {
		return this.resource.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return this.resource.equals(obj);
	}

}

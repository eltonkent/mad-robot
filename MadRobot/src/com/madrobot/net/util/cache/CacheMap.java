package com.madrobot.net.util.cache;

import java.util.LinkedHashMap;
import java.util.Map;

final class CacheMap extends LinkedHashMap<String, HttpCacheEntry> {

	private static final long serialVersionUID = -7750025207539768511L;

	private final int maxEntries;

	CacheMap(int maxEntries) {
		super(20, 0.75f, true);
		this.maxEntries = maxEntries;
	}

	@Override
	protected boolean removeEldestEntry(final Map.Entry<String, HttpCacheEntry> eldest) {
		return size() > this.maxEntries;
	}

}

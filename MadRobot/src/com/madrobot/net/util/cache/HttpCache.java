package com.madrobot.net.util.cache;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

/**
 * @since 4.1
 */
interface HttpCache {

	/**
	 * Clear all matching {@link HttpCacheEntry}s.
	 * 
	 * @param host
	 * @param request
	 * @throws IOException
	 */
	void flushCacheEntriesFor(HttpHost host, HttpRequest request) throws IOException;

	/**
	 * Clear invalidated matching {@link HttpCacheEntry}s
	 * 
	 * @param host
	 * @param request
	 * @throws IOException
	 */
	void flushInvalidatedCacheEntriesFor(HttpHost host, HttpRequest request)
			throws IOException;

	/**
	 * Clear any entries that may be invalidated by the given response to a
	 * particular request.
	 * 
	 * @param host
	 * @param request
	 * @param response
	 */
	void flushInvalidatedCacheEntriesFor(HttpHost host, HttpRequest request,
			HttpResponse response);

	/**
	 * Retrieve matching {@link HttpCacheEntry} from the cache if it exists
	 * 
	 * @param host
	 * @param request
	 * @return the matching {@link HttpCacheEntry} or {@code null}
	 * @throws IOException
	 */
	HttpCacheEntry getCacheEntry(HttpHost host, HttpRequest request) throws IOException;

	/**
	 * Retrieve all variants from the cache, if there are no variants then an
	 * empty {@link Map} is returned
	 * 
	 * @param host
	 * @param request
	 * @return a <code>Map</code> mapping Etags to variant cache entries
	 * @throws IOException
	 */
	Map<String, Variant> getVariantCacheEntriesWithEtags(HttpHost host, HttpRequest request)
			throws IOException;

	/**
	 * Store a {@link HttpResponse} in the cache if possible, and return
	 * 
	 * @param host
	 * @param request
	 * @param originResponse
	 * @param requestSent
	 * @param responseReceived
	 * @return the {@link HttpResponse}
	 * @throws IOException
	 */
	HttpResponse cacheAndReturnResponse(HttpHost host, HttpRequest request,
			HttpResponse originResponse, Date requestSent, Date responseReceived)
			throws IOException;

	/**
	 * Update a {@link HttpCacheEntry} using a 304 {@link HttpResponse}.
	 * 
	 * @param target
	 * @param request
	 * @param stale
	 * @param originResponse
	 * @param requestSent
	 * @param responseReceived
	 * @return the updated {@link HttpCacheEntry}
	 * @throws IOException
	 */
	HttpCacheEntry updateCacheEntry(HttpHost target, HttpRequest request,
			HttpCacheEntry stale, HttpResponse originResponse, Date requestSent,
			Date responseReceived) throws IOException;

	/**
	 * Update a specific {@link HttpCacheEntry} representing a cached variant
	 * using a 304 {@link HttpResponse}.
	 * 
	 * @param target
	 *            host for client request
	 * @param request
	 *            actual request from upstream client
	 * @param stale
	 *            current variant cache entry
	 * @param originResponse
	 *            304 response received from origin
	 * @param requestSent
	 *            when the validating request was sent
	 * @param responseReceived
	 *            when the validating response was received
	 * @param cacheKey
	 *            where in the cache this entry is currently stored
	 * @return the updated {@link HttpCacheEntry}
	 * @throws IOException
	 */
	HttpCacheEntry updateVariantCacheEntry(HttpHost target, HttpRequest request,
			HttpCacheEntry stale, HttpResponse originResponse, Date requestSent,
			Date responseReceived, String cacheKey) throws IOException;

	/**
	 * Specifies cache should reuse the given cached variant to satisfy requests
	 * whose varying headers match those of the given client request.
	 * 
	 * @param target
	 *            host of the upstream client request
	 * @param req
	 *            request sent by upstream client
	 * @param variant
	 *            variant cache entry to reuse
	 * @throws IOException
	 *             may be thrown during cache update
	 */
	void reuseVariantEntryFor(HttpHost target, final HttpRequest req, final Variant variant)
			throws IOException;
}

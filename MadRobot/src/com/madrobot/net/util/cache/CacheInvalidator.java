package com.madrobot.net.util.cache;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.protocol.HTTP;

import android.util.Log;

import com.madrobot.net.HttpConstants;
import com.madrobot.net.HttpConstants.HttpMethod;
import com.madrobot.net.util.cache.annotation.ThreadSafe;

/**
 * Given a particular HttpRequest, flush any cache entries that this request
 * would invalidate.
 * 
 * @since 4.1
 */
@ThreadSafe
// so long as the cache implementation is thread-safe
class CacheInvalidator {

	private final HttpCacheStorage storage;
	private final CacheKeyGenerator cacheKeyGenerator;

	/**
	 * Create a new {@link CacheInvalidator} for a given {@link HttpCache} and
	 * {@link CacheKeyGenerator}.
	 * 
	 * @param uriExtractor
	 *            Provides identifiers for the keys to store cache entries
	 * @param storage
	 *            the cache to store items away in
	 */
	public CacheInvalidator(final CacheKeyGenerator uriExtractor,
			final HttpCacheStorage storage) {
		this.cacheKeyGenerator = uriExtractor;
		this.storage = storage;
	}

	/**
	 * Remove cache entries from the cache that are no longer fresh or have been
	 * invalidated in some way.
	 * 
	 * @param host
	 *            The backend host we are talking to
	 * @param req
	 *            The HttpRequest to that host
	 */
	public void flushInvalidatedCacheEntries(HttpHost host, HttpRequest req) {
		if (requestShouldNotBeCached(req)) {
			Log.d("MadRobot", "Request should not be cached");

			String theUri = cacheKeyGenerator.getURI(host, req);

			HttpCacheEntry parent = getEntry(theUri);

			Log.d("MadRobot", "parent entry: " + parent);

			if (parent != null) {
				for (String variantURI : parent.getVariantMap().values()) {
					flushEntry(variantURI);
				}
				flushEntry(theUri);
			}
			URL reqURL = getAbsoluteURL(theUri);
			if (reqURL == null) {
				Log.d("MadRobot", "Couldn't transform request into valid URL");
				return;
			}
			Header clHdr = req.getFirstHeader("Content-Location");
			if (clHdr != null) {
				String contentLocation = clHdr.getValue();
				if (!flushAbsoluteUriFromSameHost(reqURL, contentLocation)) {
					flushRelativeUriFromSameHost(reqURL, contentLocation);
				}
			}
			Header lHdr = req.getFirstHeader("Location");
			if (lHdr != null) {
				flushAbsoluteUriFromSameHost(reqURL, lHdr.getValue());
			}
		}
	}

	private void flushEntry(String uri) {
		try {
			storage.removeEntry(uri);
		} catch (IOException ioe) {
			Log.d("MadRobot", "unable to flush cache entry", ioe);
		}
	}

	private HttpCacheEntry getEntry(String theUri) {
		try {
			return storage.getEntry(theUri);
		} catch (IOException ioe) {
			Log.d("MadRobot", "could not retrieve entry from storage", ioe);
		}
		return null;
	}

	protected void flushUriIfSameHost(URL requestURL, URL targetURL) {
		URL canonicalTarget = getAbsoluteURL(cacheKeyGenerator.canonicalizeUri(targetURL
				.toString()));
		if (canonicalTarget == null)
			return;
		if (canonicalTarget.getAuthority().equalsIgnoreCase(requestURL.getAuthority())) {
			flushEntry(canonicalTarget.toString());
		}
	}

	protected void flushRelativeUriFromSameHost(URL reqURL, String relUri) {
		URL relURL = getRelativeURL(reqURL, relUri);
		if (relURL == null)
			return;
		flushUriIfSameHost(reqURL, relURL);
	}

	protected boolean flushAbsoluteUriFromSameHost(URL reqURL, String uri) {
		URL absURL = getAbsoluteURL(uri);
		if (absURL == null)
			return false;
		flushUriIfSameHost(reqURL, absURL);
		return true;
	}

	private URL getAbsoluteURL(String uri) {
		URL absURL = null;
		try {
			absURL = new URL(uri);
		} catch (MalformedURLException mue) {
			// nop
		}
		return absURL;
	}

	private URL getRelativeURL(URL reqURL, String relUri) {
		URL relURL = null;
		try {
			relURL = new URL(reqURL, relUri);
		} catch (MalformedURLException e) {
			// nop
		}
		return relURL;
	}

	protected boolean requestShouldNotBeCached(HttpRequest req) {
		String method = req.getRequestLine().getMethod();
		return notGetOrHeadRequest(method);
	}

	private boolean notGetOrHeadRequest(String method) {
		return !(HttpMethod.GET.equals(method) || HttpMethod.HEAD.equals(method));
	}

	/**
	 * Flushes entries that were invalidated by the given response received for
	 * the given host/request pair.
	 */
	public void flushInvalidatedCacheEntries(HttpHost host, HttpRequest request,
			HttpResponse response) {
		int status = response.getStatusLine().getStatusCode();
		if (status < 200 || status > 299)
			return;
		URL reqURL = getAbsoluteURL(cacheKeyGenerator.getURI(host, request));
		if (reqURL == null)
			return;
		URL canonURL = getContentLocationURL(reqURL, response);
		if (canonURL == null)
			return;
		String cacheKey = cacheKeyGenerator.canonicalizeUri(canonURL.toString());
		HttpCacheEntry entry = getEntry(cacheKey);
		if (entry == null)
			return;

		if (!responseDateNewerThanEntryDate(response, entry))
			return;
		if (!responseAndEntryEtagsDiffer(response, entry))
			return;

		flushUriIfSameHost(reqURL, canonURL);
	}

	private URL getContentLocationURL(URL reqURL, HttpResponse response) {
		Header clHeader = response.getFirstHeader("Content-Location");
		if (clHeader == null)
			return null;
		String contentLocation = clHeader.getValue();
		URL canonURL = getAbsoluteURL(contentLocation);
		if (canonURL != null)
			return canonURL;
		return getRelativeURL(reqURL, contentLocation);
	}

	private boolean responseAndEntryEtagsDiffer(HttpResponse response, HttpCacheEntry entry) {
		Header entryEtag = entry.getFirstHeader(HttpConstants.ETAG);
		Header responseEtag = response.getFirstHeader(HttpConstants.ETAG);
		if (entryEtag == null || responseEtag == null)
			return false;
		return (!entryEtag.getValue().equals(responseEtag.getValue()));
	}

	private boolean responseDateNewerThanEntryDate(HttpResponse response, HttpCacheEntry entry) {
		Header entryDateHeader = entry.getFirstHeader(HTTP.DATE_HEADER);
		Header responseDateHeader = response.getFirstHeader(HTTP.DATE_HEADER);
		if (entryDateHeader == null || responseDateHeader == null) {
			return false;
		}
		try {
			Date entryDate = DateUtils.parseDate(entryDateHeader.getValue());
			Date responseDate = DateUtils.parseDate(responseDateHeader.getValue());
			return responseDate.after(entryDate);
		} catch (DateParseException e) {
			return false;
		}
	}
}

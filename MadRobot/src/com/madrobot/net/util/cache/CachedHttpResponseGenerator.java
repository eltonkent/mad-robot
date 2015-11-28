package com.madrobot.net.util.cache;

import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HTTP;

import com.madrobot.net.HttpConstants;
import com.madrobot.net.util.cache.annotation.Immutable;

/**
 * Rebuilds an {@link HttpResponse} from a {@link CacheEntry}
 * 
 * @since 4.1
 */
@Immutable
class CachedHttpResponseGenerator {

	private final CacheValidityPolicy validityStrategy;

	CachedHttpResponseGenerator(final CacheValidityPolicy validityStrategy) {
		super();
		this.validityStrategy = validityStrategy;
	}

	CachedHttpResponseGenerator() {
		this(new CacheValidityPolicy());
	}

	/**
	 * If I was able to use a {@link CacheEntity} to response to the
	 * {@link org.apache.http.HttpRequest} then generate an {@link HttpResponse}
	 * based on the cache entry.
	 * 
	 * @param entry
	 *            {@link CacheEntity} to transform into an {@link HttpResponse}
	 * @return {@link HttpResponse} that was constructed
	 */
	HttpResponse generateResponse(HttpCacheEntry entry) {

		Date now = new Date();
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
				entry.getStatusCode(), entry.getReasonPhrase());

		HttpEntity entity = new CacheEntity(entry);
		response.setHeaders(entry.getAllHeaders());
		addMissingContentLengthHeader(response, entity);
		response.setEntity(entity);

		long age = this.validityStrategy.getCurrentAgeSecs(entry, now);
		if (age > 0) {
			if (age >= Integer.MAX_VALUE) {
				response.setHeader(HttpConstants.AGE, "2147483648");
			} else {
				response.setHeader(HttpConstants.AGE, "" + ((int) age));
			}
		}

		return response;
	}

	/**
	 * Generate a 304 - Not Modified response from a {@link CacheEntity}. This
	 * should be used to respond to conditional requests, when the entry exists
	 * or has been re-validated.
	 * 
	 * @param entry
	 * @return
	 */
	HttpResponse generateNotModifiedResponse(HttpCacheEntry entry) {

		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
				HttpStatus.SC_NOT_MODIFIED, "Not Modified");

		// The response MUST include the following headers
		// (http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html)

		// - Date, unless its omission is required by section 14.8.1
		Header dateHeader = entry.getFirstHeader(HTTP.DATE_HEADER);
		if (dateHeader == null) {
			dateHeader = new BasicHeader(HTTP.DATE_HEADER, DateUtils.formatDate(new Date()));
		}
		response.addHeader(dateHeader);

		// - ETag and/or Content-Location, if the header would have been sent
		// in a 200 response to the same request
		Header etagHeader = entry.getFirstHeader(HttpConstants.ETAG);
		if (etagHeader != null) {
			response.addHeader(etagHeader);
		}

		Header contentLocationHeader = entry.getFirstHeader("Content-Location");
		if (contentLocationHeader != null) {
			response.addHeader(contentLocationHeader);
		}

		// - Expires, Cache-Control, and/or Vary, if the field-value might
		// differ from that sent in any previous response for the same
		// variant
		Header expiresHeader = entry.getFirstHeader(HttpConstants.EXPIRES);
		if (expiresHeader != null) {
			response.addHeader(expiresHeader);
		}

		Header cacheControlHeader = entry.getFirstHeader(HttpConstants.CACHE_CONTROL);
		if (cacheControlHeader != null) {
			response.addHeader(cacheControlHeader);
		}

		Header varyHeader = entry.getFirstHeader(HttpConstants.VARY);
		if (varyHeader != null) {
			response.addHeader(varyHeader);
		}

		return response;
	}

	private void addMissingContentLengthHeader(HttpResponse response, HttpEntity entity) {
		if (transferEncodingIsPresent(response))
			return;

		Header contentLength = response.getFirstHeader(HTTP.CONTENT_LEN);
		if (contentLength == null) {
			contentLength = new BasicHeader(HTTP.CONTENT_LEN, Long.toString(entity
					.getContentLength()));
			response.setHeader(contentLength);
		}
	}

	private boolean transferEncodingIsPresent(HttpResponse response) {
		Header hdr = response.getFirstHeader(HTTP.TRANSFER_ENCODING);
		return hdr != null;
	}
}

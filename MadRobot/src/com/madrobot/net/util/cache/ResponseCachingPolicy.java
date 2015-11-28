package com.madrobot.net.util.cache;

import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.protocol.HTTP;

import android.util.Log;

import com.madrobot.net.HttpConstants;
import com.madrobot.net.HttpConstants.HttpMethod;
import com.madrobot.net.util.cache.annotation.Immutable;

/**
 * Determines if an HttpResponse can be cached.
 * 
 */
@Immutable
class ResponseCachingPolicy {

	private final long maxObjectSizeBytes;
	private final boolean sharedCache;

	/**
	 * Define a cache policy that limits the size of things that should be
	 * stored in the cache to a maximum of {@link HttpResponse} bytes in size.
	 * 
	 * @param maxObjectSizeBytes
	 *            the size to limit items into the cache
	 * @param sharedCache
	 *            whether to behave as a shared cache (true) or a
	 *            non-shared/private cache (false)
	 */
	public ResponseCachingPolicy(long maxObjectSizeBytes, boolean sharedCache) {
		this.maxObjectSizeBytes = maxObjectSizeBytes;
		this.sharedCache = sharedCache;
	}

	/**
	 * Determines if an HttpResponse can be cached.
	 * 
	 * @param httpMethod
	 *            What type of request was this, a GET, PUT, other?
	 * @param response
	 *            The origin response
	 * @return <code>true</code> if response is cacheable
	 */
	public boolean isResponseCacheable(String httpMethod, HttpResponse response) {
		boolean cacheable = false;

		if (!HttpMethod.GET.equals(httpMethod)) {
			Log.d("MadRobot", "Response was not cacheable.");
			return false;
		}

		switch (response.getStatusLine().getStatusCode()) {
		case HttpStatus.SC_OK:
		case HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION:
		case HttpStatus.SC_MULTIPLE_CHOICES:
		case HttpStatus.SC_MOVED_PERMANENTLY:
		case HttpStatus.SC_GONE:
			// these response codes MAY be cached
			cacheable = true;
			Log.d("MadRobot", "Response was cacheable");
			break;
		case HttpStatus.SC_PARTIAL_CONTENT:
			// we don't implement Range requests and hence are not
			// allowed to cache partial content
			Log.d("MadRobot", "Response was not cacheable (Partial Content)");
			return cacheable;

		default:
			// If the status code is not one of the recognized
			// available codes in HttpStatus Don't Cache
			Log.d("MadRobot", "Response was not cacheable (Unknown Status code)");
			return cacheable;
		}

		Header contentLength = response.getFirstHeader(HTTP.CONTENT_LEN);
		if (contentLength != null) {
			int contentLengthValue = Integer.parseInt(contentLength.getValue());
			if (contentLengthValue > this.maxObjectSizeBytes)
				return false;
		}

		Header[] ageHeaders = response.getHeaders(HttpConstants.AGE);

		if (ageHeaders.length > 1)
			return false;

		Header[] expiresHeaders = response.getHeaders(HttpConstants.EXPIRES);

		if (expiresHeaders.length > 1)
			return false;

		Header[] dateHeaders = response.getHeaders(HTTP.DATE_HEADER);

		if (dateHeaders.length != 1)
			return false;

		try {
			DateUtils.parseDate(dateHeaders[0].getValue());
		} catch (DateParseException dpe) {
			return false;
		}

		for (Header varyHdr : response.getHeaders(HttpConstants.VARY)) {
			for (HeaderElement elem : varyHdr.getElements()) {
				if ("*".equals(elem.getName())) {
					return false;
				}
			}
		}

		if (isExplicitlyNonCacheable(response))
			return false;

		return (cacheable || isExplicitlyCacheable(response));
	}

	protected boolean isExplicitlyNonCacheable(HttpResponse response) {
		Header[] cacheControlHeaders = response.getHeaders(HttpConstants.CACHE_CONTROL);
		for (Header header : cacheControlHeaders) {
			for (HeaderElement elem : header.getElements()) {
				if (HttpConstants.CACHE_CONTROL_NO_STORE.equals(elem.getName())
						|| HttpConstants.CACHE_CONTROL_NO_CACHE.equals(elem.getName())
						|| (sharedCache && HttpConstants.PRIVATE.equals(elem.getName()))) {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean hasCacheControlParameterFrom(HttpMessage msg, String[] params) {
		Header[] cacheControlHeaders = msg.getHeaders(HttpConstants.CACHE_CONTROL);
		for (Header header : cacheControlHeaders) {
			for (HeaderElement elem : header.getElements()) {
				for (String param : params) {
					if (param.equalsIgnoreCase(elem.getName())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	protected boolean isExplicitlyCacheable(HttpResponse response) {
		if (response.getFirstHeader(HttpConstants.EXPIRES) != null)
			return true;
		String[] cacheableParams = { HttpConstants.CACHE_CONTROL_MAX_AGE, "s-maxage",
				HttpConstants.CACHE_CONTROL_MUST_REVALIDATE,
				HttpConstants.CACHE_CONTROL_PROXY_REVALIDATE, HttpConstants.PUBLIC };
		return hasCacheControlParameterFrom(response, cacheableParams);
	}

	/**
	 * Determine if the {@link HttpResponse} gotten from the origin is a
	 * cacheable response.
	 * 
	 * @param request
	 *            the {@link HttpRequest} that generated an origin hit
	 * @param response
	 *            the {@link HttpResponse} from the origin
	 * @return <code>true</code> if response is cacheable
	 */
	public boolean isResponseCacheable(HttpRequest request, HttpResponse response) {
		if (requestProtocolGreaterThanAccepted(request)) {
			Log.d("MadRobot", "Response was not cacheable.");
			return false;
		}

		String[] uncacheableRequestDirectives = { HttpConstants.CACHE_CONTROL_NO_STORE };
		if (hasCacheControlParameterFrom(request, uncacheableRequestDirectives)) {
			return false;
		}

		if (request.getRequestLine().getUri().contains("?")
				&& (!isExplicitlyCacheable(response) || from1_0Origin(response))) {
			Log.d("MadRobot", "Response was not cacheable.");
			return false;
		}

		if (expiresHeaderLessOrEqualToDateHeaderAndNoCacheControl(response)) {
			return false;
		}

		if (sharedCache) {
			Header[] authNHeaders = request.getHeaders(HttpConstants.AUTHORIZATION);
			if (authNHeaders != null && authNHeaders.length > 0) {
				String[] authCacheableParams = { "s-maxage",
						HttpConstants.CACHE_CONTROL_MUST_REVALIDATE, HttpConstants.PUBLIC };
				return hasCacheControlParameterFrom(response, authCacheableParams);
			}
		}

		String method = request.getRequestLine().getMethod();
		return isResponseCacheable(method, response);
	}

	private boolean expiresHeaderLessOrEqualToDateHeaderAndNoCacheControl(HttpResponse response) {
		if (response.getFirstHeader(HttpConstants.CACHE_CONTROL) != null)
			return false;
		Header expiresHdr = response.getFirstHeader(HttpConstants.EXPIRES);
		Header dateHdr = response.getFirstHeader(HTTP.DATE_HEADER);
		if (expiresHdr == null || dateHdr == null)
			return false;
		try {
			Date expires = DateUtils.parseDate(expiresHdr.getValue());
			Date date = DateUtils.parseDate(dateHdr.getValue());
			return expires.equals(date) || expires.before(date);
		} catch (DateParseException dpe) {
			return false;
		}
	}

	private boolean from1_0Origin(HttpResponse response) {
		Header via = response.getFirstHeader(HttpConstants.VIA);
		if (via != null) {
			for (HeaderElement elt : via.getElements()) {
				String proto = elt.toString().split("\\s")[0];
				if (proto.contains("/")) {
					return proto.equals("HTTP/1.0");
				} else {
					return proto.equals("1.0");
				}
			}
		}
		return HttpVersion.HTTP_1_0.equals(response.getProtocolVersion());
	}

	private boolean requestProtocolGreaterThanAccepted(HttpRequest req) {
		return req.getProtocolVersion().compareToVersion(HttpVersion.HTTP_1_1) > 0;
	}

}

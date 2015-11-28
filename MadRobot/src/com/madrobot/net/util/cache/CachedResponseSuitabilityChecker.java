package com.madrobot.net.util.cache;

import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

import android.util.Log;

import com.madrobot.net.HttpConstants;
import com.madrobot.net.util.cache.annotation.Immutable;

/**
 * Determines whether a given {@link HttpCacheEntry} is suitable to be used as a
 * response for a given {@link HttpRequest}.
 * 
 * @since 4.1
 */
@Immutable
class CachedResponseSuitabilityChecker {

	private final boolean sharedCache;
	private final boolean useHeuristicCaching;
	private final float heuristicCoefficient;
	private final long heuristicDefaultLifetime;
	private final CacheValidityPolicy validityStrategy;

	CachedResponseSuitabilityChecker(final CacheValidityPolicy validityStrategy,
			CacheConfig config) {
		super();
		this.validityStrategy = validityStrategy;
		this.sharedCache = config.isSharedCache();
		this.useHeuristicCaching = config.isHeuristicCachingEnabled();
		this.heuristicCoefficient = config.getHeuristicCoefficient();
		this.heuristicDefaultLifetime = config.getHeuristicDefaultLifetime();
	}

	CachedResponseSuitabilityChecker(CacheConfig config) {
		this(new CacheValidityPolicy(), config);
	}

	private boolean isFreshEnough(HttpCacheEntry entry, HttpRequest request, Date now) {
		if (validityStrategy.isResponseFresh(entry, now))
			return true;
		if (useHeuristicCaching
				&& validityStrategy.isResponseHeuristicallyFresh(entry, now,
						heuristicCoefficient, heuristicDefaultLifetime))
			return true;
		if (originInsistsOnFreshness(entry))
			return false;
		long maxstale = getMaxStale(request);
		if (maxstale == -1)
			return false;
		return (maxstale > validityStrategy.getStalenessSecs(entry, now));
	}

	private boolean originInsistsOnFreshness(HttpCacheEntry entry) {
		if (validityStrategy.mustRevalidate(entry))
			return true;
		if (!sharedCache)
			return false;
		return validityStrategy.proxyRevalidate(entry)
				|| validityStrategy.hasCacheControlDirective(entry, "s-maxage");
	}

	private long getMaxStale(HttpRequest request) {
		long maxstale = -1;
		for (Header h : request.getHeaders(HttpConstants.CACHE_CONTROL)) {
			for (HeaderElement elt : h.getElements()) {
				if (HttpConstants.CACHE_CONTROL_MAX_STALE.equals(elt.getName())) {
					if ((elt.getValue() == null || "".equals(elt.getValue().trim()))
							&& maxstale == -1) {
						maxstale = Long.MAX_VALUE;
					} else {
						try {
							long val = Long.parseLong(elt.getValue());
							if (val < 0)
								val = 0;
							if (maxstale == -1 || val < maxstale) {
								maxstale = val;
							}
						} catch (NumberFormatException nfe) {
							// err on the side of preserving semantic
							// transparency
							maxstale = 0;
						}
					}
				}
			}
		}
		return maxstale;
	}

	/**
	 * Determine if I can utilize a {@link HttpCacheEntry} to respond to the
	 * given {@link HttpRequest}
	 * 
	 * @param host
	 *            {@link HttpHost}
	 * @param request
	 *            {@link HttpRequest}
	 * @param entry
	 *            {@link HttpCacheEntry}
	 * @param now
	 *            Right now in time
	 * @return boolean yes/no answer
	 */
	public boolean canCachedResponseBeUsed(HttpHost host, HttpRequest request,
			HttpCacheEntry entry, Date now) {

		if (!isFreshEnough(entry, request, now)) {
			Log.d("MadRobot", "Cache entry was not fresh enough");
			return false;
		}

		if (!validityStrategy.contentLengthHeaderMatchesActualLength(entry)) {
			Log.d("MadRobot", "Cache entry Content-Length and header information do not match");
			return false;
		}

		if (hasUnsupportedConditionalHeaders(request)) {
			Log.d("MadRobot", "Request contained conditional headers we don't handle");
			return false;
		}

		if (isConditional(request) && !allConditionalsMatch(request, entry, now)) {
			return false;
		}

		for (Header ccHdr : request.getHeaders(HttpConstants.CACHE_CONTROL)) {
			for (HeaderElement elt : ccHdr.getElements()) {
				if (HttpConstants.CACHE_CONTROL_NO_CACHE.equals(elt.getName())) {
					Log.d("MadRobot",
							"Response contained NO CACHE directive, cache was not suitable");
					return false;
				}

				if (HttpConstants.CACHE_CONTROL_NO_STORE.equals(elt.getName())) {
					Log.d("MadRobot",
							"Response contained NO STORE directive, cache was not suitable");
					return false;
				}

				if (HttpConstants.CACHE_CONTROL_MAX_AGE.equals(elt.getName())) {
					try {
						int maxage = Integer.parseInt(elt.getValue());
						if (validityStrategy.getCurrentAgeSecs(entry, now) > maxage) {
							Log.d("MadRobot",
									"Response from cache was NOT suitable due to max age");
							return false;
						}
					} catch (NumberFormatException ex) {
						// err conservatively
						Log.d("MadRobot",
								"Response from cache was malformed" + ex.getMessage());
						return false;
					}
				}

				if (HttpConstants.CACHE_CONTROL_MAX_STALE.equals(elt.getName())) {
					try {
						int maxstale = Integer.parseInt(elt.getValue());
						if (validityStrategy.getFreshnessLifetimeSecs(entry) > maxstale) {
							Log.d("MadRobot",
									"Response from cache was not suitable due to Max stale freshness");
							return false;
						}
					} catch (NumberFormatException ex) {
						// err conservatively
						Log.d("MadRobot",
								"Response from cache was malformed: " + ex.getMessage());
						return false;
					}
				}

				if (HttpConstants.CACHE_CONTROL_MIN_FRESH.equals(elt.getName())) {
					try {
						long minfresh = Long.parseLong(elt.getValue());
						if (minfresh < 0L)
							return false;
						long age = validityStrategy.getCurrentAgeSecs(entry, now);
						long freshness = validityStrategy.getFreshnessLifetimeSecs(entry);
						if (freshness - age < minfresh) {
							Log.d("MadRobot",
									"Response from cache was not suitable due to min fresh "
											+ "freshness requirement");
							return false;
						}
					} catch (NumberFormatException ex) {
						// err conservatively
						Log.d("MadRobot",
								"Response from cache was malformed: " + ex.getMessage());
						return false;
					}
				}
			}
		}

		Log.d("MadRobot", "Response from cache was suitable");
		return true;
	}

	/**
	 * Is this request the type of conditional request we support?
	 * 
	 * @param request
	 *            The current httpRequest being made
	 * @return {@code true} if the request is supported
	 */
	public boolean isConditional(HttpRequest request) {
		return hasSupportedEtagValidator(request)
				|| hasSupportedLastModifiedValidator(request);
	}

	/**
	 * Check that conditionals that are part of this request match
	 * 
	 * @param request
	 *            The current httpRequest being made
	 * @param entry
	 *            the cache entry
	 * @param now
	 *            right NOW in time
	 * @return {@code true} if the request matches all conditionals
	 */
	public boolean allConditionalsMatch(HttpRequest request, HttpCacheEntry entry, Date now) {
		boolean hasEtagValidator = hasSupportedEtagValidator(request);
		boolean hasLastModifiedValidator = hasSupportedLastModifiedValidator(request);

		boolean etagValidatorMatches = (hasEtagValidator)
				&& etagValidatorMatches(request, entry);
		boolean lastModifiedValidatorMatches = (hasLastModifiedValidator)
				&& lastModifiedValidatorMatches(request, entry, now);

		if ((hasEtagValidator && hasLastModifiedValidator)
				&& !(etagValidatorMatches && lastModifiedValidatorMatches)) {
			return false;
		} else if (hasEtagValidator && !etagValidatorMatches) {
			return false;
		}

		if (hasLastModifiedValidator && !lastModifiedValidatorMatches) {
			return false;
		}
		return true;
	}

	private boolean hasUnsupportedConditionalHeaders(HttpRequest request) {
		return (request.getFirstHeader(HttpConstants.IF_RANGE) != null
				|| request.getFirstHeader(HttpConstants.IF_MATCH) != null || hasValidDateField(
					request, HttpConstants.IF_UNMODIFIED_SINCE));
	}

	private boolean hasSupportedEtagValidator(HttpRequest request) {
		return request.containsHeader(HttpConstants.IF_NONE_MATCH);
	}

	private boolean hasSupportedLastModifiedValidator(HttpRequest request) {
		return hasValidDateField(request, HttpConstants.IF_MODIFIED_SINCE);
	}

	/**
	 * Check entry against If-None-Match
	 * 
	 * @param request
	 *            The current httpRequest being made
	 * @param entry
	 *            the cache entry
	 * @return boolean does the etag validator match
	 */
	private boolean etagValidatorMatches(HttpRequest request, HttpCacheEntry entry) {
		Header etagHeader = entry.getFirstHeader(HttpConstants.ETAG);
		String etag = (etagHeader != null) ? etagHeader.getValue() : null;
		Header[] ifNoneMatch = request.getHeaders(HttpConstants.IF_NONE_MATCH);
		if (ifNoneMatch != null) {
			for (Header h : ifNoneMatch) {
				for (HeaderElement elt : h.getElements()) {
					String reqEtag = elt.toString();
					if (("*".equals(reqEtag) && etag != null) || reqEtag.equals(etag)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Check entry against If-Modified-Since, if If-Modified-Since is in the
	 * future it is invalid as per
	 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
	 * 
	 * @param request
	 *            The current httpRequest being made
	 * @param entry
	 *            the cache entry
	 * @param now
	 *            right NOW in time
	 * @return boolean Does the last modified header match
	 */
	private boolean lastModifiedValidatorMatches(HttpRequest request, HttpCacheEntry entry,
			Date now) {
		Header lastModifiedHeader = entry.getFirstHeader(HttpConstants.LAST_MODIFIED);
		Date lastModified = null;
		try {
			if (lastModifiedHeader != null) {
				lastModified = DateUtils.parseDate(lastModifiedHeader.getValue());
			}
		} catch (DateParseException dpe) {
			// nop
		}

		if (lastModified == null) {
			return false;
		}

		for (Header h : request.getHeaders(HttpConstants.IF_MODIFIED_SINCE)) {
			try {
				Date ifModifiedSince = DateUtils.parseDate(h.getValue());
				if (ifModifiedSince.after(now) || lastModified.after(ifModifiedSince)) {
					return false;
				}
			} catch (DateParseException dpe) {
				// nop
			}
		}
		return true;
	}

	private boolean hasValidDateField(HttpRequest request, String headerName) {
		for (Header h : request.getHeaders(headerName)) {
			try {
				DateUtils.parseDate(h.getValue());
				return true;
			} catch (DateParseException dpe) {
				// ignore malformed dates
			}
		}
		return false;
	}
}

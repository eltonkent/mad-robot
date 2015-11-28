package com.madrobot.net.util.cache;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.ProtocolException;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

/**
 * Class used to represent an asynchronous revalidation event, such as with
 * "stale-while-revalidate"
 */
class AsynchronousValidationRequest implements Runnable {
	private final AsynchronousValidator parent;
	private final CachingHttpClient cachingClient;
	private final HttpHost target;
	private final HttpRequest request;
	private final HttpContext context;
	private final HttpCacheEntry cacheEntry;
	private final String identifier;
	static final String TAG = "MadRobot";

	/**
	 * Used internally by {@link AsynchronousValidator} to schedule a
	 * revalidation.
	 * 
	 * @param cachingClient
	 * @param target
	 * @param request
	 * @param context
	 * @param cacheEntry
	 * @param bookKeeping
	 * @param identifier
	 */
	AsynchronousValidationRequest(AsynchronousValidator parent,
			CachingHttpClient cachingClient, HttpHost target, HttpRequest request,
			HttpContext context, HttpCacheEntry cacheEntry, String identifier) {
		this.parent = parent;
		this.cachingClient = cachingClient;
		this.target = target;
		this.request = request;
		this.context = context;
		this.cacheEntry = cacheEntry;
		this.identifier = identifier;
	}

	@Override
	public void run() {
		try {
			cachingClient.revalidateCacheEntry(target, request, context, cacheEntry);
		} catch (IOException ioe) {
			android.util.Log.d(TAG, "Asynchronous revalidation failed due to exception: "
					+ ioe);
		} catch (ProtocolException pe) {
			Log.e(TAG, "ProtocolException thrown during asynchronous revalidation: " + pe);
		} finally {
			parent.markComplete(identifier);
		}
	}

	String getIdentifier() {
		return identifier;
	}

}

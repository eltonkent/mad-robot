package com.madrobot.net.util.cache;

/**
 * Signals that {@link HttpCacheStorage} encountered an error performing an
 * update operation.
 * 
 */
public class HttpCacheUpdateException extends Exception {

	private static final long serialVersionUID = 823573584868632876L;

	public HttpCacheUpdateException(String message) {
		super(message);
	}

	public HttpCacheUpdateException(String message, Throwable cause) {
		super(message);
		initCause(cause);
	}

}

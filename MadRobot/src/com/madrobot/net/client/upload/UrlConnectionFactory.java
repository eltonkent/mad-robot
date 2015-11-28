package com.madrobot.net.client.upload;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Factory for creating HTTP connections.
 * 
 * 
 */
interface UrlConnectionFactory {

	/**
	 * Default URL connection factory.
	 */
	public static final UrlConnectionFactory DEFAULT = new UrlConnectionFactory() {
		@Override
		public HttpURLConnection create(URL url) throws IOException {
			return (HttpURLConnection) url.openConnection();
		}
	};

	/**
	 * Creates an HTTP connection to <code>url</code>.
	 * 
	 * @param url
	 *            denoting the location to connect to
	 * @return an HTTP connection to <code>url</code>
	 */
	public HttpURLConnection create(URL url) throws IOException;
}

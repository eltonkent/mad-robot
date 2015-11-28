/*******************************************************************************
 * Copyright (c) 2011 MadRobot.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *  Elton Kent - initial API and implementation
 ******************************************************************************/
package com.madrobot.net;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

/**
 * Higly flexible utility to perform any operation over HTTP.
 * <p>
 * Sample Usage:<br/>
 * 
 * <pre>
 * HttpHelperSettings settings = new HttpHelperSettings();
 * // add request parameters (if any).
 * List&lt;NameValuePair&gt; requestParameter = new ArrayList&lt;NameValuePair&gt;();
 * NameValuePair param = new BasicNameValuePair(&quot;q&quot;, &quot;Android library&quot;);
 * requestParameter.add(param);
 * // set the parameters
 * settings.setHttpRequestParameters(requestParameter);
 * HttpHelper httpTask = new HttpHelper(new URI(&quot;http://www.google.com&quot;), settings);
 * // execute the http request and return the &lt;code&gt;HttpResponse&lt;/code&gt;
 * httpTask.execute();
 * </pre>
 * 
 * </p>
 * 
 */
public class HttpHelper {

	private static final String TAG = "MadRobot";

	private HttpHelperSettings httpSettings = new HttpHelperSettings();
	// private Map<String, String> requestHeader;
	private URI requestUrl;
	private Map<String, String> responseHeader;
	private HttpContext httpContext;

	public HttpContext getHttpContext() {
		return httpContext;
	}

	public void setHttpContext(HttpContext httpContext) {
		this.httpContext = httpContext;
	}

	/**
	 * 
	 * Parameterized constructor to initialize the request details.
	 * 
	 * @param requestUrl
	 *            request url
	 */
	public HttpHelper(URI requestUrl) {
		this(requestUrl, new HttpHelperSettings());
	}

	public HttpHelper(URI requestUrl, HttpHelperSettings settings) {
		this.requestUrl = requestUrl;
		this.httpSettings = settings;
	}

	/**
	 * Access point to add the query parameter to the request url
	 * 
	 * @throws URISyntaxException
	 *             if request url syntax is wrong
	 */
	private void addQueryParameter() throws URISyntaxException {
		this.requestUrl = URIUtils.createURI(this.requestUrl.getScheme(),
				this.requestUrl.getAuthority(), -1, this.requestUrl.getPath(),
				URLEncodedUtils.format(httpSettings.getHttpRequestParameters(), HTTP.UTF_8),
				null);
	}

	/**
	 * Add the response header into response
	 * 
	 * @param responseHeader
	 *            - http headers
	 */
	private void addResponseHeaders(Header[] responseHeader) {
		for (Header header : responseHeader) {
			getResponseHeader().put(header.getName(), header.getValue());

		}
	}

	/**
	 * Cancel the request after the {@link HttpHelper#execute()} method is
	 * called
	 */
	public void cancel() {
		httpSettings.getHttpClient().getConnectionManager().shutdown();
	}

	/**
	 * 
	 * Get the http response
	 * 
	 * @return HttpEntity - the response to the request. This is always a final
	 *         response, never an intermediate response with an 1xx status code.
	 * 
	 * @throws IOException
	 *             - in case of a problem or the connection was aborted
	 * 
	 * @throws URISyntaxException
	 *             in case of request url syntax error
	 */
	public HttpResponse execute() throws IOException, URISyntaxException {

		switch (httpSettings.getHttpMethod()) {
		case GET:
			return handleOtherMethods(new HttpGet(requestUrl));
		case POST:
			return handleHttpPost();
		case DELETE:
			return handleOtherMethods(new HttpDelete(requestUrl));
		case HEAD:
			return handleOtherMethods(new HttpHead(requestUrl));
		case OPTIONS:
			return handleOtherMethods(new HttpOptions(requestUrl));
		case PUT:
			return handleOtherMethods(new HttpPut(requestUrl));
		case TRACE:
			return handleOtherMethods(new HttpTrace(requestUrl));
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * Hold the responsibility of initialize and sending the http HEAD request
	 * to the requestURL and return the http response object
	 * 
	 * @return returns the HttpEntity object
	 * 
	 * @throws IOException
	 *             - in case of a problem or the connection was aborted
	 * 
	 * @throws URISyntaxException
	 *             in case of request url syntax error
	 */
	private HttpResponse handleOtherMethods(HttpRequestBase method) throws IOException,
			URISyntaxException {

		if (hasRequestParameter()) {
			addQueryParameter();
		}
		Log.d(TAG, "Request URL:[GET] " + requestUrl);
		this.initRequest(method);
		HttpResponse httpResponse;
		if (httpContext != null) {
			httpResponse = httpSettings.getHttpClient().execute(method, httpContext);
		} else {
			httpResponse = httpSettings.getHttpClient().execute(method);
		}
		addResponseHeaders(httpResponse.getAllHeaders());
		return httpResponse;
	}

	public HttpHelperSettings getHttpSettings() {
		return httpSettings;
	}

	// ////////////////////////////////////////////////////////////////////
	// Private Method's

	/**
	 * Access point to get the response headers
	 * 
	 * @return http response headers
	 */
	public Map<String, String> getResponseHeader() {
		if (responseHeader == null) {
			responseHeader = new HashMap<String, String>();
		}
		return responseHeader;
	}

	/**
	 * Hold the responsibility of initialize and sending the http post request
	 * to the requestURL and return the http resposne object
	 * 
	 * @return returns the HttpEntity object
	 * 
	 * @throws IOException
	 *             - in case of a problem or the connection was aborted
	 * 
	 * @throws URISyntaxException
	 *             in case of request url syntax error
	 */
	private HttpResponse handleHttpPost() throws IOException, URISyntaxException {

		HttpPost httpPost = new HttpPost(requestUrl);
		this.initRequest(httpPost);
		try {
			if (hasRequestParameter()) {
				httpPost.setEntity(new UrlEncodedFormEntity(httpSettings
						.getHttpRequestParameters(), "UTF-8"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.d(TAG, "Request URL:[POST] " + requestUrl);
		HttpResponse httpResponse;
		if (httpContext != null) {
			httpResponse = httpSettings.getHttpClient().execute(httpPost, httpContext);
		} else {
			httpResponse = httpSettings.getHttpClient().execute(httpPost);
		}

		addResponseHeaders(httpResponse.getAllHeaders());
		return httpResponse;
	}

	/**
	 * Return the decision result for adding the headers
	 * 
	 * @return true - if the request header is empty otherwise false
	 */
	private boolean hasRequestHeader() {
		return httpSettings.getHttpRequestHeaders() != null;
	}

	/**
	 * Return the decision result for adding the request parameters
	 * 
	 * @return true - if the request parameter is empty otherwise false
	 */
	private boolean hasRequestParameter() {
		return httpSettings.getHttpRequestParameters() != null;
	}

	/**
	 * Access point to init the http request
	 * 
	 * @param httpRequestBase
	 *            Represents the http base
	 */
	private void initRequest(HttpRequestBase httpRequestBase) {
		setDefaultRequestHeaders(httpRequestBase);
		if (hasRequestHeader()) {
			setRequestHeaders(httpRequestBase);
		}
	}

	/**
	 * Add the default headers if request headers is empty
	 * 
	 * @param httpRequestBase
	 *            - base object for http methods
	 */
	private void setDefaultRequestHeaders(HttpRequestBase httpRequestBase) {
		List<NameValuePair> requestHeaders = httpSettings.getHttpRequestHeaders();
		if (requestHeaders != null) {
			NameValuePair pair = null;
			for (int i = 0; i < requestHeaders.size(); i++) {
				pair = requestHeaders.get(i);
				httpRequestBase.setHeader(pair.getName(), pair.getValue());
			}
		}
	}

	/**
	 * 
	 * @param httpSettings
	 */
	public void setHttpSettings(HttpHelperSettings httpSettings) {

		this.httpSettings = httpSettings;
	}

	/**
	 * Add the request header into request
	 * 
	 * @param httpRequestBase
	 *            - base object for http methods
	 */
	private void setRequestHeaders(HttpRequestBase httpRequestBase) {
		List<NameValuePair> requestHeaders = httpSettings.getHttpRequestHeaders();
		NameValuePair pair = null;
		for (int i = 0; i < requestHeaders.size(); i++) {
			pair = requestHeaders.get(i);
			httpRequestBase.setHeader(pair.getName(), pair.getValue());
		}
	}

}

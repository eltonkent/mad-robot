package com.madrobot.net.client.async;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

/**
 * A collection of string request parameters or files to send along with
 * requests made from an {@link AsyncHttpClient} instance.
 * <p>
 * For example:
 * <p>
 * 
 * <pre>
 * RequestParams params = new RequestParams();
 * params.put(&quot;username&quot;, &quot;james&quot;);
 * params.put(&quot;password&quot;, &quot;123456&quot;);
 * params.put(&quot;email&quot;, &quot;my@email.com&quot;);
 * params.put(&quot;profile_picture&quot;, new File(&quot;pic.jpg&quot;)); // Upload a File
 * params.put(&quot;profile_picture2&quot;, someInputStream); // Upload an InputStream
 * params.put(&quot;profile_picture3&quot;, new ByteArrayInputStream(someBytes)); // Upload
 * 																		// some
 * 																		// bytes
 * 
 * AsyncHttpClient client = new AsyncHttpClient();
 * client.post(&quot;http://myendpoint.com&quot;, params, responseHandler);
 * </pre>
 */
public class AsyncHttpRequestParams {
	private static class FileWrapper {
		public String contentType;
		public String fileName;
		public InputStream inputStream;

		FileWrapper(InputStream inputStream, String fileName, String contentType) {
			this.inputStream = inputStream;
			this.fileName = fileName;
			this.contentType = contentType;
		}

		String getFileName() {
			if (fileName != null) {
				return fileName;
			} else {
				return "nofilename";
			}
		}
	}

	private static String ENCODING = "UTF-8";
	protected ConcurrentHashMap<String, FileWrapper> fileParams;

	protected ConcurrentHashMap<String, String> urlParams;

	/**
	 * Constructs a new empty <code>RequestParams</code> instance.
	 */
	public AsyncHttpRequestParams() {
		init();
	}

	/**
	 * Constructs a new RequestParams instance containing the key/value string
	 * params from the specified map.
	 * 
	 * @param source
	 *            the source key/value string map to add.
	 */
	public AsyncHttpRequestParams(Map<String, String> source) {
		init();

		for (Map.Entry<String, String> entry : source.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Constructs a new RequestParams instance and populate it with a single
	 * initial key/value string param.
	 * 
	 * @param key
	 *            the key name for the intial param.
	 * @param value
	 *            the value string for the initial param.
	 */
	public AsyncHttpRequestParams(String key, String value) {
		init();

		put(key, value);
	}

	HttpEntity getEntity() {
		HttpEntity entity = null;

		if (!fileParams.isEmpty()) {
			AsyncHttpSimpleMultipartEntity multipartEntity = new AsyncHttpSimpleMultipartEntity();

			// Add string params
			for (ConcurrentHashMap.Entry<String, String> entry : urlParams
					.entrySet()) {
				multipartEntity.addPart(entry.getKey(), entry.getValue());
			}

			// Add file params
			int currentIndex = 0;
			int lastIndex = fileParams.entrySet().size() - 1;
			for (ConcurrentHashMap.Entry<String, FileWrapper> entry : fileParams
					.entrySet()) {
				FileWrapper file = entry.getValue();
				if (file.inputStream != null) {
					boolean isLast = currentIndex == lastIndex;
					if (file.contentType != null) {
						multipartEntity.addPart(entry.getKey(),
								file.getFileName(), file.inputStream,
								file.contentType, isLast);
					} else {
						multipartEntity.addPart(entry.getKey(),
								file.getFileName(), file.inputStream, isLast);
					}
				}
				currentIndex++;
			}

			entity = multipartEntity;
		} else {
			try {
				entity = new UrlEncodedFormEntity(getParamsList(), ENCODING);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		return entity;
	}

	protected List<BasicNameValuePair> getParamsList() {
		List<BasicNameValuePair> lparams = new LinkedList<BasicNameValuePair>();

		for (ConcurrentHashMap.Entry<String, String> entry : urlParams
				.entrySet()) {
			lparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		return lparams;
	}

	protected String getParamString() {
		return URLEncodedUtils.format(getParamsList(), ENCODING);
	}

	private void init() {
		urlParams = new ConcurrentHashMap<String, String>();
		fileParams = new ConcurrentHashMap<String, FileWrapper>();
	}

	/**
	 * Adds a file to the request.
	 * 
	 * @param key
	 *            the key name for the new param.
	 * @param file
	 *            the file to add.
	 */
	public void put(String key, File file) throws FileNotFoundException {
		put(key, new FileInputStream(file), file.getName());
	}

	/**
	 * Adds an input stream to the request.
	 * 
	 * @param key
	 *            the key name for the new param.
	 * @param stream
	 *            the input stream to add.
	 */
	public void put(String key, InputStream stream) {
		put(key, stream, null);
	}

	/**
	 * Adds an input stream to the request.
	 * 
	 * @param key
	 *            the key name for the new param.
	 * @param stream
	 *            the input stream to add.
	 * @param fileName
	 *            the name of the file.
	 */
	public void put(String key, InputStream stream, String fileName) {
		put(key, stream, fileName, null);
	}

	/**
	 * Adds an input stream to the request.
	 * 
	 * @param key
	 *            the key name for the new param.
	 * @param stream
	 *            the input stream to add.
	 * @param fileName
	 *            the name of the file.
	 * @param contentType
	 *            the content type of the file, eg. application/json
	 */
	public void put(String key, InputStream stream, String fileName,
			String contentType) {
		if (key != null && stream != null) {
			fileParams.put(key, new FileWrapper(stream, fileName, contentType));
		}
	}

	/**
	 * Adds a key/value string pair to the request.
	 * 
	 * @param key
	 *            the key name for the new param.
	 * @param value
	 *            the value string for the new param.
	 */
	public void put(String key, String value) {
		if (key != null && value != null) {
			urlParams.put(key, value);
		}
	}

	/**
	 * Removes a parameter from the request.
	 * 
	 * @param key
	 *            the key name for the parameter to remove.
	 */
	public void remove(String key) {
		urlParams.remove(key);
		fileParams.remove(key);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (ConcurrentHashMap.Entry<String, String> entry : urlParams
				.entrySet()) {
			if (result.length() > 0)
				result.append("&");

			result.append(entry.getKey());
			result.append("=");
			result.append(entry.getValue());
		}

		for (ConcurrentHashMap.Entry<String, FileWrapper> entry : fileParams
				.entrySet()) {
			if (result.length() > 0)
				result.append("&");

			result.append(entry.getKey());
			result.append("=");
			result.append("FILE");
		}

		return result.toString();
	}
}
package com.madrobot.net.util.cache;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHttpResponse;

import com.madrobot.net.util.cache.annotation.NotThreadSafe;

/**
 */
@NotThreadSafe
class SizeLimitedResponseReader {

	private final ResourceFactory resourceFactory;
	private final long maxResponseSizeBytes;
	private final HttpRequest request;
	private final HttpResponse response;

	private InputStream instream;
	private InputLimit limit;
	private Resource resource;
	private boolean consumed;

	/**
	 * Create an {@link HttpResponse} that is limited in size, this allows for
	 * checking the size of objects that will be stored in the cache.
	 */
	public SizeLimitedResponseReader(ResourceFactory resourceFactory,
			long maxResponseSizeBytes, HttpRequest request, HttpResponse response) {
		super();
		this.resourceFactory = resourceFactory;
		this.maxResponseSizeBytes = maxResponseSizeBytes;
		this.request = request;
		this.response = response;
	}

	protected void readResponse() throws IOException {
		if (!consumed) {
			doConsume();
		}
	}

	private void ensureNotConsumed() {
		if (consumed) {
			throw new IllegalStateException("Response has already been consumed");
		}
	}

	private void ensureConsumed() {
		if (!consumed) {
			throw new IllegalStateException("Response has not been consumed");
		}
	}

	private void doConsume() throws IOException {
		ensureNotConsumed();
		consumed = true;

		limit = new InputLimit(maxResponseSizeBytes);

		HttpEntity entity = response.getEntity();
		if (entity == null) {
			return;
		}
		String uri = request.getRequestLine().getUri();
		instream = entity.getContent();
		resource = resourceFactory.generate(uri, instream, limit);
	}

	boolean isLimitReached() {
		ensureConsumed();
		return limit.isReached();
	}

	Resource getResource() {
		ensureConsumed();
		return resource;
	}

	HttpResponse getReconstructedResponse() throws IOException {
		ensureConsumed();
		HttpResponse reconstructed = new BasicHttpResponse(response.getStatusLine());
		reconstructed.setHeaders(response.getAllHeaders());

		CombinedEntity combinedEntity = new CombinedEntity(resource, instream);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			combinedEntity.setContentType(entity.getContentType());
			combinedEntity.setContentEncoding(entity.getContentEncoding());
			combinedEntity.setChunked(entity.isChunked());
		}
		reconstructed.setEntity(combinedEntity);
		return reconstructed;
	}

}

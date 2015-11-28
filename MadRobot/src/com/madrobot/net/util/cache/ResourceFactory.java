package com.madrobot.net.util.cache;

import java.io.IOException;
import java.io.InputStream;

/**
 * Generates {@link Resource} instances for handling cached HTTP response
 * bodies.
 * 
 */
public interface ResourceFactory {

	/**
	 * Creates a {@link Resource} from a given response body.
	 * 
	 * @param requestId
	 *            a unique identifier for this particular response body
	 * @param instream
	 *            the original {@link InputStream} containing the response body
	 *            of the origin HTTP response.
	 * @param limit
	 *            maximum number of bytes to consume of the response body; if
	 *            this limit is reached before the response body is fully
	 *            consumed, mark the limit has having been reached and return a
	 *            {@code Resource} containing the data read to that point.
	 * @return a {@code Resource} containing however much of the response body
	 *         was successfully read.
	 * @throws IOException
	 */
	Resource generate(String requestId, InputStream instream, InputLimit limit)
			throws IOException;

	/**
	 * Clones an existing {@link Resource}.
	 * 
	 * @param requestId
	 *            unique identifier provided to associate with the cloned
	 *            response body.
	 * @param resource
	 *            the original response body to clone.
	 * @return the {@code Resource} copy
	 * @throws IOException
	 */
	Resource copy(String requestId, Resource resource) throws IOException;

}

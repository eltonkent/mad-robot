package com.madrobot.net.util.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Represents a disposable system resource used for handling cached response
 * bodies.
 * 
 * @since 4.1
 */
public interface Resource extends Serializable {

	/**
	 * Returns an {@link InputStream} from which the response body can be read.
	 * 
	 * @throws IOException
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Returns the length in bytes of the response body.
	 */
	long length();

	/**
	 * Indicates the system no longer needs to keep this response body and any
	 * system resources associated with it may be reclaimed.
	 */
	void dispose();

}

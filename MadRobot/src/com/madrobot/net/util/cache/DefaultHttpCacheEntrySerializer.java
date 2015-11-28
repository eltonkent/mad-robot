package com.madrobot.net.util.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.madrobot.net.util.cache.annotation.Immutable;

/**
 * {@link HttpCacheEntrySerializer} implementation that uses the default
 * (native) serialization.
 * 
 * @see java.io.Serializable
 * 
 * @since 4.1
 */
@Immutable
public class DefaultHttpCacheEntrySerializer implements HttpCacheEntrySerializer {

	@Override
	public void writeTo(HttpCacheEntry cacheEntry, OutputStream os) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(os);
		try {
			oos.writeObject(cacheEntry);
		} finally {
			oos.close();
		}
	}

	@Override
	public HttpCacheEntry readFrom(InputStream is) throws IOException {
		ObjectInputStream ois = new ObjectInputStream(is);
		try {
			return (HttpCacheEntry) ois.readObject();
		} catch (ClassNotFoundException ex) {
			throw new HttpCacheEntrySerializationException("Class not found: "
					+ ex.getMessage(), ex);
		} finally {
			ois.close();
		}
	}

}

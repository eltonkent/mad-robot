package com.madrobot.net.util.cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.madrobot.io.IOUtils;
import com.madrobot.net.util.cache.annotation.Immutable;

/**
 * Generates {@link Resource} instances stored entirely in heap.
 * 
 * @since 4.1
 */
@Immutable
public class HeapResourceFactory implements ResourceFactory {

	@Override
	public Resource generate(final String requestId, final InputStream instream,
			final InputLimit limit) throws IOException {
		ByteArrayOutputStream outstream = new ByteArrayOutputStream();
		byte[] buf = new byte[2048];
		long total = 0;
		int l;
		while ((l = instream.read(buf)) != -1) {
			outstream.write(buf, 0, l);
			total += l;
			if (limit != null && total > limit.getValue()) {
				limit.reached();
				break;
			}
		}
		return new HeapResource(outstream.toByteArray());
	}

	@Override
	public Resource copy(final String requestId, final Resource resource) throws IOException {
		byte[] body;
		if (resource instanceof HeapResource) {
			body = ((HeapResource) resource).getByteArray();
		} else {
			ByteArrayOutputStream outstream = new ByteArrayOutputStream();
			IOUtils.copyAndClose(resource.getInputStream(), outstream);
			body = outstream.toByteArray();
		}
		return new HeapResource(body);
	}

}

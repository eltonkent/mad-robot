package com.madrobot.net.util.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.madrobot.io.IOUtils;
import com.madrobot.io.NIOUtils;
import com.madrobot.net.util.cache.annotation.Immutable;

/**
 * Generates {@link Resource} instances whose body is stored in a temporary
 * file.
 * 
 * @since 4.1
 */
@Immutable
public class FileResourceFactory implements ResourceFactory {

	private final File cacheDir;
	private final BasicIdGenerator idgen;

	public FileResourceFactory(final File cacheDir) {
		super();
		this.cacheDir = cacheDir;
		this.idgen = new BasicIdGenerator();
	}

	private File generateUniqueCacheFile(final String requestId) {
		StringBuilder buffer = new StringBuilder();
		this.idgen.generate(buffer);
		buffer.append('.');
		int len = Math.min(requestId.length(), 100);
		for (int i = 0; i < len; i++) {
			char ch = requestId.charAt(i);
			if (Character.isLetterOrDigit(ch) || ch == '.') {
				buffer.append(ch);
			} else {
				buffer.append('-');
			}
		}
		return new File(this.cacheDir, buffer.toString());
	}

	@Override
	public Resource generate(final String requestId, final InputStream instream,
			final InputLimit limit) throws IOException {
		File file = generateUniqueCacheFile(requestId);
		FileOutputStream outstream = new FileOutputStream(file);
		try {
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
		} finally {
			outstream.close();
		}
		return new FileResource(file);
	}

	@Override
	public Resource copy(final String requestId, final Resource resource) throws IOException {
		File file = generateUniqueCacheFile(requestId);

		if (resource instanceof FileResource) {
			File src = ((FileResource) resource).getFile();
			NIOUtils.copyFile(src, file);
		} else {
			FileOutputStream out = new FileOutputStream(file);
			IOUtils.copyAndClose(resource.getInputStream(), out);
		}
		return new FileResource(file);
	}

}

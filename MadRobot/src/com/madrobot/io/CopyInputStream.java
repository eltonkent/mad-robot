package com.madrobot.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream that allows you to make copies of any src i/p stream
 * 
 * @author elton.stephen.kent
 * 
 */
public class CopyInputStream {
	private ByteArrayOutputStream _copy;

	public CopyInputStream(InputStream is) {
		try {
			copy(is);
		} catch (IOException ex) {
			System.out.println("IOException in CopyInputStream");
			System.out.println(ex.toString());
		}
	}

	private void copy(InputStream is) throws IOException {
		_copy = new ByteArrayOutputStream();
		IOUtils.copy(is, _copy);
	}

	public InputStream getCopy() {
		return new ByteArrayInputStream(_copy.toByteArray());
	}
}

package com.madrobot.net.client.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * An implementation of {@link UploadData} that provides data from a
 * {@code File}.
 * 
 * 
 */
public class FileUploadData implements UploadData {

	private final File file;
	private FileInputStream stream;

	public FileUploadData(File file) throws IOException {
		if (file == null) {
			throw new IOException();
		}
		this.file = file;
		if (!file.exists() || !file.canRead()) {
			throw new IOException();
		}
		stream = new FileInputStream(file);

	}

	@Override
	public long length() {
		return file.length();
	}

	@Override
	public void read(byte[] destination) throws IOException {
		stream.read(destination);
	}

	@Override
	public void setPosition(long position) throws IOException {
		stream = new FileInputStream(file);
		stream.skip(position);
	}

	@Override
	public int read(byte[] chunk, int i, int length) throws IOException {
		return stream.read(chunk, i, length);
	}

	/**
	 * Gets the filename.
	 * 
	 * @return the local file name
	 */
	public String getFileName() {
		return file.getName();
	}
}

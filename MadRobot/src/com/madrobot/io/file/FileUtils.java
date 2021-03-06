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
package com.madrobot.io.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.util.Log;

import com.madrobot.io.IOProgressCallback;
import com.madrobot.io.IOUtils;
import com.madrobot.io.NullOutputStream;
import com.madrobot.io.file.filter.FileFilterUtils;
import com.madrobot.text.StringUtils;

/**
 * File System utilities
 * 
 * @author elton.stephen.kent
 * 
 */
public class FileUtils {

	/**
	 * The number of bytes in a kilobyte.
	 */
	public static final long ONE_KB = 1024;

	/**
	 * The number of bytes in a megabyte.
	 */
	public static final long ONE_MB = ONE_KB * ONE_KB;

	/**
	 * The number of bytes in a gigabyte.
	 */
	public static final long ONE_GB = ONE_KB * ONE_MB;

	/**
	 * The number of bytes in a terabyte.
	 */
	public static final long ONE_TB = ONE_KB * ONE_GB;

	/**
	 * The number of bytes in a petabyte.
	 */
	public static final long ONE_PB = ONE_KB * ONE_TB;

	/**
	 * The number of bytes in an exabyte.
	 */
	public static final long ONE_EB = ONE_KB * ONE_PB;

	private static final String TAG = "MadRobot:FileUtil";

	/**
	 * Check if the given file is contained in the list of files
	 * 
	 * @param files
	 *            the list of files
	 * @param file
	 *            the file to search for
	 * @return true on the first occurance of the file.
	 * 
	 */
	public static boolean contains(File[] files, File file) {
		if (files == null || file == null) {
			return false;
		}
		for (int i = 0; i < files.length; i++) {
			if (files[i].equals(file)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the size of the file in byte units.
	 * <p>
	 * Eg:<br/>
	 * <code>size(7168)</code> will return <code> 7KB</code>
	 * </p>
	 * 
	 * @param size
	 * @return
	 */
	public static String getSizeRepresentation(long size) {
		if (size / (1024 * 1024) > 0) {
			float tmpSize = (float) (size) / (float) (1024 * 1024);
			DecimalFormat df = new DecimalFormat("#.##");
			return "" + df.format(tmpSize) + "MB";
		} else if (size / 1024 > 0) {
			return "" + (size / (1024)) + "KB";
		} else
			return "" + size + "Bytes";
	}

	/**
	 * Use NIO to copy a file
	 * 
	 * @param src
	 *            Source Path
	 * @param dst
	 *            Destination Path
	 * @return
	 * @throws IOException
	 * @see {@link FileUtils#recursiveCopy(File, File, boolean, boolean)}
	 */
	public static final boolean copyFileNio(File src, File dst) throws IOException {
		FileChannel srcChannel = null, dstChannel = null;
		try {
			// Create channel on the source
			srcChannel = new FileInputStream(src).getChannel();

			// Create channel on the destination
			dstChannel = new FileOutputStream(dst).getChannel();

			// // Copy file contents from source to destination
			// dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

			{
				// long theoretical_max = (64 * 1024 * 1024) - (32 * 1024);
				int safe_max = (64 * 1024 * 1024) / 4;
				long size = srcChannel.size();
				long position = 0;
				while (position < size) {
					position += srcChannel.transferTo(position, safe_max, dstChannel);
				}
			}

			// Close the channels
			srcChannel.close();
			srcChannel = null;
			dstChannel.close();
			dstChannel = null;

			return true;
		} finally {
			try {
				if (srcChannel != null) {
					srcChannel.close();
				}
			} catch (IOException e) {

			}
			try {
				if (dstChannel != null) {
					dstChannel.close();
				}
			} catch (IOException e) {

			}
		}
	}

	/**
	 * Copy a file from one location to another
	 * 
	 * @param from
	 * @param to
	 * @param overwrite
	 * @param deleteOriginal
	 * @param callback
	 *            Progres callback, can be null
	 */
	public static void copyFileToFile(File from, File to, boolean overwrite,
			boolean deleteOriginal, IOProgressCallback callback) {
		if ((!from.exists()) || from.isDirectory())
			return;
		try {
			inputStreamToFile(new FileInputStream(from), to, callback);
			if (deleteOriginal) {
				from.delete();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			if (callback != null)
				callback.onError(e);
		} catch (IOException e) {
			e.printStackTrace();
			if (callback != null)
				callback.onError(e);
		}
	}

	/**
	 * Deletes a directory recursively.
	 * 
	 * @param directory
	 *            directory to delete
	 * @throws IOException
	 *             If the directory is not empty or in case deletion is
	 *             unsuccessful.
	 * @see FileUtils#recursiveDelete(File)
	 */
	public static void deleteDirectory(File directory) throws IOException {
		if (!directory.exists()) {
			return;
		}
		if (!directory.delete()) {
			String message = "Unable to delete directory " + directory + ".";
			throw new IOException(message);
		}
	}

	public static void emptyDirectory(File directory) throws IOException {
		if (!directory.exists()) {
			String message = directory + " does not exist";
			throw new IllegalArgumentException(message);
		}

		if (!directory.isDirectory()) {
			String message = directory + " is not a directory";
			throw new IllegalArgumentException(message);
		}

		File[] files = directory.listFiles();
		if (files == null) { // null if security restricted
			throw new IOException("Failed to list contents of " + directory);
		}

		IOException exception = null;
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			try {
				forceDelete(file);
			} catch (IOException ioe) {
				exception = ioe;
			}
		}

		if (null != exception) {
			throw exception;
		}
	}

	/**
	 * Check if the file exists on the given path
	 * 
	 * @param path
	 * @return True if the file exists
	 */
	public static boolean fileExists(String path) {
		boolean exists = new File(path).exists();
		Log.d(TAG, "File Exists for file " + path + " =" + exists);
		return exists;
	}

	/**
	 * Deletes a file. If file is a directory, delete it and all
	 * sub-directories.
	 * <p>
	 * The difference between File.delete() and this method are:
	 * <ul>
	 * <li>A directory to be deleted does not have to be empty.</li>
	 * <li>You get exceptions when a file or directory cannot be deleted.
	 * (java.io.File methods returns a boolean)</li>
	 * </ul>
	 * 
	 * @param file
	 *            file or directory to delete, must not be <code>null</code>
	 * @throws NullPointerException
	 *             if the directory is <code>null</code>
	 * @throws FileNotFoundException
	 *             if the file was not found
	 * @throws IOException
	 *             in case deletion is unsuccessful
	 */
	public static void forceDelete(File file) throws IOException {
		if (file.isDirectory()) {
			deleteDirectory(file);
		} else {
			boolean filePresent = file.exists();
			if (!file.delete()) {
				if (!filePresent) {
					throw new FileNotFoundException("File does not exist: " + file);
				}
				String message = "Unable to delete file: " + file;
				throw new IOException(message);
			}
		}
	}

	/**
	 * Makes a directory, including any necessary but nonexistent parent
	 * directories. If a file already exists with specified name but it is not a
	 * directory then an IOException is thrown. If the directory cannot be
	 * created (or does not already exist) then an IOException is thrown.
	 * 
	 * @param directory
	 *            directory to create, must not be <code>null</code>
	 * @throws NullPointerException
	 *             if the directory is <code>null</code>
	 * @throws IOException
	 *             if the directory cannot be created or the file already exists
	 *             but is not a directory
	 */
	public static void forceMkdir(File directory) throws IOException {
		if (directory.exists()) {
			if (!directory.isDirectory()) {
				String message = "File " + directory + " exists and is "
						+ "not a directory. Unable to create directory.";
				throw new IOException(message);
			}
		} else {
			if (!directory.mkdirs()) {
				// Double-check that some other thread or process hasn't made
				// the directory in the background
				if (!directory.isDirectory()) {
					String message = "Unable to create directory " + directory;
					throw new IOException(message);
				}
			}
		}
	}

	public static String getDirectoryName(String filename) {
		int slashposition = filename.lastIndexOf("/");
		if (slashposition >= 0)
			filename = filename.substring(0, slashposition);
		return filename;
	}

	public static String getFileExtension(String filename) {
		int dotposition = filename.lastIndexOf(".");
		String ext = filename.substring(dotposition + 1, filename.length());
		return ext;
	}

	public static String getFileName(String filename) {
		int slashposition = filename.lastIndexOf("/");
		if (slashposition >= 0)
			filename = filename.substring(slashposition + 1, filename.length());
		return filename;
	}

	/**
	 * Finds the relative path from base to file. The file returned is such that
	 * file.getCanonicalPath().equals( new File( base, getRelativePath( base,
	 * file ).getPath() ).getCanonicalPath )
	 * 
	 * @param base
	 *            The base for the relative path
	 * @param file
	 *            The destination for the relative path
	 * @return The relative path betwixt base and file, or null if there was a
	 *         IOException
	 */
	public static File getRelativePath(File base, File file) {
		try {
			String absBase = base.getCanonicalPath();
			String absFile = file.getCanonicalPath();

			if (absBase.equals(absFile)) { // base and file are the same file
				return new File("");
			}

			// find divergence point
			int divergenceCharIndex = 0;
			int divergenceSeperatorIndex = 0;

			while ((divergenceCharIndex < absBase.length())
					&& (divergenceCharIndex < absFile.length())
					&& (absBase.charAt(divergenceCharIndex) == absFile
							.charAt(divergenceCharIndex))) {
				if ((absBase.charAt(divergenceCharIndex) == File.separatorChar)
						|| (absFile.charAt(divergenceCharIndex) == File.separatorChar)) {
					divergenceSeperatorIndex = divergenceCharIndex;
				}

				divergenceCharIndex++;
			}

			if (divergenceCharIndex == absBase.length()) { // simple case where
				// the base is an
				// ancestor of the
				// target
				// file
				return new File(absFile.substring(divergenceCharIndex + 1));
			} else { // we need to back up the tree a bit, before coming back
				// down.
				StringBuilder path = new StringBuilder();
				for (int i = divergenceSeperatorIndex; i < absBase.length(); i++) {
					if (absBase.charAt(i) == File.separatorChar) {
						path.append("..");
						path.append(File.separatorChar);
					}
				}

				path.append(absFile.substring(divergenceSeperatorIndex));

				return new File(path.toString());
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		}
	}

	public static final void inputStreamToFile(InputStream src, File file,
			IOProgressCallback callback) throws IOException {
		FileOutputStream stream = null;
		try {
			if (file.getParentFile() != null)
				file.getParentFile().mkdirs();
			stream = new FileOutputStream(file);
			IOUtils.copy(src, stream, callback);
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (Exception e) {

				e.printStackTrace();
				if (callback != null)
					callback.onError(e);
			}
		}
	}

	/**
	 * Create a directory in the given <code>path</code>
	 */
	public static boolean makeDirectory(String path, String directoryName) {
		File f = new File(path, directoryName);
		if (!f.exists()) {
			return f.mkdir();
		}
		return false;
	}

	/**
	 * Opens a {@link FileInputStream} for the specified file, providing better
	 * error messages than simply calling <code>new FileInputStream(file)</code>
	 * .
	 * <p>
	 * At the end of the method either the stream will be successfully opened,
	 * or an exception will have been thrown.
	 * <p>
	 * An exception is thrown if the file does not exist. An exception is thrown
	 * if the file object exists but is a directory. An exception is thrown if
	 * the file exists but cannot be read.
	 * 
	 * @param file
	 *            the file to open for input, must not be <code>null</code>
	 * @return a new {@link FileInputStream} for the specified file
	 * @throws FileNotFoundException
	 *             if the file does not exist
	 * @throws IOException
	 *             if the file object is a directory
	 * @throws IOException
	 *             if the file cannot be read
	 */
	public static final FileInputStream openInputStream(File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (file.canRead() == false) {
				throw new IOException("File '" + file + "' cannot be read");
			}
		} else {
			throw new FileNotFoundException("File '" + file + "' does not exist");
		}
		return new FileInputStream(file);
	}

	/**
	 * Reads the contents of a file into a byte array. The file is always
	 * closed.
	 * 
	 * @param file
	 *            the file to read, must not be <code>null</code>
	 * @return the file contents, never <code>null</code>
	 * @throws IOException
	 *             in case of an I/O error
	 */
	public static byte[] readFileToByteArray(File file) throws IOException {
		InputStream in = null;
		try {
			in = openInputStream(file);
			return IOUtils.toByteArray(in);
		} finally {
			in.close();
		}
	}

	/**
	 * Reads the contents of a file line by line to a List of Strings using the
	 * default encoding for the VM. The file is always closed.
	 * 
	 * @param file
	 *            the file to read, must not be <code>null</code>
	 * @return the list of Strings representing each line in the file, never
	 *         <code>null</code>
	 * @throws IOException
	 *             in case of an I/O error
	 */
	public static final List<String> readLines(File file) throws IOException {
		return readLines(file, null);
	}

	/**
	 * Reads the contents of a file line by line to a List of Strings. The file
	 * is always closed.
	 * 
	 * @param file
	 *            the file to read, must not be <code>null</code>
	 * @param encoding
	 *            the encoding to use, <code>null</code> means platform default
	 * @return the list of Strings representing each line in the file, never
	 *         <code>null</code>
	 * @throws IOException
	 *             in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException
	 *             if the encoding is not supported by the VM
	 */
	public static final List<String> readLines(File file, String encoding) throws IOException {
		InputStream in = null;
		try {
			in = openInputStream(file);
			return IOUtils.readLines(in, encoding);
		} finally {
			in.close();
		}
	}

	/**
	 * 
	 * @param from
	 * @param targetDirectory
	 * @param overwrite
	 * @param deleteOriginal
	 *            if true, the original file will be deleted
	 * @param callback
	 *            Progress callback. can be null.
	 */
	public static void recursiveCopy(File from, File targetDirectory, boolean overwrite,
			boolean deleteOriginal, IOProgressCallback callback) {
		if (!from.exists())
			return;

		if (!targetDirectory.exists())
			targetDirectory.mkdirs();

		File newTo = new File(targetDirectory.getAbsolutePath() + "/" + from.getName());
		if (from.isDirectory()) {
			newTo.mkdirs();
			File[] contents = from.listFiles();
			for (int i = 0; i < contents.length; i++) {
				recursiveCopy(contents[i], newTo, overwrite, deleteOriginal, callback);
			}
			if (deleteOriginal)
				from.delete();
		} else {
			copyFileToFile(from, newTo, overwrite, deleteOriginal, callback);
		}
	}

	/**
	 * Recursively delete the contents of a directory or just delete a file
	 * 
	 * @param file
	 *            File or directory to delete
	 * @param callback
	 *            Progress callback. can be null.
	 */
	public static void recursiveDelete(File file, FileProgressCallback callback) {
		if (!file.exists())
			return;
		if (file.isDirectory()) {
			File[] contents = file.listFiles();
			if (callback != null) {
				callback.onDirectoryDiscovered(contents.length);
			}
			for (int i = 0; i < contents.length; i++)
				recursiveDelete(contents[i], callback);
		}
		try {
			if (callback != null) {
				callback.beforeDelete(file);
			}
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return the size of a directory. The current java implementation returns 0
	 * if a file is a directory.
	 * <p>
	 * Could be a blocking method, good to use in a non blocking thread.
	 * </p>
	 * 
	 * @param file
	 * @return Size in bytes of the directory, 0 if the file does not exist
	 */
	public static long getDirectorySize(File file) {
		long size = 0;
		if (!file.exists())
			return 0;
		if (file.isDirectory()) {
			File[] contents = file.listFiles();
			for (int i = 0; i < contents.length; i++) {
				if (contents[i].isDirectory()) {
					size += getDirectorySize(contents[i]);
				} else {
					size += contents[i].length();
				}
			}
		} else {
			size = file.length();
		}

		return size;

	}

	/**
	 * Removes all files from the directory
	 * <p>
	 * If the <code>toKeep</code> is null or an empty array, then the directory
	 * is just deleted.
	 * </p>
	 * 
	 * @param directory
	 *            Directory containing the files
	 * @param toKeep
	 *            Files to keep
	 * @see FileUtils#deleteDirectory(File)
	 * @throws IOException
	 * @throws IOException
	 */
	public static void removeAllFilesExcept(File directory, File[] toKeep) throws IOException {
		File[] allfiles = directory.listFiles();
		if (toKeep == null || toKeep.length == 0) {
			// no files
			deleteDirectory(directory);
		} else {
			for (int i = 0; i < allfiles.length; i++) {
				if (!contains(toKeep, allfiles[i])) {
					allfiles[i].delete();
				}
			}
		}
	}

	/**
	 * Remove invalid FAT filesystem characters from the given string
	 * 
	 * @param str
	 * @return
	 */
	public static String removeInvalidFATChars(String str) {
		String[] invalidChars = new String[] { ":", "\\", "/", "<", ">", "?", "*", "|", "\"",
				";" };
		return StringUtils.strip(str, invalidChars);
	}

	/**
	 * Waits for File System to propagate a file creation, imposing a timeout.
	 * <p>
	 * This method repeatedly tests {@link File#exists()} until it returns true
	 * up to the maximum time specified in seconds.
	 * 
	 * @param file
	 *            the file to check, must not be <code>null</code>
	 * @param seconds
	 *            the maximum time in seconds to wait
	 * @return true if file exists
	 * @throws NullPointerException
	 *             if the file is <code>null</code>
	 */
	public static boolean waitFor(File file, int seconds) {
		int timeout = 0;
		int tick = 0;
		while (!file.exists()) {
			if (tick++ >= 10) {
				tick = 0;
				if (timeout++ > seconds) {
					return false;
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignore) {
				// ignore exception
			} catch (Exception ex) {
				break;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param src
	 * @param file
	 * @param callback
	 *            Progress callback. can be null
	 * @throws IOException
	 */
	public static void writeByteArrayToFile(byte[] src, File file, IOProgressCallback callback)
			throws IOException {
		ByteArrayInputStream stream = null;
		try {
			stream = new ByteArrayInputStream(src);
			inputStreamToFile(stream, file, callback);
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (Exception e) {
				e.printStackTrace();
				if (callback != null)
					callback.onError(e);
			}
		}
	}

	/**
	 * Extract the given {@code zip_file} to the given {@code directory}
	 * 
	 * @param zip_file
	 * @param directory
	 */
	public void extractZipFiles(String zip_file, String directory) {
		byte[] data = new byte[2048];
		String name, path, zipDir;
		ZipEntry entry;
		ZipInputStream zipstream;

		if (!(directory.charAt(directory.length() - 1) == '/'))
			directory += "/";

		if (zip_file.contains("/")) {
			path = zip_file;
			name = path.substring(path.lastIndexOf("/") + 1, path.length() - 4);
			zipDir = directory + name + "/";

		} else {
			path = directory + zip_file;
			name = path.substring(path.lastIndexOf("/") + 1, path.length() - 4);
			zipDir = directory + name + "/";
		}

		new File(zipDir).mkdir();

		try {
			zipstream = new ZipInputStream(new FileInputStream(path));

			while ((entry = zipstream.getNextEntry()) != null) {
				String buildDir = zipDir;
				String[] dirs = entry.getName().split("/");

				if (dirs != null && dirs.length > 0) {
					for (int i = 0; i < dirs.length - 1; i++) {
						buildDir += dirs[i] + "/";
						new File(buildDir).mkdir();
					}
				}

				int read = 0;
				FileOutputStream out = new FileOutputStream(zipDir + entry.getName());
				while ((read = zipstream.read(data, 0, 2048)) != -1)
					out.write(data, 0, read);

				zipstream.closeEntry();
				out.close();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tests if the specified <code>File</code> is newer than the reference
	 * <code>File</code>.
	 * 
	 * @param file
	 *            the <code>File</code> of which the modification date must be
	 *            compared, must not be <code>null</code>
	 * @param reference
	 *            the <code>File</code> of which the modification date is used,
	 *            must not be <code>null</code>
	 * @return true if the <code>File</code> exists and has been modified more
	 *         recently than the reference <code>File</code>
	 * @throws IllegalArgumentException
	 *             if the file is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the reference file is <code>null</code> or doesn't exist
	 */
	public static boolean isFileNewer(File file, File reference) {
		if (reference == null) {
			throw new IllegalArgumentException("No specified reference file");
		}
		if (!reference.exists()) {
			throw new IllegalArgumentException("The reference file '" + reference
					+ "' doesn't exist");
		}
		return isFileNewer(file, reference.lastModified());
	}

	/**
	 * Tests if the specified <code>File</code> is newer than the specified time
	 * reference.
	 * 
	 * @param file
	 *            the <code>File</code> of which the modification date must be
	 *            compared, must not be <code>null</code>
	 * @param timeMillis
	 *            the time reference measured in milliseconds since the epoch
	 *            (00:00:00 GMT, January 1, 1970)
	 * @return true if the <code>File</code> exists and has been modified after
	 *         the given time reference.
	 * @throws IllegalArgumentException
	 *             if the file is <code>null</code>
	 */
	public static boolean isFileNewer(File file, long timeMillis) {
		if (file == null) {
			throw new IllegalArgumentException("No specified file");
		}
		if (!file.exists()) {
			return false;
		}
		return file.lastModified() > timeMillis;
	}

	/**
	 * Computes the checksum of a file using the CRC32 checksum routine. The
	 * value of the checksum is returned.
	 * 
	 * @param file
	 *            the file to checksum, must not be <code>null</code>
	 * @return the checksum value
	 * @throws NullPointerException
	 *             if the file or checksum is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the file is a directory
	 * @throws IOException
	 *             if an IO error occurs reading the file
	 * @since 1.3
	 */
	public static long checksumCRC32(File file) throws IOException {
		CRC32 crc = new CRC32();
		checksum(file, crc);
		return crc.getValue();
	}

	/**
	 * Computes the checksum of a file using the specified checksum object.
	 * Multiple files may be checked using one <code>Checksum</code> instance if
	 * desired simply by reusing the same checksum object. For example:
	 * 
	 * <pre>
	 * long csum = FileUtils.checksum(file, new CRC32()).getValue();
	 * </pre>
	 * 
	 * @param file
	 *            the file to checksum, must not be <code>null</code>
	 * @param checksum
	 *            the checksum object to be used, must not be <code>null</code>
	 * @return the checksum specified, updated with the content of the file
	 * @throws NullPointerException
	 *             if the file or checksum is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the file is a directory
	 * @throws IOException
	 *             if an IO error occurs reading the file
	 * @since 1.3
	 */
	public static Checksum checksum(File file, Checksum checksum) throws IOException {
		if (file.isDirectory()) {
			throw new IllegalArgumentException("Checksums can't be computed on directories");
		}
		InputStream in = null;
		try {
			in = new CheckedInputStream(new FileInputStream(file), checksum);
			IOUtils.copy(in, new NullOutputStream());
		} finally {
			IOUtils.closeQuietly(in);
		}
		return checksum;
	}

	/**
	 * Finds files within a given directory (and optionally its subdirectories).
	 * All files found are filtered by an IOFileFilter.
	 * <p>
	 * If your search should recurse into subdirectories you can pass in an
	 * IOFileFilter for directories. You don't need to bind a
	 * DirectoryFileFilter (via logical AND) to this filter. This method does
	 * that for you.
	 * <p>
	 * An example: If you want to search through all directories called "temp"
	 * you pass in <code>FileFilterUtils.NameFileFilter("temp")</code>
	 * <p>
	 * Another common usage of this method is find files in a directory tree but
	 * ignoring the directories generated CVS. You can simply pass in
	 * <code>FileFilterUtils.makeCVSAware(null)</code>.
	 * 
	 * @param directory
	 *            the directory to search in
	 * @param fileFilter
	 *            filter to apply when finding files.
	 * @param dirFilter
	 *            optional filter to apply when finding subdirectories. If this
	 *            parameter is <code>null</code>, subdirectories will not be
	 *            included in the search.
	 * @return an collection of java.io.File with the matching files
	 * @see FileFilterUtils
	 */
	public static Collection<File> listFiles(File directory, IOFileFilter fileFilter,
			IOFileFilter dirFilter) {
		validateListFilesParameters(directory, fileFilter);

		// Find files
		Collection<File> files = new java.util.LinkedList<File>();
		innerListFiles(files, directory, FileFilterUtils.or(fileFilter, dirFilter), false);
		return files;
	}

	/**
	 * Validates the given arguments.
	 * <ul>
	 * <li>Throws {@link IllegalArgumentException} if {@code directory} is not a
	 * directory</li>
	 * <li>Throws {@link NullPointerException} if {@code fileFilter} is null</li>
	 * </ul>
	 * 
	 * @param directory
	 *            The File to test
	 * @param fileFilter
	 *            The IOFileFilter to test
	 */
	private static void validateListFilesParameters(File directory, IOFileFilter fileFilter) {
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("Parameter 'directory' is not a directory");
		}
		if (fileFilter == null) {
			throw new NullPointerException("Parameter 'fileFilter' is null");
		}
	}

	/**
	 * Finds files within a given directory (and optionally its subdirectories).
	 * All files found are filtered by an IOFileFilter.
	 * 
	 * @param files
	 *            the collection of files found.
	 * @param directory
	 *            the directory to search in.
	 * @param filter
	 *            the filter to apply to files and directories.
	 * @param includeSubDirectories
	 *            indicates if will include the subdirectories themselves
	 */
	private static void innerListFiles(Collection<File> files, File directory,
			IOFileFilter filter, boolean includeSubDirectories) {
		File[] found = directory.listFiles((FileFilter) filter);

		if (found != null) {
			for (File file : found) {
				if (file.isDirectory()) {
					if (includeSubDirectories) {
						files.add(file);
					}
					innerListFiles(files, file, filter, includeSubDirectories);
				} else {
					files.add(file);
				}
			}
		}
	}

	/**
	 * Compares the contents of two files to determine if they are equal or not.
	 * <p>
	 * This method checks to see if the two files are different lengths or if
	 * they point to the same file, before resorting to byte-by-byte comparison
	 * of the contents.
	 * <p>
	 * Code origin: Avalon
	 * 
	 * @param file1
	 *            the first file
	 * @param file2
	 *            the second file
	 * @return true if the content of the files are equal or they both don't
	 *         exist, false otherwise
	 * @throws IOException
	 *             in case of an I/O error
	 */
	public static boolean contentEquals(File file1, File file2) throws IOException {
		boolean file1Exists = file1.exists();
		if (file1Exists != file2.exists()) {
			return false;
		}

		if (!file1Exists) {
			// two not existing files are equal
			return true;
		}

		if (file1.isDirectory() || file2.isDirectory()) {
			// don't want to compare directory contents
			throw new IOException("Can't compare directories, only files");
		}

		if (file1.length() != file2.length()) {
			// lengths differ, cannot be equal
			return false;
		}

		if (file1.getCanonicalFile().equals(file2.getCanonicalFile())) {
			// same file
			return true;
		}

		InputStream input1 = null;
		InputStream input2 = null;
		try {
			input1 = new FileInputStream(file1);
			input2 = new FileInputStream(file2);
			return IOUtils.contentEquals(input1, input2);

		} finally {
			IOUtils.closeQuietly(input1);
			IOUtils.closeQuietly(input2);
		}
	}
}

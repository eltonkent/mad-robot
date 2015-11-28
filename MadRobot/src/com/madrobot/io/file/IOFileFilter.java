package com.madrobot.io.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * An interface which brings the FileFilter and FilenameFilter interfaces
 * together.
 * 
 * @since 1.0
 * @version $Id$
 */
public interface IOFileFilter extends FileFilter, FilenameFilter {

	/**
	 * Checks to see if the File should be accepted by this filter.
	 * <p>
	 * Defined in {@link java.io.FileFilter}.
	 * 
	 * @param file
	 *            the File to check
	 * @return true if this file matches the test
	 */
	@Override
	boolean accept(File file);

	/**
	 * Checks to see if the File should be accepted by this filter.
	 * <p>
	 * Defined in {@link java.io.FilenameFilter}.
	 * 
	 * @param dir
	 *            the directory File to check
	 * @param name
	 *            the filename within the directory to check
	 * @return true if this file matches the test
	 */
	@Override
	boolean accept(File dir, String name);

}
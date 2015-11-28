package com.madrobot.io.file.filter;

import java.io.File;
import java.io.Serializable;

import com.madrobot.io.file.IOFileFilter;

/**
 * A file filter that always returns true.
 * 
 * @see FileFilterUtils#trueFileFilter()
 */
public class TrueFileFilter implements IOFileFilter, Serializable {

	/**
	 * Singleton instance of true filter.
	 * 
	 * @since 1.3
	 */
	public static final IOFileFilter TRUE = new TrueFileFilter();
	/**
	 * Singleton instance of true filter. Please use the identical
	 * TrueFileFilter.TRUE constant. The new name is more JDK 1.5 friendly as it
	 * doesn't clash with other values when using static imports.
	 */
	public static final IOFileFilter INSTANCE = TRUE;

	/**
	 * Restrictive consructor.
	 */
	protected TrueFileFilter() {
	}

	/**
	 * Returns true.
	 * 
	 * @param file
	 *            the file to check (ignored)
	 * @return true
	 */
	@Override
	public boolean accept(File file) {
		return true;
	}

	/**
	 * Returns true.
	 * 
	 * @param dir
	 *            the directory to check (ignored)
	 * @param name
	 *            the filename (ignored)
	 * @return true
	 */
	@Override
	public boolean accept(File dir, String name) {
		return true;
	}

}
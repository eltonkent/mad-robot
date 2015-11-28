package com.madrobot.io.file.filter;

import java.io.File;
import java.io.Serializable;

import com.madrobot.io.file.IOFileFilter;

/**
 * This filter produces a logical NOT of the filters specified.
 * 
 * @see FileFilterUtils#notFileFilter(IOFileFilter)
 */
public class NotFileFilter extends AbstractFileFilter implements Serializable {

	/** The filter */
	private final IOFileFilter filter;

	/**
	 * Constructs a new file filter that NOTs the result of another filter.
	 * 
	 * @param filter
	 *            the filter, must not be null
	 * @throws IllegalArgumentException
	 *             if the filter is null
	 */
	public NotFileFilter(IOFileFilter filter) {
		if (filter == null) {
			throw new IllegalArgumentException("The filter must not be null");
		}
		this.filter = filter;
	}

	/**
	 * Returns the logical NOT of the underlying filter's return value for the
	 * same File.
	 * 
	 * @param file
	 *            the File to check
	 * @return true if the filter returns false
	 */
	@Override
	public boolean accept(File file) {
		return !filter.accept(file);
	}

	/**
	 * Returns the logical NOT of the underlying filter's return value for the
	 * same arguments.
	 * 
	 * @param file
	 *            the File directory
	 * @param name
	 *            the filename
	 * @return true if the filter returns false
	 */
	@Override
	public boolean accept(File file, String name) {
		return !filter.accept(file, name);
	}

	/**
	 * Provide a String representaion of this file filter.
	 * 
	 * @return a String representaion
	 */
	@Override
	public String toString() {
		return super.toString() + "(" + filter.toString() + ")";
	}

}
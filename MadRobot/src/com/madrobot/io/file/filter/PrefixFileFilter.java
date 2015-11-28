package com.madrobot.io.file.filter;

import java.io.Serializable;
import java.util.List;

/**
 * Filters filenames for a certain prefix.
 * <p>
 * For example, to print all files and directories in the current directory
 * whose name starts with <code>Test</code>:
 * 
 * <pre>
 * File dir = new File(&quot;.&quot;);
 * String[] files = dir.list(new PrefixFileFilter(&quot;Test&quot;));
 * for (int i = 0; i &lt; files.length; i++) {
 * 	System.out.println(files[i]);
 * }
 * </pre>
 * 
 * @see FileFilterUtils#prefixFileFilter(String)
 * @see FileFilterUtils#prefixFileFilter(String, IOCase)
 */
public class PrefixFileFilter extends NameFileFilter implements Serializable {

	/**
	 * Constructs a new Prefix file filter for a single prefix.
	 * 
	 * @param prefix
	 *            the prefix to allow, must not be null
	 * @throws IllegalArgumentException
	 *             if the prefix is null
	 */
	public PrefixFileFilter(String prefix) {
		super(prefix, IOCase.SENSITIVE);
	}

	/**
	 * Constructs a new Prefix file filter for a single prefix specifying
	 * case-sensitivity.
	 * 
	 * @param prefix
	 *            the prefix to allow, must not be null
	 * @param caseSensitivity
	 *            how to handle case sensitivity, null means case-sensitive
	 * @throws IllegalArgumentException
	 *             if the prefix is null
	 * @since 1.4
	 */
	public PrefixFileFilter(String prefix, IOCase caseSensitivity) {
		super(prefix, caseSensitivity);
	}

	/**
	 * Constructs a new Prefix file filter for any of an array of prefixes.
	 * <p>
	 * The array is not cloned, so could be changed after constructing the
	 * instance. This would be inadvisable however.
	 * 
	 * @param prefixes
	 *            the prefixes to allow, must not be null
	 * @throws IllegalArgumentException
	 *             if the prefix array is null
	 */
	public PrefixFileFilter(String[] prefixes) {
		this(prefixes, IOCase.SENSITIVE);
	}

	/**
	 * Constructs a new Prefix file filter for any of an array of prefixes
	 * specifying case-sensitivity.
	 * <p>
	 * The array is not cloned, so could be changed after constructing the
	 * instance. This would be inadvisable however.
	 * 
	 * @param prefixes
	 *            the prefixes to allow, must not be null
	 * @param caseSensitivity
	 *            how to handle case sensitivity, null means case-sensitive
	 * @throws IllegalArgumentException
	 *             if the prefix is null
	 * @since 1.4
	 */
	public PrefixFileFilter(String[] prefixes, IOCase caseSensitivity) {
		super(prefixes, caseSensitivity);
	}

	/**
	 * Constructs a new Prefix file filter for a list of prefixes.
	 * 
	 * @param prefixes
	 *            the prefixes to allow, must not be null
	 * @throws IllegalArgumentException
	 *             if the prefix list is null
	 * @throws ClassCastException
	 *             if the list does not contain Strings
	 */
	public PrefixFileFilter(List<String> prefixes) {
		this(prefixes, IOCase.SENSITIVE);
	}

	/**
	 * Constructs a new Prefix file filter for a list of prefixes specifying
	 * case-sensitivity.
	 * 
	 * @param prefixes
	 *            the prefixes to allow, must not be null
	 * @param caseSensitivity
	 *            how to handle case sensitivity, null means case-sensitive
	 * @throws IllegalArgumentException
	 *             if the prefix list is null
	 * @throws ClassCastException
	 *             if the list does not contain Strings
	 * @since 1.4
	 */
	public PrefixFileFilter(List<String> prefixes, IOCase caseSensitivity) {
		super(prefixes, caseSensitivity);
	}

}
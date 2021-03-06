/*******************************************************************************
 * Copyright (c) 2012 MadRobot.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the GNU Lesser Public License v2.1
 *  which accompanies this distribution, and is available at
 *  http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *  
 *  Contributors:
 *  Elton Kent - initial API and implementation
 ******************************************************************************/
package com.madrobot.di.xml.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.madrobot.di.xml.core.JVM;
import com.madrobot.util.ThreadSafeSimpleDateFormat;

/**
 * Converts a java.util.Date to a String as a date format, retaining precision
 * down to milliseconds. The formatted string is by default in UTC. You can
 * provide a different {@link TimeZone} that is used for serialization or
 * <code>null</code> to use always the current TimeZone. Note, that the default
 * format uses 3-letter time zones that can be ambiguous and may cause wrong
 * results at deserialization.
 * 
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 */
public class DateConverter extends AbstractSingleValueConverter implements ErrorReporter {

	private static final String[] DEFAULT_ACCEPTABLE_FORMATS;
	private static final String DEFAULT_PATTERN;
	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
	static {
		final String defaultPattern = "yyyy-MM-dd HH:mm:ss.S z";
		final List acceptablePatterns = new ArrayList();
		final boolean utcSupported = JVM.canParseUTCDateFormat();
		DEFAULT_PATTERN = utcSupported ? defaultPattern : "yyyy-MM-dd HH:mm:ss.S 'UTC'";
		if (!utcSupported) {
			acceptablePatterns.add(defaultPattern);
		}
		acceptablePatterns.add("yyyy-MM-dd HH:mm:ss.S a");
		// JDK 1.3 needs both versions
		acceptablePatterns.add("yyyy-MM-dd HH:mm:ssz");
		acceptablePatterns.add("yyyy-MM-dd HH:mm:ss z");
		if (!utcSupported) {
			acceptablePatterns.add("yyyy-MM-dd HH:mm:ss 'UTC'");
		}
		// backwards compatibility
		acceptablePatterns.add("yyyy-MM-dd HH:mm:ssa");
		DEFAULT_ACCEPTABLE_FORMATS = (String[]) acceptablePatterns
				.toArray(new String[acceptablePatterns.size()]);
	}
	private final ThreadSafeSimpleDateFormat[] acceptableFormats;
	private final ThreadSafeSimpleDateFormat defaultFormat;

	/**
	 * Construct a DateConverter with standard formats and lenient set off.
	 */
	public DateConverter() {
		this(false);
	}

	/**
	 * Construct a DateConverter with standard formats and using UTC.
	 * 
	 * @param lenient
	 *            the lenient setting of
	 *            {@link SimpleDateFormat#setLenient(boolean)}
	 * @since 1.3
	 */
	public DateConverter(boolean lenient) {
		this(DEFAULT_PATTERN, DEFAULT_ACCEPTABLE_FORMATS, lenient);
	}

	/**
	 * Construct a DateConverter with lenient set off using UTC.
	 * 
	 * @param defaultFormat
	 *            the default format
	 * @param acceptableFormats
	 *            fallback formats
	 */
	public DateConverter(String defaultFormat, String[] acceptableFormats) {
		this(defaultFormat, acceptableFormats, false);
	}

	/**
	 * Construct a DateConverter.
	 * 
	 * @param defaultFormat
	 *            the default format
	 * @param acceptableFormats
	 *            fallback formats
	 * @param lenient
	 *            the lenient setting of
	 *            {@link SimpleDateFormat#setLenient(boolean)}
	 * @since 1.3
	 */
	public DateConverter(String defaultFormat, String[] acceptableFormats, boolean lenient) {
		this(defaultFormat, acceptableFormats, UTC, lenient);
	}

	/**
	 * Construct a DateConverter with a given TimeZone and lenient set off.
	 * 
	 * @param defaultFormat
	 *            the default format
	 * @param acceptableFormats
	 *            fallback formats
	 * @since 1.4
	 */
	public DateConverter(String defaultFormat, String[] acceptableFormats, TimeZone timeZone) {
		this(defaultFormat, acceptableFormats, timeZone, false);
	}

	/**
	 * Construct a DateConverter.
	 * 
	 * @param defaultFormat
	 *            the default format
	 * @param acceptableFormats
	 *            fallback formats
	 * @param timeZone
	 *            the TimeZone used to serialize the Date
	 * @param lenient
	 *            the lenient setting of
	 *            {@link SimpleDateFormat#setLenient(boolean)}
	 * @since 1.4
	 */
	public DateConverter(String defaultFormat, String[] acceptableFormats, TimeZone timeZone,
			boolean lenient) {
		this.defaultFormat = new ThreadSafeSimpleDateFormat(defaultFormat, timeZone, 4, 20,
				lenient);
		this.acceptableFormats = acceptableFormats != null ? new ThreadSafeSimpleDateFormat[acceptableFormats.length]
				: new ThreadSafeSimpleDateFormat[0];
		for (int i = 0; i < this.acceptableFormats.length; i++) {
			this.acceptableFormats[i] = new ThreadSafeSimpleDateFormat(acceptableFormats[i],
					timeZone, 1, 20, lenient);
		}
	}

	/**
	 * Construct a DateConverter with standard formats, lenient set off and uses
	 * a given TimeZone for serialization.
	 * 
	 * @param timeZone
	 *            the TimeZone used to serialize the Date
	 * @since 1.4
	 */
	public DateConverter(TimeZone timeZone) {
		this(DEFAULT_PATTERN, DEFAULT_ACCEPTABLE_FORMATS, timeZone);
	}

	@Override
	public void appendErrors(ErrorWriter errorWriter) {
		errorWriter.add("Default date pattern", defaultFormat.toString());
		for (int i = 0; i < acceptableFormats.length; i++) {
			errorWriter.add("Alternative date pattern", acceptableFormats[i].toString());
		}
	}

	@Override
	public boolean canConvert(Class type) {
		return type.equals(Date.class);
	}

	@Override
	public Object fromString(String str) {
		try {
			return defaultFormat.parse(str);
		} catch (ParseException e) {
			for (int i = 0; i < acceptableFormats.length; i++) {
				try {
					return acceptableFormats[i].parse(str);
				} catch (ParseException e2) {
					// no worries, let's try the next format.
				}
			}
			// no dateFormats left to try
			throw new ConversionException("Cannot parse date " + str);
		}
	}

	@Override
	public String toString(Object obj) {
		return defaultFormat.format((Date) obj);
	}
}

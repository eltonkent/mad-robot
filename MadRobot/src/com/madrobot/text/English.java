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
package com.madrobot.text;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.madrobot.io.file.FileUtils;

/**
 * Convert primitive types like integers to their representation in the English
 * language.
 * 
 * @see {@link FileUtils#getSizeRepresentation(long)}
 * @author elton.stephen.kent
 * 
 */
public final class English {

	/**
	 * Representation of a minute. This value can be modified.
	 */
	public static String minute = "Minute";
	/**
	 * Representation of a minute (plural). This value can be modified.
	 */
	public static String minutes = "Minutes";
	/**
	 * Representation of a day. This value can be modified.
	 */
	public static String day = "Day";
	/**
	 * Representation of a day (plural). This value can be modified.
	 */
	public static String days = "Days";
	/**
	 * Representation of an hour. This value can be modified.
	 */
	public static String hour = "Hour";
	/**
	 * Representation of an hour (plural). This value can be modified.
	 */
	public static String hours = "Hours";
	/**
	 * Representation of a second. This value can be modified.
	 */
	public static String second = "Second";
	/**
	 * Representation of a second (plural). This value can be modified.
	 */
	public static String seconds = "Seconds";

	/**
	 * Convert a long value to the corresponding english
	 * <p>
	 * <b>Eg:</b> <code>longToEnglish(3456)</code> will return:
	 * <code>three thousand, four hundred sixty-five</code>
	 * </p>
	 * 
	 * @param i
	 * @return
	 */
	public static String longToEnglish(long i) {
		StringBuilder result = new StringBuilder();
		longToEnglish(i, result);
		return result.toString();
	}

	public static void longToEnglish(long i, StringBuilder result) {
		if (i == 0) {
			result.append("zero");
			return;
		}
		if (i < 0) {
			result.append("minus ");
			i = -i;
		}
		if (i >= 1000000000000000000l) { // quadrillion
			longToEnglish(i / 1000000000000000000l, result);
			result.append("quintillion, ");
			i = i % 1000000000000000000l;
		}
		if (i >= 1000000000000000l) { // quadrillion
			longToEnglish(i / 1000000000000000l, result);
			result.append("quadrillion, ");
			i = i % 1000000000000000l;
		}
		if (i >= 1000000000000l) { // trillions
			longToEnglish(i / 1000000000000l, result);
			result.append("trillion, ");
			i = i % 1000000000000l;
		}
		if (i >= 1000000000) { // billions
			longToEnglish(i / 1000000000, result);
			result.append("billion, ");
			i = i % 1000000000;
		}
		if (i >= 1000000) { // millions
			longToEnglish(i / 1000000, result);
			result.append("million, ");
			i = i % 1000000;
		}
		if (i >= 1000) { // thousands
			longToEnglish(i / 1000, result);
			result.append("thousand, ");
			i = i % 1000;
		}
		if (i >= 100) { // hundreds
			longToEnglish(i / 100, result);
			result.append("hundred ");
			i = i % 100;
		}
		// we know we are smaller here so we can cast
		if (i >= 20) {
			switch (((int) i) / 10) {
			case 9:
				result.append("ninety");
				break;
			case 8:
				result.append("eighty");
				break;
			case 7:
				result.append("seventy");
				break;
			case 6:
				result.append("sixty");
				break;
			case 5:
				result.append("fifty");
				break;
			case 4:
				result.append("forty");
				break;
			case 3:
				result.append("thirty");
				break;
			case 2:
				result.append("twenty");
				break;
			}
			i = i % 10;
			if (i == 0)
				result.append(" ");
			else
				result.append("-");
		}
		switch ((int) i) {
		case 19:
			result.append("nineteen ");
			break;
		case 18:
			result.append("eighteen ");
			break;
		case 17:
			result.append("seventeen ");
			break;
		case 16:
			result.append("sixteen ");
			break;
		case 15:
			result.append("fifteen ");
			break;
		case 14:
			result.append("fourteen ");
			break;
		case 13:
			result.append("thirteen ");
			break;
		case 12:
			result.append("twelve ");
			break;
		case 11:
			result.append("eleven ");
			break;
		case 10:
			result.append("ten ");
			break;
		case 9:
			result.append("nine ");
			break;
		case 8:
			result.append("eight ");
			break;
		case 7:
			result.append("seven ");
			break;
		case 6:
			result.append("six ");
			break;
		case 5:
			result.append("five ");
			break;
		case 4:
			result.append("four ");
			break;
		case 3:
			result.append("three ");
			break;
		case 2:
			result.append("two ");
			break;
		case 1:
			result.append("one ");
			break;
		case 0:
			result.append("");
			break;
		}
	}

	public static String timeToEnglish(long l) {

		StringBuilder stringbuilder = new StringBuilder();
		long l1 = l;
		long l2 = l1 / 0x5265c00L;
		if (l2 > 0L) {
			l1 -= l2 * 0x5265c00L;
			if (l2 == 1L)
				stringbuilder.append(Long.valueOf(l2) + day);
			else
				stringbuilder.append(Long.valueOf(l2) + days);
		}
		long l3 = l1 / 0x36ee80L;
		if (l3 > 0L) {
			l1 -= l3 * 0x36ee80L;
			if (stringbuilder.length() > 0)
				stringbuilder.append(", ");
			if (l3 == 1L)
				stringbuilder.append(Long.valueOf(l3) + hour);
			else
				stringbuilder.append(Long.valueOf(l3) + hours);
		}
		long l4 = l1 / 60000L;
		if (l4 > 0L) {
			l1 -= l4 * 60000L;
			if (stringbuilder.length() > 0)
				stringbuilder.append(", ");
			if (l4 == 1L)
				stringbuilder.append(Long.valueOf(l4) + minute);
			else
				stringbuilder.append(Long.valueOf(l4) + minutes);
		}
		if (l1 == 1000L) {
			if (stringbuilder.length() > 0)
				stringbuilder.append(", ");
			stringbuilder.append(Integer.valueOf(1) + second);
		} else if (l1 > 0L || stringbuilder.length() == 0) {
			if (stringbuilder.length() > 0)
				stringbuilder.append(", ");
			long l5 = l1 / 1000L;
			l1 -= l5 * 1000L;
			if (l1 % 1000L != 0L) {
				double d = l5 + l1 / 1000D;
				DecimalFormat decimalformat = new DecimalFormat("0.000");
				stringbuilder.append(decimalformat.format(d) + seconds);
			} else {
				stringbuilder.append(Long.valueOf(l5) + seconds);
			}
		}
		return stringbuilder.toString();
	}

	private static long[] TIME_FACTOR = { 60 * 60 * 1000, 60 * 1000, 1000 };

	/**
	 * Calculate the elapsed time between two times specified in milliseconds.
	 * <p>
	 * returns the elapsed time in XhYmZs format.
	 * </p>
	 * 
	 * @param start
	 *            The start of the time period
	 * @param end
	 *            The end of the time period
	 * @return a string of the form "XhYmZs" when the elapsed time is X hours, Y
	 *         minutes and Z seconds or null if start > end.
	 */
	public static String elapsedTimeToEnglish(long start, long end) {
		if (start > end) {
			return null;
		}

		long[] elapsedTime = new long[TIME_FACTOR.length];

		for (int i = 0; i < TIME_FACTOR.length; i++) {
			elapsedTime[i] = start > end ? -1 : (end - start) / TIME_FACTOR[i];
			start += TIME_FACTOR[i] * elapsedTime[i];
		}

		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(2);
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < elapsedTime.length; i++) {
			if (i > 0) {
				buf.append(":");
			}
			buf.append(nf.format(elapsedTime[i]));
		}
		return buf.toString();
	}

	private English() {
	} // no instance

}

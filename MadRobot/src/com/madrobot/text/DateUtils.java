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

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TimeZone;

/**
 * <p>
 * A suite of utilities surrounding the use of the {@link java.util.Calendar}
 * and {@link java.util.Date} object.
 * </p>
 * 
 * <p>
 * DateUtils contains a lot of common methods considering manipulations of Dates
 * or Calendars. Some methods require some extra explanation. The truncate,
 * ceiling and round methods could be considered the Math.floor(), Math.ceil()
 * or Math.round versions for dates This way date-fields will be ignored in
 * bottom-up order. As a complement to these methods we've introduced some
 * fragment-methods. With these methods the Date-fields will be ignored in
 * top-down order. Since a date without a year is not a valid date, you have to
 * decide in what kind of date-field you want your result, for instance
 * milliseconds or days.
 * </p>
 * 
 * 
 */
public final class DateUtils {

	/**
	 * <p>
	 * Date iterator.
	 * </p>
	 */
	static class DateIterator implements Iterator<Calendar> {
		private final Calendar endFinal;
		private final Calendar spot;

		/**
		 * Constructs a DateIterator that ranges from one date to another.
		 * 
		 * @param startFinal
		 *            start date (inclusive)
		 * @param endFinal
		 *            end date (not inclusive)
		 */
		DateIterator(Calendar startFinal, Calendar endFinal) {
			super();
			this.endFinal = endFinal;
			spot = startFinal;
			spot.add(Calendar.DATE, -1);
		}

		/**
		 * Has the iterator not reached the end date yet?
		 * 
		 * @return <code>true</code> if the iterator has yet to reach the end
		 *         date
		 */
		@Override
		public boolean hasNext() {
			return spot.before(endFinal);
		}

		/**
		 * Return the next calendar in the iteration
		 * 
		 * @return Object calendar for the next date
		 */
		@Override
		public Calendar next() {
			if (spot.equals(endFinal)) {
				throw new NoSuchElementException();
			}
			spot.add(Calendar.DATE, 1);
			return (Calendar) spot.clone();
		}

		/**
		 * Always throws UnsupportedOperationException.
		 * 
		 * @throws UnsupportedOperationException
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private static final int[][] fields = { { Calendar.MILLISECOND }, { Calendar.SECOND },
			{ Calendar.MINUTE }, { Calendar.HOUR_OF_DAY, Calendar.HOUR },
			{ Calendar.DATE, Calendar.DAY_OF_MONTH, Calendar.AM_PM
			/*
			 * Calendar.DAY_OF_YEAR, Calendar.DAY_OF_WEEK,
			 * Calendar.DAY_OF_WEEK_IN_MONTH
			 */
			}, { Calendar.MONTH, DateUtils.SEMI_MONTH }, { Calendar.YEAR }, { Calendar.ERA } };
	/**
	 * Number of milliseconds in a standard second.
	 * 
	 * @since 2.1
	 */
	public static final long MILLIS_PER_SECOND = 1000;
	/**
	 * Number of milliseconds in a standard minute.
	 * 
	 * @since 2.1
	 */
	public static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
	/**
	 * Number of milliseconds in a standard hour.
	 * 
	 * @since 2.1
	 */
	public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
	/**
	 * Number of milliseconds in a standard day.
	 * 
	 * @since 2.1
	 */
	public static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;

	/**
	 * Constant marker for ceiling
	 * 
	 * @since 3.0
	 */
	public final static int MODIFY_CEILING = 2;

	/**
	 * Constant marker for rounding
	 * 
	 * @since 3.0
	 */
	public final static int MODIFY_ROUND = 1;

	/**
	 * Constant marker for truncating
	 * 
	 * @since 3.0
	 */
	public final static int MODIFY_TRUNCATE = 0;

	/**
	 * A month range, the week starting on Monday.
	 */
	public final static int RANGE_MONTH_MONDAY = 6;

	/**
	 * A month range, the week starting on Sunday.
	 */
	public final static int RANGE_MONTH_SUNDAY = 5;

	/**
	 * A week range, centered around the day focused.
	 */
	public final static int RANGE_WEEK_CENTER = 4;

	/**
	 * A week range, starting on Monday.
	 */
	public final static int RANGE_WEEK_MONDAY = 2;

	/**
	 * A week range, starting on the day focused.
	 */
	public final static int RANGE_WEEK_RELATIVE = 3;

	/**
	 * A week range, starting on Sunday.
	 */
	public final static int RANGE_WEEK_SUNDAY = 1;

	/**
	 * This is half a month, so this represents whether a date is in the top or
	 * bottom half of the month.
	 */
	public final static int SEMI_MONTH = 1001;

	/**
	 * The UTC time zone (often referred to as GMT).
	 */
	public static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("GMT");

	// -----------------------------------------------------------------------
	/**
	 * Adds to a date returning a new object. The original date object is
	 * unchanged.
	 * 
	 * @param date
	 *            the date, not null
	 * @param calendarField
	 *            the calendar field to add to
	 * @param amount
	 *            the amount to add, may be negative
	 * @return the new date object with the amount added
	 * @throws IllegalArgumentException
	 *             if the date is null
	 */
	private static Date add(Date date, int calendarField, int amount) {
		if (date == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(calendarField, amount);
		return c.getTime();
	}

	/**
	 * Convert a date value / time value to a timestamp, using the default
	 * timezone.
	 * 
	 * @param dateValue
	 *            the date value
	 * @param nanos
	 *            the nanoseconds since midnight
	 * @return the timestamp
	 */
	public static Timestamp convertDateValueToTimestamp(long dateValue, long nanos) {
		long millis = nanos / 1000000;
		nanos -= millis * 1000000;
		long s = millis / 1000;
		millis -= s * 1000;
		long m = s / 60;
		s -= m * 60;
		long h = m / 60;
		m -= h * 60;
		int yearFromDateValue = (int) (dateValue >>> 9);
		int monthFromDateValue = (int) (dateValue >>> 5 & 15);
		int dayFromDateValue = (int) (dateValue & 31);
		long ms = getMillis(TimeZone.getDefault(), yearFromDateValue, monthFromDateValue,
				dayFromDateValue, (int) h, (int) m, (int) s, 0);
		Timestamp ts = new Timestamp(ms);
		ts.setNanos((int) (nanos + millis * 1000000));
		return ts;
	}

	/**
	 * Calculate the milliseconds for the given date and time in the specified
	 * timezone.
	 * 
	 * @param tz
	 *            the timezone
	 * @param year
	 *            the absolute year (positive or negative)
	 * @param month
	 *            the month (1-12)
	 * @param day
	 *            the day (1-31)
	 * @param hour
	 *            the hour (0-23)
	 * @param minute
	 *            the minutes (0-59)
	 * @param second
	 *            the number of seconds (0-59)
	 * @param millis
	 *            the number of milliseconds
	 * @return the number of milliseconds
	 */
	public static long getMillis(TimeZone tz, int year, int month, int day, int hour,
			int minute, int second, int millis) {
		try {
			return getTimeTry(false, tz, year, month, day, hour, minute, second, millis);
		} catch (IllegalArgumentException e) {
			// special case: if the time simply doesn't exist because of
			// daylight saving time changes, use the lenient version
			String message = e.toString();
			if (message.indexOf("HOUR_OF_DAY") > 0) {
				if (hour < 0 || hour > 23) {
					throw e;
				}
				return getTimeTry(true, tz, year, month, day, hour, minute, second, millis);
			} else if (message.indexOf("DAY_OF_MONTH") > 0) {
				int maxDay;
				if (month == 2) {
					maxDay = new GregorianCalendar().isLeapYear(year) ? 29 : 28;
				} else {
					maxDay = 30 + ((month + (month > 7 ? 1 : 0)) & 1);
				}
				if (day < 1 || day > maxDay) {
					throw e;
				}
				// DAY_OF_MONTH is thrown for years > 2037
				// using the timezone Brasilia and others,
				// for example for 2042-10-12 00:00:00.
				hour += 6;
				return getTimeTry(true, tz, year, month, day, hour, minute, second, millis);
			} else {
				return getTimeTry(true, tz, year, month, day, hour, minute, second, millis);
			}
		}
	}

	private static Calendar getCalendar() {
		if (cachedCalendar == null) {
			cachedCalendar = Calendar.getInstance();
			zoneOffset = cachedCalendar.get(Calendar.ZONE_OFFSET);
		}
		return cachedCalendar;
	}

	private static Calendar cachedCalendar;
	private static int zoneOffset;

	private static long getTimeTry(boolean lenient, TimeZone tz, int year, int month, int day,
			int hour, int minute, int second, int millis) {
		Calendar c;
		if (tz == null) {
			c = getCalendar();
		} else {
			c = Calendar.getInstance(tz);
		}
		synchronized (c) {
			c.clear();
			c.setLenient(lenient);
			if (year <= 0) {
				c.set(Calendar.ERA, GregorianCalendar.BC);
				c.set(Calendar.YEAR, 1 - year);
			} else {
				c.set(Calendar.ERA, GregorianCalendar.AD);
				c.set(Calendar.YEAR, year);
			}
			// january is 0
			c.set(Calendar.MONTH, month - 1);
			c.set(Calendar.DAY_OF_MONTH, day);
			c.set(Calendar.HOUR_OF_DAY, hour);
			c.set(Calendar.MINUTE, minute);
			c.set(Calendar.SECOND, second);
			c.set(Calendar.MILLISECOND, millis);
			return c.getTime().getTime();
		}
	}

	/**
	 * <p>
	 * Ceil this date, leaving the field specified as the most significant
	 * field.
	 * </p>
	 * 
	 * <p>
	 * For example, if you had the datetime of 28 Mar 2002 13:45:01.231, if you
	 * passed with HOUR, it would return 28 Mar 2002 13:00:00.000. If this was
	 * passed with MONTH, it would return 1 Mar 2002 0:00:00.000.
	 * </p>
	 * 
	 * @param date
	 *            the date to work with
	 * @param field
	 *            the field from <code>Calendar</code> or
	 *            <code>SEMI_MONTH</code>
	 * @return the rounded date (a different object)
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code>
	 * @throws ArithmeticException
	 *             if the year is over 280 million
	 * @since 2.5
	 */
	public static Calendar ceiling(Calendar date, int field) {
		if (date == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		Calendar ceiled = (Calendar) date.clone();
		modify(ceiled, field, MODIFY_CEILING);
		return ceiled;
	}

	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Ceil this date, leaving the field specified as the most significant
	 * field.
	 * </p>
	 * 
	 * <p>
	 * For example, if you had the datetime of 28 Mar 2002 13:45:01.231, if you
	 * passed with HOUR, it would return 28 Mar 2002 13:00:00.000. If this was
	 * passed with MONTH, it would return 1 Mar 2002 0:00:00.000.
	 * </p>
	 * 
	 * @param date
	 *            the date to work with
	 * @param field
	 *            the field from <code>Calendar</code> or
	 *            <code>SEMI_MONTH</code>
	 * @return the rounded date
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code>
	 * @throws ArithmeticException
	 *             if the year is over 280 million
	 * @since 2.5
	 */
	public static Date ceiling(Date date, int field) {
		if (date == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		Calendar gval = Calendar.getInstance();
		gval.setTime(date);
		modify(gval, field, MODIFY_CEILING);
		return gval.getTime();
	}

	/**
	 * <p>
	 * Ceil this date, leaving the field specified as the most significant
	 * field.
	 * </p>
	 * 
	 * <p>
	 * For example, if you had the datetime of 28 Mar 2002 13:45:01.231, if you
	 * passed with HOUR, it would return 28 Mar 2002 13:00:00.000. If this was
	 * passed with MONTH, it would return 1 Mar 2002 0:00:00.000.
	 * </p>
	 * 
	 * @param date
	 *            the date to work with, either <code>Date</code> or
	 *            <code>Calendar</code>
	 * @param field
	 *            the field from <code>Calendar</code> or
	 *            <code>SEMI_MONTH</code>
	 * @return the rounded date
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code>
	 * @throws ClassCastException
	 *             if the object type is not a <code>Date</code> or
	 *             <code>Calendar</code>
	 * @throws ArithmeticException
	 *             if the year is over 280 million
	 * @since 2.5
	 */
	public static Date ceiling(Object date, int field) {
		if (date == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		if (date instanceof Date) {
			return ceiling((Date) date, field);
		} else if (date instanceof Calendar) {
			return ceiling((Calendar) date, field).getTime();
		} else {
			throw new ClassCastException("Could not find ceiling of for type: "
					+ date.getClass());
		}
	}

	/**
	 * Calendar-version for fragment-calculation in any unit
	 * 
	 * @param calendar
	 *            the calendar to work with, not null
	 * @param fragment
	 *            the Calendar field part of calendar to calculate
	 * @param unit
	 *            Calendar field defining the unit
	 * @return number of units within the fragment of the calendar
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code> or fragment is not supported
	 * @since 2.4
	 */
	private static long getFragment(Calendar calendar, int fragment, int unit) {
		if (calendar == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		long millisPerUnit = getMillisPerUnit(unit);
		long result = 0;

		// Fragments bigger than a day require a breakdown to days
		switch (fragment) {
		case Calendar.YEAR:
			result += (calendar.get(Calendar.DAY_OF_YEAR) * MILLIS_PER_DAY) / millisPerUnit;
			break;
		case Calendar.MONTH:
			result += (calendar.get(Calendar.DAY_OF_MONTH) * MILLIS_PER_DAY) / millisPerUnit;
			break;
		}

		switch (fragment) {
		// Number of days already calculated for these cases
		case Calendar.YEAR:
		case Calendar.MONTH:

			// The rest of the valid cases
		case Calendar.DAY_OF_YEAR:
		case Calendar.DATE:
			result += (calendar.get(Calendar.HOUR_OF_DAY) * MILLIS_PER_HOUR) / millisPerUnit;
			//$FALL-THROUGH$
		case Calendar.HOUR_OF_DAY:
			result += (calendar.get(Calendar.MINUTE) * MILLIS_PER_MINUTE) / millisPerUnit;
			//$FALL-THROUGH$
		case Calendar.MINUTE:
			result += (calendar.get(Calendar.SECOND) * MILLIS_PER_SECOND) / millisPerUnit;
			//$FALL-THROUGH$
		case Calendar.SECOND:
			result += (calendar.get(Calendar.MILLISECOND) * 1) / millisPerUnit;
			break;
		case Calendar.MILLISECOND:
			break;// never useful
		default:
			throw new IllegalArgumentException("The fragment " + fragment
					+ " is not supported");
		}
		return result;
	}

	/**
	 * Date-version for fragment-calculation in any unit
	 * 
	 * @param date
	 *            the date to work with, not null
	 * @param fragment
	 *            the Calendar field part of date to calculate
	 * @param unit
	 *            Calendar field defining the unit
	 * @return number of units within the fragment of the date
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code> or fragment is not supported
	 * @since 2.4
	 */
	private static long getFragment(Date date, int fragment, int unit) {
		if (date == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return getFragment(calendar, fragment, unit);
	}

	/**
	 * <p>
	 * Returns the number of days within the fragment. All datefields greater
	 * than the fragment will be ignored.
	 * </p>
	 * 
	 * <p>
	 * Asking the days of any date will only return the number of days of the
	 * current month (resulting in a number between 1 and 31). This method will
	 * retrieve the number of days for any fragment. For example, if you want to
	 * calculate the number of days past this year, your fragment is
	 * Calendar.YEAR. The result will be all days of the past month(s).
	 * </p>
	 * 
	 * <p>
	 * Valid fragments are: Calendar.YEAR, Calendar.MONTH, both
	 * Calendar.DAY_OF_YEAR and Calendar.DATE, Calendar.HOUR_OF_DAY,
	 * Calendar.MINUTE, Calendar.SECOND and Calendar.MILLISECOND A fragment less
	 * than or equal to a DAY field will return 0.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>January 28, 2008 with Calendar.MONTH as fragment will return 28
	 * (equivalent to calendar.get(Calendar.DAY_OF_MONTH))</li>
	 * <li>February 28, 2008 with Calendar.MONTH as fragment will return 28
	 * (equivalent to calendar.get(Calendar.DAY_OF_MONTH))</li>
	 * <li>January 28, 2008 with Calendar.YEAR as fragment will return 28
	 * (equivalent to calendar.get(Calendar.DAY_OF_YEAR))</li>
	 * <li>February 28, 2008 with Calendar.YEAR as fragment will return 59
	 * (equivalent to calendar.get(Calendar.DAY_OF_YEAR))</li>
	 * <li>January 28, 2008 with Calendar.MILLISECOND as fragment will return 0
	 * (a millisecond cannot be split in days)</li>
	 * </ul>
	 * </p>
	 * 
	 * @param calendar
	 *            the calendar to work with, not null
	 * @param fragment
	 *            the Calendar field part of calendar to calculate
	 * @return number of days within the fragment of date
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code> or fragment is not supported
	 * @since 2.4
	 */
	public static long getFragmentInDays(Calendar calendar, int fragment) {
		return getFragment(calendar, fragment, Calendar.DAY_OF_YEAR);
	}

	/**
	 * <p>
	 * Returns the number of days within the fragment. All datefields greater
	 * than the fragment will be ignored.
	 * </p>
	 * 
	 * <p>
	 * Asking the days of any date will only return the number of days of the
	 * current month (resulting in a number between 1 and 31). This method will
	 * retrieve the number of days for any fragment. For example, if you want to
	 * calculate the number of days past this year, your fragment is
	 * Calendar.YEAR. The result will be all days of the past month(s).
	 * </p>
	 * 
	 * <p>
	 * Valid fragments are: Calendar.YEAR, Calendar.MONTH, both
	 * Calendar.DAY_OF_YEAR and Calendar.DATE, Calendar.HOUR_OF_DAY,
	 * Calendar.MINUTE, Calendar.SECOND and Calendar.MILLISECOND A fragment less
	 * than or equal to a DAY field will return 0.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>January 28, 2008 with Calendar.MONTH as fragment will return 28
	 * (equivalent to deprecated date.getDay())</li>
	 * <li>February 28, 2008 with Calendar.MONTH as fragment will return 28
	 * (equivalent to deprecated date.getDay())</li>
	 * <li>January 28, 2008 with Calendar.YEAR as fragment will return 28</li>
	 * <li>February 28, 2008 with Calendar.YEAR as fragment will return 59</li>
	 * <li>January 28, 2008 with Calendar.MILLISECOND as fragment will return 0
	 * (a millisecond cannot be split in days)</li>
	 * </ul>
	 * </p>
	 * 
	 * @param date
	 *            the date to work with, not null
	 * @param fragment
	 *            the Calendar field part of date to calculate
	 * @return number of days within the fragment of date
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code> or fragment is not supported
	 * @since 2.4
	 */
	public static long getFragmentInDays(Date date, int fragment) {
		return getFragment(date, fragment, Calendar.DAY_OF_YEAR);
	}

	/**
	 * <p>
	 * Returns the number of hours within the fragment. All datefields greater
	 * than the fragment will be ignored.
	 * </p>
	 * 
	 * <p>
	 * Asking the hours of any date will only return the number of hours of the
	 * current day (resulting in a number between 0 and 23). This method will
	 * retrieve the number of hours for any fragment. For example, if you want
	 * to calculate the number of hours past this month, your fragment is
	 * Calendar.MONTH. The result will be all hours of the past day(s).
	 * </p>
	 * 
	 * <p>
	 * Valid fragments are: Calendar.YEAR, Calendar.MONTH, both
	 * Calendar.DAY_OF_YEAR and Calendar.DATE, Calendar.HOUR_OF_DAY,
	 * Calendar.MINUTE, Calendar.SECOND and Calendar.MILLISECOND A fragment less
	 * than or equal to a HOUR field will return 0.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>January 1, 2008 7:15:10.538 with Calendar.DAY_OF_YEAR as fragment
	 * will return 7 (equivalent to calendar.get(Calendar.HOUR_OF_DAY))</li>
	 * <li>January 6, 2008 7:15:10.538 with Calendar.DAY_OF_YEAR as fragment
	 * will return 7 (equivalent to calendar.get(Calendar.HOUR_OF_DAY))</li>
	 * <li>January 1, 2008 7:15:10.538 with Calendar.MONTH as fragment will
	 * return 7</li>
	 * <li>January 6, 2008 7:15:10.538 with Calendar.MONTH as fragment will
	 * return 127 (5*24 + 7)</li>
	 * <li>January 16, 2008 7:15:10.538 with Calendar.MILLISECOND as fragment
	 * will return 0 (a millisecond cannot be split in hours)</li>
	 * </ul>
	 * </p>
	 * 
	 * @param calendar
	 *            the calendar to work with, not null
	 * @param fragment
	 *            the Calendar field part of calendar to calculate
	 * @return number of hours within the fragment of date
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code> or fragment is not supported
	 * @since 2.4
	 */
	public static long getFragmentInHours(Calendar calendar, int fragment) {
		return getFragment(calendar, fragment, Calendar.HOUR_OF_DAY);
	}

	/**
	 * <p>
	 * Returns the number of hours within the fragment. All datefields greater
	 * than the fragment will be ignored.
	 * </p>
	 * 
	 * <p>
	 * Asking the hours of any date will only return the number of hours of the
	 * current day (resulting in a number between 0 and 23). This method will
	 * retrieve the number of hours for any fragment. For example, if you want
	 * to calculate the number of hours past this month, your fragment is
	 * Calendar.MONTH. The result will be all hours of the past day(s).
	 * </p>
	 * 
	 * <p>
	 * Valid fragments are: Calendar.YEAR, Calendar.MONTH, both
	 * Calendar.DAY_OF_YEAR and Calendar.DATE, Calendar.HOUR_OF_DAY,
	 * Calendar.MINUTE, Calendar.SECOND and Calendar.MILLISECOND A fragment less
	 * than or equal to a HOUR field will return 0.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>January 1, 2008 7:15:10.538 with Calendar.DAY_OF_YEAR as fragment
	 * will return 7 (equivalent to deprecated date.getHours())</li>
	 * <li>January 6, 2008 7:15:10.538 with Calendar.DAY_OF_YEAR as fragment
	 * will return 7 (equivalent to deprecated date.getHours())</li>
	 * <li>January 1, 2008 7:15:10.538 with Calendar.MONTH as fragment will
	 * return 7</li>
	 * <li>January 6, 2008 7:15:10.538 with Calendar.MONTH as fragment will
	 * return 127 (5*24 + 7)</li>
	 * <li>January 16, 2008 7:15:10.538 with Calendar.MILLISECOND as fragment
	 * will return 0 (a millisecond cannot be split in hours)</li>
	 * </ul>
	 * </p>
	 * 
	 * @param date
	 *            the date to work with, not null
	 * @param fragment
	 *            the Calendar field part of date to calculate
	 * @return number of hours within the fragment of date
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code> or fragment is not supported
	 * @since 2.4
	 */
	public static long getFragmentInHours(Date date, int fragment) {
		return getFragment(date, fragment, Calendar.HOUR_OF_DAY);
	}

	/**
	 * <p>
	 * Returns the number of milliseconds within the fragment. All datefields
	 * greater than the fragment will be ignored.
	 * </p>
	 * 
	 * <p>
	 * Asking the milliseconds of any date will only return the number of
	 * milliseconds of the current second (resulting in a number between 0 and
	 * 999). This method will retrieve the number of milliseconds for any
	 * fragment. For example, if you want to calculate the number of seconds
	 * past today, your fragment is Calendar.DATE or Calendar.DAY_OF_YEAR. The
	 * result will be all seconds of the past hour(s), minutes(s) and second(s).
	 * </p>
	 * 
	 * <p>
	 * Valid fragments are: Calendar.YEAR, Calendar.MONTH, both
	 * Calendar.DAY_OF_YEAR and Calendar.DATE, Calendar.HOUR_OF_DAY,
	 * Calendar.MINUTE, Calendar.SECOND and Calendar.MILLISECOND A fragment less
	 * than or equal to a MILLISECOND field will return 0.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>January 1, 2008 7:15:10.538 with Calendar.SECOND as fragment will
	 * return 538 (equivalent to calendar.get(Calendar.MILLISECOND))</li>
	 * <li>January 6, 2008 7:15:10.538 with Calendar.SECOND as fragment will
	 * return 538 (equivalent to calendar.get(Calendar.MILLISECOND))</li>
	 * <li>January 6, 2008 7:15:10.538 with Calendar.MINUTE as fragment will
	 * return 10538 (10*1000 + 538)</li>
	 * <li>January 16, 2008 7:15:10.538 with Calendar.MILLISECOND as fragment
	 * will return 0 (a millisecond cannot be split in milliseconds)</li>
	 * </ul>
	 * </p>
	 * 
	 * @param calendar
	 *            the calendar to work with, not null
	 * @param fragment
	 *            the Calendar field part of calendar to calculate
	 * @return number of milliseconds within the fragment of date
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code> or fragment is not supported
	 * @since 2.4
	 */
	public static long getFragmentInMilliseconds(Calendar calendar, int fragment) {
		return getFragment(calendar, fragment, Calendar.MILLISECOND);
	}

	/**
	 * <p>
	 * Returns the number of milliseconds within the fragment. All datefields
	 * greater than the fragment will be ignored.
	 * </p>
	 * 
	 * <p>
	 * Asking the milliseconds of any date will only return the number of
	 * milliseconds of the current second (resulting in a number between 0 and
	 * 999). This method will retrieve the number of milliseconds for any
	 * fragment. For example, if you want to calculate the number of
	 * milliseconds past today, your fragment is Calendar.DATE or
	 * Calendar.DAY_OF_YEAR. The result will be all milliseconds of the past
	 * hour(s), minutes(s) and second(s).
	 * </p>
	 * 
	 * <p>
	 * Valid fragments are: Calendar.YEAR, Calendar.MONTH, both
	 * Calendar.DAY_OF_YEAR and Calendar.DATE, Calendar.HOUR_OF_DAY,
	 * Calendar.MINUTE, Calendar.SECOND and Calendar.MILLISECOND A fragment less
	 * than or equal to a SECOND field will return 0.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>January 1, 2008 7:15:10.538 with Calendar.SECOND as fragment will
	 * return 538</li>
	 * <li>January 6, 2008 7:15:10.538 with Calendar.SECOND as fragment will
	 * return 538</li>
	 * <li>January 6, 2008 7:15:10.538 with Calendar.MINUTE as fragment will
	 * return 10538 (10*1000 + 538)</li>
	 * <li>January 16, 2008 7:15:10.538 with Calendar.MILLISECOND as fragment
	 * will return 0 (a millisecond cannot be split in milliseconds)</li>
	 * </ul>
	 * </p>
	 * 
	 * @param date
	 *            the date to work with, not null
	 * @param fragment
	 *            the Calendar field part of date to calculate
	 * @return number of milliseconds within the fragment of date
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code> or fragment is not supported
	 * @since 2.4
	 */
	public static long getFragmentInMilliseconds(Date date, int fragment) {
		return getFragment(date, fragment, Calendar.MILLISECOND);
	}

	/**
	 * <p>
	 * Returns the number of minutes within the fragment. All datefields greater
	 * than the fragment will be ignored.
	 * </p>
	 * 
	 * <p>
	 * Asking the minutes of any date will only return the number of minutes of
	 * the current hour (resulting in a number between 0 and 59). This method
	 * will retrieve the number of minutes for any fragment. For example, if you
	 * want to calculate the number of minutes past this month, your fragment is
	 * Calendar.MONTH. The result will be all minutes of the past day(s) and
	 * hour(s).
	 * </p>
	 * 
	 * <p>
	 * Valid fragments are: Calendar.YEAR, Calendar.MONTH, both
	 * Calendar.DAY_OF_YEAR and Calendar.DATE, Calendar.HOUR_OF_DAY,
	 * Calendar.MINUTE, Calendar.SECOND and Calendar.MILLISECOND A fragment less
	 * than or equal to a MINUTE field will return 0.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>January 1, 2008 7:15:10.538 with Calendar.HOUR_OF_DAY as fragment
	 * will return 15 (equivalent to calendar.get(Calendar.MINUTES))</li>
	 * <li>January 6, 2008 7:15:10.538 with Calendar.HOUR_OF_DAY as fragment
	 * will return 15 (equivalent to calendar.get(Calendar.MINUTES))</li>
	 * <li>January 1, 2008 7:15:10.538 with Calendar.MONTH as fragment will
	 * return 15</li>
	 * <li>January 6, 2008 7:15:10.538 with Calendar.MONTH as fragment will
	 * return 435 (7*60 + 15)</li>
	 * <li>January 16, 2008 7:15:10.538 with Calendar.MILLISECOND as fragment
	 * will return 0 (a millisecond cannot be split in minutes)</li>
	 * </ul>
	 * </p>
	 * 
	 * @param calendar
	 *            the calendar to work with, not null
	 * @param fragment
	 *            the Calendar field part of calendar to calculate
	 * @return number of minutes within the fragment of date
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code> or fragment is not supported
	 * @since 2.4
	 */
	public static long getFragmentInMinutes(Calendar calendar, int fragment) {
		return getFragment(calendar, fragment, Calendar.MINUTE);
	}

	/**
	 * <p>
	 * Returns the number of minutes within the fragment. All datefields greater
	 * than the fragment will be ignored.
	 * </p>
	 * 
	 * <p>
	 * Asking the minutes of any date will only return the number of minutes of
	 * the current hour (resulting in a number between 0 and 59). This method
	 * will retrieve the number of minutes for any fragment. For example, if you
	 * want to calculate the number of minutes past this month, your fragment is
	 * Calendar.MONTH. The result will be all minutes of the past day(s) and
	 * hour(s).
	 * </p>
	 * 
	 * <p>
	 * Valid fragments are: Calendar.YEAR, Calendar.MONTH, both
	 * Calendar.DAY_OF_YEAR and Calendar.DATE, Calendar.HOUR_OF_DAY,
	 * Calendar.MINUTE, Calendar.SECOND and Calendar.MILLISECOND A fragment less
	 * than or equal to a MINUTE field will return 0.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>January 1, 2008 7:15:10.538 with Calendar.HOUR_OF_DAY as fragment
	 * will return 15 (equivalent to deprecated date.getMinutes())</li>
	 * <li>January 6, 2008 7:15:10.538 with Calendar.HOUR_OF_DAY as fragment
	 * will return 15 (equivalent to deprecated date.getMinutes())</li>
	 * <li>January 1, 2008 7:15:10.538 with Calendar.MONTH as fragment will
	 * return 15</li>
	 * <li>January 6, 2008 7:15:10.538 with Calendar.MONTH as fragment will
	 * return 435 (7*60 + 15)</li>
	 * <li>January 16, 2008 7:15:10.538 with Calendar.MILLISECOND as fragment
	 * will return 0 (a millisecond cannot be split in minutes)</li>
	 * </ul>
	 * </p>
	 * 
	 * @param date
	 *            the date to work with, not null
	 * @param fragment
	 *            the Calendar field part of date to calculate
	 * @return number of minutes within the fragment of date
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code> or fragment is not supported
	 * @since 2.4
	 */
	public static long getFragmentInMinutes(Date date, int fragment) {
		return getFragment(date, fragment, Calendar.MINUTE);
	}

	/**
	 * <p>
	 * Returns the number of seconds within the fragment. All datefields greater
	 * than the fragment will be ignored.
	 * </p>
	 * 
	 * <p>
	 * Asking the seconds of any date will only return the number of seconds of
	 * the current minute (resulting in a number between 0 and 59). This method
	 * will retrieve the number of seconds for any fragment. For example, if you
	 * want to calculate the number of seconds past today, your fragment is
	 * Calendar.DATE or Calendar.DAY_OF_YEAR. The result will be all seconds of
	 * the past hour(s) and minutes(s).
	 * </p>
	 * 
	 * <p>
	 * Valid fragments are: Calendar.YEAR, Calendar.MONTH, both
	 * Calendar.DAY_OF_YEAR and Calendar.DATE, Calendar.HOUR_OF_DAY,
	 * Calendar.MINUTE, Calendar.SECOND and Calendar.MILLISECOND A fragment less
	 * than or equal to a SECOND field will return 0.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>January 1, 2008 7:15:10.538 with Calendar.MINUTE as fragment will
	 * return 10 (equivalent to calendar.get(Calendar.SECOND))</li>
	 * <li>January 6, 2008 7:15:10.538 with Calendar.MINUTE as fragment will
	 * return 10 (equivalent to calendar.get(Calendar.SECOND))</li>
	 * <li>January 6, 2008 7:15:10.538 with Calendar.DAY_OF_YEAR as fragment
	 * will return 26110 (7*3600 + 15*60 + 10)</li>
	 * <li>January 16, 2008 7:15:10.538 with Calendar.MILLISECOND as fragment
	 * will return 0 (a millisecond cannot be split in seconds)</li>
	 * </ul>
	 * </p>
	 * 
	 * @param calendar
	 *            the calendar to work with, not null
	 * @param fragment
	 *            the Calendar field part of calendar to calculate
	 * @return number of seconds within the fragment of date
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code> or fragment is not supported
	 * @since 2.4
	 */
	public static long getFragmentInSeconds(Calendar calendar, int fragment) {
		return getFragment(calendar, fragment, Calendar.SECOND);
	}

	/**
	 * <p>
	 * Returns the number of seconds within the fragment. All datefields greater
	 * than the fragment will be ignored.
	 * </p>
	 * 
	 * <p>
	 * Asking the seconds of any date will only return the number of seconds of
	 * the current minute (resulting in a number between 0 and 59). This method
	 * will retrieve the number of seconds for any fragment. For example, if you
	 * want to calculate the number of seconds past today, your fragment is
	 * Calendar.DATE or Calendar.DAY_OF_YEAR. The result will be all seconds of
	 * the past hour(s) and minutes(s).
	 * </p>
	 * 
	 * <p>
	 * Valid fragments are: Calendar.YEAR, Calendar.MONTH, both
	 * Calendar.DAY_OF_YEAR and Calendar.DATE, Calendar.HOUR_OF_DAY,
	 * Calendar.MINUTE, Calendar.SECOND and Calendar.MILLISECOND A fragment less
	 * than or equal to a SECOND field will return 0.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>January 1, 2008 7:15:10.538 with Calendar.MINUTE as fragment will
	 * return 10 (equivalent to deprecated date.getSeconds())</li>
	 * <li>January 6, 2008 7:15:10.538 with Calendar.MINUTE as fragment will
	 * return 10 (equivalent to deprecated date.getSeconds())</li>
	 * <li>January 6, 2008 7:15:10.538 with Calendar.DAY_OF_YEAR as fragment
	 * will return 26110 (7*3600 + 15*60 + 10)</li>
	 * <li>January 16, 2008 7:15:10.538 with Calendar.MILLISECOND as fragment
	 * will return 0 (a millisecond cannot be split in seconds)</li>
	 * </ul>
	 * </p>
	 * 
	 * @param date
	 *            the date to work with, not null
	 * @param fragment
	 *            the Calendar field part of date to calculate
	 * @return number of seconds within the fragment of date
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code> or fragment is not supported
	 * @since 2.4
	 */
	public static long getFragmentInSeconds(Date date, int fragment) {
		return getFragment(date, fragment, Calendar.SECOND);
	}

	/**
	 * Returns the number of millis of a datefield, if this is a constant value
	 * 
	 * @param unit
	 *            A Calendar field which is a valid unit for a fragment
	 * @return number of millis
	 * @throws IllegalArgumentException
	 *             if date can't be represented in millisenconds
	 * @since 2.4
	 */
	private static long getMillisPerUnit(int unit) {
		long result = Long.MAX_VALUE;
		switch (unit) {
		case Calendar.DAY_OF_YEAR:
		case Calendar.DATE:
			result = MILLIS_PER_DAY;
			break;
		case Calendar.HOUR_OF_DAY:
			result = MILLIS_PER_HOUR;
			break;
		case Calendar.MINUTE:
			result = MILLIS_PER_MINUTE;
			break;
		case Calendar.SECOND:
			result = MILLIS_PER_SECOND;
			break;
		case Calendar.MILLISECOND:
			result = 1;
			break;
		default:
			throw new IllegalArgumentException("The unit " + unit
					+ " cannot be represented is milleseconds");
		}
		return result;
	}

	/**
	 * <p>
	 * Checks if two calendar objects are on the same day ignoring time.
	 * </p>
	 * 
	 * <p>
	 * 28 Mar 2002 13:45 and 28 Mar 2002 06:01 would return true. 28 Mar 2002
	 * 13:45 and 12 Mar 2002 13:45 would return false.
	 * </p>
	 * 
	 * @param cal1
	 *            the first calendar, not altered, not null
	 * @param cal2
	 *            the second calendar, not altered, not null
	 * @return true if they represent the same day
	 * @throws IllegalArgumentException
	 *             if either calendar is <code>null</code>
	 * @since 2.1
	 */
	public static boolean isSameDay(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
				&& cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1
					.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
	}

	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Checks if two date objects are on the same day ignoring time.
	 * </p>
	 * 
	 * <p>
	 * 28 Mar 2002 13:45 and 28 Mar 2002 06:01 would return true. 28 Mar 2002
	 * 13:45 and 12 Mar 2002 13:45 would return false.
	 * </p>
	 * 
	 * @param date1
	 *            the first date, not altered, not null
	 * @param date2
	 *            the second date, not altered, not null
	 * @return true if they represent the same day
	 * @throws IllegalArgumentException
	 *             if either date is <code>null</code>
	 * @since 2.1
	 */
	public static boolean isSameDay(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		return isSameDay(cal1, cal2);
	}

	public static boolean isDateThisYear(Date date) {
		Calendar d = Calendar.getInstance();
		d.setTime(date);
		Calendar today = Calendar.getInstance();

		return d.get(Calendar.YEAR) == today.get(Calendar.YEAR);
	}

	/**
	 * <p>
	 * Checks if two calendar objects represent the same instant in time.
	 * </p>
	 * 
	 * <p>
	 * This method compares the long millisecond time of the two objects.
	 * </p>
	 * 
	 * @param cal1
	 *            the first calendar, not altered, not null
	 * @param cal2
	 *            the second calendar, not altered, not null
	 * @return true if they represent the same millisecond instant
	 * @throws IllegalArgumentException
	 *             if either date is <code>null</code>
	 * @since 2.1
	 */
	public static boolean isSameInstant(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		return cal1.getTime().getTime() == cal2.getTime().getTime();
	}

	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Checks if two date objects represent the same instant in time.
	 * </p>
	 * 
	 * <p>
	 * This method compares the long millisecond time of the two objects.
	 * </p>
	 * 
	 * @param date1
	 *            the first date, not altered, not null
	 * @param date2
	 *            the second date, not altered, not null
	 * @return true if they represent the same millisecond instant
	 * @throws IllegalArgumentException
	 *             if either date is <code>null</code>
	 * @since 2.1
	 */
	public static boolean isSameInstant(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		return date1.getTime() == date2.getTime();
	}

	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Checks if two calendar objects represent the same local time.
	 * </p>
	 * 
	 * <p>
	 * This method compares the values of the fields of the two objects. In
	 * addition, both calendars must be the same of the same type.
	 * </p>
	 * 
	 * @param cal1
	 *            the first calendar, not altered, not null
	 * @param cal2
	 *            the second calendar, not altered, not null
	 * @return true if they represent the same millisecond instant
	 * @throws IllegalArgumentException
	 *             if either date is <code>null</code>
	 * @since 2.1
	 */
	public static boolean isSameLocalTime(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		return (cal1.get(Calendar.MILLISECOND) == cal2.get(Calendar.MILLISECOND)
				&& cal1.get(Calendar.SECOND) == cal2.get(Calendar.SECOND)
				&& cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE)
				&& cal1.get(Calendar.HOUR) == cal2.get(Calendar.HOUR)
				&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
				&& cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) && cal1.getClass() == cal2
				.getClass());
	}

	/**
	 * <p>
	 * This constructs an <code>Iterator</code> over each day in a date range
	 * defined by a focus date and range style.
	 * </p>
	 * 
	 * <p>
	 * For instance, passing Thursday, July 4, 2002 and a
	 * <code>RANGE_MONTH_SUNDAY</code> will return an <code>Iterator</code> that
	 * starts with Sunday, June 30, 2002 and ends with Saturday, August 3, 2002,
	 * returning a Calendar instance for each intermediate day.
	 * </p>
	 * 
	 * <p>
	 * This method provides an iterator that returns Calendar objects. The days
	 * are progressed using {@link Calendar#add(int, int)}.
	 * </p>
	 * 
	 * @param focus
	 *            the date to work with
	 * @param rangeStyle
	 *            the style constant to use. Must be one of
	 *            {@link DateUtils#RANGE_MONTH_SUNDAY},
	 *            {@link DateUtils#RANGE_MONTH_MONDAY},
	 *            {@link DateUtils#RANGE_WEEK_SUNDAY},
	 *            {@link DateUtils#RANGE_WEEK_MONDAY},
	 *            {@link DateUtils#RANGE_WEEK_RELATIVE},
	 *            {@link DateUtils#RANGE_WEEK_CENTER}
	 * @return the date iterator
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the rangeStyle is invalid
	 */
	public static Iterator<Calendar> iterator(Calendar focus, int rangeStyle) {
		if (focus == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		Calendar start = null;
		Calendar end = null;
		int startCutoff = Calendar.SUNDAY;
		int endCutoff = Calendar.SATURDAY;
		switch (rangeStyle) {
		case RANGE_MONTH_SUNDAY:
		case RANGE_MONTH_MONDAY:
			// Set start to the first of the month
			start = truncate(focus, Calendar.MONTH);
			// Set end to the last of the month
			end = (Calendar) start.clone();
			end.add(Calendar.MONTH, 1);
			end.add(Calendar.DATE, -1);
			// Loop start back to the previous sunday or monday
			if (rangeStyle == RANGE_MONTH_MONDAY) {
				startCutoff = Calendar.MONDAY;
				endCutoff = Calendar.SUNDAY;
			}
			break;
		case RANGE_WEEK_SUNDAY:
		case RANGE_WEEK_MONDAY:
		case RANGE_WEEK_RELATIVE:
		case RANGE_WEEK_CENTER:
			// Set start and end to the current date
			start = truncate(focus, Calendar.DATE);
			end = truncate(focus, Calendar.DATE);
			switch (rangeStyle) {
			case RANGE_WEEK_SUNDAY:
				// already set by default
				break;
			case RANGE_WEEK_MONDAY:
				startCutoff = Calendar.MONDAY;
				endCutoff = Calendar.SUNDAY;
				break;
			case RANGE_WEEK_RELATIVE:
				startCutoff = focus.get(Calendar.DAY_OF_WEEK);
				endCutoff = startCutoff - 1;
				break;
			case RANGE_WEEK_CENTER:
				startCutoff = focus.get(Calendar.DAY_OF_WEEK) - 3;
				endCutoff = focus.get(Calendar.DAY_OF_WEEK) + 3;
				break;
			}
			break;
		default:
			throw new IllegalArgumentException("The range style " + rangeStyle
					+ " is not valid.");
		}
		if (startCutoff < Calendar.SUNDAY) {
			startCutoff += 7;
		}
		if (startCutoff > Calendar.SATURDAY) {
			startCutoff -= 7;
		}
		if (endCutoff < Calendar.SUNDAY) {
			endCutoff += 7;
		}
		if (endCutoff > Calendar.SATURDAY) {
			endCutoff -= 7;
		}
		while (start.get(Calendar.DAY_OF_WEEK) != startCutoff) {
			start.add(Calendar.DATE, -1);
		}
		while (end.get(Calendar.DAY_OF_WEEK) != endCutoff) {
			end.add(Calendar.DATE, 1);
		}
		return new DateIterator(start, end);
	}

	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * This constructs an <code>Iterator</code> over each day in a date range
	 * defined by a focus date and range style.
	 * </p>
	 * 
	 * <p>
	 * For instance, passing Thursday, July 4, 2002 and a
	 * <code>RANGE_MONTH_SUNDAY</code> will return an <code>Iterator</code> that
	 * starts with Sunday, June 30, 2002 and ends with Saturday, August 3, 2002,
	 * returning a Calendar instance for each intermediate day.
	 * </p>
	 * 
	 * <p>
	 * This method provides an iterator that returns Calendar objects. The days
	 * are progressed using {@link Calendar#add(int, int)}.
	 * </p>
	 * 
	 * @param focus
	 *            the date to work with, not null
	 * @param rangeStyle
	 *            the style constant to use. Must be one of
	 *            {@link DateUtils#RANGE_MONTH_SUNDAY},
	 *            {@link DateUtils#RANGE_MONTH_MONDAY},
	 *            {@link DateUtils#RANGE_WEEK_SUNDAY},
	 *            {@link DateUtils#RANGE_WEEK_MONDAY},
	 *            {@link DateUtils#RANGE_WEEK_RELATIVE},
	 *            {@link DateUtils#RANGE_WEEK_CENTER}
	 * @return the date iterator, which always returns Calendar instances
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the rangeStyle is invalid
	 */
	public static Iterator<Calendar> iterator(Date focus, int rangeStyle) {
		if (focus == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		Calendar gval = Calendar.getInstance();
		gval.setTime(focus);
		return iterator(gval, rangeStyle);
	}

	/**
	 * <p>
	 * This constructs an <code>Iterator</code> over each day in a date range
	 * defined by a focus date and range style.
	 * </p>
	 * 
	 * <p>
	 * For instance, passing Thursday, July 4, 2002 and a
	 * <code>RANGE_MONTH_SUNDAY</code> will return an <code>Iterator</code> that
	 * starts with Sunday, June 30, 2002 and ends with Saturday, August 3, 2002,
	 * returning a Calendar instance for each intermediate day.
	 * </p>
	 * 
	 * @param focus
	 *            the date to work with, either <code>Date</code> or
	 *            <code>Calendar</code>
	 * @param rangeStyle
	 *            the style constant to use. Must be one of the range styles
	 *            listed for the {@link #iterator(Calendar, int)} method.
	 * @return the date iterator
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code>
	 * @throws ClassCastException
	 *             if the object type is not a <code>Date</code> or
	 *             <code>Calendar</code>
	 */
	public static Iterator<?> iterator(Object focus, int rangeStyle) {
		if (focus == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		if (focus instanceof Date) {
			return iterator((Date) focus, rangeStyle);
		} else if (focus instanceof Calendar) {
			return iterator((Calendar) focus, rangeStyle);
		} else {
			throw new ClassCastException("Could not iterate based on " + focus);
		}
	}

	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Internal calculation method.
	 * </p>
	 * 
	 * @param val
	 *            the calendar
	 * @param field
	 *            the field constant
	 * @param modType
	 *            type to truncate, round or ceiling
	 * @throws ArithmeticException
	 *             if the year is over 280 million
	 */
	private static void modify(Calendar val, int field, int modType) {
		if (val.get(Calendar.YEAR) > 280000000) {
			throw new ArithmeticException("Calendar value too large for accurate calculations");
		}

		if (field == Calendar.MILLISECOND) {
			return;
		}

		// ----------------- Fix for LANG-59 ---------------------- START
		// ---------------
		// see http://issues.apache.org/jira/browse/LANG-59
		//
		// Manually truncate milliseconds, seconds and minutes, rather than
		// using
		// Calendar methods.

		Date date = val.getTime();
		long time = date.getTime();
		boolean done = false;

		// truncate milliseconds
		int millisecs = val.get(Calendar.MILLISECOND);
		if (MODIFY_TRUNCATE == modType || millisecs < 500) {
			time = time - millisecs;
		}
		if (field == Calendar.SECOND) {
			done = true;
		}

		// truncate seconds
		int seconds = val.get(Calendar.SECOND);
		if (!done && (MODIFY_TRUNCATE == modType || seconds < 30)) {
			time = time - (seconds * 1000L);
		}
		if (field == Calendar.MINUTE) {
			done = true;
		}

		// truncate minutes
		int minutes = val.get(Calendar.MINUTE);
		if (!done && (MODIFY_TRUNCATE == modType || minutes < 30)) {
			time = time - (minutes * 60000L);
		}

		// reset time
		if (date.getTime() != time) {
			date.setTime(time);
			val.setTime(date);
		}
		// ----------------- Fix for LANG-59 ----------------------- END
		// ----------------

		boolean roundUp = false;
		for (int i = 0; i < fields.length; i++) {
			for (int j = 0; j < fields[i].length; j++) {
				if (fields[i][j] == field) {
					// This is our field... we stop looping
					if (modType == MODIFY_CEILING || (modType == MODIFY_ROUND && roundUp)) {
						if (field == DateUtils.SEMI_MONTH) {
							// This is a special case that's hard to generalize
							// If the date is 1, we round up to 16, otherwise
							// we subtract 15 days and add 1 month
							if (val.get(Calendar.DATE) == 1) {
								val.add(Calendar.DATE, 15);
							} else {
								val.add(Calendar.DATE, -15);
								val.add(Calendar.MONTH, 1);
							}
							// ----------------- Fix for LANG-440
							// ---------------------- START ---------------
						} else if (field == Calendar.AM_PM) {
							// This is a special case
							// If the time is 0, we round up to 12, otherwise
							// we subtract 12 hours and add 1 day
							if (val.get(Calendar.HOUR_OF_DAY) == 0) {
								val.add(Calendar.HOUR_OF_DAY, 12);
							} else {
								val.add(Calendar.HOUR_OF_DAY, -12);
								val.add(Calendar.DATE, 1);
							}
							// ----------------- Fix for LANG-440
							// ---------------------- END ---------------
						} else {
							// We need at add one to this field since the
							// last number causes us to round up
							val.add(fields[i][0], 1);
						}
					}
					return;
				}
			}
			// We have various fields that are not easy roundings
			int offset = 0;
			boolean offsetSet = false;
			// These are special types of fields that require different rounding
			// rules
			switch (field) {
			case DateUtils.SEMI_MONTH:
				if (fields[i][0] == Calendar.DATE) {
					// If we're going to drop the DATE field's value,
					// we want to do this our own way.
					// We need to subtrace 1 since the date has a minimum of 1
					offset = val.get(Calendar.DATE) - 1;
					// If we're above 15 days adjustment, that means we're in
					// the
					// bottom half of the month and should stay accordingly.
					if (offset >= 15) {
						offset -= 15;
					}
					// Record whether we're in the top or bottom half of that
					// range
					roundUp = offset > 7;
					offsetSet = true;
				}
				break;
			case Calendar.AM_PM:
				if (fields[i][0] == Calendar.HOUR_OF_DAY) {
					// If we're going to drop the HOUR field's value,
					// we want to do this our own way.
					offset = val.get(Calendar.HOUR_OF_DAY);
					if (offset >= 12) {
						offset -= 12;
					}
					roundUp = offset >= 6;
					offsetSet = true;
				}
				break;
			}
			if (!offsetSet) {
				int min = val.getActualMinimum(fields[i][0]);
				int max = val.getActualMaximum(fields[i][0]);
				// Calculate the offset from the minimum allowed value
				offset = val.get(fields[i][0]) - min;
				// Set roundUp if this is more than half way between the minimum
				// and maximum
				roundUp = offset > ((max - min) / 2);
			}
			// We need to remove this field
			if (offset != 0) {
				val.set(fields[i][0], val.get(fields[i][0]) - offset);
			}
		}
		throw new IllegalArgumentException("The field " + field + " is not supported");

	}

	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Parses a string representing a date by trying a variety of different
	 * parsers.
	 * </p>
	 * 
	 * <p>
	 * The parse will try each parse pattern in turn. A parse is only deemed
	 * successful if it parses the whole of the input string. If no parse
	 * patterns match, a ParseException is thrown.
	 * </p>
	 * The parser will be lenient toward the parsed date.
	 * 
	 * @param str
	 *            the date to parse, not null
	 * @param parsePatterns
	 *            the date format patterns to use, see SimpleDateFormat, not
	 *            null
	 * @return the parsed date
	 * @throws IllegalArgumentException
	 *             if the date string or pattern array is null
	 * @throws ParseException
	 *             if none of the date patterns were suitable (or there were
	 *             none)
	 */
	public static Date parseDate(String str, String... parsePatterns) throws ParseException {
		return parseDateWithLeniency(str, parsePatterns, true);
	}

	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Parses a string representing a date by trying a variety of different
	 * parsers.
	 * </p>
	 * 
	 * <p>
	 * The parse will try each parse pattern in turn. A parse is only deemed
	 * successful if it parses the whole of the input string. If no parse
	 * patterns match, a ParseException is thrown.
	 * </p>
	 * The parser parses strictly - it does not allow for dates such as
	 * "February 942, 1996".
	 * 
	 * @param str
	 *            the date to parse, not null
	 * @param parsePatterns
	 *            the date format patterns to use, see SimpleDateFormat, not
	 *            null
	 * @return the parsed date
	 * @throws IllegalArgumentException
	 *             if the date string or pattern array is null
	 * @throws ParseException
	 *             if none of the date patterns were suitable
	 * @since 2.5
	 */
	public static Date parseDateStrictly(String str, String... parsePatterns)
			throws ParseException {
		return parseDateWithLeniency(str, parsePatterns, false);
	}

	/**
	 * <p>
	 * Parses a string representing a date by trying a variety of different
	 * parsers.
	 * </p>
	 * 
	 * <p>
	 * The parse will try each parse pattern in turn. A parse is only deemed
	 * successful if it parses the whole of the input string. If no parse
	 * patterns match, a ParseException is thrown.
	 * </p>
	 * 
	 * @param str
	 *            the date to parse, not null
	 * @param parsePatterns
	 *            the date format patterns to use, see SimpleDateFormat, not
	 *            null
	 * @param lenient
	 *            Specify whether or not date/time parsing is to be lenient.
	 * @return the parsed date
	 * @throws IllegalArgumentException
	 *             if the date string or pattern array is null
	 * @throws ParseException
	 *             if none of the date patterns were suitable
	 * @see java.util.Calender#isLenient()
	 */
	private static Date parseDateWithLeniency(String str, String[] parsePatterns,
			boolean lenient) throws ParseException {
		if (str == null || parsePatterns == null) {
			throw new IllegalArgumentException("Date and Patterns must not be null");
		}

		SimpleDateFormat parser = new SimpleDateFormat();
		parser.setLenient(lenient);
		ParsePosition pos = new ParsePosition(0);
		for (int i = 0; i < parsePatterns.length; i++) {

			String pattern = parsePatterns[i];

			// LANG-530 - need to make sure 'ZZ' output doesn't get passed to
			// SimpleDateFormat
			if (parsePatterns[i].endsWith("ZZ")) {
				pattern = pattern.substring(0, pattern.length() - 1);
			}

			parser.applyPattern(pattern);
			pos.setIndex(0);

			String str2 = str;
			// LANG-530 - need to make sure 'ZZ' output doesn't hit
			// SimpleDateFormat as it will ParseException
			if (parsePatterns[i].endsWith("ZZ")) {
				str2 = str.replaceAll("([-+][0-9][0-9]):([0-9][0-9])$", "$1$2");
			}

			Date date = parser.parse(str2, pos);
			if (date != null && pos.getIndex() == str2.length()) {
				return date;
			}
		}
		throw new ParseException("Unable to parse the date: " + str, -1);
	}

	/**
	 * <p>
	 * Round this date, leaving the field specified as the most significant
	 * field.
	 * </p>
	 * 
	 * <p>
	 * For example, if you had the datetime of 28 Mar 2002 13:45:01.231, if this
	 * was passed with HOUR, it would return 28 Mar 2002 14:00:00.000. If this
	 * was passed with MONTH, it would return 1 April 2002 0:00:00.000.
	 * </p>
	 * 
	 * <p>
	 * For a date in a timezone that handles the change to daylight saving time,
	 * rounding to Calendar.HOUR_OF_DAY will behave as follows. Suppose daylight
	 * saving time begins at 02:00 on March 30. Rounding a date that crosses
	 * this time would produce the following values:
	 * <ul>
	 * <li>March 30, 2003 01:10 rounds to March 30, 2003 01:00</li>
	 * <li>March 30, 2003 01:40 rounds to March 30, 2003 03:00</li>
	 * <li>March 30, 2003 02:10 rounds to March 30, 2003 03:00</li>
	 * <li>March 30, 2003 02:40 rounds to March 30, 2003 04:00</li>
	 * </ul>
	 * </p>
	 * 
	 * @param date
	 *            the date to work with
	 * @param field
	 *            the field from <code>Calendar</code> or
	 *            <code>SEMI_MONTH</code>
	 * @return the rounded date (a different object)
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code>
	 * @throws ArithmeticException
	 *             if the year is over 280 million
	 */
	public static Calendar round(Calendar date, int field) {
		if (date == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		Calendar rounded = (Calendar) date.clone();
		modify(rounded, field, MODIFY_ROUND);
		return rounded;
	}

	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Round this date, leaving the field specified as the most significant
	 * field.
	 * </p>
	 * 
	 * <p>
	 * For example, if you had the datetime of 28 Mar 2002 13:45:01.231, if this
	 * was passed with HOUR, it would return 28 Mar 2002 14:00:00.000. If this
	 * was passed with MONTH, it would return 1 April 2002 0:00:00.000.
	 * </p>
	 * 
	 * <p>
	 * For a date in a timezone that handles the change to daylight saving time,
	 * rounding to Calendar.HOUR_OF_DAY will behave as follows. Suppose daylight
	 * saving time begins at 02:00 on March 30. Rounding a date that crosses
	 * this time would produce the following values:
	 * <ul>
	 * <li>March 30, 2003 01:10 rounds to March 30, 2003 01:00</li>
	 * <li>March 30, 2003 01:40 rounds to March 30, 2003 03:00</li>
	 * <li>March 30, 2003 02:10 rounds to March 30, 2003 03:00</li>
	 * <li>March 30, 2003 02:40 rounds to March 30, 2003 04:00</li>
	 * </ul>
	 * </p>
	 * 
	 * @param date
	 *            the date to work with
	 * @param field
	 *            the field from <code>Calendar</code> or
	 *            <code>SEMI_MONTH</code>
	 * @return the rounded date
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code>
	 * @throws ArithmeticException
	 *             if the year is over 280 million
	 */
	public static Date round(Date date, int field) {
		if (date == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		Calendar gval = Calendar.getInstance();
		gval.setTime(date);
		modify(gval, field, MODIFY_ROUND);
		return gval.getTime();
	}

	/**
	 * <p>
	 * Round this date, leaving the field specified as the most significant
	 * field.
	 * </p>
	 * 
	 * <p>
	 * For example, if you had the datetime of 28 Mar 2002 13:45:01.231, if this
	 * was passed with HOUR, it would return 28 Mar 2002 14:00:00.000. If this
	 * was passed with MONTH, it would return 1 April 2002 0:00:00.000.
	 * </p>
	 * 
	 * <p>
	 * For a date in a timezone that handles the change to daylight saving time,
	 * rounding to Calendar.HOUR_OF_DAY will behave as follows. Suppose daylight
	 * saving time begins at 02:00 on March 30. Rounding a date that crosses
	 * this time would produce the following values:
	 * <ul>
	 * <li>March 30, 2003 01:10 rounds to March 30, 2003 01:00</li>
	 * <li>March 30, 2003 01:40 rounds to March 30, 2003 03:00</li>
	 * <li>March 30, 2003 02:10 rounds to March 30, 2003 03:00</li>
	 * <li>March 30, 2003 02:40 rounds to March 30, 2003 04:00</li>
	 * </ul>
	 * </p>
	 * 
	 * @param date
	 *            the date to work with, either Date or Calendar
	 * @param field
	 *            the field from <code>Calendar</code> or
	 *            <code>SEMI_MONTH</code>
	 * @return the rounded date
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code>
	 * @throws ClassCastException
	 *             if the object type is not a <code>Date</code> or
	 *             <code>Calendar</code>
	 * @throws ArithmeticException
	 *             if the year is over 280 million
	 */
	public static Date round(Object date, int field) {
		if (date == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		if (date instanceof Date) {
			return round((Date) date, field);
		} else if (date instanceof Calendar) {
			return round((Calendar) date, field).getTime();
		} else {
			throw new ClassCastException("Could not round " + date);
		}
	}

	// -----------------------------------------------------------------------
	/**
	 * Sets the specified field to a date returning a new object. This does not
	 * use a lenient calendar. The original date object is unchanged.
	 * 
	 * @param date
	 *            the date, not null
	 * @param calendarField
	 *            the calendar field to set the amount to
	 * @param amount
	 *            the amount to set
	 * @return a new Date object set with the specified value
	 * @throws IllegalArgumentException
	 *             if the date is null
	 * @since 2.4
	 */
	private static Date set(Date date, int calendarField, int amount) {
		if (date == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		// getInstance() returns a new object, so this method is thread safe.
		Calendar c = Calendar.getInstance();
		c.setLenient(false);
		c.setTime(date);
		c.set(calendarField, amount);
		return c.getTime();
	}

	// -----------------------------------------------------------------------

	// -----------------------------------------------------------------------
	/**
	 * Convert a Date into a Calendar object.
	 * 
	 * @param date
	 *            the date to convert to a Calendar
	 * @return the created Calendar
	 * @throws NullPointerException
	 *             if null is passed in
	 */
	public static Calendar toCalendar(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c;
	}

	/**
	 * <p>
	 * Truncate this date, leaving the field specified as the most significant
	 * field.
	 * </p>
	 * 
	 * <p>
	 * For example, if you had the datetime of 28 Mar 2002 13:45:01.231, if you
	 * passed with HOUR, it would return 28 Mar 2002 13:00:00.000. If this was
	 * passed with MONTH, it would return 1 Mar 2002 0:00:00.000.
	 * </p>
	 * 
	 * @param date
	 *            the date to work with
	 * @param field
	 *            the field from <code>Calendar</code> or
	 *            <code>SEMI_MONTH</code>
	 * @return the rounded date (a different object)
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code>
	 * @throws ArithmeticException
	 *             if the year is over 280 million
	 */
	public static Calendar truncate(Calendar date, int field) {
		if (date == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		Calendar truncated = (Calendar) date.clone();
		modify(truncated, field, MODIFY_TRUNCATE);
		return truncated;
	}

	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Truncate this date, leaving the field specified as the most significant
	 * field.
	 * </p>
	 * 
	 * <p>
	 * For example, if you had the datetime of 28 Mar 2002 13:45:01.231, if you
	 * passed with HOUR, it would return 28 Mar 2002 13:00:00.000. If this was
	 * passed with MONTH, it would return 1 Mar 2002 0:00:00.000.
	 * </p>
	 * 
	 * @param date
	 *            the date to work with
	 * @param field
	 *            the field from <code>Calendar</code> or
	 *            <code>SEMI_MONTH</code>
	 * @return the rounded date
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code>
	 * @throws ArithmeticException
	 *             if the year is over 280 million
	 */
	public static Date truncate(Date date, int field) {
		if (date == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		Calendar gval = Calendar.getInstance();
		gval.setTime(date);
		modify(gval, field, MODIFY_TRUNCATE);
		return gval.getTime();
	}

	/**
	 * <p>
	 * Truncate this date, leaving the field specified as the most significant
	 * field.
	 * </p>
	 * 
	 * <p>
	 * For example, if you had the datetime of 28 Mar 2002 13:45:01.231, if you
	 * passed with HOUR, it would return 28 Mar 2002 13:00:00.000. If this was
	 * passed with MONTH, it would return 1 Mar 2002 0:00:00.000.
	 * </p>
	 * 
	 * @param date
	 *            the date to work with, either <code>Date</code> or
	 *            <code>Calendar</code>
	 * @param field
	 *            the field from <code>Calendar</code> or
	 *            <code>SEMI_MONTH</code>
	 * @return the rounded date
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code>
	 * @throws ClassCastException
	 *             if the object type is not a <code>Date</code> or
	 *             <code>Calendar</code>
	 * @throws ArithmeticException
	 *             if the year is over 280 million
	 */
	public static Date truncate(Object date, int field) {
		if (date == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		if (date instanceof Date) {
			return truncate((Date) date, field);
		} else if (date instanceof Calendar) {
			return truncate((Calendar) date, field).getTime();
		} else {
			throw new ClassCastException("Could not truncate " + date);
		}
	}

	/**
	 * Determines how two calendars compare up to no more than the specified
	 * most significant field.
	 * 
	 * @param cal1
	 *            the first calendar, not <code>null</code>
	 * @param cal2
	 *            the second calendar, not <code>null</code>
	 * @param field
	 *            the field from <code>Calendar</code>
	 * @return a negative integer, zero, or a positive integer as the first
	 *         calendar is less than, equal to, or greater than the second.
	 * @throws IllegalArgumentException
	 *             if any argument is <code>null</code>
	 * @see #truncate(Calendar, int)
	 * @see #truncatedCompareTo(Date, Date, int)
	 * @since 3.0
	 */
	public static int truncatedCompareTo(Calendar cal1, Calendar cal2, int field) {
		Calendar truncatedCal1 = truncate(cal1, field);
		Calendar truncatedCal2 = truncate(cal2, field);
		return truncatedCal1.compareTo(truncatedCal2);
	}

	/**
	 * Determines how two dates compare up to no more than the specified most
	 * significant field.
	 * 
	 * @param date1
	 *            the first date, not <code>null</code>
	 * @param date2
	 *            the second date, not <code>null</code>
	 * @param field
	 *            the field from <code>Calendar</code>
	 * @return a negative integer, zero, or a positive integer as the first date
	 *         is less than, equal to, or greater than the second.
	 * @throws IllegalArgumentException
	 *             if any argument is <code>null</code>
	 * @see #truncate(Calendar, int)
	 * @see #truncatedCompareTo(Date, Date, int)
	 * @since 3.0
	 */
	public static int truncatedCompareTo(Date date1, Date date2, int field) {
		Date truncatedDate1 = truncate(date1, field);
		Date truncatedDate2 = truncate(date2, field);
		return truncatedDate1.compareTo(truncatedDate2);
	}

	/**
	 * Determines if two calendars are equal up to no more than the specified
	 * most significant field.
	 * 
	 * @param cal1
	 *            the first calendar, not <code>null</code>
	 * @param cal2
	 *            the second calendar, not <code>null</code>
	 * @param field
	 *            the field from <code>Calendar</code>
	 * @return <code>true</code> if equal; otherwise <code>false</code>
	 * @throws IllegalArgumentException
	 *             if any argument is <code>null</code>
	 * @see #truncate(Calendar, int)
	 * @see #truncatedEquals(Date, Date, int)
	 * @since 3.0
	 */
	public static boolean truncatedEquals(Calendar cal1, Calendar cal2, int field) {
		return truncatedCompareTo(cal1, cal2, field) == 0;
	}

	/**
	 * Determines if two dates are equal up to no more than the specified most
	 * significant field.
	 * 
	 * @param date1
	 *            the first date, not <code>null</code>
	 * @param date2
	 *            the second date, not <code>null</code>
	 * @param field
	 *            the field from <code>Calendar</code>
	 * @return <code>true</code> if equal; otherwise <code>false</code>
	 * @throws IllegalArgumentException
	 *             if any argument is <code>null</code>
	 * @see #truncate(Date, int)
	 * @see #truncatedEquals(Calendar, Calendar, int)
	 * @since 3.0
	 */
	public static boolean truncatedEquals(Date date1, Date date2, int field) {
		return truncatedCompareTo(date1, date2, field) == 0;
	}

	/**
	 * <p>
	 * <code>DateUtils</code> instances should NOT be constructed in standard
	 * programming. Instead, the class should be used as
	 * <code>DateUtils.parse(str);</code>.
	 * </p>
	 * 
	 * <p>
	 * This constructor is public to permit tools that require a JavaBean
	 * instance to operate.
	 * </p>
	 */
	public DateUtils() {
		super();
	}

	/**
	 * Calculate the nanoseconds since midnight (in the default timezone) from a
	 * given time in milliseconds in UTC.
	 * 
	 * @param ms
	 *            the milliseconds
	 * @return the date value
	 */
	public static long nanosFromMilliSecs(long ms) {
		Calendar cal = getCalendar();
		synchronized (cal) {
			cal.clear();
			cal.setTimeInMillis(ms);
			int h = cal.get(Calendar.HOUR_OF_DAY);
			int m = cal.get(Calendar.MINUTE);
			int s = cal.get(Calendar.SECOND);
			int millis = cal.get(Calendar.MILLISECOND);
			return ((((((h * 60L) + m) * 60) + s) * 1000) + millis) * 1000000;
		}
	}
}
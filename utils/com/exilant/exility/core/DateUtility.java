/* *******************************************************************************************************
Copyright (c) 2015 EXILANT Technologies Private Limited

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 ******************************************************************************************************** */
package com.exilant.exility.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/***
 * 
 * @author Exilant Technologies
 * 
 *         Set of date utility functions
 * 
 */
public class DateUtility {
	/**
	 * server always uses this format for date
	 */
	public static final String SERVER_DATE_FORMAT = "yyyy-MM-dd";

	/**
	 * server always uses this format for date time
	 */
	public static final String SERVER_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

	/**
	 * specific case when we do not need ms
	 */
	public static final String SERVER_DATE_TIME_FORMAT_WITH_NO_MS = "yyyy-MM-dd HH:mm:ss";
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
			DateUtility.SERVER_DATE_FORMAT);
	private static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat(
			DateUtility.SERVER_DATE_TIME_FORMAT);
	private static final SimpleDateFormat dateTimeFormatterShort = new SimpleDateFormat(
			DateUtility.SERVER_DATE_TIME_FORMAT_WITH_NO_MS);
	/**
	 * convenient number
	 */
	public static final long MILLIS_IN_A_DAY = 86400000;

	static {
		DateUtility.dateFormatter.setLenient(false);
	}

	/***
	 * parse into a date. short-cut values ".", SYSDATE, and SYSTEMDATE are
	 * valid. dates like eom, bom, etc.. can be done on demand!! (No one has
	 * asked for it so far)
	 * 
	 * @param value
	 *            string of the form yyyy-MM-dd HH:mm:ss.SSS. Note that server
	 *            does not deal with local date formats. That is teh job of the
	 *            client
	 * @return date. null if value is not a valid date.
	 */
	public static Date parseDate(String value) {
		if (value == null || value.length() == 0) {
			return null;
		}
		try {
			// being flexible to parse time if specified
			int n = value.length();
			if (n == DateUtility.SERVER_DATE_FORMAT.length()) {
				return DateUtility.dateFormatter.parse(value);
			}

			if (n == DateUtility.SERVER_DATE_TIME_FORMAT_WITH_NO_MS.length()) {
				return DateUtility.dateTimeFormatterShort.parse(value);
			}

			if (n == DateUtility.SERVER_DATE_TIME_FORMAT.length()) {
				return DateUtility.dateTimeFormatter.parse(value);
			}

			return DateUtility.dateFormatter.parse(value);

		} catch (ParseException e) {
			// no need to do anything
		}
		Date d = DateUtility.parseShortcut(value);
		if (d == null) {
			Spit.out("ERROR "
					+ value
					+ " is not a valid date. You may get null related issue in your transaction.");
		}

		return d;
	}

	/***
	 * parse short cuts. ".", SYSDATE and SYSTEMDATE are implemented...
	 * 
	 * @param value
	 *            input date string
	 * @return date, or null if it is not a valid short cut
	 */
	private static Date parseShortcut(String value) {
		// OK it is not a date with the right format. Is it a short-cut?
		if (value.equals(".")) {
			return new Date();
		}
		String upperValue = value.toUpperCase();
		if (upperValue.equals("SYSDATE") || upperValue.equals("SYSTEMDATE")) {
			return new Date();
		}

		try {
			int i = Integer.parseInt(value);
			return DateUtility.addDays(null, i);
		} catch (Exception e) {
			//
		}

		return null;

	}

	/***
	 * parse into a date-time. short-cut values ".", SYSDATE, and SYSTEMDATE are
	 * valid. dates like eom, bom, etc.. can be done on demand!! (No one has
	 * asked for it so far)
	 * 
	 * @param value
	 *            string of the form yyyy-MM-dd HH:mm:ss.SSS. Note that server
	 *            does not deal with local date formats. That is teh job of the
	 *            client
	 * @return date-time. null if value is not a valid date.
	 */
	public static Date parseDateTime(String value) {
		try {
			int n = value.length();
			if (n > DateUtility.SERVER_DATE_TIME_FORMAT_WITH_NO_MS.length()) {
				return DateUtility.dateTimeFormatter.parse(value);
			}

			if (n == DateUtility.SERVER_DATE_TIME_FORMAT_WITH_NO_MS.length()) {
				return DateUtility.dateTimeFormatterShort.parse(value);
			}

			return DateUtility.dateFormatter.parse(value);
		} catch (ParseException e) {
			Date d = DateUtility.parseShortcut(value);
			if (d == null) {
				Spit.out("ERROR: "
						+ value
						+ " is not a valid date-time. You may get null related issues in your transactions.");
			}
			return d;
		}
	}

	/***
	 * format date into server-date format yyyy-mm-dd. If the date ihas time
	 * components, HH:MM:SS.SSS also added.
	 * 
	 * @param datePossiblyWithTime
	 * @return "" if it is null, date-time format otherwise
	 */
	public static String formatDate(Date datePossiblyWithTime) {
		if (datePossiblyWithTime == null) {
			return "";
		}

		Calendar c = Calendar.getInstance();
		c.setTime(datePossiblyWithTime);
		if (c.get(Calendar.MILLISECOND) > 0 || c.get(Calendar.SECOND) > 0
				|| c.get(Calendar.MINUTE) > 0
				|| c.get(Calendar.HOUR_OF_DAY) > 0) {
			return DateUtility.dateTimeFormatter.format(datePossiblyWithTime);
		}
		return DateUtility.dateFormatter.format(datePossiblyWithTime);
	}

	/***
	 * Format a date with time as per server format yyyy-mm-dd HH:MM:SS.SSS
	 * 
	 * @param dateWithTime
	 * @return server-formatted dated-time. Empty string if date is null.
	 */
	public static String formatDateTime(Date dateWithTime) {
		if (dateWithTime == null) {
			return "";
		}

		return DateUtility.dateTimeFormatter.format(dateWithTime);
	}

	/***
	 * calculate number of days between the dates. Exility date fields do not
	 * have time components. If date with time components are supplied, full
	 * days, as per milliseconds, is calculated
	 * 
	 * @param fromDate
	 *            from date
	 * @param toDate
	 *            to date
	 * @return number of days from from date to to dates
	 */
	public static int subtractDates(Date fromDate, Date toDate) {
		long ms = fromDate.getTime() - toDate.getTime();
		// Calendar d1 = Calendar.getInstance();
		// d1.setTime(date1);
		// long ms = d1.getTimeInMillis();
		// d1.setTime(date2);
		// ms -= d1.getTimeInMillis();
		return (int) (ms / DateUtility.MILLIS_IN_A_DAY);
	}

	/***
	 * Get today as a pure date, with no time components in that. Exility dates
	 * SHOULD use this ratehr than new Date().
	 * 
	 * @return todays date, with no time components.
	 */
	public static Date getToday() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	/***
	 * Add days to a date
	 * 
	 * @param date
	 * @param days
	 *            can be negative
	 * @return new date that is after days frays from date.
	 */
	public static Date addDays(Date date, long days) {
		return DateUtility.addDays(date, (int) days);
	}

	/***
	 * Add days to a date
	 * 
	 * @param date
	 * @param days
	 *            can be negative
	 * @return new date that is after days frays from date.
	 */
	public static Date addDays(Date date, int days) {
		Calendar d1 = Calendar.getInstance();
		if (date != null) {
			d1.setTime(date);
		}
		d1.add(Calendar.DAY_OF_YEAR, days);
		return d1.getTime();
	}

	/***
	 * get date-time
	 * 
	 * @return date-time as of now with nearest millisecond.
	 */
	public static Date getNow() {
		return (new Date());
	}
}
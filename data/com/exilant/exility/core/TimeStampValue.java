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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/***
 * NOT USED. Ideas was to use date-time as time stamp
 * 
 */
@Deprecated
public class TimeStampValue extends DateValue {
	private String milliSeconds;

	TimeStampValue(Date dateValue) {
		this.value = dateValue;
	}

	/**
	 * Lets try with milliseconds but getTime() excludes nanoseconds. Anant on
	 * 23 Otc 2013.
	 * 
	 * @param str
	 *            Note: This constructor can throw NumberFormatException in case
	 *            str either null or not a valid number.
	 */
	TimeStampValue(String str) {
		this.milliSeconds = str;
		this.value = DateUtility.parseDateTime(str);
	}

	/**
	 * Lets try with milliseconds but getTime() excludes nanoseconds. TimeStamp
	 * also extends Date so we can assign it for java.util.Date value
	 * 
	 * @param TimeStamp
	 */
	TimeStampValue(Timestamp dateValue) {
		this.value = dateValue;
		// If In Database this column is null then JDBC returns TimeStamp as
		// null.
		if (null == dateValue) {
			Spit.out("ERROR:  null is not a valid TimeStamp. You may get null related issues in your transactions.");
			return;
		}
		this.milliSeconds = dateValue.toString();
	}

	@Override
	Date getTimeStampValue() {
		return this.value;
	}

	@Override
	DataValueType getValueType() {
		return DataValueType.TIMESTAMP;
	}

	/**
	 * Don't format timeSatmp type values?Once its formated then application has
	 * maintain date formats, timeZones etc with respect to DB server.. Send
	 * timeStamp as milliseconds to client. Client should not change it. Anant
	 * on 24 Oct 2013.
	 */
	@Override
	protected String format() {
		if (null == this.value) {
			this.textValue = Value.NULL_VALUE;
		}

		// return this.textValue = DateUtility.formatDateTime(this.value);
		return String.valueOf(this.milliSeconds);
	}

	@Override
	String getQuotedValue() {
		return Chars.DATE_QUOTE + this.toString() + Chars.DATE_QUOTE;
	}

	@Override
	public void addToPrepearedStatement(PreparedStatement statement, int idx)
			throws SQLException {
		if (this.isNull()) {
			statement.setNull(idx, java.sql.Types.TIMESTAMP);
			return;
		}
		statement.setTimestamp(idx, Timestamp.valueOf(this.milliSeconds));
	}

}

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

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Date;

/**
 * A data type defines a domain for the value of a field. It first defines the
 * value to be of one of value-types (like text, integral etc..) and then allows
 * further restriction in terms of range and patterns
 * 
 */
class DateDataType extends AbstractDataType {
	/***
	 * set the lower limit for date. If the lower limit is a past date, this is
	 * a positive integer representing how many days it is into the past. If
	 * this is a negative number, then the min date is that many days into the
	 * future. e.g. if today is 1-jan-2012, and maxDaysBeforeToday = -31, then
	 * min acceptable date is 1-Feb-2012. You have to ensure that the min and
	 * max are appropriate
	 */
	int maxDaysBeforeToday = Integer.MAX_VALUE;

	/***
	 * set the upper limit for date. If the upper limit is a past date, this is
	 * a negative number indicating number of days before today. e.g. if today
	 * is 1-jan-2012, and maxDaysAfterToday = -31, then max acceptable date is
	 * 1-Dec-2011
	 */
	int maxDaysAfterToday = Integer.MAX_VALUE;

	/***
	 * do you want to track time? If false, the date object carries 00:00:00 as
	 * time
	 */
	boolean includesTime = false;

	DateDataType() {
	}

	@Override
	DataValueType getValueType() {
		return DataValueType.DATE;
	}

	@Override
	boolean isValid(Value value) {
		return this.isValid(value.getDateValue());
	}

	private boolean isValid(Date dat) {
		int diff;
		// if max/min days are not specified, we have nothing more to validate
		if ((this.maxDaysAfterToday == Integer.MAX_VALUE)
				&& (this.maxDaysBeforeToday == Integer.MAX_VALUE)) {
			return true;
		}
		Date today = DateUtility.getToday();

		diff = DateUtility.subtractDates(dat, today);
		if ((this.maxDaysAfterToday != Integer.MAX_VALUE)
				&& (diff > this.maxDaysAfterToday)) {
			return false;
		}
		if (diff >= 0) {
			return true;
		}
		diff = -diff;
		if ((this.maxDaysBeforeToday != Integer.MAX_VALUE)
				&& (diff > this.maxDaysBeforeToday)) {
			return false;
		}
		return true;
	}

	@Override
	boolean isValidList(ValueList valueList, boolean isOptional) {
		try {
			Date[] values = valueList.getDateList();
			for (int i = 0; i < values.length; i++) {
				if (valueList.isNull(i)) {
					if (!isOptional) {
						return false;
					}
				} else {
					if (!this.isValid(valueList.getDateValue(i))) {
						return false;
					}
				}
			}
			return true;
		} catch (Exception e) {
			//
		}
		return false;
	}

	@Override
	int getMaxLength() {
		if (this.includesTime) {
			return 21;
		}
		return 12;
	}

	@Override
	void addInputToStoredProcedure(CallableStatement statement, int idx,
			Value value) throws SQLException {
		statement.setDate(idx,
				new java.sql.Date(value.getDateValue().getTime()));
	}

	@Override
	void addOutputToStoredProcedure(CallableStatement statement, int idx)
			throws SQLException {
		statement.registerOutParameter(idx, java.sql.Types.DATE);
	}

	@Override
	Value extractFromStoredProcedure(CallableStatement statement, int idx)
			throws SQLException {
		return Value.newValue(statement.getDate(idx));
	}

	@Override
	public String sqlFormat(String value) {
		if (this.includesTime) {
			return AP.dateTimeFormattingPrefix + value
					+ AP.dateTimeFormattingPostfix;
		}

		return AP.dateFormattingPrefix + value + AP.dateFormattingPostfix;
	}
}
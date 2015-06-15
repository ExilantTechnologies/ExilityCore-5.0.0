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
import java.util.Date;

/***
 * represents a date value
 * 
 */
public class DateValue extends Value {
	protected Date value;

	DateValue() {

	}

	DateValue(Date dateValue) {
		this.value = dateValue;
	}

	DateValue(String str) {
		this.value = DateUtility.parseDate(str);
	}

	@Override
	public Date getDateValue() {
		return this.value;
	}

	@Override
	DataValueType getValueType() {
		return DataValueType.DATE;
	}

	@Override
	protected String format() {
		if (this.value == null) {
			this.textValue = Value.NULL_VALUE;
		}
		return this.textValue = DateUtility.formatDate(this.value);
	}

	@Override
	Value add(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL) {
			return Value.newValue(DateUtility.addDays(this.value,
					val2.getIntegralValue()));
		}
		return super.add(val2);
	}

	@Override
	Value subtract(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL) {
			return Value.newValue(DateUtility.addDays(this.value,
					-val2.getIntegralValue()));
		}

		if (val2.getValueType() == DataValueType.DATE) {
			return Value.newValue(DateUtility.subtractDates(this.value,
					val2.getDateValue()));
		}

		return super.subtract(val2);
	}

	@Override
	Value equal(Value val2) {
		if (val2.getValueType() == DataValueType.DATE) {
			return Value.newValue(this.value.equals(val2.getDateValue()));
		}
		return super.equal(val2);
	}

	@Override
	Value notEqual(Value val2) {
		if (val2.getValueType() == DataValueType.DATE) {
			return Value.newValue(!this.value.equals(val2.getDateValue()));
		}
		return super.notEqual(val2);
	}

	@Override
	Value greaterThan(Value val2) {
		if (val2.getValueType() == DataValueType.DATE) {
			return Value.newValue(this.value.after(val2.getDateValue()));
		}
		return super.greaterThan(val2);
	}

	@Override
	Value greaterThanOrEqual(Value val2) {
		if (val2.getValueType() == DataValueType.DATE) {
			return Value.newValue(!this.value.before(val2.getDateValue()));
		}
		return super.greaterThanOrEqual(val2);
	}

	@Override
	Value lessThan(Value val2) {
		if (val2.getValueType() == DataValueType.DATE) {
			return Value.newValue(this.value.before(val2.getDateValue()));
		}
		return super.lessThan(val2);
	}

	@Override
	Value lessThanOrEqual(Value val2) {
		if (val2.getValueType() == DataValueType.DATE) {
			return Value.newValue(!this.value.after(val2.getDateValue()));
		}
		return super.lessThanOrEqual(val2);
	}

	@Override
	String getQuotedValue() {
		return Chars.DATE_QUOTE + this.toString() + Chars.DATE_QUOTE;
	}

	@Override
	public boolean isNull() {
		return this.value == null;
	}

	@Override
	public void addToPrepearedStatement(PreparedStatement statement, int idx)
			throws SQLException {
		if (this.isNull()) {
			statement.setNull(idx, java.sql.Types.DATE);
			return;
		}
		statement.setDate(idx, new java.sql.Date(this.value.getTime()));
	}

	@Override
	protected boolean isSpecifiedByType() {
		return this.value != null;
	}
}

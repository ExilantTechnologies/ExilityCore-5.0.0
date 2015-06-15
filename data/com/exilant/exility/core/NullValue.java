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

/***
 * Represents null value. WE HAVE TO RE-THINK ON THE WHOLE CONCEPT OF NULL AT
 * SOME TIME
 * 
 */
public class NullValue extends Value {
	private static final String MSG = "No operations allowed when value is NULL";
	private final DataValueType valueType;

	protected NullValue(DataValueType valueType) {
		this.valueType = valueType;
		this.textValue = Value.NULL_VALUE;
	}

	@Override
	public boolean isNull() {
		return true;
	}

	@Override
	DataValueType getValueType() {
		return this.valueType;
	}

	@Override
	Value add(Value val2) {
		throw new RuntimeException(NullValue.MSG);
	}

	@Override
	String getQuotedValue() {
		return "null";
	}

	@Override
	public String toString() {
		return Value.NULL_VALUE;
	}

	@Override
	public void addToPrepearedStatement(PreparedStatement statement, int idx)
			throws SQLException {
		switch (this.valueType) {
		case BOOLEAN:
			statement.setNull(idx, java.sql.Types.BOOLEAN);
			break;
		case DATE:
			statement.setNull(idx, java.sql.Types.DATE);
			break;
		case DECIMAL:
			statement.setNull(idx, java.sql.Types.DOUBLE);
			break;
		case INTEGRAL:
			statement.setNull(idx, java.sql.Types.INTEGER);
			break;
		case TEXT:
			statement.setNull(idx, java.sql.Types.VARCHAR);
			break;
		case TIMESTAMP:
			statement.setNull(idx, java.sql.Types.DATE);
			break;
		default:
			statement.setNull(idx, java.sql.Types.VARCHAR);
			break;
		}
	}

	/***
	 * on popular demand, treating null as 0 for integer, and empty string for
	 * string and false for boolean for comparison
	 */
	@Override
	Value equal(Value val2) {
		Value val = this.getNonNullValue();
		if (val == null) {
			return BooleanValue.FALSE_VALUE;
		}

		return val.equal(val2);
	}

	@Override
	Value notEqual(Value val2) {
		Value val = this.getNonNullValue();
		if (val == null) {
			return BooleanValue.FALSE_VALUE;
		}

		return val.notEqual(val2);
	}

	/***
	 * get a default non-null value based on the data type
	 * 
	 * @return 0 for numeric, empty string for text, false for boolean and null
	 *         for date-time
	 */
	Value getNonNullValue() {
		switch (this.valueType) {
		case INTEGRAL:
			return Value.newValue(0);
		case DECIMAL:
			return Value.newValue(0.0);
		case TEXT:
			return Value.newValue("");
		case BOOLEAN:
			return BooleanValue.FALSE_VALUE;
		default:
			return null;
		}
	}
}

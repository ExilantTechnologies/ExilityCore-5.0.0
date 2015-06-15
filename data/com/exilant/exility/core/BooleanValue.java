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
 * An immutable value object that carries a boolean value. All boolean
 * operations result in false in case this carries a null value
 * 
 */
class BooleanValue extends Value {
	private final boolean value;
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	static final BooleanValue TRUE_VALUE = new BooleanValue(true);
	static final BooleanValue FALSE_VALUE = new BooleanValue(false);

	/***
	 * Utility to output string for a boolean value. Note that this is a static
	 * method, and not the standard toString() of object.
	 * 
	 * @param booleanValue
	 * @return true/false
	 */
	public static String toString(boolean booleanValue) {
		if (booleanValue) {
			return BooleanValue.TRUE;
		}
		return BooleanValue.FALSE;
	}

	/***
	 * utility to parse a string to boolean. Prefers to return false rather than
	 * throwing exception.
	 * 
	 * @param str
	 *            to be parsed
	 * @return true if 'true' or '1'. false otherwise.
	 */
	public static boolean parse(String str) {
		if (str.equalsIgnoreCase(BooleanValue.TRUE) || str.equals("1")) {
			return true;
		}
		if (str.equalsIgnoreCase(BooleanValue.FALSE) || str.equals("0")) {
			return false;
		}
		Spit.out(str + " is not a valid boolean value. It has to be either "
				+ BooleanValue.TRUE + " or " + BooleanValue.FALSE
				+ ". False value assumed");
		return false;

	}

	/***
	 * Standard constructor invoked through Value.newValue(boolean)
	 * 
	 * @param booleanValue
	 */
	protected BooleanValue(boolean booleanValue) {
		this.value = booleanValue;
		if (booleanValue) {
			this.textValue = BooleanValue.TRUE;
		} else {
			this.textValue = BooleanValue.FALSE;
		}
	}

	/***
	 * Convenient constructor to parse the input. Note that parse does not fail
	 * 
	 * @param str
	 */
	protected BooleanValue(String str) {
		this.value = BooleanValue.parse(str);
		if (this.value) {
			this.textValue = BooleanValue.TRUE;
		} else {
			this.textValue = BooleanValue.FALSE;
		}
	}

	@Override
	public boolean getBooleanValue() {
		return this.value;
	}

	@Override
	DataValueType getValueType() {
		return DataValueType.BOOLEAN;
	}

	@Override
	Value and(Value val2) {
		if (val2.getValueType() != DataValueType.BOOLEAN) {
			this.raiseException("logically operated with", val2);
			return BooleanValue.FALSE_VALUE;
		}

		if (this.getBooleanValue() == false) {
			return BooleanValue.FALSE_VALUE;
		}

		if (val2.getBooleanValue()) {
			return BooleanValue.TRUE_VALUE;
		}

		return BooleanValue.FALSE_VALUE;
	}

	@Override
	Value or(Value val2) {
		if (val2.getValueType() != DataValueType.BOOLEAN) {
			this.raiseException("logically operated with", val2);
			return BooleanValue.FALSE_VALUE;
		}

		if (this.getBooleanValue()) {
			return BooleanValue.TRUE_VALUE;
		}

		if (val2.getBooleanValue()) {
			return BooleanValue.TRUE_VALUE;
		}

		return BooleanValue.FALSE_VALUE;
	}

	@Override
	Value equal(Value val2) {
		if (val2.getValueType() != DataValueType.BOOLEAN) {
			return super.equal(val2);
		}

		if (this.getBooleanValue()) {
			if (val2.getBooleanValue()) {
				return BooleanValue.TRUE_VALUE;
			}
			return BooleanValue.FALSE_VALUE;
		}

		if (val2.getBooleanValue()) {
			return BooleanValue.FALSE_VALUE;
		}
		return BooleanValue.TRUE_VALUE;
	}

	@Override
	Value notEqual(Value val2) {
		if (val2.getValueType() != DataValueType.BOOLEAN) {
			return super.notEqual(val2);
		}

		if (this.getBooleanValue()) {
			if (val2.getBooleanValue()) {
				return BooleanValue.FALSE_VALUE;
			}
			return BooleanValue.TRUE_VALUE;
		}

		if (val2.getBooleanValue()) {
			return BooleanValue.TRUE_VALUE;
		}
		return BooleanValue.FALSE_VALUE;
	}

	@Override
	String getQuotedValue() {
		return this.toString();
	}

	@Override
	public void addToPrepearedStatement(PreparedStatement statement, int idx)
			throws SQLException {
		statement.setBoolean(idx, this.value);
	}

	@Override
	protected boolean isSpecifiedByType() {
		return this.value;
	}
}
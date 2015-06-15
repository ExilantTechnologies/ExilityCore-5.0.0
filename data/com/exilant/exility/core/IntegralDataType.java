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

/***
 * named type that puts restrictions on an integral value
 * 
 */
class IntegralDataType extends AbstractDataType {
	IntegralDataType() {
	}

	/***
	 * can the value be negative
	 */
	boolean allowNegativeValue = false;

	/***
	 * min value allowed
	 */
	long minValue = Long.MIN_VALUE;

	/***
	 * max value allowed
	 */
	long maxValue = Long.MAX_VALUE;

	@Override
	boolean isValid(Value value) {
		return this.isValid(value.getIntegralValue());
	}

	@Override
	boolean isValidList(ValueList valueList, boolean isOptional) {
		try {
			long[] values = valueList.getIntegralList();
			for (int i = 0; i < values.length; i++) {
				if (valueList.isNull(i)) {
					if (!isOptional) {
						return false;
					}
				} else {
					if (!this.isValid(valueList.getIntegralValue(i))) {
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

	private boolean isValid(long value) {
		return (value <= this.maxValue) && (value >= this.minValue);
	}

	@Override
	DataValueType getValueType() {
		return DataValueType.INTEGRAL;
	}

	@Override
	int getMaxLength() {
		if (this.maxValue == Long.MAX_VALUE) {
			return AbstractDataType.MAX_DIGITS; //
		}
		int l1 = Long.toString(this.maxValue).length();
		int l2 = Long.toString(this.minValue).length();
		System.out
				.println(this.minValue + " is min with length = " + l2
						+ " and " + this.maxValue
						+ " is max value with length = " + l1);
		return l1 > l2 ? l1 : l2;
	}

	@Override
	void addInputToStoredProcedure(CallableStatement statement, int idx,
			Value value) throws SQLException {
		statement.setLong(idx, value.getIntegralValue());
	}

	@Override
	void addOutputToStoredProcedure(CallableStatement statement, int idx)
			throws SQLException {
		statement.registerOutParameter(idx, java.sql.Types.INTEGER);
	}

	@Override
	public void initialize() {
		if (this.allowNegativeValue == false) {
			if (this.minValue < 0) {
				Spit.out(this.name
						+ " does not allow negative values, but has fixed a negative value as min value. min value is ignored.");
				this.minValue = 0;
			}
			if (this.maxValue < 0) {
				Spit.out(this.name
						+ " does not allow negative values, but has fixed a negative value as max value. max value is ignored.");
				this.maxValue = Long.MAX_VALUE;
			}
		}
		super.initialize();
	}

	@Override
	Value extractFromStoredProcedure(CallableStatement statement, int idx)
			throws SQLException {
		return Value.newValue(statement.getInt(idx));
	}

	@Override
	public String sqlFormat(String value) {
		return value;
	}
}
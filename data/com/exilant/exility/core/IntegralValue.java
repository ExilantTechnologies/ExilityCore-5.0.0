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
 * Value that can hold a long as its value
 * 
 */
public class IntegralValue extends Value {
	private long value;

	IntegralValue(long integralValue) {
		this.value = integralValue;
	}

	/***
	 * try and parse text, failing which use 0 as value
	 * 
	 * @param str
	 */
	IntegralValue(String str) {
		if (str == null || str.length() == 0) {
			this.value = 0;
			return;
		}
		try {
			this.value = Long.parseLong(str);
		} catch (NumberFormatException e) {
			try {
				this.value = (long) Double.parseDouble(str);
			} catch (NumberFormatException e1) {
				Spit.out("ERROR: |" + str
						+ "| is not a valid integer value. 0 is assumed");
			}
		}
	}

	/***
	 * convert double to long
	 * 
	 * @param decimalValue
	 */
	IntegralValue(double decimalValue) {
		this.value = (long) decimalValue;
	}

	@Override
	public long getIntegralValue() {
		return this.value;
	}

	@Override
	public double getDecimalValue() {
		return this.value;
	}

	@Override
	DataValueType getValueType() {
		return DataValueType.INTEGRAL;
	}

	@Override
	protected String format() {
		return this.textValue = Long.toString(this.value);
	}

	@Override
	Value add(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL) {
			return Value.newValue(this.value + val2.getIntegralValue());
		}

		if (val2.getValueType() == DataValueType.DECIMAL) {
			return Value.newValue(this.value + val2.getDecimalValue());
		}

		return super.add(val2);
	}

	@Override
	Value subtract(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL) {
			return Value.newValue(this.value - val2.getIntegralValue());
		}

		if (val2.getValueType() == DataValueType.DECIMAL) {
			return Value.newValue(this.value - val2.getDecimalValue());
		}

		return super.subtract(val2);
	}

	@Override
	Value multiply(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL) {
			return Value.newValue(this.value * val2.getIntegralValue());
		}

		if (val2.getValueType() == DataValueType.DECIMAL) {
			return Value.newValue(this.value * val2.getDecimalValue());
		}

		return super.multiply(val2);
	}

	@Override
	Value divide(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL) {
			return Value.newValue(this.value / val2.getIntegralValue());
		}

		if (val2.getValueType() == DataValueType.DECIMAL) {
			return Value.newValue(this.value / val2.getDecimalValue());
		}

		return super.divide(val2);
	}

	@Override
	Value remainder(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL) {
			return Value.newValue(this.value % val2.getIntegralValue());
		}

		return super.remainder(val2);
	}

	@Override
	Value power(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL) {
			return Value
					.newValue(Math.pow(this.value, val2.getIntegralValue()));
		}

		if (val2.getValueType() == DataValueType.DECIMAL) {
			return Value.newValue(Math.pow(this.value, val2.getDecimalValue()));
		}

		return super.power(val2);
	}

	@Override
	Value equal(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL) {
			return Value.newValue(this.value == val2.getIntegralValue());
		}
		if (val2.getValueType() == DataValueType.DECIMAL) {
			long v1 = Math
					.round(this.value * DecimalValue.PRECISION_MULTIPLIER);
			long v2 = Math.round(val2.getDecimalValue()
					* DecimalValue.PRECISION_MULTIPLIER);
			return Value.newValue(v1 == v2);
		}

		return super.equal(val2);
	}

	@Override
	Value notEqual(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL) {
			return Value.newValue(this.value != val2.getIntegralValue());
		}
		if (val2.getValueType() == DataValueType.DECIMAL) {
			long v1 = Math
					.round(this.value * DecimalValue.PRECISION_MULTIPLIER);
			long v2 = Math.round(val2.getDecimalValue()
					* DecimalValue.PRECISION_MULTIPLIER);
			return Value.newValue(v1 != v2);
		}

		return super.notEqual(val2);
	}

	@Override
	Value greaterThan(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL) {
			return Value.newValue(this.value > val2.getIntegralValue());
		}

		if (val2.getValueType() == DataValueType.DECIMAL) {
			return Value.newValue(this.value > val2.getDecimalValue());
		}

		return super.greaterThan(val2);
	}

	@Override
	Value greaterThanOrEqual(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL) {
			return Value.newValue(this.value >= val2.getIntegralValue());
		}

		if (val2.getValueType() == DataValueType.DECIMAL) {
			return Value.newValue(this.value >= val2.getDecimalValue());
		}

		return super.greaterThanOrEqual(val2);
	}

	@Override
	Value lessThan(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL) {
			return Value.newValue(this.value < val2.getIntegralValue());
		}

		if (val2.getValueType() == DataValueType.DECIMAL) {
			return Value.newValue(this.value < val2.getDecimalValue());
		}

		return super.lessThan(val2);
	}

	@Override
	Value lessThanOrEqual(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL) {
			return Value.newValue(this.value <= val2.getIntegralValue());
		}

		if (val2.getValueType() == DataValueType.DECIMAL) {
			return Value.newValue(this.value <= val2.getDecimalValue());
		}

		return super.lessThanOrEqual(val2);
	}

	@Override
	String getQuotedValue() {
		return this.toString();
	}

	@Override
	public void addToPrepearedStatement(PreparedStatement statement, int idx)
			throws SQLException {
		if (this.isNull()) {
			statement.setNull(idx, java.sql.Types.INTEGER);
			return;
		}
		statement.setLong(idx, this.value);
	}

	@Override
	protected boolean isSpecifiedByType() {
		return this.value != 0;
	}
}

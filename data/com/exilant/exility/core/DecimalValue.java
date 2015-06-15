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
import java.text.DecimalFormat;

/***
 * represents a double value. uses six decimal places by default
 * 
 */
public class DecimalValue extends Value {
	static final int NUMBER_OF_DECIMAL_PLACES = 6;
	static final double PRECISION_MULTIPLIER = Math.pow(10,
			DecimalValue.NUMBER_OF_DECIMAL_PLACES);
	static final DecimalFormat decimalFormatter = new DecimalFormat();
	static {
		DecimalValue.decimalFormatter
				.setMaximumFractionDigits(DecimalValue.NUMBER_OF_DECIMAL_PLACES);
		DecimalValue.decimalFormatter.setMinimumFractionDigits(1);
		DecimalValue.decimalFormatter.setGroupingUsed(false);
	}

	/***
	 * uses 0.000000 as the default formatting
	 * 
	 * @param decimalValue
	 * @return formatted value
	 */
	public static String toString(double decimalValue) {
		return DecimalValue.decimalFormatter.format(decimalValue);
	}

	private final double value;

	DecimalValue(double decimalValue) {
		this.value = decimalValue;
	}

	DecimalValue(String str) {
		this.value = Double.parseDouble(str);
	}

	DecimalValue(long integralValue) {
		this.value = integralValue;
	}

	@Override
	public double getDecimalValue() {
		return this.value;
	}

	@Override
	public long getIntegralValue() {
		return (long) this.value;
	}

	@Override
	DataValueType getValueType() {
		return DataValueType.DECIMAL;
	}

	@Override
	protected String format() {
		return this.textValue = DecimalValue.decimalFormatter
				.format(this.value);
	}

	@Override
	Value add(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL
				|| val2.getValueType() == DataValueType.DECIMAL) {
			return Value.newValue(this.value + val2.getDecimalValue());
		}

		return super.add(val2);
	}

	@Override
	Value subtract(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL
				|| val2.getValueType() == DataValueType.DECIMAL) {
			return Value.newValue(this.value - val2.getDecimalValue());
		}

		return super.subtract(val2);
	}

	@Override
	Value multiply(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL
				|| val2.getValueType() == DataValueType.DECIMAL) {
			return Value.newValue(this.value * val2.getDecimalValue());
		}

		return super.multiply(val2);
	}

	@Override
	Value divide(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL
				|| val2.getValueType() == DataValueType.DECIMAL) {
			return Value.newValue(this.value / val2.getDecimalValue());
		}

		return super.divide(val2);
	}

	@Override
	Value power(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL
				|| val2.getValueType() == DataValueType.DECIMAL) {
			return Value.newValue(Math.pow(this.value, val2.getDecimalValue()));
		}

		return super.power(val2);
	}

	@Override
	Value equal(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL
				|| val2.getValueType() == DataValueType.DECIMAL) {
			// we compare with only four decimal places of accuracy
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
		if (val2.getValueType() == DataValueType.INTEGRAL
				|| val2.getValueType() == DataValueType.DECIMAL) {
			// we compare with only four decimal places of accuracy
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
		if (val2.getValueType() == DataValueType.INTEGRAL
				|| val2.getValueType() == DataValueType.DECIMAL) {
			return Value.newValue(this.value > val2.getDecimalValue());
		}

		return super.greaterThan(val2);
	}

	@Override
	Value greaterThanOrEqual(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL
				|| val2.getValueType() == DataValueType.DECIMAL) {
			return Value.newValue(this.value >= val2.getDecimalValue());
		}

		return super.greaterThanOrEqual(val2);
	}

	@Override
	Value lessThan(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL
				|| val2.getValueType() == DataValueType.DECIMAL) {
			return Value.newValue(this.value < val2.getDecimalValue());
		}

		return super.lessThan(val2);
	}

	@Override
	Value lessThanOrEqual(Value val2) {
		if (val2.getValueType() == DataValueType.INTEGRAL
				|| val2.getValueType() == DataValueType.DECIMAL) {
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
			statement.setNull(idx, java.sql.Types.DECIMAL);
			return;
		}
		statement.setDouble(idx, this.value);
	}

	@Override
	protected boolean isSpecifiedByType() {
		return this.value != 0;
	}
}

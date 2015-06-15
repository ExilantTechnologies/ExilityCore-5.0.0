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
 * represents a decimal data vaue with further restrictions
 * 
 */
class DecimalDataType extends AbstractDataType {
	/***
	 * number of digits after the decimal point. In business, this is generally
	 * fixed. User input is rounded-off to these many decimal places. Output is
	 * formatted to these many places.
	 */
	int numberOfDecimals = 2;
	/***
	 * Requires re-factoring. min/max were optional. Which means, 0 could not be
	 * set as min. Hence this boolean. If we make min/max mandatory, we do not
	 * need this boolean.
	 */
	boolean allowNegativeValue = false;
	/***
	 * defaults to Double.MIN. A good safety, even after this is made mandatory
	 */
	double minValue = Double.MIN_VALUE;
	/***
	 * defaults to Double.MAX. A good safety, even after this is made mandatory
	 */
	double maxValue = Double.MAX_VALUE;

	/***
	 * If a value of 2 is stored in double, its string value may come out as
	 * 1.999999999999999999 formatter helps is avoiding this. Formatter is NOT
	 * meant for client-side formatting based on country et..
	 */
	private String formatterString = "0.00";

	DecimalDataType() {
	}

	@Override
	DataValueType getValueType() {
		return DataValueType.DECIMAL;
	}

	@Override
	boolean isValid(Value value) {
		return this.isValid(value.getDecimalValue());
	}

	private boolean isValid(double number) {
		// check for minValue and maxValue
		boolean result = (number >= this.minValue) && (number <= this.maxValue);
		if (!result) {
			Spit.out("Decimal validation failed for value = " + number
					+ " because max value = " + this.maxValue
					+ " and minValue=" + this.minValue);
		}
		return true;
	}

	@Override
	boolean isValidList(ValueList valueList, boolean isOptional) {
		try {
			double[] values = valueList.getDecimalList();
			for (int i = 0; i < values.length; i++) {
				if (valueList.isNull(i)) {
					if (!isOptional) {
						return false;
					}
				} else {
					if (!this.isValid(valueList.getDecimalValue(i))) {
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
	protected String formatValue(Value value) {
		if (value.getValueType() != DataValueType.DECIMAL) {
			return value.toString();
		}

		return String.format(String.valueOf(value.getDecimalValue()),
				this.formatterString);
	}

	@Override
	protected String[] formatValueList(ValueList valueList) {
		String[] returnList = null;

		try {
			returnList = new String[valueList.length()];
			for (int i = 0; i < valueList.length(); i++) {
				if (valueList.isNull(i)
						|| valueList.getValue(i).getTextValue().equals("")) {
					returnList[i] = valueList.getValue(i).toString();
				} else {
					returnList[i] = String.format(
							String.valueOf(valueList.getValue(i)),
							this.formatterString);
				}
			}
		} catch (Exception ex) {
			//
		}
		return returnList;
	}

	@Override
	int getMaxLength() {
		if (this.maxValue == Double.MAX_VALUE) {
			return AbstractDataType.MAX_DIGITS;
		}
		int i = 1;
		int val = 10;
		while (val < this.maxValue && i < AbstractDataType.MAX_DIGITS) {
			val = val * 10;
			i++;
		}
		return (i + this.numberOfDecimals + 1);

	}

	@Override
	void addInputToStoredProcedure(CallableStatement statement, int idx,
			Value value) throws SQLException {
		statement.setDouble(idx, value.getDecimalValue());
	}

	@Override
	void addOutputToStoredProcedure(CallableStatement statement, int idx)
			throws SQLException {
		statement.registerOutParameter(idx, java.sql.Types.DOUBLE);
	}

	@Override
	public void initialize() {
		if (!this.allowNegativeValue && this.minValue < 0) {
			this.minValue = 0;
		}

		if (this.numberOfDecimals != 2 && this.numberOfDecimals > 0) {
			this.formatterString = "0.";
			for (int i = this.numberOfDecimals; i > 0; i--) {
				this.formatterString += '0';
			}
		}
		super.initialize();
	}

	@Override
	Value extractFromStoredProcedure(CallableStatement statement, int idx)
			throws SQLException {
		return Value.newValue(statement.getDouble(idx));
	}

	@Override
	public String sqlFormat(String value) {
		return value;
	}
}
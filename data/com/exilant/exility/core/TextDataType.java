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
import java.util.regex.Pattern;

/**
 * represents a domain for a text value. defines restrictions on a text value.
 * 
 */
class TextDataType extends AbstractDataType {
	/**
	 * min characters required for this field to be valid
	 */
	int minLength = 0;
	/**
	 * max characters allowed for this field
	 */
	int maxLength = Integer.MAX_VALUE;

	/**
	 * regular expression that this value has to conform to
	 */
	Pattern regex = null;

	TextDataType() {
		this.name = "text";
	}

	@Override
	boolean isValid(Value value) {
		return this.isValid(value.getTextValue());
	}

	private boolean isValid(String val) {
		int len = val.length();

		/*
		 * check for min and max length
		 */
		if ((len >= this.minLength) && (len <= this.maxLength)) {
			if ((this.regex == null) || this.regex.matcher(val).matches()) {
				return true;
			}
		}
		return false;
	}

	@Override
	boolean isValidList(ValueList valueList, boolean isOptional) {
		String[] values = valueList.getTextList();
		for (int i = 0; i < values.length; i++) {
			if (valueList.isNull(i)) {
				if (!isOptional) {
					return false;
				}
			} else {
				if (!this.isValid(valueList.getTextValue(i))) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	int getMaxLength() {
		return this.maxLength;
	}

	@Override
	DataValueType getValueType() {
		return DataValueType.TEXT;
	}

	@Override
	void addInputToStoredProcedure(CallableStatement statement, int idx,
			Value value) throws SQLException {
		statement.setString(idx, value.getTextValue());
	}

	@Override
	void addOutputToStoredProcedure(CallableStatement statement, int idx)
			throws SQLException {
		statement.registerOutParameter(idx, java.sql.Types.VARCHAR);
	}

	@Override
	Value extractFromStoredProcedure(CallableStatement statement, int idx)
			throws SQLException {
		return Value.newValue(statement.getString(idx));
	}

	@Override
	public String sqlFormat(String value) {
		return '\'' + value.replace("'", "''") + '\'';
	}
}
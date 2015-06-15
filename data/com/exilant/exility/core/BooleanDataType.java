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
 * Represents a boolean value type with specific values for true/false
 * 
 */
class BooleanDataType extends AbstractDataType {
	/***
	 * text value that is considered to be true
	 */
	String trueValue = null;

	/***
	 * text value that is considered to be false
	 */
	String falseValue = null;

	BooleanDataType() {
	}

	@Override
	DataValueType getValueType() {
		return DataValueType.BOOLEAN;
	}

	@Override
	boolean isValid(Value value) {
		return true;
	}

	@Override
	boolean isValidList(ValueList valueList, boolean isOptional) {
		if (isOptional) {
			return true;
		}
		int len = valueList.length();
		for (int i = 0; i < len; i++) {
			if (valueList.isNull(i)) {
				return false;
			}
		}
		return true;
	}

	@Override
	int getMaxLength() {
		return 3;
	}

	@Override
	void addInputToStoredProcedure(CallableStatement statement, int idx,
			Value value) throws SQLException {
		statement.setBoolean(idx, value.getBooleanValue());
	}

	@Override
	void addOutputToStoredProcedure(CallableStatement statement, int idx)
			throws SQLException {
		statement.registerOutParameter(idx, java.sql.Types.BOOLEAN);
	}

	@Override
	Value extractFromStoredProcedure(CallableStatement statement, int idx)
			throws SQLException {
		return Value.newValue(statement.getBoolean(idx));
	}

	@Override
	public String sqlFormat(String value) {
		if (BooleanValue.parse(value)) {
			return AP.trueValueForSql;
		}

		return AP.falseValueForSql;
	}
}
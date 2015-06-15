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
 * NOT USED. Idea was to use date-time as time stamp
 * 
 */
@Deprecated
class TimeStampDataType extends AbstractDataType {
	TimeStampDataType() {
	}

	@Override
	DataValueType getValueType() {
		return DataValueType.TIMESTAMP;
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
		return 20;
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
		return value;
	}
}
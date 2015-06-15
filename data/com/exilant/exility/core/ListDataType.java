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

/**
 * represents a delimiter separated list of another data type. a,b,c for
 * example.
 * 
 */
class ListDataType extends AbstractDataType {
	/**
	 * date type that the entries in the list should conform to
	 */
	String baseDataType;

	/**
	 * entries are are delimited by this. defaults to ','
	 */
	String listSeparator = ",";

	/**
	 * min entries in the list
	 */
	int minCount = 0;

	/**
	 * max entries in the list
	 */
	int maxCount = Integer.MAX_VALUE;

	private AbstractDataType baseDataTypeObject = null;

	ListDataType() {
		this.name = "list";
	}

	@Override
	boolean isValid(Value value) {
		if (value == null) {
			return true;
		}

		return this.isValid(value.getTextValue());
	}

	/**
	 * validate the text value
	 * 
	 * @param val
	 *            text value to validate
	 * @return true if it is valid, false otherwise
	 */
	boolean isValid(String val) {
		String[] entries = val.split(this.listSeparator);
		if (entries.length < this.minCount || entries.length > this.maxCount) {
			return false;
		}

		for (String aVal : entries) {
			if (this.baseDataTypeObject.parseValue(null, aVal, null, null) == null) {
				return false;
			}
		}
		return true;
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
		return this.maxCount * (this.getBaseDataType().getMaxLength() + 1);
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

	/**
	 * base data type is not guaranteed to be available at the time of loading.
	 * hence we load it on a need basis
	 * 
	 * @return
	 */
	private AbstractDataType getBaseDataType() {
		if (this.baseDataTypeObject == null) {
			this.baseDataTypeObject = DataTypes
					.getDataTypeOrNull(this.baseDataType);
			if (this.baseDataTypeObject == null) {
				Spit.out("Data type "
						+ this.name
						+ " uses "
						+ this.baseDataType
						+ " as a base data type, but that base data type is not defined. A default text data type is assumed for the time being.");
				this.baseDataTypeObject = DataTypes.getDataType(
						this.baseDataType, null);
			}
		}
		return this.baseDataTypeObject;
	}

}
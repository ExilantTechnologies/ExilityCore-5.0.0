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
 * DataType is a refined value type that puts additional restrictions on the
 * range of values. Every data element in Exility should be associated with a
 * data type, so that its validation can be automated.
 * 
 */
public abstract class AbstractDataType implements ToBeInitializedInterface {
	/***
	 * list of attributes that are extracted for studio for editing
	 */
	private static final String[] ALL_ATTRIBUTES = { "name", "messageName",
			"description", "formatter", "minLength", "maxLength", "regex",
			"minValue", "maxValue", "allowNegativeValue", "numberOfDecimals",
			"maxDaysBeforeToday", "maxDaysAfterToday", "includesTime",
			"trueValue", "falseValue" };

	/***
	 * max digits that java supports
	 */
	protected static final int MAX_DIGITS = 17;
	/**
	 * default data type
	 */
	public static final String DEFAULT_TYPE = "text";

	/***
	 * data type is identified by its name. name has to be unique across all
	 * modules of an application
	 */
	String name = null;

	/***
	 * message to be used when a field that is associated with this data type
	 * fails validation. default is invalidDatatypeName
	 */
	String messageName = null;

	/***
	 * describe it. To be used as tool tip
	 */
	String description = null;

	/**
	 * formatter is used only while rendering this as an output field. Like
	 * commas in an amount
	 */
	String formatter = null;

	/***
	 * sql data type to be used to create this as a column for a table
	 */
	String sqlType = null;

	/***
	 * parse supplied text and return parsed value
	 * 
	 * @param fieldName
	 *            used for putting message into dc on validation failure
	 * @param fieldValue
	 *            text to be parsed
	 * @param validValues
	 *            in case the value is restricted to a list of values
	 * @param dc
	 *            to which message is to be added. optional, in which case
	 *            message is not added
	 * @return parsed value, or null if parse fails.
	 */
	public Value parseValue(String fieldName, String fieldValue,
			String[] validValues, DataCollection dc) {
		if (fieldValue == null
				|| (fieldValue.length() == 0 && this.getValueType() != DataValueType.TEXT)) {
			return new NullValue(this.getValueType());
		}
		// is it an enumerated value list??
		if (validValues != null) {
			for (String fval : validValues) {
				if (fval.equalsIgnoreCase(fieldValue)) {
					return Value.newValue(fieldValue, this.getValueType());
				}
			}
		} else {
			Value value = Value.newValue(fieldValue, this.getValueType());
			if (value != null && this.isValid(value)) {
				return value;
			}
		}

		// Field name might not have been supplied
		String localFieldName = (fieldName == null) ? "field" : fieldName;
		if (dc != null) {
			dc.addMessage(this.messageName, localFieldName, fieldValue);
		}

		Spit.out(fieldValue + " is invalid for " + localFieldName
				+ " that is of datatype" + this.name);
		return null;
	}

	/***
	 * parse a string array into a value list based on this data type definition
	 * 
	 * @param fieldName
	 *            used for flagging error on validation failure
	 * @param fieldValues
	 *            text array to be parsed
	 * @param isOptional
	 *            can an element of the array be empty string?
	 * @param dc
	 *            to which the message is to be added. If null, message is not
	 *            added.
	 * @return parsed value list
	 */
	ValueList getValueList(String fieldName, String[] fieldValues,
			boolean isOptional, DataCollection dc) {
		ValueList valueList = ValueList.newList(fieldValues,
				this.getValueType());
		if (valueList != null && this.isValidList(valueList, isOptional)) {
			return valueList;
		}

		// Field name might not have been supplied
		String localFieldName = (fieldName == null) ? "field" : fieldName;
		StringBuilder sbf = new StringBuilder(fieldValues[0]);
		for (int i = 1; i < fieldValues.length; i++) {
			sbf.append(',').append(fieldValues[i]);
		}

		if (dc != null) {
			dc.addMessage(this.messageName, localFieldName, sbf.toString());
		}
		Spit.out(sbf.toString() + " is invalid for " + localFieldName
				+ " that is of datatype" + this.name);
		return null;
	}

	/***
	 * validate value as per this data type definition
	 * 
	 * @param value
	 *            to be validated
	 * @return true if value is valid, false otherwise
	 */
	abstract boolean isValid(Value value);

	/***
	 * validate a list of values as per this data type definition
	 * 
	 * @param valueList
	 *            to be validated
	 * @param isOptional
	 *            whether a value in the list can be null?
	 * @return true if this list passes validation, false otherwise
	 */
	abstract boolean isValidList(ValueList valueList, boolean isOptional);

	/***
	 * Convenient method to get the actual type of the subclass
	 * 
	 * @return
	 */
	abstract DataValueType getValueType();

	/***
	 * format the value as per the formatter associated with this data type
	 * 
	 * @param value
	 * @return
	 */
	String format(Value value) {
		Value val = value;
		/*
		 * but what if the supplied value is not this type?
		 */
		DataValueType t1 = value.getValueType();
		DataValueType t2 = this.getValueType();
		if (t1 != t2) {
			val = Value.newValue(value.toString(), t2);
			/*
			 * if val is null, formatValue() takes care of it
			 */
		}

		return this.formatValue(val);
	}

	/***
	 * format a value list
	 * 
	 * @param valueList
	 * @return
	 */
	String[] format(ValueList valueList) {
		DataValueType t1 = valueList.getValueType();
		DataValueType t2 = this.getValueType();
		if (t1 != t2) {
			Spit.out("Incompatible value type provided for formatting " + t1
					+ " and " + t2);
			return valueList.format();
		}
		return this.formatValueList(valueList);
	}

	/***
	 * format this value
	 * 
	 * @param value
	 * @return
	 */
	protected String formatValue(Value value) {
		if (value == null) {
			return "";
		}
		return value.toString();
	}

	/***
	 * return a text array that represents the formatted values in the value
	 * list
	 * 
	 * @param valueList
	 * @return
	 */
	protected String[] formatValueList(ValueList valueList) {
		return valueList.format();
	}

	/***
	 * get the max length in terms of number of characters that this data type
	 * will allow
	 * 
	 * @return
	 */
	abstract int getMaxLength();

	/***
	 * add this data type as an input parameter for a stored procedure.
	 * Delegated to this class to avoid switch-case in callable statement
	 * related utility methods
	 * 
	 * @param statement
	 * @param idx
	 * @param value
	 * @throws SQLException
	 */
	abstract void addInputToStoredProcedure(CallableStatement statement,
			int idx, Value value) throws SQLException;

	/***
	 * add this data type as an output parameter for a stored procedure.
	 * Delegated to this class to avoid switch-case in callable statement
	 * related utility methods
	 * 
	 * @param statement
	 * @param idx
	 * @throws SQLException
	 */
	abstract void addOutputToStoredProcedure(CallableStatement statement,
			int idx) throws SQLException;

	/***
	 * extract value of this data type from a stored procedure statement.
	 * Delegated to this class to avoid switch-case in callable statement
	 * related utility methods
	 * 
	 * @param statement
	 * @param idx
	 * @return
	 * @throws SQLException
	 */
	abstract Value extractFromStoredProcedure(CallableStatement statement,
			int idx) throws SQLException;

	/***
	 * returns the string that represents the data type to be used in create SQL
	 * for a column of this type
	 * 
	 * @return sql type of this data type
	 */
	public String getSqlType() {
		return this.sqlType;
	}

	@Override
	public void initialize() {
		if (this.messageName == null || this.messageName.length() == 0) {
			this.messageName = "invalid"
					+ this.name.substring(0, 1).toUpperCase()
					+ this.name.substring(1);
		}
	}

	/***
	 * return a string that is suitable to be part of a sql statement
	 * 
	 * @param value
	 * @return formatted string suitable to be used in a sql statement
	 */
	abstract public String sqlFormat(String value);

	/*
	 * Methods that implement partInterface
	 */
	/**
	 * 
	 * @return all attributes that can be loaded
	 */
	public String[] getLoadableAttributes() {
		return ALL_ATTRIBUTES;
	}
}
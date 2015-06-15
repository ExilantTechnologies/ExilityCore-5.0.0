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

import java.util.Date;

/**
 * utility functions to deal with RDBMS
 * 
 */
public class SqlUtil {

	// filter field operator
	/**
	 * no condition
	 */
	public static final int OPERATOR_ANY = 0;
	/**
	 * equal
	 */
	public static final int OPERATOR_EQUAL = 1;
	/**
	 * starts with the value
	 */
	public static final int OPERATOR_STARTS_WITH = 2;
	/**
	 * contains the value
	 */
	public static final int OPERATOR_CONTAINS = 3;
	/**
	 * greater than value
	 */
	public static final int OPERATOR_GREATER_THAN = 4;
	/**
	 * less than value
	 */
	public static final int OPERATOR_LESS_THAN = 5;
	/**
	 * between two values
	 */
	public static final int OPERATOR_BETWEEN = 6;
	/**
	 * matches a value in the list
	 */
	public static final int OPERATOR_IN = 7;
	/**
	 * does not match any value in the list
	 */
	public static final int OPERATOR_NOT_IN = 8;

	private static final String[] OPERATOR_VERBS = { "", " = ", " LIKE ",
			" LIKE ", " > ", " < ", " BETWEEN ", " IN ", " NOT IN " };

	/**
	 * @param operator
	 * @return text to be used in a dynamic sql for this operator
	 */
	private static String getOperatorText(int operator) {
		if (operator < 0 || operator > 0) {
			return "";
		}
		return OPERATOR_VERBS[operator];
	}

	/**
	 * format a value in a way that is suitable to be inserted into a dynamic
	 * sql
	 * 
	 * @param val
	 *            value to be inserted
	 * @param valueType
	 *            type of value like text, integral..
	 * @return formatted text, or null if it can not be formatted as a valid
	 *         data value type
	 */
	public static String formatValue(String val, DataValueType valueType) {
		if (val.length() == 0) {
			if (valueType == DataValueType.TEXT) {
				if (AP.useNullForEmptyString) {
					return "NULL";
				}

				return "''";
			}
			return null;
		}

		switch (valueType) {
		case TEXT:
			return '\'' + val.replace("'", "''") + '\'';

		case DATE:
			if (val.equals("systemDate")) {
				return AP.systemDateFunction;
			}

			return AP.dateFormattingPrefix + val + AP.dateFormattingPostfix;

		case TIMESTAMP:
			if (val.equals("systemDate")) {
				return AP.systemDateFunction;
			}

			return AP.dateTimeFormattingPrefix + val
					+ AP.dateTimeFormattingPostfix;

			/**
			 * integral and decimal do not require any formatting
			 */
		default:
			return val;
		}
	}

	/**
	 * format a Value to a form that can be inserted into a dynamic sql
	 * 
	 * @param value
	 *            to be formatted
	 * @return text, null if it is not suitable
	 */
	public static String formatValue(Value value) {
		String val = null;
		Date dat;
		if (value != null) {
			switch (value.getValueType()) {
			case TEXT:
				val = value.getTextValue();
				if (val != null) {
					val = '\'' + val.replace("'", "''") + '\'';
				}
				break;

			case DATE:
				dat = value.getDateValue();
				val = AP.dateFormattingPrefix + DateUtility.formatDate(dat)
						+ AP.dateFormattingPostfix;
				break;

			case TIMESTAMP:
				dat = value.getTimeStampValue();
				val = AP.dateTimeFormattingPrefix + DateUtility.formatDate(dat)
						+ AP.dateTimeFormattingPostfix;
				break;

			default:
				return value.getTextValue();
			}
		}

		if (val == null) {
			if (AP.useNullForEmptyString) {
				return " NULL ";
			}
			return "''";
		}
		return val;
	}

	/**
	 * get text that is meant to insert user into a dynamic sql.
	 * 
	 * @param dc
	 * @return text
	 */
	public static String formatUser(DataCollection dc) {
		return '\'' + dc.getValue(AP.loggedInUserFieldName).toString() + '\'';
	}

	/**
	 * get text into a dynamic sql to represent a time stamp. We use system date
	 * as time stamp.
	 * 
	 * @return text
	 */
	public static String formatTimeStamp() {
		return AP.systemDateFunction;
	}

	/**
	 * format a value list into a comma separated string that is suitable to be
	 * put into a pair of '()' in a dynamic sql NOTE : this utility DOES NOT put
	 * () around the text for historical reasons.
	 * 
	 * @param values
	 * @return text
	 */
	public static String formatList(ValueList values) {
		if (values == null) {
			return "";
		}
		String[] textValues = values.getTextList();
		return SqlUtil.formatList(textValues, values.getValueType());
	}

	/**
	 * format a value list into a comma separated string that is suitable to be
	 * put into a pair of '()' in a dynamic sql NOTE : this utility DOES NOT put
	 * () around the text for historical reasons.
	 * 
	 * @param values
	 * @param valueType
	 * @return formatted text
	 */
	public static String formatList(String[] values, DataValueType valueType) {
		StringBuilder sbf = new StringBuilder();
		for (String val : values) {
			if (val == null || val.length() == 0) {
				continue;
			}
			val = SqlUtil.formatValue(val, valueType);
			if (val != null) {
				sbf.append(val).append(',');
			}
		}
		if (sbf.length() > 0) {
			sbf.deleteCharAt(sbf.length() - 1);
		}
		return sbf.toString();
	}

	/**
	 * get string to be inserted for this filter field. We look for
	 * name+Operator for the operator. If operator is between, we also look for
	 * the second field name+To
	 * 
	 * @param dc
	 * @param fieldName
	 * @param valueType
	 * @return text that represents this filter field in a dynamic sql
	 */
	public static String getFilterCondition(DataCollection dc,
			String fieldName, DataValueType valueType) {
		String operatorFieldName = fieldName + "Operator";

		Value v = dc.getValue(operatorFieldName);
		if (v == null) {
			return "";
		}

		int op;
		if (v.getValueType() == DataValueType.INTEGRAL) {
			op = (int) v.getIntegralValue();
		} else {
			op = Integer.parseInt(v.getTextValue());
		}

		if (op == SqlUtil.OPERATOR_ANY) {
			return "";
		}

		String val = dc.getTextValue(fieldName, null);
		if ((val == null) || (val.length() == 0)) {
			return "";
		}

		/**
		 * create a String of the form <op> 'value1' or optionally AND 'value2'
		 */
		StringBuilder sbf = new StringBuilder();
		sbf.append(" ").append(SqlUtil.getOperatorText(op));
		if (op == SqlUtil.OPERATOR_CONTAINS) {
			val = '%' + val + '%';
		} else if (op == SqlUtil.OPERATOR_STARTS_WITH) {
			val += '%';
		}
		sbf.append(SqlUtil.formatValue(val, valueType));

		/**
		 * take care of between operator
		 */
		if (op == SqlUtil.OPERATOR_BETWEEN) {
			String val2 = dc.getTextValue(fieldName + "To", "");
			sbf.append(" AND ");
			sbf.append(SqlUtil.formatValue(val2, valueType));
		}
		sbf.append(' ');
		return sbf.toString();
	}

}
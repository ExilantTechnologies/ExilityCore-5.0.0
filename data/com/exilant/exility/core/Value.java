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
import java.sql.Timestamp;
import java.util.Date;

/***
 * Represents base class of a value as per traditional programming practice.
 * boolean, number, text/string date/time are normally considered to be values
 * and any other object is considered to be 'data structure' that in turn
 * contain these basic values. This class is written as a being text value as
 * well as a base class for other value types to extend Also, idea is to
 * completely encapsulate extended classes, and use a factory pattern to provide
 * new Value object for any basic value like myValue = Value.newValue(22);
 * Values are immutable objects to ensure that they can, indeed, be used as
 * 'values'. Can values be null? I would have preferred to say a big NO, except
 * the fact that the primary focus of exility is transaction processing that
 * MUSt deal with NULLs in the data base. We have argued against NULL for text
 * and boolean, and got away, so far. we have argued that empty string is a good
 * substitute for null. And for boolean, we suggest that it is better to design
 * another boolean field to handle the situation rather than allowing boolean
 * field to have a null value. Oracle is still stuck with its legacy of
 * "empty string is null, but well almost always except when it is not" problem.
 * We handle this at the database utility layer. Numeric and date values can be
 * null. While comparisons involving Null will always result in false, we
 * through exception if null is involved in others.
 * 
 */
public class Value {
	/***
	 * NULL_VALUE is still a topic of internal debate. As of now, if a value is
	 * null, we want empty string to go to client. Hence this value.
	 */
	static final String NULL_VALUE = "";

	/***
	 * we frequently need text value. Once created, it is cached.
	 */
	protected String textValue;

	/***
	 * 
	 * @param integralValue
	 * @return IntegralValue that wraps the supplied integer
	 */
	public static Value newValue(long integralValue) {
		return new IntegralValue(integralValue);
	}

	/***
	 * Only two instances of BooleanValue ever exists. Through this method, we
	 * are able to implement BooleanValue more like constants
	 * 
	 * @param booleanValue
	 * @return BooleanValue that wraps input value
	 */
	public static Value newValue(boolean booleanValue) {
		if (booleanValue) {
			return BooleanValue.TRUE_VALUE;
		}

		return BooleanValue.FALSE_VALUE;
	}

	/***
	 * 
	 * @param dateValue
	 *            can be null
	 * @return DateValue
	 */
	public static Value newValue(Date dateValue) {
		return new DateValue(dateValue);
	}

	/***
	 * 
	 * @param dateValue
	 * @return Value
	 */
	@Deprecated
	public static Value newTimeStampValue(Date dateValue) {
		return new TimeStampValue(dateValue);
	}

	/***
	 * method to handle time stamp.Anant
	 * 
	 * @param dateValue
	 * @return Value
	 */
	@Deprecated
	public static Value newTimeStampValue(Timestamp dateValue) {
		return new TimeStampValue(dateValue);
	}

	/***
	 * 
	 * @param decimalValue
	 * @return Value
	 */
	public static Value newValue(double decimalValue) {
		return new DecimalValue(decimalValue);
	}

	/***
	 * A utility method that creates a Value of the right type after parsing the
	 * string value if the string is empty, numeric and date types create the
	 * desired value type with null as their type. Boolean treats it as false,
	 * while string is perfectly fine with an empty string as its non-null value
	 * 
	 * @param textValue
	 * @return Value
	 */
	public static Value newValue(String textValue) {
		if (textValue == null) {
			return new NullValue(DataValueType.TEXT);
		}

		return new Value(textValue);
	}

	/***
	 * utility method to parse and create a value of given type
	 * 
	 * @param fieldValue
	 * @param type
	 * @return Value
	 */
	@SuppressWarnings("deprecation")
	public static Value newValue(String fieldValue, DataValueType type) {
		if (fieldValue == null
				|| (DataValueType.TEXT != type && fieldValue.length() == 0)) {
			return new NullValue(type);
		}
		try {
			Value value = null;
			switch (type) {
			case NULL:
				return new NullValue(DataValueType.TEXT);
			case TEXT:
				return new Value(fieldValue);

			case INTEGRAL:
				value = new IntegralValue(fieldValue);
				value.textValue = fieldValue;
				return value;

			case BOOLEAN:
				value = new BooleanValue(fieldValue);
				value.textValue = fieldValue;
				return value;

			case DECIMAL:
				value = new DecimalValue(fieldValue);
				value.textValue = fieldValue;
				return value;

			case DATE:
				value = new DateValue(fieldValue);
				value.textValue = fieldValue;
				return value;

			case TIMESTAMP:
				value = new TimeStampValue(fieldValue);
				value.textValue = fieldValue;
				return value;

			default:
				Spit.out("Value.newValue() does not handle value type " + type);
			}
		} catch (Exception e) {
			Spit.out("Error: " + fieldValue + " could not be parsed as a "
					+ type);
			Spit.out(e);
		}
		return null;
	}

	protected Value() {
	}

	/***
	 * Is this value null? false for all sub types, except NULL type
	 * 
	 * @return true if the value is null
	 */
	public boolean isNull() {
		return false;
	}

	/***
	 * whether the value is specified?
	 * 
	 * @return true ONLY if the value exists, and it is not NULL
	 */
	public boolean isSpecified() {
		if (this.isNull()) {
			return false;
		}
		return this.isSpecifiedByType();
	}

	/***
	 * private constructor
	 * 
	 * @param textValue
	 */
	private Value(String textValue) {
		this.textValue = textValue;
	}

	/***
	 * try and parse this text value as boolean. over-ridden by sub-classes
	 * 
	 * @return true if text value is 'true' or '1', false otherwise
	 */
	public boolean getBooleanValue() {
		return BooleanValue.parse(this.textValue);
	}

	/***
	 * try and parse text into date. this is over-ridden by sub-classes
	 * 
	 * @return valid date if text value can be parsed as a valid text. null
	 *         otherwise
	 */
	public Date getDateValue() {
		return DateUtility.parseDate(this.textValue);
	}

	/***
	 * try and parse text into date-time
	 * 
	 * @return valid date if text value can be parsed as a valid text. null
	 *         otherwise
	 */
	Date getTimeStampValue() {
		return DateUtility.parseDateTime(this.textValue);
	}

	/***
	 * try and parse text value as an integral value
	 * 
	 * @return parsed integral value, or zero if it is not a valid integer
	 */
	public long getIntegralValue() {
		try {
			return Long.parseLong(this.textValue);
		} catch (Exception e) {
			return 0;
		}
	}

	/***
	 * try and parse into a decimal value.
	 * 
	 * @return parsed decimal value, or zero is it not a valid number
	 */
	public double getDecimalValue() {
		return Double.parseDouble(this.textValue);
	}

	/***
	 * get the text value
	 * 
	 * @return text representation of this value, as per normal formatting rule
	 */
	public String getTextValue() {
		return this.toString();
	}

	/***
	 * Convenient method to be over-ridden by sub classes
	 * 
	 * @return underlying value type
	 */
	DataValueType getValueType() {
		return DataValueType.TEXT;
	}

	@Override
	public String toString() {
		return this.format();
	}

	/***
	 * Convenient method for all sub-classes to raise exception when an operator
	 * is not valid for supplied value
	 * 
	 * @param oper
	 *            operation that is invalid
	 * @param val2
	 *            value that was supplied for the operation
	 */
	protected void raiseException(String oper, Value val2) {
		throw new RuntimeException("Value of type "
				+ this.getValueType().toString() + " with value "
				+ this.getTextValue() + "  can not be " + oper
				+ " a value of type " + val2.getValueType() + " with value "
				+ val2.getTextValue());
	}

	/***
	 * format value into text
	 * 
	 * @return formatted text
	 */
	String format() {
		return this.textValue;
	}

	/***
	 * add is valid so long as at least one value is text, or both are numeric,
	 * or first is is date and second one is numeric
	 * 
	 * @param val2
	 *            values to be appended
	 * @return text that is result of appending val2 to this value
	 */
	Value add(Value val2) {
		if (this.getValueType() == DataValueType.TEXT
				|| val2.getValueType() == DataValueType.TEXT) {
			return Value.newValue(this.toString() + val2.toString());
		}

		this.raiseException("added to", val2);
		return null;
	}

	/***
	 * numeric - numeric, date - numeric, or date - date are valid subtracts
	 * 
	 * @param val2
	 *            value to be subtracted from this value
	 * @return number except if it is date - number, in which case it is a date
	 */
	Value subtract(Value val2) {
		this.raiseException("subtracted from", val2);
		return null;
	}

	/***
	 * valid only for numeric values
	 * 
	 * @param val2
	 * @return
	 */
	Value multiply(Value val2) {
		this.raiseException("multiplied to", val2);
		return null;
	}

	/***
	 * valid only for numeric values
	 * 
	 * @param val2
	 * @return
	 */
	Value divide(Value val2) {
		this.raiseException("divided by", val2);
		return null;
	}

	/***
	 * valid only for numeric values
	 * 
	 * @param val2
	 * @return
	 */
	Value remainder(Value val2) {
		this.raiseException("divided by", val2);
		return null;
	}

	/***
	 * valid only for numeric values
	 * 
	 * @param val2
	 * @return
	 */
	Value power(Value val2) {
		this.raiseException("raised to power", val2);
		return null;
	}

	/***
	 * case-insensitive string comparison is used if any one of the operand is
	 * text. Otherwise, type-safe comparison is used
	 * 
	 * @param val2
	 * @return false if any one is NULL. Usual meaning otherwise
	 */
	Value equal(Value val2) {
		if (this.getValueType() == DataValueType.TEXT
				|| val2.getValueType() == DataValueType.TEXT) {
			return Value.newValue(this.toString().equalsIgnoreCase(
					val2.toString()));
		}

		if (this.getValueType() == DataValueType.NULL
				|| val2.getValueType() == DataValueType.NULL) {
			return Value.newValue(false);
		}

		this.raiseException("compared to", val2);
		return null;
	}

	/***
	 * case-insensitive string comparison is used if any one of the operand is
	 * text. Otherwise, type-safe comparison is used
	 * 
	 * @param val2
	 * @return false if any one is NULL. Usual meaning otherwise
	 */
	Value notEqual(Value val2) {
		if (this.getValueType() == DataValueType.TEXT
				|| val2.getValueType() == DataValueType.TEXT) {
			return Value.newValue(!this.toString().equalsIgnoreCase(
					val2.toString()));
		}

		if (this.getValueType() == DataValueType.NULL
				|| val2.getValueType() == DataValueType.NULL) {
			return Value.newValue(false);
		}

		this.raiseException("compared to", val2);
		return null;
	}

	/***
	 * I HAVE TO STUDY THIS ONCE AGAIN BEFORE WRITING DOC !!!!!
	 * 
	 * @param val2
	 * @return
	 */
	Value equal(NullValue val2) {
		Value val = val2.getNonNullValue();
		if (val == null) {
			this.raiseException("compared to", val2);
			return BooleanValue.FALSE_VALUE;
		}
		return val.equal(this);
	}

	/***
	 * I HAVE TO STUDY THIS ONCE AGAIN BEFORE WRITING DOC !!!!!
	 * 
	 * @param val2
	 * @return
	 */
	Value notEqual(NullValue val2) {
		Value val = val2.getNonNullValue();
		if (val == null) {
			this.raiseException("compared to", val2);
			return BooleanValue.FALSE_VALUE;
		}
		return val.notEqual(this);
	}

	/***
	 * case-insensitive string comparison is used if any one of the operand is
	 * text. Otherwise, type-safe comparison is used
	 * 
	 * @param val2
	 * @return false if any one is NULL. Usual meaning otherwise
	 */
	Value lessThan(Value val2) {
		if (this.getValueType() == DataValueType.TEXT
				|| val2.getValueType() == DataValueType.TEXT) {
			return Value.newValue(this.toString().compareToIgnoreCase(
					val2.toString()) < 0);
		}

		if (this.getValueType() == DataValueType.NULL
				|| val2.getValueType() == DataValueType.NULL) {
			return Value.newValue(false);
		}

		this.raiseException("compared to", val2);
		return null;
	}

	/***
	 * case-insensitive string comparison is used if any one of the operand is
	 * text. Otherwise, type-safe comparison is used
	 * 
	 * @param val2
	 * @return false if any one is NULL. Usual meaning otherwise
	 */
	Value lessThanOrEqual(Value val2) {
		if (this.getValueType() == DataValueType.TEXT
				|| val2.getValueType() == DataValueType.TEXT) {
			return Value.newValue(this.toString().compareToIgnoreCase(
					val2.toString()) <= 0);
		}

		if (this.getValueType() == DataValueType.NULL
				|| val2.getValueType() == DataValueType.NULL) {
			return Value.newValue(false);
		}

		this.raiseException("compared to", val2);
		return null;
	}

	/***
	 * case-insensitive string comparison is used if any one of the operand is
	 * text. Otherwise, type-safe comparison is used
	 * 
	 * @param val2
	 * @return false if any one is NULL. Usual meaning otherwise
	 */
	Value greaterThan(Value val2) {
		if (this.getValueType() == DataValueType.TEXT
				|| val2.getValueType() == DataValueType.TEXT) {
			return Value.newValue(this.toString().compareToIgnoreCase(
					val2.toString()) > 0);
		}

		if (this.getValueType() == DataValueType.NULL
				|| val2.getValueType() == DataValueType.NULL) {
			return Value.newValue(false);
		}

		this.raiseException("compared to", val2);
		return null;
	}

	/***
	 * case-insensitive string comparison is used if any one of the operand is
	 * text. Otherwise, type-safe comparison is used
	 * 
	 * @param val2
	 * @return false if any one is NULL. Usual meaning otherwise
	 */
	Value greaterThanOrEqual(Value val2) {
		if (this.getValueType() == DataValueType.TEXT
				|| val2.getValueType() == DataValueType.TEXT) {
			return Value.newValue(this.toString().compareToIgnoreCase(
					val2.toString()) >= 0);
		}

		if (this.getValueType() == DataValueType.NULL
				|| val2.getValueType() == DataValueType.NULL) {
			return Value.newValue(false);
		}

		this.raiseException("compared to", val2);
		return null;
	}

	/***
	 * Valid only amongst boolean values
	 * 
	 * @param val2
	 * @return
	 */
	Value and(Value val2) {
		this.raiseException("logically operated with", val2);
		return null;
	}

	/***
	 * Valid only amongst boolean values
	 * 
	 * @param val2
	 * @return
	 */
	Value or(Value val2) {
		this.raiseException("logically operated with", val2);
		return null;
	}

	/***
	 * Get string that is suitable to be used as quoted string
	 * 
	 * @return string within double quotes in which double quotes are escaped,
	 */
	String getQuotedValue() {
		return Chars.TEXT_QUOTE + this.getTextValue().replaceAll("\"", "\"\"")
				+ Chars.TEXT_QUOTE;
	}

	/***
	 * Convenient comparator using which several other operations can be done
	 * 
	 * @param comparator
	 * @param value
	 * @return
	 */
	boolean compare(Comparator comparator, Value value) {
		if (value == null) {
			return false;
		}
		switch (comparator) {
		case EQUALTO:
			return this.equal(value).getBooleanValue();
		case NOTEQUALTO:
			return this.notEqual(value).getBooleanValue();
		case GREATERTHAN:
			return this.greaterThan(value).getBooleanValue();
		case GREATERTHANOREQUALTO:
			return this.greaterThanOrEqual(value).getBooleanValue();
		case LESSTHAN:
			return this.lessThan(value).getBooleanValue();
		case LESSTHANOREQUALTO:
			return this.lessThanOrEqual(value).getBooleanValue();
		case DOESNOTEXIST:
			return false;
		case EXISTS:
			return true;
		case CONTAINS:
			return this.textValue.contains(value.toString());
		case FILTER:
			return false;
		case STARTSWITH:
			return this.textValue.startsWith(value.toString());
		default:
			Spit.out("Value.compare() has not implemented comparator "
					+ comparator);
			break;
		}

		return false;
	}

	/***
	 * delegated to this class to avoid switch-case in our db-driver (dbHandle)
	 * 
	 * @param statement
	 *            statement to which this value is to be added
	 * @param idx
	 *            index at which to add this value
	 * @throws SQLException
	 */
	public void addToPrepearedStatement(PreparedStatement statement, int idx)
			throws SQLException {
		if (this.isNull()) {
			statement.setNull(idx, java.sql.Types.VARCHAR);
			return;
		}
		statement.setString(idx, this.textValue);

	}

	/***
	 * Returns a value that can be used for testing. "a", 1, 1.1, today, false
	 * are returned.
	 * 
	 * @param valueType
	 *            type of value you want to generate test case for
	 * @return test value of appropriate type
	 */
	public static Value getTestValue(DataValueType valueType) {
		switch (valueType) {
		case TEXT:
			return Value.newValue("a");
		case INTEGRAL:
			return Value.newValue(1);
		case DECIMAL:
			return Value.newValue(1.0);
		case BOOLEAN:
			return Value.newValue(false);
		case DATE:
			return Value.newValue(new Date());
		case TIMESTAMP:
			return Value.newTimeStampValue(new Date());
		case NULL:
			return new NullValue(DataValueType.TEXT);
		default:
			Spit.out("Value.getTestValue() has not implemented value type "
					+ valueType);
			break;
		}
		return null;
	}

	/***
	 * Returns data type as in SpreadSheet xml for the supplied data-type
	 * 
	 * @param valueType
	 *            type of value
	 * @return xml data type
	 */
	public static String getXlType(DataValueType valueType) {
		switch (valueType) {
		case INTEGRAL:
		case DECIMAL:
			return "Number";
		case BOOLEAN:
			return "Boolean";
		case DATE:
		case TIMESTAMP:
			return "DateTime";
		default:
			return "String";
		}
	}

	// We can debate whether this method should be here, or in XlUtil. Xl is
	// unlikely to add new data types, but we are.. Hence it is here.
	/***
	 * Returns data type as in SpreadSheet xml for the supplied data-type
	 * 
	 * @param type
	 *            type of value
	 * @return xml data type
	 */
	public static DataValueType getTypeFromXl(String type) {
		if (type.equals(XlUtil.TEXT_TYPE)) {
			return DataValueType.TEXT;
		}

		if (type.equals(XlUtil.NUMBER_TYPE)) {
			return DataValueType.DECIMAL;
		}

		if (type.equals(XlUtil.DATE_TYPE)) {
			return DataValueType.DATE;
		}

		if (type.equals(XlUtil.BOOLEAN_TYPE)) {
			return DataValueType.BOOLEAN;
		}

		return DataValueType.TEXT;
	}

	/**
	 * parse text into required type of value. returns null in case the text
	 * value can not be parsed
	 * 
	 * @param valueToParse
	 *            to be parsed
	 * @param valueType
	 *            text to be parsed as
	 * @return value or null in case of parse error
	 */
	public static Value parse(String valueToParse, DataValueType valueType) {
		if (valueToParse == null || valueToParse.length() == 0) {
			return null;
		}
		Value value = null;
		switch (valueType) {
		case TEXT:
		case TIMESTAMP:
			value = Value.newValue(valueToParse);
			break;

		case INTEGRAL:
			try {
				value = Value.newValue(Long.parseLong(valueToParse));
			} catch (Exception e) {
				try {
					return Value.newValue((long) Double
							.parseDouble(valueToParse));
				} catch (Exception e1) {
					//
				}
			}
			break;
		case BOOLEAN:
			if (valueToParse.equalsIgnoreCase(BooleanValue.TRUE)
					|| valueToParse.equals("1")) {
				value = Value.newValue(true);
			} else if (valueToParse.equalsIgnoreCase(BooleanValue.FALSE)
					|| valueToParse.equals("0")) {
				value = Value.newValue(false);
			}
			break;
		case DATE:
			Date date = DateUtility.parseDate(valueToParse);
			if (date != null) {
				value = Value.newValue(date);
			}
			break;

		case DECIMAL:
			try {
				value = Value.newValue(Double.parseDouble(valueToParse));
			} catch (Exception e) {
				//
			}
			break;

		default:
			break;
		}
		return value;
	}

	/**
	 * method to be implemented by each sub-class
	 * 
	 * @return
	 */
	protected boolean isSpecifiedByType() {
		return this.textValue.length() > 0;
	}
}
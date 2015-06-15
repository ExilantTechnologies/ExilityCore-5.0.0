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

/***
 * Equivalent of an array of Values. Though list by itself is not used much,
 * grid is modeled as a collection of valueList This class is used as an
 * implementation for text list, and used as a base class for lists of other
 * value type. We have used ValueList to be TextValueLIst as well as base class
 * for other types. This design may confuse you at time.
 */
public class ValueList {
	protected static final String MSG = "List has a null value and hence can not return any other type of value";
	protected static final String INDEX_OUT_OF_RANGE = "Value list is accessed with an invalid index of ";
	protected static final String INTEGRAL = "integral";
	protected static final String DECIMAL = "decimal";
	protected static final String DATE = "date";
	protected static final String BOOLEAN = "boolean";
	protected static final String TIMESTAMP = "timestamp";

	/***
	 * Not sure why we have made this public. We have to re-factor this to
	 * change that. holds text value irrespective of the underlying value type.
	 */
	public String[] textList = null;

	/***
	 * indicates whether the value in this index is null
	 */
	boolean[] isNullList = null;

	/***
	 * convert an integral array into a VaolueList
	 * 
	 * @param integralList
	 * @return INtegralValueList for this array of values
	 */
	public static IntegralValueList newList(long[] integralList) {
		return new IntegralValueList(integralList);
	}

	/***
	 * convert a decimal array into ValueList
	 * 
	 * @param decimalList
	 * @return instance of DecimalValue list for this array
	 */
	public static DecimalValueList newList(double[] decimalList) {
		return new DecimalValueList(decimalList);
	}

	/***
	 * convert a date array into a ValueList
	 * 
	 * @param dateList
	 * @return a DateValueList for this array
	 */
	public static DateValueList newList(Date[] dateList) {
		return new DateValueList(dateList);
	}

	/***
	 * convert a date array into a ValueList of type TimeStamp
	 * 
	 * @param dateList
	 * @return a TimeStampValueList for this array of values
	 * @deprecated
	 * 
	 */
	@Deprecated
	public static TimeStampValueList newTimeStampValueList(Date[] dateList) {
		return new TimeStampValueList(dateList);
	}

	/***
	 * convert an boolean array into a ValueList
	 * 
	 * @param booleanList
	 * @return BooleanValueList for this array of values
	 */
	public static BooleanValueList newList(boolean[] booleanList) {
		return new BooleanValueList(booleanList);
	}

	/***
	 * convert an array of String into a ValueList
	 * 
	 * @param textList
	 * @return ValueList for this array of string
	 */
	public static ValueList newList(String[] textList) {
		return new ValueList(textList);
	}

	/***
	 * parse an array of string into a value-list of specified type
	 * 
	 * @param textList
	 * @param type
	 * @return ValueList of appropriate valueType
	 */
	@SuppressWarnings("deprecation")
	public static ValueList newList(String[] textList, DataValueType type) {
		switch (type) {
		case NULL:
			return null;
		case TEXT:
			return new ValueList(textList);

		case INTEGRAL:
			return new IntegralValueList(textList);

		case DATE:
			return new DateValueList(textList);

		case DECIMAL:
			return new DecimalValueList(textList);

		case BOOLEAN:
			return new BooleanValueList(textList);

		case TIMESTAMP:
			return new TimeStampValueList(textList);
		default:
			Spit.out("ERROR : DataValueType " + type
					+ " is defined but not implemented fully");
			break;
		}
		return null;
	}

	/***
	 * create an empty value-list if specific type for a specific length
	 * 
	 * @param type
	 * @param length
	 * @return an empty value list of the desired type
	 */
	@SuppressWarnings("deprecation")
	public static ValueList newList(DataValueType type, int length) {
		switch (type) {
		case NULL:
			return null;
		case TEXT:
			return new ValueList(length);

		case INTEGRAL:
			return new IntegralValueList(length);

		case DATE:
			return new DateValueList(length);

		case DECIMAL:
			return new DecimalValueList(length);

		case BOOLEAN:
			return new BooleanValueList(length);

		case TIMESTAMP:
			return new TimeStampValueList(length);
		default:
			Spit.out("ERROR : DataValueType " + type
					+ " is defined but not implemented fully");
			break;
		}
		return null;
	}

	/**
	 * default constructor
	 */
	public ValueList() {
	}

	/***
	 * create a ValueLIst from the supplied array of Strings
	 * 
	 * @param textList
	 */
	public ValueList(String[] textList) {
		this.textList = textList;
		this.isNullList = new boolean[textList.length];
		for (int i = 0; i < textList.length; i++) {
			String text = textList[i];
			if (text == null || text.length() == 0) {
				this.isNullList[i] = true;
				this.textList[i] = Value.NULL_VALUE;
			}
		}
	}

	/***
	 * create an empty ValueList of specified length. You can add values that
	 * later.
	 * 
	 * @param length
	 */
	public ValueList(int length) {
		this.textList = new String[length];
		this.isNullList = new boolean[length];
	}

	/***
	 * get the value type of the underlying list.
	 * 
	 * @return value type of this list.
	 */
	public DataValueType getValueType() {
		return DataValueType.TEXT;
		/**
		 * this will be over-ridden by sub-types
		 */
	}

	/***
	 * return capacity of this list
	 * 
	 * @return number of entries in this list
	 */
	public final int length() {
		if (this.isNullList == null) {
			return 0;
		}
		return this.isNullList.length;
	}

	/***
	 * common method for all extended classes to raise parse exception
	 * 
	 * @param expectedType
	 *            expected value type
	 */
	protected final void raiseException(String expectedType) {
		throw new RuntimeException("Value list is of type "
				+ this.getValueType() + " while " + expectedType
				+ " value is being tried to set/get.");
	}

	/***
	 * get an integral array. To be implemented by each sub-classes based on
	 * compatibility of value types
	 * 
	 * @return list, if type is compatible. throws run-time exception otherwise
	 */
	public long[] getIntegralList() {
		this.raiseException(ValueList.INTEGRAL);
		return null;
	}

	/***
	 * get an integral value from the list. To be implemented by each
	 * sub-classes based on compatibility of value types
	 * 
	 * @param index
	 * @return an integral value at that index if type is compatible. throws
	 *         run-time exception otherwise
	 */
	public long getIntegralValue(int index) {
		this.raiseException(ValueList.INTEGRAL);
		return 0;
	}

	/***
	 * set a value to the list. Sub-classes have to implement it based on
	 * compatibility of value type
	 * 
	 * @param integralValue
	 * @param index
	 */
	public void setIntegralValue(long integralValue, int index) {
		this.raiseException(ValueList.INTEGRAL);
	}

	/***
	 * get underlying values as decimal array. throws run-time exception
	 * otherwise
	 * 
	 * @return decimal value list. throws run-time exception otherwise
	 */
	public double[] getDecimalList() {
		this.raiseException(ValueList.DECIMAL);
		return null;
	}

	/***
	 * returns decimal value at the index, or throws exception if underlying
	 * type is nnot copatible
	 * 
	 * @param index
	 *            zero based
	 * @return decimal value at that index
	 */
	public double getDecimalValue(int index) {
		this.raiseException(ValueList.DECIMAL);
		return 0;
	}

	/***
	 * set a value at the specific zero-based position
	 * 
	 * @param decimalValue
	 * @param index
	 *            zero based
	 */
	public void setDecimalValue(double decimalValue, int index) {
		this.raiseException(ValueList.DECIMAL);
	}

	/***
	 * get an array of values
	 * 
	 * @return date list. run-time exception on incompatible type.
	 */
	public Date[] getDateList() {
		this.raiseException(ValueList.DATE);
		return null;
	}

	/***
	 * get value at the specified row
	 * 
	 * @param index
	 *            zero based index into list
	 * @return date value at that index. run-time exception on incompatible
	 *         type.
	 */
	public Date getDateValue(int index) {
		this.raiseException(ValueList.DATE);
		return new Date();
	}

	/***
	 * set value at the specified row
	 * 
	 * @param dateValue
	 * @param index
	 *            zero based index into list
	 */
	public void setDateValue(Date dateValue, int index) {
		this.raiseException(ValueList.DATE);
	}

	/***
	 * get array of boolean values
	 * 
	 * @return boolean value list. run-time exception on incompatible type.
	 */
	public boolean[] getBooleanList() {
		this.raiseException(ValueList.BOOLEAN);
		return null;
	}

	/***
	 * get value at the specified row
	 * 
	 * @param index
	 *            zero based index into list
	 * @return boolean value at that idex. run-time exception on incompatible
	 *         type.
	 */
	public boolean getBooleanValue(int index) {
		this.raiseException(ValueList.BOOLEAN);
		return false;
	}

	/***
	 * set value at the row
	 * 
	 * @param booleanValue
	 * @param index
	 *            zero based index into list
	 */

	public void setBooleanValue(boolean booleanValue, int index) {
		this.raiseException(ValueList.BOOLEAN);
	}

	/***
	 * get an array of values as dates
	 * 
	 * @return timeStampList. run-time exception on incompatible type.
	 */
	public Date[] getTimeStampList() {
		this.raiseException(ValueList.TIMESTAMP);
		return null;
	}

	/***
	 * get value of the row
	 * 
	 * @param index
	 *            zero based index into list
	 * @return time stamp value at that index. run-time exception on
	 *         incompatible type.
	 */
	public Date getTimeStampValue(int index) {
		this.raiseException(ValueList.TIMESTAMP);
		return new Date();
	}

	/***
	 * set value for the row
	 * 
	 * @param timeStampValue
	 * @param index
	 *            zero based index into list
	 */
	public void setTimeStampValue(Date timeStampValue, int index) {
		this.raiseException(ValueList.TIMESTAMP);
	}

	/***
	 * get value at the given row
	 * 
	 * @param index
	 *            zero based index into list
	 * @return value at that index
	 */
	public Value getValue(int index) {
		if (this.isNullList[index]) {
			return new NullValue(this.getValueType());
		}
		return Value.newValue(this.textList[index]);
	}

	/***
	 * set value, at the given row
	 * 
	 * @param value
	 * @param index
	 *            zero based index into list
	 */
	public final void setValue(Value value, int index) {
		if (value == null || value.isNull()) {
			this.isNullList[index] = true;
			if (this.textList != null) {
				this.textList[index] = Value.NULL_VALUE;
			}
			return;
		}
		DataValueType dtv = value.getValueType();
		if (dtv != this.getValueType()) {
			this.raiseException(dtv.toString());
		}

		this.isNullList[index] = false;
		if (this.textList != null) {
			this.textList[index] = value.getTextValue();
		}
		this.setValueByType(value, index);
	}

	/***
	 * set value by its type
	 * 
	 * @param value
	 * @param index
	 *            zero based index into list
	 */
	protected void setValueByType(Value value, int index) {
		// to be implemented by sub-classes
	}

	/***
	 * get values as an array of text. Same as format()
	 * 
	 * @return array of text values
	 */
	public final String[] getTextList() {
		if (this.textList == null) {
			this.format();
		}
		return this.textList;
	}

	/***
	 * get the value at this row as text
	 * 
	 * @param index
	 *            zero based index into list
	 * @return value as text
	 */
	public final String getTextValue(int index) {
		if (this.textList == null) {
			this.format();
		}
		return this.textList[index];
	}

	/***
	 * set text value to this row
	 * 
	 * @param textValue
	 * @param index
	 *            zero based index into list
	 */
	public void setTextValue(String textValue, int index) {
		if (textValue == null || textValue.length() == 0) {
			this.textList[index] = Value.NULL_VALUE;
			this.isNullList[index] = true;
			return;
		}
		this.textList[index] = textValue;
		this.isNullList[index] = false;
	}

	/***
	 * set NULL_VALUE to this row
	 * 
	 * @param index
	 *            zero based index into list
	 */
	public final void setNullValue(int index) {
		if (this.textList != null) {
			this.textList[index] = Value.NULL_VALUE;
		}
		this.isNullList[index] = true;
	}

	/***
	 * validate that the values in this list are less than or equal to the
	 * corresponding values in the supplied list
	 * 
	 * @param toList
	 *            that has to be used as "to" values
	 * @return true if validation succeeds, false otherwise
	 */
	public boolean validateTo(ValueList toList) {
		if (toList.getValueType() != this.getValueType()
				|| toList.length() != this.length()) {
			return false;
		}

		String[] toArray = toList.textList;
		for (int i = 0; i < this.textList.length; i++) {
			if (this.isNullList[i] || toList.isNullList[i]) {
				return false;
			}
			if (this.textList[i].compareTo(toArray[i]) > 0) {
				return false;
			}
		}
		return true;
	}

	/***
	 * basedOnField means that the field is optional, but is mandatory if the
	 * basedOnField value is specified.
	 * 
	 * @param basedOnList
	 *            list of values of the basedOnField
	 * @return whether the validation is successful
	 */
	public boolean validateBasedOn(ValueList basedOnList) {
		if (basedOnList.length() != this.length()) {
			return false;
		}

		String[] basedOnArray = basedOnList.getTextList();
		for (int i = 0; i < this.textList.length; i++) {
			if (basedOnArray[i].length() > 0 && this.isNullList[i]) {
				return false;
			}
		}
		return true;
	}

	/***
	 * formats the list into a text array
	 * 
	 * @return text array
	 */
	public String[] format() {
		if (this.textList == null) {
			this.textList = new String[0];
		}

		return this.textList;
	}

	/***
	 * returns an array of values that are cloned from the list. Changing the
	 * array does not affect this list
	 * 
	 * @return array of Value
	 */
	public Value[] getValueArray() {
		Value[] values = new Value[this.length()];
		for (int i = 0; i < this.textList.length; i++) {
			values[i] = (this.isNullList[i]) ? new NullValue(
					this.getValueType()) : Value.newValue(this.textList[i]);
		}
		return values;
	}

	/***
	 * is this row value set to null?
	 * 
	 * @param index
	 *            zero based index into list
	 * @return true if value at that index is null, false otherwise
	 */
	public final boolean isNull(int index) {
		return this.isNullList[index];
	}

	/***
	 * Append supplied list to this list
	 * 
	 * @param lst
	 *            to be appended
	 * @return length of the list after appending. 0 implies that the list was
	 *         not appended.
	 */
	public int append(ValueList lst) {
		if (lst.getValueType() != DataValueType.TEXT) {
			Spit.out("list of type " + lst.getValueType()
					+ " can not be appended to a list of type TEXT");
			return 0;
		}
		int currentLength = this.length();
		int lengthToAdd = lst.length();
		if (lengthToAdd == 0) {
			return currentLength;
		}

		int newLength = currentLength + lengthToAdd;
		String[] newList = new String[newLength];
		boolean[] newIsNullList = new boolean[newLength];
		for (int i = 0; i < currentLength; i++) {
			newList[i] = this.textList[i];
			newIsNullList[i] = this.isNullList[i];
		}
		for (int i = 0; i < lengthToAdd; i++) {
			newList[currentLength] = lst.textList[i];
			newIsNullList[currentLength] = lst.isNullList[i];
			currentLength++;
		}

		this.textList = newList;
		this.isNullList = newIsNullList;

		return newLength;
	}

	/***
	 * create a new value list based on filtering decisions
	 * 
	 * @param newNumberOfRows
	 *            should match the number of rows marked as filtered in
	 *            selections array
	 * @param selections
	 *            array that tells whether a specific row is to be copied or not
	 * @return filtered list
	 */
	public ValueList filter(int newNumberOfRows, boolean[] selections) {
		boolean[] ba = new boolean[newNumberOfRows];
		String[] ta = new String[newNumberOfRows];
		int j = 0;
		for (int i = 0; i < this.isNullList.length; i++) {
			if (selections[i]) {
				ba[j] = this.isNullList[i];
				ta[j] = this.textList[i];
				j++;
			}
		}
		ValueList l = new ValueList();
		l.isNullList = ba;
		l.textList = ta;
		return l;
	}

	@Override
	public ValueList clone() {
		boolean[] ba = new boolean[this.length()];
		for (int i = 0; i < this.isNullList.length; i++) {
			ba[i] = this.isNullList[i];
		}
		String[] ta = new String[this.length()];
		for (int i = 0; i < this.textList.length; i++) {
			ta[i] = this.textList[i];
		}
		ValueList l = new ValueList();
		l.isNullList = ba;
		l.textList = ta;
		return l;
	}

	/***
	 * create a new list that picks only the filtered values from the current
	 * list
	 * 
	 * @param filteredList
	 *            list to which filtered values are to be copied to
	 * @param newNumberOfRows
	 * @param selections
	 */
	protected final void copyFilteredText(ValueList filteredList,
			int newNumberOfRows, boolean[] selections) {
		if (this.textList == null) {
			return;
		}

		filteredList.textList = new String[newNumberOfRows];
		int j = 0;
		for (int i = 0; i < this.textList.length; i++) {
			if (!selections[i]) {
				continue;
			}
			if (j >= newNumberOfRows) {
				Spit.out("Design error: copyFilteredText() is called with newNumberOfRows = "
						+ newNumberOfRows
						+ " while there are more values in the filter selection array");
				continue;
			}
			filteredList.textList[j] = this.textList[i];
			j++;
		}
	}

	/***
	 * convenient method to just get a sample list of specified type
	 * 
	 * @param valueType
	 * @return
	 */
	static ValueList getTestList(DataValueType valueType) {
		switch (valueType) {
		case TEXT:
			String[] a1 = { "a" };
			return ValueList.newList(a1);
		case INTEGRAL:
			long[] a2 = { 1 };
			return ValueList.newList(a2);
		case DECIMAL:
			double[] a3 = { 1.1 };
			return ValueList.newList(a3);
		case BOOLEAN:
			boolean[] a4 = { false };
			return ValueList.newList(a4);
		case DATE:
			Date[] a5 = { new Date() };
			return ValueList.newList(a5);
		case TIMESTAMP:
			Date[] a6 = { new Date() };
			return ValueList.newList(a6);
		case NULL:
			return ValueList.newList(DataValueType.NULL, 1);
		default:
			Spit.out("Error : ValueList.getTestList() has not implemented its logic for type "
					+ valueType);
			break;

		}
		return null;
	}

	/***
	 * compare this list with the supplied list and report mismatches in dc
	 * 
	 * @param otherList
	 *            list to compare with
	 * @param dc
	 *            Data Collection in which errors are to be reported
	 * @param listName
	 *            name of list to be used for raising error message
	 * @return true if comparison succeeds, false otherwise
	 */
	public boolean equal(ValueList otherList, DataCollection dc, String listName) {
		if (this.length() != otherList.length()) {
			String msg = listName + ": Found " + this.length()
					+ " value sin list while " + otherList.length()
					+ " values were expected.";
			if (dc != null) {
				dc.addError(msg);
			} else {
				Spit.out(msg);
			}
			return false;
		}

		if (this.getValueType() != otherList.getValueType()) {
			String msg = listName + ": Found " + this.getValueType()
					+ " type while " + otherList.getValueType()
					+ " was expected.";
			if (dc != null) {
				dc.addError(msg);
			} else {
				Spit.out(msg);
			}
			return false;
		}

		// let us finish comparison here with text type, rather than delegating
		// it to extended classes
		boolean matched = true;
		String[] thisTexts = this.getTextList();
		String[] otherTexts = otherList.getTextList();
		for (int i = 0; i < thisTexts.length; i++) {
			if (thisTexts[i].equals(otherTexts[i])) {
				continue;
			}

			String msg = listName + ": row nbr (1 based) - " + (i + 1)
					+ " Found '" + thisTexts[i] + "' while '" + otherTexts[i]
					+ "' was expected.";
			if (dc != null) {
				dc.addError(msg);
			} else {
				Spit.out(msg);
			}
			matched = false;
		}

		return matched;
	}

	/**
	 * set given value to the entire list. This is a short-cut to iterating and
	 * setting value. It is also optimized
	 * 
	 * @param keyValue
	 */
	public void setValueToAll(Value keyValue) {
		if (keyValue.getValueType() != this.getValueType()) {
			Spit.out("Incompatible value type for setValueToALl. Value not set.");
			return;
		}
		this.setValueInternally(keyValue);
	}

	/**
	 * assign same value to all elements in the internal array
	 */
	protected void setValueInternally(Value keyValue) {
		String textValue = keyValue.getTextValue();
		for (int i = this.length() - 1; i >= 0; i--) {
			this.textList[i] = textValue;
			this.isNullList[i] = false;
		}
	}
}
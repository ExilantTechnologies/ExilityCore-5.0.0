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
 * represents an array of dates
 * 
 */
public class DateValueList extends ValueList {
	protected Date[] list;

	DateValueList() {

	}

	/**
	 * construct value list from an array of dates
	 * 
	 * @param list
	 */
	protected DateValueList(Date[] list) {
		this.list = list;
		this.isNullList = new boolean[list.length];
	}

	/**
	 * parse array of text into value list
	 * 
	 * @param textList
	 */
	protected DateValueList(String[] textList) {
		this.textList = textList;
		this.list = new Date[textList.length];
		this.isNullList = new boolean[textList.length];

		for (int i = 0; i < textList.length; i++) {
			String text = textList[i];
			if (text == null || text.length() == 0) {
				this.isNullList[i] = true;
			} else {
				this.list[i] = DateUtility.parseDate(text);
			}
		}
	}

	/**
	 * create a list of required length, but keep them empty
	 * 
	 * @param length
	 */
	protected DateValueList(int length) {
		this.list = new Date[length];
		this.isNullList = new boolean[length];
	}

	@Override
	public DataValueType getValueType() {
		return DataValueType.DATE;
	}

	@Override
	public Date[] getDateList() {
		return this.list;
	}

	@Override
	public Date getDateValue(int index) {
		if (this.isNullList[index]) {
			throw new RuntimeException(ValueList.MSG);
		}
		return this.list[index];
	}

	@Override
	public Date getTimeStampValue(int index) {
		if (this.isNullList[index]) {
			throw new RuntimeException(ValueList.MSG);
		}
		return this.list[index];
	}

	@Override
	public void setDateValue(Date dateValue, int index) {
		this.list[index] = dateValue;
		this.isNullList[index] = false;
		if (this.textList != null) {
			this.textList[index] = DateUtility.formatDate(dateValue);
		}
	}

	@Override
	public void setTimeStampValue(Date dateValue, int index) {
		this.list[index] = dateValue;
		this.isNullList[index] = false;
		if (this.textList != null) {
			this.textList[index] = DateUtility.formatDate(dateValue);
		}
	}

	@Override
	public String[] format() {
		if (this.textList == null) {
			this.textList = new String[this.list.length];
			for (int i = 0; i < this.list.length; i++) {
				this.textList[i] = this.isNullList[i] ? Value.NULL_VALUE
						: DateUtility.formatDate(this.list[i]);
			}
		}
		return this.textList;
	}

	@Override
	public Value[] getValueArray() {
		Value[] values = new Value[this.length()];
		for (int i = 0; i < this.list.length; i++) {
			values[i] = this.isNullList[i] ? new NullValue(DataValueType.DATE)
					: Value.newValue(this.list[i]);
		}

		return values;
	}

	@Override
	public boolean validateTo(ValueList toList) {
		if (toList.getValueType() != this.getValueType()
				|| toList.length() != this.length()) {
			return false;
		}

		Date[] toArray = ((DateValueList) toList).list;
		for (int i = 0; i < this.textList.length; i++) {
			if (this.isNullList[i] || toList.isNullList[i]
					|| this.list[i].after(toArray[i])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Value getValue(int index) {
		if (this.isNullList[index]) {
			return new NullValue(DataValueType.DATE);
		}
		return Value.newValue(this.list[index]);
	}

	@Override
	public void setTextValue(String textValue, int index) {
		throw new RuntimeException("date/timestamp");
	}

	@Override
	protected void setValueByType(Value value, int index) {
		this.list[index] = value.getDateValue();
	}

	@Override
	// appends the supplied list to the existing list and retruns the new length
	public int append(ValueList listToAppend) {
		DataValueType dvt = listToAppend.getValueType();
		if (dvt != DataValueType.DATE || dvt == DataValueType.TIMESTAMP) {
			Spit.out("list of type "
					+ listToAppend.getValueType()
					+ "can not be appended to a list of type Date or Timestamp.");
			return 0;
		}
		int currentLength = this.length();
		int lengthToAdd = listToAppend.length();
		if (lengthToAdd == 0) {
			return currentLength;
		}

		this.textList = null;
		DateValueList lst = (DateValueList) listToAppend;
		int newLength = currentLength + lengthToAdd;
		Date[] newList = new Date[newLength];
		boolean[] newIsNullList = new boolean[newLength];
		for (int i = 0; i < currentLength; i++) {
			newList[i] = this.list[i];
			newIsNullList[i] = this.isNullList[i];
		}

		for (int i = 0; i < lengthToAdd; i++) {
			newList[currentLength] = lst.list[i];
			newIsNullList[currentLength] = lst.isNullList[i];
			currentLength++;
		}
		return newLength;
	}

	@Override
	public DateValueList filter(int newNumberOfRows, boolean[] selections) {
		boolean[] ba = new boolean[newNumberOfRows];
		Date[] newList = new Date[newNumberOfRows];
		int j = 0;
		for (int i = 0; i < this.isNullList.length; i++) {
			if (selections[i]) {
				ba[j] = this.isNullList[i];
				newList[j] = this.list[i];
				j++;
			}
		}
		DateValueList l = new DateValueList();
		l.isNullList = ba;
		l.list = newList;
		if (this.textList != null) {
			super.copyFilteredText(l, newNumberOfRows, selections);
		}

		return l;
	}

	@Override
	public DateValueList clone() {
		this.format();
		boolean[] ba = new boolean[this.length()];
		String[] ta = new String[this.length()];
		Date[] a = new Date[this.length()];
		for (int i = 0; i < this.isNullList.length; i++) {
			ba[i] = this.isNullList[i];
			ta[i] = this.textList[i];
			a[i] = this.list[i];
		}
		DateValueList l = new DateValueList();
		l.isNullList = ba;
		l.textList = ta;
		l.list = a;
		return l;
	}

	@Override
	protected void setValueInternally(Value keyValue) {
		Date value = keyValue.getDateValue();
		for (int i = this.length() - 1; i >= 0; i--) {
			this.list[i] = value;
			this.isNullList[i] = false;
		}
		this.textList = null;
	}
}
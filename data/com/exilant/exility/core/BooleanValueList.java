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

/***
 * represents a boolean value
 * 
 */
public class BooleanValueList extends ValueList {
	boolean[] list;

	protected BooleanValueList() {

	}

	/***
	 * create a list from boolean values
	 * 
	 * @param list
	 */
	protected BooleanValueList(boolean[] list) {
		this.list = list;
		this.isNullList = new boolean[list.length];
	}

	/**
	 * create a list by parsing the text
	 * 
	 * @param textList
	 */
	protected BooleanValueList(String[] textList) {
		this.textList = textList;
		this.list = new boolean[textList.length];
		this.isNullList = new boolean[this.list.length];
		for (int i = 0; i < textList.length; i++) {
			String text = textList[i];
			if (text == null || text.length() == 0) {
				this.isNullList[i] = true;
			} else {
				this.list[i] = BooleanValue.parse(text);
			}
		}
	}

	/***
	 * create a default list with desired number of rows
	 * 
	 * @param length
	 */
	protected BooleanValueList(int length) {
		this.list = new boolean[length];
		this.isNullList = new boolean[length];
	}

	@Override
	public DataValueType getValueType() {
		return DataValueType.BOOLEAN;
	}

	@Override
	public boolean[] getBooleanList() {
		return this.list;
	}

	@Override
	public boolean getBooleanValue(int index) {
		if (this.isNullList[index]) {
			throw new RuntimeException(ValueList.MSG);
		}
		return this.list[index];
	}

	@Override
	public Value getValue(int index) {
		if (this.isNullList[index]) {
			return new NullValue(DataValueType.BOOLEAN);
		}
		return Value.newValue(this.list[index]);
	}

	@Override
	protected void setValueByType(Value value, int index) {
		this.list[index] = value.getBooleanValue();
	}

	@Override
	public void setTextValue(String textValue, int index) {
		super.raiseException("boolean");
	}

	@Override
	public void setBooleanValue(boolean booleanValue, int index) {
		this.list[index] = booleanValue;
		this.isNullList[index] = false;
		if (this.textList != null) {
			this.textList[index] = BooleanValue.toString(booleanValue);
		}
	}

	@Override
	public String[] format() {
		if (this.textList == null) {
			this.textList = new String[this.list.length];
			for (int i = 0; i < this.list.length; i++) {
				this.textList[i] = this.isNullList[i] ? Value.NULL_VALUE
						: BooleanValue.toString(this.list[i]);
			}
		}
		return this.textList;
	}

	@Override
	public Value[] getValueArray() {
		Value[] values = new Value[this.length()];
		for (int i = 0; i < this.list.length; i++) {
			values[i] = this.isNullList[i] ? new NullValue(
					DataValueType.BOOLEAN) : Value.newValue(this.list[i]);
		}
		return values;
	}

	@Override
	public boolean validateTo(ValueList toList) {
		return false; // there is no question of from-to-validation
	}

	@Override
	// appends the supplied list to the existing list and returns the new length
	public int append(ValueList listToAppend) {
		if (listToAppend.getValueType() != DataValueType.BOOLEAN) {
			Spit.out("list of type " + listToAppend.getValueType()
					+ "can not be appended to a list of type Boolean.");
			return 0;
		}
		int currentLength = this.length();
		int lengthToAdd = listToAppend.length();
		if (lengthToAdd == 0) {
			return currentLength;
		}

		this.textList = null;
		BooleanValueList lst = (BooleanValueList) listToAppend;
		int newLength = currentLength + lengthToAdd;
		boolean[] newList = new boolean[newLength];
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
	public BooleanValueList filter(int newNumberOfRows, boolean[] selections) {
		boolean[] ba = new boolean[newNumberOfRows];
		boolean[] newList = new boolean[newNumberOfRows];
		int j = 0;
		for (int i = 0; i < this.isNullList.length; i++) {
			if (selections[i]) {
				ba[j] = this.isNullList[i];
				newList[j] = this.list[i];
				j++;
			}
		}
		BooleanValueList l = new BooleanValueList();
		l.isNullList = ba;
		l.list = newList;
		if (this.textList != null) {
			super.copyFilteredText(l, newNumberOfRows, selections);
		}
		return l;
	}

	@Override
	public BooleanValueList clone() {
		boolean[] ba = new boolean[this.length()];
		String[] ta = new String[this.length()];
		boolean[] a = new boolean[this.length()];
		this.format();
		for (int i = 0; i < this.isNullList.length; i++) {
			ba[i] = this.isNullList[i];
			ta[i] = this.textList[i];
			a[i] = this.list[i];
		}
		BooleanValueList l = new BooleanValueList();
		l.isNullList = ba;
		l.textList = ta;
		l.list = a;
		return l;
	}

	@Override
	protected void setValueInternally(Value keyValue) {
		boolean value = keyValue.getBooleanValue();
		for (int i = this.length() - 1; i >= 0; i--) {
			this.list[i] = value;
			this.isNullList[i] = false;
		}
		this.textList = null;
	}

}
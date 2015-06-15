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
 * represents integral value list. Underlying value is stored as long
 * 
 */
public class IntegralValueList extends ValueList {
	private long[] list;

	protected IntegralValueList() {
	}

	/***
	 * construct with the supplied values as underlying array
	 * 
	 * @param list
	 */
	protected IntegralValueList(long[] list) {
		this.list = list;
		this.isNullList = new boolean[list.length];
	}

	/***
	 * construct with parsed integral values from the textList
	 * 
	 * @param textList
	 */
	protected IntegralValueList(String[] textList) {
		this.textList = textList;
		this.list = new long[textList.length];
		this.isNullList = new boolean[textList.length];
		for (int i = 0; i < textList.length; i++) {
			String text = textList[i];
			if (text == null || text.length() == 0) {
				this.isNullList[i] = true;
			} else {
				try {
					this.list[i] = Long.parseLong(text);
				} catch (Exception e) {
					if (text.indexOf('.') >= 0 || text.indexOf('E') > 0) {
						try {
							long lval = (long) Double.parseDouble(text);
							this.list[i] = lval;
							// and don't forget to change the text list
							this.textList[i] = "" + lval;
						} catch (Exception e1) {
							Spit.out(text + " is not an integer.");
						}
					}
				}
			}
		}
	}

	/***
	 * construct by converting decimal values into integral values
	 * 
	 * @param decimalList
	 */
	protected IntegralValueList(double[] decimalList) {
		this.list = new long[decimalList.length];
		this.isNullList = new boolean[decimalList.length];
		for (int i = 0; i < decimalList.length; i++) {
			this.list[i] = (long) decimalList[i];
		}
	}

	/***
	 * create an empty list to hold desired number of values
	 * 
	 * @param length
	 */
	protected IntegralValueList(int length) {
		this.list = new long[length];
		this.isNullList = new boolean[length];
	}

	@Override
	public DataValueType getValueType() {
		return DataValueType.INTEGRAL;
	}

	@Override
	public void setTextValue(String textValue, int index) {
		this.raiseException(ValueList.INTEGRAL);
	}

	@Override
	public long[] getIntegralList() {
		return this.list;
	}

	@Override
	public double[] getDecimalList() {
		double[] decimalList = new double[this.list.length];
		for (int i = 0; i < this.list.length; i++) {
			decimalList[i] = this.list[i];
		}
		return decimalList;
	}

	@Override
	public void setIntegralValue(long integralValue, int index) {
		this.list[index] = integralValue;
		this.isNullList[index] = false;
		if (this.textList != null) {
			this.textList[index] = Long.toString(integralValue);
		}
	}

	@Override
	public long getIntegralValue(int index) {
		if (this.isNullList[index]) {
			throw new RuntimeException(ValueList.MSG);
		}
		return this.list[index];
	}

	@Override
	public void setDecimalValue(double decimalValue, int index) {
		long longValue = (long) decimalValue;
		this.list[index] = longValue;
		this.isNullList[index] = false;
		if (this.textList != null) {
			this.textList[index] = Long.toString(longValue);
		}
	}

	@Override
	public double getDecimalValue(int index) {
		if (this.isNullList[index]) {
			throw new RuntimeException(ValueList.MSG);
		}
		return this.list[index];
	}

	@Override
	public String[] format() {
		if (this.textList == null) {
			this.textList = new String[this.list.length];
			for (int i = 0; i < this.list.length; i++) {
				this.textList[i] = this.isNullList[i] ? Value.NULL_VALUE : Long
						.toString(this.list[i]);
			}
		}
		return this.textList;
	}

	@Override
	public Value[] getValueArray() {
		Value[] values = new Value[this.length()];
		for (int i = 0; i < this.list.length; i++) {
			values[i] = (this.isNullList[i]) ? new NullValue(
					DataValueType.INTEGRAL) : Value.newValue(this.list[i]);
		}
		return values;
	}

	@Override
	public boolean validateTo(ValueList toList) {
		if (toList.getValueType() != DataValueType.INTEGRAL
				|| toList.length() != this.length()) {
			return false;
		}

		long[] toArray = ((IntegralValueList) toList).list;
		for (int i = 0; i < this.textList.length; i++) {
			if (this.isNullList[i] || toList.isNullList[i]
					|| (this.list[i] > toArray[i])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Value getValue(int index) {
		if (this.isNullList[index]) {
			return new NullValue(DataValueType.INTEGRAL);
		}
		return new IntegralValue(this.list[index]);
	}

	@Override
	protected void setValueByType(Value value, int index) {
		this.list[index] = value.getIntegralValue();
	}

	@Override
	// appends the supplied list to the existing list and retruns the new length
	public int append(ValueList listToAppend) {
		if (listToAppend.getValueType() != DataValueType.INTEGRAL) {
			Spit.out("list of type " + listToAppend.getValueType()
					+ "can not be appended to a list of type INtegral.");
			return 0;
		}
		int currentLength = this.length();
		int lengthToAdd = listToAppend.length();
		if (lengthToAdd == 0) {
			return currentLength;
		}

		this.textList = null;
		IntegralValueList lst = (IntegralValueList) listToAppend;
		int newLength = currentLength + lengthToAdd;
		long[] newList = new long[newLength];
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
	public IntegralValueList filter(int newNumberOfRows, boolean[] selections) {
		boolean[] ba = new boolean[newNumberOfRows];
		long[] newList = new long[newNumberOfRows];
		int j = 0;
		for (int i = 0; i < this.isNullList.length; i++) {
			if (selections[i]) {
				ba[j] = this.isNullList[i];
				newList[j] = this.list[i];
				j++;
			}
		}
		IntegralValueList l = new IntegralValueList();
		l.isNullList = ba;
		l.list = newList;

		if (this.textList != null) {
			super.copyFilteredText(l, newNumberOfRows, selections);
		}
		return l;
	}

	@Override
	public IntegralValueList clone() {
		boolean[] ba = new boolean[this.length()];
		String[] ta = new String[this.length()];
		long[] a = new long[this.length()];
		this.format();
		for (int i = 0; i < this.isNullList.length; i++) {
			ba[i] = this.isNullList[i];
			ta[i] = this.textList[i];
			a[i] = this.list[i];
		}
		IntegralValueList l = new IntegralValueList();
		l.isNullList = ba;
		l.textList = ta;
		l.list = a;
		return l;
	}

	@Override
	protected void setValueInternally(Value keyValue) {
		long value = keyValue.getIntegralValue();
		for (int i = this.length() - 1; i >= 0; i--) {
			this.list[i] = value;
			this.isNullList[i] = false;
		}
		this.textList = null;
	}
}
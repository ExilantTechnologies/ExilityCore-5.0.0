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
 * represents a field that is used as parameter in various other design
 * components
 */
class Parameter implements ToBeInitializedInterface {
	/***
	 * identifier
	 */
	String name = null;

	/***
	 * defaults to name. Should be used ONLY in case of some unusual
	 * circumstance where it does not make sense to add this field to data
	 * dictionary. like from-to field
	 */
	String dataElementName = null;

	/***
	 * label in English, in case this is to be rendered
	 */
	String label = null;

	/***
	 * Is a non-null (non-empty) value required in this field? Watch out. we use
	 * both isOptional and isRequired, to ensure that a default value of false
	 * is convenient.
	 */
	boolean isOptional = false;

	/***
	 * value to be used if it is not supplied, even if it is optional
	 */
	String defaultValue = null;

	/***
	 * for documentation
	 */
	String description = null;

	/***
	 * cache its data type to avoid going to dictionary repeatedly
	 */
	protected AbstractDataType dataTypeObject = null;

	/***
	 * cached for optimization
	 */
	protected DataValueType dataValueType = DataValueType.TEXT;

	Parameter() {
	}

	/***
	 * return underlying data type
	 * 
	 * @return data type
	 */
	public final AbstractDataType getDataType() {
		return this.dataTypeObject;
	}

	/***
	 * return underlying value type
	 * 
	 * @return data value type
	 */
	public final DataValueType getValueType() {
		return this.dataValueType;
	}

	@Override
	public void initialize() {
		if (this.dataTypeObject != null) {
			return;
		}

		String nam = (this.dataElementName != null) ? this.dataElementName
				: this.name;
		AbstractDataType dt = DataTypes.getDataType(nam, null);
		this.dataValueType = dt.getValueType();
		this.dataTypeObject = dt;
	}

	/***
	 * return default value as per required data type. Note that users would
	 * have specified the default value as string
	 * 
	 * @return default value
	 */
	public Value getDefaultValue() {
		if (this.defaultValue == null || this.defaultValue.length() == 0) {
			return null;
		}
		return this.dataTypeObject.parseValue(this.name, this.defaultValue,
				null, null);
	}

	/**
	 * transitional till we use data dictionary as well as record. Once we move
	 * completely to record, we will not use data element
	 * 
	 * @param dataType
	 */
	public void setDataType(AbstractDataType dataType) {
		this.dataTypeObject = dataType;
	}
}
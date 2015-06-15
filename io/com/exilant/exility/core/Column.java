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

/**
 * represents a column in an rdbms table
 * 
 */
class Column extends Parameter {

	/**
	 * Fields that are loaded/unloaded
	 */
	private static final String[] ALL_ATTRIBUTES = { "name", "columnName",
			"dataElementName", "label", "description", "isOptional",
			"defaultValue", "defaultValue", "isKeyColumn", "listServiceName",
			"dependantOnColumnName" };
	/**
	 * By default, the value for a column is found with the same name in DC.
	 * This can be overridden with a different name You use this facility in a
	 * bulk task when one of the column value is to be taken from the parent
	 * table
	 */
	String dataSource = null;

	/**
	 * is the column name different from the name that is used to get the value
	 * from dc?. Generally left as blank in .xml file, and set to name in
	 * initialize()
	 */
	String columnName = null;

	/**
	 * is this the key column, or a key column for this table?
	 */
	boolean isKeyColumn = false;

	/**
	 * not used any more
	 */
	boolean isClobColumn = false;
	/**
	 * meant for test data generation. Use this service to get possible values
	 * to this column
	 */
	String listServiceName = null;

	/**
	 * is this column mandatory, if another column is specified?
	 */
	String basedOnColumnName = null;

	/**
	 * is this column mandatory ONLY if another columns has a specific value?
	 * specify the column name in basedOnColumnName and the value of that
	 * column.
	 */

	String basedOnColumnValue = null;
	/**
	 * if there are two columns, of which one of them should have value, specify
	 * the other column
	 */
	String otherColumnName = null;
	/**
	 * can this column take null value, as per definition in rdbms table?
	 */
	boolean isNullable = false;

	/**
	 * is this a foreign key? name of the table to which this key points to
	 */
	// String keyFromTable = null;

	Column() {
	}

	/**
	 * validate this column value
	 * 
	 * @param value
	 *            to be validated
	 * @param dc
	 * @throws ExilityException
	 */
	void validate(Value value, DataCollection dc) throws ExilityException {
		if (null == value || value.isNull()
				|| value.getTextValue().length() == 0) {
			// value not specified. if isRequired, it is invalid
			if ((this.defaultValue == null) && this.isOptional == false) {
				dc.raiseException(ExilityMessageIds.VALUE_IS_REQUIRED,
						this.name);
			}
			/**
			 * is this column mandatory if another column is specified?
			 */
			if (this.basedOnColumnName != null) {
				Value otherValue = dc.getValue(this.basedOnColumnName);
				if (otherValue != null && otherValue.isNull() == false) {
					if (this.basedOnColumnValue == null) {
						dc.raiseException(ExilityMessageIds.VALUE_IS_REQUIRED,
								this.name);
					} else {
						if (otherValue.getTextValue().equals(
								this.basedOnColumnValue)) {
							dc.raiseException(
									ExilityMessageIds.VALUE_IS_REQUIRED,
									this.name);
						}
					}
				}
			}
			/**
			 * is this required if another column is not specified?
			 */
			if (this.otherColumnName != null) {
				Value otherValue = dc.getValue(this.otherColumnName);
				if (otherValue == null || otherValue.isNull()
						|| otherValue.getTextValue().length() == 0) {
					dc.raiseException(ExilityMessageIds.VALUE_IS_REQUIRED,
							this.name);
				}
			}
			return;
		}
	}

	/**
	 * 
	 * @return all loadable attributes
	 */
	public String[] getLoadableAttributes() {
		return ALL_ATTRIBUTES;
	}
}
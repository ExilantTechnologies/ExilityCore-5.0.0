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
 * Exility design component that specifies data type and other validations for a
 * field
 */
class FieldSpec extends Parameter {
	/***
	 * special name of column with which a called sql indicates whether the key
	 * was found or not
	 */
	static final String VALIDATION_COLUMN_NAME = "KeyFound";

	/***
	 * filter field is a special client facility meant for specifying
	 * filtering/search criterion. In this case some companion fields are
	 * expected
	 */
	boolean isFilterField = false;

	/***
	 * is this field mandatory but only when value for another field is
	 * supplied?
	 */
	String basedOnField = null;

	/***
	 * relevant only if basedOnField is set to true. Further restriction that
	 * this field is mandatory only if the other field has this specific value
	 */
	String basedOnFieldValue = null;

	/***
	 * is this a to-field for another field? Specify the name of the from field.
	 * Note that you should not specify this on both from and to fields.
	 */
	String fromField = null;

	/***
	 * is this part of a from-to field, and you want to specify that thru this
	 * field?
	 */
	String toField = null;

	/***
	 * Is this a field that has to exist as a key in some table (e.g. employee
	 * id?) In that case you can specify a sql that will check whether a row
	 * exists with this value as the key. Note that this field value will be
	 * supplied in a field named "keyValue"
	 */
	String descServiceId = null;

	/***
	 * relevant only if descServiceId is specified. Comma separated set of
	 * fields that the service is expected to return, and we have to put these
	 * values into dc
	 */
	String descFields = null;

	/***
	 * relevant only if descServiceId is specifies. Indicates that the supplied
	 * value should not already exist in the table. Like adding a new user id.
	 */
	boolean isUniqueField = false;

	/***
	 * is there a list of valid values for this field. similar to drop-down on a
	 * client.
	 */
	String[] validValues = null;

	FieldSpec() {

	}

	/***
	 * copy this field from in-data to dc.
	 * 
	 * @param inData
	 * @param dc
	 * @return true if we are successful in copying
	 */
	boolean translateInput(ServiceData inData, DataCollection dc) {
		if (inData.hasValue(this.name) == false) {
			return false;
		}

		String val = inData.getValue(this.name);
		AbstractDataType dt = this.getDataType();
		Value value = dt.parseValue(this.name, val, this.validValues, dc);

		/*
		 * If it is a filter field, we expect two associated fields for search
		 * fields. They need not be validated..
		 */
		if (this.isFilterField) {
			String fieldName = this.name + "Operator";
			dc.addTextValue(fieldName, inData.getValue(fieldName));
			fieldName = this.name + "To";
			dc.addTextValue(fieldName, inData.getValue(fieldName));
		}

		if (value == null) {
			return false;
		}

		dc.addValue(this.name, value);
		return true;
	}

	/***
	 * validate a field as per specification
	 * 
	 * @param dc
	 * @param handle
	 * @return true if validation succeeded, false otherwise
	 */
	boolean validateField(DataCollection dc, DbHandle handle) {
		Value value = dc.getValue(this.name);
		if ((value == null) || (value.isSpecified() == false)) {
			if (this.basedOnField == null) {
				if (!this.isOptional) {
					dc.addMessage("exilFieldIsRequired", this.name);
					return false;
				}
			} else {
				Value v = dc.getValue(this.basedOnField);

				if ((v != null) && v.isSpecified()) {
					if (this.basedOnFieldValue == null
							|| this.basedOnFieldValue.equals(v.toString())) {
						dc.addMessage("exilFieldIsRequired", this.name);
						return false;
					}
				}
			}
			return true;
		}

		boolean valueToReturn = true;

		if (this.fromField != null) {
			Value v = dc.getValue(this.fromField);
			if ((v != null) && v.greaterThan(value).getBooleanValue()) {
				dc.addMessage("exilInvalidFromTo", this.fromField, this.name,
						v.toString(), value.toString());
				valueToReturn = false;
			}
		}

		if (this.toField != null) {
			Value v = dc.getValue(this.toField);
			if ((v != null) && v.lessThan(value).getBooleanValue()) {
				dc.addMessage("exilInvalidFromTo", this.name, this.toField,
						value.toString(), v.toString());
				valueToReturn = false;
			}
		}
		if (this.descServiceId != null) {
			SqlInterface sql = Sqls.getSqlTemplateOrNull(this.descServiceId);
			if (sql == null) {
				dc.addMessage("exilNoSuchSql", this.descServiceId);
				return false;
			}
			/**
			 * some desc services were written with keyValue as the field name
			 */
			dc.addValue("keyValue", value);
			int n;
			try {
				n = sql.execute(dc, handle, null, null);
			} catch (ExilityException e) {
				dc.addError(e.getMessage());
				return false;
			}

			if (n == 0) {
				if (this.isUniqueField == false) {
					dc.addMessage("exilKeyNotFound", this.name,
							value.toString());
					valueToReturn = false;
				}
			} else if (this.isUniqueField) {
				dc.addMessage("exilKeyAlreadyExists", this.name,
						value.toString());
				valueToReturn = false;
			}
		}
		return valueToReturn;
	}

	/***
	 * Validate a column of data. Note that listSpec and gridSpec use fieldSpec
	 * to specify what kind of value is expected in the list/column
	 * 
	 * @param gridName
	 * @param dc
	 * @param handle
	 * @return true if validation is successful, false otherwise
	 */

	boolean validateColumn(String gridName, DataCollection dc, DbHandle handle) {
		Grid grid = dc.getGrid(gridName);
		if (grid == null) {
			return false;
		}

		ValueList column = grid.getColumn(this.name);
		if (column == null) {
			return false;
		}
		Value[] values = column.getValueArray();
		boolean valueToReturn = true;

		/*
		 * related columns that may have to be used for validation
		 */
		Value[] fromValues = null;
		Value[] toValues = null;
		Value[] basedOnValues = null;

		/*
		 * get related columns
		 */
		if (this.basedOnField != null) {
			if (grid.hasColumn(this.basedOnField) == false) {
				dc.addMessage("exilNoColumn", gridName, this.basedOnField);
				return false;
			}
			basedOnValues = grid.getColumn(this.basedOnField).getValueArray();
		}

		/*
		 * validate from field
		 */
		if (this.fromField != null) {
			if (grid.hasColumn(this.fromField) == false) {
				dc.addMessage("exilNoColumn", gridName, this.fromField);
				return false;
			}
			fromValues = grid.getColumn(this.fromField).getValueArray();
		}

		/*
		 * validate to field
		 */
		if (this.toField != null) {
			if (grid.hasColumn(this.toField) == false) {
				dc.addMessage("exilNoColumn", gridName, this.toField);
				return false;
			}
			toValues = grid.getColumn(this.toField).getValueArray();
		}

		/*
		 * we are ready to validate each row now
		 */
		int nbrRows = values.length;
		for (int i = 0; i < values.length; i++) {
			Value value = values[i];
			if ((value.isSpecified() == false)) {
				if (this.basedOnField == null) {
					if (!this.isOptional) {
						dc.addMessage("exilFieldIsRequired", this.name);
						valueToReturn = false;
					}
				}
				/*
				 * basedOnValues can not be null, but I do not want to put
				 */
				else if (basedOnValues != null
						&& basedOnValues[i].isSpecified()) {
					if (this.basedOnFieldValue == null
							|| this.basedOnFieldValue.equals(basedOnValues[i]
									.toString())) {
						dc.addMessage("exilFieldIsRequired", this.name);
						valueToReturn = false;
					}

				}
				continue;
			}

			if (this.fromField != null && fromValues != null) {
				Value v = fromValues[i];
				if (v.isSpecified() && v.greaterThan(value).getBooleanValue()) {
					dc.addMessage("exilInvalidFromTo", this.fromField,
							this.name, v.toString(), value.toString());
					valueToReturn = false;
				}
			}

			if (this.toField != null && toValues != null) {
				Value v = toValues[i];
				if (v.isSpecified() && v.lessThan(value).getBooleanValue()) {
					dc.addMessage("exilInvalidFromTo", this.name, this.toField,
							value.toString(), v.toString());
					valueToReturn = false;
				}
			}
		}// for each value

		if (this.descServiceId != null) {
			SqlInterface sql = Sqls.getSqlTemplateOrNull(this.descServiceId);
			if (sql == null) {
				dc.addError(this.descServiceId + " is not a valid sql.");
				return false;
			}
			int n = 0;
			try {
				n = sql.executeBulkTask(dc, handle, gridName, true);
			} catch (ExilityException e) {
				dc.addError(e.getMessage());
				return false;
			}

			if ((n < nbrRows && !this.isUniqueField)
					|| (n > 0 && this.isUniqueField)) {
				/*
				 * go thru the grid and find out if any one row has a problem
				 */
				ValueList result = grid.getColumn(gridName
						+ FieldSpec.VALIDATION_COLUMN_NAME);
				for (int i = 0; i < result.length(); i++) {
					if (result.getBooleanValue(i)) {
						if (this.isUniqueField) {
							dc.addMessage("exilKeyAlreadyExists", this.name,
									values[i].toString());
							valueToReturn = false;
						}
					} else if (!this.isUniqueField) {
						dc.addMessage("exilInvalidFromTo", this.name,
								values[i].toString());
						valueToReturn = false;
					}
				}
			}
		}
		return valueToReturn;
	}

	/***
	 * copy value from dc to output data
	 * 
	 * @param dc
	 * @param outData
	 */
	void translateOutput(DataCollection dc, ServiceData outData) {
		/*
		 * no need to validate, but we may need some transformation..
		 */

		AbstractDataType dt = this.getDataType();
		Value value = dc.getValue(this.name);
		if (value == null) {
			outData.addValue(this.name, Value.NULL_VALUE);
		} else {
			outData.addValue(this.name, dt.format(value));
		}
	}

	/***
	 * copy field value from one dc to the other
	 * 
	 * @param fromDc
	 * @param toDc
	 */
	public void translate(DataCollection fromDc, DataCollection toDc) {
		toDc.addValue(this.name, fromDc.getValue(this.name));
	}

}
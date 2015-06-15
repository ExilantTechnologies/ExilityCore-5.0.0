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
 * types of sql parameters
 * 
 */
enum SqlParameterType {
	NORMAL, LIST, FILTER, COMBINED
}

/**
 * place-holder in a sql template, that will be substituted with appropriate
 * string at run time for a valid sql to be formed.
 * 
 */
class SqlParameter extends Parameter {

	/**
	 * type of parameter
	 */
	SqlParameterType parameterType = SqlParameterType.NORMAL;

	/**
	 * if you do not use data dictionary, this is the bets place to provide the
	 * data type
	 */

	String dataType = null;

	/***
	 * some fields are to be used always in upper case.. i.e. the value supplied
	 * is to be UPPER CASED before using
	 **/
	boolean toUpperCase = false;

	/***
	 * By default, value of the field is formatted as per sql syntax. e.g. date
	 * will be formatted as per the underlying driver etc.. At times, users do
	 * not want such formatting. NOTE: We still replace single quotes with two
	 * single quotes, and we continue to format non-text data types
	 */
	boolean doNotFormat = false;

	/**
	 * we got into an issue with doNotFormat, because it was, for the right
	 * reason, replacing single quotes in a string with two single quotes. It
	 * was also formatting non-text data types. Some one want to take all the
	 * risk of sql injection, and didn't want us to do ANYTHING.
	 */
	boolean justLeaveMeAlone = false;

	/***
	 * what if this field is a column in a grid. NOte that this is different
	 * from the whole sql being formatted for each row of a grid Also, if
	 * parameter type is list, name of grid to take the list from
	 */
	String gridName = null;

	/***
	 * if you use grid name, normally we will have only one row, but if your
	 * design requires row index..
	 */
	int index = 0;

	/***
	 * for making the list, we can put a filter on a column using this column
	 * name and the value in the next parameter
	 */
	String basedOnColumnName = null;

	/***
	 * filtering value to be used
	 */
	String basedOnColumnValue = null;

	/**
	 * default constructor
	 */
	SqlParameter() {
	}

	/**
	 * get the string to be substituted at run time for this parameter
	 * 
	 * @param dc
	 * @param fieldName
	 *            optional. If specified, this will over-ride the name of this
	 *            parameter
	 * @param grid
	 *            optional. If specified, over-rides object attributes, and the
	 *            value will be picked up from this grid
	 * @param idx
	 *            0-based row number of the table from which to pick up the
	 *            value
	 * @return
	 */
	String getValue(DataCollection dc, String fieldName, Grid grid, int idx) {

		/**
		 * a non-null fieldName overrides the param.name a null field value
		 * means, get it from the dc.
		 */

		String fieldNameToUse = (fieldName == null) ? this.name : fieldName;
		String val = null;

		switch (this.parameterType) {

		case FILTER:
			val = SqlUtil.getFilterCondition(dc, fieldNameToUse,
					this.getValueType());
			break;

		case LIST:
			val = this.getList(dc, fieldNameToUse, grid);
			break;

		case COMBINED:
			val = this.getCombined(dc, fieldNameToUse);
			break;
		default:
			val = this.getNoramlValue(dc, fieldNameToUse, grid, idx);
			break;
		}

		if ((val == null || val.length() == 0)) {
			if (this.defaultValue == null || this.defaultValue.length() == 0) {
				return null;
			}
			return this.format(this.defaultValue);
		}

		if (this.toUpperCase) {
			val = val.toUpperCase();
		}

		return val;
	}

	private String getNoramlValue(DataCollection dc, String fieldNameToUse,
			Grid grid, int idx) {
		// we want to stick to the discipline of not modifying parameters
		Grid gridToUse = grid;
		String val = null;
		if (grid == null && this.gridName != null) {
			gridToUse = dc.getGrid(this.gridName);
		}
		if (gridToUse != null) {
			ValueList list = gridToUse.getColumn(fieldNameToUse);
			if (list != null && list.length() >= idx) {
				val = list.getTextValue(idx);
			}
			// else val will remain as null;
		} else {
			Value value = dc.getValue(fieldNameToUse);
			if (value != null && value.isNull() == false) {
				val = value.getTextValue();
			}
		}

		return this.format(val);
	}

	private String format(String val) {
		if (val == null || this.justLeaveMeAlone) {
			return val;
		}

		DataValueType vt = this.getValueType();
		if (this.doNotFormat && vt == DataValueType.TEXT) {
			return val.replaceAll("'", "''");
		}

		/**
		 * we had a bug in the earlier version because of which formatValue was
		 * not called if val.length was zero. Effectively, this bug ensured that
		 * nullForEmptyString was not effective. when we fixed this problem,
		 * existing path finder services got into trouble. Hence we have
		 * introduced another parameter to recreate the bug!!
		 */
		if (val.length() > 0 || AP.enforceNullPolicyForText) {
			return SqlUtil.formatValue(val, this.getValueType());
		}
		return val;
	}

	/**
	 * get value list from dc
	 * 
	 * @param dc
	 * @param fieldName
	 * @return
	 */
	private String getList(DataCollection dc, String fieldNameToUse, Grid grid) {
		String[] vals = null;
		if (grid != null) {
			vals = grid.getColumnAsTextArray(fieldNameToUse);
		} else {
			vals = dc.getTextValueList(this.gridName, fieldNameToUse);
		}
		if (vals == null) {
			return null;
		}
		return SqlUtil.formatList(vals, this.getValueType());
	}

	/**
	 * combined field is the one that may contain a comparator at the beginning.
	 * like =12 or *abc This is NOT used by any one. we had developed this to
	 * emulate a specific project
	 * 
	 * @param dc
	 * @param fieldName
	 * @return string to be inserted into sql
	 */
	private String getCombined(DataCollection dc, String fieldName) {
		String val = dc.getTextValue(fieldName, null);
		if (val == null) {
			return "";
		}

		int len = val.length();
		if (len == 0) {
			return val;
		}

		char firstChar = val.charAt(0);
		String newVal = val.substring(1).trim();
		if ((firstChar == '=') || (firstChar == '>') || (firstChar == '<')) {
			return firstChar + SqlUtil.formatValue(newVal, this.getValueType())
					+ ' ';
		}

		char lastChar = val.charAt(len - 1);
		// is it start with??
		if (lastChar != '%') {
			val += '%';
		}

		return " LIKE " + SqlUtil.formatValue(val, this.getValueType()) + ' ';
	}

	/***
	 * Gets a dummy value for doing a dry run. If default is already there, it
	 * uses it, else uses a default based on data type
	 * 
	 */
	void putTestValues(DataCollection dc) {
		if (this.isOptional) {
			return;
		}

		switch (this.parameterType) {
		case NORMAL:
			dc.addValue(this.name, Value.getTestValue(this.dataValueType));
			return;

		case FILTER: // let us simulate an equal condition
			dc.addValue(this.name, Value.getTestValue(this.dataValueType));
			dc.addValue(this.name + "Operator",
					Value.newValue(SqlUtil.OPERATOR_EQUAL));
			return;

		case LIST:
			dc.addValueList(this.name,
					ValueList.getTestList(this.dataValueType));
			return;

		case COMBINED:
			dc.addValue(this.name, Value.newValue("=a"));
			return;
		default:
			return;
		}
	}

	/**
	 * after introducing inputRecordName, we do not want Parameter to look for
	 * data element name. To take care of this, we suppress
	 * Parameter.initialize(). It will be triggered from Sql.initialize()
	 */
	@Override
	public void initialize() {
		/*
		 * just avoid Parameter.initialize() getting triggered on load of sql.
		 */
	}

	public void forceInitialize() {
		if (this.dataType != null) {
			this.dataTypeObject = DataTypes.getDataTypeOrNull(this.dataType);
			if (this.dataTypeObject == null) {
				throw new RuntimeException(this.dataType
						+ " is not a valid data type for sql paramater "
						+ this.name);
			}
			this.dataValueType = this.dataTypeObject.getValueType();
		}
		super.initialize();
	}

}
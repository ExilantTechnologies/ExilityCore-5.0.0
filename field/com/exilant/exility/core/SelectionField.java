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
 * how the list of values that a multi-select field is handled
 * 
 */
enum SelectionValueType {
	/**
	 * value is a comma separated list of selected values.
	 */
	text
	/**
	 * values are in a list. list is not used any more.
	 */
	, list
	/**
	 * values are put /received in a grid. name of grid is the field name.
	 */
	, grid
}

/***
 * classical drop-down field in html. We recommend assistedInputField as a
 * better alternative
 * 
 * 
 */
class SelectionField extends AbstractInputField {
	/**
	 * service that fetches the list of values for the drop-down. A sql, or a
	 * service is to be made available on the server that creates a grid with
	 * first column as internal value and second column as display value. If
	 * there is only one column, it is assumed to be both internal and display
	 * value,while third column is assumed to be to disable a given options.
	 */
	String listServiceId = null;
	/**
	 * while calling list service, value of this field is always sent with a
	 * field name as 'keyValue'. specify if any other fields to be sent for the
	 * service.
	 */
	String[] listServiceQueryFieldNames = null;

	/**
	 * in case the additional fields to be sent have their names different from
	 * the field names used in this page, provide the field names in this page
	 * in this attribute. query field names are the names with which values are
	 * sent to the server, while these are are the field names form which values
	 * are extracted from the page.
	 */
	String[] listServiceQueryFieldSources = null;

	/***
	 * if listServiceId specified above is a common service that requires a key.
	 * for example states for a country requires country code. In this case
	 * countryCode would be keyValue. It is to be noted that the list of values
	 * is assumed to be unique for a given key value.
	 ***/
	String keyValue = null;

	/*** text to be displayed when user has not made any selection ***/
	String blankOption = null;

	/***
	 * by default, options are loaded as soon the page is loaded. Typically, for
	 * options that depend on value of another field, this is not appropriate
	 ***/
	boolean noAutoLoad = false;
	/***
	 * if list of options are known at design time, use 1,One;2,Two;... If you
	 * specify this,listService is not relevant.
	 ***/
	String valueList = null;
	/**
	 * if this is field is 'required' should we select the first option by
	 * default?
	 */
	boolean selectFirstOption = false;

	/**
	 * so we allow multiple selections?
	 */
	boolean multipleSelection = false;

	/**
	 * size as in html attribute
	 */
	int size = 0;

	/**
	 * Description service is also used to get additional fields associated with
	 * a field. Hence it can be used with a selection field as well. refer to
	 * the attribute as in textInputField
	 */
	String descServiceId = null;
	/**
	 * refer to the attribute as in textInputField
	 */
	String[] descFields = null;
	/**
	 * refer to the attribute as in textInputField
	 */
	String[] descQueryFields = null;
	/**
	 * refer to the attribute as in textInputField
	 */
	String[] descQueryFieldSources = null;
	/**
	 * refer to the attribute as in textInputField
	 */
	boolean doNotMatchDescNames = false;
	/**
	 * refer to the attribute as in textInputField
	 */
	String validateQueryFields = null;
	/**
	 * refer to the attribute as in textInputField
	 */
	boolean supressDescOnLoad = false;
	/***
	 * If this is used in a grid, does the list of options vary by row? For
	 * example, if we have a state as well as country in a row, then the list of
	 * states would very by row. But if we are talking about product-type that
	 * may remains same across all rows.
	 */
	boolean sameListForAllRows = false;
	/**
	 * refer to the attribute as in textInputField
	 */
	String otherField = null;

	/**
	 * if this is multiple-select, how should the values be supplied/received?
	 * text - comma separated text, list - array of selected values, grid : two
	 * columns: first column all values, second column boolean
	 */
	SelectionValueType selectionValueType = SelectionValueType.text;

	/**
	 * refer to the attribute as in textInputField
	 */
	boolean isUniqueField = false;
	/**
	 * refer to the attribute as in textInputField
	 */
	String fieldToFocusAfterExecution = null;
	/***
	 * exis-time project wanted to show some frequently used options, and then
	 * show a 'show more' option, on-click of which, they wanted show a pop-up
	 * etc..
	 */
	String showMoreFunctionName = null;

	@Override
	void fieldToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("\n<select ");

		super.addMyAttributes(sbf, pageContext);

		if (this.multipleSelection) {
			sbf.append(" multiple=\"multiple\" ");
		}

		if (this.showMoreFunctionName != null) {
			sbf.append(" onclick=\"P2.moreOptionClicked('").append(this.name)
					.append("', event);\" ");
		}

		sbf.append(">");

		/*
		 * see if we have to add <options> design time values are supplied in
		 * vauleList as a string of ";" separated pairs of "," separated values
		 */
		if (!this.isRequired
				|| (this.valueList != null && this.valueList.length() > 0 && !this.isRequired)) {
			sbf.append("<option value=\"\">");
			if (this.blankOption != null && this.blankOption.length() != 0) {
				sbf.append(this.blankOption);
			} else {
				sbf.append("&nbsp;");
			}
			sbf.append("</option>");
		}

		if (this.valueList != null && this.valueList.length() != 0) {
			String[] list = this.valueList.split(";");
			int n = list.length;
			if (n == 1 && this.isRequired == true) {
				this.defaultValue = list[0].split(",")[0];
			}
			if (this.defaultValue == null && this.selectFirstOption) {
				this.defaultValue = list[0].split(",")[0];
			}
			for (int i = 0; i < n; i++) {
				String[] entry = list[i].split(",");

				this.appendListValues(sbf, entry);
			}
		} else {
			if (this.defaultValue != null && this.defaultValue.length() > 0) {
				sbf.append("<option value=\"").append(this.defaultValue)
						.append("\" selected=\"selected\" >")
						.append(this.defaultValue).append("</option>");
			}
		}
		sbf.append("</select>");
	}

	/**
	 * render html for options
	 * 
	 * @param sbf
	 * @param grid
	 */
	private void appendListValues(StringBuilder sbf, String[] grid) {

		if (grid == null) {
			return;
		}
		if (grid.length <= 0) {
			return;
		}

		sbf.append("<option value=\"").append(grid[0]).append("\" ");
		if (grid.length > 1) {
			if (grid[0].equals(this.defaultValue)) {
				sbf.append(" selected=\"selected\" ");
			}
		}

		if (grid.length > 2) {
			sbf.append(" class=\"").append(grid[2]).append("\" ");
		}

		sbf.append(">");
		if (grid.length > 1) {
			sbf.append(grid[1]);
		}

		sbf.append("</option>");
	}

	@Override
	public void initialize(PageGeneratorContext context) {
		super.initialize(context);
		if (this.listServiceQueryFieldSources == null
				&& this.listServiceQueryFieldSources != null) {
			this.listServiceQueryFieldSources = this.listServiceQueryFieldNames;
		}

		if (this.listServiceId == null) {
			if (this.dataElement.listServiceName != null) {
				Spit.out("listServiceId for " + this.name + " set to "
						+ this.dataElement.listServiceName
						+ " based on settings in data dictionary for "
						+ this.dataElement.name);
				this.listServiceId = this.dataElement.listServiceName;
			} else if (this.valueList == null || this.valueList.length() == 0) {
				Spit.out("No list service set for " + this.name);
			}

		}
		if (this.descQueryFields != null && this.descQueryFieldSources == null) {
			this.descQueryFieldSources = this.descQueryFields;
		}

	}
}

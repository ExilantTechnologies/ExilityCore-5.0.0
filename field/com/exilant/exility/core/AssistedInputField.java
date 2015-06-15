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
 * Field designed after learning from use of existing fields. Trying to make it
 * a smart field that adapts itself rather than asking the designer upfront as
 * to what type of field it is. As of now, this fields behaves like a simple
 * text input field, as a drop-down or as a combo (suggest)
 * 
 */
class AssistedInputField extends TextInputField {
	/**
	 * service that fetches possible suggestions
	 */
	String suggestionServiceId = null;
	/**
	 * list of possible values are fetched from server with only the first these
	 * many characters. As user types more, these are filtered on the client
	 * itself to show matching values to user.
	 */
	int suggestAfterMinChars = 1;

	/**
	 * fields that are sent along with suggestion service
	 */
	String[] suggestionServiceFields = null;

	/**
	 * in case the field names on the page are different from the names to be
	 * sent to server
	 */
	String[] suggestionServiceFieldSources = null;

	/**
	 * what are the columns to show as suggestions? default is first columns
	 */
	String columnIndexesToShow = null;

	/**
	 * in case this is to be rendered as a list service
	 */
	String listServiceId = null;
	/**
	 * fields to be sent along with list service
	 */
	String[] listServiceQueryFieldNames = null;
	/**
	 * in case the field names in the age are different from the name to be sent
	 * to server
	 */
	String[] listServiceQueryFieldSources = null;

	/**
	 * field that acts as a key to this list. Example list of states depend on
	 * country. country is keyVakue for the field state.
	 */
	String keyValue = null;

	/**
	 * label to be used for the blank option in the list
	 */
	String blankOption = null;

	/**
	 * by default, list service is invoked on loading the page. You may choose
	 * to delay it to a point when you need it.
	 */
	boolean noAutoLoad = false;
	/**
	 * If this is a mandatory field, should we use the first option a default
	 * selection?
	 */
	boolean selectFirstOption = false;
	/**
	 * is the list same across different rows if this field is inside a grid, or
	 * does it depend on another field in this row?
	 */
	boolean sameListForAllRows = false;

	/**
	 * can user select multiple options?
	 */
	boolean multiplOptions = false;

	/**
	 * by default, we suggest based on the columns that contain what user has
	 * typed. Should we use starting character as the criterion instead?
	 */
	boolean matchStartingChars = false;

	/**
	 * css class name to be used for each row in the list
	 */
	String listCss = null;

	/**
	 * if user can select multiple values, how do we transmit them between
	 * client and server? text, list and grid are possible options
	 */
	String selectionValueType = null;

	@Override
	protected boolean focusAndBlurNeeded() {
		return true;
	}

	@Override
	void fieldToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		this.fieldToHtmlWorker(sbf, pageContext);
	}

	@Override
	public void initialize(PageGeneratorContext context) {
		super.initialize(context);
		if (this.suggestionServiceFieldSources == null) {
			this.suggestionServiceFieldSources = this.suggestionServiceFields;
		}
		if (this.listServiceQueryFieldSources == null) {
			this.listServiceQueryFieldSources = this.listServiceQueryFieldNames;
		}

		/*
		 * if this is a boolean field, provide a value list so that this field
		 * works like a selection box
		 */

		if (this.valueList == null) {
			AbstractDataType dt = DataTypes.getDataType(this.dataType, null);
			if (dt != null && dt.getValueType() == DataValueType.BOOLEAN) {
				BooleanDataType b = (BooleanDataType) dt;
				this.valueList = "0," + b.falseValue + ";1," + b.trueValue;
			}
		} else {
			/**
			 * do we have to add blank option to this list?
			 */
			if (this.blankOption != null && this.blankOption.length() > 0) {
				if (this.valueList.charAt(0) == ExilityConstants.LIST_SEPARATOR_CHAR) {
					Spit.out(this.name
							+ " has blankOption set to "
							+ this.blankOption
							+ " but also has taken care of this in its valueList. blankOption is ignored.");
				} else {
					this.valueList = ExilityConstants.LIST_SEPARATOR
							+ this.blankOption
							+ ExilityConstants.PAIR_SEPARATOR + this.valueList;
				}
				this.blankOption = null;
			}
		}
	}
}

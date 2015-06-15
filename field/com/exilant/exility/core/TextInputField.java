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
 * most common input field. Has lots of attribute to cater to different
 * requirements
 * 
 */
class TextInputField extends AbstractInputField {
	/**
	 * is this a from-field of a from-to pair? then specify the to-field here.
	 * We provide provision for both froField and toField to take care a field
	 * participating in more than one pairs. Specifying either fromField or
	 * toField is enough, though it will not be an error if you do.
	 */
	String toField = null;
	/**
	 * is this a to-field of a from-to pair? then specify the from-field here.
	 * We provide provision for both froField and toField to take care a field
	 * participating in more than one pairs. Specifying either fromField or
	 * toField is enough, though it will not be an error if you do.
	 */
	String fromField = null;

	/**
	 * is there a pair of fields such that the user is supposed to specify value
	 * for only one of them, and not both. Also, one of them is a must? Then
	 * specify otherField for one of them. (It is not an error if you specify
	 * otherField for both, but it would be a redundant validation)
	 */
	String otherField = null;
	/**
	 * a default of 20 characters reserved for size/width of the field.
	 */
	int size = 20;

	/**
	 * produces protected tag for input field.
	 */
	boolean isProtected = false;

	/**
	 * this is to be used only if you use descriptionService for validating the
	 * filed. If this field is to be be unique, like userId, then set this to
	 * true. Exility uses the description service to check if this value is
	 * already taken.
	 */
	boolean isUniqueField = false;

	/**
	 * page src for the code picker
	 */
	String codePickerSrc = null;

	/**
	 * fields to be extracted from the result of description service.
	 * Description service is expected to get values for these field.
	 */
	String[] descFields = null;
	/**
	 * fields, other than the current field, to be sent to server along with
	 * description service. Note that this field is by default sent.
	 */

	String[] descQueryFields = null;
	/**
	 * in case the names to be sent to server do not match the names of the
	 * fields, use this attribute. Sources should be in the same order as the
	 * fields. DO NOT specify this if the sources are same as field names.
	 */
	String[] descQueryFieldSources = null;

	/**
	 * description service that takes this field value and returns one or more
	 * rows with the fields specified in descFields
	 */
	String descServiceId = null;

	/**
	 * some projects were reusing a service that was actually bringing all
	 * fields, but with different header names. With this directive, desc fields
	 * are assumed to be in the same order as columns in the returned grid
	 */
	boolean doNotMatchDescNames = false;

	/**
	 * by default, we assume that the field value to be sent to desc service may
	 * be partially entered, and hence we do not validate.
	 */
	String validateQueryFields = null;

	/**
	 * Should the desc service be requested even on load if a value is set to
	 * the field, or should it be trigger only after user changes the field?
	 */
	boolean supressDescOnLoad = false;

	/**
	 * for combo box. When should we start suggesting? Note that there is no
	 * point in setting it to 0, because, in that case, you are actually talking
	 * about listService, and not descService
	 */
	int minCharsToTriggerService = 1;

	/**
	 * fields that are expected back from combo/suggestion service. If not
	 * specified, columns have their default meanings
	 */
	String[] comboDisplayFields = null;

	/**
	 * for a field of date-and-time type. Should the time be shown as AM/PM or
	 * 24 hrs ?
	 */
	boolean showAmPm = false;
	/**
	 * for a field of date-and-time type. Should we show seconds at all?
	 */
	boolean suppressSeconds = false;

	/**
	 * internally used
	 */
	boolean isValid = true;

	/**
	 * filter field is a special field that has possibly two accompanying fields
	 * fieldNameOperator and fieldNameTo using which the field value can be used
	 * in filtering criterion
	 */
	boolean isFilterField = false;

	/**
	 * This is applicable if you have description service. Once the description
	 * service executes, do you want to the field focus to shift to a specific
	 * field than the normal tab order?
	 */
	String fieldToFocusAfterExecution = null;

	/**
	 * You can change the default code picker image that Exility would have put.
	 */
	String codePickerImage = null;

	/**
	 * do you want to specifically position code-picker? specify left for the
	 * pop-up window pixels
	 */
	int codePickerLeft = -1;
	/**
	 * do you want to specifically position code-picker? specify top for the
	 * pop-up window pixels
	 */
	int codePickerTop = -1;

	/**
	 * list of valid values. of the form "one,two,three..." or "1,one;2,two..."
	 * this is actually an attribute of assisted input field, but defined here
	 * for convenience
	 */
	String valueList;

	/**
	 * set this field as filter field
	 */
	void setFilterField() {
		this.isFilterField = true;
	}

	@Override
	void fieldToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		AbstractDataType dt = DataTypes.getDataType(this.dataType, null);
		if (dt.getValueType() == DataValueType.DATE) {
			if (((DateDataType) dt).includesTime) {
				this.dateTimeFieldToHtml(sbf, pageContext);
			} else {
				this.fieldToHtmlWorker(sbf, pageContext);
				this.addDatePicker(sbf, pageContext);
			}
			return;
		}

		this.fieldToHtmlWorker(sbf, pageContext);
		if (this.codePickerSrc != null) {
			this.addCodePicker(sbf, pageContext);
		}
	}

	/**
	 * special rendering for date-time field to take care of rendering it as
	 * multiple fields as far as user is concerned, but internally it is a
	 * single data element
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	void dateTimeFieldToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		String myName = pageContext.getName(this.name);
		sbf.append("<table id=\"").append(myName)
				.append("FieldsTable\"><tr><td>");
		this.fieldToHtmlWorker(sbf, pageContext);
		sbf.append("</td><td>");

		/*
		 * All Exility components are thread-safe because their instance
		 * attributes are set once on load, and are not changed during
		 * execution. (These are immutable objects) However, here we will
		 * violate that, with the comfort that the page generator does not reuse
		 * an instance of a page. A Page object is loaded from xml, page
		 * generated, and is destroyed.
		 */
		this.skipId = true;

		String commonStartText = "\n<input type=\"text\" class=\"intdecinputfield\" size=\"2\"  maxlength=\"2\" ";
		if (this.isProtected) {
			commonStartText += " readonly=\"readonly\" tabindex=\"-1\" ";
		}
		commonStartText += "id=\"" + myName;

		sbf.append(commonStartText).append("Hr\" ");
		this.addMyAttributes(sbf, pageContext);

		sbf.append("/></td><td>");

		// min field
		sbf.append("<span class=\"colonstyle\">:</span></td><td>");
		sbf.append(commonStartText).append("Mn\" ");
		this.addMyAttributes(sbf, pageContext);
		sbf.append("/></td><td>");

		if (this.suppressSeconds == false) {
			sbf.append("<span class=\"colonstyle\">:</span></td><td>");
			sbf.append(commonStartText).append("Sc\" ");
			this.addMyAttributes(sbf, pageContext);
			sbf.append("/></td><td>");
		}

		if (this.showAmPm) {
			sbf.append("\n<select id=\"").append(myName).append("Am\" ");
			super.addMyAttributes(sbf, pageContext);
			sbf.append("><option value=\"\">AM</option><option value=\"1\">PM</option></select></td><td>");
		}
		this.addDatePicker(sbf, pageContext);
		sbf.append("</td></tr></table>");
		this.skipId = false;
	}

	/**
	 * common method that can be used by extended classes to render input tag
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	protected void fieldToHtmlWorker(StringBuilder sbf,
			PageGeneratorContext pageContext) {
		sbf.append("\n<input type=\"text\" autocomplete=\"off\" ");
		if (this.cssClassName == null) {
			if ((this.valueType == DataValueType.INTEGRAL)
					|| (this.valueType == DataValueType.DECIMAL)) {
				sbf.append("class=\"intdecinputfield\" ");
			} else if ((this.valueType == DataValueType.DATE)
					|| (this.valueType == DataValueType.TIMESTAMP)) {
				sbf.append("class=\"dateinputfield\" ");
			} else {
				sbf.append("class=\"inputfield\" ");
			}
		}

		if (this.size > 0) {
			sbf.append(" size=\"").append(this.size).append("\" ");
		}
		String valToSet = this.defaultValue;
		if (this.valueList != null) {
			valToSet = this.getDefaultValueFromValueList();
		}
		if (valToSet != null) {
			sbf.append(" value=\"").append(valToSet).append("\" ");
		}
		if (this.isProtected) {
			sbf.append(" readonly=\"readonly\" tabindex=\"-1\" ");
		}

		int len = DataTypes.getDataType(this.dataType, null).getMaxLength();
		if (len > 0) {
			sbf.append(" maxlength=\"").append(len).append("\" ");
		}
		super.addMyAttributes(sbf, pageContext);
		String myName = pageContext.getName(this.name);
		boolean needPicker = (this.valueType == DataValueType.DATE)
				|| (this.codePickerSrc != null);
		boolean needCombo = (this.descServiceId != null)
				&& (this.minCharsToTriggerService > 0);
		if (needCombo) {
			/*
			 * check if onblur is already added in AbstractInputField
			 */
			if (this.onBlurActionName == null) {
				sbf.append(" onblur=\"P2.inputFocusOut(this, '").append(myName)
						.append("');\" ");
			}
		}

		if (needCombo || needPicker) {
			sbf.append(" onkeyup=\"P2.inputKeyUp(this, '").append(myName)
					.append("', event);\" ");
		}
		sbf.append("/>");
	}

	/**
	 * split this.valueList into parts. If default value is an internal value,
	 * then return corresponding display value. Else return default value itself
	 * 
	 * @return value to be used as display value
	 */
	private String getDefaultValueFromValueList() {
		String valueToMatch = this.defaultValue == null ? ""
				: this.defaultValue.toLowerCase();
		String[] vals = this.valueList.split(ExilityConstants.PAIR_SEPARATOR);
		for (String val : vals) {
			String[] parts = val.split(ExilityConstants.LIST_SEPARATOR);
			String internalValue = parts[0].trim();
			String displayVal = (parts.length > 1) ? parts[1].trim()
					: internalValue;
			if (internalValue.toLowerCase().equals(valueToMatch)) {
				return displayVal;
			}
		}
		return null;
	}

	/**
	 * add a date picker next to the field
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	private void addDatePicker(StringBuilder sbf,
			PageGeneratorContext pageContext) {
		DatePickerImageElement ele = new DatePickerImageElement();
		ele.hidden = this.hidden;
		ele.name = this.name + "Picker";
		ele.targetName = pageContext.getName(this.name);
		ele.toHtml(sbf, pageContext);
	}

	/**
	 * add a code picker next to the field
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	protected void addCodePicker(StringBuilder sbf,
			PageGeneratorContext pageContext) {
		CodePickerImageElement ele = new CodePickerImageElement();
		ele.hidden = this.hidden;
		ele.targetName = pageContext.getName(this.name);
		ele.name = this.name + "Picker";
		ele.toHtml(sbf, pageContext);
	}

	@Override
	public void initialize(PageGeneratorContext context) {
		super.initialize(context);
		if (this.descQueryFields != null && this.descQueryFieldSources == null) {
			this.descQueryFieldSources = this.descQueryFields;
		}
		if (this.descServiceId != null) {
			this.isValid = true;
		}
	}
}

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
 * type of field that is used as filtering criterion
 * 
 */
enum FilterType {
	text, date, number
}

/**
 * type of comparator for filtering criterion
 */
enum CamparisonType {
	notSpecified, equals, startsWith, contains, greaterThan, lessThan, between
}

/**
 * Filter field is a wrapper on a field for the client to manage a filtering
 * criterion as a field.In consists of a base field on which user specified
 * condition, like less than 20. Internally it is a group of two or three
 * fields. One of course is the base field. second is the comparator. In case
 * the comparator requires a limit, then we have the to-field. Our convention is
 * a suffix of "Comparator" and "To" for these two fields. It is rendered as a
 * base field followed by a drop-down of comparators and a to-field that is
 * displayed on a need basis
 * 
 */
class FilterField extends AbstractInputField {
	private static final String STRING_OPERATORS = "1,Equals;2,Starts with;3,Contains";
	private static final String NON_STRING_OPERATORS = "1,Equals;4,Greater than;5,Less than;6,Between";

	/**
	 * display size
	 */
	int size = 20;

	/**
	 * used by the comparison type drop-down
	 */
	CamparisonType defaultComparisonType = CamparisonType.notSpecified;
	private AssistedInputField firstField;
	private AssistedInputField seconfField;
	private AssistedInputField selectionField;

	@Override
	void fieldToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {

		/*
		 * first append a drop-down box that lists the operators
		 */
		this.selectionField.fieldToHtml(sbf, pageContext);
		sbf.append("&nbsp;&nbsp;");

		/*
		 * field1
		 */
		this.firstField.fieldToHtml(sbf, pageContext);

		/*
		 * for number and date fields, we need second value fields let us re-use
		 * the first field itself, and change some attributes
		 */

		if (this.seconfField != null) {
			sbf.append("&nbsp;&nbsp;");
			/*
			 * a div to show/hide second field based on operator
			 */
			sbf.append("\n<span id=\"" + this.name + "ToDiv\" ");

			/*
			 * this is to be visible only if the default comparison is between
			 */
			if (this.defaultComparisonType != CamparisonType.between) {
				sbf.append(" style=\"display:none;\" ");
			}
			sbf.append("> <label>and</label> ");

			this.seconfField.fieldToHtml(sbf, pageContext);
			sbf.append("</span>");
		}
		return;
	}

	/**
	 * render the first field
	 * 
	 * @return
	 */
	private AssistedInputField getFirstField(PageGeneratorContext context) {
		AssistedInputField inf = new AssistedInputField();
		inf.name = this.name;
		inf.labelPosition = LabelPosition.hide;
		inf.dataElementName = this.dataElementName;
		inf.defaultValue = this.defaultValue;
		inf.isRequired = this.isRequired;
		inf.size = this.size;
		inf.dataElement = this.dataElement;
		inf.dataType = this.dataType;
		inf.formatter = this.formatter;
		inf.onUserChangeActionName = this.onUserChangeActionName;
		inf.dataType = this.dataType;
		inf.valueType = this.valueType;
		inf.initialize(context);
		inf.setFilterField();
		return inf;
	}

	/**
	 * render second field
	 * 
	 * @return
	 */
	private AssistedInputField getSecondField(PageGeneratorContext context) {
		AssistedInputField inf = new AssistedInputField();
		inf.name = this.name + CommonFieldNames.FILTER_TO_SUFFIX;
		inf.labelPosition = LabelPosition.hide;
		inf.dataElementName = (this.dataElementName != null) ? this.dataElementName
				: this.name;
		inf.dataElement = this.dataElement;
		inf.dataType = this.dataType;
		inf.isRequired = false;
		inf.size = this.size;
		inf.fromField = this.name;
		inf.hidden = this.hidden;
		inf.formatter = this.formatter;

		inf.onUserChangeActionName = this.onUserChangeActionName;
		/*
		 * VERY VERY IMPORTANT : all the classes are designed for loading from
		 * XML, after which an Initialize()..
		 */
		inf.dataType = this.dataType;
		inf.valueType = this.valueType;
		inf.initialize(context);
		return inf;
	}

	/**
	 * render selection field for comparator
	 * 
	 * @return
	 */
	private AssistedInputField getSelectionField(PageGeneratorContext context) {
		AssistedInputField sf = new AssistedInputField();
		sf.name = this.name + CommonFieldNames.FILTER_OPERATOR_SUFFIX;
		sf.label = this.label;
		sf.dataElementName = DataDictionary.DEFAULT_ELEMENT_NAME;
		sf.onUserChangeActionName = this.onUserChangeActionName;
		sf.isFilterOperator = true;

		/*
		 * default value should be the internal number, and not the text of the
		 * enum
		 */
		int ct;
		if (this.defaultComparisonType == CamparisonType.notSpecified) {
			if (this.valueType == DataValueType.TEXT) {
				ct = CamparisonType.contains.ordinal();
			} else {
				ct = CamparisonType.equals.ordinal();
			}
		} else {
			ct = this.defaultComparisonType.ordinal();
		}

		sf.defaultValue = Integer.toString(ct);
		sf.isRequired = true;

		if (this.valueType == DataValueType.TEXT) {
			sf.valueList = STRING_OPERATORS;
		} else {
			sf.valueList = NON_STRING_OPERATORS;
		}
		sf.hidden = this.hidden;
		sf.dataType = "number";
		sf.dataElement = this.dataElement;
		sf.dataElementName = this.name;
		sf.valueType = DataValueType.INTEGRAL;
		sf.initialize(context);
		return sf;
	}

	@Override
	void toJs(StringBuilder js, PageGeneratorContext pageContext) {
		this.selectionField.toJs(js, pageContext);
		this.firstField.toJs(js, pageContext);
		if (this.seconfField != null) {
			this.seconfField.toJs(js, pageContext);
		}
		return;
	}

	@Override
	public void initialize(PageGeneratorContext context) {
		super.initialize(context);
		this.firstField = this.getFirstField(context);
		this.selectionField = this.getSelectionField(context);
		if (this.valueType != DataValueType.TEXT) {
			this.seconfField = this.getSecondField(context);
		}
	}
}

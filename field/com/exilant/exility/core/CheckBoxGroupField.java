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
 * This represents set of check-boxes, of which user can select multiple ones.
 * This is an alternative to multiple-select selection (drop-down) field
 */
class CheckBoxGroupField extends AbstractInputField {
	/**
	 * same meaning as in selectionField. Service that fetches teh possible set
	 * of values for this field
	 */
	String listServiceId = null;
	/**
	 * refer to selectionField
	 */
	String[] listServiceQueryFieldNames = null;
	/**
	 * refer to selectionField
	 */
	String[] listServiceQueryFieldSources = null;
	/**
	 * refer to selectionField
	 */
	String keyValue = null;
	/**
	 * refer to selectionField
	 */
	boolean noAutoLoad = false;
	/**
	 * refer to selectionField
	 */
	String valueList = null;
	/**
	 * refer to selectionField
	 */
	boolean sameListForAllRows = false;

	/**
	 * refer to selectionField
	 */
	SelectionValueType selectionValueType = SelectionValueType.text;
	/**
	 * refer to selectionField
	 */
	int minSelections = 0;
	/**
	 * refer to selectionField
	 */
	int maxSelections = 0;

	/**
	 * in case the selectionValueType is grid, what is the name of the column in
	 * the grid that has the selected values
	 */
	String columnName = null;

	/**
	 * internally used for optimization
	 */
	private String[] keyList = null;

	@Override
	void fieldToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("\n<div ");
		if (this.cssClassName == null) {
			sbf.append(" class=\"checkBoxGroup\" ");
		}
		super.addMyAttributes(sbf, pageContext);
		sbf.append(">");

		/*
		 * see if designer has supplied initial set of values
		 */
		if (this.valueList != null && this.valueList.length() != 0) {
			String[] list = this.valueList.split(";");
			int n = list.length;
			for (int i = 0; i < n; i++) {
				String[] entry = list[i].split(",");
				this.addCheckBox(sbf, entry, pageContext);
			}
		}
		sbf.append("</div>");
	}

	/***
	 * generate check box for the option.
	 * 
	 * @param sbf
	 * @param entry
	 * @param pageContext
	 */
	private void addCheckBox(StringBuilder sbf, String[] entry,
			PageGeneratorContext pageContext) {
		if (entry == null || entry.length <= 0) {
			return;
		}
		String key = entry[0];
		String lbl = entry[1];
		String parentName = pageContext.getName(this.name);
		sbf.append("<div><input type=\"checkbox\" ");

		/*
		 * id not to be generated if it is inside a grid
		 */
		if (pageContext.isInsideGrid == false) {
			sbf.append("id=\"").append(parentName).append('_').append(key)
					.append('"');
		}

		sbf.append(" value=\"").append(key).append('"');
		sbf.append(" onchange=\"P2.checkBoxGroupChanged(this, '")
				.append(parentName).append("');\" ");

		if (key.equals(this.defaultValue)) {
			sbf.append("checked=\"checked\" ");
		}
		sbf.append("/>");
		sbf.append("\n<label ");
		/*
		 * labelFor meaningful only when it is outside grid and id is added to
		 * check-box
		 */
		if (key.equals(this.defaultValue)) {
			sbf.append("for=\"").append(parentName).append('_').append(key)
					.append("\" ");
		}

		sbf.append(">").append(lbl).append("</label></div>");
	}

	@Override
	public void initialize(PageGeneratorContext context) {
		super.initialize(context);
		if (this.dataElementName == null) {
			this.dataElementName = this.name;
		}

		if (this.listServiceId == null) {
			if (this.dataElement.listServiceName != null) {
				this.listServiceId = this.dataElement.listServiceName;
			}
		}

		if (this.valueList != null && this.valueList.length() > 0) {
			String[] rows = this.valueList.split(";");
			int n = rows.length;
			this.keyList = new String[n];
			for (int i = 0; i < n; i++) {
				this.keyList[i] = rows[i].split(",")[0];
			}
		}
	}
}

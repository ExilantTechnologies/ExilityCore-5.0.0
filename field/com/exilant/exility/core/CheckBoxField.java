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
 * represents a check box field in html
 */
class CheckBoxField extends AbstractInputField {
	/**
	 * do you want the 'true' value to be the default. remember false is our
	 * default.
	 */
	boolean checkedValueIsTheDefault = false;

	/**
	 * internal value to be used when the field is checked by user
	 */
	String checkedValue = null;

	/**
	 * internal value to be used when the field is un-checked by user
	 */
	String uncheckedValue = null;

	/**
	 * what is the initial state of this field?
	 */
	boolean isChecked = false;

	/**
	 * when this field is used as a check-all field for a table. Grid name that
	 * is associated with this check box.
	 */
	String tableName = null;

	/**
	 * associate a table with this check box
	 * 
	 * @param tableName
	 */

	void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@Override
	void fieldToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		AbstractDataType dt = DataTypes.getDataType(this.dataType, null);
		/**
		 * we have some internal fields ending with "Delete" that may not be set
		 * to proper data type
		 */
		if (dt.getValueType() != DataValueType.BOOLEAN
				&& this.name.endsWith("Delete") == false) {
			Spit.out("ERROR:"
					+ this.name
					+ " is a check box but its data type is not Boolean. Some features will not work properly.");
		}
		sbf.append("\n<input type=\"checkbox\" ");

		super.addMyAttributes(sbf, pageContext);

		if (this.checkedValue != null) {
			sbf.append(" value=\"").append(this.checkedValue).append("\" ");
		}

		if (this.checkedValueIsTheDefault) {
			sbf.append(" checked = \"checked\" ");
		}

		sbf.append("/>");
		String lbl = this.getLabelToUse(pageContext);
		String layoutType = pageContext.getLayoutType();
		if (layoutType != null && layoutType.equals("5") && lbl != null) {
			sbf.append("\n<label for=\"").append(this.name).append("\" ");

			if (this.hoverText != null) {
				sbf.append("title=\"").append(this.hoverText).append("\" ");
			}

			sbf.append(">").append(lbl).append("</label>");
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.exilant.exility.core.AbstractField#fieldToHtml5(java.lang.StringBuilder,
	 *      com.exilant.exility.core.PageGeneratorContext) We have an issue with
	 *      delete check-box etc.. internally added check-box is using
	 *      htmlAttribute at this time to push some events. In general, we do
	 *      not render htmlAttribute in css mode. we need an exception for this.
	 *      Hence we have just cloned toHtml and made this one change
	 */
	@Override
	void fieldToHtml5(StringBuilder sbf, PageGeneratorContext pageContext) {
		AbstractDataType dt = DataTypes.getDataType(this.dataType, null);
		/**
		 * we have some internal fields ending with "Delete" that may not be set
		 * to proper data type
		 */
		if (dt.getValueType() != DataValueType.BOOLEAN
				&& this.name.endsWith("Delete") == false) {
			Spit.out("ERROR:"
					+ this.name
					+ " is a check box but its data type is not Boolean. Some features will not work properly.");
		}
		sbf.append("\n<input type=\"checkbox\" ");

		super.addMyAttributes(sbf, pageContext);

		if (this.checkedValue != null) {
			sbf.append(" value=\"").append(this.checkedValue).append("\" ");
		}

		if (this.checkedValueIsTheDefault) {
			sbf.append(" checked = \"checked\" ");
		}
		/**
		 * TODO : to be re-factored with a better design
		 */
		if (this.htmlAttributes != null) {
			sbf.append(' ').append(this.htmlAttributes).append(' ');
		}
		sbf.append("/>");
		String lbl = this.getLabelToUse(pageContext);
		String layoutType = pageContext.getLayoutType();
		if (layoutType != null && layoutType.equals("5") && lbl != null) {
			sbf.append("\n<label for=\"").append(this.name).append("\" ");

			if (this.hoverText != null) {
				sbf.append("title=\"").append(this.hoverText).append("\" ");
			}

			sbf.append(">").append(lbl).append("</label>");
		}
	}

	@Override
	public void initialize(PageGeneratorContext context) {
		super.initialize(context);
		if (this.checkedValueIsTheDefault) {
			this.isChecked = true;
		}
		BooleanDataType dt = null;
		try {
			dt = (BooleanDataType) DataTypes.getDataType(this.dataType, null);
		} catch (Exception e) {
			Spit.out("Field "
					+ this.name
					+ " does not have a valid boolean data type associates with that. Check box WILL NOT be initialized with the rigth default");
		}
		if (this.defaultValue == null) {
			if (this.checkedValueIsTheDefault) {
				if (this.checkedValue != null) {
					this.defaultValue = this.checkedValue;
				} else if (dt != null) {
					this.defaultValue = dt.trueValue;
				}

			} else {
				if (this.uncheckedValue != null) {
					this.defaultValue = this.uncheckedValue;
				} else if (dt != null) {
					this.defaultValue = dt.falseValue;
				}

			}
			Spit.out("A default value of " + this.defaultValue + " is set to "
					+ this.name);
		}
	}
}

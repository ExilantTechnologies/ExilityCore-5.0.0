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
 * implements a radio button control.
 * 
 */
class RadioButtonField extends AbstractInputField {
	/**
	 * of the form internalValu1,displayValue1;....
	 */
	String valueList = null;

	/**
	 * if this radio button is to be rendered based on run-time values
	 */
	String listServiceId = null;

	/**
	 * does the list service require a key value to be sent as an input. Similar
	 * to selection field
	 */
	String keyValue = null;

	/**
	 * by default, possible values for this field are fetched on load of the
	 * page by e3xplicitly requesting for this service. If you bring values for
	 * this as part of another service, or if it is triggered based on another
	 * field, you can suppress this loading.
	 */
	boolean noAutoLoad = false;

	@Override
	void fieldToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		/*
		 * we would like to move this way of generating. We missed the bus with
		 * HTML5.. This was triggered while adding listServiceId feature
		 */
		if (this.listServiceId != null) {
			this.fieldToHtmlRevised(sbf, pageContext);
			return;
		}
		String[] list = null;
		if (this.valueList != null && this.valueList.length() > 0) {
			list = this.valueList.split(";");
		} else {
			AbstractDataType dt = DataTypes.getDataType(this.dataType, null);
			if (dt == null || dt instanceof BooleanDataType == false) {
				String message = "Invalid value list for radio button "
						+ this.name;
				Spit.out(message);
				return;
			}
			BooleanDataType bd = (BooleanDataType) dt;
			list = new String[2];
			list[0] = "0," + bd.falseValue;
			list[1] = "1," + bd.trueValue;
		}

		int n = list.length;
		if (n < 2) {
			String message = "ERROR: Radio button field "
					+ this.name
					+ " with label "
					+ this.label
					+ " has been supplied with a value list = "
					+ this.valueList
					+ ". It should have at least two values in it. e.g. 0,Any Color; 1,Red; 2, Blue..";
			Spit.out(message);
			pageContext.reportError(message);
			return;
		}

		String myName = pageContext.getName(this.name);
		sbf.append("<span class=\"radiogroup\" ");
		super.addMyParentsAttributes(sbf, pageContext);
		sbf.append(">");
		for (int i = 0; i < n; i++) {
			String[] entry = list[i].split(",");
			/**
			 * IE creates confusion between name and id. e.g. if you set name=
			 * and not id= for radio, it can still be fetched using
			 * getElementById. this creates conflict between id= of span, and
			 * name= of radio. Hence I am adding Radio to the name of radio. id
			 * is set to name_value
			 */
			sbf.append("\n<input type=\"radio\" name=\"").append(myName)
					.append("Radio\" id=\"").append(this.name).append('_')
					.append(entry[0]).append("\" ");
			super.addMyAttributesOnly(sbf, pageContext);

			if (this.htmlAttributes != null) {
				sbf.append(' ').append(this.htmlAttributes).append(' ');
			}

			sbf.append(" value=\"").append(entry[0]).append("\" ");
			if (entry[0].equals(this.defaultValue)) {
				sbf.append(" checked=\"checked\" ");
			}
			sbf.append("/><label>");
			if (entry.length > 1) {
				sbf.append(entry[1]);
			} else {
				sbf.append(entry[0]);
			}
			sbf.append("</label>");
			sbf.append("&nbsp;&nbsp;");
		}
		sbf.append("</span>");
	}

	/**
	 * generate html with divs to provide maximum flexibility. html
	 * div#name.radioGroup with a child div.radio with an input and label as its
	 * children. Idea is to allow the only child to be cloned and then replace
	 * --- with value and label. refer to the client side code to understand
	 * this logic
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	private void fieldToHtmlRevised(StringBuilder sbf,
			PageGeneratorContext pageContext) {
		sbf.append("<div class=\"radiogroup\" ");
		super.addMyParentsAttributes(sbf, pageContext);

		/*
		 * innerHTML for one option. This div is hidden to ensure that we do not
		 * show dummy option
		 */
		sbf.append(" style=\"visibility:hidden;\" ><div class=\"radio\"><input type=\"radio\" name=\"");
		sbf.append(pageContext.getName(this.name));
		sbf.append("Radio\" ");
		super.addMyAttributesOnly(sbf, pageContext);
		sbf.append(" value=\"---\" /><label>---</label></div></div>");
	}

}

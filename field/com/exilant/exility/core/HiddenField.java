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
 * fields that are used internally, and returned to server. To be compliant, we
 * are rendering it as hidden field, but we could have got away by not rendering
 * it all because of our client-side MVC.
 */
class HiddenField extends AbstractInputField {
	/**
	 * clever use of hidden field lead us to provide some features of
	 * textInputField. refer to textInputField
	 */
	String[] descFields = null;
	/**
	 * refer to textInpuField
	 */
	String[] descQueryFields = null;
	/**
	 * refer to textInpuField
	 */
	String[] descQueryFieldSources = null;
	/**
	 * refer to textInpuField
	 */
	String descServiceId = null;
	/**
	 * refer to textInpuField
	 */
	boolean supressDescOnLoad = false;
	/**
	 * refer to textInpuField
	 */
	String fieldToFocusAfterExecution = null;

	@Override
	void fieldToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("\n<input type=\"hidden\" ");

		super.addMyAttributes(sbf, pageContext);

		if (this.defaultValue != null) {
			sbf.append("value=\"").append(this.defaultValue).append("\" ");
		}

		sbf.append("/>");
	}

	@Override
	public void initialize(PageGeneratorContext context) {
		super.initialize(context);
		/*
		 * hidden field, is never be put in a separate unit
		 */
		this.numberOfUnitsToUse = 0;
		/*
		 * over-ride the label position
		 */
		if (this.labelPosition != LabelPosition.none) {
			this.labelPosition = LabelPosition.hide;
		}
		this.label = null;
	}

}

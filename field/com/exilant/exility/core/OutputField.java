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
 * represents an output field. It is rendered. It can also be used as a field
 * that is not to be edited by user but send it back to server.
 * 
 */
class OutputField extends AbstractField {
	/**
	 * An occasional long value may make the html look ugly. You can restrict
	 * the rendering to certain number of characters. If teh actual length
	 * exceeds this, a tool-tip is used to show the complete value
	 */
	int maxCharacters = 0;

	/**
	 * submit-form will include this field
	 */
	boolean toBeSentToServer = false;

	/**
	 * by default we do not allow html formatted text, to avoid html injection.
	 * This option should be used with caution.
	 */
	boolean allowHtmlFormattedText = false;
	/**
	 * like "1,Yes;0,No" will out put Yes/No instead of 1/0 for a boolean value.
	 * Exility will display 'Yes' for 1 etc..
	 */
	String valueList = null;

	@Override
	void fieldToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("<div ");
		if (this.cssClassName == null) {
			sbf.append("class=\"field\" ");
		}

		super.addMyAttributes(sbf, pageContext);

		sbf.append(">");
		if (this.defaultValue != null) {
			sbf.append(this.defaultValue);
		} else {
			sbf.append("&nbsp;");
		}

		sbf.append("</div>");
	}
}

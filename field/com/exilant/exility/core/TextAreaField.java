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
 * represents a textArea tag in html that allows entry of large text with
 * indentations.
 * 
 */
class TextAreaField extends AbstractInputField {
	/**
	 * number of rows attribute of html
	 */
	int numberOfRows = 3;

	/**
	 * number of characters per row for rendering
	 */
	int numberOfCharactersPerRow = 40;

	/**
	 * should this be disabled for editing?
	 */
	boolean isProtected = false;

	/**
	 * remember that the way the html is added : RenderOpenTab() +
	 * AddMyAttributes() + RenderCloseTags()
	 */
	@Override
	void fieldToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("\n<textarea ");

		super.addMyAttributes(sbf, pageContext);

		if (this.isProtected) {
			sbf.append(" readonly=\"readonly\" tabindex=\"-1\" ");
			/*
			 * else { string myName = pageContext.GetName(this.name);
			 * sbf.Append(
			 * " onblur=\"P2.fieldChanged(this, '").Append(myName).Append
			 * ("');\" "); }
			 */
		}

		if (this.numberOfCharactersPerRow > 0) {
			sbf.append("cols=\"").append(this.numberOfCharactersPerRow)
					.append("\" ");
		}

		if (this.numberOfRows > 0) {
			sbf.append("rows=\"").append(this.numberOfRows).append("\" ");
		}

		int len = DataTypes.getDataType(this.dataType, null).getMaxLength();
		if (len > 0) {
			sbf.append(" maxLength=\"").append(len).append("\" ");
		}

		sbf.append('>');

		if (this.defaultValue != null) {
			sbf.append(this.defaultValue);
		}
		sbf.append("</textarea>");
	}
}

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
 * this is an image that is fixed at design time, and does not change at run
 * time. You are generally better-off providing height and width for image so
 * that the browser allocates that space before downloading the image, there by
 * reducing possible flicker.
 */
class StaticImageElement extends AbstractElement {
	/**
	 * src attribute of the img element
	 */
	String src = null;

	@Override
	void toHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("<img alt=\"\" ");

		super.addMyAttributes(sbf, pageContext);
		String str = "";
		// height and width have different meanings here
		if (this.height != null) {
			str += "height:" + this.height + "; ";
		}
		if (this.width != null) {
			str += "width:" + this.width + "; ";
		}
		if (this.border != null) {
			str += "border-width:" + this.border + "; ";
		}

		if (str.length() > 0) {
			sbf.append("style=\"").append(str).append("\" ");
		}

		sbf.append(" src=\"").append(this.src).append("\" ");
		if (this.name != null) {
			sbf.append("id=\"").append(this.name).append("\" ");
		}

		sbf.append("/>");
	}

	@Override
	void toHtml5(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("<div id=\"").append(pageContext.getName(this.name))
				.append("\" ");

		super.addMyAttributes(sbf, pageContext);
		sbf.append("/>");
	}
}

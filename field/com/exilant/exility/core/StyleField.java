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

/***
 * If a run-time rendering is to be achieved by changing the style attribute of
 * a div, this is the field to use. It is called a field, and not an element,
 * because it has run-time behavioral change. We simply create a div element
 * with a custom attribute names "data-style". Run time value of the field is
 * set to this attribute, using which styles can be set at run time.
 * 
 */
class StyleField extends AbstractField {
	/***
	 * This is generally an output field, but in case you need it part of server
	 * data
	 */
	boolean toBeSentToServer = false;

	@Override
	void fieldToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("<div ");
		super.addMyAttributes(sbf, pageContext);

		if (this.defaultValue != null) {
			sbf.append(" data-style=\"").append(this.defaultValue)
					.append("\" ");
		}
		sbf.append("></div>");
	}
}

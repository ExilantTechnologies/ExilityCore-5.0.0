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
 * ImageField is designed to show different images at run time based on the
 * field value src attribute is set as baseSrc + value + imageExtension. for
 * example if status field is likely to have A, B and C as three possible values
 * then you set baseSrc="your-image-folder/status" and imageExtension=".png" and
 * you have three image files called statusA.png, statusB.png and statusC.png
 * 
 * @author raghu.bhandi
 * 
 */
class ImageField extends AbstractField {
	/***
	 * src attribute of img element in html is set as baseSrc + <run time value
	 * in name> + imageExtension
	 */
	String baseSrc = null;

	/***
	 * src attribute of img element in html is set as baseSrc + <run time value
	 * in name> + imageExtension
	 */
	String imageExtension = null;

	/***
	 * in case you want this be sent back to server..
	 */
	boolean toBeSentToServer = false;

	@Override
	void fieldToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("<img alt=\"\" ");

		super.addMyAttributes(sbf, pageContext);

		// if a default value is specified..
		if (this.defaultValue != null) {
			sbf.append("src=\"").append(this.baseSrc).append(this.defaultValue)
					.append(this.imageExtension).append("\" ");
		} else {
			sbf.append("src=\"\" ");
		}
		sbf.append("/>");
	}
}

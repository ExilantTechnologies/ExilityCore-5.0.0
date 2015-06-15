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
 * This is a special case. This is actually not an element at all It is a
 * mechanism to allow a designed to have more than one form in a page Normally
 * there is only one form in a page. This is handled by the page it self. That
 * is, page always creates a form with id=form1. In case you need another form,
 * use this element as a panel. This is not a panel, because the rendering is
 * not the way a panel is rendered. Hence, though this is an element, it can be
 * used only as a panel at the page level. To accommodate this, Page declares
 * its panes as AbstractElements, and not AbstractPanels for this purpose
 * 
 */

class FormEelement extends AbstractElement {
	@Override
	void toHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("\n</form><form id=\"").append(this.name)
				.append("\" action=\"\" >");
	}

	@Override
	void toHtml5(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("\n</form><form id=\"").append(this.name)
				.append("\" action=\"\" >");
	}
}

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

class ToolbarPanel extends AbstractPanel {

	public ToolbarPanel() {
	}

	@Override
	void panelToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("\n<div ");
		if (this.cssClassName == null) {
			sbf.append("class=\"toolbarpanel\" ");
		}
		super.addMyAttributes(sbf, pageContext);
		sbf.append("> ");

		int totalElements = 0;
		if (this.elements != null) {
			totalElements = this.elements.length;
		}
		if (totalElements == 0) {
			sbf.append("</div>");
			return;
		}

		if (pageContext.getLayoutType().equals("2")) {
			sbf.append(" <table class=\"fieldstable\" ");
		} else {
			sbf.append(" <table cellpadding=\"0\" cellspacing=\"0\" class=\"fieldstable\" ");
		}

		if (AP.alignPanels) {
			sbf.append(" width=\"100%\" ");
		}
		sbf.append(">\n");

		int eleIdx = 0;
		AbstractElement ele = this.elements[eleIdx];

		sbf.append(" <tr>");
		while (true) // going to break when controlIndex == totalControls
		{
			if (ele != null) {
				if (ele.inError) {
					pageContext.reportError("");
				}
				sbf.append("<td>");
				ele.toHtml(sbf, pageContext);
				sbf.append("</td>");
			}

			eleIdx++;
			if (eleIdx == totalElements) {
				break;
			}

			ele = this.elements[eleIdx];
			if (ele == null) {
				continue;
			}
		}
		sbf.append("</tr> </table> ");
		sbf.append(" </div>");
	}

	@Override
	void panelToHtml5(StringBuilder sbf, PageGeneratorContext pageContext) {
		this.panelToHtml(sbf, pageContext);
	}
}

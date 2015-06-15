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

class ContainerPanel extends AbstractPanel {
	/***
	 * by default, div tag is used for container. You may choose table-tr-td
	 * instead
	 */
	boolean useTableLayout;

	private void toTableHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("<table ");
		if (this.cssClassName == null) {
			sbf.append(" class=\"simpleContainer\" ");
		}
		super.addMyAttributes(sbf, pageContext);
		if (this.onClickActionName != null) {
			sbf.append(" onclick=\"P2.act(this, '").append(this.name)
					.append("','").append(this.onClickActionName)
					.append("');\" ");
		}
		sbf.append("><tbody><tr>");

		if (this.elements != null && this.elements.length > 0) {
			for (int i = 0; i < this.elements.length; i++) {
				AbstractElement ele = this.elements[i];
				sbf.append("<td id=\"").append(this.name).append("Cell")
						.append(i + 1).append("\">");
				if (pageContext.useHtml5) {
					ele.toHtml5(sbf, pageContext);
				} else {
					ele.toHtml(sbf, pageContext);
				}
				sbf.append("</td>");
			}
		}
		sbf.append("\n</tr></tbody></table>");
	}

	private void toDivHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("<div ");
		if (this.cssClassName == null) {
			sbf.append(" class=\"simpleContainer\" ");
		}
		super.addMyAttributes(sbf, pageContext);
		if (this.onClickActionName != null) {
			sbf.append(" onclick=\"P2.act(this, '").append(this.name)
					.append("','").append(this.onClickActionName)
					.append("');\" ");
		}
		sbf.append(">");
		if (this.elements != null && this.elements.length > 0) {
			for (AbstractElement ele : this.elements) {
				if (pageContext.useHtml5) {
					ele.toHtml5(sbf, pageContext);
				} else {
					ele.toHtml(sbf, pageContext);
				}
			}
		}
		sbf.append("\n</div>");
	}

	@Override
	void toHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		if (this.useTableLayout) {
			this.toTableHtml(sbf, pageContext);
		} else {
			this.toDivHtml(sbf, pageContext);
		}
	}

	@Override
	void toHtml5(StringBuilder sbf, PageGeneratorContext pageContext) {
		this.toHtml(sbf, pageContext);
	}

	@Override
	void panelToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		String errorText = this.name
				+ " is a contianer panel. There is an internal error because of which panelToHtml() is invoked. Report this to exility support team.";
		pageContext.reportError(errorText);
		sbf.append(errorText);
		Spit.out(errorText);
	}

	@Override
	void panelToHtml5(StringBuilder sbf, PageGeneratorContext pageContext) {
		this.panelToHtml(sbf, pageContext);
	}

}

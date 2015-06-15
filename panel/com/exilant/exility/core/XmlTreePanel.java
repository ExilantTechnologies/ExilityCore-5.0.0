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

class XmlTreePanel extends AbstractPanel {
	String fieldName = null;
	boolean bulkCheck = false;
	boolean showValues = false;
	boolean expandAllOnLoad = false;
	String childHtmlAttributes = null; // Jul 30 2009 : Bug 519 - XMLTree
										// rendered using grid needed addational
										// functions - SABMiller : Aravinda

	@SuppressWarnings("deprecation")
	private XmlTreeField field = null;

	@Override
	void panelToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		if (this.fieldName == null) {
			String msg = "XmlTreePanel " + this.name
					+ " does not have value for fieldName.";
			Spit.out(msg);
			pageContext.reportError(msg);
			return;
		}
		sbf.append("<div class=\"xmltreepanel\" ");
		super.addMyAttributes(sbf, pageContext);
		sbf.append('>');
		if (this.bulkCheck || (this.label != null && this.label.length() > 0)) {
			sbf.append("<div class=\"xmltreelabel\" >");
			if (this.bulkCheck) {
				sbf.append(
						"<span style=\"cursor:pointer\" onclick=\"P2.xmlTreeExpandAll('")
						.append(this.fieldName).append("');\" >");
				sbf.append("<img border=\"0\" style=\"vertical-align:middle;\" src=\"");
				sbf.append(PageGeneratorContext.imageFolderName).append(
						"plus.gif\" >Expand All</span>");

				sbf.append(
						"&nbsp;&nbsp;&nbsp;&nbsp;<span  style=\"cursor:pointer\" onclick=\"P2.xmlTreeCollapseAll('")
						.append(this.fieldName).append("');\" >");
				sbf.append("<img border=\"0\" style=\"vertical-align:middle;\" src=\"");
				sbf.append(PageGeneratorContext.imageFolderName).append(
						"minus.gif\" >Collapse All</span>");
			}

			if (this.label != null && this.label.length() > 0) {
				sbf.append("&nbsp;").append(this.label);
			}
			sbf.append("</div>");
		}
		sbf.append("<div class=\"xmltree\" id=\"").append(this.fieldName)
				.append("\"></div>");
		sbf.append("</div>");
		return;
	}

	@Override
	void toJs(StringBuilder js, PageGeneratorContext pageContext) {
		this.field.toJs(js, pageContext);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void initialize(PageGeneratorContext context) {
		super.initialize(context);
		this.field = new XmlTreeField();
		this.field.name = this.fieldName;
		this.field.dataElementName = "text";
		this.field.initialize(context);
		this.field.label = null;
		this.field.showValues = this.showValues;
		this.field.expandAllOnLoad = this.expandAllOnLoad;
		this.field.childHtmlAttributes = this.childHtmlAttributes;
		this.elements = new AbstractElement[1];
		this.elements[0] = this.field;
	}

	@Override
	void panelToHtml5(StringBuilder sbf, PageGeneratorContext pageContext) {
		this.panelToHtml(sbf, pageContext);
	}
}

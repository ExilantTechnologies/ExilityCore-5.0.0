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

class XMLGridPanel extends AbstractPanel {

	@SuppressWarnings("deprecation")
	@Override
	void panelToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("<table id=\""
				+ this.name
				+ "Top\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"xmlgridpanelholdertop\">");
		sbf.append("<tr>");
		sbf.append("<td class=\"xmlgridpanelholderleft\">");
		sbf.append("</td>");
		sbf.append("<td class=\"xmlgridpanelholdercenter\">");
		sbf.append("<div class=\"xmlgridpanellabel\" id=\"" + this.name
				+ "Label\">");
		sbf.append(this.label);
		sbf.append("</div>");
		sbf.append("<div class=\"xmlgridpaneldiv\">");
		sbf.append("<table class=\"xmlgridpaneltable\">");
		sbf.append("<tr class=\"xmlgridpaneltablerow\">");
		sbf.append("<td class=\"xmlgridpaneltablesidecell\">");
		sbf.append("<div id=\""
				+ this.elements[0].name
				+ "DivWrapper\" style=\"margin: 0px; overflow: hidden; position: static;\">");
		boolean renderingFieldFlag = pageContext.renderFieldAsColumn;
		pageContext.renderFieldAsColumn = true;
		((XmlTreeField) this.elements[0]).toHtml(sbf, pageContext);
		pageContext.renderFieldAsColumn = renderingFieldFlag;
		sbf.append("</div>");
		sbf.append("</td>");
		sbf.append("<td class=\"xmlgridpaneltablecentercell\">");
		((GridPanel) this.elements[1]).headerRowHtmlAttributes = "style=\"height: 35px;\" ";
		((GridPanel) this.elements[1]).rowHtmlAttributes = "style=\"height: 25px;\" ";
		((GridPanel) this.elements[1]).toHtml(sbf, pageContext);
		sbf.append("</td>");
		sbf.append("<td class=\"xmlgridpaneltablesidecell\">");
		sbf.append("<div id=\""
				+ this.elements[2].name
				+ "DivWrapper\" style=\"margin: 0px; overflow: hidden; position: static;\">");
		((GridPanel) this.elements[2]).headerRowHtmlAttributes = "style=\"height: 25px;\" ";
		((GridPanel) this.elements[2]).rowHtmlAttributes = "style=\"height: 25px;\" ";
		((GridPanel) this.elements[2]).toHtml(sbf, pageContext);
		sbf.append("</div>");
		sbf.append("</td>");
		sbf.append("</tr>");
		sbf.append("</table>");
		sbf.append("</div>");
		sbf.append("<div id=\"" + this.name
				+ "_footer\" class=\"xmlgridpanelcontrolclass\">");
		sbf.append("<span style=\"float: left;\">");
		sbf.append("<img width=\"30px\" height=\"30px\" src=\""
				+ PageGeneratorContext.imageFolderName
				+ "docSource.png\" onclick=\"toggleXMLTree('"
				+ this.elements[0].name + "');\" />");
		sbf.append("</span>");
		sbf.append("<span style=\"float: right;\">");
		sbf.append("<img width=\"30px\" height=\"30px\" src=\""
				+ PageGeneratorContext.imageFolderName
				+ "docAttibutes.png\" onclick=\"toggleAttributeGrid('"
				+ this.elements[2].name + "');\" />");
		sbf.append("</span>");
		sbf.append("</div>");
		sbf.append("</td>");
		sbf.append("<td class=\"xmlgridpanelholderright\">");
		sbf.append("</td>");
		sbf.append("</tr>");
		sbf.append("</table>");
		return;
	}

	@Override
	void panelToHtml5(StringBuilder sbf, PageGeneratorContext pageContext) {
		this.panelToHtml(sbf, pageContext);
	}
}

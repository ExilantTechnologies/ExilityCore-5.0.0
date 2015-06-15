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
 * Row from which row should the data be used as default value for a new row
 * added at run time.
 */
enum rowType {
	none, first, current, next, last
}

/**
 * panel that renders a table of data as output (non-editable)
 * 
 */
class ListPanel extends TablePanel {

	public static final String LINEAR = "linear";
	public static final String DROP_DOWN = "dropDown";

	// should the total number of rows be shown. If you specify this string,
	// then it will be shown as
	// <your string > n where n is the total rows extracted. If you do not
	// specify this string,
	// it is not displayed at all
	String showNbrRowsAs = null;

	// rows in list are highlighted on mouse over, and a selection is
	// highlighted by default.
	// you can disable this if required. Is it disabled in the beginning?
	boolean actionDisabled = false;

	// do you want to enable user to select multiple lines and then act on them
	// on click of a button?
	// this options lets you decide what happens when you click on a row. If
	// multiple select is on, a clicked row will be
	// selected, except if it is already selected, in which case, it gets
	// de-selected.
	// In single select mode, when you select a row, the earlier selected row
	// will be de-seletced.
	boolean multipleSelect = false;

	// what is to be done on click, or dbl click? Note that onClick is already
	// there
	// internal new string onClickActionName = null;
	String onDblClickActionName = null;

	// if there is no data, do you want a message to be displayed instead of an
	// emty table
	String messageNameOnNoData = null;
	String paginateButtonType = AP.paginateButtonType;

	String paginationServiceName = null;
	String paginationServiceFieldNames = null;
	String paginationServiceFieldSources = null;
	String paginateCallback = null;

	public ListPanel() {
		this.tableType = "list";
	}

	@Override
	void addBeforePanel(StringBuilder sbf, PageGeneratorContext pageContext) {
		if (this.messageNameOnNoData != null) {
			String text = "&nbsp;";
			if (this.messageNameOnNoData.length() > 0) {
				text = Messages.getMessageText(this.messageNameOnNoData);
			}
			sbf.append("\n<div id=\"")
					.append(this.tableName)
					.append("NoData\" class=\"nodatamessage\" style=\"display:none;\" >")
					.append(text).append("</div>");
		}
	}

	@Override
	void addBeforeTable(StringBuilder sbf, PageGeneratorContext pageContext) {
		String layoutType = pageContext.getLayoutType();
		if (this.pageSize > 0) {
			if (layoutType.equals("5")) {
				sbf.append("<div id=\"")
						.append(this.tableName)
						.append("PaginationData\" class=\"paginationdatadiv\" >");
				sbf.append("\n<span id=\"").append(this.tableName)
						.append("TotalRows\"></span><span>&nbsp;")
						.append(this.paginationLabel).append("</span>");
				sbf.append("\n</div>");
				sbf.append("\n<div id=\"")
						.append(this.tableName)
						.append("Pagination\" style=\"display:none;\" class=\"paginationdiv\" >");
				sbf.append("<span class=\"activepaginationbutton\" id=\"")
						.append(this.tableName)
						.append("FirstPage\" onclick=\"P2.paginate('")
						.append(this.tableName)
						.append("','first');\"><img alt=\" \" src=\"../../images/arrowstart.png\" style=\"margin-bottom: -3px\"></span>&nbsp;&nbsp;");
				sbf.append("<span class=\"activepaginationbutton\" id=\"")
						.append(this.tableName)
						.append("PrevPage\" onclick=\"P2.paginate('")
						.append(this.tableName)
						.append("','prev');\"><img alt=\" \" src=\"../../images/arrowleft.png\" style=\"margin-bottom: -3px\"></span>&nbsp;&nbsp;");
				sbf.append("<span id=\"")
						.append(this.tableName)
						.append("PageCount\"> Page <input type=\"text\" size=\"2\" onkeydown=\"P2.checkPageValue(this, event);\" id=\"")
						.append(this.tableName)
						.append("CurrentPage\" />&nbsp;of&nbsp;<span id=\"")
						.append(this.tableName)
						.append("TotalPages\"></span>&nbsp;&nbsp;");
				sbf.append("</span>");
				sbf.append("<span class=\"activepaginationbutton\" id=\"")
						.append(this.tableName)
						.append("NextPage\" onclick=\"P2.paginate('")
						.append(this.tableName)
						.append("','next');\"><img alt=\" \" src=\"../../images/arrowright.png\" style=\"margin-bottom: -3px\"></span>&nbsp;&nbsp;");
				sbf.append("<span class=\"activepaginationbutton\" id=\"")
						.append(this.tableName)
						.append("LastPage\" onclick=\"P2.paginate('")
						.append(this.tableName)
						.append("','last');\"><img alt=\" \" src=\"../../images/arrowend.png\" style=\"margin-bottom: -3px\"></span>");
				sbf.append("\n</div>");
			} else {
				sbf.append("<div id=\"")
						.append(this.tableName)
						.append("Pagination\" style=\"display:none;\" class=\"paginationdiv\" >");
				sbf.append("\n<span id=\"")
						.append(this.tableName)
						.append("TotalRows\"></span><span>&nbsp;")
						.append(this.paginationLabel)
						.append("</span>")
						.append("<span id=\"")
						.append(this.tableName)
						.append("PageCount\"> Page <input type=\"text\" size=\"2\" onkeydown=\"P2.checkPageValue(this, event);\" id=\"")
						.append(this.tableName)
						.append("CurrentPage\" />&nbsp;of&nbsp;<span id=\"")
						.append(this.tableName)
						.append("TotalPages\"></span>&nbsp;&nbsp;");
				if (this.paginateButtonType == ListPanel.DROP_DOWN) {
					this.addDropDownPaginateButtons(sbf, pageContext);
				} else {
					this.addLinearPaginateButtons(sbf);
				}
				sbf.append("</span>");
				sbf.append("\n</div>");
			}
		}
	}

	private void addLinearPaginateButtons(StringBuilder sbf) {
		sbf.append("<span class=\"activepaginationbutton\" id=\"")
				.append(this.tableName)
				.append("FirstPage\" onclick=\"P2.paginate('")
				.append(this.tableName)
				.append("','first');\">&lt;&lt;First</span>&nbsp;&nbsp;");
		sbf.append("<span class=\"activepaginationbutton\" id=\"")
				.append(this.tableName)
				.append("PrevPage\" onclick=\"P2.paginate('")
				.append(this.tableName)
				.append("','prev');\">&lt;Prev</span>&nbsp;&nbsp;");
		sbf.append("<span class=\"activepaginationbutton\" id=\"")
				.append(this.tableName)
				.append("NextPage\" onclick=\"P2.paginate('")
				.append(this.tableName)
				.append("','next');\">Next&gt;</span>&nbsp;&nbsp;");
		sbf.append("<span class=\"activepaginationbutton\" id=\"")
				.append(this.tableName)
				.append("LastPage\" onclick=\"P2.paginate('")
				.append(this.tableName)
				.append("','last');\">Last&gt;&gt;</span>");
	}

	private void addDropDownPaginateButtons(StringBuilder sbf,
			PageGeneratorContext pageContext) {
		sbf.append("<span>Pages: </span><select id=\"")
				.append(this.tableName)
				.append("PageSelection\"><option value=\"\"></option><option value=\"first\">First</option><option value=\"prev\">Prev</option><option value=\"next\">Next</option><option value=\"last\">Last</option></select>");

		ButtonElement te = new ButtonElement();
		te.label = "&nbsp;Go&nbsp;";
		te.name = this.tableName + "GoButton";
		te.htmlAttributes = "onclick=\"P2.paginate('" + this.tableName
				+ "', 'go');\" ";
		te.cssClassName = "paginategobutton";
		te.toHtml(sbf, pageContext);
	}

	@Override
	protected void addSubClassSpecificDataRowAttributes(StringBuilder sbf,
			PageGeneratorContext pageContext) {
		String style = "";
		if ((this.actionDisabled == false)
				&& (this.multipleSelect || this.onClickActionName != null || this.onDblClickActionName != null)) {
			style = "cursor:pointer;";
		}
		if (this.rowHeight > 0) {
			style += "height:" + this.rowHeight + "px;";
		}
		if (style.length() > 0) {
			sbf.append("style=\"").append(style).append("\" ");
		}
		sbf.append("onmouseover=\"P2.listMouseOver(this, '")
				.append(this.tableName).append("', event);\" ");
		sbf.append("onmouseout=\"P2.listMouseOut(this, '")
				.append(this.tableName).append("', event);\" ");

		if (this.onDblClickActionName != null) {
			sbf.append("ondblclick=\"P2.listDblClicked(this, '")
					.append(this.tableName).append("', '")
					.append(this.onDblClickActionName).append("');\" ");
		}

		return;
	}

	@Override
	public void initialize(PageGeneratorContext context) {
		this.fieldsAreOutputByDefault = true;
		super.initialize(context);
	}
}

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

class GridPanel extends TablePanel {
	boolean rowsCanBeAdded = false;
	boolean rowsCanBeCloned = false;

	// what is that all important field that a user sees as the key on the grid?
	// This has relevance only if rows can be added. If user adds a row, but
	// leaves all fields empty, then
	// she does not expect us to add that row. keyField is used as THE field
	// that determines if she entered data to this row or not
	String keyFieldName = null;

	// if you use bulk action on the server, you have to track what should be
	// done to each of the row.
	// if a row is not touched, it should not be modified, if it is touched, it
	// should be modified, if it ia added here,
	// it has to be inserted. Exility manages to set the value of the
	// actionFieldName appopriately to take care of this.
	/*
	 * actionField shifted to abstractPanel for generating bulkAction field as
	 * of now
	 */
	// String actionFieldName = null;

	// for example uniqueColumns = "col1,col2;col3"
	String uniqueColumns = null;
	// If you want to specify a custom message for violation of uniqueness.
	String messageIdForUniqueColumns = null;

	int minRows = 0;
	int maxRows = 0;

	boolean sendAffectedRowsOnly = false;
	String functionBeforeAddRow = null;
	String functionAfterAddRow = null;
	String functionBeforeDeleteRow = null;
	String functionAfterDeleteRow = null;

	boolean dataForNewRowToBeClonedFromFirstRow = false; // set to true if you
															// want the newly
															// added row to get
															// data from first
															// row
	rowType dataForNewRowToBeClonedFromRow = rowType.none; // set appropriate
															// value if newly
															// added row has to
															// get data from row
	String newRowColumnsNotToBePopulatedWithData = null; // Column(name)s for
															// which data should
															// not be inserted
															// when a new row is
															// created
	String labelForAddRowButton = null; // Label for the Add Row button
	String hoverForDeleteCheckBox = null;
	boolean doNotDeleteAppendedRows = false;
	boolean confirmOnRowDelete = false; // Apr 07 2009 : Bug 325 - Confirmation
										// message should be shown before
										// deletion of grid items. This should
										// be for a grid level property setting.
										// Not necessarily for all grid. - Exis
										// : Aravinda
	boolean mouseOverRequired = false; // several pages are using grid but look
										// like list to users

	/***
	 * If this has a linked panel, then you can go in for each row to be saved
	 * as and when user edits and moves away from that
	 */
	String autoSaveServiceName = null;

	/***
	 * if autoSaveService feature is used, you can go in for your function to be
	 * called before the service is called. your function is called with (dc,
	 * rowIdx). dc.grids[tableName] would have teh data being sent to server.
	 * you HAVE to return true for the save to continue
	 */
	String functionBeforeAutoSave = null;

	/***
	 * this function is called with (dc, rowIdx) when server returns from an
	 * autoSave call.
	 */
	String functionAfterAutoSave = null;

	public GridPanel() {
		this.tableType = "grid";
	}

	@Override
	void addAfterTable(StringBuilder sbf, PageGeneratorContext pageContext) {
		String layoutType = pageContext.getLayoutType();
		if (layoutType != null && layoutType.equals("5")) {
			sbf.append("<div class=\"addremoveicons\" id=\"")
					.append(this.tableName).append("actiondiv\" >");
			this.addAddRowButton5(sbf);
		} else if (this.rowsCanBeAdded) {
			this.addAddRowButton(sbf, pageContext);
		}
		if (this.rowsCanBeCloned) {
			this.addCloneRowButton(sbf, pageContext);
		}
		if (layoutType != null && layoutType.equals("5")) {
			sbf.append("</div>");
		}
		return;
	}

	@Override
	protected void addSubClassSpecificDataRowAttributes(StringBuilder sbf,
			PageGeneratorContext pageContext) {
		sbf.append(" class=\"gridrow\" ");
		if (this.rowHeight > 0) {
			sbf.append("style=\"height:").append(this.rowHeight)
					.append("px;\" ");
		}
		if (this.mouseOverRequired) {
			sbf.append("onmouseover=\"P2.listMouseOver(this, '")
					.append(this.tableName).append("', event);\" ");
			sbf.append("onmouseout=\"P2.listMouseOut(this, '")
					.append(this.tableName).append("', event);\" ");
		}
		return;
	}

	/*
	 * private void addDeleteCheckBoxInHeader(StringBuilder sbf,
	 * PageGeneratorContext pageContext) { CheckBoxField chk = new
	 * CheckBoxField(); // this field is inserted into the header. There is no
	 * prefix for this field. This field name is not used // anywhere else, and
	 * hence a change to this should not affect any other part of exility.
	 * chk.name = this.tableName + "DeleteAllRows"; chk.numberOfUnitsToUse = 0;
	 * chk.onChangeActionName = "deletAllRows"; chk.fieldToHtml(sbf,
	 * pageContext); }
	 */
	private void addAddRowButton(StringBuilder sbf,
			PageGeneratorContext pageContext) {
		ButtonElement btn = new ButtonElement();
		if (this.labelForAddRowButton != null) {
			btn.label = this.labelForAddRowButton;
			if (pageContext.getLayoutType().equals("3")) {
				btn.imageName = "default";
			}
		} else {
			btn.label = "Add a Row";
		}
		btn.name = this.tableName + "AddRow";
		btn.htmlAttributes = "onclick=\"P2.addTableRow(this, '"
				+ this.tableName + "');\" ";
		btn.toHtml(sbf, pageContext);
	}

	private void addAddRowButton5(StringBuilder sbf) {
		if (this.rowsCanBeAdded) {
			sbf.append("<img class=\"addicon\" alt=\" \" src=\"")
					.append(PageGeneratorContext.imageFolderName)
					.append("addIcon.png\" border=\"0\" ");
			if (this.labelForAddRowButton != null) {
				sbf.append("alt=\"").append(this.labelForAddRowButton)
						.append("\" ");
			}
			sbf.append("style=\"cursor:pointer;\" id=\"");
			sbf.append(this.tableName)
					.append("AddRow\" height=\"15\" style=\"margin-right: 5px; margin-left: 5px;\" ");
			sbf.append("onclick=\"P2.addTableRow(this, '")
					.append(this.tableName).append("');\" />");
		}
		if (this.rowsCanBeDeleted && this.labelForBulkDeleteCheckBox != null) {
			sbf.append("<img class=\"removeicon\" alt=\" \" src=\"")
					.append(PageGeneratorContext.imageFolderName)
					.append("removeIcon.png\" border=\"0\" ");
			sbf.append("style=\"cursor:pointer;\" id=\"");
			sbf.append(this.tableName)
					.append("RemoveRow\" height=\"15\" style=\"margin-right: 5px; margin-left: 5px;\" ");
			sbf.append("onclick=\"P2.removeTableRows(this, '")
					.append(this.tableName).append("');\" />");
		}
	}

	private void addCloneRowButton(StringBuilder sbf,
			PageGeneratorContext pageContext) {
		ButtonElement btn = new ButtonElement();
		btn.label = "Clone this Row";
		btn.name = this.tableName + "CloneRow";
		btn.htmlAttributes = "onmousedown=\"P2.cloneTableRow(this, '"
				+ this.tableName + "');\" ";
		btn.toHtml(sbf, pageContext);
	}
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * common class for list panel and grid panel
 * 
 */
abstract class TablePanel extends DisplayPanel {
	private static final int ONLY_TABLE = 0;
	private static final int LEFT_TABLE = 1;
	private static final int RIGHT_TABLE = 2;
	private static final int SCROLL_TABLE = 3;
	private static final String SPACE = "&nbsp;";
	private static final String SPACER_IMAGE = "<td><img alt=\" \" src=\"../../exilityImages/space.gif\" style=\"width:1px; height:1px;\" /></td>";
	/*
	 * ************* constants for html template parsing and processing
	 * ****************
	 */
	private static final String BEGIN_TABLE = "begin-table-";
	private static final String END_TABLE = "end-table-";
	private static final String BEGIN_TOKEN = "{{";
	static final String END_COMMENT = "-->";
	static final String BEGIN_COMMENT = "<!--";
	private static final String END_TOKEN = "}}";
	private static final String BEGIN_NO_DATA = "begin-no-data";
	private static final String END_NO_DATA = "end-no-data";
	private static final String NO_DATA = "no-data";

	/**
	 * token types
	 */
	private static final int TYPE_INITIAL = 0;
	private static final int TYPE_BEGIN_NO_DATA = 1;
	private static final int TYPE_END_NO_DATA = 2;
	private static final int TYPE_BEGIN_TABLE = 3;
	private static final int TYPE_END_TABLE = 4;
	private static final int TYPE_COLUMN = 5;
	private static final String JS_NAME_FOR_FRAGMENTS = "htmlFragments";

	String tableType = null;
	String idFieldName = null;

	String childTableName = null;
	String childKeysTableName = null;
	String repeatingColumnName = null;

	String rowHtmlAttributes = null;
	String headerRowHtmlAttributes = null;
	boolean allColumnsAreSortable = false;
	boolean allColumnsAreFilterable = false;
	int rowHeight = 0;
	int headerHeight = 0;
	String freezeColumn = null;
	int leftPanelWidth = 0; // if frozen, specify widths
	int rightPanelWidth = 0;
	int frozenColumnIndex = 0; // calculated, 1 based to suit the meaning that 0
								// implies it is not relevant
	private int rightPanelWidthStartIndex = 0;
	boolean showHeader = true;
	boolean isFixedHeight = false;

	String treeViewColumnName = null;
	String treeViewKeyColumn = null;
	String treeViewParentKeyColumn = null;
	String treeViewHasChildColumn = null;

	boolean needFooter = false; // set to true if any columnSum is required
	int heightNumber = 0; // calculated based in height

	String firstFieldName = null;

	// sub-class specific attributes, but added here to simplify header/data row
	// generation.
	boolean addSeqNo = false; // list
	String headerGrouping = null; // Grouping Details of Headers

	String[] headerLevels = null;
	int noOfHeaderLevels = 0;

	boolean doNotShowTreeViewCheckBox = false; // Mar 24 2010 : Tree View Check
												// Box
	// IMPORTANT : doNotResize is set to true in initialize() if freezeColumn is
	// specified
	boolean doNotResize = false;
	int initialNumberOfRows = 0;
	rowType simulateClickOnRow = rowType.none;
	boolean simulateClickOnFirstRow = false;
	// hoverText that is common for all elements has a different meaning for
	// table. It is to be translated into rowHelpText
	String rowHelpText = null;
	/***
	 * Should the user be allowed to move rows up/down? Only move up is
	 * provided. Move down of a row is nothing but move up of the previous row
	 */
	boolean rowsCanBeMoved = false;

	/***
	 * some pages do not look good with no rows in the list/panel. Also, when we
	 * have a linked display panel, page may look ugly if the right panel is
	 * hidden. It is better to keep one empty row always.
	 */
	boolean keepABlankRow = false;

	/***
	 * Odessey wants to display passenger details in a display panel. There may
	 * be several passengers. Hence it is a repeating display panel. However,
	 * there are several journey details for a passenger that repeat. We do not
	 * allow table inside a table. for this purpose, we provide a way to design
	 * the two table independently in the page but while rendering, the
	 * list/grid can be moved within the display panel. This is called 'merging'
	 * a grid/list with a display panel.
	 */
	String mergeWithTableName = null;
	/***
	 * name of an empty panel designed inside the display panel where the
	 * list/grid is to be rendered
	 */
	String stubNameForMerging = null;

	/***
	 * You can associate a quick search field with this table. As you type
	 * characters into that field, Exility will filter rows, like spotLight
	 */
	String quickSearchFieldName = null;

	/***
	 * since filtering is allowed for grid now, we need pagination label also
	 */
	String paginationLabel = "rows selected.";
	/***
	 * Should server send all rows to client, and let client manage pagination?
	 */
	boolean localPagination = false;

	/***
	 * Name of the table that is nested inside this table. Rows are displayed in
	 * a nested way
	 */
	String nestedTableName = null;

	/***
	 * Name of the column in this table that is the foreign key for the nested
	 * table
	 */
	String nestOnColumnName = null;

	/***
	 * Name of the column in the nested table that is the foreign key to link it
	 * to this table
	 */
	String nestedTableColumnName = null;
	/***
	 * Name of the column in the parent table that has the foreign key to link
	 * it to this table
	 */
	String mergeOnColumnName = null;

	/***
	 * Should this be rendered as a display panel
	 */
	boolean renderAsADisplayPanel = false;

	/**
	 * whenever a column with duplicate values is sorted, should it always be
	 * sorted by its, say, key column?
	 */
	String additionalColumnToSort = null;

	/**
	 * this table needs pagination
	 */
	int pageSize;

	@Override
	boolean leaveHeightToMe() {
		return true;
	}

	@Override
	boolean leaveSlidingToMe() {
		return this.frozenColumnIndex > 0;
	}

	@Override
	void panelToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		if (this.name.equals(this.tableName)) {
			String err = "ERROR : Table name of a "
					+ this.tableType
					+ " Panel should be different from its name. Please change "
					+ this.tableType + "  panel " + this.name + ".";
			Spit.out(err);
			pageContext.reportError(err);
			return;
		}
		if (this.elements == null || this.elements.length == 0) {
			String err = "ERROR : Panel " + this.name
					+ " has no child elements.";
			Spit.out(err);
			pageContext.reportError(err);
			return;
		}

		this.addBeforePanel(sbf, pageContext);

		sbf.append("\n<div ");
		if (this.cssClassName == null) {
			sbf.append("class=\"").append(this.tableType).append("panel\" ");
		}

		super.addMyAttributes(sbf, pageContext);
		sbf.append('>');

		String layoutType = pageContext.getLayoutType();
		this.addBeforeTable(sbf, pageContext);
		if (layoutType != null && layoutType.equals("5")) {
			sbf.append("\n<div id=\"").append(this.tableName).append("div\" ");
			if (this.cssClassName == null) {
				sbf.append("class=\"").append(this.tableType)
						.append("paneldiv\" ");
			}
			sbf.append('>');
		}

		if (this.renderAsADisplayPanel) {
			pageContext.setTableName(this.tableName, false);
			sbf.append("\n<div id=\"").append(this.tableName)
					.append("\" class=\"tableTop\" >");
			this.addDataRow(sbf, pageContext, 0, this.elements.length, null);
			sbf.append("</div>");
		} else {
			pageContext.setTableName(this.tableName, true);
			if (this.frozenColumnIndex > 0) {
				this.generateFreezeColumnPanel(sbf, pageContext);
			} else if (this.isFixedHeight) {
				this.fixedHeightPanel(sbf, pageContext, 0,
						this.elements.length, TablePanel.ONLY_TABLE);
			} else {
				this.expandingHeightPanel(sbf, pageContext, 0,
						this.elements.length, TablePanel.ONLY_TABLE);
			}
		}

		pageContext.resetTableName();
		if (layoutType != null && layoutType.equals("5")) {
			sbf.append("\n</div>");
		}
		this.addAfterTable(sbf, pageContext);
		sbf.append("</div>");
		this.addAfterPanel(sbf, pageContext);
		return;
	}

	private void generateFreezeColumnPanel(StringBuilder sbf,
			PageGeneratorContext pageContext) {
		/*
		 * we generate left table for fixed columns, right table for scrolled
		 * columns. In addition, if fixed-height is required, we generate a
		 * scroll-table
		 */
		sbf.append("<table border=\"0\" id=\"")
				.append(this.tableName)
				.append("ContainerTable\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"overflow:hidden;\"><tr>");

		// left panel
		sbf.append("<td style=\"vertical-align:top;\">");
		// 2 pixels for the border
		int rightWidthNumber = this.widthInPixels - this.leftPanelWidth - 2;
		if (this.heightNumber > 0) {
			System.out
					.println("Going to generate fixed header scrolled-tables for "
							+ this.name);
			this.fixedHeightPanel(sbf, pageContext, 0, this.frozenColumnIndex,
					TablePanel.LEFT_TABLE);
			rightWidthNumber = rightWidthNumber - 15; // 1 for scroll table and
														// and 14 for scroll bar
		} else {
			System.out.println("Going to generate expandable-tables for "
					+ this.name);
			this.expandingHeightPanel(sbf, pageContext, 0,
					this.frozenColumnIndex, TablePanel.LEFT_TABLE);
		}

		sbf.append("</td><td style=\"vertical-align:top;\">");
		sbf.append("<div id=\"")
				.append(this.tableName)
				.append("RightContainer\" style=\"overflow-x:scroll; overflow-y:hidden; width:");
		sbf.append(rightWidthNumber).append("px;\">"); // 14 for scroll + 1 for
														// table + 2 for borders
														// for scroll bar
		if (this.heightNumber > 0) {
			this.fixedHeightPanel(sbf, pageContext, this.frozenColumnIndex,
					this.elements.length, TablePanel.RIGHT_TABLE);
		} else {
			this.expandingHeightPanel(sbf, pageContext, this.frozenColumnIndex,
					this.elements.length, TablePanel.RIGHT_TABLE);
		}
		sbf.append("</div></td>");

		// scroll panel if required
		if (this.heightNumber > 0) {
			sbf.append("<td style=\"vertical-align:top;\">");
			this.fixedHeightPanel(sbf, pageContext, 0, 0,
					TablePanel.SCROLL_TABLE);
			sbf.append("</td>");
		}

		sbf.append("</tr></table>");
	}

	// flexibility for sub classes to introduce elements by overriding these
	/**
	 * @param sbf
	 * @param pageContext
	 */
	void addBeforePanel(StringBuilder sbf, PageGeneratorContext pageContext) {
		return;
	}

	/**
	 * @param sbf
	 * @param pageContext
	 */
	void addBeforeTable(StringBuilder sbf, PageGeneratorContext pageContext) {
		return;
	}

	/**
	 * @param sbf
	 * @param pageContext
	 */
	void addAfterTable(StringBuilder sbf, PageGeneratorContext pageContext) {
		return;
	}

	/**
	 * @param sbf
	 * @param pageContext
	 */
	void addAfterPanel(StringBuilder sbf, PageGeneratorContext pageContext) {
		return;
	}

	protected void expandingHeightPanel(StringBuilder sbf,
			PageGeneratorContext pageContext, int startIndex, int endIndex,
			int forTable) {
		int widthToUse = 0;
		String tableNameToUse = this.tableName;
		if (forTable == TablePanel.RIGHT_TABLE) {
			tableNameToUse += "Right";
			widthToUse = this.rightPanelWidth;
		} else if (forTable == TablePanel.LEFT_TABLE) {
			widthToUse = this.leftPanelWidth;
		}
		sbf.append("\n<table id=\"").append(tableNameToUse);
		sbf.append("\" class=\"").append(this.tableType).append("table\" ");
		this.addTableBorderEtc(sbf, pageContext.getLayoutType());
		if (widthToUse != 0) {
			sbf.append(" style=\" width:").append(widthToUse).append("px;\" ");
		}
		sbf.append(">");
		this.addTableHeader(sbf, pageContext, startIndex, endIndex,
				AP.generateColTags ? tableNameToUse : null);
		// we do not want col tags
		this.addDataRow(sbf, pageContext, startIndex, endIndex, null);
		this.addFooterRow(sbf, pageContext, startIndex, endIndex);
		sbf.append("</table>");
		return;
	}

	private void addTableBorderEtc(StringBuilder sbf, String layoutType) {
		if (layoutType.equals("css")) {
			return;
		}
		if (layoutType.equals("2") || layoutType.equals("5")
				|| (layoutType.equals("3") && this.tableType.equals("grid"))) {
			sbf.append(" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" ");
		} else if (this.tableType.equals("grid")) {
			sbf.append(" border=\"1\" ");
		} else {
			sbf.append(" border=\"0\" ");
		}
	}

	protected void fixedHeightPanel(StringBuilder sbf,
			PageGeneratorContext pageContext, int startIndex, int endIndex,
			int forTable) {
		if (this.inError) {
			return;
		}
		String nameToUse = this.tableName;
		String widthToUse = this.width;
		String scrollToUse = "auto";
		boolean addOnScroll = false;
		// forTable == ONLY_TABLE means normal case while others for
		// freeze-column case
		if (forTable == TablePanel.ONLY_TABLE) {
			// default will do
		} else if (forTable == TablePanel.LEFT_TABLE) {
			widthToUse = this.leftPanelWidth + "px";
			scrollToUse = "hidden";
		} else if (forTable == TablePanel.RIGHT_TABLE) {
			nameToUse += "Right";
			widthToUse = this.rightPanelWidth + "px";
			scrollToUse = "hidden";
		} else if (forTable == TablePanel.SCROLL_TABLE) {
			nameToUse += "Scroll";
			widthToUse = "1px";
			scrollToUse = "scroll";
			addOnScroll = true;
		}
		sbf.append(
				"<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" id=\"")
				.append(nameToUse).append("\" class=\"").append(this.tableType)
				.append("table\" ");

		if (widthToUse != null) {
			sbf.append("style=\"width:").append(widthToUse).append(";\" ");
		}
		sbf.append("><tr><td><table ");
		this.addTableBorderEtc(sbf, pageContext.getLayoutType());
		// I would love to add width for all tables. but do not want to take
		// risk with pathFinder behaviour.... Hence adding it only for frozen
		// column case
		if (forTable != TablePanel.ONLY_TABLE) {
			sbf.append(" style=\"width:").append(widthToUse).append("\" ");
		}
		sbf.append(" class=\"").append(this.tableType).append("tableheader\" ");
		String tableNameToUse = nameToUse + "Header";
		sbf.append("id=\"").append(tableNameToUse).append("\" >");
		this.addTableHeader(sbf, pageContext, startIndex, endIndex,
				AP.generateColTags ? tableNameToUse : null);
		sbf.append("</table></td></tr><tr><td><div id=\"").append(nameToUse);
		sbf.append("BodyWrapper\" style=\"height:").append(
				this.heightNumber - this.headerHeight);
		sbf.append("px; overflow-x: hidden; overflow-y:").append(scrollToUse)
				.append("\" ");
		if (addOnScroll) {
			sbf.append(" onscroll=\"document.getElementById('")
					.append(this.tableName)
					.append("BodyWrapper').scrollTop = this.scrollTop; document.getElementById('")
					.append(this.tableName)
					.append("RightBodyWrapper').scrollTop = this.scrollTop;\" ");
		}

		sbf.append("><table ");
		this.addTableBorderEtc(sbf, pageContext.getLayoutType());
		sbf.append(" class=\"").append(this.tableType).append("tablebody\" ");
		if (widthToUse != null) {
			sbf.append("style=\"width:").append(widthToUse).append(";\" ");
		}
		String tableId = nameToUse + "Body";
		sbf.append(" id=\"").append(tableId).append("\">");
		// we have to add col tags

		this.addDataRow(sbf, pageContext, startIndex, endIndex,
				AP.generateColTags ? tableId : null);
		this.addFooterRow(sbf, pageContext, startIndex, endIndex);
		sbf.append("</table></div></td></tr></table>");
	}

	protected void expandingHeightFreezePanel(StringBuilder sbf,
			PageGeneratorContext pageContext, int startIndex, int endIndex) {
		String layoutType = pageContext.getLayoutType();
		sbf.append("<div id=\"").append(this.tableName);
		if (startIndex > 0) {
			sbf.append("RightWrapper\" ");
		} else {
			sbf.append("LeftWrapper\" ");
		}
		if (this.slideEffect.equals(SlideEffect.fromLeft) || startIndex > 0) {
			sbf.append("style=\"margin: 0px; overflow: hidden; position: static;\">");
		} else {
			sbf.append("style=\"margin: 0px;  position: static;\">");
		}

		sbf.append("<div id=\"").append(this.tableName);
		if (startIndex > 0) {
			sbf.append("RightContainer\" style=\"width:")
					.append(this.rightPanelWidth)
					.append("px; overflow-x:auto; overflow-y:hidden;\">");
		} else {
			sbf.append("LeftContainer\" style=\"position:relative; width:")
					.append(this.leftPanelWidth).append("px;\">");
		}

		sbf.append("\n<table id=\"").append(this.tableName);
		if (startIndex > 0) {
			sbf.append("Right\" width=\"").append(this.width).append("\" ");
		} else {
			sbf.append("Left\" ");
		}

		sbf.append("\" class=\"").append(this.tableType).append("table\" ");
		if (this.tableType.equals("list")) {
			if (layoutType.equals("2") || layoutType.equals("5")) {
				sbf.append(" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
			} else {
				sbf.append(" border=\"0\">");
			}
		} else {
			if (layoutType != null
					&& (layoutType.equals("2") || layoutType.equals("3") || layoutType
							.equals("5"))) {
				sbf.append(" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
			} else {
				sbf.append(" border=\"1\">");
			}
		}
		String tableNameToUse = this.tableName
				+ (startIndex > 0 ? "RightHeader" : "LeftHeader");
		sbf.append("<tr><td><table border=\"1\" id=\"").append(tableNameToUse)
				.append("\" >");
		this.addTableHeader(sbf, pageContext, startIndex, endIndex,
				AP.generateColTags ? tableNameToUse : null);
		sbf.append("</table>");
		sbf.append("</td></tr><tr><td><div id=\"").append(this.tableName);
		if (startIndex > 0) {
			sbf.append("RightBodyWrapper\" ").append("style=\"height:")
					.append(this.heightNumber - this.headerHeight)
					.append("px; overflow: hidden;\">");
		} else {
			sbf.append("LeftBodyWrapper\" ")
					.append("style=\"height:")
					.append(this.heightNumber - this.headerHeight)
					.append("px; overflow-x: hidden; overflow-y: auto;\" ")
					.append("onscroll=\"document.getElementById('")
					.append(this.tableName)
					.append("RightBodyWrapper').scrollTop = this.scrollTop;\">");
		}
		String thisTableName = this.tableName
				+ (startIndex > 0 ? "RightBody" : "LeftBody");
		sbf.append("<table border=\"1\" id=\"").append(thisTableName)
				.append("\" >");
		this.addDataRow(sbf, pageContext, startIndex, endIndex,
				AP.generateColTags ? thisTableName : null);
		this.addFooterRow(sbf, pageContext, startIndex, endIndex);
		sbf.append("</table></div></td></tr></table></div></div>");
		return;
	}

	// obsolete. retained as a precaution for PathFinder
	protected void fixedHeightFreezePanel(StringBuilder sbf,
			PageGeneratorContext pageContext, int startIndex, int endIndex) {
		if (this.headerHeight == 0) {
			String msg = "ERROR: You have asked for height of "
					+ this.height
					+ " for list panel "
					+ this.name
					+ " with table name as "
					+ this.tableName
					+ ". To implement this properly, you need to specify headerHeight as well. headerHeight would be the height of the header row. You can get it based on teh css you have used. If you are not sure, start with 21 and then adjust absed on the behaviour of your rendered table";
			Spit.out(msg);
			pageContext.reportError(msg);
			return;
		}

		sbf.append("<div id=\"").append(this.tableName);
		if (startIndex > 0) {
			sbf.append("RightWrapper\" ");
		} else {
			sbf.append("LeftWrapper\" ");
		}
		if (this.slideEffect.equals(SlideEffect.fromLeft) || startIndex > 0) {
			sbf.append("style=\"margin: 0px; overflow: hidden;  position: static;\">");
		} else {
			sbf.append("style=\"margin: 0px; position: static;\">");
		}
		sbf.append("<div id=\"").append(this.tableName);
		if (startIndex > 0) {
			sbf.append("RightContainer\" style=\"width:")
					.append(this.rightPanelWidth).append("; \">");
		} else {
			sbf.append("LeftContainer\" style=\"position:relative; width:")
					.append(this.leftPanelWidth).append(";\">");
		}

		sbf.append("<table border=\"1\" id=\"").append(this.tableName);
		if (startIndex > 0) {
			sbf.append("Right\" width=\"").append(this.width).append("\">");
		} else {
			sbf.append("Left\">");
		}
		String tableNameToUse = this.tableName
				+ (startIndex > 0 ? "RightHeader" : "LeftHeader");
		sbf.append("<tr><td><table border=\"1\" id=\"").append(tableNameToUse)
				.append("\" >");
		this.addTableHeader(sbf, pageContext, startIndex, endIndex,
				AP.generateColTags ? tableNameToUse : null);
		sbf.append("</table>");
		sbf.append("</td></tr><tr><td><div id=\"").append(this.tableName);
		if (startIndex > 0) {
			sbf.append("RightBodyWrapper\" ").append("style=\"height:")
					.append(this.heightNumber - this.headerHeight)
					.append("px; overflow: hidden;\">");
		} else {
			sbf.append("LeftBodyWrapper\" ")
					.append("style=\"height:")
					.append(this.heightNumber - this.headerHeight)
					.append("px; overflow-x: hidden; overflow-y: auto;\" ")
					.append("onscroll=\"document.getElementById('")
					.append(this.tableName)
					.append("RightBodyWrapper').scrollTop = this.scrollTop;\">");
		}

		String thisTableName = this.tableName
				+ (startIndex > 0 ? "RightBody" : "LeftBody");
		sbf.append("<table border=\"1\" id=\"").append(thisTableName)
				.append("\" >");

		// we have to add col tags
		this.addDataRow(sbf, pageContext, startIndex, endIndex,
				AP.generateColTags ? thisTableName : null);
		this.addFooterRow(sbf, pageContext, startIndex, endIndex);
		sbf.append("</table></div></td></tr></table></div></div>");
	}

	/***
	 * Adds HTML for the header for the table
	 * 
	 * @param sbf
	 *            String builder to which text is to be appended
	 * @param pc
	 *            page context
	 * @param startIndex
	 *            COL index to start adding
	 * @param endIndex
	 *            COL index to stop at
	 * @param colTagPrefix
	 *            optional. name prefix for COL tags. null if no COL tags are
	 *            required.
	 */
	protected void addTableHeader(StringBuilder sbf, PageGeneratorContext pc,
			int startIndex, int endIndex, String colTagPrefix) {
		String headerStyle = "";
		String cellStyle = "";
		if (this.headerHeight > 0) {
			if (this.noOfHeaderLevels > 0) {
				headerStyle += "height: "
						+ (this.headerHeight / (this.noOfHeaderLevels + 1))
						+ "px; ";
			} else {
				headerStyle += "height: " + this.headerHeight + "px; ";
			}
		}
		if (this.showHeader == false) {
			headerStyle += "display:none; ";
		}

		if (headerStyle.length() > 0) {
			headerStyle = "style=\"" + headerStyle + "\" ";
		}

		// let us take care of the simplest case : the scroll table that will
		// have just one column
		if (endIndex == 0) // scroll table
		{
			int n = this.noOfHeaderLevels;
			sbf.append("<thead><tr ").append(headerStyle).append('>')
					.append(TablePanel.SPACER_IMAGE).append("</tr>");
			while (n > 1) {
				sbf.append("<tr ").append(headerStyle).append(">")
						.append(TablePanel.SPACER_IMAGE)
						.append("<th>.</th></tr>");
				n--;
			}
			sbf.append("</thead>");
			return;
		}

		StringBuilder hdrSbf = new StringBuilder("<thead>"); // for header

		// for simplicity of algorithm, and at a very small cost, we always
		// build COL tags. use it only if required
		StringBuilder colSbf = new StringBuilder();
		String colTag = "<col id=\"" + colTagPrefix + "Col";
		int nbrTdsAdded = 0;

		hdrSbf.append("<tr ").append(headerStyle).append('>');

		if (startIndex == 0) {
			if (this.addSeqNo) {
				String styleWidth = "style=\"width:30px\" ";
				hdrSbf.append("<th ").append(styleWidth);
				if (this.noOfHeaderLevels > 0) {
					hdrSbf.append(" rowspan=\"")
							.append(this.noOfHeaderLevels + 1).append("\" ");
				}
				hdrSbf.append(">S No</th>");

				// colTag
				nbrTdsAdded++;
				colSbf.append(colTag).append(nbrTdsAdded).append("\" ")
						.append(styleWidth).append(" />");
			}
			if (this.rowsCanBeDeleted && AP.showDeleteOptionAtEnd == false
					&& startIndex == 0) {
				this.addHeaderCheckBox(hdrSbf, pc);

				// colTag
				nbrTdsAdded++;
				colSbf.append(colTag).append(nbrTdsAdded).append("\" />");
			}
			if (this.rowsCanBeMoved) {
				hdrSbf.append("<th id=\"").append(this.name)
						.append("MoveRowLabel\" class=\"cssField\" ");
				if (this.noOfHeaderLevels > 0) {
					hdrSbf.append(" rowspan=\"")
							.append(this.noOfHeaderLevels + 1).append("\" ");
				}
				hdrSbf.append("></th>");

				// colTag
				nbrTdsAdded++;
				colSbf.append(colTag).append(nbrTdsAdded).append("\" />");
			}
		}

		if (this.noOfHeaderLevels > 0) {
			for (int i = 0; i < this.noOfHeaderLevels; i++) {
				String currentLevel = this.headerLevels[i];
				String[] headers = currentLevel.split(",");
				int curIndex = 0;
				for (String currentHeader : headers) {
					if (curIndex < endIndex) {
						String[] currentHeaderDetails = currentHeader
								.split(":");
						curIndex += Integer.parseInt(currentHeaderDetails[0]);
						if ((curIndex > startIndex) && (curIndex <= endIndex)) {
							nbrTdsAdded++;
							hdrSbf.append("<th colspan=\"")
									.append(currentHeaderDetails[0])
									.append("\">");
							hdrSbf.append(currentHeaderDetails[1]);
							hdrSbf.append("</th>");
						}
					}
				}
				hdrSbf.append("</tr><tr ").append(headerStyle).append(">");
			}
		}

		int widthIndex = 0; // implies that width is not to be set
		int totalWidths = 0;
		if (this.columnWidths != null) {
			totalWidths = this.columnWidths.length;
			if (startIndex != 0) {
				widthIndex = this.rightPanelWidthStartIndex;
			}
		}

		for (int i = startIndex; i < endIndex; i++) {
			AbstractElement ele = this.elements[i];
			if (ele == null) {
				Spit.out("Surprising that I have a null element at " + i
						+ " for " + this.name);
				continue;
			}
			if (ele instanceof AbstractPanel) {
				String msg = "ERROR: " + this.name + " is a " + this.tableType
						+ " panel. It has another panel by name " + ele.name
						+ " as its child. This is not valid.";
				Spit.out(msg);
				pc.reportError(msg);
				continue;
			}
			String fullName = this.tableName + '_' + ele.name;
			if (ele.inError) {
				pc.reportError("");
				continue;
			}
			if (ele.numberOfUnitsToUse == 0) {
				continue;
			}

			hdrSbf.append("<th class=\"").append(this.alinementOf(ele))
					.append("\" id=\"");
			hdrSbf.append(fullName).append("Label\" ");

			cellStyle = ""; // set if td has width, and used by ColTag
			String title = null;
			if (this.allColumnsAreSortable || ele.isSortable) {
				title = "Sort";
			}
			if (this.allColumnsAreFilterable || ele.isFilterable) {
				title = (title == null) ? "Filter" : title + " / Filter";
			}

			if (widthIndex < totalWidths) {
				cellStyle = "style=\"width:" + this.columnWidths[widthIndex]
						+ "\" ";
				widthIndex++;
				hdrSbf.append(cellStyle);
			}
			/*
			 * Bug-0074: Table Column is not sorting on click of header. 1)
			 * Removed onclick event from sort and filter image element. 2)
			 * Added onclick event to table header th. 3) Introduced new custom
			 * attribute data-exilImageType for filter and sort image elements.
			 * 4) Add filter criteria also. On 06 sep 13. Bug-0081
			 */
			String layoutType = pc.getLayoutType();
			if (layoutType != null
					&& (layoutType.equals("5") || layoutType.equals("css"))
					&& (this.allColumnsAreSortable || ele.isSortable
							|| this.allColumnsAreFilterable || ele.isFilterable)) {
				hdrSbf.append(" onclick=\"P2.tableHeaderClicked(event, '");
				hdrSbf.append(this.tableName);
				hdrSbf.append("','");
				hdrSbf.append(ele.name);
				hdrSbf.append("');\" ");
			}

			hdrSbf.append('>');
			// Tree View Check Box
			if (this.treeViewColumnName != null
					&& this.treeViewColumnName == ele.name
					&& !this.doNotShowTreeViewCheckBox) {
				hdrSbf.append("<input type=\"checkbox\" title=\"Select/Deselect\" style=\"cursor:pointer;\" id=\"");
				hdrSbf.append(this.tableName).append("BulkTreeCheck\" ");
				hdrSbf.append(
						" onclick=\"P2.treeViewBulkSelectDeselectRow(this, '")
						.append(this.tableName).append("', '").append(ele.name)
						.append("');\" />");
			}

			hdrSbf.append("<div>");

			String lbl = ele.label;
			if (ele instanceof AbstractField) {
				AbstractField field = (AbstractField) ele;
				lbl = field.getLabelToUse(pc);
				if (field.bulkCheck) {
					this.addBulkCheckBoxInHeader(hdrSbf, pc, field.name);
				}
				hdrSbf.append(TablePanel.SPACE);
			}

			if (lbl == null || lbl.length() == 0 || lbl.equals(" ")) {
				hdrSbf.append("&nbsp;");
			} else {
				hdrSbf.append(lbl);
			}

			if (AP.showRequiredLabelinGrid && (ele instanceof AbstractField)
					&& ((AbstractField) ele).isRequired) {
				hdrSbf.append("<span class=\"requiredstar\">*</span>");
			}

			if (this.allColumnsAreSortable || ele.isSortable) {

				hdrSbf.append(
						"<img alt=\" \" title=\"Sort Ascending\" data-exilImageType =\"sort\" id=\"")
						.append(fullName)
						.append("SortImage\" src=\"../../exilityImages/sortable.gif\" style=\"border:0; cursor:pointer;\" ");
				hdrSbf.append("/>");

			}
			if (this.allColumnsAreFilterable || ele.isFilterable) {
				hdrSbf.append(
						"<img alt=\" \" title=\"Filter\" data-exilImageType =\"filter\" id=\"")
						.append(fullName)
						.append("FilterImage\" src=\"../../exilityImages/filterable.gif\" style=\"border:0; cursor:pointer;\" ");
				hdrSbf.append("/>");
			}
			hdrSbf.append("</div>");
			hdrSbf.append("</th>");
			nbrTdsAdded++;
			colSbf.append(colTag).append(nbrTdsAdded).append("\" ")
					.append(cellStyle).append(" />");
		}

		if (this.rowsCanBeDeleted && AP.showDeleteOptionAtEnd
				&& (this.frozenColumnIndex == 0 || startIndex > 0)) {
			this.addHeaderCheckBox(hdrSbf, pc);
			nbrTdsAdded++;
			colSbf.append(colTag).append(nbrTdsAdded).append("\" />");
		}

		// close the header row
		hdrSbf.append("</tr></thead>");
		if (colTagPrefix != null) {
			sbf.append(colSbf);
		}
		sbf.append(hdrSbf.toString());
		return;
	}

	/**
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	protected void addHeaderCheckBox(StringBuilder sbf,
			PageGeneratorContext pageContext) {
		if (this.noOfHeaderLevels > 0) {
			sbf.append("<th rowspan=\"").append(this.noOfHeaderLevels + 1)
					.append("\">");
		} else {
			sbf.append("<th>");
		}
		String layoutType = pageContext.getLayoutType();
		if (layoutType.equals("5") && AP.showIamgeForDeleteOption
				&& this.labelForBulkDeleteCheckBox != null) {
			sbf.append(this.labelForBulkDeleteCheckBox);
		} else {
			if (this.labelForBulkDeleteCheckBox != null) {
				sbf.append(this.labelForBulkDeleteCheckBox).append("<br />");
			}
			if (!layoutType.equals("5")) {
				if (AP.showIamgeForDeleteOption) {
					sbf.append("<img class=\"deleteimg\" src=\"")
							.append(PageGeneratorContext.imageFolderName)
							.append("deleteRow.gif\" border=\"0\" ");
				} else {
					sbf.append("<input type=\"checkbox\" ");
				}
				sbf.append("style=\"cursor:pointer;\" id=\"");
				sbf.append(this.tableName).append("Delete")
						.append("BulkCheck\" ");
				sbf.append("onclick=\"P2.bulkDeleteClicked(this, '")
						.append(this.tableName).append("');\" />");
			}
		}
		sbf.append("</th>");
	}

	protected String alinementOf(AbstractElement ele) {
		if (ele.align != null) {
			return ele.align;
		}

		if (ele instanceof OutputField) {
			OutputField field = (OutputField) ele;
			DataValueType vt = field.getValueType();
			if (vt == DataValueType.INTEGRAL || vt == DataValueType.DECIMAL) {
				return "right";
			}
		}
		return "left";
	}

	/**
	 * 
	 * @param sbf
	 * @param pageContext
	 * @param startIndex
	 * @param endIndex
	 */
	protected void addFooterRow(StringBuilder sbf,
			PageGeneratorContext pageContext, int startIndex, int endIndex) {
		if (this.needFooter == false) {
			return;
		}

		if (endIndex == 0) {
			sbf.append("<tfoot><tr>").append(TablePanel.SPACER_IMAGE)
					.append("</tr></tfoot>");
			return;
		}
		sbf.append("<tfoot id=\"").append(this.tableName)
				.append("Footer\"><tr>");

		if (startIndex == 0) // left or only table
		{
			if (this.addSeqNo) {
				sbf.append("<td style=\"width:30px\">&nbsp;</td>");
			}
			if (this.rowsCanBeDeleted && AP.showDeleteOptionAtEnd == false
					&& startIndex == 0) {
				sbf.append("<td style=\"width:30px\">&nbsp;</td>");
			}
		}

		int totalWidths = 0;
		int widthIndex = 0;
		if (this.columnWidths != null) {
			totalWidths = this.columnWidths.length;
			if (startIndex != 0) {
				widthIndex = this.rightPanelWidthStartIndex;
			}
		}

		for (int i = startIndex; i < endIndex; i++) {
			AbstractElement ele = this.elements[i];
			if (ele.numberOfUnitsToUse > 0 || i == startIndex) {
				if (i != 0) {
					sbf.append("</td>");
				}

				if (this.isFixedHeight) {
					String style = "";
					if (widthIndex < totalWidths) {
						style = "style=\"width:"
								+ this.columnWidths[widthIndex] + ";";
						widthIndex++;
					}

					sbf.append("<td id=\"").append(this.tableName).append('_')
							.append(ele.name).append("Footer\" ")
							.append("class=\"")
							.append(this.columnSumCssClassName).append("\" ")
							.append(style).append("\">");
				} else {
					// Mar 12 2009 : footer element should have option to
					// declare CSS - SAB Miller (Start) : Venkat
					sbf.append("<td id=\"").append(this.tableName).append('_')
							.append(ele.name).append("Footer\" ")
							.append("class=\"")
							.append(this.columnSumCssClassName).append("\" >");
					// Mar 12 2009 : footer element should have option to
					// declare CSS - SAB Miller (End) : Venkat
				}
			}
			if (ele.footerLabel != null) {
				sbf.append(ele.footerLabel);
			} else {
				sbf.append("&nbsp;");
			}
		}
		sbf.append("</td>");
		if (this.rowsCanBeDeleted && AP.showDeleteOptionAtEnd
				&& (this.frozenColumnIndex == 0 || startIndex > 0)) {
			sbf.append("<td>&nbsp;</td>");
		}
		sbf.append("</tr></tfoot>");
		return;
	}

	/***
	 * Add cells (TDs) for data in a table
	 * 
	 * @param sbf
	 *            string builder to which html text is appended
	 * @param pageContext
	 *            page context
	 * @param startIndex
	 *            column index from which to add columns. zero means this is the
	 *            only table or left table in case of frozen columns
	 * @param endIndex
	 *            last column index to be added
	 * @param colTagPrefix
	 *            optional. non-null means col tags are to be added using this
	 *            table name as part of their id
	 */
	protected void addDataRow(StringBuilder sbf,
			PageGeneratorContext pageContext, int startIndex, int endIndex,
			String colTagPrefix) {
		String nestAttr = this.nestedTableName == null ? ""
				: "data-nestedTable=\"true\" ";
		StringBuilder bodySbf = new StringBuilder();
		if (this.renderAsADisplayPanel) {
			bodySbf.append("<div ").append(nestAttr).append("><div ");
		} else {
			bodySbf.append("<tbody ").append(nestAttr).append("><tr ");
		}

		// we decided to attach onclick always...
		bodySbf.append("onclick=\"P2.listClicked(this, '")
				.append(this.tableName).append("', event);\" ");

		if (this.rowHelpText != null) {
			bodySbf.append("title=\"").append(this.rowHelpText).append("\" ");
		}
		this.addSubClassSpecificDataRowAttributes(bodySbf, pageContext);
		bodySbf.append('>');

		String colTags = "";
		if (endIndex == 0) {
			bodySbf.append(TablePanel.SPACER_IMAGE);
		} else {
			if (this.renderAsADisplayPanel) {
				if (pageContext.useHtml5) {
					this.elementsToHtml5(bodySbf, pageContext, false);
				} else {
					this.elementsToHtml(bodySbf, pageContext, false);
				}

			} else {
				colTags = this.addDataRowCells(bodySbf, pageContext,
						startIndex, endIndex, colTagPrefix);
			}
		}
		if (this.renderAsADisplayPanel) {
			bodySbf.append("</div></div>");
		} else {
			bodySbf.append("</tr></tbody>");
		}

		// do we have to add col tags?
		if (colTagPrefix != null) {
			sbf.append(colTags);
		}
		sbf.append(bodySbf.toString());
	}

	/**
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	protected void addSubClassSpecificDataRowAttributes(StringBuilder sbf,
			PageGeneratorContext pageContext) {
		return;
	}

	/***
	 * 
	 * @param sbf
	 *            to which html text is to be appended to
	 * @param pageContext
	 *            pageCOntext
	 * @param startIndex
	 *            index to this.columns[] to start from
	 * @param endIndex
	 *            index to this.columns[] up to which to add
	 * @return html to insert col tag. caller has the option of using this.
	 */
	protected String addDataRowCells(StringBuilder sbf,
			PageGeneratorContext pageContext, int startIndex, int endIndex,
			String colTagPrefix) {
		int nbrAdded = 0;
		StringBuilder colSbf = new StringBuilder();
		String colTag = "<col id=\"" + colTagPrefix + "Col";
		if (startIndex == 0) {
			if (this.addSeqNo) {
				sbf.append("<td><span id=\"").append(this.tableName)
						.append("SeqNo").append("\" ></span></td>");
				nbrAdded++;
				colSbf.append(colTag).append(nbrAdded)
						.append("\" style=\"width:30px\" />");
			}
			if (this.rowsCanBeDeleted && AP.showDeleteOptionAtEnd == false
					&& startIndex == 0) {
				this.addDeleteCheckBox(sbf, pageContext, true);
				nbrAdded++;
				colSbf.append(colTag).append(nbrAdded).append("\" />");
			}

			if (this.rowsCanBeMoved) {
				sbf.append("<td><div class=\"rowUp\" onclick=\"P2.tableRowUp('")
						.append(this.name).append("');\" id=\"")
						.append(this.name).append("RowUp\" ></div></td>");
				nbrAdded++;
				colSbf.append(colTag).append(nbrAdded).append("\" />");
			}
		}
		int totalWidths = 0;
		int widthIndex = 0;
		if (this.columnWidths != null) {
			totalWidths = this.columnWidths.length;
			if (startIndex != 0) {
				widthIndex = this.rightPanelWidthStartIndex;
			}
		}

		for (int i = startIndex; i < endIndex; i++) {
			AbstractElement ele = this.elements[i];

			if (ele.numberOfUnitsToUse != 0 || i == startIndex) {
				if (i != startIndex) {
					sbf.append("</td>");
				}

				String style = "";
				if (this.isFixedHeight) {
					if (widthIndex < totalWidths) {
						style = "style=\"width:"
								+ this.columnWidths[widthIndex] + "\";";
						widthIndex++;
					}

					sbf.append("<td class=\"").append(this.alinementOf(ele));
					sbf.append("\" ").append(style).append("\">");
				} else {
					sbf.append("<td class=\"").append(this.alinementOf(ele))
							.append("\" >");
				}
				nbrAdded++;
				colSbf.append(colTag).append(nbrAdded).append("\" ")
						.append(style).append("/>");
			}
			if (this.treeViewColumnName != null
					&& this.treeViewColumnName.equals(ele.name)) {
				sbf.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
				sbf.append("<td style=\"border:0px\" >");
				sbf.append("<img id=\"")
						.append(ele.name)
						.append("Indent\" ")
						.append(" alt=\" \" src=\"../../images/spacer.gif\" style=\"vertical-align:middle;width:1px;height:16px\" />");
				sbf.append("</td><td style=\"border:0px\" >");
				sbf.append("<img id=\"")
						.append(ele.name)
						.append("PlusMinus\" ")
						.append(" alt=\" \" src=\"../../images/minus.gif\" style=\"cursor:pointer; vertical-align:middle;width:19px;height:16px\" ");
				sbf.append(" onclick=\"P2.treeViewExpandCollapseRow(this, '")
						.append(this.tableName).append("', '").append(ele.name)
						.append("');\" />");
				sbf.append("</td><td style=\"border:0px\" >");
				// Mar 24 2010 : Tree View Check Box
				if (this.doNotShowTreeViewCheckBox) {
					sbf.append("&nbsp;");
				} else {
					sbf.append("<input type=\"checkbox\" title=\"Select/Deselect\" style=\"cursor:pointer;\" id=\"");
					sbf.append(ele.name).append("Select\" ");
					sbf.append(
							" onclick=\"P2.treeViewSelectDeselectRow(this, '")
							.append(this.tableName).append("', '")
							.append(ele.name).append("');\" />");
				}
				// Mar 24 2010 : Tree View Check Box
				sbf.append("</td><td style=\"border:0px\" >");
			}

			if (ele instanceof CheckBoxField
					&& pageContext.getLayoutType().equals("5")) {
				ele.label = "";
			}

			if (ele instanceof AbstractField) {
				if (pageContext.useHtml5) {
					((AbstractField) ele).fieldToHtml5(sbf, pageContext);
				} else {
					((AbstractField) ele).fieldToHtml(sbf, pageContext);
				}
			} else {
				if (pageContext.useHtml5) {
					ele.toHtml5(sbf, pageContext);
				} else {
					ele.toHtml(sbf, pageContext);
				}

			}
			if (this.treeViewColumnName != null
					&& this.treeViewColumnName.equals(ele.name)) {
				sbf.append("</td></tr></table>");
			}
		}
		sbf.append("</td>");
		if (this.rowsCanBeDeleted && AP.showDeleteOptionAtEnd
				&& (this.frozenColumnIndex == 0 || startIndex > 0)) {
			this.addDeleteCheckBox(sbf, pageContext, true);
			nbrAdded++;
			colSbf.append(colTag).append(nbrAdded).append("\" />");
		}
		return colSbf.toString();
	}

	// private void addTreeViewPrefixForCell(StringBuilder sbf,
	// PageGeneratorContext pageContext, String eleName)
	// {
	// //sbf.Append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
	// //sbf.Append("<td style=\"border:0px\" >");
	// sbf.append("<img id=\"").append(eleName).append("Indent\" ").append(" alt=\" \" src=\"../../images/spacer.gif\" style=\"vertical-align:middle;width:1px;height:16px\" />");
	// //sbf.Append("</td><td style=\"border:0px\" >");
	// sbf.append("<img id=\"").append(eleName).append("PlusMinus\" ").append(" alt=\" \" src=\"../../images/minus.gif\" style=\"cursor:pointer; vertical-align:middle;width:19px;height:16px\" ");
	// sbf.append(" onclick=\"P2.treeViewExpandCollapseRow(this, '").append(this.tableName).append("', '").append(eleName).append("');\" />");
	// //sbf.Append("</td><td style=\"border:0px\" >");
	// // Mar 24 2010 : Tree View Check Box
	// if (!this.doNotShowTreeViewCheckBox)
	// {
	// sbf.append("<input type=\"checkbox\" title=\"Select/Deselect\" style=\"cursor:pointer;\" id=\"");
	// sbf.append(eleName).append("Select\" ");
	// sbf.append(" onclick=\"P2.treeViewSelectDeselectRow(this, '").append(this.tableName).append("', '").append(eleName).append("');\" />");
	// }
	// //sbf.Append("</td><td style=\"border:0px\" >");
	// }

	/**
	 * 
	 * @param sbf
	 * @param pageContext
	 * @param fieldName
	 */
	private void addBulkCheckBoxInHeader(StringBuilder sbf,
			PageGeneratorContext pageContext, String fieldName) {
		sbf.append("\n<input type=\"checkbox\" id=\"").append(this.tableName)
				.append('_').append(fieldName)
				.append("Header\" onclick=\"P2.bulkCheckAction(this, '")
				.append(fieldName).append("', '").append(this.tableName)
				.append("');\" />");
	}

	@Override
	public void initialize(PageGeneratorContext context) {
		super.initialize(context);
		if (this.hoverText != null) {
			this.rowHelpText = this.hoverText;
			this.hoverText = null;
		}
		if (this.elements == null) {
			return;
		}
		if (this.height != null) {
			this.heightNumber = this.parsePixels(this.height);
			if (this.heightNumber <= 0) {
				Spit.out("Height of a list/grid panel should be expressed as an integer. It is always assumed to be in pixels. Please change it. Va value of 400 is assumed for this trial.");
				this.heightNumber = 400;
			}
		}
		int firstFieldIndex = -1;
		int lastFieldIndex = -1;
		boolean firstFieldFound = false;
		boolean foundFieldWithRowAverage = false;
		boolean foundFieldWithRowSum = false;
		boolean foundRowAverageField = false;
		boolean foundRowSumField = false;

		int wi = 0;
		for (int i = 0; i < this.elements.length; i++) {
			AbstractElement e = this.elements[i];

			if (e.numberOfUnitsToUse != 0) {
				wi++;
			}
			if (e instanceof AbstractField) {
				AbstractField f = (AbstractField) e;
				if (f.columnAverage || f.columnSum || f.footerLabel != null) {
					this.needFooter = true;
				}
				if (f.rowAverage) {
					foundFieldWithRowAverage = true;
				}
				if (f.rowSum) {
					foundFieldWithRowSum = true;
				}
				if (e.name.equals("rowAverage")) {
					foundRowAverageField = true;
				}
				if (e.name.equals("rowSum")) {
					foundRowSumField = true;
				} else {
					if ((f instanceof AbstractInputField)
							&& (f instanceof HiddenField == false)) {
						lastFieldIndex = i;
						if (!firstFieldFound) {
							firstFieldIndex = i;
							firstFieldFound = true;
						}
					}
				}
			}
			if (this.freezeColumn != null && this.freezeColumn.equals(e.name)) {
				this.frozenColumnIndex = i + 1;
				this.rightPanelWidthStartIndex = wi;
			}
		}

		if (firstFieldIndex != -1) {
			this.firstFieldName = this.elements[firstFieldIndex].name;
		}
		if (lastFieldIndex != -1) {
			((AbstractInputField) this.elements[lastFieldIndex]).setLastField();
		}

		if (foundFieldWithRowSum && !foundRowSumField) {
			context.reportError("ERROR: "
					+ this.name
					+ " uses row sum, but has not defined a field with name = rowSum");
		}
		if (foundFieldWithRowAverage && !foundRowAverageField) {
			context.reportError("ERROR: "
					+ this.name
					+ " uses row average, but has not defined a field with name = rowAverage");
		}
		if (this.height != null
				&& (this.rowHeight == 0 || this.headerHeight == 0)) {
			context.reportError("ERROR: "
					+ this.name
					+ " specifies height as "
					+ this.height
					+ ". for fixed header table to work, you shoudl provide both headerHeight and rowHeight in pixles.");
		}
		if (this.freezeColumn != null) {
			this.doNotResize = true;
			/*
			 * we do not want resize algorithm to run on tables that are managed
			 * thru freeze-column design
			 */
			if (this.frozenColumnIndex == 0 || this.leftPanelWidth == 0
					|| this.rightPanelWidth == 0 || this.widthInPixels == 0) {
				context.reportError("ERROR: "
						+ this.name
						+ " specifies "
						+ this.freezeColumn
						+ " to be frozen. For this feature to work, that column name shoudl exist, and you shoudl specify values for width, leftPanelWidth and rightPanelWidth in pixels;");
			}
		}

		if (this.headerGrouping != null) {
			this.headerLevels = this.headerGrouping.split(";");
			this.noOfHeaderLevels = this.headerLevels.length;
		}
		return;

	}

	@Override
	void panelToHtml5(StringBuilder sbf, PageGeneratorContext pageContext) {
		if (this.name.equals(this.tableName)) {
			String err = "ERROR : Table name of a "
					+ this.tableType
					+ " Panel should be different from its name. Please change "
					+ this.tableType + "  panel " + this.name + ".";
			Spit.out(err);
			pageContext.reportError(err);
			return;
		}
		if (this.elements == null || this.elements.length == 0) {
			String err = "ERROR : Panel " + this.name
					+ " has no child elements.";
			Spit.out(err);
			pageContext.reportError(err);
			return;
		}

		this.addBeforePanel(sbf, pageContext);

		sbf.append("\n<div ");
		if (this.cssClassName == null) {
			sbf.append("class=\"").append(this.tableType).append("Panel\" ");
		}

		super.addMyAttributes(sbf, pageContext);
		sbf.append('>');

		this.addBeforeTable(sbf, pageContext);

		if (this.renderAsADisplayPanel) {
			pageContext.setTableName(this.tableName, false);
			sbf.append("\n<div id=\"").append(this.tableName)
					.append("\" class=\"tableTop\" >");
			this.addDataRow(sbf, pageContext, 0, this.elements.length, null);
			sbf.append("</div>");
		} else {
			pageContext.setTableName(this.tableName, true);
			if (this.frozenColumnIndex > 0) {
				this.generateFreezeColumnPanel(sbf, pageContext);
			} else if (this.isFixedHeight) {
				this.fixedHeightPanel(sbf, pageContext, 0,
						this.elements.length, TablePanel.ONLY_TABLE);
			} else {
				this.expandingHeightPanel(sbf, pageContext, 0,
						this.elements.length, TablePanel.ONLY_TABLE);
			}
		}
		pageContext.resetTableName();
		this.addAfterTable(sbf, pageContext);
		sbf.append("</div>");
		this.addAfterPanel(sbf, pageContext);
		return;
	}

	@Override
	protected String formatHtml(String html, PageGeneratorContext context) {
		List<FragmentToken> tokens = this.tokenize(html, context);
		if (tokens == null) {
			return "Invalid template provided for this panel";
		}
		if (tokens.size() == 1) {
			// this is not a template at all..
			return html;
		}

		StringBuilder sbf = new StringBuilder("<div id=\"");
		sbf.append(this.tableName).append("\">\n");
		/**
		 * remember we are guaranteed to have at least two entries first one is
		 * ALWAYS initial text, second one is possibly begin-no-data
		 */
		FragmentToken firstToken = tokens.get(0);
		FragmentToken secondToken = tokens.get(1);
		if (secondToken.type == 1) {
			if (tokens.size() < 3 || tokens.get(2).type != 2) {
				String err = "Missing or misplaced closing tag " + END_NO_DATA
						+ " in the template file";
				Spit.out(err);
				context.reportError(err);
				return err;
			}

			if (firstToken.fragmentText.length() > 0) {
				Spit.out("Initial text before " + BEGIN_NO_DATA
						+ " will be ignored in panel " + this.name);
			}
			sbf.append(secondToken.fragmentText);
		} else {
			/**
			 * validate whether no-data section is taken care of
			 */
			for (int i = 2; i < tokens.size(); i++) {
				if (tokens.get(i).type == 1) {
					String err = BEGIN_NO_DATA
							+ " is found after other tags. This is invalid. It can only be out at the beginning of the template";
					Spit.out(err);
					context.reportError(err);
					return err;
				}
			}
		}
		sbf.append("\n</div>");
		return sbf.toString();
	}

	@Override
	protected String getJsForTemplate(String html, PageGeneratorContext context) {
		List<FragmentToken> tokens = this.tokenize(html, context);
		if (tokens == null) {
			String err = "Invalid template provided for this panel";
			Spit.out(err);
			context.reportError(err);
			return null;
		}
		if (tokens.size() == 1) {
			// this is not a template at all..
			return null;
		}

		Stack<String> tables = new Stack<String>();
		StringBuilder sbf = new StringBuilder('\n');
		sbf.append(Page.JS_VAR_NAME).append('.').append(JS_NAME_FOR_FRAGMENTS)
				.append(" = [");
		boolean noDataSectionFound = false;
		for (int i = 0; i < tokens.size(); i++) {
			FragmentToken token = tokens.get(i);
			switch (token.type) {
			case TYPE_BEGIN_NO_DATA:
				if (i != 1) {
					String err = BEGIN_NO_DATA
							+ " is to be right at the beginning of teh template";
					Spit.out(err);
					context.reportError(err);
					return null;
				}
				noDataSectionFound = true;
				break;
			case TYPE_END_NO_DATA:
				if (i != 2) {
					String err = END_NO_DATA
							+ " does not match its opening tab "
							+ BEGIN_NO_DATA;
					Spit.out(err);
					context.reportError(err);
					return null;
				}
				noDataSectionFound = false;
				break;

			case TYPE_BEGIN_TABLE:
				if (token.token == null || token.token.length() == 0) {
					String err = "Invalid table name for " + BEGIN_TABLE
							+ " token.";
					Spit.out(err);
					context.reportError(err);
					return null;
				}
				tables.push(token.token);
				break;

			case TYPE_END_TABLE:
				if (tables.size() == 0) {
					String err = END_TABLE + token.token
							+ " found with no corresponding " + END_TABLE
							+ token.token;
					Spit.out(err);
					context.reportError(err);
					return null;
				}

				String tblName = tables.pop();

				if (tblName.equals(token.token) == false) {
					String err = END_TABLE + token.token
							+ " found as the closing tag for " + BEGIN_TABLE
							+ tblName + ". Mismatched table tags";
					Spit.out(err);
					context.reportError(err);
					return null;
				}
				break;

			default:
				// nothing to check
			}
			if (i > 0) {
				sbf.append("\n,");
			}
			String htmlText = token.fragmentText.replace('\n', ' ');
			htmlText = htmlText.replace("'", "\\'");
			htmlText = htmlText.replace("\r", "");
			sbf.append('[').append(token.type).append(",'").append(token.token)
					.append("','").append(htmlText).append("']");
		}
		/**
		 * any open tags??
		 */
		if (tables.size() > 0) {
			String err = BEGIN_TABLE + tables.pop()
					+ " found with no corresponding " + END_TABLE;
			Spit.out(err);
			context.reportError(err);
			return null;
		}

		if (noDataSectionFound) {
			String err = BEGIN_NO_DATA + " does not have a matching close tab "
					+ END_NO_DATA;
			Spit.out(err);
			context.reportError(err);
			return null;
		}
		sbf.append("];\n");
		return sbf.toString();
	}

	/**
	 * we expect the string to be of the form <br />
	 * initial text {{begin-table-table1}}..... {{1}}.... {{2}}
	 * {{begin-table-childTable}} {{1}}...{{2}}...{{end-table-table1}}..{{3}}..
	 * {{end-table-table1}} some-more-text We call the text between two tokens
	 * as fragment. If there are n tokens, there will be n+1 fragments. We put
	 * initial text as 0th fragment, making it one fragment per token after that
	 * 
	 * We parse and store an array of n+1 rows, with the following fields
	 * <ul>
	 * <li>tokenType : 0 - initial, 1 - begin-table, 2 - end-table, 3 -
	 * columnNumber</li>
	 * <li>tokenName : tableName/columnNumber</li>
	 * <li>fragmentAfterToken</li>
	 * </ul>
	 * 
	 * @param text
	 *            - text to be parsed into tokens position to start parsing at
	 * @param context
	 * @return list of tokens
	 */
	private List<FragmentToken> tokenize(String text,
			PageGeneratorContext context) {
		List<FragmentToken> tokens = new ArrayList<FragmentToken>();
		int posn = text.indexOf(BEGIN_TOKEN);
		if (posn == -1) {
			posn = text.length();
		}
		/**
		 * first token is for initial fragment
		 */
		String fragment = text.substring(0, posn);
		fragment = this.trimCommentedEdges(fragment);
		tokens.add(new FragmentToken(TYPE_INITIAL, null, fragment));

		while (posn < text.length()) {
			// posn is pointing to the beginning of BEGIN_TOKEN
			int startAt = posn;
			posn = text.indexOf(END_TOKEN, posn);
			if (posn == -1) {
				String err = "Missing closing marker " + END_TOKEN;
				Spit.out(err);
				context.reportError(err);
				return null;
			}
			String token = text.substring(startAt + BEGIN_TOKEN.length(), posn);
			token = token.trim();
			int tokenType = TYPE_COLUMN;
			if (token.startsWith(BEGIN_TABLE)) {
				token = token.substring(BEGIN_TABLE.length());
				tokenType = TYPE_BEGIN_TABLE;
			} else if (token.startsWith(END_TABLE)) {
				token = token.substring(END_TABLE.length());
				tokenType = TYPE_END_TABLE;
			} else if (token.startsWith(BEGIN_NO_DATA)) {
				token = NO_DATA;
				tokenType = TYPE_BEGIN_NO_DATA;
			} else if (token.startsWith(END_NO_DATA)) {
				token = NO_DATA;
				tokenType = TYPE_END_NO_DATA;
			}
			/**
			 * let us move to the end of this token, and get the fragment
			 * between end of this token and beginning of next token
			 */
			startAt = posn + END_TOKEN.length();
			posn = text.indexOf(BEGIN_TOKEN, startAt);
			if (posn == -1) {
				posn = text.length();
			}
			fragment = this.trimCommentedEdges(text.substring(startAt, posn));
			tokens.add(new FragmentToken(tokenType, token, fragment));
		}
		return tokens;
	}

	/**
	 * trims possible "-->" in the beginning or "<--" at the end of token
	 * 
	 * @param text
	 * @return
	 */
	private String trimCommentedEdges(String text) {
		String fragmentText = text;
		if (fragmentText.startsWith(TablePanel.END_COMMENT)) {
			fragmentText = fragmentText.substring(TablePanel.END_COMMENT
					.length());
		}
		if (fragmentText.endsWith(TablePanel.BEGIN_COMMENT)) {
			fragmentText = fragmentText.substring(0, fragmentText.length()
					- TablePanel.BEGIN_COMMENT.length());
		}
		return fragmentText;
	}
}

/**
 * data structure to keep info about a token
 */

class FragmentToken {

	final int type;
	final String token;
	final String fragmentText;

	FragmentToken(int tokenType, String tokenName, String fragmentText) {
		this.type = tokenType;
		this.token = tokenName;
		this.fragmentText = fragmentText;
	}
}

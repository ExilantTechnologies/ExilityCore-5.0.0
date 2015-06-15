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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * base class for all panels in a page
 * 
 */
abstract class AbstractPanel extends AbstractElement {
	private static final String HTML_BEFORE_PANEL = " border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td width=\"10px\" >"
			+ "<img width=\"10px\"  src=\""
			+ PageGeneratorContext.imageFolderName
			+ "b2TopLeft.gif\"/></td><td background=\""
			+ PageGeneratorContext.imageFolderName
			+ "b2Top.gif\"><img src=\""
			+ PageGeneratorContext.imageFolderName
			+ "b2Top.gif\" /></td>"
			+ "<td  width=\"10px\"><img src=\""
			+ PageGeneratorContext.imageFolderName
			+ "b2TopRight.gif\" /></td></tr>"
			+ "<tr><td background=\""
			+ PageGeneratorContext.imageFolderName
			+ "b2Left.gif\" valign=\"top\" width=\"10px\"><img src=\""
			+ PageGeneratorContext.imageFolderName
			+ "b2Left.gif\" width=\"10\" valign=\"top\"/></td>"
			+ "<td bgcolor=\"#f6f6f6\" valign=\"top\">";
	private static final String HTML_AFTER_PANEL = "</td><td width=\"10px\" valign=\"top\" background=\""
			+ PageGeneratorContext.imageFolderName
			+ "b2Right.gif\"><img width=\"10px\" src=\""
			+ PageGeneratorContext.imageFolderName
			+ "b2Right.gif\"/></td></tr>"
			+ "<tr><td><img width=\"10px\"  src=\""
			+ PageGeneratorContext.imageFolderName
			+ "b2BottomLeft.gif\"/></td><td background=\""
			+ PageGeneratorContext.imageFolderName
			+ "b2Bottom.gif\"><img src=\""
			+ PageGeneratorContext.imageFolderName
			+ "b2Bottom.gif\" /></td>"
			+ "<td  width=\"10px\"><img src=\""
			+ PageGeneratorContext.imageFolderName
			+ "b2BottomRight.gif\" /></td></tr>" + "</table>";

	private static final String HTML_BEFORE_PANEL3 = " border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td width=\"10px\" colspan=\"2\">"
			+ "<img src=\""
			+ PageGeneratorContext.imageFolderName
			+ "b2TopLeft.gif\"/></td><td class=\"b2Top\"></td>"
			+ "<td  width=\"10px\" colspan=\"2\"><img src=\""
			+ PageGeneratorContext.imageFolderName
			+ "b2TopRight.gif\" /></td></tr>"
			+ "<tr><td class=\"b2Left\" valign=\"top\" width=\"1px\"></td><td class=\"b2Fill\" valign=\"top\" width=\"9px\"></td>"
			+ "<td valign=\"top\" class=\"panelholder\">";
	private static final String HTML_AFTER_PANEL3 = "</td><td width=\"9px\" valign=\"top\" class=\"b2Fill\"></td><td width=\"1px\" valign=\"top\" class=\"b2Right\"></td></tr>"
			+ "<tr><td width=\"10px\" colspan=\"2\"><img src=\""
			+ PageGeneratorContext.imageFolderName
			+ "b2BottomLeft.gif\"/></td><td class=\"b2Bottom\"></td>"
			+ "<td  width=\"10px\" colspan=\"2\"><img src=\""
			+ PageGeneratorContext.imageFolderName
			+ "b2BottomRight.gif\" /></td></tr>" + "</table>";
	private static final String RIGHT_ARROW = "<img border=\"0\" src=\""
			+ PageGeneratorContext.imageFolderName + "right.gif\" >";
	private static final String LEFT_ARROW = "<img border=\"0\" src=\""
			+ PageGeneratorContext.imageFolderName + "left.gif\" >";

	// js attributes across all classes.
	static final String[] ALL_META_ATTRIBUTES = {
			"addSeqNo",
			"rowsCanBeAdded",
			"initialNumberOfRows",
			"multipleSelect",
			// "numberOfColumnsToRepeat",
			"onClickActionName", "onDblClickActionName", "pageSize",
			"paginateButtonType", "renderingOption", "rowsCanBeCloned",
			"showNbrRowsAs", "showHeader", "simulateClickOnFirstRow",
			"simulateClickOnRow", "sendAffectedRowsOnly", "tableName",
			"childTableName", "childKeysTableName", "repeatingColumnName",
			"qtyBySize", "minRows", "maxRows", "elderBrother",
			"youngerBrother", "repeatingPanelName", "uniqueColumns",
			"functionBeforeAddRow", "functionAfterAddRow",
			"functionBeforeDeleteRow", "functionAfterDeleteRow",
			"actionDisabled", "dataForNewRowToBeClonedFromFirstRow",
			"dataForNewRowToBeClonedFromRow",
			"newRowColumnsNotToBePopulatedWithData",
			"columnNosHavingGroupedRadioButton", "treeViewColumnName",
			"treeViewKeyColumn", "treeViewParentKeyColumn",
			"treeViewHasChildColumn", "rowsCanBeDeleted", "frozenColumnIndex",
			"slideEffect", "allColumnsAreSortable", "allColumnsAreFilterable",
			"doNotDeleteAppendedRows", "confirmOnRowDelete", "headerGrouping",
			"isFixedHeight", "childHtmlAttributes",
			"doNotShowTreeViewCheckBox", "paginationServiceName",
			"paginationServiceFieldNames", "paginationServiceFieldSources",
			"paginateCallback", "doNotResize", "rowHelpText",
			"quickSearchFieldName", "linkedTableName", "rowsCanBeMoved",
			"keepABlankRow", "mergeWithTableName", "stubNameForMerging",
			"localPagination", "nestedTableName", "nestOnColumnName",
			"nestedTableColumnName", "mergeOnColumnName", "markedAsComment",
			"messageIdForUniqueColumns", "renderAsADisplayPanel",
			"autoSaveServiceName", "functionABeforeAutoSave",
			"functionAfterAutoSave", "useTableLayout",
			"additionalColumnToSort",
			/*
			 * used by chartPanel
			 */
			"chartType", "onClickFunctionName", "onMoveFunctionName",
			"xAxisColumn", "yAxisColumn", "helpTextColumn",
			"groupHelpTextColumn", "bubbleColumn", "groupByColumn",
			"bulletLabelColumn", "fromColumn", "toColumn",
			"distributionValueColumn", "coreColumn", "level1Column",
			"level2Column", "valueOfInterest", "comparativeValue",
			"firstQualitativeRange", "secondQualitativeRange", "xAxisLabel",
			"yAxisLabel", "labelColor", "xLabelFormatterFunction",
			"yLabelFormatterFunction", "minPercentToShowLabel", "minX", "minY",
			"maxX", "maxY", "bubbleRadiusDenominator", "labelLeftMargin",
			"labelBottomMargin", "colors", "childColors", "shadowSize",
			"rawDataDisplay", "hideLegend", "legendNbrColumns",
			"legendLabelFormatter", "legendLabelBoxBorderColor",
			"legendPosition", "legendMargin", "legendBackgroundColor",
			"legendBackgroundOpacity", "marginLeft", "marginBottom",
			"barLabelLeftMargin", "barLabelBottomMargin", "yLabelMaxWidth",
			"lineWidth", "barWidth", "gridColor", "tickColor",
			"showFilledPoints", "pointsRadius", "pointsFillColor" };
	static final String[] ALL_TABLE_SENSITIVE_ATTRIBUTES = { "actionFieldName",
			"idFieldName", "keyFieldName", "repeatedFieldName",
			"repeatOnFieldName", "labelFieldName", "firstFieldName" };

	boolean requiresGroupOutline = false;
	boolean isCollapsible = false;
	boolean noBorder = false; // don't need border for display/input panel (used
								// in iframes).
	int elementsPerRow = 1;
	String tabLabel = null;
	String tableName = null;
	String repeatOnFieldName = null;
	String labelFieldName = null;
	String repeatingPanelName = null;
	String elderBrother = null;
	String youngerBrother = null;
	String columnSumCssClassName = "field";
	String buttonLabel = null;
	SlideEffect slideEffect = SlideEffect.none;
	String tabIconImage = null;
	/**
	 * is there a hand crafted html for this panel. Designer can opt for her own
	 * html instead of we generating a run-of-the-mill one :-) <br />
	 * File name is relative to resource/page folder. that is, if you set this
	 * value to a.b.c.htm, we look for resource/page/a/b/c.htm
	 */
	String htmlFileName = null;

	/**
	 * create a simple html, with no wrapper, no table etc.. Applicable only for
	 * html5 (pageType = css)
	 */
	boolean keepItSimple = false;

	/**
	 * original design was to put a wrapper always. With not many takers for
	 * panel label and twistie, wrapper div could be unnecessary
	 */
	boolean doNotPutAWrapperDiv = false;
	/**
	 * name of record to use as reference for table as well as fields in this
	 * panel. Note that you can not use tableName as well as recordName.
	 */
	String recordName;

	/**
	 * use the following fields from the record that is defined with the same
	 * name as tableName. "all" means use all fields. Once this is specified,
	 * you should not use fieldsToSkip, nor should you use elements collection
	 * to enumerate fields.
	 */
	String[] fieldsToShow = null;
	/**
	 * comma separated field names to be skipped from the record. Use this if
	 * fields to skip is a smaller list than fieldsToShow. You should not
	 * confuse me by specifying both.
	 */
	String[] fieldsToSkip = null;

	/**
	 * fields that are to be put into model that are available to script, but
	 * not to html. this list should not have field names that already appear in
	 * other lists.
	 */
	String[] fieldsToHide = null;

	/**
	 * if fields are auto-generated using a record, should they be assumed to be
	 * output fields or input fields?
	 */
	boolean fieldsAreOutputByDefault = false;

	/**
	 * this is actually an attribute of gridPanel only. This is set thru schema
	 * for xsd. For the time being, we have added at this level to take care of
	 * an issue in adding bulk action field to grid from record.
	 */
	String actionFieldName = null;
	AbstractElement[] elements = null;

	AbstractPanel() {
	}

	/**
	 * render is implemented at abstract level, with some additional
	 * flexibilities for sub-classes. whether the height will be managed by
	 * sub-class
	 * 
	 * @return true if abstract class should not add height attribute
	 */
	boolean leaveHeightToMe() {
		return false;
	}

	/**
	 * sliding could be managed by the sub-class. Specifically for XML panel
	 * 
	 * @return true if sliding should not be rendered at abstract level
	 */
	boolean leaveSlidingToMe() {
		return false;
	}

	@Override
	void addMyAttributes(StringBuilder sbf, PageGeneratorContext pageContext) {
		if (this.name != null) {
			sbf.append("id=\"").append(this.name).append("\" ");
		}

		if (this.cssClassName != null) {
			sbf.append("class=\"").append(this.cssClassName).append("\" ");
		}

		if (this.htmlAttributes != null) {
			sbf.append(' ').append(this.htmlAttributes).append(' ');
		}
		String style = "";
		if (this.width != null) {
			style += "width:" + this.width + ';';
		}

		if (this.height != null && this.leaveHeightToMe() == false) {
			style += "overflow-y:auto; height:" + this.height + ';';
		} else if (this.slideEffect.equals(SlideEffect.fromLeft)) {
			style += "overflow:hidden;";
		}

		if (this.hidden) {
			style += "display:none;";
		}

		if (this.align != null && this.getClass().equals(ButtonPanel.class)
				&& AP.alignPanels) {
			style += "text-align:" + this.align + ";";
		}

		if (style.length() > 0) {
			sbf.append("style=\"").append(style).append("\" ");
		}
	}

	@Override
	void toHtml(StringBuilder sbf, PageGeneratorContext pageContext) {

		// if required, assign a name, and remove it before getting out
		boolean panelHasItsName = true;
		if (this.name == null) {
			panelHasItsName = false;
			this.name = pageContext.getPanelName();
		}
		String lt = pageContext.getLayoutType();
		if (AP.projectName.equals("Celebrus")) {
			this.toHtmlCelebrus(sbf, pageContext);
		} else if (lt == null) {
			this.toHtml0(sbf, pageContext);
		} else if (lt.equals("1")) {
			this.toHtml1(sbf, pageContext);
		} else if (lt.equals("2")) {
			this.toHtml2(sbf, pageContext);
		} else if ((lt.equals("3")) || (lt.equals("5"))) {
			this.toHtml3(sbf, pageContext);
		} else {
			this.toHtml0(sbf, pageContext);
		}

		if (panelHasItsName == false) {
			this.name = null;
		}
		return;
	}

	void toHtml0(StringBuilder sbf, PageGeneratorContext pageContext) {
		// if required, assign a name, and remove it before getting out
		boolean panelHasItsName = true;
		if (this.name == null) {
			panelHasItsName = false;
			this.name = pageContext.getPanelName();
		}
		String firstTag = "div";
		String secondTag = "span";
		if (this.requiresGroupOutline) {
			firstTag = "fieldset";
			secondTag = "legend";
		}
		String topClass = "expandedfieldset";
		if (this.hidden) {
			topClass = "collapsedfieldset";
		}

		sbf.append('<').append(firstTag).append(' ');
		if (this.name != null) {
			sbf.append(" id=\"").append(this.name).append("Top\" ");
		}

		if (this.width == null && AP.alignPanels && this.align == null) {
			// NOTE : following statement may end-up adding width="" attribute
			// to a DIV, which is wrong. But I don't want to fix, as it may be
			// used as a feature - RGB 5-May-2012
			sbf.append(" width=\"100%\" ");
		}

		sbf.append(" class=\"").append(topClass).append("\">");

		if ((this.label != null && this.label.length() > 0)
				&& !(this instanceof XMLGridPanel)) {
			sbf.append('<').append(secondTag);
			this.addLabel(sbf);
			sbf.append("</").append(secondTag).append('>');
		}

		PrePost pp = new PrePost();
		if (this.slideEffect != SlideEffect.none
				&& this.leaveSlidingToMe() == false) {
			this.getPrePostForSlider(pp, this.name);
		}
		sbf.append(pp.pre);
		if (this.htmlFileName != null) {
			String html = this.getFileContent(pageContext);
			sbf.append(this.formatHtml(html, pageContext));
		} else {
			this.panelToHtml(sbf, pageContext);
		}
		sbf.append(pp.post);

		sbf.append("</").append(firstTag).append('>');

		if (panelHasItsName == false) {
			this.name = null;
		}

		return;
	}

	private void toHtml1(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("\n<fieldset id=\"").append(this.name)
				.append("Top\" class=\"");
		if (this.requiresGroupOutline) {
			if (this.hidden) {
				sbf.append("collapsedfieldset\" ");
			} else {
				sbf.append("expandedfieldset\" ");
			}
		} else {
			sbf.append("fieldsetwithnooutline\" ");
			if (this.hidden) {
				sbf.append(" style=\"display:none;\" ");
			}
		}

		if (this.width == null && AP.alignPanels && this.align == null) {
			// NOTE : following statement may end-up adding width="" attribute
			// to a DIV, which is wrong. But I don't want to fix, as it may be
			// used as a feature - RGB 5-May-2012
			sbf.append(" width=\"100%\" ");
		}

		sbf.append(">");

		if ((this.label != null && this.label.length() > 0)
				&& !(this instanceof XMLGridPanel)) {
			sbf.append("\n<legend ");
			this.addLabel(sbf);
			sbf.append("</legend>");
		}

		PrePost pp = new PrePost();
		if (this.slideEffect != SlideEffect.none
				&& this.leaveSlidingToMe() == false) {
			this.getPrePostForSlider(pp, this.name);
		}
		sbf.append(pp.pre);
		if (this.htmlFileName != null) {
			String html = this.getFileContent(pageContext);
			sbf.append(this.formatHtml(html, pageContext));
		} else {
			this.panelToHtml(sbf, pageContext);
		}
		sbf.append(pp.post);

		sbf.append("\n</fieldset>");

		return;
	}

	void addLabel(StringBuilder sbf) {
		if (this.isCollapsible) {
			sbf.append(" class=\"twister\" id=\"").append(this.name)
					.append("Twister\" onclick=\"P2.twist(this, '")
					.append(this.name).append("');");
			if (this.onClickActionName != null && this.tabLabel == null) {
				sbf.append("P2.act('").append(this.onClickActionName)
						.append("');");
			}

			sbf.append("\"> ");
			if (this.hidden) {
				sbf.append(PageGeneratorContext.collapsedImg);
			} else {
				sbf.append(PageGeneratorContext.expandedImg);
			}

			sbf.append("<span id=\"").append(this.name).append("Label\">")
					.append(this.label).append("</span>");
		} else {
			sbf.append(" id=\"").append(this.name)
					.append("Label\" class=\"tablelabel\" ");
			if (this.onClickActionName != null && this.tabLabel == null) {
				sbf.append(" style=\"cursor:pointer\" onclick=\"P2.act('")
						.append(this.onClickActionName).append("');\" ");
			}
			sbf.append('>').append(this.label);
		}
		return;
	}

	void toHtml2(StringBuilder sbf, PageGeneratorContext pageContext) {
		if (AP.spanForButtonPanelRequires
				&& (this.getClass().equals(ButtonPanel.class))) {
			sbf.append("\n<span id=\"").append(this.name).append("Top\" ");

			if ((this.width == null)
					&& (this.getClass().equals(ButtonPanel.class) || AP.alignPanels)
					&& this.align == null) {
				sbf.append(" style=\"width:100%\" ");
			}

			sbf.append(">");
		} else {
			sbf.append("\n<table id=\"")
					.append(this.name)
					.append("Top\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" ");
			if ((this.width == null)
					&& (this.getClass().equals(ButtonPanel.class) || AP.alignPanels)
					&& this.align == null) {
				sbf.append(" width=\"100%\" ");
			}

			sbf.append("> <tr><td");
			if (this.align != null) {
				sbf.append(" align=\"").append(this.align).append("\" ");
			}
			sbf.append(">");
		}

		if ((this.label != null) && !(this instanceof XMLGridPanel)) {
			if (this.getClass().equals(ButtonPanel.class)) {
				sbf.append("<span ");
			} else {
				sbf.append("<div ");
			}
			this.addLabel(sbf);
			if (this.getClass().equals(ButtonPanel.class)) {
				sbf.append("\n</span>");
			} else {
				sbf.append("\n</div>");
			}
		}
		if (this.requiresGroupOutline) {
			sbf.append("\n<table class=\"");
			if (this.hidden) {
				sbf.append("collapsedfieldset");
			} else {
				sbf.append("expandedfieldset");
			}
			sbf.append("\" ");
			sbf.append(AbstractPanel.HTML_BEFORE_PANEL);
		}

		PrePost pp = new PrePost();
		if (this.slideEffect != SlideEffect.none
				&& this.leaveSlidingToMe() == false) {
			this.getPrePostForSlider(pp, this.name);
		}
		sbf.append(pp.pre);
		if (this.htmlFileName != null) {
			String html = this.getFileContent(pageContext);
			sbf.append(this.formatHtml(html, pageContext));
		} else {
			this.panelToHtml(sbf, pageContext);
		}
		sbf.append(pp.post);

		if (this.requiresGroupOutline) {
			sbf.append(AbstractPanel.HTML_AFTER_PANEL);
		}

		if (AP.spanForButtonPanelRequires
				&& (this.getClass().equals(ButtonPanel.class))) {
			sbf.append("</span>");
		} else {
			sbf.append("</td></tr></table>");
		}
		return;
	}

	void toHtml3(StringBuilder sbf, PageGeneratorContext pageContext) {
		String layoutType = pageContext.getLayoutType();
		sbf.append("\n<table id=\"").append(this.name).append("Top\" class=\"");
		if (this.hidden) {
			sbf.append("collapsedfieldset");
		} else {
			sbf.append("expandedfieldset");
		}
		sbf.append("\" ");

		if ((this.width == null)
				&& (this.getClass().equals(ButtonPanel.class) || AP.alignPanels)
				&& this.align == null) {
			sbf.append(" width=\"100%\" ");
		}

		if (this.requiresGroupOutline) {
			if ((layoutType.equals("3")) || (layoutType.equals("5"))) {
				sbf.append(AbstractPanel.HTML_BEFORE_PANEL3);
			} else {
				sbf.append(AbstractPanel.HTML_BEFORE_PANEL);
			}
		} else {
			sbf.append(" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>");
		}
		if ((this.label != null) && !(this instanceof XMLGridPanel)) {
			sbf.append("<div ");
			if (this.widthInPixels > 0
					&& (layoutType.equals("3") || layoutType.equals("5"))) {
				int divWidth = this.widthInPixels;
				if (this instanceof TabPanel == false) {
					divWidth += 20;
				}
				sbf.append("style=\"width: " + divWidth + "px;\" ");
			}
			this.addLabel(sbf);
			sbf.append("\n</div>");
		}
		PrePost pp = new PrePost();
		if (this.slideEffect != SlideEffect.none
				&& this.leaveSlidingToMe() == false) {
			this.getPrePostForSlider(pp, this.name);
		}
		sbf.append(pp.pre);
		if (this.htmlFileName != null) {
			String html = this.getFileContent(pageContext);
			sbf.append(this.formatHtml(html, pageContext));
		} else {
			this.panelToHtml(sbf, pageContext);
		}
		sbf.append(pp.post);

		if (this.requiresGroupOutline) {
			if ((layoutType.equals("3")) || (layoutType.equals("5"))) {
				sbf.append(AbstractPanel.HTML_AFTER_PANEL3);
			} else {
				sbf.append(AbstractPanel.HTML_AFTER_PANEL);
			}
		} else {
			sbf.append("</td></tr></table>");
		}
	}

	// THIS HAS TO BE REFACTORED. I have just put into a separate method for
	// celebrus, but it should be based on other parameters
	void toHtmlCelebrus(StringBuilder sbf, PageGeneratorContext pageContext) {
		String layoutType = pageContext.getLayoutType();
		sbf.append("\n<table id=\"").append(this.name).append("Top\" class=\"");
		if (this.hidden) {
			sbf.append("collapsedfieldset");
		} else {
			sbf.append("expandedfieldset");
		}
		sbf.append("\" ");

		if ((this.width == null)
				&& (this.getClass().equals(ButtonPanel.class) || AP.alignPanels)
				&& this.align == null) {
			sbf.append(" width=\"100%\" ");
		}
		sbf.append(" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"  onmouseover=\"PannelEffect(this)\">\n");

		if (this.requiresGroupOutline) {
			sbf.append("<tr>\n");
			sbf.append("<td colspan=\"2\" class=\"topLeftCorner\"></td>\n");
			sbf.append("<td class=\"b2Top\"></td>\n");
			sbf.append("<td colspan=\"2\" class=\"topRightCorner\"></td>\n");
			sbf.append("</tr>\n");
			sbf.append("<tr>\n");
			sbf.append("<td class=\"b2Left\"></td>\n");
			sbf.append("<td class=\"b2Fill\"></td>\n");
		} else {
			sbf.append("<tr>\n");
		}

		sbf.append("<td class=\"panelholder\">\n");
		if ((this.label != null) && !(this instanceof XMLGridPanel)) {
			sbf.append("<div ");
			if (this.widthInPixels > 0
					&& ((layoutType.equals("3") || layoutType.equals("5")))) {
				int divWidth = this.widthInPixels;
				if (this instanceof TabPanel == false) {
					divWidth += 20;
				}
				sbf.append("style=\"width: " + divWidth + "px;\" ");
			}
			this.addLabel(sbf);
			sbf.append("\n</div>");
		}
		PrePost pp = new PrePost();
		if (this.slideEffect != SlideEffect.none
				&& this.leaveSlidingToMe() == false) {
			this.getPrePostForSlider(pp, this.name);
		}
		sbf.append(pp.pre);
		if (this.htmlFileName != null) {
			String html = this.getFileContent(pageContext);
			sbf.append(this.formatHtml(html, pageContext));
		} else {
			this.panelToHtml(sbf, pageContext);
		}
		sbf.append(pp.post);

		sbf.append("</td>\n");

		if (this.requiresGroupOutline) {
			sbf.append("<td class=\"b2Fill\"></td>\n");
			sbf.append("<td class=\"b2Right\"></td>\n");
			sbf.append("</tr>\n");
			sbf.append("<tr>\n");
			sbf.append("<td colspan=\"2\" class=\"bottomLeftCorner\"></td>\n");
			sbf.append("<td class=\"b2Bottom\"></td>\n");
			sbf.append("<td colspan=\"2\" class=\"bottomRightCorner\">&nbsp;</td>\n");
			sbf.append("</tr>\n");
		} else {
			sbf.append("</tr>\n");
		}

		sbf.append("</table>\n");
	}

	void getPrePostForSlider(PrePost pp, String ename) {
		pp.pre = "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr>";
		pp.post = "</tr></table>";
		if (this.slideEffect == SlideEffect.fromLeft) {
			pp.pre = pp.pre + this.getSlider(ename) + "<td>";
			pp.post = "</td >" + pp.post;
		} else {
			pp.pre = pp.pre + "<td>";
			pp.post = "</td>" + this.getSlider(ename) + pp.post;
		}
	}

	private String getSlider(String ename) {
		String lr = (this.slideEffect == SlideEffect.fromLeft) ? "left"
				: "right";
		return "<td class=\""
				+ lr
				+ "slider\" id=\""
				+ ename
				+ "Slider\" style=\"height:"
				+ this.height
				+ "; vertical-align:middle;\" title=\"Slide\" onclick=\"P2.slidePanel(this, '"
				+ ename + "', '" + lr + "');\">" + AbstractPanel.LEFT_ARROW
				+ "<br />" + AbstractPanel.RIGHT_ARROW + "</td>";
	}

	/***
	 * each type of panel has to define how it wants to render itself
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	abstract void panelToHtml(StringBuilder sbf,
			PageGeneratorContext pageContext);

	/***
	 * next-gen generator, defaults to the old way
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	abstract void panelToHtml5(StringBuilder sbf,
			PageGeneratorContext pageContext);

	@SuppressWarnings("unused")
	// parameters are used by sub-classes
	void setChildTableColumns(StringBuilder js, PageGeneratorContext pageContext) {
		/**
		 * this may be required in some type of panels, and hence a default
		 * implementation is put here for sub classes to override on a need
		 * basis
		 */
	}

	/**
	 * generate model javascript for this panel
	 * 
	 * @param js
	 * @param pageContext
	 */
	void toJs(StringBuilder js, PageGeneratorContext pageContext) {
		if (this.tableName != null) {
			js.append("\n\n/* MetaData for Panel :").append(this.name)
					.append(" with table name = ").append(this.tableName)
					.append("*/");
			String objectName = this.getClass().getName()
					.substring(this.getClass().getName().lastIndexOf('.') + 1);
			String pName = this.name;
			if (this.repeatOnFieldName != null
					&& this.repeatingPanelName != null) {
				pName = this.repeatingPanelName;
			}

			js.append('\n').append(Page.JS_VAR_NAME).append(" = new PM.")
					.append(objectName).append("();");
			js.append('\n').append(Page.JS_VAR_NAME).append(".name = '")
					.append(this.tableName).append("';");
			js.append('\n').append(Page.JS_VAR_NAME).append(".panelName = '")
					.append(pName).append("';");

			// signal that all the fields now on are in this table
			pageContext.setTableName(this.tableName, false);

			pageContext.setAttributes(this, js,
					AbstractPanel.ALL_META_ATTRIBUTES);
			pageContext.setTableSensitiveAttributes(this, js,
					AbstractPanel.ALL_TABLE_SENSITIVE_ATTRIBUTES);

			this.setChildTableColumns(js, pageContext);
			js.append("\nP2.addTable(").append(Page.JS_VAR_NAME).append(");");

		}
		/**
		 * any html template that may induce some js?
		 */
		if (this.htmlFileName != null) {
			String html = this.getFileContent(pageContext);
			String j = this.getJsForTemplate(html, pageContext);
			if (j != null) {
				js.append(j);
			}
		}

		this.elementsToJs(js, pageContext);

		if (this.tableName != null) {
			pageContext.resetTableName();
		}
	}

	/**
	 * meant for panel to insert any javascript to take care of the included
	 * html
	 * 
	 * @param html
	 * @param pageContext
	 * @return
	 */
	protected String getJsForTemplate(String html,
			PageGeneratorContext pageContext) {
		return null;
	}

	// this panel may have children who need some meta data
	void elementsToJs(StringBuilder js, PageGeneratorContext pageContext) {
		if (this.elements == null) {
			return;
		}

		for (AbstractElement ele : this.elements) {
			if (ele == null) {
				continue;
			}

			if (ele instanceof AbstractField) {
				AbstractField f = (AbstractField) ele;
				f.toJs(js, pageContext);
			} else if (ele instanceof AbstractPanel) {
				AbstractPanel p = (AbstractPanel) ele;
				p.toJs(js, pageContext);
			}

			else if (ele instanceof ButtonElement) {
				ButtonElement be = (ButtonElement) ele;
				be.toJs(js, pageContext);
			}
		}
	}

	/**
	 * pixels to be parsed
	 * 
	 * @param txt
	 * @return
	 */
	int parsePixels(String txt) {
		int n = 0;
		String textToBeParsed = txt.replace("px", "").replace("PX", "");
		try {
			n = Integer.parseInt(textToBeParsed);
			return n;
		} catch (Exception e) {
			/**
			 * if it not a valid integer, we are igniring it..
			 */
		}
		return 0;
	}

	/***
	 * next-gen generator. Focus on dom, and delegate as much as possible to css
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	@Override
	void toHtml5(StringBuilder sbf, PageGeneratorContext pageContext) {

		if (this.name == null || this.name.length() == 0) {
			String newName = pageContext.getPanelName();
			this.name = newName;
			this.inError = true;
			pageContext
					.reportError("It is now mandatory to provide name for all panels");
		}

		if (this.keepItSimple) {
			this.toSimpleHtml(sbf, pageContext);
			return;
		}

		// we have retained lower-case class name to be compliant with .js files
		String topClass = "expandedfieldset";
		if (this.hidden) {
			topClass = "collapsedfieldset";
		}
		sbf.append("<div class=\"").append(topClass).append("\" id=\"")
				.append(this.name).append("Top\" >");

		if ((this.label != null && this.label.length() > 0)) {
			if (this.isCollapsible) {
				sbf.append("<div id=\"").append(this.name)
						.append("Twister\" class=\"")
						.append(this.hidden ? "collapsed" : "expanded")
						.append("Twister\" onclick=\"P2.twist(this, '")
						.append(this.name).append("');\" >");
			}
			sbf.append("<div id=\"").append(this.name)
					.append("Label\" class=\"panelLabel\" >")
					.append(this.label).append("</div>");
			if (this.isCollapsible) {
				sbf.append("</div>");
			}
		}
		if (this.htmlFileName != null) {
			String html = this.getFileContent(pageContext);
			sbf.append(this.formatHtml(html, pageContext));
		} else {
			this.panelToHtml5(sbf, pageContext);
		}
		sbf.append("</div>");
		return;
	}

	private AbstractElement getActionField() {
		HiddenField field = new HiddenField();
		field.name = CommonFieldNames.BULK_ACTION;
		field.dataElement = DataDictionary.getDefaultElement(field.name);
		return field;
	}

	/**
	 * simple html with no wrapper.. no features..
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	private void toSimpleHtml(StringBuilder sbf,
			PageGeneratorContext pageContext) {
		sbf.append("\n<div ");
		if (this.cssClassName == null) {
			sbf.append("class=\"simplePanel\" ");
		}

		this.addMyAttributes(sbf, pageContext);
		/*
		 * we would have missed hoverText
		 */
		if (this.hoverText != null) {
			sbf.append(" title=\"").append(this.hoverText).append("\" ");
		}

		if (this.onClickActionName != null) {
			sbf.append(" onclick=\"P2.act(this, '").append(this.name)
					.append("','").append(this.onClickActionName)
					.append("');\" ");
		}

		sbf.append("> ");
		if (this.tableName != null) {
			pageContext.setTableName(this.tableName, false);
		}
		if (this.elements != null && this.elements.length > 0) {
			for (AbstractElement ele : this.elements) {

				if (ele == null) {
					String msg = "Panel " + this.name
							+ " has an invalid element definition";
					pageContext.reportError(msg);
					sbf.append(msg);
					continue;
				}

				if (ele.inError) {
					String msg = "Element " + ele.name
							+ " has invalid defintion within panel "
							+ this.name;
					pageContext.reportError(msg);
					sbf.append(msg);
					continue;
				}

				/**
				 * fields were originally designed to be inside tr.
				 */
				if (ele instanceof AbstractField) {
					((AbstractField) ele).toSimpleHtml(sbf, pageContext);
				} else {
					ele.toHtml5(sbf, pageContext);
				}
			}
		}
		if (this.tableName != null) {
			pageContext.resetTableName();
		}
		sbf.append(" </div>");
	}

	@Override
	public void initialize(PageGeneratorContext context) {
		super.initialize(context);

		if (this.elementsPerRow <= 0) {
			this.elementsPerRow = 1;
		}
		if (this.widthInPixels == 0 && this.slideEffect != SlideEffect.none) {
			String msg = "ERROR: For sliding effect for panel " + this.name
					+ " you MUST specify width in pixels";
			Spit.out(msg);
			this.inError = true;
		}
		if (this.isCollapsible
				&& (this.label == null || this.label.length() == 0)) {
			if (this.name != null && this.name.length() > 0) {
				this.label = this.name;
			} else {
				this.label = "Label for below panel!!";
			}
		}
		if (this.recordName != null) {
			this.setFieldsFromRecord(context);
			/*
			 * Let us not get into trouble with hidden field at the beginning
			 */
			this.handleHiddenElementAtBeginning();
			if (this.actionFieldName != null) {
				AbstractElement[] newElements = new AbstractElement[this.elements.length + 1];
				int i = 0;
				for (AbstractElement ele : this.elements) {
					newElements[i] = ele;
					i++;
				}
				newElements[i] = this.getActionField();
				this.elements = newElements;
			}
		}

		if (this.elements != null && this.elements.length > 0) {
			for (AbstractElement ele : this.elements) {
				ele.initialize(context);
			}
			AbstractElement ele = this.elements[0];
			if (ele.numberOfUnitsToUse == 0) {
				ele.numberOfUnitsToUse = 1;
			}
		}
	}

	/**
	 * read file into text. In case of error, return the error text after
	 * reporting error to context
	 * 
	 * @param context
	 * @return file content, or error message. caller will not know whether the
	 *         test is error or the file content. And that suits the caller :-)
	 */
	private String getFileContent(PageGeneratorContext context) {
		String txt = null;
		if (this.htmlFileName == null) {
			txt = "templateFileName is not set for html template panel."
					+ this.name;
			Spit.out(txt);
			context.reportError(txt);
		} else {

			txt = ResourceManager.readFile("page/" + this.htmlFileName);
			if (txt == null) {
				txt = this.htmlFileName
						+ " could not be read. Ensure that this file exists inside your resource/page/ folder. Panel "
						+ this.name + " is not loaded.";
				Spit.out(txt);
				context.reportError(txt);
			}
		}
		return txt;
	}

	/***
	 * individual panels can implement their templates and over-ride this
	 * default method
	 * 
	 * @param sbf
	 * @param html
	 * @param context
	 */
	protected String formatHtml(String html, PageGeneratorContext context) {
		return html;
	}

	/*
	 * methods added for using record concept
	 */

	/**
	 * called from initializer to set fields array from record
	 * 
	 * @return true if all OK. false if some errors have been reported to
	 *         pageCOntext
	 */
	private boolean setFieldsFromRecord(PageGeneratorContext pageContext) {
		if (this.recordName == null) {
			return true;
		}
		Record record = Records.getRecord(this.recordName);
		if (record == null) {
			pageContext.reportError("Panel " + this.name + " refers to record "
					+ this.recordName + " but that record does not exist.");
			return false;
		}
		boolean elementsSpecified = this.elements != null
				&& this.elements.length > 0;

		if (this.fieldsToSkip != null && this.fieldsToShow != null) {
			pageContext
					.reportError("Panel "
							+ this.name
							+ " has fieldsToShow as well as fieldsToSkip. Please choose to skip OR to show.");
			return false;
		}

		/*
		 * case A : when child elements are specified. This means that the
		 * fields in the record should be used as reference for the fields
		 * defined as elements for this display panel.
		 */
		if (elementsSpecified) {
			return this.validateElementsAgainstRecord(record, pageContext);
		}

		if (this.fieldsToShow != null) {

			/*
			 * case B : fieldsToShow is used. Create child elements based on
			 * these names. Add hidden fields, if required, at the end.
			 */
			Map<String, Field> fieldsFromRecord = record.getFieldsMap();
			return this.useFieldsToShow(fieldsFromRecord, pageContext);
		}

		/*
		 * case C : fieldsToShow is not specified. Use all fields, except
		 * fieldsToSkip if specified
		 */
		Field[] fieldsFromRecord = record.getFields();
		return this.useAllFields(fieldsFromRecord, pageContext);
	}

	/**
	 * check elements against record, and apply any default setting from record
	 * to these elements
	 * 
	 * @param record
	 * @param pageContext
	 * @return
	 */
	private boolean validateElementsAgainstRecord(Record record,
			PageGeneratorContext pageContext) {

		/*
		 * get data from record into convenient collections
		 */
		Map<String, Field> allFields = record.getFieldsMap();
		Set<String> shownFields = null;
		Set<String> skippedFields = null;
		Set<String> hiddenFields = null;
		if (this.fieldsToShow != null) {
			shownFields = new HashSet<String>();
			for (String fieldName : this.fieldsToShow) {
				shownFields.add(fieldName);
			}
		}
		if (this.fieldsToHide != null) {
			hiddenFields = new HashSet<String>();
			for (String fieldName : this.fieldsToHide) {
				hiddenFields.add(fieldName);
			}
		}
		if (this.fieldsToSkip != null) {
			skippedFields = new HashSet<String>();
			for (String fieldName : this.fieldsToSkip) {
				skippedFields.add(fieldName);
			}
		}
		/*
		 * call the worker method to do the actual job. If fieldsToShow was also
		 * specified, this method would have removed the field names that are
		 * specified also as elements
		 */
		if (this.reviseElements(allFields, shownFields, hiddenFields,
				skippedFields, pageContext) == false) {
			return false;
		}
		/*
		 * are there fields still in fields to show
		 */
		if (shownFields == null || shownFields.size() == 0) {
			return true;
		}

		/*
		 * let us add them at end of this panel.
		 */
		AbstractElement[] newElements = new AbstractElement[this.elements.length
				+ shownFields.size()];
		int idx = 0;
		/*
		 * copy existing ones
		 */
		for (AbstractElement ele : this.elements) {
			newElements[idx] = ele;
			idx++;
		}
		/*
		 * append new ones
		 */
		for (String fieldName : shownFields) {
			Field field = allFields.get(fieldName);
			if (field == null) {
				pageContext
						.reportError(fieldName
								+ " is specified as a field to be shown, but that field does not exist in record "
								+ this.recordName);
				return false;
			}
			newElements[idx] = field
					.getPageField(this.fieldsAreOutputByDefault);
			idx++;
		}
		return true;
	}

	/**
	 * add elements based on field names in fieldsToShow
	 */
	private boolean useFieldsToShow(Map<String, Field> fieldsFromRecord,
			PageGeneratorContext pageContext) {
		boolean allOk = true;
		int nbrFieldsToShow = this.fieldsToShow.length;
		int nbrFieldsToHide = this.fieldsToHide == null ? 0
				: this.fieldsToHide.length;
		int totalFields = nbrFieldsToShow + nbrFieldsToHide;

		/*
		 * initialize array for all elements
		 */
		this.elements = new AbstractElement[totalFields];

		int i = 0;
		for (String fieldName : this.fieldsToShow) {
			Field field = fieldsFromRecord.get(fieldName);
			if (field == null) {
				pageContext.reportError(fieldName
						+ " is specified as a field in fieldsToShow for panel "
						+ this.name
						+ " but it is not found in the referred record  "
						+ this.recordName);
				allOk = false;
			} else if (allOk) {
				this.elements[i] = field
						.getPageField(this.fieldsAreOutputByDefault);
			}
			i++;
		}

		/*
		 * add hidden fields if required
		 */
		if (nbrFieldsToHide != 0) {
			for (String fieldName : this.fieldsToHide) {
				Field field = fieldsFromRecord.get(fieldName);
				if (field == null) {
					pageContext
							.reportError(fieldName
									+ " is specified as a field in fieldsToHide for panel "
									+ this.name
									+ " but it is not found in the referred record  "
									+ this.recordName);
					allOk = false;
				} else if (allOk) {
					this.elements[i] = field
							.getPageField(PageFieldType.hiddenField);
				}
				i++;
			}
		}
		return allOk;
	}

	/**
	 * add elements based on field names in fieldsToSkip
	 */
	private boolean useAllFields(Field[] fieldsFromRecord,
			PageGeneratorContext pageContext) {
		boolean allOk = true;
		int nbrSkipped = (this.fieldsToSkip == null ? 0
				: this.fieldsToSkip.length);
		int nbrHiddenFields = this.fieldsToHide == null ? 0
				: this.fieldsToHide.length;

		/*
		 * for convenience we put fields to skip and fields to hide in sets
		 */
		Set<String> skippers = null;
		if (nbrSkipped > 0) {
			skippers = new HashSet<String>();

			for (String skipper : this.fieldsToSkip) {
				skippers.add(skipper);
			}
		}
		Set<String> hiders = null;
		if (nbrHiddenFields > 0) {
			hiders = new HashSet<String>();

			for (String hider : this.fieldsToHide) {
				if (skippers != null && skippers.contains(hider)) {
					pageContext
							.reportError(hider
									+ " is specified as a field to be skipped as well as hidden in panel "
									+ this.name);
					allOk = false;
				}
				hiders.add(hider);
			}
		}

		int totalFields = fieldsFromRecord.length - nbrSkipped;
		/*
		 * We want to ensure that hidden fields come at the end. index to
		 * elements[] array to start filling non-hidden fields
		 */
		int normalIdx = 0;
		/*
		 * index to elements[] array to start filling hidden fields
		 */
		int hiddenIdx = totalFields - nbrHiddenFields;
		this.elements = new AbstractElement[totalFields];
		/*
		 * let us now add fields to elements array
		 */
		for (Field field : fieldsFromRecord) {
			String fieldName = field.getName();
			if (skippers != null && skippers.remove(fieldName)) {
				continue;
			}
			if (hiders != null && hiders.remove(fieldName)) {
				if (allOk && hiddenIdx < totalFields) {
					this.elements[hiddenIdx] = field
							.getPageField(PageFieldType.hiddenField);
				}
				hiddenIdx++;
				continue;
			}
			if (normalIdx < totalFields) {
				if (allOk) {
					this.elements[normalIdx] = field
							.getPageField(this.fieldsAreOutputByDefault);
				}
				normalIdx++;
			} else {
				/*
				 * this happens if a field to be skipped is not in the record,
				 * and hence the actual records over-shoot total fields
				 */
				allOk = false;
			}
		}

		/*
		 * any remaining fields ?
		 */
		if (skippers != null && skippers.size() > 0) {
			StringBuilder errorMessage = new StringBuilder("Field/s ");
			for (String skipper : skippers) {
				errorMessage.append(' ').append(skipper);
			}
			errorMessage.append(" is/are part of fieldsToSkip for ")
					.append(this.name).append(" that refers to record ")
					.append(this.recordName)
					.append(". But these fields are not found in record.");
			pageContext.reportError(errorMessage.toString());
			allOk = false;
		}
		/*
		 * any remaining fields ?
		 */
		if (hiders != null && hiders.size() > 0) {
			StringBuilder errorMessage = new StringBuilder("Field/s ");
			for (String hider : hiders) {
				errorMessage.append(' ').append(hider);
			}
			errorMessage.append(" is/are part of fieldsToHide for ")
					.append(this.name).append(" that refers to record ")
					.append(this.recordName)
					.append(". But these fields are not found in record.");
			pageContext.reportError(errorMessage.toString());
			allOk = false;
		}
		return allOk;
	}

	/**
	 * this is the case where designer has referred to a record, but has
	 * specified elements as well. This means that she wants the fields in this
	 * panel to refer to record.
	 */
	private boolean reviseElements(Map<String, Field> allFields,
			Set<String> shownFields, Set<String> hiddenFields,
			Set<String> skippedFields, PageGeneratorContext pageContext) {
		boolean allOk = true;
		for (AbstractElement element : this.elements) {
			if (element instanceof AbstractField) {
				AbstractField pageField = (AbstractField) element;
				String fieldName = element.name;
				Field field = allFields.get(fieldName);
				if (field != null) {
					/*
					 * is there any conflict with record level specification?
					 */
					if (hiddenFields != null && hiddenFields.remove(fieldName)) {
						pageContext
								.reportError(fieldName
										+ " is specified as an element as part of elements but you asked me to hide that as part of record. Please do not try to confuse me. Either you hide it by specifying that an element, just leave it me to create a hiddenField :-)");
						allOk = false;
					} else if (skippedFields != null
							&& skippedFields.remove(fieldName)) {
						pageContext
								.reportError(fieldName
										+ " is specified as an element as part of elements but you asked me to skip that as part of record. Please do not try to confuse me :-)");
						allOk = false;
					}
					field.revisePageField(pageField, true);
					/*
					 * if it is one of the fields to show, we are ok. Though it
					 * is unnecessary, we do not have conflict
					 */
					if (shownFields != null) {
						shownFields.remove(fieldName);
					}
				} else {
					/*
					 * not in record. Ok if it is local
					 */
					if (pageField.isLocalField) {
						if (pageField.dataType == null) {
							/*
							 * but local fields have to specify data type
							 */
							pageContext
									.reportError(element.name
											+ " is a local field in panel "
											+ this.name
											+ ". You MUST specify a dataType for local fields whil eusing recordName at panel level");
							allOk = false;
						}
					} else {
						pageContext
								.reportError(fieldName
										+ " is a field in panel "
										+ this.name
										+ " that refers to record "
										+ this.recordName
										+ " but this is not a valid field in the record, nor is this a localField.");
						allOk = false;
					}
				}
			} else if (element instanceof AbstractPanel) {
				AbstractPanel childPanel = (AbstractPanel) element;
				if (childPanel.tableName == null
						&& childPanel.recordName == null) {
					boolean childOk = childPanel.reviseElements(allFields,
							shownFields, hiddenFields, skippedFields,
							pageContext);
					allOk = allOk && childOk;
				}
			}
		}
		return allOk;
	}

	private void handleHiddenElementAtBeginning() {
		int eleAt = 0;
		AbstractElement nonHiddenElement = null;
		for (AbstractElement ele : this.elements) {
			if (ele instanceof HiddenField == false) {
				nonHiddenElement = ele;
				break;
			}
			eleAt++;
		}

		if (nonHiddenElement == null || eleAt == 0) {
			/*
			 * first one is indeed a non-hidden field.
			 */
			return;
		}

		AbstractElement[] newElements = new AbstractElement[this.elements.length];
		int idx = 0;
		for (AbstractElement ele : this.elements) {
			newElements[idx] = ele;
			idx++;
		}
		Spit.out("Going to swap first hidden field with "
				+ (nonHiddenElement.name) + " at " + eleAt);
		newElements[0] = nonHiddenElement;
		newElements[eleAt] = this.elements[0];
		this.elements = newElements;
	}
}

/**
 * for convenience of returning more than one value
 * 
 */
class PrePost {
	String pre = "";
	String post = "";
}

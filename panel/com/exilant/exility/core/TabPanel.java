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

class TabPanel extends DisplayPanel {
	private static final String ACTIVE_CLASS = "activetablabel";
	private static final String PASSIVE_CLASS = "passivetablabel";

	// tabbed panels will keep going up and down if individual panels have
	// different heights.
	// Hence the total height must be fixed by the programmer. This must be
	// equal to the highest of the panels
	String tabAreaHeight = null;
	String tabAreaWidth = null;

	@Override
	void panelToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		String existingwidth = this.width;
		String layoutType = pageContext.getLayoutType();
		if ((layoutType.equals("3")) || (layoutType.equals("5"))) {
			int tabnavwidth = 0;
			if (this.widthInPixels > 0) {
				tabnavwidth = (int) Math.ceil(0.99 * this.widthInPixels);
				this.width = "" + tabnavwidth + "px";
			} else if (existingwidth != null && existingwidth.contains("%")) {
				tabnavwidth = (int) Math.ceil(0.99 * Integer
						.parseInt(existingwidth.replace("%", "")));
				this.width = "" + tabnavwidth + "%";
			}
		}
		sbf.append("\n <div class=\"tabnav\" ");
		super.addMyAttributes(sbf, pageContext);
		if (layoutType.equals("5")) {
			sbf.append(" align=\"center\" ");
		}
		sbf.append(">");
		if (layoutType.equals("5")) {
			sbf.append("\n<table cellpadding=\"0\" cellspacing=\"0\"><tr><td>\n");
		}
		this.width = existingwidth;

		String className = TabPanel.ACTIVE_CLASS;
		String clickDisabled = " clickdisabled=\"true\" ";
		for (int i = 0; i < this.elements.length; i++) {
			AbstractElement elem = this.elements[i];
			if ((elem instanceof AbstractPanel) == false) {
				String err = "ERROR: tab panel " + this.name
						+ " has non-panels as its children";
				Spit.out(err);
				pageContext.reportError(err);
				continue;
			}
			if (elem.inError) {
				pageContext.reportError("");
			}
			if (elem.name == null) {
				elem.name = this.name + i;
			}

			AbstractPanel ele = (AbstractPanel) elem;
			String lbl = (ele.tabLabel != null) ? ele.tabLabel
					: (ele.label != null ? ele.label : ele.name);
			String tabName = ele.name + "Tab";

			sbf.append("<div id=\"").append(tabName).append("Left\"  class=\"")
					.append(className).append("Left\" ").append(clickDisabled);
			sbf.append(" tabcontainername=\"").append(this.name)
					.append("\" onclick=\"P2.tabClicked(this, '")
					.append(ele.name).append("TabsDiv', '").append(this.name)
					.append("');");
			if (ele.onClickActionName != null && ele.tabLabel != null) {
				sbf.append("P2.act(null, null, '")
						.append(ele.onClickActionName).append("');");
			}
			sbf.append("\" >").append("</div>");
			sbf.append("<div id=\"").append(tabName).append("\"  class=\"")
					.append(className).append("\" ").append(clickDisabled);
			sbf.append(" tabcontainername=\"").append(this.name)
					.append("\" onclick=\"P2.tabClicked(this, '")
					.append(ele.name).append("TabsDiv', '").append(this.name)
					.append("');");
			if (ele.onClickActionName != null && ele.tabLabel != null) {
				sbf.append("P2.act(null, null, '")
						.append(ele.onClickActionName).append("');");
			}
			sbf.append("\" >");
			if (ele.tabIconImage != null) {
				sbf.append("<img alt=\" \" src=\"../../exilityImages/"
						+ ele.tabIconImage + "\" class=\"TabIconCSS\" />\n");
			}
			sbf.append("<font class=\"TabFontCSS\">" + lbl + "</font>\n");
			sbf.append("</div>");
			sbf.append("<div id=\"").append(tabName)
					.append("Right\"  class=\"").append(className)
					.append("Right\" ").append(clickDisabled);
			sbf.append(" tabcontainername=\"").append(this.name)
					.append("\" onclick=\"P2.tabClicked(this, '")
					.append(ele.name).append("TabsDiv', '").append(this.name)
					.append("');");
			if (ele.onClickActionName != null && ele.tabLabel != null) {
				sbf.append("P2.act(null, null, '")
						.append(ele.onClickActionName).append("');");
			}
			sbf.append("\" >").append("</div>");

			className = TabPanel.PASSIVE_CLASS;
			clickDisabled = "";
		}
		if (layoutType.equals("5")) {
			sbf.append("\n</td></tr></table>\n");
		}
		sbf.append("</div>");
		if (layoutType.equals("3")) {
			sbf.append("<div class=\"tabscontainer\" id = \"");
			sbf.append(this.name + "Container\" ");
			if (this.hidden) {
				sbf.append("style=\"display:none;\">");
			} else {
				sbf.append(">");
			}
			sbf.append("<table  width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\">");
			sbf.append("<tr>");
			sbf.append("<td class=\"tabB2Left\" valign=\"top\" width=\"1px\"></td>");
			sbf.append("<td class=\"tabB2Fill\" valign=\"top\" width=\"9px\"></td>");
			sbf.append("<td></td>");
			if (layoutType.equals("5")) {
				sbf.append("\n<td class=\"tabB2Fill\" valign=\"top\" width=\"9px\"></td><td class=\"tabB2Right\" valign=\"top\" width=\"1px\"></td>\n");
			} else {
				sbf.append("<td height=\"10px\" width=\"10px\" colspan=\"2\"><img alt=\" \" src=\"../../exilityImages/tabB2TopRight.gif\"/></td>");
			}
			sbf.append("</tr>");
			sbf.append("<tr>");
			sbf.append("<td class=\"tabB2Left\" valign=\"top\" width=\"1px\"></td><td class=\"tabB2Fill\" valign=\"top\" width=\"9px\"></td>");
			sbf.append("<td valign=\"top\" class=\"panelholder\">");
		}
		sbf.append("<div class=\"tabs\" id = \"");
		sbf.append(this.name + "Tabs\" ");
		if (this.hidden) {
			sbf.append("style=\"display:none;\">");
		} else {
			sbf.append(">");
		}

		String display = "";
		for (AbstractElement panel : this.elements) {
			sbf.append("<div id=\"").append(panel.name + "TabsDiv\" ")
					.append(display).append(">");
			((AbstractPanel) panel).toHtml(sbf, pageContext);
			sbf.append("</div>");
			display = " style=\"display:none\" ";
		}

		sbf.append("</div>");

		if (layoutType.equals("3")) {
			sbf.append("</td>");
			sbf.append("<td width=\"9px\" valign=\"top\" class=\"tabB2Fill\"></td><td width=\"1px\" valign=\"top\" class=\"tabB2Right\"></td>");
			sbf.append("</tr>");
			sbf.append("<tr>");
			sbf.append("<td width=\"10px\" colspan=\"2\"><img alt=\" \" src=\""
					+ PageGeneratorContext.imageFolderName
					+ "tabB2BottomLeft.gif\"/></td><td class=\"tabB2Bottom\"></td>"
					+ "<td  width=\"10px\" colspan=\"2\"><img alt=\" \" src=\""
					+ PageGeneratorContext.imageFolderName
					+ "tabB2BottomRight.gif\" /></td>");
			sbf.append("</tr>");
			sbf.append("</table>");
			sbf.append("</div>");
		}

		return;
	}

	/***
	 * 
	 * @sbf
	 * @pageContext
	 */
	@Override
	void panelToHtml5(StringBuilder sbf, PageGeneratorContext pageContext) {
		/*
		 * <div#name.tbpanel> <div#nameTabNav.tabnav>
		 * <div#child1NameTab.active/passivetablabel>cildLabel1</div> ....
		 * </div> <div#nameTabs.tabs> <div#childNameTabsDiv>...</div> ......
		 * </div></div>
		 */
		sbf.append("\n<div ");
		if (this.cssClassName == null) {
			sbf.append("class=\"tabpanel\" ");
		}
		super.addMyAttributes(sbf, pageContext);
		sbf.append(" >");
		sbf.append("\n <div class=\"tabnav\" id=\"").append(this.name)
				.append("TabNav\" >");

		// first tab is the default cliecked tab
		int activeIdx = -1;
		// add tablabel for each child panel
		for (int i = 0; i < this.elements.length; i++) {
			AbstractElement elem = this.elements[i];

			if ((elem instanceof AbstractPanel) == false) {
				String err = "ERROR: tab panel " + this.name
						+ " has non-panels as its children";
				Spit.out(err);
				pageContext.reportError(err);
				continue;
			}

			AbstractPanel childPanel = (AbstractPanel) elem;
			String lbl = (childPanel.tabLabel != null) ? childPanel.tabLabel
					: (childPanel.label != null ? childPanel.label
							: childPanel.name);
			String tabName = childPanel.name + "Tab";

			sbf.append("<div id=\"").append(tabName).append("\"  class=\"");
			if (activeIdx >= 0 || childPanel.hidden) {
				sbf.append("passivetablabel\" clickdisabled=\"true\" ");
				if (childPanel.hidden) {
					sbf.append(" style=\"display:none;\" ");
				}
			} else {
				sbf.append("activetablabel\" ");
				activeIdx = i;
			}
			// tabcontainer naem is used by event function to idnetify this tab
			// with its container
			sbf.append(" tabcontainername=\"").append(this.name)
					.append("\" onclick=\"P2.tabClicked(this, '");
			sbf.append(childPanel.name).append("TabsDiv', '").append(this.name)
					.append("');").append("\" ");
			sbf.append(" >").append(lbl).append("</div>");
		}

		sbf.append("</div>");
		// done with tab labels. Now let us render child panels in a container
		// div
		sbf.append("<div class=\"tabs\" id=\"").append(this.name + "Tabs\">");

		for (int i = 0; i < this.elements.length; i++) {
			AbstractElement elem = this.elements[i];
			sbf.append("<div id=\"").append(elem.name + "TabsDiv\" ");
			if (i != activeIdx) {
				sbf.append(" style=\"display:none;\" ");
			}
			sbf.append(">");

			((AbstractPanel) elem).toHtml5(sbf, pageContext);
			sbf.append("</div>");
		}

		sbf.append("</div></div>");
		return;
	}

	@Override
	void toJs(StringBuilder js, PageGeneratorContext pageContext) {
		js.append("\n\n//special variable for tracking active tab of tab panel "
				+ this.name);
		js.append("\nvar " + this.name + "ActiveTabName = '")
				.append(this.elements[0].name).append("Tab';");
		js.append("\nvar " + this.name + "ActivePanelName = '")
				.append(this.elements[0].name).append("TabsDiv';");

		super.toJs(js, pageContext);
	}
}

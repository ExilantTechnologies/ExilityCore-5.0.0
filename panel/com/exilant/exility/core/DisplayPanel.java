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
 * represents a panel in a page
 * 
 */
class DisplayPanel extends AbstractPanel {
	/***
	 * Is this panel linked to a list/grid panel. In that case, this panel acts
	 * as a 'panel view' for each row in the list/grid panel
	 */
	String linkedTableName = null;

	/**
	 * number of entries should match number of columns in the table
	 */
	String[] columnWidths = null;

	/***
	 * can rows be deleted from this panel? this is actually an attribute of
	 * gridPanel, but added here to get a common method called elementsToHtml
	 */
	boolean rowsCanBeDeleted = false; // grid

	/***
	 * Do you need a label in the header for delete-check-box column? this is
	 * actually an attribute of gridPanel, but added here to get a common method
	 * called elementsToHtml
	 */
	String labelForBulkDeleteCheckBox = null;

	/**
	 * render field descriptions from data dictionary in a TD in front of the
	 * field
	 */
	boolean renderFieldDescriptions;

	@Override
	void panelToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		/*
		 * start with a div tag and add attributes for the tag
		 */
		sbf.append("\n<div ");
		if (this.cssClassName == null) {
			sbf.append("class=\"displaypanel\" ");
		}

		super.addMyAttributes(sbf, pageContext);
		if (this.onClickActionName != null) {
			sbf.append(" onclick=\"P2.act(this, '").append(this.name)
					.append("','").append(this.onClickActionName)
					.append("');\" ");
		}

		sbf.append("> ");

		/*
		 * ensure valid elements per row
		 */
		if (this.elementsPerRow < 1) {
			this.elementsPerRow = 1;
		}

		/*
		 * render child elements
		 */
		if (this.elements != null && this.elements.length > 0) {
			if (this.tableName != null) {
				pageContext.setTableName(this.tableName, false);
			}

			this.elementsToHtml(sbf, pageContext, true);

			if (this.tableName != null) {
				pageContext.resetTableName();
			}
		}
		/*
		 * close div tag
		 */
		sbf.append(" </div>");
	}

	/**
	 * render elements of the display panel
	 * 
	 * @param sbf
	 * @param pageContext
	 * @param setIdToTable
	 */
	void elementsToHtml(StringBuilder sbf, PageGeneratorContext pageContext,
			boolean setIdToTable) {

		this.setTableTag(sbf, pageContext, setIdToTable);
		/*
		 * above call opens a <table> tag. we should remember to close that.
		 */
		int totalElements = this.elements.length;

		int unitsUsed = 0;
		int eleIdx = 0;

		// we start a tr outside the loop and end it after the loop.
		sbf.append(" <tr>");

		if (this.rowsCanBeDeleted) {
			sbf.append("<td colspan=\"").append(this.elementsPerRow * 2)
					.append("\">");
			this.addDeleteCheckBox(sbf, pageContext, false);
			sbf.append("</td></tr><tr>");
		}

		/*
		 * what if user, by mistake, has used 0 units for the first element?.
		 * Let us handle this before we start the loop
		 */

		AbstractElement ele = this.elements[eleIdx];
		int units = ele.numberOfUnitsToUse;
		if (units > this.elementsPerRow) {
			pageContext.reportError("Display panel " + this.name
					+ " uses elementsPerRow=" + this.elementsPerRow
					+ " but element " + ele.name + " is asking for " + units
					+ " units. Default unit of 1 is assumed.");
			units = 1;
		}
		if (units == 0) {
			sbf.append("<td colspan=\"2\">");
		}
		/*
		 * otherwise, td is opened inside the loop for the first element anyway
		 */

		/*
		 * while loop will break when controlIndex == totalControls
		 */
		while (true) {
			if (ele != null) {
				if (ele.inError) {
					pageContext.reportError("");
				}
				if (ele instanceof AbstractField) {
					/*
					 * fields manage their own td. just call the field.render.
					 */
					AbstractField field = (AbstractField) ele;
					field.toHtml(sbf, pageContext);
				} else {
					/*
					 * for panels, we will open the td and hand over. td will be
					 * closed later in this loop
					 */
					//
					if (units > 0) {
						sbf.append(" <td colspan=\"").append(units * 2)
								.append("\" ");
						if (ele.align != null) {
							sbf.append(" align=\"").append(ele.align)
									.append("\" ");
						}
						sbf.append('>');
					}
					ele.toHtml(sbf, pageContext);
				}
			}

			unitsUsed += units;
			eleIdx++;
			if (eleIdx == totalElements) {
				break;
			}

			ele = this.elements[eleIdx];
			if (ele == null) {
				continue;
			}
			units = ele.numberOfUnitsToUse;

			/*
			 * no need to close the td if the next field/panel is going to latch
			 * on to this td
			 */

			if (units == 0) {
				continue;
			}

			sbf.append("</td>");
			/*
			 * tr is not required till we consume all units in a row
			 */
			int remainingUnits = this.elementsPerRow - unitsUsed;
			if (remainingUnits < units) {
				pageContext.reportError(ele.name + " wants " + units
						+ " units, but this row has only " + remainingUnits
						+ " left. Assuming numberOfUnitsToUse="
						+ remainingUnits + ". Rework your columns.");
				units = remainingUnits;
				remainingUnits = 0;
			}

			if (remainingUnits > 0) {
				continue;
			}

			/*
			 * end this tr and reset units counter
			 */
			sbf.append(" </tr> <tr>");
			unitsUsed = 0;
		}
		/*
		 * last td
		 */
		sbf.append("</td>");
		/*
		 * ensure that we have desired number of cells in the last row
		 */
		if (unitsUsed < this.elementsPerRow) {
			sbf.append("<td colspan=\"")
					.append((this.elementsPerRow - unitsUsed) * 2)
					.append("\">&nbsp;</td>");
		}
		/*
		 * close the last tr before closing table
		 */
		sbf.append("</tr></table>");
	}

	/**
	 * table tag depends on the page layout.
	 * 
	 * @param sbf
	 * @param pageContext
	 * @param setIdToTable
	 */
	void setTableTag(StringBuilder sbf, PageGeneratorContext pageContext,
			boolean setIdToTable) {
		String layoutType = pageContext.getLayoutType();
		if (layoutType != null && layoutType.equals("2")) {
			sbf.append(" <table class=\"fieldstable\" ");
		} else {
			sbf.append(" <table cellpadding=\"0\" cellspacing=\"0\" class=\"fieldstable\" ");
		}

		if (AP.alignPanels) {
			sbf.append(" width=\"100%\" ");
		}

		if (this.tableName != null && setIdToTable) {
			sbf.append("id=\"").append(this.tableName).append("\" >\n");
		} else {
			sbf.append(">\n");
		}

		int nbrTds = this.elementsPerRow * 2;
		boolean assignWidths = false;
		if (this.columnWidths != null) {
			if (nbrTds == this.columnWidths.length) {
				assignWidths = true;
			} else {
				String err = "ERROR: "
						+ this.name
						+ " has columnWidths set with "
						+ this.columnWidths.length
						+ " comma separated values. It should have "
						+ (this.elementsPerRow * 2)
						+ " values instead. (this is two times the elementsPerRow value) Alternately, do not use this attribute, but use css for columns in stead.";
				Spit.out(err);
				pageContext.reportError(err);
			}
		}

		/*
		 * col tags for styling columns
		 */
		if (AP.generateColTags) {
			for (int i = 0; i < nbrTds; i++) {
				sbf.append("<col id=\"").append(this.name).append("Col")
						.append(i + 1).append("\" ");
				if (assignWidths) {
					sbf.append("style=\"width:").append(this.columnWidths[i])
							.append("\" ");
				}
				sbf.append(" />");
			}
		}

		else if (assignWidths) {
			/*
			 * we have to add a row with cells having image of desired width
			 */
			sbf.append("\n<tr>");
			for (String w : this.columnWidths) {
				sbf.append("\n<td><img alt=\" \" src=\"")
						.append(PageGeneratorContext.imageFolderName)
						.append("space.gif\" height=\"1px\" width=\"" + w
								+ "\"/></td>");
			}
			sbf.append("\n</tr>");
		}
	}

	@Override
	void panelToHtml5(StringBuilder sbf, PageGeneratorContext pageContext) {
		/*
		 * start with div tag and add attributes
		 */
		sbf.append("\n<div ");
		if (this.cssClassName == null) {
			sbf.append("class=\"displayPanel\" ");
		}

		super.addMyAttributes(sbf, pageContext);
		if (this.onClickActionName != null) {
			sbf.append(" onclick=\"P2.act(this, '").append(this.name)
					.append("','").append(this.onClickActionName)
					.append("');\" ");
		}

		sbf.append("> ");

		/*
		 * render child elements
		 */
		if (this.elements != null && this.elements.length > 0) {
			if (this.tableName != null) {
				pageContext.setTableName(this.tableName, false);
			}

			this.elementsToHtml5(sbf, pageContext, true);

			if (this.tableName != null) {
				pageContext.resetTableName();
			}
		}
		/*
		 * close div tag
		 */
		sbf.append(" </div>");
	}

	/**
	 * render child elements of the panel
	 * 
	 * @param sbf
	 * @param pageContext
	 * @param setIdToTable
	 */
	void elementsToHtml5(StringBuilder sbf, PageGeneratorContext pageContext,
			boolean setIdToTable) {
		/*
		 * child elements are rendered inside a table
		 */
		sbf.append(" <table class=\"fieldsTable\" ");

		if (setIdToTable) {
			sbf.append(" id=\"");
			if (this.tableName != null) {
				sbf.append(this.tableName);
			} else {
				sbf.append(this.name).append("FieldsTable");
			}

			sbf.append("\" ");
		}
		sbf.append(">\n");

		/*
		 * we have issue with strict HTML5 compliance when we end-up having
		 * colspan across all rows. (That would be unnecessary)
		 */
		int[] tdsPerUnit = this.getTdsPerUnits(pageContext);
		int totalTds = 0;
		for (int n : tdsPerUnit) {
			totalTds += n;
		}

		/*
		 * generate col tags
		 */
		if (AP.generateColTags) {
			for (int i = 1; i <= totalTds; i++) {
				sbf.append("<col id=\"").append(this.name).append("Col")
						.append(i).append("\" />");
			}
		}
		/*
		 * generate delete button
		 */
		sbf.append("<tr>");
		if (this.rowsCanBeDeleted) {
			sbf.append("<td colspan=\"").append(totalTds).append("\">");
			this.addDeleteCheckBox(sbf, pageContext, false);
			sbf.append("</td></tr><tr>");
		}

		/*
		 * render all elements
		 */
		int unitsUsed = 0;
		boolean isFirstTd = true;

		for (AbstractElement ele : this.elements) {
			int nbrTdsForThisEle = 0;
			AbstractField thisField = (ele instanceof AbstractField) ? (AbstractField) ele
					: null;
			if (ele.numberOfUnitsToUse > 0) {
				/*
				 * need to close previous tag that was left open, except of
				 * course if this is the first td
				 */
				if (isFirstTd) {
					isFirstTd = false;
				} else {
					sbf.append("</td>");
				}

				if (unitsUsed >= this.elementsPerRow) {
					sbf.append("</tr><tr>");
					unitsUsed = 0;
				}

				/*
				 * how many tds used by this ele?
				 */
				nbrTdsForThisEle = tdsPerUnit[unitsUsed];
				if (ele.numberOfUnitsToUse > 1) {
					/*
					 * last index up to which we should accumulate tds
					 */
					int lastIdx = unitsUsed + ele.numberOfUnitsToUse - 1;
					if (lastIdx >= this.elementsPerRow) {
						lastIdx = this.elementsPerRow - 1;
					}
					while (lastIdx > unitsUsed) {
						nbrTdsForThisEle += tdsPerUnit[lastIdx];
						lastIdx--;
					}
					Spit.out("We got " + nbrTdsForThisEle + " tds for element "
							+ ele.name);
				}
				/*
				 * fields open and manage their td. Others we have to manage
				 * here
				 */
				if (thisField == null) {
					if (nbrTdsForThisEle > 1) {
						sbf.append(" <td colspan=\"").append(nbrTdsForThisEle)
								.append("\" >");
					} else {
						sbf.append("<td>");
					}
				}
				unitsUsed += ele.numberOfUnitsToUse;
			}

			if (thisField == null) {
				ele.toHtml5(sbf, pageContext);
			} else {
				thisField.toHtml5(sbf, pageContext, nbrTdsForThisEle);
			}
		}

		/*
		 * did we render anything at all?
		 */
		if (isFirstTd) {
			/*
			 * Unnecessary table
			 */
			sbf.append("<td>&nbsp;</td>");
		} else {
			sbf.append("</td>");
			/*
			 * ensure that the last tr has right number of tds
			 */
			if (unitsUsed < this.elementsPerRow) {
				sbf.append("<td colspan=\"")
						.append((this.elementsPerRow - unitsUsed) * 2)
						.append("\">&nbsp;</td>");
			}
		}
		sbf.append("</tr> </table> ");
	}

	/**
	 * delete check box in case this display panel has table associated with
	 * that
	 * 
	 * @param sbf
	 * @param pageContext
	 * @param useTd
	 */
	void addDeleteCheckBox(StringBuilder sbf, PageGeneratorContext pageContext,
			boolean useTd) {
		String closeTag;
		String layoutType = pageContext.getLayoutType();
		if (useTd) {
			closeTag = "</td>";
			if (layoutType.equals("5")) {
				sbf.append("<td style=\"width:21px;\">");
			} else {
				sbf.append("<td>");
			}
		} else {
			sbf.append("<div class=\"deleteRowButton\">");
			closeTag = "</div>";
		}
		AbstractField chk = null;

		/*
		 * image for delete button
		 */
		if (layoutType.equals("5") && AP.showIamgeForDeleteOption
				&& this.labelForBulkDeleteCheckBox != null) {
			chk = new CheckBoxField();
			chk.label = "";
		} else if (AP.showIamgeForDeleteOption) {
			ImageField img = new ImageField();
			img.baseSrc = PageGeneratorContext.imageFolderName;
			img.imageExtension = ".gif";
			img.defaultValue = "deleteRow";
			img.cssClassName = "deleteimg";
			chk = img;
		} else {
			chk = new CheckBoxField();
		}
		/*
		 * note that name will be prefixed with tableName_ by when this is
		 * inserted into the row. This name has to match with the name that is
		 * used inside gridHeaderChecked function in ui.js
		 */
		chk.name = this.tableName + "Delete";
		chk.dataElementName = "boolean";
		chk.numberOfUnitsToUse = 1;
		if (layoutType.equals("5") && this.labelForBulkDeleteCheckBox != null) {
			chk.htmlAttributes = "onkeydown=\"P2.handleGridNav(this, '"
					+ chk.name
					+ "', event);\" onclick=\"return P2.removeTableRow(event, this, '"
					+ this.tableName + "');\" ";
		} else {
			chk.htmlAttributes = "onkeydown=\"P2.handleGridNav(this, '"
					+ chk.name
					+ "', event);\" onclick=\"return P2.deleteTableRow(event, this, '"
					+ this.tableName + "');\" ";
		}
		if (pageContext.useHtml5) {
			chk.fieldToHtml5(sbf, pageContext);
		} else {
			chk.fieldToHtml(sbf, pageContext);
		}

		sbf.append(closeTag);
	}

	/**
	 * check for a field that requires two tds in each of the units of this
	 * panel
	 * 
	 * @param pageContext
	 * @return each element has true value of that unit has a field
	 */
	private int[] getTdsPerUnits(PageGeneratorContext pageContext) {

		/*
		 * issue with our "1 UNit = 2 TD" approach. HTML5 checkers raise an
		 * error if we have set colspan for td "unnecessarily". for example if
		 * we have two rows, and first td in both rows set colspan="2" checker
		 * raises an error. This is an attempt to quell that.
		 * 
		 * We need colspan="2" only if we have a field with label in that unit.
		 */

		/**
		 * did we find an element at this unit? possible to be no if elements
		 * use nbrUniysToUse > 1
		 */
		boolean[] elementFound = new boolean[this.elementsPerRow];
		/**
		 * whether a field is found at this unit
		 */
		boolean[] fieldFound = new boolean[this.elementsPerRow];
		int curUnit = 0;
		/**
		 * number of units for which elements are found
		 */
		int nbrUnitsWithEles = 0;
		int nbrUnitsWithFields = 0;
		for (AbstractElement ele : this.elements) {
			if (ele.numberOfUnitsToUse == 0) {
				continue;
			}
			if (elementFound[curUnit] == false) {
				elementFound[curUnit] = true;
				nbrUnitsWithEles++;
			}
			if (ele instanceof AbstractField) {
				AbstractField f = (AbstractField) ele;
				if (f.labelPosition == LabelPosition.left
						|| f.labelPosition == null) {
					fieldFound[curUnit] = true;
					nbrUnitsWithFields++;
					/*
					 * did we hit all units?
					 */
					if (nbrUnitsWithFields == this.elementsPerRow) {
						break;
					}
				}
			}
			curUnit += ele.numberOfUnitsToUse;
			int remainingUnits = this.elementsPerRow - curUnit;

			if (remainingUnits > 0) {
				continue;
			}

			if (remainingUnits < 0) {
				pageContext
						.reportError("Pnel "
								+ this.name
								+ " has an element named "
								+ ele.name
								+ " that uses numberOfUnitsToUse="
								+ ele.numberOfUnitsToUse
								+ " that makes the total units in that row exceed the set value of "
								+ this.elementsPerRow + " elements per row.");
				/*
				 * let us also correct this for this run, in case page is
				 * generated with errors!!
				 */
				ele.numberOfUnitsToUse += remainingUnits;
			}
			/*
			 * move to next row;
			 */
			curUnit = 0;

		}

		/*
		 * do we have an element in every unit? If not, then the designer has
		 * used unnecessary values of nbrUnintsToUse
		 */
		if (nbrUnitsWithEles < this.elementsPerRow) {
			pageContext
					.reportError("Panel "
							+ this.name
							+ " uses elementsPerRow="
							+ this.elementsPerRow
							+ ". This results in on emore units not having any element starting in that. You shoudl redesign and use minimum value required for elementsPerRow.");
		}
		/*
		 * populate number tds in each unit to 1 or 2
		 */
		int[] tds = new int[this.elementsPerRow];
		for (int i = 0; i < tds.length; i++) {
			tds[i] = fieldFound[i] ? 2 : 1;
		}
		return tds;
	}
}

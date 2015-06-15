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
 * A button's visibility can be changes when the user changes any field.
 * 
 */
enum WhatToDoOnFormChange {
	/**
	 * not affected
	 */
	LEAVEMEALONE
	/**
	 * when user changes any field for the first time, enable this button.
	 */
	, ENABLE
	/**
	 * when user changes any field for the first time, disable this button.
	 */
	, DISABLE
}

/**
 * represents a button that the user can click to take action. Buttons are
 * rendered differently based on the chosen layout
 * 
 */
class ButtonElement extends AbstractElement {
	String imageName = null;
	boolean isDefaultButton = false;
	WhatToDoOnFormChange whatToDoOnFormChange = WhatToDoOnFormChange.LEAVEMEALONE;
	String iconImage = null;

	private void toCelebrusHtml(StringBuilder sbf, String fieldName,
			PageGeneratorContext pageContext) {
		sbf.append("\n");
		sbf.append("<table");
		sbf.append(" id=\"" + fieldName + "\" ");
		sbf.append(" class=\"buttonelementtable\" cellpadding=\"0\" cellspacing=\"0\" ");
		if (this.onClickActionName != null) {
			String script = "P2.act(this, '" + fieldName + "', '"
					+ this.onClickActionName + "');";
			if (pageContext.isInsideGrid) {
				script = "P2.rowSelected('" + pageContext.getTableName()
						+ "', this);" + script;
			}
			sbf.append(" onclick=\"").append(script).append("\" ");
		}

		if (this.htmlAttributes != null) {
			sbf.append(' ').append(this.htmlAttributes).append(' ');
		}

		String style = "";

		if (this.onClickActionName != null) {
			style += "cursor:pointer;";
		}
		if (this.width != null) {
			style += "width:" + this.width + ';';
		}

		if (this.height != null) {
			style += "overflow:auto; height:" + this.height + ';';
		}

		if (this.hidden) {
			style += "display:none;";
		}

		if (style.length() > 0) {
			sbf.append(" style=\"").append(style).append("\" ");
		}

		sbf.append(" onmouseover=\"ButtonEffect(this)\" onmousedown=\"ButtonDown(this)\" >");
		sbf.append("\n");
		sbf.append("<tr>\n");
		sbf.append("<td class=\"ButtonLeftCSS\">&nbsp;</td>\n");
		sbf.append("<td class=\"ButtonMiddleCSS\">\n");
		if (this.iconImage != null) {
			sbf.append("<img src=\"../../exilityImages/" + this.iconImage
					+ "\" class=\"ButtonIconCSS\" />\n");
		}
		sbf.append("<font class=\"ButtonFontCSS\">" + this.label + "</font>\n");
		sbf.append("</td>\n");
		sbf.append("<td class=\"ButtonRightCSS\">&nbsp;</td>\n");
		sbf.append("</tr>\n");
		sbf.append("</table>\n");
	}

	private void to3and5NoImage(StringBuilder sbf, String fieldName,
			PageGeneratorContext pageContext) {
		if (this.imageName == null) {
			sbf.append("\n<input type=");
			if (this.isDefaultButton) {
				sbf.append("\"submit\" ");
			} else {
				sbf.append("\"button\" ");
			}
			if (this.cssClassName == null) {
				sbf.append(" class=\"button\" ");
			}
			sbf.append("value=\"").append(this.label).append("\" ");
			if (this.name != null) {
				sbf.append("id=\"").append(fieldName).append("\" ");
			}
			super.addMyAttributes(sbf, pageContext);

			if (this.whatToDoOnFormChange == WhatToDoOnFormChange.ENABLE) {
				sbf.append("disabled=\"disabled\" ");
			}
			sbf.append(" />");
			return;
		}
		if (!(this.imageName.equals("default"))) {
			sbf.append("\n<input type=");
			sbf.append("\"image\" ");
			if (this.cssClassName == null) {
				sbf.append(" class=\"imagebutton\" ");
			}
			sbf.append(" src=\"").append(PageGeneratorContext.imageFolderName)
					.append(this.imageName).append("\" ");

			if (this.name != null) {
				sbf.append("id=\"").append(fieldName).append("\" ");
			}
			super.addMyAttributes(sbf, pageContext);

			if (this.whatToDoOnFormChange == WhatToDoOnFormChange.ENABLE) {
				sbf.append("disabled=\"disabled\" ");
			}
			sbf.append(" />");
			return;
		}
		sbf.append("\n<table class=\"buttonelementtable\" cellpadding=\"0\" cellspacing=\"0\" ><tr>");
		sbf.append("<td id=\"").append(fieldName + "Left").append("\" ");
		if (this.hidden) {
			sbf.append(" style=\"background-image:url('../../exilityImages/buttonLeft.gif'); background-repeat:no-repeat; width:10px; height:20px; display:none;\">&nbsp;</td>");
		} else {
			sbf.append(" style=\"background-image:url('../../exilityImages/buttonLeft.gif'); background-repeat:no-repeat; width:10px; height:20px; \">&nbsp;</td>");
		}
		sbf.append("<td id=\"").append(fieldName + "Middle").append("\" ");
		if (this.hidden) {
			sbf.append(" style=\"background-image:url('../../exilityImages/buttonMiddle.gif'); background-repeat:repeat-x; height:20px; vertical-align: middle; display:none;\">"); // Sep
		} else {
			sbf.append(" style=\"background-image:url('../../exilityImages/buttonMiddle.gif'); background-repeat:repeat-x; height:20px; vertical-align: middle; \">");
		}
		if (this.iconImage != null) {
			sbf.append("<img src=\"../../exilityImages/" + this.iconImage
					+ "\"/>");
		}
		sbf.append("<font");
		if (this.cssClassName == null) {
			sbf.append(" class=\"imagebutton\" ");
		}
		if (this.name != null) {
			sbf.append(" id=\"").append(fieldName).append("\" ");
		}
		super.addMyAttributes(sbf, pageContext);
		sbf.append(">");
		sbf.append(this.label);
		sbf.append("</font></td>");
		sbf.append("<td id=\"").append(fieldName + "Right").append("\" ");
		if (this.hidden) {
			sbf.append("style=\"background-image:url('../../exilityImages/buttonRight.gif'); background-repeat:no-repeat; width:10px; height:20px; display:none;\">&nbsp;</td>"); // Sep
		} else {
			sbf.append("style=\"background-image:url('../../exilityImages/buttonRight.gif'); background-repeat:no-repeat; width:10px; height:20px; \">&nbsp;</td>"); // Sep
		}
		sbf.append("</tr></table>\n");

	}

	private void to3and5Image(StringBuilder sbf, String fieldName,
			PageGeneratorContext pageContext) {
		sbf.append("\n<input type=");
		sbf.append("\"image\" ");
		if (this.cssClassName == null) {
			sbf.append(" class=\"imagebutton\" ");
		}
		sbf.append(" src=\"").append(PageGeneratorContext.imageFolderName)
				.append(this.imageName).append("\" ");

		if (this.name != null) {
			sbf.append("id=\"").append(fieldName).append("\" ");
		}
		super.addMyAttributes(sbf, pageContext);

		if (this.whatToDoOnFormChange == WhatToDoOnFormChange.ENABLE) {
			sbf.append("disabled=\"disabled\" ");
		}
		sbf.append(" />");
	}

	private void to3and5DefaultImage(StringBuilder sbf, String fieldName,
			PageGeneratorContext pageContext) {
		sbf.append("\n<table class=\"buttonelementtable\" cellpadding=\"0\" cellspacing=\"0\" ><tr>");
		sbf.append("<td id=\"").append(fieldName + "Left").append("\" ");
		if (this.hidden) {
			sbf.append(" style=\"background-image:url('../../exilityImages/buttonLeft.gif'); background-repeat:no-repeat; width:10px; height:20px; display:none;\">&nbsp;</td>");
		} else {
			sbf.append(" style=\"background-image:url('../../exilityImages/buttonLeft.gif'); background-repeat:no-repeat; width:10px; height:20px; \">&nbsp;</td>");
		}
		sbf.append("<td id=\"").append(fieldName + "Middle").append("\" ");
		if (this.hidden) {
			sbf.append(" style=\"background-image:url('../../exilityImages/buttonMiddle.gif'); background-repeat:repeat-x; height:20px; vertical-align: middle; display:none;\">"); // Sep
		} else {
			sbf.append(" style=\"background-image:url('../../exilityImages/buttonMiddle.gif'); background-repeat:repeat-x; height:20px; vertical-align: middle; \">");
		}
		if (this.iconImage != null) {
			sbf.append("<img src=\"../../exilityImages/" + this.iconImage
					+ "\"/>");
		}
		sbf.append("<font");
		if (this.cssClassName == null) {
			sbf.append(" class=\"imagebutton\" ");
		}
		if (this.name != null) {
			sbf.append(" id=\"").append(fieldName).append("\" ");
		}
		super.addMyAttributes(sbf, pageContext);
		sbf.append(">");
		sbf.append(this.label);
		sbf.append("</font></td>");
		sbf.append("<td id=\"").append(fieldName + "Right").append("\" ");
		if (this.hidden) {
			sbf.append("style=\"background-image:url('../../exilityImages/buttonRight.gif'); background-repeat:no-repeat; width:10px; height:20px; display:none;\">&nbsp;</td>"); // Sep
		} else {
			sbf.append("style=\"background-image:url('../../exilityImages/buttonRight.gif'); background-repeat:no-repeat; width:10px; height:20px; \">&nbsp;</td>"); // Sep
		}
		sbf.append("</tr></table>\n");

	}

	@Override
	void toHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		if (pageContext.useHtml5) {
			this.toHtml5(sbf, pageContext);
			return;
		}

		String fieldName = pageContext.getName(this.name);
		if (AP.projectName.equals("Celebrus")) {
			this.toCelebrusHtml(sbf, fieldName, pageContext);
			return;
		}
		String layoutType = pageContext.getLayoutType();
		if ((layoutType.equals("3")) || (layoutType.equals("5"))) {
			if (this.imageName == null) {
				this.to3and5NoImage(sbf, fieldName, pageContext);
				return;
			}
			if (this.imageName.equals("default")) {
				this.to3and5DefaultImage(sbf, fieldName, pageContext);
				return;
			}
			this.to3and5Image(sbf, fieldName, pageContext);
			return;
		}
		this.toNon3and5(sbf, fieldName, pageContext);
	}

	private void toNon3and5(StringBuilder sbf, String fieldName,
			PageGeneratorContext pageContext) {
		sbf.append("\n<input type=");
		if (this.imageName == null) {
			if (this.isDefaultButton) {
				sbf.append("\"submit\" ");
			} else {
				sbf.append("\"button\" ");
			}
			if (this.cssClassName == null) {
				sbf.append(" class=\"button\" ");
			}
			sbf.append("value=\"").append(this.label).append("\" ");
		} else {
			sbf.append("\"image\" ");
			if (this.cssClassName == null) {
				sbf.append(" class=\"imagebutton\" ");
			}
			sbf.append(" src=\"").append(PageGeneratorContext.imageFolderName)
					.append(this.imageName).append("\" ");
		}

		if (this.name != null) {
			sbf.append("id=\"").append(fieldName).append("\" ");// Jun 17
		}
		super.addMyAttributes(sbf, pageContext);

		if (this.whatToDoOnFormChange == WhatToDoOnFormChange.ENABLE) {
			sbf.append("disabled=\"disabled\" ");
		}
		sbf.append(" />");
	}

	@Override
	void toHtml5(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("\n<input type=");
		if (this.isDefaultButton) {
			sbf.append("\"submit\" ");
		} else {
			sbf.append("\"button\" ");
		}
		sbf.append("value=\"").append(this.label).append("\" ");

		if (this.name != null) {
			sbf.append("id=\"").append(pageContext.getName(this.name))
					.append("\" ");
		}
		super.addMyAttributes(sbf, pageContext);

		if (this.whatToDoOnFormChange == WhatToDoOnFormChange.ENABLE) {
			sbf.append("disabled=\"disabled\" ");
		}
		/*
		 * and a special case for buttons that we internally generate as of now
		 */
		if (this.htmlAttributes != null) {
			sbf.append(' ').append(this.htmlAttributes).append(' ');
		}
		sbf.append(" />");
	}

	/**
	 * emit js code for this button
	 * 
	 * @param js
	 * @param pageContext
	 */
	public void toJs(StringBuilder js, PageGeneratorContext pageContext) {
		if (this.isDefaultButton) {
			js.append("// this is the default button\nP2.defaultButtonName = '"
					+ this.name + "';\n");
		}
	}
}

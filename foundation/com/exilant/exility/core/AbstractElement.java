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

/***
 * 
 * Represents an Abstract Element within a page. Panels of a page contain
 * elements. An element can be just an element that has no data binding, a panel
 * that contain other elements or a field that is bound to a data source, with
 * possible rendering.
 * 
 */

abstract class AbstractElement {

	/***
	 * name has to be unique within a panel. All panels with no table associated
	 * with them is considered to be one conceptual panel. panels with
	 * associated table, like list and grid have to have elements with unique
	 * names within them. A named element is identified by its fully qualified
	 * name. Fully qualified name is name prefixed with table name and a '_' as
	 * joiner
	 */
	String name = null;

	/**
	 * strongly recommended that elements are described on a need basis, and not
	 * to put default one like this is the name of the element
	 */
	String description = null;

	/**
	 * you should use this to override the label of the associated data element
	 */
	String label = null;

	/**
	 * if the panel associated has a table, this is the label produced on the
	 * footer
	 */
	String footerLabel = null;

	/**
	 * view attributes are discouraged now. if it is only number, it is assumed
	 * to be in pixels
	 */
	String width = null;
	/**
	 * view attributes are discouraged now. if it is only number, it is assumed
	 * to be in pixels
	 */
	String height = null;
	/**
	 * is it hidden at the time of rendering?
	 */
	boolean hidden = false;

	/**
	 * alignment values as in style.
	 */
	String align = null;

	/***
	 * This one is the most misused attributed. Typically used for setting style
	 * attributes. But that is strictly NO, as it may result in tag having two
	 * style attributes
	 **/
	String htmlAttributes = null;

	/**
	 * documentation
	 */
	String techNotes = null;

	/**
	 * documentation
	 */
	String businessValidation = null;

	/**
	 * this is showed as tool-tip whenever mouse hovers on this element
	 */
	String hoverText = null;

	/**
	 * A panel lays out its elements into rows each row accommodating a fixed
	 * number of elements. Let us say three elements per row. Does this element
	 * take its due share of one unit, or wants to grab more? It is also
	 * possible that this element does not want any of its own unit, and be
	 * pushed into the space given to the previous element itself (attribute set
	 * to 0)
	 */
	int numberOfUnitsToUse = 1;

	/**
	 * exility renders elements with certain predefined class names. You may
	 * chose to override that.
	 */
	String cssClassName = null;

	/**
	 * action to be taken when user clicks on this element. action must be
	 * defined at the page level
	 */
	String onClickActionName = null;

	/**
	 * if this is part of a table is this element sortable? Actually elements do
	 * not have run time values, and hence this attribute should have been put
	 * at abstract field level
	 */
	boolean isSortable = false;

	/**
	 * refer to isSortable. Similar meaning for filtering.
	 */
	boolean isFilterable = false;

	/**
	 * internally used during page generation to track elements that are in
	 * error
	 */
	boolean inError = false;

	/**
	 * as in css.
	 */
	String border = null;

	/**
	 * calculated field
	 */
	protected int widthInPixels = 0;

	/**
	 * generate html rendering text in to sbf
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	abstract void toHtml(StringBuilder sbf, PageGeneratorContext pageContext);

	/***
	 * we are moving away from html embedding view attributes to make use of css
	 * By default old method is retained, but some elements may override this to
	 * have a new renderer
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	abstract void toHtml5(StringBuilder sbf, PageGeneratorContext pageContext);

	/**
	 * this must be called by all sub-classes. Whenever this method is
	 * over-ridden, they should first invoke this
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	void addMyAttributes(StringBuilder sbf, PageGeneratorContext pageContext) {
		String myName = pageContext.getName(this.name);
		if (this.cssClassName != null) {
			sbf.append("class=\"").append(this.cssClassName).append("\" ");
		}

		if (this.hoverText != null) {
			sbf.append("title=\"").append(this.hoverText).append("\" ");
		}

		if (this.onClickActionName != null) {
			String script = "P2.act(this, '" + myName + "', '"
					+ this.onClickActionName + "', event);";
			if (pageContext.isInsideGrid) {
				script = "P2.rowSelected('" + pageContext.getTableName()
						+ "', this);" + script;
			}
			sbf.append("onclick=\"").append(script).append("\" ");
		}

		if (this.htmlAttributes != null && !pageContext.useHtml5) {
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

		if (this.border != null) {
			style += "border-width:" + this.border + "; ";
		}
		if (style.length() > 0) {
			sbf.append("style=\"").append(style).append("\" ");
		}
	}

	/**
	 * initialized top-down, unlike other exility work products
	 * 
	 * @param context
	 */
	public void initialize(PageGeneratorContext context) {
		/*
		 * if width is specified as just number, it is assumed to be pixels.
		 * some panels require that the width is specified in pixels for them to
		 * some calculations
		 */
		if (this.numberOfUnitsToUse < 0) {
			this.numberOfUnitsToUse = 0;
		}
		if (this.width != null && this.width.length() > 0) {
			String txt = this.width.replaceAll("[pP][xX]", "");
			try {
				this.widthInPixels = Integer.parseInt(txt);
			} catch (Exception e) {
				context.reportWarning("Width is specified as " + txt
						+ ". We will not have pixels for element " + this.name);
			}
			/*
			 * if it is only a number, put px in front width as just number
			 * without any unit is not reliable. It does not work on Safari. put
			 * px However, PathFinder lived with this bug, and have made
			 * alternate arrangement. Fixing this bug creates problem for
			 * them!!!
			 */
			if (AP.pageLayoutType.equals("5") == false) {
				char c = this.width.charAt(this.width.length() - 1);
				if (c >= '0' && c <= '9') {
					this.width += "px";
				}
			}
		}
	}
}

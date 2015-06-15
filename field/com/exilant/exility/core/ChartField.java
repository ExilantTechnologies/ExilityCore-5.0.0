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
 * represents a chart. This is a wrong design. Chart should be a panel, and not
 * a field because a chart is bound to table(s) of data and not just one field.
 * We are working on the new design, after which we start deprecating chart
 * fields. Note : most attributes use all lower case naming convention, in
 * violation of our own naming convention. Please bear with us.
 * 
 */
class ChartField extends AbstractField {
	/**
	 * service that fetches data for this chart
	 */
	String reportServiceId = null;
	/**
	 * column that represents x axis for the chart
	 */
	String xaxiscolumn = null;

	/**
	 * column that
	 */
	String yaxiscolumn = null;

	/**
	 * chart has multiple data sets. That is several charts are rendered as a
	 * group. This feature is not useful. do not use it.
	 */
	boolean isMultiDataSet = false;

	/**
	 * wherever grouping is required, this is ths colum
	 */
	String groupbycolumn = null;
	/**
	 * label for x axis
	 */
	String xaxislabel = null;
	/**
	 * label for y axis
	 */
	String yaxislabel = null;
	/**
	 * min x to be considered for determining the scale,
	 */
	String minx = null;
	/**
	 * max x to be considered for determining the scale,
	 */
	String miny = null;
	/**
	 * min y to be considered for determining the scale,
	 */
	String maxx = null;
	/**
	 * maxy y to be considered for determining the scale,
	 */
	String maxy = null;

	/**
	 * whether separate legend box needs be shown
	 */
	boolean showLegend = true;

	/**
	 * whether report service needs to be fired automatically on load, or it
	 * will be triggered later in the page
	 */
	boolean noAutoLoad = false;

	/**
	 * formatter to be used for y axis label. client code has to supply a
	 * suitable js funciton for the same
	 */
	String yaxislabelformatterid = null;

	/**
	 * column to be used for rendering bubbles
	 */
	String bubblecolumn = null;

	/**
	 * scale to be used to determine bubble radius
	 */
	double bubbleradiusdenominator = 1.0;
	// fields added after standardization

	/**
	 * name of panel that has legend container
	 */
	String legendContainer = null;

	/**
	 * number of columns per row to be used while rendering legends
	 */
	int legendNbrColumns = 0;

	/**
	 * function name to be called to format label of legend
	 */
	String legendLabelFormatter = null;

	/**
	 * border color of the small box that is rendered for each legend
	 */
	String legendLabelBoxBorderColor = null;

	/**
	 * where to draw legend. possible four corners ne, nw, sw, and se
	 */
	String legendPosition = null;

	/**
	 * margin within the legend box
	 */
	int legendMargin = 0;

	/**
	 * background color for the entire legends box
	 */
	String legendBackgroundColor = null;

	/**
	 * opacity for the entire legends box
	 */
	String legendBackgroundOpacity = null;

	/**
	 * how to show raw data. possible values are none, count and percent
	 */
	String rawDataDisplay = null;

	/**
	 * list of colors to be used for each chart component
	 */
	String colors = null;

	/**
	 * column from the grid to be used as tool-tip text
	 */
	String helpTextColumn = null;

	/**
	 * column from grid to be used as tool-tip help for groups
	 */
	String groupHelpTextColumn = null;

	/**
	 * color for rendering labels
	 */
	String labelColor = null;

	/**
	 * whether to show joints in line chart as filled points
	 */
	boolean showFilledPoints = false;

	/**
	 * note that this is not action name, but a function that is written by the
	 * client page. This function is called with suitable parameter when use
	 * clicks on the chart
	 */
	String onClickFunctionName = null;

	/**
	 * for pie chart, if the slice is too small, we do not render the label
	 */
	String minPercentToShowLabel = null;

	/**
	 * left margin for the chart area within its box
	 */
	int marginLeft = 0;

	/**
	 * bottom margin for the chart area within its box
	 */
	int marginBottom = 0;

	/**
	 * function that is available in the client page that is invoked whenever
	 * user moves cursor on the chart
	 */
	String onMoveFunctionName = null;

	/**
	 * whether to highlight the corresponding legend when user moves on a part
	 * of the chart
	 */
	boolean legendHighlight = false;

	/**
	 * label width to be limited to
	 */
	int yLabelMaxWidth = 0;

	/**
	 * function that the client page provides to format x label
	 */
	String xLabelFormatterFunction = null;
	/**
	 * function that the client page provides to format y label
	 */
	String yLabelFormatterFunction = null;

	/**
	 * rendering needs lots of improvement to use css wherever possible, We will
	 * not be improving these, as we plan to deprecate this and develop new sets
	 * of chart panels instead
	 */
	@Override
	void fieldToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("<div ");
		if (this.cssClassName == null) {
			sbf.append("class=\"chartfield\" ");
		}
		super.addMyAttributes(sbf, pageContext);
		sbf.append(">");

		String lbl = this.getLabelToUse(pageContext);
		if (lbl == null) {
			lbl = "";
		}

		sbf.append("<div class=\"chartfieldlabelholder\" id=\"")
				.append(this.name).append("LabelHolder\">");
		sbf.append("<span class=\"chartfieldlabel\" id=\"").append(this.name)
				.append("Label\">").append(lbl).append("</span>");
		sbf.append("</div>");

		if (this.yaxislabel != null) {
			sbf.append("<div class=\"chartfieldyaxislabelholder\" id=\"")
					.append(this.name).append("yaxisLabelHolder\">");
			sbf.append("<span class=\"chartfieldaxislabel\" id=\"")
					.append(this.name).append("yaxisLabel\">");
			for (int i = 0; i < this.yaxislabel.length(); i++) {
				sbf.append(this.yaxislabel.substring(i, (i + 1))).append(
						"<br/>");
			}
			sbf.append("</span>");
			sbf.append("</div>");
		}

		sbf.append("<div class=\"chartfieldcontainer\" id=\"")
				.append(this.name).append("Container\">");
		sbf.append("</div>");

		if (this.xaxislabel != null) {
			sbf.append("<div class=\"chartfieldxaxislabelholder\" id=\"")
					.append(this.name).append("xaxisLabelHolder\">");
			sbf.append("<span class=\"chartfieldaxislabel\" id=\"")
					.append(this.name).append("xaxisLabel\">")
					.append(this.xaxislabel).append("</span>");
			sbf.append("</div>");
		}

		sbf.append("</div>");
		if (this.legendContainer != null) {
			sbf.append("<div id=\"").append(this.legendContainer)
					.append("\" class=\"chartlegendcontainer\"></div>");
		}
	}
}

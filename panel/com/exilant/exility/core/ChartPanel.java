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
 * panel in which a chart is drawn for the associated table of data
 * 
 */
public class ChartPanel extends AbstractPanel {
	String chartType = "bar";
	/**
	 * what do you want to do during user interaction These functions will be
	 * called back with the row-id as parameter
	 */
	String onClickFunctionName = null;
	String onMoveFunctionName = null;
	/*
	 * each chart type has its default columns for data. You can explicitly mark
	 * columns instead
	 */
	String xAxisColumn = null;
	String yAxisColumn = null;
	String helpTextColumn = null;
	String groupHelpTextColumn = null;
	String bubbleColumn = null;
	String groupByColumn = null;
	String bulletLabelColumn = null;
	String fromColumn = null;
	String toColumn = null;
	String distributionValueColumn = null;
	String coreColumn = null;
	String level1Column = null;
	String level2Column = null;
	/*
	 * for bullet charts
	 */
	String valueOfInterest = null;
	String comparativeValue = null;
	String firstQualitativeRange = null;
	String secondQualitativeRange = null;
	/*
	 * label related
	 */
	String xAxisLabel = null;
	String yAxisLabel = null;

	/*
	 * label related
	 */
	String labelColor = null;
	String xLabelFormatterFunction = null;
	String yLabelFormatterFunction = null;
	String minPercentToShowLabel = null;
	int yLabelMaxWidth = 0;
	int labelLeftMargin = 0;
	int labelBottomMargin = 0;

	/*
	 * to take care of scale factor, fix min/max for x/y
	 */
	String minX = null;
	String minY = null;
	String maxX = null;
	String maxY = null;

	double bubbleRadiusDenominator = 1.0;

	/*
	 * Rest of the attributes apply to specific charts. Default for these
	 * attributes are defined in exilityParameters.js Attributes set at this
	 * level would over-ride them
	 */
	/**
	 * comma separated list of colors to be used for the chart. default colors
	 * are used if this is not supplied, or not enough colors are supplied
	 */
	String[] colors = null;
	String[] childColors = null;
	int shadowSize = 0;
	/**
	 * how should we show raw data?
	 */
	String rawDataDisplay = null;

	/*
	 * can we manage legends as html rather than canvas? We would like legend to
	 * be separated from charting.
	 */
	/**
	 * we have to ensure that the default value of "false" is a good default
	 */
	boolean hideLegend = false;
	int legendNbrColumns = 0;
	/**
	 * name of a function that would return the formatted label
	 */
	String legendLabelFormatter = null;
	String legendLabelBoxBorderColor = null;
	/**
	 * legend is displayed in one of the corners. default is 'ne' for north-east
	 */
	String legendPosition = null;
	/**
	 * in pixels
	 */
	int legendMargin = 0;
	/**
	 * for entire legend area
	 */
	String legendBackgroundColor = null;
	/**
	 * for entire legend area
	 */
	String legendBackgroundOpacity = null;

	/*
	 * margin related, in pixels
	 */
	int marginLeft = 0;
	int marginBottom = 0;
	int barLabelLeftMargin = 0;
	int barLabelBottomMargin = 0;

	int lineWidth = 0;
	double barWidth = 0;

	String gridColor = null;
	String tickColor = null;
	boolean showFilledPoints = false;
	int pointsRadius = 0;
	String pointsFillColor = null;

	@Override
	/**
	 * note that we are over-riding toHtml5 itself  rather than implementing panelToHtml to simplify things 
	 */
	void panelToHtml5(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("\n<div ");
		if (this.cssClassName == null) {
			sbf.append("class=\"chartPanel\" ");
		}

		super.addMyAttributes(sbf, pageContext);
		if (this.onClickActionName != null) {
			Spit.out("onClickActionName is not relevant for chart panel. Use onClickFunctionName instead.");
		}

		sbf.append("><div id=\"").append(this.tableName)
				.append("\"></div><div id=\"").append(this.tableName)
				.append("Legend\"></div></div>");

	}

	@Override
	void panelToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		this.panelToHtml5(sbf, pageContext);
	}
}

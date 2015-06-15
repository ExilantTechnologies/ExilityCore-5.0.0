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
 * chart fields are evolving. Designer made a mistake of using all lower-case
 * attribute names. bear with us till we revamp this completely
 * 
 */
class SunBurstChart extends ChartField {

	/**
	 * A sun burst chart shows total value at the center, first level of summary
	 * data around that, and further break-up of each segment in the next radial
	 * segments etc.. Ideal data structure for such a chart is a table with
	 * summary levels. However, we have found that programmers find it easier to
	 * write sql that will produce a denormlized table in which summary data
	 * will repeat for each sub-row.
	 */
	String corecolumn = null;

	/**
	 * group level 1 for sun burst
	 */
	String level1column = null;

	/**
	 * group level 2 field for sun burst
	 */
	String level2column = null;

	/**
	 * actual value column
	 */
	String distributionvaluecolumn = null;
}

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
 * filter rows from a grid. Obsolete. Use FilterRows instead
 * 
 */
@Deprecated
public class FilterGrid implements GridProcessorInterface {

	/**
	 * read from this grid
	 */
	String inputGridName = null;
	/**
	 * optional output grid name. Values are added to dc.values if grid name is
	 * not specified
	 */
	String outputGridName = null;
	/**
	 * This represents an additional (optional) condition that may be used to
	 * pick up the value
	 */
	Comparator comparator = Comparator.EQUALTO;
	/**
	 * column name to be filtered on
	 */
	String inputColumnName = null;

	/**
	 * field name
	 */
	String value = null;

	@Override
	public int process(DataCollection dc) {
		return 1;
	}

}

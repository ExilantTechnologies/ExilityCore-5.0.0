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
 * filter out rows in a grid based on values in one column. We go thru each row
 * in the grid, and retain it if the column value compares well with the
 * supplied value. For example if lessThan is the comparator and 45 is the
 * value, then we retain rows with the column value less than 45, and rows that
 * have this column value >= 45 are left out
 * 
 */
class FilterRows implements GridProcessorInterface {
	/**
	 * input grid to use for filtering
	 */
	String fromGridName = null;

	/**
	 * filter rows into this grid. Defaults to fromGridName
	 */
	String toGridName = null;

	/**
	 * name of column that is used for filtering
	 */
	String columnName = null;

	/**
	 * comparator to be used for filtering. Defaults to equalTo
	 */
	Comparator comparator = Comparator.EXISTS;

	/**
	 * value to be used for comparison
	 */
	String value = null;

	@Override
	public int process(DataCollection dc) {
		Grid fromGrid = dc.getGrid(this.fromGridName);
		if (fromGrid == null) {
			Spit.out(this.fromGridName
					+ " not found for a filterRows operation.");
			return 0;
		}
		try {
			if (this.fromGridName.equals(this.toGridName)) {
				fromGrid.filter(this.columnName, this.comparator, this.value);
				return 1;
			}

			Grid toGrid = fromGrid.filterRows(this.columnName, this.comparator,
					this.value);
			dc.addGrid(this.toGridName, toGrid);
			return 1;
		} catch (ExilityException e) {
			String errorText = "Error while filtering " + this.fromGridName
					+ e.getMessage();
			dc.addError(errorText);
			Spit.out(errorText);
			return 0;
		}
	}
}
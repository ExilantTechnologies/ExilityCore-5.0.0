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
 * add a column to an existing grid
 * 
 */
class AddColumn implements GridProcessorInterface {

	/**
	 * grid to which a column is to be added
	 */
	String gridName = null;

	/**
	 * name of the column to be added to the grid
	 */
	String columnName = null;

	/**
	 * value type of the column to be added
	 */
	DataValueType columnType = DataValueType.TEXT;

	/**
	 * An expression to be used to calculate this column value for each row.
	 * This expression can contain column names, in which case the value will be
	 * picked-up from that column in that row. Field names and constant will
	 * have the same value across all rows
	 */
	Expression expression = null;

	@Override
	public int process(DataCollection dc) {
		Grid grid = dc.getGrid(this.gridName);
		if (grid == null) {
			Spit.out(this.gridName + " not found for an addColumn operation");
			return 0;
		}

		if (grid.hasColumn(this.columnName)) {
			Spit.out(this.columnName
					+ " is over-ridden during an addColumn operation.");
		}

		try {
			ValueList col = ValueList.newList(this.columnType,
					grid.getNumberOfRows());
			grid.addColumn(this.columnName, col);
			this.expression.evaluateColumn(grid, dc, this.columnName);

			return 1;
		} catch (ExilityException e) {
			dc.addError("error while addColumn operaiton on grid "
					+ this.gridName + ". Error :" + e.getMessage());
			return 0;
		}
	}
}
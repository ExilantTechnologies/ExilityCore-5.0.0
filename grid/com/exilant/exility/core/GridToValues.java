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
 * get a row of grid into dc.values collection. column names are used as field
 * names
 * 
 */
public class GridToValues implements GridProcessorInterface {
	/**
	 * looks like some project used this class directly in their code, and made
	 * the attributes public. For compatibility we retain that.
	 */
	public String gridName = null;
	/**
	 * prefix to be used for field names. prefix + column names is used as field
	 * name. leave it null if you do not want to use prefix
	 */
	public String prefix = null;
	/**
	 * remove the grid after this operation
	 */
	public boolean removeGrid = false;

	/**
	 * row number in the grid to be extracted
	 */
	public int index = 0;

	@Override
	public int process(DataCollection dc) {
		Grid grid = dc.getGrid(this.gridName);
		if ((grid == null)) {
			Spit.out(this.gridName + " not found for a gridToValues operation");
			return 0;
		}

		int nbrRows = grid.getNumberOfRows();
		if (nbrRows == 0) {
			Spit.out(this.gridName
					+ " had no rows for a gridToValues operation");
			return 0;
		}

		if (nbrRows <= this.index) {
			Spit.out(this.gridName + " has only " + nbrRows
					+ " but a gridToValues operation wanted row number "
					+ (this.index + 1));
			return 0;
		}

		for (String columnName : grid.getColumnNames()) {
			String nameInGrid = (this.prefix == null) ? columnName
					: this.prefix + columnName;
			dc.addValue(nameInGrid, grid.getValue(columnName, this.index));
		}

		if (this.removeGrid) {
			dc.removeGrid(this.gridName);
		}
		return 1;
	}
}
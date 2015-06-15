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
 * rename a column in a grid
 * 
 */
class RenameColumn implements GridProcessorInterface {
	/**
	 * grid from which to rename the column
	 */
	String gridName = null;

	/**
	 * current name of column to be renamed
	 */
	String columnName = null;

	/**
	 * new name for the column
	 */
	String newColumnName = null;

	@Override
	public int process(DataCollection dc) {
		Grid grid = dc.getGrid(this.gridName);

		if (grid == null) {
			Spit.out(this.gridName
					+ " is not found for a renameColumn operation");
			return 0;
		}

		if (grid.hasColumn(this.columnName) == false) {
			dc.addError(this.gridName + " does not have a column named "
					+ this.columnName + " for a renameColumn operation.");
			return 0;
		}

		if (grid.hasColumn(this.newColumnName)) {
			Spit.out(this.gridName + " Already has a column named "
					+ this.newColumnName + ". this columns is replaced. ");
		}
		grid.renameColumn(this.columnName, this.newColumnName);
		return 1;
	}
}
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
 * clone a column.
 * 
 */
public class CloneColumn implements GridProcessorInterface {
	/**
	 * grid from where column needs to be cloned
	 */
	String gridName = null;

	/**
	 * name of column to be cloned
	 */
	String columnName = null;

	/**
	 * name of cloned column
	 */
	String newColumnName = null;

	@Override
	public int process(DataCollection dc) {
		Grid grid = dc.getGrid(this.gridName);
		if (grid == null) {
			Spit.out(this.gridName + " not found for cloneColumn operation");
			return 0;
		}

		ValueList column = grid.getColumn(this.columnName);
		if (column == null) {
			dc.addError(this.gridName + " does not have a column named "
					+ this.columnName + " that is to be cloned.");
			return 0;
		}

		ValueList newColumn = column.clone();
		try {
			grid.addColumn(this.newColumnName, newColumn);
			return 1;
		} catch (ExilityException e) {
			dc.addError(this.columnName + " in grid " + this.gridName
					+ " could not be cloned. Error : " + e.getMessage());
			return 0;
		}
	}
}
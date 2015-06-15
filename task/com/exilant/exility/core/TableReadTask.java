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
 * read one or more rows from a table. Underlying table can be a view that is
 * defined in the rdbms
 * 
 * 
 */
public class TableReadTask extends ExilityTask {

	/**
	 * Child records that are to be included for this task
	 */
	ChildTableTask[] childTableTasks = null;

	@Override
	public int executeTask(DataCollection dc, DbHandle handle)
			throws ExilityException {
		int n = this.getTable(dc).read(dc, handle, this.gridName, null);

		if (n > 0 && this.childTableTasks != null) {
			for (ChildTableTask child : this.childTableTasks) {
				child.filter(dc, handle);
			}
		}
		return n;
	}

	@Override
	public int executeBulkTask(DataCollection dc, DbHandle handle)
			throws ExilityException {
		Grid inputGrid = dc.getGrid(this.gridName);
		if (inputGrid == null || inputGrid.getNumberOfRows() == 0) {
			return 0;
		}
		return this.getTable(dc).massRead(dc, handle, this.gridName, inputGrid);
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.READONLY;
	}
}

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
 * 
 * look at the grid and carry out required operation on each row of the grid
 * based on the action field value
 * 
 */
public class BulkTask extends AbstractInsertTask implements
		ToBeInitializedInterface {
	/**
	 * name of the column that has the bulk action
	 */
	public String actionFieldName = "bulkAction";

	@Override
	public int executeTask(DataCollection dc, DbHandle handle)
			throws ExilityException {
		Spit.out("Starting bulk task in normal mode");
		return this.executeBulkTask(dc, handle);
	}

	@Override
	public int executeBulkTask(DataCollection dc, DbHandle handle)
			throws ExilityException {
		Spit.out("starting bulk task in bulk mode");
		TableInterface table = this.getTable(dc);
		Grid grid = dc.getGrid(this.gridName);
		if (grid == null) {
			return 0;
		}
		int nbrRows = grid.getNumberOfRows();
		if (nbrRows == 0) {
			return 0;
		}
		// copy generated keys...
		if (this.parentTableName != null) {
			this.copyGeneratedKeys(grid, dc);
			Spit.out("Going to copy primary key of parent table  "
					+ this.parentTableName);
		}

		return table.bulkAction(dc, handle, grid, this.actionFieldName);

	}

	@Override
	public void initialize() {
		this.repeatForRowsInGrid = true;
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.READWRITE;
	}
}

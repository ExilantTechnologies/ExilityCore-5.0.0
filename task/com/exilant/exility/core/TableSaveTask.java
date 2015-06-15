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
 * update or insert rows in a table, based on the availability of key field for
 * that row
 */
public class TableSaveTask extends AbstractInsertTask {
	String saveActionName = null;
	/**
	 * Child records that are to be included for this task
	 */
	ChildTableTask[] childRecords = null;

	@Override
	public int executeTask(DataCollection dc, DbHandle handle)
			throws ExilityException {
		TableInterface table = this.getTable(dc);
		int nbrRows = 0;
		if (this.saveActionName == null) {
			nbrRows = table.save(dc, handle);
		} else {
			String sa = dc.getTextValue(this.saveActionName, null);
			if (sa != null && sa.length() > 0) {
				nbrRows = table.persist(dc, handle, sa);
			}
		}
		if (nbrRows > 0 && this.childRecords != null) {
			for (ChildTableTask child : this.childRecords) {
				child.save(dc, handle);
			}
		}

		return nbrRows;
	}

	@Override
	public int executeBulkTask(DataCollection dc, DbHandle handle)
			throws ExilityException {
		TableInterface table = this.getTable(dc);
		Grid grid = dc.getGrid(this.gridName);
		if (table == null || grid == null || grid.getNumberOfRows() == 0) {
			return 0;
		}
		// copy generated keys...
		if (this.parentTableName != null) {
			this.copyGeneratedKeys(grid, dc);
		}

		return table.saveFromGrid(dc, handle, grid);
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.READWRITE;
	}
}

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

/***
 * Child table to be processed along with the parent table for read/save
 * operations. Used with TableReadTask, TableInsertTask, TableUpdateTask and
 * tableSaveTask.
 */
public class ChildTableTask {
	/***
	 * record name for child table
	 */
	String recordName;

	/***
	 * grid to be used for this table
	 */
	String gridName;

	/**
	 * read this child table based on its parent key
	 * 
	 * @param dc
	 * @param handle
	 * @return number of rows read
	 * @throws ExilityException
	 */
	public int filter(DataCollection dc, DbHandle handle)
			throws ExilityException {
		Record record = Records.getRecord(this.recordName);
		Table table = record.getTable();
		Condition[] conditions = record.getFilterForChildRows();
		return table.filter(dc, handle, this.gridName, conditions, null, null,
				null, null, false);
	}

	int insert(DataCollection dc, DbHandle handle) throws ExilityException {
		Record record = Records.getRecord(this.recordName);
		Grid grid = this.copyKeys(dc, record);
		if (grid == null) {
			return 0;
		}
		return record.getTable().insertFromGrid(dc, handle, grid);
	}

	/**
	 * save rows in the grid based on parent key
	 * 
	 * @param dc
	 * @param handle
	 * @throws ExilityException
	 */
	int save(DataCollection dc, DbHandle handle) throws ExilityException {
		Record record = Records.getRecord(this.recordName);
		Grid grid = this.copyKeys(dc, record);
		if (grid == null) {
			return 0;
		}
		return record.getTable().bulkAction(dc, handle, grid, null);
	}

	Grid copyKeys(DataCollection dc, Record record) throws ExilityException {
		Grid grid = dc.getGrid(this.gridName);
		if (grid == null) {
			Spit.out("Grid " + this.gridName
					+ " is not found. Rows not inserted into record "
					+ this.recordName);
			return null;
		}

		int nbrRows = grid.getNumberOfRows();
		if (nbrRows == 0) {
			Spit.out("Grid " + this.gridName
					+ " has no rows. Rows not inserted into record "
					+ this.recordName);
			return null;
		}

		String childKey = record.getChildKey();
		if (childKey == null) {
			throw new ExilityException(
					"Record "
							+ this.recordName
							+ " does not have a parent table defined for it. Rows cannot be inserted into this row as a child.");
		}

		String parentKey = record.getParentKey();
		Value keyValue = dc.getValue(parentKey);
		if (keyValue == null) {
			throw new ExilityException(
					"Design error : Record "
							+ this.recordName
							+ " uses "
							+ parentKey
							+ " as its parent key, but there is no value for that field.");
		}

		ValueList keyColumn = grid.getColumn(childKey);
		if (keyColumn == null) {
			keyColumn = ValueList.newList(keyValue.getValueType(), nbrRows);
			grid.addColumn(childKey, keyColumn);
		}

		keyColumn.setValueToAll(keyValue);
		return grid;
	}
}

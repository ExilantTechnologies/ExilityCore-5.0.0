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

import java.util.HashMap;
import java.util.Map;

/**
 * copy columns from one grid to the other based on matched column values. This
 * is enhanced version of copyColumns. This is typically used to copy some
 * column values from a parent grid into rows of its child grid. Parent table
 * should have a key column that is also present in the child grid as foreign
 * key
 * 
 */
public class CopyColumnsAcrossGrids implements GridProcessorInterface {
	/**
	 * grid from which to copy columns
	 */
	String fromGridName = null;

	/**
	 * grid to which columns are to be copied
	 */
	String toGridName = null;

	/**
	 * name of column in the from grid that has the key to match. This column
	 * value has to be unique within this grid.
	 */
	String keyColumnNameToMatchFrom = null;

	/**
	 * name of column in the toGrid to match key value. This must be the foreign
	 * key into the parent grid
	 */
	String keyColumnNameToMatchTo = null;

	/**
	 * columns to be copied from the grid
	 */
	String[] columnNamesToCopyFrom = null;

	/**
	 * columns to copy to. These are created if required
	 */
	String[] columnNamesToCopyTo = null;

	@Override
	public int process(DataCollection dc) {
		Grid fromGrid = dc.getGrid(this.fromGridName);

		if (fromGrid == null) {
			Spit.out(this.fromGridName
					+ " not found for a copyColumnsAcrossGrids operation.");
			return 0;
		}

		Grid toGrid = dc.getGrid(this.toGridName);
		if (toGrid == null) {
			dc.addError(this.toGridName
					+ " not found for a copyColumnsAcrossGrids operation.");
			return 0;
		}

		int nbrFields = this.columnNamesToCopyFrom == null ? 0
				: this.columnNamesToCopyFrom.length;
		if (nbrFields == 0) {
			dc.addError("columnNamesToCopyFrom not specified proerly for copyColumnsAcrossGrids operation.");
			return 0;
		}

		int nbrToCheck = this.columnNamesToCopyTo == null ? 0
				: this.columnNamesToCopyTo.length;
		if (nbrToCheck == 0) {
			dc.addError("columnNamesToCopyTo not specified properly for copyColumnsAcrossGrids operation.");
			return 0;
		}

		if (nbrFields != nbrToCheck) {
			dc.addError("columnNamesToCopyTo and  columnNamesToCopyFrom have different number of names. copyColumnsAcrossGrids operation failed.");
			return 0;
		}

		ValueList fromKeyColumn = fromGrid
				.getColumn(this.keyColumnNameToMatchFrom);
		if (fromKeyColumn == null) {
			dc.addError(this.keyColumnNameToMatchFrom
					+ " is not a column in grid " + this.fromGridName);
			return 0;
		}

		ValueList toKeyColumn = toGrid.getColumn(this.keyColumnNameToMatchTo);
		if (toKeyColumn == null) {
			dc.addError(this.keyColumnNameToMatchTo
					+ " is not a column in grid " + this.toGridName);
			return 0;
		}

		int nbrToRows = toGrid.getNumberOfRows();

		ValueList[] fromColumns = new ValueList[nbrFields];
		ValueList[] toColumns = new ValueList[nbrFields];
		for (int i = 0; i < nbrFields; i++) {
			String fromColumnName = this.columnNamesToCopyFrom[i];
			ValueList column = fromGrid.getColumn(fromColumnName);
			if (column == null) {
				dc.addError(fromColumnName + " is not column in grid "
						+ this.fromGridName
						+ ". copyColumnAcrossGrid operation failed.");
				return 0;
			}
			fromColumns[i] = column;
			DataValueType vt = column.getValueType();

			String toColumnName = this.columnNamesToCopyTo[i];
			column = toGrid.getColumn(toColumnName);
			if (column == null) {
				column = ValueList.newList(vt, nbrToRows);
				try {
					toGrid.addColumn(toColumnName, column);
				} catch (ExilityException e) {
					dc.addError("Error while adding  column " + toColumnName
							+ " to grid " + this.toGridName);
					return 0;
				}
			} else if (vt != column.getValueType()) {
				dc.addError("From column " + fromColumnName + " is of type "
						+ vt + " while to-column " + toColumnName
						+ " is of type " + column.getValueType()
						+ " copyColumnAcrossGrids failed between grid "
						+ this.fromGridName + " and " + this.toGridName);
				return 0;
			}

			toColumns[i] = column;
		}

		/*
		 * we have to identify row from fromGrid based on keyCOlumn. Let us
		 * create a map to avoid looping thru all rows
		 */
		Map<String, Integer> keys = new HashMap<String, Integer>();
		int row = 0;
		for (String key : fromKeyColumn.getTextList()) {
			keys.put(key, new Integer(row));
			row++;
		}

		/*
		 * we have the columns lined up for copying. we have to copy the right
		 * values across these columns
		 */
		for (int i = 0; i < nbrToRows; i++) {
			String toValue = toKeyColumn.getTextValue(i);
			Integer r = keys.get(toValue);
			if (r != null) {
				row = r.intValue();
				for (int k = 0; k < nbrFields; k++) {
					toColumns[k].setValue(fromColumns[k].getValue(row), i);
				}
			}
		}

		return 1;
	}
}
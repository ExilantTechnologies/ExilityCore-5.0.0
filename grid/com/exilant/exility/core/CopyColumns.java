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
 * copy columns from one grid to another. This is superseded with
 * CopyColumnAcrossGrids. retained for downward compatibility
 * 
 */
@Deprecated
public class CopyColumns implements GridProcessorInterface {

	/**
	 * grid from which to copy columns
	 */
	String fromGridName = null;

	/**
	 * grid to which to copy columns to. Can not be same as fromGrid
	 */
	String toGridName = null;

	/**
	 * name of the column that is present in both grids. This is typically the
	 * primary key of the parent table that would be present in child table as a
	 * foreign key.
	 */
	String keyColumnNameToMatch = null;

	/**
	 * column names to copy from parent grid to child grid. These columns are
	 * added if required.
	 */
	String[] columnNamesToCopy = null;

	@Override
	public int process(DataCollection dc) {

		/*
		 * do we have the grids to work on?
		 */
		Grid fromGrid = dc.getGrid(this.fromGridName);
		if (fromGrid == null) {
			Spit.out(this.fromGridName
					+ " not found for a copyColumns operation.");
			return 0;
		}

		Grid toGrid = dc.getGrid(this.toGridName);
		if (toGrid == null) {
			dc.addError(this.toGridName
					+ " not found for a copyColumns operation.");
			return 0;
		}

		/*
		 * if columns to be copied is not specified, we use all columns
		 */
		String[] copyTheseColumns = new String[0];
		int nbrFields;
		if (this.columnNamesToCopy == null) // copy all columns
		{
			copyTheseColumns = toGrid.getColumnNames();
		} else {
			nbrFields = this.columnNamesToCopy.length;
			if (nbrFields == 0) {
				copyTheseColumns = toGrid.getColumnNames();
			} else if (nbrFields == 1
					&& this.columnNamesToCopy[0].startsWith("@")) {
				String variableName = this.columnNamesToCopy[0].substring(1);
				String names = dc.getTextValue(variableName, null);
				if (names == null) {
					dc.addError("A field named "
							+ variableName
							+ " is not found that was to contain the names of columns for a copyColumns operation on grid "
							+ this.fromGridName);
					return 0;
				}
				copyTheseColumns = names.split(",");
			}
		}

		nbrFields = copyTheseColumns.length;
		ValueList fromKeyColumn = fromGrid.getColumn(this.keyColumnNameToMatch);
		if (fromKeyColumn == null) {
			dc.addError(this.keyColumnNameToMatch + " is not column in grid "
					+ this.fromGridName + ". copyColumn operation failed.");
			return 0;
		}

		ValueList toKeyColumn = toGrid.getColumn(this.keyColumnNameToMatch);
		if (toKeyColumn == null) {
			dc.addError(this.keyColumnNameToMatch + " is not column in grid "
					+ this.toGridName + ". copyColumn operation failed.");
			return 0;
		}

		ValueList[] fromColumns = new ValueList[nbrFields];
		ValueList[] toColumns = new ValueList[nbrFields];

		int nbrToRows = toGrid.getNumberOfRows();
		for (int i = 0; i < nbrFields; i++) {
			String columnName = copyTheseColumns[i].trim();
			ValueList column = fromGrid.getColumn(columnName);
			if (column == null) {
				dc.addError(columnName + " is not column in grid "
						+ this.fromGridName + ". copyColumn operation failed.");
				return 0;
			}
			fromColumns[i] = column;
			DataValueType vt = column.getValueType();

			column = toGrid.getColumn(columnName);
			if (column == null) {
				/*
				 * it is OK if column is not found. We will add it
				 */
				column = ValueList.newList(vt, nbrToRows);
				try {
					toGrid.addColumn(columnName, column);
				} catch (ExilityException exilEx) {
					dc.addError(exilEx.getMessage());
					return 0;
				}
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

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

import java.util.ArrayList;
import java.util.List;

/**
 * Split one row in an input grid into multiple rows in the output grid based on
 * the value of a column that has a list of concatenated values. This process is
 * same as de-aggregator. We retain this for downward compatibility ONly
 * different here is that you get to choose the columns to copy. In case of
 * de-aggregater, all columsn are copied, and in case you do not want some, you
 * can always remove them!!
 */
@Deprecated
class SplitRows implements GridProcessorInterface {
	/**
	 * grid from which to pick data
	 */
	String fromGridName = null;

	/**
	 * new grid to be created
	 */
	String toGridName = null;

	/**
	 * columns to be copied to be copied to new grid as it is
	 */
	String[] columnNamesToCopy = null;

	/**
	 * column that contains concatenated values. This is split to be copied into
	 * new rows
	 */
	String columnNameToSplit = null;

	/**
	 * name of the new column to be added. This column has the split values.
	 * Note that this name must be in dictionary for use to determine its data
	 * type
	 */
	String splitColumnName = null;

	/**
	 * separator used to concatenate values in the column
	 */
	String rowSeparator = ";";

	@Override
	public int process(DataCollection dc) {

		/*
		 * check for possible errors before proceeding
		 */
		if (this.columnNamesToCopy == null) {
			dc.addError("SplitRows is not set up properly for gris "
					+ this.fromGridName);
			return 0;
		}

		Grid fromGrid = dc.getGrid(this.fromGridName);
		if (fromGrid == null) {
			Spit.out("input grid  " + this.fromGridName + " is ot found in dc");
			return 0;
		}

		/*
		 * we first add an empty grid, and then add columns into this
		 */
		Grid toGrid = new Grid(this.toGridName);

		/*
		 * this is the column that is to be split
		 */
		String[] dataToSplit = fromGrid
				.getColumnAsTextArray(this.columnNameToSplit);
		int nbrRows = dataToSplit.length;

		/*
		 * our plan is to first prepare the new column as text. We keep track of
		 * number of time an existing row has to be repeated in the new grid, so
		 * that we can create the other columns as well later
		 */
		List<String> splitVals = new ArrayList<String>();
		int[] counts = new int[nbrRows];

		/*
		 * tracks number of rows in the resultant new grid. This woudl match sum
		 * of all entries in counts array
		 */
		int newNbrRows = 0;
		String[] blankArray = { "" };
		for (int i = 0; i < dataToSplit.length; i++) {
			String val = dataToSplit[i];
			String[] vals;
			if (val == null || val.length() == 0) {
				vals = blankArray;
			} else {
				vals = val.split(this.rowSeparator);
			}
			counts[i] = vals.length;
			newNbrRows += vals.length;
			for (String aVal : vals) {
				splitVals.add(aVal);
			}
		}

		try {
			/*
			 * now, we can create empty columns in the new grid and copy
			 * appropriate values into them
			 */
			for (String columnName : this.columnNamesToCopy) {
				ValueList existingColumn = fromGrid.getColumn(columnName);
				ValueList newColumn = ValueList.newList(
						existingColumn.getValueType(), newNbrRows);
				int newIdx = 0;
				for (int i = 0; i < nbrRows; i++) {
					Value val = existingColumn.getValue(i);
					newColumn.setValue(val, newIdx);
					newIdx++;
					int n = counts[i] - 1;
					/*
					 * we have added one row. Do we need to repeat?
					 */
					while (n > 0) {
						newColumn.setValue(val, newIdx);
						newIdx++;
						n--;
					}
				}
				/*
				 * this column is ready.
				 */
				toGrid.addColumn(columnName, newColumn);
			}

			/*
			 * other columns got added. Add THE column that is created by
			 * splitting
			 */
			String[] arr = splitVals.toArray(new String[0]);
			ValueList lst = ValueList.newList(arr);
			toGrid.addColumn(this.splitColumnName, lst);

		} catch (ExilityException e) {
			dc.addError("Encountered an error while splitting rows (not hair :-)) for grid "
					+ this.fromGridName + ". Error : " + e.getMessage());
			return 0;
		}
		dc.addGrid(this.toGridName, toGrid);
		return nbrRows;
	}
}
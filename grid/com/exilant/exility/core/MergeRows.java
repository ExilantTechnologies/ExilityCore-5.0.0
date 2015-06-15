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
 * merge rows by concatenating concatenating contents of a column This is
 * obsolete. Use aggregater instead
 */
@Deprecated
public class MergeRows implements GridProcessorInterface {

	/**
	 * input grid name from which rows are merged
	 */
	String fromGridName = null;

	/**
	 * output grid name to which merged rows are added
	 */
	String toGridName = null;

	/**
	 * rows with the same keys are merged into one row
	 */
	String[] keyColumnNames = null;

	/**
	 * column that is to be concatenated
	 */
	String columnNameToBeMerged = null;

	/**
	 * name of the new column that has the merged(concatenated) values
	 */
	String mergedColumnName = null;

	/**
	 * columns values are concatenated with this as separator
	 */
	String rowSeparator = ";";

	@Override
	public int process(DataCollection dc) {
		Grid fromGrid = dc.getGrid(this.fromGridName);
		/*
		 * do we have the grid?
		 */
		if (fromGrid == null) {
			Spit.out(this.fromGridName + " not found");
			return 0;
		}

		/*
		 * is there any data?
		 */
		int nbrRows = fromGrid.getNumberOfRows();
		if (nbrRows == 0) {
			Spit.out(this.fromGridName + " has no data in it");
			return 0;
		}

		int nbrKeys = this.keyColumnNames.length;
		String[][] keyColumnTexts = new String[nbrKeys][];
		ValueList[] keyColumns = new ValueList[nbrKeys];
		for (int i = 0; i < nbrKeys; i++) {
			ValueList lst = fromGrid.getColumn(this.keyColumnNames[i]);
			keyColumns[i] = lst;
			keyColumnTexts[i] = lst.getTextList();
		}

		// find out which rows have different keys. It is assumed that the grid
		// is sorted.
		boolean[] keyChanged = new boolean[nbrRows];
		int newNbrRows = 1;
		for (int row = 1; row < keyChanged.length; row++) {
			for (int keyCol = 0; keyCol < nbrKeys; keyCol++) {
				if (keyColumnTexts[keyCol][row]
						.equals(keyColumnTexts[keyCol][row - 1])) {
					continue;
				}
				newNbrRows++;
				keyChanged[row] = true;
				break;
			}
		}
		Grid toGrid = new Grid(this.toGridName);
		ValueList[] newColumns = new ValueList[nbrKeys];
		ValueList columnToMerge = fromGrid.getColumn(this.columnNameToBeMerged);
		ValueList mergedColumn = ValueList.newList(DataValueType.TEXT,
				newNbrRows);
		try {
			for (int i = 0; i < nbrKeys; i++) {
				ValueList newColumn = ValueList.newList(
						keyColumns[i].getValueType(), newNbrRows);
				newColumns[i] = newColumn;
				toGrid.addColumn(this.keyColumnNames[i], newColumn);
			}
			toGrid.addColumn(this.mergedColumnName, mergedColumn);
		} catch (Exception e) {
			Spit.out(e);
			dc.addError(e.getMessage());
			return 0;
		}
		// push the first row..
		for (int j = 0; j < newColumns.length; j++) {
			newColumns[j].setValue(keyColumns[j].getValue(0), 0);
		}

		int newIdx = 0;
		String s = columnToMerge.getTextValue(0);

		for (int i = 1; i < nbrRows; i++) {
			if (keyChanged[i]) {
				/*
				 * save the merged value in the last row
				 */
				mergedColumn.setTextValue(s, newIdx);
				newIdx++;
				/*
				 * copy key fields to the next row
				 */
				for (int j = 0; j < newColumns.length; j++) {
					newColumns[j].setValue(keyColumns[j].getValue(i), newIdx);
				}
				/*
				 * and start merging the column
				 */
				s = columnToMerge.getTextValue(i);
			} else {
				s += this.rowSeparator + columnToMerge.getTextValue(i);
			}
		}
		/*
		 * last merged column is not yet added
		 */
		mergedColumn.setTextValue(s, newIdx);
		dc.addGrid(this.toGridName, toGrid);
		return newIdx + 1;
	}
}

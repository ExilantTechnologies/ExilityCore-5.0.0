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
 * split column into several columns. This is useful when, for whatever reason,
 * client insists on sending one column in which you have values of 2 or more
 * columns merged into one string using a separator. Example we use is size-qty.
 * Instead of sending size and qty as two columns, client is sending them in one
 * column. This utility helps you in splitting this column into two. Note that
 * individual cells within the columns should have the same number of cells
 * merged into one. That is, it is an error if first row has a value "s-20",
 * while second row has "s-20-m-30"
 */
class SplitColumns implements GridProcessorInterface, ToBeInitializedInterface {
	/**
	 * grid to be processed
	 */
	String gridName = null;

	/**
	 * column that contains a merged cell that needs to be split into more
	 * columns
	 */
	String columnNameToSplit = null;

	/**
	 * names to be used for the newly created columns after splitting
	 */
	String[] columnNamesToSplitInto;

	/**
	 * character that is used by the client to merge column values. For example
	 * "s-10" uses "-" to merge size and quantity columns
	 */
	String fieldSeparator = "-";

	/**
	 * data types of the columns cached for performance
	 */
	private DataValueType[] types;

	@Override
	public int process(DataCollection dc) {

		if (this.columnNamesToSplitInto == null) {
			dc.addError("SplitColumn for " + this.gridName
					+ " is not set up properly");
			return 0;
		}

		Grid grid = dc.getGrid(this.gridName);
		if (grid == null) {
			Spit.out(this.gridName
					+ " is not found for a splitColumn operation");
			return 0;
		}

		if (grid.hasColumn(this.columnNameToSplit) == false) {
			dc.addError(this.gridName + " does not teh column "
					+ this.columnNameToSplit + " for splitting");
			return 0;
		}

		int nbrRows = grid.getNumberOfRows();
		if (nbrRows == 0) {
			Spit.out(this.gridName + " has no rows.");
			return 0;
		}

		/**
		 * initially split this column into text columns
		 */
		int nbrCols = this.columnNamesToSplitInto.length;
		String[][] splitColumns = new String[nbrCols][];
		String[] blankValues = new String[nbrCols]; // to be used in case the
													// val is empty
		for (int j = 0; j < splitColumns.length; j++) {
			splitColumns[j] = new String[nbrRows];
		}

		String[] vals = grid.getColumnAsTextArray(this.columnNameToSplit);
		for (int i = 0; i < vals.length; i++) {
			String[] splitValues;
			String val = vals[i];
			if (val == null || val.length() == 0) {
				splitValues = blankValues;
			} else {
				splitValues = val.split(this.fieldSeparator);
			}

			if (splitValues.length != nbrCols) {
				dc.addError("Row " + (i + 1) + " has " + splitValues.length
						+ " values while we expected " + nbrCols
						+ " number of values. SplitColumns operation abandoned");
				return 0;
			}

			for (int j = 0; j < splitValues.length; j++) {
				splitColumns[j][i] = splitValues[j];
			}
		}

		/*
		 * OK. We have rid the text columns. Let us convert these into grid
		 * columns
		 */

		for (int j = 0; j < this.types.length; j++) {
			ValueList list = ValueList.newList(splitColumns[j], this.types[j]);
			try {
				grid.addColumn(this.columnNamesToSplitInto[j], list);
			} catch (ExilityException e) {
				dc.addError("Error while adding column "
						+ this.columnNamesToSplitInto[j]
						+ " as a split column to grid " + this.gridName
						+ ". : " + e.getMessage());
				return 0;
			}
		}
		return nbrRows;
	}

	@Override
	public void initialize() {
		if (this.columnNamesToSplitInto != null) {
			this.types = new DataValueType[this.columnNamesToSplitInto.length];
			for (int j = 0; j < this.columnNamesToSplitInto.length; j++) {
				String columnName = this.columnNamesToSplitInto[j];
				this.types[j] = DataDictionary.getValueType(columnName);
			}
		}
	}
}
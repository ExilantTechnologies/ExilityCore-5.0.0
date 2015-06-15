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
 * a column with merged values of two columns is added merge two columns into
 * one with a separator
 * 
 */
class MergeColumns implements GridProcessorInterface {
	/**
	 * grid that has the columns to be merged. Merged column is added to the
	 * same grid
	 */
	String gridName = null;

	/**
	 * two or more columns that are to be merged into one
	 */
	String[] columnNamesToMerge = null;

	/**
	 * name of the new column to be added to the grid
	 */
	String mergedColumnName = null;

	/**
	 * this separator is inserted while merging columns
	 */
	String fieldSeparator = "-";

	@Override
	public int process(DataCollection dc) {

		/*
		 * do we have the grid?
		 */
		Grid grid = dc.getGrid(this.gridName);
		if (grid == null) {
			Spit.out("input grid  " + this.gridName + " is ot found in dc");
			return 0;
		}

		/*
		 * is this processor set properly?
		 */
		if (this.columnNamesToMerge == null) {
			dc.addError(this.gridName
					+ " is ot set up properly for mergeColumn operation.");
			return 0;
		}

		/*
		 * Is there data at all?
		 */
		int nbrRows = grid.getNumberOfRows();

		if (nbrRows == 0) {
			Spit.out(this.gridName + " has no dara.");
			return 0;
		}
		int nbrFields = this.columnNamesToMerge.length;

		/*
		 * get columns to be merged into an array
		 */
		ValueList[] columnToMerge = new ValueList[nbrFields];
		for (int j = 0; j < columnToMerge.length; j++) {
			ValueList column = grid.getColumn(this.columnNamesToMerge[j]);
			if (column == null) {
				dc.addError(this.columnNamesToMerge[j]
						+ " is not found in grid " + this.gridName
						+ " for a merge column operation.");
				return 0;
			}
			columnToMerge[j] = column;
		}

		/*
		 * create an text array that has the merged values
		 */
		String[] mergedText = new String[nbrRows];
		for (int i = 0; i < mergedText.length; i++) {
			String s = columnToMerge[0].getTextValue(i);
			for (int j = 1; j < columnToMerge.length; j++) {
				s += this.fieldSeparator + columnToMerge[j].getTextValue(i);
			}
			mergedText[i] = s;
		}

		/*
		 * convert text array into valueList and add it to teh grid
		 */
		ValueList lst = ValueList.newList(mergedText);
		try {
			grid.addColumn(this.mergedColumnName, lst);
		} catch (ExilityException e) {
			Spit.out(e.getMessage());
			dc.addError(e.getMessage());
			return 0;
		}
		return 1;
	}
}
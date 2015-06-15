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

/**
 * This is reverse of aggregater, but only for append and concatenate
 * operations. Obviously, you cna not get the list of values from average of
 * them. But a concatenated string can be split back into individual values. One
 * row is added to the output grid for every split value, copying all other
 * columns as they are.
 * 
 */
public class Deaggregater implements GridProcessorInterface {

	/**
	 * grid from which to copy after de-aggregating
	 */
	String inputGridName = null;

	/**
	 * grid to which rows to be copied into
	 */
	String outputGridName = null;

	/**
	 * Name of the column being de-aggregated could be different in input and
	 * output grids
	 */

	String deaggrgatedInputColumnName = null;

	/**
	 * how are the values concatenated into the columns?
	 */
	String fieldSeparator = ",";
	/**
	 * column to be split
	 */
	String deaggrgatedOutputColumnName = null;

	/**
	 * let me give an example for clarity.
	 * 
	 * inputGridName = inputGrid; outputGridName = outputGrid;
	 * outputColumnName=field1,field2; deaggrgatedInputColumnName=qtyBySize
	 * deaggrgatedOutputColumnName=size additionalOutputColumnName=qty In this
	 * case, input grid should have columns with names field1, field2,
	 * qtyBySize. These columns could be in any order and there could be other
	 * columns also, (which we ignore) I will create an output grid with columns
	 * field1,field2,size,qty
	 */

	@Override
	public int process(DataCollection dc) {
		/**
		 * do we have the input grid?
		 */
		Grid grid = dc.getGrid(this.inputGridName);
		if (grid == null) {
			Spit.out(this.inputGridName
					+ " not found for a de-aggregaiton process.");
			return 0;
		}

		/**
		 * do we have data?
		 */
		int nbrInputRows = grid.getNumberOfRows();
		if (nbrInputRows == 0) {
			Spit.out(this.inputGridName + " has no data");
			return 0;
		}

		/**
		 * do we find the special column that we should be splitting?
		 */
		int colIdx = grid.getColumnIndex(this.deaggrgatedInputColumnName);
		if (colIdx == -1) {
			dc.addError(this.inputGridName + " does not have the column "
					+ this.deaggrgatedInputColumnName + " for deaggregation.");
			return 0;
		}

		int nbrCols = grid.getNumberOfColumns();
		String[][] inputRows = grid.getRawData();

		/*
		 * accumulate output rows in a list
		 */
		ArrayList<String[]> newRows = new ArrayList<String[]>();

		/*
		 * first row is heading
		 */
		String[] newRow = new String[nbrCols];
		newRows.add(newRow);
		String[] heading = inputRows[0];
		for (int j = 0; j < heading.length; j++) {
			newRow[j] = heading[j];
		}

		/*
		 * we have to substitute the right column name for the split column
		 */
		newRow[colIdx] = this.deaggrgatedOutputColumnName;

		/*
		 * Now, let us process each input row
		 */
		for (int i = 1; i < nbrInputRows; i++) {
			String[] inputRow = inputRows[0];
			String val = inputRow[colIdx];

			/*
			 * do we have a value to split at all?
			 */
			if (val == null) {
				continue;
			}

			val = val.trim();
			if (val.length() == 0) {
				continue;
			}

			/*
			 * add one row to output for every split value
			 */
			for (String splitValue : val.split(this.fieldSeparator)) {
				newRow = new String[nbrCols];
				newRows.add(newRow);

				/*
				 * substitute the split value in input row before copying
				 */
				inputRow[colIdx] = splitValue.trim();
				for (int j = 0; j < inputRow.length; j++) {
					newRow[j] = inputRow[j];
				}

			}
		}
		dc.addGrid(this.outputGridName, newRows.toArray(new String[0][]));
		return 1;
	}
}

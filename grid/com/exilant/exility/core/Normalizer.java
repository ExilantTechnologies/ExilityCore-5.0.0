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
 * This is the complement of denormalizer. Please refer to Denormalizer to
 * understand this utility
 */
public class Normalizer implements GridProcessorInterface {

	/**
	 * suffix to table name that is used as the name of grid where we get all
	 * possible keys
	 */
	static final String DISTINCT_KEYS_NAME = "RepeatedIds";

	/**
	 * grid being normalized
	 */
	String inputGridName = null;

	/*
	 * new grid to be created
	 */
	String outputGridName = null;

	/**
	 * column names to be added to the new grid
	 */
	String[] columnNames = null;

	@Override
	public int process(DataCollection dc) {

		Grid grid = dc.getGrid(this.inputGridName);
		if (grid == null) {
			Spit.out(this.inputGridName
					+ " not found for a normalization process");
			return 0;
		}

		int nbrRows = grid.getNumberOfRows();
		if (nbrRows == 0) {
			Spit.out(this.inputGridName + " has no data.");
			return 0;
		}
		int foreignKeyColIdx = grid.getColumnIndex(this.columnNames[0]);
		if (foreignKeyColIdx == -1) {
			Spit.out(this.columnNames[0] + " is not found as column in grid "
					+ this.inputGridName);
			return 0;
		}

		// distinct values of key are in a tab separated string in dc.values
		String keysName = this.inputGridName + DISTINCT_KEYS_NAME;
		String keysValue = dc.getTextValue(keysName, null);
		if (keysValue == null) {
			dc.addError(" A field named "
					+ keysName
					+ " is expected to have distinct keys. It is not found. Normalization of "
					+ this.inputGridName + " abordted.");
			return 0;
		}

		String[] keys = keysValue.split("\t");
		int nbrKeys = keys.length;

		int nbrFields = this.columnNames.length;

		String[][] inputGrid = grid.getRawData();
		int nbrVals = nbrRows - 1;

		/*
		 * Number of rows in the new grid = 1(for header) + nbrVals * nbrKeys
		 */
		nbrRows = 1 + (nbrVals * nbrKeys);

		/*
		 * It will have nbrFields number of columns
		 */
		String[][] newData = new String[nbrRows][];
		for (int i = 0; i < newData.length; i++) {
			newData[i] = new String[nbrFields];
		}

		/*
		 * header is the simplest to handle.
		 */
		for (int j = 0; j < nbrFields; j++) {
			newData[0][j] = this.columnNames[j];
		}

		/*
		 * let us first fill the first two columns. it is a cross product of
		 * foreign key and primary key. Start with 1 as we have already taken
		 * care of header
		 */
		int rowIdx = 1;
		for (int keyIdx = 0; keyIdx < nbrKeys; keyIdx++) {
			for (int inputRowIdx = 1; inputRowIdx <= nbrVals; inputRowIdx++) {
				newData[rowIdx][0] = inputGrid[inputRowIdx][foreignKeyColIdx];
				newData[rowIdx][1] = keys[keyIdx];
				rowIdx++;
			}
		}
		/**
		 * for each column other than the first one
		 */
		for (int colIdx = 2; colIdx < nbrFields; colIdx++) {
			rowIdx = 1;
			/**
			 * for each repetition of the repeated field
			 */
			for (int keyIdx = 0; keyIdx < nbrKeys; keyIdx++) {
				String colName = this.columnNames[colIdx] + "__" + keys[keyIdx];
				int thisColIdx = grid.getColumnIndex(colName);
				if (thisColIdx == -1) {
					dc.addError(colName + " not found as a column in grid "
							+ this.inputGridName
							+ ". Normalization process aborted.");
					return 0;
				}
				for (int inputRowIdx = 1; inputRowIdx <= nbrVals; inputRowIdx++) {
					newData[rowIdx][colIdx] = inputGrid[inputRowIdx][thisColIdx];
					rowIdx++;
				}
			}
		}
		dc.addGrid(this.outputGridName, newData);
		return 1;
	}
}

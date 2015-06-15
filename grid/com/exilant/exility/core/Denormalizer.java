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
 * De-normalizes two grids into one. This is similar to cross-tab of ms access.
 * Rows from second grid are 'de-normalized' as number of columns and appended
 * as additional columns to the main table. I am keeping this version simple
 * with the following restrictions on the designer
 * 
 * 0. main table should have its first column as key.
 * 
 * 1. child table should have the above key as its first column. Second column
 * should be the key for itself, and the third column is the displayable key.
 * For example the first three columns of child table woudl be productId, sizeId
 * and sizeName
 * 
 * 2. second column and third column should be on-to-one. That is, across two
 * rows, they cannot have different combination. For example, if in first row
 * second column is 2 and third one if XL then, in any row if second column is
 * 2, third has to be XL
 * 
 * 3. Unique values of second column are taken across all rows. If there are n
 * district values of this colun, then each row in the main grid will have n
 * additional columns for each field in the child grid.
 * 
 * 4. Fourth column onwards in the child table are denormalized and copied to
 * the main grid.
 * 
 * 5. District values of key, as well as displayed value of that are also added
 * to the dc as tab separated fields.
 * 
 * This arrangement suits the page.xml feature for repeating columns. It also
 * ties into the algorithm that normalizes a grid (reverse of this process)
 * 
 */
public class Denormalizer implements GridProcessorInterface {

	// two variables are pushed to dc that help client manage denormalized grid
	static final String REPEATED_IDS = "RepeatedIds";

	static final String REPEATED_HEADINGS = "RepeatedHeadings";

	static final String DENORMALIZED_PRIMARY_KEY_NAME = "DenormalizedPrimaryKeyName";

	static final String DENORMALIZED_SECONDARY_KEY_NAME = "DenormalizedSecondaryKeyName";

	String outputGridName = null;

	String inputParentGridName = null;

	String inputChildGridName = null;

	@Override
	public int process(DataCollection dc) {
		String[][] mainTable = dc.getGrid(this.inputParentGridName)
				.getRawData();
		String[][] childTable = dc.getGrid(this.inputChildGridName)
				.getRawData();
		int nbrMainRows = mainTable.length;
		int nbrChildRows = childTable.length;

		if ((nbrMainRows == 0) || (mainTable[0].length == 0)) {
			dc.addMessage("exilNoDataInGrid", this.inputParentGridName);
			return 0;
		}
		if ((nbrChildRows == 0) || (childTable[0].length == 0)) {
			dc.addMessage("exilNoDataInGrid", this.inputChildGridName);
			return 0;
		}

		int nbrMainCols = mainTable[0].length;
		int nbrChildCols = childTable[0].length;
		// this one contains a map of second column to third. That is it would
		// contain distinct child keys with its
		// display value
		Map<String, String> distinctKeys = new HashMap<String, String>();

		// and this one contains all the rows hashed by parentKey_childKey for
		// us to access them later
		Map<String, String[]> dataRows = new HashMap<String, String[]>();
		getHashedValues(childTable, distinctKeys, dataRows);

		int nbrDistinctKeys = distinctKeys.size();
		String[] uniqueKeys = new String[nbrDistinctKeys];

		// create tab separated string to push keys and display fields into
		// dc.values.
		// and, in the same process, we will get an array of keys for subsequent
		// use
		StringBuilder keysToOutput = new StringBuilder();
		StringBuilder displaysToOutput = new StringBuilder();
		int i = 0;
		for (String key : distinctKeys.keySet()) {
			uniqueKeys[i] = key;
			i++;
			keysToOutput.append(key).append('\t');
			displaysToOutput.append(distinctKeys.get(key)).append('\t');
		}
		dc.addTextValue(this.outputGridName + REPEATED_IDS, keysToOutput
				.delete(keysToOutput.length() - 1, 1).toString());
		dc.addTextValue(this.outputGridName + REPEATED_HEADINGS,
				displaysToOutput.delete(displaysToOutput.length() - 1, 1)
						.toString());
		dc.addTextValue(this.outputGridName + DENORMALIZED_PRIMARY_KEY_NAME,
				mainTable[0][0]);
		dc.addTextValue(this.outputGridName + DENORMALIZED_SECONDARY_KEY_NAME,
				childTable[0][1]);
		// OK. Let us extend the grid to accomodate new values
		int totalCols = nbrMainCols + (nbrChildCols - 3) * nbrDistinctKeys;
		String[][] grid = new String[nbrMainRows][];
		// copy existing data first
		for (i = 0; i < nbrMainRows; i++) {
			grid[i] = new String[totalCols];
			for (int j = 0; j < nbrMainCols; j++) {
				grid[i][j] = mainTable[i][j];
			}
		}
		// copy the header row values
		int mainColIdx = nbrMainCols;
		for (String childKey : uniqueKeys) {
			for (int j = 3; j < nbrChildCols; j++) {
				grid[0][mainColIdx] = childTable[0][j] + "__" + childKey;
				mainColIdx++;
			}
		}

		// child table may or may not have rows for every possible combination
		// of parent key and child key.
		// null string will have to be copied to such cell. For this, let us
		// have a blank row
		String[] blankRow = new String[nbrChildCols];
		for (int j = 0; j < nbrChildCols; j++) {
			blankRow[j] = "";
		}
		// now, let us copy teh rest of the grid
		// for each row in th emain table
		for (i = 1; i < nbrMainRows; i++) {
			String mainKey = mainTable[i][0];
			mainColIdx = nbrMainCols;
			// for each unique child key value
			for (String childKey : uniqueKeys) {
				String[] vals = dataRows.get(mainKey + "_" + childKey);
				if (vals == null) {
					vals = blankRow;
				}
				// copy each of the childColumns
				for (int j = 3; j < nbrChildCols; j++) {
					grid[i][mainColIdx] = vals[j];
					mainColIdx++;
				}
			}
		}
		// new grid will replace the existing two grids in dc, but hold on, i am
		// not sure whether teh same table may be
		// used later...
		// dc.RemoveGrid(this.mainTableName);
		// dc.RemoveGrid(this.childTableName);
		dc.addGrid(this.outputGridName, grid);
		return 1;
	}

	private static void getHashedValues(String[][] childTable,
			Map<String, String> distinctKeys, Map<String, String[]> dataRows) {
		int n = childTable.length;
		// skip the header and iterate from i = 1;
		for (int i = 1; i < n; i++) {
			String[] aRow = childTable[i];
			distinctKeys.put(aRow[1], aRow[2]);
			dataRows.put(aRow[0] + "_" + aRow[1], aRow);
		}
	}
}

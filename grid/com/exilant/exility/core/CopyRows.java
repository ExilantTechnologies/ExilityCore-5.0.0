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
 * Appends rows from one grid to another. If columns do not match, then only the
 * matched columns are copied, leaving the other columns empty for the appended
 * rows.
 * 
 * 
 */
public class CopyRows implements GridProcessorInterface {
	/**
	 * from grid
	 */
	public String fromGridName = null;

	/**
	 * to grid
	 */
	public String toGridName = null;

	/**
	 * optional column in the from-grid that may have value "delete" for rows
	 * that should not be copied.
	 */
	public String actionFieldName = null;

	@Override
	public int process(DataCollection dc) {
		Grid fromGrid = dc.getGrid(this.fromGridName);
		if (fromGrid == null) {
			Spit.out(this.fromGridName + " not found for copyRows operation.");
			return 0;
		}

		String[][] fromData = fromGrid.getRawData();
		if ((fromData.length == 0) || (fromData[0].length == 0)) {
			dc.addError("Input grid for append rows " + this.fromGridName
					+ " is not found in dc");
			return 0;
		}
		int nbrRows = fromData.length;
		String[] header = fromData[0];
		int nbrCols = header.length;
		int startAt = 1; // where to start copying rows to?

		String[] actions = null;
		if (this.actionFieldName != null) {
			try {
				actions = dc.getGrid(this.fromGridName)
						.getColumn(this.actionFieldName).getTextList();
				nbrRows = nbrRows
						+ 1
						- dc.getGrid(this.fromGridName)
								.filterRows(this.actionFieldName,
										Comparator.EQUALTO, "delete")
								.getRawData().length;
			} catch (ExilityException eEx) {
				dc.addError(eEx.getMessage());
				return 0;
			}
		}

		String[][] toGrid = new String[0][];
		if (dc.hasGrid(this.toGridName)) {
			toGrid = dc.getGrid(this.toGridName).getRawData();
		}
		if ((toGrid.length == 0) || (toGrid[0].length == 0)) {
			toGrid = new String[nbrRows][];
			toGrid[0] = fromData[0];
		} else {
			if (nbrCols != toGrid[0].length) {
				dc.addError(" grid  " + this.toGridName
						+ " is not found in dc for a copyRow operation.");
				return 0;
			}
			/*
			 * increase rows and copy existing rows in that
			 */
			String[][] tempGrid = new String[nbrRows + toGrid.length - 1][];
			for (int i = 0; i < toGrid.length; i++) {
				tempGrid[i] = toGrid[i];
			}
			startAt = toGrid.length;
			toGrid = tempGrid;
		}

		/*
		 * copy existing rows
		 */
		for (int i = 1; i < fromData.length; i++) {
			if (this.actionFieldName != null && actions != null) {
				String action = actions[i - 1];
				if (action.equals("delete")) {
					continue;
				}
			}
			toGrid[startAt] = fromData[i];
			startAt++;
		}

		dc.addGrid(this.toGridName, toGrid);
		return 1;
	}
}
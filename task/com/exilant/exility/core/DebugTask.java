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
 * output values of fields/tables at this state of execution
 */

public class DebugTask extends ExilityTask {
	/**
	 * print only these values (comma separated list)
	 */
	String valueName = null;

	@Override
	public int executeBulkTask(DataCollection dc, DbHandle handle)
			throws ExilityException {
		throw new ExilityException("Bulk Use task is not yet implemented");
	}

	@Override
	public int executeTask(DataCollection dc, DbHandle handle)
			throws ExilityException {
		int workDone = 0;
		String debugString = null;

		debugString = "<table border='1'><tr><td>";
		debugString += "Debug Task : " + this.taskName;
		debugString += "</td></tr>";
		if (this.valueName != null) {
			debugString += "<tr><td>values are </td></tr>";
			if (dc.values.containsKey(this.valueName)) {
				debugString += "<tr><td><table border='1'><tr><td><b>"
						+ this.valueName + "</b></td><td>"
						+ dc.values.get(this.valueName).toString()
						+ "</td></tr></table></td></tr>";
			}
		} else if (this.gridName != null) {
			debugString += "<tr><td>grids are </td></tr>";
			if (dc.grids.containsKey(this.gridName)) {
				Grid valGrid = dc.grids.get(this.gridName);
				String[] colNames = valGrid.getColumnNames();
				int noOfRows = valGrid.getNumberOfRows();
				int noOfCols = valGrid.getNumberOfColumns();

				debugString += "<tr><td><table border='1'><tr><td><b>"
						+ this.gridName + " has " + noOfRows + " rows and "
						+ noOfCols + " columns </b></td></tr>";
				debugString += "<tr>";
				for (int ctr = 0; ctr < noOfCols; ctr++) {
					debugString += "<td><b>" + colNames[ctr] + "</b></td>";
				}
				debugString += "</tr><tr>";
				for (int ctr = 0; ctr < noOfCols; ctr++) {
					debugString += "<td>"
							+ valGrid.getColumn(colNames[ctr]).getValueType()
							+ "</td>";
				}
				debugString += "</tr>";
				for (int rowCtr = 0; rowCtr < noOfRows; rowCtr++) {
					debugString += "<tr>";
					for (int colCtr = 0; colCtr < noOfCols; colCtr++) {
						debugString += "<td>"
								+ valGrid.getValue(colNames[colCtr], rowCtr)
										.toString() + "</td>";
					}
					debugString += "</tr>";
				}
				debugString += "</table></td></tr>";
			}
		} else {
			debugString += "<tr><td>values are </td></tr>";
			for (String key : dc.values.keySet()) {
				debugString += "<tr><td><table border='1'><tr><td><b>" + key
						+ "</b></td><td>" + dc.values.get(key).toString()
						+ "</td></tr></table></td></tr>";
			}
			debugString += "<tr><td>lists are </td></tr>";
			for (String key : dc.lists.keySet()) {
				ValueList valList = dc.lists.get(key);
				debugString += "<tr><td><table border='1'><tr><td><b>" + key
						+ " has " + valList.length() + " values </b></td></tr>";
				for (int ctr = 0; ctr < valList.length(); ctr++) {
					debugString += "<tr><td>"
							+ valList.getValue(ctr).toString() + "</td></tr>";
				}
				debugString += "</table></td></tr>";
			}
			debugString += "<tr><td>grids are </td></tr>";
			for (String key : dc.grids.keySet()) {
				Grid valGrid = dc.grids.get(key);
				String[] colNames = valGrid.getColumnNames();
				int noOfRows = valGrid.getNumberOfRows();
				int noOfCols = valGrid.getNumberOfColumns();

				debugString += "<tr><td><table border='1'><tr><td><b>" + key
						+ " has " + noOfRows + " rows and " + noOfCols
						+ " columns </b></td></tr>";
				debugString += "<tr>";
				for (int ctr = 0; ctr < noOfCols; ctr++) {
					debugString += "<td><b>" + colNames[ctr] + "</b></td>";
				}
				debugString += "</tr>";
				for (int rowCtr = 0; rowCtr < noOfRows; rowCtr++) {
					debugString += "<tr>";
					for (int colCtr = 0; colCtr < noOfCols; colCtr++) {
						debugString += "<td>"
								+ valGrid.getValue(colNames[colCtr], rowCtr)
										.toString() + "</td>";
					}
					debugString += "</tr>";
				}
				debugString += "</table></td></tr>";
			}
		}
		debugString += "</table>";
		Spit.out(debugString);

		workDone = 1;
		return workDone;
	}

}

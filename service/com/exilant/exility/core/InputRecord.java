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
 * represents a record/row/table that is used as input for a service
 */
public class InputRecord {

	/**
	 * name of the record. This record must be defined under records collection
	 */
	String recordName;

	/**
	 * If you are using a subset of fields from this record, provide a comma
	 * separated list here
	 */
	String[] fieldNames = null;

	/**
	 * min rows expected. Used for validating input.
	 */
	int minRows = 0;

	/**
	 * max rows to be expected. this will default to project level setting for
	 * safety against mass uploads that may choke the application
	 */
	int maxRows = AP.maxInputRows;

	/**
	 * why is this record being input?
	 */
	InputRecordPurpose purpose = InputRecordPurpose.selectiveUpdate;

	/**
	 * name of grid in which we are expecting data
	 */
	String gridName = null;

	/**
	 * parse fields of this record from inData into dc. Error if any will be
	 * added to dc. We parse all fields irrespective of validation errors.
	 * 
	 * @param inData
	 * @param dc
	 */
	void extractInput(ServiceData inData, DataCollection dc) {
		Record record = Records.getRecord(this.recordName);
		if (record == null) {
			dc.addError("Input record "
					+ this.recordName
					+ " is not a valid record, we are unable to extract input data.");
			return;
		}
		if (this.gridName != null) {
			int nbrRowsAdded = 0;
			String[][] data = inData.getGrid(this.gridName);
			Spit.out("Going to parse "
					+ this.gridName
					+ " as a grid with input grid "
					+ (data == null ? " null " : (" havinig "
							+ (data.length - 1) + " rows")));

			Grid grid = record.parseInputTable(data, dc, this.purpose,
					this.maxRows, this.fieldNames);
			if (grid == null) {
				Spit.out("Error : unable to parse " + this.gridName);
				/*
				 * we encountered errors. Error message would have been added to
				 * dc.
				 */
				return;
			}
			nbrRowsAdded = grid.getNumberOfRows();
			Spit.out(nbrRowsAdded + " rows parsed into " + this.gridName
					+ " as compared to a minimum of " + this.minRows);
			if (nbrRowsAdded >= this.minRows) {
				dc.addGrid(this.gridName, grid);
			} else {
				dc.addMessage(Messages.EXIL_MIN_ROWS, this.minRows + "");
			}

		} else {
			Spit.out("Going to parse " + this.recordName + " from values.");

			record.parseInputValues(inData, dc, this.purpose, this.fieldNames);
		}
	}
}

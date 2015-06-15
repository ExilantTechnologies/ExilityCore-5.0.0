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
 * represents a record/row/table that is used as input/output of a service
 * 
 */
public class OutputRecord {

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
	 * expecting a grid? null means values.
	 */
	String gridName = null;

	/**
	 * copy fields of this record from dc to outData
	 * 
	 * @param outData
	 * @param dc
	 */
	void extractOutput(ServiceData outData, DataCollection dc) {
		Record record = Records.getRecord(this.recordName);
		if (record == null) {
			Spit.out("Record "
					+ this.recordName
					+ " is not a valid record, we are unable to extract output data.");
			return;
		}
		if (this.gridName != null) {
			record.extractOutputTable(outData, dc, this.fieldNames,
					this.gridName);
		} else {
			record.extractOutputValues(outData, dc, this.fieldNames);
		}
	}
}

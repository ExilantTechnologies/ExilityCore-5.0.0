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
 * update some columns to a specific values for selected rows in a table
 * 
 */
class MassUpdateStep extends AbstractStep {
	/**
	 * criterion to identify rows for update
	 */
	Condition[] selectionCriterion = new Condition[0];

	/**
	 * table to be updated
	 */
	String table = null;

	/**
	 * columns to be updated. comma separated list of columns
	 */
	String columnsToUpdate = null;

	/**
	 * values to be updated to. Comma separated list. Should match the numbers
	 * in the columns
	 */
	String valuesToUpdate = null;

	MassUpdateStep() {
		this.stepType = StepType.MASSUPDATESTEP;
	}

	@Override
	String executeStep(DataCollection dc, DbHandle handle)
			throws ExilityException {
		TableInterface tableToUse = Tables.getTable(this.table, dc);
		tableToUse.massUpdate(dc, handle, this.selectionCriterion,
				this.columnsToUpdate, this.valuesToUpdate);
		return AbstractStep.NEXT;
	}

	@Override
	public DataAccessType getDataAccessType(DataCollection dc) {
		return DataAccessType.READWRITE;
	}
}
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
 * add a column to a grid. This is similar to addColumn grid processor, but not
 * exactly. grid processor evaluates the expression for each row while this step
 * evaluates the expression once against the dc and uses the same value for all
 * rows. Hence this is deprecated and we encourage use of grid processor. T
 * 
 */
@Deprecated
class AddColumnStep extends AbstractStep {
	/**
	 * name of the grid to which we are adding the column
	 */
	String gridName = null;

	/**
	 * name of the column to be added. This should be in data dictionary. Its
	 * data type should match the resultant data type of the expression
	 */
	String columnName = null;

	/**
	 * value to be added to the column. Expression is evaluated with dc and the
	 * resultant value is used as column value for all rows. If you want the
	 * expression to contains some of the columns as well, use grid processor
	 * instead.
	 */
	Expression expression = null;

	AddColumnStep() {
		this.stepType = StepType.ADDCOLUMNSTEP;
	}

	@Override
	String executeStep(DataCollection dc, DbHandle handle)
			throws ExilityException {
		if (this.gridName == null || this.gridName.equals("")
				|| this.columnName == null || this.columnName.length() == 0) {
			dc.addError(this.stepType + " not set properly.");
			return AbstractStep.STOP;
		}

		Grid grid = dc.getGrid(this.gridName);
		if (grid == null) {
			Spit.out(this.gridName + " not found in dc.");
			return AbstractStep.NEXT;
		}

		Value val = this.expression.evaluate(dc);

		ValueList valueList = new ValueList(grid.getNumberOfRows());
		for (int i = 0; i < valueList.length(); i++) {
			valueList.setValue(val, i);
		}
		grid.addColumn(this.columnName, valueList);

		dc.addGrid(this.gridName, grid);

		return AbstractStep.NEXT;
	}

}
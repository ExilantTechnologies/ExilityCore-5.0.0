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
 * create a grid from a set of lists
 * 
 */
class GridStep extends AbstractStep {
	/**
	 * name of the grid
	 */
	String name = null;

	/**
	 * name of columns in the grid. each if this column has to exist in dc as as
	 * list
	 */
	String[] columns = null;

	GridStep() {
		this.stepType = StepType.GRIDSTEP;
	}

	@Override
	String executeStep(DataCollection dc, DbHandle handle)
			throws ExilityException {
		if (this.name == null || this.name.equals("") || this.columns == null) {
			dc.addError("Grid step not set up properly");
			return AbstractStep.NEXT;
		}

		if (dc.hasGrid(this.name)) {
			Spit.out("grid " + this.name + " will be replaced with a new grid.");
		}

		Grid grid = new Grid(this.name);
		/*
		 * all columns should have same number of elements in them
		 */
		int nbrRows = -1;
		for (String curName : this.columns) {
			ValueList column = dc.getValueList(curName);

			/*
			 * do we have the column?
			 */
			if (column == null) {
				dc.addError("list named " + curName
						+ " not found in dc. gridStep is aborted.");
				return AbstractStep.NEXT;
			}

			/*
			 * do we have the right number of elements in it?
			 */
			if (nbrRows == -1) {
				nbrRows = column.length();
			} else if (nbrRows != column.length()) {
				dc.addError("list named " + curName + " is expected to have "
						+ nbrRows + " values but it has " + column.length()
						+ " values. grid " + this.name + " not created.");
				return AbstractStep.NEXT;
			}

			grid.addColumn(curName, column);
		}

		dc.addGrid(this.name, grid);

		return AbstractStep.NEXT;
	}
}
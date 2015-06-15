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
 * copy data from one grid to other, possibly with a subset of columns and rows
 * 
 */
class CopyGrid implements GridProcessorInterface {

	/**
	 * grid from which to copy from
	 */
	String fromGridName = null;

	/**
	 * grid to which data is to be copied to. Created if required, replaced if
	 * already present
	 */
	String toGridName = null;

	/**
	 * optional selection of columns to copy. By default all columns are copied
	 */
	String[] fromColumns = null;

	/**
	 * names of columns to copy to. By default, same column names are used.
	 */
	String[] toColumns = null;

	/**
	 * selective copying of rows. Name of the column that may contain value
	 * "delete" in which that row is not copied. This feature is deprecated. Use
	 * filterRows if you need to do something like this
	 */
	public String actionFieldName = null;

	@Override
	public int process(DataCollection dc) {
		Grid fromGrid = dc.getGrid(this.fromGridName);
		if (fromGrid == null) {
			Spit.out(this.fromGridName + " is not found. Can not copy grid.");
			return 0;
		}

		try {
			if (this.actionFieldName != null) {
				if (fromGrid.hasColumn(this.actionFieldName) == false) {
					dc.addError(this.actionFieldName
							+ " is not a valid column in grid "
							+ this.fromGridName);
					return 0;
				}
				/*
				 * filter rows based on actionFieldName
				 */
				Spit.out("Warning: actionFieldName feature is deprecated in copyGrid grid processor. Use FilterGrid if you need this feature.");
				fromGrid = fromGrid.filterRows(this.actionFieldName,
						Comparator.EQUALTO, "delete");
			}

			/*
			 * Determine what columns to copy from and to
			 */
			String[] fmNames = this.fromColumns == null ? fromGrid
					.getColumnNames() : this.fromColumns;
			String[] toNames = this.toColumns == null ? fmNames
					: this.toColumns;

			/*
			 * hope we have the right column names
			 */
			if (fmNames.length != toNames.length) {
				dc.addError("Mismatch of from and to columns for copy grid operation for grid "
						+ this.fromGridName);
				return 0;
			}

			Grid newGrid = new Grid(this.toGridName);
			for (int i = 0; i < fmNames.length; i++) {
				newGrid.addColumn(toNames[i], fromGrid.getColumn(fmNames[i])
						.clone());
			}
			return 1;
		} catch (ExilityException e) {
			dc.addError("Error while copying grid from " + this.fromGridName
					+ " to " + this.toGridName + ".  " + e.getMessage());
			return 0;
		}
	}
}
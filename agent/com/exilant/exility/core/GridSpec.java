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

/***
 * Exility design component that describes data requirements in a grid/table
 */
class GridSpec {
	String name = null;
	/***
	 * Is the grid itself is optional or is it mandatory
	 */
	boolean isOptional = false;

	/***
	 * are we expecting some min rows in the grid?
	 */
	int minRows = 0;

	/***
	 * is there a max number of rows beyond which we should not entertain?
	 */
	int maxRows = 0;

	/***
	 * specification for columns
	 */
	FieldSpec[] columns = new FieldSpec[0];

	GridSpec() {

	}

	/***
	 * copy grid/table from input data to dc based on this specification
	 * 
	 * @param inData
	 * @param dc
	 * @throws ExilityException
	 */
	void translateInput(ServiceData inData, DataCollection dc) {
		if (this.columns == null || this.columns.length == 0) {
			return; // columns are required for us to create a grid
		}

		String[][] data = inData.getGrid(this.name);
		if (data == null || data.length == 0) {
			if (this.isOptional == false) {
				dc.addMessage("exilGridIsRequired", this.name);
			}
			return;
		}

		int nbrDataRows = data.length - 1;
		if (nbrDataRows < this.minRows) {
			dc.addMessage("exilNotEnoughRows", this.name,
					String.valueOf(this.minRows), String.valueOf(nbrDataRows));
			return;
		}

		if ((this.maxRows > 0) && (nbrDataRows > this.maxRows)) {
			dc.addMessage("exilTooManyRows", this.name,
					String.valueOf(this.maxRows), String.valueOf(nbrDataRows));
			return;
		}

		Grid grid = new Grid(this.name);
		for (FieldSpec column : this.columns) {
			String fieldName = column.name;
			String[] columnData = this.getColumnData(fieldName, data);
			if (columnData == null) {
				dc.addMessage("exilNoColumnInGrid", this.name, fieldName);
				return;
			}
			AbstractDataType dt = column.getDataType();
			ValueList valueList = dt.getValueList(fieldName, columnData,
					column.isOptional, dc);
			try {
				grid.addColumn(fieldName, valueList);
			} catch (ExilityException e) {
				Spit.out(e);
				dc.addError(e.getMessage());
				return;
			}
		}

		dc.addGrid(this.name, grid);
	}

	/***
	 * extract a column data based on column heading in input data
	 * 
	 * @param fieldName
	 * @param data
	 * @return
	 */
	private String[] getColumnData(String fieldName, String[][] data) {
		int columnIndex = -1;
		String[] names = data[0];
		for (int i = 0; i < names.length; i++) {
			if (!names[i].equals(fieldName)) {
				continue;
			}
			columnIndex = i;
			break;
		}

		if (columnIndex < 0) {
			return null;
		}

		String[] columnData = new String[data.length - 1]; // first row has
															// names
		for (int row = 0; row < columnData.length; row++) {
			String[] aRow = data[row + 1];
			if (aRow.length > columnIndex) {
				columnData[row] = aRow[columnIndex];
			} else {
				columnData[row] = "";
			}
		}
		return columnData;
	}

	/***
	 * validate values in the grid column as per field spec of corresponding
	 * column
	 * 
	 * @param dc
	 * @param handle
	 * @return
	 * @throws ExilityException
	 */
	boolean validate(DataCollection dc, DbHandle handle) {
		boolean valueToReturn = true;
		for (FieldSpec column : this.columns) {
			valueToReturn = valueToReturn
					&& column.validateColumn(this.name, dc, handle);
		}

		return valueToReturn;
	}

	/***
	 * copy data from dc to output service data as per this specification
	 * 
	 * @param dc
	 * @param outData
	 */
	void translateOutput(DataCollection dc, ServiceData outData) {
		Grid grid = dc.getGrid(this.name);
		if (grid == null) {
			dc.addMessage("exilNoGrid", this.name);
			return;
		}

		String[][] data = grid.getRawData(this.columns);
		outData.addGrid(this.name, data);
	}

	/***
	 * copy grid from one dc to the other. This is called from internal method,
	 * and hence no validation is carried out
	 * 
	 * @param fromDc
	 * @param toDc
	 */
	void translate(DataCollection fromDc, DataCollection toDc) {
		toDc.addGrid(this.name, fromDc.getGrid(this.name));

	}
}
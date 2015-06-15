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
 * get data types defined in this project
 * 
 */
public class GetDataTypes implements CustomCodeInterface {
	// Large part of this small class is common between get and save. Hence save
	// extends get.
	@Override
	public int execute(DataCollection dc, DbHandle dbHandle, String gridName,
			String[] parameters) {
		String fullName = this.getFileName(dc);
		DataTypes types = this.readFile(dc, fullName);
		if (types == null) {
			return 0;
		}

		String[][] data = ObjectManager.getAttributes(types.dataTypes);
		Grid grid = new Grid(gridName);
		try {
			grid.setRawData(data);
		} catch (ExilityException e) {
			dc.addError("Error while parsing dataTypes : " + e.getMessage());
			return 0;
		}
		// we have an issue with max/min value of Decimal Value being defined as
		// double for DecimalDataTyep and long for INtegralValue
		// maxValue and minValue are defined as integralDataTypes in data
		// dictionary. With the result, data above has them as decimal
		// but they are parsed into long in grid. To flush decimal values out,
		// we will call raw data again
		grid.resetRawData();
		dc.addGrid(gridName, grid);
		return data.length - 1;
	}

	protected DataTypes readFile(DataCollection dc, String fullName) {
		Object obj = ResourceManager.loadResource(fullName, DataTypes.class);
		if (obj != null && obj instanceof DataTypes) {
			return (DataTypes) obj;
		}

		dc.addError(fullName
				+ " could not be read and interpreted as DataTypes");
		return null;
	}

	/**
	 * get data type name used by dataTypes
	 * 
	 * @param dc
	 * @return file name
	 */
	public String getFileName(DataCollection dc) {
		String fileName = dc.getTextValue(CommonFieldNames.FILE_NAME, "");
		String fullName = "dataTypes";
		if (fileName.length() > 0) {
			fullName = "dataType/" + fileName;
		}
		return fullName;
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.NONE;
	}
}

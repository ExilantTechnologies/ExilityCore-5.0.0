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
 * get columns for a table from database meta data
 * 
 */
public class GetDbColumns implements CustomCodeInterface {
	static String TABLE_NAME = "table";

	@Override
	public int execute(DataCollection dc, DbHandle dbHandle, String gridName,
			String[] parameters) {
		String tableName = dc.getTextValue(GetDbColumns.TABLE_NAME, "");
		if (tableName.length() == 0) {
			dc.addError("tableName not supplied for service getDbColumns");
			return 0;
		}
		try {
			Grid grid = DbHandle.getAllColumns(tableName);
			dc.addGrid(gridName, grid);
		} catch (ExilityException e) {
			dc.addError(e.getMessage());
			return 0;
		}
		return 1;
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.READONLY;
	}
}

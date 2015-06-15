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
 * Custom code that fetches details of a SQL template
 * 
 */
public class GetSql implements CustomCodeInterface {
	static String FOLDER_NAME = "folderName";
	static String FILE_NALE = "fileName";

	@Override
	public int execute(DataCollection dc, DbHandle dbHandle, String gridName,
			String[] parameters) {
		String sqlName = dc.getTextValue(GetSql.FILE_NALE, null);
		String folderName = dc.getTextValue(GetSql.FOLDER_NAME, "");
		if (folderName.length() > 0) {
			sqlName = folderName + '.' + sqlName;
		}

		SqlInterface sql = Sqls.getSqlTemplateOrNull(sqlName);
		if (sql != null && sql instanceof Sql) {
			ObjectManager.toDc(sql, dc);
			return 1;
		}

		dc.addMessage("error", "Unable to load " + sqlName
				+ " as a sql template.");
		return 0;
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.NONE;
	}
}

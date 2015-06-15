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
package com.exilant.exility.ide;

import com.exilant.exility.core.CustomCodeInterface;
import com.exilant.exility.core.DataAccessType;
import com.exilant.exility.core.DataCollection;
import com.exilant.exility.core.DbHandle;
import com.exilant.exility.core.ObjectManager;
import com.exilant.exility.core.ResourceManager;
import com.exilant.exility.core.Sql;
import com.exilant.exility.core.SqlInterface;

/***
 * Custom Code service to save a sql into resource folder structure
 * 
 * @author Exilant Technologies
 * 
 */
public class SaveSql implements CustomCodeInterface {
	static String FOLDER_NAME = "folderName";
	static String NAME = "name";

	@Override
	public int execute(DataCollection dc, DbHandle dbHandle, String gridName,
			String[] parameters) {
		String sqlName = dc.getTextValue(SaveSql.NAME, null);
		String folderName = dc.getTextValue(SaveSql.FOLDER_NAME, "");
		String qualifiedName = sqlName;
		if (folderName.length() > 0) {
			qualifiedName = folderName + '.' + sqlName;
		}

		SqlInterface sql = new Sql();
		ObjectManager.fromDc(sql, dc);
		ResourceManager.saveResource("sql." + qualifiedName, sql);
		return 1;
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.NONE;
	}
}

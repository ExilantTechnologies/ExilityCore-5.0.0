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
import com.exilant.exility.core.Record;
import com.exilant.exility.core.XlxUtil;

/**
 * Temp utility to call something from a page. This is linked to ide.temp
 * service entry
 * 
 */
public class Temp implements CustomCodeInterface {

	private static final String FILE_NAME = "D:/b/turtle/ExilityClient/webapp/WEB-INF/exilityResource/record/field.xlsx";

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.NONE;
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		DataCollection dc = new DataCollection();
		Temp temp = new Temp();
		temp.execute(dc, null, null, null);
	}

	@Override
	public int execute(DataCollection dc, DbHandle dbHandle, String gridName,
			String[] parameters) {
		XlxUtil.getInstance().extract(FILE_NAME, dc, false);
		Record record = new Record();
		ObjectManager.fromDc(record, dc);
		return 0;
	}
}

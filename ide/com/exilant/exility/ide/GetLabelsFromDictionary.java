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
import com.exilant.exility.core.DataDictionary;
import com.exilant.exility.core.DbHandle;
import com.exilant.exility.core.Dubhashi;
import com.exilant.exility.core.ResourceManager;
import com.exilant.exility.core.Spit;
import com.exilant.exility.core.XlxUtil;

/**
 * service that generates or updates translations.xlsx file with labels from
 * data dictionary
 * 
 */
public class GetLabelsFromDictionary implements CustomCodeInterface {

	private static final String TRACE_TEXT = "traceText";

	@Override
	public int execute(DataCollection dc, DbHandle handle, String gridName,
			String[] params) {
		/**
		 * we want to get the trace text to the page irrespective of trace
		 * setting. So, we will push that into a specific field that the client
		 * page show
		 */
		String existingText = Spit.startWriter();
		String[][] labels = DataDictionary.getAllLabels();
		String fileName = ResourceManager.getResourceFolder()
				+ Dubhashi.TRANSLATIONS_FILE_PATH_FROM_RESOURCE;
		String resultText = "Translation file " + fileName;
		if (XlxUtil.getInstance().appendMissingOnes(fileName, labels)) {
			Spit.out(resultText + " saved.");
		} else {
			Spit.out(resultText + " NOT saved.");
		}
		String traceText = Spit.stopWriter();
		/**
		 * let us be a responsible Spit user. If writer was on, let us ensure
		 * that there is no change in its behavior because of us.
		 */
		if (existingText != null) {
			Spit.startWriter();
			Spit.out(existingText);
			Spit.out(traceText);
		}
		dc.addTextValue(TRACE_TEXT, traceText);
		return 1;
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.NONE;
	}
}

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
import com.exilant.exility.core.DataTypesGenerator;
import com.exilant.exility.core.DbHandle;
import com.exilant.exility.core.ResourceManager;
import com.exilant.exility.core.Spit;

/**
 * Generates dataTypes.js and dataTypes.xsd based on data types defined in this
 * project.
 * 
 */
public class GenerateDataTypes implements CustomCodeInterface {
	static String JS_FILE_NAME = "dataTypes.js";
	static String XSD_FILE_NAME = "dataTypes.xsd";

	@Override
	public int execute(DataCollection dc, DbHandle dbHandle, String gridName,
			String[] parameters) {
		try {
			String spittedSoFar = Spit.startWriter();
			String fileName = ResourceManager.getResourceFolder()
					+ GenerateDataTypes.JS_FILE_NAME;
			String txt = DataTypesGenerator.toJavaScript();
			ResourceManager.saveText(fileName, txt);
			Spit.out(fileName
					+ " saved. You may have to copy to a different place depending on your include tags in your home page.");

			fileName = ResourceManager.getResourceFolder()
					+ GenerateDataTypes.XSD_FILE_NAME;
			txt = DataTypesGenerator.toXsd();
			ResourceManager.saveText(fileName, txt);
			Spit.out(fileName + " saved as schema include for data types.");
			dc.addTextValue("traceText", Spit.stopWriter());
			Spit.out(spittedSoFar);
			return 1;
		} catch (Exception e) {
			String txt = "Unable to generate and save data types. "
					+ e.getMessage();
			dc.addError(txt);
			Spit.out(txt);
			return 0;
		}
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.NONE;
	}
}

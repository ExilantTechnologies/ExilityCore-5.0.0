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
 * Custom code that fetches details of a Service
 * 
 */
public class GetService implements CustomCodeInterface {
	static final String STEPS_NAME = "steps";
	static final String ID_PREFIX = "step_";
	static final String ID_NAME = "id";
	static String FOLDER_NAME = "folderName";
	static String FILE_NAME = "fileName";
	static String STEP_ATTRIBUTES_NAME = "stepAttributes";

	@Override
	public int execute(DataCollection dc, DbHandle dbHandle, String gridName,
			String[] parameters) {
		String serviceName = dc.getTextValue(GetService.FILE_NAME, null);
		String folderName = dc.getTextValue(GetService.FOLDER_NAME, "");
		if (folderName.length() > 0) {
			serviceName = folderName + '.' + serviceName;
		}

		ServiceInterface s = Services.getService(serviceName, dc);
		if (s == null || s instanceof Service == false) {
			dc.addError("Unable to load " + serviceName + " as a service.");
			return 0;
		}
		Service service = (Service) s;
		ObjectManager.toDc(service, dc);
		// Above method does not extract all attributes of all types of steps.
		// We need a specialized method for that
		ObjectManager.toDc(service.steps, dc, GetService.ID_PREFIX,
				GetService.STEP_ATTRIBUTES_NAME);
		// we also need to add an id column to steps
		Grid steps = dc.getGrid(GetService.STEPS_NAME);
		int nbrSteps = steps.getNumberOfRows();
		String[] ids = new String[nbrSteps];
		for (int i = 0; i < nbrSteps; i++) {
			ids[i] = GetService.ID_PREFIX + (i + 1);
		}
		ValueList column = ValueList.newList(ids);
		try {
			steps.addColumn(GetService.ID_NAME, column);
		} catch (ExilityException e) {
			dc.addError("Error while adding id columns. This is an internal programming error.");
			return 0;
		}
		return 1;
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.NONE;
	}
}

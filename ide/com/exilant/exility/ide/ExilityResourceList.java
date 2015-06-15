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
import com.exilant.exility.core.ResourceManager;
import com.exilant.exility.core.Util;

/**
 * Custom code that returns list of resources
 * 
 */
public class ExilityResourceList implements CustomCodeInterface {
	static String RESOURCE_TYPE = "resourceType";
	static String FOLDER_NAME = "folderName";
	static String TO_RECURSE = "toRecurse";
	static String EXILITY_ERROR = "exilityError";

	@Override
	public int execute(DataCollection dc, DbHandle dbHandle, String gridName,
			String[] parameters) {
		String resourceTypeName = dc.getTextValue(
				ExilityResourceList.RESOURCE_TYPE, null);
		String folderName = dc
				.getTextValue(ExilityResourceList.FOLDER_NAME, "");
		if (folderName.length() == 0) {
			folderName = resourceTypeName;
		} else {
			folderName = resourceTypeName + '/' + folderName.replace('.', '/');
		}

		String[] folderNames = ResourceManager.getResourceList(folderName,
				".xml");
		dc.addGrid(gridName, Util.namesToGrid(folderNames, "resourceName"));
		return folderNames.length;
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.NONE;
	}
}

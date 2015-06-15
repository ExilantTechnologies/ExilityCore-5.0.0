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

/**
 * get folders for a resource type
 * 
 */
public class GetFolders implements CustomCodeInterface {
	private static final String DEFAULT_GRID_NAME = "folders";
	static final String RESOURCE_TYPE = "resourceType";
	static final String FOLDER_NAME = "folderName";
	private static final String[] HEADER = { "internal", "name" };
	private static final String[] ROOT = { ".", "root folder" };

	@Override
	public int execute(DataCollection dc, DbHandle dbHandle, String gridName,
			String[] parameters) {
		String rootFolderName = dc.getTextValue(GetFolders.RESOURCE_TYPE,
				"page");
		if (rootFolderName.equals("dataDictionary")) {
			rootFolderName = "dictionary";
		}
		String[] folderNames = ResourceManager
				.getResourceFolders(rootFolderName);
		// let us add root folder to this list and send it to client in a format
		// that is good for drop-down..
		int n = folderNames.length;
		String[][] grid = new String[n + 2][]; // one for header and another for
												// root folder
		grid[0] = GetFolders.HEADER;
		grid[1] = GetFolders.ROOT;
		int i = 2;
		for (String fn : folderNames) {
			String[] row = { fn, fn };
			grid[i] = row;
			i++;
		}
		String gridNameToUse = gridName == null ? DEFAULT_GRID_NAME : gridName;
		dc.addGrid(gridNameToUse, grid);
		return folderNames.length;
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.NONE;
	}
}

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
import com.exilant.exility.core.DataTypes;
import com.exilant.exility.core.DbHandle;
import com.exilant.exility.core.ExilityException;
import com.exilant.exility.core.Grid;
import com.exilant.exility.core.Messages;
import com.exilant.exility.core.ResourceManager;
import com.exilant.exility.core.ServiceList;
import com.exilant.exility.core.ValueList;

/**
 * Get all resources as sorted lists
 * 
 */
public class GetAllResourceLists implements CustomCodeInterface {

	private static final String SERVICE_ENTRIES = "serviceEntries";
	private static final String DATA_TYPES = "dataTypes";
	private static final String MESSAGES = "messages";
	private static final String DATA_ELEMENTS = "dataElements";
	private static final String SERVICES = "services";
	private static final String SQLS = "sqls";
	private static final String TABLES = "tables";
	private static final String DB_TABLES = "dbTables";
	private static final String PAGES = "pages";

	@Override
	public int execute(DataCollection dc, DbHandle dbHandle, String gridName,
			String[] parameters) {
		String[] allNames = ServiceList.getAllEntries();
		this.addGrid(SERVICE_ENTRIES, allNames, dc);

		allNames = DataTypes.getAllDataTypes();
		this.addGrid(DATA_TYPES, allNames, dc);

		allNames = Messages.gelAllMessages();
		this.addGrid(MESSAGES, allNames, dc);

		allNames = DataDictionary.getAllElements();
		this.addGrid(DATA_ELEMENTS, allNames, dc);

		allNames = ResourceManager.getResourceList("service", ".xml");
		this.addGrid(SERVICES, allNames, dc);

		allNames = ResourceManager.getResourceList("sql", ".xml");
		this.addGrid(SQLS, allNames, dc);

		allNames = ResourceManager.getResourceList("table", ".xml");
		this.addGrid(TABLES, allNames, dc);

		allNames = ResourceManager.getResourceList("page", ".xml");
		this.addGrid(PAGES, allNames, dc);

		try {
			dc.addGrid(DB_TABLES, DbHandle.getAllTables());
		} catch (ExilityException e) {
			dc.addInfo("Error while getting db tables. " + e.getMessage());
		}
		return 1;

	}

	private void addGrid(String gridName, String[] names, DataCollection dc) {
		try {
			Grid grid = new Grid(gridName);
			grid.addColumn("name", ValueList.newList(names));
			dc.addGrid(gridName, grid);
		} catch (ExilityException ex) {
			dc.addError("Unable to add resource list for " + gridName
					+ " Error : " + ex.getMessage());
		}
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.NONE;
	}
}

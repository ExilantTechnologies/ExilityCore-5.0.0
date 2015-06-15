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

import java.util.HashMap;

/**
 * This class is responsible for loading Table classes. There are two modes of
 * operation: 1. Xml definition driven - When Xml definition files are used.
 * These are stored under folder named table under resource folder. 2. Custom
 * classes - java code that implements table interface. These are typically in
 * the project specific jar file. This class manages these so that the caller
 * can just ask for a table, without knowing whether it is java or xml.
 */

public class Tables {
	/***
	 * xml loading is expensive. This map caches xmls that are loaded into
	 * object instances. However, during development, programmers keep changing
	 * these frequently, and hence caching becomes a problem. Hence caching is
	 * controlled through "definitionsToBeCached" parameter in
	 * applicationParameters.xml This also caches the Java class instance for
	 * compatibility.
	 */
	private static final HashMap<String, TableInterface> tables = new HashMap<String, TableInterface>();

	/**
	 * Internal method to get the Table object by name
	 * 
	 * @param tableDefinitionName
	 *            The name of the table definition to load
	 * @param dc
	 *            Instance of data collection object
	 * @return An interface of type TableInterface
	 */
	public static TableInterface getTable(String tableDefinitionName,
			DataCollection dc) {
		TableInterface table = Tables.getTableWorker(tableDefinitionName);
		String err = null;
		if (table == null) {
			err = "table definition not found for " + tableDefinitionName;
		} else if (tableDefinitionName.endsWith(table.getName()) == false) {
			err = "File "
					+ tableDefinitionName
					+ " contains a table definition, but it's name is wrongly put as "
					+ table.getName();
		} else {
			return table;
		}

		Spit.out(err);
		if (dc != null) {
			dc.addError(err);
		}
		return null;
	}

	/**
	 * Return the table object if found else return null
	 * 
	 * @param tableDefinitionName
	 *            The name of the table definition to load
	 * @return An interface of type TableInterface if found, else null
	 */
	public static TableInterface getTableOrNull(String tableDefinitionName) {
		return Tables.getTableWorker(tableDefinitionName);
	}

	/**
	 * Save the definition of the object as an Xml file in a sub-directory
	 * called "table" in the "resources" folder. The Xml file name is the name
	 * of the Table object
	 * 
	 * @param table
	 *            In-memory representation of the object that implements
	 *            TableInterface whose definition has to be stored as Xml
	 * @param dc
	 *            Instance of data collection object
	 */

	static void saveTable(TableInterface table, DataCollection dc) // throws
																	// ExilityException
	{
		ResourceManager.saveResource("table." + table.getName(), table);
	}

	/**
	 * Private method to load the object. Looks for java class first if
	 * "projectPackageName" failing which looks for an xml Null is returned if
	 * definition is not found. If found, it could b cached based on application
	 * setting. (definitionsToBecached)
	 * 
	 * @param tableDefinitionName
	 *            The name of the table definition to load
	 * @param dc
	 *            Instance of data collection object
	 * @param raiseExceptionIfNotFound
	 *            Whether to raise an exception or not when class is not found
	 * @return An interface of type TableInterface
	 * @throws ExilityException
	 */
	private static TableInterface getTableWorker(String tableDefinitionName) {
		if (Tables.tables.containsKey(tableDefinitionName)) {
			return Tables.tables.get(tableDefinitionName);
		}

		TableInterface table = null;

		table = (Table) ResourceManager.loadResource("table."
				+ tableDefinitionName, Table.class);
		if (table == null || table.getName() == null) {
			return null;
		}
		if (AP.definitionsToBeCached) {
			Tables.tables.put(tableDefinitionName, table);
		}
		return table;
	}

	static void flush() {
		Tables.tables.clear();
	}

}
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

/***
 * Manages sql objects.
 * 
 */
public class Sqls {

	/**
	 * fields that are input
	 */
	public static final int FILTER_ALL_INPUT = 0;

	/**
	 * input fields with justLeavemeLone set to true
	 */
	public static final int FILTER_LEAVE_ME_ALONE = 1;

	private static final HashMap<String, SqlInterface> sqls = new HashMap<String, SqlInterface>();

	/***
	 * Returns a sql object. if it is not found, null is returned, but exception
	 * is not raised.
	 * 
	 * @param name
	 *            name of sql
	 * @return sql object or null
	 */
	public static SqlInterface getSqlTemplateOrNull(String name) {
		return Sqls.getTemplateWorker(name);
	}

	/***
	 * Returns a sql object. if it is not found, exception is raised.
	 * 
	 * @param name
	 *            name of sql
	 * @param dc
	 *            dc
	 * @return sql object or an exception is raised
	 * @throws ExilityException
	 */
	public static SqlInterface getTemplate(String name, DataCollection dc)
			throws ExilityException {
		if ((name == null) || (name.length() == 0)) {
			return null;
		}

		SqlInterface sql = Sqls.getTemplateWorker(name);
		String err = null;
		if (sql == null) {
			err = "Sql with name " + name + " not found.";
		} else if (name.endsWith(sql.getName()) == false) {
			err = "File " + name + " has a sql with its name wrongly set to "
					+ sql.getName();
		} else {
			return sql;
		}

		if (dc == null) {
			Spit.out(err);
		} else {
			dc.addError(err);
		}
		return null;
	}

	/***
	 * worker method created to retain compatibility with some existing callers.
	 * This method will return null if sql can not be found.
	 * 
	 * @param name
	 *            name of Sql template
	 * @param dc
	 * @return sql object or null
	 */
	private static SqlInterface getTemplateWorker(String name) {
		if ((name == null) || (name.length() == 0)) {
			return null;
		}
		SqlInterface sql = null;

		if (Sqls.sqls.containsKey(name)) {
			return Sqls.sqls.get(name);
		}

		// is it a java class?
		// disabling for the time being
		// if(AP.projectPackageName != null)
		// {
		// sql = (SqlInterface)ObjectManager.createNew(AP.projectPackageName,
		// name);
		// if(sql != null)
		// {
		// sqls.put(name, sql);
		// return sql;
		// }
		// }

		// try xml resource
		Object object = ResourceManager.loadResource("sql." + name, Sql.class);
		if (object == null) {
			return null;
		}
		if (object instanceof Sql == false) {
			Spit.out(name
					+ " is found in sql folder, but it is not a SQl. It is of type "
					+ object.getClass().getName());
			return null;
		}
		sql = (Sql) object;
		if (sql.getName() == null) {
			Spit.out(name + " is not a valid sql");
			return null;
		}

		if (AP.definitionsToBeCached) {
			Sqls.sqls.put(name, sql);
		}
		return sql;
	}

	/***
	 * Gets a dynamic sql string from the template. This is a short cut method
	 * to creating a sql object and invoking its getSql() method
	 * 
	 * @param name
	 *            sql template name
	 * @param dc
	 * @return dynamic sql
	 * @throws ExilityException
	 */
	public static String getSql(String name, DataCollection dc)
			throws ExilityException {
		SqlInterface sql = Sqls.getTemplateWorker(name);
		if (sql != null && sql instanceof Sql) {
			return ((Sql) sql).getSql(dc);
		}
		throw new ExilityException("No sql tempalte with name " + name);
	}

	/***
	 * extracts attributes of sql objects from dc and saves it into resource
	 * folder
	 * 
	 * @param sql
	 *            instance of sql.
	 * @param dc
	 */
	static void save(Sql sql, DataCollection dc)// throws ExilityException
	{
		ResourceManager.saveResource("sql." + sql.name, sql);
	}

	static void flush() {
		Sqls.sqls.clear();
	}
}
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

/**
 * execute a sql
 * 
 * 
 */
public class SqlTask extends ExilityTask {
	/*
	 * Exility uses a naming convention to carry data in a common data carrier
	 * (DC) Some designers use unique column names across tables, typically by
	 * using common prefix to each column in a table (foreign keys retain their
	 * names without the prefix for that table) If you do not follow such a
	 * scheme, you may have problem in using dc to carry attributes for more
	 * than one table prefix helps you in attaching a prefix to all the columns
	 * extracted by the step. If you do not use this attribute, you may have to
	 * use prefixes in your select .. as ..
	 */
	String prefix = null;

	@Override
	public int executeTask(DataCollection dc, DbHandle handle)
			throws ExilityException {
		SqlInterface sql = Sqls.getTemplate(this.taskName, dc);
		return sql.execute(dc, handle, this.gridName, this.prefix);
	}

	@Override
	public int executeBulkTask(DataCollection dc, DbHandle handle)
			throws ExilityException {
		SqlInterface sql = Sqls.getTemplate(this.taskName, dc);
		return sql.executeBulkTask(dc, handle, this.gridName, false);
	}

	@Override
	public DataAccessType getDataAccessType() {
		DataCollection dc = new DataCollection();
		try {
			SqlInterface sql = Sqls.getTemplate(this.taskName, dc);
			return sql.getDataAccessType();
		} catch (ExilityException e) {
			// sql is not available. Let there be an issue later as well :-)
			return DataAccessType.NONE;
		}
	}
}

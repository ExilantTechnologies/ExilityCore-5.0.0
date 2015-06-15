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
 * 
 * Represents set of methods a class should implement to be used by Sql-based
 * tasks.
 * 
 */
public interface SqlInterface {
	/***
	 * for documentation
	 * 
	 * @return name
	 */
	public String getName();

	/***
	 * 
	 * @param dc
	 *            standard data carrier
	 * @param handle
	 *            DbHandle that manages all io
	 * @param gridName
	 * @param prefix
	 * @gridName grid into which extracted data is to be put into
	 * @return number of rows selected or affected
	 * @throws ExilityException
	 */
	public int execute(DataCollection dc, DbHandle handle, String gridName,
			String prefix) throws ExilityException;

	/**
	 * executes a sql statement repeatedly for each row of the grid and returns
	 * total number of affected row. sql to be executed has to be a dml sql, and
	 * not a data extraction sql. An exception to this is if forValidation is
	 * set to true. In this case, the sql is to be for extraction. Rows are
	 * actually not extracted, but number of rows in the result set is saved in
	 * to a column. Caller can use this information to validate some referential
	 * requirements. Refer to fieldSPec
	 * 
	 * @param dc
	 * @param handle
	 * @param gridName
	 *            name of the grid for which the sql is to be repeated
	 * @param forValidation
	 *            If set to true, a column with name "gridName + KeyFound" will
	 *            be added to the grid. This column will be populated with
	 *            number of extracted rows for that row
	 * @return number of rows that were updated/inserted
	 * @throws ExilityException
	 */
	public int executeBulkTask(DataCollection dc, DbHandle handle,
			String gridName, boolean forValidation) throws ExilityException;

	/***
	 * get the database access requirement of this sql
	 * 
	 * @return database access requirement of this sql
	 */
	public DataAccessType getDataAccessType();

	/**
	 * piece of patch-work. We had discouraged SQL designers from using output
	 * parameters. Programmers invariably used to have mismatch between output
	 * parameters and the select clause in sql. To avoid this, we started using
	 * met data from sql. And for the sake of optimization, we keep the meta
	 * data back in sql for re-use. getColumnInfo() and setColumnInfo() were
	 * born with that. Then we added table based tasks, and hence th eneed to
	 * put this into interface
	 * 
	 * @return output columns expected from this sql, null if you don't have it.
	 */
	public OutputColumn[] getColumnInfo();

	/**
	 * refer to getCOlumnInfo(). dbHandle will initially ask for
	 * getColumnInfo(). If that returns null, then dbHandle will get it from
	 * meta-data nd call this method on sql for it to ba cached
	 * 
	 * @param columnInfo
	 */
	public void setColumnInfo(OutputColumn[] columnInfo);
}

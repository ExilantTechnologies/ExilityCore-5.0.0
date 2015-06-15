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
 * Represents set of methods a class should implement to be used by table-based
 * tasks.
 * 
 */
public interface TableInterface {
	/***
	 * for documentation
	 * 
	 * @return name of this table
	 */
	public String getName();

	/**
	 * Simple key based read.
	 * 
	 * @param dc
	 * @param handle
	 * @return 1 if row was read into dc, 0 otherwise
	 * @throws ExilityException
	 */
	public int readbasedOnKey(DataCollection dc, DbHandle handle)
			throws ExilityException;

	/***
	 * select a row from the table corresponding to the primary key value/s
	 * supplied in dc.values
	 * 
	 * @param dc
	 * @param handle
	 * @param gridName
	 *            name of the grid in which output to be added to dc. if this is
	 *            null, row is extracted into dc.values as name-value pairs.
	 * @param prefix
	 *            if non-null, all column names are prefixed with this
	 * @return number of rows extracted
	 * @throws ExilityException
	 */
	public int read(DataCollection dc, DbHandle handle, String gridName,
			String prefix) throws ExilityException;

	/***
	 * Rows from table are selected based on primary key in each line of input
	 * grid.
	 * 
	 * @param dc
	 * @param handle
	 * @param gridName
	 *            name in which output rows are to be added to dc
	 * @param grid
	 *            input grid that contains primary keys in each of its row
	 * @return total number of rows extracted
	 * @throws ExilityException
	 */
	public int massRead(DataCollection dc, DbHandle handle, String gridName,
			Grid grid) throws ExilityException;

	/***
	 * Adds a grid to dc with the desired columns. Rows from underlying
	 * table/view are filtered and sorted as per supplied criteria
	 * 
	 * @param dc
	 * @param handle
	 * @param gridName
	 *            grid is added to dc with this name
	 * @param conditions
	 *            array conditions. Rows that satisfy all conditions in this
	 *            array are filtered
	 * @param columnsToSelect
	 *            comma separated names of columns to be extracted. Output grid
	 *            will have these columns. null implies that all columns are
	 *            extracted. We recommend that you typically extract all rows,
	 *            except if it is a security issues.
	 * @param columnsToSort
	 *            null if no sort is required. Comma separated names of columns
	 *            based on which rows in the resultant grid are to be sorted.
	 *            Note that these columns need not be selected for output, but
	 *            you rarely do that.
	 * @param sortOrder
	 *            desc for descending, otherwise ascending. Note that you can
	 *            not change ascending order for different columns. All columns
	 *            are sorted by this order
	 * @param prefix
	 *            if non-null, all selected column names are prefixed with this
	 * @param eliminateDuplicates
	 *            if true, duplicate rows, rows that contain identical values
	 *            for each column, are eliminated
	 * @return number of rows added to grid. can be zero.
	 * @throws ExilityException
	 */
	public int filter(DataCollection dc, DbHandle handle, String gridName,
			Condition[] conditions, String columnsToSelect,
			String columnsToSort, String sortOrder, String prefix,
			boolean eliminateDuplicates) throws ExilityException;

	/***
	 * insert a row into the underlying table, after generating primary key if
	 * required, from values supplied in dc.values. For missing columns,
	 * Implementation should insert default values wherever possible, failing
	 * which ExilityException should be throw after inserting appropriate
	 * message in dc.
	 * 
	 * @param dc
	 * @param handle
	 * @return 1 if row was inserted, else 0
	 * @throws ExilityException
	 */
	public int insert(DataCollection dc, DbHandle handle)
			throws ExilityException;

	/***
	 * insert rows from grid into the underlying table. For missing columns,
	 * Implementation should insert default values wherever possible, failing
	 * which ExilityException should be throw after inserting appropriate
	 * message in dc.
	 * 
	 * @param dc
	 * @param handle
	 * @param grid
	 *            grid with rows to be inserted
	 * @return total number of rows inserted
	 * @throws ExilityException
	 */
	public int insertFromGrid(DataCollection dc, DbHandle handle, Grid grid)
			throws ExilityException;

	/***
	 * update a row in the underlying table based on the primary key that is
	 * supplied in dc.values. Only values that are supplied are updated. Other
	 * columns, if any, inthe table are left untouched.
	 * 
	 * @param dc
	 * @param handle
	 * @return 1 if row was updated, else 0
	 * @throws ExilityException
	 */
	public int update(DataCollection dc, DbHandle handle)
			throws ExilityException;

	/***
	 * update rows corresponding to the lines in the grid. Each rows supplied
	 * primary key to identify the row to be updated, and the columns to be
	 * updated.
	 * 
	 * @param dc
	 * @param handle
	 * @param grid
	 *            grid that has rows to be updated
	 * @return number of rows updated
	 * @throws ExilityException
	 */
	public int updateFromGrid(DataCollection dc, DbHandle handle, Grid grid)
			throws ExilityException;

	/***
	 * update rows from table that satisfy supplied conditions. Only the columns
	 * that are supplied are updated, and the rest of the columns are left
	 * unchanged
	 * 
	 * @param dc
	 * @param handle
	 * @param conditions
	 *            array of conditions based on which rows are selected for
	 *            update operation
	 * @param columNames
	 *            comma separated list of columns that are to be updated
	 * @param columnValues
	 *            comma separated set of values, in the same order as the column
	 *            names, to which the columns are to be updated.
	 * @return number of rows updated
	 * @throws ExilityException
	 */

	public int massUpdate(DataCollection dc, DbHandle handle,
			Condition[] conditions, String columNames, String columnValues)
			throws ExilityException;

	/***
	 * Save means "update the row if primary key is supplied, else insert it".
	 * Refer to insert/update methods
	 * 
	 * @param dc
	 * @param handle
	 * @return 1 if row was saved, 0 otherwise
	 * @throws ExilityException
	 */
	public int save(DataCollection dc, DbHandle handle) throws ExilityException;

	/***
	 * persists based on value in saveOperation
	 * 
	 * @param dc
	 * @param handle
	 * @param saveOperation
	 *            'add' 'modify' 'delete', similar to bulkTask
	 * @return 1 if row was saved, 0 otherwise
	 * @throws ExilityException
	 */
	public int persist(DataCollection dc, DbHandle handle, String saveOperation)
			throws ExilityException;

	/***
	 * Save means "update the row if primary key is supplied, else insert it".
	 * Refer to insert/update methods
	 * 
	 * @param dc
	 * @param handle
	 * @param grid
	 *            grid that has the rows to be saved
	 * @return total number of rows saved
	 * @throws ExilityException
	 */
	public int saveFromGrid(DataCollection dc, DbHandle handle, Grid grid)
			throws ExilityException;

	/***
	 * delete a row in the table based on primary key supplied in dc.values
	 * 
	 * @param dc
	 * @param handle
	 * @return number of rows deleted
	 * @throws ExilityException
	 */
	public int delete(DataCollection dc, DbHandle handle)
			throws ExilityException;

	/***
	 * delete rows corresponding to the rows in the input grid. Each row in the
	 * grid is to have values for key columns of this table
	 * 
	 * @param dc
	 * @param handle
	 * @param grid
	 *            each row in the grid represents a row in the database to be
	 *            deleted
	 * @return total number of rows deleted
	 * @throws ExilityException
	 */
	public int deleteFromGrid(DataCollection dc, DbHandle handle, Grid grid)
			throws ExilityException;

	/***
	 * delete rows from this table that satisfy conditions that are provided
	 * 
	 * @param dc
	 * @param handle
	 * @param conditions
	 *            array of conditions all of which to be satisfied for a row for
	 *            it to be deleted
	 * @return number of rows deleted
	 * @throws ExilityException
	 */
	public int massDelete(DataCollection dc, DbHandle handle,
			Condition[] conditions) throws ExilityException;

	/***
	 * process each row of input grid based on the action specified in that row.
	 * for each row in the grid, a row in the table may be
	 * added/modified/deleted
	 * 
	 * @param dc
	 * @param handle
	 * @param grid
	 *            input rows
	 * @param actionColumnName
	 *            name if the column in the grid that indicates the action. This
	 *            is set to bulkAction as a convention. Valid values are add,
	 *            modify and delete. It may be left blank in which case, the row
	 *            is skipped
	 * @return total number of rows affected
	 * @throws ExilityException
	 */
	public int bulkAction(DataCollection dc, DbHandle handle, Grid grid,
			String actionColumnName) throws ExilityException;
}

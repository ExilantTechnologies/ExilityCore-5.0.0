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
 * This is the base class that provides the skeleton for any class that wishes
 * to implement Table functionality
 * 
 */
public abstract class AbstractTable implements TableInterface, SqlInterface {
	private static final String WHERE = " WHERE ";
	/***
	 * Name of this entity, as used in application
	 */
	protected String name;
	/**
	 * name of the rdbms table
	 */
	protected String tableName = "AbstractTable";
	/***
	 * names of the primary key in the data base.
	 */
	protected String[] keyFieldNames = null;
	protected String[] keyColumnNames = null;
	/**
	 * Holds if this table requires to be audited or not
	 */
	protected boolean isAudited = false;
	/**
	 * Holds if the system needs to auto-generate the primary key
	 */
	protected boolean keyToBeGenerated = false;

	/**
	 * comma separated list of all fields. Created and used internally based on
	 * the column definitions
	 */
	protected String allFieldNames = null;

	/**
	 * in case this is a view, but not a view that can be defined in the RDBMS
	 * but the output format of a sql-template. This is, in some sense, a
	 * table-facade for a sql. Also, a table with this attribute can be used
	 * only for reading data
	 */
	String sqlName;

	/**
	 * cache output fields from the sql
	 */

	protected OutputColumn[] outputColumns;

	/**
	 * Accessor for table name
	 * 
	 * @return The table name this instance represents
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Accessor to check if audit entries are required or not
	 * 
	 * @return boolean
	 */
	public boolean toBeAudited() {
		return AP.enableAuditForAll || this.isAudited;
	}

	/**
	 * Accessor to check if primary key is to be generated
	 * 
	 * @return boolean
	 */
	public boolean keyToBeGenerated() {
		return this.keyToBeGenerated;
	}

	/**
	 * This method enables selective inserts from a grid based on the row
	 * selector. This is for internal use of the class and inheriting classes
	 * only
	 * 
	 * @param dc
	 *            Instance of the data collection object
	 * @param handle
	 *            Instance of underlying database connection handle
	 * @param grid
	 *            The grid that contains the values to be saved
	 * @param rowSelector
	 *            A boolean array that indicates which rows contain the values
	 *            for insertion (the ones with true)
	 * @return Number of rows affected
	 * @throws ExilityException
	 */
	protected abstract int insertFromGrid(DataCollection dc, DbHandle handle,
			Grid grid, boolean[] rowSelector) throws ExilityException;

	@Override
	public int insertFromGrid(DataCollection dc, DbHandle handle, Grid grid)
			throws ExilityException {
		return this.insertFromGrid(dc, handle, grid, null);
	}

	@Override
	public int updateFromGrid(DataCollection dc, DbHandle handle, Grid grid)
			throws ExilityException {
		return this.updateFromGrid(dc, handle, grid, null);
	}

	/**
	 * This method enables selective updates from a grid based on the row
	 * selector. This is for internal use of the class and inheriting classes
	 * only
	 * 
	 * @param dc
	 *            Instance of the data collection object
	 * @param handle
	 *            Instance of underlying database connection handle
	 * @param grid
	 *            The grid that contains the values to be saved
	 * @param rowSelector
	 *            A boolean array that indicates which rows contain the values
	 *            for updation (the ones with true)
	 * @return Number of rows affected
	 * @throws ExilityException
	 */
	protected abstract int updateFromGrid(DataCollection dc, DbHandle handle,
			Grid grid, boolean[] rowSelector) throws ExilityException;

	@Override
	public int deleteFromGrid(DataCollection dc, DbHandle handle, Grid grid)
			throws ExilityException {
		return this.deleteFromGrid(dc, handle, grid, null);
	}

	/**
	 * This method enables selective deletes from a grid based on the row
	 * selector. This is for internal use of the class and inheriting classes
	 * only
	 * 
	 * @param dc
	 *            Instance of the data collection object
	 * @param handle
	 *            Instance of underlying database connection handle
	 * @param grid
	 *            The grid that contains the values to be saved
	 * @param rowSelector
	 *            A boolean array that indicates which rows contain the values
	 *            for deletion (the ones with true)
	 * @return Number of rows affected
	 * @throws ExilityException
	 */
	protected abstract int deleteFromGrid(DataCollection dc, DbHandle handle,
			Grid grid, boolean[] rowSelector) throws ExilityException;

	@Override
	public int read(DataCollection dc, DbHandle handle, String gridName,
			String prefix) throws ExilityException {
		return this.filter(dc, handle, gridName, null, null, null, null,
				prefix, false);
	}

	/**
	 * A generic update method that allows updation of selective fields and an
	 * arbitrary where clause. This flexibility comes at a cost of dynamic
	 * building of the SQL string
	 * 
	 * @param dc
	 *            Instance of the data collection object
	 * @param handle
	 *            Instance of underlying database connection handle
	 * @param conditions
	 *            An array of conditions that can be used to create the WHERE
	 *            clause
	 * @param columnNames
	 *            A comma separated list of column names to update
	 * @param columnValues
	 *            A comma separated list of column values to update the column
	 *            with
	 * @return Number of rows affected
	 * @throws ExilityException
	 */
	@Override
	public int massUpdate(DataCollection dc, DbHandle handle,
			Condition[] conditions, String columnNames, String columnValues)
			throws ExilityException {
		String[] names = columnNames.split(",");
		String[] values = columnValues.split(",");
		if (names.length != values.length) {
			dc.addError("Column names do not have matching values to be set to in mass update step for "
					+ this.tableName);
			return 0;
		}
		StringBuilder sb = new StringBuilder();
		boolean addedAtLeastOne = false;
		sb.append("UPDATE ").append(this.tableName).append(" SET ");
		for (int i = 0; i < names.length; i++) {
			if (addedAtLeastOne) {
				sb.append(", ");
			}
			sb.append(names[i]).append(" = ")
					.append(SqlUtil.formatValue(Value.newValue(values[i])));
			addedAtLeastOne = true;
		}

		sb.append(AbstractTable.WHERE);
		boolean gotAdded = Condition.toSql(sb, conditions, dc);
		if (gotAdded == false) {
			dc.raiseException("exilUpdateWithNoConditions", this.tableName);
			return 0;
		}
		return handle.execute(sb.toString(), false);
	}

	/**
	 * A generic delete method that allows deletion of selective fields with an
	 * arbitrary where clause. This flexibility comes at a cost of dynamic
	 * building of the SQL string
	 * 
	 * @param dc
	 *            Instance of the data collection object
	 * @param handle
	 *            Instance of underlying database connection handle
	 * @param conditions
	 *            An array of conditions that can be used to create the WHERE
	 *            clause
	 * @return Number of rows affected
	 * @throws ExilityException
	 */
	@Override
	public int massDelete(DataCollection dc, DbHandle handle,
			Condition[] conditions) throws ExilityException {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(this.tableName)
				.append(AbstractTable.WHERE);

		boolean gotAdded = Condition.toSql(sb, conditions, dc);
		if (gotAdded == false) {
			dc.raiseException("exilUpdateWithNoConditions", this.getName());
			return 0;
		}
		return handle.execute(sb.toString(), false);
	}

	@Override
	public int persist(DataCollection dc, DbHandle handle, String saveOperation)
			throws ExilityException {
		if (saveOperation.equals("add")) {
			return this.insert(dc, handle);
		}

		if (saveOperation.equals("modify")) {
			return this.update(dc, handle);
		}

		if (saveOperation.equals("delete")) {
			return this.delete(dc, handle);
		}

		if (saveOperation.equals("save")) {
			return this.save(dc, handle);
		}

		return 0;

	}

	/**
	 * Save changes (Insert and update)
	 * 
	 * @param dc
	 *            Instance of the data collection object
	 * @param handle
	 *            Instance of underlying database connection handle
	 * @return Number of rows affected
	 * @throws ExilityException
	 */
	@Override
	public int save(DataCollection dc, DbHandle handle) throws ExilityException {
		// valid only if there is exactly one primary key
		if (this.keyFieldNames == null || this.keyFieldNames.length != 1) {
			throw new ExilityException(
					"Save operaiton requires that a unique key field be defined for the table. Not valid for table "
							+ this.name);
		}
		Value value = dc.getValue(this.keyFieldNames[0]);
		boolean keyFound = false;
		if (value != null && value.isNull() == false) {
			if (value.getValueType() == DataValueType.INTEGRAL) {
				if (value.getIntegralValue() > 0) {
					keyFound = true;
				}
			} else if (value.getTextValue().length() > 0) {
				keyFound = true;
			}
		}
		if (keyFound) {
			return this.update(dc, handle);
		}

		return this.insert(dc, handle);
	}

	/**
	 * Save rows present in a grid(Insert and update)
	 * 
	 * @param dc
	 *            Instance of the data collection object
	 * @param handle
	 *            Instance of underlying database connection handle
	 * @param grid
	 *            The grid that contains the values to be saved
	 * @return Number of rows affected
	 * @throws ExilityException
	 */
	@Override
	public int saveFromGrid(DataCollection dc, DbHandle handle, Grid grid)
			throws ExilityException {
		// find out what rows are to be updated and which lines to be inserted
		// valid only if there is exactly one primary key
		if (this.keyFieldNames == null || this.keyFieldNames.length != 1) {
			throw new ExilityException(
					"Save operaiton requires that a unique key field be defined for the table. Not valid for table "
							+ this.name);
		}
		long[] keys = grid.getColumn(this.keyFieldNames[0]).getIntegralList();
		int nbrRows = keys.length;
		boolean[] inserts = new boolean[nbrRows];
		boolean[] updates = new boolean[nbrRows];
		int nbrInserts = 0;
		for (int i = 0; i < keys.length; i++) {
			if (keys[i] > 0) {
				updates[i] = true;
			} else {
				inserts[i] = true;
				nbrInserts++;
			}
		}

		int totalAffected = 0;
		if (nbrInserts > 0) {
			totalAffected = this.insertFromGrid(dc, handle, grid, inserts);
		}
		if (nbrInserts < nbrRows) {
			totalAffected += this.updateFromGrid(dc, handle, grid, updates);
		}
		return totalAffected;
	}

	/**
	 * Perform action on a large set of rows (insert, update & delete)
	 * 
	 * @param dc
	 *            Instance of the data collection object
	 * @param handle
	 *            Instance of underlying database connection handle
	 * @param grid
	 *            The grid that contains the values to be acted upon
	 * @param bulkColumnName
	 *            The column name in the grid that contains the action to
	 *            perform (insert/modify/delete). If left to null, we use
	 *            standard value of CommonFieldNames.BULK_ACIOTN
	 * @return Number of rows affected
	 * @throws ExilityException
	 */
	@Override
	public int bulkAction(DataCollection dc, DbHandle handle, Grid grid,
			String bulkColumnName) throws ExilityException {
		String[] actions = grid.getColumnAsTextArray(bulkColumnName);
		if (actions == null) {
			actions = grid.getColumnAsTextArray(CommonFieldNames.BULK_ACTION);
			if (actions == null) {
				throw new ExilityException("bulkAction column for table "
						+ this.name + " not found for either '"
						+ bulkColumnName + "' or '"
						+ CommonFieldNames.BULK_ACTION + "'. Check your design");

			}
		}
		int nbrRows = actions.length;
		boolean[] inserts = new boolean[nbrRows];
		boolean[] updates = new boolean[nbrRows];
		boolean[] deletes = new boolean[nbrRows];
		int nbrInserts = 0;
		int nbrUpdates = 0;
		int nbrDeletes = 0;
		for (int i = 0; i < actions.length; i++) {
			String action = actions[i];
			if (action.equals("add") || action.equals("insert")) {
				inserts[i] = true;
				nbrInserts++;
			} else if (action.equals("modify")) {
				updates[i] = true;
				nbrUpdates++;
			} else if (action.equals("delete")) {
				deletes[i] = true;
				nbrDeletes++;
			}
		}
		Spit.out("Total Rows=" + nbrRows + ", Nbr Inserts=" + nbrInserts
				+ ", Nbr Updates=" + nbrUpdates + ", Nbr Deletes=" + nbrDeletes);
		int totalAffected = 0;
		if (nbrInserts > 0) {
			totalAffected = this.insertFromGrid(dc, handle, grid, inserts);
		}

		if (nbrUpdates > 0) {
			totalAffected += this.updateFromGrid(dc, handle, grid, updates);
		}

		if (nbrDeletes > 0) {
			totalAffected += this.deleteFromGrid(dc, handle, grid, deletes);
		}

		return totalAffected;
	}

	/**
	 * Generate the primary key for a given table
	 * 
	 * @param dc
	 *            The instance of data collection to hold the generated key
	 * @param keyColumnName
	 *            Name of the primary key column in the database
	 * @param keyFieldName
	 *            Name of the key field (as used in the application)
	 * @return The newly generated key
	 * @throws ExilityException
	 */
	protected long generateKey(DataCollection dc) throws ExilityException {
		long key = PrimaryKeyGenerator.getNextKey(this.tableName,
				this.keyColumnNames[0], 1);

		// ask dc to store generated key, so that any subsequent child table can
		// use them
		long existingKey = dc.getIntegralValue(this.keyFieldNames[0], 0);
		// just in case service designer is living with some old key.
		// Remember, whole exercise of mapping keys makes sense only if existing
		// key is <= 0
		if (existingKey > 0) {
			existingKey = 0;
		}
		dc.addGeneratedKey(this.tableName, existingKey, key);
		dc.addValue(this.keyFieldNames[0], Value.newValue(key));
		return key;
	}

	/***
	 * Generates primary key for each row in the grid, if required. It also
	 * stores a map of old keys and new keys in dc that can be used by child
	 * tables
	 * 
	 * @param dc
	 *            Instance of the data collection object that holds the values
	 * @param grid
	 *            The grid for which keys have to be generated
	 * @param keyColumnName
	 *            Name of the primary key column
	 * @return first key that is generated
	 * @throws ExilityException
	 */
	protected long generateKeysForGrid(DataCollection dc, Grid grid,
			boolean[] rowSelector) throws ExilityException {
		int nbrRows = grid.getNumberOfRows();
		int nbrKeysRequired = nbrRows;
		if (rowSelector != null) {
			if (nbrRows != rowSelector.length) {
				throw new ExilityException(
						"AbstarctTable.generateKeysForGrid is called with a selector of length "
								+ rowSelector.length
								+ " while the corresponding grid has "
								+ nbrRows + " rows.");
			}
			nbrKeysRequired = 0;
			for (boolean toSelect : rowSelector) {
				if (toSelect) {
					nbrKeysRequired++;
				}
			}
		}

		if (nbrKeysRequired == 0) {
			Spit.out("Nothing to insert into table " + this.name);
			return 0;
		}

		// valid only if there is exactly one primary key
		if (this.keyFieldNames == null || this.keyFieldNames.length != 1) {
			throw new ExilityException(
					"Save operaiton requires that a unique key field be defined for the table. Not valid for table "
							+ this.name);
		}

		String keyColumnName = this.keyColumnNames[0];
		String keyFieldName = this.keyFieldNames[0];

		long key = PrimaryKeyGenerator.getNextKey(this.tableName,
				keyColumnName, nbrKeysRequired);
		long keyToReturn = key;
		long[] existingKeys = null;
		if (grid.hasColumn(keyFieldName)) {
			existingKeys = grid.getColumn(keyFieldName).getIntegralList();
		} else {
			// add key column to grid
			ValueList list = ValueList.newList(DataValueType.INTEGRAL, nbrRows);
			grid.addColumn(keyFieldName, list);
			existingKeys = new long[nbrRows];
		}
		// dc has methods to get new keys for old(negative) keys for a table.
		// That works only if we ask dc to save the map!!
		for (int i = 0; i < existingKeys.length; i++) {
			if (rowSelector == null || rowSelector[i]) {
				dc.addGeneratedKey(this.tableName, existingKeys[i], key);
				grid.setIntegralValue(keyFieldName, i, key);
				key++;
			}
		}
		return keyToReturn;
	}

	/***
	 * Creates a valueList by saving the same value in all the rows
	 * 
	 * @param value
	 *            Value to be saved in all rows
	 * @param nbrValues
	 *            in the list
	 * @return
	 */
	protected ValueList getValueList(Value value, int nbrValues) {
		ValueList list = ValueList.newList(value.getValueType(), nbrValues);
		for (int i = 0; i < nbrValues; i++) {
			list.setValue(value, i);
		}
		return list;
	}

	@Override
	public int massRead(DataCollection dc, DbHandle handle, String gridName,
			Grid grid) throws ExilityException {
		StringBuilder sbf = new StringBuilder();
		int n = grid.getNumberOfRows();
		if (n == 0) {
			return 0;
		}
		Grid outGrid = null;
		for (int i = 1; i <= n; i++) {
			for (String keyField : this.keyFieldNames) {
				dc.addValue(keyField, grid.getValue(keyField, i));
			}
			this.getSelectStatement(sbf, dc, null, null, null, null, null,
					false);
			// note that extractToGrid will create the grid if it is null. if it
			// exists, it adds rows.
			outGrid = handle.extractToGrid(sbf.toString(), this, gridName,
					outGrid);
			sbf.setLength(0);
		}
		if (outGrid == null) {
			return 0;
		}
		dc.addGrid(gridName, outGrid);
		return outGrid.getNumberOfRows();
	}

	@Override
	public int filter(DataCollection dc, DbHandle handle, String gridName,
			Condition[] conditions, String columnsToSelect,
			String columnsToSort, String sortOrder, String prefix,
			boolean eliminateDuplicates) throws ExilityException {
		StringBuilder sbf = new StringBuilder();
		this.getSelectStatement(sbf, dc, conditions, columnsToSelect,
				columnsToSort, sortOrder, prefix, eliminateDuplicates);
		if (gridName == null) {
			return handle.extractSingleRow(sbf.toString(), dc.values, this,
					prefix);
		}
		Grid grid = handle.extractToGrid(sbf.toString(), this, gridName, null);
		dc.addGrid(gridName, grid);
		return grid.getNumberOfRows();
	}

	/***
	 * Helper method that cater read as well as filter. Since this is a flexible
	 * method, you see large number of parameters
	 * 
	 * @param sbf
	 * @param dc
	 * @param conditions
	 * @param columnsToSelect
	 * @param columnsToSort
	 * @param sortOrder
	 * @param prefix
	 * @param eliminateDuplicates
	 * @throws ExilityException
	 */
	protected void getSelectStatement(StringBuilder sbf, DataCollection dc,
			Condition[] conditions, String columnsToSelect,
			String columnsToSort, String sortOrder, String prefix,
			boolean eliminateDuplicates) throws ExilityException {

		if (this.sqlName != null) {
			sbf.append(Sqls.getSql(null, dc));
			return;
		}

		sbf.append("SELECT ");
		if (eliminateDuplicates) {
			sbf.append(" DISTINCT ");
		}
		// a as "a", b as "b" ......
		if (columnsToSelect == null) // select all columns
		{
			sbf.append(this.allFieldNames);
		} else {
			sbf.append(columnsToSelect);
		}

		sbf.append(" FROM ").append(this.tableName).append(AbstractTable.WHERE);

		// This is a common method for primary key based read as well as
		// condition based filter
		if (conditions == null) {
			sbf.append(this.getPrimaryKeyClause(dc));
		} else {
			boolean gotAdded = Condition.toSql(sbf, conditions, dc);
			if (!gotAdded)// oops why should we say "filter" and use no
							// criterion?
			{
				Spit.out(this.name
						+ " filtering: No filtering criterion. Going to read all rows.");
				sbf.setLength(sbf.length() - AbstractTable.WHERE.length());
			}
		}

		if (columnsToSort != null) {
			sbf.append(" ORDER BY ");
			if (sortOrder.equalsIgnoreCase("asc")) {
				sbf.append(columnsToSort);
			} else {
				for (String columnName : columnsToSort.split(",")) {
					sbf.append(columnName).append(" ").append(sortOrder)
							.append(",");
				}
				sbf.deleteCharAt(sbf.length() - 1);
			}
		}
	}

	protected String getPrimaryKeyClause(DataCollection dc) {
		StringBuilder sbf = new StringBuilder();
		for (int i = 0; i < this.keyFieldNames.length; i++) {
			if (i > 0) {
				sbf.append(" AND ");
			}
			Value keyValue = dc.getValue(this.keyFieldNames[i]);
			sbf.append(this.keyColumnNames[i]);
			if (keyValue == null) {
				sbf.append(" is null ");
			} else {
				sbf.append(" = ").append(SqlUtil.formatValue(keyValue));
			}
		}
		return sbf.toString();
	}

	/**
	 * we implement this as if we are asked to read a primary key based read
	 */
	@Override
	public int execute(DataCollection dc, DbHandle handle, String gridName,
			String prefix) throws ExilityException {
		throw new ExilityException(
				"Design Error: Table "
						+ this.name
						+ " is used as if it is a sql and an execute() is invoked. This is not possible.");
	}

	@Override
	public int executeBulkTask(DataCollection dc, DbHandle handle,
			String gridName, boolean forValidation) throws ExilityException {
		throw new ExilityException(
				"Design Error : Table "
						+ this.name
						+ " is used as if it is a sql to be bulk executed. This is not possible.");
	}

	@Override
	public DataAccessType getDataAccessType() {
		Spit.out("table should not be asked about its data access type. However, we have given a cautious read-write answer.");
		return DataAccessType.READWRITE;
	}

	@Override
	public OutputColumn[] getColumnInfo() {
		return this.outputColumns;
	}

	@Override
	public void setColumnInfo(OutputColumn[] columnInfo) {
		Spit.out("Table will not allow others to set its output columns.");
	}
}

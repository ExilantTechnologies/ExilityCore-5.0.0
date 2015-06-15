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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * Represents an RDBMS table
 * 
 */
public class Table extends AbstractTable implements ToBeInitializedInterface {

	private static final String AUDIT_ACTION_COLUMN_NAME = "action";
	private static final String AUDIT_ACTION_INSERT = "insert";
	private static final String AUDIT_ACTION_MODIFY = "modify";
	private static final String SQL_TABLE_ERROR = " is a sql based view, and hence data can not be inserted/manipalated using this table definition.";

	/**
	 * table belongs to a module. This table is referred to as module.name
	 */
	String module = null;
	/**
	 * documentation
	 */
	String description = null;

	/**
	 * Most master table rows should not be deleted. A small security feature..
	 */
	boolean okToDelete = false;

	/**
	 * does the primary key generation depend on some fields? like quotation
	 * number should be one up number for a given FY and a zone? While this is
	 * required by business to contribute some of the practices before using
	 * such a system, it is also not a great foreign key. It probably makes
	 * sense to have a one-up number as the primary key, and use this as the
	 * 'user-defined' key... Any way, we provide features that our customers
	 * really want from Exility. Specify the field names, separated by comma,
	 * values of which forms the prefix for the key. Key generator uses this
	 * prefix and gets the one up number for this table and this prefix
	 */
	String[] keyPrefixFieldNames = null;

	/**
	 * if surrogate key is String, then it may have to be left padded with 0's.
	 * provide keyColumnSize for this. for example, if you have defined key as
	 * VARCHAR2(4), and it has to be a one-up number, then u specify
	 * keyCOlumnWidth = 4 so that the generated key would be "0018" and not 18 A
	 * STRONG NO from Exility for this approach, but one of the projects had to
	 * do this.....
	 */
	int keyColumnWidth = 0;

	/**
	 * We understand following four fields, and manage them appropriately. Give
	 * the name here, and do not specify them in the list of columns
	 */
	String createdTimestampName = null;
	String modifiedTimestampName = null;
	String createdUserName = null;
	String modifiedUserName = null;

	/**
	 * in case you use a naming standard where column names is different from
	 * field name..
	 */
	String createdTimestampColumnName = null;
	String modifiedTimestampColumnName = null;
	String createdUserColumnName = null;
	String modifiedUserColumnName = null;
	/**
	 * array of columnDefinitions, each of which describes attributes of a
	 * column note that this array will not contain entries for the six standard
	 * fields
	 */
	Column[] columns = new Column[0];

	/**
	 * Reference tables. There can be one parent table, and any number of tables
	 * to which this may refer into.
	 * 
	 * use this ONLY if this table is a child-table in the "classical" sense.
	 * like order header is the parent table for order lines.
	 */
	String parentTableName;

	/***
	 * Related tables are other tables that contain rows that are related to a
	 * row in this table.
	 */
	RelatedTable[] relatedTables;

	/**
	 * name of field that indicates whether this row is active. Common practice
	 * to deactivate a row rather than delete it to ensure that existing
	 * transaction refer to this master, but this master can not be used to
	 * create new transactions. It is to be noted that this attribute is used
	 * only in tableDelete operation. Also, this columns is to be defined as a
	 * column, unlike the other four standard fields
	 */
	String activeField = null;

	/**
	 * 
	 */
	public Table() {
	}

	@Override
	public void initialize() {
		Set<String> allNames = new HashSet<String>();
		int nbrKeys = 0;

		int nbrDuplicates = 0;

		for (int i = 0; i < this.columns.length; i++) {
			Column column = this.columns[i];
			// column name defaults to name
			if (column.columnName == null) {
				column.columnName = column.name;
			}

			if (allNames.add(column.columnName) == false) {
				Spit.out("Design Error : Table + " + this.name
						+ " has duplicate column name " + column.columnName
						+ " this couln is removed.");
				nbrDuplicates++;
				this.columns[i] = null;
				continue;
			}

			if (column.isKeyColumn) {
				nbrKeys++;
			}
		}

		this.keyFieldNames = new String[nbrKeys];
		this.keyColumnNames = new String[nbrKeys];
		if (nbrKeys > 0) {
			int keyIdx = 0;
			for (Column column : this.columns) {
				if (column.isKeyColumn) {
					this.keyColumnNames[keyIdx] = column.columnName;
					this.keyFieldNames[keyIdx] = column.name;
					// small optimization. Most of the time, we have one key
					// field, and it is the first field. Why keep iterating?
					keyIdx++;
					if (nbrKeys == keyIdx) {
						break;
					}
				}
			}
		}
		List<OutputColumn> outCols = new ArrayList<OutputColumn>();
		// check and add standard fields
		nbrDuplicates += this.checkStandardFields(outCols, allNames);

		/*
		 * shrink columns if we removed any duplicates
		 */
		if (nbrDuplicates != 0) {
			int newNbrs = this.columns.length - nbrDuplicates;
			Column[] newColumns = new Column[newNbrs];
			int i = 0;
			for (Column column : this.columns) {
				if (column != null) {
					newColumns[i] = column;
					i++;
				}
			}
			this.columns = newColumns;
		}

		/*
		 * create outputColumns
		 */
		// selectAll fields
		StringBuilder allFieldsBuffer = new StringBuilder();
		this.outputColumns = new OutputColumn[this.columns.length
				+ outCols.size()];
		int i = 0;
		for (Column column : this.columns) {
			this.outputColumns[i] = new OutputColumn(column.name,
					column.getValueType(), column.columnName);
			i++;
			allFieldsBuffer.append(column.columnName).append(" \"")
					.append(column.name).append("\",");
		}

		for (OutputColumn column : outCols) {
			this.outputColumns[i] = column;
			i++;
			allFieldsBuffer.append(column.columnName).append(" \"")
					.append(column.fieldName).append("\",");
		}
		/*
		 * remove the last extra comma
		 */
		allFieldsBuffer.setLength(allFieldsBuffer.length() - 1);
		this.allFieldNames = allFieldsBuffer.toString();

	}

	/**
	 * check for possible error in defining the standard fields. Try to
	 * accommodate
	 * 
	 * @param all
	 *            accumulate field selection string
	 * @returns true if all OK, false in case of error
	 * 
	 */
	private int checkStandardFields(List<OutputColumn> outCols,
			Set<String> allNames) {

		int nbrDuplicates = 0;
		/**
		 * Syntax of the four standard fields is bit confusing. Possible that
		 * users make mistakes. Let us try to accommodate.
		 */
		if (this.modifiedTimestampName != null
				|| this.modifiedTimestampColumnName != null) {
			/*
			 * use any one as default for the other
			 */
			if (this.modifiedTimestampName == null) {
				this.modifiedTimestampName = this.modifiedTimestampColumnName;
			} else if (this.modifiedTimestampColumnName == null) {
				this.modifiedTimestampColumnName = this.modifiedTimestampName;
			}

			if (allNames.add(this.modifiedTimestampName) == false) {
				Spit.out("ERROR: timestamp column should not be included in the list of columns. Column is deleted by the loader.");
				this.deleteColumnDefinition(this.modifiedTimestampName);
				nbrDuplicates++;
			}

			outCols.add(new OutputColumn(this.modifiedTimestampName,
					DataValueType.TIMESTAMP, this.modifiedTimestampColumnName));
		}

		DataValueType userType = AP.loggedInUserDataTypeIsInteger ? DataValueType.INTEGRAL
				: DataValueType.TEXT;
		// similar check for modifiedBy
		if (this.modifiedUserName != null
				|| this.modifiedUserColumnName != null) {
			/*
			 * use any one as default for the other
			 */
			if (this.modifiedUserColumnName == null) {
				this.modifiedUserColumnName = this.modifiedUserName;
			} else if (this.modifiedUserName == null) {
				this.modifiedUserName = this.modifiedUserColumnName;
			}

			if (allNames.add(this.modifiedUserName) == false) {
				Spit.out("ERROR: modified user name column should not be included in the list of columns. Column is deleted by the loader.");
				this.deleteColumnDefinition(this.modifiedUserName);
				nbrDuplicates++;
			}

			outCols.add(new OutputColumn(this.modifiedUserName, userType,
					this.modifiedUserColumnName));
		}

		if (this.createdTimestampName != null
				|| this.createdTimestampColumnName != null) {
			/*
			 * use any one as default for the other
			 */
			if (this.createdTimestampName == null) {
				this.createdTimestampName = this.createdTimestampColumnName;
			} else if (this.createdTimestampColumnName == null) {
				this.createdTimestampColumnName = this.createdTimestampName;
			}

			if (allNames.add(this.createdTimestampName) == false) {
				Spit.out("ERROR: created timestamp column should not be included in the list of columns. Column is deleted by the loader.");
				this.deleteColumnDefinition(this.createdTimestampName);
				nbrDuplicates++;
			}

			outCols.add(new OutputColumn(this.createdTimestampName,
					DataValueType.TIMESTAMP, this.createdTimestampColumnName));
		}

		if (this.createdUserName != null || this.createdUserColumnName != null) {
			/*
			 * use any one as default for the other
			 */
			if (this.createdUserName == null) {
				this.createdUserName = this.createdUserColumnName;
			} else if (this.createdUserColumnName == null) {
				this.createdUserColumnName = this.createdUserName;
			}

			if (allNames.add(this.createdUserName) == false) {
				Spit.out("ERROR: createdUsaerName column should not be included in the list of columns. Column is deleted by the loader.");
				this.deleteColumnDefinition(this.createdUserName);
				nbrDuplicates++;
			}

			outCols.add(new OutputColumn(this.createdUserName, userType,
					this.createdUserColumnName));
		}
		return nbrDuplicates;
	}

	/***
	 * select all fields as per lookupStep requirement. It does not pick up
	 * special fields, and it does not need as clause.
	 * 
	 * @return
	 */
	String getAllFieldNamesForLookup() {
		StringBuilder all = new StringBuilder();
		boolean firstOne = true;
		for (Column column : this.columns) {
			if (firstOne) {
				firstOne = false;
			} else {
				all.append(", ");
			}
			all.append(column.columnName);
		}

		return all.toString();
	}

	/**
	 * remove a column from the columns array
	 * 
	 * @param fieldName
	 */
	private void deleteColumnDefinition(String fieldName) {
		for (int i = 0; i < this.columns.length; i++) {
			Column column = this.columns[i];
			if (!fieldName.equals(column.name)) {
				this.columns[i] = null;
				return;
			}
		}
		Spit.out(fieldName
				+ " is not a column and hence could not be removed from the list of columns for table "
				+ this.name);
	}

	@Override
	public int insert(DataCollection dc, DbHandle handle)
			throws ExilityException {
		if (this.sqlName != null) {
			throw new ExilityException(this.name + SQL_TABLE_ERROR);
		}

		List<String> names = new ArrayList<String>();
		List<Value> values = new ArrayList<Value>();
		if (this.keyToBeGenerated) {
			if (this.keyFieldNames.length == 0) {
				Spit.out("Key columns not defined for " + this.name
						+ " but keyToBeGenerated=true");
				dc.raiseException("exilKeysNotDefined", this.name);
			}
			this.generateKey(dc);
			Spit.out("Key generated for table " + this.tableName);
		}

		int n = this.getColumnsAndValues(dc, names, values, false);
		Spit.out("table insert for : " + this.name + " :" + n
				+ " columns will be inserted.");
		if (n == 0) {
			dc.raiseException("nothingToInsert", this.name);
		}

		StringBuilder sbf = new StringBuilder();
		this.getInsertStatement(sbf, names, this.tableName, dc);
		int rowsAffected = handle.executePreparedStatement(sbf.toString(),
				values, false);

		// do we write audits?
		if (rowsAffected > 0 && this.toBeAudited()) {
			values.add(Value.newValue(AUDIT_ACTION_INSERT));
			names.add(AUDIT_ACTION_COLUMN_NAME);
			sbf = new StringBuilder();
			this.getInsertStatement(sbf, names, this.tableName
					+ AP.audittableSuffix, dc);
			handle.executePreparedStatement(sbf.toString(), values, true);
		}
		return rowsAffected;
	}

	@Override
	public int insertFromGrid(DataCollection dc, DbHandle handle, Grid grid,
			boolean[] rowSelector) throws ExilityException {
		if (this.sqlName != null) {
			throw new ExilityException(this.name + SQL_TABLE_ERROR);
		}

		List<String> names = new ArrayList<String>();
		List<ValueList> values = new ArrayList<ValueList>();
		if (this.keyToBeGenerated) {
			if (this.keyFieldNames.length == 0) {
				dc.raiseException("exilKeysNotDefined", this.name);
			}
			this.generateKeysForGrid(dc, grid, rowSelector);
		}

		int n = this
				.getColumnsAndValuesFromGrid(dc, names, values, grid, false);
		if (n == 0) {
			dc.raiseException("nothingToInsert", this.name);
		}

		StringBuilder sbf = new StringBuilder();
		this.getInsertStatement(sbf, names, this.tableName, dc);
		int rowsAffected = handle.executePreparedStatementBatch(sbf.toString(),
				values, false, rowSelector);

		// do we write audits?
		if (rowsAffected > 0 && this.toBeAudited()) {
			values.add(this.getValueList(Value.newValue(AUDIT_ACTION_INSERT),
					grid.getNumberOfRows()));
			names.add(AUDIT_ACTION_COLUMN_NAME);
			sbf = new StringBuilder();
			this.getInsertStatement(sbf, names, this.tableName
					+ AP.audittableSuffix, dc);
			handle.executePreparedStatementBatch(sbf.toString(), values, true,
					rowSelector);
		}
		return rowsAffected;
	}

	@Override
	public int update(DataCollection dc, DbHandle handle)
			throws ExilityException {

		if (this.sqlName != null) {
			throw new ExilityException(this.name + SQL_TABLE_ERROR);
		}

		if (this.keyFieldNames.length == 0) {
			dc.raiseException("exilKeysNotDefined", this.name);
		}
		List<String> names = new ArrayList<String>();
		List<Value> values = new ArrayList<Value>();
		int n = this.getColumnsAndValues(dc, names, values, true);
		if (n == 0) {
			dc.raiseException("nothingToUpate", this.name);
		}

		StringBuilder sbf = new StringBuilder();
		// are we using time-stamp check?
		for (String keyFieldName : this.keyFieldNames) {
			values.add(dc.getValue(keyFieldName));
		}
		boolean useTimeStamp = false;
		if (this.modifiedTimestampName != null
				&& dc.hasValue(this.modifiedTimestampName)) {
			values.add(dc.getValue(this.modifiedTimestampName));
			useTimeStamp = true;
		}
		this.getUpdateStatement(sbf, names, this.tableName, dc, useTimeStamp);
		int rowsAffected = handle.executePreparedStatement(sbf.toString(),
				values, false);

		// do we write audits?
		if (rowsAffected > 0 && this.toBeAudited()) {
			values.add(Value.newValue(AUDIT_ACTION_MODIFY));
			names.add(AUDIT_ACTION_COLUMN_NAME);
			sbf = new StringBuilder();
			this.getInsertStatement(sbf, names, this.tableName
					+ AP.audittableSuffix, dc);
			handle.executePreparedStatement(sbf.toString(), values, true);
		}
		return rowsAffected;
	}

	@Override
	public int updateFromGrid(DataCollection dc, DbHandle handle, Grid grid,
			boolean[] rowSelector) throws ExilityException {

		if (this.sqlName != null) {
			throw new ExilityException(this.name + SQL_TABLE_ERROR);
		}

		if (this.keyFieldNames.length == 0) {
			dc.raiseException("exilKeysNotDefined", this.name);
		}

		List<String> names = new ArrayList<String>();
		List<ValueList> values = new ArrayList<ValueList>();
		int n = this.getColumnsAndValuesFromGrid(dc, names, values, grid, true);
		if (n == 0) {
			dc.raiseException("nothingToUpate", this.name);
		}
		// add key values
		for (String keyName : this.keyFieldNames) {
			ValueList list = grid.getColumn(keyName);
			if (list == null) {
				dc.raiseException(Message.EXILITY_ERROR, "Key column "
						+ keyName + " not found for table " + this.name);
				return 0;
			}
			values.add(list);
		}

		StringBuilder sbf = new StringBuilder();
		boolean useTimeStamp = false;
		if (this.modifiedTimestampName != null
				&& grid.hasColumn(this.modifiedTimestampName)) {
			values.add(grid.getColumn(this.modifiedTimestampName));
			useTimeStamp = true;
		}
		this.getUpdateStatement(sbf, names, this.tableName, dc, useTimeStamp);
		int rowsAffected = handle.executePreparedStatementBatch(sbf.toString(),
				values, false, rowSelector);

		// do we write audits?
		if (rowsAffected > 0 && this.toBeAudited()) {
			values.add(this.getValueList(Value.newValue(AUDIT_ACTION_MODIFY),
					grid.getNumberOfRows()));
			names.add(AUDIT_ACTION_COLUMN_NAME);
			sbf = new StringBuilder();
			this.getInsertStatement(sbf, names, this.tableName
					+ AP.audittableSuffix, dc);
			handle.executePreparedStatementBatch(sbf.toString(), values, true,
					rowSelector);
		}
		return rowsAffected;
	}

	@Override
	public int delete(DataCollection dc, DbHandle handle)
			throws ExilityException {
		return this.deleteHelper(dc, handle, null, null);
	}

	@Override
	public int deleteFromGrid(DataCollection dc, DbHandle handle, Grid grid,
			boolean[] selectors) throws ExilityException {
		return this.deleteHelper(dc, handle, grid, selectors);
	}

	/***
	 * Common helper method to delete a row
	 * 
	 * @param dc
	 * @param handle
	 * @param grid
	 *            if the rows are to be deleted for each line in this grid
	 * @param selectors
	 *            boolean array, if supplied, indicates whether to ignore this
	 *            row or not. Used when bulkAction decides what to do with the
	 *            row
	 * @return number of rows deleted
	 * @throws ExilityException
	 */
	private int deleteHelper(DataCollection dc, DbHandle handle, Grid grid,
			boolean[] selectors) throws ExilityException {

		if (this.sqlName != null) {
			throw new ExilityException(this.name + SQL_TABLE_ERROR);
		}

		if (this.keyFieldNames.length == 0) {
			dc.raiseException("exilKeysNotDefined", this.name);
		}

		List<ValueList> valuesList = new ArrayList<ValueList>();// for batch in
																// case of grid
		List<Value> values = new ArrayList<Value>();
		List<String> names = new ArrayList<String>();
		StringBuilder sbf = new StringBuilder();
		if (this.okToDelete) {
			sbf.append("DELETE FROM ").append(this.tableName).append(" WHERE ");
		} else {
			if (this.activeField == null) {
				dc.raiseException("exilDeleteNotAllowed", this.tableName);
			} else {
				sbf.append("UPDATE ").append(this.tableName).append(" SET ")
						.append(this.activeField).append(" = 'N' ");
				if (this.modifiedTimestampName != null) {
					sbf.append(this.modifiedTimestampColumnName).append(" = ")
							.append(AP.systemDateFunction).append(" ");
				}
				if (this.modifiedUserName != null) {
					sbf.append(this.modifiedUserColumnName)
							.append(" = ")
							.append(SqlUtil.formatValue(dc
									.getValue(AP.loggedInUserFieldName)));
				}
				sbf.append(" WHERE ");
			}
		}

		String prefix = "";
		for (int i = 0; i < this.keyFieldNames.length; i++) {
			ValueList keyList;
			Value keyValue;
			if (grid == null) {
				keyValue = dc.getValue(this.keyFieldNames[i]);
				if (keyValue == null || keyValue.isNull()) {
					dc.raiseException("exilNoKeyForDelete", this.tableName,
							this.keyFieldNames[i]);
				}
				values.add(keyValue);
			} else {
				keyList = grid.getColumn(this.keyFieldNames[i]);

				if (keyList == null) {
					dc.raiseException("exilNoKeyForDelete", this.tableName,
							this.keyFieldNames[i]);
				}
				valuesList.add(keyList);
			}
			sbf.append(prefix).append(this.keyColumnNames[i]).append(" = ? ");
			names.add(this.keyFieldNames[i]);
			prefix = " AND ";
		}
		int rowsAffected;
		if (grid == null) {
			rowsAffected = handle.executePreparedStatement(sbf.toString(),
					values, false);
		} else {
			rowsAffected = handle.executePreparedStatementBatch(sbf.toString(),
					valuesList, false, selectors);
		}

		// write audit if required
		if (rowsAffected > 0 && this.toBeAudited()) {
			sbf = new StringBuilder();
			names.add("operation");
			values.add(Value.newValue("delete"));
			this.getInsertStatement(sbf, names,
					this.name + AP.audittableSuffix, dc);
			if (grid == null) {
				handle.executePreparedStatement(sbf.toString(), values, true);
			} else {
				handle.executePreparedStatementBatch(sbf.toString(),
						valuesList, true, null);
			}
		}
		return rowsAffected;

	}

	/***
	 * pick up names to be included for insert/update statements. Add names and
	 * the corresponding values.
	 * 
	 * @param dc
	 * @param names
	 * @param values
	 * @param forUpdate
	 * @return number of names added
	 */
	private int getColumnsAndValues(DataCollection dc, List<String> names,
			List<Value> values, boolean forUpdate) {
		// VERY VERY IMPORTANT
		// key columns are added after all fields. If standard fields are
		// defined, modified-date/name are added before keys but
		// created-date/name added after keys
		// This is to ensure that this method can be used by both update and
		// insert routines.
		int nbrCols = 0;

		for (Column column : this.columns) {
			if (column.isKeyColumn && forUpdate) {
				continue;
			}

			Value value = dc.getValue(column.name);

			// dataSource means the data for this column is to be taken as the
			// value of "dataSource" name
			if (column.dataSource != null && column.dataSource.length() > 0) {
				if (value == null) {
					value = dc.getValue(column.dataSource);
					Spit.out(column.name + " with dataSource="
							+ column.dataSource + " got a value of " + value);
				} else if (AP.treatZeroAndEmptyStringAsNullForDataSource) {
					String textValue = value.toString();
					if (textValue.length() == 0 || textValue.equals("0")) {
						value = dc.getValue(column.dataSource);
						Spit.out(column.name + " with dataSource="
								+ column.dataSource + " got a value of "
								+ value);
					}
				}
			}

			// field is not part of update if it is not specified
			if (value == null) {
				if (forUpdate) {
					continue;
				}

				if (column.defaultValue != null
						&& column.defaultValue.length() > 0) {
					value = column.getDataType().parseValue(column.name,
							column.defaultValue, null, dc);
				}

				if (column.isNullable == false
						&& (value == null || value.isNull())) {
					continue;
				}
				Spit.out("Table=" + this.name + " column=" + column.name
						+ " is going to be inserted with its default value "
						+ column.defaultValue);
			}
			values.add(value);
			names.add(column.columnName);
			nbrCols++;
		}
		return nbrCols;
	}

	/***
	 * common method that creates list of names and valueLists suitable to be
	 * used in prepared statement. Columns are added in a specific sequence for
	 * this method to be used for both insert and update
	 * 
	 * @param dc
	 * @param names
	 *            names are added to this list
	 * @param values
	 *            values of the corresponding name in the same order
	 * @param grid
	 *            from which to extract values. Else values are taken from dc
	 * @param forUpdate
	 *            slight variation between insert and update
	 * @return number of values/names added
	 * @throws ExilityException
	 */
	private int getColumnsAndValuesFromGrid(DataCollection dc,
			List<String> names, List<ValueList> values, Grid grid,
			boolean forUpdate) throws ExilityException {
		// VERY VERY IMPORTANT
		// key columns are added after all fields. If standard fields are
		// defined, modified-date/name are added before keys but
		// created-date/name added after keys
		// This is to ensure that this method can be used by both update and
		// insert routines.
		int nbrCols = 0;
		int nbrRows = grid.getNumberOfRows();
		// look for columns from table that are present in the grid
		for (Column column : this.columns) {
			if (column.isKeyColumn && forUpdate)// to be added later
			{
				continue;
			}
			ValueList list = null;
			// dataSource implies that the value is to be taken from dc.values
			// for this column. This is typically used by child table so that
			// the generated key of the parent can be used
			// IMPORTANT: I am not sure why I was not considering dataSource for
			// update. Lokesha pointed this out on 18-apr-2012, and i am making
			// data source availabel for update as well
			// if(forUpdate == false && column.dataSource != null &&
			// dc.hasValue(column.dataSource))
			if (column.dataSource != null && dc.hasValue(column.dataSource)) {
				list = this.getValueList(dc.getValue(column.dataSource),
						nbrRows);
				grid.addColumn(column.name, list);
			}

			else if (grid.hasColumn(column.name)) {
				list = grid.getColumn(column.name);
			}

			else {
				// update is carried out only for columns that re supplied,
				// while insert will include them if a default is found
				if (forUpdate) {
					Spit.out("No value found for column " + column.name
							+ ". It will not be updated.");
					continue;
				}
				Value value = null;
				if (column.defaultValue != null
						&& column.defaultValue.length() > 0) {
					value = column.getDataType().parseValue(column.name,
							column.defaultValue, null, dc);
				}
				if (value == null || value.isNull()) {
					Spit.out("No value found for column "
							+ column.name
							+ " nor is a default specified for it. It will not be in serted.");
					continue;
				}
				list = this.getValueList(value, nbrRows);
				grid.addColumn(column.name, list);
			}
			names.add(column.columnName);
			values.add(list);
			nbrCols++;
		}
		return nbrCols;
	}

	/***
	 * Get insert statement of the form INSERT INTO tableName (a,b,..) values
	 * (?,?,?...). Note that the standard fields are populated in the sql
	 * directly, while client-supplied values are left for the prepared
	 * statement
	 * 
	 * @param sbf
	 * @param names
	 * @param tableNameToUse
	 * @param dc
	 *            to get logged-in user name
	 */
	private void getInsertStatement(StringBuilder sbf, List<String> names,
			String tableNameToUse, DataCollection dc) {
		sbf.append("INSERT INTO ").append(tableNameToUse).append(" (");
		StringBuilder ibf = new StringBuilder();
		for (String columnName : names) {
			sbf.append(columnName).append(',');
			ibf.append("?,");
		}
		sbf.deleteCharAt(sbf.length() - 1); // extra comma to be removed
		ibf.deleteCharAt(ibf.length() - 1); // extra comma to be removed

		if (this.modifiedTimestampName != null) {
			sbf.append(',').append(this.modifiedTimestampColumnName);
			ibf.append(',').append(AP.systemDateFunction);
		}
		if (this.modifiedUserName != null) {
			sbf.append(',').append(this.modifiedUserColumnName);
			ibf.append(',').append(
					SqlUtil.formatValue(dc.getValue(AP.loggedInUserFieldName)));
		}
		if (this.createdTimestampName != null) {
			sbf.append(',').append(this.createdTimestampColumnName);
			ibf.append(',').append(AP.systemDateFunction);
		}
		if (this.createdUserName != null) {
			sbf.append(',').append(this.createdUserColumnName);
			ibf.append(',').append(
					SqlUtil.formatValue(dc.getValue(AP.loggedInUserFieldName)));
		}

		sbf.append(") VALUES (").append(ibf.toString()).append(")");
	}

	/***
	 * Puts a valid UPDATE prepared statement into sbf. UPDATE tableName set a =
	 * ?, b = ?..... where key = ? AND timeStamp = ?
	 * 
	 * @param sbf
	 *            string builder to which sql is appended to
	 * @param names
	 *            column names to be updated
	 * @param tableNameToUse
	 * @param dc
	 * @param useTimestampCheck
	 *            time stamp is used in normal update to detect any concurrent
	 *            update. massUpdate does not use this.
	 */
	private void getUpdateStatement(StringBuilder sbf, List<String> names,
			String tableNameToUse, DataCollection dc, boolean useTimestampCheck) {
		sbf.append("UPDATE ").append(tableNameToUse).append(" SET ");
		for (String nam : names) {
			sbf.append(nam).append(" = ?,");
		}
		sbf.deleteCharAt(sbf.length() - 1); // extra comma to be removed

		if (this.modifiedTimestampName != null) {
			sbf.append(',').append(this.modifiedTimestampColumnName)
					.append(" = ").append(AP.systemDateFunction).append(" ");
		}
		if (this.modifiedUserName != null) {
			sbf.append(',')
					.append(this.modifiedUserColumnName)
					.append(" = ")
					.append(SqlUtil.formatValue(dc
							.getValue(AP.loggedInUserFieldName)));
		}
		sbf.append(" WHERE ");
		for (String keyColumnName : this.keyColumnNames) {
			sbf.append(keyColumnName).append(" = ? AND ");
		}
		if (useTimestampCheck) {
			sbf.append(this.modifiedTimestampName).append(" = ?");
		} else // remove the last " AND "
		{
			int len = sbf.length();
			sbf.delete(len - 4, len);
		}
	}

	/**
	 * 
	 * output a script that creates this table and adds a primary key constraint
	 * on that. NOT BUILT YET.
	 * 
	 * @param sbf
	 *            to which script needs to be appended to
	 */
	public void toScript(StringBuilder sbf) {
		Spit.out("Table.toScript(sbf) is not yet implemented");
	}

	@Override
	public int readbasedOnKey(DataCollection dc, DbHandle handle)
			throws ExilityException {
		return this.read(dc, handle, null, null);
	}
}

class DbColumn {
	String name;
	int sqlType;
	int size;
	int nbrDecimals;
	String remarks;
	boolean isNullable;
}

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

import java.util.Map;

/***
 * Manages aspects associated with joining a related table with its base table.
 * "joining" is a term used in relational data base paradigm to create a row by
 * picking columns from related tables.
 */
class JoinedTable {
	/***
	 * name of the table to join
	 */
	private String tableNameToBeJoined;
	private static final String TABLE_NAME_TO_BE_JOINED = "tableNameToBeJoined";

	/***
	 * column from base table to be used for matching value
	 */
	private String baseColumn;
	private static final String BASE_COLUMN = "baseColumn";

	/***
	 * column from the joining table to match value for joining
	 */
	private String matchingColumn;
	private static final String MATCHING_COLUMN = "matchingColumn";

	/***
	 * columns that are to be selected from this table column names are
	 * optional. It is possible that a table is joined for selection of rows,
	 * but no column from this table is selected
	 */
	private ColumnAlias[] columnNames;
	private static final String COLUMN_NAMES = "columnNames";
	/***
	 * should the row from base table be dropped if no matching rows are found
	 * in the joining table?
	 */
	private boolean excludeBaseRowIfNoMatch;
	private static final String EXCLUDE_BASE_ROW_IF_NO_MATCH = "excludeBaseRowIfNoMatch";

	/* other constants */
	private static final String COMPONENT_NAME = "unspecified";
	private static final String COMPONENT_TYPE = "joinedTable";
	private static final String COLUMN_DELIMITER = ",";
	private static final String ALIAS_DELIMITER = ":";

	public void loadFromDc(DataCollection dc) throws ExilityLoadError {
		this.loadFromValues(dc.values);
	}

	public void loadFromValues(Map<String, Value> values)
			throws ExilityLoadError {
		Value value = values.get(TABLE_NAME_TO_BE_JOINED);
		if (value == null) {
			throw new ExilityLoadError(COMPONENT_TYPE, COMPONENT_NAME,
					TABLE_NAME_TO_BE_JOINED, "");
		}
		this.tableNameToBeJoined = value.getTextValue();

		value = values.get(BASE_COLUMN);
		if (value == null) {
			throw new ExilityLoadError(COMPONENT_TYPE, COMPONENT_NAME,
					BASE_COLUMN, "");
		}
		this.baseColumn = value.getTextValue();

		value = values.get(MATCHING_COLUMN);
		if (value == null) {
			throw new ExilityLoadError(COMPONENT_TYPE, COMPONENT_NAME,
					MATCHING_COLUMN, "");
		}
		this.matchingColumn = value.getTextValue();

		/*
		 * this is an optional field. false is the default
		 */
		value = values.get(EXCLUDE_BASE_ROW_IF_NO_MATCH);
		if (value != null) {
			this.excludeBaseRowIfNoMatch = value.getBooleanValue();
		}

		value = values.get(COLUMN_NAMES);
		if (value != null) {
			// we do not mind white spaces because we are anyway going to remove
			// them later..
			String[] pairs = value.getTextValue().split(COLUMN_DELIMITER);
			int nbrCols = pairs.length;
			this.columnNames = new ColumnAlias[nbrCols];
			for (int i = 0; i < nbrCols; i++) {
				String[] pair = Util.toTextArray(pairs[i], ALIAS_DELIMITER);
				ColumnAlias alias = new ColumnAlias();
				alias.aliasName = pair[0];
				alias.columnName = pair.length > 1 ? pair[1] : pair[0];
				this.columnNames[i] = alias;
			}
		}
	}

	public void unloadToDc(DataCollection dc) {
		this.unloadToValues(dc.values);
	}

	/***
	 * extract field values into a value map
	 * 
	 * @param values
	 */
	public void unloadToValues(Map<String, Value> values) {
		values.put(TABLE_NAME_TO_BE_JOINED,
				Value.newValue(this.tableNameToBeJoined));
		values.put(BASE_COLUMN, Value.newValue(this.baseColumn));
		values.put(MATCHING_COLUMN, Value.newValue(this.matchingColumn));
		values.put(EXCLUDE_BASE_ROW_IF_NO_MATCH,
				Value.newValue(this.excludeBaseRowIfNoMatch));

		StringBuilder sbf = new StringBuilder();
		for (ColumnAlias alias : this.columnNames) {
			sbf.append(alias.aliasName).append(ALIAS_DELIMITER)
					.append(alias.columnName).append(COLUMN_DELIMITER);
		}
		sbf.deleteCharAt(sbf.length() - 1);
		values.put(COLUMN_NAMES, Value.newValue(sbf.toString()));
	}
}
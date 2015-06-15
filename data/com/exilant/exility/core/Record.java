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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * Represents a record or a row of data or a table of data that is used by the
 * application. This is a 'central' piece of design that affects almost
 * everything that is done in the application. A record is either a "storage"
 * record : represents table in a database, or represents the set of data being
 * extracted (as in report) It may also be used to just represent a data
 * structure that could be used in the application, but never used for either
 * storage or retrieval.
 * 
 */
public class Record implements ToBeInitializedInterface {

	/***
	 * Name of this record/entity, as used in application
	 */
	String name;

	/**
	 * in case you have a need to use more than one name for a record, provide
	 * the name of the record that is already defined in this attribute. name of
	 * this record becomes an alias for that record. Note that you should not
	 * give any other attribute for this record. for example, if
	 * name="customerDetailsCopy" and aliasName="customerDetails", you should
	 * have defined a record with name="customerRecord" and fully define it.
	 */
	String aliasName = null;
	/**
	 * type of this record
	 */
	RecordType recordType = RecordType.STORAGE;
	/**
	 * name of the rdbms table, if this is either a storage table, or a view
	 * that is to be defined in the rdbms
	 */
	String tableName;

	/**
	 * documentation
	 */
	String description = null;

	/**
	 * fields that this record is made-up of
	 */
	Field[] fields = null;

	/**
	 * is this table audited for any change? If true, Exility takes care of
	 * writing audit logs
	 */
	boolean isAudited;

	/**
	 * has this table got an internal key, and do you want Exility to generate
	 * that at the time of insert/add
	 */
	boolean keyToBeGenerated;
	/**
	 * Is there at least one field that has validations linked to another field.
	 * Initialized during init operation
	 */
	private boolean hasInterFieldValidations;

	/**
	 * cached table object to be used if this record is used as table in service
	 */
	private Table tableObject = null;

	/**
	 * field name in the parent record that links to this record as its child
	 */
	private String parentKey;

	/**
	 * parent record
	 */
	private String parentRecord;

	/**
	 * field in this record that links to parent record
	 */
	private String childKey;

	/**
	 * primary key of this record
	 */
	private String primaryKey;

	/**
	 * add data elements from this record to data dictionary
	 * 
	 * @param dictionary
	 */
	public void addToDictionary(DataDictionary dictionary) {
		if (this.fields == null) {
			return;
		}
		for (Field field : this.fields) {
			if (field.referredField == null) {
				dictionary.addDataElement(field.name, field.getDataElement());
			} else {
				dictionary.addReference(field.name, field.referredField,
						this.name);
			}
		}
	}

	/*
	 * methods for implementing part
	 */
	@Override
	public void initialize() {
		if (this.recordType != RecordType.STORAGE) {
			if (this.tableName == null) {
				this.tableName = this.name;
			}
		}

		if (this.fields == null) {
			this.fields = new Field[0];
			return;
		}

		for (Field field : this.fields) {
			field.initialize();
			if (!this.hasInterFieldValidations
					&& field.needsInterFieldValidation()) {
				this.hasInterFieldValidations = true;
			}

			if (field.columnType == ColumnType.primaryKey) {
				if (this.primaryKey == null) {
					this.primaryKey = field.name;
				} else {
					Spit.out("Record "
							+ this.name
							+ " specifies more than one primary keys. This is not supported. First one is used.");
				}
			} else if (field.columnType == ColumnType.parentKey) {
				if (this.parentKey != null) {
					Spit.out("Record "
							+ this.name
							+ " has more than one parentKey fields. Only the first one is used.");
				} else {
					this.parentRecord = field.referredRecord;
					this.parentKey = field.referredField;
					this.childKey = field.name;
				}
			}
		}
	}

	/**
	 * get all the fields associated with this record
	 * 
	 * @return array of fields;
	 */
	public Field[] getFields() {
		return this.fields;
	}

	/**
	 * extract a table from data as per validation requirements. We try to
	 * validate all data even if the first one itself is in error. This is to
	 * get back to client with complete list of errors.
	 * 
	 * @param data
	 * @param dc
	 *            for raising errors
	 * @param purpose
	 *            Why are we inputting this record?
	 * @param maxRows
	 *            what is the maximum rows that we extract?
	 * @param fieldNames
	 *            subset of columns to be extracted. null if all columns are to
	 *            be extracted
	 * @return grid or null in case of error
	 */
	public Grid parseInputTable(String[][] data, DataCollection dc,
			InputRecordPurpose purpose, int maxRows, String[] fieldNames) {

		/*
		 * remember, first row is header/columnNames
		 */
		if (data == null) {
			return this.getEmptyGrid(fieldNames);
		}

		int nbrDataRows = data.length - 1;
		if (maxRows > 0 && nbrDataRows > maxRows) {
			String txt = "A maximum of " + maxRows + " is allowed for"
					+ this.name;
			Spit.out(txt);
			dc.addError(txt);
			return null;
		}

		if (nbrDataRows <= 0) {
			return this.getEmptyGrid(fieldNames);
		}

		/*
		 * grid works by column, and not by rows. We are better off working that
		 * way. we keep input columns in a map for ease of processing
		 */
		Map<String, String[]> inputColumnData = new HashMap<String, String[]>();
		String[] columnNames = data[0];
		for (int j = 0; j < columnNames.length; j++) {
			String columnName = columnNames[j];
			String[] column = new String[nbrDataRows];
			/*
			 * loop for each data row. note data[i+1][] because 0th row is
			 * header
			 */
			for (int i = 0; i < nbrDataRows; i++) {
				column[i] = data[i + 1][j];
			}
			inputColumnData.put(columnName, column);
		}

		boolean errorFound = false;
		Grid grid = new Grid(this.name);
		Set<String> fieldSubset = this.getIntoSet(fieldNames);
		/*
		 * we have input columns mapped by column names. Now, let us loop by
		 * fields in this record to create a grid
		 */

		for (Field field : this.fields) {
			String fieldName = field.getName();
			/*
			 * are we interested in this field?
			 */
			if (fieldSubset != null && fieldSubset.contains(fieldName) == false) {
				Spit.out(fieldName
						+ " column is skippe dbecause we are going for a subset");
				continue;
			}

			String[] column = inputColumnData.remove(fieldName);
			if (column == null) {
				Spit.out(fieldName
						+ " is not found as a column in input data for record "
						+ this.name);
				/*
				 * is it required?
				 */
				if (field.isRequired(purpose)) {
					field.addParseErrorr(null, dc);
					errorFound = true;
				}
				continue;
			}

			ValueList gridColumn = field.parseColumn(column, dc, purpose);
			if (gridColumn == null) {
				/*
				 * parseColumn would have added error message to dc
				 */
				errorFound = true;
			}

			if (errorFound) {
				/*
				 * no point in adding column if we are already in error.
				 */
				continue;
			}

			try {
				grid.addColumn(field.getName(), gridColumn);
			} catch (ExilityException e) {
				/*
				 * only exception is if the number of rows do not match
				 */
				dc.addError("Unexpected error while adding column "
						+ field.getName() + " : " + e.getMessage());
				errorFound = true;
			}
		}
		/*
		 * special column
		 */
		String[] actionColumn = inputColumnData
				.remove(CommonFieldNames.BULK_ACTION);
		if (actionColumn != null) {
			try {
				grid.addColumn(CommonFieldNames.BULK_ACTION,
						ValueList.newList(actionColumn));
			} catch (ExilityException e) {
				dc.addError("Unexpected error while adding column "
						+ CommonFieldNames.BULK_ACTION + " : " + e.getMessage());
				errorFound = true;
			}
		}
		if (errorFound) {
			Spit.out("Error while parsing data as per record " + this.name);
			return null;
		}
		/*
		 * did we have other columns in input data? just a warning
		 */
		if (inputColumnData.size() > 0) {
			for (String columnName : inputColumnData.keySet()) {
				Spit.out(columnName
						+ " is not expected as a column, and hence is discarded.");
			}
		}

		if (this.hasInterFieldValidations == false) {
			return grid;
		}

		/*
		 * inter field validations
		 */
		for (Field field : this.fields) {
			String fieldName = field.getName();
			if (field.needsInterFieldValidation()
					&& (fieldSubset == null || fieldSubset.contains(fieldName))) {
				boolean allOk = field.validateInterField(grid, dc, purpose);
				if (errorFound == false && allOk == false) {
					errorFound = true;
				}
			}
		}

		if (errorFound) {
			Spit.out("Error while parsing data due to inter-field validations as per record "
					+ this.name);
			return null;
		}

		return grid;
	}

	/**
	 * parse fields of this record from inData, validate and add to dc. We try
	 * to find as many errors as possible, rather than abandoning at first error
	 * 
	 * @param inData
	 * @param dc
	 * @param purpose
	 *            why are we receiving this record?
	 * @param fieldNames
	 *            subset of fields to extract. null if we want all fields from
	 *            this record
	 */
	public void parseInputValues(ServiceData inData, DataCollection dc,
			InputRecordPurpose purpose, String[] fieldNames) {

		Set<String> fieldSubset = this.getIntoSet(fieldNames);
		boolean errorFound = false;
		for (Field field : this.fields) {
			String fieldName = field.getName();
			/*
			 * do we include this field?
			 */
			if (fieldSubset != null && fieldSubset.contains(fieldName) == false) {
				continue;
			}

			String textValue = inData.getValue(fieldName);
			if (textValue == null || textValue.length() == 0) {
				if (field.isRequired(purpose)) {
					field.addParseErrorr("", dc);
					errorFound = true;
				}
				continue;
			}
			/*
			 * field takes care of validation as part of parsing
			 */
			boolean thisIsOk = field.parseValue(inData, dc, purpose);
			if (thisIsOk) {
				errorFound = true;
			}
		}

		if (errorFound || this.hasInterFieldValidations == false) {
			return;
		}

		/*
		 * inter-field validations
		 */
		Map<String, Value> values = dc.values;
		for (Field field : this.fields) {
			String fieldName = field.getName();
			/*
			 * if field is required, and it needs inter-field validations
			 */
			if (field.needsInterFieldValidation()
					&& (fieldSubset == null || fieldSubset.contains(fieldName))) {
				boolean ok = field.validateInterField(values, dc, purpose);
				if (!ok) {
					errorFound = true;
				}
			}
		}
	}

	/**
	 * extract grid from dc into outData
	 * 
	 * @param outData
	 * @param dc
	 *            to which grid is to be added to
	 * @param columnNames
	 *            use this subset of columns for the grid
	 * @param gridName
	 *            name of grid to be added to dc
	 */
	public void extractOutputTable(ServiceData outData, DataCollection dc,
			String[] columnNames, String gridName) {
		Grid grid = dc.getGrid(gridName);
		if (grid != null) {
			outData.addGrid(gridName, grid.getRawData(columnNames));
		}
	}

	/**
	 * extract fields from dc.values into outData.values
	 * 
	 * @param outData
	 * @param dc
	 * @param fieldNames
	 *            subset of fields to be extracted. null if all fields are to be
	 *            extracted
	 */
	public void extractOutputValues(ServiceData outData, DataCollection dc,
			String[] fieldNames) {

		if (fieldNames == null || fieldNames.length == 0) {
			/*
			 * extract all fields
			 */
			for (Field field : this.fields) {
				String fieldName = field.getName();
				Value value = dc.getValue(fieldName);
				if (value != null) {
					outData.addValue(fieldName, value.getTextValue());
				}
			}
		} else {
			/*
			 * extract subset
			 */
			Map<String, Field> allFields = this.getFieldsMap();
			for (String fieldName : fieldNames) {
				Field field = allFields.get(fieldName);
				if (field != null) {
					Value value = dc.getValue(fieldName);
					if (value != null) {
						outData.addValue(fieldName, value.getTextValue());
					}
				}
			}
		}
	}

	/**
	 * provide default page fields for all fields defined in this record.
	 * 
	 * @param forOutput
	 *            true if this record is used as output, false if it is used for
	 *            input
	 * @return array of fields to be used by a page
	 */
	public AbstractField[] getAllPageFields(boolean forOutput) {
		AbstractField[] allFields = new AbstractField[this.fields.length];
		int i = 0;
		for (Field field : this.fields) {
			allFields[i] = field.getPageField(forOutput);
		}
		return allFields;
	}

	/**
	 * get all fields in this record indexed by name
	 * 
	 * @return fields, indexed by name
	 */
	public Map<String, Field> getFieldsMap() {
		Map<String, Field> fieldsMap = new HashMap<String, Field>();
		for (Field field : this.fields) {
			/*
			 * thought of this instead of fieldsMap.put(field.getName(), field)
			 * as a better encapsulation
			 */
			field.getIntoMap(fieldsMap);
		}
		return fieldsMap;
	}

	/**
	 * create a Table instance based on this record
	 * 
	 * @return a Table object based on this record
	 */
	public Table getTable() {
		if (this.tableObject == null) {
			this.createTableObject();
		}
		return this.tableObject;
	}

	private void createTableObject() {
		Table table = new Table();
		this.tableObject = table;

		table.name = this.name;
		table.tableName = this.tableName;
		table.isAudited = this.isAudited;
		table.keyToBeGenerated = this.keyToBeGenerated;
		table.columns = new Column[this.fields.length];
		table.description = this.description;
		table.parentTableName = this.parentRecord;
		table.okToDelete = true;
		int i = 0;
		for (Field field : this.fields) {
			switch (field.columnType) {
			case temp:
				continue;
			case view:
				continue;

			case createdByUser:
				table.createdUserName = field.name;
				table.createdUserColumnName = field.columnName;
				break;
			case createdTimeStamp:
				table.createdTimestampName = field.name;
				table.createdTimestampColumnName = field.columnName;
				break;
			case modifiedByUser:
				table.modifiedUserName = field.name;
				table.modifiedUserColumnName = field.columnName;
				break;
			case modifiedTimeStamp:
				table.modifiedTimestampName = field.name;
				table.modifiedTimestampColumnName = field.columnName;
				break;
			default:

				table.columns[i] = field.getColumn();
				i++;
			}
		}
		/*
		 * what if we did not add all fields?
		 */
		if (i < this.fields.length) {
			Column[] realColumns = new Column[i];
			while (i > 0) {
				i--;
				realColumns[i] = table.columns[i];
			}
			table.columns = realColumns;
		}
		table.initialize();
	}

	/**
	 * return named field or null if field does not exist
	 * 
	 * @param fieldName
	 * @return field, or null
	 */
	public Field getField(String fieldName) {
		for (Field field : this.fields) {
			if (field.name.equals(fieldName)) {
				return field;
			}
		}
		return null;
	}

	/**
	 * get the alias name of this record.
	 * 
	 * @return alias name of this record
	 */
	public String getAliasedRecordName() {
		return this.aliasName;
	}

	/**
	 * create an empty grid that represents this record
	 * 
	 * @param columnNames
	 *            subset of column names to include. null if all columns are to
	 *            be included
	 * 
	 * @returns grid with header, but no data
	 */
	private Grid getEmptyGrid(String[] columnNames) {
		Grid grid = new Grid(this.name);
		Set<String> set = this.getIntoSet(columnNames);
		for (Field field : this.fields) {
			/*
			 * skip this field if it is not to be included
			 */
			String columnName = field.getName();
			if (set != null && set.contains(columnName) == false) {
				continue;
			}
			try {
				grid.addColumn(field.name,
						ValueList.newList(field.getValueType(), 0));
			} catch (ExilityException e) {
				Spit.out("Unable to create an empty column for field "
						+ field.name + " from record " + this.name);
			}
		}
		return grid;
	}

	private Set<String> getIntoSet(String[] names) {
		Set<String> set = null;
		if (names != null && names.length > 0) {
			set = new HashSet<String>();
			for (String a : names) {
				set.add(a);
			}
		}
		return set;

	}

	/**
	 * get a condition that is suitable to be used to filter this table for a
	 * given parent key
	 * 
	 * @return
	 */
	Condition[] getFilterForChildRows() {
		Condition[] conditions = { new Condition(this.childKey, this.parentKey,
				Comparator.EQUALTO) };
		return conditions;
	}

	/**
	 * get conditions for filter fields
	 * 
	 * @param dc
	 * @return
	 */
	Condition[] getFilterConditions(DataCollection dc) {
		List<Condition> conditions = new ArrayList<Condition>();
		for (Field field : this.fields) {
			Condition condition = field.getFilterCondition(dc);
			if (condition != null) {
				conditions.add(condition);
			}
		}
		return conditions.toArray(new Condition[0]);
	}

	/**
	 * get the field name in this record that links it to its parent
	 * 
	 * @return childKey
	 */
	public String getChildKey() {
		return this.childKey;
	}

	/**
	 * 
	 * @return primary key of this record
	 */
	public String getPrimaryKey() {
		return this.primaryKey;
	}

	/**
	 * return name of field in the parent record that links t back to this
	 * record
	 * 
	 * @return parent key
	 */
	public String getParentKey() {
		return this.parentKey;
	}

}

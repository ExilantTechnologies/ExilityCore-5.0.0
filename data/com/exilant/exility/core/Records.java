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
import java.util.Map;

/***
 * Manages records.
 * 
 */
public class Records {
	private static final String GLOBAL_INPUT_RECORD = "globalInputRecord";
	private static final String GLOBAL_OUTPUT_RECORD = "globalOutputRecord";

	String version = "1.0";

	/**
	 * special record that has all global fields that are generally kept in
	 * session/application scope and supplied as input to all services
	 */
	private static Record globalInputRecord = null;
	/**
	 * special record that has all global fields that are generally kept in
	 * session/application scope and supplied as input to all services
	 */
	private static Record globalOutputRecord = null;

	/**
	 * records are cached for performance during production run. We may have to
	 * use WeakHashmap in case of large installations
	 */
	private static Map<String, Record> records = new HashMap<String, Record>();

	static {
		loadGlobalRecords();
	}

	/**
	 * Get a record. Cache if required
	 * 
	 * @param recordName
	 * @return record, or null if it is not found.
	 */
	public static Record getRecord(String recordName) {
		Spit.out("Going look for record " + recordName);
		Record record = records.get(recordName);
		if (record != null) {
			return record;
		}

		String errorText = null;
		record = (Record) ResourceManager.loadResource("record." + recordName,
				Record.class);

		if (record == null) {
			errorText = "Record " + recordName + " does not exist";
		} else if (recordName.endsWith(record.name) == false) {
			errorText = "Record file " + recordName
					+ " has a record with its name wrongly set to "
					+ record.name;
		} else {

			/*
			 * is it an alias by any chance?
			 */
			String aliasName = record.getAliasedRecordName();
			if (aliasName != null) {
				record = Records.getRecord(aliasName);
				if (record == null) {
					errorText = "Record " + recordName + " refers to "
							+ aliasName + " as an alias, but " + aliasName
							+ " is not defined as a record";
				}
			}
		}
		if (errorText != null) {
			throw new RuntimeException(errorText);
		}
		/*
		 * do we cache it?
		 */
		if (AP.definitionsToBeCached) {
			records.put(recordName, record);
		}
		return record;
	}

	/**
	 * remove all cached records
	 */
	public static void purge() {
		records.clear();
		globalInputRecord = null;
		globalOutputRecord = null;
	}

	/**
	 * return special record that list all common input fields for any service
	 * 
	 * @return global input record, or null if it is not defined
	 */
	public static Record getGlobalInputRecord() {
		return globalInputRecord;
	}

	/**
	 * return special record that list all common output fields from any service
	 * 
	 * @return global output record, or null if it is not defined
	 */
	public static Record getGlobalOutputRecord() {
		return globalOutputRecord;
	}

	private static void loadGlobalRecords() {
		globalInputRecord = getRecord(GLOBAL_INPUT_RECORD);
		if (globalInputRecord == null) {
			Spit.out("This projetc has not set-up a global record for standard fields that are normally expected for every service. Your services will not work properly.");
			globalInputRecord = getDefaultInputRecord();
		}

		globalOutputRecord = getRecord(GLOBAL_OUTPUT_RECORD);
		if (globalOutputRecord == null) {
			Spit.out("This project has not set-up a global record for output. Only the output fields explicitly specified by individual services will be returned to client.");
		}
	}

	/**
	 * get a table based on the record
	 * 
	 * @param recordName
	 * @return table based on the record
	 */
	public static TableInterface getTable(String recordName) {
		Record record = getRecord(recordName);
		if (record == null) {
			return null;
		}
		return record.getTable();
	}

	private static Record getDefaultInputRecord() {
		Record record = new Record();
		record.name = "defaultInput";
		record.recordType = RecordType.STRUCTURE;
		Field field = new Field();
		field.name = AP.loggedInUserFieldName;
		field.dataType = "text";
		if (AP.loggedInUserDataTypeIsInteger) {
			field.dataType = "number";
		}
		record.fields = new Field[1];
		record.fields[0] = field;
		return record;
	}
}
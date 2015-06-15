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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * generic data structure that carries all data and message during the execution
 * of a service
 * 
 */
public class DataCollection {
	private static Pattern sqlRegex = Pattern.compile("sql[0-9]*");
	/**
	 * all primitive data stored as name/value pair in a hash-map
	 */
	public Map<String, Value> values = new HashMap<String, Value>();

	/**
	 * all lists
	 */
	public Map<String, ValueList> lists = new HashMap<String, ValueList>();

	/**
	 * all grids
	 */
	public Map<String, Grid> grids = new HashMap<String, Grid>();

	/**
	 * data sets that are used as source of data for crystal reports
	 */
	public HashMap<String, Object> dataSet = new HashMap<String, Object>();

	/**
	 * list of messages (error message, warnings etc..
	 */
	public MessageList messageList;

	/**
	 * current prefix in vogue for manipulating fields
	 */
	public String prefix = null;

	private final Map<String, Map<Long, Long>> generatedKeys = new HashMap<String, Map<Long, Long>>();
	/***
	 * If you want to implement the concept that all operations within a
	 * transaction boundary have the same time-stamp..
	 */
	private Value timeStamp;

	/**
	 * 
	 */
	public DataCollection() {
		this.timeStamp = Value.newValue(new Date());
		this.messageList = new MessageList();
	}

	DataCollection(DataCollection dc) {
		// if we are creating it from an existing dc, we are to link
		this.messageList = dc.messageList;
		this.copyInputFieldsTo(dc);
	}

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addBooleanValue(String name, boolean value) {
		this.addValue(name, Value.newValue(value));
	}

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addIntegralValue(String name, long value) {
		this.addValue(name, Value.newValue(value));
	}

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addDecimalValue(String name, double value) {
		this.addValue(name, Value.newValue(value));
	}

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addDateValue(String name, Date value) {
		this.addValue(name, Value.newValue(value));
	}

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addTextValue(String name, String value) {
		this.addValue(name, Value.newValue(value));
	}

	/**
	 * concatenate values in the arrays delimited by comma
	 * 
	 * @param name
	 * @param value
	 */
	public void addTextValue(String name, String[] value) {
		String textValue = "";
		if (value != null && value.length > 0) {
			/**
			 * make it a comma separated string
			 */
			StringBuilder sbf = new StringBuilder();
			for (String val : value) {
				sbf.append(val).append(ExilityConstants.LIST_SEPARATOR);
			}

			sbf.deleteCharAt(sbf.length() - 1);
			textValue = sbf.toString();
		}

		this.addValue(name, Value.newValue(textValue));
	}

	/**
	 * convert array of numbers into a comma separated list before adding it
	 * 
	 * @param name
	 * @param value
	 */
	public void addTextValue(String name, long[] value) {
		String textValue = "";
		if (value != null && value.length > 0) {
			/**
			 * make it a comma separated string
			 */
			StringBuilder sbf = new StringBuilder();
			for (long val : value) {
				sbf.append(val).append(',');
			}

			sbf.deleteCharAt(sbf.length() - 1);
			textValue = sbf.toString();
		}

		this.addValue(name, Value.newValue(textValue));
	}

	/**
	 * add a text value with null as its underlying value. We DO NOT RECOMMEND
	 * usage of this design
	 * 
	 * @param name
	 */
	public void addNullValue(String name) {
		this.addValue(name, new NullValue(DataValueType.TEXT));
	}

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addValue(String name, Value value) {
		if (value == null) {
			String msg = "Field "
					+ name
					+ " is being added with NULL as its value. This is not allowed.";
			Spit.out(msg);
			this.addError(msg);
			return;
		}
		this.values.put(name, value);
	}

	/**
	 * 
	 * @param name
	 * @param textValue
	 * @param dataValueType
	 */
	public void addValue(String name, String textValue,
			DataValueType dataValueType) {
		Value value = Value.newValue(textValue, dataValueType);
		if (value == null) {
			String msg = "Field " + name + " has value '" + textValue
					+ "'. This is not a valid " + dataValueType;
			Spit.out(msg);
			this.addError(msg);
			return;
		}
		this.values.put(name, value);
	}

	/**
	 * 
	 * @param name
	 * @return true if an entry is found for this field
	 */
	public boolean hasValue(String name) {
		return this.values.containsKey(name);
	}

	/**
	 * 
	 * @param name
	 * @return true if field is not found, or is found but is null
	 */
	public boolean isNull(String name) {
		Value value = this.values.get(name);
		if (value == null) {
			/**
			 * very tricky. But I think users will have this situation same as
			 * null. If they care, they can always use hasValue
			 */
			return true;
		}
		return value.isNull();
	}

	/**
	 * 
	 * @param name
	 * @return value, or null if value is not found
	 */
	public Value getValue(String name) {
		return this.values.get(name);
	}

	/**
	 * 
	 * @param name
	 * @return existing value, null if no value existed
	 */
	public Value removeValue(String name) {
		return this.values.remove(name);
	}

	/**
	 * 
	 * @param name
	 * @return value type of this field. null if no field is found with this
	 *         name
	 */
	public DataValueType getValueType(String name) {
		Value value = this.getValue(name);
		if (value != null) {
			return value.getValueType();
		}

		return null;
	}

	/**
	 * since we can not return null we allow you to use a default value
	 * 
	 * @param name
	 * @param defaultValue
	 * @return value if found. default value otherwise.
	 */
	public long getIntegralValue(String name, long defaultValue) {
		Value value = this.getValue(name);
		if (value != null) {
			return value.getIntegralValue();
		}

		return defaultValue;
	}

	/**
	 * since we can not return null we allow you to use a default value
	 * 
	 * @param name
	 * @param defaultValue
	 * @return value or default
	 */
	public double getDecimalValue(String name, double defaultValue) {
		Value value = this.getValue(name);
		if (value != null) {
			return value.getDecimalValue();
		}

		return defaultValue;
	}

	/**
	 * since we can not return null we allow you to use a default value
	 * 
	 * @param name
	 * @param defaultValue
	 * @return value or default
	 */
	public boolean getBooleanValue(String name, boolean defaultValue) {
		Value value = this.getValue(name);
		if (value != null) {
			return value.getBooleanValue();
		}
		return defaultValue;
	}

	/**
	 * value or default
	 * 
	 * @param name
	 * @param defaultValue
	 * @return value or default
	 */
	public Date getDateValue(String name, Date defaultValue) {
		Value value = this.getValue(name);
		if (value != null) {
			Date d = value.getDateValue();
			if (d.equals(Value.NULL_VALUE) == false) {
				return d;
			}
		}
		return defaultValue;
	}

	/**
	 * value or default
	 * 
	 * @param name
	 * @param defaultValue
	 * @return value or default
	 */
	public String getTextValue(String name, String defaultValue) {
		Value value = this.getValue(name);
		if (value != null) {
			return value.getTextValue();
		}
		return defaultValue;
	}

	/**
	 * 
	 * @param name
	 * @param valueList
	 */
	public void addValueList(String name, ValueList valueList) {
		this.lists.put(name, valueList);
	}

	/**
	 * 
	 * @param name
	 * @return list
	 */
	public ValueList getValueList(String name) {
		ValueList list = null;
		list = this.lists.get(name);
		return list;
	}

	/**
	 * 
	 * @param name
	 * @return true if this list exists
	 */
	public boolean hasList(String name) {
		return this.lists.containsKey(name);
	}

	/**
	 * 
	 * @param name
	 */
	public void removeValueList(String name) {
		this.lists.remove(name);
	}

	/**
	 * 
	 * @param name
	 * @param grid
	 */
	public void addGrid(String name, Grid grid) {
		grid.setName(name);
		this.grids.put(name, grid);
	}

	/**
	 * use this raw data to create a grid before adding it
	 * 
	 * @param name
	 * @param data
	 */
	public void addGrid(String name, String[][] data) {
		Grid grid = new Grid(name);
		try {
			grid.setRawData(data);
			this.grids.put(name, grid);
		} catch (ExilityException e) {
			this.addError("Error while adding raw data to gird "
					+ name
					+ ". Either header row is missing, or columns may have invalid data.");
		}
	}

	/**
	 * 
	 * @param name
	 * @return true if an entry is found for this grid
	 */
	public boolean hasGrid(String name) {
		return this.grids.containsKey(name);
	}

	/**
	 * 
	 * @param name
	 * @return grid, or null if it is not found
	 */
	public Grid getGrid(String name) {
		Grid grid = null;
		grid = this.grids.get(name);
		return grid;
	}

	/**
	 * 
	 * @param name
	 */
	public void removeGrid(String name) {
		this.grids.remove(name);
	}

	/**
	 * not used in java version. used in .net version to keep data sets
	 * 
	 * @param name
	 * @param val
	 */
	public void addDataSet(String name, Object val) {
		this.dataSet.put(name, val);
	}

	/**
	 * not used in java version. used in .net version to keep data sets
	 * 
	 * @param name
	 * @return whether an entry is found for this data set
	 */
	public boolean hasDataSet(String name) {
		return this.dataSet.containsKey(name);
	}

	/**
	 * not used in java version. used in .net version to keep data sets
	 * 
	 * @param name
	 * @return data set
	 */
	public Object getDataSet(String name) {
		if (this.dataSet.containsKey(name)) {
			return this.dataSet.get(name);
		}

		return null;
	}

	/**
	 * 
	 * @param name
	 */
	public void removeDataSet(String name) {
		this.dataSet.remove(name);
	}

	/**
	 * add a message e with this id
	 * 
	 * @param messageId
	 * @param parameters
	 * @return severity of the message that god=t added
	 */
	public MessageSeverity addMessage(String messageId, String... parameters) {
		return this.messageList.addMessage(messageId, parameters);
	}

	/**
	 * 
	 * @param errorText
	 */
	public void addError(String errorText) {
		this.messageList.addError(errorText);
	}

	/**
	 * 
	 * @param warningText
	 */
	public void addWarning(String warningText) {
		this.messageList.addWarning(warningText);
	}

	/**
	 * 
	 * @param infoText
	 */
	public void addInfo(String infoText) {
		this.addInfo(infoText);
	}

	/**
	 * check if we already have a message with the given id
	 * 
	 * @param messageId
	 * @return true if message with this id id found
	 */
	public boolean hasMessage(String messageId) {
		return this.messageList.hasMessage(messageId);
	}

	/**
	 * if any of the message that is added has severity of ERROR
	 * 
	 * @return true if any message with severity ERROR is found in this dc
	 */
	public boolean hasError() {
		return (this.messageList.hasError());
	}

	/**
	 * raise an exception after adding this message
	 * 
	 * @param msgId
	 * @param parameters
	 * @throws ExilityException
	 */
	public void raiseException(String msgId, String... parameters)
			throws ExilityException {
		this.addMessage(msgId, parameters);
		Spit.out("Exception raised with message Id =  " + msgId);
		throw new ExilityException();
	}

	/**
	 * raise an exception after adding this message
	 * 
	 * @param message
	 * 
	 * @throws ExilityException
	 */
	public void raiseException(String message) throws ExilityException {
		this.addError(message);
		throw new ExilityException(message);
	}

	/**
	 * return all grid names.
	 * 
	 * @return grid names. Could be an array of size 0, but is non-null.
	 */
	public String[] getFieldNames() {
		return this.keysToArray(this.values.keySet());
	}

	/**
	 * 
	 * @return names of all grids
	 */
	public String[] getGridNames() {
		return this.keysToArray(this.grids.keySet());
	}

	/**
	 * convert a set into an array of strings.
	 * 
	 * @param keys
	 * @return array of strings for
	 */
	public String[] keysToArray(Set<String> keys) {
		String[] keysArray = new String[keys.size()];
		int i = 0;
		for (String key : keys) {
			keysArray[i++] = key;
		}

		return keysArray;
	}

	/**
	 * get severity of the last message that was added. This is ONLY for the
	 * last message, and not highest severity of all messages
	 * 
	 * @return severity of last message that was added
	 */
	public MessageSeverity getLastSevirity() {
		return this.messageList.getLastSeverity();
	}

	/**
	 * append contents of one dc to other
	 * 
	 * @param dc
	 */

	public void append(DataCollection dc) {
		// messages and fields must be already there. We are left with the three
		// collections
		for (String key : dc.values.keySet()) {
			this.values.put(key, dc.values.get(key));
		}
		for (String key : dc.grids.keySet()) {
			this.grids.put(key, dc.grids.get(key));
		}
		for (String key : dc.lists.keySet()) {
			this.lists.put(key, dc.lists.get(key));
		}
	}

	/**
	 * get highest severity of any message that is added
	 * 
	 * @return highest severity of any message added
	 */
	public MessageSeverity getSeverity() {
		return this.messageList.getSevirity();
	}

	/***
	 * Parse value as per data type of name and add parsed value to dc
	 * 
	 * @param name
	 *            name of field
	 * @param value
	 *            string value of field
	 */
	public void addValueAfterCheckingInDictionary(String name, String value) {
		if (value == null) {
			return;
		}

		DataValueType vt = DataDictionary.getDataType(name).getValueType();
		Value val = null;
		String trimmedvalue = value.trim();
		/**
		 * for text, empty string still means a valid value. For other data
		 * type, empty string translates to null
		 */
		if (trimmedvalue.length() == 0) {
			val = new NullValue(vt);
		} else {
			val = Value.newValue(trimmedvalue, vt);
			/**
			 * newValue() returns null if the value is not parsed properly. It
			 * would have taken care of logging error message
			 */
			if (val == null) {
				String msg = "Field " + name + " has value '" + trimmedvalue
						+ "'. This is not a valid " + vt;
				Spit.out(msg);
				this.addError(msg);
				return;
			}
		}

		this.values.put(name, val);
	}

	/**
	 * convert list of values into a valueList of appropriate type based on an
	 * entry in data dictionary for this name
	 * 
	 * @param name
	 *            field name
	 * @param value
	 *            list of values
	 */
	public void addListAfterCheckingInDictionary(String name, String[] value) {
		AbstractDataType dt = DataDictionary.getDataType(name);
		ValueList list = dt.getValueList(name, value, true, this);
		if (list != null) {
			this.lists.put(name, list);
		}
	}

	/**
	 * use first row as column names. Use data dictionary to get the value type
	 * of columns, and create a grid
	 * 
	 * @param name
	 *            grid name
	 * @param data
	 *            raw data in string
	 */
	public void addGridAfterCheckingInDictionary(String name, String[][] data) {
		Grid grid = new Grid(name);
		try {
			grid.setRawData(data);
			this.grids.put(name, grid);
		} catch (Exception e) {
			this.addError("Grid " + name
					+ " has some invalid numeric/date field/s. "
					+ e.getMessage());
		}
	}

	/**
	 * copy fields from service data into this dc
	 * 
	 * @param serviceData
	 */
	public void copyFrom(ServiceData serviceData) {
		for (String key : serviceData.values.keySet()) {
			this.addValueAfterCheckingInDictionary(key,
					serviceData.values.get(key));
		}

		for (String key : serviceData.lists.keySet()) {
			this.addListAfterCheckingInDictionary(key,
					serviceData.lists.get(key));
		}

		for (String key : serviceData.grids.keySet()) {
			this.addGridAfterCheckingInDictionary(key,
					serviceData.grids.get(key));
		}
	}

	/**
	 * copy data to a serviceData
	 * 
	 * @param serviceData
	 */
	public void copyTo(ServiceData serviceData) {
		try {
			for (String key : this.values.keySet()) {
				// temp fix for old version of exility that was pushing sqls as
				// fields
				if (key.indexOf("sql") == 0 && key.equals("sql") == false) {
					if (DataCollection.sqlRegex.matcher(key).matches()) {
						Spit.out(this.values.get(key).toString());
						continue;
					}
				}
				// serviceData.values[key] = this.values[key].ToString();
				Value value = this.values.get(key);
				String textValue = "";
				if (value != null && !value.isNull()) {
					AbstractDataType dt = DataDictionary.getDataType(key);
					// dt is guaranteed to be non-null. A default is returned if
					// dataType does not exist
					textValue = dt.format(value);
					if (textValue == null) {
						// If an invalid value is passed then that's an error.
						String msg = "Field " + key + " has value '"
								+ value.toString()
								+ "'. This is declared as datatype: " + dt.name
								+ " .This is not right.";
						Spit.out(msg);
						this.addError(msg);
						return;
					}
				}
				serviceData.values.put(key, textValue);
			}

			for (String key : this.lists.keySet()) {
				serviceData.lists.put(key, this.lists.get(key).format());
			}

			for (String key : this.grids.keySet()) {
				serviceData.grids.put(key, this.grids.get(key).getRawData());
			}

			serviceData.messageList = this.messageList;
		} catch (Exception ex) {
			this.addError(ex.getMessage());
			Spit.out(ex.getMessage());
		}
	}

	/**
	 * 
	 * @param serviceData
	 */
	public void copyInternalFieldsTo(ServiceData serviceData) {
		for (InternalOutputField field : InternalOutputField.values()) {
			String name = field.toString();
			if (this.hasValue(name)) {
				serviceData.values.put(name, this.getTextValue(name, ""));
			}
		}

		serviceData.messageList = this.messageList;
	}

	/**
	 * 
	 * @param serviceData
	 */
	public void copyInternalFieldsFrom(ServiceData serviceData) {
		for (InternalInputField field : InternalInputField.values()) {
			String key = field.toString();
			if (serviceData.hasValue(key)) {
				String val = serviceData.values.get(key);
				this.values.put(key, Value.newValue(val));
			}
		}
	}

	/**
	 * 
	 * @param outData
	 */
	public void copyMessages(ServiceData outData) {
		outData.messageList = this.messageList;
	}

	/**
	 * 
	 * @param dc
	 */
	public void copyInputFieldsTo(DataCollection dc) {
		for (InternalInputField field : InternalInputField.values()) {
			String fieldName = field.toString();
			dc.addValue(fieldName, this.getValue(fieldName));
		}
	}

	/**
	 * 
	 * @return a text that has all messages. new lines are used to separate
	 *         messages
	 */
	public String getErrorMessage() {
		String msg = "";
		if (this.hasError()) {
			for (String m : this.messageList.getErrorTexts()) {
				msg += '\n' + m;
			}
		}
		return msg;
	}

	/**
	 * 
	 * @param tableName
	 * @param oldKey
	 * @param generatedKey
	 */
	public void addGeneratedKey(String tableName, long oldKey, long generatedKey) {
		Map<Long, Long> keys = this.generatedKeys.get(tableName);
		if (keys == null) {
			keys = new HashMap<Long, Long>();
			this.generatedKeys.put(tableName, keys);
		}
		keys.put(new Long(oldKey), new Long(generatedKey));
	}

	/**
	 * 
	 * @param tableName
	 * @param keys
	 */
	public void addGeneratedKey(String tableName, Map<Long, Long> keys) {
		if (this.generatedKeys.containsKey(tableName)) {
			this.generatedKeys.get(tableName).putAll(keys);
			return;
		}
		this.generatedKeys.put(tableName, keys);
	}

	/**
	 * 
	 * @param tableName
	 * @return generated keys mapped by old key into new keys
	 */
	public Map<Long, Long> getGeneratedKeys(String tableName) {
		return this.getGeneratedKeys(tableName);
	}

	/**
	 * get generated key for an old key
	 * 
	 * @param tableName
	 * @param oldKey
	 * @return new key, or 0 if old key was not found
	 */
	public long getGeneratedKey(String tableName, long oldKey) {
		Map<Long, Long> keys = this.getGeneratedKeys(tableName);
		if (keys == null) {
			return 0;
		}
		Long l = keys.get(new Long(oldKey));
		if (l == null) {
			return 0;
		}
		return l.longValue();
	}

	/**
	 * clear all messages
	 */
	public void zapMessages() {
		this.messageList = new MessageList();
	}

	/**
	 * 
	 * @return user id
	 */
	public Value getUserId() {
		return this.getValue(AP.loggedInUserFieldName);
	}

	/**
	 * common time stamp created when this dc is created. objective is to use a
	 * common time stamp for all rdbms updates done for this service, though
	 * they may happen at different milli/mico second intervals
	 * 
	 * @return common time stamp
	 */
	public Value getTimeStamp() {
		return this.timeStamp;
	}

	void toSpreadSheetXml(StringBuilder sbf, String sheetPrefix) {
		String sheetName;
		int n = this.values.size();
		if (n > 0) {
			Grid grid = this.valuesToGrid();
			sheetName = CommonFieldNames.VALUES_SHEET_NAME;
			if (sheetPrefix != null) {
				sheetName = sheetPrefix + sheetName;
			}
			grid.toSpreadSheetXml(sbf, sheetName);
		}

		n = this.grids.size();
		if (n == 0) {
			return;
		}

		for (String key : this.grids.keySet()) {
			Grid grid = this.grids.get(key);
			sheetName = sheetPrefix == null ? key : sheetPrefix + key;
			grid.toSpreadSheetXml(sbf, sheetName);
		}
		return;
	}

	/***
	 * does this dc has all the expected fields and grids, as specified in the
	 * supplied dc? This is useful in automating comparison of test results with
	 * expected results
	 * 
	 * @param dc
	 *            Data Collection that has the expected fields and grids
	 * @return true if all fields and grids in dc have matching elements in this
	 */
	public boolean hasAllFieldsOf(DataCollection dc) {
		boolean matched = true;
		for (String fieldName : dc.values.keySet()) {
			Value thisValue = this.getValue(fieldName);
			if (thisValue == null) {
				this.addError(fieldName + " is not found in the reslts");
				matched = false;
			} else {
				Value otherValue = dc.getValue(fieldName);
				if (!thisValue.equal(otherValue).getBooleanValue()) {
					this.addError("Field " + fieldName
							+ " is expected to have a value of "
							+ otherValue.getTextValue() + " but its value is "
							+ thisValue.getTextValue());
					matched = false;
				}
			}
		}

		for (String gridName : dc.grids.keySet()) {
			Grid thisGrid = this.grids.get(gridName);
			if (thisGrid == null) {
				this.addError("Grid " + gridName + " not found in the reslts.");
				matched = false;
			} else {
				Grid otherGrid = dc.grids.get(gridName);
				matched = thisGrid.equal(otherGrid, this);
			}
		}

		return matched;
	}

	/***
	 * copy first row from the grid to values
	 * 
	 * @param gridName
	 *            name of the grid to copy from
	 */
	public void gridToValues(String gridName) {
		Grid grid = this.getGrid(gridName);
		if (grid == null) {
			Spit.out(gridName
					+ " is not a grid in dc. could not copy columns into values.");
			return;
		}

		for (String columnName : grid.getColumnNames()) {
			this.addValue(columnName, grid.getValue(columnName, 0));
		}
	}

	/***
	 * create a grid from values
	 * 
	 * @return grid that contains all values
	 */
	public Grid valuesToGrid() {
		Grid grid = new Grid(CommonFieldNames.VALUES_SHEET_NAME);
		for (String key : this.values.keySet()) {
			Value value = this.values.get(key);
			ValueList column = ValueList.newList(value.getValueType(), 1);
			column.setValue(value, 0);
			try {
				grid.addColumn(key, column);
			} catch (Exception e) {
				Spit.out("Column " + key
						+ " could not be added with its value "
						+ value.getTextValue());
			}
		}
		return grid;
	}

	/**
	 * Try and get a list, either from grid columns, lists or a comma separated
	 * value.
	 * 
	 * @param possibleGridName
	 *            optional, if we have to try grid
	 * @param fieldName
	 *            column name of grid, or name of list/value
	 * @return array of text values, or null if we can not find this
	 */
	public String[] getTextValueList(String possibleGridName, String fieldName) {
		ValueList list = null;
		/**
		 * first try column in a grid
		 */
		if (possibleGridName != null) {
			Grid grid = this.getGrid(possibleGridName);
			if (grid != null) {
				list = grid.getColumn(fieldName);
			}
			if (list == null) {
				return null;
			}
			return list.getTextList();
		}

		/**
		 * try in this.lists
		 */
		list = this.getValueList(fieldName);
		if (list != null) {
			return list.getTextList();
		}

		/**
		 * last resort : this.values
		 */
		String textValue = this.getTextValue(fieldName, "");
		if (textValue == null || textValue.length() == 0) {
			return null;
		}
		return textValue.split(",");
	}

	/**
	 * Saves any values in dc.values with the same name as any of the column.
	 * Then, for each row in the grid, column values are copied into dc.values,
	 * and iterator.iterate(0 method is invoked.
	 * 
	 * @param gridName
	 *            name of the grid on which to iterate.
	 * @param iterator
	 *            object that is used for iteration
	 * @return number of rows over which the iteration took place. (same as
	 *         number of rows in the grid) 0 is returned, with no error if grid
	 *         is not found, or the grid had no rows,
	 */
	public int iterate(String gridName, GridIteratorInterface iterator) {
		Grid grid = this.getGrid(gridName);
		if (grid == null) {
			return 0;
		}
		return grid.iterate(this, iterator);
	}
}
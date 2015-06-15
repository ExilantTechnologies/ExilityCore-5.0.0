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
import java.util.Set;

/***
 * Data structure to hold generic data. Data is all in text, so that this is
 * amenable to be transmitted across layers with text based protocols (like
 * HTTP)
 * 
 */
public class ServiceData {

	/***
	 * name-value pairs
	 */
	public Map<String, String> values = new HashMap<String, String>();

	/***
	 * name-list pair. generally not used. Single column grid is preferred.
	 */
	public Map<String, String[]> lists = new HashMap<String, String[]>();

	/***
	 * name-table pairs
	 */
	public Map<String, String[][]> grids = new HashMap<String, String[][]>();

	/***
	 * messages
	 */
	public MessageList messageList = new MessageList();

	/**
	 * default
	 */
	public ServiceData() {
	}

	/***
	 * add a name-value pair. Existing field with this name, if any, is replaced
	 * 
	 * @param name
	 * @param value
	 */
	public void addValue(String name, String value) {
		this.values.put(name, value);
	}

	/***
	 * get field value
	 * 
	 * @param name
	 * @return fiend value, or null
	 */
	public String getValue(String name) {
		return this.values.get(name);
	}

	/***
	 * is there some one with this name?
	 * 
	 * @param name
	 * @return true if have an entry for this field
	 */
	public boolean hasValue(String name) {
		return this.values.containsKey(name);
	}

	/***
	 * remove this field. No problem if it never existed.
	 * 
	 * @param name
	 */
	public void removeValue(String name) {
		this.values.remove(name);
	}

	/***
	 * add a grid with this name. Existing one will be replaced
	 * 
	 * @param name
	 * @param grid
	 */
	public void addGrid(String name, String[][] grid) {
		this.grids.put(name, grid);
	}

	/***
	 * Is there a grid with this name?
	 * 
	 * @param name
	 * @return true of we have an entry for this grid
	 */
	public boolean hasGrid(String name) {
		return this.grids.containsKey(name);
	}

	/***
	 * get this grid
	 * 
	 * @param name
	 * @return grid, or null
	 */
	public String[][] getGrid(String name) {
		return this.grids.get(name);
	}

	/***
	 * If you do not know the name, but want to get at least the first one, here
	 * you go..
	 * 
	 * @return first grid if there is at least one grid. null otherwise.
	 */
	public String[][] getFirstGrid() {
		if (this.grids.size() == 0) {
			return null;
		}

		String name = this.grids.keySet().iterator().next();
		return this.grids.get(name);
	}

	/***
	 * remove this grid. No problem if never was...
	 * 
	 * @param name
	 */
	public void removeGrid(String name) {
		this.grids.remove(name);
	}

	/***
	 * add this list. replace if a list exists with this name.
	 * 
	 * @param name
	 * @param list
	 */
	public void addList(String name, String[] list) {
		this.lists.put(name, list);
	}

	/***
	 * is there a list ?
	 * 
	 * @param name
	 * @return if we have an entry for this list
	 */
	public boolean hasList(String name) {
		return this.lists.containsKey(name);
	}

	/***
	 * get the list
	 * 
	 * @param name
	 * @return array, or null
	 */
	public String[] getList(String name) {
		return this.lists.get(name);
	}

	/***
	 * remove the list. No complaints if it is not there
	 * 
	 * @param name
	 */
	public void removeList(String name) {
		this.lists.remove(name);
	}

	/***
	 * get an array of all field names in the fields collection
	 * 
	 * @return array is gauranteed. array length is zero if there are no fields
	 */
	String[] getFieldNames() {
		return this.keysToArray(this.values.keySet());
	}

	/***
	 * get an array of list names in the lists collection
	 * 
	 * @return array of names. length is zero if no lists
	 */
	String[] getListNames() {
		return this.keysToArray(this.lists.keySet());
	}

	/***
	 * get all the grid names in the list collection.
	 * 
	 * @return array of grid names. zero length array in case there are no grids
	 */
	public String[] getGridNames() {
		return this.keysToArray(this.grids.keySet());
	}

	/***
	 * get the highest level of error
	 * 
	 * @return 2 for error, 1 for warning, 0 otherwise
	 */
	public int getErrorStatus() {
		switch (this.messageList.getSevirity()) {
		case IGNORE:
		case UNDEFINED:
		case INFO:
			return 0;

		case WARNING:
			return 1;

		default:
			return 2;
		}
	}

	/***
	 * convert a set of strings to an array
	 * 
	 * @param keys
	 * @return
	 */
	private String[] keysToArray(Set<String> keys) {
		String[] keysArray = new String[keys.size()];
		int i = 0;
		for (String key : keys) {
			keysArray[i++] = key;
		}

		return keysArray;
	}

	/***
	 * add a message, possibly with parameters in it
	 * 
	 * @param messageId
	 *            if the message id is not present, message is ignored
	 * @param params
	 */
	public void addMessage(String messageId, String... params) {
		this.messageList.addMessage(messageId, params);
	}

	/**
	 * Add an error text as an error message. To be used typically for internal
	 * errors that are not internationalized.
	 * 
	 * @param errorText
	 */
	public void addError(String errorText) {
		this.messageList.addError(errorText);
	}

	/***
	 * de-serialize data from a serialized text string
	 * 
	 * @param inputText
	 * @throws ExilityException
	 *             in case the string is not a valid serialized string
	 */
	public void extractData(String inputText) throws ExilityException {
		String[] tables = inputText.split(CommonFieldNames.TABLE_SEPARATOR);
		boolean valuesFound = false;
		for (String gridText : tables) {
			if (gridText.trim().length() == 0) {
				continue;
			}
			String[] data = gridText.split(CommonFieldNames.BODY_SEPARATOR);
			if (data.length != 2) {
				throw new ExilityException(
						"Invalid data serialization: name-table has "
								+ data.length + " parts.");
			}
			String gridName = data[0];
			String[] rows = data[1].split(CommonFieldNames.ROW_SEPARATOR, -1);
			if (rows.length > AP.DEFAULT_MAX_ROWS) {
				throw new ExilityException("grid " + gridName
						+ " has too many rows.");
			}
			String[][] grid = new String[rows.length][];
			String[] header = rows[0].split(CommonFieldNames.FIELD_SEPARATOR,
					-1);
			grid[0] = header;
			for (int i = 1; i < grid.length; i++)// header already extracted
			{
				String[] row = rows[i].split(CommonFieldNames.FIELD_SEPARATOR,
						-1);
				if (row.length != header.length) {
					throw new ExilityException(
							"Invalid data serialization: "
									+ gridName
									+ " does has different number of cells in its rows.");
				}
				grid[i] = row;
			}

			// name-value pairs are sent in a designated grid
			if (valuesFound == false
					&& gridName.equals(CommonFieldNames.VALUES_TABLE_NAME)) {
				valuesFound = true;
				for (String[] pair : grid) {
					this.values.put(pair[0], pair[1]);
				}
			} else {
				this.addGrid(gridName, grid);
			}
		}
	}

	/***
	 * copy data from another data structure
	 * 
	 * @param inData
	 */
	public void extractData(ServiceData inData) {
		if (inData.values.size() > 0) {
			for (String key : inData.values.keySet()) {
				this.values.put(key, inData.getValue(key));
			}
		}

		if (inData.grids.size() > 0) {
			for (String key : inData.grids.keySet()) {
				String[][] grid = inData.grids.get(key);
				int n = grid.length;
				int m = grid[0].length;
				String[][] newGrid = new String[n][];
				this.grids.put(key, newGrid);

				for (int i = 0; i < n; i++) {
					String[] newRow = new String[m];
					String[] row = grid[i];
					newGrid[i] = newRow;
					for (int j = 0; j < m; j++) {
						newRow[j] = row[j];
					}
				}
			}
		}

		if (inData.lists.size() > 0) {
			for (String key : inData.lists.keySet()) {
				String[] list = inData.lists.get(key);
				String[] newList = new String[list.length];
				this.lists.put(key, newList);
				for (int j = 0; j < list.length; j++) {
					newList[j] = list[j];
				}
			}
		}
	}

	/***
	 * serializes contents as per a simple separator based algorithm
	 * NOTE:TABLE_SEPARATOR is inserted in the beginning as well as at the end
	 * to avoid any padding characters inserted by others (like jsp)
	 * 
	 * @return serialized string that represents this data
	 */
	public String toSerializedData() {
		StringBuilder sbf = new StringBuilder();

		// values
		sbf.append(CommonFieldNames.VALUES_TABLE_NAME).append(
				CommonFieldNames.BODY_SEPARATOR);
		this.appendValues(sbf);

		// messages
		if (this.messageList.size() > 0) {
			String[][] msgs = this.messageList.toGrid();
			sbf.append(CommonFieldNames.TABLE_SEPARATOR)
					.append(CommonFieldNames.MESSAGES_TABLE_NAME)
					.append(CommonFieldNames.BODY_SEPARATOR);
			this.appendGrid(sbf, msgs);
		}

		// grids
		for (String key : this.grids.keySet()) {
			String[][] grid = this.grids.get(key);
			if (grid.length > 0 && grid[0].length > 0) {
				sbf.append(CommonFieldNames.TABLE_SEPARATOR).append(key)
						.append(CommonFieldNames.BODY_SEPARATOR);
				this.appendGrid(sbf, grid);
			}
		}
		sbf.append(CommonFieldNames.TABLE_SEPARATOR);
		return sbf.toString();
	}

	/***
	 * append grid to serialized string buffer
	 * 
	 * @param sbf
	 * @param grid
	 */
	private void appendGrid(StringBuilder sbf, String[][] grid) {
		if (grid.length == 0 || grid[0].length == 0) {
			Spit.out("Grid has no data. Not added to buffer.");
			return;
		}
		boolean firstRow = true;
		for (String[] row : grid) {
			if (firstRow) {
				firstRow = false;
			} else {
				sbf.append(CommonFieldNames.ROW_SEPARATOR);
			}
			boolean firstCol = true;
			for (String col : row) {
				if (firstCol) {
					firstCol = false;
				} else {
					sbf.append(CommonFieldNames.FIELD_SEPARATOR);
				}
				sbf.append(col);
			}
		}
	}

	/***
	 * append a value field to serialized string buffer
	 * 
	 * @param sbf
	 */
	private void appendValues(StringBuilder sbf) {
		// push the header row
		sbf.append("name").append(CommonFieldNames.FIELD_SEPARATOR)
				.append("value").append(CommonFieldNames.ROW_SEPARATOR);
		// success is a special field that is set to "0" if there is any error
		// message
		String success = this.messageList.hasError() ? "0" : "1";
		sbf.append(CommonFieldNames.SUCCESS_FIELD_NAME)
				.append(CommonFieldNames.FIELD_SEPARATOR).append(success);

		for (String key : this.values.keySet()) {
			sbf.append(CommonFieldNames.ROW_SEPARATOR).append(key)
					.append(CommonFieldNames.FIELD_SEPARATOR)
					.append(this.values.get(key));
		}
	}

	/***
	 * Does it contain errors?
	 * 
	 * @return true if highest severity of any message in the list is error.
	 *         Else false
	 */
	public boolean hasError() {
		return this.getErrorStatus() >= CommonFieldNames.SEVERITY_ERROR;
	}

	/***
	 * returns all messages as a new-line separated string
	 * 
	 * @return all messages in a string
	 */
	public String getMessages() {
		String[] msgs = this.messageList.getMessageTexts();
		String msg = "";
		for (String m : msgs) {
			msg += '\n' + m;
		}

		return msg;
	}

	/**
	 * copy all details from another service data
	 * 
	 * @param dataToCopyFrom
	 */
	public void copyFrom(ServiceData dataToCopyFrom) {
		this.values.putAll(dataToCopyFrom.values);
		this.grids.putAll(dataToCopyFrom.grids);
		if (dataToCopyFrom.lists.size() > 0) {
			this.lists.putAll(dataToCopyFrom.lists);
		}
		this.messageList.copyFrom(dataToCopyFrom.messageList);
	}
}
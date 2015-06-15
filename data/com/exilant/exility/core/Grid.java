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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/***
 * Represents a tabular data structure where each column has the same data type
 * across rows. This is modeled as a collection of columns, each column modeled
 * as a uniquely name ValueList. In its raw form, it is considered to be an
 * array, with its first row as column name, and subsequent rows representing
 * data in text format.
 */
public class Grid {
	private static final int UNKNOWN_ROWS = -1;
	private int numberOfRows = Grid.UNKNOWN_ROWS;
	private int numberOfColumns = 0;

	private String name = "noName";
	/**
	 * A simple, type-agnostic grid consists of a 2d String array that contains
	 * the name of columns in its first row in that case, we keep raw-data, as
	 * well as a map of columns names with their column index
	 */
	private String[][] rawData = null;
	private Map<String, Integer> columnIndexes = new HashMap<String, Integer>();

	/*
	 * A type-safe grid consists of ValueLists as columns. In this case, there
	 * is no order of columns.
	 */
	/***
	 * made it package-private, but did not realize that projects could create
	 * their own classes with com.exilant.exility.core!!!!
	 */
	Map<String, ValueList> columnValues = new HashMap<String, ValueList>();

	/**
	 * default constructor
	 */
	public Grid() {
	}

	/**
	 * create grid with this name
	 * 
	 * @param name
	 */
	public Grid(String name) {
		this.name = name;
	}

	/**
	 * set name of this grid
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/***
	 * parse raw data into appropriate type based on data dictionary
	 * 
	 * @param rawData
	 * @throws ExilityException
	 */
	public void setRawData(String[][] rawData) throws ExilityException {
		this.setRawData(rawData, null);
	}

	/***
	 * parse raw data into appropriate type based on data dictionary
	 * 
	 * @param rawData
	 * @param types
	 *            data value type of the columns. null if you do not know
	 * @throws ExilityException
	 */
	public void setRawData(String[][] rawData, DataValueType[] types)
			throws ExilityException {
		if (rawData == null || rawData.length == 0) {
			throw new ExilityException(
					"raw data for a grid must have a header row");
		}
		this.rawData = rawData;
		this.columnIndexes = new HashMap<String, Integer>();
		this.columnValues = new HashMap<String, ValueList>();
		this.numberOfColumns = rawData[0].length;
		this.numberOfRows = rawData.length - 1;

		String[] columnNames = rawData[0];
		for (int j = 0; j < columnNames.length; j++) {
			String columnName = columnNames[j];
			if (columnName == null) {
				columnName = "";
			}

			this.columnIndexes.put(columnName, new Integer(j));
			String[] list = new String[this.numberOfRows];

			for (int i = 0; i < list.length; i++) {
				list[i] = this.rawData[i + 1][j];
			}

			DataValueType vt = types == null ? DataDictionary
					.getValueType(columnName) : types[j];
			this.columnValues.put(columnName, ValueList.newList(list, vt));
		}
	}

	/***
	 * remove cached raw data, so that any request to get it will force
	 * recreation
	 */
	public void resetRawData() {
		this.rawData = null;
	}

	/***
	 * add a column to the grid
	 * 
	 * @param columnName
	 * @param column
	 * @throws ExilityException
	 *             , if column length mismatch
	 */
	public void addColumn(String columnName, ValueList column)
			throws ExilityException {
		int nrows = column.length();
		if (this.numberOfRows == Grid.UNKNOWN_ROWS) {
			this.numberOfRows = nrows;
		} else if (nrows != this.numberOfRows) {
			throw new ExilityException(
					"exilWrongRowMismatch. Existing grid has "
							+ this.numberOfRows
							+ " while a column is being added with " + nrows
							+ " rows.");
		}

		if (this.columnIndexes.containsKey(columnName)) {
			this.columnValues.remove(columnName);
		} else {
			this.columnIndexes.put(columnName,
					new Integer(this.numberOfColumns));
			this.numberOfColumns++;
		}
		this.columnValues.put(columnName, column);
		this.rawData = null;
	}

	/***
	 * a complex way to construct a grid. Specifically designed to help our
	 * db-driver to extract data into a grid
	 * 
	 * @param columns
	 *            array of output columns
	 * @param values
	 *            values for the column
	 * @param dc
	 *            optional. error messages, if any are added to dc
	 */
	public void setValues(OutputColumn[] columns, List<Value[]> values,
			DataCollection dc) {
		this.columnIndexes = new HashMap<String, Integer>();
		this.columnValues = new HashMap<String, ValueList>();
		this.numberOfColumns = columns.length;
		this.numberOfRows = values.size();

		for (int j = 0; j < columns.length; j++) {
			OutputColumn column = columns[j];
			String columnName = column.fieldName;
			if (this.columnIndexes.containsKey(columnName)) {
				String msg = "Error : grid " + this.name
						+ " has duplicate colum name " + columnName;
				Spit.Out(msg);
				if (dc != null) {
					dc.addError(msg);
				}
				columnName += j;
			}
			this.columnIndexes.put(columnName, new Integer(j));
			ValueList list = ValueList.newList(column.valueType,
					this.numberOfRows);
			this.columnValues.put(columnName, list);

			for (int i = 0; i < this.numberOfRows; i++) {
				list.setValue(values.get(i)[j], i);
			}
		}
	}

	/***
	 * append a row to the table. This operation is quite expensive, and is not
	 * recommended, because the underlying data is all re-constructed with new
	 * length
	 * 
	 * @param values
	 *            array that has the right-typed values for corresponding
	 *            columns
	 */
	public void appendValues(List<Value[]> values) {
		int n = values.size();
		if (n == 0) {
			return;
		}

		Value[] firstRow = values.get(0);
		int m = firstRow.length;
		if (m != this.getNumberOfColumns()) {
			Spit.out("Internal design error: invalid attempt to append rows to grid "
					+ this.name + " Number of columns mismatch.");
			return;
		}
		// for each column, create a new valueList from values, and use
		// .append() method
		for (String columnName : this.columnIndexes.keySet()) {
			int j = this.columnIndexes.get(columnName).intValue();
			ValueList list = this.columnValues.get(columnName);
			Value aValue = firstRow[j];
			ValueList newList = ValueList.newList(aValue.getValueType(), n);
			for (int i = 0; i < n; i++) {
				Value[] row = values.get(i);
				newList.setValue(row[j], i);
			}
			list.append(newList);
		}

	}

	/***
	 * @return number of rows in the grid
	 */
	public int getNumberOfRows() {
		return this.numberOfRows;
	}

	/***
	 * 
	 * @return number of columns in the grid
	 */
	public int getNumberOfColumns() {
		return this.numberOfColumns;
	}

	/***
	 * return the column
	 * 
	 * @param columnName
	 * @return null if no column exists with that name
	 */
	public ValueList getColumn(String columnName) {
		return this.columnValues.get(columnName);
	}

	/***
	 * return the zero based index of this column
	 * 
	 * @param columnName
	 * @return 0 based index of the column, or -1 if column does not exist
	 */
	public int getColumnIndex(String columnName) {
		Integer idx = this.columnIndexes.get(columnName);
		if (idx == null) {
			return -1;
		}
		return idx.intValue();
	}

	/***
	 * is there a column with this name?
	 * 
	 * @param columnName
	 * @return true if we have a column by this name
	 */
	public boolean hasColumn(String columnName) {
		return this.columnValues.containsKey(columnName);
	}

	/***
	 * get the value at the row in the column as a text value
	 * 
	 * @param columnName
	 * @param index
	 *            zero based row number
	 * @param defaultValue
	 *            if value is not found, this value is returned
	 * @return actual value, failing which default value
	 */
	public String getValueAsText(String columnName, int index,
			String defaultValue) {
		ValueList column = this.columnValues.get(columnName);
		if (column != null) {
			String val = column.getTextValue(index);

			if (val != null) {
				return val;
			}
		}
		return defaultValue;

	}

	/***
	 * Get the column as a text array
	 * 
	 * @param columnName
	 * @return null if no column exists with this name
	 */
	public String[] getColumnAsTextArray(String columnName) {
		ValueList column = this.columnValues.get(columnName);
		if (column != null) {
			return column.format();
		}
		return null;
	}

	/**
	 * get the value at the row in the column
	 * 
	 * @param columnName
	 * @param idx
	 *            zero based row number
	 * @return value. Null if column/row does not exist
	 */
	public Value getValue(String columnName, int idx) {
		ValueList column = this.columnValues.get(columnName);
		if (column == null) {
			return null;
		}
		return column.getValue(idx);
	}

	/***
	 * set value for the row in the column
	 * 
	 * @param columnName
	 * @param value
	 * @param idx
	 *            zero based row number
	 */
	public void setValue(String columnName, Value value, int idx) {
		ValueList column = this.columnValues.get(columnName);
		if (column == null) {
			return;
		}
		this.rawData = null;
		column.setValue(value, idx);
	}

	/***
	 * set value for the row in the column after parsing if required
	 * 
	 * @param columnName
	 * @param index
	 *            zero based row number
	 * @param textValue
	 */

	public void setTextValue(String columnName, int index, String textValue) {
		ValueList column = this.columnValues.get(columnName);
		if (column == null) {
			return;
		}
		this.rawData = null;
		column.setTextValue(textValue, index);
	}

	/***
	 * You should be sure that the column takes numeric value before calling
	 * this method
	 * 
	 * @param columnName
	 * @param index
	 *            zero based row number
	 * @param value
	 */
	public void setIntegralValue(String columnName, int index, long value) {
		ValueList column = this.columnValues.get(columnName);
		if (column == null) {
			return;
		}
		this.rawData = null;
		column.setIntegralValue(value, index);
	}

	/***
	 * you should be sure that the column takes boolean value while using this
	 * method
	 * 
	 * @param columnName
	 * @param index
	 *            zero based row number
	 * @param value
	 */
	public void setBooleanValue(String columnName, int index, boolean value) {
		ValueList column = this.columnValues.get(columnName);
		if (column == null) {
			return;
		}
		this.rawData = null;
		column.setBooleanValue(value, index);
	}

	/***
	 * you should be sure the column is of type date
	 * 
	 * @param columnName
	 * @param index
	 *            zero based row number
	 * @param value
	 */
	public void setDateValue(String columnName, int index, Date value) {
		ValueList column = this.columnValues.get(columnName);
		if (column == null) {
			return;
		}
		this.rawData = null;
		column.setDateValue(value, index);
	}

	/***
	 * you should be sure that the column is of numeric type
	 * 
	 * @param columnName
	 * @param index
	 *            zero based row number
	 * @param value
	 */
	public void setDecimalValue(String columnName, int index, double value) {
		ValueList column = this.columnValues.get(columnName);
		if (column == null) {
			return;
		}
		this.rawData = null;
		column.setDecimalValue(value, index);
	}

	/***
	 * get values as an array of text array, with first row as column names
	 * 
	 * @return raw data as array of array of text values, with first row being
	 *         column names
	 */
	public String[][] getRawData() {
		if (this.rawData == null) {
			this.createRawData();
		}
		return this.rawData;
	}

	/***
	 * get values as text rows, but only for the specified columns
	 * 
	 * @param columns
	 * @return raw data for selected columns
	 */
	public String[][] getRawData(Parameter[] columns) {
		if (this.rawData == null) {
			this.createRawData(columns);
		}
		String[][] data = this.rawData;
		this.rawData = null;
		return data;
	}

	/***
	 * return an array of text data, with first row as column names, but include
	 * only the specified columns
	 * 
	 * @param columnNames
	 * @return raw data for selected column names
	 */
	public String[][] getRawData(String[] columnNames) {
		if (columnNames == null || columnNames.length == 0) {
			return this.getRawData();
		}

		String[][] data = new String[this.numberOfRows + 1][columnNames.length];
		int colIdx = 0;
		for (String columnName : columnNames) {
			data[0][colIdx] = columnName;
			ValueList list = this.getColumn(columnName);
			if (list != null) {
				String[] vals = list.format();
				for (int j = 0; j < vals.length; j++) {
					data[j + 1][colIdx] = vals[j];
				}
			}
			colIdx++;
		}
		return data;
	}

	/***
	 * create text data
	 */
	private void createRawData() {

		String[][] data = new String[this.numberOfRows + 1][this.numberOfColumns];
		String[] headerRow = data[0];
		for (String nam : this.columnIndexes.keySet()) {

			int i = this.columnIndexes.get(nam).intValue();

			headerRow[i] = nam;

			ValueList list = this.getColumn(nam);
			if (list == null) {
				continue;
			}
			String[] vals = list.format();
			for (int j = 0; j < vals.length; j++) {
				data[j + 1][i] = vals[j];
			}
		}
		this.rawData = data;
	}

	/***
	 * create text data for a subset of columns
	 * 
	 * @param columns
	 */
	private void createRawData(Parameter[] columns) {
		String[][] data = new String[this.numberOfRows + 1][this.numberOfColumns];

		for (int i = 0; i < columns.length; i++) {
			Parameter column = columns[i];
			data[0][i] = column.name;
			ValueList list = this.getColumn(column.name);
			String[] vals = null;
			AbstractDataType dt = column.getDataType();
			vals = dt.format(list);
			for (int j = 0; j < vals.length; j++) {
				data[j + 1][i] = vals[j];
			}
		}
		this.rawData = data;
	}

	/***
	 * get column names as an array of text values
	 * 
	 * @return array of column names
	 */
	public String[] getColumnNames() {
		String[] columnNames = new String[this.numberOfColumns];
		for (Entry<String, Integer> entry : this.columnIndexes.entrySet()) {
			columnNames[entry.getValue().intValue()] = entry.getKey();
		}
		return columnNames;
	}

	/***
	 * rename a column. No action is taken if no column with that name exists
	 * 
	 * @param fromName
	 * @param toName
	 */
	public void renameColumn(String fromName, String toName) {
		if (this.columnIndexes.containsKey(fromName) == false) {
			return;
		}

		int colIdx = this.columnIndexes.get(fromName).intValue();
		this.columnIndexes.put(toName, new Integer(colIdx));
		this.columnIndexes.remove(fromName);
		this.columnValues.put(toName, this.columnValues.get(fromName));
		this.columnValues.remove(fromName);
		this.rawData = null;
	}

	/***
	 * filter rows based on a filter criterion for one column
	 * 
	 * @param columnName
	 *            column to be compared
	 * @param comparator
	 *            comparison
	 * @param value
	 *            value to be compared
	 * @throws ExilityException
	 *             in case of incompatible values for comparison
	 */
	public void filter(String columnName, Comparator comparator, String value)
			throws ExilityException {
		boolean[] selections = new boolean[this.numberOfRows];
		int nbrSelectedRows = this.getSelectionFlags(columnName, comparator,
				value, selections);
		if (nbrSelectedRows == this.numberOfRows) {
			return;
		}

		// filter all lists
		for (String colName : this.columnIndexes.keySet()) {
			this.columnValues.put(colName, this.columnValues.get(colName)
					.filter(nbrSelectedRows, selections));
		}

		this.rawData = null;
	}

	/***
	 * create a new grid by copying rows based on filter criterion
	 * 
	 * @param columnName
	 *            column to be compared
	 * @param comparator
	 *            comparison operator
	 * @param value
	 *            value to be compared
	 * @return new grid with filtered rows in that
	 * @throws ExilityException
	 *             in case of incompatible value type for comparison
	 */
	public Grid filterRows(String columnName, Comparator comparator,
			String value) throws ExilityException {
		boolean[] selections = new boolean[this.numberOfRows];
		int nbrSelectedRows = this.getSelectionFlags(columnName, comparator,
				value, selections);
		Grid newGrid = new Grid(this.name);

		for (String colName : this.columnIndexes.keySet()) {
			newGrid.addColumn(
					colName,
					this.columnValues.get(colName).filter(nbrSelectedRows,
							selections));
		}
		return newGrid;
	}

	/***
	 * get an boolean array that marks rows that are selectd as per selection
	 * criterion
	 * 
	 * @param columnName
	 *            column to be used for filtering
	 * @param comparator
	 *            operator for comparison
	 * @param value
	 *            value to be compared
	 * @param selections
	 *            array in which selections are to be returned. This is used as
	 *            an input to implement OR
	 * @return number of rows selected
	 * @throws ExilityException
	 *             in cae of incompatible types
	 */
	private int getSelectionFlags(String columnName, Comparator comparator,
			String value, boolean[] selections) throws ExilityException {
		ValueList list = this.columnValues.get(columnName);
		if (list == null) {
			String msg = columnName
					+ " is not a column Name. Filter will not work";
			Spit.out(msg);
			throw new ExilityException(msg);
		}
		if (value == null || value.length() == 0) // if value is not given,
													// assume that all rows are
													// selected
		{
			for (int i = 0; i < selections.length; i++) {
				selections[i] = true;
			}
			return selections.length;
		}
		int nbrRowsSelected = 0;
		Value val = Value.newValue(value, list.getValueType());
		for (int i = 0; i < selections.length; i++) {
			boolean selected = false;
			if (comparator == Comparator.EXISTS) {
				selected = true;
			} else {
				Value v = list.isNull(i) ? Value.newValue("") : list
						.getValue(i);
				selected = v.compare(comparator, val);
			}

			if (selected) {
				nbrRowsSelected++;
				selections[i] = true;
			}
		}
		return nbrRowsSelected;
	}

	/**
	 * copy values of a row into dc.values as name-value pairs. It removes the
	 * values from dc, if the index is not valid It is designed this way to suit
	 * the needs of ServiceStep to implement loop-step
	 * 
	 * @param index
	 *            zero based row number
	 * @param prefix
	 *            optional. field names are formed by prefixing this to column
	 *            name
	 * @param dc
	 *            to which fields are to be added
	 * @return true of the row existed and we copied row into fields. false if
	 *         it is a non-existent row, and we cleaned-up fields from dc.
	 */
	public boolean copyRowToDc(int index, String prefix, DataCollection dc) {
		String prefixToUse = prefix != null ? prefix : "";
		if (index >= this.numberOfRows) {
			for (String columnName : this.columnValues.keySet()) {
				dc.removeValue(prefixToUse + columnName);
			}
			return false;
		}
		for (String columnName : this.columnValues.keySet()) {
			ValueList column = this.columnValues.get(columnName);
			dc.addValue(prefixToUse + columnName, column.getValue(index));
		}
		return true;
	}

	/***
	 * format data into an html text
	 * 
	 * @return html fragment that renders this grid
	 */
	public String toHtml() {
		return HtmlUtil.rowsToTable(this.getRawData());
	}

	/***
	 * noName is the default name
	 * 
	 * @return name
	 */
	public String getName() {
		return this.name == null ? "" : this.name;
	}

	/***
	 * create xml text to represent this grid as an xl sheet
	 * 
	 * @param sbf
	 * @param sheetName
	 */
	public void toSpreadSheetXml(StringBuilder sbf, String sheetName) {
		sbf.append("\n<Worksheet ss:Name=\"").append(sheetName)
				.append("\">\n<Table>\n<Row>");

		String[][] data = this.getRawData();
		String[] header = data[0];
		String[] types = new String[this.numberOfColumns];

		// output header row. Also, collect data types
		boolean[] isDate = new boolean[this.numberOfColumns];
		for (int j = 0; j < this.numberOfColumns; j++) {
			String colName = header[j];
			DataValueType type = this.columnValues.get(colName).getValueType();
			isDate[j] = type == DataValueType.DATE;
			types[j] = Value.getXlType(type);

			sbf.append("\n<Cell><Data ss:Type=\"String\">").append(colName)
					.append("</Data></Cell>");
		}
		sbf.append("\n</Row>");

		// header row done. Let us output other rows
		for (int i = 1; i < data.length; i++) {
			String[] row = data[i];
			sbf.append("\n<Row>");
			for (int j = 0; j < row.length; j++) {
				sbf.append("\n<Cell");
				String value = row[j];
				if (isDate[j] && value.length() > 0) {
					sbf.append(" ss:StyleID=\"")
							.append(XlUtil.XL_DATE_STYLE_NAME).append('"');
					value = value.substring(0, 10) + 'T' + value.substring(11);
				} else if (types[j].equals("Boolean")) {
					value = value.toUpperCase().equals("TRUE")
							|| value.equals("1") ? "1" : "0";
				}
				sbf.append("><Data ss:Type=\"").append(types[j]).append("\">")
						.append(value).append("</Data></Cell>");
			}
			sbf.append("\n</Row>");
		}

		sbf.append("\n</Table>\n</Worksheet>");
	}

	/***
	 * compare this grid with supplied grid and put message into dc in case of
	 * differences
	 * 
	 * @param otherGrid
	 *            gird to compare with
	 * @param dc
	 *            optional. messages are added if dc is supplied
	 * @return true if all cells compare well, false otherwise. Mismatch message
	 *         are added to dc
	 */
	public boolean equal(Grid otherGrid, DataCollection dc) {
		boolean matched = true;

		int thisNbrRows = this.numberOfRows;
		int otherNbrRows = otherGrid.numberOfRows;
		if (thisNbrRows != otherNbrRows) {
			String msg = "Found " + thisNbrRows + " rows in the grid "
					+ this.name + " while " + otherNbrRows
					+ " rows are expected.";
			if (dc != null) {
				dc.addError(msg);
			} else {
				Spit.out(msg);
			}
			return false;
		}

		int thisNbrCols = this.numberOfColumns;
		int otherNbrCols = otherGrid.numberOfColumns;
		if (thisNbrCols < otherNbrCols) {
			String msg = "Found " + thisNbrCols + " columns in the grid "
					+ this.name + " while at least " + otherNbrCols
					+ " columns are expected.";
			if (dc != null) {
				dc.addError(msg);
			} else {
				Spit.out(msg);
			}
			return false;
		}

		// we are tolerant to mismatch of column orders, or extra columns.
		String[] otherColumnNames = otherGrid.getColumnNames();
		for (String colName : otherColumnNames) {
			if (!this.columnIndexes.containsKey(colName)) {
				String msg = "Column " + colName + " not found in in the grid "
						+ this.name;
				if (dc != null) {
					dc.addError(msg);
				} else {
					Spit.out(msg);
				}
				matched = false;
			}
		}

		if (!matched) {
			return false;
		}

		for (String colName : otherColumnNames) {
			ValueList thisColumn = this.columnValues.get(colName);
			ValueList otherColumn = otherGrid.columnValues.get(colName);
			if (!thisColumn.equal(otherColumn, dc, colName)) {
				matched = false;
			}
		}
		return matched;
	}

	/***
	 * remove the column
	 * 
	 * @param columnName
	 */
	public void removeColumn(String columnName) {
		if (this.numberOfRows == Grid.UNKNOWN_ROWS) {
			Spit.out("The Grid Does not have any data yet. Hence can't perform any operation");
			return;
		}

		if (this.columnIndexes.containsKey(columnName)) {

			this.columnValues.remove(columnName);
			this.columnIndexes.remove(columnName);
			// Refreshing the index
			int colIdx = 0;
			for (String nam : this.columnIndexes.keySet()) {
				this.columnIndexes.put(nam, new Integer(colIdx));
				colIdx++;
			}
			this.numberOfColumns--;

			if (this.numberOfColumns == 0) {
				// If there are no columns in grid, then make the grid rows
				// unknown
				this.numberOfRows = Grid.UNKNOWN_ROWS;
			}
		} else {
			Spit.out("The column name: " + columnName
					+ " does not exist. Hence no column deleted.");
			return;
		}

		this.resetRawData();

	}

	/***
	 * remove all data but retain column names. It becomes a grid with no rows
	 */
	public void purgeGrid() {
		if (this.numberOfRows == Grid.UNKNOWN_ROWS) {
			Spit.out("The Grid \"" + this.name
					+ "\" Does not have any data. Cannot purge anything.");

		}

		for (String nam : this.columnValues.keySet()) {
			this.columnValues.put(nam, null);
		}
		this.resetRawData();
		this.numberOfRows = 0;
	}

	/**
	 * return a list of values from a column after filtering rows based on
	 * another column
	 * 
	 * @param columnName
	 *            column from which to get values
	 * @param basedOnColumnName
	 *            column to be used for filtering
	 * @param filterValue
	 *            value to be used for filtering
	 * @return list of values
	 */
	public String[] getFilteredList(String columnName,
			String basedOnColumnName, String filterValue) {
		ValueList list = this.getColumn(columnName);
		if (list == null) {
			Spit.out("Column " + columnName + " not found in grid " + this.name);
			return null;
		}

		String[] vals = list.getTextList();
		if (basedOnColumnName == null) {
			return vals;
		}

		ValueList otherColumn = this.getColumn(basedOnColumnName);
		if (otherColumn == null) {
			Spit.out("based on column " + basedOnColumnName
					+ " not found in grid " + this.name
					+ ". values are not filtered.");
			return vals;
		}

		String[] valsToCompare = otherColumn.getTextList();
		int n = 0;
		boolean[] toInclude = new boolean[valsToCompare.length];
		for (int i = 0; i < valsToCompare.length; i++) {
			if (valsToCompare[i].equals(filterValue) == false) {
				n++;
				toInclude[i] = true;
			}
		}
		String[] newVals = new String[n];
		n = 0;
		for (int i = 0; i < toInclude.length; i++) {
			if (toInclude[i]) {
				newVals[n] = vals[i];
				n++;
			}
		}

		return newVals;
	}

	/**
	 * call iterator.iterate() after copying each row into dc
	 * 
	 * @param dc
	 * @param iterator
	 * @return number of rows over which we iterated
	 */
	public int iterate(DataCollection dc, GridIteratorInterface iterator) {

		if (this.numberOfRows == 0 || this.numberOfColumns == 0) {
			return 0;
		}
		/*
		 * save existing values that may clash with the columns
		 */
		String[] colNames = this.getColumnNames();
		Value[] savedValues = new Value[this.numberOfColumns];
		int colIdx = 0;
		for (String columnName : colNames) {
			savedValues[colIdx] = dc.removeValue(columnName);
			colIdx++;
		}

		/*
		 * iterate for each row now
		 */
		for (int i = 0; i < this.numberOfRows; i++) {
			for (String colName : colNames) {
				dc.addValue(colName, this.getValue(colName, i));
			}
			iterator.callBackForEachRow(dc);
		}

		colIdx = 0;
		for (String columnName : colNames) {
			Value value = savedValues[colIdx];
			if (value == null) {
				dc.removeValue(columnName);
			} else {
				dc.addValue(columnName, value);
			}
			colIdx++;
		}
		return this.numberOfRows;
	}

	/**
	 * extract column values from this row into a collection
	 * 
	 * @param fieldValues
	 *            collection of field values
	 * @param rowIndex
	 *            0 based row number.
	 * @return true if we extracted. false if your index was out of bound
	 */
	public boolean rowToFields(Map<String, Value> fieldValues, int rowIndex) {
		if (rowIndex < 0 || rowIndex >= this.getNumberOfRows()) {
			Spit.out("grid.toFieldValues called with idx = " + rowIndex
					+ " while there are only " + this.getNumberOfRows()
					+ " rows in grid " + this.getName());
			return false;
		}

		for (String columnName : this.getColumnNames()) {
			fieldValues.put(columnName, this.getValue(columnName, rowIndex));
		}
		return true;
	}

	/**
	 * update the row with values from the collection
	 * 
	 * @param fieldValues
	 *            collection of field values
	 * @param rowIndex
	 *            0 based row number.
	 * @return true if we updated. false if your index was out of bound
	 */
	public boolean updateRow(Map<String, Value> fieldValues, int rowIndex) {
		if (rowIndex < 0 || rowIndex >= this.getNumberOfRows()) {
			Spit.out("grid.updateRow called with idx = " + rowIndex
					+ " while there are only " + this.getNumberOfRows()
					+ " rows in grid " + this.getName());
			return false;
		}

		for (String columnName : this.getColumnNames()) {
			Value value = fieldValues.get(columnName);
			if (value != null) {
				this.setValue(columnName, value, rowIndex);
			}
		}
		return true;
	}
}

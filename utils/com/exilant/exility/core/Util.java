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
 * Utilities
 * 
 */
public class Util {
	/***
	 * Wrap an array of names into a data grid. Useful when you are supposed to
	 * respond with a grid of just one column, and you have that as an array.
	 * 
	 * @param names
	 *            array to be transformed into a column
	 * @param header
	 *            name of the column to be put in the header row
	 * @return data grid that has the supplied array as the sole column
	 */
	public static String[][] namesToGrid(String[] names, String header) {
		String[][] grid = new String[names.length + 1][];
		String[] hdr = { header };
		grid[0] = hdr;

		for (int i = 0; i < names.length; i++) {
			String[] row = { names[i] };
			grid[i + 1] = row;
		}
		return grid;
	}

	/***
	 * Sort the rows of a data grid on a column. columnName is looked-up in
	 * dictionary failing which a text is assumed. empty string is assumed to be
	 * smaller than any value for all data types
	 * 
	 * @param grid
	 *            data with a header row to be sorted
	 * @param columnToSort
	 *            name of the column to be sorted on
	 * @param sortDescending
	 *            whether to sort by descending order
	 */
	/**
	 * @param grid
	 * @param columnToSort
	 * @param sortDescending
	 */
	public static void sortGrid(String[][] grid, String columnToSort,
			boolean sortDescending) {
		int nbrRows = grid.length;
		if (nbrRows <= 1) {
			Spit.out("No data to sort on column " + columnToSort);
			return;
		}

		// find the index of the column to be sorted on
		String[] header = grid[0];
		int nbrCols = header.length;
		int colIdx = -1;
		for (int j = 0; j < nbrCols; j++) {
			if (header[j].equalsIgnoreCase(columnToSort)) {
				colIdx = j;
				break;
			}
		}

		if (colIdx == -1) {
			Spit.out("Design Error: "
					+ columnToSort
					+ " is not a column in grid that is supplied. Grid is Not Sorted.");
		}

		// what is the value type of this column?
		DataValueType valueType = DataDictionary.getValueType(columnToSort);

		// we ignore values[0], in line with rows in the grid
		Value[] values = new Value[nbrRows];
		for (int i = 1; i < nbrRows; i++) {
			String colValue = grid[i][colIdx];
			if (colValue == null || colValue.length() == 0) {
				values[i] = null;
			} else {
				values[i] = Value.newValue(colValue, valueType);
			}
		}
		// we use incremental sort algorithm. for simplicity, we always sort
		// ascending, and reverse the search for descending sort
		int startAt = 1;
		int endAt = nbrRows;
		int incr = 1;
		if (sortDescending) {
			startAt = nbrRows - 1;
			endAt = 0;
			incr = -1;
		}
		// following loop would be either
		// for(int i = 1; i != nbrRows; i++) or
		// for(int i = nbrRows -1; i != 1; i--)....
		for (int i = startAt; i != endAt; i = i + incr) {
			Value min = values[i]; // current min
			// null is guaranteed to be lowest!!
			if (min == null) {
				continue;
			}

			int idx = i; // min is found at this index.
			for (int j = i + incr; j != endAt; j = j + incr) {
				Value value = values[j];
				if (value == null) {
					min = value;
					idx = j;
					break;
				}
				if (value.lessThan(min).getBooleanValue()) {
					min = value;
					idx = j;
				}
			}
			if (idx != i) // we did find a min
			{
				// switch values
				values[idx] = values[i];
				values[i] = min;
				// switch corresponding rows in grid as well.
				// remember grid has header row. Index is logically 1 based.
				String[] row = grid[i];
				grid[i] = grid[idx];
				grid[idx] = row;
			}
		}
	}

	/***
	 * Get the index of the column in the grid
	 * 
	 * @param grid
	 *            data grid of text values with first row as header
	 * @param columnName
	 *            name of the column to look for
	 * @return zero based index of the column, or -1 if no column does not exist
	 */
	public static int getColumnIndex(String[][] grid, String columnName) {
		String[] header = grid[0];
		for (int j = 0; j < header.length; j++) {
			if (header[j].equalsIgnoreCase(columnName)) {
				return j;
			}
		}
		Spit.out("Design Error : " + columnName
				+ " is not column in the data grid.");
		return -1;
	}

	/***
	 * Get the row number that has the matching value in the given column
	 * 
	 * @param grid
	 *            data grid of text values with the first row as header
	 * @param columnName
	 *            name of the column to match
	 * @param value
	 *            value to match
	 * @return row number in the grid that matches, or -1 if no match found. row
	 *         number is 1 based.
	 */
	public static int getMatchingRow(String[][] grid, String columnName,
			String value) {
		int idx = getColumnIndex(grid, columnName);
		if (idx == -1) {
			return -1;
		}
		for (int i = 1; i < grid.length; i++) {
			if (grid[i][idx].equalsIgnoreCase(value)) {
				return i;
			}
		}
		return -1;
	}

	/***
	 * split text on delimiter and return the array that has trimmed values
	 * 
	 * @param text
	 *            to be split
	 * @param delimiter
	 *            typically a single character, like comma or colon
	 * @return array of text values
	 */
	public static String[] toTextArray(String text, String delimiter) {
		if (text == null || text.length() == 0) {
			return new String[0];
		}

		String[] arr = text.split(delimiter);
		for (int i = 0; i < arr.length; i++) {
			arr[i] = arr[i].trim();
		}
		return arr;
	}

	/***
	 * make a delimited text out of the array
	 * 
	 * @param values
	 *            text array
	 * @param delimiter
	 *            delimiter to be used
	 * @return string containing delimited values
	 */
	public static String arrayToText(String[] values, String delimiter) {
		StringBuilder sbf = new StringBuilder();
		for (String value : values) {
			sbf.append(value).append(delimiter);
		}
		sbf.deleteCharAt(sbf.length() - 1);
		return sbf.toString();
	}

	/***
	 * make a delimited text out of the array
	 * 
	 * @param values
	 *            text array
	 * @param delimiter
	 *            delimiter to be used
	 * @return string containing delimited values
	 */
	public static String arrayToText(long[] values, String delimiter) {
		StringBuilder sbf = new StringBuilder();
		for (long value : values) {
			sbf.append(value).append(delimiter);
		}
		sbf.deleteCharAt(sbf.length() - 1);
		return sbf.toString();
	}

	/***
	 * split a text into array of integers
	 * 
	 * @param text
	 *            to be split
	 * @param delimiter
	 *            to be used for splitting
	 * @return null if value can not be parsed properly
	 */
	public static long[] toIntegerArray(String text, String delimiter) {
		if (text == null || text.length() == 0) {
			return new long[0];
		}

		String[] textArray = text.split(delimiter);
		long[] arr = new long[textArray.length];
		for (int i = 0; i < arr.length; i++) {
			try {
				arr[i] = Long.parseLong(textArray[i]);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return arr;
	}
}

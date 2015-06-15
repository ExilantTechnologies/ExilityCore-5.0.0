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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/***
 * Xl related utilities. IMPORTANT: We assume that the xl is in MS-Exel format
 * and is saved as an xml. Convention followed: sheetName = values means it is
 * values. Others are grids, with first row as column Names
 * 
 * IMPORTANT: We look at only the data part of xl. Hence we assume the xml to be
 * of the form <Workbook> <Worksheet ss:name="sheetName">
 * <Table>
 * <Row> <Cell><Data ss:Type="String|Number|DateTime|Boolean">cell
 * value</Data></Cell> ...... </Row> ......
 * </Table>
 * </Worksheet> ........ </Workbook>
 */
public class XlUtil {
	// xl data types. These are actually used in Value class
	/**
	 * xls date time
	 */
	public static final String DATE_TYPE = "DateTime";

	/**
	 * xls text/string
	 */
	public static final String TEXT_TYPE = "String";

	/**
	 * xls number, integral as wella s decimal
	 */
	public static final String NUMBER_TYPE = "Number";

	/**
	 * boolean
	 */
	public static final String BOOLEAN_TYPE = "Boolean";

	/**
	 * default formatting of date fields in XL
	 */
	public static final String XL_DATE_FORMAT = "Medium Date";

	/**
	 * default style for date fields
	 */
	public static final String XL_DATE_STYLE_NAME = "dateStyle";
	/**
	 * default tag names for MS XLs.
	 */
	public static final String XL_BEGIN = "<?xml version=\"1.0\"?>"
			+ "\n<?mso-application progid=\"Excel.Sheet\"?>"
			+ "\n<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">"
			+ "\n<Styles><Style ss:ID=\"" + XlUtil.XL_DATE_STYLE_NAME
			+ "\"><NumberFormat ss:Format=\"" + XlUtil.XL_DATE_FORMAT
			+ "\"/></Style></Styles>";

	/**
	 * end for xls
	 */
	public static final String XL_END = "\n</Workbook>";
	private static final String WORK_SHEET_TAG_NAME = "Worksheet";
	private static final String QUALIFIED_WORK_SHEET_TAG_NAME = "ss:Worksheet";
	private static final String TABLE_TAG_NAME = "Table";
	private static final String ROW_TAG_NAME = "Row";
	private static final String CELL_TAG_NAME = "Cell";
	private static final String DATA_TAG_NAME = "Data";
	private static final String NAME_NAME = "ss:Name";
	private static final String TYPE_NAME = "ss:Type";
	private static final String INDEX_NAME = "ss:Index";

	/***
	 * constructor
	 */
	public XlUtil() {

	}

	/***
	 * Read contents of an xl into dc.
	 * 
	 * @param fullyQualifiedFileName
	 *            file name
	 * @param dc
	 *            dc to which data is to be read. Value/grid could be overridden
	 *            if they already exist in the dc
	 * @param useDictionaryForDataType
	 *            if true, data dictionary is used to get the value type of a
	 *            field/column
	 * @return 0 if xl could not be read. dc will have an error message. > 0
	 *         means successfully read
	 */
	public int extract(String fullyQualifiedFileName, DataCollection dc,
			boolean useDictionaryForDataType) {
		NodeList sheets = this.getSheets(fullyQualifiedFileName, dc);
		if (sheets == null) {
			return 0;
		}

		int n = sheets.getLength();
		Spit.out("There are " + n + " sheets in " + fullyQualifiedFileName);
		for (int i = 0; i < n; i++) {
			Element sheet = (Element) sheets.item(i);
			String nam = sheet.getAttribute(XlUtil.NAME_NAME);
			NodeList tables = sheet.getElementsByTagName(XlUtil.TABLE_TAG_NAME);
			int m = tables.getLength();
			if (m == 0) {
				Spit.out(nam + " has no data in it. Skipping this sheet.");
				continue;
			}
			Element table = (Element) tables.item(0);
			if (nam.equals(CommonFieldNames.VALUES_TABLE_NAME)) {
				this.extractValues(table, dc, useDictionaryForDataType);
				continue;
			}

			String[][] rawData = this.getRawData(table);
			if (rawData == null || rawData.length == 0) {
				dc.addError("Work sheet " + nam
						+ " could not be parsed in spread sheet "
						+ fullyQualifiedFileName);
				continue;
			}

			Grid grid = null;
			if (useDictionaryForDataType) {
				grid = new Grid();
				try {
					grid.setRawData(rawData);
				} catch (ExilityException e) {
					grid = null;
				}
			} else {
				grid = this.getGrid(table);
			}
			if (grid == null) {
				dc.addError("Work sheet " + nam
						+ " could not be parsed in spread sheet "
						+ fullyQualifiedFileName + ". error: ");
				continue;
			}
			dc.addGrid(nam, grid);
		}
		return 1;
	}

	/***
	 * extract data from spread sheet xml as a collection of array of arrays
	 * 
	 * @param fullyQualifiedFileName
	 *            file name
	 * @return collection of data as named array of arrays of text data
	 */
	public Map<String, String[][]> extractAsText(String fullyQualifiedFileName) {
		Map<String, String[][]> allData = new HashMap<String, String[][]>();
		NodeList sheets = this.getSheets(fullyQualifiedFileName, null);
		if (sheets == null) {
			return allData;
		}

		int n = sheets.getLength();
		Spit.out("There are " + n + " sheets in " + fullyQualifiedFileName);
		for (int i = 0; i < n; i++) {
			Element sheet = (Element) sheets.item(i);
			String nam = sheet.getAttribute(XlUtil.NAME_NAME);
			NodeList tables = sheet.getElementsByTagName(XlUtil.TABLE_TAG_NAME);
			int m = tables.getLength();
			if (m == 0) {
				Spit.out(nam + " has no data in it. Skipping this sheet.");
				continue;
			}
			String[][] rawData = this.getRawData((Element) tables.item(0));
			if (rawData == null || rawData.length == 0) {
				Spit.out("Work sheet " + nam
						+ " could not be parsed in spread sheet "
						+ fullyQualifiedFileName);
				continue;
			}
			allData.put(nam, rawData);
		}
		return allData;
	}

	/***
	 * save contents of dc into this file
	 * 
	 * @param fullyQualifiedFileName
	 *            file name
	 * @param dc
	 *            Data Collection
	 * @return 0 if there is any error. dc will have the error message. > 0 if
	 *         all is well.
	 */
	public int saveDc(String fullyQualifiedFileName, DataCollection dc) {
		File file = new File(fullyQualifiedFileName);
		if (file.exists()) {
			if (!ResourceManager.renameAsBackup(file)) {
				file.delete();
			}
		}
		StringBuilder sbf = new StringBuilder();
		this.toSpreadSheetXml(sbf, dc);
		ResourceManager.saveText(fullyQualifiedFileName, sbf.toString());
		return 1;
	}

	/***
	 * Parse sheet and get a regular table of cells. That is, all rows having
	 * same number of cells
	 * 
	 * @param Sheet
	 *            Data Sheet of an xl
	 * @return array of arrays of cell elements, with nulls
	 */
	private String[][] getRawData(Element sheet) {
		NodeList rowNodes = sheet.getElementsByTagName(XlUtil.ROW_TAG_NAME);
		int nbrRows = rowNodes.getLength();
		if (nbrRows == 0) {
			return null;
		}

		String[][] rawData = new String[nbrRows][];
		String[] row = this.textValuesOf((Element) rowNodes.item(0), 0);
		if (row == null) {
			return null;
		}

		int nbrCols = row.length;

		rawData[0] = row;
		for (int i = 1; i < nbrRows; i++) {
			rawData[i] = row = this.textValuesOf((Element) rowNodes.item(i),
					nbrCols);
			if (row == null) {
				return null;
			}
		}
		return rawData;
	}

	/***
	 * get an array of text values of cells in a row, substituting empty strings
	 * for any missing cells
	 * 
	 * @param row
	 *            row element
	 * @param expectedNbrCells
	 *            0 means that the cells are expected to be continuous (header
	 *            row), and hence return all of them. if non-null (data rows)
	 *            use this as the number of cells to be parsed.
	 * @return array of text values of cells, ignoring type attributes of cells
	 */
	private String[] textValuesOf(Element row, int expectedNbrCells) {
		if (row == null) {
			return null;
		}

		Element[] dataCells = this.getDataCells(row, expectedNbrCells);
		if (dataCells == null) {
			return null;
		}

		int nbrCols = dataCells.length;
		String[] values = new String[nbrCols];
		for (int i = 0; i < nbrCols; i++) {
			Element cell = dataCells[i];
			values[i] = (cell == null) ? "" : this.textValueOf(cell);
		}
		return values;
	}

	/***
	 * get an array of cell elements, substituting any missed cells in the row
	 * with nulls
	 * 
	 * @param row
	 *            row element to inspect for cells
	 * @param expectedNbrCells
	 *            0 means cells are expected to be non-empty. non-zero means
	 *            return these many cells.
	 * @return an array of cell elements
	 */
	private Element[] getDataCells(Element row, int expectedNbrCells) {
		if (row == null) {
			return null;
		}

		NodeList cells = row.getElementsByTagName(XlUtil.CELL_TAG_NAME);
		int nbrCells = cells.getLength();
		if (nbrCells == 0) {
			return null;
		}

		int nbrCols = expectedNbrCells == 0 ? nbrCells : expectedNbrCells;
		Element[] dataCells = new Element[nbrCols];
		int colIdx = 0;
		for (int i = 0; i < nbrCells; i++) {
			Element cell = (Element) cells.item(i);
			String text = cell.getAttribute(XlUtil.INDEX_NAME);
			if (text != null && text.length() > 0) {
				int idx = Integer.parseInt(text); // let us not suspect that the
													// xml has invalid data
				idx--; // index is 1 based, while we are following 0 based
				if (expectedNbrCells == 0 && idx != i) {
					Spit.out("header row has blank cells in the spred sheet. It will not be read properly");
					return null;
				}
				// fill missing columns with empty strings
				while (colIdx < idx && colIdx < nbrCols) {
					dataCells[colIdx] = null;
					colIdx++;
				}
				if (idx >= nbrCols) {
					Spit.out("Data found beyound column nbr " + nbrCols
							+ ". It is ignored.");
					break;
				}
			}
			dataCells[colIdx] = cell;
			colIdx++;
		}
		return dataCells;
	}

	/***
	 * return cell data as text
	 * 
	 * @param cell
	 *            row cell of a spread sheet
	 * @return text value. empty string if cell has no data element
	 */
	private String textValueOf(Element cell) {
		if (cell == null) {
			return "";
		}

		NodeList datas = cell.getElementsByTagName(XlUtil.DATA_TAG_NAME);
		if (datas.getLength() == 0) {
			return "";
		}
		return datas.item(0).getTextContent();
	}

	private Grid getGrid(Element sheet) {
		NodeList rowNodes = sheet.getElementsByTagName(XlUtil.ROW_TAG_NAME);
		int nbrRows = rowNodes.getLength();

		if (nbrRows <= 1) {
			return null;
		}

		String[] header = this.textValuesOf((Element) rowNodes.item(0), 0);
		if (header == null) {
			return null;
		}

		int nbrCols = header.length;
		int nbrDataRows = nbrRows - 1;

		// spread sheet may have empty cells, and hence each row may not contain
		// nbrCol number of cells
		// Let us get them to a regular 2 dim array, with nulls in them if
		// required
		Element[][] dataCells = new Element[nbrDataRows][];
		for (int i = 0; i < nbrDataRows; i++) {
			dataCells[i] = this.getDataCells((Element) rowNodes.item(i + 1),
					nbrCols);
			if (dataCells[i] == null) {
				return null;
			}
		}

		// let us get the data type of each of the columns, by inspecting down
		// the row till we get a cell with data in it
		DataValueType[] types = new DataValueType[nbrCols];
		for (int j = 0; j < nbrCols; j++) {
			DataValueType dt = DataValueType.NULL;
			for (int i = 0; i < nbrDataRows; i++) {
				Element dataCell = dataCells[i][j];
				if (dataCell == null) {
					continue;
				}

				DataValueType d = this.dataTypeOf(dataCell);
				if (d == DataValueType.NULL) {
					continue;
				}

				dt = d;
				break;
			}
			if (dt == DataValueType.NULL) {
				Spit.out("Column "
						+ header[j]
						+ " has no data in it, and hnece assuming it to be text.");
				dt = DataValueType.TEXT;
			}
			types[j] = dt;
		}

		// let us now add column after column to the grid
		Grid grid = new Grid();
		for (int j = 0; j < nbrCols; j++) {
			String[] values = new String[nbrDataRows];
			DataValueType type = types[j];
			for (int i = 0; i < nbrDataRows; i++) {
				String value = this.textValueOf(dataCells[i][j]);
				if (value.length() > 0 && type == DataValueType.DATE) {
					value = value.substring(0, 10) + ' ' + value.substring(11);
					Spit.out(value);
				}
				values[i] = value;
			}
			ValueList column = ValueList.newList(values, types[j]);
			try {
				grid.addColumn(header[j], column);
			} catch (ExilityException e) {
				// we know that the only reason for this exception is nbr rows
				// mismatch. But let us play it safe
				Spit.out("Error while adding column " + header[j] + " - "
						+ e.getMessage());
			}
		}

		return grid;
	}

	/***
	 * get data type of a Cell based on the xml attribute
	 * 
	 * @param cell
	 *            xml cell
	 * @return if cell is null or has no value DataValueType.NULL (not java
	 *         null). Decimal type is returned for all number type
	 */
	private DataValueType dataTypeOf(Element cell) {
		if (cell == null) {
			return DataValueType.NULL;
		}

		NodeList datas = cell.getElementsByTagName(XlUtil.DATA_TAG_NAME);
		if (datas.getLength() == 0) {
			return DataValueType.NULL;
		}

		Element dataElement = (Element) datas.item(0);
		String type = dataElement.getAttribute(XlUtil.TYPE_NAME);
		return Value.getTypeFromXl(type);
	}

	/***
	 * returns a node-list of all sheets from a spread sheet file saved in xml
	 * form
	 * 
	 * @param fileName
	 *            absolute path name
	 * @param dc
	 *            optional. error message is added to this
	 * @return NodeList of sheets, or null in case of any error
	 */
	private NodeList getSheets(String fileName, DataCollection dc) {
		File file = new File(fileName);
		String msg = null;
		if (!file.exists() || !file.isFile()) {
			msg = " is not a valid file name.";
		} else {
			Document doc = ObjectManager.getDocument(file);
			if (doc == null) {
				msg = " has an invalid xml.";
			} else {
				NodeList sheets = doc
						.getElementsByTagName(XlUtil.WORK_SHEET_TAG_NAME);
				if (sheets.getLength() == 0) {
					sheets = doc
							.getElementsByTagName(XlUtil.QUALIFIED_WORK_SHEET_TAG_NAME);
				}
				if (sheets.getLength() == 0) {
					msg = " does not have any worksheets in it.";
				} else {
					return sheets;
				}
			}
		}
		msg = fileName + msg;
		if (dc != null) {
			dc.addError(msg);
		}
		Spit.out(msg);
		return null;
	}

	/***
	 * Get xml text for the dc
	 * 
	 * @param sbf
	 *            to put the text into
	 * @param dc
	 *            data collection to be output
	 */
	private void toSpreadSheetXml(StringBuilder sbf, DataCollection dc) {
		sbf.append(XlUtil.XL_BEGIN);
		dc.toSpreadSheetXml(sbf, null);
		sbf.append(XlUtil.XL_END);
	}

	/***
	 * extract fields from spread sheet into dc
	 * 
	 * @param table
	 *            table element of spread sheet
	 * @param dc
	 *            dc
	 * @param useDictionaryForDataType
	 *            refer to data dictionary or use DataType as present in spread
	 *            sheet
	 */
	private void extractValues(Element table, DataCollection dc,
			boolean useDictionaryForDataType) {
		NodeList rows = table.getElementsByTagName(XlUtil.ROW_TAG_NAME);
		int n = rows.getLength();

		// if there are no values, following for loop will not execute..
		for (int i = 1; i < n; i++) // first row is header
		{
			NodeList cells = rows.item(i).getChildNodes();
			int nbrCells = cells.getLength();
			if (nbrCells == 0) {
				continue;
			}

			String fieldName = this.textValueOf((Element) cells.item(0));
			if (fieldName.length() == 0) {
				continue; // no name
			}

			String fieldValue;
			Element dataCell = null;
			if (nbrCells == 1) {
				fieldValue = "";
			} else {
				dataCell = (Element) cells.item(1);
				fieldValue = this.textValueOf(dataCell);
			}

			if (useDictionaryForDataType) {
				dc.addValueAfterCheckingInDictionary(fieldName, fieldValue);
			} else {
				DataValueType type = this.dataTypeOf(dataCell);
				dc.addValue(fieldName, fieldValue, type);
			}
		}
	}
}

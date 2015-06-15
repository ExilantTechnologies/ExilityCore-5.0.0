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
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.NumberToTextConverter;

/**
 * The purpose of this class is to read an excel workbook(Microsoft) and put
 * into Dc. Each sheet is a grid into dc. This utility uses apache POI library
 * to identify and read the excel workbook.
 * 
 * @Note In excel sheet first data row must be a column names.
 * @Note If an element defined in data dictionary with type TEXT/STRING then in
 *       excel this column must have String type across. In other words it is
 *       type safe.
 * @Note If an element not defined in dictionary then excel column type will be
 *       assigned which is the type of first data cell in excel.
 * @reqires following jar dependencies: 1. poi-3.9/poi-3.9/poi-3.9-20121203.jar
 *          2. poi-3.9/poi-3.9/poi-ooxml-3.9-20121203.jar 3.
 *          poi-3.9/ooxml-lib/xmlbeans-2.3.0.jar 4.
 *          poi-3.9/poi-ooxml-schemas-3.9-20121203.jar from Apache POI
 * @author Anant.
 */

public class XLSReader {
	private HashMap<Integer, XLSReader.ColumnMetaData> columnsData = null;
	private List<Value[]> rows = null;

	/**
	 * type could not be inferred
	 */
	public static final int UNKNOWN_TYPE = -1;

	/**
	 * column could not be found
	 */
	public static final int UNKNOWN_COLUMN_IDX = -1;

	/**
	 * unknown column name
	 */
	public static final String UNKNOWN_COLUMN_NAME = "UNKNOWN";
	private static final String AT_SYMBOL = "@";

	/*
	 * Messages for various situations in the method.
	 */

	private static final String INVALID_COLUMN_TYPE = " has invailid excel data type in row ";
	private static final String SKIP_BLANK_ROW = "Skiping blank row---->";
	private static final String INVALID_HEADER = "\n Sheet must have first non-empty row as valid column header.No blank column allow between first and last column in first row.";
	private static final String INSUFFICIENT_DATA_ROWS = " has insufficient data rows to be read.It must have more than 1 data rows.";
	private static final String ILLEGAL_ARGUMENT = "Supplied Parameter(s) canot be null";
	private static final String EXCEPTION_MSG = XLSReader.AT_SYMBOL + "1"
			+ " colud not read because of an exception\n" + XLSReader.AT_SYMBOL
			+ "2" + ". \n Please check in log for full stackTrace.";
	private static final String DATATYPE_MISMATCH = "Trying to set "
			+ XLSReader.AT_SYMBOL + "1" + " value and expected "
			+ XLSReader.AT_SYMBOL + "2" + " value for column '"
			+ XLSReader.AT_SYMBOL + "3" + "' in row " + XLSReader.AT_SYMBOL
			+ "4";
	private static final String DATA_ELEMENT_NOT_IN_DICTIONARY = " not defined in data dictionary. Excel column type will be assigned";

	/**
	 * 
	 */
	public XLSReader() {
		this.columnsData = new HashMap<Integer, XLSReader.ColumnMetaData>();
		this.rows = new ArrayList<Value[]>();
	}

	/**
	 * Purpose of this method to read an Microsoft Workbook in DataCollection dc
	 * supplied along with workbook. Each sheet will be a grid in dc with the
	 * same name as sheet name.
	 * 
	 * @param wb
	 *            This is an instance of MS excel workbook(i.e .xls or .xlsx)
	 *            created by POI WorkbookFactory.
	 * @param dc
	 */
	public void readAWorkbook(Workbook wb, DataCollection dc) {
		if (wb == null || dc == null) {
			throw new IllegalArgumentException(XLSReader.ILLEGAL_ARGUMENT);
		}

		int nbrSheets = wb.getNumberOfSheets();
		String sheetName = null;
		String gridName = dc.getTextValue("gridName", null);
		Sheet sheet = null;

		int nbrColumns = -1;
		int nbrPhysicalRows = 0;

		for (int k = 0; k < nbrSheets; k++) {

			sheet = wb.getSheetAt(k);
			sheetName = sheet.getSheetName();
			nbrPhysicalRows = sheet.getPhysicalNumberOfRows();
			if (nbrPhysicalRows < 2) {
				Spit.out(sheetName + XLSReader.INSUFFICIENT_DATA_ROWS);
				// dc.addMessage(XLSReader.INSUFFICIENT_ROWS, sheetName +
				// XLSReader.INSUFFICIENT_DATA_ROWS);
				continue;
			}

			try {
				nbrColumns = this.readASheet(sheet);
				/**
				 * swallow all the exceptions during excel sheet reading and put
				 * appropriate message. While reading excel following exceptions
				 * can come: 1. IllegalStateExcetion if column data type
				 * mismatch in excel sheet. 2. ExilityException etc.
				 */
			} catch (ExilityException e) {
				String msg = this.replaceMessageParams(XLSReader.EXCEPTION_MSG,
						new String[] { sheetName, e.getMessage() });
				dc.addError(msg);
				Spit.out(e);
			}

			if (nbrColumns == -1) {
				continue;
			}

			/**
			 * This is for little more flexibility to user if they have only one
			 * sheet to be read and has supplied a gridName along with service
			 * then let set first sheet one as given gridName(In case of simple
			 * file upload and read content as grid)
			 */
			if (gridName != null) {
				sheetName = gridName;
				gridName = null;
			}

			dc.addGrid(sheetName, this.getGrid());
			Spit.out(sheetName + " added to dc with " + this.rows.size()
					+ " row(s)");
			this.columnsData.clear();
			this.rows.clear();

			// this.printXlSRec(dc.getGrid(sheetName).getRawData());

		}
	}

	/**
	 * Purpose of this method to read rows from given Excel Sheet.
	 * 
	 * @param sheet
	 *            an Instance of .ss.usermodel.Sheet class from POI apache.
	 * @return -1 if fail to read sheet else number of columns read successfully
	 *         from the sheet.
	 * @throws ExilityException
	 */

	public int readASheet(Sheet sheet) throws ExilityException {
		int nonEmptyFirstRowIdx = 0;
		int lastRowIdx = 0;

		int nbrPhysicalRows = sheet.getPhysicalNumberOfRows();
		String sheetName = sheet.getSheetName();

		if (nbrPhysicalRows < 2) {
			Spit.out(sheetName + XLSReader.INSUFFICIENT_DATA_ROWS);
			return -1;
		}

		try {
			nonEmptyFirstRowIdx = sheet.getFirstRowNum();
			lastRowIdx = sheet.getLastRowNum();

			/*
			 * For checking to valid header.First row must be header.
			 */

			Row headerRow = sheet.getRow(nonEmptyFirstRowIdx);
			int nbrCol = headerRow.getPhysicalNumberOfCells();

			for (int colIdx = 0; colIdx < nbrCol; colIdx++) {
				Cell hCell = headerRow.getCell(colIdx);

				if (hCell == null
						|| hCell.getCellType() == Cell.CELL_TYPE_BLANK) {
					Spit.out("Error--->Found blank column " + (colIdx + 1)
							+ " in Sheet " + sheetName
							+ XLSReader.INVALID_HEADER);
					this.columnsData.clear();
					return -1;
				}

				String columnName = hCell.getStringCellValue();
				this.setDataType(columnName, colIdx);
			}

		} catch (Exception e) {
			Spit.out(sheetName + XLSReader.INVALID_HEADER);
			Spit.out(e);
			return -1;
		}

		int nbrColumnsInARow = this.columnsData.size();

		/*
		 * Loop starts with second data row that is first row(header as column
		 * name) excluded.
		 */
		Spit.out(sheetName + ":\n");
		for (int rowIdx = (nonEmptyFirstRowIdx + 1); rowIdx <= lastRowIdx; rowIdx++) {
			Row row = sheet.getRow(rowIdx);
			if (row == null) {
				Spit.out(XLSReader.SKIP_BLANK_ROW + rowIdx);
				continue;
			}
			/**
			 * readARow() will throws ExilityException if something goes wrong.
			 */
			this.readARow(row, nbrColumnsInARow);
		}

		return this.columnsData.size();

	}

	/**
	 * purpose of this method to create ValueList along with types and column
	 * name. Simple design followed : Just have ColumnMetaData object which
	 * contains everything. For a Cell we will have one columnMetaData object
	 * and it will have values across the
	 * 
	 * @param row
	 * @throws Exception
	 */
	private void readARow(Row row, int nbrColumnsInARow)
			throws ExilityException {
		Value[] columnValues = new Value[nbrColumnsInARow];
		Value aColumnValue = null;
		String rawValue = null;

		for (int c = 0; c < nbrColumnsInARow; c++) {
			Cell cell = row.getCell(c, Row.CREATE_NULL_AS_BLANK);

			ColumnMetaData columnInfo = this.columnsData.get(new Integer(c));
			int xlsColumnDataType = columnInfo.getXlsDataType();
			DataValueType exilDataType = null;

			int cellType = cell.getCellType();
			if (xlsColumnDataType != XLSReader.UNKNOWN_TYPE) {
				cellType = xlsColumnDataType;
			}

			try {

				switch (cellType) {
				case Cell.CELL_TYPE_NUMERIC:

					if (DateUtil.isCellDateFormatted(cell)) {
						rawValue = DateUtility.formatDate(cell
								.getDateCellValue());
						/*
						 * returns yyyy-mm-dd hh:mm:ss.sss full date with time.
						 */

						exilDataType = DataValueType.DATE;
					} else {
						double decimalNumber = cell.getNumericCellValue();
						rawValue = NumberToTextConverter.toText(decimalNumber);

						boolean isDecimal = rawValue.contains(".");
						if (isDecimal) {
							exilDataType = DataValueType.DECIMAL;
						} else {
							exilDataType = DataValueType.INTEGRAL;
						}
					}

					break;

				case Cell.CELL_TYPE_STRING:

					rawValue = cell.getStringCellValue().trim();
					exilDataType = DataValueType.TEXT;
					break;

				case Cell.CELL_TYPE_FORMULA:

					rawValue = cell.getStringCellValue().trim();
					exilDataType = DataValueType.TEXT;
					break;

				case Cell.CELL_TYPE_BLANK:

					rawValue = cell.getStringCellValue();
					exilDataType = DataValueType.NULL;
					columnInfo.setExilDataType(exilDataType);
					break;

				case Cell.CELL_TYPE_BOOLEAN:

					rawValue = cell.getBooleanCellValue() ? BooleanValue.TRUE
							: BooleanValue.FALSE;
					exilDataType = DataValueType.BOOLEAN;
					break;
				default:
					String msg = columnInfo.getColumnName()
							+ XLSReader.INVALID_COLUMN_TYPE + row.getRowNum();
					Spit.out(msg);

				}

			} catch (Exception e) {
				// Trying to set valueType value and expected valueType value
				// for column in row
				String[] params = { this.getXlsTypeAsText(cell.getCellType()),
						this.getXlsTypeAsText(cellType),
						columnInfo.getColumnName(), "" + row.getRowNum() };

				String message = this.replaceMessageParams(
						XLSReader.DATATYPE_MISMATCH, params);
				throw new ExilityException(message);
			}

			if (xlsColumnDataType == XLSReader.UNKNOWN_TYPE
					&& cellType != Cell.CELL_TYPE_BLANK) {
				columnInfo.setXlsDataType(cellType);
				columnInfo.setExilDataType(exilDataType);
			}

			exilDataType = columnInfo.getExilDataType();

			aColumnValue = Value.newValue(rawValue, exilDataType);

			columnValues[c] = aColumnValue;
			this.columnsData.put(new Integer(c), columnInfo);
		}

		this.rows.add(columnValues);
	}

	/**
	 * Purpose of this method to identify excel column data type corresponding
	 * of Exility DataDictionary element type. If element defined into data
	 * dictionary then there corresponding excel POI cell data type will be set
	 * else UNKNOWN_TYPE will be set for xls column data type and it will be
	 * checked and assigned for first data cell.(that is as the type of first
	 * data cell from excel sheet).
	 * 
	 * @param columnName
	 *            String column name from the excel sheet.
	 * @param colIdx
	 *            Column index of column in excel sheet.
	 */
	private void setDataType(String columnName, int colIdx) {

		DataElement de = DataDictionary.getElement(columnName);
		AbstractDataType dt = null;
		if (de != null) {
			dt = DataTypes.getInstance().dataTypes.get(de.dataType);
		}

		ColumnMetaData colInfo = new ColumnMetaData();

		if (de == null || dt == null) {
			Spit.out(columnName + XLSReader.DATA_ELEMENT_NOT_IN_DICTIONARY);
			colInfo.setColumnIndex(colIdx);
			colInfo.setColumnName(columnName);
			this.columnsData.put(new Integer(colIdx), colInfo);
			return;
		}

		DataValueType valueType = dt.getValueType();
		Spit.out(columnName + " has data type " + valueType
				+ " in data dictionary");
		int xlDataType = XLSReader.UNKNOWN_TYPE;

		switch (valueType) {
		case INTEGRAL:
		case DECIMAL:
		case DATE:
		case TIMESTAMP:
			xlDataType = Cell.CELL_TYPE_NUMERIC;
			break;

		case BOOLEAN:
			xlDataType = Cell.CELL_TYPE_BOOLEAN;
			break;

		case TEXT:
			xlDataType = Cell.CELL_TYPE_STRING;
			break;
		case NULL:
			xlDataType = Cell.CELL_TYPE_STRING;
			break;
		default:
			break;
		}

		colInfo.setColumnIndex(colIdx);
		colInfo.setColumnName(columnName);
		colInfo.setExilDataType(valueType);
		colInfo.setXlsDataType(xlDataType);

		this.columnsData.put(new Integer(colIdx), colInfo);

	}

	/**
	 * 
	 * @return grid that has the data from this XLS
	 */
	public Grid getGrid() {
		int nbrColumns = this.columnsData.size();
		Grid aGrid = new Grid();

		OutputColumn[] columns = new OutputColumn[nbrColumns];

		for (int colIdx = 0; colIdx < nbrColumns; colIdx++) {
			ColumnMetaData colInfo = this.columnsData.get(new Integer(colIdx));
			String fieldName = colInfo.getColumnName();
			columns[colIdx] = new OutputColumn(fieldName,
					colInfo.getExilDataType(), fieldName);
		}
		aGrid.setValues(columns, this.rows, null);
		return aGrid;

	}

	/**
	 * This can be removed because we can save type as text in MetaData class
	 * itself for each column Remove ? Next Iteration.
	 * 
	 * @param cellType
	 * @return String for xls data type as String that is enumeration value as
	 *         text.
	 */
	public String getXlsTypeAsText(int cellType) {
		String rawType = null;
		switch (cellType) {
		case Cell.CELL_TYPE_NUMERIC:

			rawType = "NUMERIC";
			break;

		case Cell.CELL_TYPE_STRING:

		case Cell.CELL_TYPE_FORMULA:

			rawType = "STRING";
			break;

		case Cell.CELL_TYPE_BOOLEAN:
			rawType = "BOOLEAN";
			break;

		default:
			rawType = XLSReader.UNKNOWN_COLUMN_NAME;

		}

		return rawType;
	}

	/**
	 * purpose of this method to replace all the @ with given parameters in the
	 * message
	 * 
	 * @param message
	 * @param params
	 * @return message with its parameters replaced with appropriate values
	 */
	public String replaceMessageParams(String message, String[] params) {
		int nbrParams = params.length;
		StringBuilder msg = new StringBuilder(message);
		int startAt = 0;
		for (int i = 0; i < nbrParams; i++) {
			startAt = msg.indexOf(XLSReader.AT_SYMBOL + (i + 1));
			msg = msg.replace(startAt, (startAt + 2), params[i]);
		}
		return msg.toString();
	}

	/**
	 * This is for testing purpose only
	 * 
	 * @param testRows
	 */
	public void printXlSRec(String[][] testRows)// for testing purpose
	{
		if (testRows == null) {
			return;
		}
		for (String[] row : testRows) {
			for (String aColVal : row) {
				Spit.out(aColVal + "\t");
			}
			Spit.out("\n");
		}
	}

	/**
	 * Convenient way to store an excel column along with Data and other
	 * information. purpose of this class just create a MetaData object for a
	 * column in an excel sheet. As usual way to do parsing the data. This
	 * object will contains a complete column info for an excel column.
	 * 
	 * @author Anant
	 * 
	 */
	private class ColumnMetaData {
		private DataValueType exilDataType;
		private int xlsDataType;
		private int columnIndex;
		private String columnName;

		/*
		 * private List<Value> columnValues; Dropped this because of
		 * performance? I can store it at the time of sheet iteration as a
		 * list<Value[]> which represents a row of sheet. this.columnValues =
		 * new ArrayList<Value>(); columnValues.toArray(new Value[0]);
		 */

		public ColumnMetaData() {
			this.exilDataType = DataValueType.TEXT;
			this.xlsDataType = XLSReader.UNKNOWN_TYPE;
			this.columnIndex = XLSReader.UNKNOWN_COLUMN_IDX;
			this.columnName = XLSReader.UNKNOWN_COLUMN_NAME;
		}

		public DataValueType getExilDataType() {
			return this.exilDataType;
		}

		public void setExilDataType(DataValueType dtVal) {
			this.exilDataType = dtVal;
		}

		public int getXlsDataType() {
			return this.xlsDataType;
		}

		public void setXlsDataType(int xlsDataType) {
			this.xlsDataType = xlsDataType;
		}

		@SuppressWarnings("unused")
		public int getColumnIndex() {
			return this.columnIndex;
		}

		public void setColumnIndex(int columnIndex) {
			this.columnIndex = columnIndex;
		}

		public String getColumnName() {
			return this.columnName;
		}

		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}

	}

}

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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/***
 * Utility to save/read exility components to/from xlsx files.
 * 
 */
public class XlxUtil {
	/**
	 * All methods are actually static. However, originally it was designed as
	 * non-static, and hence we are implementing a singleton as pf now
	 */
	private static final XlxUtil singleInstance = new XlxUtil();
	private static final String EMPTY_STRING = "";
	private static final String SHEETS_TO_BE_LOADED = "sheetsToBeLoaded";

	/***
	 * Get an instance of the utility
	 * 
	 * @return utility
	 */
	public static XlxUtil getInstance() {
		return singleInstance;
	}

	/***
	 * constructor
	 */
	private XlxUtil() {

	}

	/***
	 * Read the first sheet and get data as text
	 * 
	 * @param fullyQualifiedName
	 *            file name to read it from
	 * @return array of array of text, representing rows and columns. Note that
	 *         all rows will have same number of columns.
	 */
	public String[][] getRawData(String fullyQualifiedName) {
		List<Sheet> sheets = this.getSheets(fullyQualifiedName, null);
		if (sheets.size() == 0) {
			return null;
		}

		return this.getRawData(sheets.get(0), false);
	}

	/***
	 * read spreadsheet from stream and return rows and column representation of
	 * the first sheet in that
	 * 
	 * @param inputStream
	 * @return raw data from the spread sheet
	 */
	public String[][] getRawData(InputStream inputStream) {
		List<Sheet> sheets = this.getSheets(inputStream, null);
		if (sheets.size() == 0) {
			return null;
		}

		return this.getRawData(sheets.get(0), false);
	}

	/***
	 * Read the first sheet in the file and return that as a grid
	 * 
	 * @param fullyQualifiedName
	 *            file name to read from
	 * @param useDataDictionary
	 *            whether to use data dictionary for data types or guess it from
	 *            the spread sheet
	 * @return Grid corresponding to the first sheet of the workbook
	 */
	public Grid getGrid(String fullyQualifiedName, boolean useDataDictionary) {
		List<Sheet> sheets = this.getSheets(fullyQualifiedName, null);
		if (sheets.size() == 0) {
			return null;
		}

		return this.getGrid(sheets.get(0), useDataDictionary, false);
	}

	/***
	 * read the stream as a work book and get grid corresponding to the first
	 * sheet
	 * 
	 * @param inputStream
	 * @param useDataDictionary
	 *            whether to use data dictionary to get the data type of columns
	 * @return get the first sheet as a grid
	 */
	public Grid getGrid(InputStream inputStream, boolean useDataDictionary) {
		List<Sheet> sheets = this.getSheets(inputStream, null);
		if (sheets.size() == 0) {
			return null;
		}

		return this.getGrid(sheets.get(0), useDataDictionary, false);
	}

	/***
	 * read all sheets from the work book. Sheet named "values" is converted
	 * into name-value pair. Each sheet that has data is converted into grid and
	 * put into dc with the sheet name
	 * 
	 * @param fullyQualifiedName
	 * @param dc
	 * @param useDataDictionary
	 *            whether to use data dictionary to get the data type of columns
	 * @return number of rows extracted
	 */
	public int extract(String fullyQualifiedName, DataCollection dc,
			boolean useDataDictionary) {
		return this.extract(fullyQualifiedName, dc, useDataDictionary, false);
	}

	/***
	 * read all sheets from the work book. Sheet named "values" is converted
	 * into name-value pair. Each sheet that has data is converted into grid and
	 * put into dc with the sheet name
	 * 
	 * @param fullyQualifiedName
	 * @param dc
	 * @param useDataDictionary
	 *            whether to use data dictionary to get the data type of columns
	 * @param expectValueInFirstColumn
	 *            if set to true, a row is ignored if its first column has no
	 *            value
	 * @return number of rows extracted
	 */
	public int extract(String fullyQualifiedName, DataCollection dc,
			boolean useDataDictionary, boolean expectValueInFirstColumn) {
		Spit.out("going to extract data from spread sheet "
				+ fullyQualifiedName);
		try {
			InputStream inputStream = this.getInputStream(fullyQualifiedName,
					dc);
			if (inputStream == null) {
				return 0;
			}
			int n = this.extract(inputStream, dc, useDataDictionary);
			inputStream.close();
			return n;
		} catch (Exception e) {
			// we can only try
		}
		return 0;
	}

	/***
	 * Worker method to deliver the two extract methods exposed as public
	 * 
	 * @param inputStream
	 *            stream from which to read. Null if file is to be used
	 * @param dc
	 * @param useDictionaryForDataType
	 *            whether to use data dictionary to determine data types of
	 *            fields/columns. If this is false, we guess that data types
	 *            based on cell contents
	 * @return 0 if nothing was extracted. 1 if something is extracted at least
	 */
	public int extract(InputStream inputStream, DataCollection dc,
			boolean useDictionaryForDataType) {
		return this.extractWorker(inputStream, dc, useDictionaryForDataType,
				false);
	}

	private int extractWorker(InputStream inputStream, DataCollection dc,
			boolean useDictionaryForDataType, boolean expectValueInFirstColumn) {

		List<Sheet> sheets = this.getSheets(inputStream, dc);
		int nbrSheets = sheets.size();
		if (nbrSheets == 0) {
			return 0;
		}

		/*
		 * we know that the values_table, if exists, is the first one
		 */
		/**
		 * null means read all, otherwise read only the sheets found in this set
		 */
		Set<String> sheetsToBeRead = null;
		int startAt = 0;
		Sheet sheet = sheets.get(0);
		if (sheet.getSheetName().equals(CommonFieldNames.VALUES_TABLE_NAME)) {
			startAt = 1;
			this.extractValues(sheet, dc, useDictionaryForDataType);
			String sheetNames = dc.getTextValue(XlxUtil.SHEETS_TO_BE_LOADED,
					null);
			if (sheetNames != null) {
				sheetsToBeRead = new HashSet<String>();
				for (String sn : sheetNames.split(",")) {
					sheetsToBeRead.add(sn.trim());
				}
			}
		}
		for (int i = startAt; i < nbrSheets; i++) {
			sheet = sheets.get(i);
			String nam = sheet.getSheetName();
			/*
			 * should this sheet be read?
			 */
			if (sheetsToBeRead != null && sheetsToBeRead.contains(nam) == false) {
				Spit.out("skipping sheet " + nam
						+ " as directed by first sheet.");
				continue;
			}

			Grid grid = this.getGrid(sheet, useDictionaryForDataType,
					expectValueInFirstColumn);
			if (grid != null) {
				dc.addGrid(nam, grid);
			}
		}
		return nbrSheets;
	}

	/**
	 * save data into spread sheet
	 * 
	 * @param fullyQualifiedName
	 * @param data
	 * @return whether data was saved successfully into the file
	 */
	public boolean save(String fullyQualifiedName, String[][] data) {
		Workbook workbook = this.getWorkbookForFile(fullyQualifiedName);
		this.addSheet(workbook, data, "data");
		return this.save(workbook, fullyQualifiedName);
	}

	/**
	 * save contents of dc into a spread sheet
	 * 
	 * @param fullyQualifiedName
	 * @param dc
	 * @return true of we are able to save
	 */
	public boolean save(String fullyQualifiedName, DataCollection dc) {
		Workbook workbook = this.getWorkbookForFile(fullyQualifiedName);
		return this.save(this.copyToWorkbook(dc, workbook), fullyQualifiedName);
	}

	/**
	 * 
	 * @param fullyQualifiedName
	 * @param grid
	 * @return true if we are able to save
	 */
	public boolean save(String fullyQualifiedName, Grid grid) {
		Workbook workbook = this.getWorkbookForFile(fullyQualifiedName);
		this.addSheet(workbook, grid);
		return this.save(workbook, fullyQualifiedName);
	}

	/***
	 * get sheets from workbook. Workbook is read either form input stream, or
	 * from the file.
	 * 
	 * @param inputStream
	 *            stream from which to read. Null if the file is to be used
	 *            instead.
	 * @param fullyQualifiedFileName
	 *            if stream is null, this is to be a valid file name for the
	 *            workbook
	 * @param dc
	 * @return
	 */
	private List<Sheet> getSheets(String fullyQualifiedFileName,
			DataCollection dc) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(fullyQualifiedFileName);
		} catch (FileNotFoundException e) {
			String msg = "error while opening file " + fullyQualifiedFileName
					+ e.getMessage();
			if (dc != null) {
				dc.addError(msg);
			}
			Spit.out(msg);
		}
		if (inputStream == null) {
			return new ArrayList<Sheet>();
		}
		List<Sheet> sheets = this.getSheets(inputStream, dc);
		try {
			inputStream.close();
		} catch (IOException e) {
			// we can only try..
		}
		return sheets;
	}

	/**
	 * 
	 * @param inputStream
	 * @param dc
	 * @return
	 */
	private List<Sheet> getSheets(InputStream inputStream, DataCollection dc) {
		List<Sheet> sheets = new ArrayList<Sheet>();
		Workbook workbook = null;
		boolean valuesSheetFound = false;
		try {
			workbook = WorkbookFactory.create(inputStream);
			int n = workbook.getNumberOfSheets();
			for (int i = 0; i < n; i++) {
				Sheet sheet = workbook.getSheetAt(i);
				int nbrRows = sheet.getPhysicalNumberOfRows();
				String sheetName = sheet.getSheetName();
				if (nbrRows > 0) {
					sheets.add(sheet);

					if (!valuesSheetFound
							&& sheetName
									.equals(CommonFieldNames.VALUES_TABLE_NAME)) {
						/*
						 * this is supposed to be the first one. swap it if
						 * required
						 */
						if (i != 0) {
							sheets.add(i, sheets.get(0));
							sheets.add(0, sheet);
						}
						valuesSheetFound = true;
					}
				}

			}
		} catch (Exception e) {
			String msg = "Error while reading spread sheet. " + e.getMessage();
			Spit.out(msg);
			if (dc != null) {
				dc.addError(msg);
			}
		}
		return sheets;
	}

	/***
	 * Get a grid for the content of this sheet. First row of the sheet is
	 * assumed to be column heading. Columns from the header are assumed to be
	 * the columns for the entire sheet
	 * 
	 * @param sheet
	 * @return
	 */
	private Grid getGrid(Sheet sheet, boolean useDataDictionary,
			boolean expectValueInFirstColumn) {
		String[][] rawData = this.getRawData(sheet, expectValueInFirstColumn);

		if (rawData == null) {
			return null;
		}

		// let us get the data type of each of the columns, by inspecting down
		// the row till we get a cell with data in it
		DataValueType[] types = null;
		if (useDataDictionary == false) {
			types = this.getExilityTypes(sheet, rawData[0].length);
		}

		// let us now add column after column to the grid
		Grid grid = new Grid(sheet.getSheetName());
		try {
			grid.setRawData(rawData, types);
		} catch (ExilityException e) {
			Spit.out("Error while converting sheet " + sheet.getSheetName()
					+ " to grid " + e.getMessage());
		}
		return grid;
	}

	/***
	 * Get contents of a sheet into text rows and columns
	 * 
	 * @param sheet
	 * @return
	 */
	private String[][] getRawData(Sheet sheet, boolean expectValueInFirstColumn) {

		// let us get a normalized rows/columns out of this sheet.
		int firstRowIdx = sheet.getFirstRowNum();
		Row firstRow = sheet.getRow(firstRowIdx);
		int firstCellIdx = firstRow.getFirstCellNum();
		int lastCellAt = firstRow.getLastCellNum();
		int nbrCells = lastCellAt - firstCellIdx;

		int lastRow = sheet.getLastRowNum();

		List<String[]> rawData = new ArrayList<String[]>();
		for (int rowNbr = firstRowIdx; rowNbr <= lastRow; rowNbr++) {
			Row row = sheet.getRow(rowNbr);
			if (row == null || row.getPhysicalNumberOfCells() == 0) {
				Spit.out("row at "
						+ rowNbr
						+ "is empty. while this is not an error, we certianly discourage this.");
				continue;
			}

			String[] rowData = this.getTextValues(row, firstCellIdx, nbrCells);
			if (rowData == null) {
				continue;
			}
			if (expectValueInFirstColumn) {
				String firstData = rowData[0];
				if (firstData == null || firstData.length() == 0) {
					Spit.out("row at"
							+ rowNbr
							+ " has its first column empty, and hence the row is ignored");
					continue;
				}
			}
			rawData.add(rowData);
		}

		if (rawData.size() > 0) {
			return rawData.toArray(new String[0][0]);
		}
		return null;
	}

	/***
	 * get values in a row as per a normalized list with fixed number of columns
	 * in each row
	 * 
	 * @param row
	 *            We expect that all data rows have the same set of columns,
	 *            extra cells are ignored while blank is assumed for missing
	 *            cells
	 * @param startingCell
	 *            - as determined from header row.
	 * @param nbrCells
	 * @return
	 */
	private String[] getTextValues(Row row, int startingCell, int nbrCells) {
		if (row == null) {
			return null;
		}

		String[] values = new String[nbrCells];
		int lastCell = startingCell + nbrCells;
		boolean cellFound = false;
		for (int i = startingCell; i < lastCell; i++) {
			String textValue = this.getTextValue(row.getCell(i));
			if (cellFound == false && textValue.length() > 0) {
				cellFound = true;
			}
			values[i] = textValue;
		}
		if (cellFound) {
			return values;
		}
		return null;
	}

	/***
	 * get text value of cell irrespective of its content type
	 * 
	 * @param cell
	 * @return
	 */
	private String getTextValue(Cell cell) {
		if (cell == null) {
			return EMPTY_STRING;
		}

		int cellType = cell.getCellType();
		if (cellType == Cell.CELL_TYPE_BLANK) {
			return EMPTY_STRING;
		}

		if (cellType == Cell.CELL_TYPE_FORMULA) {
			cellType = cell.getCachedFormulaResultType();
		}

		if (cellType == Cell.CELL_TYPE_STRING) {
			return cell.getStringCellValue().trim();
		}

		/*
		 * dates are internally stored as decimal..
		 */
		if (cellType == Cell.CELL_TYPE_NUMERIC) {
			if (DateUtil.isCellDateFormatted(cell)) {
				return DateUtility.formatDate(cell.getDateCellValue());
			}

			return NumberFormat.getInstance()
					.format(cell.getNumericCellValue());
		}

		if (cellType == Cell.CELL_TYPE_BOOLEAN) {
			if (cell.getBooleanCellValue()) {
				return "1";
			}
			return "0";
		}
		return EMPTY_STRING;
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
	private void extractValues(Sheet sheet, DataCollection dc,
			boolean useDictionaryForDataType) {
		int n = sheet.getLastRowNum();

		// if there are no values, following for loop will not execute..
		for (int i = 1; i <= n; i++) // first row is header
		{
			Row row = sheet.getRow(i);
			if (row == null) {
				continue;
			}
			// value row should have just two cells in it
			int nbrCells = row.getLastCellNum();
			if (nbrCells < 1) {
				continue;
			}

			String fieldName = row.getCell(0, Row.CREATE_NULL_AS_BLANK)
					.getStringCellValue();
			if (fieldName.length() == 0) {
				continue; // no name
			}

			Cell dataCell = null;
			String fieldValue = EMPTY_STRING;
			if (nbrCells > 1) // value is present
			{
				dataCell = row.getCell(1, Row.CREATE_NULL_AS_BLANK);
				fieldValue = this.getTextValue(dataCell);
			}

			if (useDictionaryForDataType) {
				dc.addValueAfterCheckingInDictionary(fieldName, fieldValue);
			} else {
				dc.addValue(fieldName, fieldValue,
						this.getExilityType(dataCell));
			}
		}
	}

	/***
	 * get data types of column based on actual values in the sheet
	 * 
	 * @param sheet
	 * @param nbrCells
	 * @param rowStart
	 * @param rowEnd
	 * @return
	 */
	private DataValueType[] getExilityTypes(Sheet sheet, int nbrCells) {
		DataValueType[] types = new DataValueType[nbrCells];

		// though NULL is default (as of now that is the first one in ENUM) let
		// us explicitly populate it
		for (int i = 0; i < nbrCells; i++) {
			types[i] = DataValueType.NULL;
		}

		int rowStart = sheet.getFirstRowNum();
		int rowEnd = sheet.getLastRowNum();
		int nbrFound = 0;

		// which cell to start? We will go by the first cell of the first
		// physucal row
		Row firstRow = sheet.getRow(sheet.getFirstRowNum());
		int startingCellIdx = firstRow.getFirstCellNum();
		int endCellIdx = startingCellIdx + nbrCells;
		for (int i = rowStart; i <= rowEnd; i++) {
			Row row = sheet.getRow(i);
			if (row == null) {
				continue;
			}

			for (int j = startingCellIdx; j < endCellIdx; j++) {
				// do we already know this type?
				if (types[j] != DataValueType.NULL) {
					continue;
				}

				Cell cell = row.getCell(j, Row.RETURN_BLANK_AS_NULL);
				if (cell == null) {
					continue;
				}
				types[j] = this.getExilityType(cell);
				nbrFound++;
				if (nbrFound == nbrCells) {
					return types;
				}
			}
		}

		// we will treat unknown ones as text
		for (int i = 0; i < nbrCells; i++) {
			if (types[i] == DataValueType.NULL) {
				types[i] = DataValueType.TEXT;
			}
		}

		return types;
	}

	/***
	 * Guess the Exility DataValueType of a cell. Text, Decimal, Date abd
	 * boolean are the types that we infer. Boolean is straightforward. If it is
	 * numeric, we check whether it could be date. In case of formula, we go by
	 * the type of cached result.
	 * 
	 * @param cell
	 * @return Best guess of cell type
	 */
	private DataValueType getExilityType(Cell cell) {
		if (cell == null) {
			return DataValueType.TEXT;
		}

		int cellType = cell.getCellType();

		if (cellType == Cell.CELL_TYPE_FORMULA) {
			cellType = cell.getCachedFormulaResultType();
		}

		// dates are internally stored as decimal..
		if (cellType == Cell.CELL_TYPE_NUMERIC) {
			if (DateUtil.isCellDateFormatted(cell)) {
				return DataValueType.DATE;
			}

			return DataValueType.DECIMAL;
		}

		if (cellType == Cell.CELL_TYPE_BOOLEAN) {
			return DataValueType.BOOLEAN;
		}

		return DataValueType.TEXT;
	}

	private InputStream getInputStream(String fullyQualifiedName,
			DataCollection dc) {
		try {
			return new FileInputStream(fullyQualifiedName);
		} catch (Exception e) {
			String msg = "error while opening file " + fullyQualifiedName
					+ e.getMessage();
			if (dc != null) {
				dc.addError(msg);
			}
			Spit.out(msg);
		}
		return null;
	}

	private Workbook addSheet(Workbook workbook, String[][] data,
			String sheetName) {
		Sheet sheet = workbook.createSheet(sheetName);
		int rowIdx = 0;
		for (String[] dataRow : data) {
			Row row = sheet.createRow(rowIdx);
			rowIdx++;
			int cellIdx = 0;
			for (String val : dataRow) {
				row.createCell(cellIdx).setCellValue(val);
				cellIdx++;
			}
		}
		return workbook;
	}

	private Workbook copyToWorkbook(DataCollection dc, Workbook workbook) {
		/**
		 * values first
		 */
		Sheet sheet = workbook.createSheet(CommonFieldNames.VALUES_TABLE_NAME);
		Row header = sheet.createRow(0);
		Row dataRow = sheet.createRow(1);
		int colIdx = 0;
		String[] fieldNames = dc.getFieldNames();
		for (String fieldName : fieldNames) {
			header.createCell(colIdx).setCellValue(fieldName);
			this.setValue(dataRow.createCell(colIdx), dc.getValue(fieldName));
			colIdx++;
		}
		/**
		 * would like to blatantly ignore lists as NO ONE is using it. Will wait
		 * for a bug report :-)
		 */
		String[] gridNames = dc.getGridNames();
		if (gridNames != null && gridNames.length == 0) {
			for (String gridName : gridNames) {
				this.addSheet(workbook, dc.getGrid(gridName));
			}
		}
		return workbook;
	}

	private void addSheet(Workbook workbook, Grid grid) {
		Sheet sheet = workbook.createSheet(grid.getName());

		/**
		 * let us first create empty rows, so that we can go my columns later.
		 * First row is header;
		 */
		int nbrRows = grid.getNumberOfRows() + 1;
		for (int rowIdx = 0; rowIdx < nbrRows; rowIdx++) {
			sheet.createRow(rowIdx);
		}

		int colIdx = 0;
		for (String columnName : grid.getColumnNames()) {
			this.addColumn(sheet, colIdx, grid.getColumn(columnName),
					columnName);
			colIdx++;
		}
	}

	private void setValue(Cell cell, Value value) {
		if (value == null || value.isNull()) {
			cell.setCellValue(EMPTY_STRING);
			return;
		}
		switch (value.getValueType()) {
		case BOOLEAN:
			cell.setCellValue(value.getBooleanValue());
			break;

		case DATE:
			cell.setCellValue(value.getDateValue());
			break;

		case DECIMAL:
			cell.setCellValue(value.getDecimalValue());
			break;

		case INTEGRAL:
			cell.setCellValue(value.getIntegralValue());
			break;

		case NULL:
			cell.setCellValue(EMPTY_STRING);
			break;

		case TEXT:
			cell.setCellValue(value.getTextValue());
			break;

		case TIMESTAMP:
			cell.setCellValue(value.getDateValue());
			break;

		default:
			cell.setCellValue(value.getTextValue());
			break;
		}
	}

	private void addColumn(Sheet sheet, int colIdx, ValueList values,
			String header) {
		sheet.getRow(0).createCell(colIdx).setCellValue(header);
		switch (values.getValueType()) {
		case BOOLEAN:
			this.addColumn(sheet, colIdx, values.getBooleanList());
			return;
		case DATE:
		case TIMESTAMP:
			this.addColumn(sheet, colIdx, values.getDateList());
			return;

		case DECIMAL:
			this.addColumn(sheet, colIdx, values.getDecimalList());
			return;

		case INTEGRAL:
			this.addColumn(sheet, colIdx, values.getIntegralList());
			return;

		default:
			this.addColumn(sheet, colIdx, values.getTextList());
			break;
		}
	}

	private void addColumn(Sheet sheet, int colIdx, Date[] values) {
		int rowIdx = 1;
		for (Date date : values) {
			sheet.getRow(rowIdx).createCell(colIdx).setCellValue(date);
			rowIdx++;
		}
	}

	private void addColumn(Sheet sheet, int colIdx, String[] values) {
		int rowIdx = 1;
		for (String value : values) {
			sheet.getRow(rowIdx).createCell(colIdx).setCellValue(value);
			rowIdx++;
		}
	}

	private void addColumn(Sheet sheet, int colIdx, double[] values) {
		int rowIdx = 1;
		for (double value : values) {
			sheet.getRow(rowIdx).createCell(colIdx).setCellValue(value);
			rowIdx++;
		}
	}

	private void addColumn(Sheet sheet, int colIdx, long[] values) {
		int rowIdx = 1;
		for (long value : values) {
			sheet.getRow(rowIdx).createCell(colIdx).setCellValue(value);
			rowIdx++;
		}
	}

	private void addColumn(Sheet sheet, int colIdx, boolean[] values) {
		int rowIdx = 1;
		for (boolean value : values) {
			sheet.getRow(rowIdx).createCell(colIdx).setCellValue(value);
			rowIdx++;
		}
	}

	private boolean save(Workbook workbook, String fullyQualifiedName) {
		File file = new File(fullyQualifiedName);
		if (file.exists()) {
			ResourceManager.renameAsBackup(file);
			file = new File(fullyQualifiedName);
		}
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			workbook.write(os);
			os.close();
			return true;
		} catch (Exception e) {
			Spit.out("Error while saving " + fullyQualifiedName + ". "
					+ e.getMessage());
			try {
				if (os != null) {
					os.close();
				}
			} catch (IOException e1) {
				//
			}
			return false;
		}
	}

	/**
	 * Very specific requirement for saving labels. If the file exists, append
	 * only missing labels
	 * 
	 * @param fileName
	 * @param rows
	 * @return true if we are able to save the file
	 */
	public boolean appendMissingOnes(String fileName, String[][] rows) {
		File file = new File(fileName);
		Workbook workbook;
		Sheet sheet;
		if (file.exists()) {
			/**
			 * read spreadsheet
			 */
			try {
				InputStream is = new FileInputStream(file);
				workbook = WorkbookFactory.create(is);
				is.close();
				Spit.out(fileName + " read into a workbook.");
			} catch (Exception e) {
				Spit.out(fileName
						+ " is not saved because of an error while reading existing contents. "
						+ e.getMessage());
				Spit.out(e);
				return false;
			}
			sheet = workbook.getSheetAt(0);
			if (sheet == null) {
				sheet = workbook.createSheet();
			}

		} else {
			Spit.out(fileName + " does not exist. New file will be created.");
			/**
			 * first time this is being saved.
			 */
			workbook = this.getWorkbookForFile(fileName);
			sheet = workbook.createSheet();
		}
		if (sheet.getLastRowNum() > 0) {
			this.addMissingRows(sheet, rows);
		} else {
			this.addRows(sheet, rows);
		}
		return this.save(workbook, fileName);
	}

	private void addRows(Sheet sheet, String[][] rows) {
		int rowIdx = 0;
		for (String[] row : rows) {
			Row xlRow = sheet.createRow(rowIdx);
			int colIdx = 0;
			for (String columnValue : row) {
				xlRow.createCell(colIdx).setCellValue(columnValue);
				colIdx++;
			}
			rowIdx++;
		}
	}

	private void addMissingRows(Sheet sheet, String[][] rows) {
		/**
		 * create a set of existing labels
		 */
		Set<String> existingEntries = new HashSet<String>();
		int lastRow = sheet.getLastRowNum();
		for (int i = 0; i <= lastRow; i++) {
			Row row = sheet.getRow(i);
			if (row == null) {
				continue;
			}
			Cell cell = row.getCell(0);
			if (cell == null) {
				continue;
			}
			existingEntries.add(cell.getStringCellValue());
		}
		/**
		 * now, add rows, only if they are not there already
		 */
		for (String[] row : rows) {
			if (existingEntries.contains(row[0])) {
				continue;
			}
			lastRow++;
			Row xlRow = sheet.createRow(lastRow);
			int colIdx = 0;
			for (String columnValue : row) {
				xlRow.createCell(colIdx).setCellValue(columnValue);
				colIdx++;
			}
		}
	}

	private Workbook getWorkbookForFile(String fileName) {
		if (fileName.lastIndexOf('x') == fileName.length() - 1) {
			return new XSSFWorkbook();
		}

		return new HSSFWorkbook();
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		XlxUtil util = XlxUtil.getInstance();
		DataCollection dc = new DataCollection();
		util.extract(
				"D:/b/turtle/ExilityClient/webapp/WEB-INF/resource/workflow/first_wf_testing.xls",
				dc, false);
		Spit.out("We got " + dc.grids.size() + " sheets in the file.");
		for (Grid grid : dc.grids.values()) {
			Spit.out(grid.getName() + " has " + grid.getNumberOfRows()
					+ " rows and " + grid.getNumberOfColumns() + " columns.");
		}
	}
}

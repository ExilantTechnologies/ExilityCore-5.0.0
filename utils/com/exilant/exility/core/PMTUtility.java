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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * utility that was written for PathFinder.. Should be removed at some time
 * 
 */
@Deprecated
@SuppressWarnings("javadoc")
public class PMTUtility {

	public static int fileCtr = 0;
	public static int errCtr = 0;
	public static ArrayList<String> erroredFiles = new ArrayList<String>();
	public static int perrCtr = 0;
	public static ArrayList<String> perroredFiles = new ArrayList<String>();
	public static int pfileCtr = 0;
	public static FileWriter errorWriter = null;
	public static FileWriter sqlWriter = null;

	public static void parseFilesInDirectory(String dirName) {
		File rootDir = new File(dirName);
		if (rootDir.exists() && rootDir.isDirectory()) {
			File[] fileList = rootDir.listFiles();
			for (File curFile : fileList) {
				if (curFile.isDirectory()) {
					parseFilesInDirectory(curFile.getAbsolutePath());
				} else if (curFile.isFile()
						&& curFile.getName().endsWith("xml")) {
					// System.out.println("The current file is '" +
					// curFile.getAbsolutePath() + "'");
					String curFileName = curFile.getAbsolutePath();
					curFileName = curFileName.replaceAll("^.*\\\\sql\\\\", "");
					curFileName = curFileName.replaceAll("\\\\", ".");
					curFileName = curFileName.replaceAll("\\.xml$", "");
					curFileName = "sql." + curFileName;
					++PMTUtility.fileCtr;
					// System.out.println((++PMTUtility.fileCtr) + " - '" +
					// curFileName + "'");
					try {
						Sql sql = (Sql) ResourceManager.loadResource(
								curFileName, Sql.class);
						SqlParameter[] sqlParams = sql.inputParameters;
						DataCollection dc = new DataCollection();
						if (sqlParams != null) {
							for (SqlParameter sqlParam : sqlParams) {
								// System.out.println("'" + sqlParam.name +
								// "' - '" + sqlParam.getValueType() + "'");
								if (sqlParam.parameterType
										.equals(SqlParameterType.FILTER)) {
									dc.addIntegralValue(sqlParam.name
											+ "Operator", 1);
								}
								if (sqlParam.getValueType().equals(
										DataValueType.BOOLEAN)) {
									if (sqlParam.parameterType
											.equals(SqlParameterType.LIST)) {
										boolean values[] = { false };
										Grid grid;
										if (dc.hasGrid(sqlParam.gridName)) {
											grid = dc
													.getGrid(sqlParam.gridName);
										} else {
											grid = new Grid(sqlParam.gridName);
										}
										grid.addColumn(sqlParam.name,
												ValueList.newList(values));
										dc.addGrid(sqlParam.gridName, grid);
									} else {
										dc.addBooleanValue(sqlParam.name, false);
									}
								} else if (sqlParam.getValueType().equals(
										DataValueType.DATE)
										|| sqlParam.getValueType().equals(
												DataValueType.TIMESTAMP)) {
									if (sqlParam.parameterType
											.equals(SqlParameterType.LIST)) {
										Date values[] = { new Date() };
										Grid grid;
										if (dc.hasGrid(sqlParam.gridName)) {
											grid = dc
													.getGrid(sqlParam.gridName);
										} else {
											grid = new Grid(sqlParam.gridName);
										}
										grid.addColumn(sqlParam.name,
												ValueList.newList(values));
										dc.addGrid(sqlParam.gridName, grid);
									} else {
										dc.addDateValue(sqlParam.name,
												new Date());
									}
								} else if (sqlParam.getValueType().equals(
										DataValueType.DECIMAL)) {
									if (sqlParam.parameterType
											.equals(SqlParameterType.LIST)) {
										double values[] = { 1.0 };
										Grid grid;
										if (dc.hasGrid(sqlParam.gridName)) {
											grid = dc
													.getGrid(sqlParam.gridName);
										} else {
											grid = new Grid(sqlParam.gridName);
										}
										grid.addColumn(sqlParam.name,
												ValueList.newList(values));
										dc.addGrid(sqlParam.gridName, grid);
									} else {
										dc.addDecimalValue(sqlParam.name, 1.0);
									}
								} else if (sqlParam.getValueType().equals(
										DataValueType.INTEGRAL)) {
									if (sqlParam.parameterType
											.equals(SqlParameterType.LIST)) {
										long values[] = { 1 };
										Grid grid;
										if (dc.hasGrid(sqlParam.gridName)) {
											grid = dc
													.getGrid(sqlParam.gridName);
										} else {
											grid = new Grid(sqlParam.gridName);
										}
										grid.addColumn(sqlParam.name,
												ValueList.newList(values));
										dc.addGrid(sqlParam.gridName, grid);
									} else {
										dc.addIntegralValue(sqlParam.name, 1);
									}
								} else if (sqlParam.getValueType().equals(
										DataValueType.TEXT)) {
									if (sqlParam.parameterType
											.equals(SqlParameterType.LIST)) {
										String values[] = { "DEFAULTVALUE" };
										Grid grid;
										if (dc.hasGrid(sqlParam.gridName)) {
											grid = dc
													.getGrid(sqlParam.gridName);
										} else {
											grid = new Grid(sqlParam.gridName);
										}
										grid.addColumn(sqlParam.name,
												ValueList.newList(values));
										dc.addGrid(sqlParam.gridName, grid);
									} else {
										dc.addTextValue(sqlParam.name,
												new String("DEFAULTVALUE"));
									}
								} else {
									if (sqlParam.parameterType
											.equals(SqlParameterType.LIST)) {
										String values[] = { "DEFAULTVALUE" };
										Grid grid;
										if (dc.hasGrid(sqlParam.gridName)) {
											grid = dc
													.getGrid(sqlParam.gridName);
										} else {
											grid = new Grid(sqlParam.gridName);
										}
										grid.addColumn(sqlParam.name,
												ValueList.newList(values));
										dc.addGrid(sqlParam.gridName, grid);
									} else {
										dc.addTextValue(sqlParam.name,
												new String("DEFAULTVALUE"));
									}
								}
							}
							String sqlText = sql.getSql(dc);
							// System.out.println(sql.getSql(dc, null));
							PMTUtility.sqlWriter
									.write("=============================================================\r\n");
							PMTUtility.sqlWriter.write("The sql for the file '"
									+ curFileName + "' is :\r\n\r\n");
							PMTUtility.sqlWriter.write("\r\n" + sqlText
									+ "\r\n");
							PMTUtility.sqlWriter
									.write("=============================================================\r\n");
						} else {
							String sqlText = sql.getSql(dc);
							// System.out.println(sql.getSql(dc, null));
							PMTUtility.sqlWriter
									.write("=============================================================\r\n");
							PMTUtility.sqlWriter.write("The sql for the file '"
									+ curFileName + "' is :\r\n\r\n");
							PMTUtility.sqlWriter.write("\r\n" + sqlText
									+ "\r\n");
							PMTUtility.sqlWriter
									.write("=============================================================\r\n");
						}
						PMTUtility.pfileCtr++;
					} catch (ExilityException eEx) {
						PMTUtility.perrCtr++;
						perroredFiles.add(curFileName);
						try {
							PMTUtility.errorWriter.write("The exception in '"
									+ curFileName + "' is '" + eEx.getMessage()
									+ "'\r\n");
							for (StackTraceElement ste : eEx.getStackTrace()) {
								PMTUtility.errorWriter.write(ste.toString()
										+ "\r\n");
							}
							PMTUtility.errorWriter.write("\r\n");
						} catch (Exception ex) {
							//
						}
					} catch (Exception ex) {
						PMTUtility.errCtr++;
						erroredFiles.add(curFileName);
						try {
							PMTUtility.errorWriter.write("The exception in '"
									+ curFileName + "' is '" + ex.getMessage()
									+ "'\r\n");
							for (StackTraceElement ste : ex.getStackTrace()) {
								PMTUtility.errorWriter.write(ste.toString()
										+ "\r\n");
							}
							PMTUtility.errorWriter.write("\r\n");
						} catch (Exception eex) {
							//
						}
					}
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// String inputResourceDirectory =
		// "E:/Exilant/Base Line Version for Testing/2.1/ExilityDemoBaseLine/ExilityDemoBaseLine/EResource/";
		String inputResourceDirectory = "C:/workspace/SRESOURCE/";
		if (args.length > 0) {
			inputResourceDirectory = args[0].replaceAll("\\\\", "/") + "/";
		}

		ResourceManager.setResourceFolder(inputResourceDirectory);
		try {
			System.out.println("Processing the files ....");
			PMTUtility.sqlWriter = new FileWriter(new File("convertedSQLs.txt"));
			PMTUtility.errorWriter = new FileWriter(new File("errors.txt"));

			// System.out.println("Parsing the directory '" +
			// inputResourceDirectory + "'");
			PMTUtility.parseFilesInDirectory(inputResourceDirectory + "/sql");
			// System.out.println("Pasing of the directory '" +
			// inputResourceDirectory + "' completed.");

			FileWriter summaryWriter = new FileWriter("summary.txt");

			summaryWriter.write("Files with content errors : '"
					+ PMTUtility.errCtr + "'\r\n");
			for (String curFileName : PMTUtility.erroredFiles) {
				summaryWriter.write("'" + curFileName + "'\r\n");
			}
			summaryWriter.write("\r\n");
			summaryWriter.write("Files with parameter errors : '"
					+ PMTUtility.perrCtr + "'\r\n");
			for (String curFileName : PMTUtility.perroredFiles) {
				summaryWriter.write("'" + curFileName + "'\r\n");
			}
			summaryWriter.write("\r\n");
			summaryWriter.write("Files processed successfully : '"
					+ PMTUtility.pfileCtr + "'\r\n\r\n");
			summaryWriter.write("Total files in the directory : '"
					+ PMTUtility.fileCtr + "'\r\n\r\n");
			summaryWriter.close();

			PMTUtility.errorWriter.close();
			PMTUtility.sqlWriter.close();
			System.out
					.println("The files have been processed. Check summary.txt, errors.txt, convertedSQLs.txt for further details");
		} catch (IOException ioEx) {
			System.out.println("Processing failed with the error");
			System.out.println(ioEx.getMessage());
		}
	}
}

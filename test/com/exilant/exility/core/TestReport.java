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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

//TODO : found some very basic issue with local field names hiding class attributes. renamed them for the time being, but this code has to be reviewed- RGB
/**
 * This class is used to generate test reports for the test cases
 * 
 */
public class TestReport implements TestReportInterface {

	private static final String STRING = "";
	String testId = STRING;
	String description = STRING;
	String service = STRING;
	String preTestService = STRING;
	String postTestService = STRING;
	long expectedTimeInMs = 0;
	long actualTimeInMs = 0;
	boolean isTestCleared = false;
	String message = STRING;
	String fileName = STRING;
	DataCollection inDc = new DataCollection();
	DataCollection expectedDc = new DataCollection();
	DataCollection dc = new DataCollection();
	List<String[]> summary = new ArrayList<String[]>();
	List<String[]> list = new ArrayList<String[]>();

	/*
	 * Method implementing the TestProcessorInterface for processing the test
	 * cases. (non-Javadoc)
	 * 
	 * @see
	 * com.exilant.exility.core.TestProcessorInterface#process(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void process(final String fileInput, final String masterSummary)
			throws ExilityException {
		String stamp = ResourceManager.getTimeStamp();
		String qualifiedFileName = null;
		String testFileOutput = null;
		String testFile = null;
		// List<String[]> list = new ArrayList<String[]>();

		File file = new File(fileInput);
		File dir = new File(fileInput);
		testFileOutput = fileInput + "/"
				+ FilenameUtils.getName(file.toString()) + "_" + stamp
				+ Constants.OUT_SUFFIX;
		String summaryFile = fileInput + "/" + Constants.SUMMARY + "/"
				+ Constants.SUMMARY + "_" + stamp + Constants.XML;

		try {
			if (file.isFile()
					&& (file.getCanonicalPath()).endsWith(Constants.XML)) {
				qualifiedFileName = FilenameUtils.getName(file.toString());

				testFile = testFileOutput + "/" + qualifiedFileName
						+ Constants.OUT_SUFFIX + stamp + Constants.XML;
				this.test(file.toString(), testFile, this.list);
				this.generate(testFile, this.list);

			}
			// else {
			// for (File testFileInput : dir.listFiles()) {
			// try {
			// qualifiedFileName =
			// FilenameUtils.getName(testFileInput.toString());
			//
			// if (qualifiedFileName.equals(Constants.DS_STORE) ||
			// (qualifiedFileName.equals(Constants.SUMMARY))) {
			// continue;
			// }
			// if (testFileInput.isFile() &&
			// (testFileInput.getCanonicalPath()).endsWith(Constants.XML)) {
			//
			// testFile = testFileOutput + "/" +
			// qualifiedFileName.replace(Constants.XML, Constants.OUT_SUFFIX) +
			// Constants.XML;
			// test(testFileInput.toString(), testFile, list);
			// generate(testFile, list);
			//
			// }
			// else continue;
			// } catch (IOException e) {
			// Spit.out("Output file could not be created for " +
			// qualifiedFileName);
			// e.printStackTrace();
			// }
			// }
			//
			// }
			else {
				this.getFiles(dir);
			}
			this.merge(this.list);

			this.generateSummary(summaryFile, this.list, null);

			if (masterSummary != null) {
				this.generateSummary(null, this.summary, masterSummary);
			}

		} catch (IOException e) {
			Spit.out("Output file could not be created for " + fileInput);
			e.printStackTrace();
		}
	}

	// Private methods

	/***
	 * Carry out test as per test case defined in fileName and write output
	 * 
	 * @param inputFileName
	 * @param outputFileName
	 * @param results
	 */
	private void test(String inputFileName, String outputFileName,
			List<String[]> results) {
		try {
			this.doTest(inputFileName);
		} catch (Exception e) {
			Spit.out(e);
			this.message = "Error: " + e.getMessage();
			this.dc.addError(e.getMessage());
		}

		if (results != null) {
			results.add(this.toArray());
		}

		if (outputFileName != null) {
			this.toDc(this.dc);

		}

	}

	/**
	 * @param testFileName
	 * @throws ExilityException
	 */
	private void doTest(String testFileName) throws ExilityException {
		this.fileName = testFileName;
		this.resetAttributes();

		// this method is long, but that is because of large number of simple
		// validations. hence living with that.
		XlUtil util = new XlUtil();
		util.extract(this.fileName, this.dc, true);
		Grid controls = this.dc.getGrid(Constants.CONTROL_SHEET);

		if (controls == null) {
			this.message = this.fileName
					+ " does not have the sheet with name "
					+ Constants.CONTROL_SHEET
					+ " that is to contain control information.";
			return;
		}

		this.service = controls.getValueAsText(Constants.SERVICE, 0, null);
		this.testId = controls.getValueAsText("testId", 0, null);
		this.description = controls.getValueAsText("description", 0, null);
		if (this.service == null || this.service.length() == 0) {
			this.message = Constants.CONTROL_SHEET + " sheet in "
					+ this.fileName + " does not have serviceName.";
		}

		ServiceEntry se = ServiceList.getServiceEntry(this.service, true);
		if (se == null) {
			this.message = this.service + " is not a valid service.";
			return;
		}

		ServiceEntry preSe = null;
		ServiceEntry postSe = null;

		this.preTestService = controls.getValueAsText(Constants.PRE_SERVICE, 0,
				null);
		if (this.preTestService != null && this.preTestService.length() > 0) {
			preSe = ServiceList.getServiceEntry(this.preTestService, true);
			if (preSe == null) {
				this.message = this.preTestService + " is not a valid service.";
			}
		}

		this.postTestService = controls.getValueAsText(Constants.POST_SERVICE,
				0, null);
		if (this.postTestService != null && this.postTestService.length() > 0) {
			postSe = ServiceList.getServiceEntry(this.postTestService, true);
			if (postSe == null) {
				this.message = this.postTestService
						+ " is not a valid service entry or a service.";
			}
		}

		String text = controls.getValueAsText(Constants.EXPECTED_TIME, 0, null);
		if (text != null && text.length() > 0) {
			try {
				this.expectedTimeInMs = Long.parseLong(text);
			} catch (Exception e) {
				this.message = Constants.EXPECTED_TIME
						+ " has a non-integral valaue of "
						+ text
						+ ". Please provide expected completion time in milliseconds.";
			}
		}

		// we extracted all values from this grid. Let us get rid of that...
		this.dc.removeGrid(Constants.CONTROL_SHEET);
		// dc.removeValue(Constants.TEST_CASE_TO_BE_CAPTURED);

		this.loadGrids(this.dc, this.inDc, this.expectedDc);
		// inDc.removeValue(Constants.TEST_CASE_TO_BE_CAPTURED);
		// expectedDc.removeValue(Constants.TEST_CASE_TO_BE_CAPTURED);

		// reality check
		if (this.message != null) {
			// OK, we are all set for testing
			if (preSe != null) {
				preSe.serve(this.inDc);
			}
		}
		// in future, we can fire this in a separate thread, and record
		// important thread statistics as part of testing
		long startedAt = Calendar.getInstance().getTimeInMillis();
		se.serve(this.inDc);
		this.actualTimeInMs = Calendar.getInstance().getTimeInMillis()
				- startedAt;
		if (postSe != null) {
			postSe.serve(this.inDc);
		}

		if (this.inDc.hasAllFieldsOf(this.expectedDc) == false) {
			this.message = "Output data does not match with expected data.";
		}
		this.isTestCleared = true;
	}

	/***
	 * it is possible that the caller may reuse an instance to test more than
	 * one test case. Reset whenever a file is set.
	 */
	private void resetAttributes() {
		this.message = null;
		this.actualTimeInMs = 0;
		this.isTestCleared = false;
	}

	/**
	 * Converting to array to enable inserting into the excel sheet as a row.
	 */
	private String[] toArray() {
		String[] arr = { this.testId, this.description, this.service,
				this.preTestService, this.postTestService,
				Long.toString(this.expectedTimeInMs),
				Long.toString(this.actualTimeInMs),
				(this.isTestCleared ? Constants.TRUE : Constants.FALSE),
				this.message };
		return arr;
	}

	/**
	 * Inserting control sheet details into dc
	 * 
	 * @param thisDc
	 * 
	 */
	private void toDc(DataCollection thisDc) {
		thisDc.addTextValue(Constants.TEST_ID, this.testId);
		thisDc.addTextValue(Constants.DESCRIPTION, this.description);
		thisDc.addTextValue(Constants.SERVICE, this.service);
		thisDc.addTextValue(Constants.PRE_SERVICE, this.preTestService);
		thisDc.addTextValue(Constants.POST_SERVICE, this.postTestService);
		thisDc.addTextValue(Constants.EXPECTED_TIME, this.expectedTimeInMs
				+ STRING);
		thisDc.addTextValue(Constants.TEST_CLEARED, this.isTestCleared + STRING);
		thisDc.addTextValue(Constants.ACTUAL_TIME, this.actualTimeInMs + STRING);
		thisDc.addTextValue(Constants.MESSAGE, this.message);
	}

	/***
	 * Copy data from dc to inDc and expectedDc
	 * 
	 * @param thisDc
	 * @param thisInDc
	 * @param thisExpectedDc
	 */
	private void loadGrids(DataCollection thisDc, DataCollection thisInDc,
			DataCollection thisExpectedDc) {
		for (String gridName : thisDc.getGridNames()) {
			Grid grid = thisDc.getGrid(gridName);
			if (gridName.startsWith(Constants.IN_PREFIX)) {
				thisInDc.addGrid(
						gridName.substring(Constants.IN_PREFIX.length()), grid);
			} else if (gridName.startsWith(Constants.OUT_PREFIX)) {
				thisExpectedDc
						.addGrid(gridName.substring(Constants.OUT_PREFIX
								.length()), grid);
			} else {
				this.message = gridName + " is a sheet in " + this.fileName
						+ ". All sheets for input should have "
						+ Constants.IN_PREFIX
						+ " as prefix and all output sheets should have "
						+ Constants.OUT_PREFIX + " as prefix.";
				return;
			}
		}

		// _values will have to be put into dc.values
		if (thisInDc.hasGrid(Constants.VALUES_SHEET)) {
			thisInDc.gridToValues(Constants.VALUES_SHEET);
			thisInDc.removeGrid(Constants.VALUES_SHEET);
		}

		if (thisExpectedDc.hasGrid(Constants.VALUES_SHEET)) {
			thisExpectedDc.gridToValues(Constants.VALUES_SHEET);
			thisExpectedDc.removeGrid(Constants.VALUES_SHEET);
		}
	}

	/**
	 * Generating the individual test reports
	 * 
	 * @param testFileOutput
	 * @param thisList
	 */
	private void generate(final String testFileOutput, List<String[]> thisList) {
		StringBuilder sbf = new StringBuilder(XlUtil.XL_BEGIN);
		Grid controlSheet = new Grid();

		for (String[] controlContents : thisList) {

			this.testId = controlContents[0];
			this.description = controlContents[1];
			this.service = controlContents[2];
			this.preTestService = controlContents[3];
			this.postTestService = controlContents[4];
			this.expectedTimeInMs = Long.parseLong(controlContents[5]);
			this.actualTimeInMs = Long.parseLong(controlContents[6]);
			this.isTestCleared = Boolean.parseBoolean(controlContents[7]);
			this.message = controlContents[8];

			try {
				controlSheet.setRawData(this.getControlSheet());
			} catch (ExilityException e) {
				e.printStackTrace();
			}

			// Write control sheet into spread sheet
			sbf = new StringBuilder(XlUtil.XL_BEGIN);
			controlSheet.toSpreadSheetXml(sbf, "_controlSheet");
			this.inDc.toSpreadSheetXml(sbf, "out_");
			sbf.append(XlUtil.XL_END);
			ResourceManager.saveText(testFileOutput, sbf.toString());

		}

	}

	/**
	 * Method for creating summary sheet
	 * 
	 * @param summaryFile
	 * @param thisList
	 * @param masterSummary
	 * @throws ExilityException
	 */
	private void generateSummary(final String summaryFile,
			List<String[]> thisList, final String masterSummary)
			throws ExilityException {
		Grid controlSheet = new Grid();
		StringBuilder sbf = new StringBuilder(XlUtil.XL_BEGIN);

		if (masterSummary != null) {
			String[][] arraySummary = new String[this.summary.size() + 1][];

			for (int i = 0; i < this.summary.size(); i++) {
				arraySummary[i + 1] = this.summary.get(i);
			}

			arraySummary[0] = Constants.SUMMARY_FIELD_NAMES;
			controlSheet.setRawData(arraySummary);
			controlSheet.toSpreadSheetXml(sbf, Constants.SUMMARY_SHEET);
			sbf.append(this.inDc);

			sbf.append(XlUtil.XL_END);
			ResourceManager.saveText(masterSummary, sbf.toString());

		} else {
			String[][] arraySummary = new String[thisList.size() + 1][];

			for (int i = 0; i < thisList.size(); i++) {
				arraySummary[i + 1] = thisList.get(i);
			}

			arraySummary[0] = Constants.ALL_FIELD_NAMES;
			controlSheet.setRawData(arraySummary);
			controlSheet.toSpreadSheetXml(sbf, Constants.SUMMARY_SHEET);
			sbf.append(this.inDc);

			sbf.append(XlUtil.XL_END);
			ResourceManager.saveText(summaryFile, sbf.toString());

		}

	}

	/**
	 * Method for creating a 2d array for the control sheet , to add into grid
	 * 
	 * @return
	 */
	String[][] getControlSheet() {
		String[][] sheet = new String[2][];
		sheet[0] = Constants.ALL_FIELD_NAMES;
		sheet[1] = this.toArray();
		return sheet;
	}

	/**
	 * Method for creating the master summary sheet
	 * 
	 * @param summaryList
	 */
	private void merge(List<String[]> summaryList) {
		long timeTaken = 0;
		String thisTestId = "";
		String thisDescription = "";
		String thisService = "";
		String thisPreTestService = "";
		String thisPostTestService = "";
		int numberOfTestCases = 0;
		int numberPassed = 0;
		int numberFailed = 0;
		boolean localIsTestCleared = false;

		String[][] sheet = new String[1][];

		// List<String[]> serviceRow = new ArrayList<String[]>();
		for (String[] listContents : summaryList) {
			thisTestId = listContents[0];
			thisDescription = listContents[1];
			thisService = listContents[2];
			thisPreTestService = listContents[3];
			thisPostTestService = listContents[4];
			timeTaken = timeTaken + Long.parseLong(listContents[6]);
			numberOfTestCases = numberOfTestCases + 1;
			localIsTestCleared = Boolean.parseBoolean(listContents[7]);
			if (localIsTestCleared == false) {
				numberFailed++;
			} else if (localIsTestCleared == true) {
				numberPassed++;
			}
		}

		String[] arr = { thisTestId, thisDescription, thisService,
				thisPreTestService, thisPostTestService,
				Long.toString(timeTaken), Integer.toString(numberOfTestCases),
				Integer.toString(numberPassed), Integer.toString(numberFailed) };
		sheet[0] = arr;
		this.summary.add(arr);
	}

	/**
	 * Recursively get files from a directory with any number of sub-directories
	 * ; for processing all test cases under test folder if required
	 * 
	 * @param dir
	 */
	void getFiles(File dir) {
		for (File testFileInput : dir.listFiles()) {
			String qualifiedFileName = null;
			String testFileOutput = null;

			try {
				qualifiedFileName = FilenameUtils.getName(testFileInput
						.toString());

				if (qualifiedFileName.equals(Constants.DS_STORE)
						|| (qualifiedFileName.equals(Constants.SUMMARY))) {
					continue;
				}
				if (testFileInput.isFile()
						&& (testFileInput.getCanonicalPath())
								.endsWith(Constants.XML)) {

					String testFile = testFileOutput
							+ "/"
							+ qualifiedFileName.replace(Constants.XML,
									Constants.OUT_SUFFIX) + Constants.XML;
					this.test(testFileInput.toString(), testFile, this.list);
					this.generate(testFile, this.list);

				}
				if (testFileInput.isDirectory()) {
					this.getFiles(testFileInput);
				}
			} catch (IOException e) {
				Spit.out("Output file could not be created for "
						+ qualifiedFileName);
				e.printStackTrace();
			}
		}

	}
}

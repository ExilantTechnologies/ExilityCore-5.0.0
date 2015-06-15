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

/**
 * 
 *
 */
public class TestProcessor implements TestProcessorInterface {

	private static final String STRING = "";
	// attributes of a test case design, other than data. This is the control
	// data.
	String testId = STRING;
	String description = STRING;
	String service = STRING;
	String preTestService = STRING;
	String postTestService = STRING;
	long expectedTimeInMs = 0;
	long actualTimeInMs = 0;
	boolean testCleared = false;
	String message = STRING;
	String fileName = STRING;

	/**
	 * The singleton instance of this class.
	 */
	private static TestProcessor thisProcessor;

	/**
	 * Default constructor
	 */
	TestProcessor() {
		// Private so no outside instantiation
	}

	/**
	 * Get an instance of this processor.
	 * 
	 * @return instance of this processor.
	 */
	public static TestProcessorInterface getInstance() {
		if (thisProcessor == null) {
			thisProcessor = new TestProcessor();
		}
		return thisProcessor;
	}

	@Override
	public void process(String FileInput) throws ExilityException {
		String stamp = ResourceManager.getTimeStamp();
		String qualifiedFileName = null;
		String testFileOutput = null;
		String testFile = null;
		List<String[]> list = new ArrayList<String[]>();
		DataCollection dc = new DataCollection();
		File file = new File(FileInput);
		File dir = new File(FileInput);

		testFileOutput = FileInput + "/"
				+ FilenameUtils.getName(file.toString()) + Constants.OUT_SUFFIX;
		String summaryFile = FileInput + "/" + Constants.SUMMARY + "/"
				+ Constants.SUMMARY + stamp + ".xml";

		try {
			if (file.isFile() && (file.getCanonicalPath()).endsWith(".xml")) {
				qualifiedFileName = FilenameUtils.getName(file.toString());

				testFile = testFileOutput + "/" + qualifiedFileName + "_out"
						+ stamp + ".xml";
				dc = this.test(file.toString(), testFile, list);
				generate(dc, testFile, list);

			} else {
				for (File testFileInput : dir.listFiles()) {

					try {
						if (testFileInput.isFile()
								&& (testFileInput.getCanonicalPath())
										.endsWith(".xml")) {
							qualifiedFileName = FilenameUtils.getName(dir
									.toString());

							testFile = testFileOutput + "/" + qualifiedFileName
									+ "_out" + stamp + ".xml";
							dc = this.test(testFileInput.toString(), testFile,
									list);
							generate(dc, testFile, list);

						}
					} catch (IOException e) {
						Spit.out("Output file could not be created for "
								+ qualifiedFileName);
						e.printStackTrace();
					}
				}
			}
			generateSummary(dc, summaryFile, list);
		} catch (IOException e) {
			Spit.out("Output file could not be created for " + FileInput);
			e.printStackTrace();
		}
	}

	// Private methods

	/***
	 * Carry out test as per test case defined in fileName and write output
	 * 
	 * @param fileName
	 *            test case to be tested
	 * @return true if case succeeded, else false
	 */
	private DataCollection test(String inputFileName, String outputFileName,
			List<String[]> results) {
		DataCollection dc = new DataCollection();
		DataCollection inDc = new DataCollection();
		try {
			inDc = this.doTest(inputFileName, dc);
			// this.testCleared = true;
		} catch (Exception e) {
			Spit.out(e);
			this.message = "Error: " + e.getMessage();
			dc.addError(e.getMessage());
		}

		if (results != null) {
			results.add(this.toArray());
		}

		if (outputFileName != null) {
			this.toDc(dc);

		}
		return inDc;

	}

	/**
	 * @param testFileName
	 * @param dc
	 * @return
	 * @throws ExilityException
	 */
	private DataCollection doTest(String testFileName, DataCollection dc)
			throws ExilityException {
		this.fileName = testFileName;
		this.resetAttributes();

		// load inDc and expectedDc from dc
		DataCollection inDc = new DataCollection();
		DataCollection expectedDc = new DataCollection();

		// this method id long, but that is because of large number of simple
		// validations. hence living with that.
		XlUtil util = new XlUtil();
		util.extract(this.fileName, dc, true);
		Grid controls = dc.getGrid(Constants.CONTROL_SHEET);

		if (controls == null) {
			this.message = this.fileName
					+ " does not have the sheet with name "
					+ Constants.CONTROL_SHEET
					+ " that is to contain control information.";
			return null;
		}

		this.service = controls.getValueAsText(Constants.SERVICE, 0, null);
		this.testId = controls.getValueAsText("testId", 0, null);
		this.description = controls.getValueAsText("description", 0, null);
		if (this.service == null || this.service.length() == 0) {
			this.message = Constants.CONTROL_SHEET + " sheet in "
					+ this.fileName + " does not have serviceName.";
			return null;
		}

		ServiceEntry se = ServiceList.getServiceEntry(this.service, true);
		if (se == null) {
			this.message = this.service + " is not a valid service.";
			return null;
		}

		ServiceEntry preSe = null;
		ServiceEntry postSe = null;

		this.preTestService = controls.getValueAsText(Constants.PRE_SERVICE, 0,
				null);
		if (this.preTestService != null && this.preTestService.length() > 0) {
			preSe = ServiceList.getServiceEntry(this.preTestService, true);
			if (preSe == null) {
				this.message = this.preTestService + " is not a valid service.";
				return null;
			}
		}

		this.postTestService = controls.getValueAsText(Constants.POST_SERVICE,
				0, null);
		if (this.postTestService != null && this.postTestService.length() > 0) {
			postSe = ServiceList.getServiceEntry(this.postTestService, true);
			if (postSe == null) {
				this.message = this.postTestService
						+ " is not a valid service entry or a service.";
				return null;
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
				return null;
			}
		}

		// we extracted all values from this grid. Let us get rid of that...
		dc.removeGrid(Constants.CONTROL_SHEET);

		this.loadGrids(dc, inDc, expectedDc);

		// reality check
		if (this.message != null) {
			return null;
		}
		// OK, we are all set for testing
		if (preSe != null) {
			preSe.serve(inDc);
		}
		// in future, we can fire this in a separate thread, and record
		// important thread statistics as part of testing
		long startedAt = Calendar.getInstance().getTimeInMillis();
		se.serve(inDc);
		this.actualTimeInMs = Calendar.getInstance().getTimeInMillis()
				- startedAt;
		if (postSe != null) {
			postSe.serve(inDc);
		}

		if (inDc.hasAllFieldsOf(expectedDc) == false) {
			this.message = "Output data does not match with expected data.";
			return null;
		}
		this.testCleared = true;
		// System.out.println("Do test" + inDc);
		return inDc;
	}

	/***
	 * it is possible that the caller may reuse an instance to test more than
	 * one test case. Reset whenever a file is set.
	 */
	private void resetAttributes() {
		this.message = null;
		this.actualTimeInMs = 0;
		this.testCleared = false;
	}

	/**
	 * @return
	 */
	private String[] toArray() {
		String[] arr = { this.testId, this.description, this.service,
				this.preTestService, this.postTestService,
				Long.toString(this.expectedTimeInMs),
				Long.toString(this.actualTimeInMs),
				(this.testCleared ? "true" : "false"), this.message };
		return arr;
	}

	private void toDc(final DataCollection dc) {
		dc.addTextValue("testId", this.testId);
		dc.addTextValue("description", this.description);
		dc.addTextValue("service", this.service);
		dc.addTextValue("preTestService", this.preTestService);
		dc.addTextValue("postTestService", this.postTestService);
		dc.addTextValue("expectedTimeInMs", this.expectedTimeInMs + STRING);
		dc.addTextValue("testCleared", this.testCleared + STRING);
		dc.addTextValue("actualTimeInMs", this.actualTimeInMs + STRING);
		dc.addTextValue("message", this.message);
	}

	/***
	 * Copy data from dc to inDc and expectedDc
	 * 
	 * @param dc
	 * @param inDc
	 * @param expectedDc
	 */
	private void loadGrids(DataCollection dc, DataCollection inDc,
			DataCollection expectedDc) {
		for (String gridName : dc.getGridNames()) {
			Grid grid = dc.getGrid(gridName);
			if (gridName.startsWith(Constants.IN_PREFIX)) {
				inDc.addGrid(gridName.substring(Constants.IN_PREFIX.length()),
						grid);
			} else if (gridName.startsWith(Constants.OUT_PREFIX)) {
				expectedDc
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
		if (inDc.hasGrid(Constants.VALUES_SHEET)) {
			inDc.gridToValues(Constants.VALUES_SHEET);
			inDc.removeGrid(Constants.VALUES_SHEET);
		}

		if (expectedDc.hasGrid(Constants.VALUES_SHEET)) {
			expectedDc.gridToValues(Constants.VALUES_SHEET);
			expectedDc.removeGrid(Constants.VALUES_SHEET);
		}
	}

	private static void generate(final DataCollection dc,
			final String testFileOutput, List<String[]> list) {
		StringBuilder sbf = new StringBuilder(XlUtil.XL_BEGIN);
		Grid controlSheet = new Grid();

		for (String[] controlContents : list) {

			thisProcessor.testId = controlContents[0];
			thisProcessor.description = controlContents[1];
			thisProcessor.service = controlContents[2];
			thisProcessor.preTestService = controlContents[3];
			thisProcessor.postTestService = controlContents[4];
			thisProcessor.expectedTimeInMs = Long.parseLong(controlContents[5]);
			thisProcessor.actualTimeInMs = Long.parseLong(controlContents[6]);
			thisProcessor.testCleared = Boolean
					.parseBoolean(controlContents[7]);
			thisProcessor.message = controlContents[8];

			try {
				controlSheet.setRawData(thisProcessor.getControlSheet());
			} catch (ExilityException e) {
				e.printStackTrace();
			}

			// Write control sheet into spread sheet
			sbf = new StringBuilder(XlUtil.XL_BEGIN);
			controlSheet.toSpreadSheetXml(sbf, "_controlSheet");
			dc.toSpreadSheetXml(sbf, "out_");
			sbf.append(XlUtil.XL_END);
			ResourceManager.saveText(testFileOutput, sbf.toString());

		}

	}

	private static void generateSummary(final DataCollection dc,
			final String summaryFile, List<String[]> list)
			throws ExilityException {
		Grid controlSheet = new Grid();

		String[][] arraySummary = new String[list.size() + 1][];

		StringBuilder sbf = new StringBuilder(XlUtil.XL_BEGIN);

		for (int i = 0; i < list.size(); i++) {
			arraySummary[i + 1] = list.get(i);
		}

		arraySummary[0] = Constants.ALL_FIELD_NAMES;
		controlSheet.setRawData(arraySummary);
		controlSheet.toSpreadSheetXml(sbf, Constants.SUMMARY_SHEET);
		sbf.append(dc);

		sbf.append(XlUtil.XL_END);

		ResourceManager.saveText(summaryFile, sbf.toString());

	}

	String[][] getControlSheet() {
		String[][] sheet = new String[2][];
		sheet[0] = Constants.ALL_FIELD_NAMES;
		sheet[1] = this.toArray();
		return sheet;
	}
}

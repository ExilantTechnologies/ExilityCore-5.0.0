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
/*
 * Constants.java
 *
 */

package com.exilant.exility.core;

//sorry about the next cheating... 
@SuppressWarnings("javadoc")
public class Constants {

	// sheet names
	public static final String CONTROL_SHEET = "_controlSheet";
	public static final String SUMMARY_SHEET = "_summary";
	public static final String IN_VALUES_SHEET = "in__values";
	public static final String OUT_VALUES_SHEET = "out__values";

	// variable names in control sheet
	public static final String SERVICE = "service";
	public static final String PRE_SERVICE = "preTestService";
	public static final String POST_SERVICE = "postTestService";
	public static final String EXPECTED_TIME = "expectedTimeInMs";
	public static final String ACTUAL_TIME = "actualTimeInMs";

	public static final String IN_PREFIX = "in_";
	public static final String OUT_PREFIX = "out_";
	public static final String OUT_SUFFIX = "_out";
	public static final String VALUES_SHEET = "_values";
	public static final String SUMMARY = "summary";

	public static String[] ALL_FIELD_NAMES = { "testId", "description",
			"service", "preTestService", "postTestService", "expectedTimeInMs",
			"actualTimeInMs", "testCleared", "message" };
	public static String[] SUMMARY_FIELD_NAMES = { "testId", "description",
			"service", "preTestService", "postTestService", "totalTimeTaken",
			"numberOfTestCases", "numberPassed", "numberFailed" };
	public static final String TEST_CASE_TO_BE_CAPTURED = "_testCaseToBeCaptured";
	public static final String TEST = "test";
	public static final String DS_STORE = ".DS_Store";
	public static final String XML = ".xml";
	public static final String TEST_ID = "testId";
	public static final String DESCRIPTION = "description";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String TEST_CLEARED = "isTestCleared";
	public static final String MESSAGE = "message";

}

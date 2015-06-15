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

/**
 * manage reports
 * 
 */
public class Reports {

	private static Reports instance = null;
	Map<String, ReportParameters> reports = new HashMap<String, ReportParameters>();

	/**
	 * get parameters for this report
	 * 
	 * @param reportName
	 * @return parameters for this report
	 */
	public static ReportParameters getReport(String reportName) {
		load();
		// Jun 7 2010 : Bug 1213 - Jasper report integration - Nirmanith
		// (Start): Aravinda
		if (instance.reports.containsKey(reportName)) {
			return instance.reports.get(reportName);
		}
		// Jun 7 2010 : Bug 1213 - Jasper report integration - Nirmanith (End):
		// Aravinda
		Spit.out("Design error: " + reportName + " is not a valid report name");
		return null;
	}

	private static void load() {
		instance = (Reports) ResourceManager.loadResource("reportList",
				Reports.class);
		Spit.out("Loaded " + instance.reports.size() + " reports.");
	}

}

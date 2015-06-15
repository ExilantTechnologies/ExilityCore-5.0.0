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

import org.apache.commons.io.FilenameUtils;

/**
 * Thread for the the processor tool.
 * 
 */
public class LoadHandler implements Runnable, LoadHandlerInterface {

	@Override
	public void run() {
		// ResourceManager.setResourceFolder("/Users/harshabhat/Desktop/apache-tomcat-7.0.27/webapps/Ezapp/WEB-INF/resource");
		//
		// String fileName =
		// "/Users/harshabhat/Desktop/apache-tomcat-7.0.27/webapps/Ezapp/WEB-INF/resource/test";

		// Provide the path for resource folder and test filename. This should
		// be set in
		// configuration file and pickup from there or this should be a
		// parameter. For the moment
		// this value is hard-coded.
		ResourceManager.loadAllResources(null, "WEB-INF/resource");
		String fileName = "WEB-INF/resource/test/";
		String stamp = ResourceManager.getTimeStamp();
		String summary = fileName + "/" + Constants.SUMMARY + "/"
				+ Constants.SUMMARY + stamp + ".xml";

		try {
			this.process(fileName, summary);
		} catch (ExilityException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void process(final String fileName, final String summaryFile)
			throws ExilityException {
		TestReportInterface testProcessor = new TestReport();

		File dir = new File(fileName);

		for (File testFolder : dir.listFiles()) {
			String qualifiedFileName = FilenameUtils.getName(testFolder
					.toString());

			if (qualifiedFileName.equals(Constants.DS_STORE)
					|| (qualifiedFileName.equals(Constants.SUMMARY))) {
				continue;
			}
			try {
				testProcessor.process(testFolder.toString(), summaryFile);
			} catch (ExilityException e) {
				e.printStackTrace();
			}
		}
	}

}
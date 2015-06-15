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

import java.util.Date;

/***
 * 
 * Class that facilitates execution of a service in a separate thread that is
 * detached from the current thread. In other words, service is executed in the
 * background. ServiceAgent uses this class for running the service in
 * background
 * 
 */
public class BackgroundService implements Runnable {
	private final String userId;
	private final ServiceInterface service;
	private final ServiceData inData;
	private final String jobId;

	/***
	 * Creates an instance with attributes that are required for run a service
	 * in a new thread
	 * 
	 * @param userId
	 * @param service
	 * @param inData
	 * @param jobId
	 * 
	 */
	public BackgroundService(String userId, ServiceInterface service,
			ServiceData inData, String jobId) {
		this.userId = userId;
		this.service = service;
		this.inData = inData;
		this.jobId = jobId;
	}

	@Override
	public void run() {
		/*
		 * we re a new thread. Start capturing spits.
		 */
		Spit.startWriter();
		Date startTime = new Date();
		ServiceData outData = new ServiceData();
		this.service.serve(this.inData, outData);

		Date endTime = new Date();
		long diffTime = endTime.getTime() - startTime.getTime();
		String traceText = Spit.stopWriter() + "\nTime taken by Service "
				+ this.service.getName() + " is : " + diffTime
				+ " milliseconds";

		Spit.writeServiceLog(traceText, this.userId, this.service.getName());
		if (this.inData.hasValue(CommonFieldNames.TRACE_REQUEST)) {
			outData.addValue(CommonFieldNames.TRACE_TEXT, traceText);
		}
		FileUtility.writeText(FileUtility.FILE_TYPE_TEMP, this.jobId,
				outData.toSerializedData());

		/*
		 * do we need to save test case?
		 */
	}
}

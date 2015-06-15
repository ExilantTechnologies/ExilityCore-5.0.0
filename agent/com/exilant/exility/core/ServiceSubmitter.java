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
 * background.
 * 
 */
public class ServiceSubmitter implements Runnable {
	private final DataCollection dc;
	private final ServiceEntry entry;

	/***
	 * Creates an instance with its own dc for it to be run in the background.
	 * IMPORTANT: dc MUST be cloned rather than passed by refernece to avoid two
	 * threads trying to share teh same dc.
	 * 
	 * @param entry
	 *            underlying service entry that is interested in submitting a
	 *            service
	 * @param dc
	 *            cloned dc as input-output for service
	 */
	public ServiceSubmitter(ServiceEntry entry, DataCollection dc) {
		this.entry = entry;
		this.dc = dc;
	}

	@Override
	public void run() {
		/*
		 * this is a separate thread. It's trace needs to be managed
		 */
		Spit.startWriter();
		Date startedAt = new Date();
		boolean suppressLog = this.dc
				.hasValue(ExilityConstants.SUPPRESS_SQL_LOG);
		DbHandle handle = null;
		try {
			handle = DbHandle.borrowHandle(this.entry.dataAccessType,
					suppressLog);
			this.entry.execute(this.dc, handle);
			DbHandle.returnHandle(handle);
		} catch (Exception e) {
			this.dc.addError(e.getMessage());
			Spit.out(e);
		}

		/*
		 * Take care of logging the trace text
		 */
		String myTraceText = Spit.stopWriter();
		String serviceName = this.dc.getTextValue(CommonFieldNames.SERVICE_ID,
				"unknown service");
		String userId = this.dc.getTextValue(AP.loggedInUserFieldName,
				"unknown user");
		long ms = new Date().getTime() - startedAt.getTime();
		myTraceText = "\n background service started processing at "
				+ startedAt.toString() + " and took " + ms + " ms\n"
				+ myTraceText;
		Spit.writeServiceLog(myTraceText, userId, serviceName);

		/*
		 * In future, we might decide to give a verb to the designer where
		 * he/she can choose if they want to generate a job id. For now, the
		 * system always generates a job id. We will revisit this section when
		 * we provide that verb
		 */
		String jobId = this.dc.getTextValue(CommonFieldNames.BACKGROUND_JOB_ID,
				null);
		if (jobId != null) {
			// put trace text in dc if required
			if (this.dc.hasValue(CommonFieldNames.TRACE_REQUEST)) {
				this.dc.addTextValue(CommonFieldNames.TRACE_TEXT, myTraceText);
			}

			ServiceData outData = new ServiceData();
			this.dc.copyTo(outData);
			FileUtility.writeText(FileUtility.FILE_TYPE_TEMP, jobId,
					outData.toSerializedData());
		}
	}
}

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
 * Submits a task in the background (runs in a separate thread)
 * 
 */
class TaskSubmitter implements Runnable {
	private DataCollection dc;
	private ExilityTask task;

	public TaskSubmitter(DataCollection dc, ExilityTask task) {
		this.dc = dc;
		this.task = task;
	}

	@Override
	public void run() {
		Spit.startWriter();
		Date startedAt = new Date();
		Spit.out("Task " + this.task.taskName + " started at " + startedAt);
		boolean suppressLog = this.dc
				.hasValue(ExilityConstants.SUPPRESS_SQL_LOG);
		DbHandle handle = null;
		try {
			DataAccessType da = this.task.getDataAccessType();
			if (da != DataAccessType.NONE) {
				handle = DbHandle.borrowHandle(this.task.getDataAccessType(),
						suppressLog);
			}
			this.task.execute(this.dc, handle);
		} catch (Exception e) {
			this.dc.addError(e.getMessage());
			Spit.out(e);
		}
		if (handle != null) {
			DbHandle.returnHandle(handle);
		}

		Spit.out("Task " + this.task.taskName + " took "
				+ (new Date().getTime() - startedAt.getTime()) + " ms");
		Spit.stopWriter();
	}
}
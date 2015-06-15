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

/***
 * Service entry that consists of several services, each of which is executed
 * within its own transaction boundary. These services are executed in sequence.
 * Data that is coming out of the first service is input to the second service
 * One example of use is when you run a large report and then insert a row to
 * say that the report is sent. If this is one service, you end up running that
 * in a transaction boundary, with the RDBMS struggling with all the reads with
 * lock. Better to design one service that runs the report, and another service
 * that does the update.
 */
class GroupEntry extends ServiceEntry {
	ServiceEntry[] serviceEntries = null;

	@Override
	void serve(DataCollection dc) {
		if (this.serviceEntries == null || this.serviceEntries.length == 0) {
			dc.addError("No services defined for group entry " + this.name
					+ " in serviceList.xml");
			return;
		}
		for (ServiceEntry entry : this.serviceEntries) {
			entry.serve(dc);
		}
	}
}

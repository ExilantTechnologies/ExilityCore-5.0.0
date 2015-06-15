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

/**
 * Add a message to dc.Its name should have been AddMessageStep!
 * 
 */
class ValidationStep extends AbstractStep {
	/**
	 * message to add in case of error
	 */
	String messageName = "";
	/**
	 * list of parameters to be used to format message. We first treat it as a
	 * field name, failing which it is used as a ready value.
	 */

	String[] messageParameters = new String[0];

	/**
	 * should we move on with the other steps in this service even if we find
	 * error?
	 */
	boolean continueOnError = false;

	ValidationStep() {
		this.stepType = StepType.VALIDATIONSTEP;
	}

	@Override
	String executeStep(DataCollection dc, DbHandle handle) {
		if (this.messageName == null || this.messageName.length() == 0) {
			return AbstractStep.NEXT;
		}

		/*
		 * add message to dc
		 */
		MessageSeverity severity = MessageSeverity.IGNORE;
		if (this.messageParameters.length == 0) {
			severity = dc.addMessage(this.messageName, this.messageParameters);
		} else {
			String[] parms = new String[this.messageParameters.length];
			int i = 0;
			for (String parm : this.messageParameters) {
				if (dc.hasValue(parm)) {
					parms[i] = dc.getTextValue(parm, "");
				} else {
					parms[i] = parm;
				}
				i++;
			}
			severity = dc.addMessage(this.messageName, parms);
		}
		/*
		 * stop if required
		 */
		if (this.continueOnError == false && severity == MessageSeverity.ERROR) {
			return AbstractStep.STOP;
		}

		return AbstractStep.NEXT;
	}
}
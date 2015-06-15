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

enum StepType {
	DUMMYSTEP, SWITCHSTEP, IOSTEP, LOGICSTEP, ASSIGNMENTSTEP, ERRORCHECKSTEP, VALIDATIONSTEP, SERVICESTEP, HANDCODEDSTEP, GRIDPROCESSINGSTEP, FOREACHSTEP, SETVALUESTEP, LISTSTEP, APPENDTOLISTSTEP, GRIDSTEP, ADDCOLUMNSTEP, LOOKUPSTEP, FILTERSTEP, STOPSTEP, SAVESTEP, SAVEGRIDSTEP, DELETESTEP, MASSUPDATESTEP, MASSDELETESTEP, AGGREGATESTEP, LOOPSTEP, TASKSTEP, FUNCTIONSTEP
}

/**
 * represents a step in a service. A service simply consists of a number of
 * these steps that are executed
 * 
 */
public abstract class AbstractStep {
	/**
	 * This is not an enum, because the next step could be the label of the step
	 * to go to. These names have fixed meaning, and should not be used as
	 * labels.
	 */
	static final String STOP = "stop";
	static final String PREVIOUS = "previous";
	static final String NEXT = "next";
	static final String LAST = "last";
	static final String BREAK = "break";
	static final String CONTINUE = "continue";

	/**
	 * each step is identified by a distinct label within a service. Also, some
	 * step use <label>WorkDone as a field in dc to which they add the integer
	 * that represents something about the actual work that the step did. For
	 * example, it could be number of rows added to teh data base. This can be
	 * used to communicate this info across steps
	 */
	String label = null;

	/**
	 * for ease of use, instead of looking for instanceOf. This is set by
	 * concrete classes
	 */
	protected StepType stepType = StepType.DUMMYSTEP;

	/**
	 * set by extended class, for documentation only
	 */
	protected String stepSubType = null;

	/**
	 * for documentation
	 */
	String description = null;

	/**
	 * for documentation
	 */
	String techNotes = null;

	/**
	 * During the execution of a service, messages added by any step are carried
	 * by dc. Presence of a message in dc may indicate occurrence of an event,
	 * like validation failure. You may decide to skip this step if certain
	 * messages are raised before reaching here. Comma separated list is
	 * allowed.
	 */
	String[] skipOnMessageIds = new String[0];

	/*
	 * Refer to skipOnMessageIds. This option is to execute this step only of
	 * one of the messages is raised.
	 */
	String[] executeOnMessageIds = new String[0];

	/**
	 * precondition to be met for this step to be executed.
	 */
	Expression executeOnCondition = null;

	/**
	 * execute this step as per the definition of this step.
	 * 
	 * @param dc
	 *            dc
	 * @param handle
	 *            db handle
	 * @return token indicating what to do next? valid values are defined as
	 *         constants.
	 * @throws ExilityException
	 */
	String execute(DataCollection dc, DbHandle handle) throws ExilityException {
		/*
		 * is this a conditional step? i.e. to be executed only if the condition
		 * is met
		 */

		if (this.executeOnCondition != null) {
			if (!this.executeOnCondition.evaluate(dc).getBooleanValue()) {
				return AbstractStep.NEXT;
			}
		}

		/*
		 * other preconditions?? Skip if any of the listed messages are there in
		 * dc
		 */
		for (String message : this.skipOnMessageIds) {
			if (dc.hasMessage(message)) {
				return AbstractStep.NEXT;
			}
		}

		/*
		 * execute only if any one of the listed message is found in the dc
		 */
		if (this.executeOnMessageIds.length > 0) {
			boolean messageFound = false;
			for (String message : this.executeOnMessageIds) {
				if (dc.hasMessage(message)) {
					messageFound = true;
					break;
				}
			}
			if (!messageFound) {
				return AbstractStep.NEXT;
			}
		}
		/*
		 * OK. This step is to be executed.
		 */
		return this.executeStep(dc, handle);
	}

	/**
	 * execute the step. Implemented by concrete classes.
	 * 
	 * @param dc
	 *            dc
	 * @param handle
	 *            db handle
	 * @return token indicating what to do next
	 * @throws ExilityException
	 */
	abstract String executeStep(DataCollection dc, DbHandle handle)
			throws ExilityException;

	/***
	 * Let each step tell what kind of db access it wants, It helps us in
	 * determining what access a service needs
	 * 
	 * @param dc
	 * 
	 * @return data base access type required by this step.
	 */
	public DataAccessType getDataAccessType(DataCollection dc) {
		return DataAccessType.NONE;
	}
}
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

/**
 * 
 * defines a base task with frequently used methods for sub-classes
 * 
 */
public abstract class ExilityTask {
	/**
	 * execution of a step happens through a task. Name is used as the name of
	 * the underlying entity, like table/sql
	 */
	public String taskName = null;

	/**
	 * If a task is written with a step-time parameter, i.e. a common class is
	 * written for a set of related tasks
	 * 
	 * and a parameter is designed to be input to indicate which specific task
	 * you want to execute.. use this parameter Note: if you need more than one
	 * parameter, use your convention, say comma separated.
	 */

	public String[] taskParameters = new String[0];

	/**
	 * on execution, a task returns an integer that indicates the amount of work
	 * done. For example, an extract task returns number of rows extracted. Is
	 * it OK if this work is zero or do you want me to throw an exception?
	 * 
	 */
	public boolean raiseExceptionIfNoWorkDone = false;
	/**
	 * on execution, a task returns an integer that indicates the amount of work
	 * done. For example, an extract task returns number of rows extracted. Is
	 * it OK if this work is zero or do you want me to throw an exception?
	 * 
	 */
	public boolean raiseExceptionIfWorkDone = false;

	/**
	 * well, irrespective of whether I throw an exception ot not, do you want me
	 * to add a message if no work done?
	 */
	public String ifNoWorkDoneMessageName = null;

	/**
	 * And, if you need parameters to be passed to the message id, supply the
	 * name of the parameters, values for which will be found in dc for example
	 * if you say messageParameters="p1,p2,p3" Exility extracts the values for
	 * p1, p2 and p3 from DC and pushes them as parameters to your message. Note
	 * that you should have used @1, @2 etc.. in your message to make use of
	 * these parameters. for example you would have set
	 * customerNotFound="No record for customer with code=@1", you will set
	 * messageName="customerNotFound" and errorParameters="customerCode"
	 */
	public String[] ifNoWorkDoneMessageParameters = new String[0];

	/**
	 * message id to be added if work done
	 */
	public String ifWorkDoneMessageName = null;

	/**
	 * parameters for the message
	 */
	public String[] ifWorkDoneMessageParameters = new String[0];

	/**
	 * grid name is a fairly common attribute for most of them..
	 */
	public String gridName = null;

	/**
	 * if this task needs to be executed for each row of teh grid
	 */
	public boolean repeatForRowsInGrid = false;

	/**
	 * use Record in place of table for table tasks
	 */

	String recordName = null;

	/**
	 * concrete classes that implement an ExecutionStep will receive a
	 * taskContext and they have to implement execute method
	 * 
	 * @param dc
	 * @param handle
	 * @return unit of work done. 0 if no work is done
	 * @throws ExilityException
	 */
	public int execute(DataCollection dc, DbHandle handle)
			throws ExilityException {
		int workDone = 0;
		Date startDate = new Date();
		Spit.out("starting "
				+ this.taskName
				+ " with gridname as "
				+ this.gridName
				+ " and has Grid ="
				+ (this.gridName == null ? " false " : ("" + dc
						.hasGrid(this.gridName))));
		if (this.repeatForRowsInGrid) {
			if (dc.hasGrid(this.gridName)) {
				workDone = this.executeBulkTask(dc, handle);
			} else {
				Spit.out(this.gridName + " is not found for task "
						+ this.taskName);
				workDone = 0;
			}
		} else {
			workDone = this.executeTask(dc, handle);
		}
		Date endDate = new Date();
		long diffTime = endDate.getTime() - startDate.getTime();
		Spit.out("Time taken by Task : " + this.taskName + "  is :" + diffTime
				+ " milliseconds");
		// did the task do any work at all?
		if (workDone == 0) {
			if (this.ifNoWorkDoneMessageName != null) {
				if (this.raiseExceptionIfNoWorkDone) {
					dc.raiseException(this.ifNoWorkDoneMessageName, this
							.getErrorParameterValues(dc,
									this.ifNoWorkDoneMessageParameters));
				} else {
					dc.addMessage(this.ifNoWorkDoneMessageName, this
							.getErrorParameterValues(dc,
									this.ifNoWorkDoneMessageParameters));
				}
			}
		} else if (this.ifWorkDoneMessageName != null) {
			if (this.raiseExceptionIfWorkDone) {
				dc.raiseException(this.ifWorkDoneMessageName, this
						.getErrorParameterValues(dc,
								this.ifWorkDoneMessageParameters));
			} else {
				dc.addMessage(this.ifWorkDoneMessageName, this
						.getErrorParameterValues(dc,
								this.ifWorkDoneMessageParameters));
			}
		}

		return workDone;
	}

	private String[] getErrorParameterValues(DataCollection dc, String[] names) {
		if (names == null || names.length == 0) {
			return new String[0];
		}

		String[] parms = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			String parm = names[i];
			if (dc.hasValue(parm)) {
				parms[i] = dc.getTextValue(parm, "");
			} else {
				parms[i] = parm;
			}
		}
		return parms;
	}

	/**
	 * all concrete ExecutionStep classes should implement this method, and
	 * return an integer that implies the amount of work done. At this this I am
	 * looking at whether that is 0 or more. 0 implies no work done.
	 * 
	 * @param dc
	 * @param handle
	 * @return unit of work done
	 * @throws ExilityException
	 **/
	public abstract int executeTask(DataCollection dc, DbHandle handle)
			throws ExilityException;

	/**
	 * @param dc
	 * @param handle
	 * @return unit of work done. 0 if no work is done
	 * @throws ExilityException
	 */
	public abstract int executeBulkTask(DataCollection dc, DbHandle handle)
			throws ExilityException;

	/***
	 * What type of database access does this task require?
	 * 
	 * @return data access type required for this task
	 */
	public DataAccessType getDataAccessType() {
		return DataAccessType.NONE;
	}

	/**
	 * Get a table, either from tables or records based on the setting
	 * 
	 * @param dc
	 *            to which error message is added in case of missing table. null
	 *            if you do not care for that.
	 * @return table
	 * @throws ExilityException
	 *             in case table is not found
	 */
	protected TableInterface getTable(DataCollection dc)
			throws ExilityException {
		TableInterface table = null;
		if (this.recordName != null) {
			table = Records.getTable(this.recordName);
		} else {
			table = Tables.getTable(this.taskName, null);
		}

		if (table == null) {
			throw new ExilityException("Invalid table name in task "
					+ this.taskName + " with recordName set to "
					+ this.recordName);
		}
		return table;
	}
}

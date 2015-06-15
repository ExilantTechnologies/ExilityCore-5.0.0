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
 * wrapper on "task" classes so that they can be called from a service.
 * Objective of this wrapper is to define some common set of parameters that can
 * be passed to the tasks at run time
 */
class TaskStep extends AbstractStep {
	private static final String ORACLE_ISSUE_WITH_ROWS_AFFECTED = "task.ifNoWorkDone and task.ifWorkDone features does not work in case of table based operations";
	/**
	 * underlying worker. This is cached. Could be an issue if we move towards
	 * dynamic deployment etc..
	 */
	ExilityTask task = null;
	/**
	 * tasks return an integral value 0 means no work done, non-zero some
	 * indicator of what they did. DO you want to change the course of execution
	 * of service based on this outcome? Has to be a valid label in the service,
	 * or one of the standard commands like STOP
	 */
	String ifWorkDoneGoTo = AbstractStep.NEXT;
	/**
	 * tasks return an integral value 0 means no work done, non-zero some
	 * indicator of what they did. DO you want to change the course of execution
	 * of service based on this outcome? Has to be a valid label in the service,
	 * or one of the standard commands like STOP
	 */
	String ifNoWorkDoneGoTo = AbstractStep.NEXT;
	/***
	 * Should this task be submitted in a separate thread so that this service
	 * can continue without waiting for this task to complete? Also not that the
	 * work done by the submitted task is not part of this transaction, nor have
	 * you got any control back once it completes the job
	 */
	boolean submitAsBackgroundProcess = false;

	public TaskStep() {
		this.stepType = StepType.TASKSTEP;
	}

	/**
	 * concrete classes that implement an ExecutionStep will receive a
	 * taskContext and they have to implement execute method
	 */
	@Override
	String executeStep(DataCollection dc, DbHandle handle)
			throws ExilityException {
		if (this.task == null) {
			dc.addError("Error is service definition. TAsk step does not have a valid task");
			return AbstractStep.STOP;
		}

		/*
		 * submit to the background if required
		 */
		if (this.submitAsBackgroundProcess) {
			ServiceData sd = new ServiceData();
			dc.copyTo(sd);
			DataCollection newDc = new DataCollection();
			newDc.copyFrom(sd);
			TaskSubmitter submitter = new TaskSubmitter(newDc, this.task);
			Thread thread = new Thread(submitter);
			thread.start();
			Spit.out(this.label + " submitted ");
			return AbstractStep.NEXT;
		}

		int workDone = this.task.execute(dc, handle);

		/*
		 * this is a convention that is used by some logic. Work done is pushed
		 * to fields collection, so that subsequent steps can make use of that
		 */
		if (this.label != null) {
			dc.addIntegralValue(this.label + "WorkDone", workDone);
		}

		String nextStep = AbstractStep.NEXT;

		/*
		 * did the task do any work at all? TODO : There is an issue in Oracle.
		 * work done is -2 (for UNKNOWN) > hence this feature cannot be used. We
		 * have put that check into an exception as of now.
		 */
		if (this.ifNoWorkDoneGoTo != null) {
			if (workDone == 0) {
				nextStep = this.ifNoWorkDoneGoTo;
			} else if (workDone < 0) {
				/*
				 * affected rows in unknown
				 */
				throw new ExilityException(ORACLE_ISSUE_WITH_ROWS_AFFECTED);
			}
		}

		if (this.ifWorkDoneGoTo != null) {
			if (workDone > 0) {
				nextStep = this.ifWorkDoneGoTo;
			} else if (workDone < 0) {
				/*
				 * affected rows in unknown
				 */
				throw new ExilityException(ORACLE_ISSUE_WITH_ROWS_AFFECTED);
			}
		}

		return nextStep;
	}

	@Override
	public DataAccessType getDataAccessType(DataCollection dc) {
		if (this.task == null || this.submitAsBackgroundProcess) {
			return DataAccessType.NONE;
		}

		return this.task.getDataAccessType();
	}
}
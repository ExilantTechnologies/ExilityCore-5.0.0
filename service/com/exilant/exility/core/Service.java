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

/**
 * represents the core component that does THE job. One that implements a
 * service. It is important to keep in mind that the service is not a
 * transaction. Service can be used as part of different set of transactions.
 * This is not a common practice, and you may get confused about it in the
 * beginning. In that case, assume that there is a wrapper called serviceEntry
 * for every service that manages the transaction.
 * 
 * This approach gives us flexibility to deploy services in a variety of
 * deployment scenarios.
 */
public class Service implements ServiceInterface, ToBeInitializedInterface {
	private static final int NOT_IN_A_LOOP = -1;

	/**
	 * name should match the name of the file in which this is stored.
	 */
	String name = null;

	/**
	 * for organizing and annotation
	 */
	String module = null;

	/**
	 * for documentation
	 */
	String description = null;

	/**
	 * for documentation
	 */
	String techNotes = null;

	/**
	 * we keep the data access type as unknown rather than a default of
	 * read-only. This is to provide compatibility with existing services that
	 * did not bother about this field. If it is null, we try to infer this from
	 * steps.
	 */
	DataAccessType dataAccessType = null;

	/**
	 * Steps that this service consists of.
	 */
	AbstractStep[] steps = new AbstractStep[0];

	/*
	 * following attributes added to eliminate role of service entry all
	 * together.
	 */

	/**
	 * type of service, based on which we decide how to execute this service
	 */
	ServiceType serviceType;

	/**
	 * input fields/grids for this service
	 */
	InputRecord[] inputRecords = null;

	/**
	 * output fields and grids for this service
	 */
	OutputRecord[] outputRecords = null;

	/**
	 * index corresponding to a given step id is stored at the end of loading
	 * for performance sake.
	 */
	private final HashMap<String, Integer> stepIndexes = new HashMap<String, Integer>();

	/**
	 * in case the service has any design error
	 */
	private boolean inError = false;

	private boolean executeInBackground;

	/**
	 * 
	 */
	public Service() {
	}

	@Override
	public void execute(DataCollection dc, DbHandle handle)
			throws ExilityException {
		if (this.steps.length == 0 || this.inError) {
			dc.raiseException("Service " + this.name + " has design errors.");
			return;
		}

		/*
		 * A step will return a String that is the id of the task to be executed
		 * next. however, it may return specific strings to signal
		 * next/prev/last/stop
		 */
		String result;
		AbstractStep aStep;

		/*
		 * track when we are within loops
		 */
		int loopBeginsAt = Service.NOT_IN_A_LOOP;
		int loopEndsAt = Service.NOT_IN_A_LOOP;
		Spit.out("Service: " + this.name + " Steps:");
		LoopStepWorker worker = null;

		/*
		 * we loop. Default is from beginning to end, but watch-out for other
		 * traffic signals on the way...
		 */
		for (int stepIndex = 0; (stepIndex < this.steps.length)
				&& (stepIndex >= 0);) {
			/*
			 * If we are jumping out of an active loop, we have to inform the
			 * worker
			 */
			if (worker != null
					&& (stepIndex < loopBeginsAt || stepIndex > loopEndsAt)) {
				worker.jumpOut(dc);
				worker = null;
			}

			/*
			 * take the next step
			 */
			aStep = this.steps[stepIndex];
			Spit.out("Step " + stepIndex + " : "
					+ (aStep.label == null ? aStep.description : aStep.label));
			if (aStep.stepType == StepType.LOOPSTEP) {
				LoopStep loopStep = (LoopStep) aStep;
				if (worker == null) {
					/*
					 * we have just encountered a new loop
					 */
					worker = new LoopStepWorker((LoopStep) aStep, dc);
					loopBeginsAt = stepIndex;
					Integer si = this.stepIndexes
							.get(loopStep.lastStepOfTheBlock);
					if (si == null) {
						dc.raiseException("Service " + this.name
								+ " uses a non existing end label  "
								+ loopStep.lastStepOfTheBlock);
						return;
					}
					loopEndsAt = si.intValue();
				} else if (loopBeginsAt != stepIndex) {
					/*
					 * oops. this is another loop. We do not cater this as of
					 * now
					 */
					dc.raiseException("Service "
							+ this.name
							+ " has loop inside a loop. This is not implemented.");
					return;
				}

				if (worker.toContinue(dc)) {
					stepIndex++;
				} else {
					worker = null;
					stepIndex = loopEndsAt + 1;
				}

				continue;
			}

			/*
			 * execute the step
			 */
			result = aStep.execute(dc, handle);

			if (result.equals(AbstractStep.NEXT)) {
				/*
				 * "next" for the last step of a loop would be start of the loop
				 */
				if (worker != null && loopEndsAt == stepIndex) {
					stepIndex = loopBeginsAt;
				} else {
					stepIndex++;
				}
			} else if (result.equals(AbstractStep.PREVIOUS)) {
				stepIndex--;
			} else if (result.equals(AbstractStep.LAST)) {
				stepIndex = this.steps.length - 1;
			} else if (result.equals(AbstractStep.STOP)) {
				break;
			} else if (this.stepIndexes.containsKey(result)) {
				stepIndex = this.stepIndexes.get(result).intValue();
			} else {
				/*
				 * we should be throwing an error. But earlier code was just
				 * warning and moving on. We continue that to provide
				 * compatibility
				 */
				String s = "Step:"
						+ aStep.label
						+ " returned "
						+ result
						+ " but this is not an id of any step in this service. this is ignored, and execution continued with next step ";
				Spit.out(s);
				stepIndex++;
			}
		}
	}

	@Override
	public void initialize() {

		/*
		 * if this is a serviceLIst, we would like ensure that each step is a
		 * serviceStep
		 */
		boolean nonServiceStepFound = false;
		for (int i = 0; i < this.steps.length; i++) {
			AbstractStep step = this.steps[i];
			if (step == null) {
				Spit.out("Design error: "
						+ (i == 0 ? " first step " : (i + " th"))
						+ " step is null "
						+ this.steps[i - 1].getClass().getSimpleName());
				this.inError = true;
				continue;
			}

			if (nonServiceStepFound == false
					&& step instanceof ServiceStep == false) {
				nonServiceStepFound = true;
			}

			String label = step.label;
			if (label == null) {
				continue;
			}
			if (this.stepIndexes.containsKey(label)) {
				Spit.out("Design error: " + this.name
						+ " has more than one steps with label = " + label);
				this.inError = true;
				continue;
			}
			this.stepIndexes.put(label, new Integer(i));
		}
		if (this.serviceType == ServiceType.serviceList && nonServiceStepFound) {
			nonServiceStepFound = true;
			Spit.out("Design error: "
					+ this.name
					+ " should have only serviceStep as its step as it is marked as a serviceGroup");
			this.inError = true;
		}
	}

	/***
	 * returns data access requirement of this service. Used by design time
	 * component, and not at run time
	 * 
	 * @return ReadWrite, readOnly or none as data access type.
	 */
	@Override
	public DataAccessType getDataAccessType(DataCollection dc) {
		if (this.dataAccessType != null) {
			return this.dataAccessType;
		}

		boolean readRequired = false;
		for (AbstractStep step : this.steps) {
			DataAccessType thisType = step.getDataAccessType(dc);
			if (thisType == DataAccessType.READWRITE) {
				return DataAccessType.READWRITE;
			}

			if (thisType == DataAccessType.READONLY) {
				readRequired = true;
			}
		}

		if (readRequired) {
			return DataAccessType.READONLY;
		}

		return DataAccessType.NONE;
	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * return steps in this service
	 * 
	 * @return steps in this service
	 */
	public AbstractStep[] getSteps() {
		return this.steps;
	}

	/*
	 * methods added as part of re-factoring to merge serviceEntry functionality
	 * with service
	 */
	/**
	 * call for a top-level service to execute. It handles transaction.
	 * 
	 * @param inData
	 * @param outData
	 */
	@Override
	public void serve(ServiceData inData, ServiceData outData) {

		/**
		 * this is the top-level method for a service execution. We ensure that
		 * it returns ALWAYS!
		 */
		try {
			/*
			 * parse input data into dc
			 */
			DataCollection dc = new DataCollection();
			this.extractInData(inData, dc);
			if (dc.hasError()) {
				dc.copyMessages(outData);
				return;
			}

			DataAccessType accessType = this.getDataAccessType(dc);
			DbHandle handle = null;

			/*
			 * we are going to get a resource that MUST be returned. Better put
			 * a try-catch for safety
			 */
			try {
				if (accessType != DataAccessType.NONE
						&& accessType != DataAccessType.SERVICE_STEP_LEVEL) {
					handle = DbHandle.borrowHandle(accessType);

					if (accessType == DataAccessType.READWRITE) {
						handle.beginTransaction();
					} else if (accessType == DataAccessType.AUTOCOMMIT) {
						handle.startAutoCommit();
					}
				}
				/*
				 * we need another try-catch for decision on commit/roll-back
				 */
				try {
					this.execute(dc, handle);
				} catch (Throwable e) {
					Spit.out(e);
					dc.addError("Internal error on server :" + e.getMessage());
				}
				if (handle != null) {
					if (accessType == DataAccessType.READWRITE) {
						if (dc.hasError()) {
							Spit.out("Transaction rolled back due to an error.");
							handle.rollback();
						} else {
							handle.commit();
						}
					} else if (accessType == DataAccessType.AUTOCOMMIT) {
						handle.stopAutoCommit();
					}
				}
			} catch (Throwable e) {
				Spit.out(e);
				dc.addError(e.getMessage());
			} finally {
				if (handle != null) {
					DbHandle.returnHandle(handle);
				}
			}
			this.extractOutData(outData, dc);
		} catch (ServiceError error) {
			/*
			 * service error is managed by the components. they would have
			 * already put an appropriate message in dc
			 */
			Spit.out("Error while executing service " + this.name
					+ error.getMessage());
		} catch (Exception e) {
			Spit.out(e);
			outData.addError(e.getMessage());
		}
	}

	private void extractInData(ServiceData inData, DataCollection dc) {

		/*
		 * as of now, we still keep the input specification optional, though we
		 * insist in this in xml schema.
		 */
		if (this.inputRecords == null || this.inputRecords.length == 0) {
			dc.copyFrom(inData);
			return;
		}

		Record globalInput = Records.getGlobalInputRecord();
		if (globalInput != null) {
			globalInput.parseInputValues(inData, dc,
					InputRecordPurpose.selectiveUpdate, null);
		}

		for (InputRecord record : this.inputRecords) {
			record.extractInput(inData, dc);
		}

		/*
		 * and, of course, internal fields
		 */
		dc.copyInternalFieldsFrom(inData);
	}

	private void extractOutData(ServiceData outData, DataCollection dc) {
		/*
		 * we retain "all output options" internally, though in xml schema we
		 * insist that output record is a must
		 */
		if (this.outputRecords == null || this.outputRecords.length == 0) {
			dc.copyTo(outData);
			return;
		}
		Record globalOutput = Records.getGlobalOutputRecord();
		if (globalOutput != null) {
			globalOutput.extractOutputValues(outData, dc, null);
		}

		for (OutputRecord record : this.outputRecords) {
			record.extractOutput(outData, dc);
		}
		/*
		 * and of course, messages and other internal fields
		 */
		dc.copyInternalFieldsTo(outData);

	}

	/**
	 * is this service marked to be executed in the background. Typically,
	 * services that are expected to take more than few seconds are run in the
	 * background. However, this aspect has to be managed by the client
	 * properly, if the client UI is dependent on the outcome of the service
	 * 
	 * @return true if it to be run in the background
	 */
	@Override
	public boolean toBeRunInBackground() {
		return this.executeInBackground;
	}

	@Override
	public void executeAsStep(DataCollection dc, DbHandle handle)
			throws ExilityException {
		if (this.serviceType != ServiceType.procedureWithOwnTransaction) {
			this.execute(dc, handle);
			return;
		}
		DataAccessType access = this.getDataAccessType(dc);
		/*
		 * how do we know whether we are to suppress sqls? We have disabled it
		 * for the time being. Probably we need to re-factor the design for
		 * suppressing sql outputs
		 */
		DbHandle ownHandle = DbHandle.borrowHandle(access, true);
		if (access == DataAccessType.AUTOCOMMIT) {
			handle.startAutoCommit();
		} else if (access == DataAccessType.READWRITE) {
			handle.beginTransaction();
		}
		try {
			this.execute(dc, ownHandle);
		} catch (ExilityException e1) {
			// need not do anything
		} catch (Exception e) {
			dc.addError(e.getMessage());
		}
		if (this.dataAccessType == DataAccessType.READWRITE) {
			if (dc.hasError()) {
				ownHandle.rollback();
			} else {
				ownHandle.commit();
			}
		}
		DbHandle.returnHandle(ownHandle);
	}

	/*
	 * TODO : Batch is to be implemented
	 */
}

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

import java.util.Calendar;
import java.util.UUID;

/***
 * Exility design component that represents a name of service that client can
 * ask for. It describes how a service is to be completed.
 * 
 * TODO: this requires a re-factoring into two classes. As of now this class
 * handles functionality to be implemented on the middle (web/client) tier as
 * well as on the application layer. For example, inSpec is to be implemented
 * inside web-server, while the db connection etc.. would be on the pp layer.
 */
class ServiceEntry implements ToBeInitializedInterface {
	/***
	 * test harness can be requested by setting this field name to true
	 */
	public static final String TEST_CASE_FIELD_NAME = "_testCaseToBeSaved";
	/***
	 * Attributes to be extracted and sent to client for editing this design
	 * component inside studio
	 */
	private static final String[] ALL_ATTRIBUTES = { "name", "description",
			"allowAllInput", "allowAllOutput", "hasUserBasedSecurity",
			"dataAccessType", "isToBeCached" };

	/**
	 * name is unique within a file. This entry will be referred from client
	 * with a fully-qualified name. Folder names and the file name, separated'.'
	 * act as prefix for the name.
	 * 
	 */
	String name = null;

	/**
	 * Describe what this service does, and not how.
	 */
	String description = "";

	/***
	 * to be read in conjunction with spec name. Spec is not used for filtering
	 * input data if this is set to true.
	 */
	boolean allowAllInput = false;

	/***
	 * data spec is not used for filtering output if this field is set to true
	 */
	boolean allowAllOutput = false;

	/***
	 * Should access to this service be restricted to subset of authenticated
	 * users?
	 */
	boolean hasUserBasedSecurity = false;

	/***
	 * what kind of data base access does this service use? Setting the right
	 * value is critical to get the right transaction control for your service.
	 * If you set this to READONLY, then any attempt to do any update to db will
	 * result in exception. If you set this to READWRITE, all reads will be
	 * considered for update by default by the RDBMS, resulting in increased
	 * locks and performance overheads.
	 */
	DataAccessType dataAccessType = DataAccessType.READONLY;

	/***
	 * default is the service name itself, but we can reuse some generic specs
	 * across services
	 */
	String specName = null;

	/***
	 * Very useful for static data that is either completely static, or the
	 * service can live with slightly out dated data
	 */
	boolean isToBeCached;

	/***
	 * by default, entry name and service name are one and the same, but you can
	 * have it different for some specific reason
	 */
	String serviceName = null;

	/**
	 * should this job be submitted in the background?
	 */
	boolean submitAsBackgroundProcess = false;

	/**
	 * is this a java custom service?
	 */
	String fullyQualifiedClassName = null;

	/***
	 * main service used by callers. delivers the service
	 * 
	 * @param dc
	 * @throws ExilityException
	 */
	void serve(DataCollection dc) {
		boolean suppressLog = dc.hasValue(ExilityConstants.SUPPRESS_SQL_LOG);
		if (this.submitAsBackgroundProcess) {
			String jobId = this.name + "$" + UUID.randomUUID().toString();

			/**
			 * this jobId is to be pushed into this dc, as well as the dc of the
			 * submitted job.
			 */
			dc.addTextValue(CommonFieldNames.BACKGROUND_JOB_ID, jobId);
			/**
			 * we want to ensure that the new thread should carry its own data,
			 * and not interfere with dc. copying is via service data. This is
			 * not efficient, but serves the purpose for the time being
			 */
			ServiceData sd = new ServiceData();
			dc.copyTo(sd);
			DataCollection newDc = new DataCollection();
			newDc.copyFrom(sd);
			ServiceSubmitter submitter = new ServiceSubmitter(this, newDc);
			Thread thread = new Thread(submitter);
			thread.start();
			Spit.out(this.serviceName + " submitted ");
			return;
		}
		DbHandle handle = null;
		try {
			handle = DbHandle.borrowHandle(this.dataAccessType, suppressLog);
			this.execute(dc, handle);
		} catch (ExilityException e) {
			Spit.out(e);
			dc.addError(e.getMessage());
		} finally {
			DbHandle.returnHandle(handle);
		}
		return;
	}

	/***
	 * an intermediate method to allow sub classes to over-ride and still use
	 * the real method that is executeOnce()
	 * 
	 * @param dc
	 * @param handle
	 * @throws ExilityException
	 */

	protected void execute(DataCollection dc, DbHandle handle)
			throws ExilityException {
		ServiceInterface service = null;
		if (this.fullyQualifiedClassName != null) {
			try {
				Class<?> klass = Class.forName(this.fullyQualifiedClassName);
				Object instance = klass.newInstance();
				if (instance instanceof ServiceInterface) {
					service = (ServiceInterface) instance;
				}

			} catch (Exception e) {
				//
			}
			if (service == null) {
				dc.addError("Setup error : unable to load class "
						+ this.fullyQualifiedClassName + " for service "
						+ this.name + " as a ServiceInterface.");
				return;
			}

		} else {
			service = Services.getService(this.serviceName, dc);
		}
		this.executeOnce(dc, handle, service, null, this.dataAccessType);
	}

	/***
	 * worked method that does the real work
	 * 
	 * @param dc
	 * @param handle
	 * @param serviceToExecute
	 * @param taskToExecute
	 * @param accessType
	 * @throws ExilityException
	 */
	protected void executeOnce(DataCollection dc, DbHandle handle,
			ServiceInterface serviceToExecute, ExilityTask taskToExecute,
			DataAccessType accessType) throws ExilityException {
		/*
		 * for test harnessing
		 */
		boolean testCaseToBeSaved = dc.getBooleanValue(
				Constants.TEST_CASE_TO_BE_CAPTURED, false);

		StringBuilder inDc = null;
		long startedAt = 0;
		if (testCaseToBeSaved) {
			Spit.out(this.serviceName + " will be saved as a test case");
			inDc = new StringBuilder();
			startedAt = Calendar.getInstance().getTimeInMillis();
			dc.toSpreadSheetXml(inDc, "in_");
		}

		if (accessType == DataAccessType.READWRITE) {
			handle.beginTransaction();
		} else if (accessType == DataAccessType.AUTOCOMMIT) {
			handle.startAutoCommit();
		}

		try {
			if (taskToExecute != null) {
				taskToExecute.execute(dc, handle);
			}
			if (serviceToExecute != null) {
				serviceToExecute.execute(dc, handle);
			}
		} catch (Exception e) {
			if (e instanceof ExilityException == false
					|| ((ExilityException) e).messageToBeAdded) {
				Spit.out(e);
				dc.addError(e.getMessage());
				Spit.out("Service Returned with Exception : " + e.getMessage());
			}
		}
		if (accessType == DataAccessType.READWRITE) {
			if (dc.hasError()) {
				handle.rollback();
			} else {
				handle.commit();
			}
		} else if (accessType == DataAccessType.AUTOCOMMIT) {
			handle.stopAutoCommit();
		}

		if (testCaseToBeSaved) {
			this.saveTestCase(inDc, dc, startedAt);
		}

	}

	/**
	 * save test case using resource manager
	 * 
	 * @param inputDc
	 * @param outputDc
	 * @param startedAt
	 * @throws ExilityException
	 */
	private void saveTestCase(StringBuilder inputDc, DataCollection outputDc,
			long startedAt) throws ExilityException {
		String stamp = ResourceManager.getTimeStamp();
		String testFileName = ResourceManager.getTestCaseFolder()
				+ this.name.replace('.', '/') + "/test" + stamp + ".xml";

		/*
		 * crate a dummy testCase
		 */
		TestProcessor testCase = new TestProcessor();
		testCase.testId = stamp;
		testCase.description = "Test case captured";
		testCase.actualTimeInMs = Calendar.getInstance().getTimeInMillis()
				- startedAt;
		testCase.testCleared = outputDc.hasError();
		testCase.service = this.serviceName;

		/*
		 * crate a control sheet from this test case
		 */
		Grid controlSheet = new Grid();
		controlSheet.setRawData(testCase.getControlSheet());

		/*
		 * Write control sheet into spread sheet
		 */
		StringBuilder sbf = new StringBuilder(XlUtil.XL_BEGIN);
		controlSheet.toSpreadSheetXml(sbf, "_controlSheet");

		/*
		 * we had saved input dc
		 */
		sbf.append(inputDc);

		/*
		 * add output dc
		 */
		outputDc.toSpreadSheetXml(sbf, "out_");
		sbf.append(XlUtil.XL_END);
		ResourceManager.saveText(testFileName, sbf.toString());

	}

	/***
	 * service name may have to be qualified with the prefix
	 * 
	 * @param prefix
	 */
	public void qualifyServiceName(String prefix) {
		if (this.serviceName == null) {
			this.serviceName = this.name;
		}
		if (this.serviceName.indexOf(".") == -1) {
			this.serviceName = prefix + this.serviceName;
		}
	}

	@Override
	public void initialize() {
		if (this.serviceName == null) {
			this.serviceName = this.name;
		}
	}

	public String[] getLoadableAttributes() {
		return ALL_ATTRIBUTES;
	}
}

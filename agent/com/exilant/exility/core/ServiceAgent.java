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
import java.util.UUID;

/***
 * Sole agent to be approached for service from the app. This is the wrapper
 * class on the entire app. App functionality is delivered strictly thru this
 * one class. Agent prepares the right infrastructure for the service before
 * calling it. Other java classes running in the same app server CAN access
 * services directly, but they have to ensure that the infrastructure, like the
 * dbHandle, is taken care of. This has to be extended to listen to a message
 * and receive requests rather than being invoked as serve()
 * 
 * If the app has to be deployed on a separate layer behind a jms, then this
 * class needs to be made mdb.
 */
public class ServiceAgent {

	/**
	 * @return an agent. As of now this is nothing but new(), but we keep the
	 *         flexibility to have different type of agents
	 */
	public static ServiceAgent getAgent() {
		return new ServiceAgent();
	}

	/***
	 * Default constructor. Should e made private after checking with projects.
	 */
	public ServiceAgent() {
	}

	/***
	 * This method needs to be re-factored. Server side should not know how the
	 * data is going to be used. Whether a report is generated out of the data
	 * or a chart is being drawn, server call should be the same
	 * 
	 * @param serviceName
	 * @param userId
	 * @param inData
	 * @return
	 */
	DataCollection getReport(String serviceName, String userId,
			ServiceData inData) {
		DataCollection dc = new DataCollection();

		ServiceEntry entry = ServiceList.getServiceEntry(serviceName, true);
		if (entry == null) {
			Spit.out("ServiceAgent: no such service called" + serviceName);
			dc.addMessage("exilNoServiceName", serviceName);
			return dc;
		}

		this.fillDc(entry, dc, inData);
		entry.serve(dc);

		return dc;
	}

	/***
	 * This is THE method that gives access to the services hosted by the
	 * application server
	 * 
	 * @param serviceName
	 * @param userId
	 * @param inData
	 * @param outData
	 */
	public void serve(String serviceName, String userId, ServiceData inData,
			ServiceData outData) {
		/**
		 * this is the entry point to the app layer, in a distributed
		 * environment. However, in a simpler deployment, say a tomcat, then we
		 * could be in the same jvm as the client layer. Let us assume that the
		 * server layer trace starts here, but handle the scenario where client
		 * layer would have already started trace. We do this by saving any text
		 * in trace, and put it back after we are done.
		 */
		String existingTraceText = Spit.startWriter();

		Date startTime = new Date();
		this.serveWorker(serviceName, userId, inData, outData);
		Date endTime = new Date();
		long diffTime = endTime.getTime() - startTime.getTime();
		String traceText = Spit.stopWriter() + "\nTime taken by Service "
				+ serviceName + " is : " + diffTime + " milliseconds";

		/*
		 * put back original trace text on logger
		 */
		if (existingTraceText != null) {
			Spit.startWriter();
			Spit.out(existingTraceText);
		}
		Spit.writeServiceLog(traceText, userId, serviceName);
		/*
		 * if client wants trace text..
		 */
		String traceNeeded = inData.getValue(CommonFieldNames.TRACE_REQUEST);
		if (traceNeeded != null) {
			outData.addValue(CommonFieldNames.TRACE_TEXT, traceText);
		}
	}

	/***
	 * run the service and get results. Requires re-factoring to avoid license
	 * utility etc.. as this should be called from another internal routine that
	 * would have already gone thru security checks
	 * 
	 * @param serviceName
	 * @param userId
	 * @param inData
	 * @param outData
	 */
	private void serveWorker(String serviceName, String userId,
			ServiceData inData, ServiceData outData) {
		if (!this.licenseCleared(outData)) {
			return;
		}
		DataCollection dc = new DataCollection();
		try {

			/*
			 * list service does not have user based security or spec
			 */
			if (serviceName.equals(CommonFieldNames.LIST_SERVICE_ID)) {
				dc.copyFrom(inData);
				ListService.getService().execute(dc);
				dc.copyTo(outData);
				return;
			}

			/*
			 * report service also does not have security or spec
			 */
			if (serviceName.equals("reportService")) {
				ReportService service = ReportService.getService();
				dc.copyFrom(inData);
				service.execute(dc);
				dc.copyTo(outData);
				return;
			}

			ServiceEntry entry = ServiceList.getServiceEntry(serviceName, true);

			if (entry == null) {
				Spit.out("ServiceAgent: No service entry found for "
						+ serviceName);
				outData.addMessage("exilNoServiceName", serviceName);
				return;
			}

			if (entry.hasUserBasedSecurity
					&& !UserBasedSecurityAgent.isCleared(userId, serviceName,
							outData)) {
				/*
				 * security agent would have taken care of the error message
				 */
				return;
			}

			ServiceSpec spec = this.fillDc(entry, dc, inData);
			entry.serve(dc);

			if (entry.allowAllOutput || spec == null) {
				dc.copyTo(outData);
			} else {
				spec.translateOutput(dc, outData);
			}

		} catch (Exception e) {
			Spit.out(e);
			dc.addError(e.getMessage());
			dc.copyMessages(outData);
		}
		return;
	}

	/***
	 * Fill dc from input data. As a small optimization, it returns the spec, if
	 * found, that can be used for output
	 * 
	 * @param entry
	 * @param dc
	 * @param inData
	 * @return
	 * @throws ExilityException
	 */
	private ServiceSpec fillDc(ServiceEntry entry, DataCollection dc,
			ServiceData inData) {
		ServiceSpec spec = null;
		String specName = entry.name;
		if (entry.specName != null) {
			specName = entry.specName;
		}

		if (entry.allowAllInput == false || entry.allowAllOutput == false) {
			spec = ServiceSpecs.getServiceSpec(specName, dc);
			if (spec == null || spec.name == null) {
				Spit.out("Warning: Spec " + specName
						+ " not defined. AllowAll Assumed for service "
						+ entry.name);
			}
		}

		if (entry.allowAllInput || spec == null) {
			dc.copyFrom(inData);
		} else {
			spec.translateInput(inData, dc);
		}
		return spec;
	}

	/***
	 * way to invoke a service from another internal class when it already has
	 * dc and db-handle
	 * 
	 * @param serviceName
	 * @param dc
	 * @param handle
	 */
	void serve(String serviceName, DataCollection dc, DbHandle handle) {
		/**
		 * we are to just execute a service, but if the service has an
		 * associated spec, then we will have to create new dc and ensure that
		 * we respect the specs :-)
		 */
		DataCollection newDc = dc;

		ServiceEntry entry = ServiceList.getServiceEntry(serviceName, true);
		String specName = (entry.specName == null) ? serviceName
				: entry.specName;
		ServiceSpec spec = ServiceSpecs.getServiceSpec(specName, dc);
		if (entry.allowAllInput == false && spec != null && spec.inSpec != null) {
			newDc = new DataCollection(dc);
			spec.inSpec.translate(dc, newDc);
		}

		try {
			ServiceInterface service = Services.getService(serviceName, dc);
			service.execute(newDc, handle);
		} catch (Exception e) {
			dc.addError(e.getMessage());
			Spit.out(e);
		}

		if (entry.allowAllInput == false && entry.allowAllOutput == false
				&& spec != null && spec.outSpec != null) {
			spec.outSpec.translate(newDc, dc);
		}
	}

	/**
	 * check for licensing
	 * 
	 * @param outData
	 * @return
	 */
	private boolean licenseCleared(ServiceData outData) {
		if (!AP.licenceValidation) {
			return true;
		}
		try {
			LicenseUtility licUtility = new LicenseUtility();
			if (!licUtility.isValidLicense(true)) {
				outData.addMessage(
						Message.EXILITY_ERROR,
						"The license is invalid. Please Contact the Exility Support Team to get a valid License.");
				return false;
			}
			long daysLeft = licUtility.getDaysLeft();
			if (daysLeft < 10) {
				outData.addMessage(
						"warning",
						"The license is valid for only "
								+ daysLeft
								+ " days. Please Contact the Exility Support Team to extend the License.");
			}
		} catch (Exception ex) {
			outData.addError(ex.getMessage());
			return false;
		}
		return true;
	}

	/*
	 * methods for the re-factored design. We have retained existing design to e
	 * used by existing applications,and created entirely new set of methods
	 * for. We considered creating a different class, but we ServieAgent is an
	 * apt name we didn't want to replace. So, we invented new method names
	 * instead.
	 */

	/***
	 * replaces serve(). Created a a separate method to ensure that we serve
	 * existing code well, and develop this new one without any baggage. this
	 * method is optimized for different deployment scenarios, like micro
	 * service, cloud-based etc.. This method is the entry point into
	 * application layer. This can be wrapped as an mdb to make the application
	 * message-driven, or can be called directly by client layer without the
	 * over-heads of messaging, or can be called by alternate managers like
	 * LMAX.
	 * 
	 * @param serviceName
	 * @param userId
	 * @param inData
	 *            has all the data sent by end-client, as well as global
	 *            (context or state) fields maintained by the client layer (like
	 *            http session)
	 * @return outData output of the service, including any global fields that
	 *         are common across all services.
	 * 
	 */
	public ServiceData executeService(String serviceName, String userId,
			ServiceData inData) {
		/**
		 * this is the entry point to the app layer, in a distributed
		 * environment. However, in a simpler deployment, say a tomcat or jboss,
		 * then we could be in the same jvm as the client layer. Let us assume
		 * that the server layer trace starts here, but handle the scenario
		 * where client layer would have already started trace. We do this by
		 * saving any text in trace, and put it back after we are done.
		 */
		String existingTraceText = Spit.startWriter();
		ServiceData outData = new ServiceData();
		Date startTime = new Date();
		try {
			/*
			 * list service does not have user based security or spec. We should
			 * think about some generic/basic parser for list and report service
			 * requests
			 */
			if (serviceName.equals(CommonFieldNames.LIST_SERVICE_ID)) {
				DataCollection dc = new DataCollection();
				dc.copyFrom(inData);
				ListService.getService().execute(dc);
				dc.copyTo(outData);
			} else if (serviceName.equals(CommonFieldNames.REPORT_SERVICE_ID)) {
				/*
				 * report service also does not have security or spec
				 */
				ReportService service = ReportService.getService();
				DataCollection dc = new DataCollection();
				dc.copyFrom(inData);
				service.execute(dc);
				dc.copyTo(outData);
			} else {
				/*
				 * who should check for user based security? Good question.
				 * "It depends on the project" is a convenient answer. So far,
				 * no one has asked for it. We have delegated it to
				 * Services.getService() as of now.
				 */
				ServiceInterface service = Services.getService(serviceName,
						userId);

				if (service == null) {
					String msg = serviceName + " is not available for user "
							+ userId;
					Spit.out(msg);
					outData.addError(msg);
				} else {
					boolean toBeRunInBackground = false;
					String t = inData
							.getValue(CommonFieldNames.TO_BE_RUN_IN_BACKGROUND);
					if (t != null && t.endsWith("1")) {
						toBeRunInBackground = true;
					} else {
						toBeRunInBackground = service.toBeRunInBackground();
					}
					if (toBeRunInBackground) {
						this.submitService(service, inData, outData, userId);
					} else {
						service.serve(inData, outData);
					}
				}
			}
		} catch (Exception e) {
			/*
			 * this method being THE way to get the service from application
			 * layer, we are using this as catch-all
			 */
			outData.addError("There was an internal error that has been communicated to the support team. Someone will look into that. Meanwhile, you may try your luck by just trying the transaciton once again.");
		}
		Date endTime = new Date();
		long diffTime = endTime.getTime() - startTime.getTime();
		String traceText = Spit.stopWriter() + "\nTime taken by Service "
				+ serviceName + " is : " + diffTime + " milliseconds";

		/*
		 * put back original trace text on logger
		 */
		if (existingTraceText != null) {
			Spit.startWriter();
			Spit.out(existingTraceText);
		}
		Spit.writeServiceLog(traceText, userId, serviceName);
		/*
		 * if client wants trace text..
		 */
		String traceNeeded = inData.getValue(CommonFieldNames.TRACE_REQUEST);
		if (traceNeeded != null) {
			outData.addValue(CommonFieldNames.TRACE_TEXT, traceText);
		}
		return outData;
	}

	private void submitService(ServiceInterface service, ServiceData inData,
			ServiceData outData, String userId) {
		String jobId = service.getName() + "$" + UUID.randomUUID().toString();

		/**
		 * this jobId is to be pushed into this dc, as well as the dc of the
		 * submitted job.
		 */
		inData.addValue(CommonFieldNames.BACKGROUND_JOB_ID, jobId);
		outData.addValue(CommonFieldNames.BACKGROUND_JOB_ID, jobId);

		/*
		 * clone inData for the new thread, unless you want to get into trouble
		 * of course :-)
		 */
		ServiceData newInData = new ServiceData();
		newInData.extractData(inData);
		BackgroundService bs = new BackgroundService(userId, service, inData,
				jobId);
		Thread thread = new Thread(bs);
		thread.start();
		Spit.out("job " + jobId + " submitted.");
	}

	/**
	 * save test case using resource manager
	 * 
	 * @param inputDc
	 * @param outputDc
	 * @param startedAt
	 * @throws ExilityException
	 */
	@SuppressWarnings("unused")
	private void saveTestCase(ServiceData inData, ServiceData outData) {
		// Should be delegated
	}
}
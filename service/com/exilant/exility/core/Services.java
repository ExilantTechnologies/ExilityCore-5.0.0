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

import com.exilant.exility.ide.IdeServices;
import com.exilant.exility.wf.Workflow;

/**
 * manages service components
 * 
 */
public class Services {
	private static final HashMap<String, ServiceInterface> services = new HashMap<String, ServiceInterface>();
	/**
	 * work-flows have been implemented as services. To simplify usage, we have
	 * standardized on workflow as prefix for service names. Any service name
	 * that starts with "workflow." is assumed to be a workflow
	 */
	public static final String WORKFLOW_SERVICE_PREFIX = "workflow.";
	/**
	 * all ide services start with "ide."
	 */
	private static final String IDE_SERVICE_PREFIX = "ide.";

	/***
	 * returns an instance of Service for the supplied name. Service is cached
	 * based on the application settings
	 * 
	 * @param serviceName
	 *            fully qualified service name. (like moduleName.serviceName)
	 * @param dc
	 * @return instance of service, or null if it is not found
	 */
	public static ServiceInterface getService(String serviceName,
			DataCollection dc) {
		ServiceInterface service = Services.services.get(serviceName);
		if (service != null) {
			return service;
		}

		Object resource = ResourceManager.loadResource(
				"service." + serviceName, Service.class);
		if (resource == null) {
			if (serviceName.startsWith(IDE_SERVICE_PREFIX)) {
				Spit.out("Going to try " + serviceName + " as an IDE service.");
				/*
				 * ide service is not cached
				 */
				return IdeServices.getService(serviceName);
			}
			/*
			 * Is it a workflow?
			 */
			if (serviceName.startsWith(Services.WORKFLOW_SERVICE_PREFIX)) {
				Spit.out("Going to try " + serviceName
						+ " as a wrokflow service.");
				resource = ResourceManager.loadResource(serviceName,
						Workflow.class);
			}
		}

		if (resource == null) {
			return null;
		}

		if (resource instanceof ServiceInterface == false) {
			Spit.out(serviceName
					+ " is found as a resource but it is not a resource.");
			return null;
		}

		service = (ServiceInterface) resource;
		if (serviceName.endsWith(service.getName()) == false) {
			Spit.out("File name " + serviceName
					+ " has a service with its name wrongly set to "
					+ service.getName());
			return null;
		}
		if (AP.definitionsToBeCached) {
			Services.services.put(serviceName, service);
		}

		return service;
	}

	static void saveService(Service service) {
		ResourceManager.saveResource(service.name, service);
	}

	static void saveWorkflow(Workflow workflow) {
		ResourceManager.saveResource(workflow.getName(), workflow);
	}

	static void flush() {
		Services.services.clear();
	}

	/**
	 * check for access control for this user to the service before returning
	 * it. Also, this uses Service instead of ServiceInterface.
	 * 
	 * @param serviceName
	 * @param userId
	 * @return service if found, and the user is authorized to use this service.
	 *         null otherwise;
	 */
	public static ServiceInterface getService(String serviceName, String userId) {
		/**
		 * user based security is not yet designed. It is free for all right
		 * now.
		 */
		DataCollection dc = null;
		return getService(serviceName, dc);
	}

}
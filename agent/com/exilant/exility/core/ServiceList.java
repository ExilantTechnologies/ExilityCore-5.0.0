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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/***
 * Manages instances of ServiceEntry design components.
 * 
 */
public class ServiceList {
	/***
	 * singleton to offer normal methods rather than static ones
	 */
	static ServiceList instance = new ServiceList();
	/***
	 * to avoid concurrent loading by two threads
	 */
	private final static ReentrantLock lock = new ReentrantLock();

	/***
	 * way to cache entries that are read
	 */
	Map<String, ServiceEntry> serviceEntries = new HashMap<String, ServiceEntry>();

	ServiceList() {
	}

	/***
	 * get a service entry for the service. If definition is not found, a
	 * default is created for the service, provided the service exists.
	 * 
	 * @param serviceName
	 * @param createIfNotFound
	 * @return
	 */
	static ServiceEntry getServiceEntry(String serviceName,
			boolean createIfNotFound) {
		if (ServiceList.instance == null) {
			ServiceList.load();
		}

		ServiceEntry entry = ServiceList.instance.serviceEntries
				.get(serviceName);
		if (entry == null && createIfNotFound) {
			entry = ServiceList.getTempServiceEntry(serviceName);
			if (entry != null) {
				ServiceList.addServiceEntry(serviceName, entry);
			}
		}

		return entry;

	}

	/***
	 * flush the cache and reload entries
	 * 
	 * @param removeExistingEntries
	 */
	static void reload(boolean removeExistingEntries) {
		ServiceList.lock.lock();
		if (ServiceList.instance == null || removeExistingEntries) {
			ServiceList.instance = new ServiceList();
		}
		ServiceList.loadUnderLockAndKey();
		ServiceList.lock.unlock();
	}

	/***
	 * load entries
	 */
	private static void load() {
		ServiceList.lock.lock();

		if (ServiceList.instance == null) {
			ServiceList.instance = new ServiceList();
			ServiceList.loadUnderLockAndKey();
		}

		ServiceList.lock.unlock();
	}

	/***
	 * serialized load to avoid duplicate loads
	 */
	private static void loadUnderLockAndKey() {
		try {
			Map<String, Object> sls = ResourceManager.loadFromFileOrFolder(
					"serviceList", "serviceEntry", ".xml");
			for (String fileName : sls.keySet()) {
				Object obj = sls.get(fileName);
				if (obj instanceof ServiceList == false) {
					Spit.out("serviceEntry folder contains an xml that is not a serviceList. File ignored");
					continue;
				}
				ServiceList sl = (ServiceList) obj;
				ServiceList.instance.copyFrom(sl, fileName);
			}
			Spit.out(ServiceList.instance.serviceEntries.size()
					+ " service entries loaded");
		} catch (Exception e) {
			Spit.out("Unable to load Servicelists....");
		}
	}

	/***
	 * used by loader to accumulate entries from individual files
	 * 
	 * @param sl
	 *            service list to copy entries from. Note that the entries will
	 *            be cached with a qualified name
	 * @param fileName
	 */
	private void copyFrom(ServiceList sl, String fileName) {
		Spit.out("Going to copy serviceEntries from " + fileName);
		String prefix = fileName.length() == 0 ? "" : fileName + '.';
		for (ServiceEntry se : sl.serviceEntries.values()) {
			String qualifiedName = prefix + se.name;
			if (this.serviceEntries.containsKey(qualifiedName)) {
				Spit.out("Error : message "
						+ qualifiedName
						+ " is defined more than once"
						+ (prefix.length() == 0 ? "." : (" in " + prefix + ".")));
				continue;
			}
			/*
			 * historical problem: serviceEntry name was used as serviceName,
			 * but once we introduced qualified name for entry...
			 */
			se.qualifyServiceName(prefix);
			this.serviceEntries.put(qualifiedName, se);
		}
	}

	/***
	 * Add an entry to the cached list of entries
	 * 
	 * @param serviceName
	 * @param entry
	 */
	static void addServiceEntry(String serviceName, ServiceEntry entry) {
		if (ServiceList.instance == null) {
			ServiceList.load();
		}
		ServiceList.instance.serviceEntries.put(serviceName, entry);
	}

	/***
	 * create a temporary entry for the service. Used when project has not
	 * defined a service entry but the service is defined
	 * 
	 * @param serviceName
	 * @return temp entry for this service
	 */
	static ServiceEntry getTempServiceEntry(String serviceName) {

		DataCollection dc = null;
		ServiceInterface service = Services.getService(serviceName, dc);
		if (service == null) {
			return null;
		}
		ServiceEntry entry = new ServiceEntry();
		entry.name = serviceName;
		entry.serviceName = serviceName;
		entry.allowAllInput = true;
		entry.allowAllOutput = true;
		entry.dataAccessType = service.getDataAccessType(null);
		entry.hasUserBasedSecurity = true;
		entry.initialize();
		return entry;
	}

	/***
	 * Using the design pattern to provide normal methods rather than static
	 * methods
	 * 
	 * @return
	 */
	static ServiceList getInstance() {
		return ServiceList.instance;
	}

	/**
	 * get all entries
	 * 
	 * @return sorted array of all entries
	 */
	public static String[] getAllEntries() {
		String[] allOfThem = getInstance().serviceEntries.keySet().toArray(
				new String[0]);
		Arrays.sort(allOfThem);
		return allOfThem;
	}
}

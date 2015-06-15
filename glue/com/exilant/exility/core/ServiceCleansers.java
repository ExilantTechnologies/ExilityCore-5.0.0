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
import java.util.Map;

/***
 * This class needs to be re-factored to make it simpler. There is provision for
 * only one class, and hence why this collection etc???
 * 
 */
public class ServiceCleansers {
	private static ServiceCleansers singleton = null;

	Map<String, ServiceCleanserInterface> cleansers = new HashMap<String, ServiceCleanserInterface>();

	/***
	 * ask by name, but AP has provision for only one name!!
	 * 
	 * @param cleanserName
	 * @return an instance
	 */
	public static ServiceCleanserInterface getCleanser(String cleanserName) {
		if ((cleanserName == null) || (cleanserName.length() == 0)) {
			return null;
		}

		if (ServiceCleansers.singleton == null) {
			ServiceCleansers.getInstance();
		}

		ServiceCleanserInterface cleanser = null;
		if (!ServiceCleansers.singleton.cleansers.containsKey(cleanserName)) {
			cleanser = (ServiceCleanserInterface) ObjectManager
					.createNew(AP.cleanserAssemblyName, AP.cleanserNameSpace,
							cleanserName);
			ServiceCleansers.singleton.cleansers.put(cleanserName, cleanser);
		}

		else {
			cleanser = ServiceCleansers.singleton.cleansers.get(cleanserName);
		}

		return cleanser;
	}

	/**
	 * get an instance of service cleanser
	 * 
	 * @return
	 */
	static ServiceCleansers getInstance() {
		if (ServiceCleansers.singleton == null) {
			ServiceCleansers.singleton = new ServiceCleansers();
		}
		return ServiceCleansers.singleton;
	}
}

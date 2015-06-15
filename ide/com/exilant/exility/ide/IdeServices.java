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
package com.exilant.exility.ide;

import com.exilant.exility.core.CustomCodeInterface;
import com.exilant.exility.core.ServiceInterface;
import com.exilant.exility.core.Spit;

/**
 * Manages instances of IDE services
 * 
 */
public class IdeServices {

	private static final String packageName = "com.exilant.exility.ide.";

	/**
	 * get a service instance.
	 * 
	 * @param serviceName
	 * @return null if no service is defined
	 */

	public static ServiceInterface getService(String serviceName) {
		Spit.out("Going to try " + serviceName + " as a service.");
		if (serviceName.startsWith("ide.") == false) {
			return null;
		}
		String className = serviceName.substring(4, 5).toUpperCase()
				+ serviceName.substring(5);
		Spit.out("Going to try " + className + " as a class under ide package.");
		try {
			CustomCodeInterface ide = (CustomCodeInterface) Class.forName(
					packageName + className).newInstance();
			return new IdeService(serviceName, ide);
		} catch (Exception e) {
			Spit.out(serviceName + " is not an ide service. " + e.getMessage());
		}
		return null;
	}
}

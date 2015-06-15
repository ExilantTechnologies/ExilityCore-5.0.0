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

import javax.servlet.http.HttpServletRequest;

/***
 * to be re-factored
 * 
 */
public interface ServiceCleanserInterface {
	/**
	 * method that is invoked before any service is executed
	 * 
	 * @param req
	 * @param data
	 * @return true if all OK, false otherwise. caller needs to abandon service
	 *         if returned value is false
	 */
	boolean cleanseBeforeService(HttpServletRequest req, ServiceData data);

	/**
	 * method that is invoked after service is executed.
	 * 
	 * @param req
	 * @param data
	 * @return true if all OK. if false is returned, this method would have
	 *         added error message into out data
	 */
	boolean cleanseAfterService(HttpServletRequest req, ServiceData data);
}

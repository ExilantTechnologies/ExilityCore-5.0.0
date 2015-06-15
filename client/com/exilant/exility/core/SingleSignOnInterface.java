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

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Interface to be implemented by a class that is used for single-sign-on. Such
 * a class should have its instance registered with httpRequestHandler using
 * setSignoOnObject() method
 * 
 */
public interface SingleSignOnInterface {

	/**
	 * @param request
	 *            http request object
	 * @param response
	 *            http response object
	 * @param inData
	 *            source of any data that was expected from client. This is
	 *            created by request handler before calling this method
	 * @param outData
	 *            data to be sent back to client. messages can be added to this.
	 *            userId field MUST be populated if login is successful
	 * @param sessionData
	 *            to be used to push any session variable after a successful
	 *            login
	 * @return true if login was successful, and the userId is pushed to
	 *         outData. False if login failed, and an appropriate message is
	 *         pushed to outData
	 * 
	 */
	public boolean signIn(ServletRequest request, ServletResponse response,
			ServiceData inData, ServiceData outData, SessionData sessionData);
}

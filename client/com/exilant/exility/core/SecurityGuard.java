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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/***
 * It is all in the name !! security guard for an HTTP request. Functionality
 * from here is in-lined into httpRequestHandler. This is used only by the old
 * version htmlrequestHandler
 */
public class SecurityGuard {
	/***
	 * to be re-factored into enumerations
	 */
	public static final int CLEARED = 0;
	/**
	 * we find userId in cookie, but not in session
	 */
	public static final int SESSIONEXPIRED = 1;

	/**
	 * userId cookie is not found
	 */
	public static final int NOTLOGGEDIN = 2;

	/**
	 * userId mismatch between cookie and session. S
	 */
	public static final int HACKERATTEMPT = 5;

	/**
	 * field in dc that has the authentication status
	 */
	public static final String AUTHENTICATION_FIELD_NAME = "authenticationStatus";

	private static final SecurityGuard singletonInstance = new SecurityGuard();

	private SecurityGuard() {
	}

	/***
	 * get an instance.
	 * 
	 * @return instance
	 */
	public static SecurityGuard getGuard() {
		return SecurityGuard.singletonInstance;
	}

	/***
	 * do thorough security check
	 * 
	 * @param request
	 * @param outData
	 *            message for any security failure is put into this
	 * @return true if security cleared
	 */
	public boolean cleared(HttpServletRequest request, ServiceData outData) {
		int authenticationStatus = this.authenticate(request, outData);
		outData.addValue(SecurityGuard.AUTHENTICATION_FIELD_NAME,
				Integer.toString(authenticationStatus));
		if (authenticationStatus == SecurityGuard.CLEARED) {
			return true;
		}
		return false;
	}

	/***
	 * worker method for authentication
	 * 
	 * @param request
	 * @param outData
	 * @return 0 if cleared, or one of the constants defined at the class level
	 */
	int authenticate(HttpServletRequest request, ServiceData outData) {

		if (!AP.securityEnabled) {
			Object o = request.getSession().getAttribute(
					AP.loggedInUserFieldName);
			if (o == null) {
				return SecurityGuard.SESSIONEXPIRED;
			}
			return SecurityGuard.CLEARED;
		}

		// hurdle 1: request should come from a page, and not be typed on
		// address bar
		/*
		 * commented referrer part as per bhandi's suggestion. // url would be
		 * null if some one typed it from the adress bar. String refUrl =
		 * request.getHeader("referer"); // Dec 31 2009 : Bug 841 - Regarding
		 * Session Timeout - SAB Miller: Venkat if (refUrl == null) {
		 * outData.addMessage("exilNoReferrer"); return
		 * SecurityGuard.NOREFERRER; }
		 * 
		 * // hurdel 2: referrer should be the same as this host String reqHost
		 * = request.getServerName(); URI uri = null; String refHost = null; try
		 * { uri = new URI(refUrl); refHost = uri.getHost(); } catch (Exception
		 * e) { //we just tried }
		 * 
		 * if (!reqHost.equals(refHost)) { outData.addMessage("exilNoReferrer");
		 * return SecurityGuard.NOTMYHOST; }
		 */
		// hurdle 3: User should have logged in
		// If user is logged in, we will get the user id in cookies, and a
		// matching entry in session
		// check userId from session.
		Object o = request.getSession().getAttribute(AP.loggedInUserFieldName);
		if (o == null) {
			outData.addMessage("exilSessionExpired");
			return SecurityGuard.SESSIONEXPIRED;
		}

		Cookie c = null;
		Cookie reqCookie[] = request.getCookies();
		if (reqCookie != null) {
			for (Cookie cookie : reqCookie) {
				if (cookie.getName().equals(AP.loggedInUserFieldName)) {
					c = cookie;
					break;
				}
			}
		}
		if (c == null) {
			outData.addMessage("exilNotLoggedIn");
			return SecurityGuard.NOTLOGGEDIN;
		}

		String cookieUserID = c.getValue();

		String sessionUserID = o.toString();
		if (!cookieUserID.equals(sessionUserID)) {
			outData.addMessage("exilNotLoggedIn");
			return SecurityGuard.HACKERATTEMPT;
		}

		return SecurityGuard.CLEARED;
	}

	/**
	 * security implemented thru CSRF-token rather than cookie
	 * 
	 * @param request
	 * @param inData
	 * @param outData
	 * @return true if we cleared it all
	 */
	public boolean cleared(HttpServletRequest request, ServiceData inData,
			ServiceData outData) {
		HttpSession session = request.getSession(true);
		boolean allOk = false;

		// check if we are using CSRF-token
		String token = request.getHeader(CommonFieldNames.CSRF_HEADER);
		if (token == null) {
			token = inData.getValue(CommonFieldNames.CSRF_HEADER);
		}
		Spit.out("CSRF = " + token);
		if (token != null) {
			allOk = session.getAttribute(token) != null;
			Spit.out("Security cleared with " + allOk);
		} else {
			/**
			 * uses cookie for authentication
			 */
			Cookie c = null;
			Cookie reqCookie[] = request.getCookies();
			if (reqCookie != null) {
				for (Cookie cookie : reqCookie) {
					if (cookie.getName().equals(AP.loggedInUserFieldName)) {
						c = cookie;
						break;
					}
				}
			}
			if (c == null) {
				allOk = false;
			} else {
				String sessionUserId = c.getValue();
				Object o = session.getAttribute(AP.loggedInUserFieldName);
				if (o != null && sessionUserId.equals(o.toString())) {
					allOk = true;
				} else {
					allOk = false;
				}
			}
		}
		if (!allOk) {
			outData.addMessage("exilNotLoggedIn");
		}
		return allOk;
	}
}
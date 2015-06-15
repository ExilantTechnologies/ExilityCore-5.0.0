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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Workbook;

/***
 * main class that receives a request from the web-server and processes it
 * Superseded by HttpRequestHandler.
 * 
 */
public class HtmlRequestHandler {
	private static String getUserIdName() {
		return AP.loggedInUserFieldName;
	}

	private static String getLoginServiceName() {
		return AP.loginServiceId;
	}

	private static String getLogoutServiceNamr() {
		return AP.logoutServiceId;
	}

	/**
	 * field naame prefix to be used for global fields
	 */
	public static final String GLOBAL_SERVER_DATA_NAME = "sessionData";
	private static final String PAGINATION_SERVICE_ID = "paginationService";
	private static final String PAGINATION_SERVICE_FIELD_NAME = "paginationServiceName";
	private static final String SORT_COLUMN = "paginationColumnToSort";
	private static final String SORT_DESC = "paginationSortDesc";
	private static final String MATCH_COLUMN = "paginationColumnToMatch";
	private static final String MATCH_VALUE = "paginationValueToMatch";
	private static final String PAGE_SIZE = "PageSize";
	private static final Object LIST_SERVICE = "listService";
	/**
	 * path prefix for all uploaded files
	 */
	public static final Object PATH_SUFFIX = "__ExilityFilePath";
	private static final String GLOBAL_FIELDS_NAME = "exilityGlobalFields";
	/**
	 * should we use new version of agent? temp fix till every one migrates to
	 * new agent
	 */
	public static boolean useNewServiceAgent = false;

	private static HtmlRequestHandler singletonInstance = new HtmlRequestHandler();
	private static boolean suppressSqlLog;

	/***
	 * get an instance to invoke required methods
	 * 
	 * @return an instance with which to work. This is implemented as a
	 *         singleton as of now, but you caller not make that assumption
	 */
	public static HtmlRequestHandler getHandler() {
		return HtmlRequestHandler.singletonInstance;
	}

	private HtmlRequestHandler() {
	}

	/**
	 * Extract data from request object (form, data and session)
	 * 
	 * @param req
	 * @param formIsSubmitted
	 * @param hasSerializedDc
	 * @param outData
	 * @return all input fields into a service data
	 * @throws ExilityException
	 */
	@SuppressWarnings("resource")
	public ServiceData createInData(HttpServletRequest req,
			boolean formIsSubmitted, boolean hasSerializedDc,
			ServiceData outData) throws ExilityException {

		ServiceData inData = new ServiceData();
		if (formIsSubmitted == false) {
			/**
			 * most common call from client that uses serverAgent to send an
			 * ajax request with serialized dc as data
			 */
			this.extractSerializedData(req, hasSerializedDc, inData);
		} else {
			/**
			 * form is submitted. this is NOT from serverAgent.js. This call
			 * would be from other .jsp files
			 */
			if (hasSerializedDc == false) {
				/**
				 * client has submitted a form with form fields in that.
				 * Traditional form submit
				 **/
				this.extractParametersAndFiles(req, inData);
			} else {
				/**
				 * Logic got evolved over a period of time. several calling jsps
				 * actually inspect the stream for file, and in the process they
				 * would have extracted form fields into session. So, we extract
				 * form fields, as well as dip into session
				 */
				HttpSession session = req.getSession();
				if (ServletFileUpload.isMultipartContent(req) == false) {
					/**
					 * Bit convoluted. the .jsp has already extracted files and
					 * form fields into session. field.
					 */
					String txt = session.getAttribute("dc").toString();
					this.extractSerializedDc(txt, inData);
					this.extractFilesToDc(req, inData);
				} else {
					/**
					 * jsp has not touched input stream, and it wants us to do
					 * everything.
					 */
					try {
						ServletFileUpload fileUploader = new ServletFileUpload();
						fileUploader.setHeaderEncoding("UTF-8");
						FileItemIterator iterator = fileUploader
								.getItemIterator(req);
						while (iterator.hasNext()) {
							FileItemStream stream = iterator.next();
							String fieldName = stream.getFieldName();
							InputStream inStream = null;
							inStream = stream.openStream();
							try {
								if (stream.isFormField()) {
									String fieldValue = Streams
											.asString(inStream);
									/**
									 * dc is a special name that contains
									 * serialized DC
									 */
									if (fieldName.equals("dc")) {
										this.extractSerializedDc(fieldValue,
												inData);
									} else {
										inData.addValue(fieldName, fieldValue);
									}
								} else {
									/**
									 * it is a file. we assume that the files
									 * are small, and hence we carry the content
									 * in memory with a specific naming
									 * convention
									 */
									String fileContents = IOUtils
											.toString(inStream);
									inData.addValue(fieldName
											+ HtmlRequestHandler.PATH_SUFFIX,
											fileContents);
								}
							} catch (Exception e) {
								Spit.out("error whiel extracting data from request stream "
										+ e.getMessage());
							}
							IOUtils.closeQuietly(inStream);
						}
					} catch (Exception e) {
						// nothing to do here
					}
					/**
					 * read session variables
					 */
					@SuppressWarnings("rawtypes")
					Enumeration e = session.getAttributeNames();
					while (e.hasMoreElements()) {
						String name = (String) e.nextElement();
						if (name.equals("dc")) {
							this.extractSerializedDc(req.getSession()
									.getAttribute(name).toString(), inData);
						}
						String value = req.getSession().getAttribute(name)
								.toString();
						inData.addValue(name, value);
						System.out.println("name is: " + name + " value is: "
								+ value);
					}
				}
			}
		}
		this.getStandardFields(req, inData);
		return inData;
	}

	/*
	 * createInDataForStream() method is duplicate of createInData(), This
	 * method is created for razor-plan to avoid the execution of
	 * req.getParameterNames() before reading input from request stream ,
	 * ******this method has to be re-looked for re-factoring*****
	 */
	/**
	 * 
	 * @param req
	 * @param formIsSubmitted
	 * @param hasSerializedDc
	 * @param outData
	 * @return service data that contains all input fields
	 * @throws ExilityException
	 */
	public ServiceData createInDataForStream(HttpServletRequest req,
			boolean formIsSubmitted, boolean hasSerializedDc,
			ServiceData outData) throws ExilityException {
		ServiceData inData = new ServiceData();

		/**
		 * this method is structured to handle simpler cases in the beginning if
		 * form is not submitted, it has to be serialized data
		 */
		if (formIsSubmitted == false) {
			this.extractSerializedData(req, hasSerializedDc, inData);
			return inData;
		}

		if (hasSerializedDc == false) {
			this.extractParametersAndFiles(req, inData);
			return inData;
		}

		// it is a form submit with serialized DC in it
		HttpSession session = req.getSession();
		try {
			if (ServletFileUpload.isMultipartContent(req) == false) {
				String txt = session.getAttribute("dc").toString();
				this.extractSerializedDc(txt, inData);

				this.extractFilesToDc(req, inData);
				return inData;
			}
			// complex case of file upload etc..
			ServletFileUpload fileUploader = new ServletFileUpload();
			fileUploader.setHeaderEncoding("UTF-8");
			FileItemIterator iterator = fileUploader.getItemIterator(req);
			while (iterator.hasNext()) {
				FileItemStream stream = iterator.next();
				InputStream inStream = null;
				try {
					inStream = stream.openStream();
					String fieldName = stream.getFieldName();
					if (stream.isFormField()) {
						String fieldValue = Streams.asString(inStream);
						if (fieldName.equals("dc")) {
							this.extractSerializedDc(fieldValue, inData);
						} else {
							inData.addValue(fieldName, fieldValue);
						}
					} else {
						String fileContents = IOUtils.toString(inStream);
						inData.addValue(fieldName
								+ HtmlRequestHandler.PATH_SUFFIX, fileContents);
					}
					inStream.close();
				} finally {
					IOUtils.closeQuietly(inStream);
				}

			}

			@SuppressWarnings("rawtypes")
			Enumeration e = req.getSession().getAttributeNames();
			while (e.hasMoreElements()) {
				String name = (String) e.nextElement();
				if (name.equals("dc")) {
					this.extractSerializedDc(req.getSession()
							.getAttribute(name).toString(), inData);
				}
				String value = req.getSession().getAttribute(name).toString();
				inData.addValue(name, value);
				System.out.println("name is: " + name + " value is: " + value);
			}
		} catch (Exception ioEx) {
			// nothing to do here
		}
		return inData;
	}

	/**
	 * extract file paths into dc
	 * 
	 * @param req
	 * @param inData
	 */
	@SuppressWarnings("rawtypes")
	private void extractFilesToDc(HttpServletRequest req, ServiceData inData) {
		HttpSession session = req.getSession();
		Object o1 = session.getAttribute("ExilityIsMultipart");
		Object o2 = session.getAttribute("ExilityFileItemsList");
		if (o1 == null || o2 == null) {
			return;
		}

		boolean isMultipart = ((Boolean) (o1)).booleanValue();
		@SuppressWarnings("unchecked")
		List<String> items = (List) (o2);
		if (isMultipart) {
			Iterator iterator = items.iterator();
			while (iterator.hasNext()) {
				String fldName = iterator.next().toString()
						+ HtmlRequestHandler.PATH_SUFFIX;
				Object obj = session.getAttribute(fldName);
				if (obj != null) {
					inData.addValue(fldName, obj.toString());
				}
			}
		}
	}

	/***
	 * Method suitable for Web tier components to talk to app server and get the
	 * service
	 * 
	 * @param serviceName
	 *            name of service to be executed
	 * @param req
	 *            request object of httpContext
	 * @return output from the service
	 * @throws ExilityException
	 */
	public ServiceData serve(String serviceName, HttpServletRequest req)
			throws ExilityException {
		ServiceData outData = new ServiceData();
		ServiceData inData = this.createInData(req, true, false, outData);
		if (inData == null) {
			return outData;
		}

		if (SecurityGuard.getGuard().cleared(req, inData, outData) == false) {
			return outData;
		}

		this.executeService(req, inData, outData, serviceName);
		return outData;
	}

	/**
	 * process a request from client. This is the main method used by
	 * Service.jsp
	 * 
	 * @param req
	 * @param resp
	 * @param formIsSubmitted
	 *            has the caller submitted the form?
	 * @param hasSerializedDc
	 *            is it an xmlHttp request with serialised DC as data?
	 * @return output data
	 * @throws ExilityException
	 */
	public ServiceData processRequest(HttpServletRequest req,
			HttpServletResponse resp, boolean formIsSubmitted,
			boolean hasSerializedDc) throws ExilityException {
		try {
			req.setCharacterEncoding("UTF-8");
			resp.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			Spit.out("Not UTF-8 : '" + e.getMessage() + "'");
		}

		ServiceData outData = new ServiceData();

		String simpleFileUpload = req.getHeader("simple_file_upload");
		String outDataServiceId = null;
		if (simpleFileUpload != null) {
			boolean isFileUploadSucceeded = this.processSimpleFileUpload(req,
					outData);
			/**
			 * in case of any error in upload, above method would have already
			 * put error message. we have to get out of here
			 */
			if (isFileUploadSucceeded == false) {
				return outData;
			}
			outDataServiceId = outData.getValue("serviceId");
		}
		// ServiceData inData = this.createInData(req, formIsSubmitted,
		// hasSerializedDc, outData);
		/*
		 * the above is commented and changes are done below by introducing a
		 * new request header parameter "isStream" in razor plan to avoid the
		 * execution of req.getParameterNames() before reading input from
		 * request stream , ******these changes has to be relooked for
		 * refactoring*****
		 */
		ServiceData inData = new ServiceData();
		String isStream = req.getHeader("isStream");

		if (isStream == null) {
			inData = this.createInData(req, formIsSubmitted, hasSerializedDc,
					outData);
		} else {
			inData = this.createInDataForStream(req, formIsSubmitted,
					hasSerializedDc, outData);
		}
		/*
		 * If user ask for simple file upload and with serviceId then execute
		 * it. Like fileUpload. Need to look it.
		 */
		if (outDataServiceId != null && inData != null) {
			inData.extractData(outData);
		}

		if (inData == null) {
			return outData;
		}

		String serviceName = inData.getValue("serviceId");

		if (serviceName == null || serviceName.length() == 0) {
			outData.addMessage("exilNoService");
			return outData;
		}

		if (serviceName.equals(HtmlRequestHandler.getLoginServiceName())) {
			this.executeService(req, inData, outData, serviceName);
			this.doLogin(req, resp, outData);
			return outData;
		}

		if (serviceName.equals(HtmlRequestHandler.getLogoutServiceNamr())) {
			this.executeService(req, inData, outData, serviceName);
			this.doLogout(req, resp);
			return outData;
		}

		// TODO: investigate why list service is exempt from security, and make
		// changes for internal service security arrangement
		if (serviceName.equals(HtmlRequestHandler.LIST_SERVICE)) {
			// System.out.print("green channel granted to " + serviceName);
		} else if (SecurityGuard.getGuard().cleared(req, inData, outData) == false) {
			return outData;
		}

		if (serviceName.equals(HtmlRequestHandler.PAGINATION_SERVICE_ID)) {
			this.getPaginationData(req, inData, outData);
			return outData;
		}

		this.executeService(req, inData, outData, serviceName);
		this.savePaginationData(req, inData, outData);

		// do we have to set session variables?
		if (outData.hasValue(HtmlRequestHandler.GLOBAL_FIELDS_NAME)) {
			String sessionObjName = HtmlRequestHandler.GLOBAL_SERVER_DATA_NAME
					+ outData.getValue(HtmlRequestHandler.getUserIdName());
			@SuppressWarnings("unchecked")
			Map<String, String> sessionData = (Map<String, String>) req
					.getSession(true).getAttribute(sessionObjName);
			if (sessionData == null) {
				sessionData = new HashMap<String, String>();
				req.getSession(true).setAttribute(sessionObjName, sessionData);
			}
			String fieldNames = outData
					.getValue(HtmlRequestHandler.GLOBAL_FIELDS_NAME);
			for (String fieldName : fieldNames.split(",")) {
				sessionData.put(fieldName, outData.getValue(fieldName));
			}
			outData.removeValue(HtmlRequestHandler.GLOBAL_FIELDS_NAME);
		}
		return outData;
	}

	/**
	 * execute the service
	 * 
	 * @param req
	 * @param inData
	 * @param outData
	 * @param serviceName
	 */
	private void executeService(HttpServletRequest req, ServiceData inData,
			ServiceData outData, String serviceName) {
		/*
		 * TODO: needs re-factoring: If a project can set only one cleanser, why
		 * this get method? Should we just ask for qualified className in AP,
		 * and instantiate it once and for all?
		 */
		ServiceCleanserInterface serviceCleanser = null;
		if (AP.cleanserName != null) {
			serviceCleanser = ServiceCleansers.getCleanser(AP.cleanserName);
			if (serviceCleanser == null) {
				outData.addMessage("exilNoSuchCleanser", AP.cleanserName);
				return;
			}
			if (!serviceCleanser.cleanseBeforeService(req, inData)) {
				outData.addMessage("cleanseBeforeServiceFailed",
						AP.cleanserName);
				return;
			}
		}

		String userId = inData.getValue(HtmlRequestHandler.getUserIdName());
		ServiceAgent agent = new ServiceAgent();
		if (useNewServiceAgent) {
			ServiceData newOutData = agent.executeService(serviceName, userId,
					inData);
			outData.copyFrom(newOutData);
		} else {
			agent.serve(serviceName, userId, inData, outData);
		}

		if (serviceCleanser != null) {
			if (!serviceCleanser.cleanseAfterService(req, outData)) {
				outData.addMessage("cleanseAfterServiceFailed", AP.cleanserName);
				return;
			}
		}
	}

	/**
	 * Extract data elements from an input stream. This handles a typical ajax
	 * call with either name/value pairs (typical form submit style) or
	 * serialised DC
	 * 
	 * @param req
	 * @param hasSerializedDc
	 *            is there a field called data that has the entire dc serialized
	 *            in it? This is typically how an Exility client sends data
	 * @param inData
	 * @throws ExilityException
	 */
	private void extractSerializedData(HttpServletRequest req,
			boolean hasSerializedDc, ServiceData inData)
			throws ExilityException {
		try {
			// Is this the best way to read> I am sure we should be able to read
			// better than byte-by-byte
			DataInputStream sr = new DataInputStream(req.getInputStream());
			InputStreamReader isr = new InputStreamReader(sr, "UTF-8");

			StringBuffer buffer = new StringBuffer();
			Reader in = new BufferedReader(isr);
			int ch;
			while ((ch = in.read()) > -1) {
				buffer.append((char) ch);
			}

			String inputText = buffer.toString();
			if (inputText.length() == 0) {
				return;
			}

			if (hasSerializedDc) {
				this.extractSerializedDc(inputText, inData);
			} else {
				this.deserialize(inputText, inData);
			}
		} catch (IOException ioEx) {
			// nothing to do here
		}
	}

	/**
	 * extract name-value pairs from a string of the form
	 * name1=value1&name2=value2....
	 * 
	 * @param str
	 * @param data
	 */
	private void deserialize(String str, ServiceData data) {
		if (str == null || str.length() == 0) {
			return;
		}
		for (String pair : str.split("&")) {
			String[] vals = pair.split("=");
			String val = (vals.length > 1) ? vals[1] : "";
			data.addValue(vals[0], val);
		}
	}

	/**
	 * extract request parameters into inData. Any repeated parameter is assumed
	 * to be a list
	 * 
	 * @param req
	 * @param data
	 */
	private void extractParametersAndFiles(HttpServletRequest req,
			ServiceData data) {
		@SuppressWarnings("unchecked")
		Enumeration<String> paramNames = req.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			String[] vals = req.getParameterValues(paramName);
			if (vals == null || vals.length == 0) {
				data.addValue(paramName, "");
			} else if (vals.length == 1) {
				data.addValue(paramName, vals[0]);
			} else {
				data.addList(paramName, vals);
			}
		}

		this.extractFilesToDc(req, data);
	}

	/***
	 * extract service data from a serialized string as per exility standard.
	 * 
	 * @param inputText
	 * @param inData
	 * @throws ExilityException
	 */
	private void extractSerializedDc(String inputText, ServiceData inData)
			throws ExilityException {
		if (inputText.length() == 0) {
			return;
		}
		try {
			inData.extractData(inputText);
		} catch (Exception e) {
			Spit.out(e);
			inData.addError(e.getMessage());
		}
	}

	/**
	 * Extract cookies and other global fields into inData
	 * 
	 * @param req
	 * @param inData
	 */
	@SuppressWarnings("unchecked")
	private void getStandardFields(HttpServletRequest req, ServiceData inData) {
		// log field
		if (suppressSqlLog) {
			inData.addValue(ExilityConstants.SUPPRESS_SQL_LOG, "1");
		}

		if (AP.cookiesToBeExtracted != null) {
			Cookie[] cookies = req.getCookies();
			if (cookies != null && cookies.length > 0) {
				for (Cookie cookie : cookies) {
					if (AP.cookiesToBeExtracted.contains(cookie.getName())) {
						Spit.out(cookie.getName() + " extracted from cookie");
						inData.addValue(cookie.getName(), cookie.getValue());
					}
				}
			}
		}

		this.extractParametersAndFiles(req, inData);

		/**
		 * is there a sessionData object?
		 */
		HttpSession session = req.getSession();
		String token = req.getHeader(CommonFieldNames.CSRF_HEADER);
		if (token == null) {
			// try form fields
			token = inData.getValue(CommonFieldNames.CSRF_HEADER);
		}
		if (token == null) {
			// for the sake of jsps that still exist in our system..
			Object obj = session
					.getAttribute(HttpRequestHandler.SESSION_TOKEN_NAME);
			if (obj != null) {
				token = obj.toString();
			}
		}
		if (token != null) {
			Object obj = session.getAttribute(token);
			if (obj != null && obj instanceof SessionData) {
				Spit.out("Session fields being extracted from new token based object.");
				((SessionData) obj).extractAll(inData);
			} else {
				Spit.out("CSRF token found to be " + token
						+ " but session data not found");
			}
		} else {
			Spit.out("NO CSRF token. Will try old ways of session data.");
			Object data = session
					.getAttribute(HtmlRequestHandler.GLOBAL_SERVER_DATA_NAME
							+ inData.getValue(HtmlRequestHandler
									.getUserIdName()));
			if (data != null && data instanceof Map) {
				Map<String, String> sessionData = (Map<String, String>) data;
				for (String name : sessionData.keySet()) {
					// Spit.out("Trying " + name + " as a global field");
					String val = sessionData.get(name);
					if (val != null && val.length() > 0) {
						inData.addValue(name, val);
					}
				}
			}
		}
	}

	/**
	 * Add global fields as set in applicationParameters
	 * 
	 * @param req
	 * @param outData
	 */
	void addGlobalDataToSession(HttpServletRequest req, ServiceData outData) {
		Map<String, String> sessionData = new HashMap<String, String>();
		for (String name : AP.globalServerDataNames) {
			String val = outData.getValue(name);

			if (val == null || val.length() == 0) {
				continue;
			}
			Spit.out("Gloabal Field " + name + " is added and its value is "
					+ val);
			sessionData.put(name, val);
		}
		req.getSession().setAttribute(
				HtmlRequestHandler.GLOBAL_SERVER_DATA_NAME
						+ outData.getValue(HtmlRequestHandler.getUserIdName()),
				sessionData);
	}

	/**
	 * login service that can be called from the client layer. Typically used
	 * during development to do dummy logins..
	 * 
	 * @param req
	 * @param resp
	 * @param formIsSubmitted
	 *            Caller can either submit the form, or just use query fields
	 * @return true if login is successful, false otherwise
	 * @throws ExilityException
	 */
	public boolean login(HttpServletRequest req, HttpServletResponse resp,
			boolean formIsSubmitted) throws ExilityException {
		ServiceData data = new ServiceData();
		for (Object param : req.getParameterMap().keySet()) {
			data.addValue((String) param,
					(String) req.getParameterMap().get(param));
		}

		if (formIsSubmitted) {
			this.extractParametersAndFiles(req, data);
		} else {
			this.extractSerializedData(req, false, data);
		}

		if (AP.cleanserName != null) {
			ServiceCleanserInterface serviceCleanser = ServiceCleansers
					.getCleanser(AP.cleanserName);
			if (serviceCleanser == null) {
				data.addError(AP.cleanserName + " is not found as a cleanser.");
				return false;
			}
			if (!serviceCleanser.cleanseBeforeService(req, data)) {
				{
					data.addError(AP.cleanserName
							+ " failed to execute properly.");
					return false;
				}
			}
		}

		// ServiceAgent agent = ServiceAgent.getAgent();

		ServiceAgent agent = new ServiceAgent();

		agent.serve(HtmlRequestHandler.getLoginServiceName(), null, data, data);
		return this.doLogin(req, resp, data);
	}

	/**
	 * Carry out login rituals after a successful execution of login service
	 * 
	 * @param req
	 * @param resp
	 * @param data
	 * @return
	 */

	private boolean doLogin(HttpServletRequest req, HttpServletResponse resp,
			ServiceData data) {
		if (data.getErrorStatus() != CommonFieldNames.SEVERITY_SUCCESS) {
			return false;
		}

		req.getSession().setAttribute(AP.loggedInUserFieldName,
				data.getValue(AP.loggedInUserFieldName));
		// set cookies
		Cookie cookie = new Cookie(AP.loggedInUserFieldName,
				data.getValue(AP.loggedInUserFieldName));
		Date now = DateUtility.addDays(new Date(), 400);
		cookie.setMaxAge((int) now.getTime());
		resp.addCookie(cookie);
		if (AP.setCookies != null) {
			for (String name : AP.setCookies) {
				cookie = new Cookie(name, data.getValue(name));
				cookie.setPath(req.getContextPath());
				if (data.hasValue(name)) {
					Spit.out(" cookie " + name + " is set with value = "
							+ data.getValue(name));
					cookie.setMaxAge((int) now.getTime());
				} else {
					// we have to remove the cookie
					Spit.out(name
							+ " does not have value and hence cookie is not set");
					cookie.setMaxAge(-12);
				}
				resp.addCookie(cookie);
			}
		}

		data.addValue("*_usersession", req.getSession().getId());

		this.addGlobalDataToSession(req, data);

		// TEXTILE needs the following four lines
		/*
		 * ExilityInterface.Bridge br = new ExilityInterface.Bridge();
		 * DataCollection dc = new DataCollection(); dc.CopyFrom(data);
		 * br.AddoldVersionGlobalValues(dc, ctx);
		 */

		if (AP.cleanserName != null) {
			ServiceCleanserInterface serviceCleanser = ServiceCleansers
					.getCleanser(AP.cleanserName);
			if (serviceCleanser == null) {
				data.addError(AP.cleanserName
						+ " is not a valid cleanser name.");
				return false;
			}
			if (!serviceCleanser.cleanseAfterService(req, data)) {
				{
					data.addMessage("cleanseAfterServiceFailed",
							AP.cleanserName);
					return false;
				}
			}
		}

		return true;
	}

	/***
	 * logout the user from this application. Suitable to be called from client
	 * layer like jsp
	 * 
	 * @param req
	 * @param resp
	 */
	public void logout(HttpServletRequest req, HttpServletResponse resp) {
		ServiceData data = new ServiceData();
		// ServiceAgent agent = ServiceAgent.getAgent();
		ServiceAgent agent = new ServiceAgent();
		agent.serve(HtmlRequestHandler.getLogoutServiceNamr(), null, data, data);
		this.doLogout(req, resp);
	}

	/***
	 * Carry out all rituals of logging the user out
	 * 
	 * @param req
	 * @param resp
	 */
	private void doLogout(HttpServletRequest req, HttpServletResponse resp) {
		Cookie cookie = new Cookie(AP.loggedInUserFieldName, "");
		Date now = DateUtility.addDays(new Date(), -2);
		cookie.setMaxAge((int) now.getTime());
		resp.addCookie(cookie);
		req.getSession().invalidate();
		// this.removeGlobalDataFromSession(req);
	}

	/***
	 * Save grids that are to be paginated into session. replace these grids in
	 * outData with just the first page.
	 * 
	 * @param req
	 * @param inData
	 * @param outData
	 */
	private void savePaginationData(HttpServletRequest req, ServiceData inData,
			ServiceData outData) {
		ArrayList<String> gridNames = new ArrayList<String>();
		for (String name : outData.grids.keySet()) {
			gridNames.add(name);
		}

		HttpSession session = req.getSession();
		for (String name : gridNames) {
			String pageSize = inData.getValue(name
					+ HtmlRequestHandler.PAGE_SIZE);
			if (pageSize == null) {
				continue;
			}
			int ps = Integer.parseInt(pageSize);
			String[][] grid = outData.grids.get(name);
			int n = grid.length - 1;
			// Spit.out(name + " has " + n + " rows while page size is " + ps);
			if (n <= ps) {
				session.removeAttribute(name);
				continue;
			}
			session.setAttribute(name, grid);
			String[][] newGrid = new String[ps + 1][];
			newGrid[0] = grid[0];
			for (int i = 1; i < newGrid.length; i++) {
				newGrid[i] = grid[i];
			}
			outData.addGrid(name, newGrid);
			outData.addValue(name + "TotalRows", Integer.toString(n));
			// Spit.out("TotalRows for table " + name + " set to " +
			// outData.getValue(name + "TotalRows") );
		}
	}

	/***
	 * Get the required page of data into inData based on user request. If
	 * cached data is missing, get fresh data, if possible, using pagination
	 * service
	 * 
	 * @param req
	 * @param inData
	 * @param outData
	 */
	private void getPaginationData(HttpServletRequest req, ServiceData inData,
			ServiceData outData) {
		String tableName = inData.getValue("tableName");
		if (tableName == null) {
			outData.addError("Client did not send table name for paginated data.");
			return;
		}

		Object paginationData = req.getSession().getAttribute(tableName);
		int pageNo = 0;
		String txt = inData.getValue("pageNo");
		if (txt != null && txt.length() > 0) {
			pageNo = Integer.parseInt(txt);
		}

		if (pageNo == 0) // client has moved on.... signal for us to purge
							// cached data
		{
			if (paginationData != null) {
				req.getSession().removeAttribute(tableName);
			}
			return;
		}

		if (paginationData == null) // pagination data is missing. Re-login case
		{
			String newService = inData
					.getValue(HtmlRequestHandler.PAGINATION_SERVICE_FIELD_NAME);
			if (newService == null) // client has provided the service to fire
									// to get data again.
			{
				outData.addMessage(
						Message.EXILITY_ERROR,
						"Pagination data is not available, probably because you have re-logged-in. Please resubmit your query, or reset page.");
				return;
			}
			this.executeService(req, inData, outData, newService);
			paginationData = outData.getGrid(tableName);
			if (paginationData == null) {
				outData.addError("Pagination service " + newService
						+ " did not provide data in table " + tableName);
				return;
			}
			req.getSession().setAttribute(tableName, paginationData);
			// TODO: It is possible that the service would have returned
			// different sets of rows than the one returned before re-login!!!
		}

		String[][] grid = (String[][]) paginationData;
		int totalRows = grid.length - 1;
		int pageSize = Integer.parseInt(inData.getValue("pageSize"));
		String columnToSort = inData.getValue(HtmlRequestHandler.SORT_COLUMN);
		if (columnToSort != null) {
			String sortDesc = inData.getValue(HtmlRequestHandler.SORT_DESC);
			boolean toSortDesc = sortDesc != null && sortDesc.equals("1");
			Util.sortGrid(grid, columnToSort, toSortDesc);
		}
		int istart = (pageNo - 1) * pageSize + 1; // it is 1 based
		if (istart > totalRows) {
			istart = 1;
		}
		int iend = istart + pageSize - 1;
		if (iend > totalRows) {
			pageSize = totalRows - istart + 1;
		}
		String[][] newGrid = new String[pageSize][];
		for (int i = 0; i < newGrid.length; i++) {
			newGrid[i] = grid[istart];
			istart++;
		}
		outData.addGrid(tableName, newGrid);
		return;
	}

	/***
	 * sort the grid and return the row number to be part of the page to send to
	 * client
	 * 
	 * @param grid
	 *            data grid to be sorted
	 * @param inData
	 *            service data that has come from client
	 * @return 1 if client has not asked for any specific row. Else the row that
	 *         contains the column value client has asked for
	 */
	@SuppressWarnings("unused")
	// because this feature is not yet used.
	private int sortGrid(String[][] grid, ServiceData inData) {
		String sortColumn = inData.getValue(HtmlRequestHandler.SORT_COLUMN);
		String sortDesc = inData.getValue(HtmlRequestHandler.SORT_DESC);
		boolean toSortDesc = sortDesc != null && sortDesc.equals("1");
		Util.sortGrid(grid, sortColumn, toSortDesc);

		// did the client ask to send a page that contains a specific row?
		// typically, user may want to see the selected row after sorting. In
		// that case, client sends the key column name and key column value
		String columnToMatch = inData.getValue(HtmlRequestHandler.MATCH_COLUMN);
		if (columnToMatch == null) {
			return 1;
		}

		String valueToMatch = inData.getValue(HtmlRequestHandler.MATCH_VALUE);
		if (valueToMatch == null) {
			Spit.out("Value not supplied for column " + columnToMatch
					+ " to look-up a row. First page is retrned");
			return 1;
		}

		int rowNumber = Util.getMatchingRow(grid, columnToMatch, valueToMatch);
		if (rowNumber > 0) {
			return rowNumber;
		}

		Spit.out("Could not find a row with a value of " + valueToMatch
				+ " for column " + columnToMatch + ". First page is returned.");
		return 1;
	}

	/**
	 * This method write the file to server from file field and also read the
	 * content of file in case of file is the valid excel file and user has
	 * passed gridName along with header.
	 * 
	 * @param req
	 * @param data
	 * @return boolean true if file upload success else false
	 * @author Anant on 26/04/2013
	 */
	private boolean processSimpleFileUpload(HttpServletRequest req,
			ServiceData data) {
		if (!ServletFileUpload.isMultipartContent(req)) {
			data.addError("No multipart content found.");
			return false;
		}

		XLSHandler.parseMultiPartData(req, data);
		try {
			this.extractSerializedDc(data.getValue("dc"), data);
			data.removeValue("dc");
		} catch (ExilityException e1) {
			data.addError(e1.getMessage());
			Spit.out(e1);
			data.removeValue("dc");
			return false;
		}
		// If user passed gridName then read the content of file into grid and
		// add to ServiceData data
		String gridName = data.getValue("xlsGridName");
		String fileFieldNameVal = data.getValue("fileFieldName");
		String allowMultiple = req.getHeader("allowMultiple");
		Spit.out("fileFieldName returns file path at the server: ===>"
				+ fileFieldNameVal);

		if ((gridName != null && !gridName.trim().isEmpty())
				&& (allowMultiple == null)) {
			Workbook wbHandler = null;
			String fileNameWithPath = data.getValue("filePath");
			Spit.out("fileFieldName returns file path at the server: ===>"
					+ fileNameWithPath);
			if (fileNameWithPath == null || fileNameWithPath.isEmpty()) {
				Spit.out("fileFieldName returns null or empty file path at the server: check in write()===>");
				return false;
			}

			wbHandler = XLSHandler.getXLSHandler(fileNameWithPath);

			if (wbHandler == null) {
				String msg = "file ["
						+ fileNameWithPath
						+ "] content cant be read as Grid, check whether you supplied correct excel file or not. cause";
				data.addError(msg);
				Spit.out(msg);
				return false;
			}

			DataCollection dc = new DataCollection();
			dc.addTextValue("xlsGridName", gridName);

			new XLSReader().readAWorkbook(wbHandler, dc);

			dc.copyTo(data);
			if (!dc.hasError()) {
				data.addValue(fileFieldNameVal, fileNameWithPath);
				data.removeValue("filePath");
				return true;
			}

			String msg = "file ["
					+ fileNameWithPath
					+ "] content cant be read as Grid, check whether you supplied correct excel file or not.";
			data.addError(msg);
			Spit.out(msg);
			return false;
		}
		return true;
	}

	/**
	 * should logging of SQls be suppressed?
	 * 
	 * @param suppress
	 */
	public static void setSuppressSqlLog(boolean suppress) {
		suppressSqlLog = suppress;
	}
}
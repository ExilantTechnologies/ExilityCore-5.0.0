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
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;

/***
 * Single-point entry for services request from HTTP clients. Exility services
 * are exposed to clients thru this class. This is NOT a web service. We use
 * simpler protocol. Any client that wants to use these services can either use
 * full-fledged exility client or ExilityClient.js that wraps the calling
 * protocol.
 * 
 */
public class HttpRequestHandler extends HttpServlet {

	/**
	 * name under which last token is saved in session
	 */
	public static final String SESSION_TOKEN_NAME = "LAST_TOKEN_USED";
	private static final long serialVersionUID = 1L;

	private static final String CACHE_CONTROL = "Cache-Control";
	private static final String NO_CACHE = "no-cache";
	private static final String PAGINATION_SERVICE_FIELD_NAME = "paginationServiceName";
	protected static SingleSignOnInterface ssoObject = null;
	protected static ServiceCleanserInterface serviceCleanser = null;
	protected static boolean suppressSqlLog = false;
	protected static boolean useNewServiceAgent = false;

	/**
	 * should we allow multiple login-sessions per user-session? For example,
	 * from the same browser instance, should we allow a user to login as Ramesh
	 * in one window and as Suresh in another window? this attribute is set by
	 * startupServlet based on web.xml setting
	 */
	protected static boolean allowMultipleLoginsPerSession = false;

	/**
	 * This is to be called from start-up servlet
	 * 
	 * @param sso
	 *            object that is to be used for sign-in process. To disable an
	 *            existing sign-on, you may use null for this parameter
	 */
	public static void setSignoOnObject(SingleSignOnInterface sso) {
		ssoObject = sso;
	}

	/**
	 * set whether multiple logins are allowed per browser session
	 * 
	 * @param multipleLoginPerSession
	 */
	public static void setMultipleLoginOption(boolean multipleLoginPerSession) {
		allowMultipleLoginsPerSession = multipleLoginPerSession;
	}

	/**
	 * should logging of SQls be suppressed?
	 * 
	 * @param suppress
	 */
	public static void setSuppressSqlLog(boolean suppress) {
		suppressSqlLog = suppress;
	}

	/**
	 * This is to be called from start-up servlet
	 * 
	 * @param cleanser
	 *            to be used for user-defined handler before and after service
	 *            execution
	 */
	public static void setCleanser(ServiceCleanserInterface cleanser) {
		serviceCleanser = cleanser;
	}

	/***
	 * We accept only POST requests.
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// set the character encoding...
		request.setCharacterEncoding(CommonFieldNames.CHAR_ENCODING);
		response.setCharacterEncoding(CommonFieldNames.CHAR_ENCODING);
		response.setHeader(CACHE_CONTROL, NO_CACHE);
		try {
			MyAssistant assistant = new MyAssistant(request, response);
			String log = assistant.processRequest();
			System.out.println("Log \n" + log);
		} catch (Exception e) {

			/**
			 * TODO : we have to use error reporting infrastructure and probably
			 * create a ticket...
			 */
			System.out.println("Server exception " + e.getMessage());
			e.printStackTrace();
			response.getWriter()
					.write("Sorry, we are unable to service your request due to an internal error on the server. Some one is looking at the issue right away. Please retry your request after some time.");
		}
	}

	/**
	 * populate data with global/session variables, if this is an authenticated
	 * session. Caller can have the CommonFieldNames.CSRF_HEADER token either in
	 * the header, or a field already extracted into inData
	 * 
	 * @param req
	 * @param inData
	 *            into which session fields need to be extracted
	 * @return true if this is an authenticated session, false otherwise
	 */
	public static boolean extractSessionFields(HttpServletRequest req,
			ServiceData inData) {
		String token = req.getHeader(CommonFieldNames.CSRF_HEADER);
		if (token == null) {
			token = inData.getValue(CommonFieldNames.CSRF_HEADER);
		}

		if (token == null || token.length() == 0) {
			return false;
		}

		HttpSession session = req.getSession(false);
		if (session == null) {
			return false;
		}

		Object obj = session.getAttribute(token);
		if (obj == null) {
			return false;
		}

		if (obj instanceof SessionData == false) {
			return false;
		}
		((SessionData) obj).extractAll(inData);
		return true;
	}

	/**
	 * Remember, servlet is instantiated once for a web-server and reused for
	 * all requests. We use this worker class that is instantiated for each
	 * request to carry request specific attributes and process the request with
	 * these attributes
	 * 
	 */
	private class MyAssistant {

		/***
		 * Request object that we deal with
		 */
		private final HttpServletRequest req;

		/***
		 * Response object that we are dealing with
		 */
		private final HttpServletResponse resp;

		/***
		 * collects all parameters sent by the client.
		 */
		private final ServiceData inData;

		/***
		 * accumulates all data and messages to be sent back to client
		 */
		private ServiceData outData;

		/**
		 * http session associated with this client
		 */
		private HttpSession session;

		/**
		 * client token is similar to session id. Just as web server has a
		 * session id associated with each connected browser instance, we
		 * maintain a token for every logged-in user. It is possible that the
		 * same browser instance may have more than one logged-in user coming
		 * in.
		 */
		private String clientToken = null;

		/**
		 * our own session data. This is maintained inside session of
		 * web-server. We maintain this indexed by clientToken.
		 */
		private SessionData sessionData;

		/**
		 * client's status as a logged-in user
		 */
		private int authenticationStatus = CommonFieldNames.SECURITY_CLEARED;

		MyAssistant(HttpServletRequest req, HttpServletResponse resp) {
			this.req = req;
			this.resp = resp;
			this.inData = new ServiceData();
			this.outData = new ServiceData();
			/**
			 * we need to create a session if one is not there already
			 */
			this.session = req.getSession(true);

		}

		/**
		 * work, and return the trace text to be written to servlet log
		 * 
		 * @return trace to be written to log
		 * @throws IOException
		 * @throws ExilityException
		 */
		String processRequest() throws IOException {

			/**
			 * start collecting logs for this thread
			 */
			Spit.startWriter();
			Date startedAt = new Date();

			if (AP.projectName == null) {
				String txt = "Your application is not set properly. Change your web.config to set the resource folder properly.";
				this.outData.addMessage(ExilityMessageIds.ERROR, txt);
			} else {
				this.execute();
			}

			String myTraceText = Spit.stopWriter();
			String serviceName = this.inData
					.getValue(CommonFieldNames.SERVICE_ID);
			String userId = this.inData.getValue(AP.loggedInUserFieldName);
			long ms = new Date().getTime() - startedAt.getTime();

			/**
			 * We DO NOT use Spit.writeServiceLog(). Use this.log() instead to
			 * have flexibility of deployment options. This text will be
			 * returned to the master to use the web-server logging
			 */
			String log = "<webServiceLog service=\"" + serviceName
					+ "\" userId=\"" + userId + "\" dateTime=\""
					+ startedAt.toString() + "\" timeTakenInMs=\"" + ms
					+ "\" >" + myTraceText + "</webServiceLog>";

			// are we returning trace text to client?
			String clientTrace = myTraceText;
			String serverTraceText = this.outData
					.getValue(CommonFieldNames.TRACE_TEXT);
			if (serverTraceText != null) {
				clientTrace += "\n*********** app server trace  ***********"
						+ serverTraceText;
			}
			this.outData.addValue(CommonFieldNames.TRACE_TEXT, clientTrace);
			if (!serviceName.equals(CommonFieldNames.GET_BACKGROUND_JOB_RESULT)) {
				/**
				 * If user has not asked for the status of a specific job id,
				 * return the status of all background jobs..
				 */
				if (this.sessionData != null) {
					this.sessionData.setAllJobStatus(this.outData);
				}
			}
			/**
			 * OK, We are all done. Write out output
			 */
			this.resp.getWriter().write(this.outData.toSerializedData());
			return log;
		}

		/**
		 * Extract data from input stream, and execute the service that is
		 * requested by the client
		 */
		private void execute() {
			/**
			 * populate inData from client request. Request also authenticated
			 * as part of extracting in data
			 */
			boolean allOk = this.createInData();
			if (!allOk) {
				return;
			}

			String serviceName = this.inData
					.getValue(CommonFieldNames.SERVICE_ID);

			if (serviceName == null || serviceName.length() == 0) {
				this.outData.addMessage(ExilityMessageIds.NO_SERVICE);
				return;
			}

			/**
			 * some services do not require authentication
			 */
			if (serviceName.equals(AP.loginServiceId)) {
				/*
				 * if client is asking for login, first let us log her out
				 */
				this.doLogout();

				this.executeService(serviceName);
				this.doLogin();
				return;
			}

			if (serviceName.equals(AP.logoutServiceId)) {
				this.executeService(serviceName);
				this.doLogout();
				return;
			}

			if (serviceName.equals(CommonFieldNames.GET_BACKGROUND_JOB_RESULT)) {
				this.returnBackgroundJobResult();
				return;
			}

			if (serviceName.equals(CommonFieldNames.PAGINATION_SERVICE_ID)) {
				this.getPaginationData();
				return;
			}

			/**
			 * are we OK with this client?
			 */
			if (this.authenticationStatus != CommonFieldNames.SECURITY_CLEARED) {
				if (HttpRequestHandler.ssoObject == null || !this.trySso()) {
					this.outData.addValue(
							CommonFieldNames.AUTHENTICATION_FIELD_NAME, ""
									+ this.authenticationStatus);
					return;
				}
			}

			this.executeService(serviceName);
			this.savePaginationData();

			/**
			 * A service can ask us to set session variables by giving us the
			 * list of such field names in a comma separated string
			 */
			String gloabFieldNames = this.outData
					.getValue(InternallyUsedNames.GLOBAL_FIELDS_NAME);
			if (gloabFieldNames != null) {
				for (String fieldName : gloabFieldNames.split(",")) {
					fieldName = fieldName.trim();
					String val = this.outData.getValue(fieldName);
					if (this.sessionData != null) {
						if (val == null) {
							this.sessionData.removeField(fieldName);
						} else {
							this.sessionData.addField(fieldName, val);
						}
					}
				}
				this.outData
						.removeValue(InternallyUsedNames.GLOBAL_FIELDS_NAME);
			}

			/**
			 * did the service fire in background job?
			 */
			if (this.outData.hasValue(CommonFieldNames.BACKGROUND_JOB_ID)
					&& this.sessionData != null) {
				String jobId = this.outData
						.getValue(CommonFieldNames.BACKGROUND_JOB_ID);

				this.sessionData.addJobStatus(jobId,
						CommonFieldNames.JOB_IS_RUNNING);
			}
		}

		/***
		 * When client asks for the result of a background job, we put the
		 * current status in the response. If the job is done, response includes
		 * the output dc from this job.
		 * 
		 */
		private void returnBackgroundJobResult() {
			String jobId = this.inData
					.getValue(CommonFieldNames.BACKGROUND_JOB_ID);
			if (jobId == null) {
				this.outData.addMessage(ExilityMessageIds.ERROR,
						"No job id specified in field "
								+ CommonFieldNames.BACKGROUND_JOB_ID);
				return;
			}

			if (this.sessionData != null) {
				this.sessionData.updateJobStatus(jobId, this.outData);
			}
		}

		/**
		 * Extract data from request object : session data, and the data sent as
		 * pay-load returns false on any error
		 */
		// we could not avoid this because of session data
		private boolean createInData() {

			BufferedReader isr = null;
			StringBuffer buffer = new StringBuffer();
			boolean allOk = true;
			try {
				isr = this.req.getReader();
				int ch;
				while ((ch = isr.read()) > -1) {
					buffer.append((char) ch);
				}
				isr.close();
				this.inData.extractData(buffer.toString());
			} catch (Exception e) {
				String msg = "Error while reading data from client "
						+ e.getMessage();
				Spit.out(msg);
				this.outData.addMessage(ExilityMessageIds.ERROR, msg);
				allOk = false;
			} finally {
				IOUtils.closeQuietly(isr);
			}

			if (!allOk) {
				return false;
			}

			// log field
			if (suppressSqlLog) {
				this.inData.addValue(ExilityConstants.SUPPRESS_SQL_LOG, "1");
			}

			this.clientToken = this.req.getHeader(CommonFieldNames.CSRF_HEADER);
			// Spit.out("I got ||" + this.clientToken + "||");

			if (this.clientToken == null) {
				this.authenticationStatus = CommonFieldNames.SECURITY_NOT_LOGGED_IN;
				return true;
			}

			Object obj = this.session.getAttribute(this.clientToken);
			if (obj != null && obj instanceof SessionData) {
				this.sessionData = (SessionData) obj;
				this.sessionData.extractAll(this.inData);
				this.authenticationStatus = CommonFieldNames.SECURITY_CLEARED;
				return true;
			}

			this.authenticationStatus = CommonFieldNames.SECURITY_SESSION_EXPIRED;
			return true;
		}

		/**
		 * create a login-session within this web-session
		 */
		private void createSession() {
			this.session = this.req.getSession(true);
			this.clientToken = UUID.randomUUID().toString();
			this.sessionData = new SessionData();
			this.session.setAttribute(this.clientToken, this.sessionData);
			this.session.setAttribute(SESSION_TOKEN_NAME, this.clientToken);
		}

		/**
		 * execute the service
		 * 
		 * @param serviceName
		 */
		private void executeService(String serviceName) {
			String userId = this.inData.getValue(AP.loggedInUserFieldName);

			if (serviceCleanser != null) {
				if (!serviceCleanser
						.cleanseBeforeService(this.req, this.inData)) {
					this.outData
							.addMessage(ExilityMessageIds.ERROR,
									"service cleanser failed before service execution. Service not executed.");
					return;
				}
			}

			ServiceAgent agent = new ServiceAgent();
			if (useNewServiceAgent) {
				this.outData = agent.executeService(serviceName, userId,
						this.inData);
			} else {
				agent.serve(serviceName, userId, this.inData, this.outData);
			}

			if (serviceCleanser != null) {
				if (!serviceCleanser
						.cleanseAfterService(this.req, this.outData)) {
					this.outData.addMessage(ExilityMessageIds.ERROR,
							"service cleanser failed after service execution.");
					return;
				}
			}
		}

		/**
		 * Carry out login rituals after a successful execution of login service
		 * 
		 */

		private void doLogin() {
			if (this.outData.getErrorStatus() != CommonFieldNames.SEVERITY_SUCCESS) {
				return;
			}

			String userId = this.outData.getValue(AP.loggedInUserFieldName);

			if (userId == null || userId.length() == 0) {
				this.outData.addMessage(ExilityMessageIds.ERROR,
						"Login process failed");
				Spit.out("Design error: login service succeeded but failed to put value for logged in user id in the field "
						+ AP.loggedInUserFieldName);
				return;
			}

			this.createSession();
			this.resp.setHeader(CommonFieldNames.CSRF_HEADER, this.clientToken);
			/**
			 * put userId in session
			 */
			this.sessionData.addField(AP.loggedInUserFieldName, userId);
			/**
			 * to provide compatibility for the few jsps that are yet to be
			 * re-factored, let us push userId and the csrf token to session
			 */
			this.session.setAttribute(AP.loggedInUserFieldName, userId);

			this.outData.addValue("*_usersession", this.session.getId());
			if (AP.globalServerDataNames != null) {
				this.sessionData.addAllValues(AP.globalServerDataNames,
						this.outData);
			}

			if (serviceCleanser != null) {
				if (!serviceCleanser
						.cleanseAfterService(this.req, this.outData)) {
					this.outData.addMessage(ExilityMessageIds.ERROR,
							"Service cleanser failed after login ");
				}
			}
			/**
			 * we do not encourage cookies. But there are some projects that
			 * need cookies.
			 */
			this.addCookies();
			this.setGlobalFields();
		}

		/**
		 * compatibility with htmlRequestHandler for setting cookies
		 */
		private void addCookies() {
			if (AP.setCookies == null || AP.setCookies.length == 0) {
				return;
			}
			int n = (int) DateUtility.addDays(new Date(), 400).getTime();
			for (String name : AP.setCookies) {
				Cookie cookie = new Cookie(name, this.outData.getValue(name));
				cookie.setPath(this.req.getContextPath());
				if (this.outData.hasValue(name)) {
					Spit.out(" cookie " + name + " is set with value = "
							+ this.outData.getValue(name));
					cookie.setMaxAge(n);
				} else {
					// we have to remove the cookie
					Spit.out(name
							+ " does not have value and hence cookie is not set");
					cookie.setMaxAge(-12);
				}
				this.resp.addCookie(cookie);
			}
		}

		private void setGlobalFields() {
			Map<String, String> globalFields = new HashMap<String, String>();
			for (String name : AP.globalServerDataNames) {
				String val = this.outData.getValue(name);

				if (val == null || val.length() == 0) {
					continue;
				}
				Spit.out("Gloabal Field " + name
						+ " is added and its value is " + val);
				globalFields.put(name, val);
			}
			this.session.setAttribute(
					HtmlRequestHandler.GLOBAL_SERVER_DATA_NAME
							+ this.sessionData.getUserId(), globalFields);
		}

		/***
		 * invalidate session
		 */
		private void doLogout() {
			if (this.session != null) {
				this.session.invalidate();
			}

			this.resp.setHeader(CommonFieldNames.CSRF_HEADER,
					CommonFieldNames.REMOVE_CSRF);
		}

		/***
		 * for each grid in outData, check is client has asked for pagination.
		 * If a field called tableNamePageSize is set, it implies that the
		 * client wants us to paginate this grid and send the first page
		 */
		private void savePaginationData() {
			if (this.outData.grids.size() == 0) {
				return;
			}

			for (String gridName : this.outData.grids.keySet()) {
				String pageSize = this.inData.getValue(gridName
						+ CommonFieldNames.PAGE_SIZE_SUFFIX);
				if (pageSize == null) {
					continue;
				}
				int ps = 0;
				try {
					ps = Integer.parseInt(pageSize);
				} catch (Exception e) {
					this.outData.addMessage(ExilityMessageIds.WARNING,
							"Client sent an invalid page size for " + gridName
									+ CommonFieldNames.PAGE_SIZE_SUFFIX + " : "
									+ pageSize
									+ ". This grid is not paginated.");
					continue;
				}

				String[][] grid = this.outData.grids.get(gridName);

				// grid is definitely there!!
				int n = grid.length - 1;
				if (n <= ps) {
					// total rows is less than or equal to page size. No need to
					// paginate
					this.sessionData.removeGrid(gridName);
				} else {
					Spit.out("Going to paginate " + gridName
							+ " because it has " + n
							+ " rows and page size is " + ps);
					/**
					 * keep this grid in session, and put only the first page
					 * into outData
					 */
					this.sessionData.addGrid(gridName, grid);
					String[][] newGrid = new String[ps + 1][];
					newGrid[0] = grid[0];
					for (int i = 1; i < newGrid.length; i++) {
						newGrid[i] = grid[i];
					}
					this.outData.addGrid(gridName, newGrid);

					/**
					 * tell client that there are more rows. In fact, client
					 * will know about pagination based on the following field
					 */
					this.outData.addValue(gridName
							+ CommonFieldNames.TOTAL_ROWS_SUFFIX,
							Integer.toString(n));
				}
			}
		}

		/***
		 * Get the required page of data into outData based on user request. If
		 * cached data is missing, get fresh data, if possible, using pagination
		 * service
		 */
		private void getPaginationData() {
			String tableName = this.inData
					.getValue(CommonFieldNames.PAGINATION_TABLE_NAME);
			if (tableName == null) {
				this.outData.addMessage(ExilityMessageIds.ERROR,
						"Client did not send table name for paginated data.");
				return;
			}

			int pageSize = 0;
			String txt = this.inData.getValue(CommonFieldNames.PAGE_SIZE);
			try {
				pageSize = Integer.parseInt(txt);

			} catch (Exception e) {
				this.outData
						.addMessage(ExilityMessageIds.ERROR,
								"Client did not send the right page size for pagination request.");
				return;
			}

			int pageNo = 0;
			txt = this.inData.getValue(CommonFieldNames.PAGE_NUMBER);
			if (txt != null && txt.length() > 0) {
				try {
					pageNo = Integer.parseInt(txt);
				} catch (Exception e) {
					this.outData.addMessage(ExilityMessageIds.ERROR,
							"Client requested an invalid page number : " + txt);
					return;
				}
			}

			if (pageNo == 0) {
				/**
				 * page = 0 implies that the client no more needs this paginated
				 * data
				 */
				this.sessionData.removeGrid(tableName);
				return;
			}

			String[][] grid = this.sessionData.getGrid(tableName);
			if (grid == null) {
				/**
				 * pagination data is missing. Re-login case. We allow client to
				 * specify the service to be used to get data fresh on a need
				 * basis
				 */
				String newService = this.inData
						.getValue(PAGINATION_SERVICE_FIELD_NAME);
				if (newService == null) {
					/**
					 * client has NOT provided the service to fire to get data
					 * again.
					 * 
					 */
					this.outData
							.addMessage(
									ExilityMessageIds.ERROR,
									"Pagination data is not available, probably because you have re-logged-in. Please resubmit your query, or reset page.");
					return;
				}
				this.executeService(newService);
				grid = this.outData.getGrid(tableName);
				if (grid == null) {
					this.outData.addMessage(ExilityMessageIds.ERROR,
							"Pagination service " + newService
									+ " did not provide data in table "
									+ tableName);
					return;
				}
				this.sessionData.addGrid(tableName, grid);
				/**
				 * TODO: It is possible that the service would have returned
				 * different sets of rows than the one returned before
				 * re-login!!!
				 */
			}

			int totalRows = grid.length - 1;

			String columnToSort = this.inData
					.getValue(CommonFieldNames.SORT_COLUMN);
			if (columnToSort != null) {
				String sortDesc = this.inData
						.getValue(CommonFieldNames.SORT_DESC);
				boolean toSortDesc = sortDesc != null && sortDesc.equals("1");
				Util.sortGrid(grid, columnToSort, toSortDesc);
			}

			int istart = (pageNo - 1) * pageSize + 1; // it is 1 based

			// in case of invalid page no, let us reset and start with one
			if (istart > totalRows) {
				istart = 1;
			}

			int iend = istart + pageSize - 1;

			// AND, if this is the past page, we may not have enough rows to
			// fill-it
			if (iend > totalRows) {
				pageSize = totalRows - istart + 1;
			}
			String[][] newGrid = new String[pageSize][];
			for (int i = 0; i < newGrid.length; i++) {
				newGrid[i] = grid[istart];
				istart++;
			}

			this.outData.addGrid(tableName, newGrid);
			return;
		}

		/**
		 * try to login this user using sso
		 * 
		 * @return
		 */
		private boolean trySso() {
			boolean cleared = HttpRequestHandler.ssoObject.signIn(this.req,
					this.resp, this.inData, this.outData, this.sessionData);
			if (cleared) {
				String userId = this.outData.getValue(AP.loggedInUserFieldName);
				if (userId == null || userId.length() == 0) {
					Spit.out("Design error: "
							+ HttpRequestHandler.ssoObject.getClass()
									.getSimpleName()
							+ ".signIn() has should set put value of userId in the field "
							+ AP.loggedInUserFieldName);
					cleared = false;
				}
			}
			if (cleared) {
				this.doLogin();
				return true;
			}
			return false;
		}
	}
}
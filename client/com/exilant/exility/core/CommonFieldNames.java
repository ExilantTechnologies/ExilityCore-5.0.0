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

/***
 * Not a class, but a place holder that has all the naming conventions that are
 * part of Exility protocol for client-server communication
 * 
 */
public abstract class CommonFieldNames {
	/***
	 * special service request, part of the protocol, that is used for
	 * server-side pagination using session
	 */
	public static final String PAGINATION_SERVICE_ID = "paginationService";

	/***
	 * I think this is not used anymore. Let me keep it commented for a while,
	 * just in case some projects use this...
	 */
	// public static final String REDIRECTED =
	// "redirectedFromPaginationService";

	/***
	 * by default, session-cached data is used to deliver pagination. Designer
	 * can opt to have a specific service to be called instead of this data.
	 * This is the field name in which client sends the name of service to be
	 * used to get next page of data.
	 */
	public static final String PAGINATION_SERVICE_FIELD_NAME = "paginationServiceName";

	/***
	 * Pagination service also provides sorting. field name that contains the
	 * name of the column to be used for sorting.
	 */
	public static final String SORT_COLUMN = "paginationColumnToSort";

	/***
	 * ascending is the default sort order. Client can send a TRUE boolean value
	 * in this field to change the sort order to descending
	 */
	public static final String SORT_DESC = "paginationSortDesc";

	/***
	 * When user clicks on a column to sort, normally we sort and show the first
	 * page as per new sorted order. Client can instead ask for a page that
	 * contains a specific row. Row can be specified with the name of column to
	 * match and the value to match. e.g. paginationColumnToMatch="customerId"
	 * and paginationValueToMatch="1321"
	 */
	public static final String MATCH_COLUMN = "paginationColumnToMatch";

	/***
	 * When user clicks on a column to sort, normally we sort and show the first
	 * page as per new sorted order. Client can instead ask for a page that
	 * contains a specific row. Row can be specified with the name of column to
	 * match and the value to match. e.g. paginationColumnToMatch="customerId"
	 * and paginationValueToMatch="1321"
	 */
	public static final String MATCH_VALUE = "paginationValueToMatch";

	/***
	 * <tableName>PageSize is an indicator that the data for the table is to be
	 * paginated on the server. e.g. customersPageSize=40 means the rows in the
	 * grid names "customers" should be paginated
	 */
	public static final String PAGE_SIZE_SUFFIX = "PageSize";

	/**
	 * page size field name inside pagination service
	 */
	public static final String PAGE_SIZE = "pageSize";

	/**
	 * tableNameTotalRows has the total number of rows extracted for this table.
	 * This field is sent to client ONLY IF pagination is requested AND the
	 * number of rows is greater than page size
	 */
	public static final String TOTAL_ROWS_SUFFIX = "TotalRows";

	/**
	 * 1-based page number for which data is being sent
	 */
	public static final String PAGE_NUMBER_SUFFIX = "PageNo";

	/**
	 * field name specific to pagination service
	 */
	public static final String PAGE_NUMBER = "pageNo";

	/**
	 * name of the table being paginated
	 */
	public static final String PAGINATION_TABLE_NAME = "tableName";

	/***
	 * name of list service
	 */
	public static final String LIST_SERVICE_ID = "listService";

	/**
	 * name of report service
	 */
	public static final String REPORT_SERVICE_ID = "reportService";
	/***
	 * name of the grid in which list service ids are requested from client
	 */
	public static final String LIST_SERVICE_IDS = "listServiceIds";

	/***
	 * field that comes from client that indicates whether server trace is to be
	 * sent to client
	 */
	public static final String TRACE_REQUEST = "exilityServerTrace";

	/***
	 * field that contains the server trace
	 */
	public static final String TRACE_TEXT = "exilityServerTraceText";

	/**
	 * we are sticking to this standard
	 */
	public static final String CHAR_ENCODING = "UTF-8";

	/**
	 * field in which service name is being sent
	 */
	public static final String SERVICE_ID = "serviceId";

	/***
	 * If the service is submitted for as a background job, dc has an id of this
	 * job. It is expected that the background job will write out its output dc
	 * as a serialzed text into a file with this name in the designated folder
	 */
	public static final String BACKGROUND_JOB_ID = "_backgroundJobId";

	/**
	 * current status of this job
	 */
	public static final String BACKGROUND_JOB_STATUS = "_backgroundJobStatus";

	/**
	 * status of the job when it is running
	 */
	public static final String JOB_IS_RUNNING = "running";

	/**
	 * status of the job when it is done
	 */
	public static final String JOB_IS_DONE = "done";

	/**
	 * comma separated list of all jobs that are active
	 */
	public static final String BACKGROUND_JOBS = "_backgroundJobs";

	/**
	 * service name to ask for the result of a job
	 */
	public static final String GET_BACKGROUND_JOB_RESULT = "getBackgroundJobResult";

	/**
	 * file name for exility resources
	 */
	public static final String FILE_NAME = "fileName";

	/**
	 * exility version of server
	 */
	public static final String VERSION = "exilityVersion";

	/**
	 * grid name in dc in which languages are sent to client
	 */
	public static final String LANGUAGES_TABLE = "languages";

	/**
	 * column name in LANGUAGES_TABLE that has the list of languages
	 */
	public static final String LANGUAGES_TABLE_COLUMN = "language";

	/**
	 * field name with which we tell client whether the service succeeded or not
	 */
	public static final String SUCCESS_FIELD_NAME = "_success";

	/**
	 * grid name in which we send values back and forth
	 */
	public static final String VALUES_TABLE_NAME = "values";

	/**
	 * grid name that has all messages from server to client
	 */
	public static final String MESSAGES_TABLE_NAME = "_messages";
	/**
	 * separator character used for serializing
	 */
	public static final String TABLE_SEPARATOR = (char) 28 + "";// ASCII file
																// separator
	/**
	 * separator character used for serializing
	 */
	public static final String BODY_SEPARATOR = (char) 29 + "";// ASCII group
																// separator
	/**
	 * separator character used for serializing
	 */
	public static final String ROW_SEPARATOR = (char) 30 + "";// ASCII record
																// separator
	/**
	 * separator character used for serializing
	 */
	public static final String FIELD_SEPARATOR = (char) 31 + "";// ASCII unit
																// separator

	/**
	 * severity if the message is about a successful operation
	 */
	public static final int SEVERITY_SUCCESS = 0;
	/**
	 * severity when it is a warning
	 */
	public static final int SEVERITY_WARNING = 1;
	/**
	 * severity on error
	 */
	public static final int SEVERITY_ERROR = 3;

	/**
	 * sheet name that has values
	 */
	public static final String VALUES_SHEET_NAME = "_values";

	/**
	 * this is the name of the token that is sent to the client. Client MUST
	 * send this field with this value with each request. This mechanism takes
	 * care of security issue ????
	 */
	public static final String CLIENT_TOKEN_NAME = "cheeti";
	/**
	 * field in dc that has the authentication status
	 */
	public static final String AUTHENTICATION_FIELD_NAME = "authenticationStatus";
	/***
	 * to be re-factored into enumerations
	 */
	public static final int SECURITY_CLEARED = 0;
	/**
	 * we find userId in cookie, but not in session
	 */
	public static final int SECURITY_SESSION_EXPIRED = 1;

	/**
	 * userId cookie is not found
	 */
	public static final int SECURITY_NOT_LOGGED_IN = 2;

	/**
	 * HTTP header with which to exchange CSRF token
	 */
	public static final String CSRF_HEADER = "X-CSRF-Token";

	/**
	 * if the user is not logged-in, this will let the client know how many
	 * logins are already active from this client
	 */
	public static final String NUMBER_OF_EXISTING_LOGINS = "_nbr_existing_logins";

	/**
	 * value of the header that instructs the client to remove the CSRF token
	 */
	public static final String REMOVE_CSRF = "remove";

	/**
	 * field that directs the server to run the requested service in the
	 * background and return to teh caller immediately
	 */
	public static final String TO_BE_RUN_IN_BACKGROUND = "_toBeRunInBackground";

	/**
	 * suffix used for the operator field associated with a filter field
	 */
	public static final String FILTER_OPERATOR_SUFFIX = "Operator";

	/**
	 * suffix used for the To-field associated with a filter field
	 */
	public static final String FILTER_TO_SUFFIX = "To";

	/**
	 * column name in a grid that contains the operation to be carried out on
	 * that row. Traditionally known as bulk-action thatis supported with
	 * bulk-task
	 */
	public static final String BULK_ACTION = "bulkAction";
}
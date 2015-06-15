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

import java.util.Date;

/**
 * 
 * data structure that holds various configuration parameters.
 */
public class ApplicationParameters implements ToBeInitializedInterface {
	/**
	 * The name of the project
	 */
	String projectName;
	/**
	 * If true, cache the entity definitions for faster access. Please note,
	 * caching entities could also mean when you access it next time, you may
	 * have field values present in the cache from the last operation
	 */
	boolean definitionsToBeCached = false;
	/**
	 * If true, cache the primary keys loaded from Xml definition
	 */
	boolean cachePrimaryKeys = true;
	/**
	 * should the trace text be sent to client or put into log file? Typically,
	 * in dev environment we put trace = true, and set it to false in production
	 */
	boolean trace = true;

	/**
	 * The database function that is used to access the current timestamp
	 */
	String systemDateFunction = "SYSDATE";
	/**
	 * The database connection string
	 */
	String connectionString = "";
	/**
	 * Prefix for date format
	 */
	String dateFormattingPrefix = "'";
	/**
	 * Suffix for date format
	 */
	String dateFormattingPostfix = "'";
	/**
	 * Prefix for datetime format
	 */
	String dateTimeFormattingPrefix = "'";
	/**
	 * Suffix for datetime format
	 */
	String dateTimeFormattingPostfix = "'";
	/**
	 * By what key, the logged in user name is stored in DataCollection object
	 */
	String loggedInUserFieldName = "userId";
	/**
	 * Whether to use quotes in generated SQL or not
	 */
	boolean useQuotesInSql = false;
	/**
	 * The database drive to use
	 */
	String dbDriver = "";
	/**
	 * Should empty strings be treated as null values
	 */
	boolean useNullForEmptyString = false;
	/**
	 * TODO::
	 */
	boolean uniqueNamesAcrossGroups = false;
	/**
	 * The maximum date value allowed for the application
	 */
	Date globalMaxDate = null;
	/**
	 * The minimum date value allowed for the application
	 */
	Date globalMinDate = null;
	/**
	 * How are multiple data elements separated from one another
	 */
	String dataElementSeparator = ".";
	/**
	 * TODO:
	 */
	String[] globalServerDataNames = {};
	/**
	 * The service identifier used to perform login
	 */
	String loginServiceId;
	/**
	 * The service identifier used to perform logout action
	 */
	String logoutServiceId;
	/**
	 * What is the column name used across tables that hold the primary key of
	 * the table
	 */
	String commonPrimaryKeyColumnName = "id";
	/**
	 * Prefix to use for naming an image file uploaded to server
	 */
	String imageFilePrefix = "";
	/**
	 * Prefix to use for folders on the server side
	 */
	String commonFolderPrefix = "";
	/**
	 * The folder prefix for exility internal usage and exility classes
	 */
	String exilityFolderPrefix = "";
	/**
	 * The layout type identifier for the html pages
	 */
	String pageLayoutType = "0";
	/**
	 * The default page height
	 */
	int defaultPageHeight = 0;
	/**
	 * Default page width
	 */
	int defaultPageWidth = 0;
	/**
	 * Default size of rows to display per page
	 */
	int defaultPaginationSize = 0;
	/**
	 * It true, Exility will cache data received from server on the client side.
	 * This is discontinued
	 */
	// boolean cachePaginationDataOnClient = false;
	/**
	 * The style of pagination buttons
	 */
	String paginateButtonType = "linear";
	/**
	 * The names of cookies to set
	 */
	String[] setCookies = null;
	/**
	 * Get cookie names set
	 */
	String[] getCookies = null;
	/**
	 * TODO::
	 */
	String fileUploadMeans = "0";
	/**
	 * Path to save the uploaded files
	 */
	String filePath = "";
	/**
	 * TODO:
	 */
	String cleanserName = null;
	/**
	 * TODO::
	 */
	String excelFilePath = "";
	/**
	 * TODO::
	 */
	String batchConnectionString = null;
	/**
	 * TODO::
	 */
	String ExcelTemplatesPath = "";
	/**
	 * TODO::
	 */
	String excelReportFileSavePath = "";
	/**
	 * User id from which the system will send emails
	 */
	String eMailFromUserId = "";
	/**
	 * Password of the user id which the system will use to send emails
	 */
	String eMailFromPassword = "";
	/**
	 * The email body text
	 */
	String eMailBody = "";
	/**
	 * Email server name (IP/DNS)
	 */
	String eMailHost = "";
	/**
	 * The email service port
	 */
	String eMailPort = "";
	/**
	 * TODO:
	 */
	boolean showIamgeForDeleteOption = false;
	/**
	 * TODO::
	 */
	boolean showDeleteOptionAtEnd = false;
	/**
	 * Specifies the default date format to use with the TO_CHAR and TO_DATE
	 * functions in Oracle
	 */
	String nlsDateFormat;
	/**
	 * TODO::
	 */
	boolean licenceValidation = false;
	/**
	 * TODO::
	 */
	String cleanserNameSpace = null;
	/**
	 * TODO::
	 */
	String cleanserAssemblyName = null;
	/**
	 * TODO::
	 */
	boolean lastKeyEventTrigger = false;
	/**
	 * SQL command timeout
	 */
	int commandTimeOutTime = 0;
	/**
	 * If true, for each transaction in database an audit entry will be created
	 */
	boolean enableAuditForAll = false;
	/**
	 * Indicates if audit schema is separate from main database schema
	 */
	boolean isSeparateAuditSchema = false;
	/**
	 * What is the suffix used for the audit table (e.g. Audit table for main
	 * table Employee can be named as EmployeeActionAudit, ActionAudit is the
	 * suffix)
	 */
	String audittableSuffix = null;
	/**
	 * Connection string for creating audit entries
	 */
	String auditConnectionString = null;
	/**
	 * TODO::
	 */
	boolean alignPanels = false;
	/**
	 * TODO::
	 */
	boolean httpNoCacheTagRequires = false;
	/**
	 * TODO::
	 */
	boolean spanForButtonPanelRequires = false;
	/**
	 * TODO::
	 */
	boolean showRequiredLabelinGrid = false;
	/**
	 * Provide the root of HTML files relative to the resources folder
	 */
	String htmlRootRelativeToResourcePath = "";
	/**
	 * How do we track logged in user. If we use the username, make this false
	 * If we use a numeric value to track the user, make this true
	 */
	boolean loggedInUserDataTypeIsInteger = false;
	/**
	 * If data is missing, can it safely be assumed as text
	 */
	boolean assumeTextForMissingDataElement = false;
	/**
	 * should reset action get confirmation before resetting field values?
	 */
	public boolean quietResetAction = false;
	/**
	 * Holds the package name in which custom/autogenerated table entities are
	 * kept
	 */
	String projectPackageName = "";

	/**
	 * where do we show * for a field that is mandatory
	 */
	int starForRequiredField = AP.STAR_NOT_SPECIFIED;
	/***
	 * <col> tags to be generated for tables? If set to true, column widths can
	 * be controlled using css
	 */
	boolean generateColTags = false;
	/***
	 * use JNDI for confidential data like password
	 */
	String dataSource = null;
	String auditDataSource = null;

	boolean securityEnabled = true;

	/***
	 * in table.xml, to pick up value from a parent table, dataSource utility is
	 * used. Projects have exploited this to use that conditionally. While Exis
	 * wants to treat 0 as valid value PthFinder wants to treat 0 as not
	 * available. To settle this desired feature, we have this boolean.
	 */
	boolean treatZeroAndEmptyStringAsNullForDataSource = false;
	String trueValueForSql = "1";
	String falseValueForSql = "0";
	boolean enforceNullPolicyForText = false;

	/**
	 * field name as used by the application
	 */
	String createdByFieldName = null;

	/**
	 * column name as defined in rdbms
	 */
	String createdByColumnName = null;
	/**
	 * field name as used by the application
	 */
	String createdAtFieldName = null;
	/**
	 * column name as defined in rdbms
	 */
	String createdAtColumnName = null;
	/**
	 * field name as used by the application
	 */
	String modifiedByFieldName = null;
	/**
	 * column name as defined in rdbms
	 */
	String modifiedByColumnName = null;
	/**
	 * field name as used by the application
	 */
	String modifiedAtFieldName = null;
	/**
	 * column name as defined in rdbms
	 */
	String modifiedAtColumnName = null;

	/**
	 * maximum rows a client can send to a service, as a defense against too
	 * many rows being sent by a rogue client
	 */
	int maxInputRows = AP.DEFAULT_MAX_ROWS;

	/**
	 * Default constructor
	 */
	public ApplicationParameters() {
	}

	@Override
	public void initialize() {
		// Spit.out("ap.starForRequiredField is " + this.starForRequiredField);
		if (this.starForRequiredField == AP.STAR_NOT_SPECIFIED) {
			if (this.pageLayoutType.equals("5")) {
				this.starForRequiredField = AP.STAR_BEFORE_LABEL;
			} else {
				this.starForRequiredField = AP.STAR_AFTER_LABEL;
			}
			// Spit.out("ap.starForRequiredField SET TO " +
			// this.starForRequiredField);
		}
	}
}

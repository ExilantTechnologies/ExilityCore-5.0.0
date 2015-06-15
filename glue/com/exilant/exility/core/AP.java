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
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * class with static attributes that are used for configuration. BAD DESIGN. We
 * will refactor this some time into a better design.
 */
public class AP {
	// valid values for starForReuiredField
	/**
	 * page has not specified this
	 */
	public static final int STAR_NOT_SPECIFIED = 0;

	/**
	 * use * after label
	 */
	public static final int STAR_AFTER_LABEL = 1;

	/**
	 * use star before label
	 */
	public static final int STAR_BEFORE_LABEL = 2;

	/**
	 * do not put start for labels
	 */
	public static final int NO_STAR_FOR_LABEL = 3;

	/**
	 * max rows that a client can send in any table. This is a safety against a
	 * rogue client trying to choke the server
	 */
	static final int DEFAULT_MAX_ROWS = 100000;
	/**
	 * default file name for app parameters
	 */
	public static String parametersFileName = "applicationParameters";

	private static ApplicationParameters instance = null;

	/**
	 * TODO: All attributes are public. I am not sure when and why we did this.
	 * This is definitely not a good idea. We should make this package-private.
	 * With that idea, I had lived with attributes rather than getters.
	 */
	public static String projectName;
	/**
	 * during development, definitions are changed often, hence we are better
	 * off not caching them
	 */
	public static boolean definitionsToBeCached;

	/**
	 * If this is the only app that manipulates the data base, primary key
	 * values can be cached However during development,it is common that the app
	 * servers from each developers machine will be using a common database.
	 * Hence this options during development.
	 * 
	 */
	public static boolean cachePrimaryKeys;

	/**
	 * we use a simple tracing mechanism. Programmers use Spit.out instead of
	 * System.out.println Spit.out will not output anything if trace if off. If
	 * trace is on, it uses System.out Also, if trace is on, all sqls generated
	 * are also put into dc, so that they can be visible to the client
	 */
	public static boolean trace;

	/**
	 * security can be disabled during testing, providing more flexibility
	 */
	public static boolean securityEnabled = true;

	/**
	 * SQL date function used to add current date
	 */
	public static String systemDateFunction;

	/**
	 * Oracle requires date conversion function when inserting a date field. I
	 * am generalizing it as DateFormatterPrefix + dateFieldAsStringWithNoQuotes
	 * + DateFormatterPostFix e.g. for SqlServer, prefix = "'" and postfix = "'"
	 * for Oracle prefix = " TO_DATE('" and postfix = "','MM-DD-YY HH24:MI:SS')"
	 * this is to be modified further, but this flag is used at this time
	 */
	public static String dateFormattingPrefix;

	/**
	 * refer dateFormattingPrefix
	 */
	public static String dateFormattingPostfix;
	/**
	 * refer dateFormattingPrefix
	 */
	public static String dateTimeFormattingPrefix;
	/**
	 * refer dateFormattingPrefix
	 */
	public static String dateTimeFormattingPostfix;

	/**
	 * refer dateFormattingPrefix
	 */
	public static String connectionString;

	/**
	 * what is the name with which logged in user name is put in dc?
	 */
	public static String loggedInUserFieldName;

	/**
	 * whether to surround all db objects with quotes or not
	 */
	public static boolean useQuotesInSql;

	/**
	 * driver manager class for JDBC
	 */
	public static String dbDriver;

	/**
	 * oracle has this issue that empty strings cannot be saved into non-null
	 * text columns
	 */
	public static boolean useNullForEmptyString;

	/**
	 * funny. useNullForEmptyString was set to true in path finder, but it was
	 * actually not working due to a bug in SqlParamater. Path Finder assumed
	 * this to be feature and moved on. Evincus wanted to use the feature, and
	 * they reported it is not working. We fixed it for them, but then
	 * PathFinder reported issue because of this bug!!
	 * 
	 * Since PathFinder is in production, we are providing backward
	 * compatibility for the bug, and providing this new flag to be used
	 * no-onwards
	 */
	public static boolean enforceNullPolicyForText = false;
	/**
	 * 
	 */
	public static boolean uniqueNamesAcrossGroups;

	/**
	 * 
	 */
	public static Date globalMinDate;
	/**
	 * 
	 */
	public static Date globalMaxDate;
	/**
	 * 
	 */
	public static String dataElementSeparator;
	/**
	 * 
	 */
	public static String[] globalServerDataNames;
	/**
	 * 
	 */
	public static String loginServiceId;
	/**
	 * 
	 */
	public static String logoutServiceId;
	/**
	 * 
	 */
	public static String commonPrimaryKeyColumnName;
	/**
	 * 
	 */
	public static String imageFilePrefix;
	/**
	 * 
	 */
	public static String commonFolderPrefix;
	/**
	 * 
	 */
	public static String exilityFolderPrefix;
	/**
	 * 
	 */
	public static String pageLayoutType;
	/**
	 * 
	 */
	public static int defaultPageHeight;
	/**
	 * 
	 */
	public static int defaultPageWidth;
	/**
	 * 
	 */
	public static int defaultPaginationSize;
	/**
	 * 
	 */
	// public static boolean cachePaginationDataOnClient;
	public static String paginateButtonType;
	/**
	 * 
	 */
	public static String[] setCookies = null;
	/**
	 * 
	 */
	public static String[] getCookies = null;
	/**
	 * 
	 */
	public static String fileUploadMeans;
	/**
	 * 
	 */
	public static String filePath;
	/**
	 * 
	 */
	public static String cleanserName;
	/**
	 * 
	 */
	public static String excelFilePath;
	/**
	 * 
	 */
	public static String batchConnectionString;
	/**
	 * 
	 */
	public static String ExcelTemplatesPath;
	/**
	 * 
	 */
	public static String excelReportFileSavePath;
	/**
	 * 
	 */
	public static String eMailFromUserId;
	/**
	 * 
	 */
	public static String eMailFromPassword;

	/**
	 * 
	 */
	public static String eMailBody;
	/**
	 * 
	 */
	public static String eMailHost;
	/**
	 * 
	 */
	public static String eMailPort;
	/**
	 * 
	 */
	public static boolean showIamgeForDeleteOption;
	/**
	 * 
	 */
	public static boolean showDeleteOptionAtEnd;
	/**
	 * 
	 */
	public static String nlsDateFormat;
	/**
	 * 
	 */
	public static boolean licenceValidation;
	/**
	 * 
	 */
	public static String cleanserNameSpace;
	/**
	 * 
	 */
	public static String cleanserAssemblyName;
	/**
	 * 
	 */
	public static boolean lastKeyEventTrigger;
	/**
	 * 
	 */
	public static int commandTimeOutTime;
	/**
	 * 
	 */

	public static boolean enableAuditForAll = false;
	/**
	 * 
	 */
	public static boolean isSeparateAuditSchema = false;
	/**
	 * 
	 */
	public static String audittableSuffix = null;
	/**
	 * 
	 */
	public static String auditConnectionString;
	/**
	 * 
	 */
	public static boolean alignPanels = false;
	/**
	 * 
	 */
	public static boolean httpNoCacheTagRequires = false;
	/**
	 * 
	 */
	public static boolean spanForButtonPanelRequires = false;
	/**
	 * 
	 */
	public static boolean showRequiredLabelinGrid = false;
	/**
	 * 
	 */
	public static String htmlRootRelativeToResourcePath = null;
	/**
	 * 
	 */
	public static boolean loggedInUserDataTypeIsInteger = false;
	/**
	 * 
	 */
	public static boolean assumeTextForMissingDataElement = false;
	/**
	 * 
	 */
	public static boolean quietResetAction = false;
	/**
	 * 
	 */
	public static Set<String> cookiesToBeExtracted = null;
	/**
	 * 
	 */
	public static String projectPackageName = null;
	/**
	 * 
	 */
	public static int starForRequiredField = AP.STAR_AFTER_LABEL;
	/**
	 * 
	 */
	public static boolean generateColTags = false;
	/**
	 * 
	 */
	public static String dataSource = null;
	/**
	 * 
	 */
	public static String auditDataSource = null;
	/**
	 * 
	 */
	public static boolean treatZeroAndEmptyStringAsNullForDataSource = false;
	/**
	 * 
	 */
	public static String trueValueForSql = "1";
	/**
	 * 
	 */
	public static String falseValueForSql = "0";

	/*
	 * default standard field names that are used for auto generation of
	 * table.xml files from rdbms table
	 */

	/**
	 * field name as used by the application
	 */
	public static String createdByFieldName = null;

	/**
	 * column name as defined in rdbms
	 */
	public static String createdByColumnName = null;
	/**
	 * field name as used by the application
	 */
	public static String createdAtFieldName = null;
	/**
	 * column name as defined in rdbms
	 */
	public static String createdAtColumnName = null;
	/**
	 * field name as used by the application
	 */
	public static String modifiedByFieldName = null;
	/**
	 * column name as defined in rdbms
	 */
	public static String modifiedByColumnName = null;
	/**
	 * field name as used by the application
	 */
	public static String modifiedAtFieldName = null;
	/**
	 * column name as defined in rdbms
	 */
	public static String modifiedAtColumnName = null;

	/**
	 * max input rows from client for any service
	 */
	public static int maxInputRows = DEFAULT_MAX_ROWS;

	/**
	 * Get an instance of AP
	 * 
	 * @return current singleton AP. Used internally by Exility
	 */
	public static ApplicationParameters getInstance() {
		return AP.instance;
	}

	static synchronized void load() {
		ApplicationParameters ap = (ApplicationParameters) ResourceManager
				.loadResourceFromFile("applicationParameters.xml",
						ApplicationParameters.class);
		if (ap == null) {
			Spit.out("Unable to get applicationParametrs.xml");
			ap = new ApplicationParameters();
		}
		AP.setInstance(ap);
	}

	/**
	 * set this parameters as instance to be used
	 * 
	 * @param ap
	 */
	public static void setInstance(ApplicationParameters ap) {
		AP.instance = ap;
		AP.projectName = ap.projectName;
		AP.definitionsToBeCached = ap.definitionsToBeCached;
		AP.cachePrimaryKeys = ap.cachePrimaryKeys;
		AP.trace = ap.trace;
		AP.systemDateFunction = ap.systemDateFunction;
		AP.connectionString = ap.connectionString;
		AP.dateFormattingPrefix = ap.dateFormattingPrefix;
		AP.dateFormattingPostfix = ap.dateFormattingPostfix;
		AP.dateTimeFormattingPrefix = ap.dateTimeFormattingPrefix;
		AP.dateTimeFormattingPostfix = ap.dateTimeFormattingPostfix;
		AP.loggedInUserFieldName = ap.loggedInUserFieldName;
		AP.useQuotesInSql = ap.useQuotesInSql;
		AP.dbDriver = ap.dbDriver;
		AP.useNullForEmptyString = ap.useNullForEmptyString;
		AP.enforceNullPolicyForText = ap.enforceNullPolicyForText;
		AP.uniqueNamesAcrossGroups = ap.uniqueNamesAcrossGroups;
		AP.globalMinDate = ap.globalMinDate;
		AP.globalMaxDate = ap.globalMaxDate;
		AP.dataElementSeparator = ap.dataElementSeparator;
		AP.globalServerDataNames = ap.globalServerDataNames;
		AP.loginServiceId = ap.loginServiceId;
		AP.logoutServiceId = ap.logoutServiceId;
		AP.imageFilePrefix = ap.imageFilePrefix;
		AP.commonFolderPrefix = ap.commonFolderPrefix;
		AP.exilityFolderPrefix = ap.exilityFolderPrefix;
		AP.pageLayoutType = ap.pageLayoutType;
		AP.defaultPageHeight = ap.defaultPageHeight;
		AP.defaultPageWidth = ap.defaultPageWidth;
		AP.commonPrimaryKeyColumnName = ap.commonPrimaryKeyColumnName;
		AP.defaultPaginationSize = ap.defaultPaginationSize;
		// AP.cachePaginationDataOnClient = ap.cachePaginationDataOnClient;
		AP.paginateButtonType = ap.paginateButtonType;
		AP.fileUploadMeans = ap.fileUploadMeans;
		AP.filePath = ap.filePath;
		AP.setCookies = ap.setCookies;
		AP.cookiesToBeExtracted = null;
		AP.getCookies = ap.getCookies;
		if (ap.getCookies != null) {
			AP.cookiesToBeExtracted = new HashSet<String>();
			for (String cuki : ap.getCookies) {
				AP.cookiesToBeExtracted.add(cuki);
			}
		}
		AP.cleanserName = ap.cleanserName;
		AP.excelFilePath = ap.excelFilePath;
		AP.batchConnectionString = ap.batchConnectionString;
		AP.ExcelTemplatesPath = ap.ExcelTemplatesPath;
		AP.excelReportFileSavePath = ap.excelReportFileSavePath;
		AP.eMailFromUserId = ap.eMailFromUserId;
		AP.eMailFromPassword = ap.eMailFromPassword;
		AP.eMailBody = ap.eMailBody;
		AP.eMailHost = ap.eMailHost;
		AP.eMailPort = ap.eMailPort;
		AP.showIamgeForDeleteOption = ap.showIamgeForDeleteOption;
		AP.showDeleteOptionAtEnd = ap.showDeleteOptionAtEnd;
		AP.nlsDateFormat = ap.nlsDateFormat;
		AP.licenceValidation = ap.licenceValidation;
		AP.cleanserNameSpace = ap.cleanserNameSpace;
		AP.cleanserAssemblyName = ap.cleanserAssemblyName;
		AP.lastKeyEventTrigger = ap.lastKeyEventTrigger;
		AP.commandTimeOutTime = ap.commandTimeOutTime;

		AP.enableAuditForAll = ap.enableAuditForAll;
		AP.isSeparateAuditSchema = ap.isSeparateAuditSchema;
		AP.audittableSuffix = ap.audittableSuffix;
		AP.auditConnectionString = ap.auditConnectionString;
		AP.dataSource = ap.dataSource;
		AP.auditDataSource = ap.auditDataSource;
		if (!AP.isSeparateAuditSchema) {
			AP.auditConnectionString = ap.connectionString;
			AP.auditDataSource = ap.dataSource;
		}

		AP.alignPanels = ap.alignPanels;
		AP.httpNoCacheTagRequires = ap.httpNoCacheTagRequires;
		AP.spanForButtonPanelRequires = ap.spanForButtonPanelRequires;
		AP.showRequiredLabelinGrid = ap.showRequiredLabelinGrid;
		AP.htmlRootRelativeToResourcePath = ap.htmlRootRelativeToResourcePath;
		Spit.out("htmlRootRelativeToResourcePath et to "
				+ ap.htmlRootRelativeToResourcePath);
		AP.loggedInUserDataTypeIsInteger = ap.loggedInUserDataTypeIsInteger;
		AP.assumeTextForMissingDataElement = ap.assumeTextForMissingDataElement;
		AP.quietResetAction = ap.quietResetAction;
		AP.projectPackageName = ap.projectPackageName;
		AP.starForRequiredField = ap.starForRequiredField;
		AP.generateColTags = ap.generateColTags;
		AP.securityEnabled = ap.securityEnabled;
		AP.treatZeroAndEmptyStringAsNullForDataSource = ap.treatZeroAndEmptyStringAsNullForDataSource;
		AP.trueValueForSql = ap.trueValueForSql;
		AP.falseValueForSql = ap.falseValueForSql;
		AP.createdAtFieldName = ap.createdAtFieldName;
		AP.createdAtColumnName = ap.createdAtColumnName;
		AP.createdByFieldName = ap.createdByFieldName;
		AP.createdByColumnName = ap.createdByColumnName;
		AP.modifiedByFieldName = ap.modifiedByFieldName;
		AP.modifiedByColumnName = ap.modifiedByColumnName;
		AP.modifiedAtFieldName = ap.modifiedAtFieldName;
		AP.modifiedAtColumnName = ap.modifiedAtColumnName;
		AP.maxInputRows = ap.maxInputRows;

	}

	static void setFileName(String applicationParametersFileNameWithNoExtension) {
		AP.parametersFileName = applicationParametersFileNameWithNoExtension;
	}
}

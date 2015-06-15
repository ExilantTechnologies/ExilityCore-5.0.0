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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.sql.DataSource;

/***
 * Encapsulates most RDBMS aspects. Single class through which all data base
 * requests are routed through.
 * 
 */
public class DbHandle {
	// sqls used for extracting schema details
	private static final String TABLE_SELECT_SQL = "select TABLE_NAME as \"tableName\", TABLE_TYPE as \"tableType\" from INFORMATION_SCHEMA.TABLES ORDER BY TABLE_NAME";
	private static final String COLUMN_SELECT_SQL = "select COLUMN_NAME as \"columnName\", DATA_TYPE  as \"sqlDataType\", IS_NULLABLE  as \"isNullable\", COLUMN_DEFAULT  as \"defaultValue\", CHARACTER_MAXIMUM_LENGTH  as \"size\", NUMERIC_PRECISION  as \"precision\", NUMERIC_SCALE as \"scale\" from INFORMATION_SCHEMA.COLUMNS order by ORDINAL_POSITION where TABLE_NAME = '";
	private static final int FOR_INPUT = 0;
	private static final int FOR_OUTPUT = 1;
	private static final int FOR_INPUT_AND_OUTPUT = 2;

	/***
	 * data source for connection
	 */
	private static DataSource dataSource;

	/***
	 * data source for audit
	 */
	private static DataSource auditDataSource;
	/**
	 * Some RDBMS specific logic required. As of now, it is only for Oracle. If
	 * we need more complex logic, we should re-factor to a more generic design
	 */
	private static boolean oracleIsUsed = AP.dbDriver.contains("oracle")
			|| AP.connectionString.contains("oracle");

	/**
	 * just the presence of of db connection string is to imply whether audit is
	 * required or not.
	 */
	private static boolean dbIsAudited = AP.auditConnectionString != null
			&& AP.auditConnectionString.length() > 0;

	/***
	 * in case data source is not used, we store the connection string
	 */
	private static String connectionString;
	private static String auditConnectionString;
	private static String nlsDateFormat;

	// TODO: connection object. Should we store a command object instead? Is it
	// a good idea to reuse a command rather than using con.createCommand()?
	/***
	 * connection to the RDBMS
	 */
	private Connection con = null;
	/***
	 * audit connection
	 */
	private Connection auditcon = null;

	/**
	 * should we suppress logging sqls?
	 */
	private boolean suppressSqlLog = false;

	/***
	 * Is the connection opened for update? Actually this is a derived field
	 * from dataAccessType
	 */
	private final boolean updatesAllowed;

	/***
	 * data access type
	 */
	private final DataAccessType dataAccessType;
	/**
	 * Exility uses very few value types, while JDBC has several. Keep the
	 * mapping.
	 */
	private static final Map<Integer, DataValueType> JdbcToExilTypes = new HashMap<Integer, DataValueType>();

	/***
	 * open shop once and for all...
	 */
	static {
		setTypeMaps();
		initiateJdbcDriver();
	}

	/***
	 * set jadbc-exility type mapping
	 */
	private static void setTypeMaps() {
		JdbcToExilTypes.put(new Integer(Types.DATE), DataValueType.DATE);
		JdbcToExilTypes.put(new Integer(Types.TIME), DataValueType.DATE);

		// Remember java.util.Date will escape nanoseconds component but
		// java.sql.TimeStamp will hold it.
		JdbcToExilTypes.put(new Integer(Types.TIMESTAMP), DataValueType.DATE);

		JdbcToExilTypes.put(new Integer(Types.BOOLEAN), DataValueType.BOOLEAN);
		JdbcToExilTypes.put(new Integer(Types.BIT), DataValueType.BOOLEAN);

		/**
		 * decimal/numeric with fractional part 0 is actually integral. Hence,
		 * this is handled in the logic
		 */
		JdbcToExilTypes.put(new Integer(Types.DECIMAL), DataValueType.DECIMAL);
		JdbcToExilTypes.put(new Integer(Types.NUMERIC), DataValueType.DECIMAL);

		JdbcToExilTypes.put(new Integer(Types.DOUBLE), DataValueType.DECIMAL);
		JdbcToExilTypes.put(new Integer(Types.FLOAT), DataValueType.DECIMAL);
		JdbcToExilTypes.put(new Integer(Types.REAL), DataValueType.DECIMAL);

		JdbcToExilTypes.put(new Integer(Types.TINYINT), DataValueType.INTEGRAL);
		JdbcToExilTypes
				.put(new Integer(Types.SMALLINT), DataValueType.INTEGRAL);
		JdbcToExilTypes.put(new Integer(Types.INTEGER), DataValueType.INTEGRAL);
		JdbcToExilTypes.put(new Integer(Types.BIGINT), DataValueType.INTEGRAL);
		JdbcToExilTypes.put(new Integer(Types.ROWID), DataValueType.INTEGRAL);

		JdbcToExilTypes.put(new Integer(Types.CHAR), DataValueType.TEXT);
		JdbcToExilTypes.put(new Integer(Types.VARCHAR), DataValueType.TEXT);
		JdbcToExilTypes
				.put(new Integer(Types.LONGNVARCHAR), DataValueType.TEXT);
		JdbcToExilTypes.put(new Integer(Types.NVARCHAR), DataValueType.TEXT);
		JdbcToExilTypes.put(new Integer(Types.NCHAR), DataValueType.TEXT);
		JdbcToExilTypes.put(new Integer(Types.NCHAR), DataValueType.TEXT);
		JdbcToExilTypes.put(new Integer(Types.CLOB), DataValueType.TEXT);
		JdbcToExilTypes.put(new Integer(Types.BLOB), DataValueType.TEXT);
		JdbcToExilTypes.put(new Integer(Types.LONGVARCHAR), DataValueType.TEXT);
		JdbcToExilTypes.put(new Integer(Types.NCLOB), DataValueType.TEXT);

		JdbcToExilTypes.put(new Integer(Types.NULL), DataValueType.NULL);

	}

	/***
	 * Extract required parameters from AP and gear-up for jdbc Made package
	 * private to allow change of db parameters during development phase
	 */
	static void initiateJdbcDriver() {
		nlsDateFormat = AP.nlsDateFormat;
		connectionString = AP.connectionString;
		auditConnectionString = AP.auditConnectionString;

		try {
			/**
			 * It is important to check dataSource first. Project may provide
			 * dbDriver just to indicate that the driver is oracle.
			 */
			if (AP.dataSource != null) {
				InitialContext exilInitContext = new InitialContext();

				dataSource = (DataSource) exilInitContext.lookup("java:/"
						+ AP.dataSource);
				if (AP.auditDataSource != null) {
					auditDataSource = (DataSource) exilInitContext
							.lookup("java:/" + AP.auditDataSource);
					dbIsAudited = true;
				}
			} else {
				if (AP.dbDriver == null || AP.connectionString == null) {
					Spit.out("ERROR: Project is not set-up properly. No database oerations are possible");
				} else {
					Class.forName(AP.dbDriver);
					if (auditConnectionString != null) {
						dbIsAudited = true;
					}
				}
			}
			/***
			 * TODO: remove this logic and create a separate field like dbVendor
			 */
			oracleIsUsed = (AP.dbDriver != null && AP.dbDriver
					.contains("oracle"))
					|| (connectionString != null && connectionString
							.contains("oracle"));
		} catch (Exception e) {
			Spit.out("Unable to initialize jdbc driver.");
			Spit.out(e);
		}
	}

	/***
	 * Get a DbHandle instance (factory pattern). Deprecated. use
	 * borrowHandle(DataAccessType) instead
	 * 
	 * @param forUpdate
	 * @return dbHandke
	 * @throws ExilityException
	 */
	@Deprecated
	public static DbHandle borrowHandle(boolean forUpdate)
			throws ExilityException {
		return new DbHandle(forUpdate ? DataAccessType.READWRITE
				: DataAccessType.READONLY);
	}

	/***
	 * Get a DbHandle instance (factory pattern)
	 * 
	 * @param accessType
	 *            DataAccessType required for this db operation
	 * @return db handle
	 * @throws ExilityException
	 */
	public static DbHandle borrowHandle(DataAccessType accessType)
			throws ExilityException {
		return new DbHandle(accessType);
	}

	/**
	 * 
	 * @param accessType
	 * @param suppressLogging
	 *            true of you do not want sqls to e logged.
	 * @return db handle
	 * @throws ExilityException
	 */
	public static DbHandle borrowHandle(DataAccessType accessType,
			boolean suppressLogging) throws ExilityException {
		DbHandle handle = new DbHandle(accessType);
		handle.suppressSqlLog = suppressLogging;
		return handle;
	}

	/**
	 * Be fair. Return what you borrowed :-). To simplify try-catch-finally
	 * blocks for the callers, we accept null as argument
	 * 
	 * @param handle
	 */
	public static void returnHandle(DbHandle handle) {
		if (handle == null) {
			return;
		}

		/*
		 * we might have opened regular connection, as well as audit connection
		 */
		if (handle.con != null) {
			try {
				handle.con.close();
			} catch (Exception e) {
				// nothing to take care of
			}
		}

		if (handle.auditcon != null) {
			try {
				handle.auditcon.close();
			} catch (Exception e) {
				// nothing to take care of
			}
		}
	}

	/***
	 * get a db handle
	 * 
	 * @param accessType
	 *            read, read-write or autocommit
	 */
	public DbHandle(DataAccessType accessType) {
		this.dataAccessType = accessType;
		this.updatesAllowed = (accessType == DataAccessType.READWRITE)
				|| (accessType == DataAccessType.AUTOCOMMIT);
		if (accessType != DataAccessType.NONE) {
			this.openConnection();
		}
	}

	/***
	 * whether the connection was opened with forUpdate set to true
	 * 
	 * @return true of updates are alllowed for this connection
	 */
	public boolean updateIsAllowed() {
		return this.updatesAllowed;
	}

	/**
	 * TODO: we should not give this!!!! Simple getter
	 * 
	 * @return connection. Could be null if open connection failed.
	 * @deprecated
	 */
	@Deprecated
	public Connection getConnection() {
		return this.con;
	}

	/***
	 * 
	 * @param forUpdate
	 *            whether updates are allowed in this connection
	 * @throws ExilityException
	 */
	private void openConnection() {
		try {
			if (DbHandle.dataSource != null) {
				this.con = dataSource.getConnection();
				this.con.setReadOnly(!this.updatesAllowed);
				if (this.updatesAllowed && DbHandle.dbIsAudited) {
					this.auditcon = auditDataSource.getConnection();
					this.auditcon.setReadOnly(false);
				}
			}
			// non-oracle require a directive for handling clobs properly.
			// Should we re-factor with empty connProps for oracle?
			else {
				Properties connProps = new Properties();
				if (DbHandle.oracleIsUsed == false) {
					connProps.put("SetBigStringTryClob", "true");
				}
				this.con = DriverManager.getConnection(connectionString,
						connProps);
				this.con.setReadOnly(!this.updatesAllowed);

				if (DbHandle.dbIsAudited) {
					this.auditcon = DriverManager.getConnection(
							auditConnectionString, connProps);
					this.auditcon.setReadOnly(false);
				}
			}

			if (this.updatesAllowed) {
				this.con.setAutoCommit(this.dataAccessType == DataAccessType.AUTOCOMMIT);
				if (this.auditcon != null) {
					this.auditcon
							.setAutoCommit(this.dataAccessType == DataAccessType.AUTOCOMMIT);
				}
				// this could be connection property???
				if (DbHandle.oracleIsUsed) {
					String nlsformat = "ALTER SESSION SET NLS_DATE_FORMAT = '"
							+ nlsDateFormat + "'";
					Statement statement = this.con.createStatement();
					statement.execute(nlsformat);
					if (this.auditcon != null) {
						Statement auditstatement = this.auditcon
								.createStatement();
						auditstatement.execute(nlsformat);
					}
					statement.close();
				} else {
					// TODO: why get into this unsafe isolation level for
					// non-oracle??
					this.con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
					if (this.auditcon != null) {
						this.auditcon
								.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
					}
				}
			}
		} catch (Exception e) {
			Spit.out("Connection failed. Please check your database connection settings and network settings");
			Spit.out(e);
		}
	}

	/**
	 * for testing purposes, we can run without a database connection.
	 * 
	 * @return true if there is no connection to db
	 */
	boolean isDummy() {
		return (this.con == null);
	}

	/**
	 * Transaction is automatically started when we open connection for updated
	 */
	public void beginTransaction() {
		//
	}

	/**
	 * commit all updates
	 * 
	 * @throws ExilityException
	 */
	public void commit() throws ExilityException {
		try {
			if (this.con != null) {
				this.con.commit();
			}
			if (this.auditcon != null) {
				this.auditcon.commit();
			}
		} catch (SQLException e) {
			throw new ExilityException(e);
		}
	}

	/**
	 * Typical batch programs can opt to run in this mode. performance will be
	 * far better, but recovery from a failure should be planned outside of the
	 * application by DBA
	 * 
	 * @throws ExilityException
	 */
	public void startAutoCommit() throws ExilityException {
		try {
			if (this.con != null) {
				this.con.setAutoCommit(true);
			}
			if (this.auditcon != null) {
				this.auditcon.setAutoCommit(true);
			}
		} catch (SQLException e) {
			throw new ExilityException(e);
		}
	}

	/**
	 * stop auto commit.
	 * 
	 * @throws ExilityException
	 */
	public void stopAutoCommit() throws ExilityException {
		try {
			if (this.con != null) {
				this.con.setAutoCommit(false);
			}
			if (this.auditcon != null) {
				this.auditcon.setAutoCommit(false);
			}
		} catch (SQLException e) {
			throw new ExilityException(e);
		}
	}

	/**
	 * Some trouble, rollback. Note that rolling-back-by-design is not a good
	 * design :-)
	 * 
	 * @throws ExilityException
	 */
	public void rollback() throws ExilityException {
		try {
			if (this.con != null) {
				this.con.rollback();
			}
			if (this.auditcon != null) {
				this.auditcon.rollback();
			}
		} catch (SQLException e) {
			throw new ExilityException(e);
		}
	}

	/**
	 * A short-cut method when you are selecting one column and one row.
	 * 
	 * @param sqlText
	 *            Valid selection sql that selects one column
	 * @return column value returned by db, null otherwise. Caller should know
	 *         its value type and cast accordingly
	 * @throws ExilityException
	 */
	public Object extractSingleField(String sqlText) throws ExilityException {
		if (!this.suppressSqlLog) {
			Spit.out(sqlText);
		}
		if (this.isDummy()) {
			return "";
		}

		Object result = null;
		try {
			Statement statement = this.con.createStatement();
			ResultSet rs = statement.executeQuery(sqlText);
			if (rs.next()) {
				result = rs.getObject(1);
			}
			rs.getStatement().close();
		} catch (SQLException e) {
			throw new ExilityException(e);
		}
		return result;
	}

	/**
	 * Sql is expected to return one row at the most. get that row and put them
	 * into a name-value map. Also, if the supplied Sql does not have its column
	 * names set, it is set using meta data from the result set.
	 * 
	 * @param sqlText
	 *            a valid sql that selects at most one row.
	 * @param values
	 *            map to which values are to be extracted
	 * @param sql
	 *            Sql Template : this requires some re-factoring. Sql is passed
	 *            here for it to get populated with names if it did not have
	 *            them. (that optimization has justified this awkward design in
	 *            the first place)
	 * @param prefix
	 *            if specified, all field names will be prefixed with this
	 *            before putting them in dc
	 * @return 1 if we are able to extract, 0 otherwise
	 * @throws ExilityException
	 *             TO-DO this method needs to be re-factored as it blatantly
	 *             violates encapsulation :-(.
	 */
	public int extractSingleRow(String sqlText, Map<String, Value> values,
			SqlInterface sql, String prefix) throws ExilityException {
		if (!this.suppressSqlLog) {
			Spit.out(sqlText);
		}
		if (this.isDummy()) {
			return 0;
		}

		try {
			ResultSet rs = this.getRs(sqlText, false);
			if (rs == null) {
				return 0;
			}

			this.rsToCollection(rs, sql == null ? null : sql.getColumnInfo(),
					values, sql, prefix);
			rs.getStatement().close();
		} catch (SQLException e) {
			throw new ExilityException(e);
		}
		return 1;
	}

	/**
	 * Extract data to a grid, and populate columnNames in sql template if
	 * required.
	 * 
	 * @param sqlText
	 *            sql text to be used
	 * @param sql
	 *            object that may need its columnNames to be populated.
	 * @param gridName
	 *            name of the grid to be created
	 * @param gridToBeAppendedTo
	 *            optional. If non-null, rows are appended to this grid, and
	 *            this grid is returned. Grid name is ignored, and no new grid
	 *            is created. Expect errors if grid has different sets of
	 *            columns than the ones extracted in this sql
	 * @return new grid, or gridToBeAppendedTo
	 * @throws ExilityException
	 */
	public Grid extractToGrid(String sqlText, SqlInterface sql,
			String gridName, Grid gridToBeAppendedTo) throws ExilityException {
		if (!this.suppressSqlLog) {
			Spit.out(sqlText);
		}
		if (this.isDummy()) {
			return new Grid();
		}
		try {
			ResultSet rs = this.getRs(sqlText, true);
			Grid grid = this.rsToGrid(rs,
					sql == null ? null : sql.getColumnInfo(), sql, gridName,
					gridToBeAppendedTo);
			Spit.out(grid.getNumberOfRows() + " rows extracted.");
			rs.getStatement().close();
			return grid;
		} catch (SQLException e) {
			throw new ExilityException(e);
		}
	}

	/**
	 * execute a DML sql
	 * 
	 * @param sqlText
	 *            sql to be executed
	 * @param asAudit
	 *            is this on the audit tables?
	 * @return number of rows affected
	 * @throws ExilityException
	 */
	public int execute(String sqlText, boolean asAudit) throws ExilityException {
		if (!this.suppressSqlLog) {
			Spit.out(sqlText);
		}

		if (this.isDummy()) {
			return 0;
		}

		int n;
		try {
			Statement statement = asAudit ? this.auditcon.createStatement()
					: this.con.createStatement();
			n = statement.executeUpdate(sqlText);
			statement.close();
		} catch (SQLException e) {
			throw new ExilityException(e);
		}
		Spit.out(n + " rows affected.");
		return n;
	}

	/***
	 * execute a list of DML sql's in a batch
	 * 
	 * @param sqlTexts
	 * @param asAudit
	 *            is this on the audit tables?
	 * @return total number of rows affected
	 * @throws ExilityException
	 */
	public int executeBatch(String[] sqlTexts, boolean asAudit)
			throws ExilityException {
		if (sqlTexts.length == 0 || this.isDummy()) {
			return 0;
		}

		try {
			Statement statement = asAudit ? this.auditcon.createStatement()
					: this.con.createStatement();
			if (AP.commandTimeOutTime != 0) {
				statement.setQueryTimeout(AP.commandTimeOutTime);
			}
			for (String Sqltext : sqlTexts) {
				if (!this.suppressSqlLog) {
					Spit.out(Sqltext);
				}
				statement.addBatch(Sqltext);
			}
			int[] nbrs = statement.executeBatch();
			int total = 0;
			for (int n : nbrs) {
				total += n;
			}
			statement.close();
			Spit.out(total + " rows affected.");
			return total;
		} catch (SQLException e) {
			throw new ExilityException(e);
		}
	}

	/**
	 * execute a prepared statement
	 * 
	 * @param sqlText
	 *            valid prepared statement with number of place-holders(?)
	 *            matching number of entries in values list
	 * @param values
	 *            values to be used for place-holders in sql
	 * @param asAudit
	 *            is this sql to be executed as an audit?
	 * @return number of rows affected by this statement
	 * @throws ExilityException
	 */
	public int executePreparedStatement(String sqlText, List<Value> values,
			boolean asAudit) throws ExilityException {
		if (!this.suppressSqlLog) {
			Spit.out(sqlText);
		}
		if (this.isDummy()) {
			return 0;
		}
		try {
			PreparedStatement statement = asAudit ? this.auditcon
					.prepareStatement(sqlText) : this.con
					.prepareStatement(sqlText);
			int fieldAt = 1;
			for (Value value : values) {
				value.addToPrepearedStatement(statement, fieldAt);
				Spit.out(" value added = " + value.getTextValue());
				fieldAt++;
			}
			int n = statement.executeUpdate();
			statement.close();
			Spit.out(n + " rows affected.");
			return n;
		} catch (SQLException e) {
			throw new ExilityException(e);
		}
	}

	/***
	 * Extract data from rdbms and puts into either a grid or into name value
	 * pairs
	 * 
	 * @param sqlText
	 *            String that is a valid ansi sql prepared statement
	 * @param values
	 *            a List that contains values to be set for each parameter (?)
	 *            in the prepared statement in the same order as they occur in
	 *            sql. Can not have null, but a Value object may be isNull() =
	 *            true
	 * @param outputColumns
	 *            name and value types of output fields. Must be in teh same
	 *            order that your sql would have extracted
	 * @param prefix
	 *            non-null if you want this to be prefixed to each of the output
	 *            names mentioned above
	 * @param dc
	 *            dc
	 * @param gridName
	 *            grid in which output rows are to be extracted into. If null,
	 *            then only one row(possibly) is read and name-value pairs are
	 *            added to dc
	 * @param template
	 *            SqlTemplate object. If output parameters are missing in SQL,
	 *            then meta data will be used to create output parameters and
	 *            cached into the object for reuse
	 * @return number of rows extracted
	 * @throws ExilityException
	 */
	public int extractFromPreparedStatement(String sqlText, List<Value> values,
			OutputColumn[] outputColumns, String prefix, DataCollection dc,
			String gridName, Sql template) throws ExilityException {
		if (!this.suppressSqlLog) {
			Spit.out(sqlText);
		}
		if (this.isDummy()) {
			return 0;
		}
		try {
			PreparedStatement statement = this.con.prepareStatement(sqlText);
			int fieldAt = 1;
			for (Value value : values) {
				value.addToPrepearedStatement(statement, fieldAt);
				Spit.out(" value added = " + value.getTextValue());
				fieldAt++;
			}
			int nbrRows = 0;
			if (statement.execute() == false) // sql has not extracted
			{
				Spit.out("Sql has not returned results. Probably it is an update sql");
			} else {
				ResultSet rs = statement.getResultSet();
				if (gridName != null) {
					Grid grid = this.rsToGrid(rs, outputColumns, template,
							gridName, null);
					dc.addGrid(gridName, grid);
					nbrRows = grid.getNumberOfRows();
				} else {
					if (rs.next()) {
						this.rsToCollection(rs, outputColumns, dc.values,
								template, prefix);
						nbrRows = 1;
					}
				}
			}
			statement.close();
			return nbrRows;
		} catch (SQLException e) {
			throw new ExilityException(e);
		}
	}

	/**
	 * execute a prepared statement repeatedly
	 * 
	 * @param sqlText
	 *            valid prepared statement to be executed with number of
	 *            place-holders(?) matching the number of entries in each list
	 * @param values
	 *            list of value-lists. A value list is like an array of values.
	 * @param asAudit
	 *            should this be on audit tables?
	 * @param selector
	 *            array of booleans indicating whether the corresponding row is
	 *            to be selected for execution. If this is null, all rows are
	 *            considered.
	 * @return total number of affected rows
	 * @throws ExilityException
	 */
	public int executePreparedStatementBatch(String sqlText,
			List<ValueList> values, boolean asAudit, boolean[] selector)
			throws ExilityException {
		if (!this.suppressSqlLog) {
			Spit.out(sqlText);
		}
		if (this.isDummy()) {
			return 0;
		}
		try {
			PreparedStatement statement = asAudit ? this.auditcon
					.prepareStatement(sqlText) : this.con
					.prepareStatement(sqlText);
			if (AP.commandTimeOutTime != 0) {
				statement.setQueryTimeout(AP.commandTimeOutTime);
			}
			int nbrRows = values.get(0).length();
			int nbrAdded = 0;
			StringBuilder forSpitting = new StringBuilder();
			for (int i = 0; i < nbrRows; i++) {
				if (selector != null && selector[i] == false) {
					continue;
				}
				int colId = 1;
				if (!this.suppressSqlLog) {
					forSpitting.append("row:" + (i + 1));
				}
				for (ValueList list : values) {
					Value value = list.getValue(i);
					if (!this.suppressSqlLog) {
						forSpitting.append(value.getTextValue() + ',');
					}
					value.addToPrepearedStatement(statement, colId);
					colId++;
				}
				statement.addBatch();
				// remove last comma
				if (!this.suppressSqlLog) {
					forSpitting.setLength(forSpitting.length() - 1);
					Spit.out(forSpitting.toString());
					forSpitting.setLength(0);
				}
				nbrAdded++;
			}
			int nbrRowsAffected = 0;
			if (nbrAdded == 0) {
				Spit.out(" No rows selected for execution of above SQL");
			} else {
				int[] rowsAffected = statement.executeBatch();
				for (int i : rowsAffected) {
					nbrRowsAffected += i;
				}
			}
			Spit.out(nbrRowsAffected + " rows affected.");
			statement.close();
			return nbrRowsAffected;

		} catch (SQLException e) {
			throw new ExilityException(e);
		}
	}

	/**
	 * execute a stored procedure. This requires re-writing as per new design
	 * 
	 * @param spName
	 * @param inParams
	 * @param inoutParams
	 * @param outParams
	 * @param dc
	 * @return number of affected rows
	 * @throws ExilityException
	 */
	public int executeSP(String spName, Parameter[] inParams,
			Parameter[] inoutParams, Parameter[] outParams, DataCollection dc)
			throws ExilityException {
		// dry run
		if (this.isDummy()) {
			return 0;
		}

		StringBuilder sbf = new StringBuilder("{call ");
		sbf.append(spName);
		// how many parameters to be added?
		int n = 0;
		if (inParams != null) {
			n += inParams.length;
		}

		if (inoutParams != null) {
			n += inoutParams.length;
		}

		if (outParams != null) {
			n += outParams.length;
		}

		if (n == 0) {
			sbf.append("()}");
		} else {
			// it is of the form (?, ?, .....)
			sbf.append("(?");
			n--; // I hate using --n!!
			while (n > 0) {
				sbf.append(", ?");
				n--;
			}
			sbf.append(")}");
		}

		String sql = sbf.toString();
		Spit.out("Store procedure :\n" + sql);
		try {
			CallableStatement statement = this.con.prepareCall(sql);
			if (AP.commandTimeOutTime != 0) {
				statement.setQueryTimeout(AP.commandTimeOutTime);
			}

			int curIndex = 1;
			if (inParams != null && inParams.length > 0) {
				curIndex = this.addParams(statement, dc, inParams, curIndex,
						DbHandle.FOR_INPUT);
			}

			if (inoutParams != null && inoutParams.length > 0) {
				curIndex = this.addParams(statement, dc, inoutParams, curIndex,
						DbHandle.FOR_INPUT_AND_OUTPUT);
			}

			if (outParams != null && outParams.length > 0) {
				curIndex = this.addParams(statement, dc, outParams, curIndex,
						DbHandle.FOR_OUTPUT);
			}

			boolean retValue = statement.execute();

			// output parameters would start after input parameters
			curIndex = inParams == null ? 1 : inParams.length + 1;
			if (inoutParams != null && inoutParams.length > 0) {
				curIndex = this.extractOutput(statement, dc, inoutParams,
						curIndex);
			}
			if (outParams != null && outParams.length > 0) {
				this.extractOutput(statement, dc, outParams, curIndex);
			}
			statement.close();
			return retValue ? 1 : 0;
		} catch (SQLException e) {
			throw new ExilityException(e);
		}

	}

	/***
	 * extract output parameters from an executed statement into dc
	 * 
	 * @param statement
	 *            statement that is already executed, and is ready with its
	 *            output parameters
	 * @param dc
	 *            to which fields are to be extracted
	 * @param parameters
	 *            output fields
	 * @param startExtractingAt
	 *            parameter position in statement from where to extract.
	 *            Parameter provided to make thei more flexible
	 * @return next position to extract from, if required
	 * @throws SQLException
	 */
	private int extractOutput(CallableStatement statement, DataCollection dc,
			Parameter[] parameters, int startExtractingAt) throws SQLException {
		int n = startExtractingAt;
		for (Parameter p : parameters) {
			String name = p.name;
			AbstractDataType dt = DataDictionary.getDataType(p.dataElementName);
			Value value = dt.extractFromStoredProcedure(statement, n);
			if (value == null || value.isNull()) {
				// Spit.out("Output  " + name + " at " + n + " is null");
			} else {
				dc.addValue(name, value);
				// Spit.out("Output  " + name + " at " + n + " is " +
				// value.getTextValue());
			}
			n++;
		}
		return n;
	}

	/***
	 * push parameters into a prepared statement
	 * 
	 * @param statement
	 *            to which parameters are to be pushed to
	 * @param dc
	 *            from where to get values for parameters
	 * @param parameters
	 *            to be pushed
	 * @param startAddingAt
	 *            position of the parameter to start from
	 * @param inOut
	 *            whether this is input or output. Exility does not support
	 *            in-out, though one can give the same name more
	 * @return next parameter position, in case all parameter positions are not
	 *         exhausted
	 * @throws ExilityException
	 * @throws SQLException
	 */
	private int addParams(CallableStatement statement, DataCollection dc,
			Parameter[] parameters, int startAddingAt, int inOut)
			throws ExilityException, SQLException {
		int n = startAddingAt;
		for (Parameter p : parameters) {
			String name = p.name;
			DataValueType valueType = p.getValueType();
			AbstractDataType dt = p.getDataType();
			Value value = null;
			if (inOut != DbHandle.FOR_OUTPUT) {
				value = dc.getValue(name);
				if (value == null || value.isNull()) {
					if (p.defaultValue != null) {
						value = Value.newValue(p.defaultValue, valueType);
						if (value == null) {
							dc.addMessage("exilityInvalidDataFormat", p.name,
									p.defaultValue, valueType.toString());
							value = new NullValue(valueType);
						}
					} else if (p.isOptional) {
						value = new NullValue(valueType);
					} else {
						dc.raiseException("exilNoParamValue", name);
					}

				}
				dt.addInputToStoredProcedure(statement, n, value);
				if (!this.suppressSqlLog) {
					Spit.out("Added in-param " + name + " with value = "
							+ value + " at : " + n);
				}
			}
			if (inOut != DbHandle.FOR_INPUT) {
				dt.addOutputToStoredProcedure(statement, n);
				if (!this.suppressSqlLog) {
					Spit.out("Added out-param " + name + " at : " + n);
				}
			}
			n++;
		}
		return n;
	}

	/***
	 * get column information from a result set
	 * 
	 * @param rs
	 *            from which to extract the information
	 * @return column information
	 * @throws SQLException
	 */
	private OutputColumn[] getColumnInfo(ResultSet rs) throws SQLException {
		ResultSetMetaData md = rs.getMetaData();
		int columnCount = md.getColumnCount();
		OutputColumn[] columns = new OutputColumn[columnCount];
		for (int m = 0; m < columnCount; m++) {
			int mplus1 = m + 1;
			String name = md.getColumnLabel(mplus1);
			if (name == null || name.length() == 0) {
				name = md.getColumnName(mplus1);
			}

			// valueTypes[m] = this.getValueType(md.getColumnType(mplus1));

			int jdbcType = md.getColumnType(mplus1);
			DataValueType exilityType = DataValueType.TEXT;
			/**
			 * decimal with scale 0 is nothing but integral
			 */
			if (jdbcType == Types.NUMERIC || jdbcType == Types.DECIMAL) {
				if (md.getScale(mplus1) == 0) {
					exilityType = DataValueType.INTEGRAL;
				} else {
					exilityType = DataValueType.DECIMAL;
				}
			} else {
				exilityType = DbHandle.getExilType(name, jdbcType);
			}
			columns[m] = new OutputColumn(name, exilityType, name);
		}
		return columns;
	}

	/***
	 * extract values from a result set into name-value collection
	 * 
	 * @param rs
	 *            from which to extract data
	 * @param columns
	 *            output columns to be extracted
	 * @param values
	 *            name-value collection to which we have to extract data
	 * @param sqlObject
	 *            in case this object wants the outputColumsn to be set to it
	 *            for optimization. optional
	 * @param prefix
	 * @throws SQLException
	 */
	private void rsToCollection(ResultSet rs, OutputColumn[] columns,
			Map<String, Value> values, SqlInterface sqlObject, String prefix)
			throws SQLException {
		OutputColumn[] localColumns = columns;

		if (localColumns == null) {
			localColumns = this.getColumnInfo(rs);
			if (sqlObject != null) {
				sqlObject.setColumnInfo(localColumns);
			}
		}

		// it is very rare that prefix is provide. very small optimization: why
		// add empty string each time?
		if (prefix == null) {
			int m = 1;
			for (OutputColumn column : localColumns) {
				values.put(column.fieldName,
						this.getValue(rs, m, column.valueType));
				m++;
			}
		} else {
			int m = 1;
			for (OutputColumn column : localColumns) {
				values.put(prefix + column.fieldName,
						this.getValue(rs, m, column.valueType));
				m++;
			}
		}
	}

	/**
	 * 
	 * Returns an RS that is already positioned to read the first one. null
	 * otherwise. Caller MUST take care of closing the rs.
	 * 
	 * @param sql
	 *            actual sql to be used to create a result set
	 * @param doNotCheckForData
	 *            if set to true, rs is returned without checking for data.
	 *            Caller has to do next() before reading first row
	 * @return Result set, or null if no rows
	 * @throws SQLException
	 */
	public ResultSet getRs(String sql, boolean doNotCheckForData)
			throws SQLException {
		Statement statement = this.con.createStatement();
		if (AP.commandTimeOutTime != 0) {
			statement.setQueryTimeout(AP.commandTimeOutTime);
		}

		ResultSet rs = statement.executeQuery(sql);
		if (doNotCheckForData || rs.next()) {
			return rs;
		}
		statement.close();
		return null;
	}

	/*
	 * Returns an RS that is already positioned to read the first one. null
	 * otherwise.
	 */

	private Grid rsToGrid(ResultSet rs, OutputColumn[] columns,
			SqlInterface sqlObject, String gridName, Grid gridToBeAppendedTo)
			throws SQLException {
		List<Value[]> values = new ArrayList<Value[]>();
		OutputColumn[] localColumns = columns;

		if (localColumns == null) {
			localColumns = this.getColumnInfo(rs);
			if (sqlObject != null) {
				sqlObject.setColumnInfo(localColumns);
			}
		}

		while (rs.next()) {
			Value[] aRow = new Value[localColumns.length];
			int m = 0;
			for (OutputColumn column : localColumns) {
				aRow[m] = this.getValue(rs, m + 1, column.valueType);
				m++;
			}
			values.add(aRow);
		}
		if (gridToBeAppendedTo == null) {
			Grid grid = new Grid(gridName);
			grid.setValues(localColumns, values, null);
			return grid;
		}
		gridToBeAppendedTo.appendValues(values);
		return gridToBeAppendedTo;
	}

	/***
	 * purpose of this method is to get Exil type corresponding to the jdbc
	 * type. returns exility value type based on the following logic.
	 * 
	 * 1. if element is not found in data dictionary, then return a exility type
	 * corresponding to jdbc type
	 * 
	 * 2. if found, check if both are compatible. If true, return exility type.
	 * 
	 * 3. else return jdbc type itself and warn to user for dt mismatch;
	 * 
	 * 4. in case we do not either, we use TEXT as default, after a warning of
	 * course
	 * 
	 * @param eleName
	 * @param jdbcType
	 * @return DataValueType exilType if found in dictionary or Map else Text
	 *         exil value type.
	 */
	public static DataValueType getExilType(String eleName, int jdbcType) {
		DataValueType typeInDb = JdbcToExilTypes.get(new Integer(jdbcType));
		if (typeInDb == null) {
			Spit.out("ERROR : Got a sql type " + jdbcType
					+ " that is not mapped to exility type for [" + eleName
					+ "]. Please report to Exility Support team.");
		}

		DataValueType typeInDictionary = DataDictionary
				.getValueTypeOrNull(eleName);
		if (typeInDictionary == null) {
			if (typeInDb == null) {
				Spit.out("We could not determine data type for " + eleName
						+ ". We will treat it as text.");
				return DataValueType.TEXT;
			}
			Spit.out("No entry found in data dictionary for " + eleName
					+ ". its data type is inferred based on rdbms metadata");
			return typeInDb;
		}

		// Handle timeStamp issue.Remember from jdbc Date always as TimeStamp so
		// if user defined element with type timeStamp in dic.
		if (jdbcType == Types.TIMESTAMP
				&& typeInDictionary == DataValueType.TIMESTAMP) {
			return typeInDictionary;
		}

		// if we see conflict, let us warn
		if (typeInDictionary != typeInDb) {
			// there are two compatible mismatches !!

			if ((jdbcType == Types.TIMESTAMP && typeInDictionary == DataValueType.TIMESTAMP)
					|| (jdbcType == Types.CHAR && typeInDictionary == DataValueType.BOOLEAN)) {
				// we are ok
			} else {
				Spit.out("Warning: Element " + eleName + " is defined as "
						+ typeInDictionary
						+ " in data dictionary, but it is defined as a "
						+ typeInDb + " in the database.[" + typeInDb
						+ "]  type is assumed.");
			}
		}

		return typeInDb;
	}

	/***
	 * 
	 * @param rs
	 * @param idx
	 * @param type
	 * @return value from record set
	 * @throws SQLException
	 */
	@SuppressWarnings("deprecation")
	private Value getValue(ResultSet rs, int idx, DataValueType type)
			throws SQLException {
		Value value = null;
		switch (type) {
		case DATE:

			value = Value.newValue(rs.getDate(idx));
			break;

		case TIMESTAMP:
			value = Value.newTimeStampValue(rs.getTimestamp(idx));
			break;

		case BOOLEAN:
			value = Value.newValue(rs.getBoolean(idx));
			break;

		case DECIMAL:
			value = Value.newValue(rs.getDouble(idx));
			break;

		case INTEGRAL:
			value = Value.newValue(rs.getLong(idx));
			break;

		default:
			value = Value.newValue(rs.getString(idx));
			break;
		}
		if (rs.wasNull()) {
			return new NullValue(value.getValueType());
		}
		return value;
	}

	/***
	 * utility method to get list of tables defined in the data base
	 * 
	 * @return grid that has details of all tables defined in the data base
	 * 
	 * @throws ExilityException
	 */
	public static Grid getAllTables() throws ExilityException {
		DbHandle handle = DbHandle.borrowHandle(DataAccessType.READONLY);
		Grid grid = handle
				.extractToGrid(TABLE_SELECT_SQL, null, "tables", null);
		DbHandle.returnHandle(handle);
		return grid;
	}

	/***
	 * utility method to get list of columns defined for a table
	 * 
	 * @param tableName
	 *            name of the table
	 * @return grid that has details of all columns
	 * @throws ExilityException
	 */
	public static Grid getAllColumns(String tableName) throws ExilityException {
		DbHandle handle = DbHandle.borrowHandle(DataAccessType.READONLY);
		Grid grid = handle.extractToGrid(DbHandle.COLUMN_SELECT_SQL + tableName
				+ "'", null, "columns", null);
		DbHandle.returnHandle(handle);
		return grid;
	}

	/***
	 * extract values from stored procedure
	 * 
	 * @param sqlText
	 *            a sql that will actually execute a stored procedure
	 * @param inputValues
	 *            values for the stored procedure
	 * @param values
	 *            name-value collection
	 * @param prefix
	 * @return number of fields extracted
	 * @throws ExilityException
	 */
	public int extractSingleRowFromSp(String sqlText, List<Value> inputValues,
			Map<String, Value> values, String prefix) throws ExilityException {
		if (!this.suppressSqlLog) {
			Spit.out(sqlText);
		}
		try {
			ResultSet rs = this.getRsFromSp(sqlText, inputValues, false);
			if (rs == null) {
				return 0;
			}
			this.rsToCollection(rs, null, values, null, prefix);
			rs.getStatement().close();
		} catch (SQLException e) {
			throw new ExilityException(e);
		}
		return 1;
	}

	/***
	 * extract data into a grid after executing a stored procedure
	 * 
	 * @param sqlText
	 *            stored procedure text
	 * @param values
	 *            name-value collection
	 * @param gridName
	 *            to which data is to be extracted
	 * @param gridToBeAppendedTo
	 *            whether to create the gird or append rows into it
	 * @return grid that has the result of this stored procedure
	 * @throws ExilityException
	 */
	public Grid extractToGridFromSp(String sqlText, List<Value> values,
			String gridName, Grid gridToBeAppendedTo) throws ExilityException {
		if (!this.suppressSqlLog) {
			Spit.out(sqlText);
		}
		try {
			ResultSet rs = this.getRsFromSp(sqlText, values, true);
			Grid grid = this.rsToGrid(rs, null, null, gridName,
					gridToBeAppendedTo);
			Spit.out(grid.getNumberOfRows() + " rows extracted.");
			rs.getStatement().close();
			return grid;
		} catch (SQLException e) {
			throw new ExilityException(e);
		}
	}

	private ResultSet getRsFromSp(String sqlText, List<Value> values,
			boolean doNotCheckForData) throws SQLException {
		CallableStatement statement = this.con.prepareCall(sqlText);
		if (AP.commandTimeOutTime != 0) {
			statement.setQueryTimeout(AP.commandTimeOutTime);
		}
		int colId = 1;
		for (Value value : values) {
			value.addToPrepearedStatement(statement, colId);
		}
		ResultSet rs = statement.executeQuery();
		if (doNotCheckForData || rs.next()) {
			return rs;
		}
		statement.close();
		return null;
	}

	/***
	 * Tries to get the out put parameters of a sql from rdbms from the
	 * meta-data of the result set by executing a dummy sql
	 * 
	 * @param sql
	 *            Sql object. Output parameters are set back to this object
	 * @return true if we could actually do the job. False otherwise
	 * @throws ExilityException
	 */
	public boolean getOutputParamsFromDb(Sql sql) throws ExilityException {
		DataCollection dc = new DataCollection();
		for (SqlParameter p : sql.inputParameters) {
			p.putTestValues(dc);
		}

		String sqlText = sql.getSql(dc);
		try {
			ResultSet rs = this.getRs(sqlText, true);
			OutputColumn[] columns = this.getColumnInfo(rs);
			rs.getStatement().close();
			sql.setColumnInfo(columns);
			return true;
		} catch (SQLException e) {
			Spit.out("Unable to get mets data for sql " + sql.name
					+ " Error : " + e.getMessage());
			return false;
		}
	}

	/***
	 * used by batch entry to call service entry for each row of a result set of
	 * a sql
	 * 
	 * @param sql
	 *            Sql template to use for iteration
	 * @param dc
	 * @param service
	 * @throws ExilityException
	 */
	public void callServiceForEachRow(Sql sql, DataCollection dc,
			ServiceInterface service) throws ExilityException {
		if (this.isDummy()) {
			return;
		}
		String sqlText = sql.getSql(dc);

		/**
		 * have a separate handle for the service that is committed after each
		 * execution
		 */
		DbHandle updater = DbHandle.borrowHandle(DataAccessType.READWRITE);
		try {
			Statement statement = this.con.createStatement();
			ResultSet rs = statement.executeQuery(sqlText);
			updater = DbHandle.borrowHandle(DataAccessType.READWRITE);

			while (rs.next()) {
				this.rsToCollection(rs, sql.getColumnInfo(), dc.values, sql,
						null);
				updater.beginTransaction();
				try {
					service.execute(dc, updater);
				} catch (Exception e) {
					dc.addError("Error while executing batch service "
							+ service.getName() + ". " + e.getMessage());
				}
				if (dc.hasError()) {
					updater.rollback();
				} else {
					updater.commit();
				}
				dc.zapMessages();
			}
			statement.close();

		} catch (SQLException e) {
			throw new ExilityException(e);
		} finally {
			DbHandle.returnHandle(updater);
		}
	}
}

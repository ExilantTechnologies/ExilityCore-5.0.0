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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * sql can be of these types
 * 
 */
enum SqlType {
	dynamicSql, preparedStatement, storedProcedure
}

/**
 * 
 * Encapsulates a dynamic sql command to be executed with parameters supplied at
 * run time. Delivers all functionality required for a wrapper task, SqlTask.
 * 
 */
public class Sql implements SqlInterface, ToBeInitializedInterface {
	/**
	 * unique name within this module
	 */
	public String name = null;
	/**
	 * the template text e.g. SELECT a,b,c FROM t WHERE a=2 {AND b=@1}
	 */
	public String sql = null;
	/**
	 * is there a minimum number of parameters that are to be supplied?
	 * typically, you require at least one criterion to be specified for a
	 * search etc..
	 */
	int minParameters = 0;

	/**
	 * for documentation
	 */
	String module = null;

	/**
	 * for documentation
	 */
	String description = null;

	/**
	 * is it a SELECT query using which data is extracted, or any other query
	 * that needs to be executed?
	 */
	boolean toBeExecuted = false;
	/**
	 * sql has been extended to handle prepared statement and stored procedure
	 */
	SqlType sqlType = SqlType.dynamicSql;

	/**
	 * array of objects that describe each parameter. Refer to SQLParameter
	 */
	SqlParameter[] inputParameters = null;

	/**
	 * parameters that are expected from a sql (that is not executed.) We had
	 * initially made this mandatory, but later, we advised programmers not to
	 * specify this. we enhanced the design to fetch the output parameters from
	 * meta-data once and cache it.
	 */
	Parameter[] outputParameters = null;

	/**
	 * Record that defines the fields that are expected as input. This record
	 * MUST contain all the input parameters. Of course it can have other fields
	 * as well, which are not relevant for this SQL.
	 */
	String inputRecordName = null;

	/**
	 * record that defines the output row. If this sql is used to extract a
	 * grid, then this record must be the same as the output of the sql. If it
	 * is extracted into a dc, then the output could be a subset of this record.
	 */
	String outputRecordName = null;

	/**
	 * if this is a stored procedure, this is the name of the stored procedure.
	 * Valid only if sqlType is set to storedProcedure
	 */
	String storedProcedureName = null;

	/**
	 * for optimization, save name of output columns and their dataValuetypes.
	 * This is taken from outputParameters if it is given in initialize() Else
	 * it is set by dataExtractor on the first execution... This is implemented
	 * with a very tricky, and possibly flawed design as of now. We should
	 * re-factor this at some time. As of now, Sql object is passed to
	 * DbHandle.extract() always. if sql.columnNames is null, dbHandle detects
	 * this and populates. One possible redesign is to define columnNames and
	 * valueTypes as
	 */

	private OutputColumn[] outputColumns = null;
	private Record inputRecord = null;
	private Record outputRecord = null;

	/**
	 * created internally while loading it
	 */
	public Sql() {
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void initialize() {

		/*
		 * do we have input record? in that case, input parameter is based on
		 * that
		 */
		if (this.inputRecordName != null) {
			this.initializeInputParameters();
		} else if (this.inputParameters != null) {
			/*
			 * we had deferred initializing these parameters during loading,
			 * hoping that inpoutRecord would be used :-(
			 */
			for (SqlParameter param : this.inputParameters) {
				param.forceInitialize();
			}
		}

		/*
		 * do we have output record?
		 */
		if (this.outputRecordName != null) {
			this.initializeOutputParameters();
		} else if (this.outputParameters != null
				&& this.outputParameters.length > 0) {
			int n = this.outputParameters.length;
			this.outputColumns = new OutputColumn[n];
			for (int i = 0; i < n; i++) {
				Parameter p = this.outputParameters[i];
				this.outputColumns[i] = new OutputColumn(p.name,
						p.getValueType(), p.name);
			}
		}

		if (this.sql != null && this.storedProcedureName != null) {
			Spit.out("Sql "
					+ this.name
					+ " has provided stored procedure name as well as sql. storedProcedure will be ignored and this will be treated as a sql.");
			this.storedProcedureName = null;
		}
	}

	/**
	 * initialize input parameters based on inputRecord
	 */
	private void initializeInputParameters() {
		this.inputRecord = Records.getRecord(this.inputRecordName);
		if (this.inputRecord == null) {
			Spit.out("Sql " + this.name + " refers to " + this.inputRecordName
					+ " as input record, but that record does not exist.");
			return;
		}

		/*
		 * designer wants entire of inputRecord
		 */
		if (this.inputParameters == null) {
			Field[] fields = this.inputRecord.getFields();
			this.inputParameters = new SqlParameter[fields.length];
			int idx = 0;
			for (Field field : fields) {
				SqlParameter param = new SqlParameter();
				param.dataElementName = param.name = field.getName();
				param.setDataType(field.getDataType());
				param.dataValueType = field.getValueType();
				this.inputParameters[idx] = param;
				idx++;
			}
			return;
		}

		/*
		 * designer has given input parameters. Let us pick-up value type from
		 * fields
		 */
		for (SqlParameter param : this.inputParameters) {
			Field field = this.inputRecord.getField(param.name);
			if (field == null) {
				Spit.out("Sql "
						+ this.name
						+ " uses "
						+ this.inputRecordName
						+ " as input record, but it uses "
						+ param.name
						+ " as an input parameter that does not exist in that record.");
				param.dataValueType = DataValueType.TEXT;
			} else {
				param.dataValueType = field.getValueType();

			}
		}
	}

	/**
	 * initialize output parameters based on outputRecord
	 */
	private void initializeOutputParameters() {
		this.outputRecord = Records.getRecord(this.outputRecordName);
		if (this.outputRecord == null) {
			Spit.out("Sql " + this.name + " refers to " + this.outputRecordName
					+ " as output record, but that record does not exist.");
			return;
		}

		/*
		 * designer wants entire of outputRecord
		 */
		if (this.outputParameters == null) {
			Field[] fields = this.outputRecord.getFields();
			this.outputColumns = new OutputColumn[fields.length];
			int idx = 0;
			for (Field field : fields) {
				this.outputColumns[idx] = new OutputColumn(field.getName(),
						field.getValueType(), field.columnName);
				idx++;
			}
			return;
		}

		/*
		 * designer has given output parameters. Let us pick-up value type from
		 * fields
		 */
		this.outputColumns = new OutputColumn[this.outputParameters.length];
		int idx = 0;
		for (Parameter param : this.outputParameters) {
			DataValueType valueType = DataValueType.TEXT;
			Field field = this.inputRecord.getField(param.name);
			if (field == null) {
				Spit.out("Sql "
						+ this.name
						+ " uses "
						+ this.outputRecordName
						+ " as output record, but it uses "
						+ param.name
						+ " as an output parameter that does not exist in that record.");
			} else {
				valueType = field.getValueType();
			}
			this.outputColumns[idx] = new OutputColumn(param.name, valueType,
					param.name);
			idx++;
		}
	}

	@Override
	public void setColumnInfo(OutputColumn[] outputColumns) {
		this.outputColumns = outputColumns;
	}

	@Override
	public OutputColumn[] getColumnInfo() {
		return this.outputColumns;
	}

	/**
	 * Generates a SQL statement based on this template and values for
	 * parameters provided in DC
	 * 
	 * @param dc
	 *            Standard data collection
	 * @param prefix
	 *            String with which input fields is to be prefixed with
	 * @return a SQL statement based on this template and the values supplied in
	 *         dc
	 * @//throws ExilityException if any required parameter is not supplied
	 */
	String getSql(DataCollection dc) throws ExilityException {
		return this.getSql(dc, null, 0);
	}

	/***
	 * Generate a SQL statement by substituting required values at run time
	 * 
	 * @param dc
	 * @param grid
	 *            grid from which parameter values to be taken. index MUST be
	 * @param index
	 * @param inputNames
	 * @return sql after substituting its parameters
	 * @throws ExilityException
	 *             on any validation error of input sql string. An error message
	 *             would be put into dc before raising exception
	 */
	String getSql(DataCollection dc, Grid grid, int index)
			throws ExilityException {
		// if template is 'static' if there are no parameters. Just return the
		// template in that case.
		if ((this.inputParameters == null)
				|| (this.inputParameters.length == 0)) {
			return this.sql;
		}

		StringBuilder sbf = new StringBuilder(this.sql);
		int paramCount = 0; // tracks how many values were actually specified

		// starting from end to ensure that @1 will not eat @11
		for (int k = this.inputParameters.length - 1; k >= 0; k--) {
			SqlParameter param = this.inputParameters[k];
			String val = param.getValue(dc, null, grid, index);

			if (val != null && val.length() > 0) {
				paramCount++;
			} else if (param.isOptional == false) {
				dc.raiseException("exilNoValueForParameter", param.name,
						this.name);
			}

			// substitute the value of the parameter
			// while first parameter is called @1, its index is 0.
			String p = "@" + (k + 1);
			if (param.isOptional == false) {
				this.substituteRequired(p, sbf, val);
			} else {
				this.substituteOptional(p, sbf, val, dc);
			}
		}// for each parameter

		// Are there any braces remaining to be removed?
		boolean removeText = false;
		if (paramCount == 0) {
			removeText = true;
		}

		this.removeResidualBraces(sbf, dc, removeText);

		// were there enough parameters?
		if ((this.minParameters > 0) && (paramCount < this.minParameters)) {
			dc.raiseException("exilNotEnoughParameters", this.name,
					String.valueOf(this.minParameters),
					String.valueOf(paramCount));
		}
		return sbf.toString();
	}

	/*
	 * Substitutes .... { ...@1...} type of stub with value. If value is
	 * specified, it replaces @i with the value, and removes { and } if value is
	 * not specified, it removes the substring between the brackets, including
	 * the brackets
	 */
	private void substituteOptional(String param, StringBuilder sbf,
			String val, DataCollection dc) throws ExilityException {
		int paramLength = param.length();

		// we are now looking at ..{... @k ....}....{....@k....}......
		// people may use same parameter more than once. Let us loop till there
		// are no more parameters
		// We have a design dilemma: .net does not provide search abilities for
		// StringBuilder
		String op = "{";
		String cl = "}";
		for (int paramAt = sbf.indexOf(param); paramAt >= 0; paramAt = sbf
				.indexOf(param)) {
			int openBracketAt = sbf.lastIndexOf(op, paramAt);
			int closeBracketAt = sbf.indexOf(cl, paramAt);

			if ((openBracketAt < 0) || (closeBracketAt < 0)) {
				dc.raiseException("exilBracesNotFound", this.name, param);
			}

			// if parameter is not specified, we are to remove the
			if ((val == null) || (val.length() == 0)) {
				sbf.delete(openBracketAt, closeBracketAt + 1);
			} else {
				// replace @k and remove the braces. Watch out for the order in
				// which we replace so that the various index values hold
				sbf.deleteCharAt(closeBracketAt);
				sbf.replace(paramAt, paramAt + paramLength, val);
				sbf.deleteCharAt(openBracketAt);
			}
		}
	}

	private void substituteRequired(String param, StringBuilder sbf, String val) {
		int paramLength = param.length();

		// StringBuilder does not have replaceAll mehtod. I am simulating it
		// here
		int i = sbf.length();
		while (true) {
			i = sbf.lastIndexOf(param, i);
			if (i < 0) {
				return;
			}
			sbf.replace(i, i + paramLength, val);
		}
	}

	private void removeResidualBraces(StringBuilder sbf, DataCollection dc,
			boolean removeText) throws ExilityException {
		int openBracketAt = sbf.lastIndexOf("{");
		int closeBracketAt = sbf.indexOf("}");

		if ((openBracketAt < 0) && (closeBracketAt < 0)) {
			return; // no work
		}

		if ((openBracketAt < 0) || (closeBracketAt < 0)
				|| (openBracketAt > closeBracketAt)) {
			Spit.out("SQl after substituion has invalid braces in that\n"
					+ sbf.toString());
			dc.raiseException("exilUnmatchedBraces", this.name);
		}

		if (removeText) {
			sbf.delete(openBracketAt, closeBracketAt + 1);
		} else {
			sbf.deleteCharAt(closeBracketAt);
			sbf.deleteCharAt(openBracketAt);
		}
	}

	private int executePs(DataCollection dc, DbHandle handle, String gridName)
			throws ExilityException {
		String sqlText = this.getSql(dc);
		if (gridName == null) {
			List<Value> values = this.getValueList(dc);
			if (values == null) {
				return 0;
			}
			return handle.executePreparedStatement(sqlText, values, false);
		}

		List<ValueList> values = this.getValuesList(dc, gridName);
		if (values == null) {
			return 0;
		}
		return handle.executePreparedStatementBatch(sqlText, values, false,
				null);
	}

	private int extractPs(DataCollection dc, DbHandle handle, String gridName,
			String prefix) throws ExilityException {
		String sqlText = this.getSql(dc);
		List<Value> values = this.getValueList(dc);
		if (values == null) {
			return 0;
		}
		return handle.extractFromPreparedStatement(sqlText, values,
				this.outputColumns, prefix, dc, gridName, this);
	}

	private List<ValueList> getValuesList(DataCollection dc, String gridName) {
		Grid grid = dc.getGrid(gridName);
		if (grid == null) {
			dc.addError("Grid " + gridName + " is not found.");
			return null;
		}

		List<ValueList> values = new ArrayList<ValueList>();
		for (Parameter parm : this.inputParameters) {
			ValueList list = grid.getColumn(parm.name);
			if (list == null) {
				dc.addError("Column " + parm.name + " not found in grid "
						+ gridName + ".");
				return null;
			}
			values.add(list);
		}
		return values;
	}

	private List<Value> getValueList(DataCollection dc) {
		List<Value> values = new ArrayList<Value>();
		for (Parameter parm : this.inputParameters) {
			Value value = dc.getValue(parm.name);
			Spit.out("parameter " + parm.name + " has value " + value);
			if (value == null) {
				dc.addMessage("exilFieldIsRequired", parm.name);
				return null;
			}
			values.add(value);
		}
		return values;
	}

	/**
	 * Delivers complete functionality, used by validator and other internal
	 * higher-level classes to get the result of this sql.
	 * 
	 * @param dc
	 * @param handle
	 * @param gridName
	 *            name of the grid in which the result is to be put into dc. If
	 *            omitted(null) result of the sql is assumed to be a single row
	 *            and name-value pairs will be pushed to dc
	 * @return 0 if no result, else number of rows of data extracted, or number
	 *         of affected row
	 * @throws ExilityException
	 *             SQLException is caught and converted into ExilityException to
	 *             encapsulate all SQL issues into this class and below
	 */
	@Override
	public int execute(DataCollection dc, DbHandle handle, String gridName,
			String prefix) throws ExilityException {
		if (this.storedProcedureName != null) {
			return handle.executeSP(this.storedProcedureName,
					this.inputParameters, null, this.outputParameters, dc);
		}
		String sqlText = this.getSql(dc);

		// is it a prepared statement?
		if (this.sqlType == SqlType.preparedStatement) {
			if (this.toBeExecuted) {
				return this.executePs(dc, handle, gridName);
			}

			return this.extractPs(dc, handle, gridName, prefix);
		}

		// it is a dynamic sql
		if (this.toBeExecuted) {
			return handle.execute(sqlText, false);
		}

		if (gridName == null) {
			return handle.extractSingleRow(sqlText, dc.values, this, prefix);
		}

		Grid grid = handle.extractToGrid(sqlText, this, gridName, null);
		if (grid == null) {
			return 0;
		}
		dc.addGrid(gridName, grid);
		return grid.getNumberOfRows();
	}

	/**
	 * Executes this sql for each row of the specified grid. Sql should be
	 * executable, except if this is for validation.
	 * 
	 * @param dc
	 *            dc
	 * @param handle
	 *            database handle
	 * @param gridName
	 *            grid for which this sql is to be repeated
	 * @param forValidation
	 *            if set to true, one row of data is extracted for each
	 *            successful row
	 * @return total number of rows affected, or number of rows that resulted in
	 *         some extraction
	 * @throws ExilityException
	 */
	@Override
	public int executeBulkTask(DataCollection dc, DbHandle handle,
			String gridName, boolean forValidation) throws ExilityException {
		Grid grid = dc.getGrid(gridName);
		if (grid == null) {
			return 0;
		}
		int nbrRows = grid.getNumberOfRows();
		if (nbrRows == 0) {
			return 0;
		}
		String[] sqlText = new String[nbrRows];
		for (int i = 0; i < sqlText.length; i++) {
			sqlText[i] = this.getSql(dc, grid, i);
		}
		if (this.toBeExecuted) {
			return handle.executeBatch(sqlText, false);
		}

		Map<String, Value> values = new HashMap<String, Value>();
		int nbrRowsExtracted = 0;
		for (int i = 0; i < sqlText.length; i++) {
			int n = handle.extractSingleRow(sqlText[i], values, this, null);
			if (n == 0) {
				continue;
			}
			if (nbrRowsExtracted == 0) {
				this.addColumnsToGrid(grid, (forValidation ? gridName : null));
			}
			nbrRowsExtracted++;
			for (String nm : values.keySet()) {
				grid.setValue(nm, values.get(nm), i);
			}
		}
		if (nbrRowsExtracted == 0) {
			this.addColumnsToGrid(grid, (forValidation ? gridName : null));
		}

		return nbrRowsExtracted;
	}

	/***
	 * add columns of this sql to the grid
	 * 
	 * @param grid
	 *            grid
	 * @param gridName
	 * @throws ExilityException
	 */
	private void addColumnsToGrid(Grid grid, String gridName)
			throws ExilityException {
		if (this.outputColumns == null) {
			return;
		}
		int nbrRows = grid.getNumberOfRows();
		for (OutputColumn column : this.outputColumns) {
			String columnName = column.fieldName;
			if (grid.hasColumn(columnName)) {
				continue;
			}
			grid.addColumn(columnName,
					ValueList.newList(column.valueType, nbrRows));
		}
		if (gridName != null) {
			grid.addColumn(gridName + FieldSpec.VALIDATION_COLUMN_NAME,
					ValueList.newList(DataValueType.BOOLEAN, nbrRows));
		}
	}

	@Override
	public DataAccessType getDataAccessType() {
		if (this.toBeExecuted) {
			return DataAccessType.READWRITE;
		}
		return DataAccessType.READWRITE;
	}
}
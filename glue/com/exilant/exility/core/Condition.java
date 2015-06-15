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

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to dynamically build an SQL query
 * 
 */
public class Condition {
	/**
	 * List of comparators corresponding to the int value of filter-field
	 * operator.
	 */
	private static Comparator[] filterValues = new Comparator[9];
	static {
		filterValues[SqlUtil.OPERATOR_ANY] = null;
		filterValues[SqlUtil.OPERATOR_EQUAL] = Comparator.EQUALTO;
		filterValues[SqlUtil.OPERATOR_STARTS_WITH] = Comparator.STARTSWITH;
		filterValues[SqlUtil.OPERATOR_CONTAINS] = Comparator.CONTAINS;
		filterValues[SqlUtil.OPERATOR_GREATER_THAN] = Comparator.GREATERTHAN;
		filterValues[SqlUtil.OPERATOR_LESS_THAN] = Comparator.LESSTHAN;
		filterValues[SqlUtil.OPERATOR_BETWEEN] = Comparator.BETWEEN;
		filterValues[SqlUtil.OPERATOR_IN] = Comparator.IN;
		filterValues[SqlUtil.OPERATOR_NOT_IN] = Comparator.NOTIN;
	}
	/***
	 * The map of SQL operators corresponding to the logical comparators.
	 * Currently we plan to support MySQL,MSSQL and Oracle. All three provides
	 * use the same comparison operators and possibly others use the same too.
	 * So we can safely have coded here.
	 */
	private static Map<Comparator, String> sqlOperators = new HashMap<Comparator, String>() {
		private static final long serialVersionUID = 1L;

		{
			this.put(Comparator.EQUALTO, " = ");
			this.put(Comparator.NOTEQUALTO, " != ");
			this.put(Comparator.GREATERTHAN, " > ");
			this.put(Comparator.GREATERTHANOREQUALTO, " >= ");
			this.put(Comparator.LESSTHAN, " < ");
			this.put(Comparator.LESSTHANOREQUALTO, " <= ");
		}
	};
	/**
	 * The database column name to which this condition is applied to
	 */
	String columnName = null;
	/**
	 * The name of the field by which we are storing this in the data collection
	 */
	String fieldName = null;
	/**
	 * The underlying comparator this condition object holds
	 */
	Comparator comparator = Comparator.EQUALTO;

	/**
	 * name of the grid from which to get the value list. Note that once you
	 * mention a grid name, the value to e used is assumed to be a value list.
	 * As of now, IN and NOTIN use value list.
	 */
	String gridName = null;

	/**
	 * Default constructor
	 */

	public Condition() {
	}

	/**
	 * Overloaded constructor for convenience
	 * 
	 * @param colName
	 *            Name of the underlying data base column
	 * @param fieldName
	 *            Name of the field to which the condition is applied to in the
	 *            data collection object
	 * @param comp
	 *            The underlying comparator associated with this condition
	 */
	public Condition(String colName, String fieldName, Comparator comp) {
		this.columnName = colName;
		this.fieldName = fieldName;
		this.comparator = comp;
	}

	/**
	 * Build the SQL statement based on the given set of conditions
	 * 
	 * @param sbf
	 *            The StringBuilder instance to which we need to add the SQL
	 *            statement
	 * @param conditions
	 *            The array of collection objects that will be used to build the
	 *            comparator
	 * @param dc
	 *            Instance of DataCollection object
	 * @return true if SQL statement was created successfully, else false
	 * @throws ExilityException
	 */
	public static boolean toSql(StringBuilder sbf, Condition[] conditions,
			DataCollection dc) throws ExilityException {
		// Spit.out(" Conditions: " + Condition.render(conditions));
		if (conditions == null || conditions.length == 0) {
			return false;
		}
		int countOfConditionsAdded = 0;
		for (Condition condition : conditions) {
			if (countOfConditionsAdded > 0) {
				sbf.append(" AND ");
			}
			if (condition.appendToSql(sbf, dc)) {
				countOfConditionsAdded++;
			}
		}
		if (countOfConditionsAdded == 0) {
			return false;
		}

		return true;
	}

	/**
	 * Append a conditional SQL snippet to an existing SQL statement
	 * 
	 * @param sbf
	 *            The StringBuilder instance that contains the SQL statement to
	 *            which we need to append another snippet
	 * @param dc
	 *            Instance of the data collection object
	 * @return true if snippet was appended successfully, else false
	 * @throws ExilityException
	 */
	public boolean appendToSql(StringBuilder sbf, DataCollection dc)
			throws ExilityException {
		if (this.comparator == Comparator.EXISTS
				|| this.comparator == Comparator.DOESNOTEXIST) {
			throw new ExilityException(
					this.comparator.toString()
							+ " can not be used in a condition for table based opertations.");
		}

		if (this.comparator == Comparator.IN
				|| this.comparator == Comparator.NOTIN) {
			sbf.append(' ').append(this.columnName).append(' ');
			return this.appendListToSql(sbf, dc);
		}

		if (this.comparator == Comparator.FILTER) {
			return this.appendToSqlForFilter(sbf, dc);
		}

		return this.appendToSqlOthers(sbf, dc, this.comparator);
	}

	private boolean appendToSqlOthers(StringBuilder sbf, DataCollection dc,
			Comparator cmpToUse) {
		Value value = dc.getValue(this.fieldName);
		if (value == null) {
			return false;
		}

		sbf.append(this.columnName);
		/*
		 * You may wonder that the LIKE operator used here might not work in all
		 * databases. But as far as we are concerned, MySQL , Oracle and MSSQL
		 * Server ..all 3 support the same syntax. So we have covered most for
		 * now.
		 */
		if (cmpToUse == Comparator.CONTAINS) {
			sbf.append(" LIKE '%")
					.append(value.getTextValue().replaceAll("'", "''"))
					.append("%'");
		} else if (cmpToUse == Comparator.STARTSWITH) {
			sbf.append(" LIKE '")
					.append(value.getTextValue().replaceAll("'", "''"))
					.append("%'");
		} else if (cmpToUse == Comparator.BETWEEN) {
			Value toValue = dc.getValue(this.fieldName + "To");
			if (toValue == null) {
				return false;
			}
			sbf.append(" BETWEEN ").append(SqlUtil.formatValue(value))
					.append(" AND ").append(SqlUtil.formatValue(toValue))
					.append(' ');
		} else {
			sbf.append(Condition.sqlOperators.get(cmpToUse)).append(
					SqlUtil.formatValue(value));
		}
		return true;
	}

	private boolean appendToSqlForFilter(StringBuilder sbf, DataCollection dc) {
		int filterValue = 0;
		try {
			filterValue = Integer.parseInt(dc.getValue(
					this.fieldName + "Operator").toString());
		} catch (Exception e) {
			return false;
		}
		if (filterValue < 1 || filterValue > Condition.filterValues.length) {
			return false;
		}
		return this.appendToSqlOthers(sbf, dc,
				Condition.filterValues[filterValue]);
	}

	/**
	 * 
	 * @param filterComparator
	 * @return null if the number is not valid. Comparator otherwise.
	 */
	public static Comparator getComparator(int filterComparator) {
		if (filterComparator < 1
				|| filterComparator > Condition.filterValues.length) {
			return null;
		}
		return Condition.filterValues[filterComparator];
	}

	private boolean appendListToSql(StringBuilder sbf, DataCollection dc) {
		ValueList values = null;
		int dotAt = this.fieldName.indexOf('.');
		if (dotAt == -1) {
			/**
			 * it is a value list
			 */
			values = dc.getValueList(this.fieldName);
		} else {
			/**
			 * field name is of the form tableName.columnName
			 */
			String tableName = this.fieldName.substring(0, dotAt);
			String listName = this.fieldName.substring(dotAt + 1);
			Grid grid = dc.getGrid(tableName);
			if (grid != null) {
				values = grid.getColumn(listName);
			}
		}
		if (values == null || values.length() == 0) {
			return false;
		}

		if (this.comparator == Comparator.NOTIN) {
			sbf.append(" NOT ");
		}

		sbf.append(" IN (").append(SqlUtil.formatList(values)).append(')');

		return true;
	}

	static String render(Condition[] conditions) {
		StringBuilder sbf = new StringBuilder();
		for (Condition condition : conditions) {
			sbf.append(condition.columnName).append(' ')
					.append(condition.comparator.toString()).append(' ')
					.append(condition.fieldName).append('\n');
		}
		return sbf.toString();
	}
}
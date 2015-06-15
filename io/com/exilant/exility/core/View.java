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
 * View is an "extension" of a table, where in additional columns can be added
 * to the table from its related tables by specifying as to how to pick rows
 * from the related tables for a given row from the base table.
 * 
 */
public class View {
	/***
	 * name of this view
	 */
	String name;
	/***
	 * View is "viewed" as a base table to which other tables are linked to.
	 * This view is different from the general rdbms view where the list of
	 * tables are joined in an arbitrary way.
	 * 
	 */
	String baseTableName;

	/***
	 * columns to be selected from base table
	 */
	String[] baseColumnNames;
	/***
	 * related tables that need to be linked to the base table
	 */
	JoinedTable[] joinedTables;

	/***
	 * additional filtered criterion that will filter-out rows from the
	 * resultant view
	 */
	Condition[] filterConditions;
}
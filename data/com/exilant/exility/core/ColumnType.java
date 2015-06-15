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

/**
 * Different types of columns in a dataTable of type storage (and not view)
 * 
 */
public enum ColumnType {
	/**
	 * an attribute of this entity, holds data about this entity
	 */
	data

	/**
	 * primary key : normally internally and auto generated. example customerId
	 */
	, primaryKey

	/**
	 * a field that end-user sees as unique. example customerMailId
	 */
	, logicalPrimaryKey

	/**
	 * link to its parent table
	 */
	, parentKey

	/**
	 * link to another table
	 */
	, foreignKey

	/**
	 * created time stamp 1019446614
	 */
	, createdTimeStamp
	/**
	 * modified time stamp
	 */
	, modifiedTimeStamp

	/**
	 * created by user
	 */
	, createdByUser
	/**
	 * user id who modified it last
	 */
	, modifiedByUser

	/**
	 * data column of a view. Every column, except the key column in a view MUST
	 * have this as their column type
	 */
	, view

	/**
	 * this is calculated at run time based on other fields
	 */
	, temp
}

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
 * This the related table of another table. Remember this "direction" while
 * understanding all attributes and methods in this class A table is said to be
 * related to another if a given row in the primary table may have 0 or more
 * rows in the related table Rows in the related table are searched (filtered)
 * by matching columns values of corresponding columns.
 */
public class RelatedTable {
	/***
	 * table that we are describing the relation to
	 */
	String relatedTableName;

	/***
	 * columns from the primary table that the relationship depends on
	 */
	String[] primaryColumns;
	/***
	 * columns from the related table that correspond to the columns in the
	 * primary table
	 */
	String[] relatedColumns;

}

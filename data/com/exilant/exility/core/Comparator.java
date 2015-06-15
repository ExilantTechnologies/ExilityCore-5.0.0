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
 * This class is used in various expression or query builders. It defines a set
 * of comparison operators that are currently supported
 * 
 */
public enum Comparator {
	/**
	 * Check if variable exists
	 */
	EXISTS,
	/**
	 * Check if variable does not exist
	 */
	DOESNOTEXIST,
	/**
	 * Check if variable is equal to
	 */
	EQUALTO,
	/**
	 * Check if variable is not equal to
	 */
	NOTEQUALTO,
	/**
	 * Check if variable is less than
	 */
	LESSTHAN,
	/**
	 * Check if variable is less than or equal to
	 */
	LESSTHANOREQUALTO,
	/**
	 * Check if variable is greater than
	 */
	GREATERTHAN,
	/**
	 * Check if variable is greater than or equal to
	 */
	GREATERTHANOREQUALTO,
	/**
	 * Check if variable contains
	 */
	CONTAINS,
	/**
	 * Check if variable starts with
	 */
	STARTSWITH,
	/**
	 * Used to identify comparator by index (not really a comparator, but more
	 * of a indirect reference to the comparison operator)
	 */
	FILTER,
	/**
	 * check if value is in this list
	 */
	IN,
	/**
	 * check if value is NOT in this list
	 */
	NOTIN,
	/**
	 * between two values
	 */
	BETWEEN
}

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
 * different value types supported by Exility. NULL abd TIMESTAMP are not
 * recommended
 * 
 */
public enum DataValueType {
	/**
	 * we DO NOT ENCOURAGE usage of this type, but some of our designers wanted
	 * to represent NULL in an rdbms
	 */
	NULL,
	/**
	 * text
	 */
	TEXT,
	/**
	 * integral, irrespective of its length. Limited to length of long.
	 */
	INTEGRAL,
	/**
	 * non-integral number, limited to precision of double
	 */
	DECIMAL,
	/**
	 * boolean
	 */
	BOOLEAN,
	/**
	 * date, with or without time component
	 */
	DATE,
	/**
	 * currently used as a date-time object
	 */
	TIMESTAMP
}

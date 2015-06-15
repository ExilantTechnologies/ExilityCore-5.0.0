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
 * defines constants used by Exility.
 * 
 */
public class ExilityConstants {
	/***
	 * when we persist meta data, most arrays are serialized as a comma
	 * separated string into a text value
	 */
	public static final String LIST_SEPARATOR = ",";

	/***
	 * you may want to use separator char
	 */
	public static final char LIST_SEPARATOR_CHAR = ',';
	/***
	 * pairs within comma separated list are normally separated with a ';',
	 * though we would have used '=' at times.
	 */
	public static final String PAIR_SEPARATOR = ";";

	/***
	 * pairs within comma separated list are normally separated with a ';',
	 * though we would have used '=' at times.
	 */
	public static final char PAIR_SEPARATOR_CHAR = ';';
	/***
	 * empty string
	 */
	public static final String EMPTY_VALUE = "";

	/***
	 * unknown value
	 */
	public static final String UNKNOWN_VALUE = " unknown/unspecified ";
	/**
	 * literal for true
	 */
	public static final String TRUE_STRING = "true";

	/**
	 * literal for false
	 */
	public static final String FALSE_STRING = "false";

	/**
	 * field in dc for directing sql logs to be suppressed. Typically this is
	 * set in web.xml
	 */
	public static final String SUPPRESS_SQL_LOG = "suppress-sql-log";

}

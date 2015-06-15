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
package com.exilant.exility.ide;

/**
 * possible issues with a field
 */
public enum FieldIssue {

	/**
	 * No issue with this field
	 */
	NONE,
	/**
	 * field does not exist in dictionary. either data element is not defined,
	 * or even that is not in the dictionary
	 */
	UNKNOWN_TYPE,
	/**
	 * field is in dictionary, and data element is also in dictionary with same
	 * data type.
	 */
	REDUNDANT_DATA_ELEMENT,

	/**
	 * field and data element are in dictionary, data types are compatible, but
	 * are different
	 */
	COMPATIBLE_DATA_ELEMENT,
	/**
	 * field and data element have incompatible data types
	 */
	WRONG_DATA_ELEMENT,

	/**
	 * uses a data element, instead of making an entry in data dictionary
	 */
	NOT_IN_DICTIONARY,

	/**
	 * used as different data/value type across resources
	 */
	INCONSISTENT_USE
}

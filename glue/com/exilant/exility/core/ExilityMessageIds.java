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
 * Not a class, but a place holder that has all the messageIds used internally
 * by Exility. These message Ids MUSt be defined in messages.xml
 */
public abstract class ExilityMessageIds {
	/**
	 * general error
	 */
	public static final String ERROR = "exilityError";

	/**
	 * service not found
	 */
	public static final String NO_SERVICE = "exilNoService";

	/**
	 * general warning
	 */
	public static final String WARNING = "exilityWarning";

	/**
	 * this field is mandatory
	 */
	public static final String VALUE_IS_REQUIRED = "exilValueIsRequired";

	/**
	 * from-to relationship failed between fields
	 */
	public static final String FROM_TO_ERROR = "exilFromToError";
}
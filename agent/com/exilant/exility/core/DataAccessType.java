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
 * defines the type of database access required for a service
 * 
 */
public enum DataAccessType {
	/**
	 * No data base access. No need to open a connection
	 */
	NONE,
	/**
	 * use a read-only connection. transaction processing is not started. any
	 * attempt inside the service to update will result in an exception
	 */
	READONLY,
	/**
	 * a transaction processing is initiated in the beginning. At the end, it is
	 * committed except for exceptions and for error in the returned dc
	 */
	READWRITE,
	/**
	 * a read-write connection, but no transaction processing. In case of any
	 * error/exception, earlier updates are not rolled back. This the most
	 * efficient way of updating, but has no protection for any exception.
	 * Suitable if recovery processes are defined outside the application.
	 */
	AUTOCOMMIT,
	/**
	 * each service step will be executed with its own connection. This Should
	 * be used ONLY UNDER EXCEPTIONAL CONDITIONS as it violates teh golden rule
	 * of one-client-request-one-transaction. If one step succeeds, and the
	 * second one fails, the first one IS NOT ROLLED BACK. typically, you use
	 * this feature to do some set of read operations, like extracting data for
	 * a report, and then may record the fact that the data was extracted. If
	 * you run the whole thing in a transaction, the reads will result in large
	 * number of locks, affecting performance.
	 */
	SERVICE_STEP_LEVEL
}

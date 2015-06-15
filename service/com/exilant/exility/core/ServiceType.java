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
 * type of service based on which its execution strategy will be decided
 * 
 */
public enum ServiceType {
	/**
	 * services that were written before this type was introduced. This is the
	 * default
	 */
	classic
	/**
	 * regular service
	 */
	, regular
	/**
	 * sub-service. called by another service, but not called directly by
	 * clients
	 */
	, procedure
	/**
	 * sub-service. called by another service, but not called directly by
	 * clients. It opens and manages its own connection. To be used in very
	 * special circumstances after fully analyzing the possible result of some
	 * failures.
	 */
	, procedureWithOwnTransaction
	/**
	 * batch service
	 */
	, batch
	/**
	 * This service is a list of services, each if which is stated as a
	 * serviceStep, and each service would manage its own transaction.
	 */
	, serviceList
}

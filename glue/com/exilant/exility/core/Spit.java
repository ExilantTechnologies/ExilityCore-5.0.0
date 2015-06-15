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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple utility for accumulating all trace information for an
 * execution thread. This is NOT al alternative to log4j, but it serves a
 * different purpose. An execution thread, if it wants to accumulate all traces,
 * initiates with a call to startWriter(). a call to stopWriter() returns the
 * cumulative trace across all objects that get called as part of this execution
 * thread.
 * 
 * This way of logging is quite convenient in a multi-user transaction
 * processing system. One gets all the logs for one execution thread.
 * ServiceAgent.execute() makes use of this utility to get all the logs nicely
 * wrapped inside a &lt;serviceLog&gt; tag. If no startWriter() is issued, a
 * call to Spit.out() behaves the same as a call to logger.info(text) Exility
 * OPPOSES using logging for debugging during development stage.
 * 
 * Spit uses slf4j for server side logging, while the accumulated log is sent to
 * client on a need basis.
 */

public class Spit {
	/**
	 * This is used to aggregate logs for a given thread.
	 */
	private static ThreadLocal<StringBuilder> bufferHolder = new ThreadLocal<StringBuilder>();

	/***
	 * notice the use of a single in stance of logger
	 */
	private static final Logger logger = LoggerFactory.getLogger("trace");

	/**
	 * Initiates accumulation of logging for this thread.
	 * 
	 * @return accumulated log so far, or null if none
	 */
	public static String startWriter() {
		StringBuilder sbf = Spit.bufferHolder.get();
		Spit.bufferHolder.set(new StringBuilder());
		return sbf == null ? null : sbf.toString();
	}

	/**
	 * Log this trace information. Accumulated if startWriter() was issued. Else
	 * logged using standard logger
	 * 
	 * @param textToBeLogged
	 *            Trace message to be logged.
	 */
	public static void out(String textToBeLogged) {
		String trace = '\n' + textToBeLogged;

		StringBuilder sbf = Spit.bufferHolder.get();
		if (sbf == null) {
			Spit.logger.info(trace);
		} else {
			sbf.append(trace);
		}
	}

	/**
	 * Print out the exception stack trace
	 * 
	 * @param e
	 *            The exception object to print.
	 */
	public static void out(Exception e) {
		StringWriter writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		e.printStackTrace(pw);
		String trace = writer.getBuffer().toString();
		Spit.out(trace);
	}

	/**
	 * spit stack trace
	 * 
	 * @param e
	 */
	public static void out(Throwable e) {
		StringWriter writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		e.printStackTrace(pw);
		String trace = writer.getBuffer().toString();
		Spit.out(trace);
	}

	/**
	 * Stop writer and return accumulated log information.
	 * 
	 * @return empty string if startWriter() was not called, or no text was
	 *         logged after startWriter()
	 */
	public static String stopWriter() {
		StringBuilder sbf = Spit.bufferHolder.get();
		Spit.bufferHolder.set(null);
		if (sbf != null) {
			return sbf.toString();
		}
		return "";
	}

	/***
	 * logs the text within an xml-like tag with userId="forUser" and
	 * serviceId="forService" as attributes. This will help in seelctingn the
	 * relevant log during trouble shooting.
	 * 
	 * @param textToBeLogged
	 * @param forUser
	 * @param forService
	 */
	public static void writeServiceLog(String textToBeLogged, String forUser,
			String forService) {
		Spit.logger.info("<serviceLog userId=\"" + forUser + "\" serviceId=\""
				+ forService + "\"> " + textToBeLogged + "</serviceLog>");
	}

	/**
	 * use out(String) instead
	 * 
	 * @param obj
	 *            toString() of this object is treated as text to be logged
	 */
	@Deprecated
	public static void out(Object obj) {
		Spit.out(obj.toString());
	}

	/***
	 * .net does not allow out as a method name, as out is reserved word. Since
	 * we use this package for net as well, this is an alias
	 * 
	 * @param obj
	 */
	@Deprecated
	public static void Out(Object obj) {
		Spit.out(obj.toString());
	}

	/***
	 * .net does not allow out as a method name, because out is a reserved word.
	 * Since we use this package for net as well, this is an alias
	 * 
	 * @param text
	 */
	public static void Out(String text) {
		Spit.out(text);
	}

	/**
	 * Not encouraged. Exility does not encourage using logging as a debugging
	 * technique. It strongly advises its developers to use debugger to debug
	 * the engine, and restrict logging only for application developers to trace
	 * their service.
	 * 
	 * @param caller
	 *            Instance of the object that called this method
	 * @param obj
	 *            Instance of the generic object to log.
	 */
	public static void out(Object caller, Object obj) {
		Spit.out(((caller == null) ? "" : caller.toString()) + obj.toString());
	}
}
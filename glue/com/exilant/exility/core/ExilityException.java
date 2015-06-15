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

import java.sql.SQLException;
import java.util.Iterator;

/**
 * Exception that exility detects as an issue with this service execution.
 * 
 */
public class ExilityException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	boolean messageToBeAdded = false;

	/**
	 * default
	 */
	public ExilityException() {
		super();
	}

	/**
	 * exception with a message text
	 * 
	 * @param message
	 */
	public ExilityException(String message) {
		super(message);
		this.messageToBeAdded = true;
		Spit.out(message);
	}

	/**
	 * exception because of another exception
	 * 
	 * @param e
	 */
	public ExilityException(Exception e) {
		super(e.getMessage());
		this.messageToBeAdded = true;
		Spit.out(e);
	}

	/**
	 * handles SqlException that is likely to be chained to get all messages
	 * 
	 * @param e
	 */
	public ExilityException(SQLException e) {
		super((e.getNextException() != null) ? e.getNextException()
				.getMessage() : e.getMessage());
		Spit.out(e);
		StringBuilder msg = new StringBuilder();
		Iterator<Throwable> iterator = e.iterator();
		while (iterator.hasNext()) {
			Throwable e1 = iterator.next();
			msg.append(e1.getMessage()).append('\n');
		}
		Spit.out(msg.toString());
		this.messageToBeAdded = true;
	}

	/**
	 * exception when another exception occurred, with some custom message
	 * 
	 * @param message
	 * @param e
	 */
	public ExilityException(String message, Exception e) {
		super(message + "\n" + e.getMessage());
		this.messageToBeAdded = true;
		this.setStackTrace(e.getStackTrace());
		Spit.out(message + "\n" + e.getMessage());
	}
}
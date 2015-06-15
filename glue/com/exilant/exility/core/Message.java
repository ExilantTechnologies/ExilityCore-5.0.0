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
 * Represents a message to be used at run time
 * 
 */
public class Message {
	/***
	 * used by object manager to extract all relevant attributes for
	 * upload/download
	 */
	private static final String[] ALL_ATTRIBUTES = { "name", "text",
			"severity", "forClient" };

	/***
	 * common message for outputting unexpected error. This is just a place
	 * holder that uses the entire text supplied at run time as error message
	 * text
	 */
	public static final String EXILITY_ERROR = "exilityError";

	/***
	 * common warning message where in the entire text supplied at run time is
	 * used as warning text
	 */
	public static final String EXILITY_WARNING = "exilityWarning";

	/***
	 * common message to send any information
	 */
	public static final String EXILITY_INFO = "exilityInfo";

	/***
	 * name is to be unique within a project
	 */
	String name;

	/***
	 * text in English. Resource bundle can be used for i18n. text can contain @1
	 * etc.. as place holder to receive parameters at run time
	 */
	String text;

	/***
	 * is this used by client-side of the application? messages marked for
	 * client are made available to client side engine at run time
	 */
	boolean forClient = false;

	/***
	 * In a product scenario, this design can be extended to make it an
	 * installation dependent parameter
	 */
	MessageSeverity severity = MessageSeverity.UNDEFINED;

	/**
	 * 
	 */
	public Message() {
	}

	/***
	 * Return a message object with place holders replaced with the supplied
	 * parameters.
	 * 
	 * @param args
	 *            values for place holders
	 * @return message whose text is formatted
	 */
	Message getFormattedMessage(String[] args) {
		Message msg = new Message();
		msg.name = this.name;
		msg.severity = this.severity;

		String str = this.text;

		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				if (args[i] != null) {
					str = str.replaceAll("@" + (i + 1), args[i]);
				}
			}
		}
		msg.text = str;
		return msg;
	}

	/***
	 * Convenient override
	 */
	@Override
	public String toString() {
		return this.name;
	}

	/***
	 * inspect the text and see if it has place holders, and report the max
	 * index used
	 * 
	 * @return max index of parameter used.
	 */
	public int getNumberOfParameters() {
		for (char i = '1'; i <= '9'; i++) {
			if (this.text.indexOf("@" + i) < 0) {
				return i - '1';
			}
		}
		return 9;
	}

	/***
	 * Used by data type generator to mark this message to be sent to client
	 */
	void markForClient() {
		this.forClient = true;
	}

	/**
	 * 
	 * @return name of this message
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 
	 * @return all loadable attributes
	 */
	public String[] getLoadableAttributes() {
		return ALL_ATTRIBUTES;
	}
}
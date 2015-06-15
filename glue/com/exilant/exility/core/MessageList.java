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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/***
 * Holds a list of Error Objects This is designed specifically to collect
 * several messages, possibly from different routines. It can be 'passed around'
 * by objects to collect all messages before passing it to user interface. Refer
 * to Messages for details of a message
 * 
 */

public class MessageList {
	// private static final String[] HEADER = {"messageId", "severity",
	// "message"};
	private final ArrayList<Message> messages;
	private MessageSeverity highestSeverity = MessageSeverity.IGNORE;
	private final Set<String> messageIds;

	/**
	 * default
	 */
	public MessageList() {
		this.messages = new ArrayList<Message>();
		this.messageIds = new HashSet<String>();
	}

	/***
	 * @return highest severity of all messages in the list
	 */
	public MessageSeverity getSevirity() {
		return this.highestSeverity;
	}

	MessageSeverity addMessage(String messageName, String[] parameters) {
		return this.addMessage(Messages.getMessage(messageName, parameters));
	}

	void addError(String errorText) {
		Message message = Messages.getMessage(Message.EXILITY_ERROR);
		if (message == null) {
			message = new Message();
			message.name = Message.EXILITY_ERROR;
		}
		message.text = errorText;
		message.severity = MessageSeverity.ERROR;
		this.addMessage(message);
	}

	void addWarning(String warningText) {
		Message message = Messages.getMessage(Message.EXILITY_WARNING);
		if (message == null) {
			message = new Message();
			message.name = Message.EXILITY_WARNING;
		}
		message.text = warningText;
		message.severity = MessageSeverity.WARNING;
		this.addMessage(message);
	}

	void addInfo(String infoText) {
		Message message = Messages.getMessage(Message.EXILITY_INFO);
		if (message == null) {
			message = new Message();
			message.name = Message.EXILITY_INFO;
		}
		message.text = infoText;
		message.severity = MessageSeverity.INFO;
		this.addMessage(message);
	}

	private MessageSeverity addMessage(Message message) {
		if (message.severity != MessageSeverity.IGNORE) {
			this.messages.add(message);
			this.messageIds.add(message.name);
			if (message.severity.compareTo(this.highestSeverity) > 0) {
				this.highestSeverity = message.severity;
			}
		}
		return message.severity;
	}

	/**
	 * 
	 * @return true if there is at least one message whose severity level is
	 *         error
	 */
	public boolean hasError() {
		return ((this.highestSeverity == MessageSeverity.ERROR));
	}

	/***
	 * is this message in the list?
	 * 
	 * @param messageId
	 *            message-id to be matched
	 * @return true if it is found in the list
	 */
	public boolean hasMessage(String messageId) {
		return this.messageIds.contains(messageId);
	}

	Message get(int idx) {
		try {
			return this.messages.get(idx);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	MessageSeverity getLastSeverity() {
		int n = this.messages.size();
		if (n == 0) {
			return MessageSeverity.IGNORE;
		}
		return this.messages.get(n - 1).severity;
	}

	/**
	 * 
	 * @return array of all messages in display format
	 */
	public String[] getMessageTexts() {
		String[] texts = new String[this.messages.size()];
		int i = 0;
		for (Message msg : this.messages) {
			texts[i] = msg.text;
			i++;
		}
		return texts;
	}

	/***
	 * 
	 * @return array of warning messages from the list
	 */
	public String[] getWarningTexts() {
		List<String> texts = new ArrayList<String>();
		for (Message msg : this.messages) {
			if (msg.severity == MessageSeverity.WARNING
					|| msg.severity == MessageSeverity.UNDEFINED) {
				texts.add(msg.text);
			}
		}
		return texts.toArray(new String[0]);
	}

	/***
	 * 
	 * @return array of information messages from the list
	 */
	public String[] getInfoTexts() {
		List<String> texts = new ArrayList<String>();
		for (Message msg : this.messages) {
			if (msg.severity == MessageSeverity.INFO) {
				texts.add(msg.text);
			}
		}
		return texts.toArray(new String[0]);
	}

	/***
	 * 
	 * @return array of error messages (excludes info/warning etc..) from the
	 *         message list
	 */
	public String[] getErrorTexts() {
		List<String> texts = new ArrayList<String>();
		for (Message msg : this.messages) {
			if (msg.severity == MessageSeverity.ERROR) {
				texts.add(msg.text);
			}
		}
		return texts.toArray(new String[0]);
	}

	/***
	 * 
	 * @return number of messages in the message list
	 */
	public int size() {
		return this.messages.size();
	}

	/***
	 * return messages as a grid. severity and text are the columns. first row
	 * is teh header
	 * 
	 * @return grid
	 */
	public String[][] toGrid() {
		int n = this.messages.size() + 1; // 1 for header
		String[][] grid = new String[n][];
		String[] hdr = { "severity", "text" };
		grid[0] = hdr;
		for (int i = 1; i < n; i++) {
			Message msg = this.messages.get(i - 1);
			String[] row = { msg.severity.toString(), msg.text };
			grid[i] = row;
		}
		return grid;
	}

	/**
	 * copy messages from another list
	 * 
	 * @param messageListToCopyFrom
	 */
	public void copyFrom(MessageList messageListToCopyFrom) {
		for (Message msg : messageListToCopyFrom.messages) {
			this.addMessage(msg);
		}
	}
}
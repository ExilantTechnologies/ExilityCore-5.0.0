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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/***
 * manages messages
 * 
 */
public class Messages {
	private static final String[] HEADER_ROW = { "name", "text", "severity" };

	/**
	 * input grid has less rows than the min rows expected
	 */
	public static final String EXIL_MIN_ROWS = "exilMinRows";

	/**
	 * input grid has more rows than the max rows expected
	 */
	public static final String EXIL_MAX_ROWS = "exilMixRows";

	private static final Map<String, String> internalMessages = new HashMap<String, String>();
	private static Messages instance = new Messages();

	Map<String, Message> messages = new HashMap<String, Message>();

	static {
		internalMessages.put(EXIL_MIN_ROWS,
				"A minimum of @2 rows expected in table @1");
		internalMessages.put(EXIL_MAX_ROWS,
				"A maximum of @2 rows expected in table @1");
	}

	static Messages getInstance() {
		return Messages.instance;
	}

	/**
	 * default
	 */
	public Messages() {
	}

	/**
	 * get a message
	 * 
	 * @param messageName
	 * @return message with this name, or null if it is not there
	 */
	public static Message getMessage(String messageName) {
		return Messages.instance.messages.get(messageName);
	}

	/**
	 * get all messages. to be re-looked and see why is some one getting this.
	 * 
	 * @return all messages
	 */
	public Map<String, Message> getMessages() {
		return this.messages;
	}

	/**
	 * set/replace a message
	 * 
	 * @param message
	 */
	public static void setMessage(Message message) {
		Messages.instance.messages.remove(message.name);
		Messages.instance.messages.put(message.name, message);
	}

	/**
	 * remove a message
	 * 
	 * @param message
	 */
	public static void removeMessage(Message message) {
		Messages.instance.messages.remove(message.name);
	}

	/**
	 * get a message. If this is not found, a new one is created with ERROR
	 * severity. Whenever end users see this message, it is a reminder for
	 * project to add this message to messages file
	 * 
	 * @param code
	 * @param parameters
	 * @return message
	 */
	public static Message getMessage(String code, String[] parameters) {
		Message messageToReturn = null;
		if (Messages.instance.messages.containsKey(code)) {
			return Messages.instance.messages.get(code).getFormattedMessage(
					parameters);
		}

		// OK The message is not defined. Let us create a new one and return
		messageToReturn = new Message();
		messageToReturn.name = code;
		messageToReturn.severity = MessageSeverity.ERROR;
		String txt = code;
		if (parameters != null) {
			for (String parameter : parameters) {
				txt += " : " + parameter + '\t';
			}
			messageToReturn.text = txt;
		}
		Spit.out("Message "
				+ code
				+ " is not defined. An error message with this name is created and added.");
		return messageToReturn;
	}

	static void reload(boolean removeExistingMessages) {
		if (removeExistingMessages || Messages.instance == null) {
			Messages.instance = new Messages();
		}
		Messages.load();
	}

	static synchronized void load() {
		try {
			Map<String, Object> msgs = ResourceManager.loadFromFileOrFolder(
					"messages", "message", ".xml");
			for (String fileName : msgs.keySet()) {
				Object obj = msgs.get(fileName);
				if (obj instanceof Messages == false) {
					Spit.out("message folder contains an xml that is not messages. File ignored.");
					continue;
				}
				Messages msg = (Messages) obj;
				Messages.instance.copyFrom(msg);
			}
			Spit.out(Messages.instance.messages.size() + " messages loaded.");
		} catch (Exception e) {
			Spit.out("Unable to load messages. Error : " + e.getMessage());
			Spit.out(e);
		}
		/*
		 * we have some issue with projects over-riding severity of exility
		 * error. Since transaction processing depends on this, we have to
		 * over-ride these back to errors
		 */
		Message msg = instance.messages.get(Message.EXILITY_ERROR);
		if (msg != null) {
			msg.severity = MessageSeverity.ERROR;
		}
	}

	private void copyFrom(Messages msgs) {
		for (Message m : msgs.messages.values()) {
			if (this.messages.containsKey(m.name)) {
				Spit.out("Error : message " + m.name
						+ " is defined more than once");
				continue;
			}
			this.messages.put(m.name, m);
		}
	}

	static String getMessageText(String code) {
		Message message = Messages.instance.messages.get(code);
		if (message == null) {
			return code + " is not defined.";
		}

		return message.text;

	}

	static MessageSeverity getSeverity(String code) {
		Message message = Messages.instance.messages.get(code);
		if (message == null) {
			return MessageSeverity.UNDEFINED;
		}

		return message.severity;

	}

	/**
	 * return all messages as a grid, with first row as header.
	 * 
	 * @return all messages as an arroy of messages, with first row as header.
	 */
	public static String[][] getAllInGrid() {
		String[] names = Messages.instance.messages.keySet().toArray(
				new String[0]);
		Arrays.sort(names);
		String[][] rows = new String[names.length + 1][];
		rows[0] = Messages.HEADER_ROW;
		int i = 1;
		for (String aname : names) {
			String[] arow = new String[3];
			arow[0] = aname;
			Message msg = Messages.instance.messages.get(aname);
			arow[1] = msg.text;
			arow[2] = msg.severity.toString();
			rows[i] = arow;
			i++;
		}
		return rows;
	}

	/**
	 * get messages that start with given string
	 * 
	 * @param startngName
	 * @return matching list of messages
	 */
	public static String[][] getMatchingMessages(String startngName) {
		String stringToMatch = startngName.toLowerCase();
		String[] names = Messages.instance.messages.keySet().toArray(
				new String[0]);
		int nbr = 0; // number filtered
		for (int i = 0; i < names.length; i++) {
			if (names[i].toLowerCase().startsWith(stringToMatch)) {
				names[nbr] = names[i];
				nbr++;
			}
		}
		String[] filteredNames = new String[nbr];
		for (int i = 0; i < filteredNames.length; i++) {
			filteredNames[i] = names[i];
		}
		Arrays.sort(filteredNames);
		String[][] rows = new String[filteredNames.length + 1][];
		rows[0] = Messages.HEADER_ROW;
		int i = 1;
		for (String aname : filteredNames) {
			String[] arow = new String[3];
			arow[0] = aname;
			Message msg = Messages.instance.messages.get(aname);
			arow[1] = msg.text;
			arow[2] = msg.severity.toString();
			rows[i] = arow;
			i++;
		}
		return rows;
	}

	/***
	 * get messages that are meant for client
	 * 
	 * @return messages that are meant for client
	 */
	static Collection<Message> getClientMessages() {
		Collection<Message> msgs = new ArrayList<Message>();
		for (Message msg : Messages.instance.messages.values()) {
			if (msg.forClient) {
				msgs.add(msg);
			}
		}
		return msgs;
	}

	/**
	 * @return sorted array of message names
	 */
	public static String[] gelAllMessages() {
		String[] allOfThem = getInstance().messages.keySet().toArray(
				new String[0]);
		Arrays.sort(allOfThem);
		return allOfThem;
	}
}
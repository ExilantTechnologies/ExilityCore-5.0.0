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

import java.util.Map;

/***
 * Save messages into a message file. extends GetMessage just to reuse
 * getFileName()
 * 
 * @author Exilant Technologies
 * 
 */
public class SaveMessages extends GetMessages {

	/***
	 * grid with name messages is expected in dc with bulkAction as action, and
	 * other message attributes as column. Messages collection in memory is
	 * updated and resource file is saved. Unlike RDBMS update, we do not
	 * guarantee against concurrent updates here. Feature wise this is fine.
	 */
	@Override
	public int execute(DataCollection dc, DbHandle dbHandle, String gridName,
			String[] parameters) {
		Grid messagesGrid = dc.getGrid("messages");
		if (messagesGrid == null) {
			dc.addError("Data for messages is expected in 'messages' table. It is missing.");
			return 0;
		}

		int n = messagesGrid.getNumberOfRows();

		// first we validate grid against memory
		Message[] messagesInAnArray = new Message[n]; // hold parsed messages
		String[] actions = new String[n]; // and actions
		boolean gotError = false;
		Message message = null;
		String action = null;

		for (int i = 0; i < n; i++) {
			// copy columns of this row into dc.values, so that Object manager
			// can create a Message instance with those attributes
			messagesGrid.copyRowToDc(i, null, dc);

			message = new Message();
			ObjectManager.fromDc(message, dc);
			messagesInAnArray[i] = message;

			// let us validate
			action = actions[i] = dc.getTextValue("bulkAction", null);
			Message existingMessage = Messages.getMessage(message.name);
			if (action.equals("add")) {
				if (existingMessage != null) {
					dc.addMessage("error", message.name + " (row " + (i + 1)
							+ ") already exists with severity "
							+ message.severity + " and text \"" + message.text
							+ "\".");
					gotError = true;
				}
			} else {
				if (existingMessage == null) {
					dc.addMessage("error", message.name + " (row " + (i + 1)
							+ ") not found.");
					gotError = true;
				}
			}
		}

		if (gotError) {
			return 0;
		}

		Messages messages = new Messages(); // one to be saved
		Map<String, Message> msgs = messages.messages;
		int nbrSaved = 0;
		for (int i = 0; i < n; i++) {
			action = actions[i];
			message = messagesInAnArray[i];

			// remove from memory first
			if (!action.equals("add")) {
				Messages.removeMessage(message);
			}

			// add the latest one, unless it is to be deleted
			if (!action.equals("delete")) {
				Messages.setMessage(message);
				msgs.put(message.name, message);
				nbrSaved++;
			}
		}

		// save resource file
		String fullName = this.getFileName(dc);

		ResourceManager.saveResource(fullName, messages);
		dc.addMessage("info", nbrSaved + " messages saved to resource "
				+ fullName);
		return 1;
	}

}
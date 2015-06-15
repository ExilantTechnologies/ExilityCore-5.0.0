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
 * Service to get messages from a message file, used by Exility IDE
 * 
 */
public class GetMessages implements CustomCodeInterface {
	// Large part of this small class is common between get and save. Hence save
	// extends get.
	@Override
	public int execute(DataCollection dc, DbHandle dbHandle, String gridName,
			String[] parameters) {
		String[][] data = null;
		if (dc.hasValue(CommonFieldNames.FILE_NAME)) {
			String fullName = this.getFileName(dc);
			Messages msgs = this.readMessageFile(dc, fullName);
			if (msgs != null) {
				data = ObjectManager.getAttributes(msgs.messages);
			}
		} else {
			data = Messages.getAllInGrid();
		}
		if (data != null) {
			dc.addGrid("messages", data);
		}
		return 1;
	}

	protected Messages readMessageFile(DataCollection dc, String fullName) {
		Object obj = ResourceManager.loadResource(fullName, Messages.class);
		if (obj != null && obj instanceof Messages) {
			return (Messages) obj;
		}

		dc.addError(fullName
				+ " could not be read and interpreted as MessageList");
		return null;
	}

	protected String getFileName(DataCollection dc) {
		String fileName = dc.getTextValue(CommonFieldNames.FILE_NAME, "");
		String fullName = "messages";
		if (fileName.length() > 0) {
			fullName = "message/" + fileName;
		}
		return fullName;
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.NONE;
	}
}

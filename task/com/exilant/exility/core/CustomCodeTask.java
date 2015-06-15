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
 * Wrapper on a class that user writes to implement a part of the service.
 * 
 */
public class CustomCodeTask extends ExilityTask {
	String fullyQualifiedClassName = null;

	@Override
	public int executeBulkTask(DataCollection dc, DbHandle handle)
			throws ExilityException {
		throw new ExilityException(
				"custom task should not be called to be repeaetd for each row of the grid");
	}

	@Override
	public int executeTask(DataCollection dc, DbHandle handle)
			throws ExilityException {
		Object obj = ObjectManager.createNew(this.fullyQualifiedClassName);
		if (obj == null || obj instanceof CustomCodeInterface == false) {
			String msg = "Design Error: "
					+ this.fullyQualifiedClassName
					+ " is not a class, or it does not implement CustomCodeInterface";
			Spit.out(msg);
			dc.raiseException(Message.EXILITY_ERROR, msg);
			return 0;
		}
		return ((CustomCodeInterface) obj).execute(dc, handle, this.gridName,
				this.taskParameters);
	}

	@Override
	public DataAccessType getDataAccessType() {
		Object obj = ObjectManager.createNew(this.fullyQualifiedClassName);
		if (obj == null || obj instanceof CustomCodeInterface == false) {
			return DataAccessType.NONE;
		}
		return ((CustomCodeInterface) obj).getDataAccessType();
	}
}

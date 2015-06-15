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
 * read or save an xml resource
 */
public class xmlTask extends ExilityTask {

	/**
	 * if this is a standard resource that is known to Exility resource manager
	 */
	String resourceType = null;
	String folderName = null;
	String resourceName = null;
	boolean toBeSaved = false;
	String fullyQualifiedClassName = null;

	private Class<? extends Object> rootClass = null;

	@Override
	public int executeTask(DataCollection dc, DbHandle handle)
			throws ExilityException {

		if (this.resourceName == null || this.fullyQualifiedClassName == null) {
			dc.raiseException("resourceName and fullyQualifiedClassNAme are required for xmlTask "
					+ this.taskName);
			return 0;
		}
		String fileName = this.resourceName.replace('.', '/') + ".xml";
		if (this.rootClass == null) {
			try {
				this.rootClass = Class.forName(this.fullyQualifiedClassName);
			} catch (Exception e) {
				dc.raiseException(this.taskName
						+ " has an invalid class name set. Error : "
						+ e.getMessage());
				return 0;
			}
		}
		/*
		 * we expect either folder, or resource type.
		 */
		if (this.folderName != null) {
			fileName = this.folderName + '/' + fileName;
		} else if (this.resourceType != null) {
			fileName = ResourceManager.getResourceFolder() + fileName;
		} else {
			dc.raiseException("Either resource type or resource folder name must be specified for xml task "
					+ this.taskName);
			return 0;
		}
		return 1;
	}

	@Override
	public int executeBulkTask(DataCollection dc, DbHandle handle)
			throws ExilityException {
		Grid grid = dc.getGrid(this.gridName);
		if (grid == null || grid.getNumberOfRows() == 0) {
			return 0;
		}
		return this.getTable(dc).deleteFromGrid(dc, handle, grid);
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.READWRITE;
	}
}

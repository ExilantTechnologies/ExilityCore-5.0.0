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
 * Named service that handles list service calls from client. Designed to handle
 * multiple request sin one call. Each row in the named grid (listServiceIds) is
 * treated as one service request
 * 
 */
class ListService {
	/***
	 * Returns a new instance. Just keeping options open for creating different
	 * instances if required.
	 * 
	 * @return a new instance of ListService
	 */
	static ListService getService() {
		return new ListService();
	}

	/***
	 * private so that new ListService() is not called. use
	 * ListService.getService() instead
	 */
	private ListService() {

	}

	/**
	 * execute this service. Execute each of the list services in the list
	 * 
	 * @param dc
	 * @throws ExilityException
	 */
	void execute(DataCollection dc) throws ExilityException {
		/*
		 * listService may request several services in one request. These are
		 * requested in a grid Grid has a header row, and each additional row
		 * has two columns. first column is the listServiceName, and second
		 * column is keyValue.
		 */
		Grid grid = dc.getGrid(CommonFieldNames.LIST_SERVICE_IDS);
		if (grid == null) {
			dc.addError("A grid named " + CommonFieldNames.LIST_SERVICE_IDS
					+ " was not sent by client.");
			return;
		}

		/*
		 * let the grid not clutter dc any more
		 */
		dc.removeGrid(CommonFieldNames.LIST_SERVICE_IDS);

		/*
		 * for iterating thru the rows, we convert into a simple array of text
		 * rows
		 */
		String[][] services = grid.getRawData();

		/*
		 * list services do not update database...
		 */
		DbHandle handle = DbHandle.borrowHandle(DataAccessType.READONLY,
				dc.getBooleanValue(ExilityConstants.SUPPRESS_SQL_LOG, false));
		/*
		 * we have opened a db handle. We should not allow any exception to keep
		 */
		try {
			/*
			 * note that the first row is the header.
			 */
			for (int i = 1; i < services.length; i++) {
				String[] serviceRow = services[i];
				String serviceName = serviceRow[0];
				String keyValue = (serviceRow.length > 1) ? serviceRow[1] : "";

				/*
				 * serviceName and keyValues are used by sqls as parameters
				 */
				dc.addTextValue("serviceName", serviceName);
				dc.addTextValue("keyValue", keyValue);

				/*
				 * grid name in which to return the rows is serviceName or
				 * serviceName_keyValue
				 */
				String gridName = serviceName;
				if (keyValue != null && keyValue.length() > 0) {
					gridName += '_' + keyValue;
				}

				/*
				 * listservice name could be a sql. It is more often that, and
				 * hence we check for that first
				 */
				SqlInterface sql = Sqls.getSqlTemplateOrNull(serviceName);
				if (sql != null) {
					sql.execute(dc, handle, gridName, null);
					continue;
				}

				ServiceInterface thisService = Services.getService(serviceName,
						dc);

				if (thisService == null) {
					// there is no service by this name as well..
					String errorText = "ERROR: list service " + serviceName
							+ " is neither a sql nor a service.";
					Spit.out(errorText);
					dc.addError(errorText);
					continue;
				}

				thisService.execute(dc, handle);

				/*
				 * Service designer is supposed to put the result in a grid with
				 * this specific grid name
				 */
				if (dc.hasGrid(gridName) == false) {
					/*
					 * historically, we were flexible, and allowed the designer
					 * to get away so long as there was only one grid in the dc
					 */
					String[] names = dc.getGridNames();
					if (names.length == 0) {
						Spit.out(serviceName
								+ " is called as a list service, but it did not produce any grid");
					} else {
						/*
						 * rename the grid. Unfortunately, rename is not
						 * available, but the alternate, though couple of lines
						 * of extra code, is not actually expensive
						 */
						Spit.out("Going to get grid named " + names[0]
								+ " from dc and rename it as " + gridName);
						Grid serviceDataGrid = dc.getGrid(names[0]);
						serviceDataGrid.setName(gridName);
						dc.removeGrid(names[0]);
						dc.addGrid(gridName, serviceDataGrid);
					}
				}
			}
		} catch (Exception e) {
			dc.addError(e.getMessage());
		}
		// let us not forget to close the connection before getting out
		DbHandle.returnHandle(handle);
	}
}
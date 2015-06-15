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

class ReportService {
	private static final String SERVICE_TABLE_NAME = "reportServiceIds";

	/**
	 * this is a singleton implementation as of now
	 */
	private static ReportService instance = new ReportService();

	/**
	 * get an instance. It is a singleton as of now
	 * 
	 * @return
	 */
	static ReportService getService() {
		return ReportService.instance;
	}

	/**
	 * execute and get data for the report
	 * 
	 * @param dc
	 * @throws ExilityException
	 */
	void execute(DataCollection dc) throws ExilityException {
		/*
		 * reports, by definition, have to read-only
		 */
		DbHandle handle = DbHandle.borrowHandle(DataAccessType.READONLY,
				dc.getBooleanValue(ExilityConstants.SUPPRESS_SQL_LOG, false));

		/*
		 * we have taken a db handle. he HAVE to return. Hence the try-catch
		 * block
		 */
		try {
			/*
			 * original designer cloned this from list service. I can not
			 * imagine a client wanting several report requests in one. But do
			 * not want to change the design as of now. Hence we continue to
			 * expect reportServices in a grid
			 */
			Grid grid = dc.getGrid(ReportService.SERVICE_TABLE_NAME);
			if (grid == null) {
				dc.addError("Internal design error : Report service is unable to get list services in a grid named "
						+ ReportService.SERVICE_TABLE_NAME);
				return;
			}
			String[][] services = grid.getRawData();
			/*
			 * raw data has its first row as header. Skip that.
			 */
			for (int i = 1; i < services.length; i++) {
				String[] serviceRow = services[i];

				/*
				 * serviceName and keyValues are pushed to dc.fields collection.
				 * That is the convention for you to write your service for
				 * report
				 */
				String serviceName = serviceRow[0];
				String keyValue = (serviceRow.length > 1) ? serviceRow[1] : "";
				dc.addTextValue("serviceName", serviceName);
				dc.addTextValue("keyValue", keyValue);

				/*
				 * there should be a service with that name failing which we
				 * look for a sql by that name
				 */
				ServiceInterface service = Services.getService(serviceName, dc);
				if (service != null) {
					service.execute(dc, handle);
				} else {
					SqlInterface sql = Sqls.getTemplate(serviceName, dc);
					if (sql == null) {
						dc.addError("Design error no service or sql found with name "
								+ serviceName);
						continue;
					}

					/*
					 * and the service is supposed to put the result back in a
					 * grid with the service name or serviceName+keyVAlue
					 */
					String gridName = serviceName;
					if (keyValue != null && keyValue.length() > 0) {
						gridName += '_' + keyValue;
					}
					sql.execute(dc, handle, gridName, null);

				}
			}
		} catch (Exception e) {
			dc.addError("unexpected error : " + e.getMessage());
		}

		DbHandle.returnHandle(handle);
	}

	/**
	 * called by resource manager in case the project needs to be reset without
	 * re-starting the server
	 */
	static void reload() {
		ReportService.instance = new ReportService();
	}
}
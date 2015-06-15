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
 * Way to design simple batch processing. Design a service(called batchService)
 * that does the work for one entity Once this is working for you, you can
 * design another service(called inputService) that prepares a grid (called
 * inputGridName), each row of which has all the values of the entity being
 * processed in batch. You may also accumulate information that you want to
 * write-down at the end like status of each entity that was processed, and
 * summary, in the dc, which can then be written in another service(called
 * logServiceName)
 */
class BatchEntry extends ServiceEntry {
	/***
	 * service to be executed at the beginning to populate inputGrid. This is
	 * assumed to be only extracting, and not doing any updates
	 */
	String inputServiceName = null;

	/***
	 * grid that contains data for batch service, one set of data per row.
	 * BatchService is executed for each row in this grid
	 */
	String inputGridName = null;

	/***
	 * an alternative to service is sql. In this case, grid name is optional. If
	 * grid is not specified, data is not extracted to the grid, but net effect
	 * is as if the rows were extracted to teh grid, and the batch service is
	 * executed once for each row's data in teh dc.
	 */
	String inputSqlName = null;
	/***
	 * service that is executed in batch for each row in inputGrid
	 */
	String batchServiceName = null;

	/***
	 * service that is executed after the batch service. Typically used to write
	 * out logs accumulated for the batch run
	 */
	String logServiceName = null;

	@Override
	void serve(DataCollection dc) {
		try {
			if (this.inputSqlName != null) {
				/**
				 * this is a sql based batch design
				 */
				this.iterateWithSql(dc);
			} else {
				if (this.inputServiceName != null) {
					this.executeInputService(dc);
				}
				Grid grid = dc.getGrid(this.inputGridName);
				if (grid == null) {
					dc.addError("Batch process " + this.name
							+ " : input service did not generate grid "
							+ this.inputGridName);
				} else {
					this.iterateWithGrid(dc, grid);
				}

			}
			if (this.logServiceName != null) {
				this.executeLogService(dc);
			}
		} catch (Exception e) {
			Spit.out(e);
			dc.addError(e.getMessage());
		}
	}

	/***
	 * execute the input service that is to put rows into grid
	 * 
	 * @param dc
	 * @throws ExilityException
	 */
	private void executeInputService(DataCollection dc) throws ExilityException {
		/*
		 * dbHandle is a resource that HAS to be returned. So we will have to
		 * have try-finally.
		 */
		DbHandle dbHandle = null;
		try {
			dbHandle = DbHandle.borrowHandle(DataAccessType.READONLY);
			ServiceInterface service = Services.getService(
					this.inputServiceName, dc);
			service.execute(dc, dbHandle);
		} catch (Exception e) {
			dc.addError(e.getMessage());
		} finally {
			/*
			 * return is safe. It handles possible null as its argument
			 */
			DbHandle.returnHandle(dbHandle);
		}
	}

	/***
	 * execute service that will carry out required updates at the end of batch
	 * process
	 * 
	 * @param dc
	 * @throws ExilityException
	 */
	private void executeLogService(DataCollection dc) throws ExilityException {
		DbHandle dbHandle = null;
		try {
			dbHandle = DbHandle.borrowHandle(DataAccessType.READWRITE);
			ServiceInterface service = Services.getService(this.logServiceName,
					dc);
			service.execute(dc, dbHandle);
		} catch (Exception e) {
			dc.addError(e.getMessage());
		} finally {
			DbHandle.returnHandle(dbHandle);
		}
	}

	/***
	 * execute batch service once for each row in the grid
	 * 
	 * @param dc
	 * @param grid
	 * @throws ExilityException
	 */
	private void iterateWithGrid(DataCollection dc, Grid grid)
			throws ExilityException {
		ServiceInterface service = Services.getService(this.batchServiceName,
				dc);
		DbHandle handle = DbHandle.borrowHandle(DataAccessType.READWRITE,
				dc.hasValue(ExilityConstants.SUPPRESS_SQL_LOG));
		int n = grid.getNumberOfRows();
		for (int i = 0; i < n; i++) {
			grid.copyRowToDc(i, "", dc);
			handle.beginTransaction();
			try {
				service.execute(dc, handle);
			} catch (Exception e) {
				dc.addError(e.getMessage());
			}

			if (dc.hasError()) {
				handle.rollback();
			} else {
				handle.commit();
			}
			dc.zapMessages();
		}
		DbHandle.returnHandle(handle);
	}

	/***
	 * execute batch service once for each row in the output of input sql
	 * 
	 * @param dc
	 * @throws ExilityException
	 */
	private void iterateWithSql(DataCollection dc) throws ExilityException {
		SqlInterface sql = Sqls.getTemplate(this.inputSqlName, dc);
		if (sql instanceof Sql == false) {
			throw new ExilityException(
					this.inputSqlName
							+ " is not a sql template. Batch service can not be defined using this sql.");
		}
		ServiceInterface service = Services.getService(this.batchServiceName,
				dc);
		DbHandle handle = DbHandle.borrowHandle(DataAccessType.READONLY);
		handle.callServiceForEachRow((Sql) sql, dc, service);
	}
}

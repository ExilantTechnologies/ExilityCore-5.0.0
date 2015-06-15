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
package com.exilant.exility.ide;

import com.exilant.exility.core.CustomCodeInterface;
import com.exilant.exility.core.DataAccessType;
import com.exilant.exility.core.DataCollection;
import com.exilant.exility.core.DbHandle;
import com.exilant.exility.core.ExilityException;
import com.exilant.exility.core.ServiceData;
import com.exilant.exility.core.ServiceInterface;
import com.exilant.exility.core.Spit;

/**
 * Implements a work flow that is modeled as a state-transition mechanism. It is
 * important to understand that we use state-transition in a strict academic
 * way. Our belief is that this modeling will be able to simplify most of our
 * application requirements. We keep the implementation fairly flexible to allow
 * any custom code that a specific application may require.
 * 
 */
public class IdeService implements ServiceInterface {
	/**
	 * name is the name of the ide :-) Hope that is helpful.
	 */
	private final String name;

	private final CustomCodeInterface ideTask;

	/**
	 * 
	 * @param serviceName
	 * @param ide
	 */
	public IdeService(String serviceName, CustomCodeInterface ide) {
		this.name = serviceName;
		this.ideTask = ide;
	}

	@Override
	public void execute(DataCollection dc, DbHandle handle)
			throws ExilityException {
		this.ideTask.execute(dc, handle, null, null);
	}

	@Override
	public DataAccessType getDataAccessType(DataCollection dc) {
		return this.ideTask.getDataAccessType();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void executeAsStep(DataCollection dc, DbHandle handle)
			throws ExilityException {
		this.ideTask.execute(dc, handle, null, null);

	}

	@Override
	public boolean toBeRunInBackground() {
		return false;
	}

	@Override
	public void serve(ServiceData inData, ServiceData outData) {
		DataCollection dc = new DataCollection();
		dc.copyFrom(inData);
		DataAccessType access = this.ideTask.getDataAccessType();
		DbHandle handle = null;
		try {
			if (access != DataAccessType.NONE) {
				handle = DbHandle.borrowHandle(access, false);
				if (access == DataAccessType.AUTOCOMMIT) {
					handle.startAutoCommit();
				} else if (access == DataAccessType.READWRITE) {
					handle.beginTransaction();
				}
			}
			try {
				this.execute(dc, handle);
			} catch (ExilityException e1) {
				// need not do anything
			} catch (Exception e) {
				dc.addError(e.getMessage());
			}
			if (handle != null) {
				if (access == DataAccessType.READWRITE) {
					if (dc.hasError()) {
						handle.rollback();
					} else {
						handle.commit();
					}
				}
			}
		} catch (Exception e) {
			Spit.out(e);
			dc.addError(e.getMessage());
		}
		DbHandle.returnHandle(handle);
		dc.copyTo(outData);
	}
}

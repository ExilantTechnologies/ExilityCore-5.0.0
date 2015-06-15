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
 * Exility design component that defines data requirement for a service, either
 * as input or as output
 * 
 */

class DataSpec implements ToBeInitializedInterface {
	static final String SERVICE_ID_NAME = "serviceId";

	/***
	 * lists that are expected
	 */
	ListSpec[] lists = new ListSpec[0];
	/***
	 * fields that are expected
	 */
	FieldSpec[] fields = new FieldSpec[0];
	/***
	 * expected grids/tables
	 */
	GridSpec[] grids = new GridSpec[0];

	/***
	 * whether any of the field is associated with a description service. This
	 * is set in initialize() as a small optimization
	 */
	private boolean hasDescService = false;

	DataSpec() {

	}

	/***
	 * Copy data from inData to dc as per specification
	 * 
	 * @param inData
	 * @param dc
	 */
	void translateInput(ServiceData inData, DataCollection dc) {
		DbHandle handle = null;
		try {
			if (this.hasDescService) {
				handle = DbHandle.borrowHandle(DataAccessType.READONLY);
			}

			for (FieldSpec field : this.fields) {
				field.translateInput(inData, dc);
			}

			for (FieldSpec field : this.fields) {
				field.validateField(dc, handle);
			}

			for (ListSpec list : this.lists) {
				list.translateInput(inData, dc);
			}

			for (GridSpec grid : this.grids) {
				grid.translateInput(inData, dc);
				grid.validate(dc, handle);
			}
			if (handle != null) {
				DbHandle.returnHandle(handle);
			}
		} catch (ExilityException e) {
			Spit.out(e);
			dc.addError(e.getMessage());
		}
		return;
	}

	/***
	 * copy output data from dc to serviceData based on this specification
	 * 
	 * @param dc
	 * @param outData
	 */
	void translateOutput(DataCollection dc, ServiceData outData) {
		for (FieldSpec field : this.fields) {
			field.translateOutput(dc, outData);
		}

		for (ListSpec list : this.lists) {
			list.translateOutput(dc, outData);
		}

		for (GridSpec grid : this.grids) {
			grid.translateOutput(dc, outData);
		}
		return;
	}

	/***
	 * copy data from one dc to another based on this specification
	 * 
	 * @param fromDc
	 * @param toDc
	 */
	void translate(DataCollection fromDc, DataCollection toDc) {
		for (FieldSpec field : this.fields) {
			field.translate(fromDc, toDc);
		}

		for (ListSpec list : this.lists) {
			list.translate(fromDc, toDc);
		}

		for (GridSpec grid : this.grids) {
			grid.translate(fromDc, toDc);
		}
	}

	@Override
	public void initialize() {
		/*
		 * small optimization to have a flag to see if we need to access db for
		 * any validation
		 */
		if (this.fields != null) {
			for (FieldSpec f : this.fields) {
				if (f.descServiceId != null) {
					this.hasDescService = true;
				}
			}
		}
		if (this.grids != null) {
			for (GridSpec gs : this.grids) {
				if (gs.columns == null) {
					continue;
				}
				for (FieldSpec f : gs.columns) {
					if (f.descServiceId != null) {
						this.hasDescService = true;
					}
				}
			}
		}
	}
}
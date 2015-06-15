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
 * Exility design component that specifies expected data in a list/array Not
 * used by any one, and would like to discontinue it. Recommending use of grid
 * with simple column instead.
 */
class ListSpec extends Parameter {
	/***
	 * min number of entries expected in the list
	 */
	int minCols = 0;

	/***
	 * max number of entries expected in the list
	 */
	int maxCols = 0;

	/***
	 * whether the list can have skip values in-between (nulls) or every member
	 * is required.
	 */
	boolean allEntriesRequired = false;

	ListSpec() {

	}

	/***
	 * extract data from input to dc as per this specification
	 * 
	 * @param inData
	 * @param dc
	 */
	void translateInput(ServiceData inData, DataCollection dc) {
		String[] val = inData.getList(this.name);
		// validate it before putting into dc
		if (val == null || val.length == 0) {
			if (this.isOptional == false) {
				dc.addMessage("exilListIsRequired", this.name);
			}
			return;
		}
		if (this.minCols > 0 && val.length < this.minCols) {
			dc.addMessage("exilMinCols", this.name, this.minCols + "");
			return;
		}
		if (this.maxCols > 0 && val.length > this.maxCols) {
			dc.addMessage("exilMaxCols", this.name, this.minCols + "");
			return;
		}
		/*
		 * all right. everything is in order
		 */
		AbstractDataType dt = this.getDataType();
		ValueList valueList = dt.getValueList(this.name, val,
				this.allEntriesRequired, dc);
		if (valueList != null) {
			dc.addValueList(this.name, valueList);
		}
	}

	/***
	 * copy data from dc to output serviceData as per this specification
	 * 
	 * @param dc
	 * @param outData
	 */
	void translateOutput(DataCollection dc, ServiceData outData) {
		/*
		 * no need to validate, but we may need some transformation..
		 */

		AbstractDataType dt = this.getDataType();
		ValueList valueList = dc.getValueList(this.name);
		if (valueList != null) {
			outData.addList(this.name, dt.format(valueList));
		}
	}

	/***
	 * copy data from one dc to the other based on this spec. Note that this is
	 * an internal call, and hence values are expected to be in order, and no
	 * validation is done
	 * 
	 * @param fromDc
	 * @param toDc
	 */
	public void translate(DataCollection fromDc, DataCollection toDc) {
		toDc.addValueList(this.name, fromDc.getValueList(this.name));
	}

}
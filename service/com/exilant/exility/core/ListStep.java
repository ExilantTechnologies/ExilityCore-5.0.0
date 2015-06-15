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
 * create a value list. We do not use list any more, but a list can be used to
 * create a grid
 * 
 */
class ListStep extends AbstractStep {
	/**
	 * name of the list to be created
	 */
	String name = null;
	/**
	 * list of values may come from a field at run time
	 */
	String fromFieldName = null;

	/**
	 * list of comma separated values that make up the list. USe this if values
	 * are known at design time
	 */
	String values = null;

	ListStep() {
		this.stepType = StepType.LISTSTEP;
	}

	@Override
	String executeStep(DataCollection dc, DbHandle handle)
			throws ExilityException {
		if (this.name == null || this.name.equals("")) {
			dc.addError("name not specified to create the list");
			return AbstractStep.NEXT;
		}

		String value = (this.values != null) ? this.values : dc.getTextValue(
				this.fromFieldName, "");

		dc.addValueList(this.name, new ValueList(value.split(",")));

		return AbstractStep.NEXT;
	}

}
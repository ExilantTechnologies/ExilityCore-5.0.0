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
 * execute a function and assign its result to a field. This needs to be a
 * separate step because our expression is still primitive, and can not contain
 * functions.
 * 
 */
class FunctionAssignmentStep extends AbstractStep {

	/**
	 * to which value has to be assigned to
	 */
	String fieldName = null;

	/**
	 * name of the function to be executed. This function has to be registered
	 * with Exility using functions.xml.
	 */
	String functionName = null;

	/**
	 * name of the fields that are picked-up as parameters for this function.
	 * You have to set the values into these fields before executing this step.
	 */
	String[] inputParameters = null;

	public FunctionAssignmentStep() {
		this.stepType = StepType.FUNCTIONSTEP;
	}

	@Override
	String executeStep(DataCollection dc, DbHandle handle)
			throws ExilityException {
		Value val = Functions.evaluateFunction(this.functionName,
				this.inputParameters, dc);
		dc.addValue(this.fieldName, val);
		return AbstractStep.NEXT;
	}
}
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

/*
 * traffic controller step. Go to a step based on the value of a field
 */
class SwitchStep extends AbstractStep {

	/**
	 * field that has the value to be used for deciding where to go
	 */
	String fieldName = null;

	/**
	 * list of values and associated label to go to. If we do not find a
	 * matching value, default is to take next step
	 */
	SwitchAction[] switchActions = new SwitchAction[0];

	SwitchStep() {
		this.stepType = StepType.SWITCHSTEP;
	}

	@Override
	String executeStep(DataCollection dc, DbHandle handle)
			throws ExilityException {
		String result = dc.getTextValue(this.fieldName, "");
		for (SwitchAction action : this.switchActions) {
			if (action.value.equals(result)) {
				return (action.labelToGoTo);
			}
		}
		return AbstractStep.NEXT;
	}

}

/**
 * data structure for switch step
 * 
 */
class SwitchAction {
	String value = null;
	String labelToGoTo = null;
}

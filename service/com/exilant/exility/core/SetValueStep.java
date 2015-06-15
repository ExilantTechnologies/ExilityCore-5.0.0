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
 * same as expression assignment step. But condition has been added by mistake.
 * This is one of the steps added for client-side compatibility
 * 
 */
class SetValueStep extends AbstractStep {
	/**
	 * field name
	 */
	String name = null;

	/**
	 * value to be assigned to the field
	 */
	Expression expression = null;
	/**
	 * condition. deprecated. should not be used, as condition of step itself is
	 * supposed to take take care of this
	 */
	Expression condition = null;

	SetValueStep() {
		this.stepType = StepType.SETVALUESTEP;
	}

	@Override
	String executeStep(DataCollection dc, DbHandle handle)
			throws ExilityException {
		if (this.name == null || this.name.equals("")) {
			dc.addError("The variable name not specified to set the value");
			return AbstractStep.NEXT;
		}

		if ((this.condition != null) && (!this.condition.toString().equals(""))) {
			if (!this.condition.evaluate(dc).getBooleanValue()) {
				return AbstractStep.NEXT;
			}
		}

		dc.addValue(this.name, this.expression.evaluate(dc));

		return AbstractStep.NEXT;
	}

}
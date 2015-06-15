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
 * Step that indicates the beginning of a loop. This step is not executed,
 * unlike all other steps, but is marked as the beginning of a loop. Service
 * uses a loopStepWorker to track when to loop-back and when to exit
 * 
 * 
 */
class LoopStep extends AbstractStep {
	/**
	 * table based on which loop is executed.
	 */
	String tableToLoopOn = null;

	/**
	 * fields that are copied from the current row into dc.values for each loop.
	 * By default, all columns are copied
	 */
	String[] inputFields = null;

	/**
	 * fields that are calculated during this loop which need to be updated into
	 * the current
	 */
	String[] outputFields = null;

	/**
	 * text to be prefixed to field names when columns from current row are
	 * copied to dc.values
	 */
	String prefix = null;

	/**
	 * what if the designer wants no fields to be copied per loop? if
	 * inputFields is not specified, we still assume all fields.
	 */
	boolean noInputFieldsPlease = false;

	/**
	 * label of the step that is the last step of this loop block
	 */
	String lastStepOfTheBlock = null;

	@Override
	String executeStep(DataCollection dc, DbHandle handle)
			throws ExilityException {
		throw new ExilityException("Loop step should not be executed");
	}

	public LoopStep() {
		this.stepType = StepType.LOOPSTEP;
	}
}
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
 * modify the grid. Several grid processors are designed to work on grid. This
 * is the wrapper on those processors to act as a step in a service
 * 
 */
class GridProcessorStep extends AbstractStep {
	GridProcessorInterface processor = null;
	/**
	 * processor returns 0 if work was not done. Do you want to control the next
	 * step based on this?
	 */
	String ifWorkDoneGoTo = AbstractStep.NEXT;
	/**
	 * processor returns 0 if work was not done. Do you want to control the next
	 * step based on this?
	 */
	String ifNoWorkDoneGoTo = AbstractStep.NEXT;

	public GridProcessorStep() {
		this.stepType = StepType.GRIDPROCESSINGSTEP;
	}

	/**
	 * concrete classes that implement an ExecutionStep will receive a
	 * taskContext and they have to implement execute method
	 */
	@Override
	String executeStep(DataCollection dc, DbHandle handle)
			throws ExilityException {
		int workDone = this.processor.process(dc);

		/*
		 * save work done info for any possible use by subsequent steps
		 */
		if (this.label != null) {
			dc.addIntegralValue(this.label + "WorkDone", workDone);
		}

		String nextStep = AbstractStep.NEXT;

		/*
		 * did the task do any work at all?
		 */
		if (workDone == 0) {
			if (this.ifNoWorkDoneGoTo != null) {
				nextStep = this.ifNoWorkDoneGoTo;
			}
		} else {
			if (this.ifWorkDoneGoTo != null) {
				nextStep = this.ifWorkDoneGoTo;
			}
		}

		return nextStep;
	}
}
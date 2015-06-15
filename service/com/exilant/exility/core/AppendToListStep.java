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
 * Add a value to the list. This is useful in creating a table at run time. We
 * haven't seen any one using this step though. TODO: if no one has actually
 * used it so far, better to deprecate this along with other list related steps
 * 
 */
class AppendToListStep extends AbstractStep {
	/**
	 * name of this list to which value is to be appended.
	 */
	String name = null;
	/**
	 * looks like this is an error. This is no different from executeOnCondition
	 */
	Expression condition = null;

	/**
	 * value to be added
	 */
	Expression expression = null;

	AppendToListStep() {
		this.stepType = StepType.APPENDTOLISTSTEP;
	}

	@Override
	String executeStep(DataCollection dc, DbHandle handle)
			throws ExilityException {

		/*
		 * should the step be executed at all? This attribute is redundant, but
		 * for sake of backward compatibility, we have to honor it
		 */
		if ((this.condition != null) && (!this.condition.toString().equals(""))) {
			if (!this.condition.evaluate(dc).getBooleanValue()) {
				return AbstractStep.NEXT;
			}
		}

		Value val = this.expression.evaluate(dc);
		DataValueType vt = val.getValueType();

		ValueList list = dc.getValueList(this.name);
		if (list == null) {
			list = ValueList.newList(vt, 1);
		} else {
			if (vt != list.getValueType()) {
				dc.addError(this.name + " is of type " + list.getValueType()
						+ " but the expression evaluated to type " + vt
						+ ". value not appended.");
				return AbstractStep.STOP;
			}
			/*
			 * we can not extend a list. We create a new one with extra capacity
			 * and copy existing ones
			 */
			ValueList templist = ValueList.newList(vt, list.length() + 1);
			for (int i = 0; i < list.length(); i++) {
				templist.setValue(list.getValue(i), i);
			}
			list = templist;
		}

		/*
		 * add the value
		 */
		list.setValue(val, list.length() - 1);

		dc.addValueList(this.name, list);

		return AbstractStep.NEXT;
	}

}
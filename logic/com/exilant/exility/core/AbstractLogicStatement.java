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

@Deprecated
abstract class AbstractLogicStatement {
	String description = null;

	// each statement is conditioned by a logical expression which is to be
	// evaluated tobe true for that statement to be
	// executed
	// if this is not specified, it means that the statment is alwasy applicable
	Expression condition = null;

	// name of the variable/entity that is the target of this statment.
	// For example, in an assignment statment, this is the name of the varaibel
	// to which the value is set to.
	// Statement is applicable only if the objective is not yet taken care of.
	String objective = null;

	abstract Value execute(DataCollection dc, Variables variants)
			throws ExilityException;

	boolean isApplicable(Variables variants) throws ExilityException {
		if (this.condition == null) {
			return true;
		}
		return this.condition.evaluate(variants).getBooleanValue();
	}
}
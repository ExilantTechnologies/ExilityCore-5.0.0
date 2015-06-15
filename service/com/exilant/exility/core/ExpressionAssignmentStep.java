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
 * long name for a simple a=b kind of work. However, the expression itself can
 * be built at run time and them evaluated.
 * 
 */
class ExpressionAssignmentStep extends AbstractStep {
	/**
	 * to which value is to be assigned
	 */
	String fieldName = null;

	/**
	 * to be evaluated before assigning, if the expression is known at design
	 * time. Use expressionFieldName if the expression itself is going to be
	 * built at run time
	 */
	Expression expression = null;

	/**
	 * field that may contain the expression itself.
	 */
	String expressionFieldName = null;

	@Override
	String executeStep(DataCollection dc, DbHandle handle)
			throws ExilityException {
		Expression expr = this.expression;
		if (this.expressionFieldName != null) {
			String s = dc.getTextValue(this.expressionFieldName, null);
			if (s == null) {
				dc.addError(this.expressionFieldName
						+ " is to contain an expression for avaluating at run time. Field not found in dc. ");
				return AbstractStep.NEXT;
			}
			expr = new Expression();
			expr.setExpression(s);
		}

		Value value = expr.evaluate(dc);
		dc.addValue(this.fieldName, value);
		return AbstractStep.NEXT;
	}
}
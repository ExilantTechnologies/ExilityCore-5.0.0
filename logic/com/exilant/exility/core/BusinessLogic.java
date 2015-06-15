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

import java.util.HashSet;
import java.util.Set;

@Deprecated
class BusinessLogic implements ToBeInitializedInterface {
	String name = null;
	String module = null;
	String description = null;
	String techNotes = null;

	Parameter[] outputParameters = new Parameter[0];

	// data elements that are required as inputs for this module. These are
	// copied from dc to the context of this module
	// if a data element is not found, a null String is used as value
	// NOTE: if taskParameter is specified, they are used to extract the field
	// values rather than the inputElements
	// This is like a function call. inputElements are the parameter names
	// inside this functions, but the values are
	// extracted as per the parameters name
	Parameter[] inputParameters = new Parameter[0];

	// a module cnsists os an ordered list of statements.
	AbstractLogicStatement[] statements = new AbstractLogicStatement[0];

	// for optimization..
	private Set<String> objectivesSet = null;
	private String[] inputFields = null;
	private String[] outputFields = null;

	BusinessLogic() {
	}

	public int execute(DataCollection dc, String gridName, String[] parameters)
			throws ExilityException {
		Variables variables = new Variables();

		// caller might have asked for variable name mapping. i.e. she is using
		// names in her dc that are different than
		// the ones
		// with which this module is coded.
		// taskParameters should have all the names for input as well as
		// objectives, in that order as comma separated
		// Also, prefix is another way of changing the way. If supplied, this
		// will be prefixed with all names
		String[] inputNames = new String[this.inputFields.length];
		String[] outputNames = new String[this.outputFields.length];
		if ((parameters == null) || (parameters.length == 0)) {
			for (int i = 0; i < this.inputFields.length; i++) {
				inputNames[i] = this.inputFields[i];
			}
			for (int i = 0; i < this.outputFields.length; i++) {
				outputNames[i] = this.outputFields[i];
			}
		} else {
			if (parameters.length != (this.outputFields.length + this.inputFields.length)) {
				dc.raiseException(
						"exilDesignError",
						"Business logic module"
								+ this.name
								+ " invoked with invalid number of parameters. It should contain a total of "
								+ (this.inputFields.length + this.outputFields.length)
								+ " comma separated elements");
			}
			for (int i = 0; i < this.inputFields.length; i++) {
				char c = parameters[i].toCharArray()[0];
				if (Chars.isTextQuote(c) || Chars.isNumeric(c)
						|| Chars.SUBTRACT == c || Chars.isDateQuote(c)) {
					inputNames[i] = parameters[i];
				} else {
					inputNames[i] = parameters[i];
				}
			}
			int totalInputs = inputNames.length;
			for (int i = 0; i < this.outputFields.length; i++) {
				outputNames[i] = parameters[i + totalInputs];
			}
		}

		// if grid is not specified, we are to execute this once for values in
		// dc.values
		if ((gridName == null) || (gridName.length() == 0)) {
			variables.extractVariablesFromDc(this.inputFields, inputNames, dc);
			this.executeOnce(dc, variables);
			this.copyResultToDc(variables, dc, outputNames);
			return 1;
		}

		// we are to execute this for each row in the grid
		if (!dc.hasGrid(gridName)) {
			return 0;
		}

		Grid grid = dc.getGrid(gridName);
		int n = grid.getNumberOfRows();
		if (n == 0) {
			return 0;
		}
		// if required, add output columns
		for (int i = 0; i < outputNames.length; i++) {
			String columnName = outputNames[i];
			if (grid.hasColumn(columnName) == false) {
				grid.addColumn(
						columnName,
						ValueList.newList(
								this.outputParameters[i].getValueType(), n));
			}
		}
		// execute for each row. Remember 0 is the heder row.
		for (int i = 0; i < n; i++) {
			variables = new Variables();
			variables.extractVariablesFromGrid(dc, this.inputFields,
					inputNames, grid, i);
			this.executeOnce(dc, variables);
			this.copyResultToGrid(variables, grid, outputNames, i);
		}
		return 1;
	}

	private void copyResultToGrid(Variables variables, Grid grid,
			String[] outputNames, int idx) {
		for (int i = 0; i < this.outputFields.length; i++) {
			Value value = variables.getValue(this.outputFields[i]);
			if (value != null) {
				grid.setValue(outputNames[i], value, idx);
			}
		}
	}

	private void copyResultToDc(Variables variables, DataCollection dc,
			String[] namesInDc) {
		for (int i = 0; i < this.outputFields.length; i++) {
			Value value = variables.getValue(this.outputFields[i]);
			if (value != null) {
				dc.addValue(namesInDc[i], value);
			}
		}
	}

	int executeOnce(DataCollection dc, Variables variants)
			throws ExilityException {
		int objectivesMetSoFar = 0;
		HashSet<String> objectivesMet = new HashSet<String>(); // Jan 29 2011 :
																// Bug 2009 -
																// Business
																// Logic in Grid
																// - Exility App
																// : Aravinda

		for (AbstractLogicStatement statement : this.statements) {
			// objective is the name of the variable we are trying to set in
			// this statement
			String objective = statement.objective;
			// Spit.out("Obj: " + objective);
			// if it is already taken care of, we need not try this.
			// Jan 29 2011 : Bug 2009 - Business Logic in Grid - Exility App
			// (Start) : Aravinda
			// Mar 16 2011 : Changed the logic appropriately as suggested by
			// Bhandi - Exility App (Start) : Aravinda
			if (this.objectivesSet.contains(objective)) {
				if (objectivesMet.contains(objective)) {
					continue;
				}
			} else if (variants.exists(objective)) {
				continue;
				// Mar 16 2011 : Changed the logic appropriately as suggested by
				// Bhandi - Exility App (End) : Aravinda
				// Jan 29 2011 : Bug 2009 - Business Logic in Grid - Exility App
				// (End) : Aravinda
			}

			// OK, we ar interesested in this variable, but is this statement
			// applicable? (based on its condition)
			if (!statement.isApplicable(variants)) {
				continue;
			}

			// ok. this is applicable.
			Value result = statement.execute(dc, variants);
			if (result != null) {
				variants.setValue(objective, result);
			}

			// do we have errors?
			if (dc.hasError()) {
				break;
			}
			// did any of our objectives met?
			if (!this.objectivesSet.contains(objective)) {
				continue;
			}

			// OK an objective is met.
			objectivesMetSoFar++;
			objectivesMet.add(objective); // Jan 29 2011 : Bug 2009 - Business
											// Logic in Grid - Exility App :
											// Aravinda

			// are we done??
			if (objectivesMetSoFar == this.outputFields.length) {
				return objectivesMetSoFar;
			}
		}

		return objectivesMetSoFar;
	}

	@Override
	public void initialize() {
		int n = this.outputParameters.length;
		this.outputFields = new String[n];
		this.objectivesSet = new HashSet<String>();
		for (int i = 0; i < this.outputParameters.length; i++) {
			String fieldName = this.outputParameters[i].name;
			this.objectivesSet.add(fieldName);
			this.outputFields[i] = fieldName;
		}

		n = this.inputParameters.length;
		this.inputFields = new String[n];
		for (int i = 0; i < this.inputParameters.length; i++) {
			String fieldName = this.inputParameters[i].name;
			this.inputFields[i] = fieldName;
		}

	}

	public static void main(String[] args) throws ExilityException {
		Parameter p1 = new Parameter();
		p1.name = "a1";
		p1.dataElementName = "sample_int";

		Parameter p2 = new Parameter();
		p2.name = "b1";
		p2.dataElementName = "sample_int";

		Parameter p3 = new Parameter();
		p3.name = "c1";
		p3.dataElementName = "sample_int";

		Parameter p4 = new Parameter();
		p4.name = "d1";
		p4.dataElementName = "sample_int";

		Parameter p5 = new Parameter();
		p5.name = "e1";
		p5.dataElementName = "sample_int";

		Parameter[] pmin = { p1, p2, p3 };
		Parameter[] pmout = { p4, p5 };

		ExpressionStatement st1 = new ExpressionStatement();
		st1.objective = "d1";
		Expression x1 = new Expression();
		x1.setExpression("a1+b1");
		st1.expression = x1;

		ExpressionStatement st2 = new ExpressionStatement();
		st2.objective = "e1";
		Expression x2 = new Expression();
		x2.setExpression("a1+b1+c1+d1");
		st2.expression = x2;
		AbstractLogicStatement[] sts = { st1, st2 };

		BusinessLogic bl = new BusinessLogic();
		bl.name = "test";
		bl.inputParameters = pmin;
		bl.outputParameters = pmout;
		bl.statements = sts;
		bl.initialize();

		DataCollection dc = new DataCollection();
		dc.addIntegralValue("a", 12);
		dc.addIntegralValue("b", 2);
		dc.addIntegralValue("c", 3);
		String[] passedParams = { "a", "b", "c", "d", "e" };
		bl.execute(dc, null, passedParams);
		Spit.out("value of d = " + dc.getIntegralValue("d", 0));
		Spit.out("value of e = " + dc.getIntegralValue("e", 0));

		long[] ai = { 12, 13, 14, 15 };
		long[] bi = { 2, 3, 45, 5 };
		long[] ci = { 3, 4, 5, 6 };

		int len = ai.length;
		Grid grid = new Grid("test");
		grid.addColumn("a", ValueList.newList(ai));
		grid.addColumn("b", ValueList.newList(bi));
		grid.addColumn("c", ValueList.newList(ci));
		grid.addColumn("d", ValueList.newList(DataValueType.INTEGRAL, len));
		// grid.addColumn("e", ValueList.newList(DataValueType.INTEGRAL, len));
		dc.addGrid("myGrid", grid);
		bl.execute(dc, "myGrid", passedParams);
		String[][] d = grid.getRawData();
		String str = "";
		for (String[] row : d) {
			for (String s : row) {
				str += "\t" + s;
			}
			str += '\n';
		}
		Spit.out(str);
	}

}
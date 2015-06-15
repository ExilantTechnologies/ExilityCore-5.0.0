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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * represents an arithmetic/logical expression
 * 
 */
public class Expression {
	String originalExpression = null;

	/***
	 * expression is parsed into an array of components. One component
	 * represents number of net open brackets before the operand, the operand,
	 * and the operator
	 */
	private Component[] components;

	/***
	 * expression evaluation is broken into number of steps, each step is one
	 * operation
	 */
	private ExecutionStep[] steps;

	private int stepIdx;

	/***
	 * number of variable instances found in the expression. if the same name is
	 * repeated twice, it would be counted twice
	 */
	private String[] variableNames;

	/***
	 * resultant of the last expression evaluation
	 */
	private Value calcuatedValue = null;

	/**
	 * default
	 */
	public Expression() {
	}

	/***
	 * parse the text and set it as expression to be executed, possibly
	 * repeatedly against different sets of variable values
	 * 
	 * @param expr
	 *            a valid expression that can contain constants and variables
	 * @throws ExilityException
	 */

	public void setExpression(String expr) throws ExilityException {
		/*
		 * For the purpose of parsing, following are considered - Valid tokens
		 * are operands, operators,brackets, unary minus and unary not. -
		 * Operands are of two types: variable and constant. - Variable is a
		 * word starting with a letter, an underscore, or a . followed by any
		 * number of same as well as digit Note that any number of .'s are
		 * allowed in our syntax. - Constants are of four types: numeric,
		 * boolean, date and text - Numeric constant
		 * "any number of digits with at most one decimal point anywhere" Note
		 * that unary minus is is parsed as a separate token. - text variable is
		 * any text contained within a matching single or double quote. Note
		 * that this syntax does not allow inclusion of both a single quote and
		 * a double quote as part of any String. We think this is OK. - word
		 * true and false with no quotes are considered to be boolean constants
		 * - a text constant whose value is a valid date is considered to be
		 * date constant. - (, {, [ are open brackets while ), }, ] are close
		 * brackets. They are 'shape insensitive'. that is, (a + b} is all right
		 * with us!! - +, -, *, /, % and ^, <, >, =, <=, >=, !, and != are the
		 * operators. - BOth '-' and '!' are unary negative. We are using them
		 * as equivalent that is -true and !12 are OK with us!! Following simple
		 * logic is used for parsing the expression. - If we were to ignore
		 * brackets and unary operators, expression has to start with an operand
		 * followed by any number of pairs of operator and operand, that is
		 * <operand> {<operator><operand>}... - We keep track of one boolean
		 * called operandExpected. If it is true, an operand is expected, else
		 * an operator is expected. - when we expect an operand, it is OK to get
		 * a unary operator, but once. And, that too before we get any open
		 * brackets. - when we expect an operand, any number of open brackets in
		 * succession are welcome, except after a unary negative/not - when we
		 * expect an operator, we are OK to get any number of close brackets in
		 * succession, except that we can not be expected to close more brackets
		 * than that are opened. Parsing an expression into components: - If an
		 * expression has N operands, it MUST have n-1 operators (unary minus is
		 * not counted as an operator) - An expression having N operands is
		 * parsed into N components. - Open brackets and close brackets are
		 * associated with the operand they precede/succeed - An operand can not
		 * have both open brackets and close brackets associated with it. e.g.
		 * (a + (b) ) is syntactically valid, but is parsed as (a + b). i.e.
		 * parser removes any redundant pairs of brackets - Each component
		 * consists of a. its operand b. number of brackets, +ve means open
		 * brackets, and -ve means closing brackets c. operator that precedes
		 * it. Note that for the first operand, this is not applicable d.
		 * whether a unary operator is present for this e. In addition to this
		 * basic information, an indicator whether the operand is a constant,
		 * and if so, its value as double is also stored
		 * 
		 * I hope, with this explanation, you will be able to follow my compact
		 * parsing algorithm.
		 */

		if ((expr == null) || (expr.length() == 0)) {
			this.originalExpression = "";
			return;
		}
		this.originalExpression = expr;
		int totalChars = expr.length();

		// arryList that stores the components to be converted into an array at
		// the end
		ArrayList<Component> componentList = new ArrayList<Component>();
		// at any time of parsing, number of close brackets should not exceed
		// open brackets
		// net brackets is incremented on encountering an open bracket and
		// decremented..
		int netBrackets = 0;

		// start parsing expecting an operand
		boolean operandExpected = true;

		// track unary operator
		boolean unaryOperatorParsed = false;
		// start with an empty component.
		Component component = new Component();

		for (int i = 0; i < totalChars; i++) {
			// our logic is built around looking at the character on hand, and
			// see what kind of token it
			// represents. operand, operator and bracket start/end are the
			// tokens.
			char c = expr.charAt(i);
			if (Chars.isWhiteSpace(c)) {
				continue;
			}

			if (operandExpected) {
				// when an operand is expected, you may get an open bracket, a
				// unary operator, a constant or a name..
				if (Chars.isOpenBracket(c)) {
					// it is OK except if we had already parsed a - or ! for
					// that operand
					if (unaryOperatorParsed) {
						throw new ExilityParseException(
								i,
								this.originalExpression,
								ExilityParseException.CHARACTER_NOT_EXPECTED_HERE);
					}

					netBrackets++;
					component.brackets++;
					continue;
				}
				if (Chars.isUnaryOperator(c)) {
					if (unaryOperatorParsed) {
						throw new ExilityParseException(i,
								this.originalExpression,
								ExilityParseException.OPERAND_EXPECTED);
					}

					if (Chars.NOT == c) {
						int iplus1 = i + 1;
						if (iplus1 < totalChars
								&& expr.charAt(iplus1) == Chars.EXISTS) {
							i++;
							component.unaryOperator = Operator.NOT_EXISTS;
						} else {
							component.unaryOperator = Operator.NOT;
						}
					} else if (Chars.SUBTRACT == c) {
						component.unaryOperator = Operator.SUBTRACT;
					} else {
						component.unaryOperator = Operator.EXISTS;
					}
					unaryOperatorParsed = true;
					continue;
				}
				// an operand. First see if it is a constant.. and then try
				// variable name
				if (Chars.isNumeric(c)) {
					i = this.parseNumber(expr, i, component);
				} else if (Chars.isAlpha(c)) {
					i = this.parseName(expr, i, component);
				} else if (Chars.isTextQuote(c)) {
					i = this.parseString(expr, i, component);
				} else if (Chars.DATE_QUOTE == c) {
					i = this.parseDate(expr, i, component);
				} else {
					throw new ExilityParseException(i, this.originalExpression,
							ExilityParseException.OPERAND_EXPECTED);
				}
				// a small optimization : if unary was specified for a constant,
				// let us change the constant itself..
				if (unaryOperatorParsed && component.isConstant()) {
					Value oldValue = component.getValue();
					Value newValue = component.unaryOperator.operate(oldValue);
					component.setValue(newValue);
					component.unaryOperator = null;
				}

				unaryOperatorParsed = false;
				operandExpected = false;
				continue;
			}

			// so, an operand is not expected. i.e we are expecting an operator.
			// Close bracket can come in between
			if (Chars.isCloseBracket(c)) {
				if (netBrackets <= 0) {
					throw new ExilityParseException(i, this.originalExpression,
							ExilityParseException.NO_OPEN_BRACKET);
				}

				component.brackets--;
				netBrackets--;
				continue;
			}

			Operator operator = Operator.parse(expr, i);
			if (operator == null) {
				throw new ExilityParseException(i, this.originalExpression,
						ExilityParseException.OPERATOR_EXPECTED);
			}

			// by the way, operator might have parsed an additional character
			int operLength = operator.toString().length();
			if (operLength > 1) {
				i += operLength - 1;
			}

			// parsing an operator marks the beginning of a new operand. We
			// should close the previous one, and open a new one
			componentList.add(component);
			component = new Component();
			component.operator = operator;

			// and expect an operand now
			operandExpected = true;
			unaryOperatorParsed = false;
			continue;
		}
		// we are done with parsing. Any problems??
		if (operandExpected) {
			throw new ExilityParseException(totalChars,
					this.originalExpression,
					ExilityParseException.OPERAND_EXPECTED);
		}

		if (netBrackets != 0) {
			throw new ExilityParseException(totalChars,
					this.originalExpression,
					ExilityParseException.UNCLOSED_BRACKET);
		}

		// the last operation has to be closed without an operator
		componentList.add(component);

		// were there any components at all?
		int nbrCompnents = componentList.size();
		if (nbrCompnents == 0) {
			this.calcuatedValue = new NullValue(DataValueType.TEXT);
			return;
		}
		this.saveComponents(componentList);
		this.compileSteps();
		// are there any variables or is it just a constant expression?
		if (this.variableNames == null || this.variableNames.length == 0) {
			this.calcuatedValue = this.evaluate(new DataCollection());
		}
	}

	private void saveComponents(ArrayList<Component> componentList) {
		int n = componentList.size();
		this.components = new Component[n];

		// keep track of variable names required to evaluate this expression.
		int nbrNames = 0;
		String[] names = new String[n];
		for (int i = 0; i < this.components.length; i++) {
			Component c = componentList.get(i);
			this.components[i] = c;
			if (!c.isConstant()) {
				names[nbrNames++] = c.getName();
			}
		}

		// were there variables??
		if (nbrNames > 0) {
			this.variableNames = new String[nbrNames];
			for (int j = 0; j < this.variableNames.length; j++) {
				this.variableNames[j] = names[j];
			}
		}
	}

	private int parseString(String expr, int quoteAt,
			Component componentToParsedTo) throws ExilityParseException {
		// note: two consecutive quotes means one quote
		int istart = quoteAt + 1;

		// list of possible quotes. Better to have in Chars class?
		String val = "";
		int lastCharAt = expr.length() - 1;
		int ret = -1;
		while (istart <= lastCharAt) {
			ret = Chars.getCharIndex(Chars.QUOTES, istart, expr);
			if (ret < 0) {
				throw new ExilityParseException(quoteAt,
						this.originalExpression,
						ExilityParseException.NO_MATCHING_QUOTE);
			}

			if (ret == lastCharAt || !Chars.isTextQuote(expr.charAt(ret + 1))) {
				break;
			}

			// we found a ""
			val += expr.substring(istart, ret + 1);
			istart = ret + 2;
		}
		val += expr.substring(istart, ret);
		componentToParsedTo.setValue(Value.newValue(val));
		return ret;
	}

	private int parseDate(String expr, int quoteAt,
			Component componentToParsedTo) throws ExilityParseException {
		int startAt = quoteAt + 1;
		int ret = expr.indexOf(Chars.DATE_QUOTE, startAt);
		if (ret < 0) {
			throw new ExilityParseException(quoteAt, this.originalExpression,
					ExilityParseException.NO_MATCHING_QUOTE);
		}
		String val = expr.substring(startAt, ret);

		Date d = DateUtility.parseDate(val);
		if (d == null) {
			throw new ExilityParseException(quoteAt, this.originalExpression,
					ExilityParseException.INVALID_DATE);
		}

		componentToParsedTo.setValue(Value.newValue(d));
		return ret;

	}

	private int parseNumber(String expr, int startAt,
			Component componentToParsedTo) {
		boolean dotYetToBeParsed = (expr.charAt(startAt) == Chars.DOT) ? false
				: true;
		int ret = startAt;
		while (++ret < expr.length()) {
			char c = expr.charAt(ret);
			if (Chars.isDigit(c)) {
				continue;
			}
			if (dotYetToBeParsed && Chars.DOT == c) {
				dotYetToBeParsed = false;
				continue;
			}
			break;
		}
		String val = expr.substring(startAt, ret);
		if (dotYetToBeParsed) {
			long l = Long.parseLong(val);
			componentToParsedTo.setValue(Value.newValue(l));
		} else {
			double d = Double.parseDouble(val);
			componentToParsedTo.setValue(Value.newValue(d));
		}
		return ret - 1;
	}

	private int parseName(String expr, int startAt,
			Component componentToParsedTo) {
		int ret = startAt;
		while (++ret < expr.length()) {
			char c = expr.charAt(ret);
			if (!Chars.isAlphaNumeric(c)) {
				break;
			}
		}
		String val = expr.substring(startAt, ret);
		if (val.equals(ExilityConstants.TRUE_STRING)) {
			componentToParsedTo.setValue(Value.newValue(true));
		} else if (val.equals(ExilityConstants.FALSE_STRING)) {
			componentToParsedTo.setValue(Value.newValue(false));
		} else {
			componentToParsedTo.setName(val);
			val = null;
			componentToParsedTo.setValue(Value.newValue(val));
		}
		return ret - 1;
	}

	private void compileSteps() {
		// loop through the operations to find the inner most bracket from left
		// and add them to steps
		int n = this.components.length;
		int[] brackets = new int[n];
		Operator[] operatorToAdd = new Operator[n];

		for (int i = 0; i < this.components.length; i++) {
			Component comp = this.components[i];
			operatorToAdd[i] = comp.operator;
			brackets[i] = comp.brackets;
		}

		// put a pair of brackets covering the whole expression to ensure that
		// we do get a pair of brackets
		brackets[0]++;
		brackets[n - 1]--;
		int openBracketAt = 0;

		// stepIdx points to the next step to be added.
		this.stepIdx = 0;
		// there will be n-1 steps for n parsed operands
		int totalSteps = n - 1;
		this.steps = new ExecutionStep[totalSteps];

		while (this.stepIdx < totalSteps) {
			for (int i = 0; i < n; i++) {
				if (brackets[i] > 0) {
					openBracketAt = i;
				} else if (brackets[i] < 0) {
					// we got a close bracket. put steps within that bracket
					this.addSteps(operatorToAdd, openBracketAt, i);

					// we are done with the brackets. Remove this pair of
					// brackets
					brackets[openBracketAt]--;
					brackets[i]++;
					break;
				}
			}
		}
	}

	private void addSteps(Operator[] operatorToAdd, int firstOne, int lastOne) {
		// first find out how many pending operators are there in this range
		int stepsToAdd = 0;
		for (int i = firstOne; i <= lastOne; i++) {
			if (operatorToAdd[i] != null) {
				stepsToAdd++;
			}
		}

		// Do we have some work?
		if (stepsToAdd == 0) {
			return;
		}

		int stepsAdded = 0;
		for (int precedence = 1; ((stepsAdded < stepsToAdd) && (precedence <= Operator.LAST_PRECEDENCE_LEVEL)); precedence++) {
			int operand1Idx = firstOne;
			for (int i = firstOne + 1; i <= lastOne; i++) {
				Operator op = operatorToAdd[i];
				// is this operand already taken care of??
				if (op == null) {
					continue;
				}

				if (op.getPrecedence() == precedence) {
					// create and add a step;
					ExecutionStep step = new ExecutionStep();
					step.operator = op;
					step.operand1Index = operand1Idx;
					step.operand2Index = i;
					// push it
					this.steps[this.stepIdx] = step;
					this.stepIdx++;

					// increment for the while loop
					stepsAdded++;
					// this operand is taken care of. and hence reset the
					// operator
					operatorToAdd[i] = null;
					continue;
				}
				// This operand is the operand1 for the next step
				operand1Idx = i;
			}
		}
	}

	String[] getVarableNames() {
		return this.variableNames;
	}

	/***
	 * render expression with simple formatting principles (minimum space
	 * between operands and operators)
	 * 
	 * @return formatted string for this expression
	 */
	public String renderExpression() {
		StringBuilder sbf = new StringBuilder();
		for (Component c : this.components) {
			sbf.append(c);
		}
		return sbf.toString();
	}

	/***
	 * render the calculation steps that this expression is parsed into
	 * 
	 * @return formatted string for a step
	 */
	public String renderSteps() {
		StringBuilder sbf = new StringBuilder();
		for (ExecutionStep step : this.steps) {
			sbf.append("\n Value[").append(step.operand1Index).append("] ")
					.append(step.operator.toString()).append(" value[")
					.append(step.operand2Index).append("]");
		}

		return sbf.toString();
	}

	/**
	 * 
	 * @deprecated
	 * @param dc
	 * @param variables
	 * @return
	 * @throws ExilityException
	 */
	@Deprecated
	private Value[] getValues(DataCollection dc, Variables variables)
			throws ExilityException {
		Value[] vals = new Value[this.components.length];
		for (int i = 0; i < vals.length; i++) {
			Component c = this.components[i];
			Operator unary = c.unaryOperator;
			Value value = null;
			String name = c.getName();
			if (c.isConstant()) {
				value = c.getValue();
			} else {
				if (variables != null) {
					value = variables.getValue(name);
				} else {
					value = dc.getValue(name);
				}
			}

			// is it exists operator?
			if (Operator.EXISTS.equals(unary)) {
				if (value == null || value.isNull()) {
					value = BooleanValue.FALSE_VALUE;
				} else {
					value = BooleanValue.TRUE_VALUE;
				}
			} else if (Operator.NOT_EXISTS.equals(unary)) {
				if (value == null || value.isNull()) {
					value = BooleanValue.TRUE_VALUE;
				} else {
					value = BooleanValue.FALSE_VALUE;
				}
			} else if (value == null) {
				value = new NullValue(DataValueType.NULL);
				Spit.out(name
						+ " is not found and hence is set to null during expression evaluation");
			}

			else if (Operator.NOT.equals(unary))// (- and ! are the only two,
												// but ! is considered -)
			{
				value = Operator.NOT.operate(value);
			} else if (Operator.SUBTRACT.equals(unary))// - is the only other
														// possibility
			{
				value = Operator.SUBTRACT.operate(value);
			}
			vals[i] = value;
		}
		return vals;
	}

	/***
	 * evaluate this expression with values for its variables picked up from
	 * dc.values
	 * 
	 * @param dc
	 * @return resultant variable
	 * @throws ExilityException
	 *             any issue with missinig variables or type compatibility issue
	 */
	public Value evaluate(DataCollection dc) throws ExilityException {
		if (this.calcuatedValue != null) {
			return this.calcuatedValue;
		}
		Value[] values = this.getValues(dc, null);
		return this.evaluate(values);
	}

	/***
	 * evaluate this expression with values from variables
	 * 
	 * @param variables
	 * @return resultant
	 * @throws ExilityException
	 *             in case of missing variables or incompatible types
	 * @deprecated
	 */
	@Deprecated
	public Value evaluate(Variables variables) throws ExilityException {
		if (this.calcuatedValue != null) {
			return this.calcuatedValue;
		}
		Value[] values = this.getValues(null, variables);
		return this.evaluate(values);
	}

	private Value evaluate(Value[] values) throws ExilityException {
		for (ExecutionStep step : this.steps) {
			int leftIndex = step.operand1Index;
			int rightIndex = step.operand2Index;

			values[leftIndex] = step.operator.operate(values[leftIndex],
					values[rightIndex]);
		}

		return values[0];
	}

	@Override
	public String toString() {
		return this.originalExpression;
	}

	/***
	 * evaluate column as an expression for each row of the grid and update it
	 * back with the result. Variable in the expression could be either fields
	 * in dc.variables or name of other columns
	 * 
	 * @param grid
	 *            in which column is to be evaluated
	 * @param dc
	 * @param columnName
	 */
	public void evaluateColumn(Grid grid, DataCollection dc, String columnName) {
		ValueList result = grid.getColumn(columnName);
		if (result == null) {
			String str = "No column with name " + columnName
					+ " in grid for colculation.";
			Spit.out(str);
			dc.addError(str);
			return;
		}

		int nbrRows = grid.getNumberOfRows();
		int nbrVariables = 0;
		if (this.variableNames != null) {
			nbrVariables = this.variableNames.length;
		}

		if (nbrVariables == 0) {
			for (int i = 0; i < nbrRows; i++) {
				result.setValue(this.calcuatedValue, i);
				continue;
			}
			return;
		}

		Variables variables = new Variables();
		ValueList[] columnsNeeded = new ValueList[nbrVariables];
		for (int i = 0; i < this.variableNames.length; i++) {
			String name = this.variableNames[i];
			if (grid.hasColumn(name)) {
				columnsNeeded[i] = grid.getColumn(name);
			} else if (dc.hasValue(name)) {
				variables.setValue(name, dc.getValue(name));
			} else {
				String str = "No value is supplied for variable " + name
						+ " while colculating column " + columnName;
				Spit.out(str);
				dc.addError(str);
				return;
			}
		}

		for (int i = 0; i < nbrRows; i++) {
			for (int j = 0; j < this.variableNames.length; j++) {
				String name = this.variableNames[j];
				ValueList col = columnsNeeded[j];
				if (col != null) {
					variables.setValue(name, col.getValue(i));
				}
			}
			try {
				result.setValue(this.evaluate(variables), i);
			} catch (ExilityException e) {
				Spit.out(e);
				dc.addError(e.getMessage());
				return;
			}
		}
	}

	/***
	 * get possible data type of the resultant if the variables are bound to
	 * supplied data types
	 * 
	 * @param types
	 *            array of data types that match the variables in the expression
	 *            in the same order that they appear in the expression
	 * @return data type
	 * @throws ExilityException
	 */
	public DataValueType getValueType(DataValueType[] types)
			throws ExilityException {
		DataValueType[] valueTypes = this.getValueTypes(types);
		for (ExecutionStep step : this.steps) {
			int leftIndex = step.operand1Index;
			int rightIndex = step.operand2Index;

			valueTypes[leftIndex] = step.operator.getValueType(
					valueTypes[leftIndex], valueTypes[rightIndex]);
		}

		return valueTypes[0];
	}

	private DataValueType[] getValueTypes(DataValueType[] knownValueTypes)
			throws ExilityException {
		DataValueType[] types = null;
		if (knownValueTypes != null && knownValueTypes.length > 0) {
			if (knownValueTypes.length != this.variableNames.length) {
				String str = "Expression has "
						+ this.variableNames.length
						+ " variables but only "
						+ knownValueTypes.length
						+ " value types are passed for determining its vaue type ";
				Spit.out(str);
				throw new ExilityException(str);
			}
			types = knownValueTypes;
		}
		DataValueType[] valueTypes = new DataValueType[this.components.length];
		int j = 0;
		for (int i = 0; i < valueTypes.length; i++) {
			Component c = this.components[i];
			if (c.isConstant()) {
				valueTypes[i] = c.getValue().getValueType();
				continue;
			}
			if (types != null) {
				valueTypes[i] = types[j];
				j++;
				continue;
			}
			valueTypes[i] = DataDictionary.getValueType(c.getName());
		}
		return valueTypes;
	}

	/***
	 * Variant is a variable that has no fixed type. But any type, it has a
	 * value of specific type
	 * 
	 */
	class Variant {
		/***
		 * has to have a name of course
		 */
		String name;
		/***
		 * current value. Null if no value is assigned yet
		 */
		Value value;

		boolean isConstant() {
			return this.name == null;
		}

		void setName(String name) {
			this.name = name;
		}

		String getName() {
			return this.name;
		}

		void setValue(Value value) {
			this.value = value;
		}

		Value getValue() {
			return this.value;
		}

		@Override
		public String toString() {
			if (this.name == null) {
				return this.value.toString();
			}
			return this.name;
		}
	}

	/***
	 * Part of an expression. expression containsIt is one operation with two
	 * operands.
	 * 
	 */
	class Component {
		Variant variant = new Variant();
		Operator unaryOperator = null;
		Operator operator = null;
		int brackets;

		Component() {

		}

		boolean isConstant() {
			return this.variant.isConstant();
		}

		void setName(String name) {
			this.variant.setName(name);
		}

		void setValue(Value value) {
			this.variant.setValue(value);
		}

		Value getValue() {
			return this.variant.getValue();
		}

		String getName() {
			return this.variant.getName();
		}

		@Override
		public String toString() {
			// output this in a form that is part of the expression
			// space operator space open-brackets UnaryMinus operand
			// close-brackets
			StringBuilder sbf = new StringBuilder();
			if (this.operator != null) {
				sbf.append(' ').append(this.operator).append(' ');
			}

			if (this.brackets > 0) {
				for (int i = 0; i < this.brackets; i++) {
					sbf.append('(');
				}
			}

			if (this.unaryOperator != null) {
				sbf.append(this.unaryOperator);
			}

			sbf.append(this.variant);

			if (this.brackets < 0) {
				for (int i = 0; i > this.brackets; i--) {
					sbf.append(')');
				}
			}
			return sbf.toString();
		}
	}

	/***
	 * A Step represents one step of the calculation. it means
	 * value[operand1Index] = value[operand1Index] Operator value[operand2Index]
	 * e.g. value[7] = value[7] + value[9] A given expression is parsed and
	 * compiled into steps.
	 ***/

	class ExecutionStep {
		int operand1Index;
		int operand2Index;
		Operator operator;

		ExecutionStep() {
		}

		@Override
		public String toString() {
			return "value[" + this.operand1Index + "] " + this.operator
					+ " value[" + this.operand2Index + "]";
		}
	}

	/***
	 * interactive testing tool
	 * 
	 * @param args
	 *            none
	 */
	public static void main(String[] args) {
		Variables variables = new Variables();
		variables.setValue("s", Value.newValue("123\"abcd123"));
		variables.setValue("i", Value.newValue(1234));
		variables.setValue("f", Value.newValue(123.45));
		variables.setValue("b", Value.newValue(true));
		variables.setValue("d",
				Value.newValue("2007-05-23", DataValueType.DATE));
		Pattern p = Pattern.compile("([a-zA-Z]\\w*)\\s*=\\s*(.*)");

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		boolean valid = true;
		while (true) {
			if (valid) {
				Spit.out(variables.render());
			} else {
				valid = true;
			}

			Spit.out("\n type a statement in the form variable = expression\n or just enter to quit");
			try {
				String input = in.readLine();
				if (input.length() == 0) {
					break;
				}
				Matcher m = p.matcher(input);
				if (!m.matches()) {
					Spit.out("Sorry, you typed an invalid statmement.");
					valid = false;
					continue;
				}
				Spit.out("left = " + m.group(1) + " right = " + m.group(2));
				String fieldName = m.group(1);
				Expression expr = new Expression();
				expr.setExpression(m.group(2));
				Spit.out("\n parsed statement : " + fieldName + " = "
						+ expr.renderExpression());
				Spit.out("\nsteps are " + expr.renderSteps());
				variables.setValue(fieldName, expr.evaluate(variables));
			} catch (Exception e) {
				Spit.out("Exception raised :");
				Spit.out(e.getMessage());
			}

		}
		Spit.out("Thank you, Bye");
	}

}
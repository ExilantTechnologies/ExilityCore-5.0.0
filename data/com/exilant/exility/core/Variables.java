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

import java.util.HashMap;

/***
 * A collection of name value pairs. Used as a mechanism to scope variables
 * during execution of an algorithm. Was used by logic module, Not used anymore
 * 
 */
public class Variables {
	private final HashMap<String, Value> variables = new HashMap<String, Value>();

	/**
	 * 
	 */
	public Variables() {

	}

	/**
	 * convert string values into typed fields and put them in dc
	 * 
	 * @param variableNames
	 *            names of fields
	 * @param inputValues
	 *            value of the corresponding field as string. To be parsed based
	 *            on its type
	 * @param dc
	 *            to which fields are to be added to
	 */
	public void extractVariablesFromDc(String[] variableNames,
			String[] inputValues, DataCollection dc) {
		for (int i = 0; i < inputValues.length; i++) {
			String inputValue = inputValues[i];
			char firstChar = inputValue.charAt(0);
			Value value = null;
			if (Chars.isTextQuote(firstChar)) {
				value = Value.newValue(inputValue.substring(1,
						inputValue.length() - 1));
			} else if (Chars.isDateQuote(firstChar)) {
				value = Value.newValue(DateUtility.parseDate(inputValue
						.substring(1, inputValue.length() - 1)));
			} else if (Chars.isNumeric(firstChar)
					|| Chars.SUBTRACT == firstChar) {
				if (inputValue.indexOf('.') > 0) {
					value = Value.newValue(Long.parseLong(inputValue));
				} else {
					value = Value.newValue(Double.parseDouble(inputValue));
				}
			} else if (inputValue.equalsIgnoreCase("true")) {
				value = Value.newValue(true);
			} else if (inputValue.equalsIgnoreCase("false")) {
				value = Value.newValue(false);
			} else {
				value = dc.getValue(inputValue);
			}

			this.variables.put(variableNames[i], value);
		}
	}

	/**
	 * get field values either from the grid, or from values array
	 * 
	 * @param dc
	 *            to which fields are to be added
	 * @param variableNames
	 *            field names to be added to dc
	 * @param inputValues
	 *            string value for the corresponding field. This value is used
	 *            only if the grid does not have a column for this field
	 * @param grid
	 *            from which values to be extracted
	 * @param idx
	 *            row number in the grid to be used for extraction of value
	 */
	public void extractVariablesFromGrid(DataCollection dc,
			String[] variableNames, String[] inputValues, Grid grid, int idx) {
		for (int i = 0; i < inputValues.length; i++) {
			String columnName = inputValues[i];
			Value value = null;
			if (grid.hasColumn(columnName)) {
				value = grid.getValue(columnName, idx);
			} else {
				value = dc.getValue(columnName);
			}
			this.variables.put(variableNames[i], value);
		}
	}

	/**
	 * set value of a field
	 * 
	 * @param name
	 * @param value
	 */
	public void setValue(String name, Value value) {
		this.variables.put(name, value);
	}

	/**
	 * get value of a field
	 * 
	 * @param name
	 * @return value if found, null otherwise
	 */
	public Value getValue(String name) {
		return this.variables.get(name);
	}

	/**
	 * remove field from the collection. No error is reported if the field was
	 * not present in the collection
	 * 
	 * @param name
	 */
	public void removeValue(String name) {
		this.variables.remove(name);
	}

	/**
	 * check if the collection contains this field
	 * 
	 * @param name
	 * @return true if this field is found, false otherwise
	 */
	public boolean hasVariable(String name) {
		return this.variables.containsKey(name);
	}

	/**
	 * do we have a variable by this name?
	 * 
	 * @param varName
	 * @return true if it exists, false otherwise
	 */
	boolean exists(String varName) {
		return this.variables.containsKey(varName);
	}

	/**
	 * create a json string for this
	 * 
	 * @return
	 */
	String render() {
		StringBuilder sbf = new StringBuilder();
		for (String variable : this.variables.keySet()) {
			Value value = this.variables.get(variable);
			sbf.append("\n ").append(variable).append('[')
					.append(value.getValueType()).append("] = ")
					.append(value.getQuotedValue());
		}
		return sbf.toString();

	}
}
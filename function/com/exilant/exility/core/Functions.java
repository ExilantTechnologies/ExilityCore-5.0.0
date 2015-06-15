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

/**
 * caches definitions of functions used in the project
 * 
 * @author raghu.bhandi
 * 
 */
class Functions {
	HashMap<String, FunctionInterface> builtInFuntions = new HashMap<String, FunctionInterface>();
	String defaultNameSpace = "com.exilant.exility.core";
	HashMap<String, FunctionEntry> functionEntries = new HashMap<String, FunctionEntry>();
	private static Functions instance = null;

	static {
		Functions.load();
		if (Functions.instance == null) {
			Functions.instance = new Functions();
		}
		/*
		 * exility provided functions
		 */
		Functions.instance.builtInFuntions.put("Concat", new Concat());
		Functions.instance.builtInFuntions.put("In", new In());
		Functions.instance.builtInFuntions.put("AnyIn", new AnyIn());
		Functions.instance.builtInFuntions.put("Encrypt", new Encrypt());
	}

	/**
	 * get a function definition
	 * 
	 * @param functionName
	 * @return
	 */
	static FunctionInterface getFunction(String functionName) {
		FunctionInterface fn = Functions.instance.builtInFuntions
				.get(functionName);
		if (fn != null) {
			return fn;
		}

		FunctionEntry entry = Functions.instance.functionEntries
				.get(functionName);
		if (entry == null) {
			return null;
		}
		if (entry.function == null) {
			String packageName = (entry.nameSpace != null) ? entry.nameSpace
					: Functions.instance.defaultNameSpace;
			entry.function = (FunctionInterface) ObjectManager.createNew(
					packageName, functionName);
		}
		return entry.function;

	}

	/**
	 * load functions
	 */
	static void load() {
		Functions.instance = (Functions) ResourceManager.loadResource(
				"function", Functions.class);
	}

	/**
	 * evaluate a function and return result
	 * 
	 * @param functionName
	 * @param inputParameters
	 * @param dc
	 * @return value of the function
	 */
	public static Value evaluateFunction(String functionName,
			String[] inputParameters, DataCollection dc) {
		Value[] ps = null;
		if (inputParameters != null) {
			ps = new Value[inputParameters.length];
			for (int i = 0; i < ps.length; i++) {
				ps[i] = dc.getValue(inputParameters[i]);
			}
		}

		FunctionInterface function = Functions.getFunction(functionName);
		return function.evaluate(ps);
	}

	/**
	 * evaluate this function
	 * 
	 * @param functionName
	 *            name of function to be evaluated
	 * @param inputParameters
	 * @param vars
	 *            variables collection against which this function is to be
	 *            evaluated. Either this is to be non-null, or dc needs to be
	 *            non-null
	 * @param dc
	 * @return returned value from the function evaluation
	 * @deprecated
	 */
	@Deprecated
	public static Value evaluateFunction(String functionName,
			String[] inputParameters, Variables vars, DataCollection dc) {
		Value[] ps = null;
		if (inputParameters != null) {
			ps = new Value[inputParameters.length];
			for (int i = 0; i < ps.length; i++) {
				ps[i] = vars.getValue(inputParameters[i]);
			}
		}

		FunctionInterface function = Functions.getFunction(functionName);
		return function.evaluate(ps);
	}

	static Functions getInstance() {
		return Functions.instance;
	}
}

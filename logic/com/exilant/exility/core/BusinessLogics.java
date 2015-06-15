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

@Deprecated
class BusinessLogics {
	private static final HashMap<String, BusinessLogic> logics = new HashMap<String, BusinessLogic>();

	static BusinessLogic getLogic(String logicName, DataCollection dc)
			throws ExilityException {
		Spit.out("BusinessLogics:getLogic:Started:name=" + logicName);
		if ((logicName == null) || (logicName.length() == 0)) {
			return null;
		}

		if (logics.containsKey(logicName)) {
			return logics.get(logicName);
		}

		BusinessLogic logic = (BusinessLogic) ResourceManager.loadResource(
				"logic." + logicName, BusinessLogic.class);
		if ((logic.name == null) || (logic.name.length() == 0)) {
			Spit.out("Definition for  " + logicName + " not defined ");
			dc.raiseException("exilNoSuchLogic", logicName);
		}
		if (AP.definitionsToBeCached) {
			logics.put(logicName, logic);
		}

		return logic;
	}

	/**
	 * 
	 * @param logic
	 * @param dc
	 */
	static void save(BusinessLogic logic, DataCollection dc)// throws
															// ExilityException
	{
		ResourceManager.saveResource("logic." + logic.name, logic);
	}
}
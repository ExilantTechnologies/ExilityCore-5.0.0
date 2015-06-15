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

/**
 * Specialized task that helps you in sending list of valid actions that the
 * logged-in user can take on the current document being viewed.
 * 
 */
public class ActionTask extends ExilityTask {
	ActionStep[] steps = new ActionStep[0];

	@Override
	public int executeBulkTask(DataCollection dc, DbHandle handle)
			throws ExilityException {
		throw new ExilityException("Bulk Action task is not yet implemented");
	}

	@Override
	public int executeTask(DataCollection dc, DbHandle handle)
			throws ExilityException {
		Set<String> actions = new HashSet<String>();
		for (ActionStep step : this.steps) {
			if (step.condition != null) {
				BooleanValue val = (BooleanValue) step.condition.evaluate(dc);
				if (val.getBooleanValue() == false) {
					continue;
				}
			}
			if (step.task != null && step.task.execute(dc, handle) <= 0) {
				continue;
			}
			for (String action : step.actionsToEnable) {
				actions.add(action);
			}
		}
		String[] listOfNames = actions.toArray(new String[0]);
		ValueList list = ValueList.newList(listOfNames);
		Grid grid = new Grid(this.gridName);
		grid.addColumn("actions", list);
		dc.addGrid(this.gridName, grid);
		return listOfNames.length;
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.READONLY;
	}

}

class ActionStep {
	String[] actionsToEnable = null;
	Expression condition = null;
	ExilityTask task = null;
}

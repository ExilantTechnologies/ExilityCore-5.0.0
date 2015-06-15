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
package com.exilant.exility.wf;

import com.exilant.exility.core.DataCollection;
import com.exilant.exility.core.DbHandle;

/***
 * Base class for you to write your custom logic for a workflow This class can
 * be used to either augment regular Eixlity Workflow with some special
 * functions Or it can implement entire logic. Even if you implement all logic
 * in this class, this is called thru a standard Exility workflow component
 * 
 */
public abstract class CustomLogic {
	/**
	 * dc being passed around
	 */
	protected DataCollection dc;

	/**
	 * db handle associated with this service
	 */
	protected DbHandle handle;

	/**
	 * I would have preferred to use these two parameters as part of
	 * constructor, but the class is typically instantiated at run time using
	 * reflection, and hence a default constructor followed by a initialize()
	 * 
	 * @param passedDc
	 * @param dbHandle
	 */
	public void initialize(DataCollection passedDc, DbHandle dbHandle) {
		this.dc = passedDc;
		this.handle = dbHandle;
	}

	/***
	 * Implements logic for the function that is specified in workflow
	 * definition. This method is called after
	 * 
	 * @param functionName
	 */
	public abstract void executeFunction(String functionName);

	/**
	 * puts a grid named WorkflowNames.VALID_ACTIONS_GRID_NAME in dc that has
	 * the valid actions that the logged-in user can perform Also put next steps
	 * into grid
	 */
	public abstract void getValidActions();

	/**
	 * take the specified action and move the workflow thru this step
	 * 
	 * @return true if the workflow actually moved. false if the action is not
	 *         valid.
	 */
	public abstract boolean moveIt();

}

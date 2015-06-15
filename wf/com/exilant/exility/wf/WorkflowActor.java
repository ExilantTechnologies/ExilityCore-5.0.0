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

/***
 * represents a role defined for a workflow
 * 
 * @author Exilant Technologies
 * 
 */
public class WorkflowActor {
	/***
	 * unique within a workflow definition. initiator is a reserved role for the
	 * one who initiates the workflow When a workflow is initiated, initiator
	 * role is fixed as the user-role that initiated it.
	 */
	String name;

	/***
	 * To be used in case this has to be seen on a client
	 */
	String label;

	/***
	 * for documentation
	 */
	String description;

	/***
	 * Is this role determined based on another role? e.g. approver is based on
	 * initiator.
	 */
	String basedOnActor;

	/***
	 * org role name. reportingTo is a reserved name for immediate reporting
	 * manager.
	 */
	String roleOfActor;

	/***
	 * How is this role determined based on the above role? reportingTo -> role
	 * for the actor. e.g. deptHead of actor Otherwise : we have to design this
	 * aspect
	 */
	String relationship;
}

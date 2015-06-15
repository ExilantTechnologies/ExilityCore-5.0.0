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

import java.util.Map;
import java.util.Set;

import com.exilant.exility.core.DataCollection;
import com.exilant.exility.core.DbHandle;
import com.exilant.exility.core.ExilityException;
import com.exilant.exility.core.Expression;
import com.exilant.exility.core.ServiceInterface;
import com.exilant.exility.core.Services;
import com.exilant.exility.core.TableInterface;
import com.exilant.exility.core.Tables;

/**
 * Represents an action that can be taken on a document
 * 
 */
public class WorkflowStep {
	/***
	 * follow camelCase naming convention. Unique name across all steps within a
	 * workflow
	 */
	String name;

	/***
	 * Requirements/spec for this step
	 */
	String description;

	/***
	 * state for which state this step is valid. In a workflow diagram, this is
	 * the starting node of the step. Leave it null if this step is valid for
	 * more than one step. In such a case, use other features to define validity
	 */
	String currentState;

	/**
	 * who can take this step? one of the actors defined for this workflow.
	 */
	String actor;

	/***
	 * action to be taken
	 */
	String action;

	/***
	 * label as seen by client applications. It could be in a drop-down, or
	 * label of buttons
	 */
	String actionLabel;

	/***
	 * help text for clients application
	 */
	String helpText;

	/***
	 * If the validity of this step depends not only on the current state, but
	 * on some business logic, and if the business logic can be expressed as a
	 * boolean expression using fields from the document At run time, this
	 * expression is evaluated with data in dc, and the step is considered to be
	 * not valid if the expression evaluates to false.
	 */
	Expression validityExpression;

	/***
	 * if validityExpresison was not good enough to implement your logic, and
	 * you would like to write the logic in your code, then you add a method to
	 * the customClass with this name, and the method should return true/false
	 */
	String validityFunction;

	/***
	 * Use this feature if your business logic is amenable to be implemented in
	 * a service. Note that this service SHOULD NOT do any update. It only
	 * decides whether this step is valid or not. It essentially set a field, as
	 * defined in WorkflowNames, to true/false
	 */
	String validityServiceName;

	/***
	 * new state if this step is traversed. Leave it null if it has to be
	 * determined at run time based on some logic
	 */
	String newState;

	/***
	 * if newState us left null, and if the logic can be put into a string/text
	 * expression, here we go
	 */
	Expression newStateExpression;

	/***
	 * if newState and newStateExpression are not specified, and if the logic
	 * can be put into a method of the custom class..
	 */
	String newStateFunction;

	/***
	 * if the logic for this action is more complex than using newState,
	 * newStateExpression, newStateFunction and tableToBeSaved, then give me
	 * your service. This service is executed BEFORE the above four. If the
	 * service has already taken care of them then the above fields should not
	 * be specified
	 */

	String actionServiceName;
	/***
	 * Does this step allow updating the document being pushed? Having a
	 * tableDefinition allows you to selectively save fields
	 */

	String[] tablesToBeSaved;

	/***
	 * notificationName1:role11,role12;notification2:role21,role22..... If this
	 * is based on business logic, then put that in your service
	 */
	String notificationsToBeSent;

	/***
	 * notificationName1:role11,role12;notification2:role21,role22..... If this
	 * is based on business logic, then put that in your service
	 */

	String notificationsToBeRemoved;

	/***
	 * Is this step applicable or possible
	 * 
	 * @param forActors
	 *            set of actor-role that the logged-in user can play. if this is
	 *            null, we do not bother about the user
	 * @param forState
	 *            when the workflow is in this state
	 * @param dc
	 * @param dbHandle
	 * @param customLogic
	 * @return true if this step is applicable
	 * @throws ExilityException
	 */
	public boolean isApplicable(Set<String> forActors, String forState,
			DataCollection dc, DbHandle dbHandle, CustomLogic customLogic)
			throws ExilityException {
		/**
		 * Current state should match. null means action is valid in any state
		 */
		if (this.currentState != null
				&& this.currentState.equals(forState) == false) {
			return false;
		}

		/**
		 * are we to check for logged-in user?
		 */
		if (forActors != null && this.actor != null
				&& forActors.contains(this.actor) == false) {
			return false;
		}

		if (this.validityExpression != null) {
			return this.validityExpression.evaluate(dc).getBooleanValue();
		}
		/**
		 * validity function or validity service can be defined. Note that these
		 * two communicate thru a field in dc called WorkflowNames.STEP_IS_VALID
		 */
		if (this.validityFunction != null) {
			if (customLogic == null) {
				throw new ExilityException(
						"Worlfow step  "
								+ this.name
								+ " has specified "
								+ this.validityFunction
								+ " as validity funciton, but a custom logic class is not specified for teh workflow");
			}

			customLogic.executeFunction(this.validityFunction);
		} else if (this.validityServiceName != null) {
			ServiceInterface service = Services.getService(
					this.validityServiceName, dc);
			if (service == null) {
				throw new ExilityException(this.validityServiceName
						+ " is not a valid service.");
			}
			service.execute(dc, dbHandle);
		} else {
			return true;
		}

		boolean isValid = dc
				.getBooleanValue(WorkflowNames.STEP_IS_VALID, false);
		dc.removeValue(WorkflowNames.STEP_IS_VALID);
		return isValid;
	}

	/***
	 * Move the workflow thru this step after validating the step
	 * 
	 * @param forActors
	 *            : possible actor-role that the logged-in user can play
	 * @param forState
	 *            : current state of the workflow
	 * @param dc
	 * @param handle
	 * @param customLogic
	 * @return true of we successfully moved the document thru this workflow
	 *         step
	 * @throws ExilityException
	 */

	boolean moveItIfValid(Set<String> forActors, String forState,
			DataCollection dc, DbHandle handle, CustomLogic customLogic)
			throws ExilityException {
		if (!this.isApplicable(forActors, forState, dc, handle, customLogic)) {
			return false;
		}

		/**
		 * new state could be specified in different way
		 */
		String st = this.newState;
		if (st == null) {
			if (this.newStateExpression != null) {
				st = this.newStateExpression.evaluate(dc).getTextValue();
			}
		}

		/**
		 * push it to dc for rest of the logic to use it
		 */
		if (st != null) {
			dc.addTextValue(WorkflowNames.TO_STATE, st);
		}

		else if (this.newStateFunction != null) {
			customLogic.executeFunction(WorkflowStep.this.newStateFunction);
		}

		if (this.actionServiceName != null) {
			ServiceInterface service = Services.getService(
					this.actionServiceName, dc);
			service.execute(dc, handle);
		}

		// are we to save the attached document?
		if (this.tablesToBeSaved != null) {
			for (String tableToBeSaved : WorkflowStep.this.tablesToBeSaved) {
				TableInterface table = Tables.getTable(tableToBeSaved, dc);
				if (table == null) {
					throw new ExilityException(tableToBeSaved
							+ " is not a valid table.");
				}
				table.save(dc, handle);
			}
		}

		if (this.notificationsToBeSent != null) {
			this.sendNotifications(dc, handle);
		}

		if (this.notificationsToBeRemoved != null) {
			this.removeNotifications(dc, handle);
		}

		return true;
	}

	/***
	 * We have to remove notifications.
	 * 
	 * @param dc
	 * @param handle
	 */

	private void removeNotifications(DataCollection dc, DbHandle handle) {
		// TODO Auto-generated method stub
	}

	/**
	 * 
	 * @param dc
	 * @param handle
	 */
	private void sendNotifications(DataCollection dc, DbHandle handle) {
		// TODO Auto-generated method stub
	}

	/**
	 * @param actorNames
	 *            nick names of actors for this workflow. can be null if you do
	 *            not know about any of them :-)
	 * @return array of field values as per header specified in getFieldHeader()
	 */
	public String[] getFields(Map<String, String> actorNames) {
		String userNickName = null;
		if (actorNames != null) {
			userNickName = actorNames.get(this.actor);
		}
		if (userNickName == null) {
			userNickName = "";
		}
		String[] fields = { this.name, this.description, this.currentState,
				this.actor, this.action, this.actionLabel, this.helpText,
				userNickName };
		return fields;
	}
}

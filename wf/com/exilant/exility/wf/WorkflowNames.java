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

import com.exilant.exility.core.Comparator;
import com.exilant.exility.core.Condition;

/***
 * All the names that are used in workflow
 * 
 * @author Exilant Technologies
 * 
 */
public class WorkflowNames {
	/**
	 * table in which workflow roles are saved
	 */
	public static final String ROLES_TABLE = "exilWorkflowRoles";

	/**
	 * column name for role id
	 */
	public static final String ROLE_ID = "roleId";

	/**
	 * column name for role name
	 */
	public static final String ROLE_NAME = "roleName";

	/**
	 * column name for isActive
	 */
	public static final String INACTIVE = "isActive";

	/**
	 * table name for work flow users
	 */
	public static final String USERS_TABLE = "exilWorkflowUsers";

	/**
	 * column name for user id
	 */
	public static final String USER_ID = "userId";

	/**
	 * column name for user nick name
	 */
	public static final String USER_NICK_NAME = "userNickName";

	/**
	 * table name for work flow assigned roles
	 */
	public static final String ASSIGNED_ROLES_TABLE = "exilWorkflowAssignedRoles";

	/**
	 * column name for assigned roles
	 */
	public static final String ASSIGNED_ROLE_ID = "assignedRoleId";

	/**
	 * column name for reporting id
	 */
	public static final String REPORTING_ID = "reportingAssignedRoleId";

	/**
	 * table that keeps all active roles
	 */
	public static final String WORKFLOW_TABLE = "exilActiveWorkflows";

	/**
	 * column for workflow name
	 */
	public static final String WORKFLOW_NAME = "workflowName";

	/**
	 * column for
	 */
	public static final String WORKFLOW_ID = "workflowId";

	/**
	 * column for document id
	 */
	public static final String DOCUMENT_ID = "documentId";

	/**
	 * column for current state
	 */
	public static final String CURRENT_STATE = "currentState";

	/**
	 * column for initiator
	 */
	public static final String INITIATOR = "initiatorId";

	/**
	 * column for initiator's role
	 */
	public static final String INITIATOR_ROLE_ID = "initiatorAssignedRoleId";

	/**
	 * column for
	 */
	public static final String KNOWN_ACTORS = "knownActors";
	// known roles are encoded as role1=id1;role2=id2...

	/**
	 * separator between actors in the coded string
	 */
	public static final String KNOWN_ACTORS_LIST_DELIMITER = ";";

	/**
	 * separator actor and role in the coded string
	 */
	public static final String KNOWN_ACTOR_ROLE_SEPARATOR = "=";

	/**
	 * table for logs
	 */
	public static final String WORKFLOW_LOG_TABLE = "exilActiveWorkflowLog";

	/**
	 * column for actor
	 */
	public static final String WORKFLOW_ACTOR = "workflowActor";

	/**
	 * column for
	 */
	public static final String WORKFLOW_ACTION = "workflowAction";

	/**
	 * column for from-work-flow-state
	 */
	public static final String FROM_STATE = "fromWorkflowState";

	/**
	 * column for to-work-state
	 */
	public static final String TO_STATE = "toWorkflowState";

	/**
	 * column for default state of this document
	 */
	public static final String DEFAULT_STATE = "draft";

	/**
	 * column for remarks
	 */
	public static final String ACTION_REMARKS = "actionRemarks";

	/***
	 * filter condition to get roles for a given user id from assigned roles
	 * table
	 */

	public static final Condition[] ROLE_CONDITIONS = {
			new Condition(WorkflowNames.USER_ID, WorkflowNames.USER_ID,
					Comparator.EQUALTO),
			new Condition(WorkflowNames.INACTIVE, WorkflowNames.INACTIVE,
					Comparator.EQUALTO) };

	/***
	 * grid to which roles assigned to logged-in user are extracted into
	 */
	public static final String ROLES_GRID = "roles";
	/**
	 * grid to be returned as valid headers
	 */
	public static final String VALID_ACTIONS_GRID_NAME = "validActions";

	/**
	 * grid header for valid actions grid
	 */
	public static final String[] VALID_ACTIONS_HEADER = { "name", "label",
			"description" };

	/***
	 * field name inside dc that indicates whether the current step is valid or
	 * not. Used by custom functions
	 */
	public static final String STEP_IS_VALID = "_workFlowStepIsValid";

	/**
	 * next steps grid name. contains one or more steps that are valid at this
	 * state of the workflow.
	 */
	public static final String NEXT_STEPS_GRID_NAME = "nextSteps";

	/**
	 * field names that are present in NEXT_STEP_GRID
	 */
	public static final String[] NEXT_STEPS_HEADER = { "name", "description",
			"currentState", "actor", "action", "actionLabel", "helpText",
			"userNickName" };

	/**
	 * known actors' assignedRoleIds are put into this grid
	 */
	static final String KNOWN_ASSIGNED_ROLES_GRID_NAME = "knownRoles";

	// getting userIds from assigned roles. First the view that joins
	// assignedRole to users
	/**
	 * view that gets user details from assigned roles. This view will have
	 * assignedRoleId from exilworkflowAssignedRoles table. userId and
	 * userNickName from exilUsers table
	 */
	public static final String ASSIGNED_USERS_VIEW = "exilWorkflowAssignedUsers";

	/**
	 * column name in the view for nick name
	 */
	public static final String ASSIGNED_USER_NICKNAME = "assignedRoleNickName";
}

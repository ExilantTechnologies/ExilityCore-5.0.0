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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.exilant.exility.core.AP;
import com.exilant.exility.core.DataAccessType;
import com.exilant.exility.core.DataCollection;
import com.exilant.exility.core.DbHandle;
import com.exilant.exility.core.ExilityException;
import com.exilant.exility.core.ServiceData;
import com.exilant.exility.core.ServiceInterface;
import com.exilant.exility.core.Spit;
import com.exilant.exility.core.TableInterface;
import com.exilant.exility.core.Tables;
import com.exilant.exility.core.ToBeInitializedInterface;
import com.exilant.exility.core.Value;
import com.exilant.exility.core.ValueList;

/**
 * Implements a work flow that is modeled as a state-transition mechanism. It is
 * important to understand that we use state-transition in a strict academic
 * way. Our belief is that this modeling will be able to simplify most of our
 * application requirements. We keep the implementation fairly flexible to allow
 * any custom code that a specific application may require.
 * 
 */
public class Workflow implements ServiceInterface, ToBeInitializedInterface {
	/**
	 * name is the name of the workflow :-) Hope that is helpful.
	 */
	String name;

	/***
	 * for documentation
	 */
	String description;

	/***
	 * possible states (stops) in the workflow. These are the boxes in a
	 * workflow diagram.
	 */
	WorkflowState[] states;

	/***
	 * shall we say pushers? user roles who take actions that pushes the
	 * document to another state.
	 */
	WorkflowActor[] actors;

	/***
	 * steps that make-up the work flow. These are the segments that connect
	 * states in a workflow. Each step represents an action that a user take and
	 * the effect of that action.
	 */
	WorkflowStep[] steps;

	/***
	 * A workflow defines how a document goes thru the process, but generally
	 * does not bother about the document's content. However, the logic for the
	 * work flow require attributes of the document. We ALWAYS define a work
	 * flow for a specific document, and the document has to be in a table. (can
	 * be a view as well. We look for a table.xml for this)
	 */
	String tableName;

	/***
	 * key field name of the above table
	 */
	String docIdName;

	/***
	 * What if this workflow requires some additional business logic than what
	 * can be modeled here? Here is the way. write your own class that extends
	 * WorflowLogic and implement your logic inside executeFunction(); Refer to
	 * WorkflowStep to see how to invoke the logic
	 */
	String customWorkflowClassName;

	/***
	 * remember this is a service!! So you should tell me the data access you
	 * need. Default is READ_WRITE
	 */
	DataAccessType dataAccessType = DataAccessType.READWRITE;
	/***
	 * Do you want your class to handle every thing? all Workflow functions will
	 * be directly delegated to an instance of the above class
	 */
	boolean letCustomClassHandleEveryThing;
	/**
	 * Let us not keep looking around each time for the class..
	 */
	private Class<CustomLogic> customClass;

	/***
	 * name
	 * 
	 * @return name
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/***
	 * this method delivers all required services for clients dc should contain
	 * the following fields
	 * <ul>
	 * <li>1. workflowId : name the workflow definition. This is required.</li>
	 * <li>2. userId : id of the person who is taking action. This is required,
	 * except for action=getAllSteps</li>
	 * <li>3. actionName : Specific action that user wants to take. possible
	 * actions are
	 * <ul>
	 * <li>not-specified : get all actions for the logged-in user and next steps
	 * </li>
	 * <li>save : save the document associated with this workflow with no change
	 * to state</li>
	 * <li>valid action for this workflow : move the workflow with this action.</li>
	 * </ul>
	 * <li>4. documentId : internal id of the specific instance of the document
	 * that is being acted upon. Optional if action is none, or create. dc
	 * should have other fields that are required as per document work flow
	 * definition for the intended action</li>
	 * </ul>
	 * 
	 * @param dc
	 *            data collection that contains all data, and to which results
	 *            are put into
	 * @throws ExilityException
	 * 
	 */

	@Override
	public void execute(DataCollection dc, DbHandle handle)
			throws ExilityException {
		CustomLogic customLogic = null;
		if (this.customClass != null) {
			try {
				customLogic = this.customClass.newInstance();
				customLogic.initialize(dc, handle);
			} catch (Exception e) {
				throw new ExilityException("Workflow " + this.name
						+ " failed to get an instance of "
						+ this.customWorkflowClassName + " Error :"
						+ e.getMessage());
			}
		}

		String wfAction = dc.getTextValue(WorkflowNames.WORKFLOW_ACTION, null);
		if (customLogic != null && this.letCustomClassHandleEveryThing) {
			if (wfAction != null && wfAction.length() > 0) {
				customLogic.moveIt();
			} else {
				customLogic.getValidActions();
			}
		} else {
			new WorkflowLogic(dc, handle, customLogic).execute();
		}
	}

	@Override
	public DataAccessType getDataAccessType(DataCollection dc) {
		return this.dataAccessType;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialize() {
		/**
		 * get the class from class name
		 */
		if (this.customWorkflowClassName != null) {
			try {
				this.customClass = (Class<CustomLogic>) Class
						.forName(this.customWorkflowClassName);
			} catch (ClassNotFoundException e) {
				Spit.out(this.customWorkflowClassName
						+ " is not a valid class name. This class is designated as custom logic class for work flow "
						+ this.name);
			}
		}
	}

	/***
	 * Logic that uses the attributes of the workflow, and the current state
	 * from db is encapsulated into a separate class for ease of storing
	 * transient attributes
	 * 
	 * @author Exilant Technologies
	 * 
	 */
	private class WorkflowLogic {

		/**
		 * current attributes of the workflow that do not change during the life
		 * of this instance
		 */
		private final DataCollection dc;
		private final DbHandle handle;
		private final CustomLogic customLogic;
		private final long workflowId;
		private final String action;
		private final long userId;

		/**
		 * attributes mutated by methods
		 */
		private long docId;
		private String currentState;
		private int nbrKnownActors = 0;
		private String[] knownActors;
		private long[] knownAssignedRoles;
		private WorkflowStep[] possibleSteps;
		private Set<String> loggedInRoles;
		private Map<String, String> actorNames = new HashMap<String, String>();

		WorkflowLogic(DataCollection dc, DbHandle dbHandle, CustomLogic logic) {
			this.dc = dc;
			this.handle = dbHandle;
			this.customLogic = logic;
			this.workflowId = this.dc.getIntegralValue(
					WorkflowNames.WORKFLOW_ID, 0);

			this.docId = this.dc.getIntegralValue(Workflow.this.docIdName, 0);
			this.userId = this.dc.getIntegralValue(WorkflowNames.USER_ID, 0);
			this.action = this.dc.getTextValue(WorkflowNames.WORKFLOW_ACTION,
					null);
		}

		/***
		 * delegated from workflow instance to facilitate saving attributes
		 * across method executions and make it thread-safe
		 * 
		 * @throws ExilityException
		 */
		void execute() throws ExilityException {

			if (this.userId == 0) {
				throw new ExilityException(
						"WorkflowInterface request requires value for field "
								+ WorkflowNames.USER_ID);
			}

			this.readData();
			this.setKnownActors();
			this.setCurrentUserRoles();
			this.setPossibleStepsForThisUser();

			if (this.action != null && this.action.length() > 0) {
				this.moveIt();
			} else {
				this.getValidActions();
			}
		}

		/***
		 * move the document one step on the workflow maze! dc has all the input
		 * and the place to put output as well. Name of the field to look for as
		 * well as the name of the grid is defined in WorkflowNames class for
		 * all workflow related design
		 */
		boolean moveIt() throws ExilityException {
			int nbrStep = this.possibleSteps.length;
			for (int i = 0; i < nbrStep; i++) {
				WorkflowStep step = this.possibleSteps[i];
				if (step.action.equals(this.action)) {
					this.dc.addTextValue(WorkflowNames.CURRENT_STATE,
							step.newState);

					if (step.moveItIfValid(this.loggedInRoles,
							this.currentState, this.dc, this.handle,
							this.customLogic)) {
						/*
						 * This is required to keep log who has acted.
						 */
						this.dc.addTextValue(WorkflowNames.WORKFLOW_ACTOR,
								step.actor);
						this.dc.addTextValue(WorkflowNames.FROM_STATE,
								step.currentState);
						if (this.workflowId == 0) {
							/**
							 * If this workflow is not created then set
							 * INITIATOR as logged in user id.
							 */
							this.dc.addTextValue(WorkflowNames.INITIATOR,
									this.dc.getTextValue(
											AP.loggedInUserFieldName, null));
						}
						this.saveData();
						return true;
					}
				}
			}
			// no action could be taken
			this.dc.addError("No action could be taken on the work flow. Logged-in user may not have the preveleges to take this action on this document at this state");
			return false;
		}

		/***
		 * for the current document, what are the valid actions that the
		 * logged-in user can take? dc has all the input and the place to put
		 * output as well. Name of the field to look for as well as the name of
		 * the grid is defined in WorkflowNames class for all workflow related
		 * design
		 * 
		 * We are also asked to put next steps as part of this method
		 * 
		 * @throws ExilityException
		 */
		public void getValidActions() throws ExilityException {

			this.getNextSteps();

			ArrayList<String[]> validActions = new ArrayList<String[]>();
			validActions.add(WorkflowNames.VALID_ACTIONS_HEADER);

			/**
			 * filter out the steps from possible steps
			 */
			if (this.possibleSteps.length > 0) {
				for (WorkflowStep step : this.possibleSteps) {
					if (!step.isApplicable(this.loggedInRoles,
							this.currentState, this.dc, this.handle,
							this.customLogic)) {
						continue;
					}

					String[] row = { step.action, step.actionLabel,
							step.helpText };
					validActions.add(row);
				}
			}

			/**
			 * create a grid out of the array we have formed, and add it to dc
			 */
			String[][] rows = validActions.toArray(new String[0][0]); // Workflow.listTo2DArray(validActions);

			Spit.out(rows.length + " valid actions are found with last row as "
					+ rows[rows.length - 1]);
			this.dc.addGrid(WorkflowNames.VALID_ACTIONS_GRID_NAME, rows);
		}

		/***
		 * Read data from active_workflows
		 * 
		 * @return
		 * @throws ExilityException
		 */
		protected boolean readData() throws ExilityException {
			TableInterface table = null;

			/**
			 * extract work flow details if this is already flowing
			 */
			if (this.workflowId != 0) {
				table = Tables.getTable(WorkflowNames.WORKFLOW_TABLE, this.dc);
				if (table.read(this.dc, this.handle, null, null) == 0) {
					this.dc.addError("Unable to read details for id "
							+ this.workflowId + " for workflow "
							+ Workflow.this.name);
					return false;
				}

				/**
				 * get doc id from dc
				 */
				if (this.docId == 0) {
					this.docId = this.dc.getIntegralValue(
							Workflow.this.docIdName, 0);
				}
			}

			/**
			 * id document is specified, let us get details of this document
			 */
			if (this.docId != 0) {
				table = Tables.getTable(Workflow.this.tableName, this.dc);
				if (table.read(this.dc, this.handle, null, null) == 0) {
					this.dc.addError("Unable to read document for id "
							+ this.docId + " from table "
							+ Workflow.this.tableName);
					return false;
				}
			}

			this.currentState = this.dc.getTextValue(
					WorkflowNames.CURRENT_STATE, WorkflowNames.DEFAULT_STATE);
			return true;
		}

		/***
		 * decode knownActors string and set corresponding array values
		 * 
		 * @throws ExilityException
		 */
		protected void setKnownActors() throws ExilityException {
			String[] knownPairs = {};
			String knownText = this.dc.getTextValue(WorkflowNames.KNOWN_ACTORS,
					null);
			if (knownText != null && knownText.length() > 0) {
				knownPairs = knownText
						.split(WorkflowNames.KNOWN_ACTORS_LIST_DELIMITER);
			}
			this.nbrKnownActors = knownPairs.length;
			this.knownActors = new String[this.nbrKnownActors];
			this.knownAssignedRoles = new long[this.nbrKnownActors];
			TableInterface assignedUsers = Tables.getTable(
					WorkflowNames.ASSIGNED_USERS_VIEW, this.dc);
			for (int i = 0; i < this.nbrKnownActors; i++) {
				String aPair = knownPairs[i];
				try {
					/**
					 * pair is of the form actor=assignedRoleId
					 */
					String[] actorAndRole = aPair
							.split(WorkflowNames.KNOWN_ACTOR_ROLE_SEPARATOR);
					String actor = this.knownActors[i] = actorAndRole[0];
					long assignedRole = this.knownAssignedRoles[i] = Long
							.parseLong(actorAndRole[1]);
					this.dc.addValue(WorkflowNames.ASSIGNED_ROLE_ID,
							Value.newValue(assignedRole));

					if (assignedUsers.readbasedOnKey(this.dc, this.handle) != 0) {
						String nickName = this.dc.getTextValue(
								WorkflowNames.ASSIGNED_USER_NICKNAME, "");
						this.actorNames.put(actor, nickName);
					}
				} catch (Exception e) {
					this.dc.addError("Internal error: invalid encoded role and id data "
							+ aPair);
					this.knownActors[i] = "";
				}
			}
		}

		/***
		 * find out what steps are possible for current state. Put these into dc
		 * in the designated grid
		 * 
		 * @throws ExilityException
		 */
		private void getNextSteps() throws ExilityException {
			int nbrTotalSteps = Workflow.this.steps.length;
			int nbrValidSteps = 0;
			WorkflowStep[] validSteps = new WorkflowStep[nbrTotalSteps];
			for (WorkflowStep step : Workflow.this.steps) {
				if (this.currentState.equals(step.currentState)) {
					validSteps[nbrValidSteps] = step;
					nbrValidSteps++;
				}
			}
			/**
			 * we have to get user details for these assigned roles. read from
			 * userRoleDetails where assignedRoleId in this column
			 */
			String[][] data = new String[nbrValidSteps + 1][];
			data[0] = WorkflowNames.NEXT_STEPS_HEADER;
			for (int i = 0; i < nbrValidSteps; i++) {
				WorkflowStep step = validSteps[i];
				data[i + 1] = step.getFields(this.actorNames);
			}
			this.dc.addGrid(WorkflowNames.NEXT_STEPS_GRID_NAME, data);
		}

		/***
		 * find out what steps are possible for this user
		 */
		private void setPossibleStepsForThisUser() {
			ArrayList<WorkflowStep> validSteps = new ArrayList<WorkflowStep>();
			for (WorkflowStep step : Workflow.this.steps) {
				if (this.currentState.equals(step.currentState)
						&& this.loggedInRoles.contains(step.actor)) {
					validSteps.add(step);
				}
			}

			this.possibleSteps = validSteps.toArray(new WorkflowStep[validSteps
					.size()]);
		}

		/***
		 * set workflow actor roles that the logged-in user can play into
		 * this.loggedInRoles
		 * 
		 * @throws ExilityException
		 */
		private void setCurrentUserRoles() throws ExilityException {
			TableInterface rolesTable;
			/**
			 * extract roles that are assigned to this user in to a grid. Note
			 * that the id is "assigned_role_id", and not role_id
			 */
			rolesTable = Tables.getTable(WorkflowNames.ASSIGNED_ROLES_TABLE,
					this.dc);

			/**
			 * get all possible roles for the logged-in user
			 */
			int nbrRoles = rolesTable.filter(this.dc, this.handle,
					WorkflowNames.ROLES_GRID, WorkflowNames.ROLE_CONDITIONS,
					null, null, null, null, false);

			if (nbrRoles == 0) {
				Spit.out("No roles found for the logged-in user " + this.userId
						+ " for workflow " + Workflow.this.name);
				return;
			}

			/**
			 * get the list of roles from the grid
			 */
			ValueList ids = this.dc.getGrid(WorkflowNames.ROLES_GRID)
					.getColumn(WorkflowNames.ASSIGNED_ROLE_ID);
			int nbrIds = ids.length();

			/**
			 * workflow actor roles that this user can play, based on all the
			 * org-roles in the list
			 */
			this.loggedInRoles = new HashSet<String>();

			/**
			 * for each org-roles that this user is assigned, check if there is
			 * a workflow role associated with that
			 */
			for (int i = 0; i < nbrIds; i++) {
				long thisId = ids.getIntegralValue(i);

				/**
				 * go thru the array of known assigned roles and see if this
				 * user is assigned any one of them
				 */
				for (int j = 0; j < this.knownAssignedRoles.length; j++) {
					if (this.knownAssignedRoles[j] == thisId) {
						this.loggedInRoles.add(this.knownActors[j]);
					}
				}
			}
		}

		private void saveData() throws ExilityException {
			TableInterface table = Tables.getTable(
					WorkflowNames.WORKFLOW_TABLE, this.dc);
			table.save(this.dc, this.handle);// save/update data

			table = Tables.getTable(WorkflowNames.WORKFLOW_LOG_TABLE, this.dc);
			table.insert(this.dc, this.handle);// Log current action

		}
	}

	@Override
	public void executeAsStep(DataCollection dc, DbHandle handle)
			throws ExilityException {
		this.execute(dc, handle);

	}

	@Override
	public boolean toBeRunInBackground() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void serve(ServiceData inData, ServiceData outData) {
		// TODO Auto-generated method stub

	}
}

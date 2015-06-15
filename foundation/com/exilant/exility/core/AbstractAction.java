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

/**
 * an action as a response to any event on the client page. Actions are defined
 * at the page level, and can be assigned for various events such as onClick,
 * onLoad etc..
 * 
 */
abstract class AbstractAction {

	/**
	 * lists all possible attributes across all concrete classes. This is used
	 * to set attributes from a central place rather than doing it in each class
	 */
	static final String[] ACTION_ATTRIBUTE_NAMES = { "name",
			"warnIfFormIsModified", "functionName", "pageToGo", "windowToGo",
			"windowDisposal", "serviceId", "callBackActionName",
			"waitForResponse", "submitForm", "toRefreshPage", "disableForm",
			"passDc", "mailTo", "resetFormModifiedState",
			"atLeastOneFieldIsRequired", "reportName", "queryFieldNames",
			"fieldsToReset", "windowName", "validateQueryFields",
			"submitInNewWindow", "fieldToFocusAfterExecution", "closeWindow",
			"callBackEvenOnError", "popupPanel", "popdownPanel" };
	static final String[] ALL_TABLE_SENSITIVE_ATTRIBUTES = {
			"substituteValueFrom", "parameter" };
	static final String[] ALL_TABLE_SENSITIVE_ARRAYS = { "showPanels",
			"hidePanels", "queryFieldSources", "disableFields", "enableFields" };

	/***
	 * name has to be unique within a page
	 */
	String name = null;

	/***
	 * panels to be be display (unhide) when this action taken. NOt just panels,
	 * almost all elements can be showed.
	 */
	String[] showPanels = null;

	/***
	 * panels to be hidden on this action
	 */
	String[] hidePanels = null;

	/***
	 * input capable fields to be disabled on this action. List panel can also
	 * be disabled, in which clicks do not work.
	 */
	String[] disableFields = null;

	/***
	 * fields or list panel to be enabled
	 */
	String[] enableFields = null;

	/***
	 * A warning to be issued and re-confirmation sought about modifications
	 * being lost before taking this action
	 */
	boolean warnIfFormIsModified = false;

	/***
	 * This action is supposed to take care of modifications to the form, and
	 * hence once this action is taken, assume that the form is not modified.
	 */
	boolean resetFormModifiedState = false;

	/***
	 * set the input focus on this field
	 */
	String fieldToFocusAfterExecution = null;

	/***
	 * a panel that should appears as if it is a popup
	 */
	String popupPanel = null;

	/***
	 * bring back the panel to normal. Note that this could have been a boolean
	 * to say simply pop-down the one that is up. However, users may get
	 * confused with its usage. Hence I have designed it as if they have to give
	 * a panel name, but I actually ignore that name, and bring-down the
	 * popped-up panel, if it is there.
	 */
	String popdownPanel = null;

	/***
	 * document this action
	 */
	String description = null;

	/***
	 * during development, developers often debug by commenting out elements..
	 */
	boolean markedAsComment = false;

	// watch out. This method is extended by serverAction for a special case.
	/***
	 * get the javascript for this action. This is over-ridden by serverAction
	 * 
	 * @param js
	 * @param pageContext
	 */
	void toJavaScript(StringBuilder js, PageGeneratorContext pageContext) {
		js.append("\n/***** action field = ").append(this.name)
				.append("  ********/\n");
		js.append(Page.JS_VAR_NAME)
				.append(" = new PM.")
				.append(this
						.getClass()
						.getName()
						.substring(
								this.getClass().getName().lastIndexOf('.') + 1))
				.append("();");
		pageContext.setAttributes(this, js,
				AbstractAction.ACTION_ATTRIBUTE_NAMES);
		pageContext.setTableSensitiveAttributes(this, js,
				AbstractAction.ALL_TABLE_SENSITIVE_ATTRIBUTES);
		pageContext.setTableSensitiveArrays(this, js,
				AbstractAction.ALL_TABLE_SENSITIVE_ARRAYS);
		js.append('\n').append("P2.addAction(").append(Page.JS_VAR_NAME)
				.append(");");
	}
}

/**
 * no action, but do other side-effects like hide/show panels etc..
 * 
 */
class DummyAction extends AbstractAction {
	//
}

/**
 * execute a java-script function
 * 
 */
class LocalAction extends AbstractAction {
	/**
	 * name of the function to be executed. Though we designed this as function
	 * name, users could write set of javaScript statements. It worked because
	 * of the way we have initially implemented it. Since people had already
	 * assumed it to be a feature, we now allow set of statements also as a
	 * feature, even though we have re-factored the code to run better when it
	 * is a function. Function is called with
	 * (element-that-triggered-this-event, field-name, parameterValue) as
	 * arguments
	 */
	String functionName = null;
	/**
	 * in case you designed a generic function that is invoked from more than
	 * one action, you can specify the parameter here that helps the function in
	 * dealing with such a design
	 */
	String parameter = null;
}

enum RefreshPageType {
	none, beforeMyAction, afterMyAction
}

class ServerAction extends AbstractAction implements ToBeInitializedInterface {
	/**
	 * fully qualified name of service
	 */
	String serviceId = null;

	/**
	 * fields that are sent along with this service. Query field is same as
	 * submit field,except that the query fields, by default are not validated.
	 */
	String[] queryFieldNames = null;

	/**
	 * in case the names with which the query fields are to go to server are
	 * different from their name in this page.
	 */
	String[] queryFieldSources = null;

	/**
	 * do you want one of your actions to be called when the service returns
	 */
	String callBackActionName = null;

	/**
	 * should be left as false, except for some special purpose testing. If set
	 * to true, call to server for the service will wait for a response from
	 * server, and block all script execution. Even mouse move actions do not
	 * execute during this time, and system may hang if there is an issue with
	 * network etc..
	 */
	boolean waitForResponse = false;

	/**
	 * implies that all fields and tables as defined in this page will be
	 * accompany the service request. This term is because of the html norm
	 * called submitting the form.
	 */
	boolean submitForm = false;

	/**
	 * in case you want to submit a subset of fields and tables, use this
	 * feature to specify comma separated list of fields and tables.
	 */
	String submitFields = null;

	/**
	 * Very useful in a search page, where you want to insist that at least one
	 * search criterion is specified. Implies that though all the fields are
	 * optional, at least one field must have a value for the service to
	 * trigger.
	 */
	boolean atLeastOneFieldIsRequired = false;

	/**
	 * prevent user from in interacting with the page. We use a transparent
	 * image to cover the page. Note that it is still possible for a clever user
	 * to use tabs to position input focus to fields on the page and do
	 * something. This may cause some nuisance to user, but will not lead to any
	 * security issue.
	 */
	boolean disableForm = false;

	/**
	 * by default, when a service returns, we push all the field/table values
	 * into corresponding fields/tables. This process is called "refreshPage".
	 * You may change this behavior. 'afterMyAction', 'beforeMyAction' and
	 * 'none' are the non-default values.
	 */
	RefreshPageType toRefreshPage = RefreshPageType.beforeMyAction;

	/**
	 * by default query fields are not validated.
	 */
	String validateQueryFields = null;

	/**
	 * Special feature to take care of some specific requirement. Not to be used
	 * anymore.
	 */
	boolean submitInNewWindow = false;

	/**
	 * to be used in case the current window is a pop-up, and you want to close
	 * the window after this action.
	 */
	boolean closeWindow = true;

	/**
	 * by default, exility handles errors coming back from server, and does not
	 * act on the assigned call-back action, nor does it refresh page with data
	 * that has come from server. You may want to execute your action to do
	 * something even if there is some error.
	 */
	boolean callBackEvenOnError;

	@Override
	void toJavaScript(StringBuilder js, PageGeneratorContext pageContext) {
		super.toJavaScript(js, pageContext);
		if (this.submitFields == null) {
			return;
		}
		/**
		 * declare two variables that will be filled up by page.ToHTML..
		 */
		String nam = "fieldsToSubmit";
		/*
		 * var thisActionfieldsToSubmit = new Object();
		 * 
		 * ele.fieldsToSubmit = this.thisActionfieldsToSubmit;
		 */
		js.append("\nvar ").append(this.name).append(nam)
				.append(" = new Object();");
		js.append("\n ").append(Page.JS_VAR_NAME).append(".").append(nam)
				.append(" = ").append(this.name).append(nam).append(";");

		nam = "tablesToSubmit";
		js.append("\nvar ").append(this.name).append(nam)
				.append(" = new Object();");
		js.append("\n ").append(Page.JS_VAR_NAME).append(".").append(nam)
				.append(" = ").append(this.name).append(nam).append(";");
	}

	@Override
	public void initialize() {
		if (this.queryFieldSources == null) {
			this.queryFieldSources = this.queryFieldNames;
		}
		if (this.submitForm) {
			this.resetFormModifiedState = true;
		}
	}
}

/**
 * what to do with current window whenever there is a navigation action.
 */
enum WindowDisposalType {
	/**
	 * current page is gone, and replaced with the new page
	 */
	replace

	/**
	 * current page remains in tact, and the new one is displayed as a pop-up on
	 * this page
	 */
	, popup
	/**
	 * current page is in tact, but is hidden behind the new page. once the new
	 * page closes, this page will come back as if nothing happened to it
	 */
	, retainState
	/**
	 * current page context and display is retained but hidden behind the new
	 * page. Once the new page closes, this page is reloaded, possibly after
	 * execution onResetAction before showing it
	 */
	, reset
}

/**
 * represents an action that results in opening a new page.
 * 
 */
class NavigationAction extends AbstractAction implements
		ToBeInitializedInterface {

	/**
	 * new page to be opened
	 */
	String pageToGo = null;

	/**
	 * in case pagToGo requires some variable to be substituted, have #value# in
	 * the above string and specify the name of the field below, from which the
	 * value is to be substituted example ../HTML/#value#.htm can be used if the
	 * actual page name is received in a field, say pageName set next attribute
	 * to pageName.
	 */
	String substituteValueFrom = null;

	/**
	 * by default a new window/current window based on windowDisposal attribute.
	 * But if you use subWindowPanel, you may navigate to that
	 */
	String windowToGo = null;

	/**
	 * fields to be passed to next page. These are received by new page as
	 * pageParameters.
	 */
	String[] queryFieldNames = null;
	/**
	 * in case the field names are different from the one you want to send them
	 * as
	 */
	String[] queryFieldSources = null;

	/**
	 * How to deal with current page and new page windows? read comments on the
	 * enum
	 */
	WindowDisposalType windowDisposal = WindowDisposalType.replace;

	/**
	 * possible to pass several fields and tables to next page. This feature is
	 * not intuitive, and does not follow normal html beviour. Like when the
	 * next page is reset. Use with caution.
	 */
	boolean passDc = false;

	/**
	 * set the logical name of the new page window. This can be referred in your
	 * script later to get the details of this page from another
	 */
	String windowName = null;

	/**
	 * by default, query fields are not validated
	 */
	String validateQueryFields = null;

	/**
	 * add an additional parameter called serviceId and pass this value. To be
	 * processed by the new page as a page parameter
	 */
	String serviceId = null;

	@Override
	public void initialize() {
		if (this.queryFieldSources == null) {
			this.queryFieldSources = this.queryFieldNames;
		}
	}

}

/**
 * Opens a browser window with mail protocol and leaves it to browser to do the
 * rest. Not used.
 * 
 */
class MailToAction extends AbstractAction {
	/**
	 * to address
	 */
	String mailTo = null;

	/**
	 * if the mail address has a variable content, you can have #value# in
	 * mailTo which will be replaced with the value in this field
	 */
	String substituteValueFrom = null;
}

/**
 * close this page
 * 
 */
class CloseAction extends AbstractAction {
	String functionName = null;

	CloseAction() {
		this.warnIfFormIsModified = true;
	}
}

/**
 * reset and reload this action
 * 
 */
class ResetAction extends AbstractAction {
	String[] fieldsToReset = null;

	ResetAction() {
		this.warnIfFormIsModified = !AP.quietResetAction;
	}
}

/**
 * upload a file
 * 
 */
class UploadFileAction extends ServerAction {
	//
}

/**
 * download a file
 * 
 */
class DownloadFileAction extends ServerAction {
	//
}

/**
 * open report in a new window
 * 
 */
class ReportAction extends ServerAction {
	String reportName = null;
}

/**
 * save the report as xl
 * 
 */
class SaveAsXlsAction extends ReportAction {
	//
}

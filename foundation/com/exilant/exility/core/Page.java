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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.exilant.exility.ide.FieldDetails;
import com.exilant.exility.ide.FieldIssue;

/***
 * Represents a page/form that is used for user interaction Unlike other Exility
 * components, page is NOT designed to be immutable. It is assumed that the page
 * is NOT cached for reuse across sessions/threads. Its initialize() is also
 * different from other components. It is done top-down with teh context for us
 * to report errors at design time
 * 
 * @author Exilant Technologies
 * 
 */
public class Page {
	/**
	 * we use a temp variable name called ele in the generated java script
	 */
	static final String JS_VAR_NAME = "ele";
	static final String NOT_A_TABLE = " ";

	/**
	 * field names associated with the page
	 */
	private static final String[] FIELD_NAMES = { "minParametersToFireOnLoad",
			"minParameters", "defaultButtonName", "onLoadActionNames",
			"onModifyModeActionNames", "pageMode", "buttonsToEnable",
			"buttonsToDisable", "pageWidth", "pageHeight", "popupWidth",
			"popupHeight", "popupTop", "popupLeft",
			"fieldsToDisableOnModifyMode", "buttonsToHideForPicker",
			"trackFieldChanges", "reloadActionName",
			"validateOnlyOnUserChange", "breadCrumpTitle",
			"formValidationFunction", "hasChartFields", "hotkeyFunction",
			"fieldsToHideOnLoad", "doNotResizeTables",
			"onFormChangeActionName", "onFormResetActionName",
			"enableRichTextOnAreas", "firstFieldName", "title" };

	/**
	 * name of the page excluding its extension (html). This has to match the
	 * file name in which this .xml is stored. TODO: check this and generate
	 * error.
	 */
	String name = null;

	/**
	 * title to be rendered in a page. the page layout text has a specific place
	 * holder to render the title. This may also be used to put the title in the
	 * parent/index page
	 */
	String title = null;

	/**
	 * deprecated. we do not use bread crumbs anymore.
	 */
	String breadCrumpTitle = null;

	/**
	 * intended to be used for preparing a set of page types in a project so
	 * that a lot of standardization can be achieved. Not used yet by any
	 * project, and hence not yet implemented.
	 */
	String type = null;
	/**
	 * services to be called on load of page, It could be comma separated if you
	 * have more than one
	 */
	String[] onLoadActionNames = null;
	/**
	 * Action to take if the page is in edit/modify mode based on the page
	 * parameters
	 */
	String[] onModifyModeActionNames = null;

	/**
	 * if the page is in modify mode, do we disable some fields? Typically the
	 * key field would
	 */
	String[] fieldsToDisableOnModifyMode = null;

	/**
	 * it is possible that we have two page parameters, and we do something if
	 * at least one of them is given
	 */
	int minParametersToFireOnLoad = 0;
	/**
	 * do you insist on the caller passing a minimum number of parameters for
	 * you to do anything useful in this page?
	 */
	int minParameters = 0;

	/**
	 * not a good idea to specify dimensions here, but this facility exists for
	 * downward compatibility sake
	 */
	int width = 0;
	/**
	 * not a good idea to specify dimensions here, but this facility exists for
	 * downward compatibility sake
	 */
	int height = 0;

	/**
	 * in case this page is opened in pop-up mode
	 */
	int popupWidth = 0;
	/**
	 * in case this page is opened in pop-up mode
	 */
	int popupHeight = 0;
	/**
	 * in case this page is opened in pop-up mode
	 */
	int popupLeft = 0;
	/**
	 * in case this page is opened in pop-up mode
	 */
	int popupTop = 0;
	/**
	 * script files to be included. name should be relative to this page.
	 */
	String[] scriptsToInclude = null;

	/**
	 * used by generator to follow a folder naming convention of
	 * moduleName.pageName
	 */
	String module = null;

	/**
	 * for documentation
	 */
	String description = null;

	/**
	 * if this page is used as a code picker, some buttons may have to be
	 * hidden. Useful if you use the same page for regular use, as well as as
	 * code-picker
	 */
	String[] buttonsToHideForPicker = null;

	/**
	 * list of page parameters
	 */
	PageParameter[] pageParameters = null;
	/**
	 * this is actually an array of panels, but theoretically form can be used..
	 * All the panels that this page consist of. It is a good practice to use
	 * just one panel as the outer most panel to get all the flexibility to
	 * apply styles
	 */
	AbstractElement[] panels = new AbstractElement[0];

	/**
	 * actions defined for this page
	 */
	AbstractAction[] actions = new AbstractAction[0];

	/**
	 * This is not a valid parameter any more, we have retained this to detect
	 * and raise error if this is still used.
	 */
	String onLoadActionName = null;

	/**
	 * action to be executed when the page gets reloaded as part of navigation
	 * actions
	 */
	String reloadActionName = null;

	/**
	 * some projects had difficulty with their data, and they lived with invalid
	 * data on the server. This lead to our validation triggering error even
	 * when user did not change anything. They also wanted the fields to be
	 * valid if user touched them. This is the trick attribute we introduced to
	 * solve the issue. Should not be used as a feature.
	 */
	boolean validateOnlyOnUserChange = false;

	/**
	 * is this a page that user has to save before getting out? For example, a
	 * search page is not saved, but a edit/new page is. Once set to true,
	 * exilty tracks changes to fields and warns the user in case of a request
	 * to get out without saving the changes.
	 */
	boolean trackFieldChanges = false;

	/**
	 * additional validation after exility succeeds in validating all the fields
	 */
	String formValidationFunction = null;

	/**
	 * internally tracked for existence of chart fields
	 */
	boolean hasChartFields = false;

	/**
	 * handle alt+key. Not used any more after some one made a half-hearted
	 * attempt.
	 */
	String hotkeyFunction = null;

	/**
	 * deprecated. script should not be included in the page.xml. it MUST be out
	 * into a separate file.
	 */
	String script = null;
	/**
	 * deprecated. css style should not be included in the page.xml. it MUST be
	 * out into a separate file.
	 */
	String style = null;

	/**
	 * label name name to be used for all global fields that use custom label.
	 * refer to input field attributes
	 */
	String customLabelName = null;

	/**
	 * 
	 */
	boolean doNotResizeTables = false;

	/**
	 * First field that user would start editing
	 */
	String firstFieldName = null;

	/**
	 * this is set for the specific version of the project (installation
	 * specific) for labels to be customized
	 */
	static String customLabelKey = null;

	/**
	 * internally used during page generation to track actions that are used vs
	 * defined. action names referred by elements. This is built to check for
	 * referential
	 */
	private HashMap<String, String> referredActions = null;

	/*
	 * various collections built during page generation to track any possibel
	 * error
	 */
	private HashMap<String, String> allFieldNames = null;
	private HashMap<String, Set<String>> duplicateFields = null;
	private Set<String> allNonFieldNames = null;
	private Set<String> allTableNames = null;
	private HashMap<String, AbstractPanel> allPanels = null;
	private ArrayList<String> buttonsToEnableOnFormChange = null;
	private ArrayList<String> buttonsToDisableOnFormChange = null;

	String pageMode = null;
	// fields that are duplicate, but across tables. e.g. id is used in more
	// than one tables.

	String[] buttonsToEnable = null;
	String[] buttonsToDisable = null;
	int pageWidth = 0;
	int pageHeight = 0;

	String[] fieldsToHideOnLoad = null;
	String enableRichTextOnAreas = null;
	boolean hasPageSpecificCSS = false;
	String onFormChangeActionName = null;
	String onFormResetActionName = null;

	/***
	 * Is the htm hand crafted and you want to use Exility run time only? In
	 * that case, you should have ensured that the htm has the right structure
	 * as per exility requirements, and the ids are set properly Generator will
	 * generate .js, and not .htm
	 */
	boolean generateOnlyMetaData = false;

	/**
	 * a refinement over generateOnlyMetaData. You can hand-code the page
	 * layout, up to panels, and ask Exility to provide meat under the panels.
	 * In this approach, we assume that you have designed the over-all layout,
	 * and provide a tag <panel-name/> as a place holder for exility to put the
	 * relevant generated html In the template we look for <panelName/> Note
	 * that we have chosen to use the panel name as tag-name. This tag will be
	 * replaced with the generated html for the panel.
	 */
	String htmlFileName = null;

	/**
	 * track the language to which this page got translated, just in case next
	 * programmer forgets and tries to re-use the object instance
	 */
	private String translatedLanguage = null;

	/**
	 * over-ride page layout type of the project that is defined in
	 * applicationParameters.xml
	 */
	String pageLayoutType;

	/**
	 * default
	 */
	public Page() {
	}

	/**
	 * append html text to the buffer
	 * 
	 * @param html
	 * @param pageContext
	 */
	public void toHtml(StringBuilder html, PageGeneratorContext pageContext) {
		// temporary code to check if people have migrated to new syntax
		if (this.onLoadActionName != null) {
			String err = "ERROR: You are to use onLoadActionNames and not the singular onLoadActionName. Please change your page xml";
			Spit.out(err);
			pageContext.reportError(err);
			if (this.onLoadActionNames == null) {
				this.onLoadActionNames = new String[1];
				this.onLoadActionNames[0] = this.onLoadActionName;
			}
		}
		if (this.actions.length > 1) {
			this.checkForDuplicateActions(pageContext);
		}

		html.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		html.append(
				"\n<html xmlns=\"http://www.w3.org/1999/xhtml\" ><head><title>")
				.append(this.title).append("</title>");
		// a meta that helps in opening the generated html in vs for any
		// debugging purposes..
		html.append("\n<meta content='text/html; charset=UTF-8' http-equiv='Content-Type'/>");
		if (AP.httpNoCacheTagRequires) {
			html.append("\n<meta http-equiv=\"Cache-Control\" content=\"no-cache\"/>");
		}

		// common css files
		html.append("\n<link rel=\"stylesheet\" href=\"")
				.append(AP.commonFolderPrefix)
				.append("default.css\" type=\"text/css\" />");

		if (this.hasPageSpecificCSS) {
			html.append("\n<link rel=\"stylesheet\" href=\"").append(this.name)
					.append(".css\" type=\"text/css\" />");
		}

		// exility loader
		html.append("\n<script type=\"text/javascript\" src=\"")
				.append(AP.exilityFolderPrefix)
				.append("exilityLoader.js\"></script>");
		if (this.hasChartFields) {
			html.append(
					"\n<script language=\"javascript\" type=\"text/javascript\" src=\"")
					.append(AP.exilityFolderPrefix)
					.append("flotr/lib/excanvas.js\"></script>");
			html.append(
					"\n<script language=\"javascript\" type=\"text/javascript\" src=\"")
					.append(AP.exilityFolderPrefix)
					.append("flotr/lib/prototype-1.6.0.2.js\"></script>");
			html.append(
					"\n<script language=\"javascript\" type=\"text/javascript\" src=\"")
					.append(AP.exilityFolderPrefix)
					.append("flotr/flotr-0.1.0alpha.js\"></script>");
			html.append(
					"\n<script language=\"javascript\" type=\"text/javascript\" src=\"")
					.append(AP.exilityFolderPrefix)
					.append("chartutil.js\"></script>");
		}
		if (this.enableRichTextOnAreas != null) {
			html.append(
					"\n<script language=\"javascript\" type=\"text/javascript\" src=\"")
					.append(AP.exilityFolderPrefix)
					.append("../tiny_mce/tiny_mce.js\"></script>");
			html.append(
					"\n<script language=\"javascript\" type=\"text/javascript\" src=\"")
					.append(AP.exilityFolderPrefix)
					.append("../tiny_mce/config.js\"></script>");
		}

		// add include files. First one is the metadata file itself..
		this.addScriptFile(html, this.name + ".metadata.js");
		if (this.scriptsToInclude != null) {
			for (String includeFile : this.scriptsToInclude) {
				if (includeFile.endsWith(".css")) {
					this.addStyleFile(html, includeFile);
				} else {
					this.addScriptFile(html, includeFile);
				}
			}
		}

		// Scripts within page.xml - Naveen Exility BA (Start) : Aravinda
		if (this.style != null) {
			html.append("\n<style type=\"text/css\">");
			html.append(this.style);
			html.append("\n</style>");
		}
		if (this.script != null) {
			html.append("\n<script language=\"javascript\" type=\"text/javascript\">");
			html.append(this.script);
			html.append("\n</script>");
		}

		html.append("\n</head>");

		// start body.
		String layoutType = pageContext.getLayoutType();
		html.append("\n<body onload=\"exilPageLoad();");
		if ((layoutType.equals("3")) || (layoutType.equals("5"))) {
			html.append(" centerAlign();\" class=\"wmsbody\" ");
		} else {
			html.append("\" ");
		}
		html.append("  style=\"display:none;\" onunload=\"exilPageUnload();\" onscroll=\"adjustFrameHeightToBodyHeight();\" >");
		html.append("\n<form id=\"form1\" autocomplete=\"off\" onsubmit=\"return false;\" action=\"\" accept-charset=\"UTF-8\" >");

		if (this.title != null) {
			html.append("<fieldset>");
			html.append("<legend><span id=\"pageheader\" class=\"pageheader\">")
					.append(this.title).append("</span></legend>");
		}
		// I think this table is not necessary. It is just putting 10px left
		// margin and right margin.
		// This can be achived with css..

		html.append("<table id=\"pageTable\" ");

		if (AP.alignPanels) {
			html.append(" width=\"100%\" ");
		}

		if (layoutType.equals("3")) {
			html.append(" class=\"wmspage\" ><tr><td style=\"width:10px;\"></td><td>");
		} else if (layoutType.equals("5")) {
			html.append(" class=\"wmspage\" ><tr><td>");
		} else {
			html.append(" ><tr><td style=\"width:10px;\"></td><td>");
		}

		if (this.panels == null) {
			html.append("There are no panels defined for this page <br/>");
		} else {
			for (AbstractElement ele : this.panels) {
				ele.toHtml(html, pageContext);
				if (ele.inError) {
					pageContext.reportError("");
				}
			}
		}
		if (AP.pageLayoutType.equals("5")) {
			html.append("</td></tr></table>");
		} else {
			html.append("</td><td style=\"width:10px;\"></td></tr></table>");
		}
		if (this.title != null) {
			html.append("</fieldset>");
		}
		html.append("</form></body></html>");

		if (pageContext.fieldsToHideOnPageLoad.size() > 0) {
			this.fieldsToHideOnLoad = pageContext.fieldsToHideOnPageLoad
					.toArray(new String[0]);
		}

		return;
	}

	/**
	 * append javascript to the buffer
	 * 
	 * @param js
	 *            buffer
	 * @param pageContext
	 */
	public void toJavaScript(StringBuilder js, PageGeneratorContext pageContext) {
		// collections that help in detecting inconsistency in the page
		this.referredActions = new HashMap<String, String>();
		this.allFieldNames = new HashMap<String, String>();
		this.duplicateFields = new HashMap<String, Set<String>>();
		this.allNonFieldNames = new HashSet<String>();
		this.allTableNames = new HashSet<String>();
		this.allPanels = new HashMap<String, AbstractPanel>();

		// get special buttons
		this.buttonsToEnableOnFormChange = new ArrayList<String>();
		this.buttonsToDisableOnFormChange = new ArrayList<String>();

		// some of these are required in pageContext as well
		pageContext.allFieldNames = this.allFieldNames;
		pageContext.duplicateFields = this.duplicateFields;
		pageContext.allNonFieldNames = this.allNonFieldNames;
		pageContext.allTableNames = this.allTableNames;

		// populate all these collections
		this.collectAllNames(pageContext);

		// is this a dual-mode page? In which case we have to create a hidden
		// field
		if (this.isDualMode()) {
			this.pageMode = "modify";
		}

		js.append("\n var " + Page.JS_VAR_NAME + ";");
		js.append("\nvar P2 = new PM.ExilityPage(window, '").append(this.name)
				.append("');");

		if ((this.breadCrumpTitle == null) || this.breadCrumpTitle.equals("")) {
			this.breadCrumpTitle = this.name;
		}

		// are there special buttons?
		if (this.buttonsToEnableOnFormChange.size() > 0) {
			this.buttonsToEnable = this.buttonsToEnableOnFormChange
					.toArray(new String[0]);
		}

		if (this.buttonsToDisableOnFormChange.size() > 0) {
			this.buttonsToDisable = this.buttonsToDisableOnFormChange
					.toArray(new String[0]);
		}

		pageContext.setAttributes(this, "P2", js, Page.FIELD_NAMES);

		if (this.pageParameters != null) {
			js.append("\n/*Page parameters */");
			for (PageParameter pp : this.pageParameters) {
				if (pp.inError) {
					pageContext.reportError("");
				}
				pp.toJavaScript(js, pageContext);
				js.append("\nP2.addParameter(").append(Page.JS_VAR_NAME)
						.append(");");
				js.append("\n");
			}
		}

		if (this.panels != null) {
			for (AbstractElement panel : this.panels) {
				if (panel == null) {
					continue;
				}
				if (panel instanceof AbstractPanel) {
					((AbstractPanel) panel).toJs(js, pageContext);
				} else {
					Spit.out("Page "
							+ this.name
							+ " has a "
							+ panel.getClass().getSimpleName()
							+ " as its panel. Note that elements must be wrapped in a panel.");
				}
			}
		}

		ArrayList<String> unusedAction = new ArrayList<String>();
		if (this.actions != null) {

			for (AbstractAction action : this.actions) {
				if (this.referredActions.containsKey(action.name) == false) {
					unusedAction.add(action.name);
				} else {
					pageContext.setTableName(
							this.referredActions.get(action.name), false);
					this.referredActions.remove(action.name);
				}
				// are the hide/show panels defined??
				if (action.hidePanels != null) {
					this.checkPanelNames(action.hidePanels, pageContext);
				}
				if (action.showPanels != null) {
					this.checkPanelNames(action.showPanels, pageContext);
				}

				action.toJavaScript(js, pageContext);
				// is this a special action?
				if (action instanceof ServerAction) {
					ServerAction sa = (ServerAction) action;
					if (sa.submitFields != null) {
						this.outputFieldsToSubmit(js, sa.name, sa.submitFields);
					}
				}
				pageContext.resetTableName();
			}
		}
		// any unused collections?
		if (unusedAction.size() > 0) {
			Spit.out("Warning: Following action"
					+ (unusedAction.size() == 1 ? " is" : "s are")
					+ " defined but not used. Ignore this if actions are referred in your java script");
			for (String aname : unusedAction) {
				Spit.out("\t" + aname);
			}
		}
		// are there any actions still left in collection? in that case, it is
		// an undeclared action
		if (this.referredActions.size() > 0) {
			String str = "ERROR : Following ";
			if (this.referredActions.size() > 1) {
				str += " actions are ";
			} else {
				str += " action is ";
			}
			str += "used in the page but not declared.";
			for (String rname : this.referredActions.keySet()) {
				str += "\n\t" + rname;
			}
			pageContext.reportError(str);
		}

		// release memory used by cross reference checking collections
		this.allFieldNames = null;
		this.allNonFieldNames = null;
		this.allTableNames = null;
		this.duplicateFields = null;
		this.referredActions = null;
	}

	/*
	 * Designers can use submitFields="a,b,c" where a,b etc could be fields or
	 * panel names. Client side code expect a collection of fields and tables.
	 * Convert submitFields to two collections
	 */
	private void outputFieldsToSubmit(StringBuilder js, String actionName,
			String fields) {
		// action class would have produced two declarations like
		// <name>filedsToSubmit = new Object();
		// <name>tablesToSubmit = new Object();
		// we have to add fields/tables to them
		String fieldPrefix = "\n" + actionName + "fieldsToSubmit['";
		String suffix = "'] = true;";
		String tablePrefix = "\n" + actionName + "tablesToSubmit['";
		String[] entities = fields.split(",");
		for (String ntt : entities) {
			ntt = ntt.trim();// Fix Bug-0093: escaping white spaces
			if (this.allPanels.containsKey(ntt)) {
				AbstractPanel panel = this.allPanels.get(ntt);
				String prefix = "";
				if (panel.tableName != null) {
					js.append(tablePrefix).append(panel.tableName)
							.append(suffix);
					prefix = panel.tableName + "_";
				}
				for (AbstractElement ele : panel.elements) {
					if (ele instanceof AbstractField == false) {
						continue;
					}
					AbstractField field = (AbstractField) ele;
					js.append(fieldPrefix).append(prefix).append(field.name)
							.append(suffix);
				}
			} else if (this.allTableNames.contains(ntt)) {
				js.append(tablePrefix).append(ntt).append(suffix);
			} else {
				js.append(fieldPrefix).append(ntt).append(suffix);
			}
		}
	}

	/*
	 * check whether the designer has used any features associated with
	 * pageMode.
	 */
	private boolean isDualMode() {
		if (this.onModifyModeActionNames != null
				|| this.fieldsToDisableOnModifyMode != null) {
			return true;
		}
		if (this.pageParameters == null) {
			return false;
		}
		for (PageParameter p : this.pageParameters) {
			if (p.isPrimaryKey) {
				return true;
			}
		}
		return false;
	}

	private void addScriptFile(StringBuilder sbf, String fileName) {
		sbf.append("\n<script type=\"text/javascript\" src=\"")
				.append(fileName).append("\" ></script> ");
	}

	private void addStyleFile(StringBuilder sbf, String fileName) {
		sbf.append("\n<link rel=\"stylesheet\" type=\"text/css\" href=\"")
				.append(fileName).append("\" />");
	}

	/*
	 * routines to check for some errors in the design of the page
	 */
	private void checkPanelNames(String[] names, PageGeneratorContext pc) {
		for (String panelName : names) {
			if (this.allNonFieldNames.contains(panelName)) {
				continue;
			}
			if (this.allFieldNames.containsKey(panelName)) {
				continue;
			}
			String err = "ERROR : Panel "
					+ panelName
					+ " is referred but it is not defined anywhere in the page.";
			pc.reportError(err);
		}
	}

	// actions and panels make references to actions and fields. Go thru them
	// and fill the collections
	// Algo is sligtly 'twisted'. We collect references to actions, and
	// definitions of fields.
	// That is because we already have a collection of all actions, but we do
	// not have a collection of all fields.
	// During toJs() method, reference to fields are checked against the
	// allFieldNames() collections.
	private void collectAllNames(PageGeneratorContext pc) {
		String tableName = Page.NOT_A_TABLE;
		for (AbstractElement panel : this.panels) {
			this.collectNames(panel, tableName, pc);
		}
		// now verify whether all references are resolvable
		// buttons being referred
		this.verifyElementReference(this.buttonsToEnableOnFormChange
				.toArray(new String[this.buttonsToEnableOnFormChange.size()]),
				pc);
		this.verifyElementReference(this.buttonsToDisableOnFormChange
				.toArray(new String[this.buttonsToDisableOnFormChange.size()]),
				pc);
		this.verifyElementReference(this.buttonsToHideForPicker, pc);
		this.verifyElementReference(this.fieldsToDisableOnModifyMode, pc);

		// check for referenced actions
		if (this.onFormChangeActionName != null) {
			this.checkAndAddAction(this.onFormChangeActionName, tableName, pc);
		}

		if (this.onFormResetActionName != null) {
			this.checkAndAddAction(this.onFormResetActionName, tableName, pc);
		}

		if (this.onLoadActionNames != null) {
			for (String actionName : this.onLoadActionNames) {
				this.checkAndAddAction(actionName, tableName, pc);
			}
		}

		if (this.onModifyModeActionNames != null) {
			for (String actionName : this.onModifyModeActionNames) {
				this.checkAndAddAction(actionName, tableName, pc);
			}
		}

		if (this.reloadActionName != null) {
			this.checkAndAddAction(this.reloadActionName, tableName, pc);
		}

		// only serverAction makes reference to other actions
		for (AbstractAction action : this.actions) {
			if (action instanceof ServerAction) {
				ServerAction se = (ServerAction) action;
				if (se.callBackActionName != null) {
					this.checkAndAddAction(se.callBackActionName, tableName, pc);
				}
			}
		}
	}

	private void verifyElementReference(String[] names, PageGeneratorContext pc) {
		if (names == null) {
			return;
		}
		for (String ename : names) {
			if (this.allNonFieldNames.contains(ename)) {
				continue;
			}
			if (this.allFieldNames.containsKey(ename)) {
				continue;
			}
			String msg = ename + " is referred but it is not defined.";
			pc.reportError(msg);
		}
	}

	private void collectNames(AbstractElement ele, String tableName,
			PageGeneratorContext pc) {
		// is it a panel?
		if (ele instanceof AbstractPanel) {
			AbstractPanel panel = (AbstractPanel) ele;
			if (ele.name != null) {
				this.checkAndAddElementNames(ele.name, pc);
				this.allPanels.put(ele.name, panel);
			}
			String tName = tableName;
			// panels with associated table can not be nested
			if (panel.tableName != null) {
				if (!tableName.equals(Page.NOT_A_TABLE)) {
					String err = "ERROR: panel " + panel.name
							+ " has a table with name " + panel.tableName
							+ " but this panel is already inside a table "
							+ tableName;
					pc.reportError(err);
				} else {
					tName = panel.tableName;
					// is this table already used by another panel?
					if (this.allTableNames.contains(tName)) {
						String err = "ERROR: panel "
								+ panel.name
								+ " uses table "
								+ tName
								+ ". This is already used by another panel. Two panels can not use the same table name";
						pc.reportError(err);
					}

					this.allTableNames.add(tName);
				}
			}

			if (ele.onClickActionName != null) {
				this.checkAndAddAction(ele.onClickActionName, tName, pc);
			}
			if (ele instanceof ListPanel) {
				String an = ((ListPanel) ele).onDblClickActionName;
				if (an != null) {
					this.checkAndAddAction(an, tName, pc);
				}
			}

			// those were the references at the panel level. Check its child
			// elements
			if (panel.elements != null) {
				for (AbstractElement childEle : panel.elements) {

					if (childEle == null) {
						continue;
					}
					this.collectNames(childEle, tName, pc);
				}
			}
			return;
		}
		// it is not a panel. SO, it is either a field or an element
		if (ele.onClickActionName != null) {
			this.checkAndAddAction(ele.onClickActionName, tableName, pc);
		}

		if (ele instanceof AbstractField) {
			this.checkAndAddFieldName(ele.name, tableName, pc);
			if (ele instanceof AbstractInputField) {
				AbstractInputField field = (AbstractInputField) ele;
				if (field.onChangeActionName != null) {
					this.checkAndAddAction(field.onChangeActionName, tableName,
							pc);
				}
				if (field.onUserChangeActionName != null) {
					this.checkAndAddAction(field.onUserChangeActionName,
							tableName, pc);
				}
				if (ele instanceof FilterField) {
					this.checkAndAddFieldName(field.name + "Operator",
							tableName, pc);
					DataValueType vt = field.getValueType();
					if (vt == DataValueType.DATE
							|| vt == DataValueType.INTEGRAL
							|| vt == DataValueType.DECIMAL) {
						this.checkAndAddFieldName(field.name + "To", tableName,
								pc);
					}
				}
			}
		} else if (ele.name != null) {
			this.checkAndAddElementNames(ele.name, pc);
			// is this a special buttonElement
			if (ele instanceof ButtonElement) {
				ButtonElement be = (ButtonElement) ele;
				if (be.whatToDoOnFormChange != WhatToDoOnFormChange.LEAVEMEALONE) {
					if (be.name == null) {
						String err = "ERROR: Button element that needs to be enabled/disabled has to be assignd a name";
						pc.reportError(err);
						be.name = "buttonAssignedName"
								+ (this.buttonsToDisableOnFormChange.size() + this.buttonsToEnableOnFormChange
										.size());
					}
					if (be.whatToDoOnFormChange == WhatToDoOnFormChange.DISABLE) {
						this.buttonsToDisableOnFormChange.add(be.name);
					} else {
						this.buttonsToEnableOnFormChange.add(be.name);
					}
				}
			}
		}
	}

	private void checkForDuplicateActions(PageGeneratorContext pc) {
		if (this.actions == null) {
			return;
		}
		int n = this.actions.length;
		int m = n - 1;
		for (int i = 0; i < m; i++) {
			String actionName = this.actions[i].name;
			for (int j = i + 1; j < n; j++) {
				if (actionName.equals(this.actions[j].name)) {
					String err = "ERROR: Action name " + actionName
							+ " is defined more than once.";
					pc.reportError(err);
				}
			}
		}
	}

	private void checkAndAddElementNames(String ename, PageGeneratorContext pc) {
		if (this.allNonFieldNames.contains(ename)
				|| this.allFieldNames.containsKey(ename)) {
			String err = "ERROR :" + ename
					+ " is used as name for more than one element.";
			pc.reportError(err);
			return;
		}
		this.allNonFieldNames.add(ename);
	}

	void checkAndAddAction(String actionName, String tableName,
			PageGeneratorContext pc) {
		if (this.referredActions.containsKey(actionName) == false) {
			this.referredActions.put(actionName, tableName);
			return;
		}
		String tn = this.referredActions.get(actionName);
		// if table context is same, we do not have any problem.
		if (tn.equals(tableName)) {
			return;
		}
		// Only serverAction and naviagation actions, with queryields are at
		// risk
		AbstractAction action = null;
		for (AbstractAction a : this.actions) {
			if (a.name.equals(actionName)) {
				action = a;
				break;
			}
		}
		if (action == null) {
			return;
		}

		if ((action.enableFields != null && action.enableFields.length > 0)
				|| (action.disableFields != null && action.disableFields.length > 0)) {
			// we have to show error...
		} else if (action instanceof ServerAction) {
			ServerAction sa = (ServerAction) action;
			if ((sa.queryFieldNames == null || sa.queryFieldNames.length == 0)
					&& (sa.submitFields == null || sa.submitFields.length() == 0)) {
				return;
			}
		} else if (action instanceof NavigationAction) {
			NavigationAction na = (NavigationAction) action;
			if (na.queryFieldNames == null || na.queryFieldNames.length == 0) {
				return;
			}
		} else {
			return;
		}
		String err = "ERROR : Action " + actionName
				+ " is used in two different table contexts :";
		if (tn.equals(Page.NOT_A_TABLE)) {
			err += " inside table " + tn;
		} else {
			err += " outside of any table ";
		}
		if (tableName == null || tableName.equals(Page.NOT_A_TABLE)) {
			err += " and outside of any table";
		} else {
			err += " and inside table  " + tableName;
		}
		err += ".\nAny of the fields you have referred to in this action may lead to scope issue. You can resolve this issue by creating a copy of this action to be used inside the table.";

		pc.reportError(err);
	}

	void checkAndAddFieldName(String fieldName, String tableName,
			PageGeneratorContext pc) {
		if (this.allNonFieldNames.contains(fieldName)) {
			String err = "Field name " + fieldName
					+ " is used as a name for a field as well as an element";
			pc.reportError(err);
			return;
		}

		if (this.allFieldNames.containsKey(fieldName) == false) {
			this.allFieldNames.put(fieldName, tableName);
		} else {
			/*
			 * this field name is duplicate. But that itself is not a problem,
			 * if it is defined across tables
			 */
			Set<String> tables = null;
			// Is it already in duplicates collections?
			if (this.duplicateFields.containsKey(fieldName) == false) {
				tables = new HashSet<String>();
				/*
				 * add existing table first
				 */
				tables.add(this.allFieldNames.get(fieldName));
				this.duplicateFields.put(fieldName, tables);
			}
			tables = this.duplicateFields.get(fieldName);
			if (tables.contains(tableName)) {
				/*
				 * it is a duplicate within the same context
				 */
				String err = "ERROR : Field name " + fieldName
						+ " is dublicate";
				if (!tableName.equals(Page.NOT_A_TABLE)) {
					err += " inside table " + tableName;
				}

				pc.reportError(err);
			}
			tables.add(tableName);
		}
		// if this field is inside a table, then we shoudl also have the fully
		// qualified name in the name collection
		// Note that the fully qualified name is asociated with NOT_A_TABLE to
		// indicate that there is no more table qualification required
		if (tableName.equals(Page.NOT_A_TABLE) == false) {
			this.allFieldNames.put(tableName + "_" + fieldName,
					Page.NOT_A_TABLE);
		}
	}

	private void replaceIncludePanel(AbstractElement[] eles) {
		for (int i = 0; i < eles.length; i++) {
			AbstractElement ele = eles[i];
			if (ele == null) // possible loading error
			{
				ele = new TextElement();
				ele.label = "Panel/Field was not parsed properly. Please review your page.xml";
				eles[i] = ele;
				continue;

			}
			if (ele instanceof AbstractPanel == false) {
				continue;
			}
			if (ele instanceof IncludePanel) {
				IncludePanel ip = (IncludePanel) ele;
				Spit.out("found an include panel = " + ip.name
						+ ". Name of Panel.xml to be included is "
						+ ip.panelNameToBeIncluded);
				AbstractPanel newPanel = Pages
						.getPanel(ip.panelNameToBeIncluded);
				if (newPanel == null) {
					AbstractElement te = new TextElement();
					te.label = "ERROR: Include panel "
							+ ip.panelNameToBeIncluded
							+ " could not be loaded. Please look at the trace and take corrective action.";
					eles[i] = te;
				} else {
					newPanel.name = ele.name;
					eles[i] = newPanel;
				}
			} else {
				AbstractPanel panel = (AbstractPanel) ele;
				if (panel.elements != null && panel.elements.length > 0) {
					this.replaceIncludePanel(panel.elements);
				}
			}
		}
	}

	/**
	 * page is initialized top-down with the context for it to accumulate
	 * warnings and errors
	 * 
	 * @param context
	 */
	public void initialize(PageGeneratorContext context) {
		// check for an includePanel and replace it
		if (this.panels != null) {
			this.replaceIncludePanel(this.panels);
		}
		this.pageHeight = (this.height > 0) ? this.height
				: AP.defaultPageHeight;
		this.pageWidth = (this.width > 0) ? this.width : AP.defaultPageWidth;
		Page.customLabelKey = this.customLabelName;

		/*
		 * page elements are not initialized as they are loaded. This is to take
		 * care of records. Let us initialize them top-down now.
		 */
		for (AbstractElement ele : this.panels) {
			ele.initialize(context);
		}
	}

	/**
	 * generate .htm and .meta for a given language
	 * 
	 * @param generateEvenOnerror
	 *            should happen only during some test..
	 * @param language
	 *            optional. English if this is left null.
	 * @param errorMessages
	 * @return generated file name if page was generated and saved successfully,
	 *         null otherwise
	 */
	public String generateAndSavePage(boolean generateEvenOnerror,
			String language, List<String> errorMessages) {
		String layoutType = this.pageLayoutType;
		if (layoutType == null) {
			layoutType = AP.pageLayoutType;
		}
		PageGeneratorContext pageContext = new PageGeneratorContext(this.name,
				layoutType);
		this.initialize(pageContext);
		if (language != null
				&& language.equals(Dubhashi.DEFAULT_LANGUAGE) == false) {
			if (this.translatedLanguage == null) {
				Spit.out("Going to translate page to " + language
						+ " before generating.");
				this.translate(language);
			} else {
				if (this.translatedLanguage.equals(language)) {
					Spit.out("Page is already translated to " + language
							+ ". proceeding to page generation.");
				} else {
					errorMessages.add("Page is already translated to "
							+ this.translatedLanguage
							+ ". Can not generate page in " + language);
					return null;
				}

			}
		}

		String html = null;
		if (this.htmlFileName != null) {
			html = this.templateToHtml(pageContext);
		} else if (layoutType.equals("css")) {
			html = this.toHtml5(pageContext);
		} else {
			StringBuilder sbf = new StringBuilder();
			this.toHtml(sbf, pageContext);
			html = sbf.toString();
		}

		StringBuilder js = new StringBuilder();
		this.toJavaScript(js, pageContext);
		String path = AP.htmlRootRelativeToResourcePath;
		if (path == null || path.length() == 0) {
			pageContext
					.reportError("htmlRootRelativeToResourcePath is not set in your project. Page generation will not work.");
		}
		boolean errorFound = pageContext.dumpErrors(errorMessages);
		if (errorFound && generateEvenOnerror == false) {
			return null;
		}

		path = ResourceManager.getResourceFolder() + path;
		if (path.endsWith("/") == false) {
			path += "/";
		}
		// suffix language to the root folder if required.
		if (language != null && language.length() > 0
				&& language.equals(Dubhashi.DEFAULT_LANGUAGE) == false) {
			path = path.substring(0, path.length() - 1) + '-' + language + '/';
		}
		if (this.module != null) {
			path += this.module.replace('.', '/');
		}

		path += '/' + this.name;
		if (this.generateOnlyMetaData) {
			Spit.out("Html is not generated as generateOnlyMetaData is set to true in your page.xml");
		} else {
			ResourceManager.saveText(path + ".htm", html);
		}
		ResourceManager.saveText(path + ".metadata.js", js.toString());
		return path;
	}

	/***
	 * second generation generator that tries to restrict itself to dom
	 * structure, leaving view attributes to css
	 * 
	 * @param pageContext
	 *            page context
	 * @return html text
	 */
	public String toHtml5(PageGeneratorContext pageContext) {
		String err;

		String initialHtml = ResourceManager.readFile(ResourceManager
				.getResourceFolder() + "htmlTemplate.txt");
		if (initialHtml == null) {
			err = "unable to read htmlTemplate.txt from resource folder.";
			pageContext.reportError(err);
			return "";
		}
		int scriptAt = initialHtml.indexOf("@script@");
		int titleAt = initialHtml.indexOf("@title@");
		int panelsAt = initialHtml.indexOf("@panels@");
		if (scriptAt == -1 || panelsAt == -1) {
			err = "htmlTemplate.txt is not valid. Ensure that you have @script@ and @panels@ at right place in the template.";
			pageContext.reportError(err);
			return "";
		}

		// temporary code to check if people have migrated to new syntax
		if (this.onLoadActionName != null) {
			err = "ERROR: You are to use onLoadActionNames and not the singular onLoadActionName. Please change your page xml";
			pageContext.reportError(err);
			if (this.onLoadActionNames == null) {
				this.onLoadActionNames = new String[1];
				this.onLoadActionNames[0] = this.onLoadActionName;
			}
		}
		if (this.actions.length > 1) {
			this.checkForDuplicateActions(pageContext);
		}
		// OK let us start. From the beginning!
		StringBuilder html = new StringBuilder(initialHtml.substring(0,
				scriptAt));

		// insert script/cs files
		if (this.hasChartFields) {
			this.addScriptFile(html, AP.exilityFolderPrefix
					+ "flotr/lib/prototype-1.6.0.2.js");
			this.addScriptFile(html, AP.exilityFolderPrefix
					+ "flotr/flotr-0.1.0alpha.js");
		}
		this.addScriptFile(html, this.name + ".metadata.js");
		if (this.scriptsToInclude != null) {
			for (String includeFile : this.scriptsToInclude) {
				if (includeFile.endsWith(".css")) {
					this.addStyleFile(html, includeFile);
				} else {
					this.addScriptFile(html, includeFile);
				}
			}
		}

		if (titleAt > 0) {
			html.append(initialHtml.substring(scriptAt + 8, titleAt));
			html.append(this.title == null ? "" : this.title);
			html.append(initialHtml.subSequence(titleAt + 7, panelsAt));
		} else {
			html.append(initialHtml.subSequence(scriptAt + 8, panelsAt));
		}

		/**
		 * dirty design. I am unable to carry toHtml5() to its logical end at
		 * this time. Hence taking shelter under the fact that pageContext is
		 * passed everywhere
		 **/
		pageContext.useHtml5 = true;

		if (this.panels == null) {
			html.append("There are no panels defined for this page <br/>");
		} else {
			for (AbstractElement ele : this.panels) {
				if (ele != null) {
					ele.toHtml5(html, pageContext);
					if (ele.inError) {
						pageContext
								.reportError("Error while generating html for panel "
										+ ele.name);
					}
				} else {
					Spit.out("We got null as a panel in page " + this.name);
				}
			}
		}
		html.append(initialHtml.substring(panelsAt + 8));
		/**
		 * some issue with IDE's adding line-break at the end. Remove characters
		 * after </html>
		 */
		while (html.charAt(html.length() - 1) != '>') {
			html.setLength(html.length() - 1);
		}
		return html.toString();
	}

	/***
	 * second generation generator that tries to restrict itself to dom
	 * structure, leaving view attributes to css
	 * 
	 * @param pageContext
	 *            page context
	 * @return html text
	 */
	public String templateToHtml(PageGeneratorContext pageContext) {

		String err;
		String f = ResourceManager.getResourceFolder() + "page/"
				+ this.htmlFileName;
		String template = ResourceManager.readFile(f);
		if (template == null) {
			err = "You have specified htmlFileName=\""
					+ this.htmlFileName
					+ "\" for page "
					+ this.name
					+ ". As per convention, we tried reading "
					+ f
					+ " but we were got into trouble. You would have got actual file I/O related error in another part of the trace text.";
			pageContext.reportError(err);
			return "";
		}

		if (this.panels == null || this.panels.length == 0) {
			err = "No panels fround.";
			pageContext.reportError(err);
			return "";
		}

		boolean isHtml5 = false;
		if (pageContext.getLayoutType().equals("css")) {
			isHtml5 = true;
			pageContext.useHtml5 = true;
		}
		StringBuilder html = new StringBuilder(template);
		for (AbstractElement ele : this.panels) {
			if (ele.name == null) {
				err = " panels MUST be named for you to use html template.";
				pageContext.reportError(err);
				continue;
			}

			String tag = "<" + ele.name + "/>";
			int idx = html.indexOf(tag);
			if (idx == -1) {
				err = "Tag " + tag + " not found for panel " + ele.name
						+ " inside html template. ";
				pageContext.reportError(err);
				continue;
			}
			StringBuilder fragment = new StringBuilder();
			if (isHtml5) {
				ele.toHtml5(fragment, pageContext);
			} else {
				ele.toHtml(fragment, pageContext);
			}
			html.replace(idx, idx + tag.length(), fragment.toString());
		}
		return html.toString();
	}

	/**
	 * labels used across all elements. Used for providing list of translations
	 * required
	 */
	private static final String[] ALL_LABELS = { "hoverText", "footerLabe",
			"labelForBulkDeleteCheckBox", "labelForAddRowButton",
			"hoverForDeleteCheckBox", "paginationLabel", "rowHelpText",
			"blankOption", "trueValue", "falseValue", "xaxislabel",
			"yaxislabel", };
	private static final String[] ALL_LABEL_LISTS = { "xaxislabels",
			"yaxislabels" };
	private static final String[] ALL_LABEL_VALUE_LISTS = { "valueList" };

	/**
	 * return all labels used by this page. We take a simple, non-oo approach
	 * for this. We have a list of all attributes that end-up on a page across
	 * all elements. We just use reflection to get these values.
	 * 
	 * @return all labels used by this page
	 */
	public String[] getAllLabels() {

		Set<String> labels = new HashSet<String>();
		if (this.title != null) {
			labels.add(this.title);
		}
		if (this.panels != null && this.panels.length > 0) {
			for (AbstractElement ele : this.panels) {
				this.addLabelsFromElement(ele, labels);
			}
		}
		String[] arr = new String[labels.size()];
		labels.toArray(arr);
		Arrays.sort(arr);
		return arr;
	}

	/**
	 * use reflection to get any label related attribute of this element. And,
	 * if this is a panel, use recursion to traverse all child elements
	 * 
	 * @param ele
	 * @param labels
	 */
	private void addLabelsFromElement(AbstractElement ele, Set<String> labels) {
		String lbl = ele.label;
		if (lbl == null && ele instanceof AbstractField) {
			/**
			 * if it is a field, we may inherit from data element
			 */
			DataElement dataElement = ((AbstractField) ele).dataElement;
			if (dataElement != null) {
				lbl = dataElement.label;
			}
		}
		if (lbl != null) {
			labels.add(lbl);
		}
		/**
		 * others are extracted using reflection
		 */
		Class<?> klass = ele.getClass();
		for (String attribute : ALL_LABELS) {
			try {
				Field field = klass.getDeclaredField(attribute);
				field.setAccessible(true);
				Object val = field.get(ele);
				if (val != null) {
					labels.add(val.toString());
				}
			} catch (Exception e) {
				// we are OK. we just tried
			}
		}

		/**
		 * we have some attributes that have a comma separated list of values..
		 */
		for (String attribute : ALL_LABEL_LISTS) {
			try {
				Field field = klass.getDeclaredField(attribute);
				field.setAccessible(true);
				Object val = field.get(ele);
				if (val != null) {
					String[] theseLabels = val.toString().split(",");
					for (String aLabel : theseLabels) {
						labels.add(aLabel.trim());
					}
				}
			} catch (Exception e) {
				// we are OK. we just tried
			}
		}

		/**
		 * we ALSO have some attributes that have internalvalue:label; etc..
		 */
		for (String attribute : ALL_LABEL_VALUE_LISTS) {
			try {
				Field field = klass.getDeclaredField(attribute);
				field.setAccessible(true);
				Object val = field.get(ele);
				if (val != null) {
					String[] pairs = val.toString().split(
							ExilityConstants.PAIR_SEPARATOR);
					for (String pairString : pairs) {
						String[] aPair = pairString.trim().split(
								ExilityConstants.LIST_SEPARATOR);
						if (aPair.length == 2) {
							labels.add(aPair[1].trim());
						} else if (aPair.length == 2) {
							labels.add(aPair[1].trim());
						} else {
							Spit.out("Page "
									+ this.name
									+ " has an element called "
									+ ele.name
									+ ". This element has invalid value for its attribute "
									+ attribute);
						}
					}
				}
			} catch (Exception e) {
				// we are OK. we just tried
			}
		}
		/**
		 * because this is a recursive method, i would like to highlight the
		 * returns first !!
		 */
		if (ele instanceof AbstractPanel == false) {
			return;
		}

		AbstractPanel panel = (AbstractPanel) ele;
		if (panel.elements == null || panel.elements.length == 0) {
			return;
		}

		/**
		 * OK. recurse down to children
		 */
		for (AbstractElement child : ((AbstractPanel) ele).elements) {
			this.addLabelsFromElement(child, labels);
		}
	}

	/**
	 * translate all attributes that show-up on the page
	 * 
	 * @param language
	 */
	private void translate(String language) {
		Dubhashi dubhashi = Dubhashi.getDubhashi(language);
		if (this.title != null) {
			this.title = dubhashi.translateOrRetain(this.title);
		}
		if (this.panels != null && this.panels.length > 0) {
			for (AbstractElement ele : this.panels) {
				this.translateLabels(ele, dubhashi);
			}
		}
	}

	/**
	 * use reflection to get any label related attribute of this element. And,
	 * if this is a panel, use recursion to traverse all child elements
	 * 
	 * @param ele
	 * @param context
	 */
	private void translateLabels(AbstractElement ele, Dubhashi dubhashi) {

		/**
		 * translate label
		 */
		String lbl = ele.label;
		if (lbl == null && ele instanceof AbstractField) {
			/**
			 * if it is a field, we may inherit from data element
			 */
			DataElement dataElement = ((AbstractField) ele).dataElement;
			if (dataElement != null) {
				lbl = dataElement.label;
			}
		}
		if (lbl != null) {
			ele.label = dubhashi.translateOrRetain(lbl);
		}

		/**
		 * other possible attributes that may show-up on the page are fixed
		 * using reflection
		 */
		Class<?> klass = ele.getClass();
		for (String attribute : ALL_LABELS) {
			try {
				Field field = klass.getDeclaredField(attribute);
				field.setAccessible(true);
				Object val = field.get(ele);
				if (val != null) {
					val = dubhashi.translateOrRetain(val.toString());
					field.set(ele, val);
				}
			} catch (Exception e) {
				// we are OK. we just tried
			}
		}

		/**
		 * we have some attributes that have a comma separated list of values..
		 */
		for (String attribute : ALL_LABEL_LISTS) {
			try {
				Field field = klass.getDeclaredField(attribute);
				field.setAccessible(true);
				Object val = field.get(ele);
				if (val != null) {
					String[] theseLabels = val.toString().split(
							ExilityConstants.LIST_SEPARATOR);
					StringBuilder sbf = new StringBuilder();
					for (String aLabel : theseLabels) {
						sbf.append(dubhashi.translateOrRetain(aLabel)).append(
								ExilityConstants.LIST_SEPARATOR_CHAR);
					}
					sbf.deleteCharAt(sbf.length() - 1);
					field.set(ele, sbf.toString());
				}
			} catch (Exception e) {
				// we are OK. we just tried
			}
		}

		/**
		 * we ALSO have some attributes that have internalvalue:label; etc..
		 */
		for (String attribute : ALL_LABEL_VALUE_LISTS) {
			try {
				Field field = klass.getDeclaredField(attribute);
				field.setAccessible(true);
				Object val = field.get(ele);
				if (val != null) {
					StringBuilder sbf = new StringBuilder();
					String[] pairs = val.toString().split(
							ExilityConstants.PAIR_SEPARATOR);
					for (String pairString : pairs) {
						String[] aPair = pairString.trim().split(
								ExilityConstants.LIST_SEPARATOR);
						if (aPair.length == 2) {
							sbf.append(aPair[0])
									.append(ExilityConstants.LIST_SEPARATOR_CHAR)
									.append(dubhashi
											.translateOrRetain(aPair[1]))
									.append(ExilityConstants.PAIR_SEPARATOR_CHAR);
						} else if (aPair.length == 1) {
							sbf.append(aPair[0])
									.append(ExilityConstants.LIST_SEPARATOR_CHAR)
									.append(dubhashi
											.translateOrRetain(aPair[0]))
									.append(ExilityConstants.PAIR_SEPARATOR_CHAR);
						} else {
							Spit.out("Page "
									+ this.name
									+ " has an element called "
									+ ele.name
									+ ". This element as invalid value for its attribute "
									+ attribute);
						}
					}
					sbf.deleteCharAt(sbf.length() - 1);
					field.set(ele, sbf.toString());
				}
			} catch (Exception e) {
				// we are OK. we just tried
			}
		}
		/**
		 * because this is a recursive method, i would like to highlight the
		 * returns first !!
		 */
		if (ele instanceof AbstractPanel == false) {
			return;
		}

		AbstractPanel panel = (AbstractPanel) ele;
		if (panel.elements == null || panel.elements.length == 0) {
			return;
		}

		/**
		 * OK. recurse down to children
		 */
		for (AbstractElement child : ((AbstractPanel) ele).elements) {
			this.translateLabels(child, dubhashi);
		}
	}

	/**
	 * add fields that use dataElement attributes from this page to fields
	 * collection
	 * 
	 * @param fields
	 * @param filterType
	 *            refer to Pages.FIELD_TYPE_**
	 */
	public void addAllFields(Map<String, List<FieldDetails>> fields,
			int filterType) {
		this.addFieldsFromPanel(this.panels, fields, filterType);
	}

	private void addFieldsFromPanel(AbstractElement[] eles,
			Map<String, List<FieldDetails>> fieldNames, int filterType) {

		if (eles == null || eles.length == 0) {
			return;
		}

		for (AbstractElement ele : eles) {
			if (ele instanceof AbstractField) {
				this.addField((AbstractField) ele, fieldNames, filterType);
				continue;
			}
			if (ele instanceof AbstractPanel) {
				this.addFieldsFromPanel(((AbstractPanel) ele).elements,
						fieldNames, filterType);
			}
		}
	}

	@SuppressWarnings("null")
	private void addField(AbstractField field,
			Map<String, List<FieldDetails>> fieldNames, int filterType) {

		if (filterType == Pages.FIELD_FILTER_SENT_TO_SERVER) {
			/*
			 * we are interested in input field and output field that needs to
			 * be sent to server. We are doing negative check in the following
			 * code. Bear with us please.
			 */
			if (field instanceof AbstractInputField == false) {

				if (field instanceof OutputField == false
						|| ((OutputField) field).toBeSentToServer == false) {
					return;
				}
			}
		}

		String fieldType = null;
		if (field instanceof SelectionField) {
			if (((SelectionField) field).multipleSelection) {
				fieldType = "MultiSelectionField";
			} else {
				fieldType = "SelectionField";
			}
		} else {
			fieldType = field.getClass().getSimpleName();
		}
		FieldIssue issue = FieldIssue.NONE;

		/*
		 * for the field, if it is in dictionary
		 */
		DataElement dataElement = DataDictionary.getElement(field.name);
		String dataTypeName = null;
		DataValueType dataValueType = null;
		if (dataElement != null) {
			dataTypeName = dataElement.dataType;
			dataValueType = DataDictionary.getValueType(this.name);
		}

		/*
		 * for defined data element
		 */
		String dataElementName = (field.dataElementName == null || field.dataElementName
				.length() == 0) ? null : field.dataElementName;
		DataElement dataElement1 = null;
		String dataTypeName1 = null;
		DataValueType dataValueType1 = null;
		if (dataElementName != null) {
			dataElement1 = DataDictionary.getElement(dataElementName);
			if (dataElement1 != null) {
				dataTypeName1 = dataElement1.dataType;
				dataValueType1 = DataDictionary.getValueType(dataElementName);
			}
		}

		/*
		 * let us look for issues with this field. Four possible cases.
		 * 
		 * Case 1. None exist
		 */
		if (dataElement == null && dataElement1 == null) {
			issue = FieldIssue.UNKNOWN_TYPE;
			/*
			 * case 2. field not defined, but data element is defined
			 */
		} else if (dataElement == null && dataElement1 != null) {
			/*
			 * this field is not in dictionary, but data element is in
			 * dictionary
			 */
			issue = FieldIssue.NOT_IN_DICTIONARY;
			dataTypeName = dataTypeName1;
			dataValueType = dataValueType1;

			/*
			 * case 3. both are defined
			 */
		} else if (dataElement != null && dataElement1 != null) {
			/*
			 * both are defined
			 */
			if (dataTypeName.equals(dataTypeName1)) {
				issue = FieldIssue.REDUNDANT_DATA_ELEMENT;
			} else {
				if (dataValueType.equals(dataValueType1)) {
					issue = FieldIssue.COMPATIBLE_DATA_ELEMENT;
				} else {
					issue = FieldIssue.WRONG_DATA_ELEMENT;
				}
			}
		}
		/*
		 * case 4. field is defined, and we do not use data element. This is
		 * clean case
		 */

		List<FieldDetails> fieldDetails = fieldNames.get(field.name);
		if (fieldDetails == null) {
			fieldDetails = new ArrayList<FieldDetails>();
			fieldNames.put(field.name, fieldDetails);
		}

		FieldDetails newOne = new FieldDetails(this.name, field.name, issue,
				dataElementName, dataTypeName, dataValueType);
		newOne.filterBasedInfo = fieldType;
		fieldDetails.add(newOne);
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ResourceManager
				.loadAllResources(
						"d:/exilityTurtle/exilityTurtleClient/WebContent/WEB-INF/resource/",
						"d:/exilityTurtle/exilityTurtleClient/WebContent/WEB-INF/exilityResource/");
		ResourceManager
				.setRootFolder("d:/exilityTurtle//ExilityTurtleClient/WebContent/");
		Page page = Pages.getPage("test", "test");
		List<String> errorMessages = new ArrayList<String>();
		page.generateAndSavePage(false, null, errorMessages);
		System.out.print(errorMessages.size());
	}

}

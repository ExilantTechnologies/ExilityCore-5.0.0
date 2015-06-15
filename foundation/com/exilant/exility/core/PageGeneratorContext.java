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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * catch-all facility that facilitates communication across methods during page
 * generation. acts more like a global context during this process
 * 
 */
public class PageGeneratorContext {
	static final String imageFolderName = AP.imageFilePrefix;
	static String expandedImg = "\n<img alt=\"collapse\" src=\""
			+ PageGeneratorContext.imageFolderName + "expanded.gif\" /> ";
	static String collapsedImg = "\n<img alt=\"expand\" src=\""
			+ PageGeneratorContext.imageFolderName + "collapsed.gif\" /> ";
	private String tableName = null;
	// column
	private String prefix = "";
	// private int columnCount = -1;
	boolean isInsideGrid = false;
	boolean renderFieldAsColumn = false;
	private static final char BRACE_OPEN = '[';
	private static final char BRACE_CLOSE = ']';
	private static final char COMMA = ',';

	HashMap<String, String> allFieldNames = null;
	Set<String> allNonFieldNames = null;
	HashMap<String, Set<String>> duplicateFields = null;
	ArrayList<String> fieldsToHideOnPageLoad = new ArrayList<String>();
	Set<String> allTableNames = null;
	/**
	 * set from page.customLabelName and used by AbstractField.labelToHtml()
	 */
	String customLabelName = null;
	/**
	 * toHtml5() method is evolving. for the time being, I am using this flag
	 * for that sake
	 */
	boolean useHtml5 = false;
	private int lastPanelIdxUsed = 0;
	int nbrErrors = 0;
	private final List<String> errors = new ArrayList<String>();
	private final List<String> warnings = new ArrayList<String>();
	@SuppressWarnings("unused")
	/**
	 * would like to keep this though we are not using as of now..
	 */
	private final String pageName;

	/**
	 * layout type used to be set at the project level. We extended that feature
	 * at page level. Hence the attribute has been shifted to this class
	 */
	private final String pageLayoutType;

	/**
	 * create a context for this page. Use default language
	 * 
	 * @param pageName
	 * @param layOutType
	 */
	public PageGeneratorContext(String pageName, String layOutType) {
		this.pageName = pageName;
		this.pageLayoutType = layOutType;
	}

	String getLayoutType() {
		return this.pageLayoutType;
	}

	String getPanelName() {
		this.lastPanelIdxUsed++;
		return "_panel_" + this.lastPanelIdxUsed;
	}

	/**
	 * called after putting an error
	 * 
	 * @param errorText
	 */
	void reportError(String errorText) {
		this.errors.add(errorText);
		this.nbrErrors++;
	}

	/**
	 * called after putting an error
	 * 
	 * @param errorText
	 */
	void reportWarning(String warningText) {
		this.warnings.add(warningText);
	}

	/**
	 * getter
	 * 
	 * @return number of errors reported so far
	 */
	int getNbrErrors() {
		return this.nbrErrors;
	}

	/**
	 * generate javascript to set attributes of an object
	 * 
	 * @param element
	 *            from which to extract attributes
	 * @param elementName
	 *            javascript field name to which all attributes are to be
	 *            assigned to
	 * @param sbf
	 *            string buffer to which the text is to be appended to
	 * @param attributes
	 *            all possible attributes
	 */
	void setAttributes(Object element, String elementName, StringBuilder sbf,
			String[] attributes) {
		HashMap<String, Object> fields = (HashMap<String, Object>) ObjectManager
				.getAllFieldValues(element);

		for (String attribute : attributes) {

			/**
			 * just a check to confirm whether the linked table is defined or
			 * not
			 */
			if (attribute.equals("linkedTableName")) {
				boolean valFound = false;
				if (fields.containsKey(attribute)) {
					Object obj = fields.get(attribute);
					if (obj != null && obj.toString().length() > 0) {
						valFound = true;
					}
				}
				sbf.append("\n//linkedTableName ")
						.append(valFound ? " EXISTS " : " DOES NOT EXIST")
						.append("\n");
			}

			if (fields.containsKey(attribute)) {
				String stringValue = null;
				Object value = fields.get(attribute);
				if (value == null) {
					continue;
				}

				if (value instanceof Integer) {
					int i = ((Integer) value).intValue();
					if (i != 0 && i != Integer.MAX_VALUE
							&& i != Integer.MIN_VALUE) {
						stringValue = value.toString();
					}
				} else if (value instanceof Boolean) {
					if (((Boolean) value).booleanValue()) {
						stringValue = "true";
						// false is default..
					}
				} else if (value instanceof String) {
					stringValue = this.format(value.toString());
				} else if (value instanceof String[]) {
					String[] list = (String[]) value;
					stringValue = this.arrayToString(list, false);
				} else {
					stringValue = '\'' + value.toString().replace("'", "\\\'") + '\'';
				}

				// String str = "\t" + attribute + " = " + value.toString();
				if (stringValue != null) {
					// str += "... Added";
					sbf.append('\n').append(elementName).append('.')
							.append(attribute).append(" = ")
							.append(stringValue).append(";");
				} else {
					// str += "........ is default";
				}

				// Spit.Out(str);
			}
		}
	}

	/**
	 * format with quotes if required
	 * 
	 * @param val
	 * @return
	 */
	private String format(String val) {
		String stringValue = "''";
		if (val == null) {
			return stringValue;
		}
		if (val.length() > 0) {
			stringValue = '\'' + val.replace("'", "\\'") + '\'';
		}
		return stringValue;
	}

	/**
	 * rendering is entering a table panel.
	 * 
	 * @param tableName
	 * @param islistOrGrid
	 */
	void setTableName(String tableName, boolean islistOrGrid) {
		if (tableName == null || tableName.equals(Page.NOT_A_TABLE)) {
			this.resetTableName();
			return;
		}

		if (this.tableName != null) {
			String message = "ERROR: A panel that is already inside a table with name '"
					+ this.tableName
					+ "' is trying to use another table with name '"
					+ tableName + "'. This is not possible";
			Spit.out(message);
			this.reportError(message);
		}
		this.tableName = tableName;
		this.prefix = tableName + '_';
		this.isInsideGrid = true;
		if (islistOrGrid) {
			this.renderFieldAsColumn = true;
		}
	}

	/**
	 * rendering is getting out a table
	 */
	void resetTableName() {
		this.prefix = "";
		this.tableName = null;
		this.isInsideGrid = false;
		this.renderFieldAsColumn = false;
	}

	/**
	 * prefix the name if needed
	 * 
	 * @param name
	 * @return
	 */
	String getName(String name) {
		if (name == null) {
			return null;
		}
		return this.prefix + name;
	}

	/**
	 * if we are rendering within a table, return the table name, null otherwise
	 * 
	 * @return
	 */
	String getTableName() {
		return this.tableName;
	}

	/**
	 * prefix all field names in the the comma separated list if required
	 * 
	 * @param fieldNames
	 * @return
	 */
	String getFieldNames(String fieldNames) {
		return this.prefix + fieldNames.replace(",", "," + this.prefix);
	}

	/**
	 * add js text to set an attribute
	 * 
	 * @param js
	 * @param name
	 * @param value
	 */
	void setJsAttribute(StringBuilder js, String name, String value) {
		js.append('\n').append(Page.JS_VAR_NAME).append('.').append(name)
				.append(" = ").append(value).append(";");
	}

	/**
	 * add js text to set an attribute that has a text value
	 * 
	 * @param js
	 * @param name
	 * @param value
	 */
	void setJsTextAttribute(StringBuilder js, String name, String value) {
		js.append('\n').append(Page.JS_VAR_NAME).append('.').append(name)
				.append(" = ").append(this.format(value)).append(";");
	}

	/**
	 * set attributes that have field names which may have to be qualified with
	 * table names
	 * 
	 * @param objekt
	 * @param js
	 * @param names
	 */
	void setTableSensitiveAttributes(Object objekt, StringBuilder js,
			String[] names) {
		for (String name : names) {
			Object obj = ObjectManager.getFieldValue(objekt, name);
			if (obj == null) {
				continue;
			}

			String val = obj.toString();
			if (val.length() == 0 || val.equals(" ")) {
				continue;
			}
			val = this.getQualifiedFieldName(val);
			js.append('\n').append(Page.JS_VAR_NAME).append('.').append(name)
					.append(" = '").append(val).append("';");
		}
	}

	/**
	 * get a qualified name by prefixing table name to a field name
	 * 
	 * @param name
	 * @return
	 */
	private String getQualifiedFieldName(String name) {
		/*
		 * note that both names and fully qualified names are in the collection.
		 * So, the name has to exist
		 */

		if (!this.allFieldNames.containsKey(name)) {
			if (this.allNonFieldNames.contains(name)) {
				return name;
			}
			if (this.allTableNames.contains(name)) {
				Spit.out("Note: table "
						+ name
						+ " is used in place of a field. This is valid only for enable/disable action.");
				return name;
			}
			String err = "Error: " + name
					+ " is referred but not defined as a field in this page.";
			this.reportError(err);
			return name;
		}

		/*
		 * is it a field that is used in more than one table? NOTE: if a fully
		 * qualified name is used, it will not appear in duplicate collections
		 */
		if (this.duplicateFields.containsKey(name)) {
			Set<String> tableNames = this.duplicateFields.get(name);
			String tn = (this.tableName == null) ? Page.NOT_A_TABLE
					: this.tableName;
			if (tableNames.contains(tn) == false) {
				/*
				 * unqualified name is used for a field that is used in more
				 * than one table. And the reference is outside of one of these
				 * tables
				 */
				String err = "Warning: "
						+ name
						+ " is defined in more than one place. It is referred in another place. To refer to a field inside a table, use fully qualified name (tableName_fieldName). Otherwise, field name outside the table is assumed.";
				Spit.out(err);
				return name;
			}
			if (this.tableName == null) {
				return name;
			}
			return this.tableName + '_' + name;
		}
		String tblName = this.allFieldNames.get(name);

		if (tblName.equals(Page.NOT_A_TABLE)) {
			return name;
		}

		return tblName + '_' + name;
	}

	/**
	 * set field values that are arrays, but the field names are to be qualified
	 * if they are part of a table
	 * 
	 * @param objekt
	 * @param js
	 * @param names
	 */
	void setTableSensitiveArrays(Object objekt, StringBuilder js, String[] names) {
		for (String name : names) {
			Object obj = ObjectManager.getFieldValue(objekt, name);
			if (obj == null) {
				continue;
			}
			if ((obj instanceof String[]) == false) {
				String err = "ERROR : attribute " + name
						+ " is supposed to be an array of string, but it is "
						+ obj.getClass().getName();
				Spit.out(err);
				this.reportError(err);
			}
			String[] arr = (String[]) obj;
			if (arr.length == 0) {
				continue;
			}
			js.append('\n').append(Page.JS_VAR_NAME).append('.').append(name)
					.append(" = ");
			if (name.equals("descQueryFields")) {
				boolean hasSources = false;
				for (String cmpname : names) {
					if (cmpname.equals("descQueryFieldSources")) {
						hasSources = true;
						break;
					}
				}
				if (hasSources) {
					js.append(this.arrayToString(arr, false)).append(';');
				} else {
					js.append(this.arrayToString(arr, true)).append(';');
				}
			} else if (name.equals("listServiceQueryFieldNames")) {
				boolean hasSources = false;
				for (String cmpname : names) {
					if (cmpname.equals("listServiceQueryFieldSources")) {
						hasSources = true;
						break;
					}
				}
				if (hasSources) {
					js.append(this.arrayToString(arr, false)).append(';');
				} else {
					js.append(this.arrayToString(arr, true)).append(';');
				}
			} else {
				js.append(this.arrayToString(arr, true)).append(';');
			}
		}
	}

	/**
	 * generate js for an array of element as a comma separated string
	 * 
	 * @param vals
	 * @param isTableSensitive
	 * @return
	 */
	private String arrayToString(String[] vals, boolean isTableSensitive) {
		StringBuilder js = new StringBuilder();
		js.append(PageGeneratorContext.BRACE_OPEN);
		char prefixChar = ' ';
		String strval;
		for (String val : vals) {
			if (val.length() == 0 || val.equals(" ")) {
				strval = "''";
			} else if (isTableSensitive) {
				strval = '\'' + this.getQualifiedFieldName(val) + '\'';
			} else {
				strval = this.format(val);
			}
			js.append(prefixChar).append(strval);
			prefixChar = PageGeneratorContext.COMMA;
		}
		js.append(PageGeneratorContext.BRACE_CLOSE);
		return js.toString();
	}

	/**
	 * set given attributes to the object
	 * 
	 * @param objekt
	 * @param js
	 * @param names
	 */
	void setAttributes(Object objekt, StringBuilder js, String[] names) {
		this.setAttributes(objekt, Page.JS_VAR_NAME, js, names);
	}

	/**
	 * add hidden fields to the page. Note that the hidden fields that are part
	 * of a table have to be put at the same place in html so that they get
	 * repeated for each row, but fields outside of any table belong to the
	 * page, and hence are produced at the end.
	 * 
	 * @param fieldName
	 */
	void addHiddenFieldsToPage(String fieldName) {
		this.fieldsToHideOnPageLoad.add(fieldName);
	}

	/**
	 * empty (copy and then delete) accumulated error messages to the list
	 * 
	 * @param errorMessages
	 *            to which accumulated messages are to be emptied into
	 * @return
	 */
	boolean dumpErrors(List<String> errorMessages) {
		if (this.errors.size() == 0) {
			return false;
		}
		for (String msg : this.errors) {
			errorMessages.add(msg);
		}
		this.errors.clear();
		return true;
	}
}

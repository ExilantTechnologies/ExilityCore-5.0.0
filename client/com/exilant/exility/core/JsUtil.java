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

import java.util.Date;
import java.util.Map;
import java.util.Set;

/***
 * Utilities for json and http client related activities
 * 
 */
public class JsUtil {
	private static final char SINGLE_QUOTE = '\'';
	private static final char DOUBLE_QUOTE = '"';
	private static final char COLON = ':';
	private static final char SQUARE_BRACKET_OPEN = '[';
	private static final char SQUARE_BRACKET_CLOSE = ']';
	private static final char BRACE_OPEN = '{';
	private static final char BRACE_CLOSE = '}';
	private static final char SPACE = ' ';
	private static final char COMMA = ',';
	private static final String INITIAL_HTML = "<html><head><link rel=\"stylesheet\" href=\"default.css\" type=\"text/css\" />";
	private static final String FINAL_HTML = "</body></html>";

	private JsUtil() {

	}

	/***
	 * push value into json string
	 * 
	 * @param sbf
	 *            to which json for this object is to be pushed
	 * @param val
	 *            value object (String and Date is considered to be values)
	 */
	public static void toJson(StringBuilder sbf, Object val) {
		if (val == null) {
			sbf.append("''");
			return;
		}

		if (val instanceof String) {
			String str = ((String) val).replace("\\", "\\\\")
					.replace("'", "\\'").replace("\n", "\\n")
					.replace("\r", "\\r").replace("\t", "\\t");
			sbf.append(JsUtil.SINGLE_QUOTE).append(str)
					.append(JsUtil.SINGLE_QUOTE);
			return;
		}
		if (val.getClass().isEnum()) {
			sbf.append(JsUtil.SINGLE_QUOTE).append(val)
					.append(JsUtil.SINGLE_QUOTE);
			return;
		}
		if (val instanceof Value) {
			Value value = (Value) val;
			JsUtil.toJson(sbf, value.getTextValue());
			return;
		}

		if (val.getClass().isPrimitive()) {
			sbf.append(JsUtil.SPACE).append(val).append(JsUtil.SPACE);
			return;
		}

		if (val instanceof ValueList) {
			sbf.append(JsUtil.SQUARE_BRACKET_OPEN);
			char prefixChar = JsUtil.SPACE; // first field should not have a
											// prefix

			String[] vals = ((ValueList) val).format();
			for (String v : vals) {
				sbf.append(prefixChar);
				sbf.append(JsUtil.SINGLE_QUOTE).append(v)
						.append(JsUtil.SINGLE_QUOTE);
				prefixChar = JsUtil.COMMA; // second field onwards should prefix
											// a comma
			}
			sbf.append(JsUtil.SQUARE_BRACKET_CLOSE);
			return;
		}

		if (val instanceof Date) {
			sbf.append(JsUtil.SINGLE_QUOTE)
					.append(DateUtility.formatDate((Date) val))
					.append(JsUtil.SINGLE_QUOTE);
			return;
		}

		if (val instanceof Object[]) {
			sbf.append(JsUtil.SQUARE_BRACKET_OPEN);
			char prefixChar = JsUtil.SPACE; // first field should not have a
											// prefix

			Object[] list = (Object[]) val;
			for (Object v : list) {
				sbf.append(prefixChar);
				JsUtil.toJson(sbf, v);
				prefixChar = JsUtil.COMMA; // second field onwards should prefix
											// a comma
			}
			sbf.append(JsUtil.SQUARE_BRACKET_CLOSE);
			return;
		}

		if (val instanceof Set) {
			@SuppressWarnings("rawtypes")
			Set list = (Set) val;

			sbf.append(JsUtil.SQUARE_BRACKET_OPEN);
			char prefixChar = JsUtil.SPACE;

			for (Object v : list) {
				sbf.append(prefixChar);
				JsUtil.toJson(sbf, v);
				prefixChar = JsUtil.COMMA;
			}
			sbf.append(JsUtil.SQUARE_BRACKET_CLOSE);
			return;
		}

		if (val instanceof Grid) {
			sbf.append(JsUtil.SQUARE_BRACKET_OPEN);
			char prefixChar = JsUtil.SPACE;

			String[][] grid = ((Grid) val).getRawData();
			for (String[] row : grid) {
				sbf.append(prefixChar);
				JsUtil.toJson(sbf, row);
				prefixChar = JsUtil.COMMA; // second field onwards should prefix
											// a comma
			}
			sbf.append(JsUtil.SQUARE_BRACKET_CLOSE);
			return;
		}
		if (val instanceof Map) {
			sbf.append(JsUtil.BRACE_OPEN);
			String prefix = "";
			@SuppressWarnings("rawtypes")
			Map map = (Map) val;
			for (Object key : map.keySet()) {
				sbf.append(prefix).append(JsUtil.DOUBLE_QUOTE).append(key)
						.append(JsUtil.DOUBLE_QUOTE).append(JsUtil.COLON);
				JsUtil.toJson(sbf, map.get(key));
				prefix = "\n,";
			}
			sbf.append(JsUtil.BRACE_CLOSE);
			return;
		}
		sbf.append(JsUtil.BRACE_OPEN);
		char prefix = JsUtil.SPACE;
		Map<String, Object> fields = ObjectManager.getAllFieldValues(val);
		for (String key : fields.keySet()) {
			sbf.append(prefix).append(JsUtil.DOUBLE_QUOTE).append(key)
					.append(JsUtil.DOUBLE_QUOTE).append(JsUtil.COLON);
			JsUtil.toJson(sbf, fields.get(key));
			prefix = JsUtil.COMMA;
		}
		sbf.append(JsUtil.BRACE_CLOSE);

	}

	/***
	 * specific method to convert serviceData to json
	 * 
	 * @param sbf
	 * @param serviceData
	 */
	public static void toJson(StringBuilder sbf, ServiceData serviceData) {
		// set dc.success = true|false
		// sbf.append("{\"status\":").append(serviceData.getErrorStatus());

		sbf.append("{\"success\":");
		if (serviceData.getErrorStatus() >= 2) {
			sbf.append("false");
		} else {
			sbf.append("true");
		}
		sbf.append("\n,\"values\":");
		JsUtil.toJson(sbf, serviceData.values);

		sbf.append("\n,\"lists\":");
		JsUtil.toJson(sbf, serviceData.lists);

		sbf.append("\n,\"grids\":");
		JsUtil.toJson(sbf, serviceData.grids);

		sbf.append("\n,\"messages\":");
		JsUtil.toJson(sbf, serviceData.messageList);

		// that is all we have. close the Object
		sbf.append(JsUtil.BRACE_CLOSE);
		return;
	}

	/***
	 * convert dc to json
	 * 
	 * @param sbf
	 * @param dc
	 */
	public static void toJson(StringBuilder sbf, DataCollection dc) {
		// set dc.success = true|false
		sbf.append("{\"success\":");
		if (dc.hasError()) {
			sbf.append("false");
		} else {
			sbf.append("true");
		}

		sbf.append("\n,\"values\":");
		JsUtil.toJson(sbf, dc.values);

		sbf.append("\n,\"lists\":");
		JsUtil.toJson(sbf, dc.lists);

		sbf.append("\n,\"grids\":");
		JsUtil.toJson(sbf, dc.grids);

		sbf.append("\n,\"messages\":");
		JsUtil.toJson(sbf, dc.messageList);

		// that is all we have. close the Object
		sbf.append(JsUtil.BRACE_CLOSE);
		return;
	}

	/***
	 * convert to traditional js (not json)
	 * 
	 * @param sbf
	 * @param serviceData
	 */
	public static void toJs(StringBuilder sbf, ServiceData serviceData) {
		sbf.append("var dc = new PM.DataColelction();");
		sbf.append("\ndc.success = ").append(
				serviceData.getErrorStatus() > 1 ? "false;" : "true;");

		for (String name : serviceData.values.keySet()) {
			sbf.append("\ndc.values['").append(name).append("'] = '")
					.append(serviceData.getValue(name).replaceAll("'", "\\'"))
					.append("';");
		}

		for (String name : serviceData.lists.keySet()) {
			sbf.append("\ndc.lists['").append(name).append("'] = ");
			JsUtil.toJs(sbf, serviceData.lists.get(name));
			sbf.append(";");
		}

		for (String name : serviceData.grids.keySet()) {
			sbf.append("\ndc.grids['").append(name).append("'] = ");
			JsUtil.toJs(sbf, serviceData.grids.get(name));
			sbf.append(";");
		}

		JsUtil.toJs(sbf, serviceData.messageList);
		return;
	}

	/***
	 * generate java-script for message list
	 * 
	 * @param sbf
	 * @param messageList
	 */
	private static void toJs(StringBuilder sbf, MessageList messageList) {
		String[] msgs = messageList.getErrorTexts();
		if (msgs != null && msgs.length > 0) {
			sbf.append("dc.messages['error'] = ");
			JsUtil.toJs(sbf, msgs);
		}

		msgs = messageList.getWarningTexts();
		if (msgs != null && msgs.length > 0) {
			sbf.append("dc.messages['warning'] = ");
			JsUtil.toJs(sbf, msgs);
		}

		msgs = messageList.getInfoTexts();
		if (msgs != null && msgs.length > 0) {
			sbf.append("dc.messages['info'] = ");
			JsUtil.toJs(sbf, msgs);
		}
	}

	/***
	 * generate java-script for grid that is an array of text arrays
	 * 
	 * @param sbf
	 * @param strings
	 */
	private static void toJs(StringBuilder sbf, String[][] strings) {
		sbf.append('[');
		String prefix = "\n";
		for (String[] arr : strings) {
			sbf.append(prefix);
			JsUtil.toJs(sbf, arr);
			prefix = "\n,";
		}
		sbf.append('[');
	}

	/***
	 * generate java-script for text array
	 * 
	 * @param sbf
	 * @param strings
	 */
	private static void toJs(StringBuilder sbf, String[] strings) {
		sbf.append('[');
		String prefix = "\n\t'";
		for (String s : strings) {
			sbf.append(prefix).append(s.replaceAll("'", "\\'")).append('\'');
			prefix = "\n\t,'";
		}
		sbf.append(']');
	}

	/***
	 * generate json for message list
	 * 
	 * @param sbf
	 * @param messages
	 */
	public static void toJson(StringBuilder sbf, MessageList messages) {
		sbf.append(JsUtil.BRACE_OPEN);
		String[] msgs = messages.getInfoTexts();
		char prefix = JsUtil.SPACE;
		if (msgs != null && msgs.length > 0) {
			sbf.append("\n").append(prefix).append(" \"Info\":");
			prefix = JsUtil.COMMA;
			JsUtil.toJson(sbf, msgs);
		}

		msgs = messages.getWarningTexts();
		if (msgs != null && msgs.length > 0) {
			sbf.append("\n").append(prefix).append(" \"Warning\":");
			prefix = JsUtil.COMMA;
			JsUtil.toJson(sbf, msgs);
		}

		msgs = messages.getErrorTexts();
		if (msgs != null && msgs.length > 0) {
			sbf.append("\n").append(prefix).append(" \"Error\":");
			prefix = JsUtil.COMMA;
			JsUtil.toJson(sbf, msgs);
		}
		sbf.append(JsUtil.BRACE_CLOSE);
	}

	/**
	 * save data types defined for this project into a file for client side
	 * engine
	 * 
	 * @param fileName
	 */
	@Deprecated
	public static void SaveDataTypeJs(String fileName) // throws
														// ExilityException
	{
		Set<String> names = DataTypes.getDataTypeNames();
		if (names.size() == 0) {
			Spit.out("No data types defined for the project");
			return;
		}
		StringBuilder sbf = new StringBuilder();
		sbf.append("var dataTypes = {};\nvar dt;\n");

		StringBuilder msgs = new StringBuilder();
		msgs.append("\n messages = {");
		char prefix = JsUtil.SPACE;

		for (String name : names) {
			AbstractDataType dt = DataTypes.getDataType(name, null);

			String className = dt.getClass().getName();
			className = className.substring(className.lastIndexOf('.') + 1);
			sbf.append("\ndt = new PM.").append(className).append("());");
			ObjectManager.serializePrimitiveAttributes(dt, "dt", sbf);

			String msgId = dt.messageName;
			msgs.append(prefix).append("\"").append(msgId).append("\" : ");
			JsUtil.toJson(msgs, Messages.getMessageText(msgId));

			sbf.append("dataTypes['").append(name).append("'] = dt;\n");

		}
		msgs.append("};");
		ResourceManager
				.saveResource(fileName, sbf.toString() + msgs.toString());
	}

	/***
	 * generate html text to render service data
	 * 
	 * @param data
	 * @return text that can be set as innerHTML for a div element to render
	 *         this service data in a readable form on a browser
	 */
	public static String toHtml(ServiceData data) {
		StringBuilder sbf = new StringBuilder();
		JsUtil.messageToHtml(sbf, data.messageList);
		JsUtil.valuesToHtml(sbf, data.values);
		JsUtil.listToHtml(sbf, data.lists);
		for (String gridName : data.grids.keySet()) {
			JsUtil.gridToHtml(sbf, gridName, data.getGrid(gridName));
		}

		return JsUtil.INITIAL_HTML + sbf.toString() + JsUtil.FINAL_HTML;
	}

	/***
	 * render grid as an html
	 * 
	 * @param sbf
	 * @param gridName
	 * @param grid
	 */
	private static void gridToHtml(StringBuilder sbf, String gridName,
			String[][] grid) {
		sbf.append("<br /><fieldset><legend>gridName</legend><table border=\"1\"><tr>");
		String[] header = grid[0];
		for (String name : header) {
			sbf.append("<th>").append(name).append("</th>");
		}
		sbf.append("</tr>");
		for (int i = 1; i < grid.length; i++) {
			sbf.append("<tr>");
			String[] row = grid[i];
			for (String txt : row) {
				sbf.append("<td>").append(txt).append("</td>");
			}
			sbf.append("</tr>");
		}
		sbf.append("</table></fieldset>");

	}

	/***
	 * render columns as html
	 * 
	 * @param sbf
	 * @param lists
	 */
	private static void listToHtml(StringBuilder sbf,
			Map<String, String[]> lists) {
		sbf.append("<br /><fieldset><legend>Lists</legend><table border=\"0\">");
		for (String listName : lists.keySet()) {
			sbf.append("<tr><td>").append(listName).append(" :</td><td>");
			String[] vals = lists.get(listName);
			if (vals == null || vals.length == 0) {
				sbf.append("&nbsp;");
			} else {
				sbf.append(vals[0]);
				for (int i = 1; i < vals.length; i++) {
					sbf.append(", ").append(vals[i]);
				}
			}
			sbf.append("</td></tr>");
		}
		sbf.append("</table></fieldset>");
	}

	/***
	 * render values as html
	 * 
	 * @param sbf
	 * @param values
	 */
	private static void valuesToHtml(StringBuilder sbf,
			Map<String, String> values) {
		sbf.append("<br /><fieldset><legend>Values</legend><table border=\"0\">");
		for (String name : values.keySet()) {
			String txt = values.get(name);
			if (txt == null || txt.length() == 0) {
				txt = "&nbsp";
			}
			sbf.append("<tr><td>").append(name).append(" :</td><td>")
					.append(txt).append("</td></tr>");
		}
		sbf.append("</table></fieldset>");
	}

	/***
	 * render message list as html
	 * 
	 * @param sbf
	 * @param messageList
	 */
	private static void messageToHtml(StringBuilder sbf, MessageList messageList) {
		if (messageList.size() == 0) {
			return;
		}
		sbf.append("<br /><fieldset><legend>Messages</legend><table border=\"0\">");
		String[] msgs = messageList.getErrorTexts();
		if (msgs.length > 0) {
			sbf.append("<tr style=\"color:red;\"><td style=\"valign:top;\">Error</td><td>)");
			for (String msg : msgs) {
				sbf.append("<li>").append(msg).append("</li>");
			}
			sbf.append("</td></tr");
		}

		msgs = messageList.getWarningTexts();
		if (msgs.length > 0) {
			sbf.append("<tr style=\"color:blue;\"><td style=\"valign:top;\">Warning</td><td>)");
			for (String msg : msgs) {
				sbf.append("<li>").append(msg).append("</li>");
			}
			sbf.append("</td></tr");
		}

		msgs = messageList.getInfoTexts();
		if (msgs.length > 0) {
			sbf.append("<tr><td style=\"valign:top;\">Information</td><td>)");
			for (String msg : msgs) {
				sbf.append("<li>").append(msg).append("</li>");
			}
			sbf.append("</td></tr");
		}
		sbf.append("</table></fieldset>");
	}

	/***
	 * render an error as html
	 * 
	 * @param e
	 * @return String that is suitable to be rendered as an HTML fragment
	 */
	public static String toHtml(Exception e) {
		return JsUtil.INITIAL_HTML + "<h1>Error</h1><br/>" + e.getMessage()
				+ "<br /> stack trace<br/>" + JsUtil.FINAL_HTML;
	}
}
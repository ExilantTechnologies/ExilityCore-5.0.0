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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/**
 * utility that generates a .js file for all data types to be used by the client
 * engine.
 * 
 */
public class DataTypesGenerator {
	static final String JS_VAR_NAME = "dt";

	// private static final String[] FIELD_NAMES = {
	// "minParametersToFireOnLoad", "minParameters", "defaultButtonName",
	// "onLoadActionName" };

	// client error messages have to be copied.
	// TODO: Better design would be have this info in the message file itself
	private static final String[] CLIENT_MESSAGE_NAMES = {
			"exilColumnIsRequired", "exilValueRequired",
			"exilPageParameterMissing", "exilValidateDependencyFailed",
			"exilValidateUniqueColumnsFailed", "exilFromToDataTypeMismatch",
			"exilFromToValidationError", "exilInvalidFromTo" };

	/**
	 * return string that represents java-script
	 * 
	 * @return java-script string
	 */
	@SuppressWarnings("deprecation")
	public static String toJavaScript() {
		StringBuilder js = new StringBuilder();
		js.append("\n var " + DataTypesGenerator.JS_VAR_NAME + ";");
		js.append("\nvar dataTypes = {};");

		HashMap<String, String> messageTexts = new HashMap<String, String>();
		for (String name : DataTypes.getDataTypeNames()) {
			js.append("\ndataTypes['").append(name).append("'] = new ");
			AbstractDataType dt = DataTypes.getDataType(name, null);

			String messageName = dt.messageName; // message name is not null. It
													// is set in initialize() if
													// user has not provided
			Message msg = Messages.getMessage(messageName);
			String txt = msg != null ? msg.text : dt.description;
			if (txt == null) {
				txt = "";
			}

			messageTexts.put(messageName, txt);

			if (dt instanceof TextDataType) {
				TextDataType dte = (TextDataType) dt;
				String rx = (dte.regex == null) ? "null" : '/' + dte.regex
						.toString() + '/';
				// append text of the form TextDataType('name', 'messageName',
				// 'regex', minLength, maxLength);
				js.append("TextDataType('").append(name).append("', '")
						.append(messageName).append("', ").append(rx)
						.append(", ").append(dte.minLength).append(", ")
						.append(dte.maxLength).append(");");
			}

			else if (dt instanceof IntegralDataType) {
				IntegralDataType dte = (IntegralDataType) dt;
				String max = (dte.maxValue == Long.MAX_VALUE) ? "null" : Long
						.toString(dte.maxValue);
				String min = (dte.minValue == Long.MIN_VALUE) ? "null" : Long
						.toString(dte.minValue);
				js.append("IntegralDataType('").append(name).append("', '")
						.append(messageName).append("', ").append(min)
						.append(", ").append(max).append(", ");
				if (dte.allowNegativeValue) {
					js.append("true");
				} else {
					js.append("false");
				}

				js.append(");");
			}

			else if (dt instanceof DecimalDataType) {
				DecimalDataType dte = (DecimalDataType) dt;
				String max = (dte.maxValue == Double.MAX_VALUE) ? "null"
						: Double.toString(dte.maxValue);
				String min = (dte.minValue == Double.MIN_VALUE) ? "null"
						: Double.toString(dte.minValue);

				js.append("DecimalDataType('").append(name).append("', '")
						.append(messageName).append("', ").append(min)
						.append(", ").append(max).append(", ");
				if (dte.allowNegativeValue) {
					js.append("true");
				} else {
					js.append("false");
				}
				js.append(", ").append(dte.numberOfDecimals);
				js.append(");");
			}

			else if (dt instanceof DateDataType) {
				DateDataType dte = (DateDataType) dt;
				String bef = (dte.maxDaysBeforeToday == Integer.MAX_VALUE) ? "null"
						: Integer.toString(dte.maxDaysBeforeToday);
				String aft = (dte.maxDaysAfterToday == Integer.MAX_VALUE) ? "null"
						: Integer.toString(dte.maxDaysAfterToday);
				js.append("DateDataType('")
						.append(name)
						.append("', '")
						.append(messageName)
						.append("', ")
						.append(bef)
						.append(", ")
						.append(aft)
						.append(", ")
						.append(Boolean.toString(dte.includesTime)
								.toLowerCase()).append(");");
			}

			else if (dt instanceof TimeStampDataType) {
				js.append("TimeStampDataType('").append(name).append("', '")
						.append(messageName).append("'); ");
			}

			else if (dt instanceof BooleanDataType) {
				BooleanDataType dte = (BooleanDataType) dt;
				String tval = (dte.trueValue == null) ? "null"
						: '\'' + dte.trueValue + '\'';
				String fval = (dte.falseValue == null) ? "null"
						: '\'' + dte.falseValue + '\'';
				js.append("BooleanDataType('").append(name).append("', '")
						.append(messageName).append("', ").append(fval)
						.append(", ").append(tval).append(");");
			}

			else {
				String s = "//unknown data type encountered during conversion "
						+ dt.getClass().getName();
				js.append(s);
				Spit.out(s);
			}
			js.append('\n');

		}
		// messages
		js.append("\n\n//************ messages ****************");
		js.append("\n var dataTypeMessages = new Object();");
		js.append("\n// client messages for generic validation **");
		for (String name : DataTypesGenerator.CLIENT_MESSAGE_NAMES) {
			js.append("\ndataTypeMessages['")
					.append(name)
					.append("'] = '")
					.append(Messages.getMessageText(name).replace("'", "\\\\'"))
					.append("';");
		}
		js.append("\n// data type specific messge. If the message is not found in meesages.xml, description of data type is put here *");
		for (String messageName : messageTexts.keySet()) {
			if (messageTexts.get(messageName) != null) {
				js.append("\ndataTypeMessages['")
						.append(messageName)
						.append("'] = '")
						.append(messageTexts.get(messageName).replaceAll("'",
								"\\\\'")).append("';");
			} else {
				js.append("\ndataTypeMessages['").append(messageName)
						.append("'] = '';");
			}
		}

		// also add messages that are marked for client
		for (Message msg : Messages.getClientMessages()) {
			js.append("\ndataTypeMessages['").append(msg.name).append("'] = '")
					.append(msg.text.replaceAll("'", "\\\\'")).append("';");
		}
		return js.toString();
	}

	/**
	 * generate xsd options for the list of valid data types
	 * 
	 * @return xsd element for all these data types
	 */
	public static String toXsd() {
		StringBuilder sbf = new StringBuilder();
		sbf.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"
				+ "\n<xs:schema xmlns=\"http://com.exilant.exility/dataTypes\" "
				+ "\ntargetNamespace=\"http://com.exilant.exility/dataTypes\" "
				+ "\n\t\txmlns:xs=\"http://www.w3.org/2001/XMLSchema\">");

		sbf.append("\n\n\t<xs:simpleType name=\"declaredDataTypes\">\n\t\t<xs:restriction base=\"xs:NCName\">");

		Set<String> c = DataTypes.getDataTypeNames();
		String[] names = new String[c.size()];
		names = c.toArray(new String[c.size()]);
		Arrays.sort(names);
		for (String name : names) {
			AbstractDataType dt = DataTypes.getDataType(name, null);
			sbf.append("\n\t\t\t<xs:enumeration value=\"")
					.append(name)
					.append("\"><xs:annotation><xs:documentation>")
					.append(dt.description)
					.append("</xs:documentation></xs:annotation></xs:enumeration>");
		}

		sbf.append("\n\t\t</xs:restriction>\n\t</xs:simpleType>\n</xs:schema>\n");

		return sbf.toString();
	}
}

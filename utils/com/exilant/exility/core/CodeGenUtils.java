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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A generic utilities class that is used during code generation
 */
// we are not using this class, and hence we have not added java docs
@SuppressWarnings("javadoc")
public abstract class CodeGenUtils {
	public static final String NEXT_LINE = "\n\t";
	public static final String INDENT1 = "\t";
	public static final String INDENT2 = "\t\t";
	public static final String INDENT3 = "\t\t\t";
	/**
	 * Maximum characters per line in generated code (Only used for Javadoc
	 * comments for now)
	 */
	protected static final int MAX_CHARS_PER_LINE = 80;

	/**
	 * The line separator character sequence. Used for code formatting during
	 * auto-generation
	 */
	public static String newLine() {
		return System.getProperty("line.separator");
	}

	/**
	 * Accessor for the character sequence that indicates an indent. Used for
	 * code formatting during auto-generation
	 * 
	 * @param The
	 *            indent level
	 */
	public static String indent(int level) {
		StringBuffer sb = new StringBuffer();
		int i = level;
		while (i > 0) {
			sb.append('\t');
			i--;
		}
		return sb.toString();
	}

	/**
	 * Utility method to indent class attribute definitions with default
	 * indentation level
	 */
	public static String indentClassAttribute() {
		return CodeGenUtils.indent(1);
	}

	/**
	 * Utility method to indent class method definition with default indentation
	 * level
	 */
	public static String indentClassMethod() {
		return CodeGenUtils.indent(1);
	}

	/**
	 * Utility method to indent class method source code with default
	 * indentation level
	 */
	public static String indentClassMethodSrc() {
		return CodeGenUtils.indent(2);
	}

	/**
	 * Utility method to indent class method source code inside a condition
	 * block with default indentation level
	 */
	public static String indentClassMethodSrcWithinCondition() {
		return CodeGenUtils.indent(3);
	}

	/**
	 * Utility method to indent class method source code inside the try/catch
	 * block with default indentation level
	 */
	public static String indentClassMethodSrcWithinTryCatch() {
		return CodeGenUtils.indent(3);
	}

	/**
	 * Utility method to indent class method source code inside a conditional
	 * statement inside the try/catch block with default indentation level
	 */
	public static String indentClassMethodSrcWithinConditionWithinTryCatch() {
		return CodeGenUtils.indent(4);
	}

	/**
	 * Convert the given name to Camel casing...the standard naming convention
	 * used in Java.This is not an elaborate method that looks for word
	 * boundaries, it simply makes the first character to lowercase
	 * 
	 * @param input
	 *            The input string to convert to camel casing.
	 */
	public static String toCamelCase(String input) {
		if (input == null || input.trim().length() == 0) {
			return input;
		}
		String camel = input.trim();
		if (camel.length() <= 3) {
			return camel.toLowerCase();
		}

		camel = camel.substring(0, 1).toLowerCase() + camel.substring(1);

		return camel;
	}

	/**
	 * Convert the given name to Camel casing...using the dictionary
	 * entries...the standard naming convention used in Java.This method looks
	 * for word boundaries based on the dictionary entries, it simply makes the
	 * first character of the string to lowercase and for subsequent word
	 * matches with the dictionary, the first character is converted to
	 * uppercase . Thus, if the dictionary has "department" and the input string
	 * is "employeedepartment", the case conversion will result in
	 * "employeeDepartment".
	 * 
	 * @param inputString
	 *            The input string to convert to camel casing.
	 * @param dic
	 *            The dictionary containing the words to convert to Pascal case
	 *            (not camel case, this is for subsequent words)
	 */
	public static String toCamelCase(String inputString, Collection<String> dic) {
		if (inputString == null || inputString.trim().length() == 0) {
			return inputString;
		}
		String input = inputString.trim();
		if (input.length() <= 3) {
			return input.toLowerCase();
		}
		// Ensure first character is in lowercase
		input = input.substring(0, 1).toLowerCase() + input.substring(1);
		// Now compare with dictionary
		String beginPart = "";
		String matchPart = "";
		String remainingPart = "";
		if (dic != null) {
			for (String word : dic) {
				int startIdx = input.trim().toLowerCase()
						.indexOf(word.trim().toLowerCase(), 1); /*
																 * case
																 * insensitive
																 * search
																 */
				if (startIdx < 0) {
					continue;
				}
				beginPart = input.substring(0, startIdx);
				matchPart = input.substring(startIdx, startIdx + 1)
						.toUpperCase();
				remainingPart = input.substring(startIdx + 1);
				input = beginPart + matchPart + remainingPart;
			}
		}
		return input;
	}

	/**
	 * Convert the given name to Pascal casing...the standard naming convention
	 * used in Java.This is not an elaborate method that looks for word
	 * boundaries, it simply makes the first character to uppercase
	 * 
	 * @param name
	 *            The input string to convert to Pascal casing.
	 */
	public static String toPascalCase(String name) {
		String className = name.trim();
		if (className.length() <= 2) {
			return className.toUpperCase();
		}

		return className.substring(0, 1).toUpperCase() + className.substring(1);
	}

	/**
	 * Convert the given name to Pascal casing using the dictionary
	 * entries...the standard naming convention used in Java.This method looks
	 * for word boundaries based on the dictionary entries, it simply makes the
	 * first character to uppercase for word matches with the dictionary. Thus,
	 * if the dictionary has "department" and the input string is
	 * "employeedepartment", the case conversion will result in
	 * "EmployeeDepartment". Please note, this method will always convert the
	 * first character to uppercase
	 * 
	 * @param inputString
	 *            The input string to convert to pascal casing.
	 * @param dic
	 *            The dictionary containing the words to convert to Pascal case
	 */
	public static String toPascalCase(String inputString, Collection<String> dic) {
		if (inputString == null || inputString.trim().length() == 0) {
			return inputString;
		}
		String input = inputString.trim();
		if (input.length() == 1) {
			return input.toUpperCase();
		}
		/* First convert the first character to upper case */
		input = input.substring(0, 1).toUpperCase() + input.substring(1);
		// Now compare with dictionary
		String beginPart = "";
		String matchPart = "";
		String remainingPart = "";
		if (dic != null) {
			for (String word : dic) {
				int startIdx = input.trim().toLowerCase()
						.indexOf(word.trim().toLowerCase(), 1); /*
																 * case
																 * insensitive
																 * search
																 */
				if (startIdx < 0) {
					continue;
				}
				beginPart = input.substring(0, startIdx);
				matchPart = input.substring(startIdx, startIdx + 1)
						.toUpperCase();
				remainingPart = input.substring(startIdx + 1);
				input = beginPart + matchPart + remainingPart;
			}
		}

		return input;
	}

	/**
	 * Wrap a big line after the specified number of maximum characters The
	 * wrapping happens at word boundaries, so no word is broken.
	 * 
	 * @param input
	 *            The string input to wrap
	 * @param maxChars
	 *            The maximum number of characters allowed before the string is
	 *            wrapped
	 * @return A collection of String objects with each entry representing the
	 *         portion of the input string that was wrapped to next line
	 */
	public static Collection<String> wrap(String inputString, int maxChars) {
		String part = "";
		ArrayList<String> parts = new ArrayList<String>();
		int idx = 0;
		String input = inputString == null ? "" : inputString.trim();

		if (input.length() < maxChars) {
			parts.add(input);
			return parts;
		}
		/* Now do the wrapping */

		do {
			if (input.length() < maxChars) {
				break;
			}
			idx = input.indexOf(" ", maxChars);
			if (idx >= 0) {
				part = input.substring(0, idx + 1);
				parts.add(part);
				input = input.substring(idx + 1);
			}
		} while (idx >= 0);
		// add last one
		parts.add(input);
		return parts;
	}

	/**
	 * Load the code template
	 * 
	 * @param templatePath
	 *            The template path
	 * @return Read code template and load as string
	 * @throws Exception
	 */
	public static String loadCodeTemplate(String templatePath) throws Exception {
		InputStream inputStream = CodeGenUtils.class
				.getResourceAsStream(templatePath);
		Reader reader = new BufferedReader(new InputStreamReader(inputStream,
				"UTF-8"));
		char[] buffer = "".toCharArray();
		StringBuilder sb = new StringBuilder();
		while (reader.read(buffer) != -1) {
			sb.append(buffer);
			buffer = null;
			buffer = new char[1024];
			// Initialize to blank
			for (int i = 0; i < buffer.length; i++) {
				buffer[i] = ' ';
			}
		}
		inputStream.close();
		return sb.toString();
	}

	/**
	 * The way code autogeneration works, is that we create a template that can
	 * be compiled without errors. Code snippets and names, that need to be
	 * specific to the given context are wrapped inside special comments
	 * begin_stubName and end_stubName. Given a stub name, the whole begin/end
	 * section is replaced with the value
	 * 
	 * @param template
	 *            The template in which to search for the given stub
	 * @param stubName
	 *            The name of the stub to search for
	 * @param stubValue
	 *            The value with which we need to replace the stub name
	 * @return Modified template with stub names replaced with stub values
	 * @throws ExilityException
	 *             In case stub being and end both together are not found
	 */
	public static String substituteStub(String template, String stubName,
			String stubValue) throws ExilityException {
		if (template == null || template.trim().length() == 0) {
			return template;
		}
		if (stubName == null || stubName.trim().length() == 0) {
			return template;
		}
		if (stubValue == null) {
			return template; // Do not check for empty string. That may be a
								// valid value.
		}

		/*
		 * If a stub name begin is not found, we silently ignore. Assuming the
		 * caller knows what he is doing.
		 */
		String stubStartStr = "/*begin_" + stubName + "*/";
		String stubEndStr = "/*end_" + stubName + "*/";
		int stubStartIdx = template.indexOf(stubStartStr, 0);
		if (stubStartIdx < 0) {
			return template;// We did not find the template
		}
		int stubEndIdx = -1;
		String stubPlaceholderStr = "";
		String textToReturn = template;
		while (stubStartIdx >= 0) {
			/*
			 * If a stub name begin is found but end is not found then we throw
			 * an exception. Caller design time error
			 */
			stubEndIdx = textToReturn.indexOf(stubEndStr, stubStartIdx + 1);
			if (stubEndIdx < 0) {
				throw new ExilityException(
						"Cannot find end for the stub named " + stubName);
			}

			stubPlaceholderStr = textToReturn.substring(stubStartIdx,
					stubEndIdx + stubEndStr.length());
			/*
			 * We got the full placeholder, now replace this with the given
			 * value
			 */
			textToReturn = textToReturn.replace(stubPlaceholderStr, stubValue);
			stubStartIdx = textToReturn.indexOf(stubStartStr, stubEndIdx
					+ stubEndStr.length());
		}

		return textToReturn;
	}

	public static String getDataType(DataValueType valueType) {
		switch (valueType) {
		case BOOLEAN:
			return "boolean";
		case TEXT:
			return "String";
		case INTEGRAL:
			return "long";
		case DECIMAL:
			return "double";
		case DATE:
			return "Date";
		case TIMESTAMP:
			return "Date";
		case NULL:
			return "String";
		default:
			break;
		}
		return "String";
	}
}
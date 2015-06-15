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

class ExilityParseException extends ExilityException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static final String OPERAND_EXPECTED = "A variable or a constant is expected";

	static final String INVALID_CHARACTER = "Not a valid character";

	static final String NO_OPEN_BRACKET = "This close bracket does not have a corresponding open bracket";

	static final String UNCLOSED_BRACKET = "One or more open brackets have no matching close brackets";

	static final String NO_MATCHING_QUOTE = "No matching quotaiton mark";

	static final String CHARACTER_NOT_EXPECTED_HERE = "This character is not approapriate here";

	static final String NOTHING_TO_PARSE = "There was nothing to parse";

	static final String OPERATOR_EXPECTED = "expecting an operator : +,-,*,/,%, ^ , <, > = or ! expetced ";

	static final String INVALID_DATE = "Date fields are of the form 'YYYY-MM-DD'. Note that the quotes are required";

	ExilityParseException(int errorPosition, String exp, String errorCause) {
		super("Parse Exception at " + errorPosition + "  " + errorCause + "\n"
				+ exp.substring(0, errorPosition) + "^^^"
				+ exp.substring(errorPosition));
	}

	ExilityParseException(String arg0) {
		super(arg0);
	}
}
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

class Chars {
	// operators
	static final char AND = '&';
	static final char OR = '|';
	static final char NOT = '!';
	static final char ADD = '+';
	static final char SUBTRACT = '-';
	static final char MULTIPLY = '*';
	static final char DIVIDE = '/';
	static final char REMAINDER = '%';
	static final char POWER = '^';

	// comparators
	static final char EQUAL = '=';
	static final char GREATER = '>';
	static final char LESS = '<';
	static final char EXISTS = '?';

	// white spaces
	static final char SPACE = ' ';
	static final char CARRIAGE_RETURN = '\r';
	static final char TAB = '\t';
	static final char NEW_LINE = '\n';

	// brackets
	static final char BRACKET_OPEN = '(';
	static final char CURLY_OPEN = '{';
	static final char SQUARE_OPEN = '[';
	static final char BRACKET_CLOSE = ')';
	static final char CURLY_CLOSE = '}';
	static final char SQUARE_CLOSE = ']';

	// quotes
	static final char TEXT_QUOTE = '"';
	static final char TEXT_QUOTE1 = '\u201d';// character value is [Ó] and
												// unicode \u201d
	static final char[] QUOTES = { Chars.TEXT_QUOTE, Chars.TEXT_QUOTE1 };

	static final char DATE_QUOTE = '\'';

	// other convenient names
	static final char DIGIT_START = '0';
	static final char DIGIT_END = '9';
	static final char LCHAR_START = 'a';
	static final char LCHAR_END = 'z';
	static final char UCHAR_START = 'A';
	static final char UCHAR_END = 'Z';
	static final char UNDERSCORE = '_';
	static final char DOT = '.';

	static boolean isDigit(char c) {
		return ((c >= DIGIT_START) && (c <= DIGIT_END));
	}

	static boolean isNumeric(char c) {
		return (((c >= DIGIT_START) && (c <= DIGIT_END)) || (c == DOT));
	}

	static boolean isAlphaNumeric(char c) {
		return (((c >= LCHAR_START) && (c <= LCHAR_END))
				|| ((c >= UCHAR_START) && (c <= UCHAR_END))
				|| ((c >= DIGIT_START) && (c <= DIGIT_END)) || (c == DOT) || (c == UNDERSCORE));
	}

	static boolean isAlpha(char c) {
		return (((c >= LCHAR_START) && (c <= LCHAR_END))
				|| ((c >= UCHAR_START) && (c <= UCHAR_END))
				|| (c == UNDERSCORE) || (c == DOT));
	}

	static boolean isOpenBracket(char c) {
		return ((c == BRACKET_OPEN) || (c == SQUARE_OPEN) || (c == CURLY_OPEN));
	}

	static boolean isCloseBracket(char c) {
		return ((c == BRACKET_CLOSE) || (c == SQUARE_CLOSE) || (c == CURLY_CLOSE));
	}

	static boolean isTextQuote(char c) {
		for (char ch : Chars.QUOTES) {
			if (c == ch) {
				return true;
			}
		}
		return false;
	}

	/**
	 * get index of a char from the supplied list
	 * 
	 * @param chars
	 *            list of characters to search for
	 * @param startIdx
	 *            0 based index to start searching for
	 * @param textToSearchIn
	 *            string to search in.
	 * @return index at which one of the characters is found. -1 if not found
	 */
	public static int getCharIndex(char[] chars, int startIdx,
			String textToSearchIn) {

		if (null == textToSearchIn || textToSearchIn.length() == 0
				|| chars.length == 0) {
			return -1;
		}

		for (char c : chars) {
			int charIdx = textToSearchIn.indexOf(c, startIdx);
			if (charIdx >= 0) {
				return charIdx;
			}
		}
		return -1;
	}

	static boolean isDateQuote(char c) {
		return ((c == DATE_QUOTE));
	}

	static boolean isOperator(char c) {
		return ((c == ADD) || (c == SUBTRACT) || (c == MULTIPLY)
				|| (c == DIVIDE) || (c == POWER) || (c == REMAINDER)
				|| (c == EQUAL) || (c == LESS) || (c == GREATER) || (c == NOT)
				|| (c == OR) || (c == AND) || (c == EXISTS));
	}

	static boolean isWhiteSpace(char c) {
		if ((c == SPACE) || (c == TAB) || (c == NEW_LINE)
				|| (c == CARRIAGE_RETURN)) {
			return true;
		}
		return false;
	}

	static boolean isUnaryOperator(char c) {
		return ((c == NOT) || (c == SUBTRACT) || (c == EXISTS));
	}
}
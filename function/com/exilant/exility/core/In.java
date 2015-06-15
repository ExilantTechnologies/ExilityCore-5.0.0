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

/***
 * Checks whether the first parameter exists in the second parameter that is a
 * comma separated list.
 * 
 */
public class In implements FunctionInterface {
	@Override
	public Value evaluate(Value[] inParms) {
		if (inParms.length != 2) {
			Spit.out("In(value, commaSeparatedList) is the usage. You have provided "
					+ inParms.length + " parameters");
			return Value.newValue(false);
		}

		String s1 = inParms[0].getTextValue();
		String s2 = inParms[1].getTextValue();

		if (s1.length() == 0) {
			return Value.newValue(true);
		}

		if (s2.length() == 0) {
			return Value.newValue(false);
		}

		s1 = ',' + s1 + ',';
		s2 = ',' + s2 + ',';
		if (s2.indexOf(s1) >= 0) {
			return Value.newValue(true);
		}

		return Value.newValue(false);
	}
}

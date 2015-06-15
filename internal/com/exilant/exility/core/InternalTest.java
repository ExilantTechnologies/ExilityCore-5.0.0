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
 * Temp class used for testing from client
 * 
 */
public class InternalTest implements CustomCodeInterface {

	@Override
	public int execute(DataCollection dc, DbHandle dbHandle, String gridName,
			String[] parameters) {
		int n = (int) (Math.random() * 100);
		for (int i = 0; i < 10; i++) {
			Spit.out(n + "  :  " + i + "   spitted");
			try {
				Thread.sleep((int) (Math.random() * 1000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return 1;
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.NONE;
	}
}

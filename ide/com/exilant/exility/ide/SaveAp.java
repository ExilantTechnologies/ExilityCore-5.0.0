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
package com.exilant.exility.ide;

import com.exilant.exility.core.AP;
import com.exilant.exility.core.ApplicationParameters;
import com.exilant.exility.core.CustomCodeInterface;
import com.exilant.exility.core.DataAccessType;
import com.exilant.exility.core.DataCollection;
import com.exilant.exility.core.DbHandle;
import com.exilant.exility.core.ObjectManager;
import com.exilant.exility.core.ResourceManager;

/***
 * Custom code that saves application parameters
 * 
 * @author Exilant Technologies
 * 
 */
public class SaveAp implements CustomCodeInterface {

	@Override
	public int execute(DataCollection dc, DbHandle dbHandle, String gridName,
			String[] parameters) {
		ApplicationParameters ap = new ApplicationParameters();
		// parse attributes from dc
		ObjectManager.fromDc(ap, dc);
		// let us push latest values to this instance of VM
		AP.setInstance(ap);
		// and, of course, save it
		ResourceManager.saveResource(AP.parametersFileName, ap);
		return 1;
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.NONE;
	}

}

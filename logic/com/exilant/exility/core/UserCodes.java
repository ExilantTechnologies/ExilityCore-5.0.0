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

import java.util.HashMap;
import java.util.Map;

/***
 * Original design of CratfedLogic, CustomCode and UserTask is all confusing.
 * Hence deprecated, and created a clear new CustomCode class for clarity
 * 
 * @author raghu.bhandi
 * 
 */
@Deprecated
public class UserCodes {
	private static UserCodes singleton = null;
	Map<String, UserCodeEntry> codes = new HashMap<String, UserCodeEntry>();

	static {
		UserCodes.load();
	}

	/***
	 * Get an instance of this userCode. Note that this used to return a
	 * singleton earlier. Is refactored to return a new instance for each call.
	 * 
	 * @param userCodeName
	 * @param dc
	 * @return new instance of userCode class.
	 * @throws ExilityException
	 */
	static UserCodeInterface getUserCode(String userCodeName, DataCollection dc)
			throws ExilityException {
		UserCodeInterface userCode = null;
		UserCodeEntry entry = null;
		entry = UserCodes.singleton.codes.get(userCodeName);
		if (entry != null) {
			userCode = (UserCodeInterface) ObjectManager.createNew(
					entry.nameSpace, userCodeName);
		}
		if (userCode == null) {
			dc.raiseException("exilNoSuchUserCode", userCodeName);
		}

		return userCode;
	}

	static void load() {
		UserCodes.singleton = (UserCodes) ResourceManager.loadResource(
				"userCodes", UserCodes.class);
	}

	// SMCORE
	static UserCodes getInstance() {
		return UserCodes.singleton;
	}

}

@Deprecated
class UserCodeEntry {
	String name = null;
	String nameSpace = null;
	String description = null;
	UserCodeInterface userCode = null;
}

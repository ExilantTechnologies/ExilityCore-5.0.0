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
 * Hash map extended to suit Exility's requirement. Idea is to hide all the
 * internals into this class rather than expecting the user classes to take care
 * of it. that saves values with string as key, and assumes keys are case
 * insensitive, but the original case of teh the is retained. HashMAp is
 * extended with the following features 1. It is a map of String-Object, (Not
 * object-object) 2. put(key, value) checks if there is already an entry with a
 * key that matches in a case-insensitive way. If found, such an entry is
 * replaced. (both key and value are replaced) 3. get(key) will get the value by
 * matching key in a case-insensitive way. 4. similar behavior for contains(key)
 * and remove(key)
 * 
 * This case-insensitive feature is achieved with the help of an additional map.
 * 
 * @author Exilant Technologies
 * 
 *         Type of value to be stored in this map
 * @param <V>
 */
public class ExilityMap<V> extends HashMap<String, V> {
	private static final long serialVersionUID = 4031829922930165340L;
	/***
	 * map of lower-cased key to case-retained key. This helps us in looking-up
	 * for a key.
	 */
	private final Map<String, String> caseInsensitiveKeys = new HashMap<String, String>();

	@Override
	public V put(String key, V value) {
		this.caseInsensitiveKeys.put(key.toLowerCase(), key);
		return super.put(key, value);
	}

	@Override
	public V get(Object key) {
		String actualKey = this.caseInsensitiveKeys.get(key.toString()
				.toLowerCase());
		if (actualKey == null) {
			return null;
		}

		return super.get(actualKey);
	}

	@Override
	public boolean containsKey(Object key) {
		return this.caseInsensitiveKeys.containsKey(key.toString()
				.toLowerCase());
	}

	@Override
	public V remove(Object key) {
		String lowerKey = key.toString().toLowerCase();
		String existingKey = this.caseInsensitiveKeys.remove(lowerKey);
		if (existingKey == null) {
			return null;
		}

		return super.remove(existingKey);
	}

	@Override
	public void clear() {
		this.caseInsensitiveKeys.clear();
		super.clear();
	}
}

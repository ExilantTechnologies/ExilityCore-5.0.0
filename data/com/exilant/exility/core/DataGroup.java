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

/***
 * represents a data group, which is an Exility design entity
 * 
 */
public class DataGroup {
	/***
	 * name of the group, that has to be unique across the dictionary. All
	 * elements within this group may have to be prefixed with this name + '_'
	 * if project does not use uniqueNameAcrtossGroups
	 */
	String name;

	/***
	 * English label,in case this is to be rendered
	 */
	String label;

	/***
	 * documentation
	 */
	String businessDescription;
	/***
	 * documentation
	 */
	String technicalDescription;

	/***
	 * all data elements within this group
	 */
	HashMap<String, DataElement> elements = new HashMap<String, DataElement>();
}

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

/**
 * represents a parameter than can be passed into page at the time of navigating
 * to it
 * 
 */
public class PageParameter implements ToBeInitializedInterface {
	/**
	 * name with which this is passed to this page in the query string part of
	 * url
	 */
	String name = null;

	/**
	 * if this is optional and user opts out, we use this value
	 */
	String defaultValue = null;

	/**
	 * is this mandatory?
	 */
	boolean isRequired = false;

	/**
	 * in case this name is different from the actual data element that it
	 * refers to
	 */
	String dataElementName = null;
	/**
	 * setTo is optional, and defaults to name. This is the field to which the
	 * value is to be set to
	 */
	String setTo = null;

	/**
	 * decides whether this page is to be invoked in "new/add" mode or
	 * "edit/modify" mode. If all keys are passed, then we assume this to be
	 * edit mode, else new mode.
	 */
	boolean isPrimaryKey = false;

	/**
	 * for documentation
	 */
	String description = null;

	/**
	 * use this instead of dictionary
	 */
	public String dataType = null;

	/**
	 * marked during page generation
	 */
	boolean inError = false;

	private static final String[] ATTR_NAMES = { "name", "defaultValue",
			"isRequired", "dataType", "setTo", "isPrimaryKey" };

	/**
	 * default
	 */
	public PageParameter() {
	}

	/**
	 * get the data type
	 * 
	 * @return
	 */
	String getDataType() {
		return this.dataType;
	}

	/**
	 * generate js for this parameter
	 * 
	 * @param js
	 * @param pc
	 */
	void toJavaScript(StringBuilder js, PageGeneratorContext pc) {
		js.append('\n').append(Page.JS_VAR_NAME)
				.append(" = new PM.PageParameter();");
		pc.setAttributes(this, js, PageParameter.ATTR_NAMES);
	}

	@Override
	public void initialize() {
		if (this.dataType != null) {
			return;
		}
		String name1 = (this.dataElementName != null) ? this.dataElementName
				: this.name;
		DataElement de = DataDictionary.getElement(name1);
		if (de == null) {
			Spit.out("Error: Page Parameter "
					+ this.name
					+ " does not exist in dictioanry, nor is it linked to another data element name.");
			this.inError = true;
			de = DataDictionary.getDefaultElement(this.name);
		}
		AbstractDataType dt = DataDictionary.getDataType(name1);
		this.dataType = dt.name;
	}
}

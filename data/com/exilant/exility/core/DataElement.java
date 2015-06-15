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
 * Represents a data element, or a piece of information in the application TODO:
 * not sure why we made all attributes public. Default design was to have all
 * attributes package-private
 */
public class DataElement implements Cloneable {
	private static final String BEGIN_XML = "\n\t\t\t\t<dataElement name=\"";
	private static final String END_XML = " />";

	/**
	 * if this name is already defined in another group, but you want to mention
	 * that this name is also used in this group, use this attribute. Note that
	 * once you give reference, you should not give any other attributes. They
	 * are ignored anyways.
	 */
	String referenceGroup = null;

	/***
	 * normally unique in the application, unless the project prefers to use all
	 * names as groupName.name, in which case this is unique within a group
	 */
	public String name;

	/***
	 * this the default label unless over-ridden at page/report level
	 */
	public String label;

	/***
	 * Best place to document this data element from business perspective
	 */
	public String businessDescription;

	/***
	 * Best place to document any design/internal details about this data
	 * element
	 */
	public String technicalDescription;

	/**
	 * description, as seen by end-users
	 */
	public String description;
	/***
	 * Is this a non-business data element? Is it introduced because of
	 * technical/internal reasons? If set to true, it implies that this data
	 * element has no relevance/meaning to the business need o
	 */
	public boolean isInternalElement;

	/***
	 * data type as described in dataTypes.xml
	 */
	public String dataType;

	/***
	 * Is this data element associated with a drop-down? This will be used
	 * automatically whenever this data element is used in a page.
	 */
	public String listServiceName;

	/***
	 * If this field can take set of design-time determined values, this is the
	 * place.
	 */
	public String valueList;

	/***
	 * Do you want a custom error message for this field on client? Note that
	 * you have to mark this message as forClient in messages.xml
	 */
	String messageName;

	/**
	 * message if user clicks on additional help on the message panel.
	 */
	String additionalMessageName;
	/***
	 * Do you want to format this field on the client?
	 */
	String formatter;
	/***
	 * List of custom label that can be set at the installation time in a
	 * product scenario TODO: needs to be re-factored. All attributes are
	 * currently public because of this.
	 */
	public HashMap<String, CustomLabel> customLabels = new HashMap<String, CustomLabel>();

	/**
	 * method specifically added for auto-generating missing data element names
	 * in the project. Adds required xml for a missing name based on this data
	 * element. label, dataType and listServiceName are the only fields that are
	 * referenced thru dataElementName in page.xml, and hence we copy only these
	 * attributes
	 * 
	 * @param sbf
	 * @param missingName
	 */
	public void addXml(StringBuilder sbf, String missingName) {
		sbf.append(BEGIN_XML);
		sbf.append(missingName).append("\" dataType=\"");
		sbf.append(this.dataType).append("\" ");
		if (this.label != null && this.label.length() > 0) {
			sbf.append("label=\"").append(ObjectManager.xmlEscape(this.label))
					.append("\" ");
		}
		if (this.listServiceName != null && this.listServiceName.length() > 0) {
			sbf.append("listServiceName=\"").append(this.listServiceName)
					.append("\" ");
		}
		sbf.append(END_XML);
	}

	/**
	 * @return a clone of this class. null in case of error while cloning.
	 * 
	 */
	public DataElement getAClone() {
		try {
			return (DataElement) this.clone();
		} catch (CloneNotSupportedException e) {
			//
		}
		Spit.out("Trouble cloning data element " + this.name
				+ " going to return a default one");
		return null;
	}

	/**
	 * is this compatible with other data element? We say they are compatible if
	 * they have the same value type
	 * 
	 * @param dataElement
	 *            to be checked for compatibility
	 * 
	 * @return true of the two have the same value type, false otherwise
	 */
	public boolean isCompatibleWith(DataElement dataElement) {
		AbstractDataType d1 = DataTypes.getDataTypeOrNull(this.dataType);
		AbstractDataType d2 = DataTypes.getDataTypeOrNull(dataElement.dataType);
		if (d1 != null && d2 != null && d1.getValueType() == d2.getValueType()) {
			return true;
		}
		Spit.out(this.name + " is of data type " + this.dataType
				+ " and it is not compatible with " + dataElement.name
				+ " of datatype " + dataElement.dataType + " because  "
				+ (d1 == null ? "null" : d1.getValueType())
				+ " is not same as "
				+ (d2 == null ? "null" : d2.getValueType()));
		return false;
	}
}

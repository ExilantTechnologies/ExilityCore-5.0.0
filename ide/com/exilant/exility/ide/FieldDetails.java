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

import com.exilant.exility.core.DataValueType;

/**
 * this is a data structure to hold information about a field in a page for some
 * utility purposes
 * 
 */
public class FieldDetails {
	/**
	 * resource where we found this
	 */
	public final String resourceName;

	/**
	 * field name
	 */
	public final String name;

	/**
	 * issue with this field
	 */
	public final FieldIssue issue;
	/**
	 * data element used by this field
	 */
	public final String dataElementName;

	/**
	 * data type used by data element
	 */
	public final String dataTypeName;

	/**
	 * data value type of data element;
	 */
	public final DataValueType dataValueType;

	/**
	 * attributes when this is consolidated across pages
	 */
	public int nbrUsageAcrossResources = 1;
	/**
	 * if this field is used across pages
	 */
	public String commaSeparatedResourceNames;

	/**
	 * any info that is related to the filter being used
	 */
	public String filterBasedInfo;

	private String myString;
	private final char DELIMITER = '\t';

	/**
	 * You have to build this data structure all in one go
	 * 
	 * @param pageName
	 * @param name
	 * @param issue
	 * @param dataElementName
	 * @param dataTypeName
	 * @param dataValueType
	 */
	public FieldDetails(String pageName, String name, FieldIssue issue,
			String dataElementName, String dataTypeName,
			DataValueType dataValueType) {
		this.name = name;
		this.resourceName = pageName;
		this.dataElementName = dataElementName;
		this.dataTypeName = dataTypeName;
		this.dataValueType = dataValueType;
		this.issue = issue;

		this.nbrUsageAcrossResources = 1;
		this.commaSeparatedResourceNames = pageName;

		/*
		 * let us keep the toString() ready
		 */
	}

	@Override
	public String toString() {
		if (this.myString == null) {
			StringBuilder sbf = new StringBuilder(this.resourceName);
			sbf.append(this.DELIMITER).append(this.name).append(this.DELIMITER)
					.append(this.issue).append(this.DELIMITER)
					.append(this.issue).append(this.DELIMITER);

			if (this.dataElementName != null) {
				sbf.append(this.dataElementName);
			}
			sbf.append(this.DELIMITER);
			if (this.dataTypeName != null) {
				sbf.append(this.dataTypeName);
			}
			sbf.append(this.DELIMITER);
			if (this.dataValueType != null) {
				sbf.append(this.dataValueType);
			}
			sbf.append(this.DELIMITER).append(this.nbrUsageAcrossResources)
					.append(this.DELIMITER)
					.append(this.commaSeparatedResourceNames);

			this.myString = sbf.toString();
		}
		return this.myString;
	}
}

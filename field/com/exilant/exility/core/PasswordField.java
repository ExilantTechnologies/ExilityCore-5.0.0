/********************************************************************************************************
 *
 * EX!LANT CONFIDENTIAL
 * _______________________________________________________________________________________________________
 *
 *  [2004 - 2023]  EX!LANT Technologies Pvt. Ltd.
 *  All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of EX!LANT Technologies Pvt. Ltd.
 * and its suppliers,* if any. The intellectual and technical concepts contained herein are proprietary to
 * EX!LANT Technologies Pvt. Ltd.and its suppliers and may be covered by India and Foreign Patents,
 * patents in process, and are protected by trade secret or
 * copyright law.Dissemination of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from EX!LANT Technologies Pvt. Ltd.
 *********************************************************************************************************/
package com.exilant.exility.core;

/**
 * password field, as defined in html.
 */
class PasswordField extends AbstractInputField {
	int size = 20;

	@Override
	void fieldToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		sbf.append("\n<input type=\"password\" ");
		if (this.cssClassName == null) {
			sbf.append("class=\"inputfield\" ");
		}

		if (this.size > 0) {
			sbf.append(" size=\"").append(this.size).append("\" ");
		}
		if (this.defaultValue != null) {
			sbf.append(" value=\"").append(this.defaultValue).append("\" ");
		}

		int len = DataTypes.getDataType(this.dataType, null).getMaxLength();
		if (len > 0) {
			sbf.append(" maxlength=\"").append(len).append("\" ");
		}
		super.addMyAttributes(sbf, pageContext);

		sbf.append("/>");

	}
}

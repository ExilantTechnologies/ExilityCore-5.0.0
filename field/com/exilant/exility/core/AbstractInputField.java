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
 * represents a field that the user can modify and finally submit to server.
 * Several of the concrete classes are not editable though.
 * 
 */
abstract class AbstractInputField extends AbstractField {

	/**
	 * action to be taken when the field changes, either due to an internal
	 * calculation, or when user changes it
	 */
	String onChangeActionName = null;

	/**
	 * action to be taken when this field gets input focus
	 */
	String onFocusActionName = null;

	/**
	 * action to be taken when this field looses input focus
	 */
	String onBlurActionName = null;

	/**
	 * action to be taken when the user changes the value of this field
	 */
	String onUserChangeActionName = null;

	/**
	 * if this field is optional, but if another field has value then this field
	 * becomes mandatory.
	 */
	String basedOnField = null;

	/**
	 * a refinement in basedOnField. This field becomes mandatory when the other
	 * field has a specific value
	 */
	String basedOnFieldValue = null;

	/**
	 * when a value is set to this field, simply copy the field to another one,
	 * but only if it is empty at that time. Useful to provide value of one
	 * field as a default for another
	 */
	String copyTo = null;

	/**
	 * you have reason to suppress validation of this field. May be it is more
	 * complex and you want to do it thru yoyr script.
	 */
	boolean doNotValidate = false;

	/**
	 * some case where the server has a wrong value, and you want to live with
	 * that. However, if user changes it, it must be valid.
	 */
	boolean validateOnlyOnUserChange = false;

	/**
	 * additional validation to be carried out for this field
	 */
	String validationFunction = null;

	/**
	 * some drop-down may depend on the value of this field. Whenever this field
	 * is changed, we may have to trigger a list service to get valid list of
	 * values. for example, if this field is country code, state would be a
	 * dependent selection field.
	 */
	String[] dependentSelectionField = null;

	/***
	 * do you want to override the error message flashed by client when this
	 * field fails data type validation? You should set forClient for this
	 * message in messages.xml
	 */
	String messageName;

	/**
	 * help text to be displayed when user clicks on additional help when user
	 * sees an error message
	 */
	String additionalMessageName;

	/**
	 * is this the operator field for a filter field? This is internally set by
	 * FilterField
	 */
	boolean isFilterOperator = false;

	/**
	 * internally set by exility to do any special processing when the user tabs
	 * out of the last field in a grid
	 */
	private boolean isLastField = false;

	/**
	 * set this field as the last field in the grid
	 */
	void setLastField() {
		this.isLastField = true;

	}

	/**
	 * ooops. problem with naming, but that is the best we could do after naming
	 * the field to start with "is:
	 * 
	 * @return
	 */
	boolean getIslastField() {
		return this.isLastField;
	}

	@Override
	void addMyAttributes(StringBuilder sbf, PageGeneratorContext pageContext) {
		super.addMyAttributes(sbf, pageContext);
		this.addMyAttributesOnly(sbf, pageContext);
	}

	/**
	 * Bit of an issue with radio button. Group span and input tags share the
	 * attributes of the field. Requires a good re-factoring, but as an interim,
	 * I am allowing radio to skip MyAttributes and access my parents
	 * attributes.
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	void addMyParentsAttributes(StringBuilder sbf,
			PageGeneratorContext pageContext) {
		super.addMyAttributes(sbf, pageContext);
	}

	/**
	 * sets attributes at abstractInputField, and does not trigger parent's
	 * method
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	void addMyAttributesOnly(StringBuilder sbf, PageGeneratorContext pageContext) {
		String myName = pageContext.getName(this.name);
		sbf.append(" onchange=\"P2.fieldChanged(this, '").append(myName)
				.append("');\" ");

		if ((this.htmlAttributes == null)
				|| (!this.htmlAttributes.contains("onkeydown="))) {
			if (AP.lastKeyEventTrigger && pageContext.isInsideGrid
					&& this.isLastField) {
				sbf.append(" onkeydown=\"P2.handleGridNav(this, '")
						.append(myName)
						.append("', event);P2.keyPressedOnField(this, '")
						.append(myName).append("', event);\" ");
			} else if (pageContext.isInsideGrid) {
				sbf.append(" onkeydown=\"P2.handleGridNav(this, '")
						.append(myName).append("', event);\" ");
			}
		} else {
			if (AP.lastKeyEventTrigger && pageContext.isInsideGrid
					&& this.isLastField) {
				sbf.append(" onkeydown=\"P2.keyPressedOnField(this, '")
						.append(myName).append("', event);\" ");
			}
		}
		if (this.focusAndBlurNeeded() || pageContext.isInsideGrid
				|| (this.onFocusActionName != null)) {
			sbf.append(" onfocus=\"P2.fieldFocussed(this, '").append(myName)
					.append("');\" ");
		}
		if (this.focusAndBlurNeeded() || this.onBlurActionName != null) {
			sbf.append(" onblur=\"P2.inputFocusOut(this, '").append(myName)
					.append("');\" ");
		}

		/**
		 * placeholder in html 5
		 */
		if (this.hoverText != null && pageContext.useHtml5) {
			sbf.append(" placeholder=\"").append(this.hoverText).append("\" ");
		}
	}

	/**
	 * We have been adding onfocus and onblur only on need basis.
	 * commonInputField requires it. With so many projects using
	 * htmlAttribute="arbitraryAttribute" I am not comfortable adding these
	 * events always. Hence this method added.
	 */
	protected boolean focusAndBlurNeeded() {
		return false;
	}

	/**
	 * add input field specific meta data
	 * 
	 * @param js
	 * @param pageContext
	 */
	@Override
	void fieldToJs(StringBuilder js, PageGeneratorContext pageContext) {
		if (this.isLocalField == true) {
			pageContext.setJsAttribute(js, "toBeSentToServer", "false");
		}

		// messageName should default to dataElement
		String msgName = this.messageName;
		if (msgName == null) {
			msgName = this.dataElement.messageName;
		}
		this.addMessageJs(js, pageContext, "messageName", msgName);

		msgName = this.additionalMessageName;
		if (msgName == null) {
			msgName = this.dataElement.additionalMessageName;
		}
		this.addMessageJs(js, pageContext, "additionalMessageName", msgName);
	}

	/**
	 * add a message to the json
	 * 
	 * @param js
	 * @param pageContext
	 * @param attName
	 * @param msgName
	 */
	void addMessageJs(StringBuilder js, PageGeneratorContext pageContext,
			String attName, String msgName) {
		if (msgName == null) {
			return;
		}

		pageContext.setJsTextAttribute(js, attName, msgName);
		Message msg = Messages.getMessage(msgName);
		if (msg == null) {
			String msgText = this.name + " uses custom error message "
					+ msgName + ". This message is not defined.";
			Spit.out(msgText);
			pageContext.reportError(msgText);
		} else if (msg.forClient == false) {
			String msgText = this.name
					+ " uses custom error message "
					+ msgName
					+ ". This message is not marked with forClient=true. This message will not be available on the client.";
			Spit.out(msgText);
			pageContext.reportError(msgText);
		}
	}
}

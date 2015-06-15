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

import java.util.Map;

/**
 * where do we put the label for a field?
 * 
 */
enum LabelPosition {
	left, top, merge, hide, none
}

/***
 * Field is an element that holds some data that can be set at run time.(element
 * that is not a field is completely fixed at design-time.
 */
abstract class AbstractField extends AbstractElement {
	/***
	 * all attributes that are to be set for the client data model. we have
	 * chosen this design. Alternate design would be to use annotations. We are
	 * comfortable with this design at this time.
	 */
	static final String[] ALL_META_ATTRIBUTES = {
			"altKey",
			"baseSrc",
			"blankOption",
			"basedOnFieldValue",
			"columnAverage",
			"columnSum",
			"codePickerSrc",
			"dataType",
			"doNotValidate",
			"defaultValue",
			"descServiceId",
			"descQueryFields",
			"doNotMatchDescNames",
			"imageExtension",
			"isFilterOperator",
			"isUniqueField",
			"isLocalField",
			"isRequired",
			"keyValue",
			"listServiceId",
			// "label", label is calculated and added based on logic
			"mailTo", "maxCharacters", "onChangeActionName",
			"onFocusActionName", "onBlurActionName", "noAutoLoad", "repeat",
			"rowSum", "rowAverage", "selectFirstOption", "toBeSentToServer",
			"uniqueKey", "uncheckedValue", "checkedValue", "isChecked",
			"minCharsToTriggerService", "selectionValueType",
			"multipleSelection", "sameListForAllRows", "globalFieldName",
			"globalFieldLabelName", "isValid", "formatter",
			"onUserChangeActionName", "validateOnlyOnUserChange",
			"breakToNextLine", "aliasName", "isFilterField", "minSelections",
			"maxSelections", "keyList", "validateQueryFields", "bulkCheck",
			"trueValue", "falseValue", "supressDescOnLoad",
			"comboDisplayFields", "reportServiceId", "chartType",
			"xaxiscolumn", "yaxiscolumn", "isMultiDataSet", "groupbycolumn",
			"xaxislabel", "yaxislabel", "xaxislabels", "yaxislabels",
			"columnName", "displayXML", "valueList", "sortable", "filterable",
			"showValues", "expandAllOnLoad", "showLegend",
			"yaxislabelformatterid", "minx", "miny", "maxx", "maxy",
			"fieldToFocusAfterExecution", "rowSumFunction",
			"rowAverageFunction", "columnSumFunction", "columnAverageFunction",
			"childHtmlAttributes", "bubblecolumn", "bubbleradiusdenominator",
			"codePickerLeft", "codePickerTop", "donotTrackChanges",
			"validationFunction", "suggestionServiceId",
			"suggestAfterMinChars", "suggestionCss", "firstQualitativeRange",
			"secondQualitativeRange", "valueOfInterest", "comparativeValue",
			"bulletlabelcolumn", "distributionvaluecolumn", "fromcolumn",
			"tocolumn", "corecolumn", "level1column", "level2column",
			"matchStartingChars", "listCss", "allowHtmlFormattedText",
			"defaultCss"
			/* legend options for chart field */
			, "legendNbrColumns", "legendLabelFormatter",
			"legendLabelBoxBorderColor", "legendPosition", "legendContainer",
			"legendMargin", "legendBackgroundColor", "legendBackgroundOpacity",
			"rawDataDisplay", "colors", "helpTextColumn",
			"groupHelpTextColumn", "markedAsComment", "highlightColor",
			"labelColor", "showFilledPoints", "columnIndexesToShow",
			"onClickFunctionName", "minPercentToShowLabel",
			"showMoreFunctionName", "stacking", "direction", "marginLeft",
			"marginBottom", "onMoveFunctionName", "legendHighlight",
			"yLabelMaxWidth", "xLabelFormatterFunction",
			"yLabelFormatterFunction" };
	/***
	 * These are field names, that are to be fully-qualified (with table name as
	 * prefix) before setting them as client-side attributes
	 */
	static final String[] ALL_TABLE_SENSITIVE_ATTRIBUTES = { "basedOnField",
			"copyTo", "fromField", "name", "toField", "otherField",
			"suggestionDescriptionField" };

	/***
	 * These are array that contain field names, that are to be fully-qualified
	 * (with table name as prefix) before setting them as client-side attributes
	 */
	static final String[] ALL_TABLE_SENSITIVE_ARRAYS = { "descFields",
			"dependentSelectionField", "descQueryFieldSources",
			"listServiceQueryFieldNames", "listServiceQueryFieldSources",
			"suggestionServiceFields", "suggestionServiceFieldSources" };

	/**
	 * where should the label be rendered?
	 */
	LabelPosition labelPosition = LabelPosition.left;
	/**
	 * add check-box in the table header so that all rows can be
	 * selected/de-selected with this one check-box.
	 */
	boolean bulkCheck = false;

	/**
	 * used if the field is optional, and user does not specify the value. Note
	 * that it is not intuitive to make a field mandatory, and provide a default
	 * value
	 */
	String defaultValue = null;

	/**
	 * sorry about two description fields, when in reality, programmers don't
	 * even use one!!
	 */
	String technicalDescription = null;

	/**
	 * sorry about two description fields, when in reality, programmers don't
	 * even use one!!
	 */
	String businessDescription = null;

	/**
	 * to be avoided. field name itself should be the data element name, in
	 * which case you leave this null. You may resort to this only if this field
	 * is local to this page, and is not really a business field.
	 */
	String dataElementName = null;

	/**
	 * if this is an instance of one of the global fields, (like company,
	 * division etc..) that may have different labels based on project settings
	 */
	String globalFieldName = null;
	/**
	 * label name to use fromglobalFieldName
	 */
	String globalFieldLabelName = null;

	/**
	 * is this mandatory?
	 */
	boolean isRequired = false;

	/**
	 * hot key to get input focus to this field. Idea is to provide feature like
	 * alt+l should go to "login Id" etc..
	 */
	String altKey = null;

	/**
	 * should we issue a br tag before rendering this field. You may need this
	 * if you are rendering more than one field in a cell in a list or grid
	 */
	boolean breakToNextLine = false;

	/**
	 * do you want to send data to server with a different name than this one?
	 */
	String aliasName = null;

	/**
	 * exility tracks whether user changed any field and enable/disable buttons
	 * based on that. Also, asks for confirmation if user leaves the page. You
	 * mark this field as not important for this purpose.
	 */
	boolean donotTrackChanges = false;

	// rest of the attributes are applicable if they are inside a list/grid

	/**
	 * does this field repeat in a list/grid ?
	 */
	String repeat = null;

	/**
	 * if row sum is chosen for the table, this field can skip that or
	 * participate using this attribute
	 */
	boolean rowSum = false;
	/**
	 * if row average is chosen for the table, this field can skip that or
	 * participate using this attribute
	 */
	boolean rowAverage = false;

	/**
	 * if column sum is chosen for the table, this field can skip that or
	 * participate using this attribute
	 */
	boolean columnSum = false;
	/**
	 * if column average is chosen for the table, this field can skip that or
	 * participate using this attribute
	 */
	boolean columnAverage = false;

	/**
	 * this field may have a special way of dealing with sum/average. provide
	 * the name of the function that is made available in the client page
	 */
	String rowSumFunction = null;
	/**
	 * this field may have a special way of dealing with sum/average. provide
	 * the name of the function that is made available in the client page
	 */
	String rowAverageFunction = null;
	/**
	 * this field may have a special way of dealing with sum/average. provide
	 * the name of the function that is made available in the client page
	 */
	String columnSumFunction = null;
	/**
	 * this field may have a special way of dealing with sum/average. provide
	 * the name of the function that is made available in the client page
	 */
	String columnAverageFunction = null;

	/**
	 * used internally by exility, and not specified by designer. These are
	 * cached attributes for efficiency sake.
	 */
	String formatter = null;

	/**
	 * used only by radio button
	 */
	boolean skipId = false;

	/**
	 * data type as determined based on data element
	 */
	String dataType = null;

	/**
	 * actual data element object instance cached.
	 */
	DataElement dataElement = null;

	/**
	 * value type based on data type cached.
	 */
	DataValueType valueType = null;

	/**
	 * field that is defined for some
	 */
	boolean isLocalField = false;

	/***
	 * Any attributes defined at this abstract level(and above) that need to be
	 * translated into HTML attributes..
	 */
	DataValueType getValueType() {
		return this.valueType;
	}

	/***
	 * add attributes to the tag. This is defined at every class hierarchy to
	 * take care of attributes defined at that level
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	@Override
	void addMyAttributes(StringBuilder sbf, PageGeneratorContext pageContext) {
		super.addMyAttributes(sbf, pageContext);
		// field name may change if we are inside a grid
		String fieldName = pageContext.getName(this.name);

		/*
		 * some peculiar case, like radio button, where id should not be added
		 */
		if (!this.skipId) {
			sbf.append(" id=\"").append(fieldName).append("\" ");
		}
	}

	/***
	 * Some common things to emit, after which sub-class specific Field.toHtml()
	 * is invoked.
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	@SuppressWarnings("deprecation")
	/*
	 * suppressing because of XMltree as of now.
	 */
	@Override
	void toHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		if (this.breakToNextLine) {
			sbf.append("<br />");
		}

		// if this is a grid/list, no need to worry about TD and all that..
		if (pageContext.renderFieldAsColumn) {
			if (pageContext.useHtml5) {
				this.fieldToHtml5(sbf, pageContext);
			} else {
				this.fieldToHtml(sbf, pageContext);
			}
			return;
		}

		/*
		 * we have to manage the tds properly
		 */
		boolean tdOpened = false;
		int units = this.numberOfUnitsToUse;

		if (units > 0) {
			if (this.labelPosition == LabelPosition.left
					|| this.labelPosition == LabelPosition.hide) {
				sbf.append("\n<td style=\"vertical-align: middle\" align=\"right\">");
				tdOpened = true;
			} else {
				sbf.append("\n<td style=\"vertical-align: middle\" colspan=\"")
						.append(units * 2).append("\">");
			}

			if (this.breakToNextLine) {
				sbf.append("<br />");
			}
		}

		String layoutType = pageContext.getLayoutType();
		if (!(this.getClass().equals(ChartField.class) || this.getClass()
				.equals(XmlTreeField.class))
				&& !(layoutType != null && layoutType.equals("5") && this
						.getClass().equals(CheckBoxField.class))) {
			this.labelToHtml(sbf, pageContext);
		}

		if (this.labelPosition == LabelPosition.top) {
			sbf.append("<br/>");
		} else if (tdOpened) {
			sbf.append("</td><td style=\"vertical-align: middle\" ");
			if (units > 1) {
				sbf.append(" colspan=\"").append(units * 2 - 1).append("\" ");
			}
			sbf.append(">");
		}

		if (pageContext.useHtml5) {
			this.fieldToHtml5(sbf, pageContext);
		} else {
			this.fieldToHtml(sbf, pageContext);
		}
	}

	@Override
	void toHtml5(StringBuilder sbf, PageGeneratorContext pageContext) {
		this.toHtml5(sbf, pageContext, 2);
	}

	/***
	 * Some common things to emit, after which sub-class specific Field.toHtml()
	 * is invoked
	 * 
	 * @param sbf
	 * @param pageContext
	 * @param nbrTdsToBeUsed
	 *            number of tds allocated to this field null;
	 */
	void toHtml5(StringBuilder sbf, PageGeneratorContext pageContext,
			int nbrTdsToBeUsed) {
		if (this.breakToNextLine) {
			sbf.append("<br />");
		}

		/*
		 * if this is a grid/list, no need to worry about TD and all that..
		 */
		if (pageContext.renderFieldAsColumn) {
			this.fieldToHtml5(sbf, pageContext);
			return;
		}

		/*
		 * we have to manage the tds properly
		 */
		boolean tdOpenedForLabel = false;
		int tdsToBeConsumed = nbrTdsToBeUsed;
		if (this.numberOfUnitsToUse > 0) {
			/*
			 * we have to start a td for sure.
			 */
			if (this.labelPosition == LabelPosition.left
					|| this.labelPosition == LabelPosition.hide) {
				/*
				 * label is its own label
				 */
				sbf.append("\n<td class=\"labelCell\" id=\"").append(this.name)
						.append("LabelCell\" >");
				tdOpenedForLabel = true;
				tdsToBeConsumed--; // we ate one of them :-)
			} else {
				sbf.append("\n<td class=\"mergedCell\" id=\"")
						.append(this.name).append("MergedCell\" ");

				if (tdsToBeConsumed > 1) {
					sbf.append(" colspan=\"").append(tdsToBeConsumed)
							.append("\" ");
				}
				sbf.append(">");
			}

			if (this.breakToNextLine) {
				sbf.append("<br />");
			}
		}

		this.labelToHtml5(sbf, pageContext);

		if (this.labelPosition == LabelPosition.top) {
			sbf.append("<br/>");
		}
		if (tdOpenedForLabel) {
			sbf.append("</td><td class=\"fieldCell\" id=\"").append(this.name)
					.append("FieldCell\" ");
			if (tdsToBeConsumed > 1) {
				sbf.append(" colspan=\"").append(tdsToBeConsumed).append("\" ");
			}
			sbf.append(">");
		}
		this.fieldToHtml5(sbf, pageContext);
	}

	/*
	 * delegated to concrete class, but part of that job, rendering the label is
	 * provided in this class as a concrete method
	 * 
	 * @param sbf
	 * 
	 * @param pageContext
	 */
	abstract void fieldToHtml(StringBuilder sbf,
			PageGeneratorContext pageContext);

	/***
	 * next gen generator that generates a dom and leaves most view aspects to
	 * css This will be done over a period of time. Hence defaults to old one.
	 * 
	 * @param sbf
	 * @param pageContext
	 * @param showFieldDescription
	 */
	void fieldToHtml5(StringBuilder sbf, PageGeneratorContext pageContext) {
		this.fieldToHtml(sbf, pageContext);
	}

	/***
	 * render the label. caller takes care of any surrounding tags.
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	void labelToHtml(StringBuilder sbf, PageGeneratorContext pageContext) {
		String labelToUse = this.getLabelToUse(pageContext);
		if (labelToUse == null) {
			if (this.labelPosition != LabelPosition.none) {
				sbf.append("&nbsp;");
			}
			return;
		}

		if (this.isRequired && AP.starForRequiredField == AP.STAR_BEFORE_LABEL) {
			sbf.append("<reqfield>*</reqfield>");
		}
		sbf.append("<span ");
		if (this.hidden) {
			sbf.append(" style=\"display:none\" ");
		}

		if (this.name != null) {
			sbf.append("id=\"").append(this.name).append("Label\" ");
		}

		if (this.hoverText != null) {
			sbf.append("title=\"").append(this.hoverText).append("\" ");
		}

		if (this.isRequired) {
			sbf.append("class=\"requiredlabel\" >").append(labelToUse);
			if (AP.starForRequiredField == AP.STAR_AFTER_LABEL) {
				sbf.append("<span class=\"requiredstar\">*</span>");
			}
			sbf.append("</span>");
		} else {
			sbf.append("class=\"label\">").append(labelToUse)
					.append("&nbsp;</span>");
		}
		return;
	}

	/***
	 * render the label. caller takes care of any surrounding tags.
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	void labelToHtml5(StringBuilder sbf, PageGeneratorContext pageContext) {
		String labelToUse = this.getLabelToUse(pageContext);
		if (labelToUse == null) {
			if (this.labelPosition != LabelPosition.none) {
				sbf.append("&nbsp;");
			}
			return;
		}

		sbf.append("<div class=\"")
				.append(this.isRequired ? "requiredLabel" : "label")
				.append("\" ");
		if (this.hidden) {
			sbf.append(" style=\"display:none\" ");
		}

		if (this.name != null) {
			sbf.append("id=\"").append(this.name).append("Label\" ");
		}

		if (this.hoverText != null) {
			sbf.append("title=\"").append(this.hoverText).append("\" ");
		}

		sbf.append(" >").append(labelToUse).append("</div>");
		return;
	}

	/***
	 * Label may have to be taken from data element etc..
	 * 
	 * @return null if not required.
	 */
	String getLabelToUse(PageGeneratorContext pageContext) {
		if (this.labelPosition == LabelPosition.hide) {
			return null;
		}

		String lbl = this.label;
		if (lbl == null) {
			if (pageContext.customLabelName != null
					&& this.dataElement != null
					&& this.dataElement.customLabels != null
					&& this.dataElement.customLabels
							.containsKey(Page.customLabelKey)) {
				lbl = this.dataElement.customLabels.get(Page.customLabelKey).value;
			}

			// last resort. try data element
			if (lbl == null && this.dataElement != null) {
				lbl = this.dataElement.label;
			}
		}

		if (lbl == null || lbl.length() == 0 || lbl.equals(" ")) {
			return null;
		}

		return lbl;
	}

	/**
	 * for java script, a field is defined at this level. hence ToJavaScript()
	 * is sealed It calls another abstract method that the subclasses have to
	 * implement NOTE: This would have been "sealed" except for FilterField that
	 * needs to create js for possibly three fields
	 * 
	 * @param js
	 * @param pageContext
	 */
	void toJs(StringBuilder js, PageGeneratorContext pageContext) {
		String className = this.getClass().getName()
				.substring(this.getClass().getName().lastIndexOf('.') + 1);
		js.append('\n').append(Page.JS_VAR_NAME).append(" = new PM.")
				.append(className).append("();");

		pageContext.setAttributes(this, js, AbstractField.ALL_META_ATTRIBUTES);
		pageContext.setTableSensitiveAttributes(this, js,
				AbstractField.ALL_TABLE_SENSITIVE_ATTRIBUTES);
		pageContext.setTableSensitiveArrays(this, js,
				AbstractField.ALL_TABLE_SENSITIVE_ARRAYS);

		if (pageContext.isInsideGrid) {
			pageContext.setJsTextAttribute(js, "tableName",
					pageContext.getTableName());
			pageContext.setJsTextAttribute(js, "unqualifiedName", this.name);

		}

		/*
		 * formatter should default to dataElement or dataType. NOTE: if it is
		 * specified, it is taken care of by the generic utility
		 */
		if (this.formatter == null && this.dataElement != null) {
			String ft = this.dataElement.formatter;
			if (ft == null) {
				AbstractDataType dt = DataTypes
						.getDataType(this.dataType, null);
				if (dt != null) {
					ft = dt.formatter;
				}
			}

			if (ft != null) {
				pageContext.setJsTextAttribute(js, "formatter", ft);
			}
		}

		// and label.
		String labelToUse = this.getLabelToUse(pageContext);
		if (labelToUse == null) {
			labelToUse = "";
		}
		pageContext.setJsTextAttribute(js, "label", labelToUse);
		// value to be set to defaultValue
		String val = (this.defaultValue == null) ? "" : this.defaultValue;
		pageContext.setJsTextAttribute(js, "value", val);

		/*
		 * any other attribute that the concrete class wants to add
		 */
		this.fieldToJs(js, pageContext);

		js.append('\n').append("P2.addField(").append(Page.JS_VAR_NAME)
				.append(");");
	}

	/**
	 * @param js
	 * @param pageContext
	 */
	void fieldToJs(StringBuilder js, PageGeneratorContext pageContext) {
		/*
		 * provision for sub-class to over-ride
		 */
	}

	/**
	 * render without tr/td. Simply put label and field in wrapper.
	 * 
	 * @param sbf
	 * @param pageContext
	 */
	void toSimpleHtml(StringBuilder sbf, PageGeneratorContext pageContext) {

		this.labelToHtml5(sbf, pageContext);
		sbf.append("<div id=\"").append(this.name).append("Wrapper\">");
		this.fieldToHtml5(sbf, pageContext);
		sbf.append("</div>");
	}

	@Override
	public void initialize(PageGeneratorContext context) {
		super.initialize(context);
		if (this.dataElement == null) {
			String elementName = this.dataElementName;
			if (elementName == null) {
				elementName = this.name;
			}
			this.dataElement = DataDictionary.getElement(elementName);
			if (this.dataElement == null) {
				/*
				 * we have some known internal fields
				 */
				if (elementName.endsWith("Operator")) {
					this.dataElement = DataDictionary
							.getDefaultElement(elementName);
				} else if (elementName.endsWith("To")) {
					this.dataElement = DataDictionary.getElement(elementName
							.substring(0, elementName.length() - 2));
				}
				if (this.dataElement == null) {
					Spit.out("ERROR: Element Name "
							+ elementName
							+ " does not refer to any Data Element in Data Dictionary. Either add "
							+ this.name
							+ " to dictionary, or use dataElementName property to associate this with another entry in the dictionary");
					this.dataElement = DataDictionary
							.getDefaultElement(elementName);
				}
			}
		}

		if (this.dataType == null) {
			this.dataType = this.dataElement.dataType;
		}
		if (this.valueType == null) {
			AbstractDataType dt = DataTypes.getDataType(this.dataType, null);
			if (dt == null) {
				this.valueType = DataValueType.TEXT;
			} else {
				this.valueType = dt.getValueType();
			}
		}
	}

	/**
	 * set attributes of this object from the supplied list
	 * 
	 * @param values
	 */
	public void copyAttributes(Map<String, String> values) {
		ObjectManager.copyAttributes(values, this);
	}
}

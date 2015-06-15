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

/**
 * Field is a variable, a column, an attribute, a data element, a piece of
 * information.. Difficult to define, but in our context we use it as column of
 * a table, or a value in a collection of values.
 * 
 */
class Field implements ToBeInitializedInterface {

	/**
	 * identifier
	 */
	String name = null;

	/**
	 * Type of column, if this record is associated with a table
	 */
	ColumnType columnType = ColumnType.data;

	/***
	 * data type as described in dataTypes.xml
	 */
	String dataType;

	/**
	 * If this is a column in the database, and we use a different naming
	 * convention for db, this is the way to map field names to column names.
	 * Defaults to name
	 */
	String columnName = null;

	/**
	 * Can this field be null in the data base?
	 */
	boolean isNullable;

	/**
	 * if fieldType is PARENT_KEY or FOREIGN_KEY, then the table that this
	 * column points to. If the table is a view, then this is the table from
	 * which this column is picked up from
	 */
	String referredRecord = null;

	/**
	 * Valid only of the table is a view. Use this if the referred column name
	 * is different from this name. Defaults to this name.
	 */
	String referredField = null;

	/***
	 * for internal documentation
	 */
	String description = null;

	/***
	 * label in English, in case this is to be rendered
	 */

	String label = null;
	/***
	 * If this field can take set of design-time determined values, this is the
	 * place. this is of the form
	 * "internalValue1:displayValue1,internaValue2,displayValue2......."
	 */
	String valueList;

	/**
	 * if this is a foreign key, many a times the value would be available as
	 * some other name. Or if we are working with grid, value could be in
	 * dc.values. To copy the value from a field in dc.values, use this
	 * attribute
	 */
	String dataSource = null;
	/***
	 * Is a non-null (non-empty) value required in this field? If this true, we
	 * use default value, failing which the filed has to be "nullable". We do
	 * not allow only date fields to be nullable. For all other types, we will
	 * use a default value empty string, false or 0.
	 */
	boolean isOptional = false;

	/**
	 * value to be used if it is not supplied, even if it is optional
	 */
	String defaultValue = null;

	/***
	 * is this field mandatory but only when value for another field is
	 * supplied?
	 */
	String basedOnField = null;

	/**
	 * relevant only if basedOnField is set to true. Further restriction that
	 * this field is mandatory only if the other field has this specific value
	 */
	String basedOnFieldValue = null;

	/**
	 * At times, we have two fields but only one of them should have value. Do
	 * you have such a pair? If so, one of them should set this. Note that it
	 * does not imply that one of them is a must. It only means that both cannot
	 * be specified. Both can be optional is implemented by isOptional for both.
	 */
	String otherField = null;
	/***
	 * is this a to-field for another field? Specify the name of the from field.
	 * Note that you should not specify this on both from and to fields.
	 */
	String fromField = null;

	/***
	 * is this part of a from-to field, and you want to specify that thru this
	 * field?
	 */
	String toField = null;

	/*
	 * following attributes are used by clients for creating good UI
	 */
	/***
	 * message or help text that is to be flashed to user on the client as a
	 * help text and/or error text when the field value is in error.
	 */
	String messageName = null;

	/**
	 * if this field is to be rendered on a page for input what is the default
	 * field type? You may over-ride this in a page.xml
	 */
	PageFieldType defaultInputType = PageFieldType.assistedInputField;

	/**
	 * attributes that are used only by client are stored in a map, rather than
	 * defining them as individual attributes. We have taken this approach
	 * because the page field attributes are likely to keep evolving as the
	 * browser technology changes, and users demand more UX features, and these
	 * attributes are used at design time by page generator. By adopting this
	 * design, our core server side can remain stable
	 */
	Map<String, String> pageFieldAttributes = new HashMap<String, String>();

	/**
	 * data type object cached for performance
	 */
	private AbstractDataType dataTypeObject = null;

	/**
	 * default values is parsed into a Value object for performance
	 */
	private Value defaultValueObject = null;

	/**
	 * valueList is converted into list of valid values for performance
	 */
	private Map<String, Value> validValues = null;

	/**
	 * any of the inter-field validations specified? cached for performance
	 */
	private boolean hasInterFieldValidations = false;

	Field() {
	}

	/**
	 * parse textValue and add it to dc. extract filter fields as well if
	 * required. do field level validation. In case of any error, suitable error
	 * message is added to dc, and false is returned. true is returned if all
	 * ok.
	 * 
	 * @param inData
	 *            from which to parse value
	 * 
	 * @param dc
	 *            into which parsed values are to be put into. Also used to add
	 *            parse errors
	 * @param purpose
	 *            why are we parsing this
	 * @return true if all ok, false if we found some issue that we have
	 *         reported into dc.
	 */
	public boolean parseValue(ServiceData inData, DataCollection dc,
			InputRecordPurpose purpose) {

		if (this.dataTypeObject == null) {
			throw new ServiceError("Field '" + this.name
					+ "' has an invalid data type : " + this.dataType);
		}

		String textValue = inData.getValue(this.name);
		Value value = null;
		if (textValue == null || textValue.length() == 0) {
			value = this.defaultValueObject;
			if (value == null && this.isRequired(purpose)) {
				this.addParseErrorr(null, dc);
				return false;
			}
		} else {
			value = this.parse(textValue, purpose);
			if (value == null) {
				this.addParseErrorr(textValue, dc);
				return false;
			}
		}
		/*
		 * no need to add null to dc, nor do we bother about other validations
		 */
		if (value == null) {
			return true;
		}

		dc.addValue(this.name, value);

		if (purpose != InputRecordPurpose.filter) {
			return true;
		}

		/*
		 * is there a filter operator? value for these are internally added
		 * internally by exility client side. In case of error, we ignore rather
		 * than giving error message back to user
		 */
		String fieldName = this.name + CommonFieldNames.FILTER_OPERATOR_SUFFIX;
		textValue = inData.getValue(fieldName);
		value = null;
		if (textValue == null || textValue.length() == 0) {
			return true;
		}

		int operator = 0;
		try {
			operator = Integer.parseInt(textValue);
		} catch (Exception e) {
			Spit.out("Non-integral value of " + textValue
					+ "  received as operator for field " + this.name
					+ ". operater ignored.");
			return true;
		}

		if (operator < 0 || operator > CamparisonType.between.ordinal()) {
			Spit.out("Value of " + textValue
					+ "  received as operator for field " + this.name
					+ ". operater ignored.");
			return true;
		}

		/*
		 * we will add operator, but only if the possible to-value is ok.
		 */
		if (operator == CamparisonType.between.ordinal()) {
			String toFieldName = this.name + CommonFieldNames.FILTER_TO_SUFFIX;
			textValue = inData.getValue(toFieldName);
			if (textValue == null || textValue.length() == 0) {
				return true;
			}
			value = Value.parse(textValue, this.getValueType());
			if (value == null) {
				return true;
			}
			dc.addValue(toFieldName, value);
		}
		/*
		 * all right. let us add operator
		 */
		dc.addValue(fieldName, Value.newValue(operator));
		return true;
	}

	/**
	 * put an error message that the text being parsed is invalid into dc or
	 * inData (or none)
	 * 
	 * @param textValue
	 * @param outData
	 * @param dc
	 */
	void addParseErrorr(String textValue, DataCollection dc) {
		if (dc == null) {
			return;
		}
		if (this.messageName == null) {
			String txt = this.label + " : ";
			if (textValue == null || textValue.length() == 0) {
				txt += "Value is required";
			} else {
				txt += " has a value of '" + textValue
						+ "'. This is invalid as per data type "
						+ this.dataType + " ("
						+ this.dataTypeObject.description + ")";
			}
			dc.addError(txt);

		} else {
			dc.addMessage(this.messageName, this.label, textValue == null ? ""
					: textValue);
		}
	}

	/**
	 * It is assumed that the value is created using parse() method, and hence
	 * field level validation is skipped. We do inter-field and db related
	 * validations.
	 * 
	 * @param values
	 *            if this validation is for a column, then column values from
	 *            the entire row is also supplied for inter-column validations
	 * @param dc
	 *            optional. to which error message is to be added
	 * @param puprose
	 *            why are we validating this?
	 * @return true if valid, false otherwise. An error message is added to dc
	 */
	boolean validateInterField(Map<String, Value> values, DataCollection dc,
			InputRecordPurpose purpose) {
		if (!this.hasInterFieldValidations) {
			return true;
		}
		Value value = values.get(this.name);
		if (null == value || value.isNull()) {
			if (purpose != InputRecordPurpose.save) {
				return true;
			}
			/*
			 * is this field mandatory if another field is specified?
			 */
			if (this.basedOnField != null) {
				Value otherValue = values.get(this.basedOnField);
				if (otherValue != null && otherValue.isSpecified()) {
					if (this.basedOnFieldValue == null) {
						this.raiseValidationError(value, dc);
						return false;
					}
					if (otherValue.getTextValue()
							.equals(this.basedOnFieldValue)) {
						this.raiseValidationError(value, dc);
						return false;
					}
				}
			}
			/*
			 * is this required if another field is not specified?
			 */
			if (this.otherField != null) {
				Value otherValue = values.get(this.otherField);
				if (otherValue == null || otherValue.isNull()) {
					this.raiseValidationError(value, dc);
					return false;
				}
			}
			return true;
		}
		/*
		 * we have value. is it ok to have value?
		 */
		if (this.otherField != null) {
			Value otherValue = values.get(this.otherField);
			if (otherValue != null && otherValue.isNull() == false) {
				this.raiseValidationError(value, dc);
				return false;
			}
		}

		/*
		 * from-to fields
		 */
		if (this.fromField != null) {
			Value otherValue = values.get(this.fromField);
			if (otherValue != null
					&& value.compare(Comparator.LESSTHAN, otherValue)) {
				dc.addMessage(ExilityMessageIds.FROM_TO_ERROR, this.label,
						value.toString(), this.fromField, otherValue.toString());
			}
		}
		if (this.toField != null) {
			Value otherValue = values.get(this.toField);
			if (otherValue != null
					&& value.compare(Comparator.GREATERTHAN, otherValue)) {
				dc.addMessage(ExilityMessageIds.FROM_TO_ERROR, this.toField,
						otherValue.toString(), this.label, value.toString());
			}
		}
		return true;
	}

	/**
	 * value has failed validation. Put a message in dc
	 * 
	 * @param value
	 * @param dc
	 */
	void raiseValidationError(Value value, DataCollection dc) {
		if (this.messageName != null) {
			if (dc != null) {
				dc.addError(this.label + ": " + this.messageName
						+ " Invalid value of " + value.toString());
			}
		} else {
			String msg = this.label + " has an invalid value";
			if (dc != null) {
				dc.addError(msg);
			}
		}
	}

	@Override
	public void initialize() {
		if (this.columnName == null) {
			this.columnName = this.name;
		}
		/*
		 * cache Exility resources
		 */
		if (this.dataTypeObject == null) {
			this.dataTypeObject = DataTypes.getDataTypeOrNull(this.dataType);
			if (this.dataTypeObject == null) {
				throw new RuntimeException("Error : Field " + this.name
						+ " uses an invalid data type of " + this.dataType);
			}
		}

		if (this.valueList != null) {
			this.parseValidValues();
		}

		if (this.defaultValue != null) {
			this.parseDefaultValue();
		}
		this.hasInterFieldValidations = this.basedOnField != null
				|| this.fromField != null || this.toField != null
				|| this.otherField != null;

		/*
		 * for ease of operation, copy common attributes also into
		 * pageFieldAttributes.
		 */
		this.pageFieldAttributes.put("name", this.name);
		this.pageFieldAttributes.put("dataElementName", this.name);
		this.pageFieldAttributes.put("label", this.label);
		this.pageFieldAttributes.put("description", this.description);
		this.pageFieldAttributes.put("dataType", this.dataType);
		this.pageFieldAttributes.put("valueList", this.valueList);
		this.pageFieldAttributes.put("isRequired", this.isOptional ? "false"
				: "true");
		this.pageFieldAttributes.put("defaultValue", this.defaultValue);
		this.pageFieldAttributes.put("basedOnField", this.basedOnField);
		this.pageFieldAttributes.put("basedOnFieldValue",
				this.basedOnFieldValue);
		this.pageFieldAttributes.put("otherField", this.otherField);
		this.pageFieldAttributes.put("fromField", this.fromField);
		this.pageFieldAttributes.put("toField", this.toField);
		this.pageFieldAttributes.put("messageName", this.messageName);
	}

	/**
	 * parse valueList into validValues
	 */
	private void parseValidValues() {
		String[] parts = this.valueList.split(ExilityConstants.PAIR_SEPARATOR);
		if (parts.length == 1) {
			parts = this.valueList.split(ExilityConstants.LIST_SEPARATOR);
		}
		if (parts.length == 1) {
			throw new RuntimeException(this.valueList
					+ " is not a valid value list for field " + this.name
					+ ".\n");
		}
		Map<String, Value> vals = new HashMap<String, Value>();
		DataValueType valueType = this.dataTypeObject.getValueType();
		for (String aPart : parts) {
			String[] aPair = aPart.split(ExilityConstants.LIST_SEPARATOR);
			String internalValue = aPair[0];
			Value value = null;
			if (aPair.length <= 2) {
				value = Value.parse(internalValue, valueType);
			}
			if (value == null) {
				throw new RuntimeException(this.valueList
						+ " is not a valid value list for field " + this.name
						+ ".\n");
			}
			vals.put(internalValue, value);
		}
		this.validValues = vals;
		return;

	}

	/**
	 * parse default value into a Value instance
	 */
	private void parseDefaultValue() {
		if (this.validValues != null) {
			this.defaultValueObject = this.validValues.get(this.defaultValue);
		} else {
			this.defaultValueObject = this.dataTypeObject.parseValue(this.name,
					null, null, null);
		}
		if (this.defaultValueObject == null) {
			throw new RuntimeException(this.defaultValue
					+ " is the default value for field " + this.name
					+ " but it is not a valid value for the field.\n");
		}
	}

	/**
	 * DataColumn is an upgrade of DataElement, but we did not want to extend
	 * that. We want to ultimately replace DataElement with DataColumn. This is
	 * a temporary arrangement
	 * 
	 * @return equivalent data element instance
	 */
	public DataElement getDataElement() {
		DataElement element = new DataElement();
		element.name = this.name;
		element.businessDescription = this.description;
		element.dataType = this.dataType;
		element.label = this.label;
		element.valueList = this.valueList;

		return element;
	}

	/**
	 * @return instance of a column for a table
	 */

	public Column getColumn() {
		Column column = new Column();
		column.basedOnColumnName = this.basedOnField;
		column.basedOnColumnValue = this.basedOnFieldValue;
		column.columnName = this.columnName;
		column.dataTypeObject = this.getDataType();
		column.dataValueType = this.getValueType();
		/*
		 * initialize() of Column is made smart to look-up dictionary only if
		 * data element is not set already.
		 * 
		 * dataType and dataValueType are set by initialize() Parameter
		 */
		column.dataSource = this.dataSource;
		column.defaultValue = this.defaultValue;
		column.description = this.description;
		column.isKeyColumn = (this.columnType == ColumnType.primaryKey);
		column.isNullable = this.isNullable;
		column.isOptional = this.isOptional;
		/*
		 * keyFromTable is not used. As and when we use that
		 */
		// column.keyFromTable = null;
		column.label = this.label;
		column.listServiceName = this.pageFieldAttributes.get("listServiceId");
		column.name = this.name;
		column.otherColumnName = this.otherField;
		column.initialize();
		return column;
	}

	/**
	 * name of this field
	 * 
	 * @return name of this field
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 
	 * @return referred field name, or null if this field does not refer to
	 *         another one
	 */
	public String getReferredName() {
		return this.referredField;
	}

	/**
	 * Any inter-field validations specified for this field
	 * 
	 * @return true if this field's validity depend on some other field as well
	 */
	public boolean needsInterFieldValidation() {
		return this.hasInterFieldValidations;
	}

	/**
	 * parse textValue into a value object and do basic field level
	 * validation(isRequired, valueList and dataType). Inter-field validations
	 * are done with a subsequent call to validate() after parsing all fields.
	 * Parse error message if any is pushed to inData or dc if passed.
	 * 
	 * @param textValues
	 * 
	 * @param dc
	 *            Ignored is inData is non-null, or this is null. In case of
	 *            parse error, an error message is added to dc.
	 * @param purpose
	 *            why are we parsing this column?
	 * @return ValueList, or null if validation fails.
	 */
	public ValueList parseColumn(String[] textValues, DataCollection dc,
			InputRecordPurpose purpose) {
		boolean allOk = true;
		for (String textValue : textValues) {
			Value value = null;
			if (textValue == null || textValue.length() == 0) {
				value = this.defaultValueObject;
				if (value == null && this.isRequired(purpose)) {
					this.addParseErrorr(null, dc);
					allOk = false;
				}
			} else {
				value = this.parse(textValue, purpose);
				if (value == null) {
					this.addParseErrorr(textValue, dc);
				}
			}
		}

		if (!allOk) {
			return null;
		}

		return ValueList
				.newList(textValues, this.dataTypeObject.getValueType());
	}

	/**
	 * carry out inter-field validation for all rows of grid for this field
	 * 
	 * @param grid
	 *            that contains data to be validates
	 * @param dc
	 *            if outData is null, and this is non-null, is used to put
	 *            validation error. Not used for data input
	 * @param purpose
	 *            why are we validating this grid
	 * @return true if validation succeeds, false otherwise
	 */
	public boolean validateInterField(Grid grid, DataCollection dc,
			InputRecordPurpose purpose) {
		if (!this.hasInterFieldValidations) {
			return true;
		}
		/*
		 * we could be more efficient by looping over rows and doing validation
		 * within other columns, but we sacrifice that for the sake of
		 * modularity, and re-use validateInterFiield() method. So, we create
		 * values collection for each row and call inter-field validation
		 */
		String[] fieldNames = { this.name, this.basedOnField, this.fromField,
				this.toField, this.otherField };
		int nbrFields = fieldNames.length;
		ValueList[] columns = new ValueList[nbrFields];

		for (int i = 0; i < fieldNames.length; i++) {
			String fieldName = fieldNames[i];
			if (fieldName != null) {
				columns[i] = grid.getColumn(fieldName);
			}
		}
		int nbrRows = grid.getNumberOfRows();

		boolean allOk = true;

		Map<String, Value> values = new HashMap<String, Value>();
		for (int i = 0; i < nbrRows; i++) {
			values.clear();
			for (int j = 0; j < columns.length; j++) {
				ValueList column = columns[j];
				if (column != null) {
					values.put(fieldNames[j], column.getValue(i));
				}
			}
			boolean thisIsOk = this.validateInterField(values, dc, purpose);
			allOk = allOk && thisIsOk;
		}
		return allOk;
	}

	/**
	 * get a page field for this field
	 * 
	 * @param forOutput
	 *            true if this field is used as output in the page, false if it
	 *            is used as input
	 * @return field that is suitable to be included in a page panel
	 */
	public AbstractField getPageField(boolean forOutput) {
		if (forOutput) {
			if (this.defaultInputType == PageFieldType.hiddenField) {
				return this.getPageField(PageFieldType.hiddenField);
			}
			return this.getPageField(PageFieldType.outputField);
		}
		return this.getPageField(this.defaultInputType);
	}

	/**
	 * get a page field for this field
	 * 
	 * @param fieldType
	 * 
	 *            true if this field is used as output in the page, false if it
	 *            is used as input
	 * @return field that is suitable to be included in a page panel
	 */
	public AbstractField getPageField(PageFieldType fieldType) {
		AbstractField pageField = fieldType.getDefaultField();
		this.revisePageField(pageField, false);
		return pageField;
	}

	/**
	 * 
	 * @param fieldsMap
	 */
	public void getIntoMap(Map<String, Field> fieldsMap) {
		fieldsMap.put(this.name, this);
	}

	/**
	 * scrub a page field with the information available in this field. It is
	 * possible that we may create a new field all together, and hence the
	 * caller the returned value back the variable that points this page field
	 * 
	 * @param pageField
	 * @param isUserDefined
	 *            is the field specified by user? We will consider this while
	 *            over-riding attributes
	 */
	public void revisePageField(AbstractField pageField, boolean isUserDefined) {
		/*
		 * we have an issue with isRequired for filterField and hiddenField
		 */
		boolean toBeReverted = isUserDefined
				|| pageField instanceof FilterField
				|| pageField instanceof HiddenField
				|| pageField instanceof OutputField;
		boolean oldValue = pageField.isRequired;
		ObjectManager.copyAttributes(this.pageFieldAttributes, pageField);
		if (toBeReverted) {
			pageField.isRequired = oldValue;
		}
		pageField.dataElement = this.getDataElement();
		pageField.dataElementName = this.name;
		pageField.dataType = this.dataType;
		pageField.valueType = this.dataTypeObject.getValueType();
	}

	/**
	 * create an instance of OutputField for this field. We may have different
	 * type of output fields in the future. As of now, we have just one
	 * 
	 * @return output field instance
	 */
	public OutputField getOutputField() {
		OutputField field = new OutputField();
		this.revisePageField(field, false);
		return field;
	}

	/**
	 * get value value type of this field
	 * 
	 * @return value type or text if this field does not have a valid data type
	 */
	public DataValueType getValueType() {
		if (this.dataTypeObject != null) {
			return this.dataTypeObject.getValueType();
		}
		return DataValueType.TEXT;
	}

	/**
	 * get data type of this field
	 * 
	 * @return data type
	 */
	public AbstractDataType getDataType() {
		return this.dataTypeObject;
	}

	/**
	 * is this field required to have value for the purpose?
	 * 
	 * @param purpose
	 *            why it is being input
	 * @return true if value is required, false if it is ok not to have value
	 */
	public boolean isRequired(InputRecordPurpose purpose) {
		if (this.columnType == ColumnType.primaryKey) {
			return purpose == InputRecordPurpose.primaryKeyBasedRead;
		}

		return (purpose == InputRecordPurpose.save)
				&& (this.isOptional == false);
	}

	private Value parse(String textValue, InputRecordPurpose purpose) {

		if (purpose == InputRecordPurpose.filter) {
			/*
			 * for filtering, value has to conform to value type, but it need
			 * not conform to dataType restrictions
			 */
			return Value.parse(textValue, this.getValueType());
		}
		if (this.validValues != null) {
			/*
			 * we have to assume that the designer has put valid values in
			 * validValues list :-)
			 */
			return this.validValues.get(textValue);
		}

		Value value = Value
				.parse(textValue, this.dataTypeObject.getValueType());
		if (value != null && this.dataTypeObject.isValid(value)) {
			/*
			 * valid value-type, but failed dataType restrictions
			 */
			return value;
		}

		return null;
	}

	/**
	 * get filter condition for this field based on data in dc. Null if no
	 * condition.
	 * 
	 * @param dc
	 * @return condition, or null if data is not supplied
	 */
	public Condition getFilterCondition(DataCollection dc) {
		Value value = dc.getValue(this.name);
		if (value == null) {
			return null;
		}
		Comparator comparator = Comparator.EQUALTO;
		Spit.out("Going to create condition for " + this.name);
		Value operator = dc.getValue(this.name
				+ CommonFieldNames.FILTER_OPERATOR_SUFFIX);
		if (operator != null) {

			int comp = (int) operator.getIntegralValue();
			if (comp > 0) {
				comparator = Condition.getComparator(comp);
				if (comparator == null) {
					Spit.out(operator.getTextValue()
							+ " is not a valid compartor. " + this.name
							+ " is not converted into a filter field.");
					comparator = Comparator.EQUALTO;
				}
			}
		}
		Condition condition = new Condition();
		condition.columnName = this.name;
		condition.comparator = comparator;
		condition.fieldName = this.name;
		return condition;
	}
}

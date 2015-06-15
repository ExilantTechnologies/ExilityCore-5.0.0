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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/***
 * Highly specialized class that understands the conventions using which Exility
 * design component xml structures are designed. Converts object instance to
 * xml, and back.
 * 
 * This is a utility class and hence has tons of methods.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ObjectManager {
	private static final String MY_PACKAGE_NAME = ObjectManager.class
			.getPackage().getName() + '.';
	private static final String[] candidateKeyNames = { "name", "key", "id",
			"value" };
	private static final HashMap<String, String> classNames = new HashMap<String, String>();
	private static final String[] HEADER_FOR_ATTRIBUTE_ARRAY = { "key",
			"fieldName", "value" };

	static final DecimalFormat decimalFormat = new DecimalFormat();
	static {
		ObjectManager.decimalFormat.setMaximumFractionDigits(4);
		ObjectManager.decimalFormat.setMinimumFractionDigits(1);
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
				"com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");

		/**
		 * Following approach needs to be re-designed. This was a quick fix to
		 * parse services that were designed for local data to be used on server
		 * as well. local services are discontinued, and hence these are not
		 * used any more. Keeping it for a while before removing them once and
		 * for all
		 */
		ObjectManager.classNames.put("setValue", "setValueStep");
		ObjectManager.classNames.put("SetValueStep", "SetValue");
		ObjectManager.classNames.put("list", "listStep");
		ObjectManager.classNames.put("ListStep", "List");
		ObjectManager.classNames.put("appendToList", "appendToListStep");
		ObjectManager.classNames.put("AppendToListStep", "AppendToList");
		ObjectManager.classNames.put("grid", "gridStep");
		ObjectManager.classNames.put("GridStep", "Grid");
		ObjectManager.classNames.put("addColumn", "addColumnStep");
		ObjectManager.classNames.put("AddColumnStep", "AddColumn");
		ObjectManager.classNames.put("lookup", "lookupStep");
		ObjectManager.classNames.put("LookupStep", "Lookup");
		ObjectManager.classNames.put("filter", "filterStep");
		ObjectManager.classNames.put("FilterStep", "Filter");
		ObjectManager.classNames.put("stop", "stopStep");
		ObjectManager.classNames.put("StopStep", "Stop");
		ObjectManager.classNames.put("save", "saveStep");
		ObjectManager.classNames.put("SaveStep", "Save");
		ObjectManager.classNames.put("saveGrid", "saveGridStep");
		ObjectManager.classNames.put("SaveGridStep", "SaveGrid");
		ObjectManager.classNames.put("delete", "deleteStep");
		ObjectManager.classNames.put("DeleteStep", "Delete");
		ObjectManager.classNames.put("massUpdate", "massUpdateStep");
		ObjectManager.classNames.put("MassUpdateStep", "MassUpdate");
		ObjectManager.classNames.put("massDelete", "massDeleteStep");
		ObjectManager.classNames.put("MassDeleteStep", "MassDelete");
		ObjectManager.classNames.put("aggregate", "aggregateStep");
		ObjectManager.classNames.put("AggregateStep", "Aggregate");
	}

	private static final String VALUE = "value";

	/**
	 * special attribute an an xml element that directs what class to load it as
	 */
	public static final String OBJECT_TYPE = "_type";

	/**
	 * utility 1. CreateNew() - create a new Object with some info about its
	 * Looks trivial, but wanted to keep them in one place
	 * 
	 * @param type
	 * @return object instance
	 */
	public static Object createNew(Class type) {
		try {
			return type.newInstance();
		} catch (Exception e) {
			Spit.out("ObjectManager : Unable to create Object for type "
					+ type.getName() + ". " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * If you do not have a type, do you have a fully qualified name? i.e. name
	 * including package name
	 * 
	 * @param fullName
	 *            fully qualified class name
	 * @return instance
	 */
	//
	public static Object createNew(String fullName) {
		try {
			Class type = Class.forName(fullName);
			return type.newInstance();
		} catch (Exception e) {
			Spit.out("ObjectManager : Unable to create Object for type  "
					+ fullName + ". " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * migrated from .net when we can have same class in more than one assembly
	 * 
	 * @param classLoaderName
	 *            not used
	 * @param packageName
	 * @param className
	 * @return instance
	 */
	public static Object createNew(String classLoaderName, String packageName,
			String className) {
		return ObjectManager.createNew(packageName + '.' + className);
	}

	/**
	 * instance based on a class name in a package
	 * 
	 * @param packageName
	 * @param className
	 * @return instance
	 */
	public static Object createNew(String packageName, String className) {
		return ObjectManager.createNew(packageName + '.' + className);
	}

	private static Class getType(String className, boolean errorIfNotFound) {
		try {
			return Class.forName(ObjectManager.MY_PACKAGE_NAME + className);
		} catch (Exception e) {
			if (errorIfNotFound) {
				Spit.out("ERROR: Unable to create a class for component "
						+ className
						+ ". Possible that the xml being processed has invalid tags in it.");
			}
			return null;
		}
	}

	// Utility 2 : Work with fields of an Object.

	/**
	 * 
	 * @param type
	 * @param fieldName
	 * @return field
	 */
	public static Field getField(Class type, String fieldName) {
		/*
		 * in java, getField() will get only visible field to this class. To get
		 * any declared field we have to check for getDeclaredField() and
		 * traverse up the class hierarchy.
		 */
		Class typ = type;
		while (!typ.equals(Object.class)) {
			try {
				return typ.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				//
			}

			typ = typ.getSuperclass();
		}
		return null;
	}

	/**
	 * get all fields for a class
	 * 
	 * @param type
	 * @param getPrivateFieldsAlso
	 * @return all fields indexed by their names
	 */
	public static Map<String, Field> getAllFields(Class type,
			boolean getPrivateFieldsAlso) {
		Map<String, Field> fields = new HashMap<String, Field>();
		Class typ = type;
		while (!typ.equals(Object.class)) {
			for (Field field : typ.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				if (getPrivateFieldsAlso
						|| Modifier.isPrivate(field.getModifiers()) == false) {
					fields.put(field.getName(), field);
				}
			}
			typ = typ.getSuperclass();
		}
		return fields;
	}

	/**
	 * get all loadable fields from an object instance. If the object is not an
	 * instance of LaodableInterface, we return all default and public fields
	 * 
	 * @param object
	 * @return all loadable fields
	 */
	public static Field[] getLoadableFieldsFromObject(Object object) {

		Class type = object.getClass();
		List<Field> fields = new ArrayList<Field>();
		while (!type.equals(Object.class)) {
			for (Field field : type.getDeclaredFields()) {
				int modifiers = field.getModifiers();
				if (Modifier.isStatic(modifiers)) {
					continue;
				}
				if (Modifier.isPrivate(modifiers)) {
					continue;
				}
				fields.add(field);

			}
			type = type.getSuperclass();
		}
		return fields.toArray(new Field[0]);
	}

	/**
	 * IMPORTANT : for Exility, we deal with only non-static public and
	 * non-static package-private fields
	 * 
	 * @param field
	 * @return
	 */
	private static boolean toBeIgnored(Field field) {
		int mod = field.getModifiers();
		if (Modifier.isStatic(mod) || Modifier.isPrivate(mod)
				|| Modifier.isProtected(mod)) {
			return true;
		}
		return false;
	}

	/**
	 * get value of a field from an object instance
	 * 
	 * @param objekt
	 * @param fieldName
	 * @return field value, or null if it can not be found
	 */
	public static Object getFieldValue(Object objekt, String fieldName) {
		Field field = ObjectManager.getField(objekt.getClass(), fieldName);
		if (field == null) {
			return null;
		}
		try {
			field.setAccessible(true);
			return field.get(objekt);
		} catch (Exception e) {
			//
		}

		return null;
	}

	/**
	 * get field value, but into a string
	 * 
	 * @param objekt
	 * @param fieldName
	 * @return field value as string.
	 */
	public static String getFieldValueAsString(Object objekt, String fieldName) {
		Object value = ObjectManager.getFieldValue(objekt, fieldName);
		if (value == null) {
			return "";
		}
		if (ObjectManager.isValueType(objekt.getClass())) {
			return ObjectManager.valueTypeToString(objekt);
		}
		return value.toString();
	}

	/**
	 * get all field values into a map
	 * 
	 * @param objekt
	 * @return all field values indexed by field names
	 */
	public static Map<String, Object> getAllFieldValues(Object objekt) {
		Map<String, Object> values = new HashMap<String, Object>();

		Map<String, Field> fields = ObjectManager.getAllFields(
				objekt.getClass(), true);
		for (Field field : fields.values()) {
			if (ObjectManager.toBeIgnored(field)) {
				continue;
			}
			field.setAccessible(true);
			try {
				values.put(field.getName(), field.get(objekt));
			} catch (Exception e) {
				//
			}
		}
		return values;
	}

	/**
	 * 
	 * @param objekt
	 * @param fieldName
	 * @param fieldValue
	 */
	public static void setFieldValue(Object objekt, String fieldName,
			Object fieldValue) {
		Field field = ObjectManager.getField(objekt.getClass(), fieldName);
		if (field == null) {
			return;
		}
		ObjectManager.setFieldValue(objekt, field, fieldValue);
	}

	/**
	 * fieldValue need not be of the right type. In fact, this method is used
	 * when the field value is available as String and it has to be set to the
	 * field after proper parsing
	 * 
	 * @param objekt
	 * @param field
	 * @param fieldValue
	 */
	public static void setFieldValue(Object objekt, Field field,
			Object fieldValue) {
		Class fieldType = field.getType();
		Class valueType = fieldValue.getClass();

		Object rightTypedValue = null;
		// if value is compatible with field it is straight forward assignment
		if (fieldType.isAssignableFrom(valueType)) {
			rightTypedValue = fieldValue;
		} else // if it is not compatible, value has to be a String from which
				// we can parse the right type.
		if (fieldValue instanceof String) {
			String stringValue = ((String) fieldValue).trim();

			if (fieldType.isArray()) {
				rightTypedValue = ObjectManager.parseArray(fieldType,
						stringValue);
			} else {
				rightTypedValue = ObjectManager.parsePrimitive(fieldType,
						stringValue);
			}
		}
		if (rightTypedValue != null) {
			try {
				field.setAccessible(true);
				field.set(objekt, rightTypedValue);
				return;
			} catch (Exception e) {
				//
			}
		}
		Spit.out("Field " + field.getName()
				+ " could not be assigned from a value of type "
				+ fieldType.getName() + " with value = "
				+ fieldValue.toString());
		return;
	}

	private static Object parseArray(Class type, String commaSeparatedvalue) {
		Class elementType = type.getComponentType();

		String[] vals = commaSeparatedvalue.split(",");
		int n = vals.length;
		Object arr = Array.newInstance(elementType, n);
		for (int i = 0; i < vals.length; i++) {
			try {
				Object o = ObjectManager.parsePrimitive(elementType,
						vals[i].trim());
				Array.set(arr, i, o);

			} catch (Exception e) {
				return null;
			}
		}
		return arr;
	}

	private static Object parsePrimitive(Class type, String value) {
		if (type.equals(String.class)) {
			return value;
		}

		if (type.isPrimitive()) {
			if (type.equals(int.class)) {
				return new Integer(value);
			}

			if (type.equals(long.class)) {
				return new Long(value);
			}

			if (type.equals(short.class)) {
				return new Short(value);
			}

			if (type.equals(byte.class)) {
				return new Byte(value);
			}

			if (type.equals(char.class)) {
				if (value.length() == 0) {
					return new Integer(' ');
				}
				return new Integer(value.toCharArray()[0]);
			}

			if (type.equals(boolean.class)) {
				if (value.equalsIgnoreCase("true") || value.equals("1")) {
					return new Boolean(true);
				}
				return new Boolean(false);
			}

			if (type.equals(float.class)) {
				return new Float(value);
			}

			if (type.equals(double.class)) {
				return new Double(value);
			}
			return null;
		}
		if (type.isEnum()) {
			try {
				return Enum.valueOf(type, value.toUpperCase());
			} catch (IllegalArgumentException iaEx) {
				return Enum.valueOf(type, value);
			}
			// return Enum.valueOf(type, value.toUpperCase());
		}

		if (type.equals(Expression.class)) {
			Expression expr = new Expression();

			try {
				expr.setExpression(value);
			} catch (ExilityException e) {
				Spit.out("invalid expression :" + e.getMessage());
				e.printStackTrace();
			}
			return expr;
		}

		if (type.equals(Date.class)) {
			return DateUtility.parseDate(value);
		}

		if (type.equals(Pattern.class)) {
			return Pattern.compile(value);
		}

		Spit.out("Unable to parse value " + value + " into type "
				+ type.getName());
		return null;
	}

	/**
	 * copy recursively till we end-up with non-objects
	 * 
	 * @param component
	 * @return cloned object
	 */
	public static Object deepCopy(Object component) {
		Class type = component.getClass();
		if (ObjectManager.isValueType(type)) {
			return component;
		}

		if (type.isArray()) {
			Class elementType = type.getComponentType();
			int len = Array.getLength(component);
			Object newArray = Array.newInstance(elementType, len);
			for (int i = 0; i < len; i++) {
				Array.set(newArray, i,
						ObjectManager.deepCopy(Array.get(component, i)));
			}
			return newArray;
		}
		Object newComponent = null;
		try {
			newComponent = type.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		if (Map.class.isAssignableFrom(type)) {
			Map map = (Map) component;
			Map newMap = (Map) newComponent;
			newMap.putAll(map);
			return newMap;
		}
		// get all fields of this Object
		Map<String, Field> fields = ObjectManager.getAllFields(type, true);
		for (String fiedName : fields.keySet()) {
			Field field = fields.get(fiedName);
			field.setAccessible(true);
			try {
				field.set(newComponent,
						ObjectManager.deepCopy(field.get(component)));

			} catch (Exception e) {
				//
			}
		}
		return newComponent;
	}

	private static boolean isValueType(Class type) {
		if (type.isPrimitive() || type.isEnum() || type.equals(String.class)
				|| type.equals(Expression.class) || type.equals(Date.class)
				|| type.equals(Pattern.class)) {
			return true;
		}
		return false;
	}

	private static boolean isExilityType(Class type) {
		return type.getPackage().getName()
				.equals(ObjectManager.class.getPackage().getName());
	}

	// Utility 3. Load an Object from an XML
	// Assumptions:
	// 1. an element is for an Object. If this is part of a collection, then the
	// tagName should be the same as the class name, with first char to lower.
	// Otherwise, no restriction on name, but why not??
	// 2. all primitive values are supplied as attributes.
	// 3. enum values are represented by their String, and not int value.
	// 4. array and hashTable/dictionary (non-generic or generic ok) are the
	// only two collections allowed
	// 5. Collection members are same or sub classes of the specified type.
	// 6. key for hashing is always a String, and MUST be an attribute/field
	// with. This field should have one of these names id, key, name, value.
	// 7. array of a primitive results in elements for primitives. it is of the
	// form <entry value="value">...
	/**
	 * we discontinued because of ByteOrderMark issues in files
	 * 
	 * @param xml
	 * @return object insatnce for the xml
	 */
	@Deprecated
	public static Object fromXml(String xml) {
		return ObjectManager.fromXml(xml, null);
	}

	/**
	 * deprecated because of issues with byte order mark
	 * 
	 * @param xml
	 * @param rootObjectType
	 * @return object
	 */
	@Deprecated
	public static Object fromXml(String xml, Class rootObjectType) {
		Document document = null;
		try {
			StringReader reader = new StringReader(xml);
			InputSource source = new InputSource(reader);

			DocumentBuilderFactory docuBuilder = DocumentBuilderFactory
					.newInstance();
			docuBuilder.setIgnoringComments(true);
			docuBuilder.setValidating(false);
			docuBuilder.setCoalescing(false);
			docuBuilder.setXIncludeAware(false);
			docuBuilder.setNamespaceAware(false);

			DocumentBuilder builder = docuBuilder.newDocumentBuilder();
			document = builder.parse(source);
		} catch (SAXParseException e) {
			Spit.out("Error while parsing xml text. " + e.getMessage()
					+ "\n At line " + e.getLineNumber() + " and column "
					+ e.getColumnNumber());
			return null;
		} catch (Exception e) {
			Spit.out("Error while reading resource file. " + e.getMessage());
			return null;
		}

		Element rootElement = document.getDocumentElement();
		// let us clean-up and remove text/comment nodes that we do not use in
		// Exility..
		ObjectManager.removeTextChildren(rootElement);

		Class typ = rootObjectType;
		String xmlClassName = ObjectManager.toClassName(
				rootElement.getNodeName(), ""); // Bug 845 - Exility Local
												// Service Verbs should work on
												// server side also
		if (typ == null
				|| rootObjectType.getSimpleName().equals(xmlClassName) == false) {
			typ = ObjectManager.getType(xmlClassName, true);
			if (typ == null) {
				return null;
			}
		}
		return ObjectManager.fromElement(rootElement, typ);
	}

	/***
	 * returns an instance of an object corresponding to the xml content of the
	 * file
	 * 
	 * @param file
	 *            . root tag is used to get the class name for the root object.
	 * @return object instance corresponding to the file content
	 */
	public static Object fromXml(File file) {
		return ObjectManager.fromXml(file, null);
	}

	/***
	 * returns an instance of an object corresponding to the xml content of the
	 * file
	 * 
	 * @param file
	 * @param rootObjectType
	 *            - optional. if not specified, root tag is used as the class
	 *            name. (except the first character is converted to upperCase)
	 * @return object instance corresponding to the file content
	 */
	public static Object fromXml(File file, Class rootObjectType) {
		Document document = ObjectManager.getDocument(file);
		if (document == null) {
			return null;
		}

		Element rootElement = document.getDocumentElement();
		// let us clean-up and remove text/comment nodes that we do not use in
		// Exility..
		ObjectManager.removeTextChildren(rootElement);

		Class typ = rootObjectType;
		String xmlClassName = ObjectManager.toClassName(
				rootElement.getNodeName(), "");
		if (typ == null
				|| rootObjectType.getSimpleName().equals(xmlClassName) == false) {
			typ = ObjectManager.getType(xmlClassName, true);
			if (typ == null) {
				return null;
			}
		}
		return ObjectManager.fromElement(rootElement, typ);
	}

	/***
	 * parses a file into a DOM
	 * 
	 * @param file
	 *            File to be read
	 * @return DOM for the xml that the file contains
	 */
	static Document getDocument(File file) {
		Document doc = null;

		DocumentBuilderFactory docuBuilder = DocumentBuilderFactory
				.newInstance();
		docuBuilder.setIgnoringComments(true);
		docuBuilder.setValidating(false);
		docuBuilder.setCoalescing(false);
		docuBuilder.setXIncludeAware(false);
		docuBuilder.setNamespaceAware(false);

		InputStream ins = null;
		try {
			DocumentBuilder builder = docuBuilder.newDocumentBuilder();
			ins = new FileInputStream(file);
			doc = builder.parse(ins);
		} catch (SAXParseException e) {
			Spit.out("Error while parsing xml text. " + e.getMessage()
					+ "\n At line " + e.getLineNumber() + " and column "
					+ e.getColumnNumber());
		} catch (Exception e) {
			Spit.out("Error while reading resource file. " + e.getMessage());
		} finally {
			if (ins != null) {
				try {
					ins.close();
				} catch (IOException e) {
					//
				}
			}
		}
		return doc;
	}

	private static String toClassName(String tagName, String parentTagName) {
		String className;
		if (parentTagName.equalsIgnoreCase("steps")
				&& ObjectManager.classNames.containsKey(tagName)) {
			className = ObjectManager.classNames.get(tagName);
		} else {
			className = tagName;
		}

		char firstChar = className.charAt(0);
		if ((firstChar >= 'a') && (firstChar <= 'z')) {
			firstChar = (char) ('A' + firstChar - 'a');
			return firstChar + className.substring(1);
		}
		return className;
	}

	private static String toTagName(String className, int noOfInterfaces) {
		String tagName;
		if ((noOfInterfaces == 0)
				&& ObjectManager.classNames.containsKey(className)) {
			tagName = ObjectManager.classNames.get(className);
		} else {
			tagName = className;
		}

		char firstChar = tagName.charAt(0);
		if ((firstChar >= 'A') && (firstChar <= 'Z')) {
			firstChar = (char) ('a' + firstChar - 'A');
			return firstChar + tagName.substring(1);
		}
		return tagName;
	}

	static Object fromElement(Element element, Class objectType) {
		if (ObjectManager.isValueType(objectType)) {
			return ObjectManager.getValueFromElement(element, objectType);
		}

		if (objectType.isArray()) {
			return ObjectManager.getArrayFromElement(element,
					objectType.getComponentType());
		}

		// any sub class of map..
		if (Map.class.isAssignableFrom(objectType)) {
			Map map = new HashMap<Object, Object>();
			if (element.hasChildNodes()) {
				ObjectManager.fillMapFromElement(element, map);
			} else if (element.hasAttributes()) {
				ObjectManager.fillMapfromAttributes(element, map);
			} else {
				Spit.out(element.getNodeName()
						+ " has no attributes or child nodes to put to map. It willl be empty.");
			}
			return map;
		}

		NodeList children = element.getChildNodes();
		int n = children.getLength();
		// is this that special case of a field that declares a super class
		// while the actual value can be any sub-class?
		// in that case, the element will have no attributes, and will have
		// exactly one childElement

		if (!element.hasAttributes() && (n == 1)) {
			Element childElement = (Element) children.item(0);
			Class possibleType = ObjectManager.getType(ObjectManager
					.toClassName(childElement.getNodeName(),
							element.getNodeName()), false);
			if ((possibleType != null)
					&& objectType.isAssignableFrom(possibleType)) {
				return ObjectManager.fromElement(childElement, possibleType);
			}
		}
		// we do not use any other collection. So, it has to be an Object
		Object newObject = ObjectManager.createNew(objectType);
		Map<String, Field> fields = ObjectManager
				.getAllFields(objectType, true);

		// set attributes
		if (element.hasAttributes()) {
			NamedNodeMap atts = element.getAttributes();
			for (int i = atts.getLength() - 1; i >= 0; i--) {
				Attr att = (Attr) atts.item(i);
				String fieldName = att.getName();
				if (fields.containsKey(fieldName)) {
					ObjectManager.setFieldValue(newObject,
							fields.get(fieldName), att.getValue());
					continue;
				}
				if (fieldName.indexOf(":") != -1
						|| fieldName.equals(ObjectManager.OBJECT_TYPE)
						|| fieldName.equals("xmlns")) {
					continue;
				}
				Spit.out("XML Loading: " + fieldName
						+ " is not a field in type " + objectType.getName());
			}
		}
		// look at the child elements and load them as well..
		for (int i = 0; i < n; i++) {
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			String fieldName = child.getNodeName();
			if (!fields.containsKey(fieldName)) {
				if (fieldName.indexOf(":") == -1
						&& fieldName.equals(ObjectManager.OBJECT_TYPE) == false
						&& fieldName.equals("xmlns") == false) {
					Spit.out("XML Loading: " + fieldName
							+ " is not a field in type " + objectType.getName());
				}
				continue;
			}
			Element childElement = (Element) child;
			Field field = fields.get(fieldName);
			Class fieldType = ObjectManager.getElementType(childElement);
			if (fieldType == null) {
				fieldType = field.getType();
			}
			Object fieldValue = ObjectManager.fromElement((Element) child,
					fieldType);
			field.setAccessible(true);
			try {
				field.set(newObject, fieldValue);
			} catch (Exception e) {
				//
			}
		}
		// does this require initialization?
		if (newObject instanceof ToBeInitializedInterface) {
			((ToBeInitializedInterface) newObject).initialize();
		}
		return newObject;
	}

	private static Class getElementType(Element element) {
		String objectTypeName = element.getAttribute(ObjectManager.OBJECT_TYPE);
		if ((objectTypeName == null) || (objectTypeName.length() == 0)) {
			return null;
		}

		if (objectTypeName.equals("HashMap")) {
			return Map.class;
		}
		try {
			return Class
					.forName(ObjectManager.MY_PACKAGE_NAME + objectTypeName);
		} catch (Exception e) {
			return null;
		}
	}

	private static Object[] getArrayFromElement(Element element,
			Class memberType) {
		NodeList childNodes = element.getChildNodes();
		int n = childNodes.getLength();
		int nbrActualNodes = 0;
		for (int i = 0; i < n; i++) {
			Node childNode = childNodes.item(i);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				nbrActualNodes++;
			}
		}
		Object[] array = (Object[]) Array.newInstance(memberType,
				nbrActualNodes);
		Class childType = null;
		int elementIdx = 0;
		for (int i = 0; i < n; i++) {
			Node childNode = childNodes.item(i);
			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element childElement = (Element) childNode;
			childType = ObjectManager.getElementType(childElement);
			if (childType == null) {
				childType = ObjectManager.getType(ObjectManager.toClassName(
						childElement.getNodeName(), element.getNodeName()),
						true); // Bug 845 - Exility Local Service Verbs should
								// work on server side also
			}
			if (childType == null) {
				continue;
			}
			Object childObject = ObjectManager.fromElement(childElement,
					childType);
			array[elementIdx] = childObject;
			elementIdx++;
		}
		return array;
	}

	private static void fillMapFromElement(Element element, Map map) {
		if (element.hasChildNodes() == false) {
			if (element.hasAttributes() == false) {
				Spit.out(element.getNodeName()
						+ " has no attributes are child nodes.");
				return;
			}
			fillMapfromAttributes(element, map);
			return;
		}
		NodeList childNodes = element.getChildNodes();
		int n = childNodes.getLength();
		Class childType = null;
		for (int i = 0; i < n; i++) {
			Node childNode = childNodes.item(i);
			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element childElement = (Element) childNode;
			childType = ObjectManager.getElementType(childElement);
			if (childType == null) {
				childType = ObjectManager.getType(ObjectManager.toClassName(
						childElement.getNodeName(), element.getNodeName()),
						true); // Bug 845 - Exility Local Service Verbs should
								// work on server side also
			}
			if (childType == null) {
				continue;
			}
			Object childObject = ObjectManager.fromElement(childElement,
					childType);
			String key = ObjectManager.getCandidateKey(childElement);
			if (map.containsKey(key)) {
				Spit.out("ERROR : " + key + " is a duplicate entry in "
						+ element.getNodeName());
			}
			map.put(key, childObject);
		}
	}

	private static void fillMapfromAttributes(Element element, Map mapObject) {
		NamedNodeMap attributes = element.getAttributes();
		for (int i = attributes.getLength() - 1; i >= 0; i--) {
			Node attribute = attributes.item(i);
			mapObject.put(attribute.getNodeName(), attribute.getNodeValue());
		}
	}

	private static Object getValueFromElement(Element element, Class type) {
		// if there is an attribute called value, that would be the field value
		String val = element.getAttribute(ObjectManager.VALUE);

		// If value is not found, try a child text/cdata section
		if ((val == null) || (val.length() == 0)) {
			Node aNode = element.getFirstChild();
			if (aNode != null) {
				short nodeType = aNode.getNodeType();
				if ((nodeType == Node.CDATA_SECTION_NODE)
						|| (nodeType == Node.TEXT_NODE)) {
					val = aNode.getTextContent();
				}
			}
		}

		if (val == null) {
			val = "";
		}
		return ObjectManager.parsePrimitive(type, val);
	}

	private static String getCandidateKey(Element element) {
		for (String keyName : ObjectManager.candidateKeyNames) {
			String key = element.getAttribute(keyName);
			if ((key != null) && (key.length() != 0)) {
				return key;
			}
		}
		return null;
	}

	/**
	 * build an xml from an Object.
	 * 
	 * @param rootObject
	 * @return xml string that represents this object instance
	 */
	public static String toXml(Object rootObject) {
		String xml = "";
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document document = builder.newDocument();
			String elementName = ObjectManager.toTagName(rootObject.getClass()
					.getSimpleName(),
					rootObject.getClass().getInterfaces().length);
			Element rootElement = document.createElement(elementName);
			ObjectManager.toElement(rootObject, rootElement, document);
			document.appendChild(rootElement);

			// let us write it out
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			DOMSource source = new DOMSource(document);
			StringWriter writer = new StringWriter();
			StreamResult stream = new StreamResult(writer);
			transformer.transform(source, stream);
			xml = writer.getBuffer().toString();
		} catch (Exception e) {
			Spit.out("Error outputting xml for "
					+ rootObject.getClass().getName() + ". " + e.getMessage());
		}
		return xml;
	}

	/**
	 * build an xml from an Object.
	 * 
	 * @param rootObject
	 * @param file
	 */
	public static void toXmlFile(Object rootObject, File file) {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document document = builder.newDocument();
			String elementName = ObjectManager.toTagName(rootObject.getClass()
					.getSimpleName(),
					rootObject.getClass().getInterfaces().length);
			Element rootElement = document.createElement(elementName);
			ObjectManager.toElement(rootObject, rootElement, document);
			document.appendChild(rootElement);

			// let us write it out
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			DOMSource source = new DOMSource(document);
			OutputStream out = new FileOutputStream(file);
			StreamResult stream = new StreamResult(out);
			transformer.transform(source, stream);
			out.close();
		} catch (Exception e) {
			Spit.out("Error outputting xml for "
					+ rootObject.getClass().getName() + ". " + e.getMessage());
		}
	}

	// in C# we can use ICollection as the type of both array and a collection.
	// Not in Java, and hence this method
	private static void toElements(Object[] children, Element element,
			Document document) {
		for (Object child : children) {
			if (child == null) {
				continue;
			}
			String tagName = ObjectManager.toTagName(child.getClass()
					.getSimpleName(), child.getClass().getInterfaces().length);
			Element childElement = document.createElement(tagName);
			element.appendChild(childElement);
			ObjectManager.toElement(child, childElement, document);
		}
	}

	// is the mapped Object is a user defined Object, then we assme that the
	// Object itself contains the key with which
	// it is mapped.
	// otherwise, i.e. if it is a primtive, we have to add the key
	private static void toElements(Map children, Element element,
			Document document) {
		for (Object id : children.keySet()) {
			Object value = children.get(id);
			String tagName = ObjectManager.toTagName(value.getClass()
					.getSimpleName(), value.getClass().getInterfaces().length);
			Element childElement = document.createElement(tagName);
			element.appendChild(childElement);
			ObjectManager.toElement(value, childElement, document);
		}
	}

	private static void toElement(Object objekt, Element element,
			Document document) {
		Class objectType = objekt.getClass();
		element.setAttribute(ObjectManager.OBJECT_TYPE,
				objectType.getSimpleName());
		if (ObjectManager.isValueType(objectType)) {
			// let us add only non-default values
			String val = ObjectManager.valueToString(objekt);
			if (val != null) {
				element.setAttribute(ObjectManager.VALUE, val);
			}
			return;
		}

		if (objectType.isArray()) {
			// is it an array of value types. In that case, a comma separated
			// String is built from all its values
			// and is set as a property.
			Class elementType = objectType.getComponentType();
			if (ObjectManager.isValueType(elementType)) {
				element.setAttribute(ObjectManager.VALUE,
						ObjectManager.valueArrayToString(objekt));
				return;
			}

			// array of objects. Will have to go for child elements
			ObjectManager.toElements((Object[]) objekt, element, document);
			return;
		}
		if (Map.class.isAssignableFrom(objectType)) {
			ObjectManager.toElements((Map) objekt, element, document);
			return;
		}
		// get all fields of this Object
		for (Field field : ObjectManager.getAllFields(objectType, true)
				.values()) {
			if (ObjectManager.toBeIgnored(field)) {
				continue;
			}
			String fieldName = field.getName();
			field.setAccessible(true);
			Object fieldValue = null;
			try {
				fieldValue = field.get(objekt);
			} catch (Exception e) {
				continue;
			}
			if (fieldValue == null) {
				continue;
			}
			Class type = field.getType();
			if (ObjectManager.isValueType(type)) {
				// skip if default value is present
				String valu = ObjectManager.valueToString(fieldValue);
				if (valu != null) {
					element.setAttribute(fieldName, valu);
				}
				continue;
			}
			// is it an array that have to be set as an attribute with comma
			// separated values?
			if (type.isArray()) {
				Class elementType = type.getComponentType();
				if (ObjectManager.isValueType(elementType)) {
					String arv = ObjectManager.valueArrayToString(fieldValue);
					if (arv.length() > 0) {
						element.setAttribute(fieldName, arv);
					}
					continue;
				}
			}
			// it is not a simple value, but an Object
			Element newElement = document.createElement(fieldName);
			element.appendChild(newElement);
			ObjectManager.toElement(fieldValue, newElement, document);
		}
	}

	private static String valueToString(Object value) {
		if (value instanceof Date) {
			return DateUtility.formatDate((Date) value);
		}
		String s = value.toString();
		if (s.length() == 0) {
			return null;
		}
		if (value instanceof String) {
			return s;
		}
		if (s.equals("0") || s.equals("false")) {
			return null;
		}
		return s;
	}

	// reverse of parsing
	private static String valueTypeToString(Object value) {
		if (value instanceof Date) {
			return DateUtility.formatDate((Date) value);
		}

		return value.toString();
	}

	private static String valueArrayToString(Object array) {
		if (array == null) {
			return "";
		}

		int n = Array.getLength(array);
		if (n == 0) {
			return "";
		}

		String commaSeparatedvalue = ObjectManager.valueTypeToString(Array.get(
				array, 0));
		for (int i = 1; i < n; i++) {
			commaSeparatedvalue += ',' + ObjectManager.valueTypeToString(Array
					.get(array, i));
		}

		return commaSeparatedvalue;
	}

	/**
	 * This method is used by jsUtil as of now. Idea is to serialize attributes
	 * into a java/script syntax with the convention that default values are not
	 * output.
	 * 
	 * @param objekt
	 * @param objectName
	 * @param sbf
	 */
	public static void serializePrimitiveAttributes(Object objekt,
			String objectName, StringBuilder sbf) {
		Class objectType = objekt.getClass();
		Map<String, Field> fields = ObjectManager
				.getAllFields(objectType, true);

		for (Field field : fields.values()) {
			if (ObjectManager.toBeIgnored(field)) {
				continue;
			}
			String fieldName = field.getName();
			field.setAccessible(true);
			Object fieldValue = null;
			try {
				fieldValue = field.get(objekt);
			} catch (Exception e) {
				continue;
			}
			if (fieldValue == null) {
				continue;
			}
			String val = ObjectManager
					.valueToSerializedString(fieldValue, true);
			if (val == null) {
				continue;
			}

			sbf.append('\n').append(objectName).append('.').append(fieldName)
					.append(" = ").append(val);
		}
	}

	/**
	 * returns a String for the supplied Object. It is different from standard
	 * toStrinig() in that it returns null if the value is default for that
	 * type. Defaults as per Exility conventions are false, 0, MAX_VALUE,
	 * MIN_VALUE and "" This is useful in serializing the Object.
	 * 
	 * @param value
	 * @param putQuotesIfRequired
	 * @return string that is suitable to be put into serialized form
	 */
	public static String valueToSerializedString(Object value,
			boolean putQuotesIfRequired) {
		if (value == null) {
			return null;
		}

		Class type = value.getClass();

		// we need a value type, and not a user-defined Object
		if (!ObjectManager.isValueType(type)) {
			return null;
		}
		boolean quotesRequired = false;
		String val = value.toString();
		if (val.length() == 0) {
			return null;
		}
		if (value instanceof String) {
			quotesRequired = true;
		}

		else if (value instanceof Boolean) {
			if (val.equals("false")) {
				return null;
			}
			val = "true";
		}

		else if (value instanceof Integer) {
			int ival = ((Integer) value).intValue();
			if (ival == 0 || ival == Integer.MAX_VALUE
					|| ival == Integer.MIN_VALUE) {
				return null;
			}
		}

		else if (value instanceof Double) {
			double dbl = ((Double) value).doubleValue();
			if (dbl == 0 || dbl == Double.MAX_VALUE || dbl == Double.MIN_VALUE) {
				return null;
			}
			val = ObjectManager.decimalFormat.format(dbl);
		}

		else if (value instanceof Date) {
			val = DateUtility.formatDate((Date) value);
		} else {
			return null;
		}

		if (putQuotesIfRequired && quotesRequired) {
			val = '"' + val + '"';
		}

		return val;
	}

	private static void removeTextChildren(Node element) {
		NodeList children = element.getChildNodes();
		int n = children.getLength();
		for (int i = n - 1; i >= 0; i--) {
			Node child = children.item(i);
			short nodeType = child.getNodeType();
			if (nodeType == Node.TEXT_NODE || nodeType == Node.COMMENT_NODE) {
				element.removeChild(child);
			} else if (nodeType == Node.ELEMENT_NODE) {
				ObjectManager.removeTextChildren(child);
			}
		}
	}

	// does not handle cases where collection has different concrete classes
	// (with different attributes)
	/***
	 * puts all attributes of an object to dc. Any object collection is added as
	 * a grid
	 * 
	 * @param object
	 * @param dc
	 */

	public static void toDc(Object object, DataCollection dc) {
		Class objectType = object.getClass();
		Field[] fields = ObjectManager.getLoadableFieldsFromObject(object);
		/*
		 * for recreating this object, we need the class name
		 */
		dc.addTextValue(ObjectManager.OBJECT_TYPE, objectType.getName());

		/*
		 * let us put each of the field value into dc
		 */
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				Object value = field.get(object);
				Class fieldType = field.getType();
				String fieldName = field.getName();
				if (value == null) {
					if (fieldType.equals(String.class)) {
						dc.addTextValue(fieldName, "");
					} else {
						Spit.out(fieldName
								+ " is null and hence no value is extracted.");
					}
					continue;
				}

				/*
				 * boolean value
				 */
				if (fieldType.equals(boolean.class)) {
					if (((Boolean) value).booleanValue()) {
						dc.addTextValue(fieldName, "1");
					} else {
						dc.addTextValue(fieldName, "0");
					}
					continue;
				}

				/*
				 * non-boolean value
				 */
				if (ObjectManager.isValueType(fieldType)) {
					dc.addTextValue(fieldName,
							ObjectManager.valueTypeToString(value));
					continue;
				}

				/*
				 * array
				 */
				if (fieldType.isArray()) {
					Class arrayType = fieldType.getComponentType();
					Object[] arr = (Object[]) value;
					if (arr.length == 0) {
						Spit.out("Array " + fieldName
								+ " has no entries. It is not added to dc");
					}

					else if (ObjectManager.isValueType(arrayType)) {
						dc.addTextValue(fieldName,
								ObjectManager.valueArrayToString(arr));
					} else {
						dc.addGrid(fieldName, ObjectManager.getAttributes(arr));
					}
					continue;
				}

				/*
				 * collections
				 */
				if (value instanceof Map) {
					Map map = (Map) value;
					dc.addGrid(fieldName, ObjectManager.getAttributes(map));
					continue;
				}

				if (value instanceof List) {
					List list = (List) value;
					dc.addGrid(fieldName,
							ObjectManager.getAttributes(list.toArray()));
					continue;
				}

				/*
				 * OK. it is another object. We put any other object into a grid
				 * of its own to
				 */
				Object[] objects = { value };
				dc.addGrid(fieldName, ObjectManager.getAttributes(objects));
			} catch (Exception e) {
				Spit.out("Error while adding attributes of object to dc\n "
						+ e.getMessage());
			}
		}
	}

	/***
	 * Get all field names of a class for the supplied object.
	 * 
	 * @param object
	 * @return
	 */
	private static String[] getAllFieldNamesFromObject(Object object) {

		Map<String, Field> allFields = ObjectManager.getAllFields(
				object.getClass(), false);
		String[] keys = allFields.keySet().toArray(new String[0]);
		Arrays.sort(keys);
		return keys;
	}

	/***
	 * return a text value that represents the value of this object
	 * 
	 * @param object
	 * @return text value
	 */
	private static String valueOf(Object object) {
		Class type = object.getClass();
		if (ObjectManager.isValueType(type)) {
			return ObjectManager.valueTypeToString(object);
		}

		// array of value types are represented as comma separated values
		if (type.isArray()) {
			Class arrayType = type.getComponentType();
			Object[] arr = (Object[]) object;
			if (ObjectManager.isValueType(arrayType)) {
				return ObjectManager.valueArrayToString(arr);
			}
		}

		// OK. we do not know what to do. Use the default..
		return object.toString();
	}

	/***
	 * Find the class name from dc and create an instance of that class before
	 * using the dc to set its attributes
	 * 
	 * @param dc
	 *            dc
	 * @return object with its attributes set from dc
	 */
	public static Object createFromDc(DataCollection dc) {
		Object objekt = null;
		String typeName = dc.getTextValue(ObjectManager.OBJECT_TYPE, "");
		if (typeName.length() == 0) {
			dc.addError("Design error: client did not send "
					+ ObjectManager.OBJECT_TYPE);
			return null;
		}

		objekt = ObjectManager.createNew(ObjectManager.MY_PACKAGE_NAME
				+ typeName);
		if (objekt == null) {
			dc.addError("Design error: Could not instantiate class " + typeName);
			return null;
		}
		ObjectManager.fromDc(objekt, dc);
		return objekt;
	}

	/***
	 * load attributes of an object instance from values found in dc
	 * 
	 * @param objekt
	 *            to load attributes to
	 * @param dc
	 */
	public static void fromDc(Object objekt, DataCollection dc) {
		Class objectType = objekt.getClass();
		Map<String, Field> fields = ObjectManager.getAllFields(objectType,
				false);
		for (Field field : fields.values()) {
			Class fieldType = field.getType();
			String fieldName = field.getName();
			String textValue = null;
			try {
				field.setAccessible(true);

				// simple case. There is a value for this field.
				if (dc.hasValue(fieldName)) {
					textValue = dc.getTextValue(fieldName, "");
					if (textValue.length() == 0) {
						continue;
					}

					// great combination. field is also a value type
					if (ObjectManager.isValueType(fieldType)) {
						ObjectManager.setFieldValue(objekt, field, textValue);
						continue;
					}

					// slightly complex. It could be a comma separated value
					if (fieldType.isArray()) {
						Class arrayType = fieldType.getComponentType();
						if (ObjectManager.isValueType(arrayType)) {
							ObjectManager.setFieldValue(objekt, field,
									textValue);
							continue;
						}
						Spit.out("Found a value of "
								+ textValue
								+ " for field "
								+ field.getName()
								+ ". But this field is an array, and hence value is expected in a grid instead.");
						// and let us keep our options open if a grid is also
						// found. Hence we fall-off this if block, rather moving
						// to next field;
					}

					// we also allow attributes of an exility resource (special
					// object instance)
					else if (ObjectManager.isExilityType(fieldType)) {
						// in this case, attribute value represents the class to
						// be used to instantiate this resource
						Object newObject = ObjectManager.createNew(textValue);
						if (newObject == null) {
							Spit.out("Attribute "
									+ fieldName
									+ " is of type "
									+ textValue
									+ ". Unable to create an instance of this  not be assiged a value of "
									+ textValue);
						} else {
							// attributes of this class are also found in the
							// same dc!!
							ObjectManager.fromDc(newObject, dc);
						}
						continue;
					} else {
						Spit.out("Attribute " + fieldName
								+ " could not be assiged a value of "
								+ textValue);
						continue;
					}
				}

				// Okay. Is there a grid for this field?
				Grid grid = dc.getGrid(fieldName);
				if (grid == null || grid.getNumberOfRows() == 0) {
					continue;
				}

				Class type = fieldType;
				if (fieldType.isArray()) {
					type = fieldType.getComponentType();
				}

				Object value = ObjectManager.fromGrid(grid.getRawData(), type);
				if (value == null) {
					Spit.out(fieldName
							+ " could not be assigend value from its grid with the same name.");
					continue;
				}

				if (fieldType.isArray()) {
					field.set(objekt, value);
					continue;
				}

				if (value.getClass().isArray() == false) {
					Spit.out("Unabel to convert grid " + fieldName
							+ " into an array of objects.");
					continue;
				}

				Object fieldValue = field.get(objekt);
				if (fieldValue == null) {
					int modifiers = fieldType.getModifiers();
					if (Modifier.isAbstract(modifiers)
							|| Modifier.isAbstract(modifiers)) {
						Spit.out(fieldName
								+ " is not a concrete class, and there is no concrete class name is found to create an instance for this field.");
						continue;
					}
					fieldValue = ObjectManager.createNew(fieldType);
					field.set(objekt, fieldValue);
				}

				Object[] objects = (Object[]) value;
				// OK we have the values as an array of objects. Are we using
				// Map as a colleciton?
				if (fieldType.isAssignableFrom(Map.class)) {
					Map map = (Map) fieldValue;
					for (Object obj : objects) {
						map.put(ObjectManager.getKeyValue(obj), obj);
					}
					continue;
				}

				// Quite unlikely but is it a list?
				if (fieldType.isAssignableFrom(List.class)) {
					List list = (List) ObjectManager.createNew(fieldType);
					for (Object obj : objects) {
						list.add(obj);
					}
					continue;
				}
				// OK. There is still one other possibility. It can be an
				// object, whose instance was present as a single row in this
				// grid
				field.set(objekt, objects[0]);
			} catch (Exception e) {
				Spit.out("ERROR : While creating Object instance from dc "
						+ e.getMessage());
				Spit.out(e);
			}
		}
		if (objekt instanceof ToBeInitializedInterface) {
			((ToBeInitializedInterface) objekt).initialize();
		}
	}

	/***
	 * return an array of instances of type with attributes set from the
	 * supplied raw data. A column with name '_type' can have specific sub-type
	 * to be used for instantiating the object for that row
	 * 
	 * @param data
	 *            raw data (array of array of text values with first column
	 *            being column names)
	 * @param type
	 *            class of the object to be instantiated
	 * @return array of object instances
	 */
	private static Object fromGrid(String[][] data, Class type) {
		int n = data.length;
		Object arre = Array.newInstance(type, n - 1);
		String[] columnNames = data[0];

		int typeIdx = 0;
		for (String columnName : columnNames) {
			if (ObjectManager.OBJECT_TYPE.equals(columnName)) {
				break;
			}
			typeIdx++;
		}

		Class commonType = null;
		if (typeIdx >= columnNames.length) {
			commonType = type;
		}
		for (int i = 1; i < n; i++) {
			String[] aRow = data[i];
			Object object;
			if (commonType == null) {
				object = ObjectManager.createNew(ObjectManager.getType(
						aRow[typeIdx], true));
				if (object == null) {
					Spit.out("Unable to create an instance for row number " + i
							+ " with " + aRow[typeIdx] + " as its type.");
					continue;
				}
			} else {
				object = ObjectManager.createNew(commonType);
				if (object == null) {
					Spit.out("Unable to create an instance for row number " + i
							+ " with " + type.getName() + " as its type.");
					continue;
				}
			}

			Array.set(arre, i - 1, object);
			// load its attributes
			for (int j = 0; j < columnNames.length; j++) {
				if (j != typeIdx) {
					String colValue = aRow[j];
					if (colValue != null && colValue.length() > 0) {
						ObjectManager.setFieldValue(object, columnNames[j],
								colValue);
					}
				}
			}

		}
		return arre;
	}

	/***
	 * get possible key/id for this object. Predefined, ordered list of
	 * attribute names are used for this. null is returned if no suitable
	 * attribute found
	 * 
	 * @param object
	 *            object instance for which key is to be suggested
	 * @return suggested key/id or null
	 */
	private static String getKeyValue(Object object) {
		for (String key : ObjectManager.candidateKeyNames) {
			String keyValue = ObjectManager.getFieldValueAsString(object, key);
			if (keyValue != null && keyValue.length() > 0) {
				return keyValue;
			}
		}
		return null;
	}

	/***
	 * extract known set of attributes from an object and add them as values
	 * into grid
	 * 
	 * @param object
	 *            from which to extract attributes
	 * @param dc
	 *            to which values are to be added
	 * @param attributes
	 *            array of attribute names to be extracted from the object
	 */

	public static void addAttributesToDc(Object object, DataCollection dc,
			String[] attributes) {
		Class type = object.getClass();
		String typeName = type.getSimpleName();
		dc.addTextValue(ObjectManager.OBJECT_TYPE, typeName);
		for (String attName : attributes) {
			String valAsText = "";
			try {
				valAsText = ObjectManager.valueOf(type.getField(attName).get(
						object));
			} catch (Exception e) {
				Spit.out(attName + " can not be found in an instance of "
						+ typeName + ". Attribute not added to dc.");
			}
			dc.addTextValue(attName, valAsText);
		}
	}

	/***
	 * get a raw data (array of array of string) that represents all attributes
	 * of a collection of objects. This in turn uses name sake method that works
	 * on an array. Rows are sorted by key to the map
	 * 
	 * @param map
	 *            collection of objects
	 * @return raw data representing all attributes of all objects in the list
	 */
	public static String[][] getAttributes(Map<String, ? extends Object> map) {
		int n = map.size();
		if (n == 0) {
			return new String[0][0];
		}

		// get keys into an array
		String[] sortedKeys = map.keySet().toArray(new String[0]);
		Arrays.sort(sortedKeys);
		Object[] objects = new Object[n];

		// push objects into an array with this order
		int i = 0;
		for (String key : sortedKeys) {
			objects[i] = map.get(key);
			i++;
		}
		return ObjectManager.getAttributes(objects);
	}

	/***
	 * Get attributes of an array of objects into a grid, assuming that all
	 * objects in the array are instances of the same class as that of the first
	 * object. Note that only value attributes are extracted, and any object
	 * attributes are ignored
	 * 
	 * @param objects
	 *            array of objects from which to extract attributes
	 * @return array of arrays, with first row as header that has all field
	 *         names of the class. In addition, first column has the class name
	 *         of the actual instance of the object
	 */
	public static String[][] getAttributes(Object[] objects) {
		int nbrObjects = objects.length;
		if (nbrObjects == 0) {
			return new String[0][0];
		}

		/*
		 * we use the first object as the reference for getting all possible
		 * fields. This would be an issue if this array contains different
		 * extended classes, and the base class does not implement
		 * LoadableInterface.
		 */
		String[] attributes = ObjectManager
				.getAllFieldNamesFromObject(objects[0]);
		/*
		 * one additional column for object Type
		 */
		int nbrCols = attributes.length + 1;

		/*
		 * one additional row for header
		 */
		String[][] grid = new String[nbrObjects + 1][];
		String[] header = new String[nbrCols];
		header[0] = ObjectManager.OBJECT_TYPE;

		/*
		 * is the following loop better than for(i=.....)? I am assuming that to
		 * be true
		 */
		int i = 1;
		for (String s : attributes) {
			header[i] = s;
			i++;
		}

		grid[0] = header;

		/*
		 * let us push one row for each object
		 */
		i = 1;
		for (Object objekt : objects) {
			String[] row = new String[nbrCols];
			grid[i] = row;
			i++;

			/*
			 * first column is _type
			 */
			Class type = objekt.getClass();
			row[0] = type.getSimpleName();

			int j = 1;
			for (String attName : attributes) {
				row[j] = ObjectManager.getFieldValueAsString(objekt, attName);
				j++;
			}
		}

		return grid;
	}

	/***
	 * Each object in the array could be different Type. Hence, this method does
	 * not go with attributeName as header column Instead each row will have
	 * name and values as two columns, with first column as a one-up number key
	 * 
	 * @param objects
	 *            Objects whose attributes are to be extracted into array
	 * @param dc
	 *            into which this is to be added to
	 * @param keyPrefix
	 *            First column of the array will have this
	 *            prefix+indexOfObjectInArray as value for all attribute rows
	 * @param tableName
	 */
	public static void toDc(Object[] objects, DataCollection dc,
			String keyPrefix, String tableName) {
		List<String[]> entries = new ArrayList<String[]>();
		entries.add(ObjectManager.HEADER_FOR_ATTRIBUTE_ARRAY);
		int nbrObjects = objects.length;

		if (nbrObjects > 0) {
			int keyIdx = 1;
			for (Object object : objects) {
				String id = keyPrefix + keyIdx;
				keyIdx++;
				ObjectManager.addAttributesAsRows(entries, object, id, null);
			}
		}

		// create and add grid from these rows
		String[][] rawData = entries.toArray(new String[0][0]);
		dc.addGridAfterCheckingInDictionary(tableName, rawData);
	}

	/***
	 * Each attribute of the object is added as a row into the list.
	 * 
	 * @param rows
	 *            to which attributes are to be added
	 * @param object
	 *            from which attributes are to be extracted
	 * @param id
	 *            value of first column in the row. With this, each row works as
	 *            if first+second column is the key for that row
	 * @param parentFields
	 *            collection of attributes of parent object, in case this object
	 *            is a child object, and we are extracting attributes of both
	 *            into the same list
	 */
	private static void addAttributesAsRows(List<String[]> rows, Object object,
			String id, Map<String, Field> parentFields) {
		Map<String, Field> fields = ObjectManager.getAllFields(
				object.getClass(), false);
		for (String fieldName : fields.keySet()) {
			if (parentFields != null && parentFields.containsKey(fieldName)) {
				Spit.out("ERROR: Exility resource that is an attribute of a parent resource has a field name "
						+ fieldName
						+ ". This name clashes with that of teh parent resource. Current design of loading/unloadin fails in this case");
				continue;
			}

			Field field = fields.get(fieldName);
			Class fieldType = field.getType();

			// value attributes are the simplest to handle
			if (ObjectManager.isValueType(fieldType)) {
				String[] row = { id, fieldName,
						ObjectManager.getFieldValueAsString(object, fieldName) };
				rows.add(row);
				continue;
			}

			Object fieldValue = ObjectManager.getFieldValue(object, fieldName);
			if (fieldType.isArray()) {
				if (ObjectManager.isValueType(fieldType.getComponentType())) {
					String arv = ObjectManager.valueArrayToString(fieldValue);
					if (arv.length() > 0) {
						String[] row = { id, fieldName, arv };
						rows.add(row);
					}
				} else {
					Spit.out("Ojbect Manager does not know how to extract value of field '"
							+ fieldName
							+ "' as it is an array of "
							+ fieldType.getComponentType());
				}

				continue;
			}

			// we can have another resource as an attribute. e.g. task in a task
			// step.
			if (ObjectManager.isExilityType(fieldType)) // it is another Exility
														// resource
			{
				// A child can not have another child. Why? Because our design
				// can not handle this scenario as of now.
				if (parentFields != null) {
					Spit.out("Object Manager is not designed to extract attributes if on Exility Resource which that is an attribute of another Exility resource, has an Exility resource as its attribute");
					continue;
				}

				Object childObject;
				try {
					childObject = field.get(object);
				} catch (Exception e) {
					Spit.out(fieldName + " could not be extracted for id " + id
							+ " error: " + e.getMessage());
					continue;
				}

				if (childObject == null) {
					Spit.out(fieldName + " is null for id " + id);
					continue;
				}

				// add a row with class name as value of this field/attribute
				String[] row = { id, fieldName,
						childObject.getClass().getName() };
				rows.add(row);

				// recurse to add all its attributes
				ObjectManager
						.addAttributesAsRows(rows, childObject, id, fields);
				continue;
			}

			// it is some other object. This is not handled at this time
			Spit.out(fieldType.getName()
					+ " is a field that can not be loaded/extracted by ObjectManager");
		}
	}

	/**
	 * escape characters for the string to be put into an xml text. we escape &,
	 * ", and <
	 * 
	 * @param text
	 * @return text with possibly replaced characters for &, " and <
	 */
	public static String xmlEscape(String text) {
		if (text == null || text.length() == 0) {
			return "";
		}
		return text.replace("&", "&amp;").replace("\"", "&quot;")
				.replace("<", "&lt;");
	}

	/**
	 * copy attributes from one object to the other, but do not override
	 * whatever attributes fromObject already has. This is a way to populate
	 * attribute from one object to the other without over-riding existing
	 * values. attribute values from another object
	 * 
	 * @param fromValues
	 * 
	 * @param toObject
	 */
	public static void copyAttributes(Map<String, String> fromValues,
			Object toObject) {
		Map<String, Field> toFields = ObjectManager.getAllFields(
				toObject.getClass(), false);
		/*
		 * loop for each attribute-value to be set to toObject
		 */
		for (String attName : fromValues.keySet()) {
			String attValue = fromValues.get(attName);
			if (ObjectManager.isSpecified(attValue) == false) {
				continue;
			}

			Field toField = toFields.get(attName);

			if (toField == null) {
				/*
				 * no field with this name
				 */
				continue;
			}

			try {
				toField.setAccessible(true);
				Object toValue = null;
				toValue = toField.get(toObject);
				if (ObjectManager.isSpecified(toValue)) {
					/*
					 * object has a value for this field. We are not to
					 * over-ride
					 */
					continue;
				}

			} catch (Exception e) {
				Spit.out(e.getMessage());
				continue;
			}
			ObjectManager.setFieldValue(toObject, toField, attValue);
		}
	}

	private static boolean isSpecified(Object value) {
		if (value == null) {
			return false;
		}
		String textValue = value.toString();
		if (textValue.length() == 0 || textValue.equals("0")
				|| textValue.equals(Boolean.toString(false))) {
			return false;
		}
		return true;
	}

}

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/***
 * Resource class that deals with Data Types. Keeps a private static instance of
 * itself and exposes static methods
 * 
 */
public class DataTypes {
	String version = "1.0";
	/***
	 * data type name is to be unique across all files. We do not take file name
	 * as prefix.
	 */
	Map<String, AbstractDataType> dataTypes = new HashMap<String, AbstractDataType>();

	private static DataTypes instance = new DataTypes();

	/***
	 * Get data type. Data Types are cached into memory at the time of loading.
	 * If a data type is not found, a default TextDataType is returned. If you
	 * do not want a default, use getDataTypeOrNull() instead
	 * 
	 * @param dataTypeName
	 *            Name of data type
	 * @param dc
	 *            optional. Message is added to dc if data type is not found
	 * @return DataType for the name, failing which, a default TextDataType.
	 */
	public static AbstractDataType getDataType(String dataTypeName,
			DataCollection dc) {
		if (dataTypeName == null) {
			Spit.out("A request was made for a dataType with null as name. Default data type is returned.");
		} else {
			if (DataTypes.instance.dataTypes.containsKey(dataTypeName)) {
				return DataTypes.instance.dataTypes.get(dataTypeName);
			}

			Spit.out(dataTypeName
					+ " does not exist. A default data type is assumed.");
		}
		return new TextDataType();
	}

	/**
	 * internal routine that wants to deal with non-existing data types should
	 * call this, because getDataType is forgiving, and will return a default
	 * text data type without any fuss.
	 * 
	 * @param dataTypeName
	 * @return data type or null
	 */
	public static AbstractDataType getDataTypeOrNull(String dataTypeName) {
		return DataTypes.instance.dataTypes.get(dataTypeName);
	}

	static void reload(boolean removeExistingTypes) {
		if (DataTypes.instance == null || removeExistingTypes) {
			DataTypes.instance = new DataTypes();
		}
		DataTypes.load();
	}

	/***
	 * load all messages from resources folder
	 */
	static synchronized void load() {
		try {
			Map<String, Object> types = ResourceManager.loadFromFileOrFolder(
					"dataTypes", "dataType", ".xml");
			for (String fileName : types.keySet()) {
				Object obj = types.get(fileName);
				if (obj instanceof DataTypes == false) {
					Spit.out("dataTypes folder contains an xml that is not dataTypes. File ignored");
					continue;
				}
				DataTypes dt = (DataTypes) obj;
				DataTypes.instance.copyFrom(dt);
			}
			Spit.out(DataTypes.instance.dataTypes.size() + " dataTypes loaded.");
		} catch (Exception e) {
			e.printStackTrace();
			Spit.out("Unable to load data types : " + e.getMessage());
		}
	}

	/***
	 * copy and add types into global collection from the supplied collection
	 * 
	 * @param types
	 *            collection of data types to be added
	 */
	private void copyFrom(DataTypes types) {
		for (AbstractDataType dt : types.dataTypes.values()) {
			if (this.dataTypes.containsKey(dt.name)) {
				Spit.out("Error : " + dt.name
						+ " is defined more than once as a dataType");
				continue;
			}
			this.dataTypes.put(dt.name, dt);
		}
	}

	/***
	 * Get a collection of data type names
	 * 
	 * @return collection of names of data types
	 */
	public static Set<String> getDataTypeNames() {
		return DataTypes.instance.dataTypes.keySet();
	}

	/***
	 * return the singleton instance. To be called only by Internal utilities.
	 * 
	 * @return singleton instance of DataTypes
	 */
	static DataTypes getInstance() {
		return DataTypes.instance;
	}

	/***
	 * remove a data type from the collection.
	 * 
	 * @param dataType
	 *            data type to be removed from the collection
	 */
	static void removeDataType(AbstractDataType dataType) {
		if (DataTypes.instance == null) {
			return;
		}
		DataTypes.instance.dataTypes.remove(dataType.name);
	}

	/***
	 * Add/replace a data type to the collection
	 * 
	 * @param dataType
	 *            data type to be added/replaced
	 */
	static void addDataType(AbstractDataType dataType) {
		if (DataTypes.instance == null) {
			DataTypes.instance = new DataTypes();
		}
		DataTypes.instance.dataTypes.put(dataType.name, dataType);
	}

	/**
	 * add a data type to the collection
	 * 
	 * @param dataType
	 */
	public void put(AbstractDataType dataType) {
		this.dataTypes.put(dataType.name, dataType);
	}

	/**
	 * 
	 * @return sorted array of all data types defined in this project
	 */
	public static String[] getAllDataTypes() {
		String[] allOfThem = getInstance().dataTypes.keySet().toArray(
				new String[0]);
		Arrays.sort(allOfThem);
		return allOfThem;
	}
}
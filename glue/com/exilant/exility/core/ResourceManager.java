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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/***
 * Utility class to manage loading/saving resources from/to resource folders.
 * 
 */
public class ResourceManager {

	private static final String PROPERTY_NAME = "exility.resourceFolder";
	private static final String ENV_NAME = "EXILITY_RESOURCE_FOLDER";
	private static final String TEST_FOLDER_NAME = "test/testCase/";

	/***
	 * resources that are saved as .XLS. This is a first-cut design to allow
	 * workflow to be saved as XLSX We are also assuming that if this resource
	 * is in this map, then it is saved in XLX. TO-DO: re-factor as and when we
	 * have resource (component) that is in XML, and its class is not
	 * com.exilant.exility.core.
	 */
	private static Map<String, String> resourceTypes = new HashMap<String, String>();

	/***
	 * Store the resource types that are not core, AND use xlsx
	 */
	static {
		ResourceManager.resourceTypes.put("workflow", ".xls");
	}

	/**
	 * folder that stores all resources. After initial loading, this folder is
	 * where we look for resources that are not cached, like services and tasks
	 */
	private static String resourceFolder = null;

	/**
	 * root folder of the web application, if this is deployed as a web
	 * application. We just store this pass on to callers, but we do not use it
	 * anywhere. page generator is a known user of this field.
	 */
	private static String rootFolder = null;

	/**
	 * at times, we need to use an application parameters file other than the
	 * one in resource folder.
	 */
	private static String appFileName = null;

	/***
	 * this should be the first statement to execute in the entire application,
	 * except if you call setApplicationParametersFileName() called from startup
	 * servlet in case this is deployed in an HTTP server.
	 * 
	 * @deprecated use loadAllResources() instead
	 * @param folderName
	 */
	@Deprecated
	public static void setResourceFolder(String folderName) {
		loadAllResources(null, folderName);
		return;
	}

	/***
	 * root folder as in the web application
	 * 
	 * @param folderName
	 */
	public static void setRootFolder(String folderName) {
		ResourceManager.rootFolder = folderName;
	}

	/***
	 * root folder, as in the web application root
	 * 
	 * @return root folder name
	 */
	public static String getRootFolderName() {
		return ResourceManager.rootFolder;
	}

	/***
	 * resource folder is normally under WEB-INF so that it is not accessible to
	 * client
	 * 
	 * @return resource folder name
	 */
	public static String getResourceFolder() {
		return ResourceManager.resourceFolder;
	}

	/**
	 * folder where test cases are to be captured
	 * 
	 * @return name of folder ending with '/'
	 */
	public static String getTestCaseFolder() {
		return ResourceManager.resourceFolder + TEST_FOLDER_NAME;
	}

	/***
	 * try various ways of setting resource folder.<br />
	 * 1. System.getProperty called exility.resourceFolder 2.environment
	 * variable EXILITY_RESOURCE_FOLDER
	 * 
	 * @return true if we are able to set it, false otherwise
	 */
	private static boolean trySettingResourceFolder() {
		Spit.out("Resource folder was not set by a startup servlet. Locating resource folder : looking for property with name "
				+ ResourceManager.PROPERTY_NAME);
		ResourceManager.resourceFolder = System
				.getProperty(ResourceManager.PROPERTY_NAME);
		if ((ResourceManager.resourceFolder == null)
				|| (ResourceManager.resourceFolder.length() == 0)) {
			Spit.out("Property not found, trying environment variable "
					+ ResourceManager.ENV_NAME);
			ResourceManager.resourceFolder = System
					.getenv(ResourceManager.ENV_NAME);
		}
		if ((ResourceManager.resourceFolder == null)
				|| (ResourceManager.resourceFolder.length() == 0)) {
			Spit.out("ERROR: Unable to locate resource folder. You should set either property "
					+ ResourceManager.PROPERTY_NAME
					+ " or environment variable " + ResourceManager.ENV_NAME);
			Spit.out("Alternately, edit web.xml in WEB-INF folder and add a context-param with name resource-folder and value that points to the absolute pth of your resource folder.");
			Spit.out("Exility Engine will be cranky ;-)");
			return false;
		}
		if (ResourceManager.resourceFolder.endsWith("/") == false
				&& ResourceManager.resourceFolder.endsWith("\\") == false) {
			ResourceManager.resourceFolder += '/';
		}

		// TODO - temp code to set base path for FileUtility
		FileUtility.setBasePath(new File(ResourceManager.resourceFolder)
				.getParent());
		return true;
	}

	/***
	 * read a resource into a string. resource name is possibly qualified (.)
	 * that follows folder structure
	 * 
	 * @param resourceName
	 * @return contents of a resource file as text
	 */
	public static String readResourceNotUsedAnyMore(String resourceName) {
		if (ResourceManager.resourceFolder == null) {
			if (ResourceManager.trySettingResourceFolder() == false) {
				return null;
			}
		}

		String resourceFullName = ResourceManager.resourceFolder
				+ resourceName.replace('.', '/') + ".xml";
		return ResourceManager.readFile(resourceFullName);
	}

	/***
	 * read file into string.
	 * 
	 * @param fileName
	 *            fully qualified file name, including extension
	 * @return read the contents of a file as text
	 */
	public static String readFile(String fileName) {
		File file = new File(fileName);
		if (file.exists() == false) {
			Spit.out(fileName + " does not exist. trying in resource folder");
			// try and look for file in resource root
			file = new File(ResourceManager.getResourceFolder() + fileName);
			if (file.exists() == false) {
				Spit.out(ResourceManager.getResourceFolder() + fileName
						+ " does not exist in resource folder.");
				return null;
			}
		}
		return ResourceManager.readFile(file);
	}

	/***
	 * read a file into string
	 * 
	 * @param file
	 * @return read contents of a file
	 */
	public static String readFile(File file) {
		try {
			int n = (int) file.length();
			char[] buf = new char[n];
			Reader reader = new InputStreamReader(new FileInputStream(file),
					Charset.forName("UTF-8"));
			reader.read(buf);
			reader.close();
			return new String(buf);
		} catch (Exception e) {
			Spit.out("Error reading resource " + file.getName() + ". "
					+ e.getMessage());
			return null;
		}
	}

	/***
	 * gets a File for the file name, either as it is, or in resource folder
	 * 
	 * @param fileName
	 * @return file object for the file
	 */
	public static File getFile(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			return file;
		}

		Spit.out(fileName + " does not exist. trying in resource folder ");
		String tryName = ResourceManager.getResourceFolder() + fileName;
		file = new File(tryName);
		if (file.exists()) {
			return file;
		}

		Spit.out(tryName + " does not exist. trying in root folder ");
		tryName = ResourceManager.getRootFolderName() + fileName;
		file = new File(tryName);
		if (file.exists()) {
			return file;
		}

		Spit.out("Unable to locate " + fileName
				+ ". Resource will not be loaded.");
		return null;
	}

	/***
	 * return an object instance for the resource name
	 * 
	 * @param resourceName
	 * @param resourceObjectType
	 * @return instance object whose attributes are loaded from the resource
	 *         file
	 */
	@SuppressWarnings("rawtypes")
	public static Object loadResource(String resourceName,
			Class resourceObjectType) {
		if (ResourceManager.resourceFolder == null) {
			Spit.out("Resource folder is not set for this project. ResourceManager.loadAllResources() should be called");
			return null;
		}

		String resourceFullName = ResourceManager.resourceFolder
				+ resourceName.replace('.', '/');
		File file = null;

		/**
		 * see if this resource is an xlsx. as of now our design is to decide
		 * this based on resource prefixes
		 */
		int firstDelimiter = resourceName.indexOf('.');
		if (firstDelimiter > 0) {
			String resType = resourceName.substring(0, firstDelimiter);
			Spit.out("About to load a resource of type " + resType);
			String extn = ResourceManager.resourceTypes.get(resType);
			if (extn != null) // this is an xls based non-core package component
			{
				resourceFullName += extn;
				DataCollection dc = new DataCollection();
				Object object = ObjectManager.createNew(resourceObjectType);
				XlxUtil.getInstance().extract(resourceFullName, dc, true);
				ObjectManager.fromDc(object, dc);
				return object;
			}
		}

		resourceFullName += ".xml";
		file = ResourceManager.getFile(resourceFullName);
		if (file == null) {
			return null;
		}
		return ObjectManager.fromXml(file);
	}

	/***
	 * get the file name corresponding to a fully qualified resource name
	 * 
	 * @param resourceName
	 * @return file name for the resource as per convention of folder name and
	 *         folder separator
	 */
	public static String translateResourceFileName(String resourceName) {
		String resourceFullName = ResourceManager.resourceFolder
				+ resourceName.replace('.', '/') + ".xml";
		return resourceFullName;
	}

	/***
	 * load the content of the file into an exility design component instance
	 * 
	 * @param fileName
	 *            fully qualified file name
	 * @param resourceObjectType
	 * @return object instance with its attributes loaded from the file
	 */
	public static Object loadResourceFromFile(String fileName,
			@SuppressWarnings("rawtypes") Class resourceObjectType) {
		File file = ResourceManager.getFile(fileName);
		if (file == null) {
			return null;
		}
		return ObjectManager.fromXml(file, resourceObjectType);
	}

	/**
	 * 
	 * @param name
	 * @return full name of a resource
	 */
	public static String getResourceFullName(String name) {
		String fullName = null;
		Field classField = null;
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		try {
			// This can fail if this is not a Sun-compatible JVM
			// or if the security is too tight:

			classField = ClassLoader.class.getDeclaredField("classes");
			if (classField.getType() != Vector.class) {
				throw new RuntimeException("not of type java.util.Vector: "
						+ classField.getType().getName());
			}
			classField.setAccessible(true);
			@SuppressWarnings("rawtypes")
			Vector classes = (Vector) classField.get(loader);
			for (Object curObject : classes) {
				@SuppressWarnings("rawtypes")
				Class curClass = (Class) curObject;
				if (curClass.getName().endsWith(name)) {
					fullName = curClass.getCanonicalName();
				}
			}
		} catch (Exception e) {
			Spit.out("Load error: " + name + " could not be obtained. "
					+ e.getMessage());
			Spit.out(e);
			return null;
		}
		return fullName;
	}

	/***
	 * Save a component as xml in the resource folder as per naming conventions
	 * 
	 * @param resourceName
	 *            fully qualified name. qualified part of names is used for
	 *            folder structure.
	 * @param resourceObject
	 *            actual resource object that needs to be persisted
	 */
	public static void saveResource(String resourceName, Object resourceObject) {
		String fileName = ResourceManager.resourceFolder
				+ resourceName.replace('.', '/') + ".xml";
		Spit.out("Going to Save " + fileName);

		// if the file exists, let us rename it. We append current date/time to
		// file name
		String stamp = ResourceManager.getTimeStamp();
		File file;
		try {
			file = new File(fileName);
			if (file.exists()) {
				File oldFile = new File(fileName + stamp + ".bak");
				boolean ok = file.renameTo(oldFile);
				if (!ok) {
					file.delete();
				}
				file = new File(fileName);
			}
		} catch (Exception e) {
			Spit.out("Error saving resource  to file " + fileName + ". "
					+ e.getMessage());
			return;
		}
		ObjectManager.toXmlFile(resourceObject, file);
	}

	/***
	 * get a string for current time that can be used as time-stamp-up to second
	 * for files.
	 * 
	 * @return YYYY_MM_DD_HH_MM_SEC
	 */
	public static String getTimeStamp() {
		Calendar dt = Calendar.getInstance();
		String stamp = dt.get(Calendar.YEAR) + "_" + dt.get(Calendar.MONTH)
				+ "_" + dt.get(Calendar.DATE) + "_"
				+ dt.get(Calendar.HOUR_OF_DAY) + "_" + dt.get(Calendar.MINUTE)
				+ "_" + dt.get(Calendar.SECOND);

		return stamp;
	}

	/**
	 * rename a file into its back-up. This is what you use for removing a
	 * resource.
	 * 
	 * @param fileName
	 * @return true if we indeed renamed it. false if file wasn't there to be
	 *         renamed
	 */
	public static boolean renameAsBackup(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			return ResourceManager.renameAsBackup(file);
		}
		return false;
	}

	/***
	 * rename a file to a time-stamped .bak file.
	 * 
	 * @param file
	 *            file to be renamed
	 * @return true if rename was successful, false otherwise
	 */
	public static boolean renameAsBackup(File file) // throws ExilityException
	{
		// if the file exists, let us rename it. We append current date/time to
		// file name
		Calendar dt = Calendar.getInstance();
		String stamp = dt.get(Calendar.YEAR) + "_" + dt.get(Calendar.YEAR)
				+ "_" + dt.get(Calendar.MONTH) + "_" + dt.get(Calendar.DATE)
				+ "_" + dt.get(Calendar.HOUR_OF_DAY) + "_"
				+ dt.get(Calendar.MINUTE) + "_" + dt.get(Calendar.SECOND);
		try {
			File oldFile = new File(file.getAbsolutePath() + stamp + ".bak");
			return file.renameTo(oldFile);
		} catch (Exception e) {
			Spit.out("Unable to rename " + file.getAbsolutePath() + " to "
					+ file.getAbsolutePath() + stamp + ".bak. "
					+ e.getMessage());
			return false;
		}
	}

	/**
	 * 
	 * @param fileName
	 * @param content
	 */
	public static void saveText(String fileName, String content) {
		Spit.out("Going to save " + fileName);
		try {
			// create folder if required
			int idx = fileName.lastIndexOf('/');
			if (idx > 0) {
				ResourceManager.createFolder(fileName.substring(0, idx));
			}

			File file = new File(fileName);
			OutputStreamWriter writer = new OutputStreamWriter(
					new FileOutputStream(file), Charset.forName("utf-8"));
			writer.write(content);
			writer.close();
		} catch (Exception e) {
			Spit.out("Error saving resource  to file " + fileName + ". "
					+ e.getMessage());
			Spit.out("stacktrace=" + e.getStackTrace());
			return;
		}
		Spit.out(fileName + " saved.");
	}

	/**
	 * 
	 * @param startingFolder
	 * @return array of list of files. Each row has two columns first is file
	 *         name and second one to indicate whether it is a folder. First row
	 *         is the header
	 */
	public static String[][] getFiles(String startingFolder) {
		List<String> names = new ArrayList<String>();
		List<String> fileOrFolder = new ArrayList<String>();
		String folderName = ResourceManager.resourceFolder;
		if (startingFolder != null && startingFolder.length() > 0) {
			String thisFolderName = startingFolder.replaceAll(".", "/");
			folderName += thisFolderName;
			names.add(thisFolderName);
			fileOrFolder.add("folder");
		}
		File file = new File(folderName);
		ResourceManager.addFiles(file, "", names, fileOrFolder);
		int n = names.size();
		String[][] data = new String[n + 1][2];
		data[0][0] = "name";
		data[0][1] = "isFolder";
		for (int i = 0; i < n; i++) {
			String[] row = data[i + 1];
			row[0] = names.get(i);
			row[1] = fileOrFolder.get(i);
		}
		return data;
	}

	/**
	 * 
	 * @param folder
	 * @param prefix
	 * @param names
	 * @param fileOrFodler
	 */
	private static void addFiles(File folder, String prefix,
			List<String> names, List<String> fileOrFodler) {
		File[] files = folder.listFiles();
		if (files == null) {
			return;
		}

		int n = files.length;
		// accumulate filenames and folder names into separate arrays. n is
		// anyways the max. More efficient than using lists
		String[] fileNames = new String[n];
		int nbrFiles = 0;

		Map<String, File> folders = new HashMap<String, File>();
		int nbrFolders = 0;
		for (File f : files) {
			if (f.isHidden()) {
				continue;
			}
			String name = f.getName();
			if (name.equalsIgnoreCase("SVN") || name.equalsIgnoreCase("CVS")
					|| name.equalsIgnoreCase("VSS") || name.startsWith(".")) {
				continue;
			}
			if (f.isDirectory()) {
				folders.put(name, f);
				nbrFolders++;
			} else {
				fileNames[nbrFiles] = name;
				nbrFiles++;
			}
		}
		if (nbrFolders > 0) {
			String[] folderNames = folders.keySet().toArray(new String[0]);
			Arrays.sort(folderNames);
			for (String folderName : folderNames) {
				String newName = prefix + folderName;
				names.add(newName);
				fileOrFodler.add("folder");
				ResourceManager.addFiles(folders.get(folderName),
						newName + '/', names, fileOrFodler);
			}
		}
		if (nbrFiles > 0) {
			fileNames = Arrays.copyOf(fileNames, nbrFiles);
			Arrays.sort(fileNames);
			for (String fileName : fileNames) {
				names.add(prefix + fileName);
				fileOrFodler.add("file");
			}
		}
	}

	/**
	 * get all files and folders in the supplied root folder. Returned array has
	 * a header row, and a row for the root folder. If root folder is non null,
	 * a row for this root folder is added. Each row has three columns :
	 * fileName, folderName and isFolder (1/0)
	 * 
	 * @param rootFolderName
	 * @return array of all files/folder, excluding hidden ones and cvs/svn
	 *         etc..
	 */
	public static String[][] getAllFiles(String rootFolderName) {

		List<String[]> allFileNames = new ArrayList<String[]>();
		String[] header = { "fileName", "folderName", "isFolder" };
		allFileNames.add(header);
		String folderName = ResourceManager.resourceFolder;
		if (rootFolderName != null && rootFolderName.length() > 0) {
			String thisFolderName = rootFolderName.replaceAll(".", "/");
			folderName += thisFolderName;
			String[] thisRow = { ".", "", "1" };
			allFileNames.add(thisRow);
		}
		File file = new File(folderName);
		ResourceManager.addAllFiles(file, "", allFileNames);
		return allFileNames.toArray(new String[0][]);
	}

	/**
	 * recursively add all files and folder under this folder
	 * 
	 * @param folder
	 * @param prefix
	 * @param fileNames
	 */
	private static void addAllFiles(File folder, String parentfolderName,
			List<String[]> fileNames) {

		File[] filesInThisFolder = folder.listFiles();
		if (filesInThisFolder == null) {
			return;
		}

		for (File f : filesInThisFolder) {
			if (f.isHidden()) {
				continue;
			}
			String name = f.getName();
			if (name.equalsIgnoreCase("SVN") || name.equalsIgnoreCase("CVS")
					|| name.equalsIgnoreCase("VSS") || name.startsWith(".")) {
				continue;
			}
			boolean thisIsAFolder = f.isDirectory();
			String[] thisFileDetail = { name, parentfolderName,
					thisIsAFolder ? "1" : "0" };
			fileNames.add(thisFileDetail);

			if (thisIsAFolder) {
				ResourceManager.addAllFiles(f, parentfolderName + '.' + name,
						fileNames);
			}
		}
	}

	/**
	 * 
	 * @param startingFolder
	 * @param extension
	 *            like .xml or .
	 * @return list of file names
	 */
	public static String[] getResourceList(String startingFolder,
			String extension) {
		Set<String> names = new HashSet<String>();
		String folderName = ResourceManager.resourceFolder;
		if (startingFolder != null && startingFolder.length() > 0) {
			folderName += startingFolder;
		}
		File file = new File(folderName);
		ResourceManager.addResourceList(file, "", names, extension);
		String[] arr = names.toArray(new String[0]);
		Arrays.sort(arr);
		return arr;
	}

	/**
	 * map of resources
	 * 
	 * @param fileName
	 * @param folderName
	 * @param extension
	 * @return map of resources indexed by name
	 */
	public static Map<String, Object> loadFromFileOrFolder(String fileName,
			String folderName, String extension) {
		Map<String, Object> list = new HashMap<String, Object>();
		File file = new File(ResourceManager.resourceFolder
				+ fileName.replace('.', '/') + extension);
		if (file.exists() && file.isFile()) {
			list.put("", ObjectManager.fromXml(file));
		}
		File folder = new File(ResourceManager.resourceFolder + folderName);
		if (folder.exists() == false) {
			Spit.out(folderName + " is not found as a resource folder");
			return list;
		}

		Set<String> names = new HashSet<String>();
		String filePrefix = ResourceManager.resourceFolder + folderName + '/';
		ResourceManager.addResourceList(folder, "", names, extension);
		for (String fn : names) {
			String fullName = filePrefix + fn.replace('.', '/') + ".xml";
			Spit.out("Going to read " + fullName);
			try {
				File f = ResourceManager.getFile(fullName);
				list.put(fn, ObjectManager.fromXml(f));
			} catch (Exception e) {
				Spit.out("Error while parsing. " + e.getMessage()
						+ "\n dictionary skipped.");
			}
		}
		return list;
	}

	/**
	 * 
	 * @param folder
	 * @param prefix
	 * @param names
	 */
	private static void addResourceList(File folder, String prefix,
			Set<String> names, String extension) {
		File[] files = folder.listFiles();
		if (files == null) {
			return;
		}

		for (File f : files) {
			if (f.isHidden()) {
				continue;
			}
			String name = f.getName();
			if (f.isDirectory()) {
				if (name.equalsIgnoreCase("SVN")
						|| name.equalsIgnoreCase("CVS")
						|| name.equalsIgnoreCase("VSS")) {
					continue;
				}
				ResourceManager.addResourceList(f, prefix + f.getName() + '.',
						names, extension);
			} else {
				int n = name.lastIndexOf(extension);
				if (n > 0) {
					names.add(prefix + name.substring(0, n));
				}
			}
		}

	}

	/**
	 * get an array of folder names that contain resource components (files
	 * with.xml files) inside them. Include sub folders as well
	 * 
	 * @param resourceType
	 * @return resource folders
	 */
	//
	//
	public static String[] getResourceFolders(String resourceType) {
		Set<String> folderNames = new HashSet<String>();
		String folderName = ResourceManager.resourceFolder + resourceType;
		File folder = new File(folderName);
		if (folder.exists() == false || folder.isDirectory() == false) {
			return new String[0];
		}

		ResourceManager.addResourceFolder(folder, "", folderNames, true);
		String[] arr = new String[folderNames.size()];
		arr = folderNames.toArray(arr);
		Arrays.sort(arr);
		return arr;
	}

	// add the folder to the list if it has any file.xml in it or in any of its
	// sub-folder.
	private static boolean addResourceFolder(File folder, String prefix,
			Set<String> folderNames, boolean isRoot) {
		String folderName = folder.getName();
		String lowerFolder = folderName.toLowerCase();
		if (lowerFolder.equals("cvs") || folderName.equals(".svn")
				|| folderName.equals("vss")) {
			return false;
		}

		boolean toBeAdded = false;
		String newPrefix = isRoot ? "" : prefix + folderName + '.';

		for (File f : folder.listFiles()) {
			if (f.isDirectory()) {
				if (ResourceManager.addResourceFolder(f, newPrefix,
						folderNames, false)) {
					toBeAdded = true;
				}
			}
			String name = f.getName();
			int n = name.lastIndexOf(".xml");
			if (n > 0) {
				// This folder needs to be added because there is at least one
				toBeAdded = true;
			}
		}

		if (!toBeAdded || isRoot) {
			return false;
		}

		folderNames.add(prefix + folderName);
		return true;
	}

	/***
	 * create folder, and any parent folders if required
	 * 
	 * @param path
	 *            folder name to be created.
	 */
	public static void createFolder(String path) {
		File folder = new File(path);
		if (!folder.exists()) {
			Spit.out("folder " + path + " created");
			folder.mkdirs();
		}
	}

	/**
	 * reset all loaded resources
	 */
	public static void resetApplication() {
		loadAllResources(true);
	}

	/**
	 * reload all resources, but retain existing messages, data types,
	 * dictionary and service entries
	 */
	public static void reloadApplication() {
		loadAllResources(false);
	}

	/**
	 * load all resources.
	 * 
	 * @param toReset
	 */
	private static void loadAllResources(boolean toReset) {

		/*
		 * resources that are cached need to be loaded
		 */
		AP.load();
		Messages.reload(toReset);
		DataTypes.reload(toReset);
		DataDictionary.reload(toReset);
		ServiceList.reload(toReset);

		/*
		 * others have to be flushed, so that they get reloaded as and when
		 * required
		 */
		Services.flush();
		Sqls.flush();
		Tables.flush();
	}

	/***
	 * retain existing resources that are loaded, and load new resource folder.
	 * Used by IDE
	 * 
	 * @param absoluteFolderName
	 * @param dc
	 *            DataColleciton into which messages are to be added to,
	 *            optional
	 * @return 1 if successful, 0 otherwise (so that it is like a task)
	 */
	static int chooseProject(String absoluteFolderName, DataCollection dc) {
		String folderName = absoluteFolderName;
		if (folderName.endsWith("/") == false
				&& folderName.endsWith("\\") == false) {
			folderName += '/';
		}
		// does the folder exist?
		File folder = new File(folderName);
		String msg = null;
		if (!folder.exists()) {
			msg = folderName
					+ " is not a valid folder name. Can not be set as resource fodler";
		} else if (!folder.isDirectory()) {
			msg = folderName
					+ " is not a folder. Can not be set as resource fodler";
		} else {
			File file = new File(folderName + "applicationParameters.xml");
			if (file.exists() == false) {
				msg = folderName
						+ " does not contain applicationParameters.xml. Please check.";
			} else {
				ResourceManager.resourceFolder = folderName;
				ResourceManager.reloadApplication();
				return 1;
			}
		}

		// we got into trouble
		if (dc == null) {
			Spit.out(msg);
		} else {
			dc.addError(msg);
		}
		return 0;
	}

	/**
	 * load all resources at the beginning of the project
	 * 
	 * @param resFolder
	 *            from where application resources are to be loaded
	 * @param internalFolder
	 *            from where internal resources are loaded
	 * @return true if resources were loaded. false otherwise
	 */

	public static boolean loadAllResources(String resFolder,
			String internalFolder) {

		Spit.out("loading all resources using resource folder = " + resFolder
				+ " and internal folder = " + internalFolder);
		try {
			/**
			 * first load internal resources
			 */
			boolean normalResourcesToBeFlushed = true;
			if (internalFolder != null) {
				ResourceManager.resourceFolder = sanitizedFolder(internalFolder);
				loadResources(true);
				normalResourcesToBeFlushed = false;
			}

			/**
			 * load application resources, if required
			 */
			if (resFolder != null) {
				ResourceManager.resourceFolder = sanitizedFolder(resFolder);
				loadResources(normalResourcesToBeFlushed);
			}

			flushCachedResources();
			return true;
		} catch (Exception e) {
			Spit.out(e);
			return false;
		}
	}

	/**
	 * append '/' if required
	 * 
	 * @param folderName
	 * @return
	 */
	private static String sanitizedFolder(String folderName) {
		if (folderName.endsWith("/") || folderName.endsWith("\\")) {
			return folderName;
		}

		return folderName + '/';
	}

	private static void flushCachedResources() {
		Services.flush();
		Tables.flush();
		Sqls.flush();
	}

	private static void loadResources(boolean flushBeforeLoading) {
		AP.load();
		DataTypes.reload(flushBeforeLoading);
		Messages.reload(flushBeforeLoading);
		DataDictionary.reload(flushBeforeLoading);
		ServiceList.reload(flushBeforeLoading);
	}

	/***
	 * use this method to use an applicationParameters file other than the one
	 * in resource folder. Like in pageGeneration project in .net. For some
	 * version issue, generation application is run in a folder other than the
	 * one used for the actual application.
	 * 
	 * @param filePathRelativeToRoot
	 */
	@Deprecated
	public static void setApplicationParametersFileName(
			String filePathRelativeToRoot) {
		ResourceManager.appFileName = filePathRelativeToRoot;
		if (ResourceManager.appFileName.indexOf(".") == -1) {
			ResourceManager.appFileName += ".xml";
		}
		// whenever it is set, we have to read it
		ResourceManager.loadAppParameters();
	}

	@Deprecated
	private static void loadAppParameters() {
		String fileName = null;
		if (ResourceManager.appFileName == null) {
			fileName = ResourceManager.resourceFolder + AP.parametersFileName;
		} else {
			fileName = ResourceManager.rootFolder + ResourceManager.appFileName;
		}
		try {
			File file = ResourceManager.getFile(fileName);
			ApplicationParameters ap = (ApplicationParameters) ObjectManager
					.fromXml(file, ApplicationParameters.class);
			AP.setInstance(ap);
		} catch (Exception e) {
			String msg = "Unable to read applicaiton parameters file. Applicaiton will not work. Error : "
					+ e.getMessage();
			Spit.out(msg);
		}

	}

	/**
	 * 
	 * @param args
	 */
	public static void main2(String[] args) {
		ResourceManager
				.loadAllResources(
						"D:/exilityTurtle/exilityTurtleClient/WebContent/WEB-INF/resource/",
						"D:/exilityTurtle/exilityTurtleClient/WebContent/WEB-INF/exilityResource/");
		ServiceData inData = new ServiceData();
		ServiceData outData = new ServiceData();
		inData.addValue("userId", "420");
		inData.addValue("companyId", "134");
		inData.addValue("personCode", "person");
		inData.addValue("tableName", "tableName");
		inData.addValue("tableId", "2134");
		inData.addValue("actionStatus", "aciton");
		inData.addValue("remarks", "remarks to be updated");
		String serviceName = "etc.saveActionLog";
		String userId = "420";
		ServiceInterface service = Services.getService(serviceName, userId);
		service.serve(inData, outData);

		Spit.out(outData.getErrorStatus() + " is the error ststus of outdata ");
	}

	/**
	 * for local testing only
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		final String root = "D:/exilityTurtle/exilityTurtleClient/WebContent/";
		final String res = "D:/exilityTurtle/exilityTurtleClient/WebContent/WEB-INF/resource/";
		final String internal = "D:/exilityTurtle/exilityTurtleClient/WebContent/WEB-INF/exilityResource/";
		setRootFolder(root);
		loadAllResources(res, internal);
		// Page page = Pages.getPage("eta", "assetMasterSearch");

		// List<String> errorMessages = new ArrayList<String>();
		// page.generateAndSavePage(false, null, errorMessages);
		Spit.out(" is the name of the record");
	}

}
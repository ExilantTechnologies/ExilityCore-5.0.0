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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/***
 * All file accesses are routed thru this class to take care of file-systems
 * that depend on deployment scenarios
 * 
 * @author Exilant Technologies
 * 
 */
public abstract class FileUtility {
	/***
	 * file path that is to be prefixed to all file requests. TODO - where and
	 * how do we set this? We are assuming a single web-container deployment,
	 * and taking care of this in start-up servlet
	 */
	private static String basePath = "";

	/***
	 * different types of files that are handled by Exility. We will have to add
	 * as we handle more types
	 */
	public static final int FILE_TYPE_RESOURCE = 0;
	/**
	 * temp file
	 */
	public static final int FILE_TYPE_TEMP = 1;
	/**
	 * test files
	 */
	public static final int FILE_TYPE_TEST = 2;

	/***
	 * folder name after the base path for different type of files is hard coded
	 * here. These folder names are to be incorporated into set-up scripts
	 * Historically, we allowed a project to specify resource folder name.
	 * Refeer to ResourceManager.java. We have a method to change only that
	 * folder. This feature will be used after we re-factor resource manager to
	 * use FileUtility
	 */
	private static String[] filePaths = { "resource", "temp", "test" };

	/***
	 * Facility for teh resource folder be set based on deployment-time
	 * parameter
	 * 
	 * @param path
	 */
	public static void resetResourcePath(String path) {
		FileUtility.filePaths[FileUtility.FILE_TYPE_RESOURCE] = path;
	}

	/***
	 * Base path MUST be set at the time of bringing-up the application. For
	 * example in a startup servlet for web deployment.
	 * 
	 * @param path
	 */
	public static void setBasePath(String path) {
		try {
			FileUtility.basePath = path;
			if (path.endsWith(File.separator) == false) {
				FileUtility.basePath += File.separator;
			}
			// clean-up temp, and create other folders if not present.
			for (String folderName : FileUtility.filePaths) {
				File folder = new File(FileUtility.basePath + folderName);
				if (folder.exists() == false) {
					folder.mkdirs();
				} else if (folder.isFile()) {
					folder.renameTo(new File(folder.getPath() + "Backup"));
					folder = new File(FileUtility.basePath + folderName);
					folder.mkdir();
					Spit.out(folder.getPath()
							+ " Should be a folder, but a file exists with this name. File is renamed and a folder is created.");
				}
			}

			// do we clean-up temp files?
			// File tempFolder = new File( basePath +
			// filePaths[FileUtility.FILE_TYPE_TEMP]);
			// for(File child : tempFolder.listFiles())
			// {
			// child.delete();
			// }
			Spit.out("Base path set to " + FileUtility.basePath
					+ " for all files on this application.");
		} catch (Exception e) {
			Spit.out("Error while setting file system base path to " + path);
			Spit.out(e);
		}
	}

	/***
	 * write text into a file. Use this if the text is reasonably small, and you
	 * do not need buffred write etc..
	 * 
	 * @param fileType
	 *            choose one of the declared type.
	 * @param filePath
	 *            file name, including any subfolder you may have
	 * @param textToWrite
	 *            content to be written to the file
	 * @return true if we actually wrote it to the file, false in case of any
	 *         error
	 */
	public static boolean writeText(int fileType, String filePath,
			String textToWrite) {
		File file = FileUtility.getFile(fileType, filePath);
		if (file == null) {
			Spit.out("FileUtility.writeText() called with fileType = "
					+ fileType + " and filePath = " + filePath
					+ ". Unable to create file.");
			return false;
		}

		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "US-ASCII"));
			writer.append(textToWrite);
			writer.flush();
			writer.close();
			return true;
		} catch (Exception e) {
			Spit.out(e);
			return false;
		}
	}

	/***
	 * Read contents of file as text
	 * 
	 * @param fileType
	 *            choose one of the designated file type
	 *            FileUtility.FILE_TYPE_TEMP etc.. File location would depend on
	 *            this
	 * @param filePath
	 *            file name, including any subfolder. e.g.
	 *            inventory/orderDetail.txt
	 * @param deleteAfterReading
	 * @return file content TODO: what about character encoding???? That is the
	 *         reason we have separate methods for text and byte
	 */
	public static String readText(int fileType, String filePath,
			boolean deleteAfterReading) {
		File file = FileUtility.getFile(fileType, filePath);
		if (file == null || file.exists() == false) {
			Spit.out("FileUtility.readText() called with fileType = "
					+ fileType + " and filePath = " + filePath
					+ ". File does not exist.");
			return "";
		}

		StringBuffer buffer = new StringBuffer();
		try {
			InputStreamReader reader = new InputStreamReader(
					new FileInputStream(file), CommonFieldNames.CHAR_ENCODING);
			int c;
			while ((c = reader.read()) != -1) {
				buffer.append(String.valueOf((char) c));
			}
			reader.close();

			if (deleteAfterReading) {
				file.delete();
			}
		} catch (Exception e) {
			Spit.out(e);
		}
		return buffer.toString();
	}

	/***
	 * does the file exist?
	 * 
	 * @param fileType
	 * @param filePath
	 * @return whether the file exist
	 */
	public static boolean exists(int fileType, String filePath) {
		File file = FileUtility.getFile(fileType, filePath);
		return (file != null && file.exists());
	}

	/***
	 * validate file path and return a file as per our naming conventions
	 * 
	 * @param fileType
	 * @param path
	 * @return
	 */
	private static File getFile(int fileType, String path) {
		if (fileType >= FileUtility.filePaths.length) {
			return null;
		}
		File storageDir = new File(FileUtility.basePath,
				FileUtility.filePaths[fileType]);
		File theFile = new File(storageDir, path);
		storageDir = null;// remove
		return theFile;
	}
}

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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * The purpose of this class is to provide handle to excel file as stream. This
 * utility uses apache POI library to identify and create the excel handler.
 * This also parse multipart request and put data into ServiceData object
 * including files and fields.
 * 
 * 
 * Additionally, this class can also be used to get a handle to file other than
 * excel as well by invoking getStream() method.
 * 
 * The following methods can be invoked to get excel file handle by passing
 * parameters as httpServletRequest, InputStream, String (File path with file
 * name).
 * 
 * getXLSHandler(arg0) returns excel file handler based on argument supplied.
 * This is overloaded method
 * 
 * @author Anant on 13 july 2012.
 */
public class XLSHandler {

	/**
	 * 
	 * @param file
	 *            name of the file with path available onto the disk
	 * @return instance of ss.usermodel.Workbook which is either HSSFWorkbook or
	 *         XSSFWorkbook instance
	 */
	public static Workbook getXLSHandler(String file) {
		Workbook wb = null;
		try {
			InputStream is = XLSHandler.getStream(file);

			try {
				wb = WorkbookFactory.create(is);
			} catch (Exception e) {
				Spit.out(e);
			}

			is.close();
		} catch (Exception e) {
			Spit.out(e);
		}
		return wb;
	}

	/**
	 * 
	 * @param inputStream
	 * @return instance of ss.usermodel.Workbook which is either HSSFWorkbook or
	 *         XSSFWorkbook instance
	 * @throws IOException
	 * @throws InvalidFormatException
	 */

	public static Workbook getXLSHandler(InputStream inputStream)
			throws IOException, InvalidFormatException {
		if (inputStream == null) {
			Spit.out("Parameter(InputStream inputStream) being supplied in getXLSXHandler() cant be null");
			return null;
		}

		Workbook wb;
		try {
			wb = WorkbookFactory.create(inputStream);
		} catch (InvalidFormatException e) {
			Spit.out(e);
			return null;
		} catch (IOException e) {
			Spit.out(e);
			return null;
		}
		return wb;

	}

	/**
	 * This method returns input stream out of file field in HTTP request
	 * 
	 * @param req
	 * 
	 * @return InputStream
	 * @throws IOException
	 * @throws FileUploadException
	 */

	public static InputStream getStream(HttpServletRequest req)
			throws IOException, FileUploadException {
		return XLSHandler.getFileItem(req).getInputStream();
	}

	/**
	 * This method returns FileItem handler out of file field in HTTP request
	 * 
	 * @param req
	 * @return FileItem
	 * @throws IOException
	 * @throws FileUploadException
	 */
	@SuppressWarnings("unchecked")
	public static FileItem getFileItem(HttpServletRequest req)
			throws IOException, FileUploadException {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		/*
		 * we can increase the in memory size to hold the file data but its
		 * inefficient so ignoring to factory.setSizeThreshold(20*1024);
		 */
		ServletFileUpload sFileUpload = new ServletFileUpload(factory);
		List<FileItem> items = sFileUpload.parseRequest(req);
		for (FileItem item : items) {
			if (!item.isFormField()) {
				return item;
			}
		}

		throw new FileUploadException("File field not found");

	}

	/**
	 * @param req
	 * @param container
	 * @return whether we are able to parse it
	 */
	@SuppressWarnings("unchecked")
	public static boolean parseMultiPartData(HttpServletRequest req,
			ServiceData container) {
		/**
		 * I didnt check here for multipart request ?. caller should check.
		 */
		DiskFileItemFactory factory = new DiskFileItemFactory();
		/*
		 * we can increase the in memory size to hold the file data but its
		 * inefficient so ignoring to factory.setSizeThreshold(20*1024);
		 */
		ServletFileUpload sFileUpload = new ServletFileUpload(factory);
		List<FileItem> items = null;

		try {
			items = sFileUpload.parseRequest(req);
		} catch (FileUploadException e) {
			container.addMessage("fileUploadFailed", e.getMessage());
			Spit.out(e);
			return false;
		}

		/*
		 * If user is asked for multiple file upload with filesPathGridName then
		 * create a grid with below columns and send to the client/DC
		 */
		String filesPathGridName = req.getHeader("filesPathGridName");
		OutputColumn[] columns = {
				new OutputColumn("fileName", DataValueType.TEXT, "fileName"),
				new OutputColumn("fileSize", DataValueType.INTEGRAL, "fileSize"),
				new OutputColumn("filePath", DataValueType.TEXT, "filePath") };
		Value[] columnValues = null;

		FileItem f = null;
		String allowMultiple = req.getHeader("allowMultiple");

		List<Value[]> rows = new ArrayList<Value[]>();

		String fileNameWithPath = "";
		String rootPath = getResourcePath();

		String fileName = null;

		int fileCount = 0;

		for (FileItem item : items) {
			if (item.isFormField()) {
				String name = item.getFieldName();
				container.addValue(name, item.getString());
			} else {
				f = item;
				if (allowMultiple != null) {
					fileCount++;
					fileName = item.getName();
					fileNameWithPath = rootPath + getUniqueName(fileName);

					String path = XLSHandler.write(item, fileNameWithPath,
							container);
					if (path == null) {
						return false;
					}

					if (filesPathGridName != null
							&& filesPathGridName.length() > 0) {
						columnValues = new Value[3];
						columnValues[0] = Value.newValue(fileName);
						columnValues[1] = Value.newValue(f.getSize());
						columnValues[2] = Value.newValue(fileNameWithPath);
						rows.add(columnValues);
						fileNameWithPath = "";
						continue;
					}

					container.addValue("file" + fileCount + "_ExilityPath",
							fileNameWithPath);
					fileNameWithPath = "";
				}

			}
		}

		if (f != null && allowMultiple == null) {
			fileNameWithPath = rootPath + getUniqueName(f.getName());
			String path = XLSHandler.write(f, fileNameWithPath, container);
			if (path == null) {
				return false;
			}
			container.addValue(container.getValue("fileFieldName"), path);
			return true;
		}

		/**
		 * If user asked for multiple file upload and has supplied gridName for
		 * holding the file path then create a grid
		 */

		if (rows.size() > 0) {
			Grid aGrid = new Grid(filesPathGridName);
			aGrid.setValues(columns, rows, null);

			container.addGrid(filesPathGridName, aGrid.getRawData());
		}
		return true;
	}

	/**
	 * This method returns input stream to given file which resides on disk
	 * 
	 * @param file
	 * @return InputStream
	 * @throws IOException
	 */
	public static InputStream getStream(String file) throws IOException {
		if (file == null) {
			throw new IllegalArgumentException(
					"File name being supplied cant be null or blank");
		}

		return new FileInputStream(file);
	}

	/**
	 * 
	 * @param fileItem
	 *            instance of FileItem POI apache.
	 * @param fileNameWithPath
	 *            instance of String.
	 * @param data
	 *            instance of ServiceData
	 * @return filePath if success else null
	 */
	public static String write(FileItem fileItem, String fileNameWithPath,
			ServiceData data) {
		try {
			File file = new File(fileNameWithPath);
			fileItem.write(file);
		} catch (IOException ioe) {
			data.addError("file [" + fileNameWithPath + "]  upload failed");
			Spit.out(ioe);
			return null;
		}

		catch (Exception e) {
			data.addError("file [" + fileNameWithPath + "]  upload failed");
			Spit.out(e);
			return null;
		}

		Spit.out("file [" + fileNameWithPath + "] uploaded successfully");
		return fileNameWithPath;
	}

	/**
	 * purpose of this method to create a unique name for given file name
	 * 
	 * @param fileName
	 *            Name of the file
	 * @return unique name for given file name
	 */
	public static String getUniqueName(String fileName) {
		int extIdx = fileName.lastIndexOf('.');
		String timeStamp = "_" + Calendar.getInstance().getTimeInMillis();
		// if extension is exist then retain it
		if (extIdx != -1) {
			String fileExt = fileName.substring(extIdx);
			String fname = fileName.substring(0, extIdx);
			return (fname + timeStamp + fileExt);
		}

		return (fileName + timeStamp);
	}

	/**
	 * purpose of this method to return resource folder/path including ap
	 * filePath
	 * 
	 * @return resource path.
	 */
	public static String getResourcePath() {
		String apPath = AP.filePath;
		String rootPath = ResourceManager.getRootFolderName();

		if (apPath == null || apPath.length() < 1) {
			return rootPath;
		}

		if (apPath.lastIndexOf('/') != -1) {
			return (rootPath + apPath);
		}

		return (rootPath + apPath + "/");
	}
}

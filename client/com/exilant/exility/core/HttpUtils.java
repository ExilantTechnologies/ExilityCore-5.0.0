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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/***
 * work-in-progress to have http related utilities. not released yet.
 * 
 */
public class HttpUtils {
	private static HttpUtils instance = null;

	/**
	 * this is a utility class, but we have designed this as instance-based
	 * methods to keep options open for future. As of now, we are using a
	 * singleton, but you should not make that assumption in your algorithm.
	 * 
	 * @return instance of of utility
	 */
	public static HttpUtils getInstance() {
		if (HttpUtils.instance == null) {
			HttpUtils.instance = new HttpUtils();
		}
		return HttpUtils.instance;
	}

	/***
	 * Simplest way of handling files that are uploaded from form. Just put them
	 * as name-value pair into service data. This is useful ONLY if the files
	 * are reasonably small.
	 * 
	 * @param req
	 * @param data
	 *            data in which
	 */
	public void simpleExtract(HttpServletRequest req, ServiceData data) {
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		try {
			/**
			 * ServerFileUpload still deals with raw types. we have to have
			 * workaround that
			 */
			List<?> list = upload.parseRequest(req);
			if (list != null) {
				for (Object item : list) {
					if (item instanceof FileItem) {
						FileItem fileItem = (FileItem) item;
						data.addValue(fileItem.getFieldName(),
								fileItem.getString());
					} else {
						Spit.out("Servlet Upload retruned an item that is not a FileItem. Ignorinig that");
					}
				}
			}
		} catch (FileUploadException e) {
			Spit.out(e);
			data.addError("Error while parsing form data. " + e.getMessage());
		}
	}
}

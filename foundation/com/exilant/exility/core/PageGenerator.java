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
 * 
 * generate html/js for this page. deprecated. use appropriate method in Page
 * itself.
 */
@Deprecated
public class PageGenerator {

	/**
	 * 
	 * @param path
	 * @return page object for this page
	 */
	public static Page getPageObject(String path) {
		// Class myType = Type.getType("Exilant.Exility.Core.Page", false,
		// true);
		try {
			Page page = (Page) ResourceManager.loadResourceFromFile(path,
					Page.class);
			return page;
		} catch (Exception e) {
			try {
				Pages.getPanel(path);
				Spit.out(path
						+ " is an include panel, but you are trying it as a page.");
			} catch (Exception e1) {
				Spit.out("Error while parsing xml file " + path + ": "
						+ e.getMessage());
				return null;
			}
			return null;
		}
	}

	/**
	 * get html file name for this page
	 * 
	 * @param page
	 * @return file name with which html is to be saved
	 */
	public static String getHtmlFileName(Page page) {
		String htmlPath;
		if (page.module == null || page.module.length() == 0) {
			htmlPath = page.name + ".htm";
		} else {
			htmlPath = page.module + "/" + page.name + ".htm";
		}
		return htmlPath;
	}

	/**
	 * get file name to which .js file is to be saved
	 * 
	 * @param page
	 * @return .js file name
	 */

	public static String getJavaScriptFileName(Page page) {
		String jsPath;
		if (page.module == null || page.module.length() == 0) {
			jsPath = page.name + ".metadata.js";
		} else {
			jsPath = page.module + "/" + page.name + ".metadata.js";
		}
		return jsPath;
	}
}

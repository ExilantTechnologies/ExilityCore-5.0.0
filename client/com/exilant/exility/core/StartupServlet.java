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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/***
 * called at the application startup event by web server
 * 
 * @deprecated use Startup instead
 */
@Deprecated
public class StartupServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void init() throws ServletException {
		final String FOLDER_SEPARATOR = "/";
		ServletContext context = this.getServletContext();

		String rootFolder = context.getRealPath(FOLDER_SEPARATOR);
		if (rootFolder.endsWith(FOLDER_SEPARATOR) == false) {
			rootFolder += FOLDER_SEPARATOR;
		}

		ResourceManager.setRootFolder(rootFolder);
		Spit.out("Root folder set to :" + rootFolder);

		/**
		 * are we to use an application parameters file other than the one in
		 * resource folder?
		 */
		String appFileName = context
				.getInitParameter("parameters-file-path-relative-to-root");
		if (appFileName != null) {
			ResourceManager.setApplicationParametersFileName(appFileName);
		}

		// has application resource folder been specified?
		String resourceFolder = context.getInitParameter("resource-folder");
		if (resourceFolder == null || resourceFolder.length() == 0) {
			Spit.out("ERROR: a context-param with name resource-folder must be set to the absolute path of your resourcefolder. Edit web.xml file under WEB-INF folder for this.");
			Spit.out("Exility Engine can not be started.");
			return;
		}
		ResourceManager.setResourceFolder(rootFolder + resourceFolder);
		Spit.out("resource folder set to :" + rootFolder);
	}
}

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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/***
 * This class replaces StartupServlet in the previous version called at the
 * application startup event by web server. Objective is to process all
 * instructions from web.xml thru this class.
 */
public class Startup extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/*
	 * parameters from web.xml
	 */
	/**
	 * resource folder relative to web root
	 */
	private static final String RESOURCE_PARAM = "resource-folder";

	/**
	 * exility resource folder relative to root
	 */
	private static final String EXILITY_RESOURCE_PARAM = "exility-resource-folder";

	/**
	 * whether this is THE IDE project. Used internally by IDE. Should be
	 * false("0") for all projects.
	 */
	private static final String INTERNAL_ONLY = "internal-only";
	/**
	 * parameter in web.xml that decides whether a client session can have
	 * multiple logins cookie or a field
	 */
	private static final String MULTI_LOGIN = "allow-multiple-logins-per-session";

	/**
	 * upload/download other file handling routines would expect storage area.
	 * In distributed environment, we need to know where to save the files
	 */
	private static final String FILE_STORAGE_ROOT = "file-storage-root";

	/**
	 * fully qualified name of cleanser to be used. cleanser is called before
	 * and after calling a service
	 */
	private static final String CLEANSER_NAME = "cleanser-class-name";

	/**
	 * single sign on class. If specified, this class is used whenever we
	 * receive a request from a client that has not authenticated
	 */
	private static final String SSO_CLASS_NAME = "sso-class-name";

	private static final String DEFAULT_RESOURCE = "WEB-INF/resource";
	private static final String DEFAULT_EXILITYL_RESOURCE = "WEB-INF/exilityResource";
	private static final String USE_NEW_AGENT = "use-new-agent";

	@Override
	public void init() throws ServletException {
		String rootFolder = this.setRootFolder();
		this.loadResources(rootFolder);
		this.setCleanser();
		this.setFileStorage();
		this.setSso();
		this.setOptions();
	}

	/**
	 * set root folder for all resources
	 * 
	 * @return
	 */
	private String setRootFolder() {
		final String FOLDER_SEPARATOR = "/";
		ServletContext context = this.getServletContext();

		/**
		 * deployment is assumed to be in a single layer. Hence resourceManager
		 * is configured here
		 */
		String rootFolder = context.getRealPath(FOLDER_SEPARATOR);
		if (rootFolder.endsWith(FOLDER_SEPARATOR) == false
				&& rootFolder.endsWith("\\") == false) {
			rootFolder += FOLDER_SEPARATOR;
		}

		ResourceManager.setRootFolder(rootFolder);
		Spit.out("Root folder set to :" + rootFolder);

		return rootFolder;
	}

	/**
	 * load exility resources and project resources
	 * 
	 * @param rootFolder
	 */
	private void loadResources(String rootFolder) {
		ServletContext context = this.getServletContext();
		String internalFolder = context
				.getInitParameter(EXILITY_RESOURCE_PARAM);

		if (internalFolder == null) {
			internalFolder = DEFAULT_EXILITYL_RESOURCE;
		}

		internalFolder = rootFolder + internalFolder;
		String resourceFolder = null;
		String internalOnly = context.getInitParameter(INTERNAL_ONLY);

		/**
		 * load project resource, unless this is a single resource scenario :
		 * either exilityIDE, or projects that are using old way of using IDE
		 */
		if (internalOnly == null || internalOnly.equals("1") == false) {

			resourceFolder = context.getInitParameter(RESOURCE_PARAM);
			if (resourceFolder == null) {
				resourceFolder = DEFAULT_RESOURCE;
			}
			resourceFolder = rootFolder + resourceFolder;
		}

		Spit.out("Internal resource folder is " + internalFolder
				+ " and resource folder = " + resourceFolder
				+ " while internal-only = " + internalOnly);

		ResourceManager.loadAllResources(resourceFolder, internalFolder);

	}

	/**
	 * set file storage folder
	 */
	private void setFileStorage() {
		String fileStorageRoot = this.getServletContext().getInitParameter(
				FILE_STORAGE_ROOT);
		if (fileStorageRoot == null) {
			File file = new File(ResourceManager.getResourceFolder());
			if (file.exists()) {
				fileStorageRoot = file.getParent();
			}
		}
		if (fileStorageRoot != null) {
			FileUtility.setBasePath(fileStorageRoot);
		}
	}

	/**
	 * handle cleanser directive
	 */
	private void setCleanser() {
		String cleanserName = this.getServletContext().getInitParameter(
				CLEANSER_NAME);
		System.out.println("cleanser is " + cleanserName);
		if (cleanserName != null) {
			try {
				Class<?> klass = Class.forName(cleanserName);
				ServiceCleanserInterface cleanser = (ServiceCleanserInterface) (klass
						.newInstance());
				HttpRequestHandler.setCleanser(cleanser);
			} catch (Exception e) {
				System.out.println("Error while setting up cleanser "
						+ cleanserName + " for this project. " + e.getMessage()
						+ ". Cleanser will not be working for this project");
			}
		}
	}

	/**
	 * handle sso directive.
	 */
	private void setSso() {
		String ssoName = this.getServletContext().getInitParameter(
				SSO_CLASS_NAME);
		Spit.out("sso is " + ssoName);

		if (ssoName != null) {
			try {
				Class<?> klass = Class.forName(ssoName);
				SingleSignOnInterface sso = (SingleSignOnInterface) (klass
						.newInstance());
				HttpRequestHandler.setSignoOnObject(sso);
				Spit.out("Successfully assigned sso");
			} catch (Exception e) {
				Spit.out("Error while setting up sso " + ssoName
						+ " for this project. " + e.getMessage()
						+ ". SSO will not be working for this project");
			}
		}
	}

	/**
	 * set options from web.xml
	 */
	private void setOptions() {
		/*
		 * process multi-login option handling directive
		 */
		String mulitLogin = this.getServletContext().getInitParameter(
				MULTI_LOGIN);
		if (mulitLogin != null && mulitLogin.equals("1")) {
			Spit.out("useCookies set to true");
			HttpRequestHandler.setMultipleLoginOption(true);
		}

		/*
		 * process suppressSqlLog option
		 */
		String isSet = this.getServletContext().getInitParameter(
				ExilityConstants.SUPPRESS_SQL_LOG);
		if (isSet != null && isSet.equals("1")) {
			Spit.out(ExilityConstants.SUPPRESS_SQL_LOG + " option set to true");
			HttpRequestHandler.setSuppressSqlLog(true);
			HtmlRequestHandler.setSuppressSqlLog(true);
		}
		/*
		 * temporary fix for transition into new agent suppressSqlLog option
		 */
		isSet = this.getServletContext().getInitParameter(USE_NEW_AGENT);
		if (isSet != null && isSet.equals("1")) {
			Spit.out(USE_NEW_AGENT + " option set to true");
			HttpRequestHandler.useNewServiceAgent = true;
			HtmlRequestHandler.useNewServiceAgent = true;
		}
	}
}

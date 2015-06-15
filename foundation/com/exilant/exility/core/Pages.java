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
import java.util.List;
import java.util.Map;

import com.exilant.exility.ide.FieldDetails;

/***
 * Class that manages instances of Page component. However, unlike other such
 * components, Pages are not cached for re-use, as we do not intend to use this
 * at run time. At design time, it is required that we use the latest version
 * always, rather than use cached ones.
 * 
 * @author Exilant Technologies
 * 
 */
public class Pages {

	/**
	 * all fields
	 */
	public static final int FIELD_FILTER_ALL = 0;

	/**
	 * fields that are sent to server on a form submit. Note that it is possible
	 * that a programmer could send any field thru script, that is normally not
	 * sent automatically by exility. We use the logic, any input field (that is
	 * not marked as doNotSendToServer), and output fields marked as
	 * toBeSentToServer
	 */
	public static final int FIELD_FILTER_SENT_TO_SERVER = 1;

	/**
	 * get page from its persistent state
	 * 
	 * @param folderName
	 * @param pageName
	 * @return page instance, null if it is not found or could not be parsed
	 */
	public static Page getPage(String folderName, String pageName) {
		String resourceName = pageName;
		if (folderName != null) {
			resourceName = folderName + '/' + pageName;
		}
		resourceName = "page." + resourceName;
		Object o = ResourceManager.loadResource(resourceName, Page.class);
		if (o == null) {
			Spit.out(pageName
					+ " Could not be loaded. Possible that that the XML is in error. Lok at log file.");
			return null;
		}
		if (o instanceof Page == false) {
			// it is likely to be include panel..
			Spit.out(pageName
					+ " is not a page. It is proably an  include panel...");
			return null;
		}
		Page page = (Page) o;
		if (pageName.equals(page.name) == false) {
			Spit.out("File " + pageName + ".xml has a page named " + page.name
					+ ". Page name MUST match name of the file.");
			return null;
		}
		return page;
	}

	/**
	 * save this instance into its persistent form
	 * 
	 * @param thisPage
	 */
	public static void savePage(Page thisPage) {
		String fileName = "page/"
				+ ((thisPage.module != null) ? (thisPage.module + '.') : "")
				+ thisPage.name;
		ResourceManager.saveResource(fileName, thisPage);
	}

	static AbstractPanel getPanel(String panelName) {
		Spit.out("Going to load panel with file name = " + "page." + panelName
				+ ".xml");

		Object o = ResourceManager.loadResource("page/" + panelName,
				AbstractPanel.class);
		if (o != null) {
			return (AbstractPanel) o;
		}
		Spit.out(panelName + " could not be loaded");
		return null;
	}

	/**
	 * 
	 * @param filterType
	 *            FIELD_FILTER_ALL, FIELD_FILTER_SENT_TO_SERVER are implemented
	 * @return list of fields with details for each page
	 */
	public static Map<String, List<FieldDetails>> getFields(int filterType) {
		String[] names = ResourceManager.getResourceList("page", ".xml");
		int nbrPagesScanned = 0;
		Map<String, List<FieldDetails>> fields = new HashMap<String, List<FieldDetails>>();
		for (String pageName : names) {
			Page page = Pages.getPage(null, pageName);
			if (page != null) {
				page.addAllFields(fields, filterType);
			}
		}
		Spit.out(nbrPagesScanned
				+ " pages scanned for filtering field information");
		/*
		 * consolidate data from different pages for each field and populate
		 * fields in the first occurrence
		 */
		for (String fieldName : fields.keySet()) {
			List<FieldDetails> details = fields.get(fieldName);
			int nbrPages = details.size();
			FieldDetails firstOne = details.get(0);
			firstOne.nbrUsageAcrossResources = nbrPages;
			if (nbrPages > 1) {
				for (int i = 1; i < nbrPages; i++) {
					FieldDetails thisOne = details.get(i);
					firstOne.commaSeparatedResourceNames += ',' + thisOne.resourceName;
				}
			}
		}
		return fields;
	}

}

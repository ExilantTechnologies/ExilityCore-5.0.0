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
package com.exilant.exility.ide;

import java.util.ArrayList;
import java.util.List;

import com.exilant.exility.core.CustomCodeInterface;
import com.exilant.exility.core.DataAccessType;
import com.exilant.exility.core.DataCollection;
import com.exilant.exility.core.DbHandle;
import com.exilant.exility.core.Page;
import com.exilant.exility.core.Pages;
import com.exilant.exility.core.ResourceManager;
import com.exilant.exility.core.Spit;
import com.exilant.exility.core.Util;

/**
 * service to generate page
 * 
 */
public class GeneratePage implements CustomCodeInterface {

	// expected fields
	private static final String FILE_NAME = "fileName";
	private static final String FOLDER_NAME = "folderName";
	private static final String LIVE_WITH_ERRORS = "liveWithErrors";
	private static final String LANGUAGE = "language";
	private static final String MESSAGES_GRID_NAME = "messages";
	private static final String MESSAGES_HEADER = "message";
	private static final String FILES_GRID_NAME = "fileNames";
	private static final String FILES_GRID_HEADER = "fileName";

	// output fields into dc
	private static final String NBR_GENERATED = "nbrGenerated";

	@Override
	public int execute(DataCollection dc, DbHandle dbHandle, String gridName,
			String[] parameters) {
		String fileName = dc.getTextValue(GeneratePage.FILE_NAME, "");
		String language = dc.getTextValue(GeneratePage.LANGUAGE, null);
		// client can use . if they have difficulty dealing with blank
		if (fileName.equals(".")) {
			fileName = "";
		}
		String folderName = dc.getTextValue(GeneratePage.FOLDER_NAME, null);
		boolean liveWithErrors = dc.getBooleanValue(
				GeneratePage.LIVE_WITH_ERRORS, false);
		List<String> generatedFiles = new ArrayList<String>();
		List<String> errorMessages = new ArrayList<String>();

		if (fileName.equals("")) {
			Spit.out("Going to generate pages for all files in folder "
					+ folderName);
			String[] fileNames = ResourceManager.getResourceList("page/"
					+ folderName.replace('.', '/'), ".xml");
			for (String fn : fileNames) {
				try {
					String generatedFile = this.generateOnePage(folderName, fn,
							liveWithErrors, language, errorMessages);
					if (generatedFile != null) {
						generatedFiles.add(generatedFile);
					}
				} catch (Exception e) {
					dc.addError(folderName + '/' + fn
							+ " could not be generated. error : "
							+ e.getMessage());
					Spit.out(e);
				}
			}
		} else {
			Spit.out("Going to generate page for file " + fileName);
			String generatedFile = this.generateOnePage(folderName, fileName,
					liveWithErrors, language, errorMessages);
			if (generatedFile != null) {
				generatedFiles.add(generatedFile);
			}
		}

		int nbr = generatedFiles.size();
		dc.addIntegralValue(NBR_GENERATED, nbr);
		String[] vals;
		if (nbr > 0) {
			vals = generatedFiles.toArray(new String[0]);
		} else {
			vals = new String[0];
		}
		String[][] grid = Util.namesToGrid(vals, FILES_GRID_HEADER);
		dc.addGrid(FILES_GRID_NAME, grid);

		if (errorMessages.size() > 0) {
			vals = errorMessages.toArray(new String[0]);
		} else {
			vals = new String[0];
		}
		grid = Util.namesToGrid(vals, MESSAGES_HEADER);
		dc.addGrid(MESSAGES_GRID_NAME, grid);

		return nbr;
	}

	/**
	 * generate a page
	 * 
	 * @param folderName
	 * @param fileName
	 * @param generateEvenOnError
	 * @param language
	 * @param errorMessages
	 * @return file name that is generated or null if no file is generated
	 */
	public String generateOnePage(String folderName, String fileName,
			boolean generateEvenOnError, String language,
			List<String> errorMessages) {
		Page page = Pages.getPage(folderName, fileName);
		if (page == null) {
			return null;
		}
		return page.generateAndSavePage(false, language, errorMessages);
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.NONE;
	}

	// Main file is required for the projects who do not use web based page
	// generator.
	/**
	 * generate a page
	 * 
	 * @param args
	 *            folder name, file name, fully qualified resource folder name,
	 */
	public static void main(String[] args) {
		// Pass the following arguments in the order given below:
		/*
		 * 1) Folder name 2) File name (page.xml's name) 3)Resource Folder fully
		 * qualified path: ex: Users/user1/Desktop/ .......... /WEB-INF/resource
		 */
		DataCollection dc = new DataCollection();
		ResourceManager.loadAllResources(args[2], null);
		// ResourceManager.setResourceFolder(args[2]);
		dc.addTextValue(GeneratePage.FOLDER_NAME, args[0]);
		dc.addTextValue(GeneratePage.FILE_NAME, args[1]);
		dc.addBooleanValue(GeneratePage.LIVE_WITH_ERRORS, true);
		GeneratePage gen = new GeneratePage();
		gen.execute(dc, null, null, null);
	}

}

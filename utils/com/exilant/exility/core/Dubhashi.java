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
import java.util.Map;

/***
 * Dubhashi is a Translator(Hindi). Oh Yes. I am aware that we are not supposed
 * to use local language. I could not resist just this one :-) the This is based
 * on the dictionary.xlx that is provided at the root of resource folder as well
 * as any additional dictionary provided at a page level
 * 
 */
public class Dubhashi {

	/**
	 * header for the first column in the translation XLS
	 */
	public static final String DEFAULT_LANGUAGE = "English";

	/**
	 * header for description column in translation XLS
	 */
	public static final String TRANSLATION_DESCRIPTION = "Description";
	/**
	 * folder name under resource where translation related resources are stored
	 */
	public static final String TRANSLATION_FOLDER = "i18n/";
	/**
	 * name of the translations file, relative to resource folder,
	 */
	public static final String TRANSLATION_FILE = "translations.xls";

	/**
	 * relative file name from resource folder
	 */
	public static final String TRANSLATIONS_FILE_PATH_FROM_RESOURCE = TRANSLATION_FOLDER
			+ TRANSLATION_FILE;
	/**
	 * holds translations that are defined at the root resource level
	 */
	private static Map<String, Map<String, String>> dictionaries = null;

	private final String language;
	private Map<String, String> customTranslations = null;

	/**
	 * load dictionary. Can be used for reloading as well..
	 * 
	 * @param fullyQualifiedName
	 *            file name that contains xls with one sheet of translations
	 * @param flushBeforeLoading
	 *            if you are reloading after some change. Flushed before
	 *            attempting to load from the file
	 */
	public static synchronized void load(String fullyQualifiedName,
			boolean flushBeforeLoading) {

		if (flushBeforeLoading) {
			dictionaries.clear();
		}

		String[][] sheet = getSheet(fullyQualifiedName);
		if (sheet == null || sheet.length < 2 || sheet[0].length < 2) {
			Spit.out(fullyQualifiedName
					+ " does not contain translations. No translations loaded");
			return;
		}
		/**
		 * first row is the header. second column onwards contain language name.
		 */
		String[] languages = sheet[0];

		/**
		 * create a dictionary for each language, and add it to dictionaries.
		 * First column is default..
		 */
		for (int col = 1; col < languages.length; col++) {
			String language = languages[col];
			if (language == null || language.length() == 0) {
				Spit.out("Column " + col + " does not have a language name");
				continue;
			}
			language = language.toLowerCase();
			if (language.equals(TRANSLATION_DESCRIPTION.toLowerCase())) {
				Spit.out("Column " + col + " has description of labels");
				continue;
			}
			Map<String, String> translations = dictionaries.get(language);
			if (translations == null) {
				translations = new HashMap<String, String>();
				dictionaries.put(language, translations);
			}
			Spit.out("Language " + language + " added to translations");
			addTranslations(sheet, language, translations);
		}
	}

	/**
	 * get a translator for this language
	 * 
	 * @param language
	 * @return Translator for this language
	 */
	public static Dubhashi getDubhashi(String language) {
		wakeUp();
		return new Dubhashi(language);
	}

	/**
	 * what are the languages that are defined in this project. This is nothing
	 * but the fields in the header row of the translations table
	 * 
	 * @return array of languages. Will definitely have one entry for default
	 *         language
	 */
	public static String[] getLangauges() {
		wakeUp();
		String[] languages = new String[dictionaries.size() + 1];
		languages[0] = DEFAULT_LANGUAGE;
		int idx = 1;
		for (String language : dictionaries.keySet()) {
			languages[idx] = language;
			idx++;
		}
		return languages;
	}

	private static void wakeUp() {
		if (dictionaries == null) {
			dictionaries = new HashMap<String, Map<String, String>>();
			load(ResourceManager.getResourceFolder()
					+ TRANSLATIONS_FILE_PATH_FROM_RESOURCE, true);
		}
	}

	/**
	 * extract a sheet from the file
	 * 
	 * @param fullyQualifiedName
	 * @return sheet from the file
	 */
	private static String[][] getSheet(String fullyQualifiedName) {
		return XlxUtil.getInstance().getRawData(fullyQualifiedName);
	}

	/**
	 * extract translations from this sheet and add to dictionary
	 * 
	 * @param sheet
	 * @param languageToUse
	 * @param translations
	 *            to which extracted translations to be added
	 */
	private static void addTranslations(String[][] sheet, String languageToUse,
			Map<String, String> translations) {
		if (sheet == null || sheet.length < 2 || sheet[0].length < 2) {
			return;
		}
		int existingNbr = translations.size();
		String[] languages = sheet[0];
		for (int col = 1; col < languages.length; col++) {
			String lang = languages[col];

			if (lang != null && lang.equalsIgnoreCase(languageToUse)) {

				for (int i = 1; i < sheet.length; i++) {
					String[] row = sheet[i];
					String text = row[0];
					if (text == null || text.length() == 0) {
						continue;
					}
					String translatedText = row[col];
					if (translatedText != null && translatedText.length() > 0) {
						translations.put(text.toLowerCase(), translatedText);
					}
				}
				Spit.out((translations.size() - existingNbr)
						+ " translations added ");
				return;
			}
			Spit.out("No translation column found for language "
					+ languageToUse + ".");
		}

		/**
		 * oops we came out of the loop, indicating that we did not find a
		 * column for this language
		 */
		Spit.out(" No translations found for language " + languageToUse
				+ " in this sheet.");
	}

	private Dubhashi(String language) {
		this.language = language;
	}

	/**
	 * translate a phrase into a language. Check in custom translations before
	 * dipping into generic translations
	 * 
	 * @param textToTranslate
	 * @return translated text, or null if not found.
	 */
	public String translate(String textToTranslate) {

		String text = textToTranslate.toLowerCase();
		String translatedText = null;
		if (this.customTranslations != null) {
			translatedText = this.customTranslations.get(text);
		}

		if (translatedText == null) {
			Map<String, String> translations = dictionaries.get(this.language);
			if (translations != null) {
				translatedText = translations.get(text);
			}
		}

		return translatedText;
	}

	/**
	 * Convenient method to try to translate, or retain English phrase
	 * 
	 * @param textToTranslate
	 * @return translated text if possible, else the input text itself
	 */
	public String translateOrRetain(String textToTranslate) {
		String translatedText = this.translate(textToTranslate);
		if (translatedText == null) {
			return textToTranslate;
		}

		return translatedText;
	}

	/**
	 * Add page specific translations.
	 * 
	 * @param qualifiedPageName
	 *            page name for which this is called for. Convention is that
	 *            there is a folder structure similar to page for translation
	 */
	public void addCustomTranslations(String qualifiedPageName) {
		String[][] sheet = getSheet(ResourceManager.getResourceFolder()
				+ TRANSLATION_FOLDER + qualifiedPageName.replace('.', '/'));
		addTranslations(sheet, this.language, this.customTranslations);
	}
}

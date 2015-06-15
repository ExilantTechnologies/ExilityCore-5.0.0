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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 
 * This class implements code generation using the templates that can also work
 * like an example of a generated code. Idea is to use an example code, fully
 * functional, and mark areas to be either copied as-it-is, or replace with
 * generated code. for example the class definition line is typically
 * 
 * class MyClassName implements SomeInterface extends SomeAbstractClass
 * 
 * In the above line only MyClassName is to be replaced with the actual class
 * name to be generated, and rest of the line remain the same. This line is
 * converted to template
 * 
 * class /*begin_className* /MyClassName/*end_className* / implements
 * SomeInterface extends SomeAbstractClass (Of course the space between * and /
 * is for obvious reason in this text)
 * 
 * Likewise all variable part of the template is enclosed in stub markers. And
 * then, an algorithm is implemented to supply values for these stubs for
 * individual cases to generate code.
 * 
 * Template has its header with list of stubs as a cross-check. Way to use this
 * utility: 1. Develop a working example and test it to ensure that the template
 * itself has no bugs. 2. Identify variable areas and put markers. This is not
 * that straight forward. It may require few iterations of design to arrive at
 * the right stubs. 3. Develop algorithm to generate source code to be
 * substituted for these template. this is once again an iterative process along
 * with step 2. 4. Write your generator based on step 2 and 3. 5. In your code,
 * you can use this class the following way a. read your template into a string.
 * b. create an instance of the template with this string. c. getErrorMessage()
 * to check if there are any errors in the template. d. once instantiated, this
 * template can be used for repeated generation of code. (It is immutable) e.
 * for each of the code that you have to generate, get all code snippets for
 * tags into a collection and call generate(snippets) method to get the
 * generated code.
 * 
 */
public class CodeGeneratorTemplate {
	private static final String BEGIN_TAG_LIST = "/*begin_tagList";
	private static final String END_TAG_LIST = "end_tagList*/";
	private static final String NO_TAG_LIST = "Template should have a comma separated list of tags within "
			+ BEGIN_TAG_LIST
			+ " and "
			+ END_TAG_LIST
			+ ". \n e.g. "
			+ BEGIN_TAG_LIST + " tag1 tag2 tag3 " + END_TAG_LIST;
	private static final String BEGIN_COMMENT = "/*begin_templateComment";
	private static final String END_COMMENT = "end_templateComment*/";
	private static final String INVALID_COMMENT = BEGIN_COMMENT
			+ " as begin comment block and " + END_COMMENT
			+ " are not nested properly in the templeate.";
	/**
	 * Constructor parses the template. If template is in error. error message
	 * is stored in this field. If this field is null means the template is
	 * valid.
	 */
	private String errorMessage = null;
	/**
	 * template as received by the constructor
	 */
	private String[] tagList = null;
	/***
	 * parts of template after removing the text between the tags. 0th element
	 * is the string from beginning of text to the beginning of first tag first
	 * element is the text between end of first tag and beginning of second tag
	 * last element is the text between last tag and end of template
	 */
	private String[] templateParts = null;
	/***
	 * tag names corresponding to templateParts. Note that this will have one
	 * element less than templateParts (templateParts has an element for the
	 * part after end of last tag)
	 */
	private String[] tagsToInsert = null;

	/***
	 * 
	 * @param template
	 *            code template to be used for code generation
	 */
	public CodeGeneratorTemplate(String template) {
		this.parse(template);
	}

	/**
	 * 
	 * @return error message associated with this template
	 */
	public String getErrorMessage() {
		return this.errorMessage;
	}

	/***
	 * in case you need to check whether the tags you are using in your code are
	 * in synch with the template
	 * 
	 * @return distinct list of tags declared in the template
	 */
	public String[] getTags() {
		return this.tagList;
	}

	/***
	 * generate code based on the template and the snippets supplied for the tag
	 * 
	 * @param stubs
	 *            code snippets to be substituted for tags
	 * @return generated source code
	 */
	public String generate(Map<String, String> stubs) {
		if (this.errorMessage != null) {
			return this.errorMessage;
		}

		String err = "";
		StringBuilder sbf = new StringBuilder();
		for (int i = 0; i < this.tagsToInsert.length; i++) {
			String tag = this.tagsToInsert[i];
			String snippet = stubs.get(tag);
			if (snippet == null) {
				err += "Snippet for tag " + tag
						+ " not supplied for generation.";
				continue;
			}
			sbf.append(this.templateParts[i]);
			sbf.append(snippet);
		}

		// were there any errors?
		if (err.length() > 0) {
			return err;
		}
		// let us not forget the last part
		sbf.append(this.templateParts[this.templateParts.length - 1]);
		return sbf.toString();
	}

	/***
	 * parse the template, validate tags and break into appropriate pieces for
	 * ease of generation sets this.errorMessage if there are any errors in the
	 * templates sets this.tagList, this.tagsToInsert and this.templateParts
	 * This is a long method but that is OK because it does sequential
	 * processing
	 */
	private void parse(String template) {
		int beginList = template.indexOf(BEGIN_TAG_LIST);
		int endList = template.indexOf(END_TAG_LIST);
		if (beginList < 0 || endList < 0 || beginList > endList) {
			this.errorMessage = NO_TAG_LIST;
			return;
		}

		// extract list of tags
		String str = template.substring(beginList + BEGIN_TAG_LIST.length(),
				endList);
		Spit.out("list of tags = " + str);

		// tag list is comma separated
		this.tagList = str.split(",");
		for (int i = 0; i < this.tagList.length; i++) {
			this.tagList[i] = this.tagList[i].trim();
		}

		// strip tag lists to get the rest of the template
		if (beginList > 0) {
			str = template.substring(0, beginList);
		} else {
			str = "";
		}
		str += template.substring(endList + END_TAG_LIST.length());

		// remove template comment
		beginList = str.indexOf(BEGIN_COMMENT);
		if (beginList >= 0) {
			endList = str.indexOf(END_COMMENT);
			if (endList < beginList) {
				this.errorMessage = INVALID_COMMENT;
				return;
			}
			String part2 = str.substring(endList + END_COMMENT.length());
			if (beginList == 0) {
				str = part2;
			} else {
				str = str.substring(0, beginList) + part2;
			}
		}

		List<TagInfo> tags = new ArrayList<CodeGeneratorTemplate.TagInfo>();
		String err = "";
		for (String tag : this.tagList) {
			String s1 = "/*begin_" + tag;
			String s2 = "end_" + tag + "*/";
			int i = str.indexOf(s1);
			if (i == -1) {
				err += tag
						+ " is defined as a tag, but it is not used in the template.";
				continue;
			}
			// tag may occur more than once
			do {
				TagInfo ti = new TagInfo();
				ti.tag = tag;
				ti.startAt = i;
				ti.endAt = str.indexOf(s2, i);
				if (ti.endAt == -1) {
					err += "End tag not found for " + tag + ".\n";
				}
				tags.add(ti);

				// try another one
				i = str.indexOf(s1, i + 1);
			} while (i >= 0);
		}

		if (err.length() > 0) {
			this.errorMessage = err;
			return;
		}

		// let us sort tags by the order they occur in the template
		int n = tags.size();
		for (int i = 0; i < n; i++) {
			int minAt = i;
			TagInfo tag = tags.get(i);
			int min = tag.startAt;
			for (int j = i + 1; j < n; j++) {
				int startAt = tags.get(j).startAt;
				if (startAt < min) {
					min = startAt;
					minAt = j;
				}
			}
			if (minAt != i) {
				tags.set(i, tags.get(minAt));
				tags.set(minAt, tag);
			}
		}

		// look for any tags that over-lap
		n--; // we look for pairs
		for (int i = 0; i < n; i++) {
			if (tags.get(i).endAt > tags.get(i + 1).startAt) {
				err += "Tags " + tags.get(i).tag + " and "
						+ tags.get(i + 1).tag + " overlap.\n";
			}
		}

		if (err.length() > 0) {
			this.errorMessage = err;
			return;
		}

		// all OK. Let us get ready to churn out code from this template
		n++; // we had decremented :-)
		this.tagsToInsert = new String[n];
		this.templateParts = new String[n + 1];
		int startAt = 0;
		for (int i = 0; i < n; i++) {
			TagInfo tag = tags.get(i);
			this.tagsToInsert[i] = tag.tag;
			int endAt = tag.startAt;
			if (endAt <= startAt) {
				endAt = startAt;
			}
			this.templateParts[i] = str.substring(startAt, endAt);
			startAt = tag.endAt + tag.tag.length() + 6; // end_<tag>/*
		}
		// let us not forget the last chunk..
		this.templateParts[n] = str.substring(startAt);
		return;
	}

	/**
	 * For unit testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String template = "abcd"
				+ BEGIN_TAG_LIST
				+ "a,b"
				+ END_TAG_LIST
				+ "123"
				+ "/*begin_a you should never see this end_a*/78912345/*begin_b it is an error if you see me  end_b*/34567";
		CodeGeneratorTemplate gen = new CodeGeneratorTemplate(template);
		String str = gen.getErrorMessage();
		if (str != null) {
			System.out.print("Error : " + gen.errorMessage);
			return;
		}

		Map<String, String> snippets = new HashMap<String, String>();
		snippets.put("a", "456");
		snippets.put("b", "6789012");
		str = gen.generate(snippets);
		System.out.print(str);
	}

	/***
	 * data structure to hold start and end of occurrence of a tag
	 */
	class TagInfo {
		String tag;
		int startAt;
		int endAt;
	}
}

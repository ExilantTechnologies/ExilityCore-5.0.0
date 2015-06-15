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

import java.io.IOException;

/**
 * 
 * 
 *
 */
public class Test {
	/**
	 * @param args
	 * @throws IOException
	 * @throws ExilityException
	 */
	public static void main(String[] args) throws IOException, ExilityException {

		String fileName = null;
		String resourceFolder = null;

		if (args.length < 2) {
			System.out
					.println("Please provide correct input to the jar : File source as first parameter and resource folder as second parameter");
			return;
		}

		fileName = args[0];
		resourceFolder = args[1];

		ResourceManager.loadAllResources(null, resourceFolder);

		// ResourceManager.setResourceFolder("/Users/harshabhat/Desktop/apache-tomcat-7.0.27/webapps/Ezapp/WEB-INF/resource");
		// fileName =
		// "/Users/harshabhat/Desktop/apache-tomcat-7.0.27/webapps/Ezapp/WEB-INF/resource/test/getFiles";

		TestProcessorInterface testProcessor = TestProcessor.getInstance();

		testProcessor.process(fileName);
	}
}

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

/***
 * Error while loading any Exility component. This is designed to be thrown all
 * the way up to the top, making it programmer friendly(so that it need not be
 * marked in each method)
 * 
 * @author raghu.bhandi
 * 
 */
public class ExilityLoadError extends Error {

	/**
	 * standard default
	 */
	private static final long serialVersionUID = 1L;

	/***
	 * type of component that failed
	 */
	private final String componentType;

	/***
	 * name of the component that failed to load
	 */
	private final String componentName;

	/***
	 * field name that failed to load. If this is null, then the error was at
	 * the structure level, and not at field level
	 */
	private final String componentAttributeName;

	/***
	 * value of the field that could not be loaded, or description of error (if
	 * the error is not because of an invalid field value)
	 */
	private final String vlaueInError;

	/***
	 * an error while loading an exility component
	 * 
	 * @param componentType
	 *            like table, sql etc..
	 * @param componentName
	 *            name of the component
	 * @param componentAttributeName
	 *            field name that is in error. null if the error is not at the
	 *            field level.
	 * @param valueInerror
	 *            actual value of teh field that was invalid. If the error is
	 *            not at the field level, then the descriotion of error.
	 */
	public ExilityLoadError(String componentType, String componentName,
			String componentAttributeName, String valueInerror) {
		super();
		this.componentType = componentType;
		this.componentName = componentName;
		this.componentAttributeName = componentAttributeName;
		this.vlaueInError = valueInerror;
	}

	@Override
	public String getMessage() {
		String msg = this.componentName
				+ " is an applicaiton component of type " + this.componentType
				+ ". It could not be loaded because ";
		if (this.componentAttributeName == null) {
			return msg + this.vlaueInError;
		}

		return msg + " a value of '" + this.vlaueInError
				+ "' is not appropriate for field "
				+ this.componentAttributeName;
	}

	@Override
	public String toString() {
		return this.getMessage();
	}
}
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
 * enumerates all possible input fields on a page
 * 
 */
public enum PageFieldType {
	/**
	 * assisted input field
	 */
	assistedInputField {
		@Override
		AssistedInputField getDefaultField() {
			return new AssistedInputField();
		}
	}
	/**
	 * checkbox field
	 */
	,
	checkBoxField {
		@Override
		AbstractField getDefaultField() {
			return new CheckBoxField();
		}
	}
	/**
	 * check box group field
	 */
	,
	checkBoxGroupField {
		@Override
		CheckBoxGroupField getDefaultField() {
			return new CheckBoxGroupField();
		}
	}
	/**
	 * hidden field. No rendering happens and this one remains as model
	 */
	,
	hiddenField {
		@Override
		HiddenField getDefaultField() {
			return new HiddenField();
		}
	}
	/**
	 * image field
	 */
	,
	imageField {
		@Override
		ImageField getDefaultField() {
			return new ImageField();
		}
	}
	/**
	 * password field
	 */
	,
	passwordField {
		@Override
		PasswordField getDefaultField() {
			return new PasswordField();
		}
	}
	/**
	 * radio button
	 */
	,
	radioButtonField {
		@Override
		RadioButtonField getDefaultField() {
			return new RadioButtonField();
		}
	}
	/**
	 * drop-down
	 */
	,
	selectionField {
		@Override
		SelectionField getDefaultField() {
			return new SelectionField();
		}
	}
	/**
	 * style field
	 */
	,
	styleField {
		@Override
		StyleField getDefaultField() {
			return new StyleField();
		}
	}
	/**
	 * text area
	 */
	,
	textAreaField {
		@Override
		TextAreaField getDefaultField() {
			return new TextAreaField();
		}
	}
	/**
	 * text input
	 */
	,
	textInputField {
		@Override
		TextInputField getDefaultField() {
			return new TextInputField();
		}
	}
	/**
	 * output field
	 */
	,
	outputField {
		@Override
		OutputField getDefaultField() {
			return new OutputField();
		}
	};
	abstract AbstractField getDefaultField();
}

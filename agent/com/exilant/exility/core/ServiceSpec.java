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
 * Exility component that defines the input data requirement for a service as
 * well as expected output data. While usage of this component is strongly
 * recommended, we are unable to popularize its usage, as most projects are not
 * concerned about server side validation of data coming from server. Most
 * projects are dependent on their client component to validate data type based
 * validations and checking for required fields. However, they fail to recognize
 * the fact that the client components are vulnerable to manipulation.
 */
public class ServiceSpec {
	/***
	 * get a default spec that does the job of a service-spec with default
	 * setting. This is used whenever users do not supply a specific spec for a
	 * service
	 * 
	 * @return default serviceSpec
	 */
	static ServiceSpec getDefaultSpec() {
		ServiceSpec spec = new ServiceSpec();
		return spec;
	}

	/***
	 * null implies that this is a default one.
	 */
	String name = null;

	/***
	 * for documentation
	 */
	String techNotes = "";

	/***
	 * specification for input
	 */
	DataSpec inSpec = new DataSpec();

	/***
	 * specification for output
	 */
	DataSpec outSpec = new DataSpec();

	ServiceSpec() {
	}

	/***
	 * extract data from inData into dc as per input specification
	 * 
	 * @param inData
	 *            serviceData containing data supplied by client
	 * @param dc
	 *            to which data is to be extracted
	 */
	public void translateInput(ServiceData inData, DataCollection dc) {
		dc.copyInternalFieldsFrom(inData);
		if (this.inSpec == null) {
			dc.copyFrom(inData);
		} else {
			this.inSpec.translateInput(inData, dc);
		}
	}

	/***
	 * copy output required output data from dc to serviceData
	 * 
	 * @param dc
	 * @param outData
	 */
	ServiceData translateOutput(DataCollection dc, ServiceData outData) {
		dc.copyInternalFieldsTo(outData);
		if (this.outSpec == null) {
			dc.copyTo(outData);
		} else {
			this.outSpec.translateOutput(dc, outData);
		}
		return outData;
	}
}
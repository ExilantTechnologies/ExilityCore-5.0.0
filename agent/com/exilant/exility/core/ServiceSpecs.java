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

/***
 * Manager to retrieve and provide serviceSpec instances.Provides a static
 * method to get required service spec
 * 
 */
public class ServiceSpecs {
	/***
	 * Hold all the specs that are read and cache them
	 */
	private static HashMap<String, ServiceSpec> serviceSpecs = new HashMap<String, ServiceSpec>();

	/***
	 * get the spec
	 * 
	 * @param specName
	 *            fully qualified spec name
	 * @param dc
	 *            can be null. used only to put error messages if any
	 * @return spec that you asked for. If it is not found, you will still get a
	 *         spec that is default. Default spec has the required methods to
	 *         prepare dc from all inputs. Using this design, we are able to
	 *         implement spec as an optional component.
	 */
	public static ServiceSpec getServiceSpec(String specName, DataCollection dc) {
		if (ServiceSpecs.serviceSpecs.containsKey(specName)) {
			return ServiceSpecs.serviceSpecs.get(specName);
		}

		ServiceSpec spec = (ServiceSpec) ResourceManager.loadResource("spec."
				+ specName, ServiceSpec.class);
		if (spec == null) {
			// we have promised that we will not return null. we will return a
			// default spec instead.
			spec = ServiceSpec.getDefaultSpec();
		} else if (spec.name != null && spec.name.length() > 0) {
			if (AP.definitionsToBeCached) {
				ServiceSpecs.serviceSpecs.put(specName, spec);
			}
		}
		return spec;
	}

	/***
	 * Convenient method for an internal service to persist a service spec
	 * 
	 * @param spec
	 * @param dc
	 */
	static void saveServiceSpec(ServiceSpec spec, DataCollection dc) {
		ResourceManager.saveResource("spec." + spec.name, spec);
	}

	/***
	 * flush all cached specs so that they will be loaded again as and when
	 * required..
	 */
	static void refresh() {
		ServiceSpecs.serviceSpecs = new HashMap<String, ServiceSpec>();
	}
}
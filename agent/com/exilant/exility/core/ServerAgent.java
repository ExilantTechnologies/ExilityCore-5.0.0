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
 * Class to be re-looked and deprecated as it is confusing to coexist with
 * ServiceAgent. We should go in for a method in ServiceAgent rather than this
 * separate class of a specific need by a reporting interface developed for
 * Nirmanith
 * 
 */
@Deprecated
public class ServerAgent {

	private static final ServerAgent agentInstance = new ServerAgent();

	/**
	 * get an instance of ServerAgent. As of now, it is a singleton, but we keep
	 * our options open
	 * 
	 * @return an agent
	 */
	public static ServerAgent getAgent() {
		return ServerAgent.agentInstance;
	}

	private ServerAgent() {

	}

	/**
	 * method to be used by another java class that may want to execute a a
	 * service. This class is assumed to be authorized to call any service
	 * 
	 * @param serviceName
	 * @param userId
	 *            who has logged-in, or in whose name this service is to be
	 *            executed
	 * @param inData
	 * @return dc that contains the result of this service
	 */
	public DataCollection serve(String serviceName, String userId,
			ServiceData inData) {
		DataCollection dc = new DataCollection();

		ServiceEntry entry = ServiceList.getServiceEntry(serviceName, true);
		if (entry == null) {
			Spit.out("ServiceAgent: No service entry found for " + serviceName);
			dc.addMessage("exilNoServiceName", serviceName);
			return dc;
		}
		if (!(entry.hasUserBasedSecurity && !UserBasedSecurityAgent.isCleared(
				userId, serviceName, inData))) {
			dc.copyFrom(inData);
			entry.serve(dc);
		}
		return dc;
	}
}

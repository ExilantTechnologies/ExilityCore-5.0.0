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
 * Defines a Service
 * 
 */
public interface ServiceInterface {
	/**
	 * Execute this service
	 * 
	 * @param dc
	 * @param handle
	 * @throws ExilityException
	 */
	public void execute(DataCollection dc, DbHandle handle)
			throws ExilityException;

	/***
	 * what data access type this service requires
	 * 
	 * @param dc
	 * @return data access type required by this service
	 */
	public DataAccessType getDataAccessType(DataCollection dc);

	/**
	 * name of service
	 * 
	 * @return simple name of this service (without module or any prefix)
	 */
	public String getName();

	/**
	 * this service is called as a step in another service.
	 * 
	 * @param dc
	 * @param handle
	 * @throws ExilityException
	 */
	public void executeAsStep(DataCollection dc, DbHandle handle)
			throws ExilityException;

	/**
	 * should the service be fired in the background (in a separate thread)?
	 * 
	 * @return true if this service is marked for background execution
	 */
	public boolean toBeRunInBackground();

	/**
	 * 
	 * @param inData
	 * @param outData
	 */
	public void serve(ServiceData inData, ServiceData outData);
}

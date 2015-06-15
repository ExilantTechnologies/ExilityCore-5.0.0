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
 * Interface to be implemented by classes that projects write to implement
 * business logic. Such code is necessary to some thing that the Exility task
 * can not do. Such a code is to be written when it becomes simpler to write
 * such a code instead of using a set of Exility tasks
 * 
 * @author Exilant
 * 
 */
public interface CustomCodeInterface {
	/***
	 * core method that does the job.
	 * 
	 * @param dc
	 * @param dbHandle
	 *            be very careful with dbHandle, if at all you use it. Remember
	 *            that Exility would have started a transaction, and expects to
	 *            commit/roll-back at the end of the service
	 * @param gridName
	 *            commonly used parameter, and hence is provided as a named
	 *            parameter
	 * @param parameters
	 *            generic way to pass parameters. If you use these, you should
	 *            validate parameters and put message back in dc if parameters
	 *            are not appropriate, so that caller would know on execution at
	 *            least.
	 * @return 0 implies that there is no error, but no real work got dome. If
	 *         it makes sense, return some measure of what you have done, or
	 *         simply return 1.
	 */
	public int execute(DataCollection dc, DbHandle dbHandle, String gridName,
			String[] parameters);

	/***
	 * what kind of database access does this code need?
	 * 
	 * @return database access level required : None/ReadOnly, ReadWrite
	 */
	public DataAccessType getDataAccessType();
}

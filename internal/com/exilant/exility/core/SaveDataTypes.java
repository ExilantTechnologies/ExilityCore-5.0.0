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
 * Saves data types into resource file. Class name is a verb because of the job
 * it has to do !!
 * 
 * @author Exilant Technologies
 * 
 */
public class SaveDataTypes implements CustomCodeInterface {
	static final String GRID_NAME = "dataTypes";
	static final String BULK_ACTION_NAME = "bulkAction";
	static final String DELETE_ACTION = "delete";

	@Override
	public int execute(DataCollection dc, DbHandle dbHandle, String gridName,
			String[] parameters) {
		/**
		 * dc contains a grid by name dataTypes that has all the data types.
		 * This is a typical grid with bulkAction. We create contents for file
		 * using this and replace existing file, rather than reading existing
		 * file and altering it. Hence, the algorithm is to create a DataTypes
		 * instance with all data types from the grid, except the ones marked
		 * for delete
		 */
		Grid types = dc.getGrid(SaveDataTypes.GRID_NAME);
		if (types == null) {
			// this being an internal service we do not create separate message
			dc.addError("Invalid data: grid with name "
					+ SaveDataTypes.GRID_NAME + " not found in dc.");
			return 0;
		}

		int nbrTypes = types.getNumberOfRows();
		// new one to be saved
		DataTypes dts = new DataTypes();

		// If there is any error, we should not do any update. Hence we
		// accumulate types to be deleted.
		AbstractDataType[] toBedeleted = new AbstractDataType[nbrTypes];
		int nbrToBeDeleted = 0;
		int nbrTypesAdded = 0;
		boolean errorEncountered = false;

		for (int i = 0; i < nbrTypes; i++) {
			// extract all column values of each row into dc.values, and then
			// create an instance based on these values
			types.copyRowToDc(i, null, dc);
			Object obj = ObjectManager.createFromDc(dc);

			if (obj == null || obj instanceof AbstractDataType == false) {
				dc.addError("Unable to convert contents of row " + (i + 1)
						+ " into a valid data type. Probably "
						+ ObjectManager.OBJECT_TYPE
						+ " is not a valid data type in this.");
				errorEncountered = true;
				continue;
			}
			AbstractDataType dt = (AbstractDataType) obj;
			String action = dc.getTextValue(SaveDataTypes.BULK_ACTION_NAME, "");
			if (action.equals(SaveDataTypes.DELETE_ACTION)) {
				toBedeleted[nbrToBeDeleted] = dt;
				nbrToBeDeleted++;
				continue;
			}

			dts.dataTypes.put(dt.name, dt);
			nbrTypesAdded++;
		}
		if (errorEncountered) {
			return 0;
		}

		// GetDataTypes is anyway needed to re-read from resources. Let is make
		// use of that instance for getting file name as well.
		GetDataTypes getter = new GetDataTypes();
		String fullName = getter.getFileName(dc);
		dc.removeGrid(SaveDataTypes.GRID_NAME);

		ResourceManager.saveResource(fullName, dts);
		dc.addMessage("exilInfo", fullName + " saved with " + nbrTypesAdded
				+ " messages.");

		// we have to update data types that are already cached
		if (nbrToBeDeleted > 0) {
			for (AbstractDataType dt : toBedeleted) {
				DataTypes.removeDataType(dt);
			}
		}

		for (String name : dts.dataTypes.keySet()) {
			DataTypes.addDataType(DataTypes.getDataType(name, dc));
		}
		// and let us get the latest messages back...
		getter.execute(dc, dbHandle, gridName, parameters);

		return nbrTypesAdded;
	}

	@Override
	public DataAccessType getDataAccessType() {
		return DataAccessType.NONE;
	}
}

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
 * Utility class that gets the next primary key for a table. We are using a
 * table to keep the last used key. Individual RDBMS offer various utilities,
 * but each has its issue. Moreover, our approach is portable across rdbms
 * However, we have to see if this has performance issue, in which case we will
 * provide a plug-in based design to exploit the specific facility provided by
 * RDBMS
 * 
 */
public class PrimaryKeyGenerator {
	private static final String UPDATE_SQL = "update exilPrimaryKey set lastKeyUsed = lastKeyUsed + ";
	private static final String WHERE = " where tableName = '";
	private static final String INSERT_SQL = "insert into exilPrimaryKey (tableName, lastKeyUsed) values ('";

	private static final String TABLE_NAME = "exilPrimaryKey";
	private static final String SELECT_SQL = "select lastKeyUsed from exilPrimaryKey where tableName = '";

	private PrimaryKeyGenerator() {
	}

	/**
	 * get the next key to be used
	 * 
	 * @param tableName
	 * @param columnName
	 * @param nbrKeys
	 *            how many keys do you want to use
	 * @return next key to be used
	 * @throws ExilityException
	 */
	public static long getNextKey(String tableName, String columnName,
			int nbrKeys) throws ExilityException {
		long key = 0;

		DbHandle handle = DbHandle.borrowHandle(DataAccessType.READWRITE);
		handle.beginTransaction();
		boolean gotIntoTrouble = false;
		try {
			StringBuilder sql = new StringBuilder();
			// increment last key used by nbrKeys;
			sql.append(UPDATE_SQL).append(nbrKeys).append(WHERE)
					.append(tableName).append('\'');
			int n = handle.execute(sql.toString(), false);
			if (n > 0) {
				/**
				 * we just updated the column in db. let us extract it.
				 */
				Object obj = handle.extractSingleField(SELECT_SQL + tableName
						+ '\'');
				if (obj == null) {
					Spit.out("Row for table " + tableName + " not found in "
							+ PrimaryKeyGenerator.TABLE_NAME);
					key = 0;
				} else {
					key = ((Number) obj).longValue();
				}
			} else {
				/**
				 * first time use. row not found for this table. get the
				 * existing max key;
				 */
				Spit.out("row for table " + tableName + " not found in "
						+ PrimaryKeyGenerator.TABLE_NAME
						+ ". row will be inserted using max(" + columnName
						+ ") from this table.");
				sql.setLength(0);
				sql.append("select max(").append(columnName).append(") from ")
						.append(tableName);
				Object obj = handle.extractSingleField(sql.toString());
				if (obj == null) {
					Spit.out("Unable to get max key from this table");
					key = 0;
				} else {
					key = ((Number) obj).longValue();
				}

				/**
				 * insert a row with the right key
				 */
				key = key + nbrKeys;
				sql.setLength(0);
				sql.append(INSERT_SQL).append(tableName).append("',")
						.append(key).append(")");
				handle.execute(sql.toString(), false);
			}
		} catch (ExilityException e) {
			Spit.out("Error while generating primary key for table "
					+ tableName + ". " + e.getMessage());
			gotIntoTrouble = true;
		}

		if (gotIntoTrouble) {
			handle.rollback();
		} else {
			handle.commit();
		}
		DbHandle.returnHandle(handle);

		/**
		 * key is now incremented key as updated in the db. We should return the
		 * next to be used by the caller
		 */
		key = key - nbrKeys + 1;
		Spit.out(key + " generated for table " + tableName);
		return key;
	}
}
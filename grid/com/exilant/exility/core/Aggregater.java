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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * aggregate rows in a column on the lines of group-by in an rdbms sql An output
 * grid is produced by summarizing certain columns in the input grids. Summary
 * is for each distinct value of the key field. Also, we assume that the grid is
 * already sorted by this key, or at least it is already "grouped" by this
 * column
 * */
public class Aggregater implements GridProcessorInterface,
		ToBeInitializedInterface {

	/**
	 * grid from which to aggregate
	 */
	String inputGridName = null;

	/**
	 * grid to which we write output rows. Created if required, replaced if
	 * exists
	 */
	String outputGridName = null;

	/**
	 * columns to be aggregated
	 */
	AggregaterColumn[] columns = new AggregaterColumn[0];

	/**
	 * a header row is created during initialization stage based on columns
	 * meant for aggregation
	 */
	protected String[] outputHeaderRow;

	/**
	 * where does the value of a column going into the output?
	 */
	protected int[] outputIndexes;

	protected Class<? extends AbstractAggregationField>[] fieldClasses;
	/**
	 * fields corresponding to aggregation function. Note that key is mapped to
	 * first for sake of simplicity
	 */
	static Map<AggregationFunctionName, Class<? extends AbstractAggregationField>> FIELD_CLASSES = new HashMap<Aggregater.AggregationFunctionName, Class<? extends AbstractAggregationField>>();

	static {
		FIELD_CLASSES.put(AggregationFunctionName.APPEND, AppendField.class);
		FIELD_CLASSES.put(AggregationFunctionName.AVERAGE, AverageField.class);
		FIELD_CLASSES.put(AggregationFunctionName.COUNT, CountField.class);
		FIELD_CLASSES.put(AggregationFunctionName.FIRST, FirstField.class);
		FIELD_CLASSES.put(AggregationFunctionName.KEY, FirstField.class);
		FIELD_CLASSES.put(AggregationFunctionName.LIST, AppendField.class);
		FIELD_CLASSES.put(AggregationFunctionName.LAST, LastField.class);
		FIELD_CLASSES.put(AggregationFunctionName.MAX, AppendField.class);
		FIELD_CLASSES.put(AggregationFunctionName.MIN, AppendField.class);
		FIELD_CLASSES.put(AggregationFunctionName.SUM, AppendField.class);
	}

	@Override
	public int process(DataCollection dc) {
		if (this.columns == null) {
			dc.addError(this.inputGridName
					+ " not set up properly for aggregation operaiton.");
			return 0;
		}

		Grid grid = dc.getGrid(this.inputGridName);

		if (grid == null) {
			dc.addError(this.inputGridName + " not found for aggregation.");
			return 0;
		}

		int nbrRows = grid.getNumberOfRows();
		String[][] inData = grid.getRawData();
		if (nbrRows == 0) {
			Spit.out(this.inputGridName + " has no data in it.");
			return 0;
		}

		String[] inputNames = grid.getColumnNames();

		/*
		 * for modularity, we create an assistant who would do the job of
		 * aggregation for us
		 */
		Assistant assistant = new Assistant();
		assistant.setUp(dc, inputNames);
		ArrayList<String[]> newRows = new ArrayList<String[]>();
		newRows.add(this.outputHeaderRow);
		for (int i = 1; i < inData.length; i++) {
			/*
			 * assistant would return accumulated row for the previous key as
			 * and when new key is encountered
			 */
			String[] newRow = assistant.processARow(inData[i]);
			if (newRow != null) {
				newRows.add(newRow);
			}
		}
		/*
		 * assistant would still be hanging on to the last accumulated row
		 */
		newRows.add(assistant.getAccumulatedRow());

		dc.addGrid(this.outputGridName, newRows.toArray(new String[0][]));
		return 1;
	}

	@Override
	public void initialize() {
		if (this.columns == null || this.columns.length == 0) {
			Spit.out("ERROR : "
					+ this.inputGridName
					+ " has no columns defined for aggregation. This will not work");
			this.columns = null;
		}
		int nbrCols = this.columns.length;
		this.outputIndexes = new int[nbrCols];

		/*
		 * in case of any append function, we will not have an output column for
		 * that. But that care is very rare. In fact very very rare. Hence we
		 * optimize for a case where there is NO append
		 */
		String[] header = new String[nbrCols];
		int outIndex = 0;
		for (int i = 0; i < this.columns.length; i++) {
			AggregaterColumn column = this.columns[i];
			this.fieldClasses[i] = FIELD_CLASSES
					.get(column.aggregationFunction);
			if (column.aggregationFunction != AggregationFunctionName.APPEND) {
				this.outputIndexes[i] = outIndex;
				header[outIndex] = column.outputColumnName;
				outIndex++;
			}
		}

		if (outIndex != nbrCols) {
			this.outputHeaderRow = new String[outIndex];
			for (int i = 0; i < this.outputHeaderRow.length; i++) {
				this.outputHeaderRow[i] = header[i];
			}
		} else {
			this.outputHeaderRow = header;
		}
	}

	/**
	 * all exility design components are immutable to allow them to be used in a
	 * thread-safe way. Hence we can not have instance attributes that carry
	 * current execution dependent info. We create an assistant class for this
	 * sake.
	 * 
	 */
	class Assistant {
		private AbstractAggregationField[] fields;
		private boolean accumulationStarted = false;

		/**
		 * get ready to accumulate rows
		 * 
		 * @param dc
		 * @param fields
		 *            as defined for this processor
		 * @param inputHeaderRow
		 *            actual header found in the grid
		 * @return true if we are ok, false in case of any error
		 */
		boolean setUp(DataCollection dc, String[] inputHeaderRow) {
			this.accumulationStarted = false;
			int nbrFields = Aggregater.this.columns.length;
			this.fields = new AbstractAggregationField[nbrFields];

			int outputIdx = 0;
			for (int i = 0; i < this.fields.length; i++) {
				AggregaterColumn column = Aggregater.this.columns[i];
				String columnName = column.inputColumnName;
				/*
				 * Where is this column in input grid
				 */
				int inputIdx = -1;
				for (int j = 0; j < inputHeaderRow.length; j++) {
					if (columnName.equals(inputHeaderRow[j])) {
						inputIdx = j;
						break;
					}
				}
				if (inputIdx == -1) {
					dc.addError(columnName + " not found for aggregation.");
					return false;
				}

				AbstractAggregationField field = null;
				try {
					field = Aggregater.this.fieldClasses[i].newInstance();
				} catch (Exception e) {
					dc.addError("Error while working with aggregaegater ; "
							+ e.getMessage());
					return false;
				}

				boolean outputExists = field.setIndexes(inputIdx, outputIdx);
				if (outputExists) {
					outputIdx++;
				} else {
					field.setNeighbor(this.fields[i - 1]);
				}
			}
			return true;
		}

		/**
		 * accumulate this row
		 * 
		 * @param rowOfData
		 *            row to be accumulated
		 * @return last accumulated row in case the key in this row is different
		 *         from the previous row
		 */
		String[] processARow(String[] rowOfData) {
			String[] rowToReturn = null;
			// check for change in key values
			if (this.accumulationStarted) {
				for (AbstractAggregationField field : this.fields) {
					if (field.keyChanged(rowOfData)) {
						rowToReturn = this.getAccumulatedRow();
						break;
					}
				}
			} else {
				this.accumulationStarted = true;
			}

			for (AbstractAggregationField field : this.fields) {
				field.accumulate(rowToReturn);
			}
			return rowToReturn;
		}

		/**
		 * prepare an output row from accumulated field values
		 * 
		 * @return output row
		 */
		String[] getAccumulatedRow() {
			String[] rowToReturn = new String[Aggregater.this.outputIndexes.length];
			for (AbstractAggregationField field : this.fields) {
				field.spitOut(rowToReturn);
			}
			return rowToReturn;
		}

	}

	abstract class AbstractAggregationField {
		/**
		 * index of this column in the input data row
		 */
		protected int inputIdx;

		/**
		 * index of this field in the output field. will remain as -1 for field
		 * that does not have an output. AppendField is the only known field
		 * that does not have an output as of now.
		 */
		protected int outputIdx = -1;

		/**
		 * 
		 * @param row
		 *            input data row
		 * @return true if this this a key field, and the current value of the
		 *         field is non-null but is different form the one in the
		 *         supplied row
		 */
		boolean keyChanged(String[] row) {
			/*
			 * we will over-ride this for KeyField only
			 */
			return false;
		}

		/**
		 * set inputIdx and outputIdx
		 * 
		 * @param inputIdx
		 * @param outputIdx
		 * @return true if outputIdx is relevant for this field. false
		 *         otherwise.
		 */
		boolean setIndexes(int inputIdx, int outputIdx) {
			/*
			 * only AppendField will override this function
			 */
			this.inputIdx = inputIdx;
			this.outputIdx = outputIdx;
			return true;
		}

		/**
		 * Append field needs this facility to link it to listfield
		 * 
		 * @param field
		 */
		void setNeighbor(AbstractAggregationField field) {
			// will be used by AppendField only
		}

		abstract void accumulate(String[] row);

		abstract String getValue();

		void spitOut(String[] row) {
			if (this.outputIdx != -1) {
				row[this.outputIdx] = this.getValue();
			}
		}
	}

	/**
	 * this is a key field. All key fields together hare considered for
	 * grouping. Consequtive rows with same values of all key columns are
	 * grouped together for calculating aggregates
	 * 
	 */
	class KeyField extends AbstractAggregationField {
		private String value = null;

		@Override
		void accumulate(String[] row) {
			if (this.value == null) {
				this.value = row[this.inputIdx];
			}
		}

		@Override
		String getValue() {
			String val = this.value;
			this.value = null;
			return val;
		}

		@Override
		boolean keyChanged(String[] row) {
			return (this.value != null && this.value.equals(row[this.inputIdx]) == false);
		}
	}

	/**
	 * track the sum
	 * 
	 */
	class SumField extends AbstractAggregationField {
		private double value = 0D;

		@Override
		void accumulate(String[] row) {
			this.value += Double.parseDouble(row[this.inputIdx]);
		}

		@Override
		String getValue() {
			String val = Double.toString(this.value);
			this.value = 0D;
			return val;
		}
	}

	/**
	 * keep count of rows
	 * 
	 */
	class CountField extends AbstractAggregationField {
		private int count = 0;

		@Override
		void accumulate(String[] row) {
			this.count++;
		}

		@Override
		String getValue() {
			String val = Integer.toString(this.count);
			this.count = 0;
			return val;
		}

	}

	/**
	 * keep the last field
	 * 
	 */
	class LastField extends AbstractAggregationField {
		private String value;

		@Override
		void accumulate(String[] row) {
			this.value = row[this.inputIdx];
		}

		@Override
		String getValue() {
			String val = this.value;
			this.value = null;
			return val;
		}

	}

	/**
	 * keep the first field
	 * 
	 */
	class FirstField extends AbstractAggregationField {
		private String value;

		@Override
		void accumulate(String[] row) {
			if (this.value == null) {
				this.value = row[this.inputIdx];
			}
		}

		@Override
		String getValue() {
			String val = this.value;
			this.value = null;
			return val;
		}
	}

	/**
	 * get max of this column
	 * 
	 */
	class MaxField extends AbstractAggregationField {
		private double max = Double.MIN_VALUE;

		@Override
		public void accumulate(String[] row) {
			double d = Double.parseDouble(row[this.inputIdx]);
			if (d > this.max) {
				this.max = d;
			}
		}

		@Override
		String getValue() {
			String val = Double.toString(this.max);
			this.max = Double.MIN_VALUE;
			return val;
		}
	}

	/**
	 * get min of this column
	 * 
	 */
	class MinField extends AbstractAggregationField {
		private double min = Double.MAX_VALUE;

		@Override
		public void accumulate(String[] row) {
			double d = Double.parseDouble(row[this.inputIdx]);
			if (d < this.min) {
				this.min = d;
			}
		}

		@Override
		public String getValue() {
			String val = Double.toString(this.min);
			this.min = Double.MAX_VALUE;
			return val;
		}
	}

	/**
	 * Get the average of this columns
	 * 
	 */
	class AverageField extends AbstractAggregationField {
		private double value = 0D;

		private int count = 0;

		@Override
		public void accumulate(String[] row) {
			this.value += Double.parseDouble(row[this.inputIdx]);
			this.count++;
		}

		@Override
		String getValue() {
			if (this.count == 0) {
				return "0";
			}

			double d = this.value / this.count;
			this.value = 0D;
			this.count = 0;
			return Double.toString(d);
		}

	}

	/**
	 * concatenate into a comma(or any other character) separated list
	 */
	class ListField extends AbstractAggregationField {
		private final StringBuilder value = new StringBuilder();

		private int count = 0;

		@Override
		public void accumulate(String[] row) {
			if (this.count > 0) {
				this.value.append(',');
			}
			this.value.append(row[this.inputIdx]);
			this.count++;
		}

		void append(String val) {
			this.value.append('-').append(val);
		}

		@Override
		String getValue() {
			String val = this.value.toString();
			this.count = 0;
			this.value.setLength(0);
			return val;
		}

	}

	/**
	 * appends this field column to the previous column
	 * 
	 */
	class AppendField extends AbstractAggregationField {
		private ListField listField;

		@Override
		boolean setIndexes(int inputIdx, int outputIdx) {
			this.inputIdx = inputIdx;
			return false;
		}

		@Override
		void setNeighbor(AbstractAggregationField field) {
			if (field instanceof ListField) {
				this.listField = (ListField) field;
			} else {
				Spit.out("ERROR : Append will not work as it is not sitting next to a list field");
			}
		}

		@Override
		void accumulate(String[] row) {
			if (this.listField != null) {
				this.listField.append(row[this.inputIdx]);
			}
		}

		@Override
		String getValue() {
			/**
			 * this should never be called as per our design
			 */
			return "Something wrong with Agregatorg. AppendField.getValue() shoudl not have been called";
		}

	}

	enum AggregationFunctionName {
		KEY, SUM, AVERAGE, MIN, MAX, FIRST, LAST, COUNT, LIST, APPEND
	}

}

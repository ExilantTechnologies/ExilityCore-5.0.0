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
 * class that tracks progress of a loop step and determines when to loop-back
 * and when to come out
 * 
 * 
 */
class LoopStepWorker {

	/**
	 * underlying loop step that this worker is taking care of
	 */
	private LoopStep step = null;

	/**
	 * grid that is used for looping
	 */
	private Grid grid;

	/**
	 * number of rows in the grid.
	 */
	private int maxIndex = 0;

	/**
	 * track which index on the grid we have to use for next loop
	 */
	private int nextIndexToUse = 0;

	/**
	 * we may have to copy some columns into dc.fields.
	 */
	private String[] inputColumnNames = null;
	/**
	 * If prefix is used, column name is different from field name
	 */
	private String[] inputFieldNames = null;
	/**
	 * Backup existing values before replacing them with the column values
	 */
	private Value[] backedUpValues;

	/**
	 * is the service calculating some column values as part of the logic inside
	 * the loop? We will have to copy these columns into grid
	 */
	private String[] outputColumnNames;

	/**
	 * Field names could be different from column names because of prefix
	 */
	private String[] outputFieldNames;

	LoopStepWorker(LoopStep step, DataCollection dc) {
		this.step = step;
		/*
		 * do we have a grid?
		 */
		this.grid = dc.getGrid(this.step.tableToLoopOn);
		if (this.grid == null) {
			Spit.out("Deesign Error: Service has a loopstep defined on "
					+ this.step.tableToLoopOn
					+ " but there is no grid with this name.");
			return;
		}

		/*
		 * is there data?
		 */
		this.maxIndex = this.grid.getNumberOfRows();
		if (this.maxIndex == 0) {
			return;
		}

		/*
		 * add columns to the grid if required to accommodate output fields
		 */
		if (this.step.outputFields != null) {
			this.createOutputFields();
		}

		/*
		 * get ready with input fields as well
		 */
		if (!this.step.noInputFieldsPlease) {
			this.inputColumnNames = (step.inputFields == null) ? this.grid
					.getColumnNames() : step.inputFields;
			this.createInputFields(dc);
		}
	}

	/**
	 * should the loop continue, or is it time to break-out?
	 * 
	 * @param dc
	 * @return true if loop is to continue, false if it is time to break out.
	 */
	boolean toContinue(DataCollection dc) {
		/*
		 * is there anything to loop on?
		 */
		if (this.maxIndex == 0) {
			return false;
		}

		/*
		 * copy fields back to grid if at least one loop got over
		 */
		if (this.nextIndexToUse > 0 && this.outputColumnNames != null) {
			this.copyBackOutputColumns(dc);
		}

		/*
		 * time to break?
		 */
		if (this.nextIndexToUse == this.maxIndex) {
			this.endLoop(dc);
			return false;
		}

		/*
		 * copy input fields from grid to fields before saying yes to looping
		 */
		this.copyInputFields(dc);
		this.nextIndexToUse++;
		return true;
	}

	/**
	 * called when a goTo is trying to jump out of the loop
	 * 
	 * @param dc
	 * 
	 */
	void jumpOut(DataCollection dc) {

		if (this.maxIndex == 0) {
			return;
		}
		/*
		 * copy fields back to grid if at least one loop got over
		 */
		if (this.nextIndexToUse > 0 && this.outputColumnNames != null) {
			this.copyBackOutputColumns(dc);
		}

		this.endLoop(dc);
	}

	/**
	 * done. Clean-up the mess we would have created. Note that this can be
	 * called directly by service in case a goTo step is about to break the loop
	 * and go out
	 * 
	 * @param dc
	 */
	private void endLoop(DataCollection dc) {

		/*
		 * restore fields from back-up
		 */
		for (int j = 0; j < this.backedUpValues.length; j++) {
			Value value = this.backedUpValues[j];
			if (value == null) {
				dc.removeValue(this.inputFieldNames[j]);
			} else {
				dc.addValue(this.inputFieldNames[j], value);
			}
		}
	}

	/**
	 * copy all column values into dc.fields collection
	 * 
	 * @param dc
	 */
	private void copyInputFields(DataCollection dc) {

		for (int j = 0; j < this.inputColumnNames.length; j++) {
			dc.addValue(this.inputFieldNames[j], this.grid.getValue(
					this.inputColumnNames[j], this.nextIndexToUse));
		}
	}

	/**
	 * before starting the next loop, we copy result fields back from dc.fields
	 * into columns of the grid
	 * 
	 * @param dc
	 */
	private void copyBackOutputColumns(DataCollection dc) {
		int idx = this.nextIndexToUse - 1;
		for (int j = 0; j < this.outputColumnNames.length; j++) {
			Value value = dc.getValue(this.outputFieldNames[j]);
			if (value != null) {
				this.grid.setValue(this.outputColumnNames[j], value, idx);
			}
		}
	}

	/**
	 * If output fields are specified, we have to add them to the grid of
	 * required. Called only if output fields exists.
	 * 
	 * @param outputFields
	 */
	private void createOutputFields() {
		/**
		 * add missing columns
		 */
		for (String name : this.outputColumnNames) {
			if (this.grid.hasColumn(name) == false) {
				DataValueType vt = DataDictionary.getValueType(name);
				try {
					this.grid.addColumn(name,
							ValueList.newList(vt, this.maxIndex));
				} catch (ExilityException e) {
					/*
					 * we know that this exception occurs if if the numbers
					 * differ...
					 */
					Spit.out(e.getMessage());
				}
			}
		}

		/**
		 * prepare output field names
		 */
		if (this.step.prefix != null) {
			String p = this.step.prefix;
			this.outputFieldNames = new String[this.outputColumnNames.length];
			for (int j = 0; j < this.outputColumnNames.length; j++) {
				this.outputFieldNames[j] = p + this.outputColumnNames[j];
			}
		} else {
			this.outputFieldNames = this.outputColumnNames;
		}
	}

	/**
	 * We will be copying some fields from grid to fields collection. Back these
	 * up before we start looping. These will be restored at the end of the
	 * loop.
	 * 
	 * @param dc
	 */
	private void createInputFields(DataCollection dc) {

		/*
		 * input columns defaults to all
		 */
		this.inputColumnNames = (this.step.inputFields == null) ? this.grid
				.getColumnNames() : this.step.inputFields;

		/*
		 * handle prefix once and for all, so that we need not worry about this
		 * on each loop
		 */
		if (this.step.prefix != null) {
			this.inputFieldNames = new String[this.inputColumnNames.length];
			String p = this.step.prefix;
			for (int j = 0; j < this.inputColumnNames.length; j++) {
				this.inputFieldNames[j] = p + this.inputColumnNames[j];
			}
		} else {
			this.inputFieldNames = this.inputColumnNames;
		}

		/*
		 * keep a back-up of fields before we start copying columns
		 */
		this.backedUpValues = new Value[this.inputFieldNames.length];
		for (int j = 0; j < this.backedUpValues.length; j++) {
			this.backedUpValues[j] = dc.getValue(this.inputFieldNames[j]);
		}
	}
}
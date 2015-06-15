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
import java.util.Map;

/**
 * data structure that stores all session data. One instance is associated with
 * one userId, and we are not allowed to change the assigned user id once it is
 * set. In essence, this helps in implementing a sub-session by userId within a
 * web-server session
 * 
 */
public class SessionData {

	/**
	 * this session data is for a logged-in user. By definition this is to be
	 * set once, and should not be changed
	 */
	private String userId = null;

	/**
	 * fields that are to be shared across all service requests
	 */
	private Map<String, String> fields = new HashMap<String, String>();

	/**
	 * grids are saved only if the client asks for paginated. This map is
	 * created on a need basis
	 */
	private Map<String, String[][]> grids = null;

	/**
	 * server may decide to run a service in the background, and return to the
	 * client immediately. we store the assigned file name for such jobs in this
	 * map. Map is created only on a need basis
	 */
	private Map<String, String> backgroundJobs = null;

	/**
	 * save a field
	 * 
	 * @param fieldName
	 * @param value
	 */
	public void addField(String fieldName, String value) {
		if (fieldName != null) {
			this.fields.put(fieldName, value);
		}
	}

	/**
	 * get value for a field
	 * 
	 * @param fieldName
	 * @return value of the field, or null if no value was saved for this field
	 */
	public String getFieldValue(String fieldName) {
		return this.fields.get(fieldName);
	}

	/**
	 * @param fieldName
	 *            to be removed
	 * @return value of the field that was removed, or null if it was not saved
	 * 
	 */
	public String removeField(String fieldName) {
		if (fieldName == null) {
			return null;
		}

		return this.fields.remove(fieldName);
	}

	/**
	 * save fields that are named in a comma separated string
	 * 
	 * @param fieldNames
	 *            fields to be set
	 * @param values
	 */
	public void addAllValues(String[] fieldNames, ServiceData values) {
		if (fieldNames == null || fieldNames.length == 0) {
			return;
		}
		for (String fieldName : fieldNames) {
			if (fieldName != null) {
				fieldName = fieldName.trim();
				String value = values.getValue(fieldName);
				this.fields.put(fieldName, value);
			}
		}
	}

	/**
	 * extract all fields into data
	 * 
	 * @param inData
	 *            to which fields are to be extracted into
	 */
	public void extractAll(ServiceData inData) {
		if (this.fields.size() > 0) {
			for (String fieldName : this.fields.keySet()) {
				inData.addValue(fieldName, this.fields.get(fieldName));
			}
		}
	}

	/**
	 * save a grid
	 * 
	 * @param gridName
	 * @param grid
	 */
	public void addGrid(String gridName, String[][] grid) {
		if (this.grids == null) {
			this.grids = new HashMap<String, String[][]>();
		}
		if (gridName != null) {
			this.grids.put(gridName, grid);
		}
	}

	/**
	 * get a saved grid
	 * 
	 * @param gridName
	 * @return grid, or null if no grid was saved with this name
	 */
	public String[][] getGrid(String gridName) {
		if (this.grids != null && gridName != null) {
			return this.grids.get(gridName);
		}
		return null;
	}

	/**
	 * remove a grid
	 * 
	 * @param gridName
	 *            to be removed
	 * @return grid that is removed, or null if it was not saved
	 */
	public String[][] removeGrid(String gridName) {
		if (gridName == null || this.grids == null) {
			return null;
		}
		return this.grids.remove(gridName);
	}

	/**
	 * userId can be assigned, but once.
	 * 
	 * @param userIdToBeAssigned
	 * @return true if this was assigned. false if user id was already set, and
	 *         we refused to change it
	 */
	public boolean assignUserId(String userIdToBeAssigned) {
		if (this.userId == null) {
			this.userId = userIdToBeAssigned;
			return true;
		}
		return false;
	}

	/**
	 * get the userId with which this sesionData is associated with
	 * 
	 * @return user id this session data is associated with
	 */
	public String getUserId() {
		return this.userId;
	}

	/**
	 * save a job status
	 * 
	 * @param jobId
	 * @param jobStatus
	 */
	public void addJobStatus(String jobId, String jobStatus) {
		if (jobId != null) {
			if (this.backgroundJobs == null) {
				this.backgroundJobs = new HashMap<String, String>();
			}
			this.backgroundJobs.put(jobId, jobStatus);
		}
	}

	/**
	 * get job status
	 * 
	 * @param jobId
	 * @return job status, or null if this job id was not saved
	 */
	public String getJobStatus(String jobId) {
		if (jobId != null && this.backgroundJobs != null) {
			return this.backgroundJobs.get(jobId);
		}
		return null;
	}

	/**
	 * remove a job
	 * 
	 * @param jobId
	 *            to be removed
	 * @return status of the job being removed, or null if no job was saved with
	 *         this id
	 */
	public String removeJob(String jobId) {
		if (jobId == null || this.backgroundJobs == null) {
			return null;
		}
		return this.backgroundJobs.remove(jobId);
	}

	/**
	 * Make a string of the form jobId=status;.... for all known jobs
	 * 
	 * @param outData
	 */
	public void setAllJobStatus(ServiceData outData) {
		if (this.backgroundJobs == null || this.backgroundJobs.size() == 0) {
			Spit.out("No jobs found in session for user id " + this.userId);
			return;
		}

		StringBuilder sbf = new StringBuilder();
		for (String jobId : this.backgroundJobs.keySet()) {
			String jobStatus = this.backgroundJobs.get(jobId);

			if (jobStatus.equals(CommonFieldNames.JOB_IS_RUNNING)) {
				if (FileUtility.exists(FileUtility.FILE_TYPE_TEMP, jobId)) {
					jobStatus = CommonFieldNames.JOB_IS_DONE;
					this.backgroundJobs.put(jobId, jobStatus);
				}
			}

			sbf.append(jobId).append('=').append(jobStatus).append(';');
		}
		int n = sbf.length();
		if (n == 0) {
			return;
		}

		// remove the last ';' that is extra
		sbf.deleteCharAt(n - 1);
		outData.addValue(CommonFieldNames.BACKGROUND_JOBS, sbf.toString());

	}

	/**
	 * check the status of a job. If it is done, return the data associated with
	 * the job in outData and remove the job entry
	 * 
	 * @param jobId
	 *            to be checked
	 * @param outData
	 *            into which status of job, and possibly data returned by the
	 *            job
	 */
	public void updateJobStatus(String jobId, ServiceData outData) {
		if (jobId == null || outData == null || this.backgroundJobs == null) {
			return;
		}

		String jobStatus = this.getJobStatus(jobId);
		if (jobStatus == null) {
			outData.addMessage(ExilityMessageIds.ERROR,
					"No pending job with id " + jobId);
			return;
		}

		/**
		 * read result from this job.
		 */
		String data = FileUtility.readText(FileUtility.FILE_TYPE_TEMP, jobId,
				true);
		if (data == null || data.length() == 0) // job is not yet done
		{
			outData.addValue(CommonFieldNames.BACKGROUND_JOB_STATUS, jobStatus);
			return;
		}

		/**
		 * that file content is nothing but serialized serviceData There is some
		 * inefficiency in this approach of serializing but let me tackle that
		 * during next re-factoring
		 */
		try {
			outData.extractData(data);
			this.backgroundJobs.remove(jobId);
			outData.addValue(CommonFieldNames.BACKGROUND_JOB_ID, jobId);
			outData.addValue(CommonFieldNames.BACKGROUND_JOB_STATUS,
					CommonFieldNames.JOB_IS_DONE);
		} catch (ExilityException e) {
			outData.addMessage(ExilityMessageIds.ERROR, e.getMessage());
		}
	}

}

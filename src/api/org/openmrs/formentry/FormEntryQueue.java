package org.openmrs.formentry;

import java.util.Date;

import org.openmrs.User;

public class FormEntryQueue {

	private int formEntryQueueId;
	private String formData;
	private int status = 0;
	private Date dateProcessed;
	private String errorMsg;
	private User creator;
	private Date dateCreated;

	/**
	 * @return Returns the formEntryQueueId.
	 */
	public int getFormEntryQueueId() {
		return formEntryQueueId;
	}

	/**
	 * @param formEntryQueueId
	 *            The formEntryQueueId to set.
	 */
	public void setFormEntryQueueId(int formEntryQueueId) {
		this.formEntryQueueId = formEntryQueueId;
	}

	/**
	 * @return Returns the formData.
	 */
	public String getFormData() {
		return formData;
	}

	/**
	 * @param formData
	 *            The formData to set.
	 */
	public void setFormData(String formData) {
		this.formData = formData;
	}


	/**
	 * @return Returns the status.
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status The status to set.
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return Returns the dateProcessed.
	 */
	public Date getDateProcessed() {
		return dateProcessed;
	}

	/**
	 * @param dateProcessed The dateProcessed to set.
	 */
	public void setDateProcessed(Date dateProcessed) {
		this.dateProcessed = dateProcessed;
	}

	/**
	 * @return Returns the errorMsg.
	 */
	public String getErrorMsg() {
		return errorMsg;
	}

	/**
	 * @param errorMsg The errorMsg to set.
	 */
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            The creator to set.
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated
	 *            The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

}

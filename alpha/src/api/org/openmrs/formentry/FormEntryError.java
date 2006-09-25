package org.openmrs.formentry;

import java.util.Date;

import org.openmrs.User;

public class FormEntryError {

	private Integer formEntryErrorId;
	private String formData;
	private String error;
	private String errorDetails;
	private User creator;
	private Date dateCreated;

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

	/**
	 * @return Returns the error.
	 */
	public String getError() {
		return error;
	}

	/**
	 * @param error
	 *            The error to set.
	 */
	public void setError(String error) {
		this.error = error;
	}

	/**
	 * @return Returns the errorDetails.
	 */
	public String getErrorDetails() {
		return errorDetails;
	}

	/**
	 * @param errorDetails
	 *            The errorDetails to set.
	 */
	public void setErrorDetails(String errorDetails) {
		this.errorDetails = errorDetails;
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
	 * @return Returns the formEntryErrorId.
	 */
	public Integer getFormEntryErrorId() {
		return formEntryErrorId;
	}

	/**
	 * @param formEntryErrorId
	 *            The formEntryErrorId to set.
	 */
	public void setFormEntryErrorId(Integer formEntryErrorId) {
		this.formEntryErrorId = formEntryErrorId;
	}

}

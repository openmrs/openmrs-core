package org.openmrs.formentry;

import java.util.Date;

import org.openmrs.User;

public class FormEntryArchive {
	
	private Integer formEntryArchiveId;
	private String formData;
	private User creator;
	private Date dateCreated;
	
	public FormEntryArchive() {
	}
	
	public FormEntryArchive(FormEntryQueue formEntryQueue) {
		setFormData(formEntryQueue.getFormData());
		setCreator(formEntryQueue.getCreator());
	}
	
	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}
	/**
	 * @param creator The creator to set.
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
	 * @param dateCreated The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	/**
	 * @return Returns the formData.
	 */
	public String getFormData() {
		return formData;
	}
	/**
	 * @param formData The formData to set.
	 */
	public void setFormData(String formData) {
		this.formData = formData;
	}
	/**
	 * @return Returns the formEntryArchiveId.
	 */
	public Integer getFormEntryArchiveId() {
		return formEntryArchiveId;
	}
	/**
	 * @param formEntryArchiveId The formEntryArchiveId to set.
	 */
	public void setFormEntryArchiveId(Integer formEntryArchiveId) {
		this.formEntryArchiveId = formEntryArchiveId;
	}
	
	

}

package org.openmrs.reporting;

import java.util.Date;

import org.openmrs.User;

public abstract class AbstractReportObject {

	private Integer reportObjectId; // database primary key
	private String name;
	private String description;
	private String type;
	private String subType;
	private User creator;
	private Date dateCreated;
	private User changedBy;
	private Date dateChanged;
	private Boolean voided = false;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;

	public AbstractReportObject()
	{
		// do nothing
	}

	public AbstractReportObject(Integer reportObjectId, String name, String description, String type, String subType, 
			User creator, Date dateCreated, User changedBy, Date dateChanged, Boolean voided, User voidedBy,
			Date dateVoided, String voidReason )
	{
		this.reportObjectId = reportObjectId;
		this.name = name;
		this.description = description;
		this.type = type;
		this.subType = subType;
		this.creator = creator;
		this.dateCreated = dateCreated;
		this.changedBy = changedBy;
		this.dateChanged = dateChanged;
		this.voided = voided;
		this.voidedBy = voidedBy;
		this.dateVoided = dateVoided;
		this.voidReason = voidReason;
	}
		
	/**
	 * @return Returns the reportObjectId.
	 */
	public Integer getReportObjectId() {
		return reportObjectId;
	}
	
	/**
	 * @param reportObjectId The reportObjectId to set.
	 */
	public void setReportObjectId(Integer reportObjectId) {
		this.reportObjectId = reportObjectId;
	
	}

	/**
	 * @return Returns the name;
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param name The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}	

	/**
	 * @return Returns the changedBy.
	 */
	public User getChangedBy() {
		return changedBy;
	}

	/**
	 * @param changedBy The changedBy to set.
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
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
	 * @return Returns the dateChanged.
	 */
	public Date getDateChanged() {
		return dateChanged;
	}
	/**
	 * @param dateChanged The dateChanged to set.
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
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
	 * @return Returns the dateVoided.
	 */
	public Date getDateVoided() {
		return dateVoided;
	}
	/**
	 * @param dateVoided The dateVoided to set.
	 */
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}
	/**
	 * @return Returns the voided.
	 */
	public Boolean getVoided() {
		return voided;
	}
	/**
	 * @param voided The voided to set.
	 */
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}
	/**
	 * @return Returns the voidedBy.
	 */
	public User getVoidedBy() {
		return voidedBy;
	}
	/**
	 * @param voidedBy The voidedBy to set.
	 */
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}
	/**
	 * @return Returns the voidReason.
	 */
	public String getVoidReason() {
		return voidReason;
	}
	/**
	 * @param voidReason The voidReason to set.
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}
	
	/**
	 * Helper function that does a null-safe test for equality between two objects. Does not check for type-safety. 
	 * @return Whether or not two objects (of the same class) are equal. 
	 */
	public static boolean equals(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}
	
	public String toString() {
		return this.getReportObjectId() + ", " + this.getName() + ", " + this.getDescription() + ", " + this.getType() + ", " + this.getSubType();
	}
}

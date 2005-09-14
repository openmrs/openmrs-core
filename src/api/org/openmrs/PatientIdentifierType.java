package org.openmrs;

import java.util.Date;

/**
 * PatientIdentifierType 
 */
public class PatientIdentifierType implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer patientIdentifierTypeId;
	private String name;
	private String description;
	private Date dateCreated;
	private User creator;

	/** default constructor */
	public PatientIdentifierType() {
	}

	/** constructor with id */
	public PatientIdentifierType(Integer patientIdentifierTypeId) {
		this.patientIdentifierTypeId = patientIdentifierTypeId;
	}
	
	/** 
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PatientIdentifierType) {
			PatientIdentifierType p = (PatientIdentifierType)obj;
			if (p != null)
				return (patientIdentifierTypeId == p.getPatientIdentifierTypeId());
		}
		return false;
	}
	
	// Property accessors

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
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the name.
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
	 * @return Returns the patientIdentifierTypeId.
	 */
	public Integer getPatientIdentifierTypeId() {
		return patientIdentifierTypeId;
	}

	/**
	 * @param patientIdentifierTypeId The patientIdentifierTypeId to set.
	 */
	public void setPatientIdentifierTypeId(Integer patientIdentifierTypeId) {
		this.patientIdentifierTypeId = patientIdentifierTypeId;
	}

	
}
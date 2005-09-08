package org.openmrs;

import java.util.Date;
import java.util.Set;

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

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPatientIdentifierTypeId() {
		return patientIdentifierTypeId;
	}

	public void setPatientIdentifierTypeId(Integer patientIdentifierTypeId) {
		this.patientIdentifierTypeId = patientIdentifierTypeId;
	}

	// Property accessors

	
}
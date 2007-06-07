package org.openmrs;

import java.util.Date;

/**
 * EncounterType 
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class EncounterType implements java.io.Serializable {

	public static final long serialVersionUID = 789L;

	// Fields

	private Integer encounterTypeId;
	private String name;
	private String description;
	private Date dateCreated;
	private User creator;

	// Constructors

	/** default constructor */
	public EncounterType() {
	}

	/** constructor with id */
	public EncounterType(Integer encounterTypeId) {
		this.encounterTypeId = encounterTypeId;
	}

	/** 
	 * Compares two EncounterType objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (encounterTypeId == null || obj == null || !(obj instanceof EncounterType))
			return false;
		
		EncounterType encounterType = (EncounterType) obj;
		return (this.encounterTypeId.equals(encounterType.getEncounterTypeId()));
	}
	
	public int hashCode() {
		if (this.getEncounterTypeId() == null) return super.hashCode();
		return this.getEncounterTypeId().hashCode();
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
	 * @return Returns the encounterTypeId.
	 */
	public Integer getEncounterTypeId() {
		return encounterTypeId;
	}

	/**
	 * @param encounterTypeId The encounterTypeId to set.
	 */
	public void setEncounterTypeId(Integer encounterTypeId) {
		this.encounterTypeId = encounterTypeId;
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
	
	public String toString() {
		return name;
	}
}
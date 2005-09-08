package org.openmrs;

import java.util.Date;
import java.util.Set;

/**
 * EncounterType 
 */
public class EncounterType implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer encounterTypeId;
	private String name;
	private String description;
	private Date dateCreated;
	private Set encounters;
	private User user;

	// Constructors

	/** default constructor */
	public EncounterType() {
	}

	/** constructor with id */
	public EncounterType(Integer encounterTypeId) {
		this.encounterTypeId = encounterTypeId;
	}

	// Property accessors

	/**
	 * 
	 */
	public Integer getEncounterTypeId() {
		return this.encounterTypeId;
	}

	public void setEncounterTypeId(Integer encounterTypeId) {
		this.encounterTypeId = encounterTypeId;
	}

	/**
	 * 
	 */
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 */
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 
	 */
	public Date getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * 
	 */
	public Set getEncounters() {
		return this.encounters;
	}

	public void setEncounters(Set encounters) {
		this.encounters = encounters;
	}

	/**
	 * 
	 */
	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
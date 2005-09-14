package org.openmrs;

import java.util.*;

/**
 * ConceptClass 
 */
public class ConceptClass implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer conceptClassId;
	private String name;
	private String description;
	private Boolean set;
	private User creator;
	private Date dateCreated;

	// Constructors

	/** default constructor */
	public ConceptClass() {
	}

	/** constructor with id */
	public ConceptClass(Integer conceptClassId) {
		this.conceptClassId = conceptClassId;
	}

	// Property accessors

	/**
	 * 
	 */
	public Integer getConceptClassId() {
		return this.conceptClassId;
	}

	public void setConceptClassId(Integer conceptClassId) {
		this.conceptClassId = conceptClassId;
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
	public User getCreateor() {
		return this.creator;
	}

	public void setCreator(User user) {
		this.creator = user;
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
	public boolean isSet() {
		return this.set;
	}

	public void setSet(boolean set) {
		this.set = set;
	}


}
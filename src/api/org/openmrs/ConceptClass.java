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
	private Date dateCreated;
	private Boolean isSet;
	private Set concepts;
	private User user;

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
	public Date getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * 
	 */
	public Boolean getIsSet() {
		return this.isSet;
	}

	public void setIsSet(Boolean isSet) {
		this.isSet = isSet;
	}

	/**
	 * 
	 */
	public Set getConcepts() {
		return this.concepts;
	}

	public void setConcepts(Set concepts) {
		this.concepts = concepts;
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
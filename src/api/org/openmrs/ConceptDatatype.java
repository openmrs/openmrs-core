package org.openmrs;

import java.util.Date;
import java.util.Set;

/**
 * ConceptDatatype 
 */
public class ConceptDatatype implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer conceptDatatypeId;
	private String name;
	private String definition;
	private Date dateCreated;
	private Set concepts;
	private User user;

	// Constructors

	/** default constructor */
	public ConceptDatatype() {
	}

	/** constructor with id */
	public ConceptDatatype(Integer conceptDatatypeId) {
		this.conceptDatatypeId = conceptDatatypeId;
	}

	// Property accessors

	/**
	 * 
	 */
	public Integer getConceptDatatypeId() {
		return this.conceptDatatypeId;
	}

	public void setConceptDatatypeId(Integer conceptDatatypeId) {
		this.conceptDatatypeId = conceptDatatypeId;
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
	public String getDefinition() {
		return this.definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
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
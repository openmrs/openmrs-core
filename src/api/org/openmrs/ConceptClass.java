package org.openmrs;

import java.util.Date;

/**
 * ConceptClass 
 */
public class ConceptClass implements java.io.Serializable {

	public static final long serialVersionUID = 33473L;

	// Fields

	private Integer conceptClassId;
	private String name;
	private String description;
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
	
	public boolean equals(Object obj) {
		if (obj instanceof ConceptClass) {
			ConceptClass c = (ConceptClass)obj;
			return (this.conceptClassId.equals(c.getConceptClassId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getConceptClassId() == null) return super.hashCode();
		return this.getConceptClassId().hashCode();
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
	public User getCreator() {
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

}
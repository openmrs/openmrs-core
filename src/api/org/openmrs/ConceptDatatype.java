package org.openmrs;

import java.util.Date;

/**
 * ConceptDatatype 
 */
public class ConceptDatatype implements java.io.Serializable {

	public static final long serialVersionUID = 473L;

	// Fields

	private Integer conceptDatatypeId;
	private String name;
	private String description;
	private String hl7Abbreviation;
	private Date dateCreated;
	private User creator;

	// Constructors

	/** default constructor */
	public ConceptDatatype() {
	}

	/** constructor with id */
	public ConceptDatatype(Integer conceptDatatypeId) {
		this.conceptDatatypeId = conceptDatatypeId;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof ConceptDatatype) {
			ConceptDatatype c = (ConceptDatatype)obj;
			return (this.conceptDatatypeId.equals(c.getConceptDatatypeId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getConceptDatatypeId() == null) return super.hashCode();
		return this.getConceptDatatypeId().hashCode();
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
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the hl7Abbreviation.
	 */
	public String getHl7Abbreviation() {
		return hl7Abbreviation;
	}

	/**
	 * @param hl7Abbreviation The hl7Abbreviation to set.
	 */
	public void setHl7Abbreviation(String hl7Abbreviation) {
		this.hl7Abbreviation = hl7Abbreviation;
	}

	/**
	 * 
	 */
	public User getCreator() {
		return this.creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
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
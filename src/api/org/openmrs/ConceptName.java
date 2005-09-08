package org.openmrs;

import java.util.Date;

/**
 * ConceptName 
 */
public class ConceptName implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private ConceptNameId conceptNameId;
	private String shortName;
	private User creator;
	private Date dateCreated;

	// Constructors

	/** default constructor */
	public ConceptName() {
	}

	public ConceptName(ConceptNameId conceptNameId) {
		this.conceptNameId = conceptNameId;
	}

	// Property accessors

	/**
	 * 
	 */
	public ConceptNameId getConceptNameId() {
		return conceptNameId;
	}

	public void setConceptNameId(ConceptNameId conceptNameId) {
		this.conceptNameId = conceptNameId;
	}

	/**
	 * 
	 */
	public Concept getConcept() {
		return conceptNameId.getConcept();
	}

	public void setConcept(Concept concept) {
		conceptNameId.setConcept(concept);
	}

	/**
	 * 
	 */
	public String getName() {
		return conceptNameId.getName();
	}

	public void setName(String name) {
		conceptNameId.setName(name);
	}

	/**
	 * 
	 */
	public String getLocale() {
		return conceptNameId.getLocale();
	}

	public void setLocale(String locale) {
		conceptNameId.setLocale(locale);
	}

	/**
	 * @return Returns the shortName.
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * @param shortName
	 *            The shortName to set.
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            The creator to set.
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
	 * @param dateCreated
	 *            The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

}
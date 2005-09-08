package org.openmrs;

import java.util.Date;

/**
 * ConceptSet 
 */
public class ConceptSet implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private ConceptSetId conceptSetId;
	private Double sortWeight;
	private User creator;
	private Date dateCreated;

	// Constructors

	/**
	 * @return Returns the conceptSetId.
	 */
	public ConceptSetId getConceptSetId() {
		return conceptSetId;
	}

	/**
	 * @param conceptSetId
	 *            The conceptSetId to set.
	 */
	public void setConceptSetId(ConceptSetId conceptSetId) {
		this.conceptSetId = conceptSetId;
	}

	/** default constructor */
	public ConceptSet() {
	}

	public ConceptSet(ConceptSetId conceptSetId) {
		this.conceptSetId = conceptSetId;
	}

	// Property accessors

	/**
	 * 
	 */
	public Concept getConcept() {
		return conceptSetId.getConcept();
	}

	public void setConcept(Concept concept) {
		conceptSetId.setConcept(concept);
	}

	/**
	 * 
	 */
	public Concept getSet() {
		return conceptSetId.getSet();
	}

	public void setSet(Concept set) {
		conceptSetId.setSet(set);
	}

	/**
	 * @return Returns the sortWeight.
	 */
	public Double getSortWeight() {
		return sortWeight;
	}

	/**
	 * @param sortWeight
	 *            The sortWeight to set.
	 */
	public void setSortWeight(Double sortWeight) {
		this.sortWeight = sortWeight;
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
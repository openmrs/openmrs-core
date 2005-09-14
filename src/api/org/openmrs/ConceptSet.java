package org.openmrs;

import java.util.Date;

/**
 * ConceptSet 
 */
public class ConceptSet implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Concept concept;
	private Concept set;
	private Double sortWeight;
	private User creator;
	private Date dateCreated;

	// Constructors

	/** default constructor */
	public ConceptSet() {
	}

	// Property accessors

	/**
	 * 
	 */
	public Concept getConcept() {
		return concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * 
	 */
	public Concept getSet() {
		return set;
	}

	public void setSet(Concept set) {
		this.set = set;
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
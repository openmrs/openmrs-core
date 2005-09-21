package org.openmrs;

import java.util.Date;

/**
 * ConceptAnswer 
 */
public class ConceptAnswer implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer conceptAnswerId;
	private Concept concept;
	private Concept answerConcept;
	private Integer answerDrug;
	private User creator;
	private Date dateCreated;

	// Constructors

	/** default constructor */
	public ConceptAnswer() {
	}

	/** constructor with id */
	public ConceptAnswer(Integer conceptAnswerId) {
		this.conceptAnswerId = conceptAnswerId;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof ConceptAnswer) {
			ConceptAnswer c = (ConceptAnswer)obj;
			return (this.conceptAnswerId.equals(c.getConceptAnswerId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getConceptAnswerId() == null) return super.hashCode();
		return this.getConceptAnswerId().hashCode();
	}

	/**
	 * @return Returns the answerConcept.
	 */
	public Concept getAnswerConcept() {
		return answerConcept;
	}

	/**
	 * @param answerConcept The answerConcept to set.
	 */
	public void setAnswerConcept(Concept answerConcept) {
		this.answerConcept = answerConcept;
	}

	/**
	 * @return Returns the answerDrug.
	 */
	public Integer getAnswerDrug() {
		return answerDrug;
	}

	/**
	 * @param answerDrug The answerDrug to set.
	 */
	public void setAnswerDrug(Integer answerDrug) {
		this.answerDrug = answerDrug;
	}

	/**
	 * @return Returns the concept.
	 */
	public Concept getConcept() {
		return concept;
	}

	/**
	 * @param concept The concept to set.
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * @return Returns the conceptAnswerId.
	 */
	public Integer getConceptAnswerId() {
		return conceptAnswerId;
	}

	/**
	 * @param conceptAnswerId The conceptAnswerId to set.
	 */
	public void setConceptAnswerId(Integer conceptAnswerId) {
		this.conceptAnswerId = conceptAnswerId;
	}

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

	// Property accessors

}
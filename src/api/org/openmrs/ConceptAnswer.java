package org.openmrs;

import java.util.*;

/**
 * ConceptAnswer 
 */
public class ConceptAnswer implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer conceptAnswerId;
	private Integer answerDrug;
	private Date dateCreated;
	private Concept conceptByConceptId;
	private Concept conceptByAnswerConcept;
	private User user;

	// Constructors

	/** default constructor */
	public ConceptAnswer() {
	}

	/** constructor with id */
	public ConceptAnswer(Integer conceptAnswerId) {
		this.conceptAnswerId = conceptAnswerId;
	}

	// Property accessors

	/**
	 * 
	 */
	public Integer getConceptAnswerId() {
		return this.conceptAnswerId;
	}

	public void setConceptAnswerId(Integer conceptAnswerId) {
		this.conceptAnswerId = conceptAnswerId;
	}

	/**
	 * 
	 */
	public Integer getAnswerDrug() {
		return this.answerDrug;
	}

	public void setAnswerDrug(Integer answerDrug) {
		this.answerDrug = answerDrug;
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
	public Concept getConceptByConceptId() {
		return this.conceptByConceptId;
	}

	public void setConceptByConceptId(Concept conceptByConceptId) {
		this.conceptByConceptId = conceptByConceptId;
	}

	/**
	 * 
	 */
	public Concept getConceptByAnswerConcept() {
		return this.conceptByAnswerConcept;
	}

	public void setConceptByAnswerConcept(Concept conceptByAnswerConcept) {
		this.conceptByAnswerConcept = conceptByAnswerConcept;
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
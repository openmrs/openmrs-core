/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.util.Date;

/**
 * This class represents one option for an answer to a question type of {@link Concept}. The link to
 * the parent question Concept is stored in {@link #getConcept()} and the answer this object is
 * representing is stored in {@link #getAnswerConcept()}.
 *
 * @see Concept#getAnswers()
 */
public class ConceptAnswer extends BaseOpenmrsObject implements Auditable, java.io.Serializable, Comparable<ConceptAnswer> {
	
	public static final long serialVersionUID = 3744L;
	
	// Fields
	private Integer conceptAnswerId;
	
	/**
	 * The question concept that this object is answering
	 */
	private Concept concept;
	
	/**
	 * The answer to the question
	 */
	private Concept answerConcept;
	
	/**
	 * The {@link Drug} answer to the question. This can be null if this does not represent a drug
	 * type of answer
	 */
	private Drug answerDrug;
	
	private User creator;
	
	private Date dateCreated;
	
	private Double sortWeight;
	
	// Constructors
	
	/** default constructor */
	public ConceptAnswer() {
	}
	
	/** constructor with id */
	public ConceptAnswer(Integer conceptAnswerId) {
		this.conceptAnswerId = conceptAnswerId;
	}
	
	public ConceptAnswer(Concept answerConcept) {
		this.answerConcept = answerConcept;
	}
	
	public ConceptAnswer(Concept answerConcept, Drug d) {
		this.answerConcept = answerConcept;
		this.answerDrug = d;
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
	public Drug getAnswerDrug() {
		return answerDrug;
	}
	
	/**
	 * @param answerDrug The answerDrug to set.
	 */
	public void setAnswerDrug(Drug answerDrug) {
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
	@Override
	public User getCreator() {
		return creator;
	}
	
	/**
	 * @param creator The creator to set.
	 */
	@Override
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	/**
	 * @return Returns the dateCreated.
	 */
	@Override
	public Date getDateCreated() {
		return dateCreated;
	}
	
	/**
	 * @param dateCreated The dateCreated to set.
	 */
	@Override
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getConceptAnswerId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setConceptAnswerId(id);
	}
	
	/**
	 * Not currently used. Always returns null.
	 *
	 * @see org.openmrs.Auditable#getChangedBy()
	 */
	@Override
	public User getChangedBy() {
		return null;
	}
	
	/**
	 * Not currently used. Always returns null.
	 *
	 * @see org.openmrs.Auditable#getDateChanged()
	 */
	@Override
	public Date getDateChanged() {
		return null;
	}
	
	/**
	 * Not currently used.
	 *
	 * @see org.openmrs.Auditable#setChangedBy(org.openmrs.User)
	 */
	@Override
	public void setChangedBy(User changedBy) {
	}
	
	/**
	 * Not currently used.
	 *
	 * @see org.openmrs.Auditable#setDateChanged(java.util.Date)
	 */
	@Override
	public void setDateChanged(Date dateChanged) {
	}
	
	/**
	 * @return Returns the sortWeight.
	 */
	public Double getSortWeight() {
		return sortWeight;
	}
	
	/**
	 * @param sortWeight The sortWeight to set.
	 */
	public void setSortWeight(Double sortWeight) {
		this.sortWeight = sortWeight;
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * Note: this comparator imposes orderings that are inconsistent with equals.
	 */
	@Override
	@SuppressWarnings("squid:S1210")
	public int compareTo(ConceptAnswer ca) {
		if ((getSortWeight() == null) && (ca.getSortWeight() != null)) {
			return -1;
		}
		if ((getSortWeight() != null) && (ca.getSortWeight() == null)) {
			return 1;
		}
		if ((getSortWeight() == null) && (ca.getSortWeight() == null)) {
			return 0;
		}
		return (getSortWeight() < ca.getSortWeight()) ? -1 : (getSortWeight() > ca.getSortWeight()) ? 1 : 0;
	}
}

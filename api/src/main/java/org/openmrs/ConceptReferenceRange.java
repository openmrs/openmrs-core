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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * A concept reference range is typically a range of a concept for certain factor(s) e.g. age, gender e.t.c. 
 * 
 * <p>
 * The criteria is used to evaluate if certain attributes of a patient meet a certain factor range. 
 * For example, if criteria is of factor age(say age between 1-5), then the ranges only apply for this age group. 
 * </p>
 *
 * @since 2.7.0
 */
@Entity
@Table(name = "concept_reference_range")
@Audited
public class ConceptReferenceRange extends BaseOpenmrsObject implements Auditable, Retireable {
	private static final long serialVersionUID = 47329L;

	// Fields
	@DocumentId
	@Id
	@Column(name = "concept_reference_range_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer conceptReferenceRangeId;

	@Column(name = "hi_absolute")
	private Double hiAbsolute;

	@Column(name = "hi_critical")
	private Double hiCritical;

	@Column(name = "hi_normal")
	private Double hiNormal;

	@Column(name = "low_absolute")
	private Double lowAbsolute;

	@Column(name = "low_critical")
	private Double lowCritical;

	@Column(name = "low_normal")
	private Double lowNormal;

	@Column(name = "criteria")
	private String criteria;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concept_id", nullable = false)
	private Concept concept;

	@ManyToOne
	@JoinColumn(name = "creator")
	private User creator;

	@ManyToOne
	@JoinColumn(name = "changed_by")
	private User changedBy;

	@Column(name = "date_changed")
	private Date dateChanged;

	@Column(name = "date_created")
	private Date dateCreated;

	@Field
	private Boolean retired = false;

	@ManyToOne
	@JoinColumn(name = "retired_by")
	private User retiredBy;

	@Column(name = "date_retired")
	private Date dateRetired;

	@Column(name = "retire_reason")
	private String retireReason;
	
	// Constructors
	
	/** Default constructor */
	public ConceptReferenceRange() {
	}

	// Property accessors

	/**
	 * Returns high absolute value of the conceptReferenceRange
	 * 
	 * @return hiAbsolute the high absolute value
	 */
	public Double getHiAbsolute() {
		return this.hiAbsolute;
	}

	/**
	 * Sets high absolute value of the conceptReferenceRange
	 * 
	 *  @param hiAbsolute low critical value to set
	 */
	public void setHiAbsolute(Double hiAbsolute) {
		this.hiAbsolute = hiAbsolute;
	}

	/**
	 * Returns high critical value of the conceptReferenceRange
	 *
	 * @return hiCritical the high critical value
	 */
	public Double getHiCritical() {
		return this.hiCritical;
	}

	/**
	 * Sets high critical value of the conceptReferenceRange
	 * 
	 *  @param hiCritical low critical value to set
	 */
	public void setHiCritical(Double hiCritical) {
		this.hiCritical = hiCritical;
	}

	/**
	 * Returns high normal value of the conceptReferenceRange
	 *
	 * @return hiNormal the high normal value
	 */
	public Double getHiNormal() {
		return this.hiNormal;
	}

	/**
	 * Sets high normal value of the conceptReferenceRange
	 * 
	 *  @param hiNormal low critical value to set
	 */
	public void setHiNormal(Double hiNormal) {
		this.hiNormal = hiNormal;
	}

	/**
	 * Returns low absolute value of the conceptReferenceRange
	 *
	 * @return lowAbsolute the low absolute value
	 */
	public Double getLowAbsolute() {
		return this.lowAbsolute;
	}

	/**
	 * Sets low absolute value of the conceptReferenceRange
	 * 
	 * @param lowAbsolute low critical value to set
	 */
	public void setLowAbsolute(Double lowAbsolute) {
		this.lowAbsolute = lowAbsolute;
	}

	/**
	 * Returns low critical value of the conceptReferenceRange
	 *
	 * @return lowCritical the low critical value
	 */
	public Double getLowCritical() {
		return this.lowCritical;
	}

	/**
	 * Sets low critical value of the conceptReferenceRange
	 * 
	 * @param lowCritical low critical value to set
	 */
	public void setLowCritical(Double lowCritical) {
		this.lowCritical = lowCritical;
	}

	/**
	 * Returns low absolute value of the conceptReferenceRange
	 *
	 * @return lowAbsolute the low absolute value
	 */
	public Double getLowNormal() {
		return this.lowNormal;
	}

	/**
	 * Sets low normal value of the conceptReferenceRange
	 * 
	 * @param lowNormal low normal value to set
	 */
	public void setLowNormal(Double lowNormal) {
		this.lowNormal = lowNormal;
	}

	/**
	 * @return Returns the conceptRangeId.
	 */
	public Integer getConceptReferenceRangeId() {
		return conceptReferenceRangeId;
	}

	/**
	 * @param conceptReferenceRangeId The conceptReferenceRangeId to set.
	 */
	public void setConceptReferenceRangeId(Integer conceptReferenceRangeId) {
		this.conceptReferenceRangeId = conceptReferenceRangeId;
	}

	/**
	 * Returns the criteria of the conceptReferenceRange
	 *
	 * @return criteria the criteria
	 */
	public String getCriteria() {
		return this.criteria;
	}

	/**
	 * Sets the criteria of the conceptReferenceRange
	 *
	 * @param criteria the criteria to set
	 */
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

	/**
	 * @return Returns the concept.
	 */
	public Concept getConcept() {
		return concept;
	}

	/**
	 * @param concept concept to set.
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	/**
	 * @return Returns the changedBy.
	 */
	@Override
	public User getChangedBy() {
		return changedBy;
	}

	/**
	 * @param changedBy The changedBy to set.
	 */
	@Override
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	/**
	 * @return Returns the dateChanged.
	 */
	@Override
	public Date getDateChanged() {
		return dateChanged;
	}

	/**
	 * @param dateChanged The dateChanged to set.
	 */
	@Override
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
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
	 * @see org.openmrs.OpenmrsObject#getId()
	 * @since 2.7.0
	 */
	@Override
	public Integer getId() {
		return getConceptReferenceRangeId();
	}

	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 * @since 2.7.0
	 */
	@Override
	public void setId(Integer id) {
		setConceptReferenceRangeId(id);
	}

	/**
	 * @return Returns the retired.
	 *
	 * @deprecated as of 2.0, use {@link #getRetired()}
	 */
	@Override
	@Deprecated
	@JsonIgnore
	public Boolean isRetired() {
		return getRetired();
	}

	/**
	 * This method delegates to {@link #isRetired()}.
	 *
	 * @see org.openmrs.Retireable#isRetired()
	 */
	@Override
	public Boolean getRetired() {
		return retired;
	}

	/**
	 * @param retired The retired to set.
	 */
	@Override
	public void setRetired(Boolean retired) {
		this.retired = retired;
	}

	/**
	 * @return the retiredBy
	 */
	@Override
	public User getRetiredBy() {
		return retiredBy;
	}

	/**
	 * @param retiredBy the retiredBy to set
	 */
	@Override
	public void setRetiredBy(User retiredBy) {
		this.retiredBy = retiredBy;
	}

	/**
	 * @return the dateRetired
	 */
	@Override
	public Date getDateRetired() {
		return dateRetired;
	}

	/**
	 * @param dateRetired the dateRetired to set
	 */
	@Override
	public void setDateRetired(Date dateRetired) {
		this.dateRetired = dateRetired;
	}

	/**
	 * @return the retireReason
	 */
	@Override
	public String getRetireReason() {
		return retireReason;
	}

	/**
	 * @param retireReason the retireReason to set
	 */
	@Override
	public void setRetireReason(String retireReason) {
		this.retireReason = retireReason;
	}

}

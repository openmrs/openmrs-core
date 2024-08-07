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

import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * ObsReferenceRange is typically a reference range of an Observation 
 * The reference range is created at the point of recording {@link Obs}
 *
 * @since 2.7.0
 */
@Entity
@Table(name = "obs_reference_range")
public class ObsReferenceRange extends BaseReferenceRange implements Creatable, Voidable, java.io.Serializable {
	
	private static final long serialVersionUID = 473299L;

	@DocumentId
	@Id
	@Column(name = "obs_reference_range_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer obsReferenceRangeId;

	@OneToOne
	@JoinColumn(name = "creator")
	private User creator;

	@Column(name = "date_created")
	private Date dateCreated;

	@Field
	@Column
	private Boolean voided = false;

	@OneToOne
	@JoinColumn(name = "voided_by")
	private User voidedBy;

	@Column(name = "date_voided")
	private Date dateVoided;

	@Column(name = "void_reason")
	private String voidReason;

	public ObsReferenceRange() {
	}
	
	/**
	 * Gets the obsReferenceRangeId
	 * 
	 * @since 2.7.0
	 * 
	 * @return Returns the obsReferenceRangeId.
	 */
	public Integer getObsReferenceRangeId() {
		return obsReferenceRangeId;
	}

	/**
	 * Sets the obsReferenceRangeId
	 * 
	 * @since 2.7.0
	 * 
	 * @param obsReferenceRangeId The obsReferenceRangeId to set.
	 */
	public void setObsReferenceRangeId(Integer obsReferenceRangeId) {
		this.obsReferenceRangeId = obsReferenceRangeId;
	}

	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 * 
	 * @since 2.7.0
	 */
	@Override
	public Integer getId() {
		return getObsReferenceRangeId();
	}

	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 * 
	 * @since 2.7.0
	 */
	@Override
	public void setId(Integer id) {
		setObsReferenceRangeId(id);
	}

	/**
	 * Gets the creator
	 * 
	 * @since 2.7.0
	 * 
	 * @return Returns the creator.
	 */
	@Override
	public User getCreator() {
		return creator;
	}

	/**
	 * Sets the creator
	 * 
	 * @since 2.7.0
	 * 
	 * @param creator The creator to set.
	 */
	@Override
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * Gets date created
	 * 
	 * @since 2.7.0
	 * 
	 * @return Returns the dateCreated.
	 */
	@Override
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * Sets the date created 
	 * 
	 * @since 2.7.0
	 * 
	 * @param dateCreated The dateCreated to set.
	 */
	@Override
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * Returns whether the ObsReferenceRange has been voided.
	 *
	 * @since 2.7.0
	 *
	 * @return true if the ObsReferenceRange has been voided, false otherwise.
	 */
	@Override
	public Boolean isVoided() {
		return getVoided();
	}

	/**
	 * Returns whether the ObsReferenceRange has been voided.
	 * 
	 * @since 2.7.0
	 *
	 * @return true if the ObsReferenceRange has been voided, false otherwise.
	 */
	@Override
	public Boolean getVoided() {
		return voided;
	}

	/**
	 * Sets the voided status of this ObsReferenceRange.
	 * 
	 * @since 2.7.0
	 *
	 * @param voided the voided status to set.
	 */
	@Override
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	/**
	 * Returns the User who voided this ObsReferenceRange.
	 * 
	 * @since 2.7.0
	 *
	 * @return the User who voided this ObsReferenceRange, or null if not set
	 */
	@Override
	public User getVoidedBy() {
		return voidedBy;
	}

	/**
	 * Sets the User who voided this ObsReferenceRange.
	 * 
	 * @since 2.7.0
	 *
	 * @param voidedBy the user who voided this ObsReferenceRange.
	 */
	@Override
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	/**
	 * Returns the Date this ObsReferenceRange was voided.
	 * 
	 * @since 2.7.0
	 *
	 * @return the Date this ObsReferenceRange was voided.
	 */
	@Override
	public Date getDateVoided() {
		return dateVoided;
	}

	/**
	 * Sets the Data this ObsReferenceRange was voided.
	 * 
	 * @since 2.7.0
	 *
	 * @param dateVoided the date the ObsReferenceRange was voided.
	 */
	@Override
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	/**
	 * Returns the reason this ObsReferenceRange was voided.
	 * 
	 * @since 2.7.0
	 *
	 * @return the reason this ObsReferenceRange was voided
	 */
	@Override
	public String getVoidReason() {
		return voidReason;
	}

	/**
	 * Sets the reason this ObsReferenceRange was voided.
	 * 
	 * @since 2.7.0
	 *
	 * @param voidReason the reason this ObsReferenceRange was voided
	 */
	@Override
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}
}

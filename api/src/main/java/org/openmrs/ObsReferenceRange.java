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

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.DocumentId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * ObsReferenceRange is typically a reference range of a numeric Observation 
 * The reference range is created at the point of creating {@link Obs}
 *
 * @since 2.7.0
 */
@Audited
@Entity
@Table(name = "obs_reference_range")
public class ObsReferenceRange extends BaseReferenceRange {
	
	private static final long serialVersionUID = 473299L;

	@DocumentId
	@Id
	@Column(name = "obs_reference_range_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer obsReferenceRangeId;
	
	@OneToOne
	@MapsId
	@JoinColumn(name = "obs_id", referencedColumnName = "obs_id", unique = true)
	private Obs obs;

	public ObsReferenceRange() {
	}
	
	/**
	 * Gets the obsReferenceRangeId
	 * 
	 * @return Returns the obsReferenceRangeId.
	 */
	public Integer getObsReferenceRangeId() {
		return obsReferenceRangeId;
	}

	/**
	 * Sets the obsReferenceRangeId
	 * 
	 * @param obsReferenceRangeId The obsReferenceRangeId to set.
	 */
	public void setObsReferenceRangeId(Integer obsReferenceRangeId) {
		this.obsReferenceRangeId = obsReferenceRangeId;
	}

	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getObsReferenceRangeId();
	}

	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setObsReferenceRangeId(id);
	}

	/**
	 * Gets Obs
	 * 
	 * @return Obs
	 */
	public Obs getObs() {
		return obs;
	}

	/**
	 * Sets obs 
	 * 
	 * @param obs Obs to set
	 */
	public void setObs(Obs obs) {
		this.obs = obs;
	}
}

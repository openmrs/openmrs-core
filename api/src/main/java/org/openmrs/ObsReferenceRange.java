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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

/**
 * ObsReferenceRange is typically a reference range of an Observation 
 * The reference range is created at the point of recording {@link Obs}
 *
 * @since 2.7.0
 */
@Entity
@Table(name = "obs_reference_range")
public class ObsReferenceRange extends BaseReferenceRange implements java.io.Serializable {
	
	private static final long serialVersionUID = 473299L;

	@DocumentId
	@Id
	@Column(name = "obs_reference_range_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer obsReferenceRangeId;

	@JoinColumn(name = "obs_id", nullable = false)
	private Obs obs;

	public ObsReferenceRange() {
	}
	
	/**
	 * @return Returns the obsReferenceRangeId.
	 */
	public Integer getObsReferenceRangeId() {
		return obsReferenceRangeId;
	}

	/**
	 * @param obsReferenceRangeId The obsReferenceRangeId to set.
	 */
	public void setObsReferenceRangeId(Integer obsReferenceRangeId) {
		this.obsReferenceRangeId = obsReferenceRangeId;
	}

	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 * @since 2.7.0
	 */
	@Override
	public Integer getId() {
		return getObsReferenceRangeId();
	}

	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 * @since 2.7.0
	 */
	@Override
	public void setId(Integer id) {
		setObsReferenceRangeId(id);
	}

	/**
	 * @return Returns the Obs.
	 */
	public Obs getObs() {
		return obs;
	}

	/**
	 * @param obs Obs to set.
	 */
	public void setObs(Obs obs) {
		this.obs = obs;
	}
}

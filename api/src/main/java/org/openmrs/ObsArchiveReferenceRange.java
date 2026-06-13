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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Entity representing archived observation reference ranges.
 */
@Entity
@Table(name = "obs_archive_reference_range")
public class ObsArchiveReferenceRange extends BaseReferenceRange {

	private static final long serialVersionUID = 473300L;

	@Id
	@Column(name = "obs_reference_range_id")
	private Integer obsReferenceRangeId;

	@OneToOne
	@MapsId
	@JoinColumn(name = "obs_id", referencedColumnName = "obs_id", unique = true)
	private ObsArchive obsArchive;

	public ObsArchiveReferenceRange() {
	}

	public Integer getObsReferenceRangeId() {
		return obsReferenceRangeId;
	}

	public void setObsReferenceRangeId(Integer obsReferenceRangeId) {
		this.obsReferenceRangeId = obsReferenceRangeId;
	}

	public ObsArchive getObsArchive() {
		return obsArchive;
	}

	public void setObsArchive(ObsArchive obsArchive) {
		this.obsArchive = obsArchive;
	}

	@Override
	public Integer getId() {
		return getObsReferenceRangeId();
	}

	@Override
	public void setId(Integer id) {
		setObsReferenceRangeId(id);
	}
}

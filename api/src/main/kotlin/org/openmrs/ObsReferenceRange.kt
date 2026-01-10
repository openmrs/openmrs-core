/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.envers.Audited
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId

/**
 * ObsReferenceRange is typically a reference range of a numeric Observation 
 * The reference range is created at the point of creating [Obs]
 *
 * @since 2.7.0
 */
@Audited
@Entity
@Table(name = "obs_reference_range")
open class ObsReferenceRange : BaseReferenceRange() {
	
	@DocumentId
	@Id
	@Column(name = "obs_reference_range_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	var obsReferenceRangeId: Int? = null
	
	@OneToOne
	@MapsId
	@JoinColumn(name = "obs_id", referencedColumnName = "obs_id", unique = true)
	var obs: Obs? = null

	override fun getId(): Int? = obsReferenceRangeId

	override fun setId(id: Int?) {
		obsReferenceRangeId = id
	}
	
	companion object {
		private const val serialVersionUID = 473299L
	}
}

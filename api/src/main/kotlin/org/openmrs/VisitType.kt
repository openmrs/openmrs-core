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
import jakarta.persistence.Table
import org.hibernate.envers.Audited

/**
 * Represents the assortment of visit types available to an implementation. These could include
 * items like "Initial HIV Clinic Visit", "Return TB Clinic Visit", and "Hospitalization".
 * 
 * @since 1.9
 */
@Entity
@Table(name = "visit_type")
@Audited
class VisitType() : BaseChangeableOpenmrsMetadata() {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "visit_type_id")
	var visitTypeId: Int? = null
	
	/**
	 * Constructor with id
	 * 
	 * @should set visit type id with given parameter
	 */
	constructor(visitTypeId: Int?) : this() {
		this.visitTypeId = visitTypeId
	}
	
	/**
	 * Required values constructor. This is the minimum number of values that must be non-null in
	 * order to have a successful save to the database
	 * 
	 * @param name the name of this visit type
	 * @param description a short description of why this visit type exists
	 */
	constructor(name: String, description: String) : this() {
		this.name = name
		this.description = description
	}
	
	override fun getId(): Int? = visitTypeId
	
	override fun setId(id: Int?) {
		visitTypeId = id
	}
	
	companion object {
		private const val serialVersionUID = 1L
	}
}

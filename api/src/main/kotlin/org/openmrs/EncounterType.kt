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

import org.hibernate.envers.Audited

/**
 * An EncounterType defines how a certain kind of [Encounter].
 * 
 * @see Encounter
 */
@Audited
open class EncounterType() : BaseChangeableOpenmrsMetadata() {
	
	private var encounterTypeId: Int? = null
	
	/**
	 * Gets privilege which can view this type of encounters
	 * @return the viewPrivilege the privilege instance
	 */
	var viewPrivilege: Privilege? = null
	
	/**
	 * Gets privilege which can edit this type of encounters
	 * @return the editPrivilege the privilege instance
	 */
	var editPrivilege: Privilege? = null
	
	/**
	 * Constructor with id
	 * 
	 * <strong>Should</strong> set encounter type id with given parameter
	 */
	constructor(encounterTypeId: Int?) : this() {
		this.encounterTypeId = encounterTypeId
	}
	
	/**
	 * Required values constructor. This is the minimum number of values that must be non-null in
	 * order to have a successful save to the database
	 * 
	 * @param name the name of this encounter type
	 * @param description a short description of why this encounter type exists
	 */
	constructor(name: String?, description: String?) : this() {
		this.name = name
		this.description = description
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject.getId
	 */
	override var id: Int?
		get() = encounterTypeId
		set(value) {
			encounterTypeId = value
		}
	
	companion object {
		const val serialVersionUID = 789L
	}
}

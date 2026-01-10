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
 * Mapping Class between Encounters and Providers which allows many to many relationship.
 * 
 * @since 1.9
 */
@Audited
open class EncounterProvider : BaseChangeableOpenmrsData() {
	
	var encounterProviderId: Int? = null
		set(value) {
			field = value
		}
	
	var encounter: Encounter? = null
	
	var provider: Provider? = null
	
	var encounterRole: EncounterRole? = null
	
	/**
	 * @see OpenmrsObject.getId
	 */
	override var id: Int?
		get() = encounterProviderId
		set(value) {
			encounterProviderId = value
		}
	
	/**
	 * @return copied encounter provider
	 *
	 * <strong>Should</strong> copy all EncounterProvider data
	 */
	fun copy(): EncounterProvider {
		return EncounterProvider().apply {
			changedBy = this@EncounterProvider.changedBy
			creator = this@EncounterProvider.creator
			dateChanged = this@EncounterProvider.dateChanged
			dateCreated = this@EncounterProvider.dateCreated
			dateVoided = this@EncounterProvider.dateVoided
			voided = this@EncounterProvider.voided
			voidedBy = this@EncounterProvider.voidedBy
			voidReason = this@EncounterProvider.voidReason
			encounter = this@EncounterProvider.encounter
			encounterRole = this@EncounterProvider.encounterRole
			provider = this@EncounterProvider.provider
		}
	}
	
	companion object {
		const val serialVersionUID = 1L
	}
}

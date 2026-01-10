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

/**
 * Provides properties for several order types like TestOrder, ReferralOrder
 * and others depending on the openmrs implementation use case as need arises
 * 
 * @since 2.5.0
 */
abstract class ServiceOrder : Order() {

	enum class Laterality {
		LEFT,
		RIGHT,
		BILATERAL
	}

	var specimenSource: Concept? = null

	var laterality: Laterality? = null

	var clinicalHistory: String? = null

	var frequency: OrderFrequency? = null

	var numberOfRepeats: Int? = null
	
	var location: Concept? = null

	protected fun copyHelper(target: ServiceOrder): ServiceOrder {
		super.copyHelper(target)
		target.specimenSource = specimenSource
		target.laterality = laterality
		target.clinicalHistory = clinicalHistory
		target.frequency = frequency
		target.numberOfRepeats = numberOfRepeats
		target.location = location
		return target
	}

	/**
	 * The purpose of this method is to allow subclasses of a ServiceOrder to delegate a portion of their
	 * cloneForDiscontinuing() method back to the superclass, in case the base class implementation
	 * changes.
	 *
	 * @param target a particular order that will have the state of `this` copied into it
	 * @return Returns the Order that was passed in, with state copied into it
	 */
	protected fun cloneForDiscontinuingHelper(target: ServiceOrder): ServiceOrder {
		target.careSetting = careSetting
		target.concept = concept
		target.action = Action.DISCONTINUE
		target.previousOrder = this
		target.patient = patient
		target.orderType = orderType
		return target
	}

	protected fun cloneForRevisionHelper(target: ServiceOrder): ServiceOrder {
		super.cloneForRevisionHelper(target)
		target.specimenSource = specimenSource
		target.laterality = laterality
		target.clinicalHistory = clinicalHistory
		target.frequency = frequency
		target.numberOfRepeats = numberOfRepeats
		target.location = location
		return target
	}

	companion object {
		const val serialVersionUID = 1L
	}
}

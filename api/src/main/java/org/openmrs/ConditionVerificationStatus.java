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

/**
 * The ConditionVerificationStatus is what we've historically called "diagnosis certainty" and what FHIR refers to as
 * ConditionVerificationStatus. This is the verification status to support or decline the clinical status of the condition or
 * diagnosis. The following subset of FHIR statuses are currently defined:
 * <li>{@link #PROVISIONAL}</li>
 * <li>{@link #CONFIRMED}</li>
 * 
 * @since 2.2
 */
public enum ConditionVerificationStatus {
	
	/**
	 * This is a tentative diagnosis - still a candidate that is under consideration. This was called "Presumed" in the 
	 * original EMRAPI module implementation.
	 */
	PROVISIONAL,

	/**
	 * There is sufficient diagnostic and/or clinical evidence to treat this as a confirmed condition.
	 */
	CONFIRMED
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import org.openmrs.Condition;
import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.util.PrivilegeConstants;

import java.util.List;

/**
* This interface defines methods for condition objects.
*
* @since 2.2
*/
public interface ConditionService extends OpenmrsService {

	/**
	 * Saves a condition
	 * 
	 * @param condition - the condition to be saved
	 */
	@Authorized({ PrivilegeConstants.EDIT_CONDITIONS })
	Condition save(Condition condition);

	/**
	 * Voids a condition
	 * 
	 * @param condition - the condition to be voided
	 * @param voidReason - the reason for voiding the condition
	 */
	@Authorized({ PrivilegeConstants.EDIT_CONDITIONS })
	Condition voidCondition(Condition condition, String voidReason);

	/**
	 * Gets a condition based on the uuid
	 * 
	 * @param uuid - uuid of the condition to be returned
	 * @return the condition
	 */
	@Authorized({ PrivilegeConstants.GET_CONDITIONS })
	Condition getConditionByUuid(String uuid);

	/**
	 * Gets a patient's active conditions
	 * 
	 * @param patient - the patient to retrieve conditions for
	 * @return a list of the patient's active conditions
	 */
	@Authorized({ PrivilegeConstants.GET_CONDITIONS })
	List<Condition> getActiveConditions(Patient patient);
}

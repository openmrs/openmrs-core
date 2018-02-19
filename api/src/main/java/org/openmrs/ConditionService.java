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

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.util.PrivilegeConstants;

import java.util.List;

public interface ConditionService extends OpenmrsService {

	/**
	 * @param condition - the condition to be saved
	 */
	@Authorized({ PrivilegeConstants.EDIT_CONDITIONS })
	Condition save(Condition condition);

	/**
	 * @param condition - the condition to be saved
	 * @param voidReason - description of condition
	 */
	@Authorized({ PrivilegeConstants.EDIT_CONDITIONS })
	Condition voidCondition(Condition condition, String voidReason);

	/**
	 * @param uuid - id of the condition to be returned
	 * @return the condition
	 */
	Condition getConditionByUuid(String uuid);

	/**
	 * @param patient - the patient in question
	 * @return a list of the patient's active conditions
	 */
	@Authorized({ PrivilegeConstants.GET_CONDITIONS })
	List<Condition> getActiveConditions(Patient patient);

	/**
	 * @return a list of concepts
	 */
	List<Concept> getEndReasonConcepts();
}

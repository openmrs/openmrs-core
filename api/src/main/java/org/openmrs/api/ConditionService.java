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
	 * @throws APIException   
	 */
	@Authorized({ PrivilegeConstants.EDIT_CONDITIONS })
	Condition saveCondition(Condition condition) throws APIException;

	/**
	 * Voids a condition
	 *
	 * @param condition - the condition to be voided
	 * @param voidReason - the reason for voiding the condition
	 * @throws APIException   
	 */
	@Authorized({ PrivilegeConstants.EDIT_CONDITIONS })
	Condition voidCondition(Condition condition, String voidReason) throws APIException;

	/**
	 * Gets a condition based on the uuid
	 *
	 * @param uuid - uuid of the condition to be returned
	 * @throws APIException   
	 * @return the condition
	 */
	@Authorized({ PrivilegeConstants.GET_CONDITIONS })
	Condition getConditionByUuid(String uuid) throws APIException;

	/**
	 * Gets a patient's active conditions
	 *
	 * @param patient - the patient to retrieve conditions for
	 * @throws APIException   
	 * @return a list of the patient's active conditions
	 */
	@Authorized({ PrivilegeConstants.GET_CONDITIONS })
	List<Condition> getActiveConditions(Patient patient) throws APIException;

	/**
	 * Gets a condition by id
	 *
	 * @param conditionId - the id of the Condition to retrieve
	 * @return the Condition with the given id, or null if none exists
	 * @throws APIException
	 */
	@Authorized({ PrivilegeConstants.GET_CONDITIONS })
	Condition getCondition(Integer conditionId) throws APIException;

	/**
	 * Revive a condition (pull a Lazarus)
	 *
	 * @param condition Condition to unvoid
	 * @throws APIException
	 * @should unset voided bit on given condition
	 */
	@Authorized(PrivilegeConstants.EDIT_CONDITIONS)
	Condition unvoidCondition(Condition condition) throws APIException;

	/**
	 * Completely remove a condition from the database. This should typically not be called
	 * because we don't want to ever lose data. The data really <i>should</i> be voided and then it
	 * is not seen in interface any longer (see #voidCondition(Condition) for that one) If other things link to
	 * this condition, an error will be thrown.
	 *
	 * @param condition
	 * @throws APIException
	 * @should delete the given condition from the database
	 */
	@Authorized(PrivilegeConstants.DELETE_CONDITIONS)
	void purgeCondition(Condition condition) throws APIException;
}

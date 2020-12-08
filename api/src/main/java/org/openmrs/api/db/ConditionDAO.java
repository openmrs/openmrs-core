/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import java.util.List;

import org.openmrs.Condition;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.APIException;

/**
 * This interface defines database methods for condition objects.
 *
 * @since 2.2
 */
public interface ConditionDAO {

	/**
	 * Saves the condition.
	 *
	 * @param condition the condition to save.
	 * @return the saved condition.
	 */
	Condition saveCondition(Condition condition);

	/**
	 * Gets the condition attached to the specified UUID.
	 *
	 * @param uuid the UUID to search for in the database.
	 * @return the condition associated with the UUID.
	 */
	Condition getConditionByUuid(String uuid);

	/**
	 * Gets all conditions related to the specified patient.
	 *
	 * @param patient the patient whose condition history is being queried.
	 * @return all active and non active conditions related to the specified patient.
	 */
	List<Condition> getConditionHistory(Patient patient);

	/**
	 * Gets all active conditions related to the specified patient.
	 *
	 * @param patient the patient whose active conditions are being queried.
	 * @return all active conditions associated with the specified patient.
	 */
	List<Condition> getActiveConditions(Patient patient);

	/**
	 * @see ConditionService#getAllConditions(Patient)
	 */
	List<Condition> getAllConditions(Patient patient);

	/**
	 * @see ConditionService#getConditionsByEncounter(Encounter)
	 */
	List<Condition> getConditionsByEncounter(Encounter encounter) throws APIException;

	/**
	 * Gets a condition by id
	 *
	 * @param conditionId the id of the condition to return
	 * @return the condition associated with the id
	 */
	Condition getCondition(Integer conditionId);
	
	/**
	 * Removes a condition from the database
	 * 
	 * @param condition the condition object to delete from database
	 */
	void deleteCondition(Condition condition) throws DAOException;
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api

import org.openmrs.Condition
import org.openmrs.Encounter
import org.openmrs.Patient
import org.openmrs.annotation.Authorized
import org.openmrs.util.PrivilegeConstants

/**
 * This interface defines methods for condition objects.
 *
 * @since 2.2
 */
interface ConditionService : OpenmrsService {

    /**
     * Gets a condition based on the uuid.
     *
     * @param uuid uuid of the condition to be returned
     * @return the condition
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.GET_CONDITIONS)
    @Throws(APIException::class)
    fun getConditionByUuid(uuid: String): Condition?

    /**
     * Gets a patient's active conditions.
     *
     * @param patient the patient to retrieve conditions for
     * @return a list of the patient's active conditions
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.GET_CONDITIONS)
    @Throws(APIException::class)
    fun getActiveConditions(patient: Patient): List<Condition>

    /**
     * Gets all conditions i.e both active and inactive conditions, associated with a patient.
     *
     * @param patient the patient to retrieve conditions for
     * @return a list of the patient's conditions
     * @throws APIException
     * @since 2.2.1
     */
    @Authorized(PrivilegeConstants.GET_CONDITIONS)
    @Throws(APIException::class)
    fun getAllConditions(patient: Patient): List<Condition>

    /**
     * Gets all conditions (not voided) of an encounter.
     *
     * @param encounter the encounter to retrieve conditions for
     * @return a list of encounter's conditions
     * @throws APIException
     * @since 2.4.0, 2.3.1
     */
    @Throws(APIException::class)
    fun getConditionsByEncounter(encounter: Encounter): List<Condition>

    /**
     * Gets a condition by id.
     *
     * @param conditionId the id of the Condition to retrieve
     * @return the Condition with the given id, or null if none exists
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.GET_CONDITIONS)
    @Throws(APIException::class)
    fun getCondition(conditionId: Int?): Condition?

    /**
     * Saves a condition.
     *
     * @param condition the condition to be saved
     * @return the saved condition
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.EDIT_CONDITIONS)
    @Throws(APIException::class)
    fun saveCondition(condition: Condition): Condition

    /**
     * Voids a condition.
     *
     * @param condition the condition to be voided
     * @param voidReason the reason for voiding the condition
     * @return the voided condition
     * @throws APIException if an error occurs while voiding the condition
     */
    @Authorized(PrivilegeConstants.EDIT_CONDITIONS)
    @Throws(APIException::class)
    fun voidCondition(condition: Condition, voidReason: String): Condition

    /**
     * Revive a previously voided condition.
     *
     * @param condition Condition to unvoid
     * @return the unvoided condition
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.EDIT_CONDITIONS)
    @Throws(APIException::class)
    fun unvoidCondition(condition: Condition): Condition

    /**
     * Completely remove a condition from the database. This should typically not be called
     * because we don't want to ever lose data. The data really should be voided and then it
     * is not seen in interface any longer (see [voidCondition] for that one). If other things link to
     * this condition, an error will be thrown.
     *
     * @param condition the condition to purge
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.DELETE_CONDITIONS)
    @Throws(APIException::class)
    fun purgeCondition(condition: Condition)
}

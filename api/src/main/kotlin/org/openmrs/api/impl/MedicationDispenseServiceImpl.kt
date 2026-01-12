/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl

import org.apache.commons.lang3.StringUtils
import org.openmrs.MedicationDispense
import org.openmrs.api.APIException
import org.openmrs.api.MedicationDispenseService
import org.openmrs.api.RefByUuid
import org.openmrs.api.db.MedicationDispenseDAO
import org.openmrs.parameter.MedicationDispenseCriteria
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * This class implements the [MedicationDispenseService] interface
 * It defines the API for interacting with MedicationDispense objects.
 * @since 2.6.0
 */
@Service("medicationDispenseService")
@Transactional
open class MedicationDispenseServiceImpl : BaseOpenmrsService(), MedicationDispenseService, RefByUuid {

    @Autowired
    lateinit var medicationDispenseDAO: MedicationDispenseDAO

    @Transactional(readOnly = true)
    override fun getMedicationDispense(medicationDispenseId: Int): MedicationDispense? {
        return medicationDispenseDAO.getMedicationDispense(medicationDispenseId)
    }

    @Transactional(readOnly = true)
    override fun getMedicationDispenseByUuid(uuid: String): MedicationDispense? {
        return medicationDispenseDAO.getMedicationDispenseByUuid(uuid)
    }

    @Transactional(readOnly = true)
    override fun getMedicationDispenseByCriteria(criteria: MedicationDispenseCriteria): List<MedicationDispense> {
        return medicationDispenseDAO.getMedicationDispenseByCriteria(criteria)
    }

    override fun saveMedicationDispense(medicationDispense: MedicationDispense): MedicationDispense {
        return medicationDispenseDAO.saveMedicationDispense(medicationDispense)
    }

    override fun voidMedicationDispense(
        medicationDispense: MedicationDispense,
        reason: String
    ): MedicationDispense {
        require(StringUtils.isNotBlank(reason)) { "voidReason cannot be null or empty" }
        // Actual voiding logic is done in the BaseVoidHandler
        return saveMedicationDispense(medicationDispense)
    }

    override fun unvoidMedicationDispense(medicationDispense: MedicationDispense): MedicationDispense {
        // Actual un-voiding logic is done in the BaseUnvoidHandler
        return saveMedicationDispense(medicationDispense)
    }

    override fun purgeMedicationDispense(medicationDispense: MedicationDispense) {
        medicationDispenseDAO.deleteMedicationDispense(medicationDispense)
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun <T> getRefByUuid(type: Class<T>, uuid: String): T? {
        if (MedicationDispense::class.java == type) {
            return getMedicationDispenseByUuid(uuid) as T?
        }
        throw APIException("Unsupported type for getRefByUuid: ${type?.name ?: "null"}")
    }

    override fun getRefTypes(): List<Class<*>> {
        return listOf(MedicationDispense::class.java)
    }
}

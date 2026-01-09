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

import org.openmrs.api.AdministrationService
import org.openmrs.api.ConceptService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component("allergyProperties")
class AllergyProperties {

    @Autowired
    @Qualifier("conceptService")
    protected lateinit var conceptService: ConceptService

    @Autowired
    @Qualifier("adminService")
    protected lateinit var administrationService: AdministrationService

    protected fun getConceptByGlobalProperty(globalPropertyName: String): Concept {
        val globalProperty = administrationService.getGlobalProperty(globalPropertyName)
            ?: throw IllegalStateException("Configuration required: $globalPropertyName")
        return conceptService.getConceptByUuid(globalProperty)
            ?: throw IllegalStateException("Configuration required: $globalPropertyName")
    }

    fun getMildSeverityConcept(): Concept =
        getConceptByGlobalProperty("allergy.concept.severity.mild")

    fun getModerateSeverityConcept(): Concept =
        getConceptByGlobalProperty("allergy.concept.severity.moderate")

    fun getSevereSeverityConcept(): Concept =
        getConceptByGlobalProperty("allergy.concept.severity.severe")

    fun getFoodAllergensConcept(): Concept =
        getConceptByGlobalProperty("allergy.concept.allergen.food")

    fun getDrugAllergensConcept(): Concept =
        getConceptByGlobalProperty("allergy.concept.allergen.drug")

    fun getEnvironmentAllergensConcept(): Concept =
        getConceptByGlobalProperty("allergy.concept.allergen.environment")

    fun getAllergyReactionsConcept(): Concept =
        getConceptByGlobalProperty("allergy.concept.reactions")

    fun getOtherNonCodedConcept(): Concept =
        getConceptByGlobalProperty("allergy.concept.otherNonCoded")

    fun getUnknownConcept(): Concept =
        getConceptByGlobalProperty("allergy.concept.unknown")
}

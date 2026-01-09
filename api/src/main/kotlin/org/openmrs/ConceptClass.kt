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

import jakarta.persistence.Cacheable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.envers.Audited

/**
 * ConceptClass
 */
@Entity
@Table(name = "concept_class")
@Audited
@Cacheable
class ConceptClass() : BaseChangeableOpenmrsMetadata() {

    companion object {
        const val serialVersionUID: Long = 33473L

        // UUIDs for core concept classes
        const val TEST_UUID: String = "8d4907b2-c2cc-11de-8d13-0010c6dffd0f"
        const val PROCEDURE_UUID: String = "8d490bf4-c2cc-11de-8d13-0010c6dffd0f"
        const val DRUG_UUID: String = "8d490dfc-c2cc-11de-8d13-0010c6dffd0f"
        const val DIAGNOSIS_UUID: String = "8d4918b0-c2cc-11de-8d13-0010c6dffd0f"
        const val FINDING_UUID: String = "8d491a9a-c2cc-11de-8d13-0010c6dffd0f"
        const val ANATOMY_UUID: String = "8d491c7a-c2cc-11de-8d13-0010c6dffd0f"
        const val QUESTION_UUID: String = "8d491e50-c2cc-11de-8d13-0010c6dffd0f"
        const val LABSET_UUID: String = "8d492026-c2cc-11de-8d13-0010c6dffd0f"
        const val MEDSET_UUID: String = "8d4923b4-c2cc-11de-8d13-0010c6dffd0f"
        const val CONVSET_UUID: String = "8d492594-c2cc-11de-8d13-0010c6dffd0f"
        const val MISC_UUID: String = "8d492774-c2cc-11de-8d13-0010c6dffd0f"
        const val SYMPTOM_UUID: String = "8d492954-c2cc-11de-8d13-0010c6dffd0f"
        const val SYMPTOM_FINDING_UUID: String = "8d492b2a-c2cc-11de-8d13-0010c6dffd0f"
        const val SPECIMEN_UUID: String = "8d492d0a-c2cc-11de-8d13-0010c6dffd0f"
        const val MISC_ORDER_UUID: String = "8d492ee0-c2cc-11de-8d13-0010c6dffd0f"
        const val ORDER_SET_UUID: String = "baa7a1b8-a1ba-11e0-9616-705ab6a580e0"
        const val FREQUENCY_UUID: String = "8e071bfe-520c-44c0-a89b-538e9129b42a"
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "concept_class_id_seq")
    @GenericGenerator(
        name = "concept_class_id_seq",
        strategy = "native",
        parameters = [Parameter(name = "sequence", value = "concept_class_concept_class_id_seq")]
    )
    @Column(name = "concept_class_id", nullable = false)
    var conceptClassId: Int? = null

    constructor(conceptClassId: Int?) : this() {
        this.conceptClassId = conceptClassId
    }

    override var id: Integer?
        get() = conceptClassId?.let { Integer(it) }
        set(value) { conceptClassId = value?.toInt() }
}

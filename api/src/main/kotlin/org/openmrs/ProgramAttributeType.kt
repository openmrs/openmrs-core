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

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.envers.Audited
import org.openmrs.attribute.AttributeType
import org.openmrs.attribute.BaseAttributeType

@Entity
@Table(name = "program_attribute_type")
@Audited
open class ProgramAttributeType : BaseAttributeType<PatientProgram>(), AttributeType<PatientProgram> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "program_attribute_type_id_seq")
    @GenericGenerator(
        name = "program_attribute_type_id_seq",
        strategy = "native",
        parameters = [Parameter(name = "sequence", value = "program_attribute_type_program_attribute_type_id_seq")]
    )
    @Column(name = "program_attribute_type_id")
    open var programAttributeTypeId: Int? = null
    
    override var id: Int?
        get() = programAttributeTypeId
        set(id) {
            programAttributeTypeId = id
        }
}

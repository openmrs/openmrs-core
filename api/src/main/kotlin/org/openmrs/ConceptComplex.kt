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

import org.hibernate.envers.Audited
import org.openmrs.obs.ComplexObsHandler

/**
 * Child class of Concept that has a [ComplexObsHandler] associated with the Concept.
 *
 * @since 1.5
 */
@Audited
class ConceptComplex() : Concept() {

    companion object {
        const val serialVersionUID: Long = 473231233L
    }

    var handler: String? = null

    constructor(conceptId: Int?) : this() {
        setConceptId(conceptId)
    }

    /**
     * Constructor with conceptId and ConceptComplexHandler
     *
     * @param conceptId
     * @param handler
     */
    constructor(conceptId: Int?, handler: String?) : this() {
        setConceptId(conceptId)
        this.handler = handler
    }

    /**
     * Constructor from Concept.
     *
     * @param c
     */
    constructor(c: Concept) : this() {
        answers = c.getAnswers(true).toMutableSet()
        changedBy = c.changedBy
        conceptClass = c.conceptClass
        setConceptId(c.getConceptId())
        conceptSets = c.conceptSets
        creator = c.creator
        datatype = c.datatype
        dateChanged = c.dateChanged
        dateCreated = c.dateCreated
        set = c.set
        names = c.names
        descriptions = c.descriptions
        conceptMappings = c.conceptMappings
        retired = c.retired
        version = c.version
        uuid = c.uuid

        handler = ""
    }

    /**
     * Overrides parent method and returns true if this Concept.getDatatype() equals "Complex"..
     *
     * @see Concept.isComplex
     */
    override fun isComplex(): Boolean {
        if (datatype == null || datatype?.hl7Abbreviation == null) {
            return false
        }
        return "ED" == datatype?.hl7Abbreviation
    }
}

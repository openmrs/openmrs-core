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

import org.codehaus.jackson.annotate.JsonIgnore
import org.hibernate.envers.Audited
import java.util.HashSet
import java.util.LinkedHashSet
import java.util.TreeSet

/**
 * The ConceptNumeric extends upon the Concept object by adding some number range values
 *
 * @see Concept
 */
@Audited
class ConceptNumeric() : Concept() {

    companion object {
        const val serialVersionUID: Long = 47323L
    }

    var hiAbsolute: Double? = null

    var hiCritical: Double? = null

    var hiNormal: Double? = null

    var lowAbsolute: Double? = null

    var lowCritical: Double? = null

    var lowNormal: Double? = null

    var units: String? = null

    var allowDecimal: Boolean = false

    var referenceRanges: MutableSet<ConceptReferenceRange> = LinkedHashSet()

    /**
     * displayPrecision, represents the number of significant digits
     * to be used for display of a numeric value
     */
    var displayPrecision: Int? = null

    /**
     * Generic constructor taking the primary key
     *
     * @param conceptId key for this numeric concept
     */
    constructor(conceptId: Int?) : this() {
        setConceptId(conceptId)
    }

    /**
     * Optional constructor for turning a Concept into a ConceptNumeric
     *
     * Note: This cannot copy over numeric specific values
     *
     * @param c
     * <strong>Should</strong> make deep copy of collections
     * <strong>Should</strong> change reference to the parent object  for objects in answers collection
     * <strong>Should</strong> change reference to the parent object  for objects in conceptSets collection
     * <strong>Should</strong> change reference to the parent object  for objects in names collection
     * <strong>Should</strong> change reference to the parent object  for objects in descriptions collection
     * <strong>Should</strong> change reference to the parent object  for objects in conceptMappings collection
     */
    constructor(c: Concept) : this() {
        changedBy = c.changedBy
        conceptClass = c.conceptClass
        setConceptId(c.getConceptId())
        creator = c.creator
        datatype = c.datatype
        dateChanged = c.dateChanged
        dateCreated = c.dateCreated
        set = c.set
        retired = c.retired
        retiredBy = c.retiredBy
        retireReason = c.retireReason
        version = c.version
        uuid = c.uuid

        names = HashSet(c.names)
        for (cName in names) {
            cName.concept = this
        }

        answers = HashSet(c.getAnswers(true)).toMutableSet()
        for (cAnswer in answers) {
            cAnswer.concept = this
        }

        conceptSets = TreeSet(c.conceptSets)
        for (cSet in conceptSets) {
            cSet.conceptSet = this
        }

        descriptions = HashSet(c.descriptions)
        for (cDescription in descriptions) {
            cDescription.concept = this
        }

        conceptMappings = HashSet(c.conceptMappings)
        for (cMap in conceptMappings) {
            cMap.concept = this
        }

        hiAbsolute = null
        hiCritical = null
        hiNormal = null
        lowAbsolute = null
        lowCritical = null
        lowNormal = null
        units = ""
        allowDecimal = false
    }

    /**
     * This method will <i>always</i> return true for ConceptNumeric objects that have a datatype of
     * Numeric
     *
     * @see Concept.isNumeric
     */
    override fun isNumeric(): Boolean = "Numeric" == datatype?.name?.name

    fun getAllowDecimal(): Boolean = allowDecimal

    /**
     * @deprecated as of 2.0, use [getAllowDecimal]
     */
    @Deprecated("Use allowDecimal property", ReplaceWith("allowDecimal"))
    @JsonIgnore
    fun isAllowDecimal(): Boolean = getAllowDecimal()

    /**
     * Helper method used to add conceptReferenceRange to the list of conceptReferenceRanges
     *
     * @since 2.7.0
     *
     * @param referenceRange to add
     */
    fun addReferenceRange(referenceRange: ConceptReferenceRange) {
        referenceRanges.add(referenceRange)
    }

    /**
     * Helper method used to remove conceptReferenceRange from a list of conceptReferenceRanges
     *
     * @param referenceRange reference range to remove
     *
     * @since 2.7.0
     */
    fun removeReferenceRange(referenceRange: ConceptReferenceRange) {
        referenceRanges.remove(referenceRange)
    }
}

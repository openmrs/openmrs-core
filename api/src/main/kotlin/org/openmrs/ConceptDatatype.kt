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
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.envers.Audited

/**
 * ConceptDatatype
 */
@Entity
@Table(name = "concept_datatype")
@Audited
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
class ConceptDatatype() : BaseChangeableOpenmrsMetadata() {

    companion object {
        const val serialVersionUID: Long = 473L

        // HL7 abbreviations (along with our own boolean creature)
        const val BOOLEAN: String = "BIT"
        const val CODED: String = "CWE"
        const val DATE: String = "DT"
        const val DATETIME: String = "TS"
        const val DOCUMENT: String = "RP"
        const val NUMERIC: String = "NM"
        const val TEXT: String = "ST"
        const val TIME: String = "TM"

        // UUIDs for core datatypes
        const val NUMERIC_UUID: String = "8d4a4488-c2cc-11de-8d13-0010c6dffd0f"
        const val CODED_UUID: String = "8d4a48b6-c2cc-11de-8d13-0010c6dffd0f"
        const val TEXT_UUID: String = "8d4a4ab4-c2cc-11de-8d13-0010c6dffd0f"
        const val N_A_UUID: String = "8d4a4c94-c2cc-11de-8d13-0010c6dffd0f"
        const val DOCUMENT_UUID: String = "8d4a4e74-c2cc-11de-8d13-0010c6dffd0f"
        const val DATE_UUID: String = "8d4a505e-c2cc-11de-8d13-0010c6dffd0f"
        const val TIME_UUID: String = "8d4a591e-c2cc-11de-8d13-0010c6dffd0f"
        const val DATETIME_UUID: String = "8d4a5af4-c2cc-11de-8d13-0010c6dffd0f"
        const val BOOLEAN_UUID: String = "8d4a5cca-c2cc-11de-8d13-0010c6dffd0f"
        const val RULE_UUID: String = "8d4a5e96-c2cc-11de-8d13-0010c6dffd0f"
        const val STRUCTURED_NUMERIC_UUID: String = "8d4a606c-c2cc-11de-8d13-0010c6dffd0f"
        const val COMPLEX_UUID: String = "8d4a6242-c2cc-11de-8d13-0010c6dffd0f"
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "concept_datatype_id_seq")
    @GenericGenerator(
        name = "concept_datatype_id_seq",
        strategy = "native",
        parameters = [Parameter(name = "sequence", value = "concept_datatype_concept_datatype_id_seq")]
    )
    @Column(name = "concept_datatype_id", nullable = false)
    var conceptDatatypeId: Int? = null

    @Column(name = "hl7_abbreviation", length = 3)
    var hl7Abbreviation: String? = null

    constructor(conceptDatatypeId: Int?) : this() {
        this.conceptDatatypeId = conceptDatatypeId
    }

    override var id: Integer?
        get() = conceptDatatypeId?.let { Integer(it) }
        set(value) { conceptDatatypeId = value?.toInt() }

    /*
     * Convenience methods for resolving common data types
     */

    /**
     * @return `true` if datatype is N/A, i.e. this concept is only an answer, not a question
     */
    fun isAnswerOnly(): Boolean = N_A_UUID == uuid

    /**
     * @return `true` if datatype is a numeric datatype
     */
    fun isNumeric(): Boolean = NUMERIC_UUID == uuid

    /**
     * @return `true` if datatype is coded (i.e., an identifier from a vocabulary)
     */
    fun isCoded(): Boolean = CODED_UUID == uuid

    /**
     * @return `true` if datatype is representation of date (but NOT a time or
     *         datatime--see containsDate() and containsTime())
     */
    fun isDate(): Boolean = DATE_UUID == uuid

    /**
     * @return `true` if datatype is representation of time
     * @since 1.7
     */
    fun isTime(): Boolean = TIME_UUID == uuid

    /**
     * @return `true` if datatype is representation of Datetime
     * @since 1.7
     */
    fun isDateTime(): Boolean = DATETIME_UUID == uuid

    /**
     * @return `true` if datatype is representation of either date or Datetime
     * @since 1.7
     */
    fun containsDate(): Boolean = DATE_UUID == uuid || DATETIME_UUID == uuid

    /**
     * @return `true` if datatype is representation of either time or Datetime
     * @since 1.7
     */
    fun containsTime(): Boolean = TIME_UUID == uuid || DATETIME_UUID == uuid

    /**
     * @return `true` if datatype is text-based
     */
    fun isText(): Boolean = TEXT_UUID == uuid || DOCUMENT_UUID == uuid

    /**
     * @return `true` if datatype is boolean
     */
    fun isBoolean(): Boolean = BOOLEAN_UUID == uuid

    /**
     * @return `true` if datatype is complex
     * @since 1.7
     */
    fun isComplex(): Boolean = COMPLEX_UUID == uuid

    /**
     * @return `true` if datatype is a rule
     * @since 1.7
     */
    fun isRule(): Boolean = RULE_UUID == uuid
}

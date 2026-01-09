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
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.envers.Audited
import java.io.Serializable
import java.util.Date

/**
 * This class represents one option for an answer to a question type of [Concept]. The link to
 * the parent question Concept is stored in [concept] and the answer this object is
 * representing is stored in [answerConcept].
 *
 * @see Concept.getAnswers
 */
@Entity
@Table(name = "concept_answer")
@BatchSize(size = 25)
@Audited
class ConceptAnswer() : BaseOpenmrsObject(), Auditable, Serializable, Comparable<ConceptAnswer> {

    companion object {
        const val serialVersionUID: Long = 3744L
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "concept_answer_id_seq")
    @GenericGenerator(
        name = "concept_answer_id_seq",
        strategy = "native",
        parameters = [Parameter(name = "sequence", value = "concept_answer_concept_answer_id_seq")]
    )
    @Column(name = "concept_answer_id")
    var conceptAnswerId: Int? = null

    /**
     * The question concept that this object is answering
     */
    @ManyToOne
    @JoinColumn(name = "concept_id", nullable = false)
    var concept: Concept? = null

    /**
     * The answer to the question
     */
    @ManyToOne
    @JoinColumn(name = "answer_concept", nullable = false)
    var answerConcept: Concept? = null

    /**
     * The [Drug] answer to the question. This can be null if this does not represent a drug
     * type of answer
     */
    @ManyToOne
    @JoinColumn(name = "answer_drug")
    var answerDrug: Drug? = null

    @ManyToOne
    @JoinColumn(name = "creator", nullable = false)
    override var creator: User? = null

    @Column(name = "date_created", nullable = false)
    override var dateCreated: Date? = null

    @Column(name = "sort_weight")
    var sortWeight: Double? = null

    constructor(conceptAnswerId: Int?) : this() {
        this.conceptAnswerId = conceptAnswerId
    }

    constructor(answerConcept: Concept?) : this() {
        this.answerConcept = answerConcept
    }

    constructor(answerConcept: Concept?, d: Drug?) : this() {
        this.answerConcept = answerConcept
        this.answerDrug = d
    }

    override var id: Integer?
        get() = conceptAnswerId?.let { Integer(it) }
        set(value) { conceptAnswerId = value?.toInt() }

    /**
     * Not currently used. Always returns null.
     *
     * @see Auditable.getChangedBy
     */
    override var changedBy: User?
        get() = null
        set(_) {}

    /**
     * Not currently used. Always returns null.
     *
     * @see Auditable.getDateChanged
     */
    override var dateChanged: Date?
        get() = null
        set(_) {}

    /**
     * @see Comparable.compareTo
     * Note: this comparator imposes orderings that are inconsistent with equals.
     */
    @Suppress("squid:S1210")
    override fun compareTo(other: ConceptAnswer): Int {
        val thisSortWeight = sortWeight
        val otherSortWeight = other.sortWeight

        return when {
            thisSortWeight == null && otherSortWeight != null -> -1
            thisSortWeight != null && otherSortWeight == null -> 1
            thisSortWeight == null && otherSortWeight == null -> 0
            else -> thisSortWeight!!.compareTo(otherSortWeight!!)
        }
    }
}

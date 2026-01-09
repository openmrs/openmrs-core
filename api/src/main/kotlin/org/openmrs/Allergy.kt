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

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.apache.commons.lang3.StringUtils
import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import org.hibernate.envers.Audited
import org.openmrs.util.OpenmrsUtil
import java.util.Date
import java.util.UUID

/**
 * Represent allergy
 */
@Entity
@Table(name = "allergy")
@Audited
class Allergy() : BaseFormRecordableOpenmrsData() {

    companion object {
        const val serialVersionUID: Long = 1L
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allergy_id")
    var allergyId: Int? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id")
    var patient: Patient? = null

    @Embedded
    var allergen: Allergen = Allergen()

    @ManyToOne
    @JoinColumn(name = "severity_concept_id")
    var severity: Concept? = null

    @Column(name = "comments", length = 1024)
    var comments: String? = null

    @OneToMany(mappedBy = "allergy", cascade = [CascadeType.ALL], orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    var reactions: MutableList<AllergyReaction> = ArrayList()
        set(value) {
            // we do not allow to be in a state where reactions is null
            field.clear()
            field.addAll(value)
        }

    @ManyToOne(optional = true)
    @JoinColumn(name = "encounter_id")
    var encounter: Encounter? = null

    constructor(
        patient: Patient?,
        allergen: Allergen,
        severity: Concept?,
        comments: String?,
        reactions: List<AllergyReaction>?
    ) : this() {
        this.patient = patient
        this.allergen = allergen
        this.severity = severity
        this.comments = comments
        reactions?.let { this.reactions.addAll(it) }
    }

    override var id: Integer?
        get() = allergyId?.let { Integer(it) }
        set(value) { allergyId = value?.toInt() }

    /**
     * @return the allergyType
     */
    var allergenType: AllergenType?
        get() = allergen.allergenType
        set(value) { allergen.allergenType = value }

    /**
     * set the allergyType of the Allergy. Here the allergy type will be chosen from the enum values in the [AllergenType], according to the given String type.
     * @param type the allergyType to set
     */
    fun setAllergenType(type: String?) {
        allergen.allergenType = if (StringUtils.isBlank(type)) null else AllergenType.valueOf(type!!)
    }

    /**
     * @return Returns the comment
     * @deprecated as of 2.3.0, replaced by [comments]
     */
    @Deprecated("Use comments property", ReplaceWith("comments"))
    var comment: String?
        get() = comments
        set(value) { comments = value }

    /**
     * Adds a new allergy reaction
     *
     * @param reaction the reaction to add
     * @return true if the reaction was added, else false
     */
    fun addReaction(reaction: AllergyReaction): Boolean {
        if (reactionConcepts.contains(reaction.reaction)) {
            return false
        }
        reaction.allergy = this
        return reactions.add(reaction)
    }

    /**
     * Removes an allergy reaction
     *
     * @param reaction the reaction to remove
     * @return true if the reaction was found and removed, else false.
     */
    fun removeReaction(reaction: AllergyReaction): Boolean = reactions.remove(reaction)

    val dateLastUpdated: Date?
        get() = dateChanged ?: dateCreated

    /**
     * Checks if this allergy has the same values as a given one.
     *
     * @param allergy the allergy whose values to compare with
     * @return true if the values match, else false
     */
    fun hasSameValues(allergy: Allergy): Boolean {
        if (!OpenmrsUtil.nullSafeEquals(allergyId, allergy.allergyId)) {
            return false
        }
        if (!OpenmrsUtil.nullSafeEquals(patient, allergy.patient)) {
            // if object instances are different but with the same patient id, then not changed
            if (patient != null && allergy.patient != null) {
                if (!OpenmrsUtil.nullSafeEquals(patient?.id, allergy.patient?.id)) {
                    return false
                }
            } else {
                return false
            }
        }
        if (!OpenmrsUtil.nullSafeEquals(allergen.codedAllergen, allergy.allergen.codedAllergen)) {
            // if object instances are different but with the same concept id, then not changed
            if (allergen.codedAllergen != null && allergy.allergen.codedAllergen != null) {
                if (!OpenmrsUtil.nullSafeEquals(allergen.codedAllergen?.id, allergy.allergen.codedAllergen?.id)) {
                    return false
                }
            } else {
                return false
            }
        }
        if (!OpenmrsUtil.nullSafeEquals(allergen.nonCodedAllergen, allergy.allergen.nonCodedAllergen)) {
            return false
        }
        if (!OpenmrsUtil.nullSafeEquals(severity, allergy.severity)) {
            // if object instances are different but with the same concept id, then not changed
            if (severity != null && allergy.severity != null) {
                if (!OpenmrsUtil.nullSafeEquals(severity?.id, allergy.severity?.id)) {
                    return false
                }
            } else {
                return false
            }
        }
        if (!OpenmrsUtil.nullSafeEquals(comment, allergy.comment)) {
            return false
        }
        return hasSameReactions(allergy)
    }

    /**
     * Checks if this allergy has the same reaction values as those in the given one
     *
     * @param allergy the allergy who reaction values to compare with
     * @return true if the values match, else false
     */
    private fun hasSameReactions(allergy: Allergy): Boolean {
        if (reactions.size != allergy.reactions.size) {
            return false
        }
        for (reaction in reactions) {
            val rc = allergy.getAllergyReaction(reaction.allergyReactionId)
            if (!reaction.hasSameValues(rc)) {
                return false
            }
        }
        return true
    }

    /**
     * Gets an allergy reaction with a given id
     *
     * @param allergyReactionId the allergy reaction id
     * @return the allergy reaction with a matching id
     */
    fun getAllergyReaction(allergyReactionId: Int?): AllergyReaction? =
        reactions.firstOrNull { OpenmrsUtil.nullSafeEquals(it.allergyReactionId, allergyReactionId) }

    /**
     * Copies all property values, apart from the id and uuid,
     * from the given allergy into this object
     *
     * @param allergy the allergy whose property values to copy
     */
    fun copy(allergy: Allergy) {
        allergyId = null
        uuid = UUID.randomUUID().toString()
        patient = allergy.patient
        allergen = allergy.allergen
        severity = allergy.severity
        comment = allergy.comment
        reactions = ArrayList()

        for (reaction in allergy.reactions) {
            reactions.add(reaction)
            reaction.allergyReactionId = null
            reaction.uuid = UUID.randomUUID().toString()
        }
    }

    private val reactionConcepts: List<Concept>
        get() = reactions.mapNotNull { it.reaction }

    /**
     * @return Returns the reactionNonCoded
     */
    val reactionNonCoded: String?
        get() = reactions.firstOrNull { StringUtils.isNotBlank(it.reactionNonCoded) }?.reactionNonCoded

    /**
     * Gets the reaction with a given concept
     *
     * @param concept the concept
     * @return the reaction if any exists
     */
    fun getReaction(concept: Concept): AllergyReaction? =
        reactions.firstOrNull { it.reaction == concept }

    /**
     * Checks if we have the same allergen as that in the given allergy
     *
     * @param allergy the given allergy whose allergen to check
     * @return true if the same, else false
     */
    fun hasSameAllergen(allergy: Allergy): Boolean {
        if (allergen == null || allergy.allergen == null) {
            return false
        }
        return allergen.isSameAllergen(allergy.allergen)
    }
}

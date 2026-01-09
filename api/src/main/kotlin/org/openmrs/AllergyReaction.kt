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
import org.apache.commons.lang3.StringUtils
import org.hibernate.envers.Audited
import org.openmrs.util.OpenmrsUtil
import java.io.Serializable

/**
 * Represent allergy reactions
 */
@Audited
@Entity
@Table(name = "allergy_reaction")
class AllergyReaction() : BaseOpenmrsObject(), Serializable {

    companion object {
        const val serialVersionUID: Long = 1L
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allergy_reaction_id")
    var allergyReactionId: Int? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "allergy_id", nullable = false)
    var allergy: Allergy? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "reaction_concept_id", nullable = false)
    var reaction: Concept? = null

    @Column(name = "reaction_non_coded")
    var reactionNonCoded: String? = null

    constructor(allergy: Allergy?, reaction: Concept?, reactionNonCoded: String?) : this() {
        this.allergy = allergy
        this.reaction = reaction
        this.reactionNonCoded = reactionNonCoded
    }

    override var id: Integer?
        get() = allergyReactionId?.let { Integer(it) }
        set(value) { allergyReactionId = value?.toInt() }

    override fun toString(): String =
        if (StringUtils.isNotBlank(reactionNonCoded)) {
            reactionNonCoded!!
        } else {
            reaction?.getName()?.name ?: ""
        }

    /**
     * Checks if this reaction has the same values as the given one
     *
     * @param reaction the reaction whose values to compare with
     * @return true if the values match, else false
     */
    fun hasSameValues(reaction: AllergyReaction?): Boolean {
        if (!OpenmrsUtil.nullSafeEquals(allergyReactionId, reaction?.allergyReactionId)) {
            return false
        }
        if (!OpenmrsUtil.nullSafeEquals(this.reaction, reaction?.reaction)) {
            // if object instances are different but with the same concept id, then not changed
            if (this.reaction != null && reaction?.reaction != null) {
                if (!OpenmrsUtil.nullSafeEquals(this.reaction?.id, reaction.reaction?.id)) {
                    return false
                }
            } else {
                return false
            }
        }
        return OpenmrsUtil.nullSafeEquals(reactionNonCoded, reaction?.reactionNonCoded)
    }
}

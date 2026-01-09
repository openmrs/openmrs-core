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
import org.openmrs.util.OpenmrsConstants
import java.util.Date

/**
 * A ConceptProposal is a temporary holder for concept that should be in the system. When defining
 * an observation, a user can "propose" a new concept if one isn't found already. The proposal is a
 * simple text entry that will be reviewed later. When a proposal is (edited and) accepted, the
 * encounter that prompted this proposal is updated with a new observation pointing at the new (or
 * edited) concept.
 */
@Audited
class ConceptProposal() : BaseOpenmrsObject() {

    companion object {
        const val serialVersionUID: Long = 57344L
    }

    var conceptProposalId: Int? = null

    var encounter: Encounter? = null

    var obsConcept: Concept? = null

    var obs: Obs? = null

    var mappedConcept: Concept? = null

    var originalText: String? = null

    var finalText: String? = null

    var state: String? = null

    var comments: String? = null

    var creator: User? = null

    var dateCreated: Date? = null

    var changedBy: User? = null

    var dateChanged: Date? = null

    constructor(conceptProposalId: Int?) : this() {
        this.conceptProposalId = conceptProposalId
    }

    override var id: Integer?
        get() = conceptProposalId?.let { Integer(it) }
        set(value) { conceptProposalId = value?.toInt() }

    override fun toString(): String = conceptProposalId?.toString() ?: ""

    /**
     * Convenience method to mark this proposal as rejected. Be sure to call
     * Context.getConceptService().saveConceptProposal(/thisObject/) after calling this method
     */
    fun rejectConceptProposal() {
        state = OpenmrsConstants.CONCEPT_PROPOSAL_REJECT
        finalText = ""
    }
}

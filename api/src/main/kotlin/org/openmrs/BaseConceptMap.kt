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
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import java.util.Date

/**
 * Superclass for [ConceptMap]s and [ConceptReferenceTermMap]s
 *
 * @since 1.9
 */
@MappedSuperclass
abstract class BaseConceptMap : BaseOpenmrsObject(), Auditable {

    @ManyToOne(optional = false)
    @JoinColumn(name = "a_is_to_b_id", nullable = false)
    var conceptMapType: ConceptMapType? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "creator", nullable = false)
    override var creator: User? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    override var changedBy: User? = null

    @Column(name = "date_created", nullable = false)
    private var _dateCreated: Date? = null

    @Column(name = "date_changed")
    private var _dateChanged: Date? = null

    override var dateCreated: Date?
        get() = _dateCreated?.let { it.clone() as Date }
        set(value) {
            _dateCreated = value?.let { Date(it.time) }
        }

    override var dateChanged: Date?
        get() = _dateChanged?.let { it.clone() as Date }
        set(value) {
            _dateChanged = value?.let { Date(it.time) }
        }
}

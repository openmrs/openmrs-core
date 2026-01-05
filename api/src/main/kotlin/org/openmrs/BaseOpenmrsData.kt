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
import org.codehaus.jackson.annotate.JsonIgnore
import org.hibernate.envers.Audited
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField
import java.util.Date

/**
 * In OpenMRS, we distinguish between data and metadata within our data model. Data (as opposed to
 * metadata) generally represent person- or patient-specific data. This provides a default abstract
 * implementation of the OpenmrsData interface.
 *
 * @since 1.5
 * @see OpenmrsData
 */
@MappedSuperclass
@Audited
abstract class BaseOpenmrsData : BaseOpenmrsObject(), OpenmrsData {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "creator", updatable = false)
    override var creator: User? = null

    @Column(name = "date_created", nullable = false, updatable = false)
    override var dateCreated: Date? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    @Deprecated("as of version 2.2")
    override var changedBy: User? = null

    @Column(name = "date_changed")
    @Deprecated("as of version 2.2")
    override var dateChanged: Date? = null

    @Column(name = "voided", nullable = false)
    @GenericField
    override var voided: Boolean? = false

    @Column(name = "date_voided")
    override var dateVoided: Date? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voided_by")
    override var voidedBy: User? = null

    @Column(name = "void_reason", length = 255)
    override var voidReason: String? = null

    /**
     * @deprecated as of 2.0, use [voided]
     */
    @get:JsonIgnore
    @Deprecated("as of 2.0, use voided property", ReplaceWith("voided"))
    override val isVoided: Boolean?
        get() = voided
}

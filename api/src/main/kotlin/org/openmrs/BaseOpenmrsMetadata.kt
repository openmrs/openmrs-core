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
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField
import org.openmrs.api.db.hibernate.search.SearchAnalysis
import java.util.Date

/**
 * In OpenMRS, we distinguish between data and metadata within our data model. Metadata represent
 * system and descriptive data such as data types - a relationship type or encounter type.
 * Metadata are generally referenced by clinical data but don't represent patient-specific data
 * themselves. This provides a default abstract implementation of the OpenmrsMetadata interface.
 *
 * @since 1.5
 * @see OpenmrsMetadata
 */
@MappedSuperclass
@Audited
abstract class BaseOpenmrsMetadata : BaseOpenmrsObject(), OpenmrsMetadata {

    @Column(name = "name", nullable = false, length = 255)
    @FullTextField(analyzer = SearchAnalysis.NAME_ANALYZER)
    override var name: String? = null

    @Column(name = "description", length = 255)
    override var description: String? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "creator")
    override var creator: User? = null

    @Column(name = "date_created", nullable = false)
    override var dateCreated: Date? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    @Deprecated("as of version 2.2")
    override var changedBy: User? = null

    @Column(name = "date_changed")
    @Deprecated("as of version 2.2")
    override var dateChanged: Date? = null

    @Column(name = "retired", nullable = false)
    @GenericField
    override var retired: Boolean? = false

    @Column(name = "date_retired")
    override var dateRetired: Date? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retired_by")
    override var retiredBy: User? = null

    @Column(name = "retire_reason", length = 255)
    override var retireReason: String? = null

    /**
     * @deprecated as of 2.0, use [retired]
     */
    @get:JsonIgnore
    @Deprecated("as of 2.0, use retired property", ReplaceWith("retired"))
    override val isRetired: Boolean?
        get() = retired
}

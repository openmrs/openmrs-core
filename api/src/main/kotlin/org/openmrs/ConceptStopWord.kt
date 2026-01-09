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
import org.openmrs.api.context.Context
import org.springframework.util.StringUtils
import java.util.Locale

/**
 * ConceptStopWord is the real world term used to filter the words for indexing
 * from search phrase. Common words like 'and', 'if' are examples of this. It's
 * specific to locale.
 *
 * @since 1.8
 */
@Audited
class ConceptStopWord() : BaseOpenmrsObject() {

    companion object {
        private const val serialVersionUID: Long = 3671020002642184656L
    }

    var conceptStopWordId: Int? = null

    var value: String? = null
        set(value) {
            field = if (StringUtils.hasText(value)) value?.uppercase() else value
        }

    var locale: Locale? = null
        set(value) {
            field = value ?: Context.getLocale()
        }

    /**
     * Convenience constructor to create a ConceptStopWord object with default
     * locale English
     *
     * @param value
     */
    constructor(value: String?) : this(value, Context.getLocale())

    /**
     * Convenience constructor to create a ConceptStopWord object with value and
     * locale
     *
     * @param value
     * @param locale
     */
    constructor(value: String?, locale: Locale?) : this() {
        this.value = value
        this.locale = locale
    }

    override var id: Integer?
        get() = conceptStopWordId?.let { Integer(it) }
        set(value) { conceptStopWordId = value?.toInt() }

    override fun toString(): String = "ConceptStopWord: $value, Locale: $locale"
}

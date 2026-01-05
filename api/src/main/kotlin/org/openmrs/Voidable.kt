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
import java.util.Date

/**
 * In OpenMRS, data are rarely fully deleted (purged) from the system; rather, they are either
 * voided or retired. When data can be removed (effectively deleted from the user's perspective),
 * then they are voidable. Voided data are no longer valid and references from other non-voided data
 * are not valid. For example, when duplicate patient records are merged, the record that is not
 * kept is voided (invalidated). Unlike [Retireable], voiding data invalidates any data
 * referencing them. e.g., when a patient is voided, all observations for that patient must be
 * voided as well.
 *
 * @since 1.5
 * @see OpenmrsData
 * @see Retireable
 */
interface Voidable : OpenmrsObject {

    /**
     * Whether or not this object is voided.
     *
     * @deprecated as of 2.0, use [voided]
     */
    @get:JsonIgnore
    @Deprecated("as of 2.0, use voided property", ReplaceWith("voided"))
    val isVoided: Boolean?
        get() = voided

    /**
     * Whether or not this object is voided.
     */
    var voided: Boolean?

    /**
     * The user who voided the object.
     */
    var voidedBy: User?

    /**
     * The date the object was voided.
     */
    var dateVoided: Date?

    /**
     * The reason the object was voided.
     */
    var voidReason: String?
}

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
import org.openmrs.attribute.Attribute
import org.openmrs.attribute.BaseAttribute

/**
 * A value for a user-defined [VisitAttributeType] that is stored on a [Visit].
 * @see Attribute
 * @since 1.9
 */
@Audited
class VisitAttribute : BaseAttribute<VisitAttributeType, Visit>(), Attribute<VisitAttributeType, Visit> {

    var visitAttributeId: Int? = null

    // BaseAttribute<Visit> has an "owner" property of type Visit, which we re-expose as "visit"

    /**
     * @return the visit
     */
    var visit: Visit?
        get() = owner
        set(value) {
            owner = value
        }

    /**
     * @see org.openmrs.OpenmrsObject.getId
     */
    override var id: Int?
        get() = visitAttributeId
        set(value) {
            visitAttributeId = value
        }
}

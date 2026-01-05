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

import java.util.Date

/**
 * In OpenMRS, we distinguish between data and metadata within our data model. Metadata represent
 * system and descriptive data such as data types - a relationship type or encounter type.
 * Metadata are generally referenced by clinical data but don't represent patient-specific data
 * themselves. OpenMRS objects that represent metadata should implement this interface.
 *
 * @see OpenmrsData
 * @see BaseChangeableOpenmrsMetadata
 * @since 1.5
 */
interface OpenmrsMetadata : Auditable, Retireable {

    /**
     * The name of this metadata object.
     */
    var name: String?

    /**
     * The description of this metadata object.
     */
    var description: String?

    /**
     * @deprecated As of version 2.2 OpenmrsMetadata is immutable by default, it's up to the
     *             subclasses to make themselves mutable by extending BaseChangeableOpenmrsMetadata,
     *             this method will be removed in 2.3
     */
    @Deprecated("As of version 2.2 OpenmrsMetadata is immutable by default")
    override var changedBy: User?

    /**
     * @deprecated As of version 2.2 OpenmrsMetadata is immutable by default, it's up to the
     *             subclasses to make themselves mutable by extending BaseChangeableOpenmrsMetadata,
     *             this method will be removed in 2.3
     */
    @Deprecated("As of version 2.2 OpenmrsMetadata is immutable by default")
    override var dateChanged: Date?
}

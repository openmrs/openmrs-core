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
 * In OpenMRS, we distinguish between data and metadata within our data model. Data (as opposed to
 * metadata) generally represent person- or patient-specific data. OpenMRS objects that represent
 * person- or patient-specific data should implement this interface.
 *
 * @see OpenmrsMetadata
 * @see BaseChangeableOpenmrsData
 * @since 1.5
 */
interface OpenmrsData : Auditable, Voidable {

    /**
     * @deprecated As of version 2.2, OpenmrsData is immutable by default, it's up to the subclasses
     *             to make themselves mutable by extending BaseChangeableOpenmrsData, this method
     *             will be removed in 2.3
     */
    @Deprecated("As of version 2.2, OpenmrsData is immutable by default")
    override var changedBy: User?

    /**
     * @deprecated As of version 2.2, OpenmrsData is immutable by default, it's up to the subclasses
     *             to make themselves mutable by extending BaseChangeableOpenmrsData, this method
     *             will be removed in 2.3
     */
    @Deprecated("As of version 2.2, OpenmrsData is immutable by default")
    override var dateChanged: Date?
}

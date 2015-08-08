/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.attribute;

import org.openmrs.OpenmrsMetadata;
import org.openmrs.customdatatype.Customizable;
import org.openmrs.customdatatype.RepeatingCustomValueDescriptor;

/**
 * Common interface for user-defined extensions to core domain objects, which would be handled by adding
 * custom database columns in a less generic system. 
 * For example Visit has VisitAttributes that are defined by VisitAttributeTypes (that implement
 * AttributeType&lt;Visit&gt;).
 * @see Attribute
 * @see Customizable
 * @since 1.9
 */
public interface AttributeType<OwningType extends Customizable<?>> extends RepeatingCustomValueDescriptor, OpenmrsMetadata {

}

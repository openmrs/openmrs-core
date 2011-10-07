/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.attribute;

import org.openmrs.OpenmrsMetadata;
import org.openmrs.customdatatype.Customizable;
import org.openmrs.customdatatype.RepeatingCustomValueDescriptor;

/**
 * Common interface for user-defined extensions to core domain objects, which would be handled by adding
 * custom database columns in a less generic system. 
 * For example Visit has VisitAttributes that are defined by VisitAttributeTypes (that implement
 * AttributeType<Visit>).
 * @see Attribute
 * @see Customizable
 * @see AttributeHandler
 * @since 1.9
 */
public interface AttributeType<OwningType extends Customizable<?>> extends RepeatingCustomValueDescriptor, OpenmrsMetadata {

}

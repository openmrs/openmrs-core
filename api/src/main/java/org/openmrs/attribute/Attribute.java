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

import org.openmrs.OpenmrsData;
import org.openmrs.customdatatype.Customizable;
import org.openmrs.customdatatype.SingleCustomValue;

/**
 * Common interface for user-defined attribute that is are added to a base class.
 * Every attribute corresponds to a {@link AttributeType}, which defines, among other things, whether it
 * is required, whether it may repeat, and how it is serialized and deserialized for storage.
 * These attribute types are intended for use cases that would involve adding custom columns to the base
 * table in a less generic system. 
 * For example Visit has VisitAttributes (which implements Attribute<Visit>) that are defined by
 * VisitAttributeTypes.
 * @param <AT> the AttributeType for this Attribute
 * @param <OT> the type this attribute can belong to
 * @see AttributeType
 * @see Customizable
 * @see AttributeHandler
 * @since 1.9
 */
public interface Attribute<AT extends AttributeType, OT extends Customizable<?>> extends OpenmrsData, SingleCustomValue<AT> {
	
	/**
	 * @return the owner that this attribute belongs to
	 */
	OT getOwner();
	
	/**
	 * Sets the owner of this attribute
	 * 
	 * @param owner
	 */
	void setOwner(OT owner);
	
	/**
	 * @return the AttributeType that controls this attribute's behavior
	 */
	AT getAttributeType();
	
}

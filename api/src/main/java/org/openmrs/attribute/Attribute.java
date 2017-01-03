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

import org.openmrs.OpenmrsData;
import org.openmrs.customdatatype.Customizable;
import org.openmrs.customdatatype.SingleCustomValue;

/**
 * Common interface for user-defined attribute that is are added to a base class.
 * Every attribute corresponds to a {@link AttributeType}, which defines, among other things, whether it
 * is required, whether it may repeat, and how it is serialized and deserialized for storage.
 * These attribute types are intended for use cases that would involve adding custom columns to the base
 * table in a less generic system. 
 * For example Visit has VisitAttributes (which implements Attribute&lt;Visit&gt;) that are defined by
 * VisitAttributeTypes.
 * @param <AT> the AttributeType for this Attribute
 * @param <OT> the type this attribute can belong to
 * @see AttributeType
 * @see Customizable
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
	 * @return the AttributeType that controls the behaviour of this attribute
	 */
	AT getAttributeType();
	
}

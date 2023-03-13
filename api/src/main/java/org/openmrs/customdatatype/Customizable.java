/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.customdatatype;

import java.util.Collection;
import java.util.List;

import org.openmrs.attribute.Attribute;

/**
 * Marker interface for classes that may be customized by the user by adding custom attributes, e.g. Visit
 * has VisitAttributes, so it implements {@link Customizable}&lt;VisitAttribute&gt;
 * @param <A> the type of attribute held
 * @since 1.9
 */
@SuppressWarnings("rawtypes")
public interface Customizable<A extends Attribute> {
	
	/**
	 * @return all attributes (including voided ones)
	 */
	Collection<A> getAttributes();
	
	/**
	 * @return non-voided attributes
	 */
	Collection<A> getActiveAttributes();
	
	/**
	 * @param ofType
	 * @return non-voided attributes of the given type
	 */
	List<A> getActiveAttributes(CustomValueDescriptor ofType);
	
	/**
	 * Adds an attribute.
	 * 
	 * @param attribute
	 */
	void addAttribute(A attribute);
	
}

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
package org.openmrs.customdatatype;

import java.util.Collection;
import java.util.List;

import org.openmrs.attribute.Attribute;

/**
 * Marker interface for classes that may be customized by the user by adding custom attributes, e.g. Visit
 * has VisitAttributes, so it implements {@link Customizable}<VisitAttribute>
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

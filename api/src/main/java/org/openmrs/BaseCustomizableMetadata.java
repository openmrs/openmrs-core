/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.openmrs.attribute.Attribute;
import org.openmrs.customdatatype.Customizable;

/**
 * Extension of {@link org.openmrs.BaseOpenmrsMetadata} for classes that support customization via
 * user-defined attributes.
 * 
 * @param <A> the type of attribute held
 * @since 1.9
 */
public abstract class BaseCustomizableMetadata<A extends Attribute> extends BaseOpenmrsMetadata implements Customizable<A> {
	
	private Collection<A> attributes = new LinkedHashSet<A>();
	
	/**
	 * @see org.openmrs.customdatatype.Customizable#getAttributes()
	 */
	@Override
	public Collection<A> getAttributes() {
		return attributes;
	}
	
	/**
	 * @see org.openmrs.customdatatype.Customizable#setAttributes()
	 */
	@Override
	public void setAttributes(Collection<A> attributes) {
		this.attributes = attributes;
	}
	
	/**
	 * @see org.openmrs.customdatatype.Customizable#getActiveAttributes()
	 * @hint has do be passed through, otherwise BeanWrapper.getPropertyValue() throws
	 *       NotReadablePropertyException
	 */
	@Override
	public Collection<A> getActiveAttributes() {
		return Customizable.super.getActiveAttributes();
	}
}

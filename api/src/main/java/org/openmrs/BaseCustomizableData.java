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
package org.openmrs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.Customizable;
import org.openmrs.attribute.AttributeType;

/**
 * Extension of {@link BaseOpenmrsData} for classes that support customization via user-defined attributes.
 * @param <AttrClass> the type of attribute held
 * @since 1.9
 */
public abstract class BaseCustomizableData<AttrClass extends Attribute> extends BaseOpenmrsData implements Customizable<AttrClass> {
	
	private Set<AttrClass> attributes;
	
	/**
	 * @see org.openmrs.attribute.Customizable#getAttributes()
	 */
	@Override
	public Set<AttrClass> getAttributes() {
		return attributes;
	}
	
	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(Set<AttrClass> attributes) {
		this.attributes = attributes;
	}
	
	/**
	 * @see org.openmrs.attribute.Customizable#getActiveAttributes()
	 */
	@Override
	public List<AttrClass> getActiveAttributes() {
		List<AttrClass> ret = new ArrayList<AttrClass>();
		if (getAttributes() != null)
			for (AttrClass attr : getAttributes())
				if (!attr.isVoided())
					ret.add(attr);
		return ret;
	}
	
	/**
	 * @see org.openmrs.attribute.Customizable#getActiveAttributes(org.openmrs.attribute.AttributeType)
	 */
	@Override
	public List<AttrClass> getActiveAttributes(AttributeType<?> ofType) {
		List<AttrClass> ret = getActiveAttributes();
		for (Iterator<AttrClass> i = ret.iterator(); i.hasNext();)
			if (!(i.next().getAttributeType().equals(ofType)))
				i.remove();
		return ret;
	}
	
	/**
	 * @see org.openmrs.attribute.Customizable#addAttribute(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void addAttribute(AttrClass attribute) {
		if (getAttributes() == null)
			setAttributes(new HashSet<AttrClass>());
		// TODO validate
		getAttributes().add(attribute);
		attribute.setOwner(this);
	}
	
	/**
	 * @see org.openmrs.attribute.Customizable#setAttribute(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setAttribute(AttrClass attribute) {
		if (getAttributes() == null)
			setAttributes(new HashSet<AttrClass>());
		// TODO validate
		for (AttrClass existing : getAttributes())
			if (existing.getAttributeType().equals(attribute.getAttributeType()))
				existing.setVoided(true);
		getAttributes().add(attribute);
		attribute.setOwner(this);
	}
	
}

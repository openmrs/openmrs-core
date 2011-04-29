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
 * @param <AttrType> the type of attribute held
 */
public abstract class BaseCustomizableData<AttrType extends Attribute> extends BaseOpenmrsData implements Customizable<AttrType> {
	
	private Set<AttrType> attributes;
	
	/**
	 * @see org.openmrs.attribute.Customizable#getAttributes()
	 */
	@Override
	public Set<AttrType> getAttributes() {
		return attributes;
	}
	
	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(Set<AttrType> attributes) {
		this.attributes = attributes;
	}
	
	/**
	 * @see org.openmrs.attribute.Customizable#getActiveAttributes()
	 */
	@Override
	public List<AttrType> getActiveAttributes() {
		List<AttrType> ret = new ArrayList<AttrType>();
		if (getAttributes() != null)
			for (AttrType attr : getAttributes())
				if (!attr.isVoided())
					ret.add(attr);
		return ret;
	}
	
	/**
	 * @see org.openmrs.attribute.Customizable#getActiveAttributes(org.openmrs.attribute.AttributeType)
	 */
	@Override
	public List<AttrType> getActiveAttributes(AttributeType<?> ofType) {
		List<AttrType> ret = getActiveAttributes();
		for (Iterator<AttrType> i = ret.iterator(); i.hasNext();)
			if (!(i.next().getAttributeType().equals(ofType)))
				i.remove();
		return ret;
	}
	
	/**
	 * @see org.openmrs.attribute.Customizable#addAttribute(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void addAttribute(AttrType attribute) {
		if (getAttributes() == null)
			setAttributes(new HashSet<AttrType>());
		// TODO validate
		getAttributes().add(attribute);
		attribute.setOwner(this);
	}
	
	/**
	 * @see org.openmrs.attribute.Customizable#setAttribute(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setAttribute(AttrType attribute) {
		if (getAttributes() == null)
			setAttributes(new HashSet<AttrType>());
		// TODO validate
		for (AttrType existing : getAttributes())
			if (existing.getAttributeType().equals(attribute.getAttributeType()))
				existing.setVoided(true);
		getAttributes().add(attribute);
		attribute.setOwner(this);
	}
	
}

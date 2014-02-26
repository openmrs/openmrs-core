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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.attribute.Attribute;
import org.openmrs.customdatatype.CustomValueDescriptor;
import org.openmrs.customdatatype.Customizable;

/**
 * Extension of {@link BaseOpenmrsData} for classes that support customization via user-defined attributes.
 * @param <AttrClass> the type of attribute held
 * @since 1.9
 */
public abstract class BaseCustomizableData<A extends Attribute> extends BaseOpenmrsData implements Customizable<A> {
	
	private Set<A> attributes = new LinkedHashSet<A>();
	
	/**
	 * @see org.openmrs.customdatatype.Customizable#getAttributes()
	 */
	@Override
	public Set<A> getAttributes() {
		return attributes;
	}
	
	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(Set<A> attributes) {
		this.attributes = attributes;
	}
	
	/**
	 * @see org.openmrs.customdatatype.Customizable#getActiveAttributes()
	 */
	@Override
	public Collection<A> getActiveAttributes() {
		List<A> ret = new ArrayList<A>();
		if (getAttributes() != null) {
			for (A attr : getAttributes()) {
				if (!attr.isVoided()) {
					ret.add(attr);
				}
			}
		}
		return ret;
	}
	
	/**
	 * @see org.openmrs.customdatatype.Customizable#getActiveAttributes(org.openmrs.customdatatype.CustomValueDescriptor)
	 */
	@Override
	public List<A> getActiveAttributes(CustomValueDescriptor ofType) {
		List<A> ret = new ArrayList<A>();
		if (getAttributes() != null) {
			for (A attr : getAttributes()) {
				if (attr.getAttributeType().equals(ofType) && !attr.isVoided()) {
					ret.add(attr);
				}
			}
		}
		return ret;
	}
	
	/**
	 * @see org.openmrs.customdatatype.Customizable#addAttribute(org.openmrs.customdatatype.SingleCustomValue)
	 */
	@Override
	public void addAttribute(A attribute) {
		if (getAttributes() == null) {
			setAttributes(new LinkedHashSet<A>());
		}
		// TODO validate
		getAttributes().add(attribute);
		attribute.setOwner(this);
	}
	
	/**
	 * Convenience method that voids all existing attributes of the given type, and sets this new one.
	 * TODO fail if minOccurs > 1
	 * TODO decide whether this should require maxOccurs=1
	 * @should void the attribute if an attribute with same attribute type already exists and the maxOccurs is set to 1
	 *
	 * @param attribute
	 */
	@SuppressWarnings("unchecked")
	public void setAttribute(A attribute) {
		if (getAttributes() == null) {
			addAttribute(attribute);
			return;
		}
		
		if (getActiveAttributes(attribute.getAttributeType()).size() == 1) {
			A existing = getActiveAttributes(attribute.getAttributeType()).get(0);
			if (existing.getValue().equals(attribute.getValue())) {
				// do nothing, since the value is already as-specified
			} else {
				if (existing.getId() != null) {
					existing.setVoided(true);
				} else {
					getAttributes().remove(existing);
				}
				getAttributes().add(attribute);
				attribute.setOwner(this);
			}
			
		} else {
			for (A existing : getActiveAttributes(attribute.getAttributeType())) {
				if (existing.getAttributeType().equals(attribute.getAttributeType())) {
					if (existing.getId() != null) {
						existing.setVoided(true);
					} else {
						getAttributes().remove(existing);
					}
				}
			}
			getAttributes().add(attribute);
			attribute.setOwner(this);
		}
	}
	
}

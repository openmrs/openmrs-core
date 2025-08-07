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

import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.openmrs.attribute.Attribute;
import org.openmrs.customdatatype.CustomValueDescriptor;
import org.openmrs.customdatatype.Customizable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Extension of {@link org.openmrs.BaseOpenmrsMetadata} for classes that support customization via user-defined attributes.
 * @param <A> the type of attribute held
 * @since 1.9
 */
@MappedSuperclass
@Audited
public abstract class BaseCustomizableMetadata<A extends Attribute> extends BaseChangeableOpenmrsMetadata implements Customizable<A> {
	
	@OrderBy("voided asc")
	@BatchSize(size = 100)
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "location_id")
	private Set<A> attributes = new LinkedHashSet<>();
	
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
		List<A> ret = new ArrayList<>();
		if (getAttributes() != null) {
			for (A attr : getAttributes()) {
				if (!attr.getVoided()) {
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
		List<A> ret = new ArrayList<>();
		if (getAttributes() != null) {
			for (A attr : getAttributes()) {
				if (attr.getAttributeType().equals(ofType) && !attr.getVoided()) {
					ret.add(attr);
				}
			}
		}
		return ret;
	}
	
	/**
	 * @see org.openmrs.customdatatype.Customizable#addAttribute(Attribute)
	 */
	@Override
	public void addAttribute(A attribute) {
		if (getAttributes() == null) {
			setAttributes(new LinkedHashSet<>());
		}
		getAttributes().add(attribute);
		attribute.setOwner(this);
	}
	
	/**
	 * Convenience method that voids all existing attributes of the given type, and sets this new one.
	 * <strong>Should</strong> void the attribute if an attribute with same attribute type already exists and the maxOccurs is set to 1
	 * <strong>Should</strong> work for attributes with datatypes whose values are stored in other tables
	 *
	 * @param attribute
	 */
	public void setAttribute(A attribute) {
		if (getAttributes() == null) {
			addAttribute(attribute);
			return;
		}
		
		if (getActiveAttributes(attribute.getAttributeType()).size() == 1) {
			A existing = getActiveAttributes(attribute.getAttributeType()).get(0);
			if (!existing.getValue().equals(attribute.getValue())) {
				if (existing.getId() != null) {
					existing.setVoided(true);
				} else {
					getAttributes().remove(existing);
				}
				getAttributes().add(attribute);
				attribute.setOwner(this);
			}
			return;
		}
		
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

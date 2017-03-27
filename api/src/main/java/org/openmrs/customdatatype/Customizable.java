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
import java.util.stream.Collectors;

import org.openmrs.attribute.Attribute;

/**
 * Marker interface for classes that may be customized by the user by adding custom attributes, e.g.
 * Visit has VisitAttributes, so it implements {@link Customizable}&lt;VisitAttribute&gt;
 * 
 * @param <A> the type of attribute held
 * @since 1.9
 */
@SuppressWarnings("rawtypes")
public interface Customizable<A extends Attribute> {
	
	/**
	 * @return all attributes (including voided ones), must not be null
	 */
	Collection<A> getAttributes();
	
	/**
	 * @param attributes the attributes to set
	 * @since 2.2
	 */
	void setAttributes(Collection<A> attributes);
	
	/**
	 * @return non-voided attributes
	 * @should return empty collection if attributes collection is empty
	 * @should return only non voided attributes
	 */
	public default Collection<A> getActiveAttributes() {
		return getAttributes().stream()
				.filter(attr -> !attr.getVoided())
				.collect(Collectors.toList());
	}
	
	/**
	 * @param ofType the type of the active attributes to get
	 * @return non-voided attributes of the given type
	 * @should return empty collection if attributes collection is empty
	 * @should return only non voided attributes of type
	 */
	public default List<A> getActiveAttributes(CustomValueDescriptor ofType) {
		return getAttributes().stream()
		        .filter(attr -> attr.getAttributeType().equals(ofType))
		        .filter(attr -> !attr.getVoided())
		        .collect(Collectors.toList());
	}
	
	/**
	 * Adds an attribute.
	 * 
	 * @param attribute the attribute to add
	 * @should add attribute if attributes collection is empty
	 * @should add attribute if attributes collection is not empty
	 */
	@SuppressWarnings("unchecked")
	public default void addAttribute(A attribute) {
		getAttributes().add(attribute);
		attribute.setOwner(this);
	}
	
	/**
	 * Convenience method that voids all existing attributes of the given type, and sets this new
	 * one.
	 * 
	 * @param attribute the attribute to set
	 * @should add attribute if attribute collection is null
	 * @should do nothing if has single existing attribute of same type and value
	 * @should remove existing attribute with id null and add attribute
	 * @should void the attribute if an attribute with same attribute type already exists and add
	 *         attribute
	 * @should set all existing attributes with non null id to voided and add attribute
	 * @should set all existing attributes with non null id to voided and remove attribute with null
	 *         id and add attribute
	 */
	@SuppressWarnings("unchecked")
	public default void setAttribute(A attribute) {
		if (getAttributes().isEmpty()) {
			addAttribute(attribute);
			return;
		}
		
		Collection<A> activeAttributesOfAttributeType = getActiveAttributes(attribute.getAttributeType());
		
		if (activeAttributesOfAttributeType.size() == 1) {
			A existing = activeAttributesOfAttributeType.iterator().next();
			if (existing.getValue().equals(attribute.getValue())) {
				// do nothing, since the value is already as-specified
				return;
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
			activeAttributesOfAttributeType.stream()
			        .filter(existing -> existing.getAttributeType().equals(attribute.getAttributeType()))
			        .forEach(existing -> {
				        if (existing.getId() != null) {
					        existing.setVoided(true);
				        } else {
					        getAttributes().remove(existing);
				        }
			        });
			getAttributes().add(attribute);
			attribute.setOwner(this);
		}
	}
}

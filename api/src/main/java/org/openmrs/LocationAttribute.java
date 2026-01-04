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

import org.hibernate.envers.Audited;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.AssociationOverride;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.BaseAttribute;

/**
 * A value for a user-defined {@link LocationAttributeType} that is stored on a {@link Location}.
 * @see Attribute
 * @since 1.9
 */
@Audited
@Entity
@Table(name = "location_attribute")
@AssociationOverride(
	name="owner",
	joinColumns = @JoinColumn(name="location_id", nullable = false)
)
public class LocationAttribute extends BaseAttribute<LocationAttributeType, Location> implements Attribute<LocationAttributeType, Location> {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="location_attribute_id")
	private Integer locationAttributeId;
	
	// BaseAttribute<Location> has an "owner" property of type Location, which we re-expose as "location"
	
	/**
	 * @return the locationAttributeId
	 */
	public Integer getLocationAttributeId() {
		return locationAttributeId;
	}
	
	/**
	 * @param locationAttributeId the locationAttributeId to set
	 */
	public void setLocationAttributeId(Integer locationAttributeId) {
		this.locationAttributeId = locationAttributeId;
	}
	
	/**
	 * @return the location
	 */
	public Location getLocation() {
		return getOwner();
	}
	
	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		setOwner(location);
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getLocationAttributeId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setLocationAttributeId(id);
	}
	
}

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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.envers.Audited;
import org.openmrs.attribute.AttributeType;
import org.openmrs.attribute.BaseAttributeType;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A user-defined extension to the {@link Location} class.
 * @see AttributeType
 * @since 1.9
 */
@Entity
@Table(name = "location_attribute_type")
@Audited
@AttributeOverrides(value = {
	@AttributeOverride(name = "description", column = @Column(name = "description", length = 1024))
})
public class LocationAttributeType extends BaseAttributeType<Location> implements AttributeType<Location> {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "location_attribute_type_id_seq")
	@GenericGenerator(
		name = "location_attribute_type_id_seq",
		strategy = "native",
		parameters = @Parameter(name = "sequence", value = "location_attribute_type_location_attribute_type_id_seq")
	)
	@Column(name = "location_attribute_type_id")
	private Integer locationAttributeTypeId;
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getLocationAttributeTypeId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setLocationAttributeTypeId(id);
	}
	
	/**
	 * @return the locationAttributeTypeId
	 */
	public Integer getLocationAttributeTypeId() {
		return locationAttributeTypeId;
	}
	
	/**
	 * @param locationAttributeTypeId the locationAttributeTypeId to set
	 */
	public void setLocationAttributeTypeId(Integer locationAttributeTypeId) {
		this.locationAttributeTypeId = locationAttributeTypeId;
	}
	
}

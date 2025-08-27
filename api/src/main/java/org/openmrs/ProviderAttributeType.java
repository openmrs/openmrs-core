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
 * A user-defined extension to the {@link Provider} class.
 * 
 * @since 1.9
 */
@Entity
@Table(name = "provider_attribute_type")
@Audited
@AttributeOverrides(value = {
	@AttributeOverride(name = "description", column = @Column(name = "description", length = 1024))
})
public class ProviderAttributeType extends BaseAttributeType<Provider> implements AttributeType<Provider> {

	@Id
	@Column(name = "provider_attribute_type_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer providerAttributeTypeId;

	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getProviderAttributeTypeId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setProviderAttributeTypeId(id);
	}
	
	public void setProviderAttributeTypeId(Integer providerAttributeTypeId) {
		this.providerAttributeTypeId = providerAttributeTypeId;
	}
	
	public Integer getProviderAttributeTypeId() {
		return providerAttributeTypeId;
	}
	
}

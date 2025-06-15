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
import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.BaseAttribute;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * A value for a user-defined {@link ProviderAttributeType} that is stored on a {@link Provider}.
 *
 * @see Attribute
 * @since 1.9
 */
@Entity
@Table(name = "provider_attribute")
@Audited
public class ProviderAttribute extends BaseAttribute<ProviderAttributeType, Provider> implements Attribute<ProviderAttributeType, Provider> {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "provider_attribute_id_seq")
	@GenericGenerator(
			name = "provider_attribute_id_seq",
			strategy = "native",
			parameters = @Parameter(name = "sequence", value = "provider_attribute_provider_attribute_id_seq")
	)
	@Column(name = "provider_attribute_id", nullable = false)
	private Integer providerAttributeId;

	public Integer getProviderAttributeId() {
		return providerAttributeId;
	}

	public void setProviderAttributeId(Integer providerAttributeId) {
		this.providerAttributeId = providerAttributeId;
	}

	public Provider getProvider() {
		return getOwner();
	}

	public void setProvider(Provider provider) {
		setOwner(provider);
	}

	@Override
	public Integer getId() {
		return getProviderAttributeId();
	}

	@Override
	public void setId(Integer id) {
		setProviderAttributeId(id);
	}

}

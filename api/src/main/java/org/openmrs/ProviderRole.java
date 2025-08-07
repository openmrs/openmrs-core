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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;

import java.io.Serializable;

/**
 * Used to store the possible provider roles.  A Provide can only have a single role (though a single person
 * could be associated with more than one Provider object).
 * <p>
 * A provider role specifies what Provider/Patient relationships a provider with that role can support,
 * as well as the provider roles that another provider role can provider oversight for.
 * <p>
 * For example, a "Community Health Worker" role might support an "Accompagnateur" relationship,
 * and "Head Surgeon" role might be able to oversee a person with Provider Role of "Surgeon".
 * 
 * @since 2.8.0
 */
@Entity
@Table(name = "provider_role")
@Audited
public class ProviderRole extends BaseOpenmrsMetadata implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "provider_role_id")
	private Integer providerRoleId;

	@Override
	public Integer getId() {
		return providerRoleId;
	}

	@Override
	public void setId(Integer id) {
		this.providerRoleId = id;
	}

	public Integer getProviderRoleId() {
		return providerRoleId;
	}

	public void setProviderRoleId(Integer id) {
		this.providerRoleId = id;
	}

	@Override
	public String toString() {
		return "ProviderRole{" +
			"providerRoleId=" + providerRoleId +
			", name=" + this.getName() +
			'}';
	}
}

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

import org.openmrs.attribute.AttributeType;
import org.openmrs.attribute.BaseAttributeType;

/**
 * A user-defined extension to the {@link Provider} class.
 * 
 * @since 1.9
 */
public class ProviderAttributeType extends BaseAttributeType<Provider> implements AttributeType<Provider> {
	
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

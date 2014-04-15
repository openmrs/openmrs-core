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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a person who may provide care to a patient during an encounter
 *
 * @since 1.9
 */
public class Provider extends BaseCustomizableMetadata<ProviderAttribute> {
	
	private final Log log = LogFactory.getLog(getClass());
	
	private Integer providerId;
	
	private Person person;
	
	private String identifier;
	
	public Provider() {
	}
	
	public Provider(Integer providerId) {
		this.providerId = providerId;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getProviderId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setProviderId(id);
	}
	
	/**
	 * @param providerId the providerId to set
	 */
	public void setProviderId(Integer providerId) {
		this.providerId = providerId;
	}
	
	/**
	 * @return the providerId
	 */
	public Integer getProviderId() {
		return providerId;
	}
	
	/**
	 * @param person the person to set
	 * @should blank out name if set to non null person
	 */
	public void setPerson(Person person) {
		this.person = person;
		
		//blank out name so that there isn't double data sitting in the provider table.
		if (person != null) {
			setName(null);
		}
	}
	
	/**
	 * @return the person
	 */
	public Person getPerson() {
		return person;
	}
	
	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(providerId);
		sb.append(" providerName:");
		sb.append((person != null) ? person.getNames() : "");
		return "[Provider: providerId:" + sb.toString() + " ]";
	}
	
	/**
	 * @see org.openmrs.BaseOpenmrsMetadata#getName()
	 * @should return person full name if person is not null
	 */
	@Override
	public String getName() {
		if (getPerson() != null && getPerson().getPersonName() != null) {
			return getPerson().getPersonName().getFullName();
		} else {
			return super.getName();
		}
	}
	
	/**
	 * @see org.openmrs.BaseOpenmrsMetadata#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		super.setName(name);
		
		//Trace message if we are setting a name when already attached to a person.
		if (getPerson() != null && !StringUtils.isBlank(super.getName())) {
			log.trace("Setting name for a provider who is already attached to a person");
		}
	}
}

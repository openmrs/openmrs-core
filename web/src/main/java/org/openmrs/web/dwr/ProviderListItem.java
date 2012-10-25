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
package org.openmrs.web.dwr;

import org.openmrs.Person;
import org.openmrs.Provider;

/**
 * A mini/simplified provider object. Used as the return object from DWR methods to allow javascript
 * and other consumers to easily use all methods. This class guarantees that all objects in this
 * class will be initialized (copied) off of the provider object.
 * 
 * @see Provider
 * @see DWRProviderService
 */
public class ProviderListItem {
	
	private String identifier;
	
	private String displayName;
	
	private Integer providerId;
	
	private boolean retired = false;
	
	public ProviderListItem(Provider provider) {
		Person person = provider.getPerson();
		if (person != null) {
			displayName = person.getPersonName().getFullName();
		} else {
			displayName = provider.getName();
		}
		identifier = provider.getIdentifier();
		providerId = provider.getProviderId();
		retired = provider.isRetired();
	}
	
	/**
	 * @return the identifier of the provider
	 * @should return the identifier that is mentioned for the provider when a person is not
	 *         specified
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * @return the display name for the provider
	 * @should return a display name based on whether provider has a person associated
	 * @should return a display name based on provider name when person is not associated
	 */
	public String getDisplayName() {
		return displayName;
	}
	
	/**
	 * @return the provider id
	 * @should return the provider id
	 */
	public Integer getProviderId() {
		return providerId;
	}
	
	/**
	 * @return the retired
	 */
	public boolean isRetired() {
		return retired;
	}
	
	/**
	 * @param retired the retired to set
	 */
	public void setRetired(boolean retired) {
		this.retired = retired;
	}
}

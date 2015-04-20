/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.ProviderService;

/**
 * Data Access function for Provider
 * 
 * @since 1.9
 */
public interface ProviderDAO {
	
	/**
	 * Gets all Providers
	 * 
	 * @param includeRetired - whether or not to include retired Provider
	 */
	List<Provider> getAllProviders(boolean includeRetired);
	
	/**
	 * Saves/Updates a given Provider
	 */
	public Provider saveProvider(Provider provider);
	
	/**
	 * deletes an exisiting Provider
	 */
	public void deleteProvider(Provider provider);
	
	/**
	 * @param id
	 * @return Provider gets the Provider based on id
	 */
	public Provider getProvider(Integer id);
	
	/**
	 * @param uuid
	 * @return Provider gets the Provider based on uuid
	 */
	
	public Provider getProviderByUuid(String uuid);
	
	/**
	 * @see ProviderService#getProvidersByPerson( Person, boolean )
	 */
	public Collection<Provider> getProvidersByPerson(Person person, boolean includeRetired);
	
	/**
	 * @param name
	 * @param serializedAttributeValues
	 * @param start
	 * @param length
	 * @param includeRetired
	 * @return List of Providers
	 */
	public List<Provider> getProviders(String name, Map<ProviderAttributeType, String> serializedAttributeValues,
	        Integer start, Integer length, boolean includeRetired);
	
	/**
	 * @param name
	 * @param includeRetired
	 * @return Count of providers satisfying the given query
	 */
	public Long getCountOfProviders(String name, boolean includeRetired);
	
	/**
	 * @see ProviderService#getAllProviderAttributeTypes(boolean)
	 * @see ProviderService#getAllProviderAttributeTypes()
	 */
	List<ProviderAttributeType> getAllProviderAttributeTypes(boolean includeRetired);
	
	/**
	 * @see ProviderService#getProviderAttributeType(Integer)
	 */
	ProviderAttributeType getProviderAttributeType(Integer providerAttributeTypeId);
	
	/**
	 * @see ProviderService#getProviderAttributeTypeByUuid(String)
	 */
	ProviderAttributeType getProviderAttributeTypeByUuid(String uuid);
	
	/**
	 * @see ProviderService#saveProviderAttributeType(ProviderAttributeType)
	 */
	ProviderAttributeType saveProviderAttributeType(ProviderAttributeType providerAttributeType);
	
	/**
	 * @see ProviderService#purgeProviderAttributeType(ProviderAttributeType)
	 */
	void deleteProviderAttributeType(ProviderAttributeType providerAttributeType);
	
	/**
	 * @see ProviderService#getProviderAttribute(Integer)
	 */
	
	ProviderAttribute getProviderAttribute(Integer providerAttributeID);
	
	/**
	 * @see ProviderService#getProviderAttributeByUuid(String)
	 */
	ProviderAttribute getProviderAttributeByUuid(String uuid);
	
	/**
	 * @see ProviderService#isProviderIdentifierUnique(Provider)
	 */
	public boolean isProviderIdentifierUnique(Provider provider) throws DAOException;
	
	/**
	 * @see ProviderService#getProviderByIdentifier(String)
	 */
	public Provider getProviderByIdentifier(String identifier);
}

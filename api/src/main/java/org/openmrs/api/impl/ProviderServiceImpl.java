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
package org.openmrs.api.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.APIException;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ProviderDAO;
import org.openmrs.attribute.AttributeType;
import org.openmrs.validator.ProviderValidator;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Default implementation of the {@link ProviderService}. This class should not be used on its own.
 * The current OpenMRS implementation should be fetched from the Context.
 * 
 * @since 1.9
 */
public class ProviderServiceImpl extends BaseOpenmrsService implements ProviderService {
	
	private ProviderDAO dao;
	
	/**
	 * Sets the data access object for Concepts. The dao is used for saving and getting concepts
	 * to/from the database
	 * 
	 * @param dao The data access object to use
	 */
	public void setProviderDAO(ProviderDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#getAllProviders()
	 */
	@Override
	public List<Provider> getAllProviders() {
		return getAllProviders(true);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#getAllProviders(boolean)
	 */
	public List<Provider> getAllProviders(boolean includeRetired) {
		return dao.getAllProviders(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#retireProvider(org.openmrs.Provider, java.lang.String)
	 */
	public void retireProvider(Provider provider, String reason) {
		dao.saveProvider(provider);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#unretireProvider(org.openmrs.Provider)
	 */
	public Provider unretireProvider(Provider provider) {
		return dao.saveProvider(provider);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#purgeProvider(org.openmrs.Provider)
	 */
	public void purgeProvider(Provider provider) {
		dao.deleteProvider(provider);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#getProvider(java.lang.Integer)
	 */
	@Override
	public Provider getProvider(Integer providerId) {
		return dao.getProvider(providerId);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#saveProvider(org.openmrs.Provider)
	 */
	@Override
	public Provider saveProvider(Provider provider) {
		//This should be done via AOP
		Errors errors = new BindException(provider, "provider");
		new ProviderValidator().validate(provider, errors);
		if (errors.hasErrors())
			throw new APIException(Context.getMessageSourceService().getMessage("error.foundValidationErrors"));
		
		return dao.saveProvider(provider);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#getProviderbyUuid(java.lang.String)
	 */
	@Override
	public Provider getProviderbyUuid(String uuid) {
		return dao.getProviderByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#getProvidersByPerson(org.openmrs.Person)
	 */
	@Override
	public Collection<Provider> getProvidersByPerson(Person person) {
		Validate.notNull(person, "Person must not be null");
		return dao.getProvidersByPerson(person);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#getCountOfProviders(java.lang.String)
	 */
	@Override
	public Integer getCountOfProviders(String query) {
		return dao.getCountOfProviders(query);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#getProviders(String, Integer, Integer, java.util.Map
	 */
	@Override
	public List<Provider> getProviders(String query, Integer start, Integer length,
	        Map<ProviderAttributeType, Object> attributeValues) {
		Map<ProviderAttributeType, String> serializedAttributeValues = Context.getAttributeService()
		        .getSerializedAttributeValues(attributeValues);
		return dao.getProviders(query, serializedAttributeValues, start, length);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#getAllProviderAttributeTypes()
	 */
	@Override
	public List<ProviderAttributeType> getAllProviderAttributeTypes() {
		return dao.getAllProviderAttributeTypes(true);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#getAllProviderAttributeTypes(boolean)
	 */
	@Override
	public List<ProviderAttributeType> getAllProviderAttributeTypes(boolean includeRetired) {
		return dao.getAllProviderAttributeTypes(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#getProviderAttributeType(java.lang.Integer)
	 */
	@Override
	public ProviderAttributeType getProviderAttributeType(Integer providerAttributeTypeId) {
		return dao.getProviderAttributeType(providerAttributeTypeId);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#getProviderAttributeTypeByUuid(java.lang.String)
	 */
	@Override
	public ProviderAttributeType getProviderAttributeTypeByUuid(String uuid) {
		return dao.getProviderAttributeTypeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#getProviderAttribute(java.lang.Integer)
	 */
	
	@Override
	public ProviderAttribute getProviderAttribute(Integer providerAttributeID) {
		return dao.getProviderAttribute(providerAttributeID);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#getProviderAttributeByUuid(String)
	 */
	
	@Override
	public ProviderAttribute getProviderAttributeByUuid(String uuid) {
		return dao.getProviderAttributeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#saveProviderAttributeType(org.openmrs.ProviderAttributeType)
	 */
	@Override
	public ProviderAttributeType saveProviderAttributeType(ProviderAttributeType providerAttributeType) {
		return dao.saveProviderAttributeType(providerAttributeType);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#retireProviderAttributeType(org.openmrs.ProviderAttributeType,
	 *      java.lang.String)
	 */
	@Override
	public ProviderAttributeType retireProviderAttributeType(ProviderAttributeType providerAttributeType, String reason) {
		return saveProviderAttributeType(providerAttributeType);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#unretireProviderAttributeType(org.openmrs.ProviderAttributeType)
	 */
	@Override
	public ProviderAttributeType unretireProviderAttributeType(ProviderAttributeType providerAttributeType) {
		return saveProviderAttributeType(providerAttributeType);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#purgeProviderAttributeType(org.openmrs.ProviderAttributeType)
	 */
	@Override
	public void purgeProviderAttributeType(ProviderAttributeType providerAttributeType) {
		dao.deleteProviderAttributeType(providerAttributeType);
	}
	
	/**
	 * @see org.openmrs.api.ProviderService#isProviderIdentifierUnique(Provider)
	 */
	@Override
	public boolean isProviderIdentifierUnique(Provider provider) throws APIException {
		return dao.isProviderIdentifierUnique(provider);
	}
}

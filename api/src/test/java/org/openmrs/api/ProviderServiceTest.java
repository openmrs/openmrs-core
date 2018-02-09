/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;

/**
 * This test class (should) contain tests for all of the ProviderService
 * 
 * @see org.openmrs.api.ProviderService
 */
public class ProviderServiceTest extends BaseContextSensitiveTest {
	
	private static final String PROVIDERS_INITIAL_XML = "org/openmrs/api/include/ProviderServiceTest-initial.xml";
	
	private static final String PROVIDER_ATTRIBUTE_TYPES_XML = "org/openmrs/api/include/ProviderServiceTest-providerAttributes.xml";
	
	private static final String OTHERS_PROVIDERS_XML = "org/openmrs/api/include/ProviderServiceTest-otherProviders.xml";
	
	private ProviderService service;
	
	@Before
	public void before() {
		service = Context.getProviderService();
		executeDataSet(PROVIDERS_INITIAL_XML);
		executeDataSet(PROVIDER_ATTRIBUTE_TYPES_XML);
	}
	
	/**
	 * @see ProviderService#getAllProviderAttributeTypes(boolean)
	 */
	@Test
	public void getAllProviderAttributeTypes_shouldGetAllProviderAttributeTypesExcludingRetired() {
		List<ProviderAttributeType> types = service.getAllProviderAttributeTypes(false);
		assertEquals(2, types.size());
	}
	
	/**
	 * @see ProviderService#getAllProviderAttributeTypes(boolean)
	 */
	@Test
	public void getAllProviderAttributeTypes_shouldGetAllProviderAttributeTypesIncludingRetired() {
		List<ProviderAttributeType> types = service.getAllProviderAttributeTypes(true);
		assertEquals(3, types.size());
	}
	
	/**
	 * @see ProviderService#getAllProviderAttributeTypes()
	 */
	@Test
	public void getAllProviderAttributeTypes_shouldGetAllProviderAttributeTypesIncludingRetiredByDefault() {
		List<ProviderAttributeType> types = service.getAllProviderAttributeTypes();
		assertEquals(3, types.size());
	}
	
	/**
	 * @see ProviderService#getAllProviders()
	 */
	@Test
	public void getAllProviders_shouldGetAllProviders() {
		List<Provider> providers = service.getAllProviders();
		assertEquals(9, providers.size());
	}
	
	/**
	 * @see ProviderService#getAllProviders(boolean)
	 */
	@Test
	public void getAllProviders_shouldGetAllProvidersThatAreUnretired() {
		List<Provider> providers = service.getAllProviders(false);
		assertEquals(7, providers.size());
	}
	
	/**
	 * @see ProviderService#getProvider(Integer)
	 */
	@Test
	public void getProvider_shouldGetProviderGivenID() {
		Provider provider = service.getProvider(2);
		assertEquals("Mr. Horatio Test Hornblower", provider.getName());
	}
	
	/**
	 * @see ProviderService#getProviderAttribute(Integer)
	 */
	@Test
	public void getProviderAttribute_shouldGetProviderAttributeGivenID() {
		ProviderAttribute providerAttribute = service.getProviderAttribute(321);
		assertEquals("Mr. Horatio Test Hornblower", providerAttribute.getProvider().getName());
	}
	
	/**
	 * @see ProviderService#getProviderAttributeByUuid(String)
	 */
	
	@Test
	public void getProviderAttributeByUuid_shouldGetProviderAttributeGivenUuid() {
		ProviderAttribute providerAttribute = service.getProviderAttributeByUuid("823382cd-5faa-4b57-8b34-fed33b9c8c65");
		assertEquals("Mr. Horatio Test Hornblower", providerAttribute.getProvider().getName());
	}
	
	/**
	 * @see ProviderService#getProviderAttributeType(Integer)
	 */
	@Test
	public void getProviderAttributeType_shouldGetProviderAttributeTypeForTheGivenId() {
		ProviderAttributeType providerAttributeType = service.getProviderAttributeType(1);
		assertEquals("Audit Date", providerAttributeType.getName());
		assertEquals("9516cc50-6f9f-11e0-8414-001e378eb67e", providerAttributeType.getUuid());
	}
	
	/**
	 * @see ProviderService#getProviderAttributeTypeByUuid(String)
	 */
	@Test
	public void getProviderAttributeTypeByUuid_shouldGetTheProviderAttributeTypeByItsUuid() {
		ProviderAttributeType providerAttributeType = service
		        .getProviderAttributeTypeByUuid("9516cc50-6f9f-11e0-8414-001e378eb67e");
		assertEquals("Audit Date", providerAttributeType.getName());
	}
	
	/**
	 * @see ProviderService#getProviderByUuid(String)
	 */
	@Test
	public void getProviderByUuid_shouldGetProviderGivenUuid() {
		Provider provider = service.getProviderByUuid("ba4781f4-6b94-11e0-93c3-18a905e044dc");
		assertEquals("Collet Test Chebaskwony", provider.getName());
		assertNotNull(provider);
	}
	
	/**
	 * @see ProviderService#getProviders(String, Integer, Integer, java.util.Map)
	 */
	@Test
	public void getProviders_shouldFetchProviderWithGivenNameWithCaseInSensitive() {
		List<Provider> providers = service.getProviders("colle", 0, null, null);
		assertEquals(1, providers.size());
	}
	
	/**
	 * @see ProviderService#getProviders(String, Integer, Integer, java.util.Map)
	 */
	@Test
	public void getProviders_shouldFetchProviderByMatchingQueryStringWithAnyUnVoidedPersonNamesGivenName() {
		assertEquals(1, service.getProviders("COL", 0, null, null).size());
	}
	
	/**
	 * @see ProviderService#getProviders(String, Integer, Integer, java.util.Map)
	 */
	@Test
	public void getProviders_shouldFetchProviderByMatchingQueryStringWithAnyUnVoidedPersonNamesMiddleName() {
		assertEquals(6, service.getProviders("Tes", 0, null, null).size());
	}
	
	/**
	 * @see ProviderService#getProviders(String, Integer, Integer, java.util.Map)
	 */
	@Test
	public void getProviders_shouldFetchProviderByMatchingQueryStringWithAnyUnVoidedPersonsFamilyName() {
		assertEquals(2, service.getProviders("Che", 0, null, null, true).size());
	}
	
	/**
	 * @see ProviderService#getProviders(String, Integer, Integer, java.util.Map)
	 */
	@Test
	public void getProviders_shouldNotFetchProviderIfTheQueryStringMatchesWithAnyVoidedPersonNameForThat() {
		assertEquals(0, service.getProviders("Hit", 0, null, null).size());
		assertEquals(1, service.getProviders("coll", 0, null, null).size());
	}
	
	/**
	 * @see ProviderService#purgeProvider(Provider)
	 */
	@Test
	public void purgeProvider_shouldDeleteAProvider() {
		Provider provider = service.getProvider(2);
		service.purgeProvider(provider);
		assertEquals(8, Context.getProviderService().getAllProviders().size());
	}
	
	/**
	 * @see ProviderService#purgeProviderAttributeType(ProviderAttributeType)
	 */
	@Test
	public void purgeProviderAttributeType_shouldDeleteAProviderAttributeType() {
		int size = service.getAllProviderAttributeTypes().size();
		ProviderAttributeType providerAttributeType = service.getProviderAttributeType(2);
		service.purgeProviderAttributeType(providerAttributeType);
		assertEquals(size - 1, service.getAllProviderAttributeTypes().size());
	}
	
	/**
	 * @see ProviderService#retireProvider(Provider,String)
	 */
	@Test
	public void retireProvider_shouldRetireAProvider() {
		Provider provider = service.getProvider(1);
		assertFalse(provider.getRetired());
		assertNull(provider.getRetireReason());
		service.retireProvider(provider, "retire reason");
		assertTrue(provider.getRetired());
		assertEquals("retire reason", provider.getRetireReason());
		assertEquals(6, service.getAllProviders(false).size());
	}
	
	/**
	 * @see ProviderService#retireProviderAttributeType(ProviderAttributeType,String)
	 */
	@Test
	public void retireProviderAttributeType_shouldRetireProviderTypeAttribute() {
		ProviderAttributeType providerAttributeType = service.getProviderAttributeType(1);
		assertFalse(providerAttributeType.getRetired());
		assertNull(providerAttributeType.getRetireReason());
		assertEquals(2, service.getAllProviderAttributeTypes(false).size());
		service.retireProviderAttributeType(providerAttributeType, "retire reason");
		assertTrue(providerAttributeType.getRetired());
		assertEquals("retire reason", providerAttributeType.getRetireReason());
		assertEquals(1, service.getAllProviderAttributeTypes(false).size());
	}
	
	/**
	 * @see ProviderService#saveProvider(Provider)
	 */
	@Test
	public void saveProvider_shouldSaveAProviderWithPersonAlone() {
		Provider provider = new Provider();
		provider.setIdentifier("unique");
		Person person = Context.getPersonService().getPerson(999);
		provider.setPerson(person);
		service.saveProvider(provider);
		Assert.assertNotNull(provider.getId());
		Assert.assertNotNull(provider.getUuid());
		Assert.assertNotNull(provider.getCreator());
		Assert.assertNotNull(provider.getDateCreated());
		Assert.assertEquals(999, provider.getPerson().getId().intValue());
		
	}
	
	/**
	 * @see ProviderService#saveProviderAttributeType(ProviderAttributeType)
	 */
	@Test
	public void saveProviderAttributeType_shouldSaveTheProviderAttributeType() {
		int size = service.getAllProviderAttributeTypes().size();
		ProviderAttributeType providerAttributeType = new ProviderAttributeType();
		providerAttributeType.setName("new");
		providerAttributeType.setDatatypeClassname(FreeTextDatatype.class.getName());
		providerAttributeType = service.saveProviderAttributeType(providerAttributeType);
		assertEquals(size + 1, service.getAllProviderAttributeTypes().size());
		assertNotNull(providerAttributeType.getId());
	}
	
	/**
	 * @see ProviderService#unretireProvider(Provider)
	 */
	@Test
	public void unretireProvider_shouldUnretireAProvider() {
		Provider provider = service.getProvider(2);
		service.unretireProvider(provider);
		assertFalse(provider.getRetired());
		assertNull(provider.getRetireReason());
	}
	
	/**
	 * @see ProviderService#unretireProviderAttributeType(ProviderAttributeType)
	 */
	@Test
	public void unretireProviderAttributeType_shouldUnretireAProviderAttributeType() {
		ProviderAttributeType providerAttributeType = service.getProviderAttributeType(2);
		assertTrue(providerAttributeType.getRetired());
		service.unretireProviderAttributeType(providerAttributeType);
		assertFalse(providerAttributeType.getRetired());
		assertNull(providerAttributeType.getRetireReason());
	}
	
	/**
	 * @throws ParseException
	 * @see ProviderService#getProviders(String, Integer, Integer, java.util.Map)
	 */
	@Test
	public void getProviders_shouldGetAllProvidersWithGivenAttributeValues() throws ParseException {
		Map<ProviderAttributeType, Object> attributes = new HashMap<>();
		attributes.put(service.getProviderAttributeType(1), new SimpleDateFormat("yyyy-MM-dd").parse("2011-04-25"));
		List<Provider> providers = service.getProviders("RobertClive", 0, null, attributes);
		Assert.assertEquals(1, providers.size());
		Assert.assertEquals(Integer.valueOf(1), providers.get(0).getProviderId());
	}
	
	/**
	 * @throws ParseException
	 * @see ProviderService#getProviders(String, Integer, Integer, java.util.Map)
	 */
	@Test
	public void getProviders_shouldNotFindAnyProvidersIfNoneHaveGivenAttributeValues() throws ParseException {
		Map<ProviderAttributeType, Object> attributes = new HashMap<>();
		attributes.put(service.getProviderAttributeType(1), new SimpleDateFormat("yyyy-MM-dd").parse("1411-04-25"));
		List<Provider> providers = service.getProviders("RobertClive", 0, null, attributes);
		Assert.assertEquals(0, providers.size());
	}
	
	/**
	 * @see ProviderService#getProviders(String, Integer, Integer, java.util.Map)
	 */
	@Test
	public void getProviders_shouldReturnRetiredProvidersByDefault() {
		List<Provider> providers = service.getProviders(null, null, null, null);
		Assert.assertEquals(9, providers.size());
	}
	
	/**
	 * @see ProviderService#getProviders(String, Integer, Integer, java.util.Map, boolean)
	 */
	@Test
	public void getProviders_shouldNotReturnRetiredProvidersIfIncludeRetiredIsFalse() {
		List<Provider> providers = service.getProviders(null, null, null, null, false);
		Assert.assertEquals(7, providers.size());
	}
	
	/**
	 * @see ProviderService#getProvidersByPerson(Person)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getProvidersByPerson_shouldFailIfPersonIsNull() {
		//given
		
		//when
		service.getProvidersByPerson(null);
		
		//then
		Assert.fail();
	}
	
	/**
	 * @see ProviderService#getProvidersByPerson(Person)
	 */
	@Test
	public void getProvidersByPerson_shouldReturnProvidersForGivenPerson() {
		//given
		Person person = Context.getPersonService().getPerson(999);
		Provider provider = new Provider();
		provider.setIdentifier("unique");
		provider.setPerson(person);
		provider = service.saveProvider(provider);
		
		//when
		Collection<Provider> providers = service.getProvidersByPerson(person);
		
		//then
		Assert.assertEquals(1, providers.size());
		Assert.assertTrue(providers.contains(provider));
	}
	
	/**
	 * @see ProviderService#getProviders(String,Integer,Integer,Map)
	 */
	@Test
	public void getProviders_shouldReturnAllProvidersIfQueryIsEmptyAndIncludeRetiredTrue() {
		//given
		List<Provider> allProviders = service.getAllProviders();
		
		//when
		List<Provider> providers = service.getProviders("", null, null, null, true);
		
		//then
		Assert.assertEquals(allProviders.size(), providers.size());
	}
	
	/**
	 * @see ProviderService#isProviderIdentifierUnique(Provider)
	 */
	@Test
	public void isProviderIdentifierUnique_shouldReturnFalseIfTheIdentifierIsADuplicate() {
		executeDataSet(OTHERS_PROVIDERS_XML);
		Provider duplicateProvider = service.getProvider(200);
		
		Provider existingProviderToEdit = service.getProvider(1);
		existingProviderToEdit.setIdentifier(duplicateProvider.getIdentifier());
		Assert.assertFalse(service.isProviderIdentifierUnique(existingProviderToEdit));
	}
	
	/**
	 * @see ProviderService#getProviderByIdentifier(String)
	 */
	@Test
	public void getProviderByIdentifier_shouldGetAProviderMatchingTheSpecifiedIdentifierIgnoringCase() {
		String identifier = "8a760";
		Provider provider = service.getProviderByIdentifier(identifier);
		Assert.assertEquals("a2c3868a-6b90-11e0-93c3-18a905e044dc", provider.getUuid());
		//ensures that the case sensitive test stays valid just in case 
		//the test dataset is edited and the case is changed
		Assert.assertNotSame(identifier, provider.getIdentifier());
	}
	
	/**
	 * @see ProviderService#getProviders(String,Integer,Integer,Map,boolean)
	 */
	@Test
	public void getProviders_shouldFindProviderByIdentifier() {
		String identifier = "8a760";
		List<Provider> providers = service.getProviders(identifier, null, null, null, true);
		Provider provider = service.getProviderByIdentifier(identifier);
		
		Assert.assertTrue(providers.contains(provider));
	}
	
	/**
	 * @see ProviderService#isProviderIdentifierUnique(Provider)
	 */
	@Test
	public void isProviderIdentifierUnique_shouldReturnTrueIfTheIdentifierIsNull() {
		Provider provider = new Provider();
		Assert.assertTrue(service.isProviderIdentifierUnique(provider));
	}
	
	/**
	 * @see ProviderService#isProviderIdentifierUnique(Provider)
	 */
	@Test
	public void isProviderIdentifierUnique_shouldReturnTrueIfTheIdentifierIsABlankString() {
		Provider provider = new Provider();
		provider.setIdentifier("");
		Assert.assertTrue(service.isProviderIdentifierUnique(provider));
	}
	
	/**
	 * @see ProviderService#getCountOfProviders(String,null)
	 */
	@Test
	public void getCountOfProviders_shouldFetchNumberOfProviderMatchingGivenQuery() {
		assertEquals(1, service.getCountOfProviders("Hippo").intValue());
		Person person = Context.getPersonService().getPerson(502);
		Set<PersonName> names = person.getNames();
		for (PersonName name : names) {
			name.setVoided(true);

		}
		PersonName personName = new PersonName("Hippot", "A", "B");
		personName.setPreferred(true);
		person.addName(personName);
		Context.getPersonService().savePerson(person);
		assertEquals(1, service.getCountOfProviders("Hippo").intValue());
	}

	/**
	 * @see ProviderService#getCountOfProviders(String)
	 */
	@Test
	public void getCountOfProviders_shouldExcludeRetiredProviders() {
		assertEquals(2, service.getCountOfProviders("provider").intValue());
	}
	
	/**
	 * @see ProviderService#getCountOfProviders(String,null)
	 */
	@Test
	public void getCountOfProviders_shouldIncludeRetiredProvidersIfIncludeRetiredIsSetToTrue() {
		assertEquals(4, service.getCountOfProviders("provider", true).intValue());
	}
	
	/**
	 * @see ProviderService#getUnknownProvider()
	 */
	@Test
	public void getUnknownProvider_shouldGetTheUnknownProviderAccount() {
		Provider provider = new Provider();
		
		provider.setPerson(newPerson("Unknown Provider"));
		
		provider.setIdentifier("Test Unknown Provider");
		provider = service.saveProvider(provider);
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GP_UNKNOWN_PROVIDER_UUID, provider.getUuid(), null);
		Context.getAdministrationService().saveGlobalProperty(gp);
		assertEquals(provider, service.getUnknownProvider());
	}
	
	private Person newPerson(String name) {
		Person person = new Person();
		Set<PersonName> personNames = new TreeSet<>();
		PersonName personName = new PersonName();
		personName.setFamilyName(name);
		personNames.add(personName);
		person.setNames(personNames);
		return person;
	}
	
}

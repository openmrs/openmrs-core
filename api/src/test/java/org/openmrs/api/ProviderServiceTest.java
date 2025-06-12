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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.ProviderRole;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
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

	protected static final String PROVIDER_ROLES_XML_DATASET = "org/openmrs/api/include/ProviderServiceTest-ProviderRoles-dataset.xml";
	
	private ProviderService service;
	
	@BeforeEach
	public void before() {
		service = Context.getProviderService();
		executeDataSet(PROVIDERS_INITIAL_XML);
		executeDataSet(PROVIDER_ATTRIBUTE_TYPES_XML);
		executeDataSet(PROVIDER_ROLES_XML_DATASET);
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
		assertEquals(17, providers.size());
	}
	
	/**
	 * @see ProviderService#getAllProviders(boolean)
	 */
	@Test
	public void getAllProviders_shouldGetAllProvidersThatAreUnretired() {
		List<Provider> providers = service.getAllProviders(false);
		assertEquals(15, providers.size());
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
	 * @see ProviderService#getProviderAttributeTypeByName(String)
	 */
	@Test
	public void getProviderAttributeTypeByName_shouldGetTheProviderAttributeTypeByItsName() {
		ProviderAttributeType providerAttributeType = service.getProviderAttributeTypeByName("Audit Date");
		assertEquals("Audit Date", providerAttributeType.getName());
		assertEquals("9516cc50-6f9f-11e0-8414-001e378eb67e", providerAttributeType.getUuid());
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
		assertEquals(2, providers.size());
	}
	
	/**
	 * @see ProviderService#getProviders(String, Integer, Integer, java.util.Map)
	 */
	@Test
	public void getProviders_shouldFetchProviderByMatchingQueryStringWithAnyUnVoidedPersonNamesGivenName() {
		assertEquals(2, service.getProviders("COL", 0, null, null).size());
	}
	
	/**
	 * @see ProviderService#getProviders(String, Integer, Integer, java.util.Map)
	 */
	@Test
	public void getProviders_shouldFetchProviderByMatchingQueryStringWithAnyUnVoidedPersonNamesMiddleName() {
		assertEquals(11, service.getProviders("Tes", 0, null, null).size());
	}
	
	/**
	 * @see ProviderService#getProviders(String, Integer, Integer, java.util.Map)
	 */
	@Test
	public void getProviders_shouldFetchProviderByMatchingQueryStringWithAnyUnVoidedPersonsFamilyName() {
		assertEquals(4, service.getProviders("Che", 0, null, null, true).size());
	}
	
	/**
	 * @see ProviderService#getProviders(String, Integer, Integer, java.util.Map)
	 */
	@Test
	public void getProviders_shouldNotFetchProviderIfTheQueryStringMatchesWithAnyVoidedPersonNameForThat() {
		assertEquals(0, service.getProviders("Hit", 0, null, null).size());
		assertEquals(2, service.getProviders("coll", 0, null, null).size());
	}
	
	/**
	 * @see ProviderService#purgeProvider(Provider)
	 */
	@Test
	public void purgeProvider_shouldDeleteAProvider() {
		Provider provider = service.getProvider(2);
		service.purgeProvider(provider);
		assertEquals(16, Context.getProviderService().getAllProviders().size());
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
		assertEquals(14, service.getAllProviders(false).size());
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
		assertNotNull(provider.getId());
		assertNotNull(provider.getUuid());
		assertNotNull(provider.getCreator());
		assertNotNull(provider.getDateCreated());
		assertEquals(999, provider.getPerson().getId().intValue());
		
	}

	/**
	 * @see ProviderService#saveProvider(Provider)
	 */
	@Test
	public void saveProvider_shouldSaveAProviderWithProviderRole() {
		Provider provider = new Provider();
		provider.setIdentifier("prov");
		provider.setPerson(Context.getPersonService().getPerson(2));
		
		ProviderRole providerRole = new ProviderRole();
		providerRole.setName("Community Health Worker");
		providerRole.setDescription("Test Description");
		provider.setProviderRole(providerRole);

		Provider savedProvider = service.saveProvider(provider);
		assertNotNull(savedProvider.getId());
		assertNotNull(savedProvider.getUuid());
		assertNotNull(savedProvider.getCreator());
		assertNotNull(savedProvider.getDateCreated());
		
		Provider fetchedProvider = service.getProvider(savedProvider.getProviderId());

		assertNotNull(fetchedProvider);
		assertEquals(2, fetchedProvider.getPerson().getId().intValue());
		assertEquals("Community Health Worker", fetchedProvider.getProviderRole().getName());
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
	 * @see ProviderService#saveProviderAttributeType(ProviderAttributeType)
	 */
	@Test
	public void saveProviderAttributeType_shouldNotSaveProviderAttributeTypeWithDuplicateName() {
		//duplication
		ProviderAttributeType duplicatedAttributeType = new ProviderAttributeType();
		duplicatedAttributeType.setName("Audit Date");
		duplicatedAttributeType.setDatatypeClassname(FreeTextDatatype.class.getName());
		
		assertThrows(ValidationException.class, () -> {
			service.saveProviderAttributeType(duplicatedAttributeType);
		});
	}
	
	@Test
	public void saveProviderAttributeType_shouldSaveProviderAttributeTypeWithSameNameAsRetiredType() {
		int size = service.getAllProviderAttributeTypes().size();
		ProviderAttributeType providerAttributeType = new ProviderAttributeType();
		providerAttributeType.setName("A Date We Don't Care About");
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
		assertEquals(1, providers.size());
		assertEquals(Integer.valueOf(1), providers.get(0).getProviderId());
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
		assertEquals(0, providers.size());
	}
	
	/**
	 * @see ProviderService#getProviders(String, Integer, Integer, java.util.Map)
	 */
	@Test
	public void getProviders_shouldReturnRetiredProvidersByDefault() {
		List<Provider> providers = service.getProviders(null, null, null, null);
		assertEquals(17, providers.size());
	}
	
	/**
	 * @see ProviderService#getProviders(String, Integer, Integer, java.util.Map, boolean)
	 */
	@Test
	public void getProviders_shouldNotReturnRetiredProvidersIfIncludeRetiredIsFalse() {
		List<Provider> providers = service.getProviders(null, null, null, null, false);
		assertEquals(15, providers.size());
	}
	
	/**
	 * @see ProviderService#getProvidersByPerson(Person)
	 */
	@Test
	public void getProvidersByPerson_shouldFailIfPersonIsNull() {
		//given
		
		//when
		assertThrows(IllegalArgumentException.class, () -> service.getProvidersByPerson(null));
		
		//then
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
		assertEquals(1, providers.size());
		assertTrue(providers.contains(provider));
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
		assertEquals(allProviders.size(), providers.size());
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
		assertFalse(service.isProviderIdentifierUnique(existingProviderToEdit));
	}
	
	/**
	 * @see ProviderService#getProviderByIdentifier(String)
	 */
	@Test
	public void getProviderByIdentifier_shouldGetAProviderMatchingTheSpecifiedIdentifierIgnoringCase() {
		String identifier = "8a760";
		Provider provider = service.getProviderByIdentifier(identifier);
		assertEquals("a2c3868a-6b90-11e0-93c3-18a905e044dc", provider.getUuid());
		//ensures that the case sensitive test stays valid just in case 
		//the test dataset is edited and the case is changed
		assertNotSame(identifier, provider.getIdentifier());
	}
	
	/**
	 * @see ProviderService#getProviders(String,Integer,Integer,Map,boolean)
	 */
	@Test
	public void getProviders_shouldFindProviderByIdentifier() {
		String identifier = "8a760";
		List<Provider> providers = service.getProviders(identifier, null, null, null, true);
		Provider provider = service.getProviderByIdentifier(identifier);
		
		assertTrue(providers.contains(provider));
	}
	
	/**
	 * @see ProviderService#isProviderIdentifierUnique(Provider)
	 */
	@Test
	public void isProviderIdentifierUnique_shouldReturnTrueIfTheIdentifierIsNull() {
		Provider provider = new Provider();
		assertTrue(service.isProviderIdentifierUnique(provider));
	}
	
	/**
	 * @see ProviderService#isProviderIdentifierUnique(Provider)
	 */
	@Test
	public void isProviderIdentifierUnique_shouldReturnTrueIfTheIdentifierIsABlankString() {
		Provider provider = new Provider();
		provider.setIdentifier("");
		assertTrue(service.isProviderIdentifierUnique(provider));
	}
	
	/**
	 * @see ProviderService#getCountOfProviders(String) 
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
	 * @see ProviderService#getCountOfProviders(String)
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
		
		provider.setPerson(new Person(2));
		
		provider.setIdentifier("Test Unknown Provider");
		provider = service.saveProvider(provider);
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GP_UNKNOWN_PROVIDER_UUID, provider.getUuid(), null);
		Context.getAdministrationService().saveGlobalProperty(gp);
		assertEquals(provider, service.getUnknownProvider());
	}

	/**
	 * @see ProviderService#getProviderRole(Integer) 
	 */
	@Test
	public void getProviderRole_shouldReturnTheProviderRoleIfExists() {
		assertNotNull(service.getProviderRole(1003));
	}

	/**
	 * @see ProviderService#getProviderRole(Integer)
	 */
	@Test
	public void getProviderRole_shouldReturnNullIfNotExists() {
		assertNull(service.getProviderRole(200));
	}

	/**
	 * @see ProviderService#getProvidersByRoles(List) 
	 */
	@Test
	public void getProvidersByRoles_shouldGetProvidersByRoles() {
		List<ProviderRole> roles = new ArrayList<>();
		roles.add(service.getProviderRole(1001));
		roles.add(service.getProviderRole(1002));

		List<Provider> providers = service.getProvidersByRoles(roles);
		assertEquals(5, providers.size());
	}

	@Test
	public void getAllProviderRoles_shouldGetAllProviderRolesExcludingRetired() {
		List<ProviderRole> roles = service.getAllProviderRoles(false);
		assertEquals(4, roles.size());
	}
	
	/**
	 * @see ProviderService#getProvider(Integer)
	 */
	@Test
	public void getProvider_shouldReturnProviderWithAssociatedProviderRole() {
		Provider provider = service.getProvider(1009);
		assertNotNull(provider);
		assertEquals(1005, provider.getProviderRole().getProviderRoleId());
		assertEquals("Community health nurse", provider.getProviderRole().getName());
		assertEquals("da7f623f-37ce-4bb2-86d6-6d1d05312bd5", provider.getProviderRole().getUuid());
	}

	@Test
	public void getAllProviderRoles_shouldGetAllProviderRoles() {
		List<ProviderRole> roles = service.getAllProviderRoles(true);
		assertEquals(12, roles.size());
	}

	@Test
	public void getProviderRole_shouldGetProviderRole() {
		ProviderRole role = service.getProviderRole(1002);
		assertEquals(1002, role.getId());
		assertEquals("Community Health Worker", role.getName());
	}

	@Test
	public void getProviderRole_shouldReturnNullIfNoProviderForId() {
		assertNull(service.getProviderRole(200));
	}

	@Test
	public void getProviderRoleByUuid_shouldGetProviderRoleByUuid() {
		ProviderRole role = service.getProviderRoleByUuid("db7f523f-27ce-4bb2-86d6-6d1d05312bd5");
		assertEquals(1003, role.getId());
		assertEquals("Cell supervisor", role.getName());
	}

	@Test
	public void getProviderRoleByUuid_shouldReturnNullIfNoProviderRoleForUuid() {
		ProviderRole role = service.getProviderRoleByUuid("zzz");
		assertNull(role);
	}

	@Test
	public void getProviderRoles_shouldGetProviderRoles() {
		Person provider = Context.getPersonService().getPerson(2);
		List<ProviderRole> roles = service.getProviderRoles(provider);
		assertEquals(2, (Integer) roles.size());

		Iterator<ProviderRole> i = roles.iterator();
		while (i.hasNext()) {
			ProviderRole role = i.next();
			int id = role.getId();

			if (id == 1001 || id == 1005 ) {
				i.remove();
			}
		}

		assertEquals(0, roles.size());
	}

	@Test
	public void getProviderRoles_shouldReturnEmptySetForProviderWithNoRole()  {
		Person provider = Context.getProviderService().getProvider(1002).getPerson();
		List<ProviderRole> roles = service.getProviderRoles(provider);
		assertEquals(0, (Integer) roles.size());
	}

	@Test
	public void getProviderRoles_shouldIgnoreRetiredRoles() {
		Person provider = Context.getPersonService().getPerson(2);
		Context.getProviderService().retireProvider(Context.getProviderService().getProvider(1003), "test");

		List<ProviderRole> roles = service.getProviderRoles(provider);
		assertEquals(1, (Integer) roles.size());
		assertEquals(1005, roles.get(0).getId());
	}

	@Test
	public void assignProviderRoleToPerson_shouldAssignProviderRole() {
		Person provider = Context.getProviderService().getProvider(1006).getPerson();
		ProviderRole role = service.getProviderRole(1003);
		service.assignProviderRoleToPerson(provider, role, "123");
		List<ProviderRole> providerRoles = service.getProviderRoles(provider);
		assertEquals(2, providerRoles.size());
		
		Iterator<ProviderRole> i = providerRoles.iterator();

		while (i.hasNext()) {
			ProviderRole providerRole = i.next();
			int id = providerRole.getId();

			if (id == 1002 || id == 1003) {
				i.remove();
			}
		}

		assertEquals(0, providerRoles.size());
	}

	@Test
	public void assignProviderRoleToPerson_shouldNotFailIfProviderAlreadyHasRole() {
		Person provider = Context.getProviderService().getProvider(1006).getPerson();
		ProviderRole role = service.getProviderRole(1002);
		service.assignProviderRoleToPerson(provider, role, "123");
		List<ProviderRole> providerRoles = service.getProviderRoles(provider);
		assertEquals(1, providerRoles.size());
		assertEquals(1002, providerRoles.get(0).getId());
	}

	@Test
	public void assignProviderRoleToPerson_shouldFailIfUnderlyingPersonVoided() {
		assertThrows(APIException.class, () -> {
			Person provider = Context.getProviderService().getProvider(1006).getPerson();
			ProviderRole role = service.getProviderRole(1002);
			Context.getPersonService().voidPerson(provider, "test");
			service.assignProviderRoleToPerson(provider, role, "123");
		});
	}

	@Test
	public void unassignProviderRoleFromPerson_shouldUnassignRoleFromProvider() {
		Person provider = Context.getProviderService().getProvider(1006).getPerson();
		ProviderRole role = service.getProviderRole(1002);
		service.unassignProviderRoleFromPerson(provider, role);
		assertEquals(0, service.getProviderRoles(provider).size());

		Provider p = Context.getProviderService().getProvider(1006);
		assertTrue(p.getRetired());
	}

	@Test
	public void unassignProviderRoleFromPerson_shouldLeaveOtherRoleUntouched() {
		Person provider = Context.getPersonService().getPerson(2);
		service.unassignProviderRoleFromPerson(provider, service.getProviderRole(1001));

		List<ProviderRole> roles = service.getProviderRoles(provider);
		assertEquals(1, roles.size());
		assertEquals(1005, roles.get(0).getId());
	}

	@Test
	public void unassignProviderRoleFromPerson_shouldNotFailIfProviderDoesNotHaveRole() {
		Person provider = Context.getPersonService().getPerson(6);
		service.unassignProviderRoleFromPerson(provider, service.getProviderRole(1002));

		List<ProviderRole> roles = service.getProviderRoles(provider);
		assertEquals(1,roles.size());
		assertEquals(1001, roles.get(0).getId());
	}

	@Test
	public void unassignProviderRoleFromPerson_shouldNotFailIfProviderHasNoRoles() {
		Person provider = Context.getPersonService().getPerson(1);
		service.unassignProviderRoleFromPerson(provider, service.getProviderRole(1002));

		List<ProviderRole> roles = service.getProviderRoles(provider);
		assertEquals(0, roles.size());
	}

	@Test
	public void unassignProviderRoleFromPerson_shouldNotFailIfPersonIsNotProvider() {
		Person provider = Context.getPersonService().getPerson(5002);

		service.unassignProviderRoleFromPerson(provider, service.getProviderRole(1002));
		assertTrue(!service.isProvider(provider));
	}

	@Test
	public void isProvider_shouldReturnTrue() {
		assertTrue(service.isProvider(Context.getPersonService().getPerson(2)));
	}

	@Test
	public void isProvider_shouldReturnFalse() {
		assertFalse(service.isProvider(Context.getPersonService().getPerson(203)));
	}

	@Test
	public void isProvider_shouldReturnTrueEvenIfAllAssociatedProvidersRetired() {
		Context.getProviderService().retireProvider(Context.getProviderService().getProvider(1003), "test");
		Context.getProviderService().retireProvider(Context.getProviderService().getProvider(1009), "test");

		assertTrue(service.isProvider(Context.getPersonService().getPerson(2)));
	}

	@Test
	public void isProvider_shouldFailIfPersonNull() {
		assertThrows(APIException.class, () -> {
			service.isProvider(null);
		});
	}

	@Test
	public void hasRole_shouldReturnTrue() {
		ProviderRole role1 = Context.getService(ProviderService.class).getProviderRole(1001);
		ProviderRole role2 = Context.getService(ProviderService.class).getProviderRole(1005);
		Person provider = Context.getPersonService().getPerson(2);

		assertTrue(service.hasRole(provider, role1));
		assertTrue(service.hasRole(provider, role2));
	}

	@Test
	public void hasRole_shouldReturnFalse() {
		ProviderRole role = Context.getService(ProviderService.class).getProviderRole(1002);
		Person provider = Context.getPersonService().getPerson(2);
		assertFalse(service.hasRole(provider, role));
	}

	@Test
	public void hasRole_shouldReturnFalseIfRoleRetired() {
		ProviderRole role = Context.getService(ProviderService.class).getProviderRole(1001);
		Person provider = Context.getPersonService().getPerson(2);

		Context.getProviderService().retireProvider(Context.getProviderService().getProvider(1003), "test");
		assertFalse(service.hasRole(provider, role));
	}

	@Test
	public void hasRole_shouldReturnFalseIfProviderHasNoRoles() {
		ProviderRole role = Context.getService(ProviderService.class).getProviderRole(1002);
		Person provider = Context.getPersonService().getPerson(1);
		assertFalse(service.hasRole(provider, role));
	}

	@Test
	public void hasRole_shouldReturnFalseIfPersonIsNotProvider() {
		ProviderRole role = Context.getService(ProviderService.class).getProviderRole(1002);
		Person provider = Context.getPersonService().getPerson(502);
		assertFalse(service.hasRole(provider, role));
	}

	@Test
	public void saveProviderRole_shouldSaveProviderRole() {
		ProviderRole role = new ProviderRole();
		role.setName("Some provider role");

		ProviderRole saved = Context.getService(ProviderService.class).saveProviderRole(role);

		assertNotNull(saved.getProviderRoleId());
		assertEquals("Some provider role", saved.getName());

		ProviderRole fetched = service.getProviderRoleByUuid(saved.getUuid());
		assertNotNull(fetched);
		assertEquals("Some provider role", fetched.getName());

		//confirm increment of provider roles
		List<ProviderRole> roles = service.getAllProviderRoles(true);
		assertEquals(13, roles.size());
	}

	@Test
	public void deleteProviderRole_shouldDeleteProviderRole() throws Exception {
		ProviderRole role = service.getProviderRole(1012);
		assertNotNull(role);

		service.purgeProviderRole(role);

		ProviderRole deleted = service.getProviderRole(1012);
		assertNull(deleted);
	}

	@Test
	public void retireProviderRole_shouldRetireProviderRole() {
		ProviderRole role = service.getProviderRole(1002);
		assertFalse(role.getRetired());

		service.retireProviderRole(role, "test reason");

		ProviderRole retiredRole = service.getProviderRole(1002);
		assertTrue(retiredRole.getRetired());
		assertEquals("test reason", retiredRole.getRetireReason());
	}

	@Test
	public void unretireProviderRole_shouldUnretireProviderRole() {
		ProviderRole role = service.getProviderRole(1010);
		assertTrue(role.getRetired());

		service.unretireProviderRole(role);

		role = service.getProviderRole(1010);
		assertFalse(role.getRetired());
	}
	
}

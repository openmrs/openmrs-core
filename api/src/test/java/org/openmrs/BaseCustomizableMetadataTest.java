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

import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

public class BaseCustomizableMetadataTest extends BaseContextSensitiveTest {
	
	private static final String PROVIDERS_INITIAL_XML = "org/openmrs/api/include/ProviderServiceTest-initial.xml";
	
	private static final String PROVIDER_ATTRIBUTE_TYPES_XML = "org/openmrs/api/include/ProviderServiceTest-providerAttributes.xml";
	
	private static final String EXTRA_ATTRIBUTE_TYPES_XML = "org/openmrs/api/include/BaseCustomizableMetadataTest-attributesAndTypes.xml";
	
	private ProviderService service;
	
	@Before
	public void before() {
		service = Context.getProviderService();
		executeDataSet(PROVIDERS_INITIAL_XML);
		executeDataSet(PROVIDER_ATTRIBUTE_TYPES_XML);
		executeDataSet(EXTRA_ATTRIBUTE_TYPES_XML);
	}
	
	/**
	 * @see org.openmrs.BaseCustomizableMetadata#setAttribute(org.openmrs.attribute.Attribute)
	 */
	@Test
	public void setAttribute_shouldVoidTheAttributeIfAnAttributeWithSameAttributeTypeAlreadyExistsAndTheMaxOccursIsSetTo1()
	{
		Provider provider = new Provider();
		provider.setIdentifier("test");
		
		provider.setPerson(newPerson("name"));
		
		ProviderAttributeType place = service.getProviderAttributeType(3);
		provider.setAttribute(buildProviderAttribute(place, "bangalore"));
		provider.setAttribute(buildProviderAttribute(place, "chennai"));
		
		Assert.assertEquals(1, provider.getAttributes().size());
		
		service.saveProvider(provider);
		Assert.assertNotNull(provider.getId());
		
		provider.setAttribute(buildProviderAttribute(place, "seattle"));
		Assert.assertEquals(2, provider.getAttributes().size());
		ProviderAttribute lastAttribute = (ProviderAttribute) provider.getAttributes().toArray()[0];
		Assert.assertTrue(lastAttribute.getVoided());
	}
	
	/**
	 * @see org.openmrs.BaseCustomizableMetadata#setAttribute(org.openmrs.attribute.Attribute)
	 */
	@Test
	public void setAttribute_shouldWorkForAttriubutesWithDatatypesWhoseValuesAreStoredInOtherTables() {
		Provider provider = new Provider();
		provider.setIdentifier("test");
		
		provider.setPerson(newPerson("name"));
		
		ProviderAttributeType cv = service.getProviderAttributeType(4);
		provider.setAttribute(buildProviderAttribute(cv, "Worked lots of places..."));
		
		service.saveProvider(provider);
		Context.flushSession();
		Assert.assertNotNull(provider.getId());
		Assert.assertEquals(1, provider.getAttributes().size());
		
		provider.setAttribute(buildProviderAttribute(cv, "Worked even more places..."));
		service.saveProvider(provider);
		Assert.assertEquals(2, provider.getAttributes().size());
		ProviderAttribute lastAttribute = (ProviderAttribute) provider.getAttributes().toArray()[0];
		Assert.assertTrue(lastAttribute.getVoided());
	}
	
	private ProviderAttribute buildProviderAttribute(ProviderAttributeType providerAttributeType, Object value)
	{
		ProviderAttribute providerAttribute = new ProviderAttribute();
		providerAttribute.setAttributeType(providerAttributeType);
		providerAttribute.setValue(value.toString());
		return providerAttribute;
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

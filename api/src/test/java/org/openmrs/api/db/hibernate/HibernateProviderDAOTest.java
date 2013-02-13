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
package org.openmrs.api.db.hibernate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HibernateProviderDAOTest extends BaseContextSensitiveTest {
	
	private static final String PROVIDERS_INITIAL_XML = "org/openmrs/api/include/ProviderServiceTest-initial.xml";
	
	private HibernatePersonDAO personDao = null;
	
	private ProviderService service;
	
	/**
	 * Run this before each unit test in this class.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		service = Context.getProviderService();
		
		if (personDao == null)
			personDao = (HibernatePersonDAO) applicationContext.getBean("personDAO");
		
		executeDataSet(PROVIDERS_INITIAL_XML);
	}
	
	/**
	 * @see HibernateProviderDAO#getProvidersByPerson(Person,boolean)
	 * @verifies not return retired providers if includeRetired false
	 */
	@Test
	public void getProvidersByPerson_shouldNotReturnRetiredProvidersIfIncludeRetiredFalse() throws Exception {
		Collection<Provider> providers = service.getProvidersByPerson(personDao.getPerson(2), false);
		Assert.assertEquals(1, providers.size());
		Assert.assertFalse(providers.iterator().next().isRetired());
	}
	
	/**
	 * @see HibernateProviderDAO#getProvidersByPerson(Person,boolean)
	 * @verifies list retired providers at the end
	 */
	@Test
	public void getProvidersByPerson_shouldListRetiredProvidersAtTheEnd() throws Exception {
		List<Provider> providers = new ArrayList<Provider>();
		providers = (List<Provider>) service.getProvidersByPerson(personDao.getPerson(2), true);
		
		Assert.assertEquals(true, providers.get(1).getRetired());
	}
	
	/**
	 * @see HibernateProviderDAO#getProvidersByPerson(Person,boolean)
	 * @verifies return all providers if includeRetired true
	 */
	@Test
	public void getProvidersByPerson_shouldReturnAllProvidersIfIncludeRetiredTrue() throws Exception {
		Assert.assertEquals(2, service.getProvidersByPerson(personDao.getPerson(2), true).size());
	}
}

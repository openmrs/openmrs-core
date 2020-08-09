/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

public class HibernateProviderDAOTest extends BaseContextSensitiveTest {
	
	private static final String PROVIDERS_INITIAL_XML = "org/openmrs/api/include/ProviderServiceTest-initial.xml";
	
	private HibernatePersonDAO personDao = null;
	
	private ProviderService service;
	
	/**
	 * Run this before each unit test in this class.
	 * 
	 * @throws Exception
	 */
	@BeforeEach
	public void runBeforeEachTest() {
		service = Context.getProviderService();
		
		if (personDao == null)
			personDao = (HibernatePersonDAO) applicationContext.getBean("personDAO");
		
		executeDataSet(PROVIDERS_INITIAL_XML);
	}
	
	/**
	 * @see HibernateProviderDAO#getProvidersByPerson(Person,boolean)
	 */
	@Test
	public void getProvidersByPerson_shouldNotReturnRetiredProvidersIfIncludeRetiredFalse() {
		Collection<Provider> providers = service.getProvidersByPerson(personDao.getPerson(2), false);
		assertEquals(1, providers.size());
		assertFalse(providers.iterator().next().getRetired());
	}
	
	/**
	 * @see HibernateProviderDAO#getProvidersByPerson(Person,boolean)
	 */
	@Test
	public void getProvidersByPerson_shouldListRetiredProvidersAtTheEnd() {
		List<Provider> providers = new ArrayList<>();
		providers = (List<Provider>) service.getProvidersByPerson(personDao.getPerson(2), true);
		
		assertTrue(providers.get(1).getRetired());
	}
	
	/**
	 * @see HibernateProviderDAO#getProvidersByPerson(Person,boolean)
	 */
	@Test
	public void getProvidersByPerson_shouldReturnAllProvidersIfIncludeRetiredTrue() {
		assertEquals(2, service.getProvidersByPerson(personDao.getPerson(2), true).size());
	}
}

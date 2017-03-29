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

import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.api.db.PersonDAO;
import org.openmrs.api.db.ProviderDAO;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class ProviderDAOTest extends BaseContextSensitiveTest {
	
	private static final String PROVIDERS_INITIAL_XML = "org/openmrs/api/include/ProviderServiceTest-initial.xml";
	
	@Autowired
	private PersonDAO personDao;
	
	@Autowired
	private ProviderDAO providerDao;
	
	/**
	 * Run this before each unit test in this class.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() {
		executeDataSet(PROVIDERS_INITIAL_XML);
	}
	
	/**
	 * @see ProviderDAO#getProvidersByPerson(Person,boolean)
	 */
	@Test
	public void getProvidersByPerson_shouldNotReturnRetiredProvidersIfIncludeRetiredFalse() {
		Collection<Provider> providers = providerDao.getProvidersByPerson(personDao.getPerson(2), false);
		Assert.assertEquals(1, providers.size());
		Assert.assertFalse(providers.iterator().next().getRetired());
	}
	
	/**
	 * @see ProviderDAO#getProvidersByPerson(Person,boolean)
	 */
	@Test
	public void getProvidersByPerson_shouldListRetiredProvidersAtTheEnd() {
		List<Provider> providers = (List<Provider>) providerDao.getProvidersByPerson(personDao.getPerson(2), true);
		Assert.assertEquals(true, providers.get(1).getRetired());
	}
	
	/**
	 * @see ProviderDAO#getProvidersByPerson(Person,boolean)
	 */
	@Test
	public void getProvidersByPerson_shouldReturnAllProvidersIfIncludeRetiredTrue() {
		Assert.assertEquals(2, providerDao.getProvidersByPerson(personDao.getPerson(2), true).size());
	}
}

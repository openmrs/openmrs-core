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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class PersonAttributeHelperTest extends BaseContextSensitiveTest {
	
	private final static Log log = LogFactory.getLog(PersonAttributeHelperTest.class);
	
	private final static String PEOPLE_FROM_THE_SHIRE_XML = "org/openmrs/api/db/hibernate/include/HibernatePersonDAOTest-people.xml";
	
	private SessionFactory sessionFactory = null;
	
	private PersonAttributeHelper helper = null;
	
	@Before
	public void getPersonDAO() throws Exception {
		executeDataSet(PEOPLE_FROM_THE_SHIRE_XML);
		sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
		helper = new PersonAttributeHelper(sessionFactory);
	}
	
	/**
	 * @verifies return true if a person attribute exists
	 * @see PersonAttributeHelper#personAttributeExists(String)
	 */
	@Test
	public void personAttributeExists_shouldReturnTrueIfAPersonAttributeExists() throws Exception {
		Assert.assertTrue(helper.personAttributeExists("Master thief"));
		Assert.assertTrue(helper.personAttributeExists("Senior ring bearer"));
		Assert.assertTrue(helper.personAttributeExists("Story teller"));
		Assert.assertTrue(helper.personAttributeExists("Porridge with honey"));
		Assert.assertTrue(helper.personAttributeExists("Mushroom pie"));
		
		Assert.assertFalse(helper.personAttributeExists("Unexpected attribute value"));
	}
	
	/**
	 * @verifies return true if a voided person attribute exists
	 * @see PersonAttributeHelper#voidedPersonAttributeExists(String)
	 */
	@Test
	public void voidedPersonAttributeExists_shouldReturnTrueIfAVoidedPersonAttributeExists() throws Exception {
		Assert.assertTrue(helper.voidedPersonAttributeExists("Master thief"));
		Assert.assertTrue(helper.voidedPersonAttributeExists("Mushroom pie"));
		
		Assert.assertFalse(helper.voidedPersonAttributeExists("Unexpected attribute value"));
	}
	
	/**
	 * @verifies return true if a non-voided person attribute exists
	 * @see PersonAttributeHelper#nonVoidedPersonAttributeExists(String)
	 */
	@Test
	public void nonVoidedPersonAttributeExists_shouldReturnTrueIfANonvoidedPersonAttributeExists() throws Exception {
		Assert.assertTrue(helper.nonVoidedPersonAttributeExists("Story teller"));
		Assert.assertTrue(helper.nonVoidedPersonAttributeExists("Porridge with honey"));
		
		Assert.assertFalse(helper.nonVoidedPersonAttributeExists("Unexpected attribute value"));
	}
	
	/**
	 * @verifies return true if a non-searchable person attribute exists
	 * @see PersonAttributeHelper#nonSearchablePersonAttributeExists(String)
	 */
	@Test
	public void nonSearchablePersonAttributeExists_shouldReturnTrueIfANonsearchablePersonAttributeExists() throws Exception {
		Assert.assertFalse(helper.nonSearchablePersonAttributeExists("Master thief"));
		Assert.assertFalse(helper.nonSearchablePersonAttributeExists("Senior ring bearer"));
		Assert.assertFalse(helper.nonSearchablePersonAttributeExists("Story teller"));
		
		Assert.assertTrue(helper.nonSearchablePersonAttributeExists("Porridge with honey"));
		Assert.assertTrue(helper.nonSearchablePersonAttributeExists("Mushroom pie"));
		
		Assert.assertFalse(helper.nonSearchablePersonAttributeExists("Unexpected attribute value"));
	}
	
	/**
	 * @verifies return true if a searchable person attribute exists
	 * @see PersonAttributeHelper#searchablePersonAttributeExists(String)
	 */
	@Test
	public void searchablePersonAttributeExists_shouldReturnTrueIfASearchablePersonAttributeExists() throws Exception {
		Assert.assertTrue(helper.searchablePersonAttributeExists("Master thief"));
		Assert.assertTrue(helper.searchablePersonAttributeExists("Senior ring bearer"));
		Assert.assertTrue(helper.searchablePersonAttributeExists("Story teller"));
		
		Assert.assertFalse(helper.searchablePersonAttributeExists("Porridge with honey"));
		Assert.assertFalse(helper.searchablePersonAttributeExists("Mushroom pie"));
		
		Assert.assertFalse(helper.nonSearchablePersonAttributeExists("Unexpected attribute value"));
	}
}

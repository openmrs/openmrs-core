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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersonAttributeHelperTest extends BaseContextSensitiveTest {
	
	private static final Logger log = LoggerFactory.getLogger(PersonAttributeHelperTest.class);
	
	private final static String PEOPLE_FROM_THE_SHIRE_XML = "org/openmrs/api/db/hibernate/include/HibernatePersonDAOTest-people.xml";
	
	private SessionFactory sessionFactory = null;
	
	private PersonAttributeHelper helper = null;
	
	@BeforeEach
	public void getPersonDAO() {
		executeDataSet(PEOPLE_FROM_THE_SHIRE_XML);
		sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
		helper = new PersonAttributeHelper(sessionFactory);
	}
	
	/**
	 * @see PersonAttributeHelper#personAttributeExists(String)
	 */
	@Test
	public void personAttributeExists_shouldReturnTrueIfAPersonAttributeExists() {
		assertTrue(helper.personAttributeExists("Master thief"));
		assertTrue(helper.personAttributeExists("Senior ring bearer"));
		assertTrue(helper.personAttributeExists("Story teller"));
		assertTrue(helper.personAttributeExists("Porridge with honey"));
		assertTrue(helper.personAttributeExists("Mushroom pie"));
		
		assertFalse(helper.personAttributeExists("Unexpected attribute value"));
	}
	
	/**
	 * @see PersonAttributeHelper#voidedPersonAttributeExists(String)
	 */
	@Test
	public void voidedPersonAttributeExists_shouldReturnTrueIfAVoidedPersonAttributeExists() {
		assertTrue(helper.voidedPersonAttributeExists("Master thief"));
		assertTrue(helper.voidedPersonAttributeExists("Mushroom pie"));
		
		assertFalse(helper.voidedPersonAttributeExists("Unexpected attribute value"));
	}
	
	/**
	 * @see PersonAttributeHelper#nonVoidedPersonAttributeExists(String)
	 */
	@Test
	public void nonVoidedPersonAttributeExists_shouldReturnTrueIfANonvoidedPersonAttributeExists() {
		assertTrue(helper.nonVoidedPersonAttributeExists("Story teller"));
		assertTrue(helper.nonVoidedPersonAttributeExists("Porridge with honey"));
		
		assertFalse(helper.nonVoidedPersonAttributeExists("Unexpected attribute value"));
	}
	
	/**
	 * @see PersonAttributeHelper#nonSearchablePersonAttributeExists(String)
	 */
	@Test
	public void nonSearchablePersonAttributeExists_shouldReturnTrueIfANonsearchablePersonAttributeExists() {
		assertFalse(helper.nonSearchablePersonAttributeExists("Master thief"));
		assertFalse(helper.nonSearchablePersonAttributeExists("Senior ring bearer"));
		assertFalse(helper.nonSearchablePersonAttributeExists("Story teller"));
		
		assertTrue(helper.nonSearchablePersonAttributeExists("Porridge with honey"));
		assertTrue(helper.nonSearchablePersonAttributeExists("Mushroom pie"));
		
		assertFalse(helper.nonSearchablePersonAttributeExists("Unexpected attribute value"));
	}
	
	/**
	 * @see PersonAttributeHelper#searchablePersonAttributeExists(String)
	 */
	@Test
	public void searchablePersonAttributeExists_shouldReturnTrueIfASearchablePersonAttributeExists() {
		assertTrue(helper.searchablePersonAttributeExists("Master thief"));
		assertTrue(helper.searchablePersonAttributeExists("Senior ring bearer"));
		assertTrue(helper.searchablePersonAttributeExists("Story teller"));
		
		assertFalse(helper.searchablePersonAttributeExists("Porridge with honey"));
		assertFalse(helper.searchablePersonAttributeExists("Mushroom pie"));
		
		assertFalse(helper.nonSearchablePersonAttributeExists("Unexpected attribute value"));
	}
}

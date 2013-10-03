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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class HibernatePersonAttributeHelperTest extends BaseContextSensitiveTest {
	
	private final static Log log = LogFactory.getLog(HibernatePersonAttributeHelperTest.class);
	
	private final static String PEOPLE_FROM_THE_SHIRE_XML = "org/openmrs/api/db/hibernate/include/HibernatePersonDAOTest-people.xml";
	
	private SessionFactory sessionFactory = null;
	
	private HibernatePersonAttributeHelper helper = null;
	
	@Before
	public void getPersonDAO() throws Exception {
		executeDataSet(PEOPLE_FROM_THE_SHIRE_XML);
		sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
		helper = new HibernatePersonAttributeHelper(sessionFactory);
	}
	
	/**
	 * @verifies return true if a person attribute exists
	 * @see HibernatePersonAttributeHelper#personAttributeExists(String)
	 */
	@Test
	public void personAttributeExists_shouldReturnTrueIfAPersonAttributeExists() throws Exception {
		// assert that all 6 people from PEOPLE_FROM_THE_SHIRE_XML (who are neither dead nor voided) are retrieved
		//
		Assert.assertTrue(helper.personAttributeExists("Master thief"));
		Assert.assertTrue(helper.personAttributeExists("Senior ring bearer"));
		Assert.assertTrue(helper.personAttributeExists("Story writer"));
		Assert.assertTrue(helper.personAttributeExists("Porridge with honey"));
		Assert.assertTrue(helper.personAttributeExists("Junior ring bearer"));
		Assert.assertTrue(helper.personAttributeExists("Mushroom pie"));
		
		Assert.assertFalse(helper.personAttributeExists("Unexpected attribute value"));
	}
	
	/**
	 * @verifies return true if a voided person attribute exists
	 * @see HibernatePersonAttributeHelper#voidedPersonAttributeExists(String)
	 */
	@Test
	public void voidedPersonAttributeExists_shouldReturnTrueIfAVoidedPersonAttributeExists() throws Exception {
		Assert.assertTrue(helper.voidedPersonAttributeExists("Master thief"));
		Assert.assertTrue(helper.voidedPersonAttributeExists("Mushroom pie"));
		
		Assert.assertFalse(helper.personAttributeExists("Unexpected attribute value"));
	}
	
	/**
	 * @verifies return true if a non-searchable person attribute exists
	 * @see HibernatePersonAttributeHelper#nonSearchablePersonAttributeExists(String)
	 */
	@Test
	public void nonSearchablePersonAttributeExists_shouldReturnTrueIfANonsearchablePersonAttributeExists() throws Exception {
		Assert.assertFalse(helper.nonSearchablePersonAttributeExists("Master thief"));
		Assert.assertFalse(helper.nonSearchablePersonAttributeExists("Senior ring bearer"));
		Assert.assertFalse(helper.nonSearchablePersonAttributeExists("Story writer"));
		Assert.assertFalse(helper.nonSearchablePersonAttributeExists("Junior ring bearer"));
		
		Assert.assertTrue(helper.nonSearchablePersonAttributeExists("Porridge with honey"));
		Assert.assertTrue(helper.nonSearchablePersonAttributeExists("Mushroom pie"));
		
		Assert.assertFalse(helper.nonSearchablePersonAttributeExists("Unexpected attribute value"));
	}
}

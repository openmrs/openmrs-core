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
package org.openmrs.api.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

public class PersonDAOTest extends BaseContextSensitiveTest {
	
	private PersonDAO dao = null;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		// fetch the dao from the spring application context 
		// this bean name matches the name in /metadata/spring/applicationContext-service.xml 
		dao = (PersonDAO) applicationContext.getBean("personDAO");
	}
	
	/**
	 * @see {@link PersonDAO#getSavedPersonAttributeTypeName(org.openmrs.PersonAttributeType)}
	 */
	@Test
	@Verifies(value = "should get saved personAttributeType name from database", method = "getSavedPersonAttributeTypeName(org.openmrs.PersonAttributeType)")
	public void getSavedPersonAttributeTypeName_shouldGetSavedPersonAttributeTypeNameFromDatabase() throws Exception {
		PersonAttributeType pat = Context.getPersonService().getPersonAttributeType(1);
		
		// save the name from the db for later checks
		String origName = pat.getName();
		String newName = "Race Updated";
		
		assertFalse(newName.equals(origName));
		
		// change the name on the java pojo (NOT in the database)
		pat.setName(newName);
		
		// the value from the database should match the original name from the 
		// pat right after /it/ was fetched from the database
		String nameFromDatabase = dao.getSavedPersonAttributeTypeName(pat);
		assertEquals(origName, nameFromDatabase);
	}
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PersonDAOTest extends BaseContextSensitiveTest {
	
	private PersonDAO dao = null;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@BeforeEach
	public void runBeforeEachTest() {
		// fetch the dao from the spring application context 
		// this bean name matches the name in /metadata/spring/applicationContext-service.xml 
		dao = (PersonDAO) applicationContext.getBean("personDAO");
	}
	
	/**
	 * @see PersonDAO#getSavedPersonAttributeTypeName(org.openmrs.PersonAttributeType)
	 */
	@Test
	public void getSavedPersonAttributeTypeName_shouldGetSavedPersonAttributeTypeNameFromDatabase() {
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
	
	@Test
	public void getPersonName_shouldGetSavedPersonNameById() {
		PersonName personName = dao.getPersonName(2);
		assertEquals(2, (int) personName.getId());
	}
	
	@Test
	public void getPersonName_shouldNotGetPersonNameGivenInvalidId() {
		PersonName personName = dao.getPersonName(-1);
		assertNull(personName);
	}
	
	@Test
	public void getSavedPersonAttributeTypeSearchable_shouldFetchSearchablePropertyForAPersonAttributeTypeBypassingCache(){
		PersonAttributeType pat = dao.getPersonAttributeType(1);
		pat.setSearchable(true);
		// should still be false in the DB
		Boolean searchable = dao.getSavedPersonAttributeTypeSearchable(pat);
		assertFalse(searchable);
	}
	
}

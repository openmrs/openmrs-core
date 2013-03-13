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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PatientIdentifierType;
import org.openmrs.test.BaseContextSensitiveTest;

public class HibernatePatientDAOTest extends BaseContextSensitiveTest {
	
	private HibernatePatientDAO dao = null;
	
	/**
	 * Run this before each unit test in this class.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		
		if (dao == null)
			// fetch the dao from the spring application context
			dao = (HibernatePatientDAO) applicationContext.getBean("patientDAO");
	}
	
	/**
	 * @see HibernatePatientDAO#getAllPatientIdentifierTypes(boolean)
	 * @verifies not return null when includeRetired is false
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldNotReturnNullWhenIncludeRetiredIsFalse() throws Exception {
		Assert.assertNotNull(dao.getAllPatientIdentifierTypes(false));
	}
	
	/**
	 * @see HibernatePatientDAO#getAllPatientIdentifierTypes(boolean)
	 * @verifies not return retired when includeRetired is false
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldNotReturnRetiredWhenIncludeRetiredIsFalse() throws Exception {
		List<PatientIdentifierType> patientIdentifierTypes = dao.getAllPatientIdentifierTypes(false);
		Assert.assertEquals("patientIdentifierTypes list should have 2 elements", 2, patientIdentifierTypes.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getAllPatientIdentifierTypes(boolean)
	 * @verifies not return null when includeRetired is true
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldNotReturnNullWhenIncludeRetiredIsTrue() throws Exception {
		Assert.assertNotNull(dao.getAllPatientIdentifierTypes(true));
	}
	
	/**
	 * @see HibernatePatientDAO#getAllPatientIdentifierTypes(boolean)
	 * @verifies return all when includeRetired is true
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldReturnAllWhenIncludeRetiredIsTrue() throws Exception {
		List<PatientIdentifierType> patientIdentifierTypes = dao.getAllPatientIdentifierTypes(true);
		Assert.assertEquals("patientIdentifierTypes list should have 3 elements", 3, patientIdentifierTypes.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getAllPatientIdentifierTypes(boolean)
	 * @verifies return ordered
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldReturnOrdered() throws Exception {
		//given
		PatientIdentifierType patientIdentifierType1 = dao.getPatientIdentifierType(1); //non retired, non required
		
		PatientIdentifierType patientIdentifierType2 = dao.getPatientIdentifierType(2); //non retired, required
		patientIdentifierType2.setRequired(true);
		dao.savePatientIdentifierType(patientIdentifierType2);
		
		PatientIdentifierType patientIdentifierType4 = dao.getPatientIdentifierType(4); //retired
		
		//when
		List<PatientIdentifierType> all = dao.getAllPatientIdentifierTypes(true);
		
		//then
		Assert.assertArrayEquals(new Object[] { patientIdentifierType2, patientIdentifierType1, patientIdentifierType4 },
		    all.toArray());
	}
}

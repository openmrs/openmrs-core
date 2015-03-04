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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PatientIdentifierType;
import org.openmrs.test.BaseContextSensitiveTest;

import java.util.List;

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
	 * @verifies not return null excluding retired
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldNotReturnNullExcludingRetired() throws Exception {
		Assert.assertNotNull(dao.getAllPatientIdentifierTypes(false));
	}
	
	/**
	 * @see HibernatePatientDAO#getAllPatientIdentifierTypes(boolean)
	 * @verifies not return retired
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldNotReturnRetired() throws Exception {
		List<PatientIdentifierType> patientIdentifierTypes = dao.getAllPatientIdentifierTypes(false);
		Assert.assertEquals("patientIdentifierTypes list should have 2 elements", 2, patientIdentifierTypes.size());
	}
	
	/**
	 * @see HibernatePatientDAO#getAllPatientIdentifierTypes(boolean)
	 * @verifies not return null including retired
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldNotReturnNullIncludingRetired() throws Exception {
		Assert.assertNotNull(dao.getAllPatientIdentifierTypes(true));
	}
	
	/**
	 * @see HibernatePatientDAO#getAllPatientIdentifierTypes(boolean)
	 * @verifies return all
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldReturnAll() throws Exception {
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

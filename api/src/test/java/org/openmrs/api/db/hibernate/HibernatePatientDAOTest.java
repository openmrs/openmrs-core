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
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.test.BaseContextSensitiveTest;

import java.util.Collections;
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
	
	/**
	 * @see HibernatePatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 * @verifies return non retired patient identifier types with given name
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnNonRetiredPatientIdentifierTypesWithGivenName() {
		PatientIdentifierType oldIdNumberNonRetired = dao.getPatientIdentifierType(2);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes("Old Identification Number",
		    null, null, null);
		
		Assert.assertEquals(patientIdentifierTypes.size(), 1);
		Assert.assertEquals(oldIdNumberNonRetired, patientIdentifierTypes.get(0));
	}
	
	/**
	 * @see HibernatePatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 * @verifies return non retired patient identifier types with given format
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnNonRetiredPatientIdentifierTypesWithGivenFormat() {
		PatientIdentifierType formatOneNonRetired = dao.getPatientIdentifierType(1);
		formatOneNonRetired.setFormat("1");
		dao.savePatientIdentifierType(formatOneNonRetired);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes(null, "1", null, null);
		
		Assert.assertEquals(patientIdentifierTypes.size(), 1);
		Assert.assertEquals(formatOneNonRetired, patientIdentifierTypes.get(0));
	}
	
	/**
	 * @see HibernatePatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 * @verifies return non retired patient identifie types that are not required
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnNonRetiredPatientIdentifierTypesThatAreNotRequired() {
		PatientIdentifierType nonRetiredNonRequired1 = dao.getPatientIdentifierType(1);
		PatientIdentifierType nonRetiredNonRequired2 = dao.getPatientIdentifierType(2);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes(null, null, false, null);
		
		Assert.assertEquals(patientIdentifierTypes.size(), 2);
		Assert.assertTrue(patientIdentifierTypes.contains(nonRetiredNonRequired1));
		Assert.assertTrue(patientIdentifierTypes.contains(nonRetiredNonRequired2));
	}
	
	/**
	 * @see HibernatePatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 * @verifies return non retired patient identifier types that are required
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnNonRetiredPatientIdentifierTypesThatAreRequired() {
		PatientIdentifierType nonRetiredRequired = dao.getPatientIdentifierType(4);
		nonRetiredRequired.setRetired(false);
		nonRetiredRequired.setRequired(true);
		dao.savePatientIdentifierType(nonRetiredRequired);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes(null, null, true, null);
		
		Assert.assertEquals(patientIdentifierTypes.size(), 1);
		Assert.assertEquals(nonRetiredRequired, patientIdentifierTypes.get(0));
	}
	
	/**
	 * @see HibernatePatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 * @verifies return non retired patient identifier types that has checkDigit
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnNonRetiredPatientIdentifierTypesThatHasCheckDigit() {
		PatientIdentifierType nonRetiredHasDigit = dao.getPatientIdentifierType(1);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes(null, null, null, true);
		
		Assert.assertEquals(patientIdentifierTypes.size(), 1);
		Assert.assertEquals(nonRetiredHasDigit, patientIdentifierTypes.get(0));
	}
	
	/**
	 * @see HibernatePatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 * @verifies return non retired patient identifier types that has not CheckDigit
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnNonRetiredPatientIdentifierTypesThatHasNotCheckDigit() {
		PatientIdentifierType nonRetiredHasNoDigit = dao.getPatientIdentifierType(2);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes(null, null, null, false);
		
		Assert.assertEquals(patientIdentifierTypes.size(), 1);
		Assert.assertEquals(nonRetiredHasNoDigit, patientIdentifierTypes.get(0));
	}
	
	/**
	 * @see HibernatePatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 * @verifies return only non retired patient identifier types
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnOnlyNonRetiredPatientIdentifierTypes() {
		PatientIdentifierType nonRetiredType1 = dao.getPatientIdentifierType(1);
		Assert.assertEquals(nonRetiredType1.getRetired(), false);
		
		PatientIdentifierType nonRetiredType2 = dao.getPatientIdentifierType(2);
		Assert.assertEquals(nonRetiredType2.getRetired(), false);
		
		PatientIdentifierType retiredType = dao.getPatientIdentifierType(4);
		Assert.assertEquals(retiredType.getRetired(), true);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes(null, null, null, null);
		
		Assert.assertEquals(patientIdentifierTypes.size(), 2);
		Assert.assertTrue(patientIdentifierTypes.contains(nonRetiredType1));
		Assert.assertTrue(patientIdentifierTypes.contains(nonRetiredType2));
	}
	
	/**
	 * @see HibernatePatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 * @verifies return non retired patient identifier types ordered by required first
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnNonRetiredPatientIdentifierTypes_OrderedByRequiredFirst() {
		PatientIdentifierType nonRetiredNonRequiredType = dao.getPatientIdentifierType(1);
		PatientIdentifierType nonRetiredRequiredType = dao.getPatientIdentifierType(2);
		nonRetiredRequiredType.setRequired(true);
		dao.savePatientIdentifierType(nonRetiredRequiredType);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes(null, null, null, null);
		
		Assert.assertArrayEquals(new Object[] { nonRetiredRequiredType, nonRetiredNonRequiredType }, patientIdentifierTypes
		        .toArray());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 * @verifies return non retired patient identifier types ordered by required and name
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnNonRetiredPatientIdentifierTypes_OrderedByRequiredAndName() {
		PatientIdentifierType openMRSIdNumber = dao.getPatientIdentifierType(1);
		
		PatientIdentifierType oldIdNumber = dao.getPatientIdentifierType(2);
		oldIdNumber.setRequired(true);
		dao.savePatientIdentifierType(oldIdNumber);
		
		PatientIdentifierType socialSecNumber = dao.getPatientIdentifierType(4);
		socialSecNumber.setName("ASecurityNumber");
		socialSecNumber.setRequired(true);
		socialSecNumber.setRetired(false);
		dao.savePatientIdentifierType(socialSecNumber);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes(null, null, null, null);
		
		Assert.assertArrayEquals(new Object[] { socialSecNumber, oldIdNumber, openMRSIdNumber }, patientIdentifierTypes
		        .toArray());
	}
	
	/**
	 * @see HibernatePatientDAO#getPatientIdentifierTypes(String, String, Boolean, Boolean)
	 * @verifies return non retired patient identifier types ordered by required name and type id
	 */
	@Test
	public void getPatientIdentifierTypes_shouldReturnNonRetiredPatientIdentifierTypes_OrderedByRequiredNameAndTypeId() {
		PatientIdentifierType openMRSIdNumber = dao.getPatientIdentifierType(1);
		openMRSIdNumber.setName("IdNumber");
		openMRSIdNumber.setRequired(true);
		dao.savePatientIdentifierType(openMRSIdNumber);
		
		PatientIdentifierType oldIdNumber = dao.getPatientIdentifierType(2);
		oldIdNumber.setName("IdNumber");
		oldIdNumber.setRequired(true);
		dao.savePatientIdentifierType(oldIdNumber);
		
		PatientIdentifierType socialSecNumber = dao.getPatientIdentifierType(4);
		socialSecNumber.setRequired(true);
		socialSecNumber.setRetired(false);
		dao.savePatientIdentifierType(socialSecNumber);
		
		List<PatientIdentifierType> patientIdentifierTypes = dao.getPatientIdentifierTypes(null, null, null, null);
		
		Assert.assertArrayEquals(new Object[] { openMRSIdNumber, oldIdNumber, socialSecNumber }, patientIdentifierTypes
		        .toArray());
	}

    @Test
    public void getPatients_shouldNotMatchVoidedPatients(){
        List<PatientIdentifierType> identifierTypes = Collections.emptyList();
        List<Patient> patients = dao.getPatients("Hornblower3", null, identifierTypes, false, 0, 11, false);
        Assert.assertEquals(1, patients.size());

        Patient patient = patients.get(0);
        patient.setVoided(true);
        dao.savePatient(patient);

        patients = dao.getPatients("Hornblower3", null, identifierTypes, false, 0, 11, false);
        Assert.assertEquals(0, patients.size());
    }
}

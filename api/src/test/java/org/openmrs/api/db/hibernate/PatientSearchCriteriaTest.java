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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.GlobalPropertiesTestHelper;
import org.openmrs.util.OpenmrsConstants;

public class PatientSearchCriteriaTest extends BaseContextSensitiveTest {
	
	private PatientSearchCriteria patientSearchCriteria;
	
	private GlobalPropertiesTestHelper globalPropertiesTestHelper;
	
	@BeforeEach
	public void setUp() {
		SessionFactory sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Patient.class);
		
		patientSearchCriteria = new PatientSearchCriteria(sessionFactory, criteria);
		globalPropertiesTestHelper = new GlobalPropertiesTestHelper(Context.getAdministrationService());
	}
	
	/**
	 * @see PatientSearchCriteria#getQueryParts(String)
	 */
	@Test
	public void getQueryParts_shouldProcessSimpleSpaceAsSeparator() {
		String[] actual = patientSearchCriteria.getQueryParts("Anton Bert Charles");
		String[] expected = { "Anton", "Bert", "Charles" };
		
		assertArrayEquals(expected, actual);
	}
	
	/**
	 * @see PatientSearchCriteria#getQueryParts(String)
	 */
	@Test
	public void getQueryParts_shouldProcessCommaAsSeparator() {
		String[] actual = patientSearchCriteria.getQueryParts("Anton,Bert,Charles");
		String[] expected = { "Anton", "Bert", "Charles" };
		
		assertArrayEquals(expected, actual);
	}
	
	/**
	 * @see PatientSearchCriteria#getQueryParts(String)
	 */
	@Test
	public void getQueryParts_shouldProcessMixedSeparators() {
		String[] actual = patientSearchCriteria.getQueryParts("Anton,Bert, Charles Dorian  Ernie");
		String[] expected = { "Anton", "Bert", "Charles", "Dorian", "Ernie" };
		
		assertArrayEquals(expected, actual);
	}
	
	/**
	 * @see PatientSearchCriteria#getQueryParts(String)
	 */
	@Test
	public void getQueryParts_shouldNotReturnEmptyNameParts() {
		String[] actual = patientSearchCriteria.getQueryParts(" Anton  Bert   Charles ");
		String[] expected = { "Anton", "Bert", "Charles" };
		
		assertArrayEquals(expected, actual);
	}
	
	/**
	 * @see PatientSearchCriteria#getQueryParts(String)
	 */
	@Test
	public void getQueryParts_shouldRejectNullAsName() {
		assertThrows(IllegalArgumentException.class, () -> patientSearchCriteria.getQueryParts(null));
	}
	
	/**
	 * @see PatientSearchCriteria#isShortName(String)
	 */
	@Test
	public void isShortName_shouldRecogniseShortName() {
		assertTrue(patientSearchCriteria.isShortName("J"));
	}
	
	/**
	 * @see PatientSearchCriteria#isShortName(String)
	 */
	@Test
	public void isShortName_shouldRecogniseLongName() {
		assertFalse(patientSearchCriteria.isShortName("Jo"));
	}
	
	/**
	 * @see PatientSearchCriteria#getMatchMode()
	 */
	@Test
	public void getMatchMode_shouldReturnStartAsDefaultMatchMode() {
		globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE);
		assertEquals(MatchMode.START, patientSearchCriteria.getMatchMode());
	}
	
	/**
	 * @see PatientSearchCriteria#getMatchMode()
	 */
	@Test
	public void getMatchMode_shouldReturnStartAsConfiguredMatchMode() {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START);
		
		assertEquals(MatchMode.START, patientSearchCriteria.getMatchMode());
		
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
			    oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE);
		}
	}
	
	/**
	 * @see PatientSearchCriteria#getMatchMode()
	 */
	@Test
	public void getMatchMode_shouldReturnAnywhereAsConfiguredMatchMode() {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_ANYWHERE);
		
		assertEquals(MatchMode.ANYWHERE, patientSearchCriteria.getMatchMode());
		
		if (oldPropertyValue != null) {
			globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
			    oldPropertyValue);
		} else {
			globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE);
		}
	}
	
	/**
	 * @see PatientSearchCriteria#getSearchMode(String, String, java.util.List, boolean)
	 */
	@Test
	public void getSearchMode_shouldIdentifySearchByName() {
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME, patientSearchCriteria.getSearchMode("name", null,
		    null, false));
		
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME, patientSearchCriteria.getSearchMode("name", "", null,
		    false));
		
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME, patientSearchCriteria.getSearchMode("name", "  \n\t",
		    null, false));
		
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME, patientSearchCriteria.getSearchMode("name", null,
				new ArrayList<>(), false));
		
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME, patientSearchCriteria.getSearchMode("name", "",
				new ArrayList<>(), false));
	}
	
	/**
	 * @see PatientSearchCriteria#getSearchMode(String, String, java.util.List, boolean)
	 */
	@Test
	public void getSearchMode_shouldIdentifySearchByIdentifier() {
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode(null,
		    "identifier", null, false));
		
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("",
		    "identifier", null, false));
		
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("  \n\t",
		    "identifier", null, false));
	}
	
	/**
	 * @see PatientSearchCriteria#getSearchMode(String, String, java.util.List, boolean)
	 */
	@Test
	public void getSearchMode_shouldIdentifySearchByIdentifierTypeList() {
		List<PatientIdentifierType> patientIdentifierTypeList = new ArrayList<>();
		patientIdentifierTypeList.add(new PatientIdentifierType());
		
		// testing variations of empty or blank value for name
		//
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode(null, null,
		    patientIdentifierTypeList, false));
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("", null,
		    patientIdentifierTypeList, false));
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("  \n\t",
		    null, patientIdentifierTypeList, false));
		
		// testing variations of empty or blank value for identifier
		//
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode(null, "",
		    patientIdentifierTypeList, false));
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode(null,
		    "  \n\t", patientIdentifierTypeList, false));
		
		// testing variations of empty or blank values for name and identifier
		//
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("", "",
		    patientIdentifierTypeList, false));
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("  \n\t",
		    "", patientIdentifierTypeList, false));
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("", "\n\t",
		    patientIdentifierTypeList, false));
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("  \n\t",
		    "  \n\t", patientIdentifierTypeList, false));
	}
	
	/**
	 * @see PatientSearchCriteria#getSearchMode(String, String, java.util.List, boolean)
	 */
	@Test
	public void getSearchMode_shouldIdentifySearchByIdentifierAndIdentifierTypeList() {
		List<PatientIdentifierType> patientIdentifierTypeList = new ArrayList<>();
		patientIdentifierTypeList.add(new PatientIdentifierType());
		
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode(null,
		    "identifier", patientIdentifierTypeList, false));
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("",
		    "identifier", patientIdentifierTypeList, false));
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("  \n\t",
		    "identifier", patientIdentifierTypeList, false));
	}
	
	/**
	 * @see PatientSearchCriteria#getSearchMode(String, String, java.util.List, boolean)
	 */
	@Test
	public void getSearchMode_shouldIdentifySearchByNameOrIdentifier() {
		List<PatientIdentifierType> patientIdentifierTypeList = new ArrayList<>();
		patientIdentifierTypeList.add(new PatientIdentifierType());
		
		// test cases where "name" is the only parameter that is non-empty and non-blank
		//
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", null, null, true));
		
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", "", null, true));
		
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", "  \n\t", null, true));
		
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", null, new ArrayList<>(), true));
		
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", "", new ArrayList<>(), true));
		
		// test cases where "identifier" is the only parameter that is non-empty and non-blank
		//
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    null, "identifier", null, true));
		
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode("",
		    "identifier", null, true));
		
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "  \n\t", "identifier", null, true));
		
		// test cases where 'patientIdentifierTypeList' is the only parameter that is non-empty and non-blank
		//
		//   testing variations of empty or blank value for name
		//
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    null, null, patientIdentifierTypeList, true));
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode("",
		    null, patientIdentifierTypeList, true));
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "  \n\t", null, patientIdentifierTypeList, true));
		
		//   testing variations of empty or blank value for identifier
		//
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    null, "", patientIdentifierTypeList, true));
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    null, "  \n\t", patientIdentifierTypeList, true));
		
		//   testing variations of empty or blank values for name and identifier
		//
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode("",
		    "", patientIdentifierTypeList, true));
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "  \n\t", "", patientIdentifierTypeList, true));
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode("",
		    "\n\t", patientIdentifierTypeList, true));
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "  \n\t", "  \n\t", patientIdentifierTypeList, true));
		
		// test cases where 'name' is the only parameter that is empty or blank
		//
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    null, "identifier", patientIdentifierTypeList, true));
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode("",
		    "identifier", patientIdentifierTypeList, true));
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "  \n\t", "identifier", patientIdentifierTypeList, true));
	}
	
	/**
	 * @see PatientSearchCriteria#getSearchMode(String, String, java.util.List, boolean)
	 */
	@Test
	public void getSearchMode_shouldIdentifySearchByNameAndIdentifier() {
		List<PatientIdentifierType> patientIdentifierTypeList = new ArrayList<>();
		patientIdentifierTypeList.add(new PatientIdentifierType());
		
		// test cases where "name" and "identifier" are the only parameters that are non-empty and non-blank
		//
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_AND_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", "identifier", null, false));
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_AND_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", "identifier", new ArrayList<>(), false));
		
		// test cases where "name" and 'identifierTypeList' are the only parameters that are non-empty and non-blank
		//
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_AND_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", null, patientIdentifierTypeList, false));
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_AND_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", "", patientIdentifierTypeList, false));
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_AND_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", "  \n\t ", patientIdentifierTypeList, false));
		
		// test case where all parameters are non-empty and non-blank
		assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_AND_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", "identifier", patientIdentifierTypeList, false));
	}
	
	/**
	 * @see PatientSearchCriteria#copySearchParameter(String, String)
	 */
	@Test
	public void copySearchParameter_shouldReturnSourceValueWhenTargetIsBlank() {
		assertEquals("source", patientSearchCriteria.copySearchParameter("source", null));
		assertEquals("source", patientSearchCriteria.copySearchParameter("source", ""));
		assertEquals("source", patientSearchCriteria.copySearchParameter("source", "   \n\t "));
	}
	
	/**
	 * @see PatientSearchCriteria#copySearchParameter(String, String)
	 */
	@Test
	public void copySearchParameter_shouldReturnTargetValueWhenTargetIsNonblank() {
		assertEquals("target", patientSearchCriteria.copySearchParameter(null, "target"));
		assertEquals("target", patientSearchCriteria.copySearchParameter("", "target"));
		assertEquals("target", patientSearchCriteria.copySearchParameter("   \n\t ", "target"));
		assertEquals("target", patientSearchCriteria.copySearchParameter("source", "target"));
	}
}

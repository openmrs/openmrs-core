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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.GlobalPropertiesTestHelper;
import org.openmrs.util.OpenmrsConstants;

public class PatientSearchCriteriaTest extends BaseContextSensitiveTest {
	
	private PatientSearchCriteria patientSearchCriteria;
	
	private GlobalPropertiesTestHelper globalPropertiesTestHelper;
	
	@Before
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
		
		Assert.assertArrayEquals(expected, actual);
	}
	
	/**
	 * @see PatientSearchCriteria#getQueryParts(String)
	 */
	@Test
	public void getQueryParts_shouldProcessCommaAsSeparator() {
		String[] actual = patientSearchCriteria.getQueryParts("Anton,Bert,Charles");
		String[] expected = { "Anton", "Bert", "Charles" };
		
		Assert.assertArrayEquals(expected, actual);
	}
	
	/**
	 * @see PatientSearchCriteria#getQueryParts(String)
	 */
	@Test
	public void getQueryParts_shouldProcessMixedSeparators() {
		String[] actual = patientSearchCriteria.getQueryParts("Anton,Bert, Charles Dorian  Ernie");
		String[] expected = { "Anton", "Bert", "Charles", "Dorian", "Ernie" };
		
		Assert.assertArrayEquals(expected, actual);
	}
	
	/**
	 * @see PatientSearchCriteria#getQueryParts(String)
	 */
	@Test
	public void getQueryParts_shouldNotReturnEmptyNameParts() {
		String[] actual = patientSearchCriteria.getQueryParts(" Anton  Bert   Charles ");
		String[] expected = { "Anton", "Bert", "Charles" };
		
		Assert.assertArrayEquals(expected, actual);
	}
	
	/**
	 * @see PatientSearchCriteria#getQueryParts(String)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getQueryParts_shouldRejectNullAsName() {
		patientSearchCriteria.getQueryParts(null);
	}
	
	/**
	 * @see PatientSearchCriteria#isShortName(String)
	 */
	@Test
	public void isShortName_shouldRecogniseShortName() {
		Assert.assertTrue(patientSearchCriteria.isShortName("J"));
	}
	
	/**
	 * @see PatientSearchCriteria#isShortName(String)
	 */
	@Test
	public void isShortName_shouldRecogniseLongName() {
		Assert.assertFalse(patientSearchCriteria.isShortName("Jo"));
	}
	
	/**
	 * @see PatientSearchCriteria#getMatchMode()
	 */
	@Test
	public void getMatchMode_shouldReturnStartAsDefaultMatchMode() {
		globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE);
		Assert.assertEquals(MatchMode.START, patientSearchCriteria.getMatchMode());
	}
	
	/**
	 * @see PatientSearchCriteria#getMatchMode()
	 */
	@Test
	public void getMatchMode_shouldReturnStartAsConfiguredMatchMode() {
		String oldPropertyValue = globalPropertiesTestHelper.setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START);
		
		Assert.assertEquals(MatchMode.START, patientSearchCriteria.getMatchMode());
		
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
		
		Assert.assertEquals(MatchMode.ANYWHERE, patientSearchCriteria.getMatchMode());
		
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
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME, patientSearchCriteria.getSearchMode("name", null,
		    null, false));
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME, patientSearchCriteria.getSearchMode("name", "", null,
		    false));
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME, patientSearchCriteria.getSearchMode("name", "  \n\t",
		    null, false));
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME, patientSearchCriteria.getSearchMode("name", null,
				new ArrayList<>(), false));
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME, patientSearchCriteria.getSearchMode("name", "",
				new ArrayList<>(), false));
	}
	
	/**
	 * @see PatientSearchCriteria#getSearchMode(String, String, java.util.List, boolean)
	 */
	@Test
	public void getSearchMode_shouldIdentifySearchByIdentifier() {
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode(null,
		    "identifier", null, false));
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("",
		    "identifier", null, false));
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("  \n\t",
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
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode(null, null,
		    patientIdentifierTypeList, false));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("", null,
		    patientIdentifierTypeList, false));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("  \n\t",
		    null, patientIdentifierTypeList, false));
		
		// testing variations of empty or blank value for identifier
		//
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode(null, "",
		    patientIdentifierTypeList, false));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode(null,
		    "  \n\t", patientIdentifierTypeList, false));
		
		// testing variations of empty or blank values for name and identifier
		//
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("", "",
		    patientIdentifierTypeList, false));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("  \n\t",
		    "", patientIdentifierTypeList, false));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("", "\n\t",
		    patientIdentifierTypeList, false));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("  \n\t",
		    "  \n\t", patientIdentifierTypeList, false));
	}
	
	/**
	 * @see PatientSearchCriteria#getSearchMode(String, String, java.util.List, boolean)
	 */
	@Test
	public void getSearchMode_shouldIdentifySearchByIdentifierAndIdentifierTypeList() {
		List<PatientIdentifierType> patientIdentifierTypeList = new ArrayList<>();
		patientIdentifierTypeList.add(new PatientIdentifierType());
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode(null,
		    "identifier", patientIdentifierTypeList, false));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("",
		    "identifier", patientIdentifierTypeList, false));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("  \n\t",
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
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", null, null, true));
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", "", null, true));
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", "  \n\t", null, true));
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", null, new ArrayList<>(), true));
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", "", new ArrayList<>(), true));
		
		// test cases where "identifier" is the only parameter that is non-empty and non-blank
		//
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    null, "identifier", null, true));
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode("",
		    "identifier", null, true));
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "  \n\t", "identifier", null, true));
		
		// test cases where 'patientIdentifierTypeList' is the only parameter that is non-empty and non-blank
		//
		//   testing variations of empty or blank value for name
		//
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    null, null, patientIdentifierTypeList, true));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode("",
		    null, patientIdentifierTypeList, true));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "  \n\t", null, patientIdentifierTypeList, true));
		
		//   testing variations of empty or blank value for identifier
		//
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    null, "", patientIdentifierTypeList, true));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    null, "  \n\t", patientIdentifierTypeList, true));
		
		//   testing variations of empty or blank values for name and identifier
		//
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode("",
		    "", patientIdentifierTypeList, true));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "  \n\t", "", patientIdentifierTypeList, true));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode("",
		    "\n\t", patientIdentifierTypeList, true));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "  \n\t", "  \n\t", patientIdentifierTypeList, true));
		
		// test cases where 'name' is the only parameter that is empty or blank
		//
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    null, "identifier", patientIdentifierTypeList, true));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode("",
		    "identifier", patientIdentifierTypeList, true));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
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
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_AND_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", "identifier", null, false));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_AND_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", "identifier", new ArrayList<>(), false));
		
		// test cases where "name" and 'identifierTypeList' are the only parameters that are non-empty and non-blank
		//
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_AND_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", null, patientIdentifierTypeList, false));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_AND_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", "", patientIdentifierTypeList, false));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_AND_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", "  \n\t ", patientIdentifierTypeList, false));
		
		// test case where all parameters are non-empty and non-blank
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_AND_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", "identifier", patientIdentifierTypeList, false));
	}
	
	/**
	 * @see PatientSearchCriteria#copySearchParameter(String, String)
	 */
	@Test
	public void copySearchParameter_shouldReturnSourceValueWhenTargetIsBlank() {
		Assert.assertEquals("source", patientSearchCriteria.copySearchParameter("source", null));
		Assert.assertEquals("source", patientSearchCriteria.copySearchParameter("source", ""));
		Assert.assertEquals("source", patientSearchCriteria.copySearchParameter("source", "   \n\t "));
	}
	
	/**
	 * @see PatientSearchCriteria#copySearchParameter(String, String)
	 */
	@Test
	public void copySearchParameter_shouldReturnTargetValueWhenTargetIsNonblank() {
		Assert.assertEquals("target", patientSearchCriteria.copySearchParameter(null, "target"));
		Assert.assertEquals("target", patientSearchCriteria.copySearchParameter("", "target"));
		Assert.assertEquals("target", patientSearchCriteria.copySearchParameter("   \n\t ", "target"));
		Assert.assertEquals("target", patientSearchCriteria.copySearchParameter("source", "target"));
	}
}

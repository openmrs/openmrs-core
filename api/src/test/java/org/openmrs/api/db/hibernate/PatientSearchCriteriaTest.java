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

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.util.GlobalPropertiesTestHelper;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;

import java.util.ArrayList;
import java.util.List;

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
	 * @verifies process simple space as separator
	 * @see PatientSearchCriteria#getQueryParts(String)
	 */
	@Test
	public void getQueryParts_shouldProcessSimpleSpaceAsSeparator() throws Exception {
		String[] actual = patientSearchCriteria.getQueryParts("Anton Bert Charles");
		String[] expected = { "Anton", "Bert", "Charles" };
		
		Assert.assertArrayEquals(expected, actual);
	}
	
	/**
	 * @verifies process comma as separator
	 * @see PatientSearchCriteria#getQueryParts(String)
	 */
	@Test
	public void getQueryParts_shouldProcessCommaAsSeparator() throws Exception {
		String[] actual = patientSearchCriteria.getQueryParts("Anton,Bert,Charles");
		String[] expected = { "Anton", "Bert", "Charles" };
		
		Assert.assertArrayEquals(expected, actual);
	}
	
	/**
	 * @verifies process mixed separators
	 * @see PatientSearchCriteria#getQueryParts(String)
	 */
	@Test
	public void getQueryParts_shouldProcessMixedSeparators() throws Exception {
		String[] actual = patientSearchCriteria.getQueryParts("Anton,Bert, Charles Dorian  Ernie");
		String[] expected = { "Anton", "Bert", "Charles", "Dorian", "Ernie" };
		
		Assert.assertArrayEquals(expected, actual);
	}
	
	/**
	 * @verifies not return empty name parts
	 * @see PatientSearchCriteria#getQueryParts(String)
	 */
	@Test
	public void getQueryParts_shouldNotReturnEmptyNameParts() throws Exception {
		String[] actual = patientSearchCriteria.getQueryParts(" Anton  Bert   Charles ");
		String[] expected = { "Anton", "Bert", "Charles" };
		
		Assert.assertArrayEquals(expected, actual);
	}
	
	/**
	 * @verifies reject null as name
	 * @see PatientSearchCriteria#getQueryParts(String)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getQueryParts_shouldRejectNullAsName() throws Exception {
		patientSearchCriteria.getQueryParts(null);
	}
	
	/**
	 * @verifies recognise short name
	 * @see PatientSearchCriteria#isShortName(String)
	 */
	@Test
	public void isShortName_shouldRecogniseShortName() throws Exception {
		Assert.assertTrue(patientSearchCriteria.isShortName("J"));
	}
	
	/**
	 * @verifies recognise long name
	 * @see PatientSearchCriteria#isShortName(String)
	 */
	@Test
	public void isShortName_shouldRecogniseLongName() throws Exception {
		Assert.assertFalse(patientSearchCriteria.isShortName("Jo"));
	}
	
	/**
	 * @verifies return start as default match mode
	 * @see PatientSearchCriteria#getMatchMode()
	 */
	@Test
	public void getMatchMode_shouldReturnStartAsDefaultMatchMode() throws Exception {
		globalPropertiesTestHelper.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE);
		Assert.assertEquals(MatchMode.START, patientSearchCriteria.getMatchMode());
	}
	
	/**
	 * @verifies return start as configured match mode
	 * @see PatientSearchCriteria#getMatchMode()
	 */
	@Test
	public void getMatchMode_shouldReturnStartAsConfiguredMatchMode() throws Exception {
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
	 * @verifies return anywhere as configured match mode
	 * @see PatientSearchCriteria#getMatchMode()
	 */
	@Test
	public void getMatchMode_shouldReturnAnywhereAsConfiguredMatchMode() throws Exception {
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
	 * @verifies identify search by name
	 * @see PatientSearchCriteria#getSearchMode(String, String, java.util.List, boolean)
	 */
	@Test
	public void getSearchMode_shouldIdentifySearchByName() throws Exception {
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME, patientSearchCriteria.getSearchMode("name", null,
		    null, false));
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME, patientSearchCriteria.getSearchMode("name", "", null,
		    false));
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME, patientSearchCriteria.getSearchMode("name", "  \n\t",
		    null, false));
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME, patientSearchCriteria.getSearchMode("name", null,
		    new ArrayList<PatientIdentifierType>(), false));
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME, patientSearchCriteria.getSearchMode("name", "",
		    new ArrayList<PatientIdentifierType>(), false));
	}
	
	/**
	 * @verifies identify search by identifier
	 * @see PatientSearchCriteria#getSearchMode(String, String, java.util.List, boolean)
	 */
	@Test
	public void getSearchMode_shouldIdentifySearchByIdentifier() throws Exception {
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode(null,
		    "identifier", null, false));
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("",
		    "identifier", null, false));
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("  \n\t",
		    "identifier", null, false));
	}
	
	/**
	 * @verifies identify search by identifier type list
	 * @see PatientSearchCriteria#getSearchMode(String, String, java.util.List, boolean)
	 */
	@Test
	public void getSearchMode_shouldIdentifySearchByIdentifierTypeList() throws Exception {
		List<PatientIdentifierType> patientIdentifierTypeList = new ArrayList<PatientIdentifierType>();
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
	 * @verifies identify search by identifier and identifier type list
	 * @see PatientSearchCriteria#getSearchMode(String, String, java.util.List, boolean)
	 */
	@Test
	public void getSearchMode_shouldIdentifySearchByIdentifierAndIdentifierTypeList() throws Exception {
		List<PatientIdentifierType> patientIdentifierTypeList = new ArrayList<PatientIdentifierType>();
		patientIdentifierTypeList.add(new PatientIdentifierType());
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode(null,
		    "identifier", patientIdentifierTypeList, false));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("",
		    "identifier", patientIdentifierTypeList, false));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER, patientSearchCriteria.getSearchMode("  \n\t",
		    "identifier", patientIdentifierTypeList, false));
	}
	
	/**
	 * @verifies identify search by name or identifier
	 * @see PatientSearchCriteria#getSearchMode(String, String, java.util.List, boolean)
	 */
	@Test
	public void getSearchMode_shouldIdentifySearchByNameOrIdentifier() throws Exception {
		List<PatientIdentifierType> patientIdentifierTypeList = new ArrayList<PatientIdentifierType>();
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
		    "name", null, new ArrayList<PatientIdentifierType>(), true));
		
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", "", new ArrayList<PatientIdentifierType>(), true));
		
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
	 * @verifies identify search by name and identifier
	 * @see PatientSearchCriteria#getSearchMode(String, String, java.util.List, boolean)
	 */
	@Test
	public void getSearchMode_shouldIdentifySearchByNameAndIdentifier() throws Exception {
		List<PatientIdentifierType> patientIdentifierTypeList = new ArrayList<PatientIdentifierType>();
		patientIdentifierTypeList.add(new PatientIdentifierType());
		
		// test cases where "name" and "identifier" are the only parameters that are non-empty and non-blank
		//
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_AND_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", "identifier", null, false));
		Assert.assertEquals(PatientSearchMode.PATIENT_SEARCH_BY_NAME_AND_IDENTIFIER, patientSearchCriteria.getSearchMode(
		    "name", "identifier", new ArrayList<PatientIdentifierType>(), false));
		
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
	 * @verifies return source value when target is blank
	 * @see PatientSearchCriteria#copySearchParameter(String, String)
	 */
	@Test
	public void copySearchParameter_shouldReturnSourceValueWhenTargetIsBlank() throws Exception {
		Assert.assertEquals("source", patientSearchCriteria.copySearchParameter("source", null));
		Assert.assertEquals("source", patientSearchCriteria.copySearchParameter("source", ""));
		Assert.assertEquals("source", patientSearchCriteria.copySearchParameter("source", "   \n\t "));
	}
	
	/**
	 * @verifies return target value when target is non-blank
	 * @see PatientSearchCriteria#copySearchParameter(String, String)
	 */
	@Test
	public void copySearchParameter_shouldReturnTargetValueWhenTargetIsNonblank() throws Exception {
		Assert.assertEquals("target", patientSearchCriteria.copySearchParameter(null, "target"));
		Assert.assertEquals("target", patientSearchCriteria.copySearchParameter("", "target"));
		Assert.assertEquals("target", patientSearchCriteria.copySearchParameter("   \n\t ", "target"));
		Assert.assertEquals("target", patientSearchCriteria.copySearchParameter("source", "target"));
	}
}

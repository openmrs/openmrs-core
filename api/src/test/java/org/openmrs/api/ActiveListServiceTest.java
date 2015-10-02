/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.activelist.ActiveListItem;
import org.openmrs.activelist.ActiveListType;
<<<<<<< HEAD
import org.openmrs.Allergy;
import org.openmrs.AllergySeverity;
import org.openmrs.AllergenType;
import org.openmrs.Allergen;
import org.openmrs.AllergyProperties;
=======
import org.openmrs.allergyapi.Allergy;
import org.openmrs.allergyapi.AllergySeverity;
import org.openmrs.allergyapi.AllergenType;
import org.openmrs.allergyapi.Allergen;
import org.openmrs.allergyapi.AllergyProperties;
>>>>>>> origin/TRUNK-4747
import org.openmrs.activelist.Problem;
import org.openmrs.activelist.ProblemModifier;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 *
 */
public class ActiveListServiceTest extends BaseContextSensitiveTest {
	
	private static final String ACTIVE_LIST_INITIAL_XML = "org/openmrs/api/include/ActiveListTest.xml";
	
	protected static PatientService patientService = null;
	
	protected static ActiveListService activeListService = null;
	
	@Before
	public void runBeforeAllTests() throws Exception {
		if (patientService == null) {
			patientService = Context.getPatientService();
			activeListService = Context.getActiveListService();
		}
		executeDataSet(ACTIVE_LIST_INITIAL_XML);
	}
	
	//	public List<ActiveListItem> getActiveListItems(Person p, ActiveListType type) throws Exception;
	@Test
	public void should_getActiveListItems() throws Exception {
		Patient p = patientService.getPatient(2);
		List<ActiveListItem> items = activeListService.getActiveListItems(p, new ActiveListType(2));
		assertEquals(1, items.size());
		System.out.println("instance=" + items.get(0).getClass().toString());
	}
	
	//	public <T extends ActiveListItem> List<T> getActiveListItems(Class<T> clazz, Person p, ActiveListType type) throws Exception;
	@Test
	public void should_getActiveListItems_withProblem() throws Exception {
		Patient p = patientService.getPatient(2);
		List<Problem> items = activeListService.getActiveListItems(Problem.class, p, new ActiveListType(2));
		assertEquals(1, items.size());
	}
	
<<<<<<< HEAD
=======
	/*
	 * TRUNK 4747
	@Test
	public void should_getActiveListItems_withAllergy() throws Exception {
		Patient p = patientService.getPatient(2);
		List<org.openmrs.activelist.Allergy> items = activeListService.getActiveListItems(org.openmrs.activelist.Allergy.class, p, new ActiveListType(1));
		assertEquals(1, items.size());
	}
	 */
	
	//	public <T extends ActiveListItem> T getActiveListItem(Class<T> clazz, Integer activeListItemId) throws Exception;
	/*
	 * TRUNK 4747
	@Test
	public void should_getActiveListItem_Allergy() throws Exception {
		org.openmrs.activelist.Allergy item = activeListService.getActiveListItem(org.openmrs.activelist.Allergy.class, 1);
		Assert.assertNotNull(item);
		Assert.assertTrue(item instanceof org.openmrs.activelist.Allergy);
	}
	 */
	
>>>>>>> origin/TRUNK-4747
	//	public ActiveListItem getActiveListItemByUuid(String uuid) throws Exception;
	//
	//	public ActiveListItem saveActiveListItem(ActiveListItem item) throws Exception;
	@Test
	public void should_saveActiveListItem_Problem() throws Exception {
		Patient p = patientService.getPatient(2);
		List<Problem> items = activeListService.getActiveListItems(Problem.class, p, new ActiveListType(2));
		assertEquals(1, items.size());
		
		Concept concept = Context.getConceptService().getConcept(88);//Aspirin
		Problem problem = new Problem(p, concept, new Date(), ProblemModifier.HISTORY_OF, "", null);
		activeListService.saveActiveListItem(problem);
		
		items = activeListService.getActiveListItems(Problem.class, p, new ActiveListType(2));
		assertEquals(2, items.size());
	}
<<<<<<< HEAD
		
=======
	
	/*
	 * TRUNK 4747
	@Test
	public void should_saveActiveListItem_Allergy() throws Exception {
		Patient p = patientService.getPatient(2);
		List<org.openmrs.activelist.Allergy> items = activeListService.getActiveListItems(org.openmrs.activelist.Allergy.class, p, new ActiveListType(1));
		assertEquals(1, items.size());
		
		Concept concept = Context.getConceptService().getConcept(88);//Aspirin
		AllergyProperties ap = new AllergyProperties();
		org.openmrs.activelist.Allergy allergy = new org.openmrs.activelist.Allergy(p, 
				concept, new Date(), org.openmrs.activelist.AllergyType.ANIMAL, null, 
				org.openmrs.activelist.AllergySeverity.INTOLERANCE);
		activeListService.saveActiveListItem(allergy);
		
		items = activeListService.getActiveListItems(org.openmrs.activelist.Allergy.class, p, new ActiveListType(1));
		assertEquals(2, items.size());
	}
	 */
	
	//	public ActiveListItem removeActiveListItem(ActiveListItem item, Date endDate) throws Exception;
	/*
	 * TRUNK 4747
	@Test
	public void should_removeActiveListItem_Allergy() throws Exception {
		org.openmrs.activelist.Allergy item = activeListService.getActiveListItem(org.openmrs.activelist.Allergy.class, 1);
		activeListService.removeActiveListItem(item, null);
		
		item = activeListService.getActiveListItem(org.openmrs.activelist.Allergy.class, 1);
		Assert.assertNotNull(item);
		Assert.assertNotNull(item.getEndDate());
	}
	 */
	
	//	public ActiveListItem voidActiveListItem(ActiveListItem item, String reason) throws Exception;
	/*
	 * TRUNK 4747
	@Test
	public void should_voidActiveListItem_Allergy() throws Exception {
		org.openmrs.activelist.Allergy item = activeListService.getActiveListItem(org.openmrs.activelist.Allergy.class, 1);
		item = (org.openmrs.activelist.Allergy) activeListService.voidActiveListItem(item, "Because");
		Assert.assertTrue(item.isVoided());
		
		Patient p = patientService.getPatient(2);
		List<org.openmrs.activelist.Allergy> items = activeListService.getActiveListItems(org.openmrs.activelist.Allergy.class, p, new ActiveListType(1));
		assertEquals(0, items.size());
	}
	 */
	
	/**
	 * @see ActiveListService#purgeActiveListItem(ActiveListItem)
	 * @verifies purge active list item from database
	 */
	/*
	 * TRUNK 4747
	@Test
	public void purgeActiveListItem_shouldPurgeActiveListItemFromDatabase() throws Exception {
		ActiveListItem item = activeListService.getActiveListItem(org.openmrs.activelist.Allergy.class, 1);
		activeListService.purgeActiveListItem(item);
		
		item = activeListService.getActiveListItem(ActiveListItem.class, 1);
		Assert.assertNull(item);
	}
	 */
	
>>>>>>> origin/TRUNK-4747
	private void assertEquals(int i1, int i2) {
		Assert.assertEquals(Integer.valueOf(i1), Integer.valueOf(i2));
	}
	
}

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
import org.openmrs.activelist.Allergy;
import org.openmrs.activelist.AllergySeverity;
import org.openmrs.activelist.AllergyType;
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
	public void getActiveListItems() throws Exception {
		Patient p = patientService.getPatient(2);
		List<ActiveListItem> items = activeListService.getActiveListItems(p, new ActiveListType(2));
		assertEquals(1, items.size());
		System.out.println("instance=" + items.get(0).getClass().toString());
	}
	
	//	public <T extends ActiveListItem> List<T> getActiveListItems(Class<T> clazz, Person p, ActiveListType type) throws Exception;
	@Test
	public void getActiveListItems_withProblem() throws Exception {
		Patient p = patientService.getPatient(2);
		List<Problem> items = activeListService.getActiveListItems(Problem.class, p, new ActiveListType(2));
		assertEquals(1, items.size());
	}
	
	@Test
	public void getActiveListItems_withAllergy() throws Exception {
		Patient p = patientService.getPatient(2);
		List<Allergy> items = activeListService.getActiveListItems(Allergy.class, p, new ActiveListType(1));
		assertEquals(1, items.size());
	}

	//	public <T extends ActiveListItem> T getActiveListItem(Class<T> clazz, Integer activeListItemId) throws Exception;
	@Test
	public void getActiveListItem_Allergy() throws Exception {
		Allergy item = activeListService.getActiveListItem(Allergy.class, 1);
		Assert.assertNotNull(item);
		Assert.assertTrue(item instanceof Allergy);
	}

	//	public ActiveListItem getActiveListItemByUuid(String uuid) throws Exception;
	//
	//	public ActiveListItem saveActiveListItem(ActiveListItem item) throws Exception;
	@Test
	public void saveActiveListItem_Problem() throws Exception {
		Patient p = patientService.getPatient(2);
		List<Problem> items = activeListService.getActiveListItems(Problem.class, p, new ActiveListType(2));
		assertEquals(1, items.size());
		
		Concept concept = Context.getConceptService().getConcept(88);//Aspirin
		Problem problem = new Problem(p, concept, new Date(), ProblemModifier.HISTORY_OF, "", null);
		activeListService.saveActiveListItem(problem);
		
		items = activeListService.getActiveListItems(Problem.class, p, new ActiveListType(2));
		assertEquals(2, items.size());
	}
	
	@Test
	public void saveActiveListItem_Allergy() throws Exception {
		Patient p = patientService.getPatient(2);
		List<Allergy> items = activeListService.getActiveListItems(Allergy.class, p, new ActiveListType(1));
		assertEquals(1, items.size());
		
		Concept concept = Context.getConceptService().getConcept(88);//Aspirin
		Allergy allergy = new Allergy(p, concept, new Date(), AllergyType.ANIMAL, null,
		        AllergySeverity.INTOLERANCE);
		activeListService.saveActiveListItem(allergy);
		
		items = activeListService.getActiveListItems(Allergy.class, p, new ActiveListType(1));
		assertEquals(2, items.size());
	}

	//	public ActiveListItem removeActiveListItem(ActiveListItem item, Date endDate) throws Exception;
	@Test
	public void removeActiveListItem_Allergy() throws Exception {
		Allergy item = activeListService.getActiveListItem(Allergy.class, 1);
		activeListService.removeActiveListItem(item, null);
		
		item = activeListService.getActiveListItem(Allergy.class, 1);
		Assert.assertNotNull(item);
		Assert.assertNotNull(item.getEndDate());
	}

	//	public ActiveListItem voidActiveListItem(ActiveListItem item, String reason) throws Exception;
	@Test
	public void voidActiveListItem_Allergy() throws Exception {
		Allergy item = activeListService.getActiveListItem(Allergy.class, 1);
		item = (Allergy) activeListService.voidActiveListItem(item, "Because");
		Assert.assertTrue(item.isVoided());
		
		Patient p = patientService.getPatient(2);
		List<Allergy> items = activeListService.getActiveListItems(Allergy.class, p, new ActiveListType(1));
		assertEquals(0, items.size());
	}
	
	private void assertEquals(int i1, int i2) {
		Assert.assertEquals(new Integer(i1), new Integer(i2));
	}
}

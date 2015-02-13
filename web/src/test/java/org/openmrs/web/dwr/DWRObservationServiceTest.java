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
package org.openmrs.web.dwr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

public class DWRObservationServiceTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link org.openmrs.web.dwr.DWRObsService#createObs(Integer, Integer, Integer, String, String)}
	 */
	@Test
	@Verifies(value = "should pass test on saving observation with coded concepts", method = "createObs(Integer, Integer,Integer, String, String)")
	public void createObservation_shouldCreateObservationWithCodedConcept() throws Exception {
		DWRObsService dwrService = new DWRObsService();
		ConceptService conceptService = Context.getConceptService();
		ObsService obsService = Context.getObsService();
		Person person = Context.getPersonService().getPerson(2);
		Concept concept = conceptService.getConcept(21);
		List<Obs> obsListBefore = obsService.getObservationsByPersonAndConcept(person, concept);
		dwrService.createObs(2, null, 21, "7", "1/12/2014");
		List<Obs> obsListAfter = obsService.getObservationsByPersonAndConcept(person, concept);
		assertEquals(obsListBefore.size() + 1, obsListAfter.size());
		Concept answerConcept = conceptService.getConcept(7);
		Obs addedObs = (Obs) CollectionUtils.subtract(obsListAfter, obsListBefore).iterator().next();
		assertNotNull(addedObs);
		assertNotNull(addedObs.getValueCoded());
		assertEquals(answerConcept, addedObs.getValueCoded());
	}
	
	/**
	 * @see {@link org.openmrs.web.dwr.DWRObsService#createObs(Integer, Integer, Integer, String, String)}
	 */
	@Test
	@Verifies(value = "should pass test on saving observation with boolean concepts with value yes", method = "createObs(Integer, Integer, Integer, String, String)")
	public void createObservation_shouldCreateObservationWithBooleanConceptWithValueYes() throws Exception {
		DWRObsService dwrService = new DWRObsService();
		ConceptService conceptService = Context.getConceptService();
		ObsService obsService = Context.getObsService();
		AdministrationService administrationService = Context.getAdministrationService();
		Person person = Context.getPersonService().getPerson(2);
		Concept concept = conceptService.getConcept(18);
		List<Obs> obsListBefore = obsService.getObservationsByPersonAndConcept(person, concept);
		int obsListSizeBeforeSaveObs = obsListBefore.size();
		String obsDateTime = "1/12/2014";
		dwrService.createObs(2, null, 18, "Yes", obsDateTime);
		List<Obs> obsListAfter = obsService.getObservationsByPersonAndConcept(person, concept);
		assertEquals(obsListSizeBeforeSaveObs + 1, obsListAfter.size());
		String booleanConceptId = administrationService.getGlobalProperty("concept.true");
		Concept booleanConcept = Context.getConceptService().getConcept(Integer.parseInt(booleanConceptId));
		Obs addedObs = (Obs) CollectionUtils.subtract(obsListAfter, obsListBefore).iterator().next();
		assertNotNull(addedObs);
		assertNotNull(addedObs.getValueCoded());
		assertEquals(booleanConcept, addedObs.getValueCoded());
	}
	
	/**
	 * @see {@link org.openmrs.web.dwr.DWRObsService#createObs(Integer, Integer, Integer, String, String)}
	 */
	@Test
	@Verifies(value = "should pass test on saving observation with boolean concepts with value no", method = "createObs(Integer, Integer, Integer, String, String)")
	public void createObservation_shouldCreateObservationWithBooleanConceptWithValueNo() throws Exception {
		DWRObsService dwrService = new DWRObsService();
		ConceptService conceptService = Context.getConceptService();
		ObsService obsService = Context.getObsService();
		AdministrationService administrationService = Context.getAdministrationService();
		Person person = Context.getPersonService().getPerson(2);
		Concept concept = conceptService.getConcept(18);
		List<Obs> obsListBefore = obsService.getObservationsByPersonAndConcept(person, concept);
		int obsListSizeBeforeSaveObs = obsListBefore.size();
		String obsDateTime = "2/12/2014";
		dwrService.createObs(2, null, 18, "No", obsDateTime);
		List<Obs> obsListAfter = obsService.getObservationsByPersonAndConcept(person, concept);
		assertEquals(obsListSizeBeforeSaveObs + 1, obsListAfter.size());
		String booleanConceptId = administrationService.getGlobalProperty("concept.false");
		Concept booleanConcept = Context.getConceptService().getConcept(Integer.parseInt(booleanConceptId));
		Obs addedObs = (Obs) CollectionUtils.subtract(obsListAfter, obsListBefore).iterator().next();
		assertNotNull(addedObs);
		assertNotNull(addedObs.getValueCoded());
		assertEquals(booleanConcept, addedObs.getValueCoded());
	}
	
	/**
	 * @see {@link org.openmrs.web.dwr.DWRObsService#createObs(Integer, Integer, Integer, String, String)}
	 */
	@Test
	@Verifies(value = "should pass test on saving observation with boolean concepts with value true", method = "createObs(Integer, Integer, Integer, String, String)")
	public void createObservation_shouldCreateObservationWithBooleanConceptWithValueTrue() throws Exception {
		DWRObsService dwrService = new DWRObsService();
		ConceptService conceptService = Context.getConceptService();
		ObsService obsService = Context.getObsService();
		AdministrationService administrationService = Context.getAdministrationService();
		Person person = Context.getPersonService().getPerson(2);
		Concept concept = conceptService.getConcept(18);
		List<Obs> obsListBefore = obsService.getObservationsByPersonAndConcept(person, concept);
		int obsListSizeBeforeSaveObs = obsListBefore.size();
		String obsDateTime = "3/12/2014";
		dwrService.createObs(2, null, 18, "True", obsDateTime);
		List<Obs> obsListAfter = obsService.getObservationsByPersonAndConcept(person, concept);
		assertEquals(obsListSizeBeforeSaveObs + 1, obsListAfter.size());
		String booleanConceptId = administrationService.getGlobalProperty("concept.true");
		Concept booleanConcept = Context.getConceptService().getConcept(Integer.parseInt(booleanConceptId));
		Obs addedObs = (Obs) CollectionUtils.subtract(obsListAfter, obsListBefore).iterator().next();
		assertNotNull(addedObs);
		assertNotNull(addedObs.getValueCoded());
		assertEquals(booleanConcept, addedObs.getValueCoded());
	}
	
	/**
	 * @see {@link org.openmrs.web.dwr.DWRObsService#createObs(Integer, Integer, Integer, String, String)}
	 */
	@Test
	@Verifies(value = "should pass test on saving observation with boolean concepts with value false", method = "createObs(Integer, Integer, Integer, String, String)")
	public void createObservation_shouldCreateObservationWithBooleanConceptWithValueFalse() throws Exception {
		DWRObsService dwrService = new DWRObsService();
		ConceptService conceptService = Context.getConceptService();
		ObsService obsService = Context.getObsService();
		AdministrationService administrationService = Context.getAdministrationService();
		Person person = Context.getPersonService().getPerson(2);
		Concept concept = conceptService.getConcept(18);
		List<Obs> obsListBefore = obsService.getObservationsByPersonAndConcept(person, concept);
		int obsListSizeBeforeSaveObs = obsListBefore.size();
		String obsDateTime = "4/12/2014";
		dwrService.createObs(2, null, 18, "False", obsDateTime);
		List<Obs> obsListAfter = obsService.getObservationsByPersonAndConcept(person, concept);
		assertEquals(obsListSizeBeforeSaveObs + 1, obsListAfter.size());
		String booleanConceptId = administrationService.getGlobalProperty("concept.false");
		Concept booleanConcept = Context.getConceptService().getConcept(Integer.parseInt(booleanConceptId));
		Obs addedObs = (Obs) CollectionUtils.subtract(obsListAfter, obsListBefore).iterator().next();
		assertNotNull(addedObs);
		assertNotNull(addedObs.getValueCoded());
		assertEquals(booleanConcept, addedObs.getValueCoded());
	}
	
	/**
	 * @see {@link org.openmrs.web.dwr.DWRObsService#createObs(Integer, Integer, Integer, String, String)}
	 */
	@Test
	@Verifies(value = "should pass test on saving observation with boolean concepts with value zero", method = "createObs(Integer, Integer, Integer, String, String)")
	public void createObservation_shouldCreateObservationWithBooleanConceptWithValueZero() throws Exception {
		DWRObsService dwrService = new DWRObsService();
		ConceptService conceptService = Context.getConceptService();
		ObsService obsService = Context.getObsService();
		AdministrationService administrationService = Context.getAdministrationService();
		Person person = Context.getPersonService().getPerson(2);
		Concept concept = conceptService.getConcept(18);
		List<Obs> obsListBefore = obsService.getObservationsByPersonAndConcept(person, concept);
		int obsListSizeBeforeSaveObs = obsListBefore.size();
		String obsDateTime = "5/12/2014";
		dwrService.createObs(2, null, 18, "0", obsDateTime);
		List<Obs> obsListAfter = obsService.getObservationsByPersonAndConcept(person, concept);
		assertEquals(obsListSizeBeforeSaveObs + 1, obsListAfter.size());
		String booleanConceptId = administrationService.getGlobalProperty("concept.false");
		Concept booleanConcept = Context.getConceptService().getConcept(Integer.parseInt(booleanConceptId));
		Obs addedObs = (Obs) CollectionUtils.subtract(obsListAfter, obsListBefore).iterator().next();
		assertNotNull(addedObs);
		assertNotNull(addedObs.getValueCoded());
		assertEquals(booleanConcept, addedObs.getValueCoded());
	}
	
	/**
	 * @see {@link org.openmrs.web.dwr.DWRObsService#createObs(Integer, Integer, Integer, String, String)}
	 */
	@Test
	@Verifies(value = "should pass test on saving observation with boolean concepts with value one", method = "createObs(Integer, Integer, Integer, String, String)")
	public void createObservation_shouldCreateObservationWithBooleanConceptWithValueOne() throws Exception {
		DWRObsService dwrService = new DWRObsService();
		ConceptService conceptService = Context.getConceptService();
		ObsService obsService = Context.getObsService();
		AdministrationService administrationService = Context.getAdministrationService();
		Person person = Context.getPersonService().getPerson(2);
		Concept concept = conceptService.getConcept(18);
		List<Obs> obsListBefore = obsService.getObservationsByPersonAndConcept(person, concept);
		int obsListSizeBeforeSaveObs = obsListBefore.size();
		String obsDateTime = "6/12/2014";
		dwrService.createObs(2, null, 18, "1", obsDateTime);
		List<Obs> obsListAfter = obsService.getObservationsByPersonAndConcept(person, concept);
		assertEquals(obsListSizeBeforeSaveObs + 1, obsListAfter.size());
		String booleanConceptId = administrationService.getGlobalProperty("concept.true");
		Concept booleanConcept = Context.getConceptService().getConcept(Integer.parseInt(booleanConceptId));
		Obs addedObs = (Obs) CollectionUtils.subtract(obsListAfter, obsListBefore).iterator().next();
		assertNotNull(addedObs);
		assertNotNull(addedObs.getValueCoded());
		assertEquals(booleanConcept, addedObs.getValueCoded());
	}
}

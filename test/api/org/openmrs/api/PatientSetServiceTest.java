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

import static org.junit.Assert.assertNotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.DrugOrder;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.PatientSetService.Modifier;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 *
 */
public class PatientSetServiceTest extends BaseContextSensitiveTest {
	
	PatientSetService service;
	
	@Before
	public void getService() {
		service = Context.getPatientSetService();
	}
	
	@Test
	public void shouldGetDrugOrders() throws Exception {
		PatientSetService service = Context.getPatientSetService();
		Cohort nobody = new Cohort();
		Map<Integer, List<DrugOrder>> results = service.getDrugOrders(nobody, null);
		assertNotNull(results);
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsHavingObs(Integer,TimeModifier,Modifier,Object,Date,Date)}
	 *      test = should get patients by concept and false boolean value
	 */
	@Test
	@Verifies(value = "should get patients by concept and false boolean value", method = "getPatientsHavingObs(Integer,TimeModifier,Modifier,Object,Date,Date)")
	public void getPatientsHavingObs_shouldGetPatientsByConceptAndFalseBooleanValue() throws Exception {
		// there aren't any of these obs in the standard test dataset. Verify that:
		Cohort cohort = service.getPatientsHavingObs(18, TimeModifier.ANY, Modifier.EQUAL, Boolean.FALSE, null, null);
		Assert.assertEquals(0, cohort.size());
		// now create a couple of these so we can try searching.
		DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
		{ // start with a true one
			Obs obs = new Obs();
			obs.setPerson(new Person(8));
			obs.setConcept(Context.getConceptService().getConcept(18));
			obs.setObsDatetime(ymd.parse("2007-01-01"));
			obs.setValueNumeric(1.0);
			obs.setLocation(new Location(1));
			Context.getObsService().saveObs(obs, null);
		}
		{ // then do a false one
			Obs obs = new Obs();
			obs.setPerson(new Person(8));
			obs.setConcept(Context.getConceptService().getConcept(18));
			obs.setObsDatetime(ymd.parse("2008-01-01"));
			obs.setValueNumeric(0.0);
			obs.setLocation(new Location(1));
			Context.getObsService().saveObs(obs, null);
		}
		// search again
		cohort = service.getPatientsHavingObs(18, TimeModifier.FIRST, Modifier.EQUAL, Boolean.FALSE, null, null);
		Assert.assertEquals(0, cohort.size());
		cohort = service.getPatientsHavingObs(18, TimeModifier.LAST, Modifier.EQUAL, Boolean.FALSE, null, null);
		Assert.assertEquals(1, cohort.size());
		Assert.assertTrue(cohort.contains(8));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsHavingObs(Integer,TimeModifier,Modifier,Object,Date,Date)}
	 *      test = should get patients by concept and true boolean value
	 */
	@Test
	@Verifies(value = "should get patients by concept and true boolean value", method = "getPatientsHavingObs(Integer,TimeModifier,Modifier,Object,Date,Date)")
	public void getPatientsHavingObs_shouldGetPatientsByConceptAndTrueBooleanValue() throws Exception {
		Cohort cohort = service.getPatientsHavingObs(18, TimeModifier.ANY, Modifier.EQUAL, Boolean.TRUE, null, null);
		Assert.assertEquals(1, cohort.size());
		Assert.assertTrue(cohort.contains(7));
	}
	
}

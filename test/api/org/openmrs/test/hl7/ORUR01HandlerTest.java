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
package org.openmrs.test.hl7;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.handler.ORUR01Handler;
import org.openmrs.test.testutil.BaseContextSensitiveTest;

import ca.uhn.hl7v2.app.MessageTypeRouter;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.GenericParser;

/**
 * TODO finish testing all methods ORUR01Handler
 */
public class ORUR01HandlerTest extends BaseContextSensitiveTest {

	protected static final String ORU_INITIAL_DATA_XML = "org/openmrs/test/hl7/include/ORUTest-initialData.xml";
	
	// hl7 parser to be used throughout
	protected static GenericParser parser = new GenericParser();
	private static MessageTypeRouter router = new MessageTypeRouter();
	
	static {
		router.registerApplication("ORU", "R01", new ORUR01Handler());
	}
	
	@Before
	public void runBeforeEachTest() throws Exception {
		//deleteAllData();
		
		initializeInMemoryDatabase();
		executeDataSet(ORU_INITIAL_DATA_XML);
		authenticate();
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldBasicCreate() throws Exception {
		authenticate();
		ObsService obsService = Context.getObsService();
		
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\rPID|||3^^^^||John3^Doe^||\rPV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\rORC|RE||||||||20080226102537|1^Super User\rOBR|1|||\rOBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\rOBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212";
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		
		//System.out.println("obs size for pat 2: " + obsService.getObservations(new Patient(2), false));
		
		Patient patient = new Patient(3);
		
		// check for any obs
		Set<Obs> obsForPatient3 = obsService.getObservations(patient, false);
		assertNotNull(obsForPatient3);
		assertTrue("There should be some obs created for #3", obsForPatient3.size() > 0);
		
		// check for the return visit date obs 
		Concept returnVisitDateConcept = new Concept(5096);
		Calendar cal = new GregorianCalendar();
		cal.set(2008, Calendar.FEBRUARY, 29, 0, 0, 0);
		Date returnVisitDate = cal.getTime();
		Set<Obs> returnVisitDateObsForPatient3 = obsService.getObservations(patient, returnVisitDateConcept, false);
		assertEquals("There should be a return visit date", 1, returnVisitDateObsForPatient3.size());
		
		Obs firstObs = (Obs)returnVisitDateObsForPatient3.toArray()[0];
		assertEquals("The date should be the 29th", returnVisitDate.toString(), firstObs.getValueDatetime().toString());
	}
	
	/**
	 * This method checks that obs grouping is happening correctly when 
	 * processing an ORUR01
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGroupObsCreate() throws Exception {
		authenticate();
		ObsService obsService = Context.getObsService();
		
		//System.out.println("obs size for patient #2: " + obsService.getObservations(new Patient(2), false));
		
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226103553||ORU^R01|OD9PWqcD9g0NKn81rvSD|P|2.5|1||||||||66^AMRS.ELD.FORMID\rPID|||3^^^^||John^Doe^||\rPV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080205|||||||V\rORC|RE||||||||20080226103428|1^Super User\rOBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\rOBX|1|DT|1592^MISSED RETURNED VISIT DATE^99DCT||20080201|||||||||20080205\rOBR|2|||1726^FOLLOW-UP ACTION^99DCT\rOBX|1|CWE|1558^PATIENT CONTACT METHOD^99DCT|1|1555^PHONE^99DCT|||||||||20080205\rOBX|2|NM|1553^NUMBER OF ATTEMPTS^99DCT|1|1|||||||||20080205\rOBX|3|NM|1554^SUCCESSFUL^99DCT|1|1|||||||||20080205";
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		
		Patient patient = new Patient(3);
		
		Context.clearSession();
		
		// check for any obs
		List<Obs> obsForPatient2 = obsService.getObservationsByPerson(patient);
		assertNotNull(obsForPatient2);
		assertTrue("There should be some obs created for #3", obsForPatient2.size() > 0);
		
		// check for the missed return visit date obs 
		Concept returnVisitDateConcept = new Concept(1592);
		Calendar cal = new GregorianCalendar();
		cal.set(2008, Calendar.FEBRUARY, 1, 0, 0, 0);
		Date returnVisitDate = cal.getTime();
		Set<Obs> returnVisitDateObsForPatient2 = obsService.getObservations(patient, returnVisitDateConcept, false);
		assertEquals("There should be a return visit date", 1, returnVisitDateObsForPatient2.size());
		Obs firstObs = (Obs)returnVisitDateObsForPatient2.toArray()[0];
		
		cal.setTime(firstObs.getValueDatetime());
		cal.clear(Calendar.HOUR);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);
		assertEquals("The date should be the 1st", returnVisitDate.toString(), cal.getTime().toString());
		
		// check for the grouped obs
		Concept contactMethod = new Concept(1558);
		Concept phoneContact = Context.getConceptService().getConcept(1555);
		Set<Obs> contactMethodObsForPatient2 = obsService.getObservations(patient, contactMethod, false);
		assertEquals("There should be a contact method", 1, contactMethodObsForPatient2.size());
		Obs firstContactMethodObs = (Obs)contactMethodObsForPatient2.toArray()[0];
		assertEquals("The contact method should be phone", phoneContact, firstContactMethodObs.getValueCoded());
		
		// check that there is a group id
		Obs obsGroup = firstContactMethodObs.getObsGroup();
		assertNotNull("Their should be a grouping obs", obsGroup);
		assertNotNull("Their should be an associated encounter", firstContactMethodObs.getEncounter());
		
		// check that the obs that are grouped have the same group id
		List<Integer> groupedConceptIds = new Vector<Integer>();
		groupedConceptIds.add(1558);
		groupedConceptIds.add(1553);
		groupedConceptIds.add(1554);
		
		// total obs should be 5
		assertEquals(5, obsForPatient2.size());
		
		int groupedObsCount = 0;
		for (Obs obs : obsForPatient2) {
			System.out.println("obs: " + obs.getConcept());
			if (groupedConceptIds.contains(obs.getConcept().getConceptId())) {
				groupedObsCount += 1;
				assertEquals("All of the parent groups should match", obsGroup, obs.getObsGroup());
			}
		}
		
		// the number of obs that were grouped
		assertEquals(3, groupedObsCount);
		
	}

}

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
package org.openmrs.hl7;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptProposal;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.handler.ORUR01Handler;
import org.openmrs.test.BaseContextSensitiveTest;

import ca.uhn.hl7v2.app.MessageTypeRouter;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.GenericParser;

/**
 * TODO finish testing all methods ORUR01Handler
 */
public class ORUR01HandlerTest extends BaseContextSensitiveTest {
	
	protected static final String ORU_INITIAL_DATA_XML = "org/openmrs/hl7/include/ORUTest-initialData.xml";
	
	// hl7 parser to be used throughout
	protected static GenericParser parser = new GenericParser();
	
	private static MessageTypeRouter router = new MessageTypeRouter();
	
	static {
		router.registerApplication("ORU", "R01", new ORUR01Handler());
	}
	
	/**
	 * Run this before each unit test in this class. This adds the hl7 specific data to the initial
	 * and demo data done in the "@Before" method in {@link BaseContextSensitiveTest}.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet(ORU_INITIAL_DATA_XML);
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void shouldBasicCreate() throws Exception {
		ObsService obsService = Context.getObsService();
		
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\rPID|||3^^^^||John3^Doe^||\rPV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\rORC|RE||||||||20080226102537|1^Super User\rOBR|1|||\rOBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\rOBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212";
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		
		// ABKTODO: base test has changed. Is this needed?
		// commitTransaction(false);
		
		//System.out.println("obs size for pat 2: " + obsService.getObservations(new Patient(2), false));
		
		Patient patient = new Patient(3);
		
		// check for any obs
		List<Obs> obsForPatient3 = obsService.getObservationsByPerson(patient);
		assertNotNull(obsForPatient3);
		assertTrue("There should be some obs created for #3", obsForPatient3.size() > 0);
		
		// check for the return visit date obs 
		Concept returnVisitDateConcept = new Concept(5096);
		Calendar cal = Calendar.getInstance();
		cal.set(2008, Calendar.FEBRUARY, 29, 0, 0, 0);
		Date returnVisitDate = cal.getTime();
		List<Obs> returnVisitDateObsForPatient3 = obsService.getObservationsByPersonAndConcept(patient,
		    returnVisitDateConcept);
		assertEquals("There should be a return visit date", 1, returnVisitDateObsForPatient3.size());
		
		Obs firstObs = (Obs) returnVisitDateObsForPatient3.toArray()[0];
		cal.setTime(firstObs.getValueDatetime());
		Date firstObsValueDatetime = cal.getTime();
		assertEquals("The date should be the 29th", returnVisitDate.toString(), firstObsValueDatetime.toString());
	}
	
	/**
	 * This method checks that obs grouping is happening correctly when processing an ORUR01
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGroupObsCreate() throws Exception {
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
		Obs firstObs = (Obs) returnVisitDateObsForPatient2.toArray()[0];
		
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
		Obs firstContactMethodObs = (Obs) contactMethodObsForPatient2.toArray()[0];
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
			//System.out.println("obs: " + obs.getConcept());
			if (groupedConceptIds.contains(obs.getConcept().getConceptId())) {
				groupedObsCount += 1;
				assertEquals("All of the parent groups should match", obsGroup, obs.getObsGroup());
			}
		}
		
		// the number of obs that were grouped
		assertEquals(3, groupedObsCount);
		
	}
	
	/**
	 * If an hl7 message contains a "visit number" pv1-19 value, then assume its an encounter_id and that
	 * information in the hl7 message should be appended to that encounter.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldAppendToExistingEncounter() throws Exception {
		
		// there should be an encounter with encounter_id == 3 for this test
		// to append to
		assertNotNull(Context.getEncounterService().getEncounter(3));
		
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080902151831||ORU^R01|yow3LEP6bycnLfoPyI31|P|2.5|1||||||||3^AMRS.ELD.FORMID\rPID|||7^^^^||Indakasi^Testarius^Ambote||\rPV1||O|1||||1^Super User (1-8)||||||||||||3|||||||||||||||||||||||||20080831|||||||V\rORC|RE||||||||20080902150000|1^Super User\rOBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\rOBX|1|NM|10^CD4 COUNT^99DCT||250|||||||||20080831";
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		
		Patient patient = new Patient(7);
		Concept question = new Concept(10);
		// check that the CD4 count obs in the hl7 message was appended to the 
		// encounter with encounter_id == 3 and _not_ put into a new encounter
		// that has encounter_id == (autoincremented value)
		List<Obs> obsForPatient = Context.getObsService().getObservationsByPersonAndConcept(patient, question);
		assertEquals(1, obsForPatient.size()); // there should be 1 obs now for this patient
		assertEquals(new Encounter(3), obsForPatient.get(0).getEncounter());
		
	}
	
	/**
	 * Should create a concept proposal because of the key string in the message
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCreateConceptProposal() throws Exception {
		
		List<ConceptProposal> proposals = Context.getConceptService().getAllConceptProposals(false);
		Assert.assertEquals(0, proposals.size());
		
		// there should be an encounter with encounter_id == 3 for this test
		// to append to
		assertNotNull(Context.getEncounterService().getEncounter(3));
		
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080924022306||ORU^R01|Z185fTD0YozQ5kvQZD7i|P|2.5|1||||||||3^AMRS.ELD.FORMID\rPID|||7^^^^||Joe^S^Mith||\rPV1||O|1^Unknown Module 2||||1^Joe (1-1)|||||||||||||||||||||||||||||||||||||20080212|||||||V\rORC|RE||||||||20080219085345|1^Joe\rOBR|1|||\rOBX|18|DT|5096^RETURN VISIT DATE^99DCT||20080506|||||||||20080212\rOBR|19|||5096^PROBLEM LIST^99DCT\rOBX|1|CWE|5096^PROBLEM ADDED^99DCT||PROPOSED^PELVIC MASS^99DCT|||||||||20080212";
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		
		ConceptProposal proposal = Context.getConceptService().getAllConceptProposals(false).get(0);
		assertEquals("PELVIC MASS", proposal.getOriginalText());
		
	}
	
	/**
	 * Should create a concept proposal because of the key string in the message
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCreateConceptProposal2() throws Exception {
		
		List<ConceptProposal> proposals = Context.getConceptService().getAllConceptProposals(false);
		Assert.assertEquals(0, proposals.size());
		
		// there should be an encounter with encounter_id == 3 for this test
		// to append to
		assertNotNull(Context.getEncounterService().getEncounter(3));
		
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20081006115934||ORU^R01|a1NZBpKqu54QyrWBEUKf|P|2.5|1||||||||3^AMRS.ELD.FORMID\rPID|||7^^^^~asdf^^^^||Joe^ ^Smith||\rPV1||O|1^Bishop Muge||||1^asdf asdf (5-9)|||||||||||||||||||||||||||||||||||||20081003|||||||V\rORC|RE||||||||20081006115645|1^Super User\rOBR|1|||\rOBX|1|CWE|5096^PAY CATEGORY^99DCT||5096^PILOT^99DCT|||||||||20081003\rOBX|2|DT|5096^RETURN VISIT DATE^99DCT||20081004|||||||||20081003\rOBR|3|||5096^PROBLEM LIST^99DCT\rOBX|1|CWE|5018^PROBLEM ADDED^99DCT||5096^HUMAN IMMUNODEFICIENCY VIRUS^99DCT|||||||||20081003\rOBX|2|CWE|5089^PROBLEM ADDED^99DCT||PROPOSED^ASDFASDFASDF^99DCT|||||||||20081003";
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		
		ConceptProposal proposal = Context.getConceptService().getAllConceptProposals(false).get(0);
		Assert.assertNotNull(proposal);
		assertEquals("ASDFASDFASDF", proposal.getOriginalText());
		
	}
}

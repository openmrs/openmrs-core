/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.hl7.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptProposal;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Form;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7Constants;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.app.MessageTypeRouter;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.NK1;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import ca.uhn.hl7v2.model.v26.segment.MSH;
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
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test
	public void processMessage_shouldCreateEncounterAndObsFromHl7Message() throws Exception {
		ObsService obsService = Context.getObsService();
		
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		        + "PID|||3^^^^||John3^Doe^||\r"
		        + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		        + "ORC|RE||||||||20080226102537|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		        + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212";
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		
		Patient patient = new Patient(3);
		
		// check for an encounter
		List<Encounter> encForPatient3 = Context.getEncounterService().getEncountersByPatient(patient);
		assertNotNull(encForPatient3);
		assertTrue("There should be an encounter created", encForPatient3.size() == 1);
		
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
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test
	public void processMessage_shouldCreateObsGroupForOBRs() throws Exception {
		ObsService obsService = Context.getObsService();
		
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226103553||ORU^R01|OD9PWqcD9g0NKn81rvSD|P|2.5|1||||||||66^AMRS.ELD.FORMID\r"
		        + "PID|||3^^^^||John^Doe^||\r"
		        + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080205|||||||V\r"
		        + "ORC|RE||||||||20080226103428|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|1|DT|1592^MISSED RETURNED VISIT DATE^99DCT||20080201|||||||||20080205\r"
		        + "OBR|2|||1726^FOLLOW-UP ACTION^99DCT\r"
		        + "OBX|1|CWE|1558^PATIENT CONTACT METHOD^99DCT|1|1555^PHONE^99DCT|||||||||20080205\r"
		        + "OBX|2|NM|1553^NUMBER OF ATTEMPTS^99DCT|1|1|||||||||20080205\r"
		        + "OBX|3|NM|1554^SUCCESSFUL^99DCT|1|1|||||||||20080205";
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
		List<Obs> returnVisitDateObsForPatient2 = obsService.getObservationsByPersonAndConcept(patient,
		    returnVisitDateConcept);
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
		List<Obs> contactMethodObsForPatient2 = obsService.getObservationsByPersonAndConcept(patient, contactMethod);
		assertEquals("There should be a contact method", 1, contactMethodObsForPatient2.size());
		Obs firstContactMethodObs = (Obs) contactMethodObsForPatient2.toArray()[0];
		assertEquals("The contact method should be phone", phoneContact, firstContactMethodObs.getValueCoded());
		
		// check that there is a group id
		Obs obsGroup = firstContactMethodObs.getObsGroup();
		assertNotNull("Their should be a grouping obs", obsGroup);
		assertNotNull("Their should be an associated encounter", firstContactMethodObs.getEncounter());
		
		// check that the obs that are grouped have the same group id
		List<Integer> groupedConceptIds = new ArrayList<>();
		groupedConceptIds.add(1558);
		groupedConceptIds.add(1553);
		groupedConceptIds.add(1554);
		
		// total obs should be 5
		assertEquals(5, obsForPatient2.size());
		
		int groupedObsCount = 0;
		for (Obs obs : obsForPatient2) {
			if (groupedConceptIds.contains(obs.getConcept().getConceptId())) {
				groupedObsCount += 1;
				assertEquals("All of the parent groups should match", obsGroup, obs.getObsGroup());
			}
		}
		
		// the number of obs that were grouped
		assertEquals(3, groupedObsCount);
		
	}
	
	/**
	 * If an hl7 message contains a "visit number" pv1-19 value, then assume its an encounter_id and
	 * that information in the hl7 message should be appended to that encounter.
	 * 
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test
	public void processMessage_shouldAppendToAnExistingEncounter() throws Exception {
		
		// there should be an encounter with encounter_id == 3 for this test
		// to append to
		assertNotNull(Context.getEncounterService().getEncounter(3));
		
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080902151831||ORU^R01|yow3LEP6bycnLfoPyI31|P|2.5|1||||||||3^AMRS.ELD.FORMID\r"
		        + "PID|||7^^^^||Indakasi^Testarius^Ambote||\r"
		        + "PV1||O|1||||1^Super User (1-8)||||||||||||3|||||||||||||||||||||||||20080831|||||||V\r"
		        + "ORC|RE||||||||20080902150000|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|1|NM|10^CD4 COUNT^99DCT||250|||||||||20080831";
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		
		Patient patient = new Patient(7);
		Concept question = new Concept(10);
		// check that the CD4 count obs in the hl7 message was appended to the
		// encounter with encounter_id == 3 and _not_ put into a new encounter
		// that has encounter_id == (autoincremented value)
		List<Obs> obsForPatient = Context.getObsService().getObservationsByPersonAndConcept(patient, question);
		assertEquals(1, obsForPatient.size()); // there should be 1 obs now for
		// this patient
		assertEquals(3, obsForPatient.get(0).getEncounter().getId().intValue());
		
	}
	
	/**
	 * Should create a concept proposal because of the key string in the message
	 * 
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test
	public void processMessage_shouldCreateBasicConceptProposal() throws Exception {
		
		// remember initial occurrence of proposal's text in the model
		int initialOccurrences = Context.getConceptService().getConceptProposals("PELVIC MASS").size();
		
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080924022306||ORU^R01|Z185fTD0YozQ5kvQZD7i|P|2.5|1||||||||3^AMRS.ELD.FORMID\r"
		        + "PID|||7^^^^||Joe^S^Mith||\r"
		        + "PV1||O|1^Unknown Module 2||||1^Joe (1-1)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		        + "ORC|RE||||||||20080219085345|1^Joe\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|18|DT|5096^RETURN VISIT DATE^99DCT||20080506|||||||||20080212\r"
		        + "OBR|19|||1284^PROBLEM LIST^99DCT\r"
		        + "OBX|1|CWE|6042^PROBLEM ADDED^99DCT||PROPOSED^PELVIC MASS^99DCT|||||||||20080212";
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		
		//make sure that the proposal was added
		Assert.assertEquals("Processing of the HL7 message did not result in the new proposal being added to the model",
		    initialOccurrences + 1, Context.getConceptService().getConceptProposals("PELVIC MASS").size());
		
	}

	/**
	 * Should create a concept proposal because of the key string in the message
	 * 
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test
	public void processMessage_shouldCreateConceptProposalAndWithObsAlongside() throws Exception {
		
		// remember initial occurrence of proposal's text in the model
		int initialOccurrences = Context.getConceptService().getConceptProposals("ASDFASDFASDF").size();
		
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20081006115934||ORU^R01|a1NZBpKqu54QyrWBEUKf|P|2.5|1||||||||3^AMRS.ELD.FORMID\r"
		        + "PID|||7^^^^~asdf^^^^||Joe^ ^Smith||\r"
		        + "PV1||O|1^Bishop Muge||||1^asdf asdf (5-9)|||||||||||||||||||||||||||||||||||||20081003|||||||V\r"
		        + "ORC|RE||||||||20081006115645|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20081004|||||||||20081003\r"
		        + "OBR|3|||1284^PROBLEM LIST^99DCT\r"
		        + "OBX|2|CWE|6042^PROBLEM ADDED^99DCT||PROPOSED^ASDFASDFASDF^99DCT|||||||||20081003";
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		
		//make sure that the proposal was added
		Assert.assertEquals("Processing of the HL7 message did not result in the new proposal being added to the model",
		    initialOccurrences + 1, Context.getConceptService().getConceptProposals("ASDFASDFASDF").size());
		
	}
	
	/**
	 * Tests that a ConceptProposal row can be written by the processor
	 * 
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test
	public void processMessage_shouldNotCreateProblemListObservationWithConceptProposals() throws Exception {
		ObsService obsService = Context.getObsService();
		ConceptService conceptService = Context.getConceptService();
		EncounterService encService = Context.getEncounterService();
		
		String hl7String = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080630094800||ORU^R01|kgWdFt0SVwwClOfJm3pe|P|2.5|1||||||||15^AMRS.ELD.FORMID\r"
		        + "PID|||3^^^^~d3811480^^^^||John3^Doe^||\r"
		        + "PV1||O|1^Unknown||||1^Super User (admin)|||||||||||||||||||||||||||||||||||||20080208|||||||V\r"
		        + "ORC|RE||||||||20080208000000|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBR|1|||1284^PROBLEM LIST^99DCT\r"
		        + "OBX|1|CWE|6042^PROBLEM ADDED^99DCT||PROPOSED^SEVERO DOLOR DE CABEZA^99DCT|||||||||20080208";
		Message hl7message = parser.parse(hl7String);
		router.processMessage(hl7message);
		
		Patient patient = new Patient(3);
		
		// check for any obs
		assertEquals("There should not be any obs created for #3", 0, obsService.getObservationsByPerson(patient).size());
		
		// check for a new encounter
		assertEquals("There should be 1 new encounter created for #3", 1, encService.getEncountersByPatient(patient).size());
		
		// check for the proposed concept
		List<ConceptProposal> proposedConcepts = conceptService.getConceptProposals("SEVERO DOLOR DE CABEZA");
		assertEquals("There should be a proposed concept by this name", 1, proposedConcepts.size());
		assertEquals(encService.getEncountersByPatient(patient).get(0), proposedConcepts.get(0).getEncounter());
	}
	
	/**
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test
	public void processMessage_shouldCreateObsValueCodedName() throws Exception {
		ObsService obsService = Context.getObsService();
		Patient patient = new Patient(3); // the patient that is the focus of
		// this hl7 message
		Concept concept = new Concept(21); // the question concept for
		// "Food assistance for entire family?"
		
		// sanity check to make sure this obs doesn't exist already
		Assert.assertEquals(0, obsService.getObservationsByPersonAndConcept(patient, concept).size());
		
		String hl7String = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20090728170332||ORU^R01|gu99yBh4loLX2mh9cHaV|P|2.5|1||||||||4^AMRS.ELD.FORMID\r"
		        + "PID|||3^^^^||Beren^John^Bondo||\r"
		        + "PV1||O|1^Unknown||||1^Super User (admin)|||||||||||||||||||||||||||||||||||||20090714|||||||V\r"
		        + "ORC|RE||||||||20090728165937|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|2|NM|5497^CD4 COUNT^99DCT||123|||||||||20090714\r"
		        + "OBR|3|||23^FOOD CONSTRUCT^99DCT\r"
		        + "OBX|1|CWE|21^FOOD ASSISTANCE FOR ENTIRE FAMILY^99DCT||22^UNKNOWN^99DCT^2471^UNKNOWN^99NAM|||||||||20090714";
		Message hl7message = parser.parse(hl7String);
		router.processMessage(hl7message);
		
		List<Obs> obss = obsService.getObservationsByPersonAndConcept(patient, concept);
		
		ConceptName name = obss.get(0).getValueCodedName();
		Assert.assertNotNull(name);
		Assert.assertEquals("The valueCodedName should be 2471", 2471, name.getId().intValue());
	}
	
	/**
	 * @see ORUR01Handler#getConcept(String,String)
	 */
	@Test
	public void getConcept_shouldReturnAConceptIfGivenLocalCodingSystem() throws Exception {
		Assert.assertEquals(5089, new ORUR01Handler().getConcept("5089", "99DCT", "xj39bnj4k34nmf").getId().intValue());
	}
	
	/**
	 * @see ORUR01Handler#getConcept(String,String)
	 */
	@Test
	public void getConcept_shouldReturnAMappedConceptIfGivenAValidMapping() throws Exception {
		Assert.assertEquals(5089, new ORUR01Handler().getConcept("WGT234", "SSTRM", "23498343sdnm3").getId().intValue());
	}
	
	/**
	 * @see ORUR01Handler#getConcept(String,String)
	 */
	@Test
	public void getConcept_shouldReturnNullIfCodingSystemNotFound() throws Exception {
		Assert.assertNull(new ORUR01Handler().getConcept("123", "a nonexistent coding system", "n3jn2345g89n4"));
		Assert.assertNull(new ORUR01Handler().getConcept("93939434834", "SSTRM", "xcjk23h89gn34k234"));
	}
	
	/**
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test(expected = ApplicationException.class)
	public void processMessage_shouldFailOnEmptyConceptAnswers() throws Exception {
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080630094800||ORU^R01|kgWdFt0SVwwClOfJm3pe|P|2.5|1||||||||15^AMRS.ELD.FORMID\r"
		        + "PID|||3^^^^~d3811480^^^^||John3^Doe^||\r"
		        + "PV1||O|1^Unknown||||1^Super User (admin)|||||||||||||||||||||||||||||||||||||20080208|||||||V\r"
		        + "ORC|RE||||||||20080208000000|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|1|CWE|5497^CD4, BY FACS^99DCT||^^99DCT|||||||||20080208";
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
	}
	
	/**
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test(expected = ApplicationException.class)
	public void processMessage_shouldFailOnEmptyConceptProposals() throws Exception {
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080630094800||ORU^R01|kgWdFt0SVwwClOfJm3pe|P|2.5|1||||||||15^AMRS.ELD.FORMID\r"
		        + "PID|||3^^^^~d3811480^^^^||John3^Doe^||\r"
		        + "PV1||O|1^Unknown||||1^Super User (admin)|||||||||||||||||||||||||||||||||||||20080208|||||||V\r"
		        + "ORC|RE||||||||20080208000000|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBR|1|||1284^PROBLEM LIST^99DCT\r"
		        + "OBX|1|CWE|6042^PROBLEM ADDED^99DCT||PROPOSED^^99DCT|||||||||20080208";
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
	}
	
	/**
	 * @see ORUR01Handler#processNK1(Patient,NK1)
	 */
	@Test
	public void processNK1_shouldCreateARelationshipFromANK1Segment() throws Exception {
		PersonService personService = Context.getPersonService();
		Patient patient = new Patient(3); // the patient that is the focus of
		// this hl7 message
		Patient relative = new Patient(2); // the patient that is related to
		// patientA
		
		// process a message with a single NK1 segment
		// defines relative as patient's Parent
		String hl7String = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20090728170332||ORU^R01|gu99yBh4loLX2mh9cHaV|P|2.5|1||||||||4^AMRS.ELD.FORMID\r"
		        + "PID|||3^^^^||Beren^John^Bondo||\r"
		        + "NK1|1|Jones^Jane^Lee^^RN|3A^Parent^99REL||||||||||||F|19751016|||||||||||||||||2^^^L^PI\r"
		        + "PV1||O|1^Unknown||||1^Super User (admin)|||||||||||||||||||||||||||||||||||||20090714|||||||V\r"
		        + "ORC|RE||||||||20090728165937|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|2|NM|5497^CD4 COUNT^99DCT||123|||||||||20090714\r"
		        + "OBR|3|||23^FOOD CONSTRUCT^99DCT\r"
		        + "OBX|1|CWE|21^FOOD ASSISTANCE FOR ENTIRE FAMILY^99DCT||22^UNKNOWN^99DCT^2471^UNKNOWN^99NAM|||||||||20090714";
		
		ORUR01Handler oruHandler = new ORUR01Handler();
		Message hl7message = parser.parse(hl7String);
		ORU_R01 oru = (ORU_R01) hl7message;
		List<NK1> nk1List = oruHandler.getNK1List(oru);
		for (NK1 nk1 : nk1List)
			oruHandler.processNK1(patient, nk1);
		
		// verify relationship was created
		List<Relationship> rels = personService.getRelationships(relative, patient, new RelationshipType(3));
		Assert.assertTrue("new relationship was not created", !rels.isEmpty() && rels.size() == 1);
	}
	
	/**
	 * @see ORUR01Handler#processNK1(Patient,NK1)
	 */
	@Test(expected = HL7Exception.class)
	public void processNK1_shouldFailIfTheCodingSystemIsNot99REL() throws Exception {
		// process a message with an invalid coding system
		Patient patient = new Patient(3); // the patient that is the focus of
		// this hl7 message
		String hl7String = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20090728170332||ORU^R01|gu99yBh4loLX2mh9cHaV|P|2.5|1||||||||4^AMRS.ELD.FORMID\r"
		        + "PID|||3^^^^||Beren^John^Bondo||\r"
		        + "NK1|1|Jones^Jane^Lee^^RN|3A^Parent^ACKFOO||||||||||||F|19751016|||||||||||||||||2^^^L^PI\r"
		        + "PV1||O|1^Unknown||||1^Super User (admin)|||||||||||||||||||||||||||||||||||||20090714|||||||V\r"
		        + "ORC|RE||||||||20090728165937|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|2|NM|5497^CD4 COUNT^99DCT||123|||||||||20090714\r"
		        + "OBR|3|||23^FOOD CONSTRUCT^99DCT\r"
		        + "OBX|1|CWE|21^FOOD ASSISTANCE FOR ENTIRE FAMILY^99DCT||22^UNKNOWN^99DCT^2471^UNKNOWN^99NAM|||||||||20090714";
		ORUR01Handler oruHandler = new ORUR01Handler();
		Message hl7message = parser.parse(hl7String);
		ORU_R01 oru = (ORU_R01) hl7message;
		List<NK1> nk1List = oruHandler.getNK1List(oru);
		for (NK1 nk1 : nk1List)
			oruHandler.processNK1(patient, nk1);
	}
	
	/**
	 * @see ORUR01Handler#processNK1(Patient,NK1)
	 */
	@Test(expected = HL7Exception.class)
	public void processNK1_shouldFailIfTheRelationshipIdentifierIsFormattedImproperly() throws Exception {
		// process a message with an invalid relationship identifier format
		Patient patient = new Patient(3); // the patient that is the focus of
		// this hl7 message
		String hl7String = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20090728170332||ORU^R01|gu99yBh4loLX2mh9cHaV|P|2.5|1||||||||4^AMRS.ELD.FORMID\r"
		        + "PID|||3^^^^||Beren^John^Bondo||\r"
		        + "NK1|1|Jones^Jane^Lee^^RN|3C^Parent^99REL||||||||||||F|19751016|||||||||||||||||2^^^L^PI\r"
		        + "PV1||O|1^Unknown||||1^Super User (admin)|||||||||||||||||||||||||||||||||||||20090714|||||||V\r"
		        + "ORC|RE||||||||20090728165937|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|2|NM|5497^CD4 COUNT^99DCT||123|||||||||20090714\r"
		        + "OBR|3|||23^FOOD CONSTRUCT^99DCT\r"
		        + "OBX|1|CWE|21^FOOD ASSISTANCE FOR ENTIRE FAMILY^99DCT||22^UNKNOWN^99DCT^2471^UNKNOWN^99NAM|||||||||20090714";
		ORUR01Handler oruHandler = new ORUR01Handler();
		Message hl7message = parser.parse(hl7String);
		ORU_R01 oru = (ORU_R01) hl7message;
		List<NK1> nk1List = oruHandler.getNK1List(oru);
		for (NK1 nk1 : nk1List)
			oruHandler.processNK1(patient, nk1);
	}
	
	/**
	 * @see ORUR01Handler#processNK1(Patient,NK1)
	 */
	@Test(expected = HL7Exception.class)
	public void processNK1_shouldFailIfTheRelationshipTypeIsNotFound() throws Exception {
		// process a message with a non-existent relationship type
		Patient patient = new Patient(3); // the patient that is the focus of
		// this hl7 message
		String hl7String = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20090728170332||ORU^R01|gu99yBh4loLX2mh9cHaV|P|2.5|1||||||||4^AMRS.ELD.FORMID\r"
		        + "PID|||3^^^^||Beren^John^Bondo||\r"
		        + "NK1|1|Jones^Jane^Lee^^RN|3952A^Fifth Cousin Twice Removed^99REL||||||||||||F|19751016|||||||||||||||||2^^^L^PI\r"
		        + "PV1||O|1^Unknown||||1^Super User (admin)|||||||||||||||||||||||||||||||||||||20090714|||||||V\r"
		        + "ORC|RE||||||||20090728165937|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|2|NM|5497^CD4 COUNT^99DCT||123|||||||||20090714\r"
		        + "OBR|3|||23^FOOD CONSTRUCT^99DCT\r"
		        + "OBX|1|CWE|21^FOOD ASSISTANCE FOR ENTIRE FAMILY^99DCT||22^UNKNOWN^99DCT^2471^UNKNOWN^99NAM|||||||||20090714";
		ORUR01Handler oruHandler = new ORUR01Handler();
		Message hl7message = parser.parse(hl7String);
		ORU_R01 oru = (ORU_R01) hl7message;
		List<NK1> nk1List = oruHandler.getNK1List(oru);
		for (NK1 nk1 : nk1List)
			oruHandler.processNK1(patient, nk1);
	}
	
	/**
	 * @see ORUR01Handler#processNK1(Patient,NK1)
	 */
	@Test
	public void processNK1_shouldNotCreateARelationshipIfOneExists() throws Exception {
		PersonService personService = Context.getPersonService();
		Patient patient = new Patient(3); // the patient that is the focus of
		// this hl7 message
		Patient relative = new Patient(2); // the patient that is related to
		// patientA
		
		// create a relationship in the database
		Relationship rel = new Relationship();
		rel.setRelationshipType(new RelationshipType(3));
		rel.setPersonA(relative);
		rel.setPersonB(patient);
		personService.saveRelationship(rel);
		
		// verify relationship exists
		Assert.assertEquals(1, personService.getRelationships(relative, patient, new RelationshipType(3)).size());
		
		// process a message with a single NK1 segment
		// defines relative as patient's Parent
		String hl7String = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20090728170332||ORU^R01|gu99yBh4loLX2mh9cHaV|P|2.5|1||||||||4^AMRS.ELD.FORMID\r"
		        + "PID|||3^^^^||Beren^John^Bondo||\r"
		        + "NK1|1|Jones^Jane^Lee^^RN|3A^Parent^99REL||||||||||||F|19751016|||||||||||||||||2^^^L^PI\r"
		        + "PV1||O|1^Unknown||||1^Super User (admin)|||||||||||||||||||||||||||||||||||||20090714|||||||V\r"
		        + "ORC|RE||||||||20090728165937|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|2|NM|5497^CD4 COUNT^99DCT||123|||||||||20090714\r"
		        + "OBR|3|||23^FOOD CONSTRUCT^99DCT\r"
		        + "OBX|1|CWE|21^FOOD ASSISTANCE FOR ENTIRE FAMILY^99DCT||22^UNKNOWN^99DCT^2471^UNKNOWN^99NAM|||||||||20090714";
		
		ORUR01Handler oruHandler = new ORUR01Handler();
		Message hl7message = parser.parse(hl7String);
		ORU_R01 oru = (ORU_R01) hl7message;
		List<NK1> nk1List = oruHandler.getNK1List(oru);
		for (NK1 nk1 : nk1List)
			oruHandler.processNK1(patient, nk1);
		
		// verify existing relationship
		List<Relationship> rels = personService.getRelationships(relative, patient, new RelationshipType(3));
		Assert.assertTrue("existing relationship was not retained", !rels.isEmpty() && rels.size() == 1);
	}
	
	/**
	 * @see ORUR01Handler#processORU_R01(ORU_R01)
	 */
	@Test
	public void processORU_R01_shouldProcessMultipleNK1Segments() throws Exception {
		PersonService personService = Context.getPersonService();
		Patient patient = new Patient(3); // the patient that is the focus of
		// this hl7 message
		Patient relative = new Patient(2); // the patient that is related to
		// patientA
		
		// create a relationship in the database
		Relationship newRel = new Relationship();
		newRel.setRelationshipType(new RelationshipType(3));
		newRel.setPersonA(relative);
		newRel.setPersonB(patient);
		personService.saveRelationship(newRel);
		
		// verify relationship exists
		Assert.assertEquals(1, personService.getRelationships(relative, patient, new RelationshipType(3)).size());
		
		// process a new message with multiple NK1 segments
		// this one defines patientB as patientA's Sibling and Patient
		String hl7String = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20090728170333||ORU^R01|gu99yBh4loLX2mh9cHaV|P|2.5|1||||||||4^AMRS.ELD.FORMID\r"
		        + "PID|||3^^^^||Beren^John^Bondo||\r"
		        + "NK1|1|Jones^Jane^Lee^^RN|2A^Sibling^99REL||||||||||||F|19751016|||||||||||||||||2^^^L^PI\r"
		        + "NK1|2|Jones^Jane^Lee^^RN|1B^Patient^99REL||||||||||||F|19751016|||||||||||||||||2^^^L^PI\r"
		        + "PV1||O|1^Unknown||||1^Super User (admin)|||||||||||||||||||||||||||||||||||||20090714|||||||V\r"
		        + "ORC|RE||||||||20090728165937|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|2|NM|5497^CD4 COUNT^99DCT||123|||||||||20090714\r"
		        + "OBR|3|||23^FOOD CONSTRUCT^99DCT\r"
		        + "OBX|1|CWE|21^FOOD ASSISTANCE FOR ENTIRE FAMILY^99DCT||22^UNKNOWN^99DCT^2471^UNKNOWN^99NAM|||||||||20090714";
		
		Message hl7message = parser.parse(hl7String);
		router.processMessage(hl7message);
		
		// verify existing relationship
		List<Relationship> rels = personService.getRelationships(relative, patient, new RelationshipType(3));
		Assert.assertTrue("existing relationship was not retained", !rels.isEmpty() && rels.size() == 1);
		
		// verify first new relationship
		rels = personService.getRelationships(patient, relative, new RelationshipType(2));
		Assert.assertTrue("first new relationship was not created", !rels.isEmpty() && rels.size() == 1);
		
		// verify second new relationship
		rels = personService.getRelationships(patient, relative, new RelationshipType(1));
		Assert.assertTrue("second new relationship was not created", !rels.isEmpty() && rels.size() == 1);
	}
	
	/**
	 * @see ORUR01Handler#processNK1(Patient,NK1)
	 */
	@Test
	public void processNK1_shouldCreateAPersonIfTheRelativeIsNotFound() throws Exception {
		// process a message with an invalid relative identifier
		PersonService personService = Context.getPersonService();
		Patient patient = new Patient(3); // the patient that is the focus of
		// this hl7 message
		
		String hl7String = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20090728170332||ORU^R01|gu99yBh4loLX2mh9cHaV|P|2.5|1||||||||4^AMRS.ELD.FORMID\r"
		        + "PID|||3^^^^||Beren^John^Bondo||\r"
		        + "NK1|1|Jones^Jane^Lee^^RN|3A^Parent^99REL||||||||||||F|19751016|||||||||||||||||2178037d-f86b-4f12-8d8b-be3ebc220029^^^UUID^v4\r"
		        + "PV1||O|1^Unknown||||1^Super User (admin)|||||||||||||||||||||||||||||||||||||20090714|||||||V\r"
		        + "ORC|RE||||||||20090728165937|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|2|NM|5497^CD4 COUNT^99DCT||123|||||||||20090714\r"
		        + "OBR|3|||23^FOOD CONSTRUCT^99DCT\r"
		        + "OBX|1|CWE|21^FOOD ASSISTANCE FOR ENTIRE FAMILY^99DCT||22^UNKNOWN^99DCT^2471^UNKNOWN^99NAM|||||||||20090714";
		ORUR01Handler oruHandler = new ORUR01Handler();
		Message hl7message = parser.parse(hl7String);
		ORU_R01 oru = (ORU_R01) hl7message;
		List<NK1> nk1List = oruHandler.getNK1List(oru);
		for (NK1 nk1 : nk1List)
			oruHandler.processNK1(patient, nk1);
		
		// find the relative in the database
		Person relative = personService.getPersonByUuid("2178037d-f86b-4f12-8d8b-be3ebc220029");
		Assert.assertNotNull("a new person was not created", relative);
		
		// see if the relative made it into the relationship properly
		List<Relationship> rels = personService.getRelationships(relative, patient, new RelationshipType(3));
		Assert.assertTrue("new relationship was not created", !rels.isEmpty() && rels.size() == 1);
	}
	
	/**
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test(expected = ApplicationException.class)
	public void processMessage_shouldFailIfQuestionDatatypeIsCodedAndABooleanIsNotAValidAnswer() throws Exception {
		GlobalProperty trueConceptGlobalProperty = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT, "7",
		        "Concept id of the concept defining the TRUE boolean concept");
		GlobalProperty falseConceptGlobalProperty = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT, "8",
		        "Concept id of the concept defining the TRUE boolean concept");
		Context.getAdministrationService().saveGlobalProperty(trueConceptGlobalProperty);
		Context.getAdministrationService().saveGlobalProperty(falseConceptGlobalProperty);
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		        + "PID|||7^^^^||Collet^Test^Chebaskwony||\r"
		        + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		        + "ORC|RE||||||||20080226102537|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|2|NM|4^CIVIL STATUS^99DCT||1|||||||||20080206";
		Assert.assertEquals("Coded", Context.getConceptService().getConcept(4).getDatatype().getName());
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
	}
	
	/**
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test(expected = ApplicationException.class)
	public void processMessage_shouldFailIfQuestionDatatypeIsNeitherBooleanNorNumericNorCoded() throws Exception {
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		        + "PID|||7^^^^||Collet^Test^Chebaskwony||\r"
		        + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		        + "ORC|RE||||||||20080226102537|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|2|NM|19^FAVORITE FOOD, NON-CODED^99DCT||1|||||||||20080206";
		Assert.assertEquals("Text", Context.getConceptService().getConcept(19).getDatatype().getName());
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
	}
	
	/**
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test
	public void processMessage_shouldSetValueAsBooleanForObsIfTheAnswerIs0Or1AndQuestionDatatypeIsBoolean() throws Exception {
		GlobalProperty trueConceptGlobalProperty = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT, "7",
		        "Concept id of the concept defining the TRUE boolean concept");
		GlobalProperty falseConceptGlobalProperty = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT, "8",
		        "Concept id of the concept defining the TRUE boolean concept");
		Context.getAdministrationService().saveGlobalProperty(trueConceptGlobalProperty);
		Context.getAdministrationService().saveGlobalProperty(falseConceptGlobalProperty);
		ObsService os = Context.getObsService();
		Assert.assertNull(os.getObs(17));
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		        + "PID|||7^^^^||Collet^Test^Chebaskwony||\r"
		        + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		        + "ORC|RE||||||||20080226102537|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|1|NM|18^FOOD ASSISTANCE^99DCT||0|||||||||20080206";
		// the expected question for the obs in the hl7 message has to be
		// Boolean
		Assert.assertEquals("Boolean", Context.getConceptService().getConcept(18).getDatatype().getName());
		List<Obs> oldList = os.getObservationsByPersonAndConcept(new Person(7), new Concept(18));
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		List<Obs> newList = os.getObservationsByPersonAndConcept(new Person(7), new Concept(18));
		Obs newObservation = null;
		for (Obs newObs : newList) {
			if (!oldList.contains(newObs) && !newObs.isObsGrouping()) {
				newObservation = newObs;
			}
		}
		Assert.assertEquals(false, newObservation.getValueBoolean());
	}
	
	/**
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test
	public void processMessage_shouldSetValue_CodedMatchingABooleanConceptForObsIfTheAnswerIs0Or1AndQuestionDatatypeIsCoded()
	        throws Exception {
		ObsService os = Context.getObsService();
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		        + "PID|||7^^^^||Collet^Test^Chebaskwony||\r"
		        + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		        + "ORC|RE||||||||20080226102537|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|2|NM|21^CIVIL STATUS^99DCT||1|||||||||20080206";
		// the expected question for the obs in the hl7 message has to be coded
		Assert.assertEquals("Coded", Context.getConceptService().getConcept(21).getDatatype().getName());
		List<Obs> oldList = os.getObservationsByPersonAndConcept(new Person(7), new Concept(21));
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		// hacky way to get the newly added obs and make tests on it
		List<Obs> newList = os.getObservationsByPersonAndConcept(new Person(7), new Concept(21));
		Obs newObservation = null;
		for (Obs newObs : newList) {
			if (!oldList.contains(newObs) && !newObs.isObsGrouping()) {
				newObservation = newObs;
			}
		}
		Assert.assertEquals(Context.getConceptService().getTrueConcept(), newObservation.getValueCoded());
	}
	
	/**
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test
	public void processMessage_shouldSetValue_NumericForObsIfQuestionDatatypeIsNumeric() throws Exception {
		ObsService os = Context.getObsService();
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		        + "PID|||7^^^^||Collet^Test^Chebaskwony||\r"
		        + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		        + "ORC|RE||||||||20080226102537|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206";
		// the expected question for the obs in the hl7 message has to be
		// numeric
		Assert.assertEquals("Numeric", Context.getConceptService().getConcept(5497).getDatatype().getName());
		List<Obs> oldList = os.getObservationsByPersonAndConcept(new Person(7), new Concept(5497));
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		List<Obs> newList = os.getObservationsByPersonAndConcept(new Person(7), new Concept(5497));
		Obs newObservation = null;
		for (Obs newObs : newList) {
			if (!oldList.contains(newObs) && !newObs.isObsGrouping()) {
				newObservation = newObs;
			}
		}
		Assert.assertEquals(450, newObservation.getValueNumeric().intValue());
	}
	
	/**
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test
	public void processMessage_shouldSetValue_NumericForObsIfQuestionDatatypeIsNumericAndTheAnswerIsEither0Or1()
	        throws Exception {
		ObsService os = Context.getObsService();
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		        + "PID|||7^^^^||Collet^Test^Chebaskwony||\r"
		        + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		        + "ORC|RE||||||||20080226102537|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|1|NM|5497^CD4, BY FACS^99DCT||1|||||||||20080206";
		// the expected question for the obs in the hl7 message has to be
		// numeric
		Assert.assertEquals("Numeric", Context.getConceptService().getConcept(5497).getDatatype().getName());
		List<Obs> oldList = os.getObservationsByPersonAndConcept(new Person(7), new Concept(5497));
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		List<Obs> newList = os.getObservationsByPersonAndConcept(new Person(7), new Concept(5497));
		Obs newObservation = null;
		for (Obs newObs : newList) {
			if (!oldList.contains(newObs) && !newObs.isObsGrouping()) {
				newObservation = newObs;
			}
		}
		Assert.assertEquals(1, newObservation.getValueNumeric().intValue());
	}
	
	/**
	 * @see ORUR01Handler#parseObs(Encounter,OBX,OBR,String)
	 */
	@Test
	public void parseObs_shouldAddCommentsToAnObservationFromNTESegments() throws Exception {
		ObsService os = Context.getObsService();
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		        + "PID|||7^^^^||Collet^Test^Chebaskwony||\r"
		        + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		        + "ORC|RE||||||||20080226102537|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|1|NM|5497^CD4, BY FACS^99DCT||1|||||||||20080206\r" + "NTE|1|L|This is a comment";
		// the expected question for the obs in the hl7 message has to be
		// numeric
		Assert.assertEquals("Numeric", Context.getConceptService().getConcept(5497).getDatatype().getName());
		List<Obs> oldList = os.getObservationsByPersonAndConcept(new Person(7), new Concept(5497));
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		List<Obs> newList = os.getObservationsByPersonAndConcept(new Person(7), new Concept(5497));
		Obs newObservation = null;
		for (Obs newObs : newList) {
			if (!oldList.contains(newObs) && !newObs.isObsGrouping()) {
				newObservation = newObs;
			}
		}
		Assert.assertEquals("This is a comment", newObservation.getComment());
	}
	
	/**
	 * @see ORUR01Handler#parseObs(Encounter,OBX,OBR,String)
	 */
	@Test
	public void parseObs_shouldAddMultipleCommentsForAnObservationAsOneComment() throws Exception {
		ObsService os = Context.getObsService();
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		        + "PID|||7^^^^||Collet^Test^Chebaskwony||\r"
		        + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		        + "ORC|RE||||||||20080226102537|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|1|NM|5497^CD4, BY FACS^99DCT||1|||||||||20080206\r"
		        + "NTE|1|L|This is a comment\r"
		        + "NTE|2|L|that spans two lines\r" + "OBX|2|NM|5497^CD4, BY FACS^99DCT||2|||||||||20080206";
		// the expected question for the obs in the hl7 message has to be
		// numeric
		Assert.assertEquals("Numeric", Context.getConceptService().getConcept(5497).getDatatype().getName());
		List<Obs> oldList = os.getObservationsByPersonAndConcept(new Person(7), new Concept(5497));
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		List<Obs> newList = os.getObservationsByPersonAndConcept(new Person(7), new Concept(5497));
		
		// get the new observation with a not-null comment; not interested in
		// the other one
		Obs newObservation = null;
		int thisIndex = 0;
		while (newObservation == null && thisIndex < newList.size()) {
			Obs newObs = newList.get(thisIndex++);
			if (!oldList.contains(newObs) && !newObs.isObsGrouping() && newObs.getComment() != null)
				newObservation = newObs;
		}
		Assert.assertEquals("This is a comment that spans two lines", newObservation.getComment());
	}
	
	/**
	 * @see ORUR01Handler#parseObs(Encounter,OBX,OBR,String)
	 */
	@Test
	public void parseObs_shouldAddCommentsToAnObservationGroup() throws Exception {
		ObsService os = Context.getObsService();
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		        + "PID|||7^^^^||Collet^Test^Chebaskwony||\r"
		        + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		        + "ORC|RE||||||||20080226102537|1^Super User\r"
		        + "OBR|1|||23^FOOD CONSTRUCT^99DCT\r"
		        + "NTE|1|L|This is a comment\r"
		        + "OBX|1|NM|5497^CD4, BY FACS^99DCT||1|||||||||20080206\r"
		        + "NTE|1|L|This should not be considered :-)";
		List<Obs> oldList = os.getObservationsByPersonAndConcept(new Person(7), new Concept(23));
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		List<Obs> newList = os.getObservationsByPersonAndConcept(new Person(7), new Concept(23));
		Obs newObservation = null;
		for (Obs newObs : newList) {
			if (!oldList.contains(newObs) && newObs.isObsGrouping()) {
				newObservation = newObs;
			}
		}
		Assert.assertEquals("This is a comment", newObservation.getComment());
	}
	
	/**
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test
	public void processMessage_shouldCreateAnEncounterAndFindTheProviderByIdentifier() throws Exception {
		executeDataSet("org/openmrs/api/include/ProviderServiceTest-initial.xml");
		int patientId = 2;
		Patient patient = new Patient(patientId);
		List<Encounter> encForPatient1 = Context.getEncounterService().getEncountersByPatient(patient);
		
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		        + "PID|||"
		        + patientId
		        + "^^^^||Hornblower^Horatio^Test||\r"
		        + "PV1||O|1^Unknown Location||||8a760^name^^^^^^^&"
		        + HL7Constants.PROVIDER_ASSIGNING_AUTH_IDENTIFIER
		        + "&L|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		        + "ORC|RE||||||||20080226102537|1^Super User\r" + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT";
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		
		// check for the new encounter
		List<Encounter> encForPatient2 = Context.getEncounterService().getEncountersByPatient(patient);
		encForPatient2.removeAll(encForPatient1);//retain only the new encounter
		Assert.assertTrue(encForPatient2.size() == 1);
		
		Provider newProvider = encForPatient2.get(0).getProvidersByRole(
		    Context.getEncounterService().getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID)).iterator()
		        .next();
		Assert.assertEquals("a2c3868a-6b90-11e0-93c3-18a905e044dc", newProvider.getUuid());
	}
	
	/**
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test
	public void processMessage_shouldCreateAnEncounterAndFindTheProviderByPersonId() throws Exception {
		int patientId = 2;
		Patient patient = new Patient(patientId);
		List<Encounter> encForPatient1 = Context.getEncounterService().getEncountersByPatient(patient);
		
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		        + "PID|||"
		        + patientId
		        + "^^^^||Hornblower^Horatio^Test||\r"
		        + "PV1||O|1^Unknown Location||||1^name|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		        + "ORC|RE||||||||20080226102537|1^Super User\r" + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT";
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		
		List<Encounter> encForPatient2 = Context.getEncounterService().getEncountersByPatient(patient);
		assertTrue("An encounter should have been created", (encForPatient1.size() + 1) == encForPatient2.size());
		
		encForPatient2.removeAll(encForPatient1);
		Assert.assertTrue(encForPatient2.size() == 1);
		
		Provider newProvider = encForPatient2.get(0).getProvidersByRole(
		    Context.getEncounterService().getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID)).iterator()
		        .next();
		Assert.assertEquals("c2299800-cca9-11e0-9572-0800200c9a66", newProvider.getUuid());
	}
	
	/**
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test
	public void processMessage_shouldCreateAnEncounterAndFindTheProviderByProviderId() throws Exception {
		executeDataSet("org/openmrs/api/include/ProviderServiceTest-initial.xml");
		int patientId = 2;
		Patient patient = new Patient(patientId);
		List<Encounter> encForPatient1 = Context.getEncounterService().getEncountersByPatient(patient);
		
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		        + "PID|||"
		        + patientId
		        + "^^^^||Hornblower^Horatio^Test||\r"
		        + "PV1||O|1^Unknown Location||||8^name^^^^^^^&"
		        + HL7Constants.PROVIDER_ASSIGNING_AUTH_PROV_ID
		        + "&L|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		        + "ORC|RE||||||||20080226102537|1^Super User\r" + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT";
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		
		// check for the new encounter
		List<Encounter> encForPatient2 = Context.getEncounterService().getEncountersByPatient(patient);
		encForPatient2.removeAll(encForPatient1);
		Assert.assertTrue(encForPatient2.size() == 1);
		
		Provider newProvider = encForPatient2.get(0).getProvidersByRole(
		    Context.getEncounterService().getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID)).iterator()
		        .next();
		Assert.assertEquals("1f9e8336-6b95-11e0-93c3-18a905e044dc", newProvider.getUuid());
	}
	
	/**
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test
	public void processMessage_shouldCreateAnEncounterAndFindTheProviderByUuid() throws Exception {
		executeDataSet("org/openmrs/api/include/ProviderServiceTest-initial.xml");
		int patientId = 2;
		Patient patient = new Patient(patientId);
		List<Encounter> encForPatient1 = Context.getEncounterService().getEncountersByPatient(patient);
		ProviderService ps = Context.getProviderService();
		Provider provider = ps.getProviderByIdentifier("8a760");
		
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		        + "PID|||"
		        + patientId
		        + "^^^^||Hornblower^Horatio^Test||\r"
		        + "PV1||O|1^Unknown Location||||ba4781f4-6b94-11e0-93c3-18a905e044dc^name^^^^^^^&"
		        + HL7Constants.PROVIDER_ASSIGNING_AUTH_PROV_UUID
		        + "&L|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		        + "ORC|RE||||||||20080226102537|1^Super User\r" + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT";
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
		
		// check for the new encounter
		List<Encounter> encForPatient2 = Context.getEncounterService().getEncountersByPatient(patient);
		encForPatient2.removeAll(encForPatient1);
		Assert.assertTrue(encForPatient2.size() == 1);
		
		Provider newProvider = encForPatient2.get(0).getProvidersByRole(
		    Context.getEncounterService().getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID)).iterator()
		        .next();
		Assert.assertEquals(4, newProvider.getId().intValue());
	}
	
	/**
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test(expected = ApplicationException.class)
	public void processMessage_shouldFailIfTheProviderNameTypeCodeIsNotSpecifiedAndIsNotAPersonId() throws Exception {
		int patientId = 2;
		String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		        + "PID|||"
		        + patientId
		        + "^^^^||Hornblower^Horatio^Test||\r"
		        + "PV1||O|1^Unknown Location||||Test^Super User (1-8)^^^^^^^&"
		        + HL7Constants.PROVIDER_ASSIGNING_AUTH_IDENTIFIER
		        + "&^|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		        + "ORC|RE||||||||20080226102537|1^Super User\r" + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT";
		Message hl7message = parser.parse(hl7string);
		router.processMessage(hl7message);
	}
	
	/**
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test
	public void processMessage_shouldUnderstandFormUuidIfPresent() throws Exception {
		
		// save original encounter count
		List<Encounter> encounters = Context.getEncounterService().getEncountersByPatient(new Patient(3));
		Integer originalEncounters = encounters.size();
		
		// process message
		String hl7String = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20090728170332||ORU^R01|gu99yBh4loLX2mh9cHaV|P|2.5|1||||||||d9218f76-6c39-45f4-8efa-4c5c6c199f50^AMRS.ELD.FORMUUID\r"
		        + "PID|||3^^^^||Beren^John^Bondo||\r"
		        + "PV1||O|1^Unknown||||1^Super User (admin)|||||||||||||||||||||||||||||||||||||20090714|||||||V\r"
		        + "ORC|RE||||||||20090728165937|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|2|NM|5497^CD4 COUNT^99DCT||123|||||||||20090714";
		
		Message hl7message = parser.parse(hl7String);
		router.processMessage(hl7message);
		
		// make sure an encounter was added
		encounters = Context.getEncounterService().getEncountersByPatient(new Patient(3));
		Assert.assertEquals(originalEncounters + 1, encounters.size());
		
		// get last encounter
		Encounter enc = encounters.get(encounters.size() - 1);
		
		// check the form uuid
		Form form = enc.getForm();
		Assert.assertEquals("d9218f76-6c39-45f4-8efa-4c5c6c199f50", form.getUuid());
	}
	
	/**
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test
	public void processMessage_shouldPreferFormUuidOverIdIfBothArePresent() throws Exception {
		
		// save original encounter count
		List<Encounter> encounters = Context.getEncounterService().getEncountersByPatient(new Patient(3));
		Integer originalEncounters = encounters.size();
		
		// process message
		String hl7String = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20090728170332||ORU^R01|gu99yBh4loLX2mh9cHaV|P|2.5|1||||||||4^AMRS.ELD.FORMID~c156e1a8-6731-4ebd-89ff-d0d1c45eb004^AMRS.ELD.FORMUUID\r"
		        + "PID|||3^^^^||Beren^John^Bondo||\r"
		        + "PV1||O|1^Unknown||||1^Super User (admin)|||||||||||||||||||||||||||||||||||||20090714|||||||V\r"
		        + "ORC|RE||||||||20090728165937|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|2|NM|5497^CD4 COUNT^99DCT||123|||||||||20090714";
		
		Message hl7message = parser.parse(hl7String);
		router.processMessage(hl7message);
		
		// make sure an encounter was added
		encounters = Context.getEncounterService().getEncountersByPatient(new Patient(3));
		Assert.assertEquals(originalEncounters + 1, encounters.size());
		
		// get last encounter
		Encounter enc = encounters.get(encounters.size() - 1);
		
		// check the form uuid
		Form form = enc.getForm();
		Assert.assertEquals("c156e1a8-6731-4ebd-89ff-d0d1c45eb004", form.getUuid());
	}
	
	/**
	 * @see ORUR01Handler#processMessage(Message)
	 */
	@Test
	public void processMessage_shouldPreferFormIdIfUuidIsNotFound() throws Exception {
		
		// save original encounter count
		List<Encounter> encounters = Context.getEncounterService().getEncountersByPatient(new Patient(3));
		Integer originalEncounters = encounters.size();
		
		// process message
		String hl7String = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20090728170332||ORU^R01|gu99yBh4loLX2mh9cHaV|P|2.5|1||||||||1^AMRS.ELD.FORMID~oicu812^AMRS.ELD.FORMUUID^^\r"
		        + "PID|||3^^^^||Beren^John^Bondo||\r"
		        + "PV1||O|1^Unknown||||1^Super User (admin)|||||||||||||||||||||||||||||||||||||20090714|||||||V\r"
		        + "ORC|RE||||||||20090728165937|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|2|NM|5497^CD4 COUNT^99DCT||123|||||||||20090714";
		
		Message hl7message = parser.parse(hl7String);
		router.processMessage(hl7message);
		
		// make sure an encounter was added
		encounters = Context.getEncounterService().getEncountersByPatient(new Patient(3));
		Assert.assertEquals(originalEncounters + 1, encounters.size());
		
		// get last encounter
		Encounter enc = encounters.get(encounters.size() - 1);
		
		// check the form id
		Form form = enc.getForm();
		Assert.assertEquals(1, form.getId().intValue());
	}
	
	/**
	 * @see ORUR01Handler#processMessage(Message)
	 * 
	 */
	@Test
	public void processMessage_shouldSetComplexDataForObsWithComplexConcepts() throws Exception {
		ObsHandler handler = new ObsHandler();
		final String handlerName = "NeigborHandler";
		final String data = "{\"firstname\":\"Horatio\"}";
		Context.getObsService().registerHandler(handlerName, handler);
		try {
			String hl7string = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
			        + "PID|||3^^^^||John3^Doe^||\r"
			        + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
			        + "ORC|RE||||||||20080226102537|1^Super User\r"
			        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
			        + "OBX|1|ED|6043^uiNEIHBOR^99DCT||^^^^" + data + "|||||||||20080206\r";
			Message hl7message = parser.parse(hl7string);
			router.processMessage(hl7message);
		}
		finally {
			Context.getObsService().removeHandler(handlerName);
		}
		Assert.assertEquals(data, handler.getCreatedObs().getComplexData().getData());
	}
	
	private class ObsHandler implements ComplexObsHandler {
		
		private Obs createdObs;
		
		/**
		 * @return the createdObs
		 */
		public Obs getCreatedObs() {
			return createdObs;
		}
		
		@Override
		public Obs saveObs(Obs obs) throws APIException {
			createdObs = obs;
			return obs;
		}
		
		@Override
		public Obs getObs(Obs obs, String view) {
			return null;
		}
		
		@Override
		public boolean purgeComplexData(Obs obs) {
			return false;
		}
		
		/**
		 * @see org.openmrs.obs.ComplexObsHandler#getSupportedViews()
		 */
		@Override
		public String[] getSupportedViews() {
			return new String[0];
		}
		
		/**
		 * @see org.openmrs.obs.ComplexObsHandler#supportsView(java.lang.String)
		 */
		@Override
		public boolean supportsView(String view) {
			return false;
		}
	}



	/**
	 * @see ORUR01Handler#getForm(MSH)
	 */
	@Test
	public void getForm_shouldPassIfReturnValueIsNullWhenUuidAndIdIsNull() throws Exception {
		String hl7String = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20090728170332||ORU^R01|gu99yBh4loLX2mh9cHaV|P|2.5|1||||||||\r"
		        + "PID|||3^^^^||Beren^John^Bondo||\r"
		        + "NK1|1|Jones^Jane^Lee^^RN|3A^Parent^99REL||||||||||||F|19751016|||||||||||||||||2^^^L^PI\r"
		        + "PV1||O|1^Unknown||||1^Super User (admin)|||||||||||||||||||||||||||||||||||||20090714|||||||V\r"
		        + "ORC|RE||||||||20090728165937|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|2|NM|5497^CD4 COUNT^99DCT||123|||||||||20090714\r"
		        + "OBR|3|||23^FOOD CONSTRUCT^99DCT\r"
		        + "OBX|1|CWE|21^FOOD ASSISTANCE FOR ENTIRE FAMILY^99DCT||22^UNKNOWN^99DCT^2471^UNKNOWN^99NAM|||||||||20090714";
		
		ORUR01Handler oruHandler = new ORUR01Handler();
		Message hl7message = parser.parse(hl7String);
		ORU_R01 oru = (ORU_R01) hl7message;
		ca.uhn.hl7v2.model.v25.segment.MSH msh = oru.getMSH();
		Form form = oruHandler.getForm(msh);
		Assert.assertNull(form);
	}

	/**
	 * @see ORUR01Handler#getForm(MSH)
	 */
	@Test
	public void getForm_shouldPassIfReturnValueIsNotNullWhenUuidOrIdIsNotNull() throws Exception {
		String hl7String = "MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20090728170332||ORU^R01|gu99yBh4loLX2mh9cHaV|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		        + "PID|||3^^^^||Beren^John^Bondo||\r"
		        + "NK1|1|Jones^Jane^Lee^^RN|3A^Parent^99REL||||||||||||F|19751016|||||||||||||||||2^^^L^PI\r"
		        + "PV1||O|1^Unknown||||1^Super User (admin)|||||||||||||||||||||||||||||||||||||20090714|||||||V\r"
		        + "ORC|RE||||||||20090728165937|1^Super User\r"
		        + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		        + "OBX|2|NM|5497^CD4 COUNT^99DCT||123|||||||||20090714\r"
		        + "OBR|3|||23^FOOD CONSTRUCT^99DCT\r"
		        + "OBX|1|CWE|21^FOOD ASSISTANCE FOR ENTIRE FAMILY^99DCT||22^UNKNOWN^99DCT^2471^UNKNOWN^99NAM|||||||||20090714";
		
		ORUR01Handler oruHandler = new ORUR01Handler();
		Message hl7message = parser.parse(hl7String);
		ORU_R01 oru = (ORU_R01) hl7message;
		ca.uhn.hl7v2.model.v25.segment.MSH msh = oru.getMSH();
		Form form = oruHandler.getForm(msh);
		Assert.assertNotNull(form);	
		}
}

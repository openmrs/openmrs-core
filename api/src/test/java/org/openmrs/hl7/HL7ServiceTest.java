/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.hl7;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.handler.ORUR01Handler;
import org.openmrs.hl7.impl.HL7ServiceImpl;
import org.openmrs.module.ModuleConstants;
import org.openmrs.module.ModuleUtil;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.test.annotation.DirtiesContext;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.PL;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.NK1;
import ca.uhn.hl7v2.model.v25.segment.PV1;

/**
 * Tests methods in the {@link HL7Service}
 */
public class HL7ServiceTest extends BaseContextSensitiveTest {
	
	private Log log = LogFactory.getLog(HL7ServiceTest.class);
	
	/**
	 * @see {@link HL7Service#saveHL7InQueue(HL7InQueue)}
	 */
	@Test
	@Verifies(value = "should add generated uuid if uuid is null", method = "saveHL7InQueue(HL7InQueue)")
	public void saveHL7InQueue_shouldAddGeneratedUuidIfUuidIsNull() throws Exception {
		HL7InQueue hl7 = new HL7InQueue();
		
		hl7.setHL7Data("dummy data");
		hl7.setHL7Source(new HL7Source(1));
		hl7.setHL7SourceKey("a random key");
		hl7.setMessageState(HL7Constants.HL7_STATUS_PROCESSING);
		
		Context.getHL7Service().saveHL7InQueue(hl7);
		Assert.assertNotNull(hl7.getUuid());
	}
	
	/**
	 * @see {@link HL7Service#processHL7InQueue(HL7InQueue)}
	 */
	@Test
	@Verifies(value = "should create HL7InArchive after successful parsing", method = "processHL7InQueue(HL7InQueue)")
	public void processHL7InQueue_shouldCreateHL7InArchiveAfterSuccessfulParsing() throws Exception {
		executeDataSet("org/openmrs/hl7/include/ORUTest-initialData.xml");
		
		File tempDir = new File(System.getProperty("java.io.tmpdir"), HL7Constants.HL7_ARCHIVE_DIRECTORY_NAME);
		
		if (tempDir.exists() && tempDir.isDirectory())
			Assert.assertEquals(true, OpenmrsUtil.deleteDirectory(tempDir));
		
		//set a global property for the archives directory as a temporary folder
		GlobalProperty gp = new GlobalProperty();
		gp.setProperty(OpenmrsConstants.GLOBAL_PROPERTY_HL7_ARCHIVE_DIRECTORY);
		gp.setPropertyValue(tempDir.getAbsolutePath());
		gp.setDescription("temp test dir");
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		HL7Service hl7service = Context.getHL7Service();
		Assert.assertEquals(0, hl7service.getAllHL7InArchives().size());
		
		HL7InQueue queueItem = hl7service.getHL7InQueue(1);
		hl7service.processHL7InQueue(queueItem);
		
		Assert.assertEquals(1, hl7service.getAllHL7InArchives().size());
	}
	
	/**
	 * @see {@link HL7Service#processHL7InQueue(HL7InQueue)}
	 */
	@Test
	@Verifies(value = "should create HL7InError after failed parsing", method = "processHL7InQueue(HL7InQueue)")
	public void processHL7InQueue_shouldCreateHL7InErrorAfterFailedParsing() throws Exception {
		executeDataSet("org/openmrs/hl7/include/ORUTest-initialData.xml");
		
		// sanity check, make sure there aren't any error items
		HL7Service hl7service = Context.getHL7Service();
		Assert.assertEquals(0, hl7service.getAllHL7InErrors().size());
		
		HL7InQueue queueItem = hl7service.getHL7InQueue(2);
		hl7service.processHL7InQueue(queueItem);
		
		Assert.assertEquals(1, hl7service.getAllHL7InErrors().size());
	}
	
	/**
	 * @see {@link HL7Service#processHL7InQueue(HL7InQueue)}
	 */
	@Test(expected = HL7Exception.class)
	@Verifies(value = "should fail if given inQueue is already marked as processing", method = "processHL7InQueue(HL7InQueue)")
	public void processHL7InQueue_shouldFailIfGivenInQueueIsAlreadyMarkedAsProcessing() throws Exception {
		executeDataSet("org/openmrs/hl7/include/ORUTest-initialData.xml");
		
		HL7Service hl7service = Context.getHL7Service();
		HL7InQueue queueItem = hl7service.getHL7InQueue(1);
		queueItem.setMessageState(HL7Constants.HL7_STATUS_PROCESSING); // set this to processing
		hl7service.processHL7InQueue(queueItem);
	}
	
	/**
	 * @see {@link HL7Service#processHL7Message(Message)}
	 */
	@Test
	@Verifies(value = "should save hl7Message to the database", method = "processHL7Message(Message)")
	public void processHL7Message_shouldSaveHl7MessageToTheDatabase() throws Exception {
		executeDataSet("org/openmrs/hl7/include/ORUTest-initialData.xml");
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		
		Message result = hl7service.processHL7Message(message);
		Assert.assertNotNull(result);
		
		Concept returnVisitDateConcept = new Concept(5096);
		Calendar cal = Calendar.getInstance();
		cal.set(2008, Calendar.FEBRUARY, 29, 0, 0, 0);
		List<Obs> returnVisitDateObsForPatient3 = Context.getObsService().getObservationsByPersonAndConcept(new Patient(3),
		    returnVisitDateConcept);
		assertEquals("There should be a return visit date", 1, returnVisitDateObsForPatient3.size());
		
	}
	
	/**
	 * @see {@link HL7Service#parseHL7String(String)}
	 */
	@Test
	@Verifies(value = "should parse the given string into Message", method = "parseHL7String(String)")
	public void parseHL7String_shouldParseTheGivenStringIntoMessage() throws Exception {
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		Assert.assertNotNull(message);
	}
	
	/**
	 * @see {@link HL7Service#processHL7Message(Message)}
	 */
	@Test
	@Ignore("TRUNK-3945")
	@Verifies(value = "should parse message type supplied by module", method = "processHL7Message(Message)")
	public void processHL7Message_shouldParseMessageTypeSuppliedByModule() throws Exception {
		Properties props = super.getRuntimeProperties();
		
		props.setProperty(ModuleConstants.RUNTIMEPROPERTY_MODULE_LIST_TO_LOAD,
		    "org/openmrs/hl7/include/examplehl7handlers-0.1.omod");
		// the above module provides a handler for messages of type "ADR" with trigger "A19"
		
		ModuleUtil.startup(props);
		
		// the application context cannot restart here to load in the moduleApplicationContext that
		// calls the setHL7Handlers method so we're doing it manually here
		Class<Application> c = (Class<Application>) Context.loadClass("org.openmrs.module.examplehl7handlers.ADRHandler");
		Application classInstance = c.newInstance();
		HashMap<String, Application> map = new HashMap<String, Application>();
		map.put("ADR_A19", classInstance);
		HL7ServiceImpl.getInstance().setHL7Handlers(map);
		
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ADR^A19|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		Assert.assertNotNull(message);
		
		try {
			Message result = hl7service.processHL7Message(message);
			Assert.fail("Should not be here. The ADR_A19 parser provided by the module throws an ApplicationException.");
		}
		catch (HL7Exception e) {
			if (e.getCause() != null)
				Assert.assertEquals("In ADR A19 parser", e.getCause().getMessage());
			else {
				log.error("unable to parse message", e);
				Assert.fail("something bad happened, check the log statement 1 line up");
			}
		}
		
		ModuleUtil.shutdown();
	}
	
	/**
	 * @see {@link HL7Service#processHL7InQueue(HL7InQueue)}
	 */
	@Test
	@Ignore("TRUNK-3945")
	@Verifies(value = "should parse oru r01 message using overridden parser provided by a module", method = "processHL7InQueue(HL7InQueue)")
	public void processHL7InQueue_shouldParseOruR01MessageUsingOverriddenParserProvidedByAModule() throws Exception {
		executeDataSet("org/openmrs/hl7/include/ORUTest-initialData.xml");
		
		Properties props = super.getRuntimeProperties();
		
		props.setProperty(ModuleConstants.RUNTIMEPROPERTY_MODULE_LIST_TO_LOAD,
		    "org/openmrs/hl7/include/examplehl7handlers-0.1.omod");
		// the above module provides a handler for messages of type "ADR" with trigger "A19"
		
		ModuleUtil.startup(props);
		
		// the application context cannot restart here to load in the moduleApplicationContext that
		// calls the setHL7Handlers method so we're doing it manually here
		Class<Application> c = (Class<Application>) Context
		        .loadClass("org.openmrs.module.examplehl7handlers.AlternateORUR01Handler");
		Application classInstance = c.newInstance();
		HashMap<String, Application> map = new HashMap<String, Application>();
		map.put("ORU_R01", classInstance);
		HL7ServiceImpl.getInstance().setHL7Handlers(map);
		
		HL7Service hl7service = Context.getHL7Service();
		HL7InQueue queueItem = hl7service.getHL7InQueue(1); // a valid ORU_R01
		
		// this will create 1 HL7InError item
		hl7service.processHL7InQueue(queueItem);
		
		List<HL7InError> errors = hl7service.getAllHL7InErrors();
		HL7InError error = errors.get(errors.size() - 1); // get the last error, the one made by this test presumably
		Assert.assertTrue(error.getErrorDetails().contains("In alternate oru r01 parser"));
		
		ModuleUtil.shutdown();
	}
	
	/**
	 * @see {@link HL7Service#resolvePersonFromIdentifiers(null)}
	 */
	@Test
	@Verifies(value = "should find a person based on a patient identifier", method = "resolvePersonFromIdentifiers(null)")
	public void resolvePersonFromIdentifiers_shouldFindAPersonBasedOnAPatientIdentifier() throws Exception {
		executeDataSet("org/openmrs/hl7/include/ORUTest-initialData.xml");
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||1234^^^Test Identifier Type^PT||||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		List<NK1> nk1List = new ORUR01Handler().getNK1List(oru);
		Assert.assertEquals("too many NK1s parsed out", 1, nk1List.size());
		Person result = hl7service.resolvePersonFromIdentifiers(nk1List.get(0).getNextOfKinAssociatedPartySIdentifiers());
		Assert.assertNotNull("should have found a person", result);
		Assert.assertEquals("found the wrong person", 2, result.getId().intValue());
	}
	
	/**
	 * @see {@link HL7Service#resolvePersonFromIdentifiers(null)}
	 */
	@Test
	@Verifies(value = "should find a person based on a UUID", method = "resolvePersonFromIdentifiers(null)")
	public void resolvePersonFromIdentifiers_shouldFindAPersonBasedOnAUUID() throws Exception {
		executeDataSet("org/openmrs/hl7/include/ORUTest-initialData.xml");
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||2178037d-f86b-4f12-8d8b-be3ebc220022^^^UUID^v4||||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		List<NK1> nk1List = new ORUR01Handler().getNK1List(oru);
		Assert.assertEquals("too many NK1s parsed out", 1, nk1List.size());
		Person result = hl7service.resolvePersonFromIdentifiers(nk1List.get(0).getNextOfKinAssociatedPartySIdentifiers());
		Assert.assertNotNull("should have found a person", result);
		Assert.assertEquals("found the wrong person", 2, result.getId().intValue());
	}
	
	/**
	 * @see {@link HL7Service#resolvePersonFromIdentifiers(null)}
	 */
	@Test
	@Verifies(value = "should find a person based on the internal person ID", method = "resolvePersonFromIdentifiers(null)")
	public void resolvePersonFromIdentifiers_shouldFindAPersonBasedOnTheInternalPersonID() throws Exception {
		executeDataSet("org/openmrs/hl7/include/ORUTest-initialData.xml");
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||2^^^L^PN||||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		List<NK1> nk1List = new ORUR01Handler().getNK1List(oru);
		Assert.assertEquals("too many NK1s parsed out", 1, nk1List.size());
		Person result = hl7service.resolvePersonFromIdentifiers(nk1List.get(0).getNextOfKinAssociatedPartySIdentifiers());
		Assert.assertNotNull("should have found a person", result);
		Assert.assertEquals("found the wrong person", 2, result.getId().intValue());
	}
	
	/**
	 * @see {@link HL7Service#resolvePersonFromIdentifiers(null)}
	 */
	@Test
	@Verifies(value = "should return null if no person is found", method = "resolvePersonFromIdentifiers(null)")
	public void resolvePersonFromIdentifiers_shouldReturnNullIfNoPersonIsFound() throws Exception {
		executeDataSet("org/openmrs/hl7/include/ORUTest-initialData.xml");
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||1000^^^L^PN||||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		List<NK1> nk1List = new ORUR01Handler().getNK1List(oru);
		Assert.assertEquals("too many NK1s parsed out", 1, nk1List.size());
		Person result = hl7service.resolvePersonFromIdentifiers(nk1List.get(0).getNextOfKinAssociatedPartySIdentifiers());
		Assert.assertNull("should not have found a person", result);
	}
	
	/**
	 * @see {@link HL7Service#createPersonFromNK1(NK1)}
	 */
	@Test(expected = HL7Exception.class)
	@Verifies(value = "should fail if a person with the same UUID exists", method = "getPersonFromNK1(NK1)")
	public void getPersonFromNK1_shouldFailIfAPersonWithTheSameUUIDExists() throws Exception {
		executeDataSet("org/openmrs/hl7/include/ORUTest-initialData.xml");
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||2178037d-f86b-4f12-8d8b-be3ebc220022^^^UUID^v4||||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		List<NK1> nk1List = new ORUR01Handler().getNK1List(oru);
		hl7service.createPersonFromNK1(nk1List.get(0));
		Assert.fail("should have thrown an exception");
	}
	
	/**
	 * @see {@link HL7Service#createPersonFromNK1(NK1)}
	 */
	@Test(expected = HL7Exception.class)
	@Verifies(value = "should fail if no birthdate specified", method = "getPersonFromNK1(NK1)")
	public void getPersonFromNK1_shouldFailIfNoBirthdateSpecified() throws Exception {
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M||||||||||||||||||2178037d-f86b-4f12-8d8b-be3ebc220022^^^UUID^v4||||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		List<NK1> nk1List = new ORUR01Handler().getNK1List(oru);
		hl7service.createPersonFromNK1(nk1List.get(0));
		Assert.fail("should have thrown an exception");
	}
	
	/**
	 * @see {@link HL7Service#createPersonFromNK1(NK1)}
	 */
	@Test(expected = HL7Exception.class)
	@Verifies(value = "should fail if no gender specified", method = "getPersonFromNK1(NK1)")
	public void getPersonFromNK1_shouldFailIfNoGenderSpecified() throws Exception {
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL|||||||||||||19410501|||||||||||||||||2178037d-f86b-4f12-8d8b-be3ebc220022^^^UUID^v4||||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		List<NK1> nk1List = new ORUR01Handler().getNK1List(oru);
		hl7service.createPersonFromNK1(nk1List.get(0));
		Assert.fail("should have thrown an exception");
	}
	
	/**
	 * @see {@link HL7Service#createPersonFromNK1(NK1)}
	 */
	@Test(expected = HL7Exception.class)
	@Verifies(value = "should fail on an invalid gender", method = "getPersonFromNK1(NK1)")
	public void getPersonFromNK1_shouldFailOnAnInvalidGender() throws Exception {
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||Q|19410501|||||||||||||||||2178037d-f86b-4f12-8d8b-be3ebc220022^^^UUID^v4||||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		List<NK1> nk1List = new ORUR01Handler().getNK1List(oru);
		hl7service.createPersonFromNK1(nk1List.get(0));
		Assert.fail("should have thrown an exception");
	}
	
	/**
	 * @see {@link HL7Service#createPersonFromNK1(NK1)}
	 */
	@Test
	@Verifies(value = "should return a saved new person", method = "getPersonFromNK1(NK1)")
	public void getPersonFromNK1_shouldReturnASavedNewPerson() throws Exception {
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||2178037d-f86b-4f12-8d8b-be3ebc220022^^^UUID^v4||||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		List<NK1> nk1List = new ORUR01Handler().getNK1List(oru);
		Person result = hl7service.createPersonFromNK1(nk1List.get(0));
		Assert.assertNotNull("should have returned a person", result);
		Assert.assertNotNull("the person should exist", Context.getPersonService().getPersonByUuid(result.getUuid()));
	}
	
	/**
	 * @see {@link HL7Service#createPersonFromNK1(NK1)}
	 */
	@Test
	@Verifies(value = "should return a Patient if valid patient identifiers exist", method = "getPersonFromNK1(NK1)")
	public void getPersonFromNK1_shouldReturnAPatientIfValidPatientIdentifiersExist() throws Exception {
		executeDataSet("org/openmrs/hl7/include/ORUTest-initialData.xml");
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||2178037d-f86b-4f12-8d8b-be3ebc220029^^^UUID^v4~9-1^^^Test Identifier Type^PT||||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		List<NK1> nk1List = new ORUR01Handler().getNK1List(oru);
		Person result = hl7service.createPersonFromNK1(nk1List.get(0));
		Assert.assertNotNull("should have returned something", result);
		Assert.assertTrue("should have returned a Patient", result instanceof Patient);
	}
	
	/**
	 * @see {@link HL7Service#getUuidFromIdentifiers(null)}
	 */
	@Test
	@Verifies(value = "should find a UUID in any position of the array", method = "getUuidFromIdentifiers(null)")
	public void getUuidFromIdentifiers_shouldFindAUUIDInAnyPositionOfTheArray() throws Exception {
		// at the beginning of the list
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||2178037d-f86b-4f12-8d8b-be3ebc220022^^^UUID^v4~5^^^L^PN||||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		List<NK1> nk1List = new ORUR01Handler().getNK1List(oru);
		CX[] identifiers = nk1List.get(0).getNextOfKinAssociatedPartySIdentifiers();
		String result = hl7service.getUuidFromIdentifiers(identifiers);
		Assert.assertEquals("2178037d-f86b-4f12-8d8b-be3ebc220022", result);
		result = null;
		
		// at the end of the list
		message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||5^^^L^PN~2178037d-f86b-4f12-8d8b-be3ebc220022^^^UUID^v4||||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		oru = (ORU_R01) message;
		nk1List = new ORUR01Handler().getNK1List(oru);
		identifiers = nk1List.get(0).getNextOfKinAssociatedPartySIdentifiers();
		result = hl7service.getUuidFromIdentifiers(identifiers);
		Assert.assertEquals("2178037d-f86b-4f12-8d8b-be3ebc220022", result);
		result = null;
		
		// middle of the list
		message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||5^^^L^PN~2178037d-f86b-4f12-8d8b-be3ebc220022^^^UUID^v4~101-3^^^MTRH^PT||||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		oru = (ORU_R01) message;
		nk1List = new ORUR01Handler().getNK1List(oru);
		identifiers = nk1List.get(0).getNextOfKinAssociatedPartySIdentifiers();
		result = hl7service.getUuidFromIdentifiers(identifiers);
		Assert.assertEquals("2178037d-f86b-4f12-8d8b-be3ebc220022", result);
	}
	
	/**
	 * @see {@link HL7Service#getUuidFromIdentifiers(null)}
	 */
	@Test
	@Verifies(value = "should return null if no UUID found", method = "getUuidFromIdentifiers(null)")
	public void getUuidFromIdentifiers_shouldReturnNullIfNoUUIDFound() throws Exception {
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||5^^^L^PN||||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		List<NK1> nk1List = new ORUR01Handler().getNK1List(oru);
		CX[] identifiers = nk1List.get(0).getNextOfKinAssociatedPartySIdentifiers();
		String result = hl7service.getUuidFromIdentifiers(identifiers);
		Assert.assertNull("should have returned null", result);
	}
	
	/**
	 * @see {@link HL7Service#getUuidFromIdentifiers(null)}
	 */
	@Test
	@Verifies(value = "should not fail if multiple similar UUIDs exist in identifiers", method = "getUuidFromIdentifiers(null)")
	public void getUuidFromIdentifiers_shouldNotFailIfMultipleSimilarUUIDsExistInIdentifiers() throws Exception {
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||2178037d-f86b-4f12-8d8b-be3ebc220022^^^UUID^v4~2178037d-f86b-4f12-8d8b-be3ebc220022^^^UUID^v4||||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		List<NK1> nk1List = new ORUR01Handler().getNK1List(oru);
		CX[] identifiers = nk1List.get(0).getNextOfKinAssociatedPartySIdentifiers();
		String result = hl7service.getUuidFromIdentifiers(identifiers);
		Assert.assertEquals("2178037d-f86b-4f12-8d8b-be3ebc220022", result);
	}
	
	/**
	 * @see {@link HL7Service#getUuidFromIdentifiers(null)}
	 */
	@Test(expected = HL7Exception.class)
	@Verifies(value = "should fail if multiple different UUIDs exist in identifiers", method = "getUuidFromIdentifiers(null)")
	public void getUuidFromIdentifiers_shouldFailIfMultipleDifferentUUIDsExistInIdentifiers() throws Exception {
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||2178037d-f86b-4f12-8d8b-be3ebc220022^^^UUID^v4~2178037d-f86b-4f12-8d8b-be3ebc220023^^^UUID^v4||||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		List<NK1> nk1List = new ORUR01Handler().getNK1List(oru);
		CX[] identifiers = nk1List.get(0).getNextOfKinAssociatedPartySIdentifiers();
		hl7service.getUuidFromIdentifiers(identifiers);
		Assert.fail("should have failed");
	}
	
	/**
	 * @see {@link HL7Service#getUuidFromIdentifiers(null)}
	 */
	@Test
	@Verifies(value = "should not fail if no assigning authority is found", method = "getUuidFromIdentifiers(null)")
	public void getUuidFromIdentifiers_shouldNotFailIfNoAssigningAuthorityIsFound() throws Exception {
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||5^^^^PT||||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		List<NK1> nk1List = new ORUR01Handler().getNK1List(oru);
		CX[] identifiers = nk1List.get(0).getNextOfKinAssociatedPartySIdentifiers();
		hl7service.getUuidFromIdentifiers(identifiers);
	}
	
	/**
	 * @see {@link HL7Service#resolveLocationId(ca.uhn.hl7v2.model.v25.datatype.PL)}
	 */
	@Test
	@Verifies(value = "should return internal identifier of location if only location name is specified", method = "resolveLocationId(ca.uhn.hl7v2.model.v25.datatype.PL)")
	public void resolveLocationId_shouldReturnInternalIdentifierOfLocationIfOnlyLocationNameIsSpecified() throws Exception {
		executeDataSet("org/openmrs/hl7/include/ORUTest-initialData.xml");
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||1000^^^L^PN||||\r"
		                + "PV1||O|99999^0^0^0&Test Location&0||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		PV1 pv1 = oru.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1();
		Assert.assertNotNull("PV1 parsed as null", pv1);
		PL hl7Location = pv1.getAssignedPatientLocation();
		Integer locationId = hl7service.resolveLocationId(hl7Location);
		Assert.assertEquals("Resolved and given locationId shoud be equals", Integer.valueOf(1), locationId);
	}
	
	/**
	 * @see {@link HL7Service#resolveLocationId(ca.uhn.hl7v2.model.v25.datatype.PL)}
	 */
	@Test
	@Verifies(value = "should return internal identifier of location if only location id is specified", method = "resolveLocationId(null)")
	public void resolveLocationId_shouldReturnInternalIdentifierOfLocationIfOnlyLocationIdIsSpecified() throws Exception {
		executeDataSet("org/openmrs/hl7/include/ORUTest-initialData.xml");
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||1000^^^L^PN||||\r"
		                + "PV1||O|1^0^0^0&Test Location&0||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		PV1 pv1 = oru.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1();
		Assert.assertNotNull("PV1 parsed as null", pv1);
		PL hl7Location = pv1.getAssignedPatientLocation();
		Integer locationId = hl7service.resolveLocationId(hl7Location);
		Assert.assertEquals("Resolved and given locationId shoud be equals", Integer.valueOf(1), locationId);
	}
	
	/**
	 * @see {@link HL7Service#resolveLocationId(ca.uhn.hl7v2.model.v25.datatype.PL)}
	 */
	@Test
	@Verifies(value = "should return null if location id and name are incorrect", method = "resolveLocationId(null)")
	public void resolveLocationId_shouldReturnNullIfLocationIdAndNameAreIncorrect() throws Exception {
		executeDataSet("org/openmrs/hl7/include/ORUTest-initialData.xml");
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||1000^^^L^PN||||\r"
		                + "PV1||O|99999^0^0^0&Unknown&0||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		PV1 pv1 = oru.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1();
		Assert.assertNotNull("PV1 parsed as null", pv1);
		PL hl7Location = pv1.getAssignedPatientLocation();
		Integer locationId = hl7service.resolveLocationId(hl7Location);
		Assert.assertNull(locationId);
	}
	
}

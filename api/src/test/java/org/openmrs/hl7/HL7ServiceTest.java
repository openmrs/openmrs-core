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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.openmrs.util.OpenmrsUtil.deleteDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.PL;
import ca.uhn.hl7v2.model.v25.datatype.XCN;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.NK1;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.model.v25.segment.PV1;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests methods in the {@link HL7Service}
 */
public class HL7ServiceTest extends BaseContextSensitiveTest {
	
	private static final Logger log = LoggerFactory.getLogger(HL7ServiceTest.class);
	protected static final String XML_FILENAME = "org/openmrs/api/include/UserServiceTest.xml";
	
	/**
	 * @see HL7Service#saveHL7InQueue(HL7InQueue)
	 */
	@Test
	public void saveHL7InQueue_shouldAddGeneratedUuidIfUuidIsNull() {
		HL7InQueue hl7 = new HL7InQueue();
		
		hl7.setHL7Data("dummy data");
		hl7.setHL7Source(new HL7Source(1));
		hl7.setHL7SourceKey("a random key");
		hl7.setMessageState(HL7Constants.HL7_STATUS_PROCESSING);
		
		Context.getHL7Service().saveHL7InQueue(hl7);
		assertNotNull(hl7.getUuid());
	}
	
	/**
	 * @throws HL7Exception
	 * @throws IOException
	 * @see HL7Service#processHL7InQueue(HL7InQueue)
	 */
	@Test
	public void processHL7InQueue_shouldCreateHL7InArchiveAfterSuccessfulParsing() throws HL7Exception, IOException {
		executeDataSet("org/openmrs/hl7/include/ORUTest-initialData.xml");
		
		File tempDir = new File(System.getProperty("java.io.tmpdir"), HL7Constants.HL7_ARCHIVE_DIRECTORY_NAME);
		
		if (tempDir.exists() && tempDir.isDirectory())
			assertTrue(deleteDirectory(tempDir));
		
		//set a global property for the archives directory as a temporary folder
		GlobalProperty gp = new GlobalProperty();
		gp.setProperty(OpenmrsConstants.GLOBAL_PROPERTY_HL7_ARCHIVE_DIRECTORY);
		gp.setPropertyValue(tempDir.getAbsolutePath());
		gp.setDescription("temp test dir");
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		HL7Service hl7service = Context.getHL7Service();
		assertEquals(0, hl7service.getAllHL7InArchives().size());
		
		HL7InQueue queueItem = hl7service.getHL7InQueue(1);
		hl7service.processHL7InQueue(queueItem);
		
		assertEquals(1, hl7service.getAllHL7InArchives().size());
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#processHL7InQueue(HL7InQueue)
	 */
	@Test
	public void processHL7InQueue_shouldCreateHL7InErrorAfterFailedParsing() throws HL7Exception {
		executeDataSet("org/openmrs/hl7/include/ORUTest-initialData.xml");
		
		// sanity check, make sure there aren't any error items
		HL7Service hl7service = Context.getHL7Service();
		assertEquals(0, hl7service.getAllHL7InErrors().size());
		
		HL7InQueue queueItem = hl7service.getHL7InQueue(2);
		hl7service.processHL7InQueue(queueItem);
		
		assertEquals(1, hl7service.getAllHL7InErrors().size());
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#processHL7InQueue(HL7InQueue)
	 */
	@Test
	public void processHL7InQueue_shouldFailIfGivenInQueueIsAlreadyMarkedAsProcessing() throws HL7Exception {
		executeDataSet("org/openmrs/hl7/include/ORUTest-initialData.xml");
		
		HL7Service hl7service = Context.getHL7Service();
		HL7InQueue queueItem = hl7service.getHL7InQueue(1);
		queueItem.setMessageState(HL7Constants.HL7_STATUS_PROCESSING); // set this to processing
		assertThrows(HL7Exception.class, () -> hl7service.processHL7InQueue(queueItem));
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#processHL7Message(Message)
	 */
	@Test
	public void processHL7Message_shouldSaveHl7MessageToTheDatabase() throws HL7Exception {
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
		assertNotNull(result);
		
		Concept returnVisitDateConcept = new Concept(5096);
		Calendar cal = Calendar.getInstance();
		cal.set(2008, Calendar.FEBRUARY, 29, 0, 0, 0);
		List<Obs> returnVisitDateObsForPatient3 = Context.getObsService().getObservationsByPersonAndConcept(new Patient(3),
		    returnVisitDateConcept);
		assertEquals(1, returnVisitDateObsForPatient3.size(), "There should be a return visit date");
		
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#parseHL7String(String)
	 */
	@Test
	public void parseHL7String_shouldParseTheGivenStringIntoMessage() throws HL7Exception {
		HL7Service hl7service = Context.getHL7Service();
		Message message = hl7service
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r"
		                + "PID|||3^^^^||John3^Doe^||\r"
		                + "PV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r"
		                + "ORC|RE||||||||20080226102537|1^Super User\r"
		                + "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r"
		                + "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r"
		                + "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		assertNotNull(message);
	}
	
	/**
	 * @see HL7Service#processHL7Message(Message)
	 */
	@Test
	@Disabled("TRUNK-3945")
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
		HashMap<String, Application> map = new HashMap<>();
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
		assertNotNull(message);
		
		try {
			hl7service.processHL7Message(message);
			fail("Should not be here. The ADR_A19 parser provided by the module throws an ApplicationException.");
		}
		catch (HL7Exception e) {
			if (e.getCause() != null)
				assertEquals("In ADR A19 parser", e.getCause().getMessage());
			else {
				log.error("unable to parse message", e);
				fail("something bad happened, check the log statement 1 line up");
			}
		}
		
		ModuleUtil.shutdown();
	}
	
	/**
	 * @see HL7Service#processHL7InQueue(HL7InQueue)
	 */
	@Test
	@Disabled("TRUNK-3945")
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
		HashMap<String, Application> map = new HashMap<>();
		map.put("ORU_R01", classInstance);
		HL7ServiceImpl.getInstance().setHL7Handlers(map);
		
		HL7Service hl7service = Context.getHL7Service();
		HL7InQueue queueItem = hl7service.getHL7InQueue(1); // a valid ORU_R01
		
		// this will create 1 HL7InError item
		hl7service.processHL7InQueue(queueItem);
		
		List<HL7InError> errors = hl7service.getAllHL7InErrors();
		HL7InError error = errors.get(errors.size() - 1); // get the last error, the one made by this test presumably
		assertTrue(error.getErrorDetails().contains("In alternate oru r01 parser"));
		
		ModuleUtil.shutdown();
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#resolvePersonFromIdentifiers(null)
	 */
	@Test
	public void resolvePersonFromIdentifiers_shouldFindAPersonBasedOnAPatientIdentifier() throws HL7Exception {
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
		assertEquals(1, nk1List.size(), "too many NK1s parsed out");
		Person result = hl7service.resolvePersonFromIdentifiers(nk1List.get(0).getNextOfKinAssociatedPartySIdentifiers());
		assertNotNull(result, "should have found a person");
		assertEquals(2, result.getId().intValue(), "found the wrong person");
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#resolvePersonFromIdentifiers(null)
	 */
	@Test
	public void resolvePersonFromIdentifiers_shouldFindAPersonBasedOnAUUID() throws HL7Exception {
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
		assertEquals(1, nk1List.size(), "too many NK1s parsed out");
		Person result = hl7service.resolvePersonFromIdentifiers(nk1List.get(0).getNextOfKinAssociatedPartySIdentifiers());
		assertNotNull(result, "should have found a person");
		assertEquals(2, result.getId().intValue(), "found the wrong person");
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#resolvePersonFromIdentifiers(null)
	 */
	@Test
	public void resolvePersonFromIdentifiers_shouldFindAPersonBasedOnTheInternalPersonID() throws HL7Exception {
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
		assertEquals(1, nk1List.size(), "too many NK1s parsed out");
		Person result = hl7service.resolvePersonFromIdentifiers(nk1List.get(0).getNextOfKinAssociatedPartySIdentifiers());
		assertNotNull(result, "should have found a person");
		assertEquals(2, result.getId().intValue(), "found the wrong person");
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#resolvePersonFromIdentifiers(null)
	 */
	@Test
	public void resolvePersonFromIdentifiers_shouldReturnNullIfNoPersonIsFound() throws HL7Exception {
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
		assertEquals( 1, nk1List.size(), "too many NK1s parsed out");
		Person result = hl7service.resolvePersonFromIdentifiers(nk1List.get(0).getNextOfKinAssociatedPartySIdentifiers());
		assertNull(result, "should not have found a person");
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#createPersonFromNK1(NK1)
	 */
	@Test
	public void getPersonFromNK1_shouldFailIfAPersonWithTheSameUUIDExists() throws HL7Exception {
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
		assertThrows(HL7Exception.class, () -> hl7service.createPersonFromNK1(nk1List.get(0)));
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#createPersonFromNK1(NK1)
	 */
	@Test
	public void getPersonFromNK1_shouldFailIfNoBirthdateSpecified() throws HL7Exception {
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
		assertThrows(HL7Exception.class, () -> hl7service.createPersonFromNK1(nk1List.get(0)));
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#createPersonFromNK1(NK1)
	 */
	@Test
	public void getPersonFromNK1_shouldFailIfNoGenderSpecified() throws HL7Exception {
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
		assertThrows(HL7Exception.class, () -> hl7service.createPersonFromNK1(nk1List.get(0)));
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#createPersonFromNK1(NK1)
	 */
	@Test
	public void getPersonFromNK1_shouldFailOnAnInvalidGender() throws HL7Exception {
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
		assertThrows(HL7Exception.class, () -> hl7service.createPersonFromNK1(nk1List.get(0)));
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#createPersonFromNK1(NK1)
	 */
	@Test
	public void getPersonFromNK1_shouldReturnASavedNewPerson() throws HL7Exception {
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
		assertNotNull(result, "should have returned a person");
		assertNotNull(Context.getPersonService().getPersonByUuid(result.getUuid()), "the person should exist");
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#createPersonFromNK1(NK1)
	 */
	@Test
	public void getPersonFromNK1_shouldReturnAPatientIfValidPatientIdentifiersExist() throws HL7Exception {
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
		assertNotNull(result, "should have returned something");
		assertTrue(result instanceof Patient, "should have returned a Patient");
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#getUuidFromIdentifiers(null)
	 */
	@Test
	public void getUuidFromIdentifiers_shouldFindAUUIDInAnyPositionOfTheArray() throws HL7Exception {
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
		assertEquals("2178037d-f86b-4f12-8d8b-be3ebc220022", result);
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
		assertEquals("2178037d-f86b-4f12-8d8b-be3ebc220022", result);
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
		assertEquals("2178037d-f86b-4f12-8d8b-be3ebc220022", result);
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#getUuidFromIdentifiers(null)
	 */
	@Test
	public void getUuidFromIdentifiers_shouldReturnNullIfNoUUIDFound() throws HL7Exception {
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
		assertNull(result, "should have returned null");
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#getUuidFromIdentifiers(null)
	 */
	@Test
	public void getUuidFromIdentifiers_shouldNotFailIfMultipleSimilarUUIDsExistInIdentifiers() throws HL7Exception {
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
		assertEquals("2178037d-f86b-4f12-8d8b-be3ebc220022", result);
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#getUuidFromIdentifiers(null)
	 */
	@Test
	public void getUuidFromIdentifiers_shouldFailIfMultipleDifferentUUIDsExistInIdentifiers() throws HL7Exception {
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
		assertThrows(HL7Exception.class, () -> hl7service.getUuidFromIdentifiers(identifiers));
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#getUuidFromIdentifiers(null)
	 */
	@Test
	public void getUuidFromIdentifiers_shouldNotFailIfNoAssigningAuthorityIsFound() throws HL7Exception {
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
	 * @throws HL7Exception
	 * @see HL7Service#resolveLocationId(ca.uhn.hl7v2.model.v25.datatype.PL)
	 */
	@Test
	public void resolveLocationId_shouldReturnInternalIdentifierOfLocationIfOnlyLocationNameIsSpecified()
	        throws HL7Exception {
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
		assertNotNull(pv1, "PV1 parsed as null");
		PL hl7Location = pv1.getAssignedPatientLocation();
		Integer locationId = hl7service.resolveLocationId(hl7Location);
		assertEquals(Integer.valueOf(1), locationId, "Resolved and given locationId shoud be equals");
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#resolveLocationId(ca.uhn.hl7v2.model.v25.datatype.PL)
	 */
	@Test
	public void resolveLocationId_shouldReturnInternalIdentifierOfLocationIfOnlyLocationIdIsSpecified() throws HL7Exception {
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
		assertNotNull(pv1, "PV1 parsed as null");
		PL hl7Location = pv1.getAssignedPatientLocation();
		Integer locationId = hl7service.resolveLocationId(hl7Location);
		assertEquals(Integer.valueOf(1),locationId, "Resolved and given locationId shoud be equals");
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#resolveLocationId(ca.uhn.hl7v2.model.v25.datatype.PL)
	 */
	@Test
	public void resolveLocationId_shouldReturnNullIfLocationIdAndNameAreIncorrect() throws HL7Exception {
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
		assertNotNull(pv1, "PV1 parsed as null");
		PL hl7Location = pv1.getAssignedPatientLocation();
		Integer locationId = hl7service.resolveLocationId(hl7Location);
		assertNull(locationId);
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#resolveUserId(ca.uhn.hl7v2.model.v25.datatype.XCN)
	 */
	@Test
	public void resolveUserId_shouldReturnNullForAmbiguousUsersUsingFirstAndLastNameGivenUserIDIsNull() throws HL7Exception {
		HL7Service hl7service = Context.getHL7Service();
		executeDataSet(XML_FILENAME);
		//construct a message such that id Number at ORC is null
		Message message = hl7service
								  .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r" +
												  "PID|||3^^^^||John3^Doe^||\r" +
												  "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||1000^^^L^PN||||\r" +
												  "PV1||O|99999^0^0^0&Unknown&0||||^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\r" +
												  "ORC|RE||||||||20080226102537|^User^Super\r" +
												  "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r" +
												  "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r" +
												  "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		ORC orc = oru.getPATIENT_RESULT().getORDER_OBSERVATION().getORC();
		XCN xcn = orc.getEnteredBy(0);
		//userId should be null since there exist two ambiguous users that has givename=Super and lastname=User.
		Integer userId = hl7service.resolveUserId(xcn);
		assertNull(userId);
	}

	/**
	 * @throws HL7Exception
	 * @see HL7Service#resolveUserId(ca.uhn.hl7v2.model.v25.datatype.XCN)
	 */
	@Test
	public void resolveUserId_shouldReturnUserUsingFirstAndLastNameGivenUserIDIsNull() throws HL7Exception {
		HL7Service hl7service = Context.getHL7Service();
		//construct a message such that id Number at ORC is null
		Message message = hl7service
								  .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r" +
												  "PID|||3^^^^||John3^Doe^||\r" +
												  "NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||1000^^^L^PN||||\r" +
												  "ORC|RE||||||||20080226102537|^Otterbourg^Bruno\r" +
												  "OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r" +
												  "OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r" +
												  "OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		ORC orc = oru.getPATIENT_RESULT().getORDER_OBSERVATION().getORC();
		XCN xcn = orc.getEnteredBy(0);
		Integer userId = hl7service.resolveUserId(xcn);
		assertThat(userId, is(501));
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Service#resolveUserId(ca.uhn.hl7v2.model.v25.datatype.XCN)
	 */
	@Test
	public void resolveUserId_shouldReturnUserUsingUsername() throws HL7Exception {
		HL7Service hl7service = Context.getHL7Service();
		//construct a message such that id Number at ORC is null
		Message message = hl7service
				.parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\r" +
						"PID|||3^^^^||John3^Doe^||\r" +
						"NK1|1|Hornblower^Horatio^L|2B^Sibling^99REL||||||||||||M|19410501|||||||||||||||||1000^^^L^PN||||\r" +
						"ORC|RE||||||||20080226102537|^butch\r" +
						"OBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\r" +
						"OBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\r" +
						"OBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		ORU_R01 oru = (ORU_R01) message;
		ORC orc = oru.getPATIENT_RESULT().getORDER_OBSERVATION().getORC();
		XCN xcn = orc.getEnteredBy(0);
		Integer userId = hl7service.resolveUserId(xcn);
		assertThat(userId, is(502));
	}
}

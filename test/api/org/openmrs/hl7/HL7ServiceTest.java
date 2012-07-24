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

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.impl.HL7ServiceImpl;
import org.openmrs.module.ModuleConstants;
import org.openmrs.module.ModuleUtil;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.model.Message;

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
		
		// sanity check, make sure there aren't any archive items
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
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\rPID|||3^^^^||John3^Doe^||\rPV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\rORC|RE||||||||20080226102537|1^Super User\rOBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\rOBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\rOBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		
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
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ORU^R01|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\rPID|||3^^^^||John3^Doe^||\rPV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\rORC|RE||||||||20080226102537|1^Super User\rOBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\rOBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\rOBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
		Assert.assertNotNull(message);
	}
	
	/**
	 * @see {@link HL7Service#processHL7Message(Message)}
	 */
	@Test
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
		        .parseHL7String("MSH|^~\\&|FORMENTRY|AMRS.ELD|HL7LISTENER|AMRS.ELD|20080226102656||ADR^A19|JqnfhKKtouEz8kzTk6Zo|P|2.5|1||||||||16^AMRS.ELD.FORMID\rPID|||3^^^^||John3^Doe^||\rPV1||O|1^Unknown Location||||1^Super User (1-8)|||||||||||||||||||||||||||||||||||||20080212|||||||V\rORC|RE||||||||20080226102537|1^Super User\rOBR|1|||1238^MEDICAL RECORD OBSERVATIONS^99DCT\rOBX|1|NM|5497^CD4, BY FACS^99DCT||450|||||||||20080206\rOBX|2|DT|5096^RETURN VISIT DATE^99DCT||20080229|||||||||20080212");
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
}

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
package org.openmrs.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.xstream.XStreamSerializer;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

/**
 * Test class that tests the serialization and deserialization of a patient
 */
public class PatientSerializationTest extends BaseContextSensitiveTest {
	
	/**
	 * create a patient and make sure it can be serialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	@SkipBaseSetup
	public void shouldPatientSerialization() throws Exception {
		//instantiate object
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/serialization/include/PatientSerializationTest.xml");
		authenticate();
		
		Patient patient = Context.getPatientService().getPatient(999);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		//serialize and compare with a give string
		String xmlOutput = Context.getSerializationService().serialize(patient, XStreamSerializer.class);
		XMLAssert.assertXpathEvaluatesTo("86526ed6-3c11-11de-a0ba-001e378eb67e", "/patient/@uuid", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("true", "/patient/@voided", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("1", "/patient/creator/personId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo(sdf.format(patient.getDateCreated()), "/patient/dateCreated", xmlOutput);
		XMLAssert.assertXpathExists("/patient/changedBy/@reference", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo(sdf.format(patient.getDateChanged()), "/patient/dateChanged", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("For test purposes", "/patient/voidReason", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("999", "/patient/personId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("M", "/patient/gender", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("false", "/patient/dead", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("true", "/patient/isPatient", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("999", "/patient/patientId", xmlOutput);
		XMLAssert.assertXpathExists("/patient/identifiers/patientIdentifier[patientIdentifierId=7]", xmlOutput);
	}
	
	/**
	 * construct a deserialized xml string and make sure it can be deserialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldPatientDeserialization() throws Exception {
		//construct given string to be deserialized
		StringBuilder xmlBuilder = new StringBuilder();
		xmlBuilder.append("<patient id=\"1\" uuid=\"86526ed6-3c11-11de-a0ba-001e378eb67e\" voided=\"true\">\n");
		xmlBuilder.append("  <creator id=\"2\" uuid=\"6adb7c42-cfd2-4301-b53b-ff17c5654ff7\" voided=\"false\">\n");
		xmlBuilder.append("    <creator reference=\"2\"/>\n");
		xmlBuilder.append("    <dateCreated class=\"sql-timestamp\" id=\"3\">2005-01-01 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("    <changedBy reference=\"2\"/>\n");
		xmlBuilder.append("    <dateChanged class=\"sql-timestamp\" id=\"4\">2007-09-20 21:54:12 CST</dateChanged>\n");
		xmlBuilder.append("    <voidReason></voidReason>\n");
		xmlBuilder.append("    <personId>1</personId>\n");
		xmlBuilder.append("    <addresses class=\"tree-set\" id=\"5\">\n");
		xmlBuilder.append("      <no-comparator/>\n");
		xmlBuilder.append("    </addresses>\n");
		xmlBuilder.append("    <names class=\"tree-set\" id=\"6\">\n");
		xmlBuilder.append("      <no-comparator/>\n");
		xmlBuilder.append("    </names>\n");
		xmlBuilder.append("    <attributes class=\"tree-set\" id=\"7\">\n");
		xmlBuilder.append("      <no-comparator/>\n");
		xmlBuilder.append("    </attributes>\n");
		xmlBuilder.append("    <gender></gender>\n");
		xmlBuilder.append("    <birthdate class=\"sql-timestamp\" id=\"8\">1975-06-30 00:00:00 CST</birthdate>\n");
		xmlBuilder.append("    <birthdateEstimated>false</birthdateEstimated>\n");
		xmlBuilder.append("    <dead>false</dead>\n");
		xmlBuilder.append("    <personCreator reference=\"2\"/>\n");
		xmlBuilder
		        .append("    <personDateCreated class=\"sql-timestamp\" id=\"9\">2005-01-01 00:00:00 CST</personDateCreated>\n");
		xmlBuilder.append("    <personChangedBy reference=\"2\"/>\n");
		xmlBuilder
		        .append("    <personDateChanged class=\"sql-timestamp\" id=\"10\">2007-09-20 21:54:12 CST</personDateChanged>\n");
		xmlBuilder.append("    <personVoided>false</personVoided>\n");
		xmlBuilder.append("    <personVoidReason></personVoidReason>\n");
		xmlBuilder.append("    <isPatient>false</isPatient>\n");
		xmlBuilder.append("    <isUser>true</isUser>\n");
		xmlBuilder.append("    <userId>1</userId>\n");
		xmlBuilder.append("    <systemId>1-8</systemId>\n");
		xmlBuilder.append("    <username>admin</username>\n");
		xmlBuilder.append("    <secretQuestion></secretQuestion>\n");
		xmlBuilder.append("    <roles id=\"11\">\n");
		xmlBuilder.append("      <role id=\"12\" uuid=\"0e43640b-67d1-4458-b47f-b64fd8ce4b0d\" retired=\"false\">\n");
		xmlBuilder
		        .append("        <description>Developers of the OpenMRS .. have additional access to change fundamental structure of the database model.</description>\n");
		xmlBuilder.append("        <role>System Developer</role>\n");
		xmlBuilder.append("        <privileges id=\"13\"/>\n");
		xmlBuilder.append("        <inheritedRoles id=\"14\"/>\n");
		xmlBuilder.append("      </role>\n");
		xmlBuilder.append("    </roles>\n");
		xmlBuilder.append("    <userProperties id=\"15\"/>\n");
		xmlBuilder.append("    <parsedProficientLocalesProperty></parsedProficientLocalesProperty>\n");
		xmlBuilder.append("  </creator>\n");
		xmlBuilder.append("  <dateCreated class=\"sql-timestamp\" id=\"16\">2006-01-18 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("  <changedBy reference=\"2\"/>\n");
		xmlBuilder.append("  <dateChanged class=\"sql-timestamp\" id=\"17\">2008-08-18 12:24:34 CST</dateChanged>\n");
		xmlBuilder.append("  <voidReason>For test purposes</voidReason>\n");
		xmlBuilder.append("  <personId>999</personId>\n");
		xmlBuilder.append("  <addresses class=\"tree-set\" id=\"18\">\n");
		xmlBuilder.append("    <no-comparator/>\n");
		xmlBuilder.append("  </addresses>\n");
		xmlBuilder.append("  <names class=\"tree-set\" id=\"19\">\n");
		xmlBuilder.append("    <no-comparator/>\n");
		xmlBuilder.append("  </names>\n");
		xmlBuilder.append("  <attributes class=\"tree-set\" id=\"20\">\n");
		xmlBuilder.append("    <no-comparator/>\n");
		xmlBuilder.append("  </attributes>\n");
		xmlBuilder.append("  <gender>M</gender>\n");
		xmlBuilder.append("  <dead>false</dead>\n");
		xmlBuilder.append("  <personCreator reference=\"2\"/>\n");
		xmlBuilder
		        .append("  <personDateCreated class=\"sql-timestamp\" id=\"21\">2006-01-18 00:00:00 CST</personDateCreated>\n");
		xmlBuilder.append("  <personChangedBy reference=\"2\"/>\n");
		xmlBuilder
		        .append("  <personDateChanged class=\"sql-timestamp\" id=\"22\">2008-08-18 12:24:34 CST</personDateChanged>\n");
		xmlBuilder.append("  <personVoided>true</personVoided>\n");
		xmlBuilder.append("  <personVoidReason>For test purposes</personVoidReason>\n");
		xmlBuilder.append("  <isPatient>true</isPatient>\n");
		xmlBuilder.append("  <isUser>false</isUser>\n");
		xmlBuilder.append("  <patientId>999</patientId>\n");
		xmlBuilder.append("  <identifiers class=\"tree-set\" id=\"23\">\n");
		xmlBuilder.append("    <no-comparator/>\n");
		xmlBuilder.append("    <patientIdentifier id=\"24\" voided=\"false\">\n");
		xmlBuilder.append("      <creator reference=\"2\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"25\">2006-01-18 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("      <patientIdentifierId>7</patientIdentifierId>\n");
		xmlBuilder.append("      <patient reference=\"1\"/>\n");
		xmlBuilder.append("      <identifier>XYZ</identifier>\n");
		xmlBuilder
		        .append("      <identifierType id=\"26\" uuid=\"2f470aa8-1d73-43b7-81b5-01f0c0dfa53c\" retired=\"false\">\n");
		xmlBuilder.append("        <name>Old Identification Number</name>\n");
		xmlBuilder
		        .append("        <description>Number given out prior to the OpenMRS system (No check digit)</description>\n");
		xmlBuilder.append("        <creator reference=\"2\"/>\n");
		xmlBuilder.append("        <dateCreated class=\"sql-timestamp\" id=\"27\">2005-09-22 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("        <patientIdentifierTypeId>2</patientIdentifierTypeId>\n");
		xmlBuilder.append("        <format></format>\n");
		xmlBuilder.append("        <required>false</required>\n");
		xmlBuilder.append("        <checkDigit>false</checkDigit>\n");
		xmlBuilder.append("      </identifierType>\n");
		xmlBuilder.append("      <location id=\"28\" uuid=\"dc5c1fcc-0459-4201-bf70-0b90535ba362\" retired=\"false\">\n");
		xmlBuilder.append("        <name>Unknown Location</name>\n");
		xmlBuilder
		        .append("        <description>The default location used when limited information is known</description>\n");
		xmlBuilder.append("        <creator reference=\"2\"/>\n");
		xmlBuilder.append("        <dateCreated class=\"sql-timestamp\" id=\"29\">2005-09-22 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("        <locationId>1</locationId>\n");
		xmlBuilder.append("        <address1></address1>\n");
		xmlBuilder.append("        <address2></address2>\n");
		xmlBuilder.append("        <cityVillage></cityVillage>\n");
		xmlBuilder.append("        <stateProvince></stateProvince>\n");
		xmlBuilder.append("        <country></country>\n");
		xmlBuilder.append("        <postalCode></postalCode>\n");
		xmlBuilder.append("        <childLocations id=\"30\"/>\n");
		xmlBuilder.append("        <tags id=\"31\"/>\n");
		xmlBuilder.append("      </location>\n");
		xmlBuilder.append("      <preferred>true</preferred>\n");
		xmlBuilder.append("    </patientIdentifier>\n");
		xmlBuilder.append("  </identifiers>\n");
		xmlBuilder.append("</patient>\n");
		
		//deserialize and make sure everything has been put into object
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		Patient patient = Context.getSerializationService().deserialize(xmlBuilder.toString(), Patient.class,
		    XStreamSerializer.class);
		
		assertEquals("86526ed6-3c11-11de-a0ba-001e378eb67e", patient.getUuid());
		assertTrue("The voided shouldn't be " + patient.getVoided(), patient.getVoided());
		assertEquals(1, patient.getCreator().getPersonId().intValue());
		assertEquals(sdf.parse("2006-01-18 00:00:00 CST"), patient.getDateCreated());
		assertEquals(1, patient.getChangedBy().getPersonId().intValue());
		assertEquals(sdf.parse("2008-08-18 12:24:34 CST"), patient.getDateChanged());
		assertEquals("For test purposes", patient.getVoidReason());
		assertEquals(999, patient.getPersonId().intValue());
		assertEquals("M", patient.getGender());
		assertFalse("The dead shouldn't be " + patient.getDead(), patient.getDead());
		assertTrue("The isPatient shouldn't be " + patient.isPatient(), patient.isPatient());
		assertEquals(999, patient.getPatientId().intValue());
		assertEquals(1, patient.getIdentifiers().size());
	}
}

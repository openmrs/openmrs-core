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
package org.openmrs.serialization.xstream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.xstream.XStreamSerializer;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

/**
 * Test class that tests the serialization and deserialization of a PersonName
 */
public class PersonNameSerializationTest extends BaseContextSensitiveTest {
	
	/**
	 * create a person name and make sure it can be serialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	@SkipBaseSetup
	public void shouldSerializePersonName() throws Exception {
		//instantiate necessary objects
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/serialization/include/PersonNameSerializationTest.xml");
		authenticate();
		PersonName pn = Context.getPersonService().getPersonNameByUuid("399e3a7b-6482-487d-94ce-c07bb3ca3cc7");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		//serialize and compare with a give string
		String xmlOutput = Context.getSerializationService().serialize(pn, XStreamSerializer.class);
		XMLAssert.assertXpathEvaluatesTo("2", "//personName/personNameId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("true", "/personName/preferred", xmlOutput);
		XMLAssert.assertXpathExists("/personName/creator", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("2", "/personName/person/personId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("Mr.", "/personName/prefix", xmlOutput);
		
		XMLAssert.assertXpathEvaluatesTo("Horatio", "/personName/givenName", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("Test", "/personName/middleName", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("Hornblower", "/personName/familyName", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("Esq.", "/personName/familyNameSuffix", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo(sdf.format(pn.getDateCreated()), "/personName/dateCreated", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("false", "/personName/voided", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("399e3a7b-6482-487d-94ce-c07bb3ca3cc7", "/personName/@uuid", xmlOutput);
	}
	
	/**
	 * Construct a serialized xml string and make sure it can be deserialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldDeserializePersonName() throws Exception {
		//construct the deserialized xml string
		StringBuilder xmlBuilder = new StringBuilder();
		xmlBuilder.append("<personName id=\"1\" uuid=\"399e3a7b-6482-487d-94ce-c07bb3ca3cc7\" retired=\"false\">\n");
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
		xmlBuilder.append("    <personDateCreated class=\"sql-timestamp\" id=\"9\">2005-01-01 00:00:00 CST</personDateCreated>\n");
		xmlBuilder.append("    <personChangedBy reference=\"2\"/>\n");
		xmlBuilder.append("    <personDateChanged class=\"sql-timestamp\" id=\"10\">2007-09-20 21:54:12 CST</personDateChanged>\n");
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
		xmlBuilder.append("        <description>Developers of the OpenMRS .. have additional access to change fundamental structure of the database model.</description>\n");
		xmlBuilder.append("        <role>System Developer</role>\n");
		xmlBuilder.append("        <privileges id=\"13\"/>\n");
		xmlBuilder.append("        <inheritedRoles id=\"14\"/>\n");
		xmlBuilder.append("      </role>\n");
		xmlBuilder.append("    </roles>\n");
		xmlBuilder.append("    <userProperties id=\"15\"/>\n");
		xmlBuilder.append("    <parsedProficientLocalesProperty></parsedProficientLocalesProperty>\n");
		xmlBuilder.append("  </creator>\n");
		xmlBuilder.append("  <dateCreated class=\"sql-timestamp\" id=\"16\">2005-09-22 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("  <personNameId>2</personNameId>\n");
		xmlBuilder.append("  <person id=\"17\" resolves-to=\"patient\" uuid=\"da7f524f-27ce-4bb2-86d6-6d1d05312bd5\" voided=\"false\">\n");
		xmlBuilder.append("    <creator reference=\"2\"/>\n");
		xmlBuilder.append("    <dateCreated class=\"sql-timestamp\" id=\"18\">2005-09-22 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("    <changedBy reference=\"2\"/>\n");
		xmlBuilder.append("    <dateChanged class=\"sql-timestamp\" id=\"19\">2008-08-18 12:29:59 CST</dateChanged>\n");
		xmlBuilder.append("    <voidReason></voidReason>\n");
		xmlBuilder.append("    <personId>2</personId>\n");
		xmlBuilder.append("    <addresses class=\"tree-set\" id=\"20\">\n");
		xmlBuilder.append("      <no-comparator/>\n");
		xmlBuilder.append("    </addresses>\n");
		xmlBuilder.append("    <names class=\"tree-set\" id=\"21\">\n");
		xmlBuilder.append("      <no-comparator/>\n");
		xmlBuilder.append("      <personName reference=\"1\"/>\n");
		xmlBuilder.append("    </names>\n");
		xmlBuilder.append("    <attributes class=\"tree-set\" id=\"22\">\n");
		xmlBuilder.append("      <no-comparator/>\n");
		xmlBuilder.append("    </attributes>\n");
		xmlBuilder.append("    <gender>M</gender>\n");
		xmlBuilder.append("    <birthdate class=\"sql-timestamp\" id=\"23\">1975-04-08 00:00:00 CST</birthdate>\n");
		xmlBuilder.append("    <dead>false</dead>\n");
		xmlBuilder.append("    <personCreator reference=\"2\"/>\n");
		xmlBuilder.append("    <personDateCreated class=\"sql-timestamp\" id=\"24\">2005-09-22 00:00:00 CST</personDateCreated>\n");
		xmlBuilder.append("    <personVoided>false</personVoided>\n");
		xmlBuilder.append("    <isPatient>true</isPatient>\n");
		xmlBuilder.append("    <isUser>false</isUser>\n");
		xmlBuilder.append("    <patientId>2</patientId>\n");
		xmlBuilder.append("    <identifiers class=\"tree-set\" id=\"25\">\n");
		xmlBuilder.append("      <no-comparator/>\n");
		xmlBuilder.append("    </identifiers>\n");
		xmlBuilder.append("  </person>\n");
		xmlBuilder.append("  <preferred>true</preferred>\n");
		xmlBuilder.append("  <prefix>Mr.</prefix>\n");
		xmlBuilder.append("  <givenName>Horatio</givenName>\n");
		xmlBuilder.append("  <middleName>Test</middleName>\n");
		xmlBuilder.append("  <familyName>Hornblower</familyName>\n");
		xmlBuilder.append("  <familyNameSuffix>Esq.</familyNameSuffix>\n");
		xmlBuilder.append("  <voided>false</voided>\n");
		xmlBuilder.append("  <voidReason></voidReason>\n");
		xmlBuilder.append("</personName>\n");
		
		//deserialize and make sure everything has been put into object
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		PersonName pn = Context.getSerializationService().deserialize(xmlBuilder.toString(), PersonName.class,
		    XStreamSerializer.class);
		
		assertEquals(2, pn.getPersonNameId().intValue());
		assertEquals("399e3a7b-6482-487d-94ce-c07bb3ca3cc7", pn.getUuid());
		assertEquals(1, pn.getCreator().getPersonId().intValue());
		assertEquals(2, pn.getPerson().getPersonId().intValue());
		assertEquals(sdf.parse("2005-09-22 00:00:00 CST"), pn.getDateCreated());
		assertFalse("The voided shouldn't be " + pn.getVoided(), pn.getVoided());
		assertTrue("The preferred shouldn't be " + pn.getPreferred(), pn.getPreferred());
		assertEquals("Mr.", pn.getPrefix());
		assertEquals("Horatio", pn.getGivenName());
		assertEquals("Test", pn.getMiddleName());
		assertEquals("Hornblower", pn.getFamilyName());
		assertEquals("Esq.", pn.getFamilyNameSuffix());
	}
}

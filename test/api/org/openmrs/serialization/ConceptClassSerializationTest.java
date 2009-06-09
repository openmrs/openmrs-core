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
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.openmrs.ConceptClass;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.xstream.XStreamSerializer;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

/**
 * Test class that tests the serialization and deserialization of a conceptClass
 */
public class ConceptClassSerializationTest extends BaseContextSensitiveTest {
	
	/**
	 * create a conceptClass and make sure it can be serialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	@SkipBaseSetup
	public void shouldConceptClassSerialization() throws Exception {
		//instantiate object
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/serialization/include/ConceptClassSerializationTest.xml");
		authenticate();
		
		ConceptClass cc = Context.getConceptService().getConceptClass(4);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		//serialize and compare with a give string
		String xmlOutput = Context.getSerializationService().serialize(cc, XStreamSerializer.class);
		XMLAssert.assertXpathEvaluatesTo("4", "/conceptClass/conceptClassId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("003d0731-92b2-102c-adee-6014420f8468", "/conceptClass/@uuid", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("true", "/conceptClass/@retired", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("Diagnosis", "/conceptClass/name", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("Conclusion drawn through findings", "/conceptClass/description", xmlOutput);
		XMLAssert.assertXpathExists("/conceptClass/creator", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo(sdf.format(cc.getDateCreated()), "/conceptClass/dateCreated", xmlOutput);
		XMLAssert.assertXpathExists("/conceptClass/retiredBy", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo(sdf.format(cc.getDateRetired()), "/conceptClass/dateRetired", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("we don't want to use it", "/conceptClass/retireReason", xmlOutput);
	}
	
	/**
	 * construct a deserialized xml string and make sure it can be deserialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldConceptClassDeserialization() throws Exception {
		//construct given string to be deserialized
		StringBuilder xmlBuilder = new StringBuilder();
		xmlBuilder.append("<conceptClass id=\"1\" uuid=\"003d0731-92b2-102c-adee-6014420f8468\" retired=\"true\">\n");
		xmlBuilder.append("  <name>Diagnosis</name>\n");
		xmlBuilder.append("  <description>Conclusion drawn through findings</description>\n");
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
		xmlBuilder.append("  <dateCreated class=\"sql-timestamp\" id=\"16\">2004-02-02 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("  <dateRetired class=\"sql-timestamp\" id=\"17\">2006-02-02 00:00:00 CST</dateRetired>\n");
		xmlBuilder.append("  <retiredBy reference=\"2\"/>\n");
		xmlBuilder.append("  <retireReason>we don&apos;t want to use it</retireReason>\n");
		xmlBuilder.append("  <conceptClassId>4</conceptClassId>\n");
		xmlBuilder.append("</conceptClass>\n");
		
		//deserialize and make sure everything has been put into object
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		ConceptClass cc = Context.getSerializationService().deserialize(xmlBuilder.toString(), ConceptClass.class, XStreamSerializer.class);
		assertEquals("003d0731-92b2-102c-adee-6014420f8468", cc.getUuid());
		assertEquals(4, cc.getConceptClassId().intValue());
		assertEquals("Diagnosis", cc.getName());
		assertEquals("Conclusion drawn through findings", cc.getDescription());
		assertEquals(1, cc.getCreator().getPersonId().intValue());
		assertEquals(sdf.parse("2004-02-02 00:00:00 CST"), cc.getDateCreated());
		assertTrue("The retired shouldn't be " + cc.getRetired(), cc.getRetired());
		assertEquals(sdf.parse("2006-02-02 00:00:00 CST"), cc.getDateRetired());
		assertEquals(1, cc.getRetiredBy().getPersonId().intValue());
		assertEquals("we don't want to use it", cc.getRetireReason());
	}
}

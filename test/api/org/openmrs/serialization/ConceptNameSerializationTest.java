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

import java.text.SimpleDateFormat;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.openmrs.ConceptName;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.xstream.XStreamSerializer;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

/**
 * Test class that tests the serialization and deserialization of a conceptName
 */
public class ConceptNameSerializationTest extends BaseContextSensitiveTest {
	
	/**
	 * create a conceptName and make sure it can be serialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	@SkipBaseSetup
	public void shouldConceptNameSerialization() throws Exception {
		//instantiate object
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/serialization/include/ConceptNameSerializationTest.xml");
		authenticate();
		
		ConceptName cn = Context.getConceptService().getConceptName(2456);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		//serialize and compare with a give string
		String xmlOutput = Context.getSerializationService().serialize(cn, XStreamSerializer.class);
		XMLAssert.assertXpathEvaluatesTo("b8159118-c97b-4d5a-a63e-d4aa4be0c4d3", "/conceptName/@uuid", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("2456", "/conceptName/conceptNameId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("3", "/conceptName/concept/conceptId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("COUGH SYRUP", "/conceptName/name", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("en", "/conceptName/locale", xmlOutput);
		XMLAssert.assertXpathExists("/conceptName/creator", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo(sdf.format(cn.getDateCreated()), "/conceptName/dateCreated", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("false", "/conceptName/voided", xmlOutput);
		XMLAssert.assertXpathExists("/conceptName/tags/conceptNameTag[conceptNameTagId=4]", xmlOutput);
	}
	
	/**
	 * construct a deserialized xml string and make sure it can be deserialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldConceptNameDeserialization() throws Exception {
		//construct given string to be deserialized
		StringBuilder xmlBuilder = new StringBuilder();
		xmlBuilder.append("<conceptName id=\"1\" uuid=\"b8159118-c97b-4d5a-a63e-d4aa4be0c4d3\">\n");
		xmlBuilder.append("  <conceptNameId>2456</conceptNameId>\n");
		xmlBuilder.append("  <concept id=\"2\" uuid=\"0cbe2ed3-cd5f-4f46-9459-26127c9265ab\">\n");
		xmlBuilder.append("    <conceptId>3</conceptId>\n");
		xmlBuilder.append("    <retired>false</retired>\n");
		xmlBuilder.append("    <datatype id=\"3\" uuid=\"bf51481d-9b76-4a88-8ca4-88dd082ddf90\" retired=\"false\">\n");
		xmlBuilder.append("      <name>N/A</name>\n");
		xmlBuilder.append("      <description>Not associated with a datatype (e.g., term answers, sets)</description>\n");
		xmlBuilder.append("      <creator id=\"4\" uuid=\"6adb7c42-cfd2-4301-b53b-ff17c5654ff7\" voided=\"false\">\n");
		xmlBuilder.append("        <creator reference=\"4\"/>\n");
		xmlBuilder.append("        <dateCreated class=\"sql-timestamp\" id=\"5\">2005-01-01 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("        <changedBy reference=\"4\"/>\n");
		xmlBuilder.append("        <dateChanged class=\"sql-timestamp\" id=\"6\">2007-09-20 21:54:12 CST</dateChanged>\n");
		xmlBuilder.append("        <voidReason></voidReason>\n");
		xmlBuilder.append("        <personId>1</personId>\n");
		xmlBuilder.append("        <addresses class=\"tree-set\" id=\"7\">\n");
		xmlBuilder.append("          <no-comparator/>\n");
		xmlBuilder.append("        </addresses>\n");
		xmlBuilder.append("        <names class=\"tree-set\" id=\"8\">\n");
		xmlBuilder.append("          <no-comparator/>\n");
		xmlBuilder.append("        </names>\n");
		xmlBuilder.append("        <attributes class=\"tree-set\" id=\"9\">\n");
		xmlBuilder.append("          <no-comparator/>\n");
		xmlBuilder.append("        </attributes>\n");
		xmlBuilder.append("        <gender></gender>\n");
		xmlBuilder.append("        <birthdate class=\"sql-timestamp\" id=\"10\">1975-06-30 00:00:00 CST</birthdate>\n");
		xmlBuilder.append("        <birthdateEstimated>false</birthdateEstimated>\n");
		xmlBuilder.append("        <dead>false</dead>\n");
		xmlBuilder.append("        <personCreator reference=\"4\"/>\n");
		xmlBuilder
		        .append("        <personDateCreated class=\"sql-timestamp\" id=\"11\">2005-01-01 00:00:00 CST</personDateCreated>\n");
		xmlBuilder.append("        <personChangedBy reference=\"4\"/>\n");
		xmlBuilder
		        .append("        <personDateChanged class=\"sql-timestamp\" id=\"12\">2007-09-20 21:54:12 CST</personDateChanged>\n");
		xmlBuilder.append("        <personVoided>false</personVoided>\n");
		xmlBuilder.append("        <personVoidReason></personVoidReason>\n");
		xmlBuilder.append("        <isPatient>false</isPatient>\n");
		xmlBuilder.append("        <isUser>true</isUser>\n");
		xmlBuilder.append("        <userId>1</userId>\n");
		xmlBuilder.append("        <systemId>1-8</systemId>\n");
		xmlBuilder.append("        <username>admin</username>\n");
		xmlBuilder.append("        <secretQuestion></secretQuestion>\n");
		xmlBuilder.append("        <roles id=\"13\">\n");
		xmlBuilder.append("          <role id=\"14\" uuid=\"0e43640b-67d1-4458-b47f-b64fd8ce4b0d\" retired=\"false\">\n");
		xmlBuilder
		        .append("            <description>Developers of the OpenMRS .. have additional access to change fundamental structure of the database model.</description>\n");
		xmlBuilder.append("            <role>System Developer</role>\n");
		xmlBuilder.append("            <privileges id=\"15\"/>\n");
		xmlBuilder.append("            <inheritedRoles id=\"16\"/>\n");
		xmlBuilder.append("          </role>\n");
		xmlBuilder.append("        </roles>\n");
		xmlBuilder.append("        <userProperties id=\"17\"/>\n");
		xmlBuilder.append("        <parsedProficientLocalesProperty></parsedProficientLocalesProperty>\n");
		xmlBuilder.append("      </creator>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"18\">2004-02-02 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("      <conceptDatatypeId>4</conceptDatatypeId>\n");
		xmlBuilder.append("      <hl7Abbreviation>ZZ</hl7Abbreviation>\n");
		xmlBuilder.append("    </datatype>\n");
		xmlBuilder.append("    <conceptClass id=\"19\" uuid=\"3d065ed4-b0b9-4710-9a17-6d8c4fd259b7\" retired=\"false\">\n");
		xmlBuilder.append("      <name>Drug</name>\n");
		xmlBuilder.append("      <description>Drug</description>\n");
		xmlBuilder.append("      <creator reference=\"4\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"20\">2004-02-02 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("      <conceptClassId>3</conceptClassId>\n");
		xmlBuilder.append("    </conceptClass>\n");
		xmlBuilder.append("    <set>false</set>\n");
		xmlBuilder.append("    <creator reference=\"4\"/>\n");
		xmlBuilder.append("    <dateCreated class=\"sql-timestamp\" id=\"21\">2008-08-15 15:27:51 CST</dateCreated>\n");
		xmlBuilder.append("    <names class=\"set\" id=\"22\">\n");
		xmlBuilder.append("      <conceptName reference=\"1\"/>\n");
		xmlBuilder.append("    </names>\n");
		xmlBuilder.append("    <answers class=\"set\" id=\"23\"/>\n");
		xmlBuilder.append("    <conceptSets class=\"set\" id=\"24\"/>\n");
		xmlBuilder.append("    <descriptions class=\"set\" id=\"25\"/>\n");
		xmlBuilder.append("    <conceptMappings class=\"set\" id=\"26\"/>\n");
		xmlBuilder.append("  </concept>\n");
		xmlBuilder.append("  <name>COUGH SYRUP</name>\n");
		xmlBuilder.append("  <locale id=\"27\">en</locale>\n");
		xmlBuilder.append("  <creator reference=\"4\"/>\n");
		xmlBuilder.append("  <dateCreated class=\"sql-timestamp\" id=\"28\">2008-08-15 15:27:51 CST</dateCreated>\n");
		xmlBuilder.append("  <voided>false</voided>\n");
		xmlBuilder.append("  <tags class=\"set\" id=\"29\">\n");
		xmlBuilder.append("    <conceptNameTag id=\"30\" uuid=\"73e8f75b-9133-426e-ac3f-40a903ceb0bf\">\n");
		xmlBuilder.append("      <conceptNameTagId>4</conceptNameTagId>\n");
		xmlBuilder.append("      <tag>preferred</tag>\n");
		xmlBuilder.append("      <description>preferred name in English</description>\n");
		xmlBuilder.append("      <creator reference=\"4\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"31\">2007-05-01 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("      <voided>false</voided>\n");
		xmlBuilder.append("    </conceptNameTag>\n");
		xmlBuilder.append("  </tags>\n");
		xmlBuilder.append("</conceptName>\n");
		
		//deserialize and make sure everything has been put into object
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		ConceptName cn = Context.getSerializationService().deserialize(xmlBuilder.toString(), ConceptName.class,
		    XStreamSerializer.class);
		assertEquals("b8159118-c97b-4d5a-a63e-d4aa4be0c4d3", cn.getUuid());
		assertEquals(2456, cn.getConceptNameId().intValue());
		assertEquals(3, cn.getConcept().getConceptId().intValue());
		assertEquals("COUGH SYRUP", cn.getName());
		assertEquals("en", cn.getLocale().toString());
		assertEquals(1, cn.getCreator().getPersonId().intValue());
		assertEquals(sdf.parse("2008-08-15 15:27:51 CST"), cn.getDateCreated());
		assertFalse("The voided shouldn't be " + cn.getVoided(), cn.getVoided());
		assertEquals(1, cn.getTags().size());
	}
}

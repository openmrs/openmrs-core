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

import java.text.SimpleDateFormat;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.openmrs.ConceptSet;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.xstream.XStreamSerializer;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

/**
 * Test class that tests the serialization and deserialization of a conceptSet
 */
public class ConceptSetSerializationTest extends BaseContextSensitiveTest {
	
	/**
	 * create a conceptSet and make sure it can be serialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	@SkipBaseSetup
	public void shouldSerializeConceptSet() throws Exception {
		//instantiate object
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/serialization/include/ConceptSetSerializationTest.xml");
		authenticate();
		
		ConceptSet cs = Context.getConceptService().getConceptSetByUuid("1a111827-639f-4cb4-961f-1e025bf88d90");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		//serialize and compare with a give string
		String xmlOutput = Context.getSerializationService().serialize(cs, XStreamSerializer.class);
		XMLAssert.assertXpathEvaluatesTo("1a111827-639f-4cb4-961f-1e025bf88d90", "/conceptSet/@uuid", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("1", "/conceptSet/conceptSetId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("18", "/conceptSet/concept/conceptId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("23", "/conceptSet/conceptSet/conceptId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("0.0", "/conceptSet/sortWeight", xmlOutput);
		XMLAssert.assertXpathExists("/conceptSet/creator", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo(sdf.format(cs.getDateCreated()), "/conceptSet/dateCreated", xmlOutput);
	}
	
	/**
	 * Construct a serialized xml string and make sure it can be deserialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldDeserializeConceptSet() throws Exception {
		//construct given string to be deserialized
		StringBuilder xmlBuilder = new StringBuilder();
		xmlBuilder.append("<conceptSet id=\"1\" uuid=\"1a111827-639f-4cb4-961f-1e025bf88d90\">\n");
		xmlBuilder.append("  <conceptSetId>1</conceptSetId>\n");
		xmlBuilder.append("  <concept id=\"2\" uuid=\"0dde1358-7fcf-4341-a330-f119241a46e8\">\n");
		xmlBuilder.append("    <conceptId>18</conceptId>\n");
		xmlBuilder.append("    <retired>false</retired>\n");
		xmlBuilder.append("    <datatype id=\"3\" uuid=\"bd9813c4-e111-49cd-b7ff-fd08aacadfb7\" retired=\"false\">\n");
		xmlBuilder.append("      <name>Boolean</name>\n");
		xmlBuilder.append("      <description>Boolean value (yes/no, true/false)</description>\n");
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
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"18\">2004-08-26 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("      <conceptDatatypeId>10</conceptDatatypeId>\n");
		xmlBuilder.append("      <hl7Abbreviation>BIT</hl7Abbreviation>\n");
		xmlBuilder.append("    </datatype>\n");
		xmlBuilder.append("    <conceptClass id=\"19\" uuid=\"a82ef63c-e4e4-48d6-988a-fdd74d7541a7\" retired=\"false\">\n");
		xmlBuilder.append("      <name>Question</name>\n");
		xmlBuilder.append("      <description>Question (eg, patient history, SF36 items)</description>\n");
		xmlBuilder.append("      <creator reference=\"4\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"20\">2004-03-02 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("      <conceptClassId>7</conceptClassId>\n");
		xmlBuilder.append("    </conceptClass>\n");
		xmlBuilder.append("    <set>false</set>\n");
		xmlBuilder.append("    <version></version>\n");
		xmlBuilder.append("    <creator reference=\"4\"/>\n");
		xmlBuilder.append("    <dateCreated class=\"sql-timestamp\" id=\"21\">2008-08-18 12:31:52 CST</dateCreated>\n");
		xmlBuilder.append("    <names class=\"set\" id=\"22\"/>\n");
		xmlBuilder.append("    <answers class=\"set\" id=\"23\"/>\n");
		xmlBuilder.append("    <conceptSets class=\"set\" id=\"24\"/>\n");
		xmlBuilder.append("    <descriptions class=\"set\" id=\"25\"/>\n");
		xmlBuilder.append("    <conceptMappings class=\"set\" id=\"26\"/>\n");
		xmlBuilder.append("  </concept>\n");
		xmlBuilder.append("  <conceptSet id=\"27\" uuid=\"0f97e14e-cdc2-49ac-9255-b5126f8a5147\">\n");
		xmlBuilder.append("    <conceptId>23</conceptId>\n");
		xmlBuilder.append("    <retired>false</retired>\n");
		xmlBuilder.append("    <datatype id=\"28\" uuid=\"bf51481d-9b76-4a88-8ca4-88dd082ddf90\" retired=\"false\">\n");
		xmlBuilder.append("      <name>N/A</name>\n");
		xmlBuilder.append("      <description>Not associated with a datatype (e.g., term answers, sets)</description>\n");
		xmlBuilder.append("      <creator reference=\"4\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"29\">2004-02-02 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("      <conceptDatatypeId>4</conceptDatatypeId>\n");
		xmlBuilder.append("      <hl7Abbreviation>ZZ</hl7Abbreviation>\n");
		xmlBuilder.append("    </datatype>\n");
		xmlBuilder.append("    <conceptClass id=\"30\" uuid=\"0248f513-d023-40b6-a274-235a33f6e25f\" retired=\"false\">\n");
		xmlBuilder.append("      <name>ConvSet</name>\n");
		xmlBuilder.append("      <description>Term to describe convenience sets</description>\n");
		xmlBuilder.append("      <creator reference=\"4\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"31\">2004-03-02 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("      <conceptClassId>10</conceptClassId>\n");
		xmlBuilder.append("    </conceptClass>\n");
		xmlBuilder.append("    <set>true</set>\n");
		xmlBuilder.append("    <version></version>\n");
		xmlBuilder.append("    <creator reference=\"4\"/>\n");
		xmlBuilder.append("    <dateCreated class=\"sql-timestamp\" id=\"32\">2008-08-18 12:38:58 CST</dateCreated>\n");
		xmlBuilder.append("    <names class=\"set\" id=\"33\"/>\n");
		xmlBuilder.append("    <answers class=\"set\" id=\"34\"/>\n");
		xmlBuilder.append("    <conceptSets class=\"set\" id=\"35\">\n");
		xmlBuilder.append("      <conceptSet reference=\"1\"/>\n");
		xmlBuilder.append("    </conceptSets>\n");
		xmlBuilder.append("    <descriptions class=\"set\" id=\"36\"/>\n");
		xmlBuilder.append("    <conceptMappings class=\"set\" id=\"37\"/>\n");
		xmlBuilder.append("  </conceptSet>\n");
		xmlBuilder.append("  <sortWeight>0.0</sortWeight>\n");
		xmlBuilder.append("  <creator reference=\"4\"/>\n");
		xmlBuilder.append("  <dateCreated class=\"sql-timestamp\" id=\"38\">2008-08-18 12:38:58 CST</dateCreated>\n");
		xmlBuilder.append("</conceptSet>\n");
		
		//deserialize and make sure everything has been put into object
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

		ConceptSet cs = Context.getSerializationService().deserialize(xmlBuilder.toString(), ConceptSet.class, XStreamSerializer.class);
		assertEquals("1a111827-639f-4cb4-961f-1e025bf88d90", cs.getUuid());
		assertEquals(1, cs.getConceptSetId().intValue());
		assertEquals(18, cs.getConcept().getConceptId().intValue());
		assertEquals(23, cs.getConceptSet().getConceptId().intValue());
		assertEquals("0.0", cs.getSortWeight().toString());
		assertEquals(1, cs.getCreator().getPersonId().intValue());
		assertEquals(sdf.parse("2008-08-18 12:38:58 CST"), cs.getDateCreated());
	}
}

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

import java.text.SimpleDateFormat;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.openmrs.ConceptAnswer;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.xstream.XStreamSerializer;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

/**
 * Test class that tests the serialization and deserialization of a conceptAnswer
 */
public class ConceptAnswerSerializationTest extends BaseContextSensitiveTest {
	
	/**
	 * create a conceptAnswer and make sure it can be serialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	@SkipBaseSetup
	public void shouldConceptAnswerSerialization() throws Exception {
		//instantiate object
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/serialization/include/ConceptAnswerSerializationTest.xml");
		authenticate();
		
		ConceptAnswer ca = Context.getConceptService().getConceptAnswer(762);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		//serialize and compare with a give string
		String xmlOutput = Context.getSerializationService().serialize(ca, XStreamSerializer.class);
		XMLAssert.assertXpathEvaluatesTo("0038bcf6-92b2-102c-adee-6014420f8468", "/conceptAnswer/@uuid", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("762", "/conceptAnswer/conceptAnswerId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("1088", "/conceptAnswer/concept/conceptId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("796", "/conceptAnswer/answerConcept/conceptId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("10", "/conceptAnswer/answerDrug/drugId", xmlOutput);
		XMLAssert.assertXpathExists("/conceptAnswer/creator", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo(sdf.format(ca.getDateCreated()), "/conceptAnswer/dateCreated", xmlOutput);
	}
	
	/**
	 * construct a deserialized xml string and make sure it can be deserialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldConceptAnswerDeserialization() throws Exception {
		//construct given string to be deserialized
		StringBuilder xmlBuilder = new StringBuilder();
		xmlBuilder.append("<conceptAnswer id=\"1\" uuid=\"0038bcf6-92b2-102c-adee-6014420f8468\">\n");
		xmlBuilder.append("  <conceptAnswerId>762</conceptAnswerId>\n");
		xmlBuilder.append("  <concept id=\"2\" uuid=\"001ba2b2-92b2-102c-adee-6014420f8468\">\n");
		xmlBuilder.append("    <conceptId>1088</conceptId>\n");
		xmlBuilder.append("    <retired>false</retired>\n");
		xmlBuilder.append("    <datatype id=\"3\" uuid=\"003d8f84-92b2-102c-adee-6014420f8468\" retired=\"false\">\n");
		xmlBuilder.append("      <name>Coded</name>\n");
		xmlBuilder
		        .append("      <description>Value determined by term dictionary lookup (i.e., term identifier)</description>\n");
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
		xmlBuilder.append("      <conceptDatatypeId>2</conceptDatatypeId>\n");
		xmlBuilder.append("      <hl7Abbreviation>CWE</hl7Abbreviation>\n");
		xmlBuilder.append("    </datatype>\n");
		xmlBuilder.append("    <conceptClass id=\"19\" uuid=\"003d0a99-92b2-102c-adee-6014420f8468\" retired=\"false\">\n");
		xmlBuilder.append("      <name>Question</name>\n");
		xmlBuilder.append("      <description>Question (eg, patient history, SF36 items)</description>\n");
		xmlBuilder.append("      <creator reference=\"4\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"20\">2004-03-02 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("      <conceptClassId>7</conceptClassId>\n");
		xmlBuilder.append("    </conceptClass>\n");
		xmlBuilder.append("    <set>false</set>\n");
		xmlBuilder.append("    <creator reference=\"4\"/>\n");
		xmlBuilder.append("    <dateCreated class=\"sql-timestamp\" id=\"21\">2005-01-12 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("    <changedBy reference=\"4\"/>\n");
		xmlBuilder.append("    <dateChanged class=\"sql-timestamp\" id=\"22\">2006-02-14 14:14:22 CST</dateChanged>\n");
		xmlBuilder.append("    <names class=\"set\" id=\"23\"/>\n");
		xmlBuilder.append("    <answers class=\"set\" id=\"24\">\n");
		xmlBuilder.append("      <conceptAnswer reference=\"1\"/>\n");
		xmlBuilder.append("    </answers>\n");
		xmlBuilder.append("    <conceptSets class=\"set\" id=\"25\"/>\n");
		xmlBuilder.append("    <descriptions class=\"set\" id=\"26\"/>\n");
		xmlBuilder.append("    <conceptMappings class=\"set\" id=\"27\"/>\n");
		xmlBuilder.append("  </concept>\n");
		xmlBuilder.append("  <answerConcept id=\"28\" uuid=\"00174a3f-92b2-102c-adee-6014420f8468\">\n");
		xmlBuilder.append("    <conceptId>796</conceptId>\n");
		xmlBuilder.append("    <retired>false</retired>\n");
		xmlBuilder.append("    <datatype id=\"29\" uuid=\"003d9648-92b2-102c-adee-6014420f8468\" retired=\"false\">\n");
		xmlBuilder.append("      <name>N/A</name>\n");
		xmlBuilder.append("      <description>Not associated with a datatype (e.g., term answers, sets)</description>\n");
		xmlBuilder.append("      <creator reference=\"4\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"30\">2004-02-02 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("      <conceptDatatypeId>4</conceptDatatypeId>\n");
		xmlBuilder.append("      <hl7Abbreviation>ZZ</hl7Abbreviation>\n");
		xmlBuilder.append("    </datatype>\n");
		xmlBuilder.append("    <conceptClass id=\"31\" uuid=\"003d006a-92b2-102c-adee-6014420f8468\" retired=\"false\">\n");
		xmlBuilder.append("      <name>Drug</name>\n");
		xmlBuilder.append("      <description>Drug</description>\n");
		xmlBuilder.append("      <creator reference=\"4\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"32\">2004-02-02 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("      <conceptClassId>3</conceptClassId>\n");
		xmlBuilder.append("    </conceptClass>\n");
		xmlBuilder.append("    <set>false</set>\n");
		xmlBuilder.append("    <creator reference=\"4\"/>\n");
		xmlBuilder.append("    <dateCreated class=\"sql-timestamp\" id=\"33\">2004-04-08 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("    <changedBy reference=\"4\"/>\n");
		xmlBuilder.append("    <dateChanged class=\"sql-timestamp\" id=\"34\">2005-02-22 12:50:02 CST</dateChanged>\n");
		xmlBuilder.append("    <names class=\"set\" id=\"35\"/>\n");
		xmlBuilder.append("    <answers class=\"set\" id=\"36\"/>\n");
		xmlBuilder.append("    <conceptSets class=\"set\" id=\"37\"/>\n");
		xmlBuilder.append("    <descriptions class=\"set\" id=\"38\"/>\n");
		xmlBuilder.append("    <conceptMappings class=\"set\" id=\"39\"/>\n");
		xmlBuilder.append("  </answerConcept>\n");
		xmlBuilder.append("  <answerDrug id=\"40\" uuid=\"00a66162-92b2-102c-adee-6014420f8468\" retired=\"false\">\n");
		xmlBuilder.append("    <name>DDI 200</name>\n");
		xmlBuilder.append("    <creator reference=\"4\"/>\n");
		xmlBuilder.append("    <dateCreated class=\"sql-timestamp\" id=\"41\">2005-02-24 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("    <drugId>10</drugId>\n");
		xmlBuilder.append("    <combination>false</combination>\n");
		xmlBuilder.append("    <units>mg</units>\n");
		xmlBuilder.append("    <concept reference=\"28\"/>\n");
		xmlBuilder.append("  </answerDrug>\n");
		xmlBuilder.append("  <creator reference=\"4\"/>\n");
		xmlBuilder.append("  <dateCreated class=\"sql-timestamp\" id=\"42\">2006-02-14 14:22:55 CST</dateCreated>\n");
		xmlBuilder.append("</conceptAnswer>\n");
		
		//deserialize and make sure everything has been put into object
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		ConceptAnswer ca = Context.getSerializationService().deserialize(xmlBuilder.toString(), ConceptAnswer.class,
		    XStreamSerializer.class);
		assertEquals("0038bcf6-92b2-102c-adee-6014420f8468", ca.getUuid());
		assertEquals(762, ca.getConceptAnswerId().intValue());
		assertEquals(1088, ca.getConcept().getConceptId().intValue());
		assertEquals(796, ca.getAnswerConcept().getConceptId().intValue());
		assertEquals(10, ca.getAnswerDrug().getDrugId().intValue());
		assertEquals(1, ca.getCreator().getPersonId().intValue());
		assertEquals(sdf.parse("2006-02-14 14:22:55 CST"), ca.getDateCreated());
	}
}

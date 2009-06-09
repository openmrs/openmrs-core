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
import org.openmrs.ConceptDerived;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.xstream.XStreamSerializer;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

/**
 * Test class that tests the serialization and deserialization of a conceptDerived
 */
public class ConceptDerivedSerializationTest extends BaseContextSensitiveTest {
	
	/**
	 * create a conceptDerived and make sure it can be serialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	@SkipBaseSetup
	public void shouldConceptDerivedSerialization() throws Exception {
		//instantiate object
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/serialization/include/ConceptDerivedSerializationTest.xml");
		authenticate();
		
		ConceptDerived cd = (ConceptDerived) Context.getConceptService().getConcept(3);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		//serialize and compare with a give string
		String xmlOutput = Context.getSerializationService().serialize(cd, XStreamSerializer.class);
		XMLAssert.assertXpathEvaluatesTo("0cbe2ed3-cd5f-4f46-9459-26127c9265ab", "/conceptDerived/@uuid", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("3", "/conceptDerived/conceptId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("false", "/conceptDerived/retired", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("4", "/conceptDerived/datatype/conceptDatatypeId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("3", "/conceptDerived/conceptClass/conceptClassId", xmlOutput);
		XMLAssert.assertXpathExists("/conceptDerived/creator", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("false", "/conceptDerived/set", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo(sdf.format(cd.getDateCreated()), "/conceptDerived/dateCreated", xmlOutput);
		XMLAssert.assertXpathExists("/conceptDerived/names/conceptName[conceptNameId=2456]", xmlOutput);
		XMLAssert.assertXpathExists("/conceptDerived/answers/conceptAnswer[conceptAnswerId=762]", xmlOutput);
		XMLAssert.assertXpathExists("/conceptDerived/conceptSets/conceptSet[conceptSetId=1]", xmlOutput);
		XMLAssert.assertXpathExists("/conceptDerived/descriptions/conceptDescription[conceptDescriptionId=9]", xmlOutput);
		XMLAssert.assertXpathExists("/conceptDerived/conceptMappings/conceptMap[conceptMapId=1]", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("The test rule for serializing conceptderived", "/conceptDerived/rule", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo(sdf.format(cd.getCompileDate()), "/conceptDerived/compileDate", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("success", "/conceptDerived/compileStatus", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("The class test for serializing conceptderived", "/conceptDerived/className",
		    xmlOutput);
	}
	
	/**
	 * construct a deserialized xml string and make sure it can be deserialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldConceptDerivedDeserialization() throws Exception {
		//construct given string to be deserialized
		StringBuilder xmlBuilder = new StringBuilder();
		xmlBuilder.append("<conceptDerived id=\"1\" uuid=\"0cbe2ed3-cd5f-4f46-9459-26127c9265ab\">\n");
		xmlBuilder.append("  <conceptId>3</conceptId>\n");
		xmlBuilder.append("  <retired>false</retired>\n");
		xmlBuilder.append("  <datatype id=\"2\" uuid=\"bf51481d-9b76-4a88-8ca4-88dd082ddf90\" retired=\"false\">\n");
		xmlBuilder.append("    <name>N/A</name>\n");
		xmlBuilder.append("    <description>Not associated with a datatype (e.g., term answers, sets)</description>\n");
		xmlBuilder.append("    <creator id=\"3\" uuid=\"6adb7c42-cfd2-4301-b53b-ff17c5654ff7\" voided=\"false\">\n");
		xmlBuilder.append("      <creator reference=\"3\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"4\">2005-01-01 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("      <changedBy reference=\"3\"/>\n");
		xmlBuilder.append("      <dateChanged class=\"sql-timestamp\" id=\"5\">2007-09-20 21:54:12 CST</dateChanged>\n");
		xmlBuilder.append("      <voidReason></voidReason>\n");
		xmlBuilder.append("      <personId>1</personId>\n");
		xmlBuilder.append("      <addresses class=\"tree-set\" id=\"6\">\n");
		xmlBuilder.append("        <no-comparator/>\n");
		xmlBuilder.append("      </addresses>\n");
		xmlBuilder.append("      <names class=\"tree-set\" id=\"7\">\n");
		xmlBuilder.append("        <no-comparator/>\n");
		xmlBuilder.append("      </names>\n");
		xmlBuilder.append("      <attributes class=\"tree-set\" id=\"8\">\n");
		xmlBuilder.append("        <no-comparator/>\n");
		xmlBuilder.append("      </attributes>\n");
		xmlBuilder.append("      <gender></gender>\n");
		xmlBuilder.append("      <birthdate class=\"sql-timestamp\" id=\"9\">1975-06-30 00:00:00 CST</birthdate>\n");
		xmlBuilder.append("      <birthdateEstimated>false</birthdateEstimated>\n");
		xmlBuilder.append("      <dead>false</dead>\n");
		xmlBuilder.append("      <personCreator reference=\"3\"/>\n");
		xmlBuilder
		        .append("      <personDateCreated class=\"sql-timestamp\" id=\"10\">2005-01-01 00:00:00 CST</personDateCreated>\n");
		xmlBuilder.append("      <personChangedBy reference=\"3\"/>\n");
		xmlBuilder
		        .append("      <personDateChanged class=\"sql-timestamp\" id=\"11\">2007-09-20 21:54:12 CST</personDateChanged>\n");
		xmlBuilder.append("      <personVoided>false</personVoided>\n");
		xmlBuilder.append("      <personVoidReason></personVoidReason>\n");
		xmlBuilder.append("      <isPatient>false</isPatient>\n");
		xmlBuilder.append("      <isUser>true</isUser>\n");
		xmlBuilder.append("      <userId>1</userId>\n");
		xmlBuilder.append("      <systemId>1-8</systemId>\n");
		xmlBuilder.append("      <username>admin</username>\n");
		xmlBuilder.append("      <secretQuestion></secretQuestion>\n");
		xmlBuilder.append("      <roles id=\"12\">\n");
		xmlBuilder.append("        <role id=\"13\" uuid=\"0e43640b-67d1-4458-b47f-b64fd8ce4b0d\" retired=\"false\">\n");
		xmlBuilder
		        .append("          <description>Developers of the OpenMRS .. have additional access to change fundamental structure of the database model.</description>\n");
		xmlBuilder.append("          <role>System Developer</role>\n");
		xmlBuilder.append("          <privileges id=\"14\"/>\n");
		xmlBuilder.append("          <inheritedRoles id=\"15\"/>\n");
		xmlBuilder.append("        </role>\n");
		xmlBuilder.append("      </roles>\n");
		xmlBuilder.append("      <userProperties id=\"16\"/>\n");
		xmlBuilder.append("      <parsedProficientLocalesProperty></parsedProficientLocalesProperty>\n");
		xmlBuilder.append("    </creator>\n");
		xmlBuilder.append("    <dateCreated class=\"sql-timestamp\" id=\"17\">2004-02-02 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("    <conceptDatatypeId>4</conceptDatatypeId>\n");
		xmlBuilder.append("    <hl7Abbreviation>ZZ</hl7Abbreviation>\n");
		xmlBuilder.append("  </datatype>\n");
		xmlBuilder.append("  <conceptClass id=\"18\" uuid=\"3d065ed4-b0b9-4710-9a17-6d8c4fd259b7\" retired=\"false\">\n");
		xmlBuilder.append("    <name>Drug</name>\n");
		xmlBuilder.append("    <description>Drug</description>\n");
		xmlBuilder.append("    <creator reference=\"3\"/>\n");
		xmlBuilder.append("    <dateCreated class=\"sql-timestamp\" id=\"19\">2004-02-02 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("    <conceptClassId>3</conceptClassId>\n");
		xmlBuilder.append("  </conceptClass>\n");
		xmlBuilder.append("  <set>false</set>\n");
		xmlBuilder.append("  <creator reference=\"3\"/>\n");
		xmlBuilder.append("  <dateCreated class=\"sql-timestamp\" id=\"20\">2008-08-15 15:27:51 CST</dateCreated>\n");
		xmlBuilder.append("  <names class=\"set\" id=\"21\">\n");
		xmlBuilder.append("    <conceptName id=\"22\" uuid=\"b8159118-c97b-4d5a-a63e-d4aa4be0c4d3\">\n");
		xmlBuilder.append("      <conceptNameId>2456</conceptNameId>\n");
		xmlBuilder.append("      <concept class=\"conceptDerived\" reference=\"1\"/>\n");
		xmlBuilder.append("      <name>COUGH SYRUP</name>\n");
		xmlBuilder.append("      <locale id=\"23\">en</locale>\n");
		xmlBuilder.append("      <creator reference=\"3\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"24\">2008-08-15 15:27:51 CST</dateCreated>\n");
		xmlBuilder.append("      <voided>false</voided>\n");
		xmlBuilder.append("      <tags class=\"set\" id=\"25\">\n");
		xmlBuilder.append("        <conceptNameTag id=\"26\" uuid=\"73e8f75b-9133-426e-ac3f-40a903ceb0bf\">\n");
		xmlBuilder.append("          <conceptNameTagId>4</conceptNameTagId>\n");
		xmlBuilder.append("          <tag>preferred</tag>\n");
		xmlBuilder.append("          <description>preferred name in English</description>\n");
		xmlBuilder.append("          <creator reference=\"3\"/>\n");
		xmlBuilder
		        .append("          <dateCreated class=\"sql-timestamp\" id=\"27\">2007-05-01 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("          <voided>false</voided>\n");
		xmlBuilder.append("        </conceptNameTag>\n");
		xmlBuilder.append("      </tags>\n");
		xmlBuilder.append("    </conceptName>\n");
		xmlBuilder.append("  </names>\n");
		xmlBuilder.append("  <answers class=\"set\" id=\"28\">\n");
		xmlBuilder.append("    <conceptAnswer id=\"29\" uuid=\"0038bcf6-92b2-102c-adee-6014420f8468\">\n");
		xmlBuilder.append("      <conceptAnswerId>762</conceptAnswerId>\n");
		xmlBuilder.append("      <concept class=\"conceptDerived\" reference=\"1\"/>\n");
		xmlBuilder.append("      <answerConcept id=\"30\" uuid=\"00174a3f-92b2-102c-adee-6014420f8468\">\n");
		xmlBuilder.append("        <conceptId>796</conceptId>\n");
		xmlBuilder.append("        <retired>false</retired>\n");
		xmlBuilder.append("        <datatype reference=\"2\"/>\n");
		xmlBuilder.append("        <conceptClass reference=\"18\"/>\n");
		xmlBuilder.append("        <set>false</set>\n");
		xmlBuilder.append("        <creator reference=\"3\"/>\n");
		xmlBuilder.append("        <dateCreated class=\"sql-timestamp\" id=\"31\">2004-04-08 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("        <changedBy reference=\"3\"/>\n");
		xmlBuilder.append("        <dateChanged class=\"sql-timestamp\" id=\"32\">2005-02-22 12:50:02 CST</dateChanged>\n");
		xmlBuilder.append("        <names class=\"set\" id=\"33\"/>\n");
		xmlBuilder.append("        <answers class=\"set\" id=\"34\"/>\n");
		xmlBuilder.append("        <conceptSets class=\"set\" id=\"35\"/>\n");
		xmlBuilder.append("        <descriptions class=\"set\" id=\"36\"/>\n");
		xmlBuilder.append("        <conceptMappings class=\"set\" id=\"37\"/>\n");
		xmlBuilder.append("      </answerConcept>\n");
		xmlBuilder.append("      <answerDrug id=\"38\" uuid=\"00a66162-92b2-102c-adee-6014420f8468\" retired=\"false\">\n");
		xmlBuilder.append("        <name>DDI 200</name>\n");
		xmlBuilder.append("        <creator reference=\"3\"/>\n");
		xmlBuilder.append("        <dateCreated class=\"sql-timestamp\" id=\"39\">2005-02-24 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("        <drugId>10</drugId>\n");
		xmlBuilder.append("        <combination>false</combination>\n");
		xmlBuilder.append("        <units>mg</units>\n");
		xmlBuilder.append("        <concept reference=\"30\"/>\n");
		xmlBuilder.append("      </answerDrug>\n");
		xmlBuilder.append("      <creator reference=\"3\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"40\">2006-02-14 14:22:55 CST</dateCreated>\n");
		xmlBuilder.append("    </conceptAnswer>\n");
		xmlBuilder.append("  </answers>\n");
		xmlBuilder.append("  <conceptSets class=\"set\" id=\"41\">\n");
		xmlBuilder.append("    <conceptSet id=\"42\" uuid=\"1a111827-639f-4cb4-961f-1e025bf88d90\">\n");
		xmlBuilder.append("      <conceptSetId>1</conceptSetId>\n");
		xmlBuilder.append("      <concept id=\"43\" uuid=\"0f97e14e-cdc2-49ac-9255-b5126f8a5147\">\n");
		xmlBuilder.append("        <conceptId>23</conceptId>\n");
		xmlBuilder.append("        <retired>false</retired>\n");
		xmlBuilder.append("        <datatype reference=\"2\"/>\n");
		xmlBuilder
		        .append("        <conceptClass id=\"44\" uuid=\"0248f513-d023-40b6-a274-235a33f6e25f\" retired=\"false\">\n");
		xmlBuilder.append("          <name>ConvSet</name>\n");
		xmlBuilder.append("          <description>Term to describe convenience sets</description>\n");
		xmlBuilder.append("          <creator reference=\"3\"/>\n");
		xmlBuilder
		        .append("          <dateCreated class=\"sql-timestamp\" id=\"45\">2004-03-02 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("          <conceptClassId>10</conceptClassId>\n");
		xmlBuilder.append("        </conceptClass>\n");
		xmlBuilder.append("        <set>true</set>\n");
		xmlBuilder.append("        <version></version>\n");
		xmlBuilder.append("        <creator reference=\"3\"/>\n");
		xmlBuilder.append("        <dateCreated class=\"sql-timestamp\" id=\"46\">2008-08-18 12:38:58 CST</dateCreated>\n");
		xmlBuilder.append("        <names class=\"set\" id=\"47\"/>\n");
		xmlBuilder.append("        <answers class=\"set\" id=\"48\"/>\n");
		xmlBuilder.append("        <conceptSets class=\"set\" id=\"49\"/>\n");
		xmlBuilder.append("        <descriptions class=\"set\" id=\"50\"/>\n");
		xmlBuilder.append("        <conceptMappings class=\"set\" id=\"51\"/>\n");
		xmlBuilder.append("      </concept>\n");
		xmlBuilder.append("      <conceptSet class=\"conceptDerived\" reference=\"1\"/>\n");
		xmlBuilder.append("      <sortWeight>0.0</sortWeight>\n");
		xmlBuilder.append("      <creator reference=\"3\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"52\">2008-08-18 12:38:58 CST</dateCreated>\n");
		xmlBuilder.append("    </conceptSet>\n");
		xmlBuilder.append("  </conceptSets>\n");
		xmlBuilder.append("  <descriptions class=\"set\" id=\"53\">\n");
		xmlBuilder.append("    <conceptDescription id=\"54\" uuid=\"79a3efa7-3a43-4b38-ac5d-9b68aee086c6\">\n");
		xmlBuilder.append("      <conceptDescriptionId>9</conceptDescriptionId>\n");
		xmlBuilder.append("      <concept class=\"conceptDerived\" reference=\"1\"/>\n");
		xmlBuilder.append("      <description>This is used for coughs</description>\n");
		xmlBuilder.append("      <locale id=\"55\">en</locale>\n");
		xmlBuilder.append("      <creator reference=\"3\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"56\">2008-08-15 15:27:51 CST</dateCreated>\n");
		xmlBuilder.append("    </conceptDescription>\n");
		xmlBuilder.append("  </descriptions>\n");
		xmlBuilder.append("  <conceptMappings class=\"set\" id=\"57\">\n");
		xmlBuilder.append("    <conceptMap id=\"58\" uuid=\"6c36f786-957d-4a14-a6ed-e66ced057066\">\n");
		xmlBuilder.append("      <conceptMapId>1</conceptMapId>\n");
		xmlBuilder.append("      <concept class=\"conceptDerived\" reference=\"1\"/>\n");
		xmlBuilder.append("      <source id=\"59\" uuid=\"14ea70c7-fe49-46ae-9957-8a678c82d1d8\">\n");
		xmlBuilder.append("        <conceptSourceId>1</conceptSourceId>\n");
		xmlBuilder.append("        <name>SNOMED</name>\n");
		xmlBuilder.append("        <description>Systematized Nomenclature of Medicine -- Clinical Terms</description>\n");
		xmlBuilder.append("        <hl7Code>test</hl7Code>\n");
		xmlBuilder.append("        <creator reference=\"3\"/>\n");
		xmlBuilder.append("        <dateCreated class=\"sql-timestamp\" id=\"60\">2006-01-20 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("        <voided>false</voided>\n");
		xmlBuilder.append("      </source>\n");
		xmlBuilder.append("      <sourceCode>test</sourceCode>\n");
		xmlBuilder.append("      <comment>test</comment>\n");
		xmlBuilder.append("      <creator reference=\"3\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"61\">2006-02-20 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("    </conceptMap>\n");
		xmlBuilder.append("  </conceptMappings>\n");
		xmlBuilder.append("  <rule>The test rule for serializing conceptderived</rule>\n");
		xmlBuilder.append("  <compileDate class=\"sql-timestamp\" id=\"62\">2006-02-20 15:00:00 CST</compileDate>\n");
		xmlBuilder.append("  <compileStatus>success</compileStatus>\n");
		xmlBuilder.append("  <className>The class test for serializing conceptderived</className>\n");
		xmlBuilder.append("</conceptDerived>\n");
		
		//deserialize and make sure everything has been put into object
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		ConceptDerived cd = Context.getSerializationService().deserialize(xmlBuilder.toString(), ConceptDerived.class,
		    XStreamSerializer.class);
		assertEquals("0cbe2ed3-cd5f-4f46-9459-26127c9265ab", cd.getUuid());
		assertEquals(3, cd.getConceptId().intValue());
		assertFalse("The retired shouldn't be " + cd.isRetired(), cd.isRetired());
		assertEquals(4, cd.getDatatype().getConceptDatatypeId().intValue());
		assertEquals(3, cd.getConceptClass().getConceptClassId().intValue());
		assertEquals(1, cd.getCreator().getPersonId().intValue());
		assertFalse("The set shouldn't be " + cd.getSet(), cd.getSet());
		assertEquals(sdf.parse("2008-08-15 15:27:51 CST"), cd.getDateCreated());
		assertEquals(1, cd.getNames().size());
		assertEquals(1, cd.getAnswers().size());
		assertEquals(1, cd.getDescriptions().size());
		assertEquals(1, cd.getConceptSets().size());
		assertEquals(1, cd.getConceptMappings().size());
		assertEquals("The test rule for serializing conceptderived", cd.getRule());
		assertEquals(sdf.parse("2006-02-20 15:00:00 CST"), cd.getCompileDate());
		assertEquals("success", cd.getCompileStatus());
		assertEquals("The class test for serializing conceptderived", cd.getClassName());
	}
}

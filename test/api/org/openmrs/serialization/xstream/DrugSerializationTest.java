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
import org.openmrs.Drug;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.xstream.XStreamSerializer;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

/**
 * Test class that tests the serialization and deserialization of a drug
 */
public class DrugSerializationTest extends BaseContextSensitiveTest {
	
	/**
	 * create a drug and make sure it can be serialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	@SkipBaseSetup
	public void shouldSerializeDrug() throws Exception {
		//instantiate object
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/serialization/include/DrugSerializationTest.xml");
		authenticate();
		
		Drug drug = Context.getConceptService().getDrug(2);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		//serialize and compare with a give string
		String xmlOutput = Context.getSerializationService().serialize(drug, XStreamSerializer.class);
		XMLAssert.assertXpathEvaluatesTo("3cfcf118-931c-46f7-8ff6-7b876f0d4202", "/drug/@uuid", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("2", "/drug/drugId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("false", "/drug/@retired", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("Triomune-30", "/drug/name", xmlOutput);
		XMLAssert.assertXpathExists("/drug/creator", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo(sdf.format(drug.getDateCreated()), "/drug/dateCreated", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("true", "/drug/combination", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("3", "/drug/dosageForm/conceptId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("1.0", "/drug/doseStrength", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("5", "/drug/route/conceptId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("tab(s)", "/drug/units", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("4", "/drug/concept/conceptId", xmlOutput);
	}
	
	/**
	 * Construct a serialized xml string and make sure it can be deserialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldDeserializeDrug() throws Exception {
		//construct given string to be deserialized
		StringBuilder xmlBuilder = new StringBuilder();
		xmlBuilder.append("<drug id=\"1\" uuid=\"3cfcf118-931c-46f7-8ff6-7b876f0d4202\" retired=\"false\">\n");
		xmlBuilder.append("  <name>Triomune-30</name>\n");
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
		xmlBuilder.append("  <dateCreated class=\"sql-timestamp\" id=\"16\">2005-02-24 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("  <drugId>2</drugId>\n");
		xmlBuilder.append("  <combination>true</combination>\n");
		xmlBuilder.append("  <dosageForm id=\"17\" uuid=\"0cbe2ed3-cd5f-4f46-9459-26127c9265ab\">\n");
		xmlBuilder.append("    <conceptId>3</conceptId>\n");
		xmlBuilder.append("    <retired>false</retired>\n");
		xmlBuilder.append("    <datatype id=\"18\" uuid=\"749b5078-8371-4849-aeab-181e3aed9415\" retired=\"false\">\n");
		xmlBuilder.append("      <name>Numeric</name>\n");
		xmlBuilder
		        .append("      <description>Numeric value, including integer or float (e.g., creatinine, weight)</description>\n");
		xmlBuilder.append("      <creator reference=\"2\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"19\">2004-02-02 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("      <conceptDatatypeId>1</conceptDatatypeId>\n");
		xmlBuilder.append("      <hl7Abbreviation>NM</hl7Abbreviation>\n");
		xmlBuilder.append("    </datatype>\n");
		xmlBuilder.append("    <conceptClass id=\"20\" uuid=\"3d065ed4-b0b9-4710-9a17-6d8c4fd259b7\" retired=\"false\">\n");
		xmlBuilder.append("      <name>Drug</name>\n");
		xmlBuilder.append("      <description>Drug</description>\n");
		xmlBuilder.append("      <creator reference=\"2\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"21\">2004-02-02 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("      <conceptClassId>3</conceptClassId>\n");
		xmlBuilder.append("    </conceptClass>\n");
		xmlBuilder.append("    <set>false</set>\n");
		xmlBuilder.append("    <version></version>\n");
		xmlBuilder.append("    <creator reference=\"2\"/>\n");
		xmlBuilder.append("    <dateCreated class=\"sql-timestamp\" id=\"22\">2008-08-15 15:27:51 CST</dateCreated>\n");
		xmlBuilder.append("    <names class=\"set\" id=\"23\"/>\n");
		xmlBuilder.append("    <answers class=\"set\" id=\"24\"/>\n");
		xmlBuilder.append("    <conceptSets class=\"set\" id=\"25\"/>\n");
		xmlBuilder.append("    <descriptions class=\"set\" id=\"26\"/>\n");
		xmlBuilder.append("    <conceptMappings class=\"set\" id=\"27\"/>\n");
		xmlBuilder.append("  </dosageForm>\n");
		xmlBuilder.append("  <doseStrength>1.0</doseStrength>\n");
		xmlBuilder.append("  <route id=\"28\" uuid=\"32d3611a-6699-4d52-823f-b4b788bac3e3\">\n");
		xmlBuilder.append("    <conceptId>5</conceptId>\n");
		xmlBuilder.append("    <retired>false</retired>\n");
		xmlBuilder.append("    <datatype reference=\"18\"/>\n");
		xmlBuilder.append("    <conceptClass reference=\"20\"/>\n");
		xmlBuilder.append("    <set>false</set>\n");
		xmlBuilder.append("    <version></version>\n");
		xmlBuilder.append("    <creator reference=\"2\"/>\n");
		xmlBuilder.append("    <dateCreated class=\"sql-timestamp\" id=\"29\">2008-08-15 15:51:57 CST</dateCreated>\n");
		xmlBuilder.append("    <names class=\"set\" id=\"30\"/>\n");
		xmlBuilder.append("    <answers class=\"set\" id=\"31\"/>\n");
		xmlBuilder.append("    <conceptSets class=\"set\" id=\"32\"/>\n");
		xmlBuilder.append("    <descriptions class=\"set\" id=\"33\"/>\n");
		xmlBuilder.append("    <conceptMappings class=\"set\" id=\"34\"/>\n");
		xmlBuilder.append("  </route>\n");
		xmlBuilder.append("  <units>tab(s)</units>\n");
		xmlBuilder.append("  <concept id=\"35\" uuid=\"89ca642a-dab6-4f20-b712-e12ca4fc6d36\">\n");
		xmlBuilder.append("    <conceptId>4</conceptId>\n");
		xmlBuilder.append("    <retired>false</retired>\n");
		xmlBuilder.append("    <datatype id=\"36\" uuid=\"ef301729-eec1-4de9-8bb9-3532e3abb9bc\" retired=\"false\">\n");
		xmlBuilder.append("      <name>Coded</name>\n");
		xmlBuilder
		        .append("      <description>Value determined by term dictionary lookup (i.e., term identifier)</description>\n");
		xmlBuilder.append("      <creator reference=\"2\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"37\">2004-02-02 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("      <conceptDatatypeId>2</conceptDatatypeId>\n");
		xmlBuilder.append("      <hl7Abbreviation>CWE</hl7Abbreviation>\n");
		xmlBuilder.append("    </datatype>\n");
		xmlBuilder.append("    <conceptClass reference=\"20\"/>\n");
		xmlBuilder.append("    <set>false</set>\n");
		xmlBuilder.append("    <version></version>\n");
		xmlBuilder.append("    <creator reference=\"2\"/>\n");
		xmlBuilder.append("    <dateCreated class=\"sql-timestamp\" id=\"38\">2008-08-15 15:51:39 CST</dateCreated>\n");
		xmlBuilder.append("    <changedBy reference=\"2\"/>\n");
		xmlBuilder.append("    <dateChanged class=\"sql-timestamp\" id=\"39\">2008-08-15 15:52:38 CST</dateChanged>\n");
		xmlBuilder.append("    <names class=\"set\" id=\"40\"/>\n");
		xmlBuilder.append("    <answers class=\"set\" id=\"41\"/>\n");
		xmlBuilder.append("    <conceptSets class=\"set\" id=\"42\"/>\n");
		xmlBuilder.append("    <descriptions class=\"set\" id=\"43\"/>\n");
		xmlBuilder.append("    <conceptMappings class=\"set\" id=\"44\"/>\n");
		xmlBuilder.append("  </concept>\n");
		xmlBuilder.append("</drug>\n");
		
		//deserialize and make sure everything has been put into object
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		Drug drug = Context.getSerializationService().deserialize(xmlBuilder.toString(), Drug.class, XStreamSerializer.class);
		assertEquals("3cfcf118-931c-46f7-8ff6-7b876f0d4202", drug.getUuid());
		assertEquals(2, drug.getDrugId().intValue());
		assertFalse("The retired shouldn't be " + drug.getRetired(), drug.getRetired());
		assertEquals("Triomune-30", drug.getName());
		assertEquals(1, drug.getCreator().getPersonId().intValue());
		assertEquals(sdf.parse("2005-02-24 00:00:00 CST"), drug.getDateCreated());
		assertTrue("The combination shouldn't be " + drug.getCombination(), drug.getCombination());
		assertEquals(3, drug.getDosageForm().getConceptId().intValue());
		assertEquals("1.0", drug.getDoseStrength().toString());
		assertEquals(5, drug.getRoute().getConceptId().intValue());
		assertEquals("tab(s)", drug.getUnits());
		assertEquals(4, drug.getConcept().getConceptId().intValue());
	}
}

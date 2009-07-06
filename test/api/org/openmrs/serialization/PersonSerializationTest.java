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
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.xstream.XStreamSerializer;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

/**
 * Test class that tests the serialization and deserialization of a person
 */
public class PersonSerializationTest extends BaseContextSensitiveTest {
	
	/**
	 * create a person and make sure it can be serialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	@SkipBaseSetup
	public void shouldPersonSerialization() throws Exception {
		//instantiate object
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/serialization/include/PersonSerializationTest.xml");
		authenticate();
		
		Person person = Context.getPersonService().getPerson(1000);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		//serialize and compare with a give string
		String xmlOutput = Context.getSerializationService().serialize(person, XStreamSerializer.class);
		XMLAssert.assertXpathEvaluatesTo("04079813-4c9d-4f9d-b676-4c0502a5c1c3", "/person/@uuid", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("1000", "/person/personId", xmlOutput);
		XMLAssert.assertXpathExists("/person/addresses/personAddress[personAddressId=1]", xmlOutput);
		XMLAssert.assertXpathExists("/person/names/personName[personNameId=2]", xmlOutput);
		XMLAssert.assertXpathExists("/person/attributes/personAttribute[personAttributeId=1]", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("M", "/person/gender", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo(sdf.format(person.getBirthdate()), "/person/birthdate", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("false", "/person/birthdateEstimated", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("true", "/person/dead", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo(sdf.format(person.getDeathDate()), "/person/deathDate", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("1088", "/person/causeOfDeath/conceptId", xmlOutput);
		XMLAssert.assertXpathExists("/person/personCreator", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo(sdf.format(person.getPersonDateCreated()), "/person/personDateCreated", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("true", "/person/personVoided", xmlOutput);
		XMLAssert.assertXpathExists("/person/personVoidedBy", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo(sdf.format(person.getPersonDateVoided()), "/person/personDateVoided", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("test purpose", "/person/personVoidReason", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("false", "/person/isPatient", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("false", "/person/isUser", xmlOutput);
	}
	
	/**
	 * construct a deserialized xml string and make sure it can be deserialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldPersonDeserialization() throws Exception {
		//construct given string to be deserialized
		StringBuilder xmlBuilder = new StringBuilder();
		xmlBuilder.append("<person id=\"1\" uuid=\"04079813-4c9d-4f9d-b676-4c0502a5c1c3\" voided=\"false\">\n");
		xmlBuilder.append("  <personId>1000</personId>\n");
		xmlBuilder.append("  <addresses class=\"tree-set\" id=\"2\">\n");
		xmlBuilder.append("    <no-comparator/>\n");
		xmlBuilder.append("    <personAddress id=\"3\" uuid=\"921c0e23-d941-4bac-8ce4-ab0d0f7d8123\" voided=\"false\">\n");
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
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"18\">2006-01-18 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("      <personAddressId>1</personAddressId>\n");
		xmlBuilder.append("      <person reference=\"1\"/>\n");
		xmlBuilder.append("      <preferred>false</preferred>\n");
		xmlBuilder.append("      <address1>1050 Wishard Blvd.</address1>\n");
		xmlBuilder.append("      <address2>RG5</address2>\n");
		xmlBuilder.append("      <cityVillage>Kapina</cityVillage>\n");
		xmlBuilder.append("      <stateProvince>New York</stateProvince>\n");
		xmlBuilder.append("      <country>USA</country>\n");
		xmlBuilder.append("      <postalCode>46202</postalCode>\n");
		xmlBuilder.append("    </personAddress>\n");
		xmlBuilder.append("  </addresses>\n");
		xmlBuilder.append("  <names class=\"tree-set\" id=\"19\">\n");
		xmlBuilder.append("    <no-comparator/>\n");
		xmlBuilder.append("    <personName id=\"20\" uuid=\"399e3a7b-6482-487d-94ce-c07bb3ca3cc7\" retired=\"false\">\n");
		xmlBuilder.append("      <creator reference=\"4\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"21\">2005-09-22 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("      <personNameId>2</personNameId>\n");
		xmlBuilder.append("      <person reference=\"1\"/>\n");
		xmlBuilder.append("      <preferred>true</preferred>\n");
		xmlBuilder.append("      <prefix>Mr.</prefix>\n");
		xmlBuilder.append("      <givenName>Horatio</givenName>\n");
		xmlBuilder.append("      <middleName>Test</middleName>\n");
		xmlBuilder.append("      <familyName>Hornblower</familyName>\n");
		xmlBuilder.append("      <familyNameSuffix>Esq.</familyNameSuffix>\n");
		xmlBuilder.append("      <voided>false</voided>\n");
		xmlBuilder.append("    </personName>\n");
		xmlBuilder.append("  </names>\n");
		xmlBuilder.append("  <attributes class=\"tree-set\" id=\"22\">\n");
		xmlBuilder.append("    <no-comparator/>\n");
		xmlBuilder
		        .append("    <personAttribute id=\"23\" uuid=\"0768f3da-b692-44b7-a33f-abf2c450474e\" voided=\"false\">\n");
		xmlBuilder.append("      <creator reference=\"4\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"24\">2008-08-15 15:46:47 CST</dateCreated>\n");
		xmlBuilder.append("      <personAttributeId>1</personAttributeId>\n");
		xmlBuilder.append("      <person reference=\"1\"/>\n");
		xmlBuilder
		        .append("      <attributeType id=\"25\" uuid=\"b3b6d540-a32e-44c7-91b3-292d97667518\" retired=\"true\">\n");
		xmlBuilder.append("        <name>Race</name>\n");
		xmlBuilder.append("        <description>Group of persons related by common descent or heredity</description>\n");
		xmlBuilder.append("        <creator reference=\"4\"/>\n");
		xmlBuilder.append("        <dateCreated class=\"sql-timestamp\" id=\"26\">2007-05-04 09:59:23 CST</dateCreated>\n");
		xmlBuilder.append("        <dateRetired class=\"sql-timestamp\" id=\"27\">2008-08-15 00:00:00 CST</dateRetired>\n");
		xmlBuilder.append("        <retiredBy reference=\"4\"/>\n");
		xmlBuilder.append("        <retireReason>test</retireReason>\n");
		xmlBuilder.append("        <personAttributeTypeId>1</personAttributeTypeId>\n");
		xmlBuilder.append("        <format>java.lang.String</format>\n");
		xmlBuilder.append("        <searchable>false</searchable>\n");
		xmlBuilder.append("      </attributeType>\n");
		xmlBuilder.append("      <value></value>\n");
		xmlBuilder.append("    </personAttribute>\n");
		xmlBuilder.append("  </attributes>\n");
		xmlBuilder.append("  <gender>M</gender>\n");
		xmlBuilder.append("  <birthdate class=\"sql-timestamp\" id=\"28\">1945-12-30 00:00:00 CST</birthdate>\n");
		xmlBuilder.append("  <birthdateEstimated>false</birthdateEstimated>\n");
		xmlBuilder.append("  <dead>true</dead>\n");
		xmlBuilder.append("  <deathDate class=\"sql-timestamp\" id=\"29\">2005-02-10 15:30:00 CST</deathDate>\n");
		xmlBuilder.append("  <causeOfDeath id=\"30\" uuid=\"001ba2b2-92b2-102c-adee-6014420f8468\">\n");
		xmlBuilder.append("    <conceptId>1088</conceptId>\n");
		xmlBuilder.append("    <retired>false</retired>\n");
		xmlBuilder.append("    <datatype id=\"31\" uuid=\"003d8f84-92b2-102c-adee-6014420f8468\" retired=\"false\">\n");
		xmlBuilder.append("      <name>Coded</name>\n");
		xmlBuilder
		        .append("      <description>Value determined by term dictionary lookup (i.e., term identifier)</description>\n");
		xmlBuilder.append("      <creator reference=\"4\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"32\">2004-02-02 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("      <conceptDatatypeId>2</conceptDatatypeId>\n");
		xmlBuilder.append("      <hl7Abbreviation>CWE</hl7Abbreviation>\n");
		xmlBuilder.append("    </datatype>\n");
		xmlBuilder.append("    <conceptClass id=\"33\" uuid=\"003d0a99-92b2-102c-adee-6014420f8468\" retired=\"false\">\n");
		xmlBuilder.append("      <name>Question</name>\n");
		xmlBuilder.append("      <description>Question (eg, patient history, SF36 items)</description>\n");
		xmlBuilder.append("      <creator reference=\"4\"/>\n");
		xmlBuilder.append("      <dateCreated class=\"sql-timestamp\" id=\"34\">2004-03-02 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("      <conceptClassId>7</conceptClassId>\n");
		xmlBuilder.append("    </conceptClass>\n");
		xmlBuilder.append("    <set>false</set>\n");
		xmlBuilder.append("    <creator reference=\"4\"/>\n");
		xmlBuilder.append("    <dateCreated class=\"sql-timestamp\" id=\"35\">2005-01-12 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("    <changedBy reference=\"4\"/>\n");
		xmlBuilder.append("    <dateChanged class=\"sql-timestamp\" id=\"36\">2006-02-14 14:14:22 CST</dateChanged>\n");
		xmlBuilder.append("    <names class=\"set\" id=\"37\"/>\n");
		xmlBuilder.append("    <answers class=\"set\" id=\"38\"/>\n");
		xmlBuilder.append("    <conceptSets class=\"set\" id=\"39\"/>\n");
		xmlBuilder.append("    <descriptions class=\"set\" id=\"40\"/>\n");
		xmlBuilder.append("    <conceptMappings class=\"set\" id=\"41\"/>\n");
		xmlBuilder.append("  </causeOfDeath>\n");
		xmlBuilder.append("  <personCreator reference=\"4\"/>\n");
		xmlBuilder
		        .append("  <personDateCreated class=\"sql-timestamp\" id=\"42\">2006-01-18 00:00:00 CST</personDateCreated>\n");
		xmlBuilder.append("  <personVoided>true</personVoided>\n");
		xmlBuilder.append("  <personVoidedBy reference=\"4\"/>\n");
		xmlBuilder
		        .append("  <personDateVoided class=\"sql-timestamp\" id=\"43\">2006-09-18 00:00:00 CST</personDateVoided>\n");
		xmlBuilder.append("  <personVoidReason>test purpose</personVoidReason>\n");
		xmlBuilder.append("  <isPatient>false</isPatient>\n");
		xmlBuilder.append("  <isUser>false</isUser>\n");
		xmlBuilder.append("</person>\n");
		
		//deserialize and make sure everything has been put into object
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		Person person = Context.getSerializationService().deserialize(xmlBuilder.toString(), Person.class, XStreamSerializer.class);
		assertEquals("04079813-4c9d-4f9d-b676-4c0502a5c1c3", person.getUuid());
		assertEquals(1000, person.getPersonId().intValue());
		assertEquals(1, person.getAddresses().size());
		assertEquals(1, person.getNames().size());
		assertEquals(1, person.getAttributes().size());
		assertEquals("M", person.getGender());
		assertEquals(sdf.parse("1945-12-30 00:00:00 CST"), person.getBirthdate());
		assertFalse("The birthdateEstimated shouldn't be " + person.getBirthdateEstimated(), person.getBirthdateEstimated());
		assertTrue("The dead shouldn't be " + person.getDead(), person.getDead());
		assertEquals(sdf.parse("2005-02-10 15:30:00 CST"), person.getDeathDate());
		assertEquals(1088, person.getCauseOfDeath().getConceptId().intValue());
		assertEquals(1, person.getPersonCreator().getPersonId().intValue());
		assertEquals(sdf.parse("2006-01-18 00:00:00 CST"), person.getPersonDateCreated());
		assertTrue("The personVoided shouldn't be " + person.getPersonVoided(), person.getPersonVoided());
		assertEquals(1, person.getPersonVoidedBy().getPersonId().intValue());
		assertEquals(sdf.parse("2006-09-18 00:00:00 CST"), person.getPersonDateVoided());
		assertEquals("test purpose", person.getPersonVoidReason());
		assertFalse("The isPatient shouldn't be " + person.isPatient(), person.isPatient());
		assertFalse("The isUser shouldn't be " + person.isUser(), person.isUser());
	}
}

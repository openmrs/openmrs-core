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
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.xstream.XStreamSerializer;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

/**
 * Test class that tests the serialization and deserialization of a PersonAttributeType
 */
public class PersonAttributeTypeSerializationTest extends BaseContextSensitiveTest {
	
	/**
	 * create a person attribute type and make sure it can be serialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	@SkipBaseSetup
	public void shouldPersonAttributeTypeSerialization() throws Exception {
		//instantiate object
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/serialization/include/PersonAttributeTypeSerializationTest.xml");
		authenticate();
		
		PersonAttributeType personAttributeType = Context.getPersonService().getPersonAttributeType(1);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		//serialize and compare with a give string
		String xmlOutput = Context.getSerializationService().serialize(personAttributeType, XStreamSerializer.class);
		XMLAssert.assertXpathEvaluatesTo("1", "/personAttributeType/personAttributeTypeId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("true", "/personAttributeType/@retired", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("1053", "/personAttributeType/foreignKey", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("false", "/personAttributeType/searchable", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("b3b6d540-a32e-44c7-91b3-292d97667518", "/personAttributeType/@uuid", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("Race", "/personAttributeType/name", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("Group of persons related by common descent or heredity",
		    "/personAttributeType/description", xmlOutput);
		XMLAssert.assertXpathExists("/personAttributeType/creator", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo(sdf.format(personAttributeType.getDateCreated()),
		    "/personAttributeType/dateCreated", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo(sdf.format(personAttributeType.getDateRetired()),
		    "/personAttributeType/dateRetired", xmlOutput);
		XMLAssert.assertXpathExists("/personAttributeType/retiredBy", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("test", "/personAttributeType/retireReason", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("java.lang.String", "/personAttributeType/format", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("Delete Cohorts", "/personAttributeType/editPrivilege/privilege", xmlOutput);
	}
	
	/**
	 * construct a deserialized xml string and make sure it can be deserialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldPersonAttributeTypeDeserialization() throws Exception {
		//construct given string to be deserialized
		StringBuilder xmlBuilder = new StringBuilder();
		xmlBuilder.append("<personAttributeType id=\"1\" uuid=\"b3b6d540-a32e-44c7-91b3-292d97667518\" retired=\"true\">\n");
		xmlBuilder.append("  <name>Race</name>\n");
		xmlBuilder.append("  <description>Group of persons related by common descent or heredity</description>\n");
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
		xmlBuilder.append("  <dateCreated class=\"sql-timestamp\" id=\"16\">2007-05-04 09:59:23 CST</dateCreated>\n");
		xmlBuilder.append("  <dateRetired class=\"sql-timestamp\" id=\"17\">2008-08-15 00:00:00 CST</dateRetired>\n");
		xmlBuilder.append("  <retiredBy reference=\"2\"/>\n");
		xmlBuilder.append("  <retireReason>test</retireReason>\n");
		xmlBuilder.append("  <personAttributeTypeId>1</personAttributeTypeId>\n");
		xmlBuilder.append("  <format>java.lang.String</format>\n");
		xmlBuilder.append("  <foreignKey>1053</foreignKey>\n");
		xmlBuilder.append("  <searchable>false</searchable>\n");
		xmlBuilder.append("  <editPrivilege id=\"18\" retired=\"false\">\n");
		xmlBuilder.append("    <description>Able to add a cohort to the system</description>\n");
		xmlBuilder.append("    <privilege>Delete Cohorts</privilege>\n");
		xmlBuilder.append("  </editPrivilege>\n");
		xmlBuilder.append("</personAttributeType>\n");
		
		//deserialize and make sure everything has been put into object
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		PersonAttributeType personAttributeType = Context.getSerializationService().deserialize(xmlBuilder.toString(),
		    PersonAttributeType.class, XStreamSerializer.class);
		assertEquals(1, personAttributeType.getPersonAttributeTypeId().intValue());
		assertTrue("The retired shouldn't be " + personAttributeType.getRetired(), personAttributeType.getRetired());
		assertFalse("The searchable shouldn't be " + personAttributeType.getSearchable(), personAttributeType
		        .getSearchable());
		assertEquals(1053, personAttributeType.getForeignKey().intValue());
		assertEquals("b3b6d540-a32e-44c7-91b3-292d97667518", personAttributeType.getUuid());
		assertEquals("Race", personAttributeType.getName());
		assertEquals("Group of persons related by common descent or heredity", personAttributeType.getDescription());
		assertEquals(1, personAttributeType.getCreator().getPersonId().intValue());
		assertEquals(sdf.parse("2007-05-04 09:59:23 CST"), personAttributeType.getDateCreated());
		assertEquals(sdf.parse("2008-08-15 00:00:00 CST"), personAttributeType.getDateRetired());
		assertEquals(1, personAttributeType.getRetiredBy().getPersonId().intValue());
		assertEquals("test", personAttributeType.getRetireReason());
		assertEquals("java.lang.String", personAttributeType.getFormat());
		assertEquals("Delete Cohorts", personAttributeType.getEditPrivilege().getPrivilege());
	}
}

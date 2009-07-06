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
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.xstream.XStreamSerializer;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

/**
 * Test class that tests the serialization and deserialization of a PersonAddress
 */
public class PersonAddressSerializationTest extends BaseContextSensitiveTest {
	
	/**
	 * create a person address and make sure it can be serialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	@SkipBaseSetup
	public void shouldPersonAddressSerialization() throws Exception {
		
		//instantiate object
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/serialization/include/PersonAddressSerializationTest.xml");
		authenticate();
		PersonAddress pa = Context.getPersonService().getPersonAddressByUuid("921c0e23-d941-4bac-8ce4-ab0d0f7d8123");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		//serialize and compare with a give string
		String xmlOutput = Context.getSerializationService().serialize(pa, XStreamSerializer.class);
		
		XMLAssert.assertXpathEvaluatesTo("921c0e23-d941-4bac-8ce4-ab0d0f7d8123", "/personAddress/@uuid", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("1", "/personAddress/personAddressId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("false", "/personAddress/@voided", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("false", "/personAddress/preferred", xmlOutput);
		XMLAssert.assertXpathExists("/personAddress/creator", xmlOutput);
		XMLAssert.assertXpathExists("/personAddress/person/@reference", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("1050 Wishard Blvd.", "/personAddress/address1", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("RG5", "/personAddress/address2", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("New York", "/personAddress/stateProvince", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("USA", "/personAddress/country", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("Kapina", "/personAddress/cityVillage", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("46202", "/personAddress/postalCode", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo(sdf.format(pa.getDateCreated()), "/personAddress/dateCreated", xmlOutput);
	}
	
	/**
	 * construct a deserialized xml string and make sure it can be deserialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldPersonAddressDeserialization() throws Exception {
		//construct given string to be deserialized
		StringBuilder xmlBuilder = new StringBuilder();
		xmlBuilder.append("<personAddress id=\"1\" uuid=\"921c0e23-d941-4bac-8ce4-ab0d0f7d8123\" voided=\"false\">\n");
		xmlBuilder.append("  <creator id=\"2\" uuid=\"6adb7c42-cfd2-4301-b53b-ff17c5654ff7\" voided=\"false\">\n");
		xmlBuilder.append("    <creator reference=\"2\"/>\n");
		xmlBuilder.append("    <dateCreated class=\"sql-timestamp\" id=\"3\">2005-01-01 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("    <changedBy reference=\"2\"/>\n");
		xmlBuilder.append("    <dateChanged class=\"sql-timestamp\" id=\"4\">2007-09-20 21:54:12 CST</dateChanged>\n");
		xmlBuilder.append("    <voidReason></voidReason>\n");
		xmlBuilder.append("    <personId>1</personId>\n");
		xmlBuilder.append("    <addresses class=\"tree-set\" id=\"5\">\n");
		xmlBuilder.append("      <no-comparator/>\n");
		xmlBuilder.append("      <personAddress reference=\"1\"/>\n");
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
		xmlBuilder.append("  <dateCreated class=\"sql-timestamp\" id=\"16\">2006-01-18 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("  <voidReason></voidReason>\n");
		xmlBuilder.append("  <personAddressId>1</personAddressId>\n");
		xmlBuilder.append("  <person class=\"user\" reference=\"2\"/>\n");
		xmlBuilder.append("  <preferred>false</preferred>\n");
		xmlBuilder.append("  <address1>1050 Wishard Blvd.</address1>\n");
		xmlBuilder.append("  <address2>RG5</address2>\n");
		xmlBuilder.append("  <cityVillage>Kapina</cityVillage>\n");
		xmlBuilder.append("  <stateProvince>New York</stateProvince>\n");
		xmlBuilder.append("  <country>USA</country>\n");
		xmlBuilder.append("  <postalCode>46202</postalCode>\n");
		xmlBuilder.append("</personAddress>\n");
		
		//deserialize and make sure everything has been put into object
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		PersonAddress pa = Context.getSerializationService().deserialize(xmlBuilder.toString(), PersonAddress.class,
		    XStreamSerializer.class);
		assertEquals(1, pa.getPersonAddressId().intValue());
		assertEquals("921c0e23-d941-4bac-8ce4-ab0d0f7d8123", pa.getUuid());
		assertFalse("The voided shouldn't be " + pa.getVoided(), pa.getVoided());
		assertFalse("The preferred shouldn't be " + pa.getPreferred(), pa.getPreferred());
		assertEquals(1, pa.getCreator().getPersonId().intValue());
		assertEquals(sdf.parse("2006-01-18 00:00:00 CST"), pa.getDateCreated());
		assertEquals(1, pa.getPerson().getPersonId().intValue());
		assertEquals("1050 Wishard Blvd.", pa.getAddress1());
		assertEquals("RG5", pa.getAddress2());
		assertEquals("Kapina", pa.getCityVillage());
		assertEquals("New York", pa.getStateProvince());
		assertEquals("USA", pa.getCountry());
		assertEquals("46202", pa.getPostalCode());
	}
}

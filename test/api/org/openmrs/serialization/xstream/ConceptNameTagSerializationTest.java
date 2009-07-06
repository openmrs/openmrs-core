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

import java.text.SimpleDateFormat;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.openmrs.ConceptNameTag;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.xstream.XStreamSerializer;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

/**
 * Test class that tests the serialization and deserialization of a conceptNameTag
 */
public class ConceptNameTagSerializationTest extends BaseContextSensitiveTest {
	
	/**
	 * create a conceptNameTag and make sure it can be serialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	@SkipBaseSetup
	public void shouldSerializeConcetpNameTag() throws Exception {
		//instantiate object
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/serialization/include/ConceptNameTagSerializationTest.xml");
		authenticate();
		
		ConceptNameTag cnt = Context.getConceptService().getConceptNameTag(1);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		//serialize and compare with a give string
		String xmlOutput = Context.getSerializationService().serialize(cnt, XStreamSerializer.class);
		XMLAssert.assertXpathEvaluatesTo("9e9df183-2328-4117-acd8-fb9bf400911d", "//conceptNameTag/@uuid", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("1", "/conceptNameTag/conceptNameTagId", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("default", "/conceptNameTag/tag", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("name to use when nothing else is available", "/conceptNameTag/description",
		    xmlOutput);
		XMLAssert.assertXpathExists("/conceptNameTag/creator", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo(sdf.format(cnt.getDateCreated()), "/conceptNameTag/dateCreated", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("false", "/conceptNameTag/voided", xmlOutput);
	}
	
	/**
	 * Construct a serialized xml string and make sure it can be deserialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldDeserializeConceptNameTag() throws Exception {
		//construct given string to be deserialized
		StringBuilder xmlBuilder = new StringBuilder();
		xmlBuilder.append("<conceptNameTag id=\"1\" uuid=\"9e9df183-2328-4117-acd8-fb9bf400911d\">\n");
		xmlBuilder.append("  <conceptNameTagId>1</conceptNameTagId>\n");
		xmlBuilder.append("  <tag>default</tag>\n");
		xmlBuilder.append("  <description>name to use when nothing else is available</description>\n");
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
		xmlBuilder.append("  <dateCreated class=\"sql-timestamp\" id=\"16\">2007-05-01 00:00:00 CST</dateCreated>\n");
		xmlBuilder.append("  <voided>false</voided>\n");
		xmlBuilder.append("</conceptNameTag>\n");
		
		//deserialize and make sure everything has been put into object
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		
		ConceptNameTag cnt = Context.getSerializationService().deserialize(xmlBuilder.toString(), ConceptNameTag.class,
		    XStreamSerializer.class);
		assertEquals("9e9df183-2328-4117-acd8-fb9bf400911d", cnt.getUuid());
		assertEquals(1, cnt.getConceptNameTagId().intValue());
		assertEquals("default", cnt.getTag());
		assertEquals("name to use when nothing else is available", cnt.getDescription());
		assertEquals(1, cnt.getCreator().getPersonId().intValue());
		assertEquals(sdf.parse("2007-05-01 00:00:00 CST"), cnt.getDateCreated());
		assertFalse("The voided shouldn't be " + cnt.getVoided(), cnt.getVoided());
	}
}

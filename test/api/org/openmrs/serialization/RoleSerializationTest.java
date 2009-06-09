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

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.openmrs.Role;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.xstream.XStreamSerializer;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

/**
 * Test class that tests the serialization and deserialization of a role
 */
public class RoleSerializationTest extends BaseContextSensitiveTest {
	
	/**
	 * create a role and make sure it can be serialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	@SkipBaseSetup
	public void shouldRoleSerialization() throws Exception {
		//instantiate object
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/serialization/include/RoleSerializationTest.xml");
		authenticate();
		Role role = Context.getUserService().getRole("Data Manager");
		//serialize and compare with a give string
		String xmlOutput = Context.getSerializationService().serialize(role, XStreamSerializer.class);
		
		XMLAssert.assertXpathEvaluatesTo("00eb3992-92b2-102c-adee-6014420f8468", "/role/@uuid", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("Data Manager", "/role/role", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("false", "/role/@retired", xmlOutput);
		XMLAssert.assertXpathEvaluatesTo("User who maintains clinical data stored within the OpenMRS repository.",
		    "/role/description", xmlOutput);
		XMLAssert.assertXpathExists("/role/privileges/privilege[privilege='Add Observations']", xmlOutput);
		XMLAssert.assertXpathExists("/role/privileges/privilege[privilege='Add Patients']", xmlOutput);
		XMLAssert.assertXpathExists("/role/privileges/privilege[privilege='Delete Observations']", xmlOutput);
		XMLAssert.assertXpathExists("/role/privileges/privilege[privilege='Delete Report Objects']", xmlOutput);
		XMLAssert.assertXpathExists("/role/inheritedRoles/role[role='Data Assistant']", xmlOutput);
	}
	
	/**
	 * construct a deserialized xml string and make sure it can be deserialized correctly
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldRoleDeserialization() throws Exception {
		//construct given string to be deserialized
		StringBuilder xmlBuilder = new StringBuilder();
		xmlBuilder.append("<role id=\"1\" uuid=\"00eb3992-92b2-102c-adee-6014420f8468\" retired=\"false\">\n");
		xmlBuilder
		        .append("  <description>User who maintains clinical data stored within the OpenMRS repository.</description>\n");
		xmlBuilder.append("  <role>Data Manager</role>\n");
		xmlBuilder.append("  <privileges id=\"2\">\n");
		xmlBuilder.append("    <privilege id=\"3\" uuid=\"00e8c00e-92b2-102c-adee-6014420f8468\" retired=\"false\">\n");
		xmlBuilder.append("      <description>Able to add patients</description>\n");
		xmlBuilder.append("      <privilege>Add Patients</privilege>\n");
		xmlBuilder.append("    </privilege>\n");
		xmlBuilder.append("    <privilege id=\"4\" uuid=\"00e8bcbf-92b2-102c-adee-6014420f8468\" retired=\"false\">\n");
		xmlBuilder.append("      <description>Able to add patient observations</description>\n");
		xmlBuilder.append("      <privilege>Add Observations</privilege>\n");
		xmlBuilder.append("    </privilege>\n");
		xmlBuilder.append("    <privilege id=\"5\" uuid=\"00e8def6-92b2-102c-adee-6014420f8468\" retired=\"false\">\n");
		xmlBuilder.append("      <description>Able to delete patient observations</description>\n");
		xmlBuilder.append("      <privilege>Delete Observations</privilege>\n");
		xmlBuilder.append("    </privilege>\n");
		xmlBuilder.append("    <privilege id=\"6\" uuid=\"00e8e5ae-92b2-102c-adee-6014420f8468\" retired=\"false\">\n");
		xmlBuilder.append("      <description>Able to delete report objects</description>\n");
		xmlBuilder.append("      <privilege>Delete Report Objects</privilege>\n");
		xmlBuilder.append("    </privilege>\n");
		xmlBuilder.append("  </privileges>\n");
		xmlBuilder.append("  <inheritedRoles id=\"7\">\n");
		xmlBuilder.append("    <role id=\"8\" uuid=\"00eb389f-92b2-102c-adee-6014420f8468\" retired=\"false\">\n");
		xmlBuilder.append("      <description>Clerks who perform data entry.</description>\n");
		xmlBuilder.append("      <role>Data Assistant</role>\n");
		xmlBuilder.append("      <privileges id=\"9\">\n");
		xmlBuilder.append("        <privilege id=\"10\" uuid=\"00e8c0df-92b2-102c-adee-6014420f8468\" retired=\"false\">\n");
		xmlBuilder.append("          <description>Able to add person objects</description>\n");
		xmlBuilder.append("          <privilege>Add People</privilege>\n");
		xmlBuilder.append("        </privilege>\n");
		xmlBuilder.append("        <privilege reference=\"3\"/>\n");
		xmlBuilder.append("        <privilege id=\"11\" uuid=\"00e8d4fc-92b2-102c-adee-6014420f8468\" retired=\"false\">\n");
		xmlBuilder.append("          <description>Able to add relationships</description>\n");
		xmlBuilder.append("          <privilege>Add Relationships</privilege>\n");
		xmlBuilder.append("        </privilege>\n");
		xmlBuilder.append("      </privileges>\n");
		xmlBuilder.append("      <inheritedRoles id=\"12\"/>\n");
		xmlBuilder.append("    </role>\n");
		xmlBuilder.append("  </inheritedRoles>\n");
		xmlBuilder.append("</role>\n");
		
		//deserialize and make sure everything has been put into object
		Role role = Context.getSerializationService().deserialize(xmlBuilder.toString(), Role.class, XStreamSerializer.class);
		assertEquals("00eb3992-92b2-102c-adee-6014420f8468", role.getUuid());
		assertFalse("The retired shouldn't be " + role.getRetired(), role.getRetired());
		assertEquals("Data Manager", role.getRole());
		assertEquals("User who maintains clinical data stored within the OpenMRS repository.", role.getDescription());
		assertEquals(4, role.getPrivileges().size());
		assertEquals(1, role.getInheritedRoles().size());
	}
}

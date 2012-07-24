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
package org.openmrs.test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.RelationshipType;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;

/**
 * This is a helper class that creates the liquibase xml for setting common UUIDs on the base
 * dataset. See ticket <a href="http://dev.openmrs.org/ticket/1842">#1842</a>:
 * "Synchronize core metadata UUIDs across sites".
 */
@Ignore
public class CreateCoreUuids extends BaseContextSensitiveTest {
	
	//@Test
	@SkipBaseSetup
	public void getUUIDs() throws Exception {
		Context.authenticate("admin", "test");
		System.out.println("db: " + OpenmrsConstants.DATABASE_NAME);
		
		Map<String, List<? extends OpenmrsMetadata>> coremetadatas = new LinkedHashMap<String, List<? extends OpenmrsMetadata>>();
		
		coremetadatas.put("field_type", Context.getFormService().getAllFieldTypes(true));
		coremetadatas.put("person_attribute_type", Context.getPersonService().getAllPersonAttributeTypes(true));
		coremetadatas.put("encounter_type", Context.getEncounterService().getAllEncounterTypes(true));
		coremetadatas.put("concept_datatype", Context.getConceptService().getAllConceptDatatypes(true));
		coremetadatas.put("concept_class", Context.getConceptService().getAllConceptClasses(true));
		coremetadatas.put("patient_identifier_type", Context.getPatientService().getAllPatientIdentifierTypes(true));
		coremetadatas.put("location", Context.getLocationService().getAllLocations(true));
		coremetadatas.put("hl7_source", Context.getHL7Service().getAllHL7Sources());
		
		for (Map.Entry<String, List<? extends OpenmrsMetadata>> entry : coremetadatas.entrySet()) {
			System.out.println("new table: " + entry.getKey());
			
			for (OpenmrsMetadata obj : entry.getValue()) {
				
				String output = "<update tableName=\"" + entry.getKey() + "\"><column name=\"uuid\" value=\""
				        + obj.getUuid() + "\"/><where>" + entry.getKey() + "_id" + "= '" + obj.getId() + "' and name = '"
				        + obj.getName().replace("'", "\\'") + "'</where></update>";
				System.out.println(output);
			}
		}
		
		///////////////////////////////////////
		// exceptions:
		//
		
		// relationship types
		System.out.println("Relationship type");
		for (RelationshipType type : Context.getPersonService().getAllRelationshipTypes()) {
			String output = "<update tableName=\"relationship_type\"><column name=\"uuid\" value=\"" + type.getUuid()
			        + "\"/><where> relationship_type_id = '" + type.getRelationshipTypeId() + "' and a_is_to_b = '"
			        + type.getaIsToB().replace("'", "\\'") + "' and b_is_to_a = '" + type.getbIsToA().replace("'", "\\'")
			        + "'</where></update>";
			System.out.println(output);
		}
		
		// roles:
		System.out.println("Roles");
		for (Role role : Context.getUserService().getAllRoles()) {
			String output = "<update tableName=\"role\"><column name=\"uuid\" value=\"" + role.getUuid()
			        + "\"/><where> role = '" + role.getRole().replace("'", "\\'") + "'</where></update>";
			System.out.println(output);
		}
		
		// user:
		System.out.println("Users");
		for (User user : Context.getUserService().getAllUsers()) {
			String output = "<update tableName=\"users\"><column name=\"uuid\" value=\"" + user.getUuid()
			        + "\"/><where> user_id = '" + user.getUserId() + "' and system_id = '"
			        + user.getSystemId().replace("'", "\\'") + "'</where></update>";
			System.out.println(output);
		}
		
	}
	
	/**
	 * Make sure we use the database defined by the runtime properties and not the hsql in-memory
	 * database
	 * 
	 * @see org.openmrs.test.BaseContextSensitiveTest#useInMemoryDatabase()
	 */
	@Override
	public Boolean useInMemoryDatabase() {
		return false;
	}
}

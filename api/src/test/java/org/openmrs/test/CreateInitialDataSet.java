/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.test;

import java.io.FileOutputStream;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This class can be run like a junit test, but it is not actually a test. JUnit won't run it
 * because it does not have "Test" in its class name. The
 * {@link BaseContextSensitiveTest#INITIAL_DATA_SET_XML_FILENAME} file is overwritten by values in
 * the database defined by the runtime properties
 */
@Ignore
public class CreateInitialDataSet extends BaseContextSensitiveTest {
	
	/**
	 * This test creates an xml dbunit file from the current database connection information found
	 * in the runtime properties. This method has to "skip over the base setup" because it tries to
	 * do things (like initialize the database) that shouldn't be done to a standard mysql database.
	 * 
	 * @throws Exception
	 */
	@Test
	@SkipBaseSetup
	public void shouldCreateInitialTestDataSetXmlFile() throws Exception {
		
		// only run this test if it is being run alone.
		// this allows the junit-report ant target and the "right-
		// click-on-/test/api-->run as-->junit test" methods to skip
		// over this whole "test"
		if (getLoadCount() != 1)
			return;
		
		// database connection for dbunit
		IDatabaseConnection connection = new DatabaseConnection(getConnection());
		
		// partial database export
		QueryDataSet initialDataSet = new QueryDataSet(connection);
		initialDataSet.addTable("concept", "SELECT * FROM concept");
		initialDataSet.addTable("concept_answer", "SELECT * FROM concept_answer");
		initialDataSet.addTable("concept_class", "SELECT * FROM concept_class");
		initialDataSet.addTable("concept_datatype", "SELECT * FROM concept_datatype");
		initialDataSet.addTable("concept_name", "SELECT * FROM concept_name");
		initialDataSet.addTable("concept_numeric", "SELECT * FROM concept_numeric");
		initialDataSet.addTable("concept_set", "SELECT * FROM concept_set");
		initialDataSet.addTable("concept_synonym", "SELECT * FROM concept_synonym");
		initialDataSet.addTable("drug", "SELECT * FROM drug");
		initialDataSet.addTable("drug_order", "SELECT * FROM drug_order");
		initialDataSet.addTable("encounter", "SELECT * FROM encounter");
		initialDataSet.addTable("encounter_type", "SELECT * FROM encounter_type");
		initialDataSet.addTable("location", "SELECT * FROM location");
		initialDataSet.addTable("obs", "SELECT * FROM obs");
		initialDataSet.addTable("order_type", "SELECT * FROM order_type");
		initialDataSet.addTable("orders", "SELECT * FROM orders");
		initialDataSet.addTable("patient", "SELECT * FROM patient");
		initialDataSet.addTable("patient_identifier", "SELECT * FROM patient_identifier");
		initialDataSet.addTable("patient_identifier_type", "SELECT * FROM patient_identifier_type");
		initialDataSet.addTable("patient_program", "SELECT * FROM patient_program");
		initialDataSet.addTable("patient_state", "SELECT * FROM patient_state");
		initialDataSet.addTable("person", "SELECT * FROM person");
		initialDataSet.addTable("person_address", "SELECT * FROM person_address");
		initialDataSet.addTable("person_attribute", "SELECT * FROM person_attribute");
		initialDataSet.addTable("person_attribute_type", "SELECT * FROM person_attribute_type");
		initialDataSet.addTable("person_name", "SELECT * FROM person_name");
		initialDataSet.addTable("privilege", "SELECT * FROM privilege");
		initialDataSet.addTable("program", "SELECT * FROM program");
		initialDataSet.addTable("program_workflow", "SELECT * FROM program_workflow");
		initialDataSet.addTable("program_workflow_state", "SELECT * FROM program_workflow_state");
		initialDataSet.addTable("relationship", "SELECT * FROM relationship");
		initialDataSet.addTable("relationship_type", "SELECT * FROM relationship_type");
		initialDataSet.addTable("role", "SELECT * FROM role");
		initialDataSet.addTable("role_privilege", "SELECT * FROM role_privilege");
		initialDataSet.addTable("role_role", "SELECT * FROM role_role");
		initialDataSet.addTable("user_role", "SELECT * FROM user_role");
		initialDataSet.addTable("users", "SELECT * FROM users");
		
		/*
		initialDataSet.addTable("field", "SELECT * FROM field");
		initialDataSet.addTable("field_answer", "SELECT * FROM field_answer");
		initialDataSet.addTable("field_type", "SELECT * FROM field_type");
		initialDataSet.addTable("form", "SELECT * FROM form");
		initialDataSet.addTable("form_field", "SELECT * FROM form_field");
		initialDataSet.addTable("hl7_source", "SELECT * FROM hl7_source");
		*/

		FlatXmlDataSet.write(initialDataSet, new FileOutputStream("test/api/org/openmrs/logic/include/LogicBasicTest.xml"));
		
		// full database export
		//IDataSet fullDataSet = connection.createDataSet();
		//FlatXmlDataSet.write(fullDataSet, new FileOutputStream("full.xml"));
		
		// dependent tables database export: export table X and all tables that
		// have a PK which is a FK on X, in the right order for insertion
		//String[] depTableNames = TablesDependencyHelper.getAllDependentTables(connection, "X");
		//IDataSet depDataset = connection.createDataSet( depTableNames );
		//FlatXmlDataSet.write(depDataSet, new FileOutputStream("dependents.xml")); 
		
		//TestUtil.printOutTableContents(getConnection(), "encounter_type", "encounter");
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

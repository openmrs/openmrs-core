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
package org.openmrs;

import java.io.FileOutputStream;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

/**
 * This class can be run like a junit test, but it is not actually a test. JUnit
 * won't run it because it does not have "Test" in its class name.
 * 
 * The {@link BaseContextSensitiveTest#INITIAL_DATA_SET_XML_FILENAME} file is
 * overwritten by values in the database defined by the runtime properties
 */
public class CreateInitialDataSet extends BaseContextSensitiveTest {
	
	/**
	 * Do the stuff for this class (create the file)
	 * 
	 * @throws Exception
	 */
	public void testcreateInitialTestDataSetXmlFile() throws Exception {
		// database connection for dbunit
		IDatabaseConnection connection = new DatabaseConnection(getConnection());
        
        // partial database export
        QueryDataSet initialDataSet = new QueryDataSet(connection);
        initialDataSet.addTable("person", "SELECT * FROM person WHERE person_id = 1");
        initialDataSet.addTable("users", "SELECT * FROM users WHERE user_id = 1");
        initialDataSet.addTable("role", "SELECT * FROM role WHERE role = 'System Developer'");
        initialDataSet.addTable("user_role", "SELECT * FROM user_role WHERE user_id = 1");
        
        FlatXmlDataSet.write(initialDataSet, new FileOutputStream("test/api/" + INITIAL_XML_DATASET_PACKAGE_PATH));

        // full database export
        //IDataSet fullDataSet = connection.createDataSet();
        //FlatXmlDataSet.write(fullDataSet, new FileOutputStream("full.xml"));
        
        // dependent tables database export: export table X and all tables that
        // have a PK which is a FK on X, in the right order for insertion
        //String[] depTableNames = TablesDependencyHelper.getAllDependentTables(connection, "X");
        //IDataSet depDataset = connection.createDataSet( depTableNames );
        //FlatXmlDataSet.write(depDataSet, new FileOutputStream("dependents.xml")); 
	}
	
	/**
	 * Make sure we use the database defined by the runtime properties
	 * and not the hsql in-memory database
	 * 
	 * @see org.openmrs.BaseContextSensitiveTest#useInMemoryDatabase()
	 */
	@Override
	public Boolean useInMemoryDatabase() {
		return false;
	}
}

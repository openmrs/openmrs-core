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

import java.io.FileOutputStream;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.ForwardOnlyResultSetTableFactory;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Creates conceptDictionaryDataSet.xml. You must set connection details in
 * hibernate.default.properties.
 */
@Ignore
public class CreateConceptDictionaryDataSet extends BaseContextSensitiveTest {
	
	@Override
	public Boolean useInMemoryDatabase() {
		return false;
	}
	
	@Test
	@SkipBaseSetup
	public void createConceptDictionaryDataSet() throws Exception {
		IDatabaseConnection connection = new DatabaseConnection(getConnection());
		DatabaseConfig config = connection.getConfig();
		
		config.setProperty(DatabaseConfig.PROPERTY_RESULTSET_TABLE_FACTORY, new ForwardOnlyResultSetTableFactory());
		
		String[] tableNames = new String[] { "concept_class", "concept_datatype", "concept_map_type",
		        "concept_reference_source", "concept", "concept_numeric", "concept_description", "concept_name",
		        "concept_reference_term", "concept_reference_map", "concept_reference_term_map", "concept_set",
		        "concept_complex", "concept_answer", "concept_stop_word" };
		IDataSet dataSet = connection.createDataSet(tableNames);
		
		FlatXmlDataSet.write(dataSet, new FileOutputStream("target/conceptDictionaryDataSet.xml"));
	}
}

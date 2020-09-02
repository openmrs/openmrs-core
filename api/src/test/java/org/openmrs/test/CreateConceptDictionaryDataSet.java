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

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.ForwardOnlyResultSetTableFactory;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Creates conceptDictionaryDataSet.xml. You must set connection details in
 * hibernate.default.properties.
 */
@Disabled
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

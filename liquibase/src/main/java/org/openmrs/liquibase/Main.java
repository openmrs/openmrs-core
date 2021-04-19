/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.liquibase;

import java.io.IOException;
import java.nio.file.Paths;

import org.dom4j.DocumentException;

public class Main {
	
	public static final String LIQUIBASE_CORE_DATA_SOURCE_PATH = Paths
	        .get(".", "snapshots", "liquibase-core-data-SNAPSHOT.xml").toString();
	
	public static final String LIQUIBASE_CORE_DATA_TARGET_PATH = Paths
	        .get(".", "snapshots", "liquibase-core-data-UPDATED-SNAPSHOT.xml").toString();
	
	public static final String LIQUIBASE_SCHEMA_ONLY_SOURCE_PATH = Paths
	        .get(".", "snapshots", "liquibase-schema-only-SNAPSHOT.xml").toString();
	
	public static final String LIQUIBASE_SCHEMA_ONLY_TARGET_PATH = Paths
	        .get(".", "snapshots", "liquibase-schema-only-UPDATED-SNAPSHOT.xml").toString();
	
	private static CoreDataTuner coreDataTuner;
	
	private static SchemaOnlyTuner schemaOnlyTuner;
	
	static {
		coreDataTuner = new CoreDataTuner();
		schemaOnlyTuner = new SchemaOnlyTuner();
	}
	
	public static void main(String[] args) throws DocumentException, IOException {
		coreDataTuner.addLicenseHeaderToFileIfNeeded(LIQUIBASE_CORE_DATA_SOURCE_PATH);
		coreDataTuner.createUpdatedChangeLogFile(LIQUIBASE_CORE_DATA_SOURCE_PATH, LIQUIBASE_CORE_DATA_TARGET_PATH);
		schemaOnlyTuner.addLicenseHeaderToFileIfNeeded(LIQUIBASE_SCHEMA_ONLY_SOURCE_PATH);
		schemaOnlyTuner.createUpdatedChangeLogFile(LIQUIBASE_SCHEMA_ONLY_SOURCE_PATH, LIQUIBASE_SCHEMA_ONLY_TARGET_PATH);
	}
	
	static void setCoreDataTuner(CoreDataTuner coreDataTuner) {
		Main.coreDataTuner = coreDataTuner;
	}
	
	static void setSchemaOnlyTuner(SchemaOnlyTuner schemaOnlyTuner) {
		Main.schemaOnlyTuner = schemaOnlyTuner;
	}
}

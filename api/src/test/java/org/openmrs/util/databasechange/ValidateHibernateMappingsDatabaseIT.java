/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util.databasechange;

import javax.persistence.Entity;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.junit.jupiter.api.Test;
import org.openmrs.api.OrderServiceTest;
import org.openmrs.liquibase.ChangeLogVersionFinder;
import org.openmrs.util.H2DatabaseIT;
import org.openmrs.util.OpenmrsClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates Hibernate mapping files.
 */
public class ValidateHibernateMappingsDatabaseIT extends H2DatabaseIT {
	
	private static final Logger log = LoggerFactory.getLogger(ValidateHibernateMappingsDatabaseIT.class);
	
	
	@Test
	public void shouldValidateHibernateMappings() throws Exception {
		/*
		 * Drop all database objects before running the test as previously run tests may have left tables behind.
		 */
		this.dropAllDatabaseObjects();
		
		ChangeLogVersionFinder changeLogVersionFinder = new ChangeLogVersionFinder();
		Map<String, List<String>> changeLogCombinations = changeLogVersionFinder.getChangeLogCombinations();
		
		// test all possible combinations of liquibase snapshot and update files
		//
		for (List<String> snapshotAndUpdateFileNames : changeLogCombinations.values()) {
			
			this.initializeDatabase();
			
			log.info(
			    "liquibase files used for creating and updating the OpenMRS database are: " + snapshotAndUpdateFileNames);
			
			for (String fileName : snapshotAndUpdateFileNames) {
				// process the core data file only for the first generation of liquibase snapshot files
				//
				if (fileName.contains("liquibase-core-data-1.9.x.xml")) {
					log.info("processing " + fileName);
					this.updateDatabase(fileName);
				}
				
				// exclude the core data file for subsequent generations of liquibase snapshot files
				//
				if (!fileName.contains("liquibase-core-data")) {
					log.info("processing " + fileName);
					this.updateDatabase(fileName);
				}
			}
			
			// this is the core of this test: building the session factory validates if the generated database schema 
			// corresponds to Hibernate mappings
			//
			this.buildSessionFactory();
			
			this.dropAllDatabaseObjects();
		}
	}
	
	private SessionFactory buildSessionFactory() {
		Configuration configuration = new Configuration().configure();
		
		Set<Class<?>> entityClasses = OpenmrsClassScanner.getInstance().getClassesWithAnnotation(Entity.class);
		if (entityClasses.contains(OrderServiceTest.SomeTestOrder.class)) {
			entityClasses.remove(OrderServiceTest.SomeTestOrder.class);
		}
		for (Class<?> clazz : entityClasses) {
			configuration.addAnnotatedClass(clazz);
		}
		configuration.setProperty(Environment.DIALECT, H2LessStrictDialect.class.getName());
		configuration.setProperty(Environment.URL, "jdbc:h2:mem:openmrs;AUTO_RECONNECT=TRUE;DB_CLOSE_DELAY=-1");
		configuration.setProperty(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
		configuration.setProperty(Environment.USER, USER_NAME);
		configuration.setProperty(Environment.PASS, PASSWORD);
		configuration.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "false");
		configuration.setProperty(Environment.USE_QUERY_CACHE, "false");
		
		// Validate HBMs against the actual schema
		configuration.setProperty(Environment.HBM2DDL_AUTO, "validate");
		
		return configuration.buildSessionFactory();
	}
}

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

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.envers.Audited;
import org.junit.jupiter.api.Test;
import org.openmrs.api.OrderServiceTest;
import org.openmrs.api.db.hibernate.envers.OpenmrsRevisionEntity;
import org.openmrs.util.DatabaseIT;
import org.openmrs.util.OpenmrsClassScanner;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Validates that Hibernate Envers audit tables are correctly generated for all @Audited entities.
 */
public class ValidateHibernateEnversAuditTablesDatabaseIT extends DatabaseIT {
	
	@Test
	public void shouldGenerateAuditTablesForAllAuditedEntities() throws Exception {

		this.dropAllDatabaseObjects();
		this.initializeDatabase();
		this.updateDatabase("liquibase-schema-only.xml");
		
		Set<Class<?>> auditedClasses = OpenmrsClassScanner.getInstance().getClassesWithAnnotation(Audited.class);
		SessionFactory sessionFactory = this.buildSessionFactoryWithEnvers();
		
		// This is main part, it cross verifies and fails immediately if any audit table is not generated.
		 verifyAuditTablesExist(auditedClasses);
		
		if (sessionFactory != null) {
			sessionFactory.close();
		}
		
		this.dropAllDatabaseObjects();
	}
	
	private SessionFactory buildSessionFactoryWithEnvers() {
		Configuration configuration = new Configuration().configure();
		
		Set<Class<?>> entityClasses = OpenmrsClassScanner.getInstance().getClassesWithAnnotation(Entity.class);
		entityClasses.remove(OrderServiceTest.SomeTestOrder.class);
		entityClasses.remove(OpenmrsRevisionEntity.class);
		for (Class<?> clazz : entityClasses) {
			configuration.addAnnotatedClass(clazz);
		}
		
		configuration.setProperty(Environment.DIALECT, System.getProperty("databaseDialect"));
		configuration.setProperty(Environment.URL, CONNECTION_URL);
		configuration.setProperty(Environment.USER, USER_NAME);
		configuration.setProperty(Environment.PASS, PASSWORD);
		configuration.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "false");
		configuration.setProperty(Environment.USE_QUERY_CACHE, "false");
		configuration.setProperty("hibernate.integration.envers.enabled", "true");
		configuration.setProperty(Environment.HBM2DDL_AUTO, "update");
		configuration.setProperty("hibernate.search.backend.type", "lucene");
		configuration.setProperty("hibernate.search.backend.analysis.configurer", "class:org.openmrs.api.db.hibernate.search.lucene.LuceneConfig");
		
		return configuration.buildSessionFactory();
	}
	
	private void verifyAuditTablesExist(Set<Class<?>> auditedClasses) throws SQLException {
		Set<String> existingTables = getExistingTables();
		
		for (Class<?> clazz : auditedClasses) {
			String tableName = getTableName(clazz);
			if (tableName != null) {
				String auditTableName = tableName.toUpperCase() + "_AUD";
				
				if (!existingTables.contains(auditTableName)) {
					fail("Missing audit table " + auditTableName + " for entity " + clazz.getSimpleName());
				}
			}
		}
	}
	
	private Set<String> getExistingTables() throws SQLException {
		Set<String> tables = new HashSet<>();
		
		try (Connection connection = getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();
			
			// This will give all the tables generated including the audit tables
			try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
				while (rs.next()) {
					String tableName = rs.getString("TABLE_NAME");
					if (tableName != null) {
						tables.add(tableName.toUpperCase());
					}
				}
			}
		}
		
		return tables;
	}
	
	private String getTableName(Class<?> clazz) {
		Table tableAnnotation = clazz.getAnnotation(Table.class);
		if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
			return tableAnnotation.name();
		}
		return null;
	}
}

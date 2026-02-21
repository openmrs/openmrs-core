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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.envers.Audited;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.OrderServiceTest;
import org.openmrs.api.db.hibernate.envers.OpenmrsRevisionEntity;
import org.openmrs.liquibase.ChangeLogVersionFinder;
import org.openmrs.util.DatabaseIT;
import org.openmrs.util.EnversAuditTableInitializer;
import org.openmrs.util.OpenmrsClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates that Envers audit tables are correctly generated when auditing is enabled.
 */
class EnversAuditTableInitializerDatabaseIT extends DatabaseIT {
	
	private static final Logger log = LoggerFactory.getLogger(EnversAuditTableInitializerDatabaseIT.class);
	
	@BeforeEach
	void beforeEach() throws Exception {
		this.dropAllDatabaseObjects();
		
		ChangeLogVersionFinder changeLogVersionFinder = new ChangeLogVersionFinder();
		String latestVersion = changeLogVersionFinder.getLatestSnapshotVersion()
		        .orElseThrow(() -> new RuntimeException("No snapshot version found"));
		List<String> snapshotFiles = changeLogVersionFinder.getSnapshotFilenames(latestVersion);
		
		this.initializeDatabase();

		log.info("Liquibase files used for creating the OpenMRS database are: {}", snapshotFiles);
		
		for (String fileName : snapshotFiles) {
			log.info("processing {}", fileName);
			this.updateDatabase(fileName);
		}
	}
	
	@Test
	void shouldCreateEnversAuditTablesWhenEnversIsEnabled() throws Exception {
		List<Class<?>> auditedEntities = getAuditedEntityClasses();
		log.info("Found {} @Audited entity classes", auditedEntities.size());
		
		try (SessionFactory sessionFactory = buildSessionFactoryWithEnvers(true, null)) {
			assertTrue(tableExists("revinfo"), "revinfo table should exist");
			List<String> missingTables = new ArrayList<>();
			for (Class<?> entityClass : auditedEntities) {
				String expectedAuditTableName = getExpectedAuditTableName(entityClass, "_audit");
				if (!tableExists(expectedAuditTableName)) {
					missingTables.add(expectedAuditTableName);
				}
			}
			assertTrue(missingTables.isEmpty(), "Missing audit tables: " + missingTables);
		}
		
		this.dropAllDatabaseObjects();
	}
	
	@Test
	void shouldNotCreateAuditTablesWhenEnversIsDisabled() throws Exception {
		try (SessionFactory sessionFactory = buildSessionFactoryWithEnvers(false, null)) {
			assertFalse(tableExists("patient_audit"), "patient_aud table should not exist");
			assertFalse(tableExists("encounter_audit"), "encounter_aud table should not exist");
			assertFalse(tableExists("concept_audit"), "concept_aud table should not exist");
		}
		this.dropAllDatabaseObjects();
	}
	
	@Test
	void shouldRespectCustomAuditTableSuffix() throws Exception {
		try (SessionFactory sessionFactory = buildSessionFactoryWithEnvers(true, "_Aaa")) {
			assertTrue(tableExists("patient_Aaa"), "patient_AUDIT table should exist");
			assertTrue(tableExists("encounter_Aaa"), "encounter_AUDIT table should exist");
			assertTrue(tableExists("concept_Aaa"), "concept_AUDIT table should exist");
			assertFalse(tableExists("patient_audit"), "patient_aud table should not exist");
		}
		this.dropAllDatabaseObjects();
	}
	
	private SessionFactory buildSessionFactoryWithEnvers(boolean enversEnabled, String customSuffix) {
		Integrator enversIntegrator = new Integrator() {
			
			@Override
			public void integrate(@UnknownKeyFor @NonNull @Initialized Metadata metadata,
			        @UnknownKeyFor @NonNull @Initialized BootstrapContext bootstrapContext,
			        @UnknownKeyFor @NonNull @Initialized SessionFactoryImplementor sessionFactory) {
				if (enversEnabled) {
					try {
						Properties properties = new java.util.Properties();
						properties.setProperty("hibernate.integration.envers.enabled", "true");
						String suffix = "_audit";
						if (customSuffix != null) {
							suffix = customSuffix;
						}
						properties.setProperty("org.hibernate.envers.audit_table_suffix", suffix);
						EnversAuditTableInitializer.initialize(metadata, properties, bootstrapContext.getServiceRegistry());
					}
					catch (Exception e) {
						throw new RuntimeException("Failed to initialize audit tables", e);
					}
				}
			}
			
			@Override
			public void disintegrate(
			        @UnknownKeyFor @NonNull @Initialized SessionFactoryImplementor sessionFactoryImplementor,
			        @UnknownKeyFor @NonNull @Initialized SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
				// No cleanup needed: the audit table initialization performed in integrate() is a one-time
				// schema operation with no resources to release when the SessionFactory is closed.
			}
		};
		
		BootstrapServiceRegistry bootstrapRegistry = new BootstrapServiceRegistryBuilder().applyIntegrator(enversIntegrator)
		        .build();
		
		Configuration configuration = new Configuration(bootstrapRegistry).configure();
		
		Set<Class<?>> entityClasses = OpenmrsClassScanner.getInstance().getClassesWithAnnotation(Entity.class);
		entityClasses.remove(OrderServiceTest.SomeTestOrder.class);
		entityClasses.remove(OpenmrsRevisionEntity.class);
		for (Class<?> clazz : entityClasses) {
			configuration.addAnnotatedClass(clazz);
		}
		configuration.setProperty(Environment.DIALECT, System.getProperty("databaseDialect"));
		configuration.setProperty(Environment.JAKARTA_JDBC_URL, CONNECTION_URL);
		configuration.setProperty(Environment.JAKARTA_JDBC_USER, USER_NAME);
		configuration.setProperty(Environment.JAKARTA_JDBC_PASSWORD, PASSWORD);
		configuration.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "false");
		configuration.setProperty(Environment.USE_QUERY_CACHE, "false");
		configuration.setProperty("hibernate.integration.envers.enabled", String.valueOf(enversEnabled));

		String suffix = "_audit";
		if (customSuffix != null) {
			suffix = customSuffix;
		}
		configuration.setProperty("org.hibernate.envers.audit_table_suffix", suffix);


		configuration.setProperty("hibernate.search.backend.type", "lucene");
		configuration.setProperty("hibernate.search.backend.analysis.configurer",
		    "class:org.openmrs.api.db.hibernate.search.lucene.LuceneConfig");
		configuration.setProperty(Environment.HBM2DDL_AUTO, "none");
		return configuration.buildSessionFactory();
	}
	
	private boolean tableExists(String tableName) throws Exception {
		try (Connection connection = getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();
			
			try (ResultSet rs = metaData.getTables(null, null, "%", new String[] { "TABLE" })) {
				while (rs.next()) {
					String existingTableName = rs.getString("TABLE_NAME");
					if (existingTableName.equalsIgnoreCase(tableName)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Gets all entity classes that are annotated with @Audited.
	 */
	private List<Class<?>> getAuditedEntityClasses() {
		Set<Class<?>> entityClasses = OpenmrsClassScanner.getInstance().getClassesWithAnnotation(Entity.class);
		List<Class<?>> auditedClasses = new ArrayList<>();
		
		for (Class<?> entityClass : entityClasses) {
			if (entityClass.equals(OrderServiceTest.SomeTestOrder.class)) {
				continue;
			}
			if (isAudited(entityClass)) {
				auditedClasses.add(entityClass);
			}
		}
		return auditedClasses;
	}
	
	/**
	 * Checks if a class or any of its superclasses is annotated with @Audited.
	 */
	private boolean isAudited(Class<?> clazz) {
		Class<?> current = clazz;
		while (current != null && current != Object.class) {
			if (current.isAnnotationPresent(Audited.class)) {
				return true;
			}
			current = current.getSuperclass();
		}
		return false;
	}
	
	/**
	 * Gets the expected audit table name for an entity class.
	 */
	private String getExpectedAuditTableName(Class<?> entityClass, String suffix) {
		Table tableAnnotation = entityClass.getAnnotation(Table.class);
		String baseTableName;
		if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
			baseTableName = tableAnnotation.name();
		} else {
			baseTableName = entityClass.getSimpleName();
		}
		return baseTableName + suffix;
	}
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.util.EnumSet;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hibernate.boot.Metadata;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.schema.TargetType;
import org.hibernate.tool.schema.spi.ExceptionHandler;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.hibernate.tool.schema.spi.SchemaManagementTool;
import org.hibernate.tool.schema.spi.SchemaMigrator;
import org.hibernate.tool.schema.spi.ScriptTargetOutput;
import org.hibernate.tool.schema.spi.TargetDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes Hibernate Envers audit tables when auditing is enabled. This class is responsible for
 * conditionally creating audit tables only when hibernate.integration.envers.enabled=true.
 */
public class EnversAuditTableInitializer {
	
	private static final Logger log = LoggerFactory.getLogger(EnversAuditTableInitializer.class);

	private EnversAuditTableInitializer() {
		
	}

	/**
	 * Checks if Envers is enabled and creates/updates audit tables as needed. This will Create or
	 * Update audit tables if they don't exist - Update existing audit tables if the schema has
	 * changed
	 * 
	 * @param metadata Hibernate metadata containing entity mappings
	 * @param hibernateProperties properties containing Envers configuration
	 * @param serviceRegistry Hibernate service registry
	 */
	public static void initialize(Metadata metadata, Properties hibernateProperties,
								  ServiceRegistry serviceRegistry) {
		
		if (!isEnversEnabled(hibernateProperties)) {
			log.debug("Hibernate Envers is not enabled. Skipping audit table initialization.");
			return;
		}
		
		updateAuditTables(metadata, hibernateProperties, serviceRegistry);
	}
	
	/**
	 * Checks if Hibernate Envers is enabled in the configuration.
	 * 
	 * @param properties Hibernate properties
	 * @return true if Envers is enabled, false otherwise
	 */
	private static boolean isEnversEnabled(Properties properties) {
		String enversEnabled = properties.getProperty("hibernate.integration.envers.enabled");
		return "true".equalsIgnoreCase(enversEnabled);
	}
	
	/**
	 * Creates or updates audit tables using Hibernate's {@link SchemaMigrator}. This method filters
	 * to only process audit tables.
	 * 
	 * @param metadata Hibernate metadata containing entity mappings (includes Envers audit
	 *            entities)
	 * @param hibernateProperties Hibernate configuration properties
	 * @param serviceRegistry Hibernate service registry
     */
	private static void updateAuditTables(Metadata metadata, Properties hibernateProperties,
	        ServiceRegistry serviceRegistry) {
		String auditTablePrefix = hibernateProperties.getProperty("org.hibernate.envers.audit_table_prefix", "");
		String auditTableSuffix = hibernateProperties.getProperty("org.hibernate.envers.audit_table_suffix", "_audit");

		Map<String, Object> settings = (Map) hibernateProperties;
		AtomicBoolean hasErrors = new AtomicBoolean(false);
		ExecutionOptions executionOptions = getExecutionOptions(settings, hasErrors);
		SchemaMigrator schemaMigrator = serviceRegistry.getService(SchemaManagementTool.class).getSchemaMigrator(settings);

		TargetDescriptor targetDescriptor = getTargetDescriptor();

		schemaMigrator.doMigration(metadata, executionOptions, contributed -> {
			String tableName = contributed.getExportIdentifier();
			if (tableName == null) {
				return false;
			}

			String lowerTableName = tableName.toLowerCase();

			if (lowerTableName.contains("revision") || lowerTableName.equals("revinfo")) {
				return true;
			}

			String lowerPrefix = auditTablePrefix.toLowerCase();
			String lowerSuffix = auditTableSuffix.toLowerCase();

			boolean hasPrefix = lowerPrefix.isEmpty() || lowerTableName.startsWith(lowerPrefix);
			boolean hasSuffix = lowerSuffix.isEmpty() || lowerTableName.endsWith(lowerSuffix);

			return hasPrefix && hasSuffix;
		}, targetDescriptor);

		if (hasErrors.get()) {
			log.warn("Envers audit table migration completed with errors.");
		} else {
			log.info("Successfully created/updated Envers audit tables using Hibernate SchemaManagementTool.");
		}
	}

	private static TargetDescriptor getTargetDescriptor() {
		return new TargetDescriptor() {
			@Override
			public EnumSet<TargetType> getTargetTypes() {
				return EnumSet.of(TargetType.DATABASE);
			}

			@Override
			public ScriptTargetOutput getScriptTargetOutput() {
				return null;
			}
		};
	}

	private static ExecutionOptions getExecutionOptions(Map<String, Object> settings, AtomicBoolean hasErrors) {
		return new ExecutionOptions() {
			@Override
			public Map<String, Object> getConfigurationValues() {
				return settings;
			}

			@Override
			public boolean shouldManageNamespaces() {
				return false;
			}

			@Override
			public ExceptionHandler getExceptionHandler() {
				return throwable -> {
					hasErrors.set(true);
					log.warn("Schema migration encountered an issue: {}", throwable.getMessage());
				};
			}
		};
	}
}

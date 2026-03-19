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

import java.util.Collections;
import java.util.Properties;

import org.hibernate.boot.Metadata;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.schema.spi.SchemaManagementTool;
import org.hibernate.tool.schema.spi.SchemaMigrator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EnversAuditTableInitializer}, focused on the audit table schema creation
 * behaviour. Backfill tests are in
 * {@link org.openmrs.util.databasechange.BackfillEnversAuditTablesChangesetTest}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EnversAuditTableInitializerTest {

	@Mock
	private Metadata metadata;

	@Mock
	private ServiceRegistry serviceRegistry;

	@Mock
	private SchemaManagementTool schemaManagementTool;

	@Mock
	private SchemaMigrator schemaMigrator;

	@Test
	void initialize_shouldDoNothingWhenEnversIsDisabled() {
		Properties disabledProps = new Properties();
		disabledProps.setProperty("hibernate.integration.envers.enabled", "false");

		EnversAuditTableInitializer.initialize(metadata, disabledProps, serviceRegistry);

		verifyNoInteractions(serviceRegistry);
	}

	@Test
	void initialize_shouldCompleteWithoutErrorWhenNoAuditedEntitiesAreFound() {
		when(serviceRegistry.getService(SchemaManagementTool.class)).thenReturn(schemaManagementTool);
		when(schemaManagementTool.getSchemaMigrator(any())).thenReturn(schemaMigrator);
		when(metadata.getEntityBindings()).thenReturn(Collections.emptyList());

		Properties props = new Properties();
		props.setProperty("hibernate.integration.envers.enabled", "true");
		props.setProperty("org.hibernate.envers.audit_table_suffix", "_audit");

		EnversAuditTableInitializer.initialize(metadata, props, serviceRegistry);
	}
}

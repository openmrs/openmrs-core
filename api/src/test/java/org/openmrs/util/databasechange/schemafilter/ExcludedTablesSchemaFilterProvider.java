/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util.databasechange.schemafilter;

import org.hibernate.tool.schema.internal.DefaultSchemaFilter;
import org.hibernate.tool.schema.spi.SchemaFilter;
import org.hibernate.tool.schema.spi.SchemaFilterProvider;

/**
 * Provides schema filters for database operations, specifically excluding certain tables from validation.
 * <p>This implementation uses the {@link ExcludedTablesSchemaFilter} for validation to exclude certain tables, while using
 * the default filter for other operations like creation, dropping, truncating, and migration.
 */
public class ExcludedTablesSchemaFilterProvider implements SchemaFilterProvider {
	
	@Override
	public SchemaFilter getCreateFilter() {
		return DefaultSchemaFilter.INSTANCE;
	}
	
	@Override
	public SchemaFilter getDropFilter() {
		return DefaultSchemaFilter.INSTANCE;
	}
	
	@Override
	public SchemaFilter getTruncatorFilter() {
		return DefaultSchemaFilter.INSTANCE;
	}
	
	@Override
	public SchemaFilter getMigrateFilter() {
		return DefaultSchemaFilter.INSTANCE;
	}
	
	@Override
	public SchemaFilter getValidateFilter() {
		return ExcludedTablesSchemaFilter.INSTANCE;
	}
}

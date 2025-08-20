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

import java.util.Set;

import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.model.relational.Sequence;
import org.hibernate.mapping.Table;
import org.hibernate.tool.schema.spi.SchemaFilter;

/**
 * Custom {@link SchemaFilter} implementation that excludes certain tables from schema validation.
 * <p>This addresses schema validation failures where entity classes inherit fields from base classes
 * but the corresponding database tables don't contain all inherited columns.
 * <p><strong>Note:</strong> This is a temporary workaround until the schema/mapping mismatches are resolved.
 */
public class ExcludedTablesSchemaFilter implements SchemaFilter {
	
	public static final ExcludedTablesSchemaFilter INSTANCE = new ExcludedTablesSchemaFilter();
	
	// tables to be excluded from validation
	private static final Set<String> EXCLUDED_TABLES = Set.of("drug");
	
	@Override
	public boolean includeNamespace(Namespace namespace) {
		return true;
	}
	
	@Override
	public boolean includeTable(Table table) {
		return !EXCLUDED_TABLES.contains(table.getName());
	}
	
	@Override
	public boolean includeSequence(Sequence sequence) {
		return true;
	}
}

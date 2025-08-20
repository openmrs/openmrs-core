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

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package liquibase.ext.sqlgenerator.core;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;

@SuppressWarnings("unused")
public class ModifyColumnGenerator extends AbstractSqlGenerator<ModifyColumnStatement> {
	
	private static final Logger log = LoggerFactory.getLogger(ModifyColumnGenerator.class);
	
	@Override
	public Warnings warn(ModifyColumnStatement modifyColumnStatement, Database database,
		SqlGeneratorChain sqlGeneratorChain) {
		return new Warnings();
	}
	
	public ValidationErrors validate(ModifyColumnStatement statement, Database database,
		SqlGeneratorChain sqlGeneratorChain) {
		
		ValidationErrors validationErrors = new ValidationErrors();
		validationErrors.checkRequiredField("tableName", statement.getTableName());
		validationErrors.checkRequiredField("columns", statement.getColumns());
		
		for (ColumnConfig column : statement.getColumns()) {
			if (column.getConstraints() != null
				&& Boolean.TRUE.equals(column.getConstraints().isPrimaryKey())
				&& (database instanceof H2Database
				|| database instanceof DB2Database
				|| database instanceof DerbyDatabase
				|| database instanceof SQLiteDatabase)) {
				
				validationErrors.addError(
					"Adding primary key columns is not supported on " + database.getShortName());
			}
		}
		
		return validationErrors;
	}
	
	@Override
	public Sql[] generateSql(ModifyColumnStatement statement, Database database,
		SqlGeneratorChain sqlGeneratorChain) {
		
		List<Sql> sql = new ArrayList<>();
		
		for (ColumnConfig column : statement.getColumns()) {
			
			StringBuilder alterTable = new StringBuilder();
			
			alterTable.append("ALTER TABLE ")
				.append(database.escapeTableName(null,
					statement.getSchemaName(),
					statement.getTableName()));
			
			alterTable.append(" ")
				.append(getModifyString(database))
				.append(" ");
			
			alterTable.append(database.escapeColumnName(null,
				statement.getSchemaName(),
				statement.getTableName(),
				column.getName()));
			
			alterTable.append(getPreDataTypeString(database));
			
			alterTable.append(DataTypeFactory.getInstance()
				.fromDescription(column.getType(), database)
				.toDatabaseDataType(database));
			
			if (supportsExtraMetaData(database)) {
				appendExtraMetadata(alterTable, column, database);
			}
			
			alterTable.append(getPostDataTypeString(database));
			
			sql.add(new UnparsedSql(alterTable.toString()));
		}
		
		log.debug("generated sql is '{}'", sql);
		
		return sql.toArray(new Sql[0]);
	}
	
	private void appendExtraMetadata(StringBuilder alterTable, ColumnConfig column, Database database) {
		
		appendNullableClause(alterTable, column, database);
		
		alterTable.append(getDefaultClause(column, database));
		
		appendAutoIncrementClause(alterTable, column, database);
		
		appendPrimaryKeyClause(alterTable, column);
	}
	
	private void appendNullableClause(StringBuilder alterTable, ColumnConfig column, Database database) {
		
		if (column.getConstraints() != null && !column.getConstraints().isNullable()) {
			alterTable.append(" NOT NULL");
			return;
		}
		
		if (database instanceof SybaseDatabase || database instanceof SybaseASADatabase) {
			alterTable.append(" NULL");
		}
	}
	
	private void appendAutoIncrementClause(StringBuilder alterTable, ColumnConfig column, Database database) {
		
		if (Boolean.TRUE.equals(column.isAutoIncrement())) {
			alterTable.append(" ")
				.append(database.getAutoIncrementClause(BigInteger.ONE, BigInteger.ONE, null, null));
		}
	}
	
	private void appendPrimaryKeyClause(StringBuilder alterTable, ColumnConfig column) {
		
		if (column.getConstraints() != null
			&& Boolean.TRUE.equals(column.getConstraints().isPrimaryKey())) {
			
			alterTable.append(" PRIMARY KEY");
		}
	}
	
	boolean supportsExtraMetaData(Database database) {
		return database instanceof MSSQLDatabase || database instanceof MySQLDatabase;
	}
	
	String getModifyString(Database database) {
		
		if (database instanceof HsqlDatabase
			|| database instanceof H2Database
			|| database instanceof DerbyDatabase
			|| database instanceof DB2Database
			|| database instanceof MSSQLDatabase) {
			
			return "ALTER COLUMN";
			
		} else if (database instanceof SybaseASADatabase
			|| database instanceof SybaseDatabase
			|| database instanceof MySQLDatabase) {
			
			return "MODIFY";
			
		} else if (database instanceof OracleDatabase) {
			
			return "MODIFY (";
			
		} else {
			
			return "ALTER COLUMN";
		}
	}
	
	String getPreDataTypeString(Database database) {
		
		if (database instanceof DerbyDatabase || database instanceof DB2Database) {
			return " SET DATA TYPE ";
			
		} else if (database instanceof SybaseASADatabase
			|| database instanceof SybaseDatabase
			|| database instanceof MSSQLDatabase
			|| database instanceof MySQLDatabase
			|| database instanceof HsqlDatabase
			|| database instanceof H2Database
			|| database instanceof OracleDatabase) {
			
			return " ";
			
		} else {
			return " TYPE ";
		}
	}
	
	String getPostDataTypeString(Database database) {
		
		if (database instanceof OracleDatabase) {
			return " )";
		}
		return "";
	}
	
	String getDefaultClause(ColumnConfig column, Database database) {
		
		String clause = "";
		String defaultValue = column.getDefaultValue();
		
		if (defaultValue != null && database instanceof MySQLDatabase) {
			
			clause += " DEFAULT "
				+ DataTypeFactory.getInstance()
				.fromObject(defaultValue, database)
				.objectToSql(defaultValue, database);
		}
		
		return clause;
	}
}

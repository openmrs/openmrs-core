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

/**
 * Generates the SQL code to implement a modifyColumn change.
 *
 * @since 2.4
 */
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
			if (column.getConstraints() != null && column.getConstraints().isPrimaryKey() != null && column.getConstraints()
				.isPrimaryKey() && (database instanceof H2Database
				|| database instanceof DB2Database
				|| database instanceof DerbyDatabase
				|| database instanceof SQLiteDatabase)) {
				validationErrors.addError("Adding primary key columns is not supported on " + database.getShortName());
			}
		}

		return validationErrors;
	}

	public Sql[] generateSql(ModifyColumnStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
		List<Sql> sql = new ArrayList<>();
		for (ColumnConfig column : statement.getColumns()) {
			String alterTable =
				"ALTER TABLE " + database.escapeTableName(null, statement.getSchemaName(), statement.getTableName());

			// add "MODIFY"
			alterTable += " " + getModifyString(database) + " ";

			// add column name
			alterTable += database
				.escapeColumnName(null, statement.getSchemaName(), statement.getTableName(), column.getName());

			alterTable += getPreDataTypeString(database); // adds a space if nothing else

			// add column type
			alterTable += DataTypeFactory.getInstance().fromDescription(column.getType(), database)
				.toDatabaseDataType(database);

			if (supportsExtraMetaData(database)) {
				if (column.getConstraints() != null && !column.getConstraints().isNullable()) {
					alterTable += " NOT NULL";
				} else {
					if (database instanceof SybaseDatabase || database instanceof SybaseASADatabase) {
						alterTable += " NULL";
					}
				}

				alterTable += getDefaultClause(column, database);

				if (column.isAutoIncrement() != null && column.isAutoIncrement()) {
					alterTable += " " + database.getAutoIncrementClause(BigInteger.ONE, BigInteger.ONE, null, null);
				}

				if (column.getConstraints() != null && column.getConstraints().isPrimaryKey() != null && column
					.getConstraints().isPrimaryKey()) {
					alterTable += " PRIMARY KEY";
				}
			}

			alterTable += getPostDataTypeString(database);

			sql.add(new UnparsedSql(alterTable));
		}
		
		log.debug("generated sql is '{}'", sql);

		return sql.toArray(new Sql[0]);
	}

	/**
	 * Whether the ALTER command can take things like "DEFAULT VALUE" or "PRIMARY KEY" as well as type changes
	 *
	 * @param database
	 * @return true/false whether extra information can be included
	 */
	boolean supportsExtraMetaData(Database database) {
		return database instanceof MSSQLDatabase || database instanceof MySQLDatabase;
	}

	/**
	 * @return either "MODIFY" or "ALTER COLUMN" depending on the current db
	 */
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

	/**
	 * @return the string that comes before the column type
	 * definition (like 'set data type' for derby or an open parentheses for Oracle)
	 */
	String getPreDataTypeString(Database database) {
		if (database instanceof DerbyDatabase
			|| database instanceof DB2Database) {
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

	/**
	 * @return the string that comes after the column type definition (like a close parentheses for Oracle)
	 */
	String getPostDataTypeString(Database database) {
		if (database instanceof OracleDatabase) {
			return " )";
		} else {
			return "";
		}
	}

	String getDefaultClause(ColumnConfig column, Database database) {
		String clause = "";
		String defaultValue = column.getDefaultValue();
		if (defaultValue != null && database instanceof MySQLDatabase) {
			clause += " DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database)
				.objectToSql(defaultValue, database);
		}
		return clause;
	}

}

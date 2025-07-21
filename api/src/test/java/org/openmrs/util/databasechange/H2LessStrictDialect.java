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

import org.hibernate.dialect.H2Dialect;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.spi.JdbcTypeRegistry;

import static java.sql.Types.BIGINT;
import static java.sql.Types.CHAR;
import static java.sql.Types.CLOB;
import static java.sql.Types.DATE;
import static java.sql.Types.INTEGER;
import static java.sql.Types.LONGVARCHAR;
import static java.sql.Types.TIMESTAMP;
import static java.sql.Types.VARCHAR;

/**
 * H2 version we use behaves differently from H2Dialect bundled with Hibernate so we provide a
 * custom implementation for the purpose of validation in {@link DatabaseUpgradeTestUtil}
 */
public class H2LessStrictDialect extends H2Dialect {

	@Override
	public JdbcType resolveSqlTypeDescriptor(String columnTypeName, int jdbcTypeCode, int precision, int scale, JdbcTypeRegistry jdbcTypeRegistry) {
		switch (jdbcTypeCode) {

			// H2Dialect incorrectly sets these to synonyms in H2
			case INTEGER:
				return super.resolveSqlTypeDescriptor(
					"BIGINT",
					BIGINT,
					precision,
					scale,
					jdbcTypeRegistry);

			// Liquibase incorrectly creates varchar for clob in H2 so we just tell Hibernate it's ok
			case VARCHAR:
				return super.resolveSqlTypeDescriptor(
					"CLOB",
					CLOB,
					precision,
					scale,
					jdbcTypeRegistry);

			//person.birthdate is not a timestamp, but date in db
			case DATE:
				return super.resolveSqlTypeDescriptor(
					"TIMESTAMP",
					TIMESTAMP,
					precision,
					scale,
					jdbcTypeRegistry);

			// UUIDs are created as char(38), but H2Dialect maps them to varchars
			case CHAR:
				if (precision == 38) {
					return super.resolveSqlTypeDescriptor(
						"VARCHAR",
						VARCHAR,
						precision,
						scale,
						jdbcTypeRegistry);
				}
				break;

			// These mappings are required for "long" fields of type java.lang.String that are declared as 'text' 
			// in Hibernate change sets.
			case CLOB:
				if (precision == Integer.MAX_VALUE) {
					return super.resolveSqlTypeDescriptor(
						"LONGVARCHAR",
						LONGVARCHAR,
						precision,
						scale,
						jdbcTypeRegistry);
				}
				break;
				
				default:
					break;
		}

		return super.resolveSqlTypeDescriptor(
			columnTypeName,
			jdbcTypeCode,
			precision,
			scale,
			jdbcTypeRegistry
		);
	}
}

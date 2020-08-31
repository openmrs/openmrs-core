/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package liquibase.ext.change.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.UUID;

import liquibase.change.ColumnConfig;
import liquibase.database.core.MySQLDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.structure.core.Column;
import org.junit.jupiter.api.Test;

public class InsertWithUuidDataChangeTest {
	@Test
	public void shouldNotModifyUuidValueIfColumnExists() {
		InsertWithUuidDataChange dataChange = new InsertWithUuidDataChange();
		String expected = "expected_value_for_UUID";

		ColumnConfig uuid = new ColumnConfig( new Column("uuid") );
		uuid.setValue( expected );
		dataChange.addColumn( uuid );

		SqlStatement[] statements = dataChange.generateStatements( new MySQLDatabase() );
		InsertStatement insertStatement = ( InsertStatement ) statements[0];
		Object actual = insertStatement.getColumnValue( "uuid" );

		assertEquals( expected, actual );
	}
	
	@Test
	public void shouldAddUuid() {
		InsertWithUuidDataChange dataChange = new InsertWithUuidDataChange();

		SqlStatement[] statements = dataChange.generateStatements( new MySQLDatabase() );
		InsertStatement insertStatement = ( InsertStatement ) statements[0];
		Object actual = insertStatement.getColumnValue( "uuid" );

		try {
			UUID.fromString( (String) actual );
		} catch (RuntimeException re) {
			fail("generated uuid is not valid");
		}
	}
}

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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import liquibase.change.ColumnConfig;
import liquibase.database.core.MySQLDatabase;
import liquibase.statement.SqlStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;
import org.junit.jupiter.api.Test;

public class ModifyColumnChangeTest {
	
	@Test
	public void shouldGenerateStatements() {
		ModifyColumnChange change = new ModifyColumnChange();
		change.setSchemaName("openmrs");
		change.setTableName("some_table");
		change.setColumns(
		    Arrays.asList(new ColumnConfig(new Column("column_one")), new ColumnConfig(new Column("column_two"))));
		
		SqlStatement[] statements = change.generateStatements(new MySQLDatabase());
		ModifyColumnStatement actual = (ModifyColumnStatement) statements[0];
		
		assertEquals("openmrs", actual.getSchemaName());
		assertEquals("some_table", actual.getTableName());
		assertEquals("column_one", actual.getColumns()[0].getName());
		assertEquals("column_two", actual.getColumns()[1].getName());
	}
	
	@Test
	public void shouldGetConfirmationMessage() {
		ModifyColumnChange change = new ModifyColumnChange();
		
		change.setSchemaName("openmrs");
		change.setTableName("some_table");
		
		Column columnOne = new Column("column_one");
		columnOne.setType(new DataType("type_one"));
		
		Column columnTwo = new Column("column_two");
		columnTwo.setType(new DataType("type_two"));
		
		change.setColumns(Arrays.asList(new ColumnConfig(columnOne), new ColumnConfig(columnTwo)));
		
		String actual = change.getConfirmationMessage();
		String expected = "Columns column_one(type_one),column_two(type_two) of some_table modified";
		
		assertEquals( expected, actual );
	}
}

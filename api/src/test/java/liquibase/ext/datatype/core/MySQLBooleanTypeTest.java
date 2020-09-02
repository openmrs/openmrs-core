/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package liquibase.ext.datatype.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Set;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.AbstractDb2Database;
import liquibase.database.core.MariaDBDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.UnsupportedDatabase;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.core.BooleanType;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

public class MySQLBooleanTypeTest {
	
	@Test
	public void shouldReturnTinyIntForMySQLDatabase() {
		MySQLBooleanType dataType = new MySQLBooleanType();
		DatabaseDataType actual = dataType.toDatabaseDataType(new MySQLDatabase());
		
		String expected = "TINYINT(1)";
		assertEquals(expected, actual.getType());
	}
	
	@Test
	public void shouldDelegateToLiquibaseDatabases()
	        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		BooleanType booleanType = new BooleanType();
		
		Reflections reflections = new Reflections("liquibase.database", new SubTypesScanner());
		Set<Class<? extends Database>> databaseClasses = reflections.getSubTypesOf(Database.class);
		
		databaseClasses.remove(AbstractDb2Database.class);
		databaseClasses.remove(AbstractJdbcDatabase.class);
		databaseClasses.remove(MariaDBDatabase.class);
		databaseClasses.remove(MySQLDatabase.class);
		databaseClasses.remove(UnsupportedDatabase.class);
		
		for (Class<? extends Database> clazz : new ArrayList<>(databaseClasses)) {
			Constructor<?> clazzConstructor = clazz.getConstructor();
			Database database = (Database) clazzConstructor.newInstance();
			DatabaseDataType expected = booleanType.toDatabaseDataType(database);
			
			MySQLBooleanType dataType = new MySQLBooleanType();
			DatabaseDataType actual = dataType.toDatabaseDataType(database);
			
			assertEquals(expected.getType(), actual.getType());
		}
	}
	
	@Test
	public void shouldHaveHigherPriority() {
		int priorityOne = new MySQLBooleanType().getPriority();
		int priorityTwo = new BooleanType().getPriority();
		
		assertTrue(priorityOne > priorityTwo);
	}
}

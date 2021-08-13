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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Set;

import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.AbstractDb2Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MariaDBDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.database.core.UnsupportedDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.structure.core.Column;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

public class ModifyColumnGeneratorTest {
	@Test
	public void shouldGenerateSql() {
		ModifyColumnGenerator generator = new ModifyColumnGenerator();
		
		ColumnConfig config = new ColumnConfig(new Column("column_name"));
		config.setConstraints(new ConstraintsConfig().setPrimaryKey(true).setNullable(false));
		config.setType("int");
		
		ModifyColumnStatement statement = new ModifyColumnStatement("some_schema", "some_table", config);
		Sql[] sql = generator.generateSql(statement, new MySQLDatabase(), null);
		
		String actual = sql[0].toSql();
		String expected = "ALTER TABLE some_schema.some_table MODIFY column_name INT NOT NULL PRIMARY KEY";
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void shouldGetDefaultClause()
	        throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
		ModifyColumnGenerator generator = new ModifyColumnGenerator();
		ColumnConfig config = new ColumnConfig(new Column("column_name"));
		config.setDefaultValue("default_value");
		
		assertEquals(" DEFAULT 'default_value'", generator.getDefaultClause(config, new MySQLDatabase()));
		assertEquals(" DEFAULT 'default_value'", generator.getDefaultClause(config, new MariaDBDatabase()));
		
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
			config = new ColumnConfig(new Column("column_name"));
			
			assertEquals("", generator.getDefaultClause(config, database));
		}
	}
	
	@Test
	public void shouldGetModifyString()
	        throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
		ModifyColumnGenerator generator = new ModifyColumnGenerator();
		
		assertEquals("ALTER COLUMN", generator.getModifyString(new DB2Database()));
		assertEquals("ALTER COLUMN", generator.getModifyString(new DerbyDatabase()));
		assertEquals("ALTER COLUMN", generator.getModifyString(new H2Database()));
		assertEquals("ALTER COLUMN", generator.getModifyString(new HsqlDatabase()));
		assertEquals("ALTER COLUMN", generator.getModifyString(new MSSQLDatabase()));
		
		assertEquals("MODIFY", generator.getModifyString(new MariaDBDatabase()));
		assertEquals("MODIFY", generator.getModifyString(new MySQLDatabase()));
		assertEquals("MODIFY (", generator.getModifyString(new OracleDatabase()));
		assertEquals("MODIFY", generator.getModifyString(new SybaseASADatabase()));
		assertEquals("MODIFY", generator.getModifyString(new SybaseDatabase()));
		
		Reflections reflections = new Reflections("liquibase.database", new SubTypesScanner());
		Set<Class<? extends Database>> databaseClasses = reflections.getSubTypesOf(Database.class);
		
		databaseClasses.remove(AbstractDb2Database.class);
		databaseClasses.remove(AbstractJdbcDatabase.class);
		
		databaseClasses.remove(DB2Database.class);
		databaseClasses.remove(DerbyDatabase.class);
		databaseClasses.remove(H2Database.class);
		databaseClasses.remove(HsqlDatabase.class);
		databaseClasses.remove(MariaDBDatabase.class);
		
		databaseClasses.remove(MSSQLDatabase.class);
		databaseClasses.remove(MySQLDatabase.class);
		databaseClasses.remove(OracleDatabase.class);
		databaseClasses.remove(SybaseASADatabase.class);
		databaseClasses.remove(SybaseDatabase.class);
		
		databaseClasses.remove(UnsupportedDatabase.class);
		
		for (Class<? extends Database> clazz : new ArrayList<>(databaseClasses)) {
			Constructor<?> clazzConstructor = clazz.getConstructor();
			Database database = (Database) clazzConstructor.newInstance();
			
			assertEquals("ALTER COLUMN", generator.getModifyString(database));
		}
	}
	
	@Test
	public void shouldGetPostDataTypeString()
	        throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
		ModifyColumnGenerator generator = new ModifyColumnGenerator();
		
		assertEquals(" )", generator.getPostDataTypeString(new OracleDatabase()));
		
		Reflections reflections = new Reflections("liquibase.database", new SubTypesScanner());
		Set<Class<? extends Database>> databaseClasses = reflections.getSubTypesOf(Database.class);
		
		databaseClasses.remove(AbstractDb2Database.class);
		databaseClasses.remove(AbstractJdbcDatabase.class);
		databaseClasses.remove(OracleDatabase.class);
		databaseClasses.remove(UnsupportedDatabase.class);
		
		for (Class<? extends Database> clazz : new ArrayList<>(databaseClasses)) {
			Constructor<?> clazzConstructor = clazz.getConstructor();
			Database database = (Database) clazzConstructor.newInstance();
			
			assertEquals("", generator.getPostDataTypeString(database));
		}
	}
	
	@Test
	public void shouldGetPreDataTypeString()
	        throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
		ModifyColumnGenerator generator = new ModifyColumnGenerator();
		
		assertEquals(" SET DATA TYPE ", generator.getPreDataTypeString(new DB2Database()));
		assertEquals(" SET DATA TYPE ", generator.getPreDataTypeString(new DerbyDatabase()));
		
		assertEquals(" ", generator.getPreDataTypeString(new H2Database()));
		assertEquals(" ", generator.getPreDataTypeString(new HsqlDatabase()));
		assertEquals(" ", generator.getPreDataTypeString(new MSSQLDatabase()));
		assertEquals(" ", generator.getPreDataTypeString(new MariaDBDatabase()));
		assertEquals(" ", generator.getPreDataTypeString(new MySQLDatabase()));
		assertEquals(" ", generator.getPreDataTypeString(new OracleDatabase()));
		assertEquals(" ", generator.getPreDataTypeString(new SybaseASADatabase()));
		assertEquals(" ", generator.getPreDataTypeString(new SybaseDatabase()));
		
		Reflections reflections = new Reflections("liquibase.database", new SubTypesScanner());
		Set<Class<? extends Database>> databaseClasses = reflections.getSubTypesOf(Database.class);
		
		databaseClasses.remove(AbstractDb2Database.class);
		databaseClasses.remove(AbstractJdbcDatabase.class);
		
		databaseClasses.remove(DB2Database.class);
		databaseClasses.remove(DerbyDatabase.class);
		databaseClasses.remove(H2Database.class);
		databaseClasses.remove(HsqlDatabase.class);
		databaseClasses.remove(MariaDBDatabase.class);
		
		databaseClasses.remove(MSSQLDatabase.class);
		databaseClasses.remove(MySQLDatabase.class);
		databaseClasses.remove(OracleDatabase.class);
		databaseClasses.remove(SybaseASADatabase.class);
		databaseClasses.remove(SybaseDatabase.class);
		
		databaseClasses.remove(UnsupportedDatabase.class);
		
		for (Class<? extends Database> clazz : new ArrayList<>(databaseClasses)) {
			Constructor<?> clazzConstructor = clazz.getConstructor();
			Database database = (Database) clazzConstructor.newInstance();
			
			assertEquals(" TYPE ", generator.getPreDataTypeString(database));
		}
	}
	
	@Test
	public void shouldRejectMissingTableNameAndEmptyColumns() {
		ModifyColumnGenerator generator = new ModifyColumnGenerator();
		ModifyColumnStatement statement = new ModifyColumnStatement(null, null);
		ValidationErrors errors = generator.validate(statement, new MySQLDatabase(), null);
		
		String actual = errors.toString();
		String expected = "tableName is required; No columns defined";
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void shouldRejectAddingAPrimaryKeyColumnToH2Database() {
		shouldRejectAddingAPrimaryKeyColumn(new DB2Database(), "Adding primary key columns is not supported on db2");
		shouldRejectAddingAPrimaryKeyColumn(new DerbyDatabase(), "Adding primary key columns is not supported on derby");
		shouldRejectAddingAPrimaryKeyColumn(new H2Database(), "Adding primary key columns is not supported on h2");
		shouldRejectAddingAPrimaryKeyColumn(new SQLiteDatabase(), "Adding primary key columns is not supported on sqlite");
	}
	
	private void shouldRejectAddingAPrimaryKeyColumn(Database database, String expected) {
		ModifyColumnGenerator generator = new ModifyColumnGenerator();
		
		ColumnConfig config = new ColumnConfig(new Column("column_name"));
		config.setConstraints(new ConstraintsConfig().setPrimaryKey(true).setNullable(false));
		
		ModifyColumnStatement statement = new ModifyColumnStatement("some_schema", "some_table", config);
		ValidationErrors errors = generator.validate(statement, database, null);
		
		String actual = errors.toString();
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void shouldSupportExtraMetaData()
	        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		ModifyColumnGenerator generator = new ModifyColumnGenerator();
		
		assertTrue(generator.supportsExtraMetaData(new MariaDBDatabase()));
		assertTrue(generator.supportsExtraMetaData(new MSSQLDatabase()));
		assertTrue(generator.supportsExtraMetaData(new MySQLDatabase()));
		
		Reflections reflections = new Reflections("liquibase.database", new SubTypesScanner());
		Set<Class<? extends Database>> databaseClasses = reflections.getSubTypesOf(Database.class);
		
		databaseClasses.remove(AbstractDb2Database.class);
		databaseClasses.remove(AbstractJdbcDatabase.class);
		databaseClasses.remove(MariaDBDatabase.class);
		databaseClasses.remove(MSSQLDatabase.class);
		databaseClasses.remove(MySQLDatabase.class);
		databaseClasses.remove(UnsupportedDatabase.class);
		
		for (Class<? extends Database> clazz : new ArrayList<>(databaseClasses)) {
			Constructor<?> clazzConstructor = clazz.getConstructor();
			Database database = (Database) clazzConstructor.newInstance();
			
			assertFalse(generator.supportsExtraMetaData(database));
		}
	}
}

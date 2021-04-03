/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang3.time.DateUtils;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

/**
 * Methods use by the Openmrs tests
 */
public class TestUtil {
	
	/**
	 * Additional assert method for testing that will test that two Collections have equal contents
	 * The Collections must be of equal size, and each object from one Collection must equal an
	 * object in the other Collection Order is not considered.
	 * 
	 * @param expected
	 * @param actual
	 */
	public static void assertCollectionContentsEquals(Collection<?> expected, Collection<?> actual) throws AssertionError {
		try {
			if (!expected.containsAll(actual) || !actual.containsAll(expected)) {
				throw new AssertionError("Expected " + expected + " but found " + actual);
			}
		}
		catch (Exception e) {
			throw new AssertionError(e);
		}
	}
	
	/**
	 * Mimics org.openmrs.web.Listener.getRuntimeProperties()
	 * 
	 * @param webappName name to use when looking up the runtime properties env var or filename
	 * @return Properties runtime
	 */
	public static Properties getRuntimeProperties(String webappName) {
		
		Properties props = new Properties();
		
		try {
			FileInputStream propertyStream = null;
			
			// Look for environment variable
			// {WEBAPP.NAME}_RUNTIME_PROPERTIES_FILE
			String env = webappName.toUpperCase() + "_RUNTIME_PROPERTIES_FILE";
			
			String filepath = System.getenv(env);
			
			if (filepath != null) {
				try {
					propertyStream = new FileInputStream(filepath);
				}
				catch (IOException e) {}
			}
			
			// env is the name of the file to look for in the directories
			String filename = webappName + "-runtime.properties";
			
			if (propertyStream == null) {
				filepath = OpenmrsUtil.getApplicationDataDirectory() + filename;
				try {
					propertyStream = new FileInputStream(filepath);
				}
				catch (IOException e) {}
			}
			
			// look in current directory last
			if (propertyStream == null) {
				filepath = filename;
				try {
					propertyStream = new FileInputStream(filepath);
				}
				catch (IOException e) {}
			}
			
			if (propertyStream == null)
				throw new IOException("Could not open '" + filename + "' in user or local directory.");
			OpenmrsUtil.loadProperties(props, propertyStream);
			propertyStream.close();
			
		}
		catch (IOException e) {}
		
		return props;
	}
	
	/**
	 * Convert the given xml output to a string that is assignable to a single String variable
	 * 
	 * @param output multi line string to convert
	 */
	public static void printAssignableToSingleString(String output) {
		output = output.replace("\n", "\\n");
		output = output.replace("\"", "\\\"");
		System.out.println(output);
	}
	
	/**
	 * Convert the given multi-line output to lines of StringBuilder.append lines <br>
	 * <br>
	 * From an input of this this:
	 * 
	 * <pre>
	 * asdf
	 *  asdfasdf
	 * asdf"asdf"
	 * </pre>
	 * 
	 * To this:<br>
	 * <br>
	 * StringBuilder correctOutput = new StringBuilder();<br>
	 * correctOutput.append("asdf\n");<br>
	 * correctOutput.append(" asdfasdf\n");<br>
	 * correctOutput.append("asdf\"asdf\"\n");<br>
	 * 
	 * @param output multi line string to convert
	 */
	public static void printStringBuilderOutput(String output) {
		output = output.replace("\"", "\\\"");
		String[] lines = output.split("\n");
		
		System.out.println("StringBuilder correctOutput = new StringBuilder();");
		for (String line : lines) {
			System.out.print("correctOutput.append(\"");
			System.out.print(line);
			System.out.println("\\n\");");
		}
		
	}
	
	/**
	 * Print the contents of the given tableName to system.out<br>
	 * <br>
	 * Call this from any {@link BaseContextSensitiveTest} child by:
	 * TestUtil.printOutTableContents(getConnection(), "encounter");
	 * 
	 * @param sqlConnection the connection to use
	 * @param tableNames the name(s) of the table(s) to print out
	 * @throws Exception
	 */
	public static void printOutTableContents(Connection sqlConnection, String... tableNames) throws Exception {
		for (String tableName : tableNames) {
			System.out.println("The contents of table: " + tableName);
			IDatabaseConnection connection = new DatabaseConnection(sqlConnection);
			DatabaseConfig config = connection.getConfig();
			config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());
			QueryDataSet outputSet = new QueryDataSet(connection);
			outputSet.addTable(tableName);
			FlatXmlDataSet.write(outputSet, System.out);
		}
	}
	
	/**
	 * Utility method that allows tests to easily configure and save a global property
	 * @param string the name of the property to save
	 * @param value the value of the property to save
	 */
	public static void saveGlobalProperty(String name, String value) {
		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(name);
		if (gp == null) {
			gp = new GlobalProperty(name);
		}
		gp.setPropertyValue(value);
		Context.getAdministrationService().saveGlobalProperty(gp);
	}
	
	/**
	 * Utility method to check if a list contains a BaseOpenmrsObject using the id
	 * @param list
	 * @param id
	 * @return true if list contains object with the id else false
	 */
	public static boolean containsId(Collection<? extends BaseOpenmrsObject> list, Integer id) {
		for (BaseOpenmrsObject baseOpenmrsObject : list) {
			if (baseOpenmrsObject.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Waits until System.currentTimeMillis() has flipped to the next second. Since the OpenMRS database is only precise
	 * to the second, if you want to test something "later" you need to wait this long. (Also, useful because the granularity
	 * of the clock on some systems is low, so doing a Thread.sleep(10) may not give you a different clock value
	 *
	 * @see org.openmrs.api.db.hibernate.DropMillisecondsHibernateInterceptor
	 */
	public static void waitForClockTick() {
		long t = System.currentTimeMillis() / 1000;
		while (System.currentTimeMillis() / 1000 == t) {
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException ex) {}
		}
	}
	
	/**
	 * Test utility method to create date time using standard 'yyyy-MM-dd hh:mm:ss' format
	 * @param dateTimeString in 'yyyy-MM-dd hh:mm:ss' format
	 */
	public static Date createDateTime(String dateTimeString) throws ParseException {
		return DateUtils.parseDate(dateTimeString, "yyyy-MM-dd hh:mm:ss.SSS", "yyyy-MM-dd hh:mm:ss", "yyyy-MM-dd");
	}
}

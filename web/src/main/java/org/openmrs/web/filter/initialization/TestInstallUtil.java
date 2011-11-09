/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.filter.initialization;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ModuleConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * Contains static methods to be used by the installation wizard when creating a testing
 * installation
 */
public class TestInstallUtil {
	
	private static final Log log = LogFactory.getLog(TestInstallUtil.class);
	
	private static final File SQL_DUMP_FILE = new File(System.getProperty("java.io.tmpdir"), "sqldump.sql");
	
	/**
	 * Creates a new database for testing
	 * 
	 * @param connectionUrl
	 * @param databaseName
	 * @param databaseDriver
	 * @param user
	 * @param pwd
	 * @return
	 */
	protected static int createTestDatabase(String connectionUrl, String databaseName, String databaseDriver, String user,
	        String pwd) {
		Connection connection = null;
		Statement statement = null;
		
		try {
			Class.forName(databaseDriver).newInstance();
			
			connection = DriverManager.getConnection(connectionUrl, user, pwd);
			statement = connection.createStatement();
			String createDBsql = "create database if not exists `" + databaseName + "` default character set utf8";
			if (!connectionUrl.contains("mysql"))
				createDBsql = createDBsql.replaceAll("`", "\"");
			
			return statement.executeUpdate(createDBsql);
		}
		catch (InstantiationException e) {
			log.error("error:", e);
		}
		catch (IllegalAccessException e) {
			log.error("error:", e);
		}
		catch (ClassNotFoundException e) {
			log.error("error:", e);
		}
		catch (SQLException sqlEx) {
			log.error("Failed to create a test database:", sqlEx);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					log.warn("Error while closing sql statemnt: ", e);
				}
			}
			
			if (connection != null) {
				try {
					connection.close();
				}
				catch (SQLException e) {
					log.warn("Error while closing connection: ", e);
				}
			}
		}
		
		return -1;
	}
	
	/**
	 * Adds data to the test database from a sql dump file
	 * 
	 * @param host
	 * @param port
	 * @param databaseName
	 * @param user
	 * @param pwd
	 * @return
	 */
	protected static boolean addTestData(String host, String port, String databaseName, String user, String pwd) {
		Process proc = null;
		BufferedReader br = null;
		String errorMsg = null;
		String[] command = new String[] { "mysql", "--host=" + host, "--port=" + port, "--user=" + user,
		        "--password=" + pwd, "--database=" + databaseName, "-e", "source " + SQL_DUMP_FILE.getAbsolutePath() };
		
		try {
			proc = Runtime.getRuntime().exec(command);
			try {
				br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
				String line;
				StringBuffer sb = new StringBuffer();
				while ((line = br.readLine()) != null) {
					sb.append(System.getProperty("line.separator"));
					sb.append(line);
				}
				errorMsg = sb.toString();
			}
			catch (IOException e) {
				log.error("Failed to add test data:", e);
			}
			finally {
				if (br != null) {
					try {
						br.close();
					}
					catch (Exception e) {
						log.error("Failed to close the inputstream:", e);
					}
				}
			}
			
			//print out the error messages from the process
			if (StringUtils.isNotBlank(errorMsg))
				log.error(errorMsg);
			
			if (proc.waitFor() == 0) {
				if (log.isDebugEnabled())
					log.debug("Added test data successfully");
				return true;
			}
			
			log.error("The process terminated abnormally while adding test data");
			
		}
		catch (IOException e) {
			log.error("Failed to create the sql dump", e);
		}
		catch (InterruptedException e) {
			log.error("The back up was interrupted while adding test data", e);
		}
		
		return false;
	}
	
	/**
	 * Creates a sql dump from the database matching the specified credentials
	 * 
	 * @param host
	 * @param port
	 * @param databaseName
	 * @param user
	 * @param pwd
	 * @return
	 */
	protected static boolean createSqlDump(String host, String port, String databaseName, String user, String pwd) {
		Process proc = null;
		BufferedReader br = null;
		String errorMsg = null;
		String[] command = new String[] { "mysqldump", "--host=" + host, "--port=" + port, "--user=" + user,
		        "--password=" + pwd, "--result-file=" + SQL_DUMP_FILE.getAbsolutePath(), "--skip-extended-insert",
		        "--skip-quick", "--skip-comments", "--skip-add-drop-table", "--default-character-set=utf8", databaseName };
		try {
			proc = Runtime.getRuntime().exec(command);
			try {
				br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
				String line;
				StringBuffer sb = new StringBuffer();
				while ((line = br.readLine()) != null) {
					sb.append(line);
					sb.append(System.getProperty("line.separator"));
				}
				errorMsg = sb.toString();
			}
			catch (IOException e) {
				log.error("error", e);
			}
			finally {
				if (br != null) {
					try {
						br.close();
					}
					catch (Exception e) {
						log.error("error: ", e);
					}
				}
			}
			
			//print out the error messages from the process
			if (StringUtils.isNotBlank(errorMsg))
				log.error(errorMsg);
			
			if (proc.waitFor() == 0) {
				if (log.isDebugEnabled())
					log.debug("The sql dump file was created successfully");
				
				return true;
			}
			
			log.error("The process terminated abnormally while creating the sql dump");
			
		}
		catch (IOException e) {
			log.error("Failed to create the sql dump", e);
		}
		catch (InterruptedException e) {
			log.error("The back up was interrupted while creating the sql dump", e);
		}
		
		return false;
	}
	
	/**
	 * Extracts .omod files from the specified zip file and copies them to the module repository of
	 * the test application data directory
	 * 
	 * @param moduleFileItems the uploaded file times for the uploading modules to be added to the
	 *            testing environment
	 */
	@SuppressWarnings("rawtypes")
	protected static boolean addZippedTestModules(File testModulesZipFile) {
		Enumeration entries;
		ZipFile zipFile = null;
		boolean successfullyAdded = true;
		
		try {
			File moduleRepository = OpenmrsUtil
			        .getDirectoryInApplicationDataDirectory(ModuleConstants.REPOSITORY_FOLDER_PROPERTY_DEFAULT);
			zipFile = new ZipFile(testModulesZipFile);
			entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (entry.isDirectory()) {
					log.debug("Skipping directory: " + entry.getName());
					continue;
				}
				
				String fileName = entry.getName();
				if (fileName.endsWith(".omod")) {
					//Convert the names of .omod files located in nested directories so that they get
					//created under the module repo directory when being copied
					if (fileName.contains(System.getProperty("file.separator")))
						fileName = new File(entry.getName()).getName();
					
					log.info("Extracting module file: " + fileName);
					OpenmrsUtil.copyFile(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(
					        new File(moduleRepository, fileName))));
				} else
					log.debug("Ignoring file that is not a .omod '" + fileName);
			}
		}
		catch (IOException e) {
			log.error("An error occured while copying modules to the test server:", e);
			successfullyAdded = false;
		}
		finally {
			if (zipFile != null) {
				try {
					zipFile.close();
				}
				catch (IOException e) {
					log.error("Failed to close zip file: ", e);
				}
			}
		}
		
		return successfullyAdded;
	}
	
	/**
	 * Tests the connection to the specified URL
	 * 
	 * @param urlString the url to test
	 * @return true if a connection a established otherwise false
	 */
	protected static boolean testConnection(String urlString) {
		try {
			HttpURLConnection urlConnect = (HttpURLConnection) new URL(urlString).openConnection();
			//wait for 15sec
			urlConnect.setConnectTimeout(15000);
			urlConnect.setUseCaches(false);
			//trying to retrieve data from the source. If there
			//is no connection, this line will fail
			urlConnect.getContent();
			return true;
		}
		catch (UnknownHostException e) {
			log.error("Error generated:", e);
		}
		catch (IOException e) {
			log.error("Error generated:", e);
		}
		
		return false;
	}
}

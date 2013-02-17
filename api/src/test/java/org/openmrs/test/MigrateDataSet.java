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
package org.openmrs.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.swing.JFileChooser;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlWriter;
import org.dbunit.operation.DatabaseOperation;

/**
 * This is a runnable java class that migrates/converts the current dbunit xml files to a new schema
 * structure. After running this class, you are able to pick either a folder to convert. Xmls files
 * are recursively found and converted. The
 * {@link BaseContextSensitiveTest#INITIAL_DATA_SET_XML_FILENAME} file is overwritten by values in
 * the database defined by the runtime properties
 */
public final class MigrateDataSet {

    private static String[] credentials = BaseContextSensitiveTest
	        .askForUsernameAndPassword("Enter your MYSQL DATABASE username and password");
	
	private static String tempDatabaseName = "junitmigration";

    private MigrateDataSet() {
    }

    /**
	 * Do the stuff for this class (create the file)
	 * 
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		System.out.println("Starting...");
		
		String wd = "./test";
		
		// choose the directory to open
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(wd));
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int result = chooser.showOpenDialog(null);
		
		if (result == JFileChooser.APPROVE_OPTION) {
			File[] dirs = chooser.getSelectedFiles();
			
			for (File directory : dirs) {
				System.out.println("Doing migration on directory: " + directory.getAbsolutePath());
				doMigration(directory);
			}
		}
	}
	
	/**
	 * Recurse over the given file and convert all xml files there in
	 * 
	 * @param fileOrDirectory
	 * @throws Exception
	 */
	private static void doMigration(File fileOrDirectory) throws Exception {
		
		String filename = fileOrDirectory.getName();
		
		if (filename.startsWith(".svn") || filename.endsWith("TestingApplicationContext.xml")) {
			// skip .svn files
		} else if (fileOrDirectory.isDirectory()) {
			for (File innerFile : fileOrDirectory.listFiles()) {
				doMigration(innerFile);
			}
		} else if (filename.endsWith(".xml")) {
			InputStream fileOrDirectoryStream = new FileInputStream(fileOrDirectory);
			
			System.out.println("Migrating " + fileOrDirectory.getAbsolutePath());
			
			System.out.println(execMysqlCmd("DROP DATABASE IF EXISTS " + tempDatabaseName, null, false));
			System.out.println(execMysqlCmd("CREATE DATABASE " + tempDatabaseName + " DEFAULT CHARACTER SET utf8", null,
			    false));
            String OLD_SCHEMA_FILE = "/home/ben/workspace/openmrs-trunk/metadata/model/1.3.0-schema-only.sql";
            System.out.println(execMysqlCmd(null, OLD_SCHEMA_FILE, true));
            String OLD_UPDATE_FILE = "/home/ben/workspace/openmrs-trunk/metadata/model/update-to-latest-db.mysqldiff.sql";
            System.out.println(execMysqlCmd(null, OLD_UPDATE_FILE, true));
			
			// the straight-up database connection
			String url = "jdbc:mysql://localhost/" + tempDatabaseName;
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection(url, credentials[0], credentials[1]);
			
			// database connection for dbunit
			@org.jetbrains.annotations.Nullable IDatabaseConnection dbunitConnection = new DatabaseConnection(con);
			
			try {
				PreparedStatement ps = con.prepareStatement("SET FOREIGN_KEY_CHECKS=0;");
				ps.execute();
				ps.close();
				
				IDataSet dataset = new FlatXmlDataSet(fileOrDirectoryStream);
				
				DatabaseOperation.REFRESH.execute(dbunitConnection, dataset);
				
				//turn off foreign key checks here too.
                String NEW_UPDATE_FILE = "/home/ben/workspace/openmrs-concept-name-tag/metadata/model/update-to-latest-db.mysqldiff.sql";
                System.out.println(execMysqlCmd("SET FOREIGN_KEY_CHECKS=0", NEW_UPDATE_FILE, true));
				
				System.out.println("Dumping new xml file");
				
				// get a new connection so dbunit knows the right column headers
				dbunitConnection = new DatabaseConnection(con);
				
				// full database export that will ignore empty tables
				FlatXmlWriter datasetWriter = new FlatXmlWriter(new FileOutputStream(fileOrDirectory));
				datasetWriter.write(dbunitConnection.createDataSet());
			}
			catch (Exception e) {
				System.err.println("Unable to convert: " + filename + " Error: " + e.getMessage());
			}
			finally {
				fileOrDirectoryStream.close();
				dbunitConnection = null;
			}
			
			System.out.println("Finished!");
		}
	}
	
	/**
	 * Execute the given sourceFile and the given command against the temporary mysql database.
	 * 
	 * @param sourceFile (nullable)
	 * @param cmd (nullable)
	 * @param includeDB
	 * @return
	 * @throws Exception
	 */
	private static String execMysqlCmd(String cmd, String sourceFile, boolean includeDB) throws Exception {
		
		if (sourceFile == null && cmd == null)
			throw new Exception("wha...?");
		
		String shellCommand = "";
		if (cmd != null)
			shellCommand = "echo " + cmd + "\\; | ";
		
		if (sourceFile != null) {
			shellCommand = shellCommand + "cat " + (cmd != null ? "-" : "") + " " + sourceFile + " | ";
		}
		
		shellCommand = shellCommand + "mysql -u" + credentials[0] + " -p" + credentials[1];
		
		if (includeDB)
			shellCommand = shellCommand + " -D" + tempDatabaseName;
		
		System.out.println("Executing: " + shellCommand);
		
		String[] cmds = new String[] { "/bin/sh", "-c", shellCommand };
		
		File wd = new File("/tmp");
		
		StringBuffer out = new StringBuffer();
		
		try {
			// Needed to add support for working directory because of a linux
			// file system permission issue.
			// Could not create lcab.tmp file in default working directory
			// (jmiranda).
			Process p = (wd != null) ? Runtime.getRuntime().exec(cmds, null, wd) : Runtime.getRuntime().exec(cmds);
			
			// get the stdout
			out.append("Normal cmd output:\n");
			Reader reader = new InputStreamReader(p.getInputStream());
			BufferedReader input = new BufferedReader(reader);
			int readChar = 0;
			while ((readChar = input.read()) != -1) {
				out.append((char) readChar);
			}
			input.close();
			reader.close();
			
			// get the errout
			out.append("ErrorStream cmd output:\n");
			reader = new InputStreamReader(p.getErrorStream());
			input = new BufferedReader(reader);
			readChar = 0;
			while ((readChar = input.read()) != -1) {
				out.append((char) readChar);
			}
			input.close();
			reader.close();
			
			// wait for the thread to finish and get the exit value
			int exitValue = p.waitFor();
			
		}
		catch (Exception e) {
			System.out.println("Error while executing command: '" + cmd + "': " + e.getMessage());
		}
		
		return out.toString();
	}
}

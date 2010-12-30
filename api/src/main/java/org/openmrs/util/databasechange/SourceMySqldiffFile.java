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
package org.openmrs.util.databasechange;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * Executes (aka "source"s) the given file on the current database. <br/>
 * <br/>
 * Expects parameter: "sqlFile" : name of file on classpath to source on mysql
 */
public class SourceMySqldiffFile implements CustomTaskChange {
	
	private static Log log = LogFactory.getLog(SourceMySqldiffFile.class);
	
	/**
	 * Absolute path and name of file to source
	 */
	private String sqlFile = null;
	
	private FileOpener fileOpener = null;
	
	/**
	 * Does the work of executing the file on mysql
	 * 
	 * @see liquibase.change.custom.CustomTaskChange#execute(liquibase.database.Database)
	 */
	public void execute(Database database) throws CustomChangeException, UnsupportedChangeException {
		
		Properties runtimeProperties = Context.getRuntimeProperties();
		
		// if we're in a "generate sql file" mode, quit early
		if (runtimeProperties.size() == 0)
			return;
		
		DatabaseConnection connection = database.getConnection();
		
		// copy the file from the classpath to a real file
		File tmpOutputFile = null;
		try {
			tmpOutputFile = File.createTempFile(sqlFile, "tmp");
			InputStream sqlFileInputStream = fileOpener.getResourceAsStream(sqlFile);
			OutputStream outputStream = new FileOutputStream(tmpOutputFile);
			OpenmrsUtil.copyFile(sqlFileInputStream, outputStream);
		}
		catch (IOException e) {
			throw new CustomChangeException("Unable to copy " + sqlFile + " to file: " + tmpOutputFile.getAbsolutePath(), e);
		}
		
		// build the mysql command line string
		List<String> commands = new ArrayList<String>();
		String username = runtimeProperties.getProperty("connection.username");
		String password = runtimeProperties.getProperty("connection.password");
		String databaseName;
		try {
			commands.add("mysql");
			commands.add("-u" + username);
			commands.add("-p" + password);
			String path = tmpOutputFile.getAbsolutePath();
			if (!OpenmrsConstants.UNIX_BASED_OPERATING_SYSTEM) {
				// windows hacks
				path = fixWindowsPathHack(path);
			}
			
			commands.add("-esource " + path);
			databaseName = connection.getCatalog();
			commands.add(databaseName);
		}
		catch (SQLException e) {
			throw new CustomChangeException("Unable to generate command string for file: " + sqlFile, e);
		}
		
		// to be used in error messages if this fails
		String errorCommand = "\"mysql -u" + runtimeProperties.getProperty("connection.username") + " -p -e\"source "
		        + tmpOutputFile.getAbsolutePath() + "\"" + databaseName;
		
		// run the command line string
		StringBuffer output = new StringBuffer();
		Integer exitValue = -1; // default to a non-zero exit value in case of exceptions
		try {
			exitValue = execCmd(tmpOutputFile.getParentFile(), commands.toArray(new String[] {}), output);
		}
		catch (IOException io) {
			if (io.getMessage().endsWith("not found")) {
				throw new UnsupportedChangeException("Unable to run command: " + commands.get(0)
				        + ".  Make sure that it is on the PATH and then restart your server and try again. " + " Or run "
				        + errorCommand + " at the command line with the appropriate full mysql path", io);
			}
		}
		catch (Exception e) {
			throw new UnsupportedChangeException("Error while executing command: '" + commands.get(0) + "'", e);
		}
		
		log.debug("Exec called: " + Arrays.asList(commands));
		
		if (exitValue != 0) {
			log.error("There was an error while running the " + commands.get(0) + " command.  Command output: "
			        + output.toString());
			throw new UnsupportedChangeException(
			        "There was an error while running the "
			                + commands.get(0)
			                + " command. See your server's error log for the full error output. As an alternative, you"
			                + " can run this command manually on your database to skip over this error.  Run this at the command line "
			                + errorCommand + "  ");
		} else {
			// a normal exit value
			log.debug("Output of exec: " + output);
		}
		
	}
	
	/**
	 * A hacky way to get rid of the spaces in the java exec call because mysql and java are not
	 * communicating well
	 * 
	 * @param path
	 * @return
	 */
	private String fixWindowsPathHack(String path) {
		StringBuffer returnedPath = new StringBuffer();
		
		path = path.replace("\\", "/"); // so java doesn't freak out with windows backslashes
		for (String pathPart : path.split("/")) {
			if (pathPart.contains(" ")) {
				// shorten to the first 6 characters uppercased
				pathPart = pathPart.substring(0, 6).toUpperCase();
				// add in the tilda and assume the first one (very hacky part)
				pathPart = pathPart + "~1";
			}
			returnedPath.append(pathPart).append("/");
		}
		returnedPath.deleteCharAt(returnedPath.length() - 1);
		
		return returnedPath.toString();
	}
	
	/**
	 * @param cmdWithArguments
	 * @param wd
	 * @param the string
	 * @return process exit value
	 */
	private Integer execCmd(File wd, String[] cmdWithArguments, StringBuffer out) throws Exception {
		log.debug("executing command: " + Arrays.toString(cmdWithArguments));
		
		Integer exitValue = -1;
		
		// Needed to add support for working directory because of a linux
		// file system permission issue.
		
		if (!OpenmrsConstants.UNIX_BASED_OPERATING_SYSTEM)
			wd = null;
		
		Process p = (wd != null) ? Runtime.getRuntime().exec(cmdWithArguments, null, wd) : Runtime.getRuntime().exec(
		    cmdWithArguments);
		
		out.append("Normal cmd output:\n");
		Reader reader = new InputStreamReader(p.getInputStream());
		BufferedReader input = new BufferedReader(reader);
		int readChar = 0;
		while ((readChar = input.read()) != -1) {
			out.append((char) readChar);
		}
		input.close();
		reader.close();
		
		out.append("ErrorStream cmd output:\n");
		reader = new InputStreamReader(p.getErrorStream());
		input = new BufferedReader(reader);
		readChar = 0;
		while ((readChar = input.read()) != -1) {
			out.append((char) readChar);
		}
		input.close();
		reader.close();
		
		exitValue = p.waitFor();
		
		log.debug("Process exit value: " + exitValue);
		
		log.debug("execCmd output: \n" + out.toString());
		
		return exitValue;
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#getConfirmationMessage()
	 */
	public String getConfirmationMessage() {
		return "Finished executing " + sqlFile + " on database";
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#setFileOpener(liquibase.FileOpener)
	 */
	public void setFileOpener(FileOpener fileOpener) {
		this.fileOpener = fileOpener;
	}
	
	/**
	 * Get the values of the parameters passed in and set them to the local variables on this class.
	 * 
	 * @see liquibase.change.custom.CustomChange#setUp()
	 */
	public void setUp() throws SetupException {
		
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#validate(liquibase.database.Database)
	 */
	public void validate(Database database) throws InvalidChangeDefinitionException {
		
	}
	
	/**
	 * @param sqlFile the sqlFile to set
	 */
	public void setSqlFile(String sqlFile) {
		this.sqlFile = sqlFile;
	}
	
}

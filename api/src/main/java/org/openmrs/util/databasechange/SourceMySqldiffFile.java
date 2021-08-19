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

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.InputStreamList;
import liquibase.resource.ResourceAccessor;
import org.openmrs.api.context.Context;
import org.openmrs.util.ClassLoaderFileOpener;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Executes (aka "source"s) the given file on the current database. <br>
 * <br>
 * Expects parameter: "sqlFile" : name of file on classpath to source on mysql
 */
public class SourceMySqldiffFile implements CustomTaskChange {
	
	public static final String CONNECTION_USERNAME = "connection.username";
	
	public static final String CONNECTION_PASSWORD = "connection.password";
	
	private static final Logger log = LoggerFactory.getLogger(SourceMySqldiffFile.class);
	
	/**
	 * Absolute path and name of file to source
	 */
	private String sqlFile = null;
	
	private ResourceAccessor fileOpener = null;
	
	/**
	 * Does the work of executing the file on mysql
	 *
	 * @see liquibase.change.custom.CustomTaskChange#execute(liquibase.database.Database)
	 */
	@Override
	public void execute(Database database) throws CustomChangeException {
		
		Properties runtimeProperties = Context.getRuntimeProperties();
		
		String username = runtimeProperties.getProperty(CONNECTION_USERNAME);
		String password = runtimeProperties.getProperty(CONNECTION_PASSWORD);
		
		if (username == null) {
			username = System.getProperty(CONNECTION_USERNAME);
		}
		if (password == null) {
			password = System.getProperty(CONNECTION_PASSWORD);
		}
		
		// if we're in a "generate sql file" mode, quit early
		if (username == null || password == null) {
			return;
		}
		
		DatabaseConnection connection = database.getConnection();
		
		// copy the file from the classpath to a real file
		File tmpOutputFile = null;
		try {
			tmpOutputFile = File.createTempFile(sqlFile, "tmp");
			
			fileOpener = new ClassLoaderFileOpener(OpenmrsClassLoader.getInstance());
			try (InputStreamList sqlFileInputStream = fileOpener.openStreams(null, sqlFile);
			     OutputStream outputStream = new FileOutputStream(tmpOutputFile)) {
				if (sqlFileInputStream != null && !sqlFileInputStream.isEmpty()) {
					OpenmrsUtil.copyFile(sqlFileInputStream.iterator().next(), outputStream);
				}
			}
		}
		catch (IOException e) {
			if (tmpOutputFile != null) {
				throw new CustomChangeException("Unable to copy " + sqlFile + " to file: " + tmpOutputFile.getAbsolutePath(),
				        e);
			} else {
				throw new CustomChangeException("Unable to copy " + sqlFile, e);
			}
		}
		
		// build the mysql command line string
		List<String> commands = new ArrayList<>();
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
		catch (DatabaseException e) {
			throw new CustomChangeException("Unable to generate command string for file: " + sqlFile, e);
		}
		
		// to be used in error messages if this fails
		String errorCommand = "\"mysql -u" + username + " -p -e\"source " + tmpOutputFile.getAbsolutePath() + "\""
		        + databaseName;
		
		// run the command line string
		StringBuilder output = new StringBuilder();
		Integer exitValue = -1; // default to a non-zero exit value in case of exceptions
		try {
			exitValue = execCmd(tmpOutputFile.getParentFile(), commands.toArray(new String[] {}), output);
		}
		catch (IOException io) {
			if (io.getMessage().endsWith("not found")) {
				throw new CustomChangeException(
				        "Unable to run command: " + commands.get(0)
				                + ".  Make sure that it is on the PATH and then restart your server and try again. "
				                + " Or run " + errorCommand + " at the command line with the appropriate full mysql path",
				        io);
			}
		}
		catch (Exception e) {
			throw new CustomChangeException("Error while executing command: '" + commands.get(0) + "'", e);
		}
		
		log.debug("Exec called: " + Collections.singletonList(commands));
		
		if (exitValue != 0) {
			log.error("There was an error while running the " + commands.get(0) + " command.  Command output: "
			        + output.toString());
			throw new CustomChangeException("There was an error while running the " + commands.get(0)
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
		StringBuilder returnedPath = new StringBuilder();
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
	 * @param out
	 * @return process exit value
	 */
	private Integer execCmd(File wd, String[] cmdWithArguments, StringBuilder out) throws Exception {
		log.debug("executing command: " + Arrays.toString(cmdWithArguments));
		
		Integer exitValue;
		
		// Needed to add support for working directory because of a linux
		// file system permission issue.
		
		if (!OpenmrsConstants.UNIX_BASED_OPERATING_SYSTEM) {
			wd = null;
		}
		
		Process p = (wd != null) ? Runtime.getRuntime().exec(cmdWithArguments, null, wd)
		        : Runtime.getRuntime().exec(cmdWithArguments);
		
		out.append("Normal cmd output:\n");
		Reader reader = new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8);
		BufferedReader input = new BufferedReader(reader);
		int readChar;
		while ((readChar = input.read()) != -1) {
			out.append((char) readChar);
		}
		input.close();
		reader.close();
		
		out.append("ErrorStream cmd output:\n");
		reader = new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8);
		input = new BufferedReader(reader);
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
	@Override
	public String getConfirmationMessage() {
		return "Finished executing " + sqlFile + " on database";
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#setFileOpener(ResourceAccessor)
	 */
	@Override
	public void setFileOpener(ResourceAccessor fileOpener) {
		this.fileOpener = fileOpener;
	}
	
	/**
	 * Get the values of the parameters passed in and set them to the local variables on this class.
	 *
	 * @see liquibase.change.custom.CustomChange#setUp()
	 */
	@Override
	public void setUp() throws SetupException {
		
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#validate(liquibase.database.Database)
	 */
	@Override
	public ValidationErrors validate(Database database) {
		return new ValidationErrors();
	}
	
	/**
	 * @param sqlFile the sqlFile to set
	 */
	public void setSqlFile(String sqlFile) {
		this.sqlFile = sqlFile;
	}
	
}

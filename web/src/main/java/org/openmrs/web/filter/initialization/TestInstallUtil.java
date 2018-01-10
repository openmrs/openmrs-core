/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.initialization;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleConstants;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.filter.util.FilterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains static methods to be used by the installation wizard when creating a testing
 * installation
 */
public class TestInstallUtil {

	private TestInstallUtil() {
	}
	
	private static final Logger log = LoggerFactory.getLogger(TestInstallUtil.class);
	
	/**
	 * Adds data to the test database from a sql dump file
	 *
	 * @param host
	 * @param port
	 * @param databaseName
	 * @param user
	 * @param pwd
	 * @return true if data was added successfully
	 */
	protected static boolean addTestData(String host, int port, String databaseName, String user, String pwd, String filePath) {
		Process proc;
		BufferedReader br = null;
		String errorMsg = null;
		String[] command = new String[] { "mysql", "--host=" + host, "--port=" + port, "--user=" + user,
		        "--password=" + pwd, "--database=" + databaseName, "-e", "source " + filePath };
		
		//For stand-alone, use explicit path to the mysql executable.
		String runDirectory = System.getProperties().getProperty("user.dir");
		File file = new File(runDirectory + File.separatorChar + "database" + File.separatorChar + "bin"
		        + File.separatorChar + "mysql");
		
		if (file.exists()) {
			command[0] = file.getAbsolutePath();
		}
		
		try {
			proc = Runtime.getRuntime().exec(command);
			try {
				br = new BufferedReader(new InputStreamReader(proc.getErrorStream(), StandardCharsets.UTF_8));
				String line;
				StringBuilder sb = new StringBuilder();
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
			if (StringUtils.isNotBlank(errorMsg)) {
				log.error(errorMsg);
			}
			
			if (proc.waitFor() == 0) {
				if (log.isDebugEnabled()) {
					log.debug("Added test data successfully");
				}
				return true;
			}
			
			log
			        .error("The process terminated abnormally while adding test data. Please look under the Configuration section at: https://wiki.openmrs.org/display/docs/Release+Testing+Helper+Module");
			
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
	 * Extracts .omod files from the specified {@link InputStream} and copies them to the module
	 * repository of the test application data directory, the method always closes the InputStream
	 * before returning
	 *
	 * @param in the {@link InputStream} for the zip file
	 */
	@SuppressWarnings("rawtypes")
	protected static boolean addZippedTestModules(InputStream in) {
		ZipFile zipFile = null;
		FileOutputStream out = null;
		File tempFile = null;
		boolean successfullyAdded = true;
		
		try {
			tempFile = File.createTempFile("modules", null);
			out = new FileOutputStream(tempFile);
			IOUtils.copy(in, out);
			zipFile = new ZipFile(tempFile);
			Enumeration entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (entry.isDirectory()) {
					if (log.isDebugEnabled()) {
						log.debug("Skipping directory: " + entry.getName());
					}
					continue;
				}
				
				String fileName = entry.getName();
				if (fileName.endsWith(".omod")) {
					//Convert the names of .omod files located in nested directories so that they get
					//created under the module repo directory when being copied
					if (fileName.contains(System.getProperty("file.separator"))) {
						fileName = new File(entry.getName()).getName();
					}
					
					if (log.isDebugEnabled()) {
						log.debug("Extracting module file: " + fileName);
					}
					
					//use the module repository folder GP value if specified
					String moduleRepositoryFolder = FilterUtil
					        .getGlobalPropertyValue(ModuleConstants.REPOSITORY_FOLDER_PROPERTY);
					if (StringUtils.isBlank(moduleRepositoryFolder)) {
						moduleRepositoryFolder = ModuleConstants.REPOSITORY_FOLDER_PROPERTY_DEFAULT;
					}
					
					//At this point 'OpenmrsConstants.APPLICATION_DATA_DIRECTORY' is still null so we need check
					//for the app data directory defined in the runtime props file if any otherwise the logic in
					//the OpenmrsUtil.getDirectoryInApplicationDataDirectory(String) will default to the other
					String appDataDirectory = Context.getRuntimeProperties().getProperty(
					    OpenmrsConstants.APPLICATION_DATA_DIRECTORY_RUNTIME_PROPERTY);
					if (StringUtils.isNotBlank(appDataDirectory)) {
						OpenmrsUtil.setApplicationDataDirectory(appDataDirectory);
					}
					
					File moduleRepository = OpenmrsUtil.getDirectoryInApplicationDataDirectory(moduleRepositoryFolder);
					
					//delete all previously added modules in case of prior test installations
					FileUtils.cleanDirectory(moduleRepository);
					
					OpenmrsUtil.copyFile(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(
					        new File(moduleRepository, fileName))));
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Ignoring file that is not a .omod '" + fileName);
					}
				}
			}
		}
		catch (IOException e) {
			log.error("An error occured while copying modules to the test system:", e);
			successfullyAdded = false;
		}
		finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
			if (zipFile != null) {
				try {
					zipFile.close();
				}
				catch (IOException e) {
					log.error("Failed to close zip file: ", e);
				}
			}
			if (tempFile != null) {
				tempFile.delete();
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
		catch (IOException e) {
			if (log.isDebugEnabled()) {
				log.debug("Error generated:", e);
			}
		}
		
		return false;
	}
	
	/**
	 * @param url
	 * @param openmrsUsername
	 * @param openmrsPassword
	 * @return input stream
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	protected static InputStream getResourceInputStream(String url, String openmrsUsername, String openmrsPassword)
	        throws MalformedURLException, IOException, APIException {
		
		HttpURLConnection connection = createConnection(url);
		OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
		out.write(encodeCredentials(openmrsUsername, openmrsPassword));
		out.flush();
		out.close();
		
		log.info("Http response message: {}, Code: {}", connection.getResponseMessage(), connection.getResponseCode());
		
		if (connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
			throw new APIAuthenticationException("Invalid username or password");
		} else if (connection.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
			throw new APIException("error.occurred.on.remote.server", (Object[]) null);
		}
		
		return connection.getInputStream();
	}

	private static HttpURLConnection createConnection(String url)
	        throws IOException, MalformedURLException {
		final HttpURLConnection result = (HttpURLConnection) new URL(url).openConnection();
		result.setRequestMethod("POST");
		result.setConnectTimeout(15000);
		result.setUseCaches(false);
		result.setDoOutput(true);
		return result;
	}

	private static String encodeCredentials(String openmrsUsername, String openmrsPassword) {
		final StringBuilder result = new StringBuilder();
		result.append("username=");
		final Encoder encoder = Base64.getEncoder();
		final Charset utf8 = StandardCharsets.UTF_8;
		result.append(new String(encoder.encode(openmrsUsername.getBytes(utf8)), utf8));
		result.append("&password=");
		result.append(new String(encoder.encode(openmrsPassword.getBytes(utf8)), utf8));
		return result.toString();
	}
}

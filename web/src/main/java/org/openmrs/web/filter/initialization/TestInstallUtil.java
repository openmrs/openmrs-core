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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
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
	protected static boolean addTestData(String host, int port, String databaseName, String user, String pwd, String filePath) {
		Process proc = null;
		BufferedReader br = null;
		String errorMsg = null;
		String[] command = new String[] { "mysql", "--host=" + host, "--port=" + port, "--user=" + user,
		        "--password=" + pwd, "--database=" + databaseName, "-e", "source " + filePath };
		
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
	 * Extracts .omod files from the specified {@link ZipInputStream} and copies them to the module
	 * repository of the test application data directory, the method always closes the
	 * zipInputStream before returning
	 * 
	 * @param in the {@link InputStream} for the zip file
	 */
	protected static boolean addZippedTestModules(ZipInputStream in) {
		boolean successfullyAdded = true;
		ZipInputStream zipIn = in;
		BufferedOutputStream out = null;
		try {
			File moduleRepository = OpenmrsUtil
			        .getDirectoryInApplicationDataDirectory(ModuleConstants.REPOSITORY_FOLDER_PROPERTY_DEFAULT);
			ZipEntry entry = zipIn.getNextEntry();
			while (entry != null) {
				String fileName = entry.getName();
				if (fileName.endsWith(".omod")) {
					//Convert the names of .omod files located in nested directories so that they get
					//created under the module repo directory when being copied
					if (fileName.contains(System.getProperty("file.separator")))
						fileName = new File(entry.getName()).getName();
					if (log.isDebugEnabled())
						log.debug("Extracting module file: " + fileName);
					out = new BufferedOutputStream(new FileOutputStream(new File(moduleRepository, fileName)));
					IOUtils.copy(zipIn, out);
				} else {
					if (log.isDebugEnabled())
						log.debug("Ignoring file that is not a .omod '" + fileName);
				}
				
				entry = zipIn.getNextEntry();
			}
		}
		catch (IOException e) {
			log.error("An error occured while copying modules to the test server:", e);
			successfullyAdded = false;
		}
		finally {
			if (zipIn != null) {
				IOUtils.closeQuietly(zipIn);
			}
			if (out != null) {
				IOUtils.closeQuietly(out);
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
	
	/**
	 * @param urlString
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	protected static InputStream getResourceInputStream(String urlString) throws MalformedURLException, IOException {
		HttpURLConnection urlConnection = (HttpURLConnection) new URL(urlString).openConnection();
		urlConnection.setConnectTimeout(15000);
		urlConnection.setUseCaches(false);
		return urlConnection.getInputStream();
	}
}

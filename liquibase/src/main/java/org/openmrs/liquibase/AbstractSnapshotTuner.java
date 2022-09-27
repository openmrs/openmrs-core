/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.liquibase;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the base class for applying changes to generated Liquibase snapshots. This class provides
 * methods required by both subclasses.
 */
public abstract class AbstractSnapshotTuner {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractSnapshotTuner.class);
	
	private static final String OPENMRS_LICENSE_HEADER = "<!--\n" + "\n"
		+ "    This Source Code Form is subject to the terms of the Mozilla Public License,\n"
		+ "    v. 2.0. If a copy of the MPL was not distributed with this file, You can\n"
		+ "    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under\n"
		+ "    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.\n" + "\n"
		+ "    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS\n"
		+ "    graphic logo is a trademark of OpenMRS Inc.\n" + "\n" + "-->\n";
	
	private static final String OPENMRS_LICENSE_SNIPPET = "the terms of the Healthcare Disclaimer located at http://openmrs.org/license";
	
	private final Map<String, String> namespaceUris;
	
	public AbstractSnapshotTuner() {
		namespaceUris = new HashMap<>(1);
		namespaceUris.put("dbchangelog", "http://www.liquibase.org/xml/ns/dbchangelog");
	}
	
	public abstract Document updateChangeLog(Document document);
	
	public void addLicenseHeaderToFileIfNeeded(String path) throws IOException {
		if (!isLicenseHeaderInFile(path)) {
			log.info("Adding the OpenMRS license header to file '{}'....", path);
			String content = addLicenseHeaderToFileContent(path);
			deleteFile(path);
			writeFile(content, path);
		}
		log.info("The file '{}' already contains the OpenMRS license header.", path);
	}
	
	public void createUpdatedChangeLogFile(String sourcePath, String targetPath) throws DocumentException, IOException {
		deleteFile(targetPath);
		log.info("Updating generated Liquibase file:  '{}'...", sourcePath);
		Document document = readChangeLogFile(sourcePath);
		document = updateChangeLog(document);
		writeChangeLogFile(document, targetPath);
		log.info("The updated file is available under:  '{}'", targetPath);
	}
	
	public Map<String, String> getNamespaceUris() {
		return namespaceUris;
	}
	
	String addLicenseHeaderToFileContent(String path) throws FileNotFoundException {
		StringBuilder buffer = new StringBuilder();
		
		try (Scanner scanner = new Scanner(new File(path))) {
			// read first line of xml file
			if (scanner.hasNextLine()) {
				buffer.append(scanner.nextLine()).append("\n");
			}
			
			// append license header
			buffer.append(OPENMRS_LICENSE_HEADER).append("\n");
			
			// append the rest of the xml file
			while (scanner.hasNextLine()) {
				buffer.append(scanner.nextLine()).append("\n");
			}
		}
		catch (FileNotFoundException e) {
			log.error("file '{}' was not found", path, e);
			throw e;
		}
		
		return buffer.toString();
	}
	
	void deleteFile(String path) {
		File file = Paths.get(path).toFile();
		if (file.exists() && file.isFile()) {
			log.info("Deleting updated file from previous run: '{}'...", path);
			file.delete();
		}
	}
	
	boolean isLicenseHeaderInFile(String path) throws FileNotFoundException {
		try (Scanner scanner = new Scanner(new File(path))) {
			while (scanner.hasNextLine()) {
				if (scanner.nextLine().contains(OPENMRS_LICENSE_SNIPPET)) {
					return true;
				}
			}
		}
		catch (FileNotFoundException e) {
			log.error("file '{}' was not found", path, e);
			throw e;
		}
		return false;
	}
	
	Document readChangeLogFile(String path) throws DocumentException {
		File file = Paths.get(path).toFile();
		if (!file.exists()) {
			log.error("The source file '{}' does not exist. Please generate both Liquibase changelog files and retry. "
					+ "Please check if you are running this program from the 'openmrs-core/liquibase' folder.",
				path);
			System.exit(0);
		}
		SAXReader reader = new SAXReader();
		Document document;
		try {
			document = reader.read(file);
		}
		catch (DocumentException e) {
			log.error("processing the file '{}' raised an exception", path, e);
			throw e;
		}
		return document;
	}
	
	Document readChangeLogResource(String resourceName) throws DocumentException, IOException {
		Document document;
		try (InputStream is = getResourceAsStream(resourceName)) {
			SAXReader reader = new SAXReader();
			try {
				document = reader.read(is);
			}
			catch (DocumentException e) {
				log.error("processing the resource '{}' raised an exception", resourceName, e);
				throw e;
			}
		}
		return document;
	}
	
	static String readInputStream(InputStream is) throws IOException {
		// this may over-allocate, but we're only holding it in memory temporarily
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8192);
		byte[] buffer = new byte[8192];
		int length;
		while ((length = is.read(buffer)) != -1) {
			outputStream.write(buffer, 0, length);
		}
		
		return outputStream.toString(StandardCharsets.UTF_8.name());
	}
	
	private InputStream getResourceAsStream(String resourceName) {
		return getClass().getClassLoader().getResourceAsStream(resourceName);
	}
	
	String readResource(String resourceName) throws IOException {
		try (InputStream is = getResourceAsStream(resourceName)) {
			return readInputStream(is);
		}
	}
	
	void writeChangeLogFile(Document document, String path) throws IOException {
		XMLWriter xmlWriter = null;
		try {
			try (OutputStreamWriter out = new OutputStreamWriter (new FileOutputStream (path), StandardCharsets.UTF_8);) {
				OutputFormat format = OutputFormat.createPrettyPrint();
				xmlWriter = new XMLWriter(out, format);
				xmlWriter.write(document);
			}
		}
		catch (IOException | UnsupportedOperationException e) {
			log.error("writing the updated changelog file to '{}' raised an exception", path, e);
			throw e;
		}
		finally {
			try {
				if (xmlWriter != null) {
					xmlWriter.close();
				}
			}
			catch (IOException e) {
				log.error("closing the xml writer for '{}' raised an exception", path, e);
			}
		}
	}
	
	void writeFile(String content, String path) throws IOException {
		File file = Paths.get(path).toFile();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(content);
		}
		catch (IOException e) {
			log.error("writing a file to '{}' raised an exception", path, e);
			throw e;
		}
	}
}

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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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
	
	private Map<String, String> namespaceUris;
	
	public AbstractSnapshotTuner() {
		namespaceUris = new HashMap<>();
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
		Scanner scanner = null;
		StringBuffer buffer = new StringBuffer();
		
		try {
			scanner = new Scanner(new File(path));
			
			// read first line of xml file
			if (scanner.hasNextLine()) {
				buffer.append(scanner.nextLine());
				buffer.append("\n");
			}
			
			// append license header
			buffer.append(OPENMRS_LICENSE_HEADER);
			
			// append the rest of the xml file
			while (scanner.hasNextLine()) {
				buffer.append(scanner.nextLine());
				buffer.append("\n");
			}
		}
		catch (FileNotFoundException e) {
			log.error(String.format("file '{}' was not found", path), e);
			throw e;
		}
		finally {
			scanner.close();
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
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(path));
			while (scanner.hasNextLine()) {
				if (scanner.nextLine().contains(OPENMRS_LICENSE_SNIPPET)) {
					return true;
				}
			}
		}
		catch (FileNotFoundException e) {
			log.error(String.format("file '{}' was not found", path), e);
			throw e;
		}
		finally {
			scanner.close();
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
		Document document = null;
		try {
			document = reader.read(file);
		}
		catch (DocumentException e) {
			log.error(String.format("processing the file '{}' raised an exception", path), e);
			throw e;
		}
		return document;
	}
	
	Document readChangeLogResource(String resourceName) throws DocumentException {
		File file = new File(getClass().getClassLoader().getResource(resourceName).getFile());
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(file);
		}
		catch (DocumentException e) {
			log.error(String.format("processing the resource '{}' raised an exception", resourceName), e);
			throw e;
		}
		return document;
	}
	
	private String readFile(File file) throws FileNotFoundException {
		Scanner scanner = null;
		StringBuffer buffer = new StringBuffer();
		try {
			scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				buffer.append(scanner.nextLine());
			}
		}
		catch (FileNotFoundException e) {
			log.error(String.format("file '{}' was not found", file.getPath()), e);
			throw e;
		}
		finally {
			scanner.close();
		}
		return buffer.toString();
	}
	
	String readFile(String path) throws FileNotFoundException {
		File file = Paths.get(path).toFile();
		return readFile(file);
	}
	
	String readResource(String resourceName) throws FileNotFoundException {
		File file = new File(getClass().getClassLoader().getResource(resourceName).getFile());
		return readFile(file);
	}
	
	void writeChangeLogFile(Document document, String path) throws IOException {
		XMLWriter xmlWriter = null;
		try {
			File file = Paths.get(path).toFile();
			FileWriter fileWriter = new FileWriter(file);
			OutputFormat format = OutputFormat.createPrettyPrint();
			xmlWriter = new XMLWriter(fileWriter, format);
			xmlWriter.write(document);
		}
		catch (IOException e) {
			log.error(String.format("writing the updated changelog file to '%s' raised an exception", path), e);
			throw e;
		}
		finally {
			try {
				xmlWriter.close();
			}
			catch (IOException e) {
				log.error(String.format("closing the xml writer for '%s' raised an exception", path), e);
				throw e;
			}
		}
	}
	
	void writeFile(String content, String path) throws IOException {
		BufferedWriter writer = null;
		try {
			File file = Paths.get(path).toFile();
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(content);
		}
		catch (IOException e) {
			log.error(String.format("writing a file to '%s' raised an exception", path), e);
			throw e;
		}
		finally {
			try {
				writer.close();
			}
			catch (IOException e) {
				log.error(String.format("closing the writer for '%s' raised an exception", path), e);
				throw e;
			}
		}
	}
}

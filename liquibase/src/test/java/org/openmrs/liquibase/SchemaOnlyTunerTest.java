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
import static org.mockito.ArgumentMatchers.any;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

public class SchemaOnlyTunerTest {
	
	private static final String LIQUIBASE_SCHEMA_ONLY_SNAPSHOT_XML = Paths
	        .get("org", "openmrs", "liquibase", "snapshots", "schema-only", "liquibase-schema-only-SNAPSHOT.xml").toString();
	
	private static final String LIQUIBASE_SCHEMA_ONLY_UPDATED_SNAPSHOT_XML = Paths
	        .get("org", "openmrs", "liquibase", "snapshots", "schema-only", "liquibase-schema-only-UPDATED-SNAPSHOT.xml")
	        .toString();
	
	private static String PATH_TO_TEST_RESOURCES = Paths.get("src", "test", "resources").toString();
	
	private Document document;
	
	private Map<String, String> namespaceUris;
	
	private SchemaOnlyTuner schemaOnlyTuner;
	
	@BeforeEach
	public void setup() throws DocumentException {
		schemaOnlyTuner = new SchemaOnlyTuner();
		document = schemaOnlyTuner.readChangeLogResource(LIQUIBASE_SCHEMA_ONLY_SNAPSHOT_XML);
		namespaceUris = schemaOnlyTuner.getNamespaceUris();
	}
	
	@Test
	public void shouldCreateUpdatedChangeLogFile(@TempDir Path tempDir) throws DocumentException, IOException {
		// given
		String sourcePath = PATH_TO_TEST_RESOURCES + LIQUIBASE_SCHEMA_ONLY_SNAPSHOT_XML;
		String targetPath = tempDir.resolve("liquibase-schema-only-UPDATED-SNAPSHOT.xml").toString();
		
		// when
		schemaOnlyTuner.createUpdatedChangeLogFile(sourcePath, targetPath);
		
		// then
		Document expected = schemaOnlyTuner.readChangeLogResource(LIQUIBASE_SCHEMA_ONLY_UPDATED_SNAPSHOT_XML);
		Document actual = schemaOnlyTuner.readChangeLogFile(targetPath);
		
		assertThat(expected.asXML(), equalToCompressingWhiteSpace(actual.asXML()));
	}
	
	@Test
	public void shouldDetachChangeSetsForLiquibaseTables() {
		// given
		XPath xpathOne = DocumentHelper.createXPath("//dbchangelog:createTable[@tableName=\"liquibasechangelog\"]\"");
		xpathOne.setNamespaceURIs(namespaceUris);
		
		XPath xpathTwo = DocumentHelper.createXPath("//dbchangelog:createTable[@tableName=\"liquibasechangeloglock\"]\"");
		xpathTwo.setNamespaceURIs(namespaceUris);
		
		assertEquals(1, xpathOne.selectNodes(document).size());
		assertEquals(1, xpathTwo.selectNodes(document).size());
		
		// when
		Document actual = schemaOnlyTuner.detachLiquibaseTables(document);
		
		// then
		assertEquals(0, xpathOne.selectNodes(actual).size());
		assertEquals(0, xpathTwo.selectNodes(actual).size());
	}
	
	@Test
	public void shouldReplaceBitTypeWithBooleanType() {
		// given
		XPath xpath = DocumentHelper.createXPath("//dbchangelog:column[@type=\"BIT(1)\"]/attribute::type");
		xpath.setNamespaceURIs(namespaceUris);
		
		List<Node> nodes = xpath.selectNodes(document);
		assertEquals(94, nodes.size());
		
		// when
		Document actual = schemaOnlyTuner.replaceBitWithBoolean(document);
		
		// then
		for (Node node : nodes) {
			assertEquals("BOOLEAN", node.getParent().attributeValue("type"));
		}
	}
	
	@Test
	public void shouldReplaceLongtextTypeWithClobType() {
		// given
		XPath xPath = DocumentHelper.createXPath("//dbchangelog:column[@type=\"LONGTEXT\"]/attribute::type");
		xPath.setNamespaceURIs(namespaceUris);
		
		List<Node> nodes = xPath.selectNodes(document);
		assertEquals(1, nodes.size());
		
		// when
		SchemaOnlyTuner schemaOnlyTunerSpy = Mockito.spy(schemaOnlyTuner);
		Document actual = schemaOnlyTunerSpy.replaceLongtextWithClob(document);
		
		// then
		for (Node node : nodes) {
			assertEquals("CLOB", node.getParent().attributeValue("type"));
		}
		
		Mockito.verify(schemaOnlyTunerSpy).assertLongtextNodes(any());
	}
	
	@Test
	public void shouldAssertLongtextNode() {
		// given
		XPath xPath = DocumentHelper.createXPath("//dbchangelog:column[@type=\"LONGTEXT\"]/attribute::type");
		xPath.setNamespaceURIs(namespaceUris);
		
		List<Node> nodes = xPath.selectNodes(document);
		assertEquals(1, nodes.size());
		
		// when and then
		assertTrue(schemaOnlyTuner.assertLongtextNodes(nodes));
	}
	
	@Test
	public void shouldDetectWrongNumberOfLongtextNodes() {
		assertThrows(AssertionError.class, () -> {
			// given
			XPath xPath = DocumentHelper.createXPath("//dbchangelog:column[@type=\"BIT(1)\"]/attribute::type");
			xPath.setNamespaceURIs(namespaceUris);
			
			List<Node> nodes = xPath.selectNodes(document);
			assertEquals(94, nodes.size());
			
			// when
			schemaOnlyTuner.assertLongtextNodes(nodes);
		});
	}
	
	@Test
	public void shouldDetectWrongGrandParentNodeOfLongtextNode() {
		assertThrows(AssertionError.class, () -> {
			// given
			XPath xPath = DocumentHelper.createXPath("//dbchangelog:column[@type=\"BIT(1)\"]/attribute::type");
			xPath.setNamespaceURIs(namespaceUris);
			
			Node node = xPath.selectSingleNode(document);
			List<Node> nodes = new ArrayList<>();
			nodes.add(node);
			
			// when
			schemaOnlyTuner.assertLongtextNodes(nodes);
		});
	}
}

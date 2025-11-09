/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under the terms of the Healthcare Disclaimer
 * located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc.
 * OpenMRS is a registered trademark and the OpenMRS graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.liquibase;

import java.util.List;
import java.util.Objects;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;

/**
 * This class:
 * <ul>
 *   <li>Changes selected data types in Liquibase schema-only snapshots.</li>
 *   <li>Removes change sets for the tables 'liquibasechangelog' and 'liquibasechangeloglock'.</li>
 * </ul>
 * The necessary changes after generating a Liquibase schema-only snapshot are documented in
 * <code>liquibase/README.md</code>.
 */
public class SchemaOnlyTuner extends AbstractSnapshotTuner {

	@Override
	public Document updateChangeLog(Document document) {
		Objects.requireNonNull(document, "Document must not be null");
		document = replaceBitWithBoolean(document);
		document = replaceLongtextWithClob(document);
		return detachLiquibaseTables(document);
	}

	private Document detachLiquibaseTables(Document document) {
		document = detachChangeSet(document, "liquibasechangelog");
		return detachChangeSet(document, "liquibasechangeloglock");
	}

	private Document detachChangeSet(Document document, String tableName) {
		XPath xPath = DocumentHelper.createXPath(
			String.format("//dbchangelog:createTable[@tableName='%s']", tableName));
		xPath.setNamespaceURIs(getNamespaceUris());

		Node node = xPath.selectSingleNode(document);
		if (node != null && node.getParent() != null) {
			node.getParent().detach();
		}
		return document;
	}

	private Document replaceBitWithBoolean(Document document) {
		XPath xPath = DocumentHelper.createXPath("//dbchangelog:column[@type='BIT']/@type");
		xPath.setNamespaceURIs(getNamespaceUris());

		List<Node> nodes = xPath.selectNodes(document);
		for (Node node : nodes) {
			Element parent = node.getParent();
			parent.addAttribute("type", "BOOLEAN");

			String defaultValue = parent.attributeValue("defaultValueNumeric");
			if ("1".equals(defaultValue)) {
				parent.addAttribute("defaultValueBoolean", "true");
				parent.remove(parent.attribute("defaultValueNumeric"));
			} else if ("0".equals(defaultValue)) {
				parent.addAttribute("defaultValueBoolean", "false");
				parent.remove(parent.attribute("defaultValueNumeric"));
			}
		}
		return document;
	}

	private Document replaceLongtextWithClob(Document document) {
		XPath xPath = DocumentHelper.createXPath("//dbchangelog:column[@type='LONGTEXT']/@type");
		xPath.setNamespaceURIs(getNamespaceUris());

		List<Node> nodes = xPath.selectNodes(document);
		assertLongtextNodes(nodes);

		for (Node node : nodes) {
			node.getParent().addAttribute("type", "CLOB");
		}
		return document;
	}

	/**
	 * Asserts that the Liquibase schema-only snapshot contains only two 'LONGTEXT' columns.
	 * This was true when Liquibase snapshots were introduced in May 2020. 
	 * Future snapshots containing additional 'LONGTEXT' columns likely need manual review.
	 *
	 * @param nodes the nodes to assert
	 * @return true if the assertion passes, false otherwise
	 */
	boolean assertLongtextNodes(List<Node> nodes) {
		int size = nodes.size();
		assert size == 2 : String.format(
			"Expected exactly 2 'LONGTEXT' columns, found %d", size);

		Node node = nodes.get(0);
		Element grandParent = node.getParent().getParent();
		String tableName = grandParent.attributeValue("tableName");

		assert "clob_datatype_storage".equals(tableName)
			: "Expected 'LONGTEXT' column to belong to table 'clob_datatype_storage'";

		return true;
	}
}

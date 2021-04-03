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

import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;

/**
 * This class (a) changes selected data types in Liquibase schema-only snapshots and (b) discards
 * change sets for the tables 'liquibasechangelog' and 'liquibasechangeloglock'. The changes
 * required after generating a Liquibase schema-only snapshot are documented in the
 * liquibase/README.md file.
 */
public class SchemaOnlyTuner extends AbstractSnapshotTuner {

	@Override
	public Document updateChangeLog(Document document) {
		document = replaceBitWithBoolean(document);
		document = replaceLongtextWithClob(document);
		document = detachLiquibaseTables( document );
		return document;
	}
	
	Document detachLiquibaseTables(Document document) {
		document = detachChangeSet(document, "liquibasechangelog");
		document = detachChangeSet(document, "liquibasechangeloglock");
		
		return document;
	}
	
	Document detachChangeSet(Document document, String tableName) {
		XPath xPath = DocumentHelper.createXPath(String.format("//dbchangelog:createTable[@tableName=\"%s\"]", tableName));
		xPath.setNamespaceURIs(getNamespaceUris());
		
		Node node = xPath.selectSingleNode(document);
		node.getParent().detach();
		
		return document;
	}
	
	Document replaceBitWithBoolean(Document document) {
		XPath xPath = DocumentHelper.createXPath("//dbchangelog:column[@type=\"BIT(1)\"]/attribute::type");
		xPath.setNamespaceURIs(getNamespaceUris());
		
		List<Node> nodes = xPath.selectNodes(document);
		for (Node node : nodes) {
			Element parent = node.getParent();
			parent.addAttribute("type", "BOOLEAN");
		}
		
		return document;
	}
	
	Document replaceLongtextWithClob(Document document) {
		XPath xPath = DocumentHelper.createXPath("//dbchangelog:column[@type=\"LONGTEXT\"]/attribute::type");
		xPath.setNamespaceURIs(getNamespaceUris());
		
		List<Node> nodes = xPath.selectNodes(document);
		assertLongtextNodes(nodes);
		
		for (Node node : nodes) {
			Element parent = node.getParent();
			parent.addAttribute("type", "CLOB");
		}
		
		return document;
	}
	
	/**
	 * This method asserts that the Liquibase schema only snapshot contains only one column of type
	 * 'LONGTEXT'. This was the case when Liquibase snapshots were introduced in May 2020. If future
	 * snapshots contain additional columns of type 'LONGTEXT', they most likely need to be changed to
	 * 'CLOB' as well. However, someone needs to look into it first and is reminded to do so by this
	 * assertion failing.
	 *
	 * @param nodes define the nodes to assert
	 * @return a boolean value for unit testing
	 */
	boolean assertLongtextNodes(List<Node> nodes) {
		assert nodes.size() == 1 : String
		        .format("replacing the column type 'LONGTEXT' failed as the number of nodes is not 1 but %d", nodes.size());
		
		Node node = nodes.get(0);
		Element grandParent = node.getParent().getParent();
		
		assert grandParent.attributeValue("tableName").equals(
		    "clob_datatype_storage") : "replacing the column type 'LONGTEXT' failed as the node does not refer to the 'clob_datatype_storage' table";
		
		return true;
	}
}

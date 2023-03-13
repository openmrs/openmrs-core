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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;

/**
 * This class (a) changes the order of change sets in Liquibase core-data snapshots, (b) discards
 * change sets for the tables 'liquibasechangelog' and 'liquibasechangeloglock' and (c) ensures the
 * admin user has working default values. The changes required after generating a Liquibase
 * core-data snapshot are documented in the liquibase/README.md file.
 */
public class CoreDataTuner extends AbstractSnapshotTuner {
	
	private static final String PASSWORD = "4a1750c8607dfa237de36c6305715c223415189";
	
	private static final String USERNAME = "";
	
	private static final List<String> firstTableNames = Arrays.asList("person", "users", "care_setting", "concept_class",
	    "concept_datatype", "concept");
	
	private static final List<String> liquibaseTableNames = Arrays.asList("liquibasechangelog", "liquibasechangeloglock");
	
	private Map<String, Node> changeSetsByTableName;
	
	private List<String> tableNames;
	
	public CoreDataTuner() {
		changeSetsByTableName = new HashMap<>();
		tableNames = new ArrayList<>();
	}
	
	@Override
	public Document updateChangeLog(Document document) {
		document = detachAndCacheChangeSets(document);
		document = addChangeSets(document);
		document = updateAdminUser(document);
		return document;
	}

	/**
	 * This method prepares for changing the order of change sets by removing all change sets from the document and 
	 * caching them.
	 * 
	 * @param document the document to detach change sets from
	 * @return the document without change sets
	 */
	Document detachAndCacheChangeSets(Document document) {
		XPath xPath = DocumentHelper.createXPath("//dbchangelog:insert/attribute::tableName");
		xPath.setNamespaceURIs(getNamespaceUris());
		
		List<Node> nodes = xPath.selectNodes(document);
		
		for (Node node : nodes) {
			String tableName = node.getStringValue();
			Element changeSet = node.getParent().getParent();
			
			/*
			 * process each table name only once
			 */
			if (!tableNames.contains(tableName)) {
				/*
				 * the change sets for the tables 'liquibasechangelog' and 'liquibasechangeloglock' do not need to be
				 * cached as they are discarded from the Liquibase snapshot
				 */
				if (!liquibaseTableNames.contains(tableName)) {
					tableNames.add(tableName);
					changeSetsByTableName.put(tableName, changeSet);
				}
				changeSet.detach();
			}
		}
		
		return document;
	}

	/**
	 * This method adds change sets in a different order compared to the generated change log file.
	 * 
	 * @param document the document to add the change sets to
	 * @return the document with change sets added
	 */
	Document addChangeSets(Document document) {
		XPath xPath = DocumentHelper.createXPath("//dbchangelog:changeSet");
		xPath.setNamespaceURIs(getNamespaceUris());
		
		List<Node> changeSets = xPath.selectNodes(document);
		assert changeSets.size() == 0 : "aborting as document already contains changeSets";
		
		Element databaseChangeLog = getDatabaseChangeLogElement(document);
		
		/*
		 * start by adding change sets for tables that need to come first
		 */
		for (String tableName : firstTableNames) {
			databaseChangeLog.add(changeSetsByTableName.get(tableName));
		}
		
		/*
		 * next add the remaining change sets in the order they had been generated
		 */
		for (String tableName : tableNames) {
			if (!firstTableNames.contains(tableName)) {
				databaseChangeLog.add(changeSetsByTableName.get(tableName));
			}
		}
		
		return document;
	}
	
	Document updateAdminUser(Document document) {
		XPath xPath = DocumentHelper
		        .createXPath("//dbchangelog:insert[@tableName=\"users\"]/dbchangelog:column[@value=\"admin\"]");
		xPath.setNamespaceURIs(getNamespaceUris());
		
		Node columnNode = xPath.selectSingleNode(document);
		Element insertNode = columnNode.getParent();
		
		for (Iterator<Element> it = insertNode.elementIterator(); it.hasNext();) {
			Element element = it.next();
			
			if (element.attribute("name").getValue().equals("username")) {
				element.addAttribute("value", USERNAME);
			}
			if (element.attribute("name").getValue().equals("password")) {
				element.addAttribute("value", PASSWORD);
			}
		}
		
		return document;
	}
	
	Element getDatabaseChangeLogElement(Document document) {
		XPath xPathForDatabaseChangeLog = DocumentHelper.createXPath("/dbchangelog:databaseChangeLog");
		xPathForDatabaseChangeLog.setNamespaceURIs(getNamespaceUris());
		
		Node databaseChangeLog = xPathForDatabaseChangeLog.selectSingleNode(document);
		return (Element) databaseChangeLog;
	}
	
	public static List<String> getFirstTableNames() {
		return firstTableNames;
	}
	
	public Map<String, Node> getChangeSetsByTableName() {
		return changeSetsByTableName;
	}
	
	public List<String> getTableNames() {
		return tableNames;
	}
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This class will parse an xml sql diff file
 *
 * @version 1.0
 */
public class SqlDiffFileParser {

	private SqlDiffFileParser() {
	}
	
	private static final Logger log = LoggerFactory.getLogger(SqlDiffFileParser.class);
	
	private static final String SQLDIFF_CHANGELOG_FILENAME = "sqldiff.xml";
	
	/**
	 * Get the diff map. Return a sorted map&lt;version, sql statements&gt;
	 *
	 * @return SortedMap&lt;String, String&gt;
	 * @throws ModuleException
	 */
	public static SortedMap<String, String> getSqlDiffs(Module module) throws ModuleException {
		if (module == null) {
			throw new ModuleException("Module cannot be null");
		}
		
		SortedMap<String, String> map = new TreeMap<>(new VersionComparator());
		
		InputStream diffStream;
		
		// get the diff stream
		JarFile jarfile = null;
		try {
			try {
				jarfile = new JarFile(module.getFile());
			}
			catch (IOException e) {
				throw new ModuleException("Unable to get jar file", module.getName(), e);
			}
			
			diffStream = ModuleUtil.getResourceFromApi(jarfile, module.getModuleId(), module.getVersion(),
			    SQLDIFF_CHANGELOG_FILENAME);
			if (diffStream == null) {
				// Try the old way. Loading from the root of the omod
				ZipEntry diffEntry = jarfile.getEntry(SQLDIFF_CHANGELOG_FILENAME);
				if (diffEntry == null) {
					log.debug("No sqldiff.xml found for module: " + module.getName());
					return map;
				} else {
					try {
						diffStream = jarfile.getInputStream(diffEntry);
					}
					catch (IOException e) {
						throw new ModuleException("Unable to get sql diff file stream", module.getName(), e);
					}
				}
			}
			
			try {
				// turn the diff stream into an xml document
				Document diffDoc;
				try {
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					db.setEntityResolver((publicId, systemId) -> {
						// When asked to resolve external entities (such as a DTD) we return an InputSource
						// with no data at the end, causing the parser to ignore the DTD.
						return new InputSource(new StringReader(""));
					});
					diffDoc = db.parse(diffStream);
				}
				catch (Exception e) {
					throw new ModuleException("Error parsing diff sqldiff.xml file", module.getName(), e);
				}
				
				Element rootNode = diffDoc.getDocumentElement();
				
				String diffVersion = rootNode.getAttribute("version");
				
				if (!validConfigVersions().contains(diffVersion)) {
					throw new ModuleException("Invalid config version: " + diffVersion, module.getModuleId());
				}
				
				NodeList diffNodes = getDiffNodes(rootNode, diffVersion);
				
				if (diffNodes != null && diffNodes.getLength() > 0) {
					int i = 0;
					while (i < diffNodes.getLength()) {
						Element el = (Element) diffNodes.item(i++);
						String version = getElement(el, diffVersion, "version");
						String sql = getElement(el, diffVersion, "sql");
						map.put(version, sql);
					}
				}
			}
			catch (ModuleException e) {
				if (diffStream != null) {
					try {
						diffStream.close();
					}
					catch (IOException io) {
						log.error("Error while closing config stream for module: " + module.getModuleId(), io);
					}
				}
				
				// rethrow the moduleException
				throw e;
			}
			
		}
		finally {
			try {
				if (jarfile != null) {
					jarfile.close();
				}
			}
			catch (IOException e) {
				log.warn("Unable to close jarfile: " + jarfile.getName());
			}
		}
		return map;
	}
	
	/**
	 * Generic method to get a module tag
	 *
	 * @param element
	 * @param version
	 * @param tag
	 * @return
	 */
	private static String getElement(Element element, String version, String tag) {
		if (element.getElementsByTagName(tag).getLength() > 0) {
			return element.getElementsByTagName(tag).item(0).getTextContent();
		}
		return "";
	}
	
	/**
	 * List of the valid sqldiff versions
	 *
	 * @return
	 */
	private static List<String> validConfigVersions() {
		List<String> versions = new ArrayList<>();
		versions.add("1.0");
		return versions;
	}
	
	/**
	 * Finds the nodes that contain diff information
	 *
	 * @param element
	 * @param version
	 * @return
	 */
	private static NodeList getDiffNodes(Element element, String version) {
		NodeList diffNodes = null;
		
		if ("1.0".equals(version)) {
			diffNodes = element.getElementsByTagName("diff");
		}
		
		return diffNodes;
	}
	
}

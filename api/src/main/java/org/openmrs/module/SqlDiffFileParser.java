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
package org.openmrs.module;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class will parse an xml sql diff file
 * 
 * @version 1.0
 */
public class SqlDiffFileParser {
	
	private static Log log = LogFactory.getLog(SqlDiffFileParser.class);
	
	/**
	 * Get the diff map. Return a sorted map<version, sql statements>
	 * 
	 * @return SortedMap<String, String>
	 * @throws ModuleException
	 */
	public static SortedMap<String, String> getSqlDiffs(Module module) throws ModuleException {
		if (module == null)
			throw new ModuleException("Module cannot be null");
		
		SortedMap<String, String> map = new TreeMap<String, String>();
		
		InputStream diffStream = null;
		
		// get the diff stream
		JarFile jarfile = null;
		try {
			try {
				jarfile = new JarFile(module.getFile());
			}
			catch (IOException e) {
				throw new ModuleException("Unable to get jar file", module.getName(), e);
			}
			
			ZipEntry diffEntry = jarfile.getEntry("sqldiff.xml");
			
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
			
			try {
				
				// turn the diff stream into an xml document
				Document diffDoc = null;
				try {
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					db.setEntityResolver(new EntityResolver() {
						
						public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
							// When asked to resolve external entities (such as a DTD) we return an InputSource
							// with no data at the end, causing the parser to ignore the DTD.
							return new InputSource(new StringReader(""));
						}
					});
					diffDoc = db.parse(diffStream);
				}
				catch (Exception e) {
					throw new ModuleException("Error parsing diff sqldiff.xml file", module.getName(), e);
				}
				
				Element rootNode = diffDoc.getDocumentElement();
				
				String diffVersion = rootNode.getAttribute("version");
				
				if (!validConfigVersions().contains(diffVersion))
					throw new ModuleException("Invalid config version: " + diffVersion, module.getModuleId());
				
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
				if (jarfile != null)
					jarfile.close();
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
		if (element.getElementsByTagName(tag).getLength() > 0)
			return element.getElementsByTagName(tag).item(0).getTextContent();
		return "";
	}
	
	/**
	 * List of the valid sqldiff versions
	 * 
	 * @return
	 */
	private static List<String> validConfigVersions() {
		List<String> versions = new Vector<String>();
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
		
		if ("1.0".equals(version))
			diffNodes = element.getElementsByTagName("diff");
		
		return diffNodes;
	}
	
}

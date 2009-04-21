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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.Privilege;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class will parse a file into an org.openmrs.module.Module object
 */
public class ModuleFileParser {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private File moduleFile = null;
	
	/**
	 * List out all of the possible version numbers for config files that openmrs has DTDs for.
	 * These are usually stored at http://resources.openmrs.org/doctype/config-x.x.dt
	 */
	private static List<String> validConfigVersions = new ArrayList<String>();
	
	static {
		validConfigVersions.add("1.0");
		validConfigVersions.add("1.1");
		validConfigVersions.add("1.2");
	}
	
	/**
	 * Constructor
	 * 
	 * @param moduleFile the module (jar)file that will be parsed
	 */
	public ModuleFileParser(File moduleFile) {
		if (moduleFile == null)
			throw new ModuleException("Module file cannot be null");
		
		if (!moduleFile.getName().endsWith(".omod"))
			throw new ModuleException("Module file does not have the correct .omod file extension", moduleFile.getName());
		
		this.moduleFile = moduleFile;
	}
	
	/**
	 * Get the module
	 * 
	 * @return new module object
	 */
	public Module parse() throws ModuleException {
		
		Module module = null;
		JarFile jarfile = null;
		InputStream configStream = null;
		
		try {
			try {
				jarfile = new JarFile(moduleFile);
			}
			catch (IOException e) {
				throw new ModuleException("Unable to get jar file", moduleFile.getName(), e);
			}
			
			// look for config.xml in the root of the module
			ZipEntry config = jarfile.getEntry("config.xml");
			if (config == null)
				throw new ModuleException("Error loading module. No config.xml found.", moduleFile.getName());
			
			// get a config file stream
			try {
				configStream = jarfile.getInputStream(config);
			}
			catch (IOException e) {
				throw new ModuleException("Unable to get config file stream", moduleFile.getName(), e);
			}
			
			// turn the config file into an xml document
			Document configDoc = null;
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				db.setEntityResolver(new EntityResolver() {
					
					public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
						// When asked to resolve external entities (such as a
						// DTD) we return an InputSource
						// with no data at the end, causing the parser to ignore
						// the DTD.
						return new InputSource(new StringReader(""));
					}
				});
				
				configDoc = db.parse(configStream);
			}
			catch (Exception e) {
				log.error("Error parsing config.xml: " + configStream.toString(), e);
				
				OutputStream out = null;
				String output = "";
				try {
					out = new ByteArrayOutputStream();
					// Now copy bytes from the URL to the output stream
					byte[] buffer = new byte[4096];
					int bytes_read;
					while ((bytes_read = configStream.read(buffer)) != -1)
						out.write(buffer, 0, bytes_read);
					output = out.toString();
				}
				catch (Exception e2) {
					log.warn("Another error parsing config.xml", e2);
				}
				finally {
					try {
						out.close();
					}
					catch (Exception e3) {}
					;
				}
				
				log.error("config.xml content: " + output);
				throw new ModuleException("Error parsing module config.xml file", moduleFile.getName(), e);
			}
			
			Element rootNode = configDoc.getDocumentElement();
			
			String configVersion = rootNode.getAttribute("configVersion");
			
			if (!validConfigVersions.contains(configVersion))
				throw new ModuleException("Invalid config version: " + configVersion, moduleFile.getName());
			
			String name = getElement(rootNode, configVersion, "name");
			String moduleId = getElement(rootNode, configVersion, "id");
			String packageName = getElement(rootNode, configVersion, "package");
			String author = getElement(rootNode, configVersion, "author");
			String desc = getElement(rootNode, configVersion, "description");
			String version = getElement(rootNode, configVersion, "version");
			
			// do some validation
			if (name == null || name.length() == 0)
				throw new ModuleException("name cannot be empty", moduleFile.getName());
			if (moduleId == null || moduleId.length() == 0)
				throw new ModuleException("module id cannot be empty", name);
			if (packageName == null || packageName.length() == 0)
				throw new ModuleException("package cannot be empty", name);
			
			// look for log4j.xml in the root of the module
			Document log4jDoc = null;
			try {
				ZipEntry log4j = jarfile.getEntry("log4j.xml");
				if (log4j != null) {
					InputStream log4jStream = jarfile.getInputStream(log4j);
					
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					db.setEntityResolver(new EntityResolver() {
						
						public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
							// When asked to resolve external entities (such as
							// a
							// DTD) we return an InputSource
							// with no data at the end, causing the parser to
							// ignore
							// the DTD.
							return new InputSource(new StringReader(""));
						}
					});
					
					log4jDoc = db.parse(log4jStream);
				}
			}
			catch (Exception e) {}
			
			// create the module object
			module = new Module(name, moduleId, packageName, author, desc, version);
			
			// find and load the activator class
			module.setActivatorName(getElement(rootNode, configVersion, "activator"));
			
			module.setRequireDatabaseVersion(getElement(rootNode, configVersion, "require_database_version"));
			module.setRequireOpenmrsVersion(getElement(rootNode, configVersion, "require_version"));
			module.setUpdateURL(getElement(rootNode, configVersion, "updateURL"));
			module.setRequiredModulesMap(getRequiredModules(rootNode, configVersion));
			
			module.setAdvicePoints(getAdvice(rootNode, configVersion, module));
			module.setExtensionNames(getExtensions(rootNode, configVersion));
			
			module.setPrivileges(getPrivileges(rootNode, configVersion));
			module.setGlobalProperties(getGlobalProperties(rootNode, configVersion));
			
			module.setMessages(getMessages(rootNode, configVersion, jarfile));
			
			module.setMappingFiles(getMappingFiles(rootNode, configVersion, jarfile));
			
			module.setConfig(configDoc);
			
			module.setLog4j(log4jDoc);
			
			module.setFile(moduleFile);
		}
		finally {
			try {
				jarfile.close();
			}
			catch (Exception e) {
				log.warn("Unable to close jarfile: " + jarfile, e);
			}
			if (configStream != null) {
				try {
					configStream.close();
				}
				catch (Exception io) {
					log.error("Error while closing config stream for module: " + moduleFile.getAbsolutePath(), io);
				}
			}
		}
		
		return module;
	}
	
	/**
	 * Generic method to get a module tag
	 * 
	 * @param root
	 * @param version
	 * @param tag
	 * @return
	 */
	private String getElement(Element root, String version, String tag) {
		if (root.getElementsByTagName(tag).getLength() > 0)
			return root.getElementsByTagName(tag).item(0).getTextContent();
		return "";
	}
	
	/**
	 * load in required modules list
	 * 
	 * @param root element in the xml doc object
	 * @param version of the config file
	 * @return map from module package name to required version
	 * @since 1.5
	 */
	private Map<String, String> getRequiredModules(Element root, String version) {
		NodeList requiredModulesParents = root.getElementsByTagName("require_modules");
		
		Map<String, String> packageNamesToVersion = new HashMap<String, String>();
		
		// TODO test require_modules section
		if (requiredModulesParents.getLength() > 0) {
			Node requiredModulesParent = requiredModulesParents.item(0);
			
			NodeList requiredModules = requiredModulesParent.getChildNodes();
			
			int i = 0;
			while (i < requiredModules.getLength()) {
				Node n = requiredModules.item(i);
				if (n != null && "require_module".equals(n.getNodeName())) {
					NamedNodeMap attributes = n.getAttributes();
					Node versionNode = attributes.getNamedItem("version");
					String reqVersion = versionNode == null ? null : versionNode.getNodeValue();
					packageNamesToVersion.put(n.getTextContent(), reqVersion);
				}
				i++;
			}
		}
		return packageNamesToVersion;
	}
	
	/**
	 * load in advicePoints
	 * 
	 * @param root
	 * @param version
	 * @return
	 */
	private List<AdvicePoint> getAdvice(Element root, String version, Module mod) {
		
		List<AdvicePoint> advicePoints = new Vector<AdvicePoint>();
		
		NodeList advice = root.getElementsByTagName("advice");
		if (advice.getLength() > 0) {
			log.debug("# advice: " + advice.getLength());
			int i = 0;
			while (i < advice.getLength()) {
				Node node = advice.item(i);
				NodeList nodes = node.getChildNodes();
				int x = 0;
				String point = "", adviceClass = "";
				while (x < nodes.getLength()) {
					Node childNode = nodes.item(x);
					if ("point".equals(childNode.getNodeName()))
						point = childNode.getTextContent();
					else if ("class".equals(childNode.getNodeName()))
						adviceClass = childNode.getTextContent();
					x++;
				}
				log.debug("point: " + point + " class: " + adviceClass);
				
				// point and class are required
				if (point.length() > 0 && adviceClass.length() > 0) {
					advicePoints.add(new AdvicePoint(mod, point, adviceClass));
				} else
					log.warn("'point' and 'class' are required for advice. Given '" + point + "' and '" + adviceClass + "'");
				
				i++;
			}
		}
		
		return advicePoints;
	}
	
	/**
	 * load in extensions
	 * 
	 * @param root
	 * @param configVersion
	 * @return
	 */
	private IdentityHashMap<String, String> getExtensions(Element root, String configVersion) {
		
		IdentityHashMap<String, String> extensions = new IdentityHashMap<String, String>();
		
		NodeList extensionNodes = root.getElementsByTagName("extension");
		if (extensionNodes.getLength() > 0) {
			log.debug("# extensions: " + extensionNodes.getLength());
			int i = 0;
			while (i < extensionNodes.getLength()) {
				Node node = extensionNodes.item(i);
				NodeList nodes = node.getChildNodes();
				int x = 0;
				String point = "", extClass = "";
				while (x < nodes.getLength()) {
					Node childNode = nodes.item(x);
					if ("point".equals(childNode.getNodeName()))
						point = childNode.getTextContent();
					else if ("class".equals(childNode.getNodeName()))
						extClass = childNode.getTextContent();
					x++;
				}
				log.debug("point: " + point + " class: " + extClass);
				
				// point and class are required
				if (point.length() > 0 && extClass.length() > 0) {
					if (point.indexOf(Extension.extensionIdSeparator) != -1)
						log.warn("Point id contains illegal character: '" + Extension.extensionIdSeparator + "'");
					else {
						extensions.put(point, extClass);
					}
				} else
					log
					        .warn("'point' and 'class' are required for extensions. Given '" + point + "' and '" + extClass
					                + "'");
				i++;
			}
		}
		
		return extensions;
		
	}
	
	/**
	 * load in messages
	 * 
	 * @param root
	 * @param configVersion
	 * @return
	 */
	private Map<String, Properties> getMessages(Element root, String configVersion, JarFile jarfile) {
		
		Map<String, Properties> messages = new HashMap<String, Properties>();
		
		NodeList messageNodes = root.getElementsByTagName("messages");
		if (messageNodes.getLength() > 0) {
			log.debug("# message nodes: " + messageNodes.getLength());
			int i = 0;
			while (i < messageNodes.getLength()) {
				Node node = messageNodes.item(i);
				NodeList nodes = node.getChildNodes();
				int x = 0;
				String lang = "", file = "";
				while (x < nodes.getLength()) {
					Node childNode = nodes.item(x);
					if ("lang".equals(childNode.getNodeName()))
						lang = childNode.getTextContent();
					else if ("file".equals(childNode.getNodeName()))
						file = childNode.getTextContent();
					x++;
				}
				log.debug("lang: " + lang + " file: " + file);
				
				// lang and file are required
				if (lang.length() > 0 && file.length() > 0) {
					InputStream inStream = null;
					try {
						ZipEntry entry = jarfile.getEntry(file);
						inStream = jarfile.getInputStream(entry);
						Properties props = new Properties();
						props.load(inStream);
						messages.put(lang, props);
					}
					catch (IOException e) {
						log.warn("Unable to load properties: " + file);
					}
					finally {
						if (inStream != null) {
							try {
								inStream.close();
							}
							catch (IOException io) {
								log.error("Error while closing property input stream for module: "
								        + moduleFile.getAbsolutePath(), io);
							}
						}
					}
				} else
					log.warn("'lang' and 'file' are required for extensions. Given '" + lang + "' and '" + file + "'");
				i++;
			}
		}
		
		return messages;
	}
	
	/**
	 * load in required privileges
	 * 
	 * @param root
	 * @param version
	 * @return
	 */
	private List<Privilege> getPrivileges(Element root, String version) {
		
		List<Privilege> privileges = new Vector<Privilege>();
		
		NodeList privNodes = root.getElementsByTagName("privilege");
		if (privNodes.getLength() > 0) {
			log.debug("# privileges: " + privNodes.getLength());
			int i = 0;
			while (i < privNodes.getLength()) {
				Node node = privNodes.item(i);
				NodeList nodes = node.getChildNodes();
				int x = 0;
				String name = "", description = "";
				while (x < nodes.getLength()) {
					Node childNode = nodes.item(x);
					if ("name".equals(childNode.getNodeName()))
						name = childNode.getTextContent();
					else if ("description".equals(childNode.getNodeName()))
						description = childNode.getTextContent();
					x++;
				}
				log.debug("name: " + name + " description: " + description);
				
				// name and desc are required
				if (name.length() > 0 && description.length() > 0)
					privileges.add(new Privilege(name, description));
				else
					log.warn("'name' and 'description' are required for privileges. Given '" + name + "' and '"
					        + description + "'");
				
				i++;
			}
		}
		
		return privileges;
	}
	
	/**
	 * load in required global properties and defaults
	 * 
	 * @param root
	 * @param version
	 * @return
	 */
	private List<GlobalProperty> getGlobalProperties(Element root, String version) {
		
		List<GlobalProperty> properties = new Vector<GlobalProperty>();
		
		NodeList propNodes = root.getElementsByTagName("globalProperty");
		if (propNodes.getLength() > 0) {
			log.debug("# global props: " + propNodes.getLength());
			int i = 0;
			while (i < propNodes.getLength()) {
				Node node = propNodes.item(i);
				NodeList nodes = node.getChildNodes();
				int x = 0;
				String property = "", defaultValue = "", description = "";
				while (x < nodes.getLength()) {
					Node childNode = nodes.item(x);
					if ("property".equals(childNode.getNodeName()))
						property = childNode.getTextContent();
					else if ("defaultValue".equals(childNode.getNodeName()))
						defaultValue = childNode.getTextContent();
					else if ("description".equals(childNode.getNodeName()))
						description = childNode.getTextContent();
					
					x++;
				}
				log.debug("property: " + property + " defaultValue: " + defaultValue + " description: " + description);
				
				// remove tabs from description and trim start/end whitespace
				if (description != null)
					description = description.replaceAll("	", "").trim();
				
				// name is required
				if (property.length() > 0)
					properties.add(new GlobalProperty(property, defaultValue, description));
				else
					log.warn("'property' is required for global properties. Given '" + property + "'");
				
				i++;
			}
		}
		
		return properties;
	}
	
	/**
	 * Load in the defined mapping file names
	 * 
	 * @param rootNode
	 * @param configVersion
	 * @param jarfile
	 * @return
	 */
	private List<String> getMappingFiles(Element rootNode, String configVersion, JarFile jarfile) {
		String mappingString = getElement(rootNode, configVersion, "mappingFiles");
		List<String> mappings = new Vector<String>();
		for (String s : mappingString.split("\\s")) {
			String s2 = s.trim();
			if (s2.length() > 0)
				mappings.add(s2);
		}
		return mappings;
	}
}

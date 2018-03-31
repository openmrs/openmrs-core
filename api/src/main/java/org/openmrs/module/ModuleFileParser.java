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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.Privilege;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This class will parse a file into an org.openmrs.module.Module object
 */
public class ModuleFileParser {
	
	private static final Logger log = LoggerFactory.getLogger(ModuleFileParser.class);
	
	private static final String MODULE_CONFIG_XML_FILENAME = "config.xml";
	
	/**
	 * List out all of the possible version numbers for config files that openmrs has DTDs for.
	 * These are usually stored at http://resources.openmrs.org/doctype/config-x.x.dt
	 */
	private static List<String> validConfigVersions = new ArrayList<>();
	
	static {
		validConfigVersions.add("1.0");
		validConfigVersions.add("1.1");
		validConfigVersions.add("1.2");
		validConfigVersions.add("1.3");
		validConfigVersions.add("1.4");
		validConfigVersions.add("1.5");
		validConfigVersions.add("1.6");
	}
	
	private File moduleFile;
	
	/**
	 * Constructor
	 *
	 * @param moduleFile the module (jar)file that will be parsed
	 */
	public ModuleFileParser(File moduleFile) {
		if (moduleFile == null) {
			throw new ModuleException(Context.getMessageSourceService().getMessage("Module.error.fileCannotBeNull"));
		}
		
		if (!moduleFile.getName().endsWith(".omod")) {
			throw new ModuleException(Context.getMessageSourceService().getMessage("Module.error.invalidFileExtension"),
			        moduleFile.getName());
		}
		
		this.moduleFile = moduleFile;
	}
	
	/**
	 * Convenience constructor to parse the given inputStream file into an omod. <br>
	 * This copies the stream into a temporary file just so things can be parsed.<br>
	 *
	 * @param inputStream the inputStream pointing to an omod file
	 */
	public ModuleFileParser(InputStream inputStream) {
		moduleFile = createTempFile("moduleUpgrade", "omod");
		copyInputStreamToFile(inputStream, moduleFile);
	}

	private File createTempFile(String prefix, String suffix) {
		File file;
		try {
			file = File.createTempFile(prefix, suffix);
		}
		catch (IOException e) {
			throw new ModuleException(Context.getMessageSourceService().getMessage("Module.error.cannotCreateFile"), e);
		}
		return file;
	}

	private void copyInputStreamToFile(InputStream inputStream, File file) {
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			OpenmrsUtil.copyFile(inputStream, outputStream);
		}
		catch (IOException e) {
			throw new ModuleException(Context.getMessageSourceService().getMessage("Module.error.cannotCreateFile"), e);
		}
		finally {
			try {
				inputStream.close();
			}
			catch (Exception e) { /* pass */}
		}
	}

	
	ModuleFileParser() {
	}
	
	/**
	 * Get the module
	 *
	 * @return new module object
	 */
	public Module parse() throws ModuleException {
		
		return createModule(getModuleConfigXml());
	}

	private Document getModuleConfigXml() {
		Document config;
		try (JarFile jarfile = new JarFile(moduleFile)) {
			ZipEntry configEntry = getConfigXmlZipEntry(jarfile);
			config = parseConfigXml(jarfile, configEntry);
		}
		catch (IOException e) {
			throw new ModuleException(Context.getMessageSourceService().getMessage("Module.error.cannotGetJarFile"),
				moduleFile.getName(), e);
		}
		return config;
	}

	private ZipEntry getConfigXmlZipEntry(JarFile jarfile) {
		ZipEntry config = jarfile.getEntry(MODULE_CONFIG_XML_FILENAME);
		if (config == null) {
			throw new ModuleException(Context.getMessageSourceService().getMessage("Module.error.noConfigFile"),
				moduleFile.getName());
		}
		return config;
	}
	
	private Document parseConfigXml(JarFile jarfile, ZipEntry configEntry) {
		Document config;
		try (InputStream configStream = jarfile.getInputStream(configEntry)) {
			config = parseConfigXmlStream(configStream);
		}
		catch (IOException e) {
			throw new ModuleException(Context.getMessageSourceService().getMessage(
				"Module.error.cannotGetConfigFileStream"), moduleFile.getName(), e);
		}
		return config;
	}
	
	private Document parseConfigXmlStream(InputStream configStream) {
		Document config;
		try {
			DocumentBuilder db = newDocumentBuilder();
			config = db.parse(configStream);
		}
		catch (Exception e) {
			log.error("Error parsing " + MODULE_CONFIG_XML_FILENAME + ": " + configStream.toString(), e);

			String output = "";
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				// Now copy bytes from the URL to the output stream
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = configStream.read(buffer)) != -1) {
					out.write(buffer, 0, bytesRead);
				}
				output = out.toString(StandardCharsets.UTF_8.name());
			}
			catch (Exception e2) {
				log.warn("Another error parsing " + MODULE_CONFIG_XML_FILENAME, e2);
			}

			log.error("{} content: {}", MODULE_CONFIG_XML_FILENAME, output);
			throw new ModuleException(
				Context.getMessageSourceService().getMessage("Module.error.cannotParseConfigFile"), moduleFile
				.getName(), e);
		}
		return config;
	}

	private DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		db.setEntityResolver((publicId, systemId) -> {
			// When asked to resolve external entities (such as a
			// DTD) we return an InputSource
			// with no data at the end, causing the parser to ignore
			// the DTD.
			return new InputSource(new StringReader(""));
		});
		return db;
	}

	private Module createModule(Document config) {
		Element rootNode = config.getDocumentElement();

		String configVersion = ensureValidModuleConfigVersion(rootNode);
		
		String name = ensureNonEmptyName(rootNode);
		String moduleId = ensureNonEmptyId(rootNode, name);
		String packageName = ensureNonEmptyPackage(rootNode, name);
		
		String author = getElementTrimmed(rootNode, "author");
		String desc = getElementTrimmed(rootNode, "description");
		String version = getElementTrimmed(rootNode, "version");

		Module module = new Module(name, moduleId, packageName, author, desc, version);

		module.setActivatorName(getElementTrimmed(rootNode, "activator"));
		module.setRequireDatabaseVersion(getElementTrimmed(rootNode, "require_database_version"));
		module.setRequireOpenmrsVersion(getElementTrimmed(rootNode, "require_version"));
		module.setUpdateURL(getElementTrimmed(rootNode, "updateURL"));
		
		module.setRequiredModulesMap(getRequiredModules(rootNode));
		module.setAwareOfModulesMap(getAwareOfModules(rootNode));
		module.setStartBeforeModulesMap(getStartBeforeModules(rootNode));
		module.setAdvicePoints(getAdvice(rootNode,  module));
		module.setExtensionNames(getExtensions(rootNode));
		module.setPrivileges(getPrivileges(rootNode));
		module.setGlobalProperties(getGlobalProperties(rootNode));
		module.setMappingFiles(getMappingFiles(rootNode));
		module.setPackagesWithMappedClasses(getPackagesWithMappedClasses(rootNode));

		module.setConfig(config);

		module.setMandatory(getMandatory(rootNode, configVersion));

		module.setFile(moduleFile);

		module.setConditionalResources(getConditionalResources(rootNode));
		return module;
	}
	
	private String ensureValidModuleConfigVersion(Element rootNode) {
		String configVersion = rootNode.getAttribute("configVersion").trim();
		validateModuleConfigVersion(configVersion);
		return configVersion;
	}
	
	private void validateModuleConfigVersion(String version) {
		if (!validConfigVersions.contains(version)) {
			throw new ModuleException(Context.getMessageSourceService().getMessage("Module.error.invalidConfigVersion",
			    new Object[] { version, String.join(", ", validConfigVersions) }, Context.getLocale()), moduleFile.getName());
		}
	}

	private String ensureNonEmptyName(Element rootNode) {
		return getTrimmedElementOrFail(rootNode, "name", "Module.error.nameCannotBeEmpty", moduleFile.getName());
	}

	private String ensureNonEmptyId(Element rootNode, String name) {
		return getTrimmedElementOrFail(rootNode, "id", "Module.error.idCannotBeEmpty", name);
	}

	private String ensureNonEmptyPackage(Element rootNode, String name) {
		return getTrimmedElementOrFail(rootNode, "package", "Module.error.packageCannotBeEmpty", name);
	}

	/**
	 * Parses conditionalResources tag.
	 * @param rootNode
	 * @return
	 *
	 * @should parse openmrsVersion and modules
	 * @should parse conditionalResource with whitespace
	 * @should throw exception if multiple conditionalResources tags found
	 * @should throw exception if conditionalResources contains invalid tag
	 * @should throw exception if path is blank
	 */
	List<ModuleConditionalResource> getConditionalResources(Element rootNode) {
		List<ModuleConditionalResource> conditionalResources = new ArrayList<>();
		
		NodeList parentConditionalResources = rootNode.getElementsByTagName("conditionalResources");
		
		if (parentConditionalResources.getLength() == 0) {
			return new ArrayList<>();
		} else if (parentConditionalResources.getLength() > 1) {
			throw new IllegalArgumentException("Found multiple conditionalResources tags. There can be only one.");
		}
		
		NodeList conditionalResourcesNode = parentConditionalResources.item(0).getChildNodes();
		
		for (int i = 0; i < conditionalResourcesNode.getLength(); i++) {
			Node conditionalResourceNode = conditionalResourcesNode.item(i);
			
			if ("#text".equals(conditionalResourceNode.getNodeName())) {
				continue; //ignore text and whitespace in particular
			}
			
			if (!"conditionalResource".equals(conditionalResourceNode.getNodeName())) {
				throw new IllegalArgumentException("Found the " + conditionalResourceNode.getNodeName()
				        + " node under conditionalResources. Only conditionalResource is allowed.");
			}
			
			NodeList resourceElements = conditionalResourceNode.getChildNodes();
			
			ModuleConditionalResource resource = new ModuleConditionalResource();
			conditionalResources.add(resource);
			
			for (int j = 0; j < resourceElements.getLength(); j++) {
				Node resourceElement = resourceElements.item(j);
				
				if ("path".equals(resourceElement.getNodeName())) {
					if (StringUtils.isBlank(resourceElement.getTextContent())) {
						throw new IllegalArgumentException("The path of a conditional resource must not be blank");
					}
					resource.setPath(resourceElement.getTextContent());
				} else if ("openmrsVersion".equals(resourceElement.getNodeName())) {
					if (StringUtils.isBlank(resource.getOpenmrsPlatformVersion())) {
						resource.setOpenmrsPlatformVersion(resourceElement.getTextContent());
					}
				} else if ("openmrsPlatformVersion".equals(resourceElement.getNodeName())) {
					resource.setOpenmrsPlatformVersion(resourceElement.getTextContent());
				} else if ("modules".equals(resourceElement.getNodeName())) {
					NodeList modulesNode = resourceElement.getChildNodes();
					for (int k = 0; k < modulesNode.getLength(); k++) {
						Node moduleNode = modulesNode.item(k);
						if ("module".equals(moduleNode.getNodeName())) {
							NodeList moduleElements = moduleNode.getChildNodes();
							
							ModuleConditionalResource.ModuleAndVersion module = new ModuleConditionalResource.ModuleAndVersion();
							resource.getModules().add(module);
							for (int m = 0; m < moduleElements.getLength(); m++) {
								Node moduleElement = moduleElements.item(m);
								
								if ("moduleId".equals(moduleElement.getNodeName())) {
									module.setModuleId(moduleElement.getTextContent());
								} else if ("version".equals(moduleElement.getNodeName())) {
									module.setVersion(moduleElement.getTextContent());
								}
							}
						}
					}
				}
			}
		}
		
		return conditionalResources;
	}
	
	/**
	 * load in required modules list
	 *
	 * @param root element in the xml doc object
	 * @return map from module package name to required version
	 * @since 1.5
	 */
	private Map<String, String> getRequiredModules(Element root) {
		return getModuleToVersionMap("require_modules", "require_module", root);
	}
	
	/**
	 * load in list of modules we are aware of.
	 *
	 * @param root element in the xml doc object
	 * @return map from module package name to aware of version
	 * @since 1.9
	 */
	private Map<String, String> getAwareOfModules(Element root) {
		return getModuleToVersionMap("aware_of_modules", "aware_of_module", root);
	}
	
	private Map<String, String> getStartBeforeModules(Element root) {
		return getModuleToVersionMap("start_before_modules", "module", root);
	}
	
	private Map<String, String> getModuleToVersionMap(String elementParentName, String elementName, Element root) {
		
		NodeList modulesParents = root.getElementsByTagName(elementParentName);
		
		Map<String, String> packageNamesToVersion = new HashMap<>();
		
		if (modulesParents.getLength() > 0) {
			Node modulesParent = modulesParents.item(0);
			
			NodeList childModules = modulesParent.getChildNodes();
			
			int i = 0;
			while (i < childModules.getLength()) {
				Node n = childModules.item(i);
				if (n != null && elementName.equals(n.getNodeName())) {
					NamedNodeMap attributes = n.getAttributes();
					Node versionNode = attributes.getNamedItem("version");
					String moduleVersion = versionNode == null ? null : versionNode.getNodeValue();
					packageNamesToVersion.put(n.getTextContent().trim(), moduleVersion);
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
	 * @return
	 */
	private List<AdvicePoint> getAdvice(Element root, Module mod) {
		
		List<AdvicePoint> advicePoints = new ArrayList<>();
		
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
					if ("point".equals(childNode.getNodeName())) {
						point = childNode.getTextContent().trim();
					} else if ("class".equals(childNode.getNodeName())) {
						adviceClass = childNode.getTextContent().trim();
					}
					x++;
				}
				log.debug("point: " + point + " class: " + adviceClass);
				
				// point and class are required
				if (point.length() > 0 && adviceClass.length() > 0) {
					advicePoints.add(new AdvicePoint(mod, point, adviceClass));
				} else {
					log.warn("'point' and 'class' are required for advice. Given '" + point + "' and '" + adviceClass + "'");
				}
				
				i++;
			}
		}
		
		return advicePoints;
	}
	
	/**
	 * load in extensions
	 *
	 * @param root
	 * @return
	 */
	private IdentityHashMap<String, String> getExtensions(Element root) {
		
		IdentityHashMap<String, String> extensions = new IdentityHashMap<>();
		
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
					if ("point".equals(childNode.getNodeName())) {
						point = childNode.getTextContent().trim();
					} else if ("class".equals(childNode.getNodeName())) {
						extClass = childNode.getTextContent().trim();
					}
					x++;
				}
				log.debug("point: " + point + " class: " + extClass);
				
				// point and class are required
				if (point.length() > 0 && extClass.length() > 0) {
					if (point.contains(Extension.extensionIdSeparator)) {
						log.warn("Point id contains illegal character: '" + Extension.extensionIdSeparator + "'");
					} else {
						extensions.put(point, extClass);
					}
				} else {
					log
					        .warn("'point' and 'class' are required for extensions. Given '" + point + "' and '" + extClass
					                + "'");
				}
				i++;
			}
		}
		
		return extensions;
		
	}
		
	/**
	 * load in required privileges
	 *
	 * @param root
	 * @return
	 */
	private List<Privilege> getPrivileges(Element root) {
		
		List<Privilege> privileges = new ArrayList<>();
		
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
					if ("name".equals(childNode.getNodeName())) {
						name = childNode.getTextContent().trim();
					} else if ("description".equals(childNode.getNodeName())) {
						description = childNode.getTextContent().trim();
					}
					x++;
				}
				log.debug("name: " + name + " description: " + description);
				
				// name and desc are required
				if (name.length() > 0 && description.length() > 0) {
					privileges.add(new Privilege(name, description));
				} else {
					log.warn("'name' and 'description' are required for privileges. Given '" + name + "' and '"
					        + description + "'");
				}
				
				i++;
			}
		}
		
		return privileges;
	}
	
	/**
	 * load in required global properties and defaults
	 *
	 * @param root
	 * @return
	 */
	private List<GlobalProperty> getGlobalProperties(Element root) {
		
		List<GlobalProperty> result = new ArrayList<>();
		
		NodeList propNodes = root.getElementsByTagName("globalProperty");
		if (propNodes.getLength() == 0) {
			return result;
		}
		
		log.debug("# global props: " + propNodes.getLength());
		int i = 0;
		while (i < propNodes.getLength()) {
			Node node = propNodes.item(i);
			GlobalProperty globalProperty = extractGlobalProperty(node);
			
			if (globalProperty != null) {
				result.add(globalProperty);
			}
			
			i++;
		}
		
		return result;
	}

	private GlobalProperty extractGlobalProperty(Node node) {
		GlobalProperty globalProperty = null;
		NodeList nodes = node.getChildNodes();
		int x = 0;
		String property = "", defaultValue = "", description = "", datatypeClassname = "", datatypeConfig = "";
		while (x < nodes.getLength()) {
			Node childNode = nodes.item(x);
			if ("property".equals(childNode.getNodeName())) {
				property = childNode.getTextContent().trim();
			} else if ("defaultValue".equals(childNode.getNodeName())) {
				defaultValue = childNode.getTextContent();
			} else if ("description".equals(childNode.getNodeName())) {
				description = childNode.getTextContent().trim();
			} else if ("datatypeClassname".equals(childNode.getNodeName())) {
				datatypeClassname = childNode.getTextContent().trim();
			} else if ("datatypeConfig".equals(childNode.getNodeName())) {
				datatypeConfig = childNode.getTextContent().trim();
			}

			x++;
		}
		log.debug("property: " + property + " defaultValue: " + defaultValue + " description: " + description);
		log.debug("datatypeClassname: " + datatypeClassname + " datatypeConfig: " + datatypeConfig);

		// remove tabs from description and trim start/end whitespace
		if (description != null) {
			description = description.replaceAll("	", "").trim();
		}

		// name is required
		if (datatypeClassname.length() > 0 && property.length() > 0) {
			try {
				Class<CustomDatatype<?>> datatypeClazz = (Class<CustomDatatype<?>>) Class.forName(datatypeClassname)
					.asSubclass(CustomDatatype.class);
				globalProperty = new GlobalProperty(property, defaultValue, description, datatypeClazz, datatypeConfig);
			}
			catch (ClassCastException ex) {
				log.error("The class specified by 'datatypeClassname' (" + datatypeClassname
					+ ") must be a subtype of 'org.openmrs.customdatatype.CustomDatatype<?>'.", ex);
			}
			catch (ClassNotFoundException ex) {
				log.error("The class specified by 'datatypeClassname' (" + datatypeClassname
					+ ") could not be found.", ex);
			}
		} else if (property.length() > 0) {
			globalProperty = new GlobalProperty(property, defaultValue, description);
		} else {
			log.warn("'property' is required for global properties. Given '" + property + "'");
		}
		return globalProperty;
	}

	private List<String> getMappingFiles(Element rootNode) {
		List<String> result = new ArrayList<>();
		splitTagContentByWhitespace(rootNode, "mappingFiles", result);
		return result;
	}

	private Set<String> getPackagesWithMappedClasses(Element rootNode) {
		Set<String> result = new HashSet<>();
		splitTagContentByWhitespace(rootNode, "packagesWithMappedClasses", result);
		return result;
	}

	private Collection<String> splitTagContentByWhitespace(Element rootNode, String tag, Collection<String> result) {
		String content = getElement(rootNode, tag);
		for (String s : content.split("\\s")) {
			String s2 = s.trim();
			if (s2.length() > 0) {
				result.add(s2);
			}
		}
		return result;
	}
	
	private String getTrimmedElementOrFail(Element rootNode, String elementName, String errorMessageKey, String moduleName) {
		String element = getElementTrimmed(rootNode, elementName);
		if (element == null || element.length() == 0) {
			throw new ModuleException(Context.getMessageSourceService().getMessage(errorMessageKey),
				moduleName);
		}
		return element;
	}

	private String getElementTrimmed(Element element, String name) {
		return getElement(element, name).trim();
	}

	private String getElement(Element root, String tag) {
		if (root.getElementsByTagName(tag).getLength() > 0) {
			return root.getElementsByTagName(tag).item(0).getTextContent();
		}
		return "";
	}
	
	/**
	 * Looks for the "<mandatory>" element in the config file and returns true if the value is
	 * exactly "true".
	 *
	 * @param rootNode
	 * @param configVersion
	 * @return true if the mandatory element is set to true
	 */
	private boolean getMandatory(Element rootNode, String configVersion) {
		if (Double.parseDouble(configVersion) >= 1.3) {
			String mandatory = getElementTrimmed(rootNode, "mandatory");
			return "true".equalsIgnoreCase(mandatory);
		}
		
		return false; // this module has an older config file
	}
}

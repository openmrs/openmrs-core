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
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.Privilege;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This class will parse an OpenMRS module, specifically its {@code config.xml} file into a {@link org.openmrs.module.Module} object.
 * <p>Typical usage is:
 * <ol>
 * <li>Create a {@code ModuleFileParser} with {@link #ModuleFileParser(MessageSourceService)}.
 * <li>Parse the module by passing the file to {@link #parse(File)}.</li>
 * </ol>
 * Note that the parser does not validate the {@code config.xml} file against the document type definition's (DTD).
 */
public class ModuleFileParser {
	
	private static final Logger log = LoggerFactory.getLogger(ModuleFileParser.class);

	private static final String MODULE_CONFIG_XML_FILENAME = "config.xml";

	private static final String OPENMRS_MODULE_FILE_EXTENSION = ".omod";
	
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
	
	// TODO - remove this field once ModuleFileParser(File), ModuleFileParser(InputStream) are removed.
	// There is no need to keep the file as state since moduleFileParser.parse(File) does not need it.
	// this is also why all private methods that need access to the file for parsing or error message get it as a parameter
	private File moduleFile;

	private MessageSourceService messageSourceService;

	/**
	 * Creates a ModuleFileParser.
	 *
	 * @param messageSourceService the message source used for error messages
	 * @since 2.2.0
	 */
	public ModuleFileParser(MessageSourceService messageSourceService) {
		this.messageSourceService = Objects.requireNonNull(messageSourceService, "messageSourceService must not be null");
	}
	
	/**
	 * Constructor
	 *
	 * @param moduleFile the module (jar)file that will be parsed
	 * @deprecated since 2.2.0 use {@link #ModuleFileParser(MessageSourceService)}
	 */
	@Deprecated
	public ModuleFileParser(File moduleFile) {
		this.messageSourceService = Context.getMessageSourceService();
		validateFileIsNotNull(moduleFile);
		validateFileHasModuleFileExtension(moduleFile);
		this.moduleFile = moduleFile;
	}

	private void validateFileIsNotNull(File moduleFile) {
		if (moduleFile == null) {
			throw new ModuleException(messageSourceService.getMessage("Module.error.fileCannotBeNull"));
		}
	}

	private void validateFileHasModuleFileExtension(File moduleFile) {
		if (!moduleFile.getName().endsWith(OPENMRS_MODULE_FILE_EXTENSION)) {
			throw new ModuleException(messageSourceService.getMessage("Module.error.invalidFileExtension"),
				moduleFile.getName());
		}
	}

	/**
	 * Convenience constructor to parse the given inputStream file into an omod. <br>
	 * This copies the stream into a temporary file just so things can be parsed.<br>
	 *
	 * @param inputStream the inputStream pointing to an omod file
	 * @deprecated since 2.2.0 use {@link #ModuleFileParser(MessageSourceService)}
	 */
	@Deprecated
	public ModuleFileParser(InputStream inputStream) {
		this.messageSourceService = Context.getMessageSourceService();
		this.moduleFile = createTempFile("moduleUpgrade", OPENMRS_MODULE_FILE_EXTENSION);
		copyInputStreamToFile(inputStream, this.moduleFile);
	}

	/**
	 * Parses the given {@code InputStream} of an OpenMRS module into a {@code Module}.
	 * This copies the stream into a temporary file and close given {@code InputStream}.
	 *
	 * @param inputStream the inputStream pointing to an omod file
	 * @since 2.2.0
	 */
	public Module parse(InputStream inputStream) {
		File moduleFile = createTempFile("moduleUpgrade", OPENMRS_MODULE_FILE_EXTENSION);
		copyInputStreamToFile(inputStream, moduleFile);
		return parse(moduleFile);
	}

	private File createTempFile(String prefix, String suffix) {
		File file;
		try {
			file = File.createTempFile(prefix, suffix);
		}
		catch (IOException e) {
			throw new ModuleException(messageSourceService.getMessage("Module.error.cannotCreateFile"), e);
		}
		return file;
	}

	private void copyInputStreamToFile(InputStream inputStream, File file) {
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			OpenmrsUtil.copyFile(inputStream, outputStream);
		}
		catch (IOException e) {
			throw new ModuleException(messageSourceService.getMessage("Module.error.cannotCreateFile"), e);
		}
		finally {
			try {
				inputStream.close();
			}
			catch (Exception e) { /* pass */}
		}
	}

	/**
	 * This constructor was created for testing purposes and is now deprecated.
	 * DO NOT USE.
	 *
	 * @deprecated since 2.2.0 use {@link #ModuleFileParser(MessageSourceService)}
	 */
	@Deprecated
	ModuleFileParser() {
	}

	/**
	 * Get the module.
	 * If you use this method only do so together with {@link #ModuleFileParser(File)} or {@link #ModuleFileParser(InputStream)}.
	 * Best use {@link #ModuleFileParser(MessageSourceService)} and {@link #parse(File)}
	 * since this method is deprecated.
	 *
	 * @return new module object
	 * @deprecated since 2.2.0 use {@link #parse(File)}
	 */
	@Deprecated
	public Module parse() throws ModuleException {
		return parse(this.moduleFile);
	}

	/**
	 * Get the module from an OpenMRS module file.
	 * 
	 * @param moduleFile the module file to be parsed
	 * @return new module object
	 * @since 2.2.0
	 */
	public Module parse(File moduleFile) {
		validateFileIsNotNull(moduleFile);
		validateFileHasModuleFileExtension(moduleFile);
		return createModule(getModuleConfigXml(moduleFile), moduleFile);
	}

	private Document getModuleConfigXml(File moduleFile) {
		Document config;
		try (JarFile jarfile = new JarFile(moduleFile)) {
			ZipEntry configEntry = getConfigXmlZipEntry(jarfile, moduleFile);
			config = parseConfigXml(jarfile, configEntry, moduleFile);
		}
		catch (IOException e) {
			throw new ModuleException(messageSourceService.getMessage("Module.error.cannotGetJarFile"),
				moduleFile.getName(), e);
		}
		return config;
	}

	private ZipEntry getConfigXmlZipEntry(JarFile jarfile, File moduleFile) {
		ZipEntry config = jarfile.getEntry(MODULE_CONFIG_XML_FILENAME);
		if (config == null) {
			throw new ModuleException(messageSourceService.getMessage("Module.error.noConfigFile"),
				moduleFile.getName());
		}
		return config;
	}

	private Document parseConfigXml(JarFile jarfile, ZipEntry configEntry, File moduleFile) {
		Document config;
		try (InputStream configStream = jarfile.getInputStream(configEntry)) {
			config = parseConfigXmlStream(configStream, moduleFile);
		}
		catch (IOException e) {
			throw new ModuleException(messageSourceService.getMessage(
				"Module.error.cannotGetConfigFileStream"), moduleFile.getName(), e);
		}
		return config;
	}

	private Document parseConfigXmlStream(InputStream configStream, File moduleFile) {
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
			throw new ModuleException(messageSourceService.getMessage("Module.error.cannotParseConfigFile"),
				moduleFile.getName(), e);
		}
		return config;
	}

	private DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();

		// When asked to resolve external entities (such as a
		// DTD) we return an InputSource
		// with no data at the end, causing the parser to ignore
		// the DTD.
		db.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
		return db;
	}

	private Module createModule(Document config, File moduleFile) {
		Element configRoot = config.getDocumentElement();

		String configVersion = ensureValidModuleConfigVersion(configRoot, moduleFile);
		
		String name = ensureNonEmptyName(configRoot, moduleFile);
		String moduleId = ensureNonEmptyId(configRoot, name);
		String packageName = ensureNonEmptyPackage(configRoot, name);
		
		String author = getElementTrimmed(configRoot, "author");
		String desc = getElementTrimmed(configRoot, "description");
		String version = getElementTrimmed(configRoot, "version");

		Module module = new Module(name, moduleId, packageName, author, desc, version);

		module.setActivatorName(getElementTrimmed(configRoot, "activator"));
		module.setRequireDatabaseVersion(getElementTrimmed(configRoot, "require_database_version"));
		module.setRequireOpenmrsVersion(getElementTrimmed(configRoot, "require_version"));
		module.setUpdateURL(getElementTrimmed(configRoot, "updateURL"));

		module.setRequiredModulesMap(extractRequiredModules(configRoot));
		module.setAwareOfModulesMap(extractAwareOfModules(configRoot));
		module.setStartBeforeModulesMap(extractStartBeforeModules(configRoot));
		module.setAdvicePoints(extractAdvice(configRoot, module));
		module.setExtensionNames(extractExtensions(configRoot));
		module.setPrivileges(extractPrivileges(configRoot));
		module.setGlobalProperties(extractGlobalProperties(configRoot));
		module.setMappingFiles(extractMappingFiles(configRoot));
		module.setPackagesWithMappedClasses(extractPackagesWithMappedClasses(configRoot));
		module.setMandatory(extractMandatory(configRoot, configVersion));
		module.setConditionalResources(extractConditionalResources(configRoot));

		module.setConfig(config);
		module.setFile(moduleFile);

		return module;
	}

	private String ensureValidModuleConfigVersion(Element configRoot, File moduleFile) {
		String configVersion = configRoot.getAttribute("configVersion").trim();
		validateModuleConfigVersion(configVersion, moduleFile);
		return configVersion;
	}

	private void validateModuleConfigVersion(String version, File moduleFile) {
		if (!validConfigVersions.contains(version)) {
			throw new ModuleException(Context.getMessageSourceService().getMessage("Module.error.invalidConfigVersion",
				new Object[] { version, String.join(", ", validConfigVersions) }, Context.getLocale()),
				moduleFile.getName());
		}
	}

	private String ensureNonEmptyName(Element configRoot, File moduleFile) {
		return getTrimmedElementOrFail(configRoot, "name", "Module.error.nameCannotBeEmpty", moduleFile.getName());
	}

	private String ensureNonEmptyId(Element configRoot, String name) {
		return getTrimmedElementOrFail(configRoot, "id", "Module.error.idCannotBeEmpty", name);
	}

	private String ensureNonEmptyPackage(Element configRoot, String name) {
		return getTrimmedElementOrFail(configRoot, "package", "Module.error.packageCannotBeEmpty", name);
	}

	/**
	 * load in required modules list
	 *
	 * @return map from module package name to required version
	 * @since 1.5
	 */
	private Map<String, String> extractRequiredModules(Element configRoot) {
		return extractModulesWithVersionAttribute(configRoot, "require_module", "require_modules");
	}
	
	/**
	 * load in list of modules we are aware of.
	 *
	 * @return map from module package name to aware of version
	 * @since 1.9
	 */
	private Map<String, String> extractAwareOfModules(Element configRoot) {
		return extractModulesWithVersionAttribute(configRoot, "aware_of_module", "aware_of_modules");
	}
	
	private Map<String, String> extractStartBeforeModules(Element configRoot) {
		return extractModulesWithVersionAttribute(configRoot, "module", "start_before_modules");
	}

	private Map<String, String> extractModulesWithVersionAttribute(Element configRoot, String elementName,
		String elementParentName) {
		
		NodeList parents = configRoot.getElementsByTagName(elementParentName);
		
		Map<String, String> result = new HashMap<>();
		if (parents.getLength() == 0) {
			return result;
		}
		
		Element firstParent = (Element) parents.item(0);
		NodeList children = firstParent.getElementsByTagName(elementName);
		
		int i = 0;
		while (i < children.getLength()) {
			Element child = (Element) children.item(i);
			Attr versionAttribute = child.getAttributeNode("version");
			String version = versionAttribute == null ? null : versionAttribute.getValue();
			result.put(child.getTextContent().trim(), version);
			i++;
		}
		return result;
	}

	private List<AdvicePoint> extractAdvice(Element configRoot, Module module) {

		List<AdvicePoint> result = new ArrayList<>();

		NodeList advice = configRoot.getElementsByTagName("advice");
		if (advice.getLength() == 0) {
			return result;
		}

		log.debug("# advice: {}", advice.getLength());
		int i = 0;
		while (i < advice.getLength()) {
			Element element = (Element) advice.item(i);
			String point = getElementTrimmed(element, "point");
			String adviceClass = getElementTrimmed(element, "class");
			log.debug("advice point: {}, class: {}", point, adviceClass);

			if (point.isEmpty() || adviceClass.isEmpty()) {
				log.warn("'point' and 'class' are required for advice. Given '{}' and '{}'", point, adviceClass);
			} else {
				result.add(new AdvicePoint(module, point, adviceClass));
			}
			i++;
		}

		return result;
	}

	private Map<String, String> extractExtensions(Element configRoot) {

		Map<String, String> result = new IdentityHashMap<>();

		NodeList extensions = configRoot.getElementsByTagName("extension");
		if (extensions.getLength() == 0) {
			return result;
		}

		log.debug("# extensions: {}", extensions.getLength());
		int i = 0;
		while (i < extensions.getLength()) {
			Element element = (Element) extensions.item(i);
			String point = getElementTrimmed(element, "point");
			String extClass = getElementTrimmed(element, "class");
			log.debug("extension point: {}, class: {}", point, extClass);

			if (point.isEmpty() || extClass.isEmpty()) {
				log.warn("'point' and 'class' are required for extensions. Given '{}' and '{}'", point, extClass);
			} else if (point.contains(Extension.EXTENSION_ID_SEPARATOR)) {
				log.warn("Point id contains illegal character: '{}'", Extension.EXTENSION_ID_SEPARATOR);
			} else {
				result.put(point, extClass);
			}
			i++;
		}
		return result;
	}

	private List<Privilege> extractPrivileges(Element configRoot) {

		List<Privilege> result = new ArrayList<>();

		NodeList privileges = configRoot.getElementsByTagName("privilege");
		if (privileges.getLength() == 0) {
			return result;
		}

		log.debug("# privileges: {}", privileges.getLength());
		int i = 0;
		while (i < privileges.getLength()) {
			Element element = (Element) privileges.item(i);
			String name = getElementTrimmed(element, "name");
			String description = getElementTrimmed(element, "description");
			log.debug("extension name: {}, description: {}", name, description);

			if (name.isEmpty() || description.isEmpty()) {
				log.warn("'name' and 'description' are required for privilege. Given '{}' and '{}'", name, description);
			} else {
				result.add(new Privilege(name, description));
			}
			i++;
		}
		return result;
	}
	
	private List<GlobalProperty> extractGlobalProperties(Element configRoot) {
		
		List<GlobalProperty> result = new ArrayList<>();
		
		NodeList propNodes = configRoot.getElementsByTagName("globalProperty");
		if (propNodes.getLength() == 0) {
			return result;
		}
		
		log.debug("# global properties: {}", propNodes.getLength());
		int i = 0;
		while (i < propNodes.getLength()) {
			Element gpElement = (Element) propNodes.item(i);
			GlobalProperty globalProperty = extractGlobalProperty(gpElement);
			
			if (globalProperty != null) {
				result.add(globalProperty);
			}
			
			i++;
		}
		
		return result;
	}

	private GlobalProperty extractGlobalProperty(Element element) {
		String property = getElementTrimmed(element, "property");
		String defaultValue = getElementTrimmed(element, "defaultValue");
		String description = removeTabsAndTrim(getElementTrimmed(element, "description"));
		String datatypeClassname = getElementTrimmed(element, "datatypeClassname");
		String datatypeConfig = getElementTrimmed(element, "datatypeConfig");
		
		log.debug("property: {}, defaultValue: {}", property, defaultValue);
		log.debug("description: {}, datatypeClassname: {}", description, datatypeClassname);
		log.debug("datatypeConfig: {}", datatypeConfig);

		return createGlobalProperty(property, defaultValue, description, datatypeClassname,
			datatypeConfig);
	}

	private String removeTabsAndTrim(String string) {
		return string.replaceAll("	", "").trim();
	}

	private GlobalProperty createGlobalProperty(String property, String defaultValue, String description,
		String datatypeClassname, String datatypeConfig) {

		GlobalProperty globalProperty = null;
		if (property.isEmpty()) {
			log.warn("'property' is required for global properties. Given '{}'", property);
			return globalProperty;
		}

		if (!datatypeClassname.isEmpty()) {
			globalProperty = createGlobalPropertyWithDatatype(property, defaultValue, description, datatypeClassname,
				datatypeConfig);
		} else {
			globalProperty = new GlobalProperty(property, defaultValue, description);
		}
		return globalProperty;
	}

	private GlobalProperty createGlobalPropertyWithDatatype(String property, String defaultValue, String description,
		String datatypeClassname, String datatypeConfig) {
		GlobalProperty globalProperty = null;
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
		return globalProperty;
	}

	private List<String> extractMappingFiles(Element configRoot) {
		List<String> result = new ArrayList<>();
		splitTagContentByWhitespace(configRoot, "mappingFiles", result);
		return result;
	}

	private Set<String> extractPackagesWithMappedClasses(Element configRoot) {
		Set<String> result = new HashSet<>();
		splitTagContentByWhitespace(configRoot, "packagesWithMappedClasses", result);
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
			throw new ModuleException(messageSourceService.getMessage(errorMessageKey),
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
	 */
	private boolean extractMandatory(Element configRoot, String configVersion) {
		if (Double.parseDouble(configVersion) >= 1.3) {
			String mandatory = getElementTrimmed(configRoot, "mandatory");
			return "true".equalsIgnoreCase(mandatory);
		}

		// this module has an older config file
		return false;
	}

	/**
	 * Parses conditionalResources tag.
	 *
	 * <strong>Should</strong> parse openmrsVersion and modules
	 * <strong>Should</strong> parse conditionalResource with whitespace
	 * <strong>Should</strong> throw exception if multiple conditionalResources tags found
	 * <strong>Should</strong> throw exception if conditionalResources contains invalid tag
	 * <strong>Should</strong> throw exception if path is blank
	 */
	List<ModuleConditionalResource> extractConditionalResources(Element configRoot) {
		List<ModuleConditionalResource> conditionalResources = new ArrayList<>();

		NodeList parentConditionalResources = configRoot.getElementsByTagName("conditionalResources");

		if (parentConditionalResources.getLength() == 0) {
			return new ArrayList<>();
		} else if (parentConditionalResources.getLength() > 1) {
			throw new IllegalArgumentException("Found multiple conditionalResources tags. There can be only one.");
		}

		NodeList conditionalResourcesNode = parentConditionalResources.item(0).getChildNodes();

		for (int i = 0; i < conditionalResourcesNode.getLength(); i++) {
			Node conditionalResourceNode = conditionalResourcesNode.item(i);

			if ("#text".equals(conditionalResourceNode.getNodeName())) {
				//ignore text and whitespace in particular
				continue;
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
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.dtd;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ConfigXmlBuilder is a utility class for tests that need to build a configuration XML file for OpenMRS.
 * It provides a fluent API to add various elements to the configuration file.
 *
 * The structure of the XML is dictated by a Document Type Definition (DTD), 
 * the version of which is passed into the constructor of this class.
 *
 * Note: This class is intended for use in testing scenarios only. Do not use it for 
 * generating configuration XML files for production use.
 */
public class ConfigXmlBuilder {
	
	private static final String MODULE_NAME = "module";
	
	private static final String TAG_ID = "id";
	
	private static final String TAG_NAME = "name";
	
	private static final String TAG_VERSION = "version";
	
	private static final String TAG_PACKAGE = "package";
	
	private static final String TAG_AUTHOR = "author";
	
	private static final String TAG_DESCRIPTION = "description";
	
	private static final String TAG_ACTIVATOR = "activator";
	
	private static final String TAG_REQUIRE_MODULES = "require_modules";
	
	private static final String TAG_REQUIRE_MODULE = "require_module";
	
	private static final String TAG_UPDATE_URL = "updateURL";
	
	private static final String TAG_REQUIRE_VERSION = "require_version";
	
	private static final String TAG_REQUIRE_DATABASE_VERSION = "require_database_version";
	
	private static final String TAG_LIBRARY = "library";
	
	private static final String TAG_EXTENSION = "extension";
	
	private static final String TAG_ADVICE = "advice";
	
	private static final String TAG_PRIVILEGE = "privilege";
	
	private static final String TAG_GLOBAL_PROPERTY = "globalProperty";
	
	private static final String TAG_DWR = "dwr";
	
	private static final String TAG_SERVLET = "servlet";
	
	private static final String TAG_MESSAGES = "messages";
	
	private static final String TAG_FILTER = "filter";
	
	private static final String TAG_FILTER_MAPPING = "filter-mapping";
	
	private static final String TAG_AWARE_OF_MODULES = "aware_of_modules";
	
	private static final String TAG_AWARE_OF_MODULE = "aware_of_module";
	
	private static final String TAG_CONDITIONAL_RESOURCES = "conditionalResources";
	
	private static final String TAG_CONDITIONAL_RESOURCE = "conditionalResource";
	
	private static final String TAG_MAPPING_FILES = "mappingFiles";
	
	private static final String TAG_MANDATORY = "mandatory";
	
	private static final String TAG_PACKAGED_WITH_MAPPED_CLASSES = "packagesWithMappedClasses";
	
	private static final String TAG_POINT = "point";
	
	private static final String TAG_CLASS = "class";
	
	private static final String TAG_PROPERTY = "property";
	
	private static final String TAG_DEFAULT_VALUE = "defaultValue";
	
	private static final String TAG_ALLOW ="allow" ;
	
	private static final String TAG_SIGNATURES = "signatures";
	
	private static final String TAG_CREATE = "create";
	
	private static final String TAG_PARAM = "param";
	
	private static final String TAG_INCLUDE = "include";
	
	private static final String TAG_METHOD = "method";
	
	private static final String TAG_CONVERT = "convert";
	
	private static final String TAG_FILTER_NAME = "filter-name";
	
	private static final String TAG_FILTER_CLASS = "filter-class";
	
	private static final String TAG_INIT_PARAM = "init-param";
	
	private static final String TAG_PARAM_NAME = "param-name";
	
	private static final String TAG_URL_PATTERN = "url-pattern";
	
	private static final String TAG_PARAM_VALUE = "param-value";
	
	private static final String TAG_SERVLET_NAME = "servlet-name";
	
	private static final String TAG_PATH = "path";
	
	private static final String TAG_OPENMRS_VERSION = "openmrsVersion";
	
	private static final String TAG_LOAD_MODULES_IF_PRESENT = "loadIfModulesPresent";
	
	private static final String TAG_OPENMRS_MODULE = "openmrsModule";
	
	private static final String TAG_MODULE_ID = "moduleId";
	
	private static final String ATTRIBUTE_ID = "id";
	
	private static final String ATTRIBUTE_PATH = "path";
	
	private static final String ATTRIBUTE_TYPE = "type";
	
	private static final String ATTRIBUTE_CREATOR = "creator";
	
	private static final String ATTRIBUTE_JAVASCRIPT = "javascript";
	
	private static final String ATTRIBUTE_NAME = "name";
	
	private static final String ATTRIBUTE_VALUE = "value";
	
	private static final String ATTRIBUTE_CONVERTER = "converter";
	
	private static final String ATTRIBUTE_MATCH = "match";
	
	private static final String ATTRIBUTE_VERSION = "version";
	
	private static final String PUBLIC_IDENTIFIER = "-//OpenMRS//DTD OpenMRS Config 1.0//EN";
	
	private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	
	private final String dtdVersion;
	
	private Document configXml;

	/**
	 * Constructs a new ConfigXmlBuilder for testing purposes.
	 *
	 * @param dtdVersion the version of the DTD (Document Type Definition) to use
	 *                   for validating the generated XML. The DTD dictates the structure 
	 *                   and permissible values of the XML document. 
	 *
	 * @throws ParserConfigurationException if a DocumentBuilder cannot be created which
	 *                                      satisfies the configuration requested.
	 *
	 * Note: This constructor is intended for use in testing scenarios only. Do not use it for 
	 * generating configuration XML files for production use.
	 */
	public ConfigXmlBuilder(String dtdVersion) throws ParserConfigurationException, FileNotFoundException, URISyntaxException {
		this.dtdVersion = dtdVersion;
		initDocument();
		setDocType();
	}

	/**
	 * Converts the provided XML Document to an InputStream.
	 *
	 * This can be useful for tests or other scenarios where you need to consume
	 * the XML document as a stream (e.g., when passing the document to a method
	 * that requires an InputStream).
	 *
	 * Note: This method does not close the provided Document or the resulting InputStream.
	 *       It's the caller's responsibility to close the stream after use.
	 *
	 * @param document the XML Document to convert to an InputStream
	 * @return an InputStream representing the provided Document
	 *
	 * @throws TransformerException if an unrecoverable error occurs during the transformation
	 */
	public static InputStream writeToInputStream(Document document) throws TransformerException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Source xmlSource = new DOMSource(document);
		Result outputTarget = new StreamResult(outputStream);
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, document.getDoctype().getPublicId());
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, document.getDoctype().getSystemId());
		transformer.transform(xmlSource, outputTarget);
		return new ByteArrayInputStream(outputStream.toByteArray());
	}
	
	protected static ConfigXmlBuilder withMinimalTags(String dtdVersion) throws ParserConfigurationException, FileNotFoundException, URISyntaxException {
		return new ConfigXmlBuilder(dtdVersion).withId("basicexample").withName("Basicexample").withVersion("1.2.3")
				.withPackage("org.openmrs.module.basicexample").withAuthor("Community").withDescription("First module")
				.withActivator("org.openmrs.module.basicexample.BasicexampleActivator");
	}
	
	private void initDocument() throws ParserConfigurationException {
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		
		configXml = documentBuilder.newDocument();
		documentBuilder.newDocument();
	}
	
	private void setDocType() throws FileNotFoundException, URISyntaxException {
		DOMImplementation domImpl = configXml.getImplementation();

		URL dtdResource = ConfigXmlBuilder.class.getResource("/org/openmrs/module/dtd/config-" + dtdVersion + ".dtd");
		if (dtdResource == null) {
			throw new FileNotFoundException("DTD file not found for version " + dtdVersion);
		}
		String dtdUri = dtdResource.toURI().toString();
		DocumentType doctype = domImpl.createDocumentType(MODULE_NAME, PUBLIC_IDENTIFIER, dtdUri);
		configXml.appendChild(doctype);
		Element root = configXml.createElement(MODULE_NAME);
		configXml.appendChild(root);
	}
	
	public ConfigXmlBuilder withId(String id) {
		createElementWithText(TAG_ID, id);
		return this;
	}
	
	public ConfigXmlBuilder withName(String name) {
		createElementWithText(TAG_NAME, name);
		return this;
	}
	
	public ConfigXmlBuilder withVersion(String version) {
		createElementWithText(TAG_VERSION, version);
		return this;
	}
	
	public ConfigXmlBuilder withPackage(String packageName) {
		createElementWithText(TAG_PACKAGE, packageName);
		return this;
	}
	
	public ConfigXmlBuilder withAuthor(String author) {
		createElementWithText(TAG_AUTHOR, author);
		return this;
	}
	
	public ConfigXmlBuilder withDescription(String description) {
		createElementWithText(TAG_DESCRIPTION, description);
		return this;
	}
	
	public ConfigXmlBuilder withActivator(String activator) {
		createElementWithText(TAG_ACTIVATOR, activator);
		return this;
	}
	
	public ConfigXmlBuilder withInvalidTag(String text) {
		createElementWithText("invalid_tag", text);
		return this;
	}
	
	public ConfigXmlBuilder withUpdateUrl(String updateUrl) {
		createElementWithText(TAG_UPDATE_URL, updateUrl);
		return this;
	}
	
	public ConfigXmlBuilder withRequireVersion(String requireVersion) {
		createElementWithText(TAG_REQUIRE_VERSION, requireVersion);
		return this;
	}
	
	public ConfigXmlBuilder withRequireDatabaseVersion(String requireDatabaseVersion) {
		createElementWithText(TAG_REQUIRE_DATABASE_VERSION, requireDatabaseVersion);
		return this;
	}
	
	public ConfigXmlBuilder withMappingFiles(String mappingFiles) {
		createElementWithText(TAG_MAPPING_FILES, mappingFiles);
		return this;
	}
	
	public ConfigXmlBuilder withMandatory(String mandatory) {
		createElementWithText(TAG_MANDATORY, mandatory);
		return this;
	}
	
	public ConfigXmlBuilder withPackagesWithMappedClasses(String packagesWithMappedClasses) {
		createElementWithText(TAG_PACKAGED_WITH_MAPPED_CLASSES, packagesWithMappedClasses);
		return this;
	}
	
	public ConfigXmlBuilder withLibrary(Optional<String> id, Optional<String> path, Optional<String> type) {
		Element element = configXml.createElement(TAG_LIBRARY);
		id.ifPresent(libraryId -> element.setAttribute(ATTRIBUTE_ID, libraryId));
		path.ifPresent(libraryPath -> element.setAttribute(ATTRIBUTE_PATH, libraryPath));
		type.ifPresent(libraryType -> element.setAttribute(ATTRIBUTE_TYPE, libraryType));
		configXml.getDocumentElement().appendChild(element);
		return this;
	}
	
	public ConfigXmlBuilder withExtension(Optional<String> point, Optional<String> cls) {
		Element extensionElement = configXml.createElement(TAG_EXTENSION);
		point.ifPresent(extensionPoint -> {
			Element pointElement = configXml.createElement(TAG_POINT);
			pointElement.setTextContent(point.get());
			extensionElement.appendChild(pointElement);
		});
		cls.ifPresent(extensionClass -> {
			Element classElement = configXml.createElement(TAG_CLASS);
			classElement.setTextContent(cls.get());
			extensionElement.appendChild(classElement);
		});
		configXml.getDocumentElement().appendChild(extensionElement);
		return this;
	}
	
	public ConfigXmlBuilder withAdvice(Optional<String> point, Optional<String> cls) {
		Element adviceElement = configXml.createElement(TAG_ADVICE);
		point.ifPresent(advicePoint -> {
			Element pointElement = configXml.createElement(TAG_POINT);
			pointElement.setTextContent(point.get());
			adviceElement.appendChild(pointElement);
		});
		cls.ifPresent(adviceClass -> {
			Element classElement = configXml.createElement(TAG_CLASS);
			classElement.setTextContent(cls.get());
			adviceElement.appendChild(classElement);
		});
		configXml.getDocumentElement().appendChild(adviceElement);
		return this;
	}
	
	public ConfigXmlBuilder withPrivilege(Optional<String> name, Optional<String> description) {
		Element privilegeElement = configXml.createElement(TAG_PRIVILEGE);
		name.ifPresent(privilegeName -> {
			Element nameElement = configXml.createElement(TAG_NAME);
			nameElement.setTextContent(name.get());
			privilegeElement.appendChild(nameElement);
		});
		description.ifPresent(privilegeDescription -> {
			Element descriptionElement = configXml.createElement(TAG_DESCRIPTION);
			descriptionElement.setTextContent(description.get());
			privilegeElement.appendChild(descriptionElement);
		});
		configXml.getDocumentElement().appendChild(privilegeElement);
		return this;
	}
	
	public ConfigXmlBuilder withGlobalProperty(Optional<String> property, Optional<String> defaultValue,
	        Optional<String> description) {
		Element globalPropertyElement = configXml.createElement(TAG_GLOBAL_PROPERTY);
		property.ifPresent(propertyValue -> {
			Element propertyElement = configXml.createElement(TAG_PROPERTY);
			propertyElement.setTextContent(property.get());
			globalPropertyElement.appendChild(propertyElement);
		});
		defaultValue.ifPresent(privilegeDefaultValue -> {
			Element defaultElement = configXml.createElement(TAG_DEFAULT_VALUE);
			defaultElement.setTextContent(defaultValue.get());
			globalPropertyElement.appendChild(defaultElement);
		});
		description.ifPresent(privilegeDescription -> {
			Element descriptionElement = configXml.createElement(TAG_DESCRIPTION);
			descriptionElement.setTextContent(description.get());
			globalPropertyElement.appendChild(descriptionElement);
		});
		configXml.getDocumentElement().appendChild(globalPropertyElement);
		return this;
	}
	
	public ConfigXmlBuilder withDwr(Dwr dwr) {
		Element dwrElement = configXml.createElement(TAG_DWR);
		
		dwr.allow.ifPresent(allow -> {
			Element allowElement = configXml.createElement(TAG_ALLOW);
			dwrElement.appendChild(allowElement);
			withCreates(allowElement, allow.getCreates());
			withConverts(allowElement, allow.getConverts());
		});
		
		dwr.getSignatures().ifPresent(dwrSig -> createElementWithText(dwrElement, TAG_SIGNATURES, dwrSig));
		
		configXml.getDocumentElement().appendChild(dwrElement);
		return this;
	}
	
	public void withCreates(Element parent, List<Create> creates) {
		for (Create create : creates) {
			Element createElement = configXml.createElement(TAG_CREATE);
			
			create.getAttCreator().ifPresent(creatorVal -> createElement.setAttribute(ATTRIBUTE_CREATOR, creatorVal));
			create.getAttJavascript().ifPresent(javascriptVal -> createElement.setAttribute(ATTRIBUTE_JAVASCRIPT, javascriptVal));
			
			create.getParam().ifPresent(createParam -> {
				Element paramElement = configXml.createElement(TAG_PARAM);
				createParam.getAttName().ifPresent(attNameVal -> paramElement.setAttribute(ATTRIBUTE_NAME, attNameVal));
				createParam.getAttValue().ifPresent(attValueVal -> paramElement.setAttribute(ATTRIBUTE_VALUE, attValueVal));
				createElement.appendChild(paramElement);
			});
			
			for (Include include : create.getIncludes()) {
				Element includeElement = configXml.createElement(TAG_INCLUDE);
				include.method.ifPresent(method -> includeElement.setAttribute(TAG_METHOD, method));
				createElement.appendChild(includeElement);
			}
			
			parent.appendChild(createElement);
		}
	}
	
	public void withConverts(Element parent, List<Convert> converts) {
		for (Convert convert : converts) {
			
			Element convertElement = configXml.createElement(TAG_CONVERT);
			
			convert.getParam().ifPresent(createParam -> {
				Element paramElement = configXml.createElement(TAG_PARAM);
				createParam.getAttName().ifPresent(attNameVal -> paramElement.setAttribute(ATTRIBUTE_NAME, attNameVal));
				createParam.getAttValue().ifPresent(attValueVal -> paramElement.setAttribute(ATTRIBUTE_VALUE, attValueVal));
				convertElement.appendChild(paramElement);
			});
			
			convert.getConverter().ifPresent(convertVal -> convertElement.setAttribute(ATTRIBUTE_CONVERTER, convertVal));
			convert.getMatch().ifPresent(matchVal -> convertElement.setAttribute(ATTRIBUTE_MATCH, matchVal));
			
			parent.appendChild(convertElement);
		}
	}
	
	public ConfigXmlBuilder withRequireModules(String... modules) {
		Element requireModulesElement = configXml.createElement(TAG_REQUIRE_MODULES);
		configXml.getDocumentElement().appendChild(requireModulesElement);
		
		for (String module : modules) {
			createElementWithText(requireModulesElement, TAG_REQUIRE_MODULE, module);
		}
		
		return this;
	}
	
	public ConfigXmlBuilder withRequireModules(List<String> modules, List<Optional<String>> versions) {
		assert modules.size() == versions.size();
		Element requireModulesElement = configXml.createElement(TAG_REQUIRE_MODULES);
		configXml.getDocumentElement().appendChild(requireModulesElement);
		
		for (int i = 0; i < modules.size(); i++) {
			String module = modules.get(i);
			
			Element requireModuleElement = configXml.createElement(TAG_REQUIRE_MODULE);
			requireModuleElement.setTextContent(module);
			
			createElementWithText(requireModulesElement, TAG_REQUIRE_MODULE, module);
			versions.get(i).ifPresent(version -> requireModuleElement.setAttribute(ATTRIBUTE_VERSION, version));
		}
		
		return this;
	}

	public ConfigXmlBuilder withServlet(Optional<String> servletName, Optional<String> servletClass, Map<String, String> initParams) {
		Element servletElement = configXml.createElement(TAG_SERVLET);

		servletName.ifPresent(servletNameVal -> {
			Element servletNameElement = configXml.createElement("servlet-name");
			servletNameElement.setTextContent(servletNameVal);
			servletElement.appendChild(servletNameElement);
		});

		servletClass.ifPresent(servletClassVal -> {
			Element servletClassElement = configXml.createElement("servlet-class");
			servletClassElement.setTextContent(servletClassVal);
			servletElement.appendChild(servletClassElement);
		});

		for (Map.Entry<String, String> entry : initParams.entrySet()) {
			Element initParamElement = configXml.createElement("init-param");

			Element paramNameElement = configXml.createElement("param-name");
			paramNameElement.setTextContent(entry.getKey());
			initParamElement.appendChild(paramNameElement);

			Element paramValueElement = configXml.createElement("param-value");
			paramValueElement.setTextContent(entry.getValue());
			initParamElement.appendChild(paramValueElement);

			servletElement.appendChild(initParamElement);
		}

		configXml.getDocumentElement().appendChild(servletElement);
		return this;
	}
	
	public ConfigXmlBuilder withMessages(Optional<String> lang, Optional<String> file) {
		Element messagesElement = configXml.createElement(TAG_MESSAGES);
		lang.ifPresent(langVal -> {
			Element langElement = configXml.createElement("lang");
			langElement.setTextContent(langVal);
			messagesElement.appendChild(langElement);
		});
		file.ifPresent(fileVal -> {
			Element fileElement = configXml.createElement("file");
			fileElement.setTextContent(fileVal);
			messagesElement.appendChild(fileElement);
		});
		configXml.getDocumentElement().appendChild(messagesElement);
		return this;
	}
	
	public ConfigXmlBuilder withFilter(Filter filter) {
		Element filterElement = configXml.createElement(TAG_FILTER);
		
		filter.filterName.ifPresent(filterName -> {
			Element filterNameElement = configXml.createElement(TAG_FILTER_NAME);
			filterNameElement.setTextContent(filterName);
			filterElement.appendChild(filterNameElement);
		});
		filter.filterClass.ifPresent(filterClass -> {
			Element filterClassElement = configXml.createElement(TAG_FILTER_CLASS);
			filterClassElement.setTextContent(filterClass);
			filterElement.appendChild(filterClassElement);
		});
		
		for (InitParam initParam : filter.getInitParams()) {
			Element initParamElement = configXml.createElement(TAG_INIT_PARAM);
			
			filterElement.appendChild(initParamElement);
			initParam.getParamName().ifPresent(initParamName -> {
				createElementWithText(initParamElement, TAG_PARAM_NAME, initParamName);
			});
			initParam.getParamValue().ifPresent(initParamValue -> {
				createElementWithText(initParamElement, TAG_PARAM_VALUE, initParamValue);
			});
		}
		
		configXml.getDocumentElement().appendChild(filterElement);
		return this;
	}
	
	public ConfigXmlBuilder withFilterMapping(FilterMapping filterMapping) {
		Element filterElement = configXml.createElement(TAG_FILTER_MAPPING);
		
		filterMapping.filterName.ifPresent(filterName -> {
			Element filterNameElement = configXml.createElement(TAG_FILTER_NAME);
			filterNameElement.setTextContent(filterName);
			filterElement.appendChild(filterNameElement);
		});
		filterMapping.urlPattern.ifPresent(urlPattern -> {
			Element urlPatternElement = configXml.createElement(TAG_URL_PATTERN);
			urlPatternElement.setTextContent(urlPattern);
			filterElement.appendChild(urlPatternElement);
		});
		filterMapping.servletName.ifPresent(servletName -> {
			Element servletNameElement = configXml.createElement(TAG_SERVLET_NAME);
			servletNameElement.setTextContent(servletName);
			filterElement.appendChild(servletNameElement);
		});

		configXml.getDocumentElement().appendChild(filterElement);
		return this;
	}
	
	public ConfigXmlBuilder withAwareOfModules(List<AwareOfModule> awareOfModules) {
		Element awareOfModulesElement = configXml.createElement(TAG_AWARE_OF_MODULES);
		
		for (AwareOfModule awareOfModule : awareOfModules) {
			
			Element awareOfModuleElement = configXml.createElement(TAG_AWARE_OF_MODULE);
			
			awareOfModule.getAwareOfModule().ifPresent(awareOfModuleValue -> {
				awareOfModuleElement.setTextContent(awareOfModuleValue);
				awareOfModule.getVersionAtt().ifPresent(versionAtt -> awareOfModuleElement.setAttribute(ATTRIBUTE_VERSION, versionAtt));
			});
			awareOfModulesElement.appendChild(awareOfModuleElement);
		}
		
		configXml.getDocumentElement().appendChild(awareOfModulesElement);
		return this;
	}
	
	public ConfigXmlBuilder withConditionalResources(List<ConditionalResource> conditionalResources) {
		Element conditionalResourcesElement = configXml.createElement(TAG_CONDITIONAL_RESOURCES);
		
		for (ConditionalResource conditionalResource : conditionalResources) {
			
			Element conditionalResourceElement = configXml.createElement(TAG_CONDITIONAL_RESOURCE);
			
			conditionalResource.getPath().ifPresent(path -> createElementWithText(conditionalResourceElement, TAG_PATH, path));
			conditionalResource.getOpenmrsVersion().ifPresent(openmrsVersion -> createElementWithText(conditionalResourceElement, TAG_OPENMRS_VERSION, openmrsVersion));
			
			if (conditionalResource.getLoadIfModulesPresent().size() > 0) {
				Element loadIfModulesPresentElement = configXml.createElement(TAG_LOAD_MODULES_IF_PRESENT);
				conditionalResourceElement.appendChild(loadIfModulesPresentElement);
				
				for (OpenMRSModule openMRSModule : conditionalResource.getLoadIfModulesPresent()) {
					
					Element openMRSModuleElement = configXml.createElement(TAG_OPENMRS_MODULE);
					
					openMRSModule.getModuleId().ifPresent(moduleId -> createElementWithText(openMRSModuleElement, TAG_MODULE_ID, moduleId));
					openMRSModule.getVersion().ifPresent(version -> createElementWithText(openMRSModuleElement, TAG_VERSION, version));
					
					loadIfModulesPresentElement.appendChild(openMRSModuleElement);
				}
			}
			
			conditionalResourcesElement.appendChild(conditionalResourceElement);
		}
		
		configXml.getDocumentElement().appendChild(conditionalResourcesElement);
		return this;
	}
	
	private void createElementWithText(String tag, String text) {
		createElementWithText(configXml.getDocumentElement(), tag, text);
	}
	
	private void createElementWithText(Node parent, String tag, String text) {
		Element element = configXml.createElement(tag);
		element.setTextContent(text);
		parent.appendChild(element);
	}
	
	public Document build() throws TransformerException {
		return configXml;
	}
	
	protected static final class Dwr {
		
		private final Optional<Allow> allow;
		
		private final Optional<String> signatures;
		
		public Dwr(Optional<Allow> allow, Optional<String> signatures) {
			this.allow = allow;
			this.signatures = signatures;
		}
		
		public Optional<String> getSignatures() {
			return signatures;
		}
		
		public Optional<Allow> getAllow() {
			return allow;
		}
	}
	
	protected static final class Allow {
		
		private final List<Create> creates = new ArrayList<>();
		
		private final List<Convert> converts = new ArrayList<>();
		
		public void addCreate(Create create) {
			this.creates.add(create);
		}
		
		public void addConvert(Convert convert) {
			this.converts.add(convert);
		}
		
		public List<Create> getCreates() {
			return new ArrayList<>(creates);
		}
		
		public List<Convert> getConverts() {
			return new ArrayList<>(converts);
		}
	}
	
	protected static final class Create {
		
		private final Optional<Param> param;
		
		private final Optional<String> attCreator;
		
		private final Optional<String> attJavascript;
		
		private final List<Include> includes = new ArrayList<>();
		
		public Create(Optional<String> attCreator, Optional<String> attJavascript, Optional<Param> param) {
			this.param = param;
			this.attCreator = attCreator;
			this.attJavascript = attJavascript;
		}
		
		public void addIncludes(Include include) {
			this.includes.add(include);
		}
		
		public Optional<Param> getParam() {
			return param;
		}
		
		public Optional<String> getAttCreator() {
			return attCreator;
		}
		
		public Optional<String> getAttJavascript() {
			return attJavascript;
		}
		
		public List<Include> getIncludes() {
			return new ArrayList<>(includes);
		}
	}
	
	protected static final class Convert {
		
		private final Optional<Param> param;
		
		private final Optional<String> converter;
		
		private final Optional<String> match;
		
		public Convert(Optional<Param> param, Optional<String> converter, Optional<String> match) {
			this.param = param;
			this.converter = converter;
			this.match = match;
		}
		
		public Optional<Param> getParam() {
			return param;
		}
		
		public Optional<String> getConverter() {
			return converter;
		}
		
		public Optional<String> getMatch() {
			return match;
		}
	}
	
	protected static final class Param {
		
		private final Optional<String> attName;
		
		private final Optional<String> attValue;
		
		public Param(Optional<String> attName, Optional<String> attValue) {
			this.attName = attName;
			this.attValue = attValue;
		}
		
		public Optional<String> getAttName() {
			return attName;
		}
		
		public Optional<String> getAttValue() {
			return attValue;
		}
	}
	
	protected static final class Include {
		
		private final Optional<String> method;
		
		public Include(Optional<String> method) {
			this.method = method;
		}
		
		public Optional<String> getMethod() {
			return method;
		}
	}
	
	protected static final class Filter {
		
		private final Optional<String> filterName;
		private final Optional<String> filterClass;
		private final List<InitParam> initParams = new ArrayList<>();
		
		public Filter(Optional<String> filterName, Optional<String> filterClass) {
			this.filterName = filterName;
			this.filterClass = filterClass;
		}
		
		public Optional<String> getFilterName() {
			return filterName;
		}
		
		public Optional<String> getFilterClass() {
			return filterClass;
		}
		
		public void addInitParam(InitParam initParam) {
			this.initParams.add(initParam);
		}
		
		public List<InitParam> getInitParams() {
			return new ArrayList<>(initParams);
		}
	}
	
	protected static final class InitParam {
		
		private final Optional<String> paramName;
		private final Optional<String> paramValue;
		
		public InitParam(Optional<String> paramName, Optional<String> paramValue) {
			this.paramName = paramName;
			this.paramValue = paramValue;
		}
		
		public Optional<String> getParamName() {
			return paramName;
		}
		
		public Optional<String> getParamValue() {
			return paramValue;
		}
	}
	
	protected static final class FilterMapping {
		
		private final Optional<String> filterName;
		private final Optional<String> urlPattern;
		private final Optional<String> servletName;
		
		public FilterMapping(Optional<String> filterName, Optional<String> urlPattern, Optional<String> servletName) {
			this.filterName = filterName;
			this.urlPattern = urlPattern;
			this.servletName = servletName;
		}
		
		public Optional<String> getFilterName() {
			return filterName;
		}
		
		public Optional<String> getUrlPattern() {
			return urlPattern;
		}
		
		public Optional<String> getServletName() {
			return servletName;
		}
	}
	
	protected static final class AwareOfModule {
		
		private final Optional<String> awareOfModule;
		private final Optional<String> versionAtt;
		
		public AwareOfModule(Optional<String> awareOfModule, Optional<String> versionAtt) {
			this.awareOfModule = awareOfModule;
			this.versionAtt = versionAtt;
		}
		
		public Optional<String> getAwareOfModule() {
			return awareOfModule;
		}
		
		public Optional<String> getVersionAtt() {
			return versionAtt;
		}
	}
	
	protected static final class ConditionalResource {
		
		private final Optional<String> path;
		private final Optional<String> openmrsVersion;
		private final List<OpenMRSModule> loadIfModulesPresent = new ArrayList<>();
		
		public ConditionalResource(Optional<String> path, Optional<String> openmrsVersion) {
			this.path = path;
			this.openmrsVersion = openmrsVersion;
		}
		
		public Optional<String> getPath() {
			return path;
		}
		
		public Optional<String> getOpenmrsVersion() {
			return openmrsVersion;
		}
		
		public void addModule(OpenMRSModule module) {
			loadIfModulesPresent.add(module);
		}
		
		public List<OpenMRSModule> getLoadIfModulesPresent() {
			return new ArrayList<>(loadIfModulesPresent);
		}
	}
	
	protected static final class OpenMRSModule {
		
		private final Optional<String> moduleId;
		private final Optional<String> version;
		
		public OpenMRSModule(Optional<String> moduleId, Optional<String> version) {
			this.moduleId = moduleId;
			this.version = version;
		}
		
		public Optional<String> getModuleId() {
			return moduleId;
		}
		
		public Optional<String> getVersion() {
			return version;
		}
	}
}

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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.openmrs.GlobalProperty;
import org.openmrs.Privilege;
import org.openmrs.customdatatype.datatype.RegexValidatedTextDatatype;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.test.BaseContextMockTest;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * Tests {@link ModuleFileParser} without a database but with file IO (so technically not a unit test but close).
 */
public class ModuleFileParserUnitTest extends BaseContextMockTest {

	private static DocumentBuilderFactory documentBuilderFactory;

	private static DocumentBuilder documentBuilder;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Mock
	private MessageSourceService messageSourceService;

	private ModuleFileParser parser;

	@BeforeClass
	public static void setUp() throws ParserConfigurationException {
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilder = documentBuilderFactory.newDocumentBuilder();
	}

	@Before
	public void setUpModuleFileParser() {
		parser = new ModuleFileParser(messageSourceService);
	}

	@After
	public void after() {
		// needed so other are not affected by tests registering a ModuleClassLoader
		ModuleFactory.moduleClassLoaders = null;
	}

	@Test
	public void moduleFileParserConstruction_shouldFailIfGivenNull() {

		expectedException.expect(NullPointerException.class);
		expectedException.expectMessage("messageSourceService must not be null");

		new ModuleFileParser((MessageSourceService) null);
	}

	@Test
	public void deprecatedParse_shouldFailIfParserWasCreatedWithNewConstructorAndModuleFileIsNull() {

		String messageKey = "Module.error.fileCannotBeNull";
		whenGettingMessageFromMessageSourceServiceWithKeyReturnSameKey(messageKey);

		expectModuleExceptionWithMessage(messageKey);

		ModuleFileParser moduleFileParser = new ModuleFileParser(messageSourceService);
		moduleFileParser.parse();
	}

	@Test
	public void parseFromInputstream_shouldParseValidXmlConfig() throws IOException {

		Document config = new ModuleConfigXmlBuilder(documentBuilder)
			.withModuleRoot()
			.withConfigVersion("1.6")
			.withModuleName("Reporting")
			.withModuleId("reporting")
			.withPackage("org.openmrs.module.reporting")
			.build();

		File moduleFile = writeConfigXmlToFile(config);
		InputStream inputStream = new FileInputStream(moduleFile);

		Module module = parser.parse(inputStream);

		assertThat(module.getModuleId(), is("reporting"));
		assertThat(module.getName(), is("Reporting"));
		assertThat(module.getPackageName(), is("org.openmrs.module.reporting"));
	}

	@Test
	public void parseInputStream_shouldFailFileIfInputStreamClosed() throws IOException {

		String messageKey = "Module.error.cannotCreateFile";
		whenGettingMessageFromMessageSourceServiceWithKeyReturnSameKey(messageKey);

		Document config = new ModuleConfigXmlBuilder(documentBuilder)
			.withModuleRoot()
			.withConfigVersion("1.6")
			.withModuleName("Reporting")
			.withModuleId("reporting")
			.withPackage("org.openmrs.module.reporting")
			.build();

		File moduleFile = writeConfigXmlToFile(config);
		InputStream inputStream = new FileInputStream(moduleFile);
		inputStream.close();

		expectModuleExceptionWithMessage(messageKey);

		parser.parse(inputStream);
	}

	@Test
	public void parse_shouldFailIfModuleFileIsNull() {

		String messageKey = "Module.error.fileCannotBeNull";
		whenGettingMessageFromMessageSourceServiceWithKeyReturnSameKey(messageKey);

		expectModuleExceptionWithMessage(messageKey);

		parser.parse((File) null);
	}

	@Test
	public void parse_shouldFailIfModuleFileHasInvalidExtension() {

		String messageKey = "Module.error.invalidFileExtension";
		whenGettingMessageFromMessageSourceServiceWithKeyReturnSameKey(messageKey);

		expectModuleExceptionWithMessage(messageKey);

		parser.parse(new File("unknownmodule.jar"));
	}

	@Test
	public void parse_shouldFailIfModuleFileCannotBeFound() {

		String messageKey = "Module.error.cannotGetJarFile";
		whenGettingMessageFromMessageSourceServiceWithKeyReturnSameKey(messageKey);

		expectModuleExceptionWithMessage(messageKey);

		parser.parse(new File("unknownmodule.omod"));
	}

	@Test
	public void parse_shouldFailIfModuleHasNoConfigXmlInRoot() throws Exception {

		File file = temporaryFolder.newFile("modulewithoutconfig.omod");
		JarOutputStream jar = new JarOutputStream(new FileOutputStream(file));
		jar.close();

		String messageKey = "Module.error.noConfigFile";
		whenGettingMessageFromMessageSourceServiceWithKeyReturnSameKey(messageKey);

		expectModuleExceptionWithMessage(messageKey);

		parser.parse(file);
	}

	@Test
	public void parse_shouldFailIfModuleHasConfigXmlInRootWhichCannotBeParsed() throws Exception {

		File file = temporaryFolder.newFile("modulewithoutconfig.omod");
		JarOutputStream jar = createJarWithConfigXmlEntry(file);
		jar.close();

		String messageKey = "Module.error.cannotParseConfigFile";
		whenGettingMessageFromMessageSourceServiceWithKeyReturnSameKey(messageKey);

		expectModuleExceptionWithMessage(messageKey);

		parser.parse(file);
	}

	@Test
	public void parse_shouldFailIfModuleHasConfigXmlInRootWhichCannotBeParsedBecauseOfInvalidXml() throws Exception {

		File file = writeConfigXmlToFile("<?xml version='1.0' encoding='UTF-8'?><module configVersion='1.5'>");

		String messageKey = "Module.error.cannotParseConfigFile";
		whenGettingMessageFromMessageSourceServiceWithKeyReturnSameKey(messageKey);

		expectModuleExceptionWithMessage(messageKey);

		parser.parse(file);
	}

	@Test
	public void parse_shouldFailIfModuleHasConfigWithNoModuleName() throws Exception {

		Document config = new ModuleConfigXmlBuilder(documentBuilder)
			.withModuleRoot()
			.withConfigVersion("1.6")
			.build();

		String messageKey = "Module.error.nameCannotBeEmpty";
		whenGettingMessageFromMessageSourceServiceWithKeyReturnSameKey(messageKey);

		expectModuleExceptionWithMessage(messageKey);

		parser.parse(writeConfigXmlToFile(config));
	}

	@Test
	public void parse_shouldFailIfModuleHasConfigWithEmptyModuleName() throws Exception {

		Document config = new ModuleConfigXmlBuilder(documentBuilder)
			.withModuleRoot()
			.withConfigVersion("1.6")
			.withModuleName("  ")
			.build();

		String messageKey = "Module.error.nameCannotBeEmpty";
		whenGettingMessageFromMessageSourceServiceWithKeyReturnSameKey(messageKey);

		expectModuleExceptionWithMessage(messageKey);

		parser.parse(writeConfigXmlToFile(config));
	}

	@Test
	public void parse_shouldFailIfModuleHasConfigWithNoModuleId() throws Exception {

		Document config = new ModuleConfigXmlBuilder(documentBuilder)
			.withModuleRoot()
			.withConfigVersion("1.6")
			.withModuleName("report")
			.build();

		String messageKey = "Module.error.idCannotBeEmpty";
		whenGettingMessageFromMessageSourceServiceWithKeyReturnSameKey(messageKey);

		expectModuleExceptionWithMessage(messageKey);

		parser.parse(writeConfigXmlToFile(config));
	}

	@Test
	public void parse_shouldFailIfModuleHasConfigWithEmptyModuleId() throws Exception {

		Document config = new ModuleConfigXmlBuilder(documentBuilder)
			.withModuleRoot()
			.withConfigVersion("1.6")
			.withModuleName("Reporting")
			.withModuleId("   ")
			.build();

		String messageKey = "Module.error.idCannotBeEmpty";
		whenGettingMessageFromMessageSourceServiceWithKeyReturnSameKey(messageKey);

		expectModuleExceptionWithMessage(messageKey);

		parser.parse(writeConfigXmlToFile(config));
	}

	@Test
	public void parse_shouldFailIfModuleHasConfigWithNoPackageName() throws Exception {

		Document config = new ModuleConfigXmlBuilder(documentBuilder)
			.withModuleRoot()
			.withConfigVersion("1.6")
			.withModuleName("Reporting")
			.withModuleId("reporting")
			.build();

		String messageKey = "Module.error.packageCannotBeEmpty";
		whenGettingMessageFromMessageSourceServiceWithKeyReturnSameKey(messageKey);

		expectModuleExceptionWithMessage(messageKey);

		parser.parse(writeConfigXmlToFile(config));
	}

	@Test
	public void parse_shouldFailIfModuleHasConfigWithEmptyPackageName() throws Exception {

		Document config = new ModuleConfigXmlBuilder(documentBuilder)
			.withModuleRoot()
			.withConfigVersion("1.6")
			.withModuleName("Reporting")
			.withModuleId("reporting")
			.withPackage("  ")
			.build();

		String messageKey = "Module.error.packageCannotBeEmpty";
		whenGettingMessageFromMessageSourceServiceWithKeyReturnSameKey(messageKey);

		expectModuleExceptionWithMessage(messageKey);

		parser.parse(writeConfigXmlToFile(config));
	}

	@Test
	public void parse_shouldParseValidXmlConfig() throws Exception {

		Document config = new ModuleConfigXmlBuilder(documentBuilder)
			.withModuleRoot()
			.withConfigVersion("1.6")
			.withModuleName("Reporting")
			.withModuleId("reporting")
			.withPackage("org.openmrs.module.reporting")
			.withTextNode("author", "Community")
			.withTextNode("description", "Reporting everything")
			.withTextNode("activator", "org.openmrs.module.ReportingModuleActivator")
			.withTextNode("require_database_version", "0.9.5")
			.withTextNode("require_version", "1.11.3 - 1.11.*, 1.12")
			.withTextNode("updateURL", "https://dev.openmrs.org/modules/download/reporting/update.rdf")
			.build();

		File moduleFile = writeConfigXmlToFile(config);
		Module module = parser.parse(moduleFile);

		assertThat(module.getModuleId(), is("reporting"));
		assertThat(module.getName(), is("Reporting"));
		assertThat(module.getPackageName(), is("org.openmrs.module.reporting"));
		assertThat(module.getAuthor(), is("Community"));
		assertThat(module.getDescription(), is("Reporting everything"));
		assertThat(module.getActivatorName(), is("org.openmrs.module.ReportingModuleActivator"));
		assertThat(module.getRequireDatabaseVersion(), is("0.9.5"));
		assertThat(module.getRequireOpenmrsVersion(), is("1.11.3 - 1.11.*, 1.12"));
		assertThat(module.getUpdateURL(), is("https://dev.openmrs.org/modules/download/reporting/update.rdf"));
		assertThat(module.getFile(), is(moduleFile));
		assertThat(module.getPrivileges(), is(equalTo(Collections.EMPTY_LIST)));
		assertThat(module.getGlobalProperties(), is(equalTo(Collections.EMPTY_LIST)));
		assertThat(module.getMappingFiles(), is(equalTo(Collections.EMPTY_LIST)));
		assertThat(module.getPackagesWithMappedClasses(), is(equalTo(Collections.EMPTY_SET)));
		assertThat(module.isMandatory(), is(false));
	}

	@Test
	public void parse_shouldParseValidXmlAndIgnoreExternalEntitiesLikeDtd() throws IOException {

		String configVersion = "1.6";
		Document config = new ModuleConfigXmlBuilder(documentBuilder)
			.withDoctype(configVersion)
			.withModuleRoot()
			.withConfigVersion(configVersion)
			.withModuleName("Reporting")
			.withModuleId("reporting")
			.withPackage("org.openmrs.module.reporting")
			.withTextNode("author", "Community")
			.withTextNode("description", "Reporting everything")
			.withTextNode("activator", "org.openmrs.module.ReportingModuleActivator")
			.withTextNode("require_database_version", "0.9.5")
			.withTextNode("require_version", "1.11.3 - 1.11.*, 1.12")
			.withTextNode("updateURL", "https://dev.openmrs.org/modules/download/reporting/update.rdf")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getModuleId(), is("reporting"));
	}

	@Test
	public void parse_shouldParseValidXmlConfigAndEmptyRequireModules() throws IOException {

		Document config = new ModuleConfigXmlBuilder(documentBuilder)
			.withModuleRoot()
			.withConfigVersion("1.6")
			.withModuleName("Reporting")
			.withModuleId("reporting")
			.withPackage("org.openmrs.module.reporting")
			.withTextNode("require_modules", "")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getRequiredModulesMap(), is(equalTo(Collections.EMPTY_MAP)));
	}

	@Test
	public void parse_shouldParseRequireModulesContainingMultipleChildren() throws IOException {

		Document config = buildOnValidConfigXml()
			.withRequireModules(
				new String[] { "org.openmrs.module.serialization.xstream", "1.0.3" },
				new String[] { "org.openmrs.module.htmlwidgets", "2.0.4" }
			)
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getRequiredModulesMap().size(), is(2));
		assertThat(module.getRequiredModulesMap(), hasEntry("org.openmrs.module.serialization.xstream", "1.0.3"));
		assertThat(module.getRequiredModulesMap(), hasEntry("org.openmrs.module.htmlwidgets", "2.0.4"));
	}

	@Test
	public void parse_shouldParseRequireModulesContainingModuleWithoutVersionAttribute()
		throws IOException {

		Document config = buildOnValidConfigXml()
			.withRequireModules(
				new String[] { "org.openmrs.module.htmlwidgets" }
			)
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getRequiredModulesMap().size(), is(1));
		assertThat(module.getRequiredModulesMap(), hasEntry("org.openmrs.module.htmlwidgets", null));
	}

	@Test
	public void parse_shouldParseRequireModulesContainingModuleWithEmptyVersionAttribute()
		throws IOException {

		Document config = buildOnValidConfigXml()
			.withRequireModules(
				new String[] { "org.openmrs.module.htmlwidgets", "" }
			)
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getRequiredModulesMap().size(), is(1));
		assertThat(module.getRequiredModulesMap(), hasEntry("org.openmrs.module.htmlwidgets", ""));
	}

	@Test
	public void parse_shouldParseRequireModulesContainingDuplicatesAndKeepOnlyTheLastOneRegardlessOfVersions()
		throws IOException {

		Document config = buildOnValidConfigXml()
			.withRequireModules(
				new String[] { "org.openmrs.module.serialization.xstream", "1.0.3" },
				new String[] { "org.openmrs.module.serialization.xstream", "3.1.4" },
				new String[] { "org.openmrs.module.serialization.xstream", "2.0.3" }
			)
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getRequiredModulesMap().size(), is(1));
		assertThat(module.getRequiredModulesMap(), hasEntry("org.openmrs.module.serialization.xstream", "2.0.3"));
	}

	@Test
	public void parse_shouldParseRequireModulesByTakingTheFirstRequireModulesIfMultipleExist()
		throws IOException {

		Document config = buildOnValidConfigXml()
			.withRequireModules(
				new String[] { "org.openmrs.module.serialization.xstream", "1.0.3" }
			)
			.withRequireModules(
				new String[] { "org.openmrs.module.htmlwidgets", "2.0.4" }
			)
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getRequiredModulesMap().size(), is(1));
		assertThat(module.getRequiredModulesMap(), hasEntry("org.openmrs.module.serialization.xstream", "1.0.3"));
	}

	@Test
	public void parse_shouldParseAwareOfModulesWithoutChildren() throws IOException {

		Document config = buildOnValidConfigXml()
			.withAwareOfModules()
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getAwareOfModules(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldParseAwareOfModulesOnlyContainingText() throws IOException {

		Document config = buildOnValidConfigXml()
			.withTextNode("aware_of_modules", "will be ignored")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getAwareOfModules(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldParseAwareOfModulesContainingDuplicatesAndKeepOnlyOneModule()
		throws IOException {

		Document config = buildOnValidConfigXml()
			.withAwareOfModules(
				"org.openmrs.module.serialization.xstream",
				"org.openmrs.module.serialization.xstream"
			)
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getAwareOfModules().size(), is(1));
		assertThat(module.getAwareOfModules(), hasItems("org.openmrs.module.serialization.xstream"));
	}

	@Test
	public void parse_shouldParseAwareOfModulesContainingMultipleChildren() throws IOException {

		Document config = buildOnValidConfigXml()
			.withAwareOfModules(
				"org.openmrs.module.serialization.xstream",
				"org.openmrs.module.legacyui"
			)
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getAwareOfModules().size(), is(2));
		assertThat(module.getAwareOfModules(),
			hasItems("org.openmrs.module.serialization.xstream", "org.openmrs.module.legacyui"));
	}

	@Test
	public void parse_shouldParseExtensions() throws IOException {

		Document config = buildOnValidConfigXml()
			.withExtension("org.openmrs.admin.list", AccessibleExtension.class.getName())
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		registerModuleClassloader(module);
		assertThat(module.getExtensions().size(), is(1));
		assertThat(module.getExtensions().get(0).getPointId(), is("org.openmrs.admin.list"));
		assertThat(module.getExtensions().get(0), is(instanceOf(AccessibleExtension.class)));
	}

	@Test
	public void parse_shouldIgnoreExtensionWithExtensionIdSeparatorInPoint() throws IOException {

		Document config = buildOnValidConfigXml()
			.withExtension("org.openmrs.admin.list" + Extension.extensionIdSeparator, AccessibleExtension.class.getName())
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		registerModuleClassloader(module);
		assertThat(module.getExtensions(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldIgnoreExtensionWithoutPointAndClass() throws IOException {

		Document config = buildOnValidConfigXml().build();
		config.getDocumentElement().appendChild(config.createElement("extension"));

		Module module = parser.parse(writeConfigXmlToFile(config));

		registerModuleClassloader(module);
		assertThat(module.getExtensions(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldIgnoreExtensionOnlyContainingText() throws IOException {

		Document config = buildOnValidConfigXml()
			.withTextNode("extension", "will be ignored")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		registerModuleClassloader(module);
		assertThat(module.getExtensions(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldIgnoreExtensionWithoutClass() throws IOException {

		Document config = buildOnValidConfigXml()
			.withExtension("org.openmrs.admin.list", null)
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		registerModuleClassloader(module);
		assertThat(module.getExtensions(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldIgnoreExtensionWithoutPoint() throws IOException {

		Document config = buildOnValidConfigXml()
			.withExtension(null, "org.openmrs.module.web.extension.ManageAdminListExt")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		registerModuleClassloader(module);
		assertThat(module.getExtensions(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldParsePrivileges() throws IOException {

		Privilege p1 = new Privilege("Manage Reports", "Add report");
		Privilege p2 = new Privilege("Manage Report Definitions", "Add report definitions");
		Document config = buildOnValidConfigXml()
			.withPrivilege(p1.getName(), p1.getDescription())
			.withPrivilege(p2.getName(), p2.getDescription())
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getPrivileges().size(), is(2));
		assertThat(module.getPrivileges().get(0).getPrivilege(), is(p1.getPrivilege()));
		assertThat(module.getPrivileges().get(0).getDescription(), is(p1.getDescription()));
		assertThat(module.getPrivileges().get(1).getPrivilege(), is(p2.getPrivilege()));
		assertThat(module.getPrivileges().get(1).getDescription(), is(p2.getDescription()));
	}

	@Test
	public void parse_shouldParsePrivilegeContainingElementsOtherThanNameAndDescription() throws IOException {

		Privilege p1 = new Privilege("Manage Reports", "Add report");
		Document config = buildOnValidConfigXml()
			.withPrivilege(p1.getName(), p1.getDescription())
			.build();
		config.getElementsByTagName("privilege").item(0).appendChild(config.createElement("ignoreMe"));

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getPrivileges().size(), is(1));
		assertThat(module.getPrivileges().get(0).getPrivilege(), is(p1.getPrivilege()));
		assertThat(module.getPrivileges().get(0).getDescription(), is(p1.getDescription()));
	}

	@Test
	public void parse_shouldIgnorePrivilegeWithoutChildren() throws IOException {

		Document config = buildOnValidConfigXml().build();
		config.getDocumentElement().appendChild(config.createElement("privilege"));

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getPrivileges(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldIgnorePrivilegeOnlyContainingText() throws IOException {

		Document config = buildOnValidConfigXml()
			.withTextNode("privilege", "will be ignored")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getPrivileges(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldIgnorePrivilegeWithoutDescription() throws IOException {

		Document config = buildOnValidConfigXml()
			.withPrivilege("Add Report", null)
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getPrivileges(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldIgnorePrivilegeWithoutName() throws IOException {

		Document config = buildOnValidConfigXml()
			.withPrivilege(null, "Add report")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getPrivileges(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldParseGlobalProperty() throws IOException {

		GlobalProperty gp1 = new GlobalProperty("report.deleteReportsAgeInHours", "72", "delete reports after hours");
		GlobalProperty gp2 = new GlobalProperty("report.validateInput", "2", "to validate input",
			RegexValidatedTextDatatype.class, "^\\d+$");
		Document config = buildOnValidConfigXml()
			.withGlobalProperty(gp1.getProperty(), gp1.getPropertyValue(), gp1.getDescription(), null, null)
			.withGlobalProperty(gp2.getProperty(), gp2.getPropertyValue(), gp2.getDescription(), gp2.getDatatypeClassname(),
				gp2.getDatatypeConfig())
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getGlobalProperties().size(), is(2));
		assertThat(module.getGlobalProperties().get(0).getProperty(), is(gp1.getProperty()));
		assertThat(module.getGlobalProperties().get(0).getPropertyValue(), is(gp1.getPropertyValue()));
		assertThat(module.getGlobalProperties().get(0).getDescription(), is(gp1.getDescription()));
		assertThat(module.getGlobalProperties().get(0).getDatatypeClassname(), is(gp1.getDatatypeClassname()));
		assertThat(module.getGlobalProperties().get(0).getDatatypeConfig(), is(gp1.getDatatypeConfig()));
		assertThat(module.getGlobalProperties().get(1).getProperty(), is(gp2.getProperty()));
		assertThat(module.getGlobalProperties().get(1).getPropertyValue(), is(gp2.getPropertyValue()));
		assertThat(module.getGlobalProperties().get(1).getDescription(), is(gp2.getDescription()));
		assertThat(module.getGlobalProperties().get(1).getDatatypeClassname(), is(gp2.getDatatypeClassname()));
		assertThat(module.getGlobalProperties().get(1).getDatatypeConfig(), is(gp2.getDatatypeConfig()));
	}

	@Test
	public void parse_shouldParseGlobalPropertyAndTrimWhitespacesFromDescription() throws IOException {

		Document config = buildOnValidConfigXml()
			.withGlobalProperty("report.deleteReportsAgeInHours", "72", "  \n\t delete reports after\t hours  ", null, null)
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getGlobalProperties().size(), is(1));
		assertThat(module.getGlobalProperties().get(0).getDescription(), is("delete reports after hours"));
	}

	@Test
	public void parse_shouldParseGlobalPropertyWithoutDescriptionElement() throws IOException {

		Document config = buildOnValidConfigXml()
			.withGlobalProperty("report.deleteReportsAgeInHours", "72", null, null, null)
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getGlobalProperties().size(), is(1));
		assertThat(module.getGlobalProperties().get(0).getProperty(), is("report.deleteReportsAgeInHours"));
		assertThat(module.getGlobalProperties().get(0).getDescription(), is(""));
	}

	@Test
	public void parse_shouldParseGlobalPropertyContainingElementsNotIncludedInGlobalProperty() throws IOException {

		GlobalProperty gp1 = new GlobalProperty("report.deleteReportsAgeInHours", "72", "delete reports after");
		Document config = buildOnValidConfigXml()
			.withGlobalProperty(gp1.getProperty(), gp1.getPropertyValue(), gp1.getDescription(), null, null)
			.build();
		config.getElementsByTagName("globalProperty").item(0).appendChild(config.createElement("ignoreMe"));

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getGlobalProperties().size(), is(1));
		assertThat(module.getGlobalProperties().get(0).getProperty(), is(gp1.getProperty()));
		assertThat(module.getGlobalProperties().get(0).getPropertyValue(), is(gp1.getPropertyValue()));
		assertThat(module.getGlobalProperties().get(0).getDescription(), is(gp1.getDescription()));
	}

	@Test
	public void parse_shouldIgnoreGlobalPropertyWithoutChildren() throws IOException {

		Document config = buildOnValidConfigXml().build();
		config.getDocumentElement().appendChild(config.createElement("globalProperty"));

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getGlobalProperties(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldIgnoreGlobalPropertyOnlyContainingText() throws IOException {

		Document config = buildOnValidConfigXml()
			.withTextNode("globalProperty", "will be ignored")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getGlobalProperties(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldIgnoreGlobalPropertyWithoutPropertyElement() throws IOException {

		Document config = buildOnValidConfigXml()
			.withGlobalProperty(null, "72", "some", null, null)
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getGlobalProperties(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldIgnoreGlobalPropertyWithEmptyProperty() throws IOException {

		Document config = buildOnValidConfigXml()
			.withGlobalProperty("  ", "72", "some", null, null)
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getGlobalProperties(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldIgnoreGlobalPropertyWithDatatypeClassThatIsNotSubclassingCustomDatatype() throws IOException {

		Document config = buildOnValidConfigXml()
			.withGlobalProperty("report.deleteReportsAgeInHours", "72", "some", "java.lang.String", null)
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getGlobalProperties(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldIgnoreGlobalPropertyWithDatatypeClassThatIsNotFound() throws IOException {

		Document config = buildOnValidConfigXml()
			.withGlobalProperty("report.deleteReportsAgeInHours", "72", "some", "String", null)
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getGlobalProperties(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldParseMappingFiles() throws IOException {

		Document config = buildOnValidConfigXml()
			.withTextNode("mappingFiles", "\n  ReportDesign.hbm.xml ReportDesign.hbm.xml\n \t\tReportRequest.hbm.xml  \t")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getMappingFiles().size(), is(3));
		assertThat(module.getMappingFiles(), hasItems("ReportDesign.hbm.xml", "ReportRequest.hbm.xml"));
	}

	@Test
	public void parse_shouldIgnoreMappingFilesOnlyContainingWhitespaces() throws IOException {

		Document config = buildOnValidConfigXml()
			.withTextNode("mappingFiles", "\n  \n \t\t      \t")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getMappingFiles(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldParsePackagesWithMappedClasses() throws IOException {

		Document config = buildOnValidConfigXml()
			.withTextNode(
				"packagesWithMappedClasses",
				"\n  org.openmrs.module.openconceptlab org.openmrs.module.openconceptlab\n \torg.openmrs.module.systemmetrics  \t")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getPackagesWithMappedClasses().size(), is(2));
		assertThat(module.getPackagesWithMappedClasses(),
			hasItems("org.openmrs.module.openconceptlab", "org.openmrs.module.systemmetrics"));
	}

	@Test
	public void parse_shouldIgnorePackagesWithMappedClassesOnlyContainingWhitespaces() throws IOException {

		Document config = buildOnValidConfigXml()
			.withTextNode("packagesWithMappedClasses", "\n  \n \t\t      \t")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getPackagesWithMappedClasses(), is(equalTo(Collections.EMPTY_SET)));
	}

	@Test
	public void parse_shouldParseMandatoryAtSpecificConfigVersion() throws IOException {

		Document config = buildOnValidConfigXml("1.3")
			.withTextNode("mandatory", " true   ")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.isMandatory(), is(true));
	}

	@Test
	public void parse_shouldParseMandatoryAfterSpecificConfigVersion() throws IOException {

		Document config = buildOnValidConfigXml("1.4")
			.withTextNode("mandatory", " true   ")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.isMandatory(), is(true));
	}

	@Test
	public void parse_shouldParseMandatoryAndSetToFalse() throws IOException {

		Document config = buildOnValidConfigXml("1.4")
			.withTextNode("mandatory", " false   ")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.isMandatory(), is(false));
	}

	@Test
	public void parse_shouldIgnoreEmptyMandatory() throws IOException {

		Document config = buildOnValidConfigXml("1.4")
			.withTextNode("mandatory", "")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.isMandatory(), is(false));
	}

	@Test
	public void parse_shouldIgnoreMandatoryBeforeSpecificConfigVersion() throws IOException {

		Document config = buildOnValidConfigXml("1.2")
			.withTextNode("mandatory", " true   ")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.isMandatory(), is(false));
	}

	@Test
	public void parse_shouldParseConditionalResources() throws IOException {

		HashMap<String, String> modules = new HashMap<>();
		modules.put("metadatamapping", "1.0");
		modules.put("reporting", "2.0");
		Document config = buildOnValidConfigXml("1.2")
			.withConditionalResource("/lib/htmlformentry-api-1.10*", "1.10")
			.withConditionalResource("/lib/metadatasharing-api-1.9*", "1.9", modules)
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		ModuleConditionalResource htmlformentry = new ModuleConditionalResource();
		htmlformentry.setPath("/lib/htmlformentry-api-1.10*");
		htmlformentry.setOpenmrsPlatformVersion("1.10");

		ModuleConditionalResource metadatasharing = new ModuleConditionalResource();
		metadatasharing.setPath("/lib/metadatasharing-api-1.9*");
		metadatasharing.setOpenmrsPlatformVersion("1.9");
		ModuleConditionalResource.ModuleAndVersion metadatamapping = new ModuleConditionalResource.ModuleAndVersion();
		metadatamapping.setModuleId("metadatamapping");
		metadatamapping.setVersion("1.0");
		ModuleConditionalResource.ModuleAndVersion reporting = new ModuleConditionalResource.ModuleAndVersion();
		reporting.setModuleId("reporting");
		reporting.setVersion("2.0");

		metadatasharing.setModules(Arrays.asList(metadatamapping, reporting));

		assertThat(module.getConditionalResources().size(), is(2));
		assertThat(module.getConditionalResources(), contains(htmlformentry, metadatasharing));
	}

	@Test
	public void parse_shouldParseConditionalResourcesWithWhitespace() throws IOException {

		Document config = buildOnValidConfigXml("1.2")
			.withTextNode("conditionalResources", "        	")
			.withConditionalResource("/lib/htmlformentry-api-1.10*", "1.10")
			.build();
		config.getElementsByTagName("conditionalResource")
			.item(0)
			.appendChild(config.createTextNode("        	"));

		Module module = parser.parse(writeConfigXmlToFile(config));

		ModuleConditionalResource htmlformentry = new ModuleConditionalResource();
		htmlformentry.setPath("/lib/htmlformentry-api-1.10*");
		htmlformentry.setOpenmrsPlatformVersion("1.10");

		assertThat(module.getConditionalResources().size(), is(1));
		assertThat(module.getConditionalResources(), contains(htmlformentry));
	}

	@Test
	public void parse_shouldParseConditionalResourcesEvenIfVersionIsMissing() throws IOException {

		Document config = buildOnValidConfigXml("1.2")
			.withConditionalResource("/lib/htmlformentry-api-1.10*", null)
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		ModuleConditionalResource htmlformentry = new ModuleConditionalResource();
		htmlformentry.setPath("/lib/htmlformentry-api-1.10*");

		assertThat(module.getConditionalResources().size(), is(1));
		assertThat(module.getConditionalResources(), contains(htmlformentry));
	}

	@Test
	public void parse_shouldParseConditionalResourcesEvenIfPathIsMissing() throws IOException {

		Document config = buildOnValidConfigXml("1.2")
			.withConditionalResource(null, "1.10")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		ModuleConditionalResource conditionalResource = new ModuleConditionalResource();
		conditionalResource.setOpenmrsPlatformVersion("1.10");

		assertThat(module.getConditionalResources().size(), is(1));
		assertThat(module.getConditionalResources(), contains(conditionalResource));
	}

	@Test
	public void parse_shouldParseConditionalResourcesEvenIfPathAndVersionAreMissing() throws IOException {

		Document config = buildOnValidConfigXml("1.2")
			.withConditionalResource(null, null)
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getConditionalResources().size(), is(1));
		assertThat(module.getConditionalResources(), contains(new ModuleConditionalResource()));
	}

	@Test
	public void parse_shouldFailIfMultipleConditionalResourcesTagsFound() throws IOException {

		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Found multiple conditionalResources tags.");

		Document config = buildOnValidConfigXml("1.2")
			.withTextNode("conditionalResources", "")
			.withTextNode("conditionalResources", "")
			.build();

		parser.parse(writeConfigXmlToFile(config));
	}

	@Test
	public void parse_shouldFailIfConditionalResourcesContainInvalidTags() throws IOException {

		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Found the invalidTag node under conditionalResources.");

		Document config = buildOnValidConfigXml("1.2")
			.withConditionalResource("/lib/reporting-api-1.9.*", "1.10")
			.build();
		config.getElementsByTagName("conditionalResources")
			.item(0)
			.appendChild(config.createElement("invalidTag"));

		parser.parse(writeConfigXmlToFile(config));
	}

	@Test
	public void parse_shouldFailIfConditionalResourcePathIsBlank() throws IOException {

		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("The path of a conditional resource must not be blank");

		Document config = buildOnValidConfigXml("1.2")
			.withConditionalResource("", "1.10")
			.build();

		parser.parse(writeConfigXmlToFile(config));
	}

	@Test
	public void parse_shouldParseAdvice() throws IOException {

		AdvicePoint a1 = new AdvicePoint("org.openmrs.api.PatientService", String.class);
		AdvicePoint a2 = new AdvicePoint("org.openmrs.api.PersonService", String.class);
		Document config = buildOnValidConfigXml()
			.withAdvice(a1.getPoint(), "String")
			.withAdvice(a2.getPoint(), "String")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getAdvicePoints().size(), is(2));
		assertThat(module.getAdvicePoints().get(0).getPoint(), is(a1.getPoint()));
		assertThat(module.getAdvicePoints().get(0).getClassName(), is("String"));
		assertThat(module.getAdvicePoints().get(1).getPoint(), is(a2.getPoint()));
		assertThat(module.getAdvicePoints().get(1).getClassName(), is("String"));
	}

	@Test
	public void parse_shouldParseAdviceContainingElementsOtherThanPointAndClass() throws IOException {

		Document config = buildOnValidConfigXml()
			.withAdvice("org.openmrs.api.PatientService", "String")
			.build();
		config.getElementsByTagName("advice").item(0).appendChild(config.createElement("ignoreMe"));

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getAdvicePoints().size(), is(1));
	}

	@Test
	public void parse_shouldIgnoreAdviceWithoutChildren() throws IOException {

		Document config = buildOnValidConfigXml().build();
		config.getDocumentElement().appendChild(config.createElement("advice"));

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getAdvicePoints(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldIgnoreAdviceOnlyContainingText() throws IOException {

		Document config = buildOnValidConfigXml()
			.withTextNode("advice", "will be ignored")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getAdvicePoints(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldIgnoreAdviceWithoutClass() throws IOException {

		Document config = buildOnValidConfigXml()
			.withAdvice("org.openmrs.api.PatientService", null)
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getAdvicePoints(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void parse_shouldIgnoreAdviceWithoutPoint() throws IOException {

		Document config = buildOnValidConfigXml()
			.withAdvice(null, "String")
			.build();

		Module module = parser.parse(writeConfigXmlToFile(config));

		assertThat(module.getAdvicePoints(), is(equalTo(Collections.EMPTY_LIST)));
	}

	private void expectModuleExceptionWithMessage(String expectedMessage) {
		expectedException.expect(ModuleException.class);
		expectedException.expectMessage(expectedMessage);
	}

	private void whenGettingMessageFromMessageSourceServiceWithKeyReturnSameKey(String messageKey) {
		when(messageSourceService.getMessage(messageKey)).thenReturn(messageKey);
	}

	private ModuleConfigXmlBuilder buildOnValidConfigXml() {

		return buildOnValidConfigXml("1.6");
	}

	private ModuleConfigXmlBuilder buildOnValidConfigXml(String version) {

		return new ModuleConfigXmlBuilder(documentBuilder)
			.withModuleRoot()
			.withConfigVersion(version)
			.withModuleName("Reporting")
			.withModuleId("reporting")
			.withPackage("org.openmrs.module.reporting");
	}

	private class ModuleConfigXmlBuilder {

		private Document configXml;

		public ModuleConfigXmlBuilder(DocumentBuilder documentBuilder) {
			this.configXml = documentBuilder.newDocument();
		}

		public ModuleConfigXmlBuilder withDoctype(String configVersion) {
			DOMImplementation domImpl = this.configXml.getImplementation();
			DocumentType doctype = domImpl.createDocumentType(
				"module",
				"-//OpenMRS//DTD OpenMRS Config 1.0//EN",
				"https://resources.openmrs.org/doctype/config-" + configVersion + ".dtd"
			);
			this.configXml.appendChild(doctype);
			return this;
		}

		public ModuleConfigXmlBuilder withModuleRoot() {
			Element root = configXml.createElement("module");
			configXml.appendChild(root);
			return this;
		}

		public ModuleConfigXmlBuilder withConfigVersion(String version) {
			configXml.getDocumentElement().setAttribute("configVersion", version);
			return this;
		}

		public ModuleConfigXmlBuilder withModuleName(String name) {
			this.withTextNode("name", name);
			return this;
		}

		public ModuleConfigXmlBuilder withModuleId(String id) {
			this.withTextNode("id", id);
			return this;
		}

		public ModuleConfigXmlBuilder withPackage(String packageName) {
			this.withTextNode("package", packageName);
			return this;
		}

		public ModuleConfigXmlBuilder withTextNode(String tag, String text) {
			Element element = configXml.createElement(tag);
			element.setTextContent(text);
			configXml.getDocumentElement().appendChild(element);
			return this;
		}

		public ModuleConfigXmlBuilder withRequireModules(String[]... modules) {
			Element requireModules = configXml.createElement("require_modules");
			for (String[] module : modules) {
				Element requireModule = configXml.createElement("require_module");
				requireModule.setTextContent(module[0]);
				if (module.length > 1) {
					requireModule.setAttribute("version", module[1]);
				}
				requireModules.appendChild(requireModule);
			}
			configXml.getDocumentElement().appendChild(requireModules);
			return this;
		}

		public ModuleConfigXmlBuilder withAwareOfModules(String... modules) {
			Element awareOfModules = configXml.createElement("aware_of_modules");
			for (String module : modules) {
				Element awareOfModule = configXml.createElement("aware_of_module");
				awareOfModule.setTextContent(module);
				awareOfModules.appendChild(awareOfModule);
			}
			configXml.getDocumentElement().appendChild(awareOfModules);
			return this;
		}

		public ModuleConfigXmlBuilder withPrivilege(String name, String description) {
			Map<String, String> children = new HashMap<>();
			if (name != null) {
				children.put("name", name);
			}
			if (description != null) {
				children.put("description", description);
			}
			return withElementsAttachedToRoot("privilege", children);
		}

		public ModuleConfigXmlBuilder withExtension(String point, String className) {
			Map<String, String> children = new HashMap<>();
			if (point != null) {
				children.put("point", point);
			}
			if (className != null) {
				children.put("class", className);
			}
			return withElementsAttachedToRoot("extension", children);
		}

		public ModuleConfigXmlBuilder withAdvice(String point, String className) {
			Map<String, String> children = new HashMap<>();
			if (point != null) {
				children.put("point", point);
			}
			if (className != null) {
				children.put("class", className);
			}
			return withElementsAttachedToRoot("advice", children);
		}

		public ModuleConfigXmlBuilder withGlobalProperty(String property, String defaultValue, String description,
			String datatypeClassname, String datatypeConfig) {
			Map<String, String> children = new HashMap<>();
			if (property != null) {
				children.put("property", property);
			}
			if (defaultValue != null) {
				children.put("defaultValue", defaultValue);
			}
			if (description != null) {
				children.put("description", description);
			}
			if (datatypeClassname != null) {
				children.put("datatypeClassname", datatypeClassname);
			}
			if (datatypeConfig != null) {
				children.put("datatypeConfig", datatypeConfig);
			}
			return withElementsAttachedToRoot("globalProperty", children);
		}

		public ModuleConfigXmlBuilder withConditionalResource(String path, String openmrsVersion) {
			return withConditionalResource(path, openmrsVersion, null);
		}

		public ModuleConfigXmlBuilder withConditionalResource(String path, String openmrsVersion,
			Map<String, String> modules) {
			Map<String, String> children = new HashMap<>();
			if (path != null) {
				children.put("path", path);
			}
			if (openmrsVersion != null) {
				children.put("openmrsVersion", openmrsVersion);
			}
			Element conditionalResource = createElementWithChildren("conditionalResource", children);
			attachToGroupElement("conditionalResources", conditionalResource);
			if (modules != null) {
				Element modulesElement = configXml.createElement("modules");
				for (Map.Entry<String, String> entries : modules.entrySet()) {
					Element module = configXml.createElement("module");
					module.appendChild(createElement("moduleId", entries.getKey()));
					module.appendChild(createElement("version", entries.getValue()));
					modulesElement.appendChild(module);
				}
				conditionalResource.appendChild(modulesElement);
			}
			return this;
		}

		public ModuleConfigXmlBuilder withElementsAttachedToRoot(String parentElementName,
			Map<String, String> childElements) {
			configXml.getDocumentElement().appendChild(createElementWithChildren(parentElementName, childElements));
			return this;
		}

		private Element createElementWithChildren(String parentElementName, Map<String, String> childElements) {
			Element parentElement = configXml.createElement(parentElementName);
			for (Map.Entry<String, String> child : childElements.entrySet()) {
				parentElement.appendChild(createElement(child.getKey(), child.getValue()));
			}
			return parentElement;
		}

		private Element createElement(String name, String value) {
			Element element = configXml.createElement(name);
			element.setTextContent(value);
			return element;
		}

		private void attachToGroupElement(String groupElementName, Element element) {
			Element parent;
			NodeList nodes = configXml.getElementsByTagName(groupElementName);
			if (nodes.getLength() == 0) {
				parent = configXml.createElement(groupElementName);
				configXml.getDocumentElement().appendChild(parent);
			} else {
				parent = (Element) nodes.item(0);
			}
			parent.appendChild(element);
		}

		public Document build() {
			return configXml;
		}
	}

	private File writeConfigXmlToFile(Document config) throws IOException {
		File file = temporaryFolder.newFile("modulefileparsertest.omod");
		JarOutputStream jar = createJarWithConfigXmlEntry(file);
		writeConfigXmlToJar(jar, config);
		return file;
	}

	private JarOutputStream createJarWithConfigXmlEntry(File file) throws IOException {
		JarOutputStream jar = new JarOutputStream(new FileOutputStream(file));
		ZipEntry config = new ZipEntry("config.xml");
		jar.putNextEntry(config);
		return jar;
	}

	private File writeConfigXmlToFile(String config) throws IOException {
		File file = temporaryFolder.newFile("modulefileparsertest.omod");
		JarOutputStream jar = createJarWithConfigXmlEntry(file);
		jar.write(config.getBytes());
		jar.closeEntry();
		jar.close();
		return file;
	}

	private void writeConfigXmlToJar(JarOutputStream jar, Document config) throws IOException {
		jar.write(getByteArray(config));
		jar.closeEntry();
		jar.close();
	}

	private byte[] getByteArray(Document config) {
		return getString(config).getBytes();
	}

	private String getString(Document config) {
		DOMImplementationLS impl = (DOMImplementationLS) config.getImplementation();
		LSSerializer serializer = impl.createLSSerializer();
		LSOutput out = impl.createLSOutput();
		out.setEncoding("UTF-8");
		Writer stringWriter = new StringWriter();
		out.setCharacterStream(stringWriter);
		serializer.write(config, out);
		return stringWriter.toString();
	}

	static class AccessibleExtension extends Extension {

		@Override
		public Extension.MEDIA_TYPE getMediaType() {
			return null;
		}
	}

	private void registerModuleClassloader(Module module) {
		// module.getExtensions() is the only public way for us to assert the extensions have properly been parsed
		// unfortunately it is contains quite some logic, creating instances of above extension classes
		// we therefore also need to register a ModuleClassLoader otherwise getExtension always returns an empty list
		ModuleClassLoader moduleClassLoader = new ModuleClassLoader(module, new ArrayList<>(), getClass().getClassLoader());
		ModuleFactory.getModuleClassLoaderMap().put(module, moduleClassLoader);
	}
}

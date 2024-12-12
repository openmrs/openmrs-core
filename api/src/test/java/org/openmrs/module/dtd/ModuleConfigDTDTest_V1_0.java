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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openmrs.module.dtd.ConfigXmlBuilder.withMinimalTags;
import static org.openmrs.module.dtd.ConfigXmlBuilder.writeToInputStream;
import static org.openmrs.module.dtd.DtdTestValidator.isValidConfigXml;

public class ModuleConfigDTDTest_V1_0 {
	
	private static final String[] compatibleVersions = new String[] { "1.0", "1.1", "1.2", "1.3", "1.4", "1.5", "1.6", "1.7" };
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlWithMinimalRequirements(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlFailsWhenOutOfOrder(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = new ConfigXmlBuilder(version).withName("Basicexample") // id should be first
		        .withId("basicexample").withVersion("1.2.3").withPackage("org.openmrs.module.basicexample")
		        .withAuthor("Community").withDescription("First module")
		        .withActivator("org.openmrs.module.basicexample.BasicexampleActivator")
		        .withInvalidTag("org.openmrs.module.basicexample.BasicexampleActivator").build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlFailsWithMissingId(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = new ConfigXmlBuilder(version).withName("Basicexample").withVersion("1.2.3")
		        .withPackage("org.openmrs.module.basicexample").withAuthor("Community").withDescription("First module")
		        .withActivator("org.openmrs.module.basicexample.BasicexampleActivator").build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlFailsWithMissingName(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = new ConfigXmlBuilder(version).withId("basicexample").withVersion("1.2.3")
		        .withPackage("org.openmrs.module.basicexample").withAuthor("Community").withDescription("First module")
		        .withActivator("org.openmrs.module.basicexample.BasicexampleActivator").build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlFailsWithMissingVersion(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = new ConfigXmlBuilder(version).withId("basicexample").withName("Basicexample")
		        .withPackage("org.openmrs.module.basicexample").withAuthor("Community").withDescription("First module")
		        .withActivator("org.openmrs.module.basicexample.BasicexampleActivator").build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlFailsWithMissingPackage(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = new ConfigXmlBuilder(version).withId("basicexample").withName("Basicexample")
		        .withVersion("1.2.3").withAuthor("Community").withDescription("First module")
		        .withActivator("org.openmrs.module.basicexample.BasicexampleActivator").build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlFailsWithMissingAuthor(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = new ConfigXmlBuilder(version).withId("basicexample").withName("Basicexample")
		        .withVersion("1.2.3").withPackage("org.openmrs.module.basicexample").withDescription("First module")
		        .withActivator("org.openmrs.module.basicexample.BasicexampleActivator").build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlFailsWithMissingDescription(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = new ConfigXmlBuilder(version).withId("basicexample").withName("Basicexample")
		        .withVersion("1.2.3").withPackage("org.openmrs.module.basicexample").withAuthor("Community")
		        .withActivator("org.openmrs.module.basicexample.BasicexampleActivator").build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlFailsWithMissingActivator(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = new ConfigXmlBuilder(version).withId("basicexample").withName("Basicexample")
		        .withVersion("1.2.3").withPackage("org.openmrs.module.basicexample").withAuthor("Community")
		        .withDescription("First module").build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlValidRequireModulesWithSingleModule(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = new ConfigXmlBuilder(version).withId("basicexample").withName("Basicexample")
		        .withVersion("1.2.3").withPackage("org.openmrs.module.basicexample").withAuthor("Community")
		        .withDescription("First module").withActivator("org.openmrs.module.basicexample.BasicexampleActivator")
		        .withRequireModules("module1").build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlValidRequireModulesWithMultipleModules(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = new ConfigXmlBuilder(version).withId("basicexample").withName("Basicexample")
		        .withVersion("1.2.3").withPackage("org.openmrs.module.basicexample").withAuthor("Community")
		        .withDescription("First module").withActivator("org.openmrs.module.basicexample.BasicexampleActivator")
		        .withRequireModules("module1", "module2", "module3").build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlequireModulesMissingModules(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = new ConfigXmlBuilder(version).withId("basicexample").withName("Basicexample")
		        .withVersion("1.2.3").withPackage("org.openmrs.module.basicexample").withAuthor("Community")
		        .withDescription("First module").withActivator("org.openmrs.module.basicexample.BasicexampleActivator")
		        .withRequireModules().build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlFailsWithInvalidTag(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version).withInvalidTag("some text").build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlLibraryWithResourcesType(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version)
		        .withLibrary(Optional.of("id"), Optional.of("path/to/library"), Optional.of("resources")).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlLibraryWithLibraryType(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version)
		        .withLibrary(Optional.of("id"), Optional.of("path/to/library"), Optional.of("library")).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlMultipleLibraries(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version)
		        .withLibrary(Optional.of("id1"), Optional.of("path/to/library1"), Optional.of("resources"))
		        .withLibrary(Optional.of("id2"), Optional.of("path/to/library2"), Optional.of("library")).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlLibraryWithMissingId(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version)
		        .withLibrary(Optional.empty(), Optional.of("path/to/library"), Optional.of("library")).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlLibraryWithMissingPath(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version)
		        .withLibrary(Optional.of("id"), Optional.empty(), Optional.of("library")).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlLibraryWithMissingLibrary(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version)
		        .withLibrary(Optional.of("id"), Optional.of("path/to/library"), Optional.empty()).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlLibraryWithInvalidLibrary(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version)
		        .withLibrary(Optional.of("id"), Optional.of("path/to/library"), Optional.of("invalid")).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlValidExtension(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version)
		        .withExtension(Optional.of("org.openmrs.extensionPoint"), Optional.of("ExampleExtension")).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlExtensionMissingPoint(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version).withExtension(Optional.empty(), Optional.of("ExampleExtension"))
		        .build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlExtensionMissingClass(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version)
		        .withExtension(Optional.of("org.openmrs.extensionPoint"), Optional.empty()).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlValidAdvice(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version)
		        .withAdvice(Optional.of("org.openmrs.advicePoint"), Optional.of("ExampleAdvice")).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlAdviceMissingPoint(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version).withExtension(Optional.empty(), Optional.of("ExampleAdvice")).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlAdviceMissingClass(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = null;
		try {
			configXml = withMinimalTags(version).withExtension(Optional.of("org.openmrs.advicePoint"), Optional.empty())
					.build();
		} catch (TransformerException e) {
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlValidPrivilege(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = null;
		try {
			configXml = withMinimalTags(version).withPrivilege(Optional.of("Manage Reports"), Optional.of("Add report"))
					.build();
		} catch (TransformerException e) {
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlPrivilegeMissingName(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version).withPrivilege(Optional.empty(), Optional.of("Add report")).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlPrivilegeMissingDescription(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version).withPrivilege(Optional.of("Manage Reports"), Optional.empty()).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlValidGlobalProperty(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version).withGlobalProperty(Optional.of("report.deleteReportsAgeInHours"),
		    Optional.of("48"), Optional.of("delete reports after hours")).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlValidGlobalPropertyWithoutDefault(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version).withGlobalProperty(Optional.of("report.deleteReportsAgeInHours"),
		    Optional.empty(), Optional.of("delete reports after hours")).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlInvalidGlobalPropertyMissingProperty(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version)
		        .withGlobalProperty(Optional.empty(), Optional.empty(), Optional.of("delete reports after hours")).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlValidGlobalPropertyMissingDescription(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version)
		        .withGlobalProperty(Optional.of("report.deleteReportsAgeInHours"), Optional.empty(), Optional.empty())
		        .build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlValidDwrAllFields(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		
		ConfigXmlBuilder.Param param1 = new ConfigXmlBuilder.Param(Optional.of("name1"), Optional.of("val1"));
		ConfigXmlBuilder.Param param2 = new ConfigXmlBuilder.Param(Optional.of("name2"), Optional.of("val2"));
		
		ConfigXmlBuilder.Create create1 = new ConfigXmlBuilder.Create(Optional.of("creator1"), Optional.of("javascript1"),
		        Optional.of(param1));
		ConfigXmlBuilder.Create create2 = new ConfigXmlBuilder.Create(Optional.of("creator2"), Optional.of("javascript2"),
		        Optional.of(param2));
		
		ConfigXmlBuilder.Convert convert1 = new ConfigXmlBuilder.Convert(Optional.of(param1), Optional.of("converter1"),
		        Optional.of("match1"));
		ConfigXmlBuilder.Convert convert2 = new ConfigXmlBuilder.Convert(Optional.of(param2), Optional.of("converter2"),
		        Optional.of("match2"));
		
		ConfigXmlBuilder.Allow allow = new ConfigXmlBuilder.Allow();
		
		allow.addCreate(create1);
		allow.addCreate(create2);
		
		allow.addConvert(convert1);
		allow.addConvert(convert2);
		
		ConfigXmlBuilder.Dwr dwr = new ConfigXmlBuilder.Dwr(Optional.of(allow), Optional.of("signatures"));
		
		Document configXml = withMinimalTags(version).withDwr(dwr).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlValidDwrWithoutSigs(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		
		ConfigXmlBuilder.Param param1 = new ConfigXmlBuilder.Param(Optional.of("name1"), Optional.of("val1"));
		ConfigXmlBuilder.Param param2 = new ConfigXmlBuilder.Param(Optional.of("name2"), Optional.of("val2"));
		
		ConfigXmlBuilder.Create create1 = new ConfigXmlBuilder.Create(Optional.of("creator1"), Optional.of("javascript1"),
		        Optional.of(param1));
		ConfigXmlBuilder.Create create2 = new ConfigXmlBuilder.Create(Optional.of("creator2"), Optional.of("javascript2"),
		        Optional.of(param2));
		
		ConfigXmlBuilder.Convert convert1 = new ConfigXmlBuilder.Convert(Optional.of(param1), Optional.of("converter1"),
		        Optional.of("match1"));
		ConfigXmlBuilder.Convert convert2 = new ConfigXmlBuilder.Convert(Optional.of(param2), Optional.of("converter2"),
		        Optional.of("match2"));
		
		ConfigXmlBuilder.Allow allow = new ConfigXmlBuilder.Allow();
		
		allow.addCreate(create1);
		allow.addCreate(create2);
		
		allow.addConvert(convert1);
		allow.addConvert(convert2);
		
		ConfigXmlBuilder.Dwr dwr = new ConfigXmlBuilder.Dwr(Optional.of(allow), Optional.empty());
		
		Document configXml = withMinimalTags(version).withDwr(dwr).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlValidDwrWithoutCreate(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		
		ConfigXmlBuilder.Param param1 = new ConfigXmlBuilder.Param(Optional.of("name1"), Optional.of("val1"));
		ConfigXmlBuilder.Param param2 = new ConfigXmlBuilder.Param(Optional.of("name2"), Optional.of("val2"));
		
		ConfigXmlBuilder.Convert convert1 = new ConfigXmlBuilder.Convert(Optional.of(param1), Optional.of("converter1"),
		        Optional.of("match1"));
		ConfigXmlBuilder.Convert convert2 = new ConfigXmlBuilder.Convert(Optional.of(param2), Optional.of("converter2"),
		        Optional.of("match2"));
		
		ConfigXmlBuilder.Allow allow = new ConfigXmlBuilder.Allow();
		
		allow.addConvert(convert1);
		allow.addConvert(convert2);
		
		ConfigXmlBuilder.Dwr dwr = new ConfigXmlBuilder.Dwr(Optional.of(allow), Optional.empty());
		
		Document configXml = withMinimalTags(version).withDwr(dwr).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlValidDwrWithoutConvert(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		
		ConfigXmlBuilder.Param param1 = new ConfigXmlBuilder.Param(Optional.of("name1"), Optional.of("val1"));
		ConfigXmlBuilder.Param param2 = new ConfigXmlBuilder.Param(Optional.of("name2"), Optional.of("val2"));
		
		ConfigXmlBuilder.Create create1 = new ConfigXmlBuilder.Create(Optional.of("creator1"), Optional.of("javascript1"),
		        Optional.of(param1));
		ConfigXmlBuilder.Create create2 = new ConfigXmlBuilder.Create(Optional.of("creator2"), Optional.of("javascript2"),
		        Optional.of(param2));
		
		ConfigXmlBuilder.Allow allow = new ConfigXmlBuilder.Allow();
		
		allow.addCreate(create1);
		allow.addCreate(create2);
		
		ConfigXmlBuilder.Dwr dwr = new ConfigXmlBuilder.Dwr(Optional.of(allow), Optional.of("signatures"));
		
		Document configXml = withMinimalTags(version).withDwr(dwr).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlDwrFailsMissingAllow(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		
		ConfigXmlBuilder.Dwr dwr = new ConfigXmlBuilder.Dwr(Optional.empty(), Optional.empty());
		
		Document configXml = withMinimalTags(version).withDwr(dwr).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlDwrMissingCreateCreator(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		
		ConfigXmlBuilder.Param param1 = new ConfigXmlBuilder.Param(Optional.of("name1"), Optional.of("val1"));
		ConfigXmlBuilder.Param param2 = new ConfigXmlBuilder.Param(Optional.of("name2"), Optional.of("val2"));
		
		ConfigXmlBuilder.Create create1 = new ConfigXmlBuilder.Create(Optional.of("creator1"), Optional.of("javascript1"),
		        Optional.of(param1));
		ConfigXmlBuilder.Create create2 = new ConfigXmlBuilder.Create(Optional.empty(), Optional.of("javascript2"),
		        Optional.of(param2));
		
		ConfigXmlBuilder.Convert convert1 = new ConfigXmlBuilder.Convert(Optional.of(param1), Optional.of("converter1"),
		        Optional.of("match1"));
		ConfigXmlBuilder.Convert convert2 = new ConfigXmlBuilder.Convert(Optional.of(param2), Optional.of("converter2"),
		        Optional.of("match2"));
		
		ConfigXmlBuilder.Allow allow = new ConfigXmlBuilder.Allow();
		
		allow.addCreate(create1);
		allow.addCreate(create2);
		
		allow.addConvert(convert1);
		allow.addConvert(convert2);
		
		ConfigXmlBuilder.Dwr dwr = new ConfigXmlBuilder.Dwr(Optional.of(allow), Optional.of("signatures"));
		
		Document configXml = withMinimalTags(version).withDwr(dwr).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlDwrMissingJavascript(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		
		ConfigXmlBuilder.Param param1 = new ConfigXmlBuilder.Param(Optional.of("name1"), Optional.of("val1"));
		ConfigXmlBuilder.Param param2 = new ConfigXmlBuilder.Param(Optional.of("name2"), Optional.of("val2"));
		
		ConfigXmlBuilder.Create create1 = new ConfigXmlBuilder.Create(Optional.of("creator1"), Optional.empty(),
		        Optional.of(param1));
		ConfigXmlBuilder.Create create2 = new ConfigXmlBuilder.Create(Optional.of("creator1"), Optional.of("javascript2"),
		        Optional.of(param1));
		
		ConfigXmlBuilder.Convert convert1 = new ConfigXmlBuilder.Convert(Optional.of(param1), Optional.of("converter1"),
		        Optional.of("match1"));
		ConfigXmlBuilder.Convert convert2 = new ConfigXmlBuilder.Convert(Optional.of(param2), Optional.of("converter2"),
		        Optional.of("match2"));
		
		ConfigXmlBuilder.Allow allow = new ConfigXmlBuilder.Allow();
		
		allow.addCreate(create1);
		allow.addCreate(create2);
		
		allow.addConvert(convert1);
		allow.addConvert(convert2);
		
		ConfigXmlBuilder.Dwr dwr = new ConfigXmlBuilder.Dwr(Optional.of(allow), Optional.of("signatures"));
		
		Document configXml = withMinimalTags(version).withDwr(dwr).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlDwrMissingCreateParam(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		
		ConfigXmlBuilder.Param param1 = new ConfigXmlBuilder.Param(Optional.of("name1"), Optional.of("val1"));
		ConfigXmlBuilder.Param param2 = new ConfigXmlBuilder.Param(Optional.of("name2"), Optional.of("val2"));
		
		ConfigXmlBuilder.Create create1 = new ConfigXmlBuilder.Create(Optional.of("creator1"), Optional.empty(),
		        Optional.empty());
		
		ConfigXmlBuilder.Convert convert1 = new ConfigXmlBuilder.Convert(Optional.of(param1), Optional.of("converter1"),
		        Optional.of("match1"));
		ConfigXmlBuilder.Convert convert2 = new ConfigXmlBuilder.Convert(Optional.of(param2), Optional.of("converter2"),
		        Optional.of("match2"));
		
		ConfigXmlBuilder.Allow allow = new ConfigXmlBuilder.Allow();
		
		allow.addCreate(create1);
		
		allow.addConvert(convert1);
		allow.addConvert(convert2);
		
		ConfigXmlBuilder.Dwr dwr = new ConfigXmlBuilder.Dwr(Optional.of(allow), Optional.of("signatures"));
		
		Document configXml = withMinimalTags(version).withDwr(dwr).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlDwrCreateWithoutParam(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		
		ConfigXmlBuilder.Param param2 = new ConfigXmlBuilder.Param(Optional.of("name2"), Optional.of("val2"));
		
		ConfigXmlBuilder.Create create1 = new ConfigXmlBuilder.Create(Optional.of("creator1"), Optional.of("javascript"),
		        Optional.empty());
		
		ConfigXmlBuilder.Convert convert1 = new ConfigXmlBuilder.Convert(Optional.empty(), Optional.of("converter1"),
		        Optional.of("match1"));
		ConfigXmlBuilder.Convert convert2 = new ConfigXmlBuilder.Convert(Optional.of(param2), Optional.of("converter2"),
		        Optional.of("match2"));
		
		ConfigXmlBuilder.Allow allow = new ConfigXmlBuilder.Allow();
		
		allow.addCreate(create1);
		
		allow.addConvert(convert1);
		allow.addConvert(convert2);
		
		ConfigXmlBuilder.Dwr dwr = new ConfigXmlBuilder.Dwr(Optional.of(allow), Optional.of("signatures"));
		
		Document configXml = withMinimalTags(version).withDwr(dwr).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlValidDwrConvertMissingConverter(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		ConfigXmlBuilder.Param param1 = new ConfigXmlBuilder.Param(Optional.of("name1"), Optional.of("val1"));
		ConfigXmlBuilder.Param param2 = new ConfigXmlBuilder.Param(Optional.of("name2"), Optional.of("val2"));
		
		ConfigXmlBuilder.Create create1 = new ConfigXmlBuilder.Create(Optional.of("creator1"), Optional.of("javascript1"),
		        Optional.of(param1));
		ConfigXmlBuilder.Create create2 = new ConfigXmlBuilder.Create(Optional.of("creator2"), Optional.of("javascript2"),
		        Optional.of(param2));
		
		ConfigXmlBuilder.Convert convert1 = new ConfigXmlBuilder.Convert(Optional.of(param1), Optional.empty(),
		        Optional.of("match1"));
		ConfigXmlBuilder.Convert convert2 = new ConfigXmlBuilder.Convert(Optional.of(param2), Optional.of("converter2"),
		        Optional.of("match2"));
		
		ConfigXmlBuilder.Allow allow = new ConfigXmlBuilder.Allow();
		
		allow.addCreate(create1);
		allow.addCreate(create2);
		
		allow.addConvert(convert1);
		allow.addConvert(convert2);
		
		ConfigXmlBuilder.Dwr dwr = new ConfigXmlBuilder.Dwr(Optional.of(allow), Optional.of("signatures"));
		
		Document configXml = withMinimalTags(version).withDwr(dwr).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlValidDwrConverterMissingMatch(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		ConfigXmlBuilder.Param param1 = new ConfigXmlBuilder.Param(Optional.of("name1"), Optional.of("val1"));
		ConfigXmlBuilder.Param param2 = new ConfigXmlBuilder.Param(Optional.of("name2"), Optional.of("val2"));
		
		ConfigXmlBuilder.Create create1 = new ConfigXmlBuilder.Create(Optional.of("creator1"), Optional.of("javascript1"),
		        Optional.of(param1));
		ConfigXmlBuilder.Create create2 = new ConfigXmlBuilder.Create(Optional.of("creator2"), Optional.of("javascript2"),
		        Optional.of(param2));
		
		ConfigXmlBuilder.Convert convert1 = new ConfigXmlBuilder.Convert(Optional.of(param1), Optional.of("converter1"),
		        Optional.of("match1"));
		ConfigXmlBuilder.Convert convert2 = new ConfigXmlBuilder.Convert(Optional.of(param2), Optional.of("converter2"),
		        Optional.empty());
		
		ConfigXmlBuilder.Allow allow = new ConfigXmlBuilder.Allow();
		
		allow.addCreate(create1);
		allow.addCreate(create2);
		
		allow.addConvert(convert1);
		allow.addConvert(convert2);
		
		ConfigXmlBuilder.Dwr dwr = new ConfigXmlBuilder.Dwr(Optional.of(allow), Optional.of("signatures"));
		
		Document configXml = withMinimalTags(version).withDwr(dwr).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlValidServlet(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version)
		        .withServlet(Optional.of("ServletName"), Optional.of("ServletClass"), Collections.emptyMap()).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlServletMissingName(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version)
		        .withServlet(Optional.empty(), Optional.of("ServletClass"), Collections.emptyMap()).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlServletMissingClass(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version)
		        .withServlet(Optional.of("ServletName"), Optional.empty(), Collections.emptyMap()).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlValidMessages(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version).withMessages(Optional.of("en-US"), Optional.of("file")).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlMessagesMissingLang(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version).withMessages(Optional.empty(), Optional.of("file")).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmMissingFile(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version).withMessages(Optional.of("en-US"), Optional.empty()).build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlWithOptionalFields(String version)
		throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version).withUpdateUrl("updateUrl").withRequireVersion("1.2.3")
		        .withRequireDatabaseVersion("1.2.4").withMappingFiles("mapping files").build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	private static Stream<Arguments> getCompatibleVersions() {
		return Arrays.stream(compatibleVersions).map(Arguments::of);
	}
}

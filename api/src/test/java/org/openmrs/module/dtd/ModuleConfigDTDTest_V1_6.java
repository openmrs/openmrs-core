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
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openmrs.module.dtd.ConfigXmlBuilder.withMinimalTags;
import static org.openmrs.module.dtd.ConfigXmlBuilder.writeToInputStream;
import static org.openmrs.module.dtd.DtdTestValidator.isValidConfigXml;

public class ModuleConfigDTDTest_V1_6 {
	
	private static final String[] compatibleVersions = new String[] { "1.6", "1.7" };
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void validXmlConditionalResourcesWithVersion(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		List<ConfigXmlBuilder.ConditionalResource> conditionalResources = new ArrayList<>();
		
		ConfigXmlBuilder.ConditionalResource conditionalResource1 = new ConfigXmlBuilder.ConditionalResource(Optional.of("path/to/resource1"), Optional.of("1.2.3"));
		ConfigXmlBuilder.ConditionalResource conditionalResource2 = new ConfigXmlBuilder.ConditionalResource(Optional.of("path/to/resource2"), Optional.of("1.2.4"));
		
		conditionalResources.add(conditionalResource1);
		conditionalResources.add(conditionalResource2);
		
		Document configXml = withMinimalTags(version)
				.withConditionalResources(conditionalResources)
				.build();

		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void validXmlConditionalResourcesWithLoadModules(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		List<ConfigXmlBuilder.ConditionalResource> conditionalResources = new ArrayList<>();
		
		ConfigXmlBuilder.ConditionalResource conditionalResource1 = new ConfigXmlBuilder.ConditionalResource(Optional.of("path/to/resource1"), Optional.empty());
		conditionalResource1.addModule(new ConfigXmlBuilder.OpenMRSModule(Optional.of("mod123"), Optional.of("1.0")));
		conditionalResource1.addModule(new ConfigXmlBuilder.OpenMRSModule(Optional.of("mod124"), Optional.of("1.1")));
		
		ConfigXmlBuilder.ConditionalResource conditionalResource2 = new ConfigXmlBuilder.ConditionalResource(Optional.of("path/to/resource2"), Optional.empty());
		conditionalResource2.addModule(new ConfigXmlBuilder.OpenMRSModule(Optional.of("mod125"), Optional.of("2.0")));
		conditionalResource2.addModule(new ConfigXmlBuilder.OpenMRSModule(Optional.of("mod126"), Optional.of("2.1")));
		
		conditionalResources.add(conditionalResource1);
		conditionalResources.add(conditionalResource2);
		
		Document configXml = withMinimalTags(version)
				.withConditionalResources(conditionalResources)
				.build();

		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void invalidXmlWhenLoadModulesPresentWithVersion(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		List<ConfigXmlBuilder.ConditionalResource> conditionalResources = new ArrayList<>();

		ConfigXmlBuilder.ConditionalResource conditionalResource1 = new ConfigXmlBuilder.ConditionalResource(Optional.of("path/to/resource1"), Optional.of("1.2.3"));
		conditionalResource1.addModule(new ConfigXmlBuilder.OpenMRSModule(Optional.of("mod123"), Optional.of("1.0")));
		conditionalResource1.addModule(new ConfigXmlBuilder.OpenMRSModule(Optional.of("mod124"), Optional.of("1.1")));

		ConfigXmlBuilder.ConditionalResource conditionalResource2 = new ConfigXmlBuilder.ConditionalResource(Optional.of("path/to/resource2"), Optional.of("1.2.4"));
		conditionalResource2.addModule(new ConfigXmlBuilder.OpenMRSModule(Optional.of("mod125"), Optional.of("2.0")));
		conditionalResource2.addModule(new ConfigXmlBuilder.OpenMRSModule(Optional.of("mod126"), Optional.of("2.1")));

		conditionalResources.add(conditionalResource1);
		conditionalResources.add(conditionalResource2);

		Document configXml = withMinimalTags(version)
			.withConditionalResources(conditionalResources)
			.build();

		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void invalidXmlWhenMissingPath(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		List<ConfigXmlBuilder.ConditionalResource> conditionalResources = new ArrayList<>();
		
		ConfigXmlBuilder.ConditionalResource conditionalResource1 = new ConfigXmlBuilder.ConditionalResource(Optional.empty(), Optional.of("1.2.3"));
		ConfigXmlBuilder.ConditionalResource conditionalResource2 = new ConfigXmlBuilder.ConditionalResource(Optional.of("path/to/resource2"), Optional.of("1.2.4"));
		
		conditionalResources.add(conditionalResource1);
		conditionalResources.add(conditionalResource2);
		
		Document configXml = withMinimalTags(version)
				.withConditionalResources(conditionalResources)
				.build();

		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}		
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void invalidXmlWhenBothVersionAndLoadModulesMissing(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		List<ConfigXmlBuilder.ConditionalResource> conditionalResources = new ArrayList<>();
		
		ConfigXmlBuilder.ConditionalResource conditionalResource1 = new ConfigXmlBuilder.ConditionalResource(Optional.of("path/to/resource1"), Optional.empty());
		ConfigXmlBuilder.ConditionalResource conditionalResource2 = new ConfigXmlBuilder.ConditionalResource(Optional.of("path/to/resource2"), Optional.empty());
		
		conditionalResources.add(conditionalResource1);
		conditionalResources.add(conditionalResource2);
		
		Document configXml = withMinimalTags(version)
				.withConditionalResources(conditionalResources)
				.build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void invalidXmlWhenLoadModulesMissingModuleId(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		List<ConfigXmlBuilder.ConditionalResource> conditionalResources = new ArrayList<>();
		
		ConfigXmlBuilder.ConditionalResource conditionalResource1 = new ConfigXmlBuilder.ConditionalResource(Optional.of("path/to/resource1"), Optional.empty());
		conditionalResource1.addModule(new ConfigXmlBuilder.OpenMRSModule(Optional.empty(), Optional.of("1.0")));
		conditionalResource1.addModule(new ConfigXmlBuilder.OpenMRSModule(Optional.of("mod124"), Optional.of("1.1")));

		conditionalResources.add(conditionalResource1);
		
		Document configXml = withMinimalTags(version)
				.withConditionalResources(conditionalResources)
				.build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void invalidXmlWhenLoadModulesMissingVersion(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		List<ConfigXmlBuilder.ConditionalResource> conditionalResources = new ArrayList<>();
		
		ConfigXmlBuilder.ConditionalResource conditionalResource1 = new ConfigXmlBuilder.ConditionalResource(Optional.of("path/to/resource1"), Optional.empty());
		conditionalResource1.addModule(new ConfigXmlBuilder.OpenMRSModule(Optional.of("mod123"), Optional.empty()));
		conditionalResource1.addModule(new ConfigXmlBuilder.OpenMRSModule(Optional.of("mod124"), Optional.of("1.1")));
		
		conditionalResources.add(conditionalResource1);
		
		Document configXml = withMinimalTags(version)
				.withConditionalResources(conditionalResources)
				.build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	private static Stream<Arguments> getCompatibleVersions() {
		return Arrays.stream(compatibleVersions).map(Arguments::of);
	}
}

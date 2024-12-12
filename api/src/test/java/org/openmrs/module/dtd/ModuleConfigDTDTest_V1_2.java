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
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openmrs.module.dtd.ConfigXmlBuilder.withMinimalTags;
import static org.openmrs.module.dtd.ConfigXmlBuilder.writeToInputStream;
import static org.openmrs.module.dtd.DtdTestValidator.isValidConfigXml;

public class ModuleConfigDTDTest_V1_2 {
	
	private static final String[] compatibleVersions = new String[] {"1.2", "1.3", "1.4", "1.5", "1.6", "1.7" };
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void filterWithAllValuesSet(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		ConfigXmlBuilder.Filter filter = new ConfigXmlBuilder.Filter(Optional.of("FilterName"), Optional.of("FilterClass"));
		filter.addInitParam(new ConfigXmlBuilder.InitParam(Optional.of("paramName1"), Optional.of("paramVal1")));
		filter.addInitParam(new ConfigXmlBuilder.InitParam(Optional.of("paramName2"), Optional.of("paramVal2")));
		filter.addInitParam(new ConfigXmlBuilder.InitParam(Optional.of("paramName3"), Optional.of("paramVal3")));
		
		Document configXml = withMinimalTags(version)
				.withFilter(filter)
				.build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void filterValidWithoutInitParams(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		ConfigXmlBuilder.Filter filter = new ConfigXmlBuilder.Filter(Optional.of("FilterName"), Optional.of("FilterClass"));
		filter.addInitParam(new ConfigXmlBuilder.InitParam(Optional.empty(), Optional.of("paramVal1")));
		filter.addInitParam(new ConfigXmlBuilder.InitParam(Optional.of("paramName2"), Optional.of("paramVal2")));
		filter.addInitParam(new ConfigXmlBuilder.InitParam(Optional.of("paramName3"), Optional.of("paramVal3")));
		
		Document configXml = withMinimalTags(version)
				.withFilter(filter)
				.build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void filterInvalidWhenMissingFilterName(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		ConfigXmlBuilder.Filter filter = new ConfigXmlBuilder.Filter(Optional.empty(), Optional.of("FilterClass"));
		filter.addInitParam(new ConfigXmlBuilder.InitParam(Optional.of("paramName1"), Optional.of("paramVal1")));
		filter.addInitParam(new ConfigXmlBuilder.InitParam(Optional.of("paramName2"), Optional.of("paramVal2")));
		filter.addInitParam(new ConfigXmlBuilder.InitParam(Optional.of("paramName3"), Optional.of("paramVal3")));
		
		Document configXml = withMinimalTags(version)
				.withFilter(filter)
				.build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void filterInvalidWhenMissingFilterClass(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		ConfigXmlBuilder.Filter filter = new ConfigXmlBuilder.Filter(Optional.of("FilterName"), Optional.empty());
		filter.addInitParam(new ConfigXmlBuilder.InitParam(Optional.of("paramName1"), Optional.of("paramVal1")));
		filter.addInitParam(new ConfigXmlBuilder.InitParam(Optional.of("paramName2"), Optional.of("paramVal2")));
		filter.addInitParam(new ConfigXmlBuilder.InitParam(Optional.of("paramName3"), Optional.of("paramVal3")));
		
		Document configXml = withMinimalTags(version)
				.withFilter(filter)
				.build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void filterInvalidWithInitParamNameMissing(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		ConfigXmlBuilder.Filter filter = new ConfigXmlBuilder.Filter(Optional.of("FilterName"), Optional.of("FilterClass"));
		
		Document configXml = withMinimalTags(version)
				.withFilter(filter)
				.build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void filterInvalidWithInitParamValueMissing(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		ConfigXmlBuilder.Filter filter = new ConfigXmlBuilder.Filter(Optional.of("FilterName"), Optional.of("FilterClass"));
		filter.addInitParam(new ConfigXmlBuilder.InitParam(Optional.of("paramName1"), Optional.empty()));
		filter.addInitParam(new ConfigXmlBuilder.InitParam(Optional.of("paramName2"), Optional.of("paramVal2")));
		filter.addInitParam(new ConfigXmlBuilder.InitParam(Optional.of("paramName3"), Optional.of("paramVal3")));
		
		Document configXml = withMinimalTags(version)
				.withFilter(filter)
				.build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void filterMappingWithUrlPattern(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		ConfigXmlBuilder.FilterMapping filterMapping = new ConfigXmlBuilder.FilterMapping(Optional.of("FilterName"), Optional.of("*.jsp"), Optional.empty());

		Document configXml = withMinimalTags(version)
				.withFilterMapping(filterMapping)
				.build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void filterMappingWithServletName(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		ConfigXmlBuilder.FilterMapping filterMapping = new ConfigXmlBuilder.FilterMapping(Optional.of("FilterName"), Optional.empty(), Optional.of("ServletName"));
		
		Document configXml = withMinimalTags(version)
				.withFilterMapping(filterMapping)
				.build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void filterMappingWithBothUrlPatternAndServletNameFails(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		ConfigXmlBuilder.FilterMapping filterMapping = new ConfigXmlBuilder.FilterMapping(Optional.of("FilterName"), Optional.of("*.jsp"), Optional.of("ServletName"));
		
		Document configXml = withMinimalTags(version)
				.withFilterMapping(filterMapping)
				.build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void filterMappingWithNeitherUrlPatternOrServletNameFails(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		ConfigXmlBuilder.FilterMapping filterMapping = new ConfigXmlBuilder.FilterMapping(Optional.of("FilterName"), Optional.empty(), Optional.empty());
		
		Document configXml = withMinimalTags(version)
				.withFilterMapping(filterMapping)
				.build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	private static Stream<Arguments> getCompatibleVersions() {
		return Arrays.stream(compatibleVersions).map(Arguments::of);
	}
}

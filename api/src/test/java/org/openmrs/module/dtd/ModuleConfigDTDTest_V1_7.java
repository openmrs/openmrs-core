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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openmrs.module.dtd.ConfigXmlBuilder.withMinimalTags;
import static org.openmrs.module.dtd.ConfigXmlBuilder.writeToInputStream;
import static org.openmrs.module.dtd.DtdTestValidator.isValidConfigXml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;

public class ModuleConfigDTDTest_V1_7 {
	
	private static final String[] compatibleVersions = new String[] { "1.7" };
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlServletWithInitParams(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Map<String, String> initParams = new HashMap<>();
		initParams.put("param1", "value1");
		initParams.put("param2", "value2");

		Document configXml = withMinimalTags(version)
			.withServlet(Optional.of("ServletName"), Optional.of("ServletClass"), initParams)
			.build();

		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}

	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void configXmlServletMissingInitParamsIsValid(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		Document configXml = withMinimalTags(version)
			.withServlet(Optional.of("ServletName"), Optional.of("ServletClass"), Collections.emptyMap())
			.build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	private static Stream<Arguments> getCompatibleVersions() {
		return Arrays.stream(compatibleVersions).map(Arguments::of);
	}
}

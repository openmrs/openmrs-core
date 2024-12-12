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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openmrs.module.dtd.ConfigXmlBuilder.withMinimalTags;
import static org.openmrs.module.dtd.ConfigXmlBuilder.writeToInputStream;
import static org.openmrs.module.dtd.DtdTestValidator.isValidConfigXml;

public class ModuleConfigDTDTest_V1_5 {
	
	private static final String[] compatibleVersions = new String[] {"1.5", "1.6", "1.7" };
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void validXmlWithMultipleAwareOfModules(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		List<ConfigXmlBuilder.AwareOfModule> awareOfModules = new ArrayList<>();
		awareOfModules.add(new ConfigXmlBuilder.AwareOfModule(Optional.of("mod1"), Optional.of("1.2.3")));
		awareOfModules.add(new ConfigXmlBuilder.AwareOfModule(Optional.of("mod2"), Optional.of("1.2.4")));
		awareOfModules.add(new ConfigXmlBuilder.AwareOfModule(Optional.of("mod3"), Optional.of("1.2.5")));
		
		Document configXml = withMinimalTags(version)
				.withAwareOfModules(awareOfModules)
				.withPackagesWithMappedClasses("org.openmrs.package1 org.openmrs.package2")
				.build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	public static String toString(Document doc) {
		try {
			StringWriter sw = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			
			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString();
		} catch (Exception ex) {
			throw new RuntimeException("Error converting to String", ex);
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void validXmlWithMissingVersion(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		List<ConfigXmlBuilder.AwareOfModule> awareOfModules = new ArrayList<>();
		awareOfModules.add(new ConfigXmlBuilder.AwareOfModule(Optional.of("mod1"), Optional.empty()));
		awareOfModules.add(new ConfigXmlBuilder.AwareOfModule(Optional.of("mod2"), Optional.of("1.2.4")));
		awareOfModules.add(new ConfigXmlBuilder.AwareOfModule(Optional.of("mod3"), Optional.of("1.2.5")));
		
		Document configXml = withMinimalTags(version)
				.withAwareOfModules(awareOfModules)
				.build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertTrue(isValidConfigXml(inputStream));
		}
	}
	
	@ParameterizedTest
	@MethodSource("getCompatibleVersions")
	public void xmlFailsWithNoModules(String version) throws ParserConfigurationException, TransformerException, IOException, URISyntaxException {
		List<ConfigXmlBuilder.AwareOfModule> awareOfModules = new ArrayList<>();
		
		Document configXml = withMinimalTags(version)
				.withAwareOfModules(awareOfModules)
				.build();
		
		try (InputStream inputStream = writeToInputStream(configXml)) {
			assertFalse(isValidConfigXml(inputStream));
		}
	}
	
	private static Stream<Arguments> getCompatibleVersions() {
		return Arrays.stream(compatibleVersions).map(Arguments::of);
	}
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.filter;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.Matchers.*;

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;
import java.io.StringReader;

import java.util.List;

import org.openmrs.module.Module;
import org.openmrs.module.ModuleException;
import org.openmrs.module.web.filter.ModuleFilterDefinition;

import org.junit.rules.ExpectedException;
import org.junit.Rule;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;

@PrepareForTest(ModuleFilterDefinition.class)
public class ModuleFilterDefinitionTest {
	@Rule
	public final ExpectedException expectedException = ExpectedException.none();
	
	public static Document getDocument(String xmlString) {
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new InputSource(new StringReader(xmlString)));
			return doc;
		} catch (Exception e) {
			return null;
		}
	}	

	/**
	 * @see ModulefilterDefinition#retrieveFilterDefinitions
	 * @throws ModuleException
	 */
	@Test
	public void retrieveFilterDefinitions_shouldThrowModuleExceptionIfNoConfig() {
		expectedException.expect(ModuleException.class);
		expectedException.expectMessage("Unable to parse filters in module configuration.");
		Module module = new Module("test");
		ModuleFilterDefinition.retrieveFilterDefinitions(module);
	}

	/**
	 * @see ModulefilterDefinition#retrieveFilterDefinitions
	 * @throws ModuleException
	 */
	@Test
	public void retrieveFilterDefinitions_shouldReturnEmptyListIfNoFilterNodes() {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						 + "<data></data>";
		Module module = new Module("test");
		module.setConfig(getDocument(xmlString));

		List<ModuleFilterDefinition> out = ModuleFilterDefinition.retrieveFilterDefinitions(module);
		assertTrue(out.isEmpty());
	}
	
	/**
	 * @see ModulefilterDefinition#retrieveFilterDefinitions
	 * @throws ModuleException
	 */
	@Test
	public void retrieveFilterDefinitions_shouldReturnListOfSizeOneUsingInitParams() {
		String xmlString = "<?xml version =\"1.0\" encoding=\"UTF-8\"?>\n"
						 + "<data><filter>\n"
						 + "	<init-param>\n"
						 + "		<param-name>test</param-name>\n"
						 + "		<param-value>123</param-value>\n"
						 + "	</init-param>\n"
						 + "</filter></data>";
		Module module = new Module("test");
		module.setConfig(getDocument(xmlString));
		
		List<ModuleFilterDefinition> out = ModuleFilterDefinition.retrieveFilterDefinitions(module);
		assertThat(out.size(), is(1));
		assertThat(out.get(0).getInitParameters().get("test"), is("123"));
	}
	
	/**
	 * @see ModulefilterDefinition#retrieveFilterDefinitions
	 * @throws ModuleException
	 */
	@Test
	public void retrieveFilterDefinitions_shouldReturnListOfSizeOneUsingFilterNameAndClass() {
		String xmlString = "<?xml version =\"1.0\" encoding=\"UTF-8\"?>\n"
						 + "<data><filter>\n"
						 + "	<filter-name>test</filter-name>\n"
						 + "	<filter-class>123</filter-class>\n"
						 + "</filter></data>";
		Module module = new Module("test");
		module.setConfig(getDocument(xmlString));
		
		List<ModuleFilterDefinition> out = ModuleFilterDefinition.retrieveFilterDefinitions(module);
		assertThat(out.size(), is(1));
		assertThat(out.get(0).getFilterName(), is("test"));
		assertThat(out.get(0).getFilterClass(), is("123"));
	}
}
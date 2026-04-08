/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.layout.address;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AddressSupportTest extends BaseContextSensitiveTest {

	private static final String GP_ADDRESS_TEMPLATE = OpenmrsConstants.GLOBAL_PROPERTY_ADDRESS_TEMPLATE;

	private static final String REQUIRED_ADDRESS_TEMPLATE_XML = "<org.openmrs.layout.address.AddressTemplate>\n"
	        + "    <nameMappings class=\"properties\">\n"
	        + "      <property name=\"address1\" value=\"Location.address1\"/>\n" + "    </nameMappings>\n"
	        + "    <sizeMappings class=\"properties\">\n" + "      <property name=\"address1\" value=\"40\"/>\n"
	        + "    </sizeMappings>\n" + "    <lineByLineFormat>\n" + "      <string>address1</string>\n"
	        + "    </lineByLineFormat>\n" + "    <requiredElements>\n" + "      <string>address1</string>\n"
	        + "    </requiredElements>\n" + "</org.openmrs.layout.address.AddressTemplate>";

	/**
	 * Regression test for ticket: GP screen can persist escaped xml; AddressSupport should still
	 * deserialize.
	 */
	@Test
	public void getAddressTemplate_shouldDeserializeXmlEscapedTemplateSavedViaGlobalProperty() {
		AddressSupport addressSupport = AddressSupport.getInstance();
		Context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(GP_ADDRESS_TEMPLATE, escapeXml(REQUIRED_ADDRESS_TEMPLATE_XML)));

		List<AddressTemplate> templates = addressSupport.getAddressTemplate();
		assertNotNull(templates);
		assertFalse(templates.isEmpty());
		AddressTemplate template = templates.get(0);
		assertNotNull(template.getRequiredElements());
		assertFalse(template.getRequiredElements().isEmpty());
	}

	@Test
	public void getAddressTemplate_shouldDeserializeUnescapedXmlTemplate() {
		AddressSupport addressSupport = AddressSupport.getInstance();
		Context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(GP_ADDRESS_TEMPLATE, REQUIRED_ADDRESS_TEMPLATE_XML));

		List<AddressTemplate> templates = addressSupport.getAddressTemplate();
		assertNotNull(templates);
		assertFalse(templates.isEmpty());
		AddressTemplate template = templates.get(0);
		assertNotNull(template.getRequiredElements());
		assertFalse(template.getRequiredElements().isEmpty());
	}

	@Test
	public void getAddressTemplate_shouldHandleInvalidPropertyNames() {
		final String xml = "<org.openmrs.layout.address.AddressTemplate>\n" + "    <nameMappings class=\"properties\">\n"
		        + "      <property name=\"invalidProperty\" value=\"Location.address1\"/>\n" + "    </nameMappings>\n"
		        + "    <sizeMappings class=\"properties\">\n" + "      <property name=\"address1\" value=\"40\"/>\n"
		        + "    </sizeMappings>\n" + "    <lineByLineFormat>\n" + "      <string>address1</string>\n"
		        + "    </lineByLineFormat>\n" + "    <requiredElements>\n" + "      <string>address1</string>\n"
		        + "    </requiredElements>\n" + "</org.openmrs.layout.address.AddressTemplate>";
		AddressSupport addressSupport = AddressSupport.getInstance();
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(GP_ADDRESS_TEMPLATE, xml));

		List<AddressTemplate> templates = addressSupport.getAddressTemplate();
		assertNotNull(templates);
		assertFalse(templates.isEmpty());
		AddressTemplate template = templates.get(0);
		assertNotNull(template.getRequiredElements());
		assertFalse(template.getRequiredElements().isEmpty());
	}

	@Test
	public void getAddressTemplate_shouldHandleUnknownXmlSections() {
		final String xml = "<org.openmrs.layout.address.AddressTemplate>\n" + "    <nameMappings class=\"properties\">\n"
		        + "      <property name=\"address1\" value=\"Location.address1\"/>\n" + "    </nameMappings>\n"
		        + "    <sizeMappings class=\"properties\">\n" + "      <property name=\"address1\" value=\"40\"/>\n"
		        + "    </sizeMappings>\n" + "    <lineByLineFormat>\n" + "      <string>address1</string>\n"
		        + "    </lineByLineFormat>\n" + "    <requiredElements>\n" + "      <string>address1</string>\n"
		        + "    </requiredElements>\n" + "    <unknownSection>\n" + "      <string>unknown</string>\n"
		        + "    </unknownSection>\n" + "</org.openmrs.layout.address.AddressTemplate>";
		AddressSupport addressSupport = AddressSupport.getInstance();
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(GP_ADDRESS_TEMPLATE, xml));

		List<AddressTemplate> templates = addressSupport.getAddressTemplate();
		assertNotNull(templates);
		assertFalse(templates.isEmpty());
	}

	@Test
	public void getAddressTemplate_shouldHandleMissingLineByLineFormat() {
		final String xml = "<org.openmrs.layout.address.AddressTemplate>\n" + "    <nameMappings class=\"properties\">\n"
		        + "      <property name=\"address1\" value=\"Location.address1\"/>\n" + "    </nameMappings>\n"
		        + "    <sizeMappings class=\"properties\">\n" + "      <property name=\"address1\" value=\"40\"/>\n"
		        + "    </sizeMappings>\n" + "    <requiredElements>\n" + "      <string>address1</string>\n"
		        + "    </requiredElements>\n" + "</org.openmrs.layout.address.AddressTemplate>";
		AddressSupport addressSupport = AddressSupport.getInstance();
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(GP_ADDRESS_TEMPLATE, xml));

		List<AddressTemplate> templates = addressSupport.getAddressTemplate();
		assertNotNull(templates);
		assertFalse(templates.isEmpty());
		AddressTemplate template = templates.get(0);
		assertNotNull(template.getRequiredElements());
		assertFalse(template.getRequiredElements().isEmpty());
	}

	@AfterEach
	public void resetAddressTemplateAfterEachTest() {
		Context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(GP_ADDRESS_TEMPLATE, OpenmrsConstants.DEFAULT_ADDRESS_TEMPLATE));
		Context.getAdministrationService()
		        .saveGlobalProperty(Context.getAdministrationService().getGlobalPropertyObject(GP_ADDRESS_TEMPLATE));
	}

	private String escapeXml(String xml) {
		return xml.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'",
		    "&apos;");
	}
}

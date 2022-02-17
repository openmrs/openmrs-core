/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Behavior-driven unit tests for {@link ConfigUtil} class
 */
public class ConfigUtilTest extends BaseContextSensitiveTest {

	@Autowired 
	@Qualifier("adminService")
	AdministrationService administrationService;
	
	static {
		System.setProperty("inRuntimeAndSystem", "system-property-value");
		System.setProperty("inSystemNotRuntime", "system-property-value");
		System.setProperty("emptyPropertyValue", "");
	}

	@Override
	public Properties getRuntimeProperties() {
		Properties p = super.getRuntimeProperties();
		p.put("inRuntimeAndSystem", "runtime-property-value");
		p.put("inRuntimeNotSystem", "runtime-property-value");
		p.put("blankPropertyValue", " ");
		return p;
	}

	@BeforeEach
	public void setupProperties() {
		administrationService.setGlobalProperty("mail.user", "user1@test.com");
		administrationService.setGlobalProperty("mail.password", "Test123");
		administrationService.setGlobalProperty("inRuntimeAndSystem", "global-property-value");
		administrationService.setGlobalProperty("inRuntimeNotSystem", "global-property-value");
		administrationService.setGlobalProperty("inSystemNotRuntime", "global-property-value");
		administrationService.setGlobalProperty("inGlobalOnly", "global-property-value");
	}

	@Test
	public void shouldGetGlobalProperty() {
		assertThat(ConfigUtil.getGlobalProperty("mail.user"), is("user1@test.com"));
		assertThat(ConfigUtil.getGlobalProperty("mail.password"), is("Test123"));
		assertThat(ConfigUtil.getGlobalProperty("mail.undefined"), nullValue());
		assertThat(ConfigUtil.getGlobalProperty("inRuntimeAndSystem"), is("global-property-value"));
		assertThat(ConfigUtil.getGlobalProperty("inRuntimeNotSystem"), is("global-property-value"));
	}

	@Test
	public void shouldGetUpdatedGlobalPropertyIfChanged() {
		assertThat(ConfigUtil.getGlobalProperty("mail.user"), is("user1@test.com"));
		administrationService.setGlobalProperty("mail.user", "user2@test.org");
		assertThat(ConfigUtil.getGlobalProperty("mail.user"), is("user2@test.org"));
	}

	@Test
	public void shouldGetUpdatedGlobalPropertyIfDeleted() {
		assertThat(ConfigUtil.getGlobalProperty("mail.user"), is("user1@test.com"));
		GlobalProperty p = administrationService.getGlobalPropertyObject("mail.user");
		administrationService.purgeGlobalProperty(p);
		assertThat(ConfigUtil.getGlobalProperty("mail.user"), nullValue());
	}

	@Test
	public void shouldDetermineIfRuntimePropertyIsDefined() {
		assertFalse(ConfigUtil.hasRuntimeProperty("undefined.property"));
		int numMatches = 0;
		for (String property : Context.getRuntimeProperties().stringPropertyNames()) {
			assertTrue(ConfigUtil.hasRuntimeProperty(property));
			numMatches++;
		}
		assertTrue(numMatches > 0);
	}

	@Test
	public void shouldGetRuntimeProperty() {
		assertThat(ConfigUtil.getRuntimeProperty("undefined.property"), nullValue());
		int numMatches = 0;
		Properties properties = Context.getRuntimeProperties();
		for (String property : properties.stringPropertyNames()) {
			assertThat(ConfigUtil.getRuntimeProperty(property), is(properties.getProperty(property)));
			numMatches++;
		}
		assertTrue(numMatches > 0);
		assertThat(ConfigUtil.getRuntimeProperty("inRuntimeAndSystem"), is("runtime-property-value"));
		assertThat(ConfigUtil.getRuntimeProperty("inRuntimeNotSystem"), is("runtime-property-value"));
	}

	@Test
	public void shouldDetermineIfSystemPropertyIsDefined() {
		assertFalse(ConfigUtil.hasSystemProperty("undefined.property"));
		int numMatches = 0;
		for (String property : System.getProperties().stringPropertyNames()) {
			assertTrue(ConfigUtil.hasSystemProperty(property));
			numMatches++;
		}
		assertTrue(numMatches > 0);
	}

	@Test
	public void shouldGetSystemProperty() {
		assertThat(ConfigUtil.getSystemProperty("undefined.property"), nullValue());
		int numMatches = 0;
		Properties properties = System.getProperties();
		for (String property : properties.stringPropertyNames()) {
			assertThat(ConfigUtil.getSystemProperty(property), is(properties.getProperty(property)));
			numMatches++;
		}
		assertTrue(numMatches > 0);
		assertThat(ConfigUtil.getSystemProperty("inRuntimeAndSystem"), is("system-property-value"));
	}

	@Test
	public void shouldReturnSystemPropertyOverRuntimeProperty() {
		assertThat(ConfigUtil.getProperty("inRuntimeAndSystem"), is("system-property-value"));
		assertThat(ConfigUtil.getProperty("inRuntimeNotSystem"), is("runtime-property-value"));
	}

	@Test
	public void shouldReturnSystemPropertyOverGlobalProperty() {
		assertThat(ConfigUtil.getProperty("inSystemNotRuntime"), is("system-property-value"));
		assertThat(ConfigUtil.getProperty("inGlobalOnly"), is("global-property-value"));
	}

	@Test
	public void shouldReturnRuntimePropertyOverGlobalProperty() {
		assertThat(ConfigUtil.getProperty("inRuntimeNotSystem"), is("runtime-property-value"));
		assertThat(ConfigUtil.getProperty("inGlobalOnly"), is("global-property-value"));
	}

	@Test
	public void shouldReturnDefaultValueIfValueIsBlank() {
		assertThat(ConfigUtil.getProperty("undefined.property"), nullValue());
		assertThat(ConfigUtil.getProperty("undefined.property", "default"), is("default"));
		assertThat(ConfigUtil.getProperty("emptyPropertyValue"), is(""));
		assertThat(ConfigUtil.getProperty("emptyPropertyValue", "default"), is("default"));
		assertThat(ConfigUtil.getProperty("blankPropertyValue"), is(" "));
		assertThat(ConfigUtil.getProperty("blankPropertyValue", "default"), is("default"));
	}
}

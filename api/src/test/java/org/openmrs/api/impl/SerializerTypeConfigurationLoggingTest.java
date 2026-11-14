/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.logging.MemoryAppender;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.test.serialization.ProbeTarget;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * Checks how often the ignored serializer type configuration is reported: a value stored by other
 * means is reported at WARN the first time the list is built and afterwards only at DEBUG, so an
 * operation does not see the same lines on every expiry of the cached list.
 */
public class SerializerTypeConfigurationLoggingTest extends BaseContextSensitiveTest {

	private AdministrationDAO administrationDao;

	private MemoryAppender memoryAppender;

	private Logger implLogger;

	private Level originalLevel;

	private boolean originalAdditive;

	@BeforeEach
	public void attachMemoryAppender() {
		AdministrationServiceImpl.resetSerializerTypeConfigurationProblemReporting();
		administrationDao = Context.getRegisteredComponent("adminDAO", AdministrationDAO.class);
		// No layout is set, so every supported line falls back to the default OpenMRS pattern.
		memoryAppender = MemoryAppender.newBuilder().setName("SerializerTypeConfigurationLoggingTest-" + System.nanoTime())
		        .build();
		memoryAppender.start();
		implLogger = (Logger) LogManager.getLogger(AdministrationServiceImpl.class);
		originalLevel = implLogger.getLevel();
		originalAdditive = implLogger.isAdditive();
		implLogger.setAdditive(false);
		implLogger.setLevel(Level.WARN);
		implLogger.addAppender(memoryAppender);

		Module testModule = new Module("Test Module", "testmodule", "org.openmrs.module.testmodule", "OpenMRS",
		        "module used by tests", "1.0", "1");
		ModuleFactory.loadModule(testModule, true);
	}

	@AfterEach
	public void detachMemoryAppender() {
		implLogger.removeAppender(memoryAppender);
		implLogger.setLevel(originalLevel);
		implLogger.setAdditive(originalAdditive);
		memoryAppender.stop();
		ModuleFactory.getLoadedModulesMap().remove("testmodule");
		AdministrationServiceImpl.resetSerializerTypeConfigurationProblemReporting();
	}

	private List<String> warnLines() {
		return memoryAppender.getLogLines().stream().filter(line -> !line.startsWith("Ignoring entry"))
		        .collect(Collectors.toList());
	}

	private List<String> warnLinesAbout(String text) {
		return memoryAppender.getLogLines().stream().filter(line -> line.contains(text)).collect(Collectors.toList());
	}

	private AdministrationServiceImpl rawService() {
		AdministrationServiceImpl impl = new AdministrationServiceImpl();
		impl.setAdministrationDAO(administrationDao);
		return impl;
	}

	@Test
	public void shouldReportAStoredProblemOnceAtWarnAcrossRebuilds() {
		administrationDao.saveGlobalProperty(new GlobalProperty("x.serializer.whitelist.types", "**"));

		for (int i = 0; i < 3; i++) {
			rawService().getSerializerWhitelistTypes();
		}

		assertThat(warnLinesAbout("x.serializer.whitelist.types"), hasSize(1));
	}

	@Test
	public void shouldReportEachDistinctProblemOnce() {
		administrationDao.saveGlobalProperty(new GlobalProperty("x.serializer.whitelist.types", "**"));
		administrationDao.saveGlobalProperty(new GlobalProperty("y.serializer.whitelist.types", "**"));

		rawService().getSerializerWhitelistTypes();
		rawService().getSerializerWhitelistTypes();

		assertThat(warnLinesAbout("x.serializer.whitelist.types"), hasSize(1));
		assertThat(warnLinesAbout("y.serializer.whitelist.types"), hasSize(1));
	}

	@Test
	public void shouldReportRejectedEntriesOnceAndRememberedByPropertyAndEntry() {
		administrationDao.saveGlobalProperty(new GlobalProperty("testmodule.serializer.whitelist.types", "**"));

		rawService().getSerializerWhitelistTypes();
		rawService().getSerializerWhitelistTypes();

		List<String> entryLines = memoryAppender.getLogLines().stream().filter(line -> line.contains("Ignoring entry"))
		        .collect(Collectors.toList());
		assertThat(entryLines, hasSize(1));
	}

	@Test
	public void shouldNotWarnForAcceptedConfiguration() {
		administrationDao.saveGlobalProperty(
		    new GlobalProperty("testmodule.serializer.whitelist.types", "org.openmrs.module.testmodule.SomeClass"));

		rawService().getSerializerWhitelistTypes();
		rawService().getSerializerWhitelistTypes();

		List<String> warns = memoryAppender.getLogLines().stream().filter(line -> line.contains("Ignoring"))
		        .collect(Collectors.toList());
		assertThat(warns.size(), is(0));
	}

	@Test
	public void shouldUseTheSanitizedEntryInReportedLines() {
		administrationDao.saveGlobalProperty(new GlobalProperty("x.serializer.whitelist.types", "**"));

		rawService().getSerializerWhitelistTypes();

		List<String> lines = memoryAppender.getLogLines().stream()
		        .filter(line -> line.contains("x.serializer.whitelist.types")).collect(Collectors.toList());
		assertThat(lines, hasSize(1));
		assertThat(lines.get(0).contains(ProbeTarget.class.getSimpleName()), is(false));
	}
}

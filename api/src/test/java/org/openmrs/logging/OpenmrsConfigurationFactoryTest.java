/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.openmrs.util.OpenmrsUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.mockito.Mockito.mockStatic;

/**
 * Tests for {@link OpenmrsConfigurationFactory}.
 * <p/>
 * These tests focus on configuration file discovery and the static customisation logic. Full
 * integration with Log4J2 context lifecycle is covered by the XML/YAML/JSON config subclasses
 * implicitly through the build's plugin processor.
 */
class OpenmrsConfigurationFactoryTest {

	@TempDir
	Path tempDir;

	private MockedStatic<OpenmrsUtil> openmrsUtilMock;

	@BeforeEach
	void setUp() {
		openmrsUtilMock = mockStatic(OpenmrsUtil.class);
	}

	@AfterEach
	void tearDown() {
		openmrsUtilMock.close();
	}

	@Test
	void getConfigurationFiles_shouldReturnEmptyListWhenNoConfigFilesExist() {
		openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectoryAsFile).thenReturn(tempDir.toFile());
		openmrsUtilMock.when(() -> OpenmrsUtil.getDirectoryInApplicationDataDirectory("configuration"))
		        .thenReturn(tempDir.resolve("configuration").toFile());

		OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();

		assertThat(factory.getConfigurationFiles(), empty());
	}

	@Test
	void getConfigurationFiles_shouldFindXmlConfigFile() throws IOException {
		Path configDir = tempDir.resolve("configuration");
		Files.createDirectories(configDir);
		Files.writeString(configDir.resolve("log4j2.xml"), "<Configuration/>");

		openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectoryAsFile).thenReturn(tempDir.toFile());
		openmrsUtilMock.when(() -> OpenmrsUtil.getDirectoryInApplicationDataDirectory("configuration"))
		        .thenReturn(configDir.toFile());

		OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();

		assertThat(factory.getConfigurationFiles(), hasSize(1));
	}

	@Test
	void getConfigurationFiles_shouldFindYamlAndJsonConfigFiles() throws IOException {
		Path configDir = tempDir.resolve("configuration");
		Files.createDirectories(configDir);
		Files.writeString(configDir.resolve("log4j2.yaml"), "Configuration: {}");
		Files.writeString(configDir.resolve("log4j2.json"), "{}");

		openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectoryAsFile).thenReturn(tempDir.toFile());
		openmrsUtilMock.when(() -> OpenmrsUtil.getDirectoryInApplicationDataDirectory("configuration"))
		        .thenReturn(configDir.toFile());

		OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();

		assertThat(factory.getConfigurationFiles(), hasSize(2));
	}

	@Test
	void getConfigurationFiles_shouldIgnoreNonConfigFiles() throws IOException {
		Path configDir = tempDir.resolve("configuration");
		Files.createDirectories(configDir);
		Files.writeString(configDir.resolve("log4j2.xml"), "<Configuration/>");
		Files.writeString(configDir.resolve("other-config.properties"), "key=value");
		Files.writeString(configDir.resolve("readme.txt"), "not a config");

		openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectoryAsFile).thenReturn(tempDir.toFile());
		openmrsUtilMock.when(() -> OpenmrsUtil.getDirectoryInApplicationDataDirectory("configuration"))
		        .thenReturn(configDir.toFile());

		OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();

		assertThat(factory.getConfigurationFiles(), hasSize(1));
	}

	@Test
	void getConfigurationFiles_shouldFindConfigInApplicationDataDir() throws IOException {
		Files.writeString(tempDir.resolve("log4j2.xml"), "<Configuration/>");

		openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectoryAsFile).thenReturn(tempDir.toFile());
		openmrsUtilMock.when(() -> OpenmrsUtil.getDirectoryInApplicationDataDirectory("configuration"))
		        .thenReturn(tempDir.resolve("nonexistent").toFile());

		OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();

		assertThat(factory.getConfigurationFiles(), hasSize(1));
	}

	@Test
	void getConfigurationFiles_shouldSortFilesByName() throws IOException {
		Path configDir = tempDir.resolve("configuration");
		Files.createDirectories(configDir);
		Files.writeString(configDir.resolve("log4j2-b.xml"), "<Configuration/>");
		Files.writeString(configDir.resolve("log4j2-a.xml"), "<Configuration/>");

		openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectoryAsFile).thenReturn(tempDir.toFile());
		openmrsUtilMock.when(() -> OpenmrsUtil.getDirectoryInApplicationDataDirectory("configuration"))
		        .thenReturn(configDir.toFile());

		OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();

		assertThat("Should find both files, sorted by name", factory.getConfigurationFiles(), hasSize(2));
		assertThat("First file should be log4j2-a.xml",
		    factory.getConfigurationFiles().get(0).getName().endsWith("log4j2-a.xml"));
	}

	@Test
	void getConfigurationFiles_shouldIgnoreUnreadableFiles() throws IOException {
		Path configDir = tempDir.resolve("configuration");
		Files.createDirectories(configDir);
		Path unreadable = configDir.resolve("log4j2.xml");
		Files.writeString(unreadable, "<Configuration/>");
		Assumptions.assumeTrue(unreadable.toFile().setReadable(false),
		    "Cannot make file unreadable on this platform; skipping test");

		try {
			openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectoryAsFile).thenReturn(tempDir.toFile());
			openmrsUtilMock.when(() -> OpenmrsUtil.getDirectoryInApplicationDataDirectory("configuration"))
			        .thenReturn(configDir.toFile());

			OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();
			assertThat(factory.getConfigurationFiles(), empty());
		} finally {
			unreadable.toFile().setReadable(true);
		}
	}

	@Test
	void extensions_shouldMatchSupportedSuffixesMinusWildcard() {
		String[] suffixes = OpenmrsConfigurationFactory.SUFFIXES;
		String[] extensions = OpenmrsConfigurationFactory.EXTENSIONS;

		assertThat("Extensions should be one fewer than suffixes (minus wildcard '*')", extensions.length,
		    org.hamcrest.Matchers.equalTo(suffixes.length - 1));

		for (String ext : extensions) {
			assertThat("Extension '" + ext + "' should not contain a dot", ext.indexOf('.') < 0);
		}
	}
}

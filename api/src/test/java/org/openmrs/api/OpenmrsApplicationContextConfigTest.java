/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Unit tests for {@link OpenmrsApplicationContextConfig}, in particular for how the runtime
 * properties file is resolved for the placeholder configurer.
 */
public class OpenmrsApplicationContextConfigTest {

	private String savedApplicationDataDirectory;

	private Path applicationDataDirectory;

	@BeforeEach
	void setUp() throws IOException {
		savedApplicationDataDirectory = System.getProperty(OpenmrsConstants.KEY_OPENMRS_APPLICATION_DATA_DIRECTORY);
		applicationDataDirectory = Files.createTempDirectory("openmrs-application-context-config-test");
		OpenmrsUtil.setApplicationDataDirectory(applicationDataDirectory.toString());
		OpenmrsUtil.setLoadedRuntimePropertiesFilePathName(null);
	}

	@AfterEach
	void tearDown() throws IOException {
		OpenmrsUtil.setApplicationName(null);
		OpenmrsUtil.setLoadedRuntimePropertiesFilePathName(null);
		OpenmrsUtil.setApplicationDataDirectory(savedApplicationDataDirectory);
		if (applicationDataDirectory != null && Files.exists(applicationDataDirectory)) {
			try (Stream<Path> paths = Files.walk(applicationDataDirectory)) {
				paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			}
		}
	}

	@Test
	void getApplicationName_shouldDefaultToOpenmrs() {
		OpenmrsUtil.setApplicationName(null);

		assertEquals("openmrs", OpenmrsUtil.getApplicationName());
	}

	@Test
	void getApplicationName_shouldReturnTheNameThatWasSet() {
		OpenmrsUtil.setApplicationName("myapp");

		assertEquals("myapp", OpenmrsUtil.getApplicationName());
	}

	@Test
	void runtimePropertiesResource_shouldFindTheFileForANonDefaultApplicationNameInTheApplicationDataDirectory()
	        throws IOException {
		OpenmrsUtil.setApplicationName("myapp");
		Path runtimeProperties = applicationDataDirectory.resolve("myapp-runtime.properties");
		Files.writeString(runtimeProperties, "cache.config=ehcache");

		Resource resource = OpenmrsApplicationContextConfig
		        .runtimePropertiesResource(applicationDataDirectory.toString());

		assertEquals(runtimeProperties.toFile(), resource.getFile());
	}

	@Test
	void runtimePropertiesResource_shouldFindTheFileForTheDefaultApplicationNameInTheApplicationDataDirectory()
	        throws IOException {
		assumeFalse(Files.exists(Path.of("openmrs-runtime.properties")),
		    "openmrs-runtime.properties exists in the working directory");
		OpenmrsUtil.setApplicationName(null);
		Path runtimeProperties = applicationDataDirectory.resolve("openmrs-runtime.properties");
		Files.writeString(runtimeProperties, "cache.config=ehcache");

		Resource resource = OpenmrsApplicationContextConfig
		        .runtimePropertiesResource(applicationDataDirectory.toString());

		assertEquals(runtimeProperties.toFile(), resource.getFile());
	}

	@Test
	void runtimePropertiesResource_shouldFindTheFileInTheWorkingDirectory() throws IOException {
		Path runtimeProperties = Path.of("trunk6841test-runtime.properties");
		assumeFalse(Files.exists(runtimeProperties), "trunk6841test-runtime.properties exists in the working directory");
		OpenmrsUtil.setApplicationName("trunk6841test");
		try {
			Files.writeString(runtimeProperties, "cache.config=ehcache");

			Resource resource = OpenmrsApplicationContextConfig
			        .runtimePropertiesResource(applicationDataDirectory.toString());

			assertEquals(runtimeProperties.toFile().getAbsoluteFile(), resource.getFile().getAbsoluteFile());
		} finally {
			Files.deleteIfExists(runtimeProperties);
		}
	}

	@Test
	void runtimePropertiesResource_shouldFallBackToTheApplicationDataDirectoryIfNoFileExists() throws IOException {
		OpenmrsUtil.setApplicationName("trunk6841missing");

		Resource resource = OpenmrsApplicationContextConfig
		        .runtimePropertiesResource(applicationDataDirectory.toString());

		assertEquals(applicationDataDirectory.resolve("trunk6841missing-runtime.properties").toFile(),
		    resource.getFile());
		assertFalse(resource.exists());
	}

	@Test
	void runtimePropertiesResource_shouldUseTheRuntimePropertiesFileThatWasLoadedAtStartup() throws IOException {
		OpenmrsUtil.setApplicationName("myapp");
		Path loadedDirectory = Files.createTempDirectory("openmrs-loaded-runtime-properties-test");
		try {
			Path loadedRuntimeProperties = loadedDirectory.resolve("myapp-runtime.properties");
			Files.writeString(loadedRuntimeProperties, "cache.config=ehcache");
			OpenmrsUtil.setLoadedRuntimePropertiesFilePathName(loadedRuntimeProperties.toString());

			Resource resource = OpenmrsApplicationContextConfig
			        .runtimePropertiesResource(applicationDataDirectory.toString());

			assertEquals(loadedRuntimeProperties.toFile(), resource.getFile());
		} finally {
			try (Stream<Path> paths = Files.walk(loadedDirectory)) {
				paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			}
		}
	}

	@Test
	void propertySourcesPlaceholderConfigurer_shouldResolvePlaceholdersFromTheRuntimePropertiesFile()
	        throws IOException {
		OpenmrsUtil.setApplicationName("myapp");
		Files.writeString(applicationDataDirectory.resolve("myapp-runtime.properties"),
		    "trunk6841.test.value=value from the runtime properties file");

		PropertySourcesPlaceholderConfigurer configurer = new OpenmrsApplicationContextConfig()
		        .propertySourcesPlaceholderConfigurer();
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		RootBeanDefinition beanDefinition = new RootBeanDefinition(PlaceholderBean.class);
		MutablePropertyValues propertyValues = new MutablePropertyValues();
		propertyValues.add("value", "${trunk6841.test.value}");
		beanDefinition.setPropertyValues(propertyValues);
		beanFactory.registerBeanDefinition("placeholderBean", beanDefinition);
		configurer.postProcessBeanFactory(beanFactory);

		assertEquals("value from the runtime properties file", beanFactory.getBean(PlaceholderBean.class).getValue());
	}

	public static class PlaceholderBean {

		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
}

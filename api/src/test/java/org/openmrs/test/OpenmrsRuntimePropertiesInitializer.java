/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.test;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Initializes the OpenMRS runtime environment for testing purposes.
 * <p>
 * This {@link org.springframework.context.ApplicationContextInitializer} implementation
 * sets the {@code OPENMRS_APPLICATION_DATA_DIRECTORY} system property to a temporary directory
 * and creates a minimal {@code openmrs-runtime.properties} file within it.
 * This setup ensures that the Spring application context can be initialized without
 * encountering a {@link java.io.FileNotFoundException} for the runtime properties file.
 * </p>
 */
public class OpenmrsRuntimePropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
	
	@Override
	public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
		try {
			Path tempDir = Files.createTempDirectory("openmrs-test-appdir");
			System.setProperty("OPENMRS_APPLICATION_DATA_DIRECTORY", tempDir.toString());
			Path runtimeProps = tempDir.resolve("openmrs-runtime.properties");
			String content = String.join("\n",
				"connection.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
				"connection.username=sa",
				"connection.password=",
				"hibernate.dialect=org.hibernate.dialect.H2Dialect"
			);
			Files.write(runtimeProps, content.getBytes());
		} catch (IOException e) {
			throw new RuntimeException("Failed to initialize OPENMRS_APPLICATION_DATA_DIRECTORY", e);
		}
	}
}

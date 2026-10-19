/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.context;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests {@link Context}'s static initialization in a class loader of its own, where nothing has
 * started log4j yet, so creating Context's logger is what configures logging.
 */
public class ContextStaticInitializationTest {

	@Test
	public void shouldInitializeWhenCreatingItsLoggerStartsLog4j() throws Exception {
		try (URLClassLoader loader = new URLClassLoader(getClasspath(), ClassLoader.getPlatformClassLoader())) {
			// OpenmrsConfigurationFactory calls Context.isSessionOpen() while it configures log4j, which here
			// happens while Context's static fields are still being initialized
			Class<?> context = Class.forName(Context.class.getName(), true, loader);

			assertFalse((Boolean) context.getMethod("isSessionOpen").invoke(null));
		}
	}

	private static URL[] getClasspath() throws MalformedURLException {
		List<URL> urls = new ArrayList<>();
		for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
			urls.add(new File(entry).toURI().toURL());
		}
		return urls.toArray(new URL[0]);
	}
}

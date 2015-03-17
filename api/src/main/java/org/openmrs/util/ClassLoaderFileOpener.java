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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import liquibase.resource.ResourceAccessor;

/**
 * Implementation of liquibase FileOpener interface so that the {@link OpenmrsClassLoader} will be
 * used to find files (or any other classloader that is passed into the contructor). This allows
 * liquibase xml files in modules to be found.
 */
public class ClassLoaderFileOpener implements ResourceAccessor {
	
	/**
	 * The classloader to read from
	 */
	private ClassLoader cl;
	
	/**
	 * @param cl the {@link ClassLoader} to use for finding files.
	 */
	public ClassLoaderFileOpener(ClassLoader cl) {
		this.cl = cl;
	}
	
	@Override
	public InputStream getResourceAsStream(String file) throws IOException {
		return cl.getResourceAsStream(file);
	}
	
	@Override
	public Enumeration<URL> getResources(String packageName) throws IOException {
		return cl.getResources(packageName);
	}
	
	@Override
	public ClassLoader toClassLoader() {
		return cl;
	}
}

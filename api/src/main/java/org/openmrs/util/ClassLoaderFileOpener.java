/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import liquibase.FileOpener;

/**
 * Implementation of liquibase FileOpener interface so that the {@link OpenmrsClassLoader} will be
 * used to find files (or any other classloader that is passed into the contructor). This allows
 * liquibase xml files in modules to be found.
 */
public class ClassLoaderFileOpener implements FileOpener {
	
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

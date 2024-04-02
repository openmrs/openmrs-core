/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.liquibase;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.Resource;
import org.openmrs.util.OpenmrsClassLoader;

/**
 * A customization of Liquibase's {@link ClassLoaderResourceAccessor} which defaults to the OpenMRS
 * ClassLoader and has special handling for our liquibase.xml files, which occur multiple times on
 * the classpath.
 */
public class OpenmrsClassLoaderResourceAccessor extends ClassLoaderResourceAccessor {
	
	public OpenmrsClassLoaderResourceAccessor() {
		super(OpenmrsClassLoader.getInstance());
	}
	
	public OpenmrsClassLoaderResourceAccessor(ClassLoader classLoader) {
		super(classLoader);
	}
	
	@Override
	public List<Resource> getAll(String path) throws IOException {
		List<Resource> resources = super.getAll(path);
		
		if (resources == null || resources.isEmpty()) {
			return Collections.emptyList();
		}
		
		return resources.stream().limit(1).collect(Collectors.toList());
	}
	
}

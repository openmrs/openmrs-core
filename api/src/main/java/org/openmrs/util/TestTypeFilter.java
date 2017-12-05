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
import java.util.List;
import java.util.ArrayList;

import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

/**
 * This class exists so that Spring component-scans can exclude any unit test classes that were
 * accidentally included on the classpath (like in an omod). <br>
 * This filter returns true for any class that has a super class that is a Test like
 * BaseContextSensitiveTest or TestCase. <br>
 * Example usage:
 * 
 * <pre>
 *  &lt;context:component-scan base-package="org.openmrs"&gt;
 *    &lt;context:exclude-filter type="custom" expression="org.openmrs.util.TestTypeFilter"/&gt;
 *  &lt;/context:component-scan&gt;
 * </pre>
 * 
 * (Look at the applicationContext-service.xml file to see this in action)
 */
public class TestTypeFilter implements TypeFilter {
	
	/**
	 * Any class with a super class in this list will not be loaded (scanned) by Spring
	 */
	private static List<String> superClassNamesToExclude = new ArrayList<>();
	
	static {
		superClassNamesToExclude.add("org.openmrs.test.BaseContextSensitiveTest");
		superClassNamesToExclude.add("org.openmrs.test.BaseModuleContextSensitiveTest");
		superClassNamesToExclude.add("org.openmrs.web.test.BaseWebContextSensitiveTest");
		superClassNamesToExclude.add("org.openmrs.web.test.BaseModuleWebContextSensitiveTest");
		superClassNamesToExclude.add("org.springframework.test.AbstractTransactionalSpringContextTests");
		superClassNamesToExclude.add("org.openmrs.BaseTest");
		superClassNamesToExclude.add("junit.framework.TestCase");
	}
	
	/**
	 * @see org.springframework.core.type.filter.TypeFilter#match(org.springframework.core.type.classreading.MetadataReader,
	 *      org.springframework.core.type.classreading.MetadataReaderFactory)
	 */
	@Override
	public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
		String superClassName = metadataReader.getClassMetadata().getSuperClassName();
		if (superClassName != null) {
			return superClassNamesToExclude.contains(superClassName);
		}

		return false;
	}
	
}

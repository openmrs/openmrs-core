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
import java.util.List;
import java.util.Vector;

import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

/**
 * This class exists so that Spring component-scans can exclude any unit test classes that were
 * accidentally included on the classpath (like in an omod). <br/>
 * This filter returns true for any class that has a super class that is a Test like
 * BaseContextSensitiveTest or TestCase. <br/>
 * Example usage:
 * 
 * <pre>
 *  &lt;context:component-scan base-package="org.openmrs">
 *    &lt;context:exclude-filter type="custom" expression="org.openmrs.util.TestTypeFilter"/>
 *  &lt;/context:component-scan>
 * </pre>
 * 
 * (Look at the applicationContext-service.xml file to see this in action)
 */
public class TestTypeFilter implements TypeFilter {
	
	/**
	 * Any class with a super class in this list will not be loaded (scanned) by Spring
	 */
	private static List<String> superClassNamesToExclude = new Vector<String>();
	
	static {
		superClassNamesToExclude.add("org.openmrs.test.BaseContextSensitiveTest");
		superClassNamesToExclude.add("org.openmrs.test.BaseModuleContextSensitiveTest");
		superClassNamesToExclude.add("org.openmrs.web.test.BaseWebContextSensitiveTest");
		superClassNamesToExclude.add("org.openmrs.web.test.BaseModuleWebContextSensitiveTest");
		superClassNamesToExclude.add("junit.framework.TestCase");
	}
	
	/**
	 * @see org.springframework.core.type.filter.TypeFilter#match(org.springframework.core.type.classreading.MetadataReader,
	 *      org.springframework.core.type.classreading.MetadataReaderFactory)
	 */
	public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
		String superClassName = metadataReader.getClassMetadata().getSuperClassName();
		if (superClassName != null) {
			return superClassNamesToExclude.contains(superClassName);
		}
		
		return false;
	}
	
}

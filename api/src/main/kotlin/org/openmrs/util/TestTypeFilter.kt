/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util

import org.springframework.core.type.classreading.MetadataReader
import org.springframework.core.type.classreading.MetadataReaderFactory
import org.springframework.core.type.filter.TypeFilter

/**
 * This class exists so that Spring component-scans can exclude any unit test classes that were
 * accidentally included on the classpath (like in an omod).
 * This filter returns true for any class that has a super class that is a Test like
 * BaseContextSensitiveTest or TestCase.
 * Example usage:
 * ```
 *  <context:component-scan base-package="org.openmrs">
 *    <context:exclude-filter type="custom" expression="org.openmrs.util.TestTypeFilter"/>
 *  </context:component-scan>
 * ```
 * 
 * (Look at the applicationContext-service.xml file to see this in action)
 */
class TestTypeFilter : TypeFilter {
    
    /**
     * @see TypeFilter.match
     */
    override fun match(metadataReader: MetadataReader, metadataReaderFactory: MetadataReaderFactory): Boolean {
        val superClassName = metadataReader.classMetadata.superClassName
        if (superClassName != null) {
            return superClassNamesToExclude.contains(superClassName)
        }
        
        return false
    }
    
    companion object {
        /**
         * Any class with a super class in this list will not be loaded (scanned) by Spring
         */
        private val superClassNamesToExclude = listOf(
            "org.openmrs.test.BaseContextSensitiveTest",
            "org.openmrs.test.BaseModuleContextSensitiveTest",
            "org.openmrs.web.test.BaseWebContextSensitiveTest",
            "org.openmrs.web.test.BaseModuleWebContextSensitiveTest",
            "org.springframework.test.AbstractTransactionalSpringContextTests",
            "org.openmrs.BaseTest",
            "junit.framework.TestCase"
        )
    }
}

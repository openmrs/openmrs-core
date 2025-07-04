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

import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import java.util.Properties;

/**
 * Provides a PropertySourcesPlaceholderConfigurer that uses OpenmrsUtil.getApplicationDataDirectory()
 * to resolve the runtime properties file location, ensuring the property is always set.
 * 
 * @since 2.8.0
 */
@Configuration
public class OpenmrsPropertyConfig {
	
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        String appDataDir = OpenmrsUtil.getApplicationDataDirectory();
        Properties props = new Properties();
        props.setProperty(OpenmrsConstants.KEY_OPENMRS_APPLICATION_DATA_DIRECTORY, appDataDir);
        configurer.setProperties(props);
        configurer.setLocations(new ClassPathResource("hibernate.default.properties"), 
			new FileSystemResource(appDataDir + "/openmrs-runtime.properties"));
        configurer.setIgnoreResourceNotFound(true);
        configurer.setLocalOverride(true);
        return configurer;
    }
}

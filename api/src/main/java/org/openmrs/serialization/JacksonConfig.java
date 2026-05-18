/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.serialization;

import org.openmrs.util.OpenmrsJacksonLocaleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate7.Hibernate7Module;

/**
 * Default Jackson object mapper configuration.
 * <p>
 * Used for example by {@link org.openmrs.scheduler.jobrunr.JobRunrConfig}.
 *
 * @since 2.9.x
 */
@Configuration
public class JacksonConfig {

	@Bean
	public ObjectMapper schedulerObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate7Module());
		mapper.registerModule(new OpenmrsJacksonLocaleModule());
		return mapper;
	}

}

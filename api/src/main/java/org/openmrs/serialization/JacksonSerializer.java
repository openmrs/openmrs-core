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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;

public class JacksonSerializer {

	private static final ObjectMapper mapper = new ObjectMapper();

	public static String toJson(Object object) throws JsonProcessingException {
		return mapper.writeValueAsString(object);
	}

	public static <T> T fromJson(String json, Class<T> clazz) throws IOException {
		return mapper.readValue(json, clazz);
	}
}

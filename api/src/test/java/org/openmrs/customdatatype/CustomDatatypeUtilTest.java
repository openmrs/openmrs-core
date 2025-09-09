/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.customdatatype;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

public class CustomDatatypeUtilTest extends BaseContextSensitiveTest {
	
	/**
	 * @see CustomDatatypeUtil#deserializeSimpleConfiguration(String)
	 */
	@Test
	public void deserializeSimpleConfiguration_shouldDeserializeAConfigurationSerializedByTheCorrespondingSerializeMethod() {
		Map<String, String> config = new HashMap<>();
		config.put("one property", "one value");
		config.put("another property", "another value < with > strange&nbsp;characters");
		
		String serialized = CustomDatatypeUtil.serializeSimpleConfiguration(config);
		Map<String, String> deserialized = CustomDatatypeUtil.deserializeSimpleConfiguration(serialized);
		assertEquals(2, deserialized.size());
		assertEquals("one value", deserialized.get("one property"));
		assertEquals("another value < with > strange&nbsp;characters", deserialized.get("another property"));
	}
}

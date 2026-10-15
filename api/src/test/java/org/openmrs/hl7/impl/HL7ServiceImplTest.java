/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark of OpenMRS Inc.
 */
package org.openmrs.hl7.impl;

import org.junit.jupiter.api.Test;
import org.openmrs.api.APIException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HL7ServiceImplTest {

	@Test
	public void getRefByUuid_shouldThrowExceptionForUnsupportedType() {
		HL7ServiceImpl service = new HL7ServiceImpl();

		APIException exception = assertThrows(APIException.class, () -> service.getRefByUuid(String.class, "test-uuid"));

		assertEquals("Unsupported type for getRefByUuid: java.lang.String", exception.getMessage());
	}

	@Test
	public void getRefByUuid_shouldHandleNullType() {
		HL7ServiceImpl service = new HL7ServiceImpl();

		APIException exception = assertThrows(APIException.class, () -> service.getRefByUuid(null, "test-uuid"));

		assertEquals("Unsupported type for getRefByUuid: null", exception.getMessage());
	}
}

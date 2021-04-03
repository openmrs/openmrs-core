/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.messagesource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.impl.MessageSourceServiceImpl;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.MapBindingResult;

/**
 *
 */
public class MessageSourceServiceTest extends BaseContextSensitiveTest {
	
	/**
	 * MessageSourceServiceImpl.getMessage()should return last error code if no localization found
	 *
	 * @see MessageSourceServiceImpl#getMessage(MessageSourceResolvable resolvable, Locale locale)
	 */
	@Test
	public void getMessage_shouldReturnTheLastErrorCodeIfnoLocalizationIsFound() {
		MapBindingResult errors = new MapBindingResult(new HashMap<String, Object>(), "request");
		errors.rejectValue("myField", "myErrorCode");
		MessageSourceResolvable fieldError = errors.getFieldError("myField");
		assertEquals(3, fieldError.getCodes().length);
		assertEquals("myErrorCode.request.myField", fieldError.getCodes()[0]);
		assertEquals("myErrorCode.myField", fieldError.getCodes()[1]);
		assertEquals("myErrorCode", fieldError.getCodes()[2]);
		assertEquals("myErrorCode", Context.getMessageSourceService().getMessage(fieldError, Context.getLocale()));
	}
}

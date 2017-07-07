/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.messagesource.impl;

import org.junit.Test;
import org.openmrs.api.context.Context;

/**
 * Ensures that, when an exception with a message to localize, is thrown
 * before spring starts up, we do not get this exception:
 * org.openmrs.api.APIException: Service not found: interface org.openmrs.messagesource.MessageSourceService
 */
public class DefaultMessageSourceServiceImplTest {
	
	@Test
	public void getMessageSourceService_shouldNotThrowServiceNotFoundException() {
		Context.getMessageSourceService();
	}
}

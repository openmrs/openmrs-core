/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.layout.address;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;

public class AddressSupportTest extends BaseContextSensitiveTest {

	@Test
	public void deserializeAddressTemplate_shouldDeserializeHtmlEscapedXml() throws Exception {
		AddressTemplate addressTemplate = AddressSupport.getInstance()
				.deserializeAddressTemplate(StringEscapeUtils.escapeHtml4(OpenmrsConstants.DEFAULT_ADDRESS_TEMPLATE));

		assertNotNull(addressTemplate);
	}
}

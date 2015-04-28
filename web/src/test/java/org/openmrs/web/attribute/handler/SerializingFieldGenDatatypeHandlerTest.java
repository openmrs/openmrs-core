/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.attribute.handler;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Location;
import org.openmrs.customdatatype.datatype.MockLocationDatatype;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;

@RunWith(PowerMockRunner.class)
public class SerializingFieldGenDatatypeHandlerTest {
	
	/**
	 * @verifies return the correct typed value
	 * @see SerializingFieldGenDatatypeHandler#getValue(org.openmrs.customdatatype.SerializingCustomDatatype,
	 *      javax.servlet.http.HttpServletRequest, String)
	 */
	@Test
	public void getValue_shouldReturnTheCorrectTypedValue() throws Exception {
		final String locationUuid = "some uuid";
		final String formFieldName = "uuid";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(formFieldName, locationUuid);
		Location expectedLocation = mock(Location.class);
		MockLocationDatatype datatype = mock(MockLocationDatatype.class);
		when(datatype.deserialize(eq(locationUuid))).thenReturn(expectedLocation);
		SerializingFieldGenDatatypeHandler handler = new MockLocationFieldGenDatatypeHandler();
		Assert.assertEquals(expectedLocation, handler.getValue(datatype, request, formFieldName));
	}
}

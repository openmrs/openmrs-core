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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Location;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.datatype.MockLocationDatatype;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class BaseMetadataFieldGenDatatypeHandlerTest {
	
	private BaseMetadataFieldGenDatatypeHandler handler = new MockLocationFieldGenDatatypeHandler();
	
	/**
	 * @verifies return the name
	 * @see BaseMetadataFieldGenDatatypeHandler#toHtml(org.openmrs.customdatatype.CustomDatatype,
	 *      String)
	 */
	@Test
	public void toHtml_shouldReturnTheName() throws Exception {
		final String locationUuid = "some uuid";
		final String locationName = "some name";
		Location expectedLocation = new Location();
		expectedLocation.setName(locationName);
		
		MockLocationDatatype datatype = mock(MockLocationDatatype.class);
		when(datatype.deserialize(eq(locationUuid))).thenReturn(expectedLocation);
		when(datatype.getTextSummary(any(String.class))).thenCallRealMethod();
		when(datatype.doGetTextSummary(any(Location.class))).thenCallRealMethod();
		assertEquals(locationName, handler.toHtml(datatype, locationUuid));
	}
	
	/**
	 * @verifies use the name in the html summary instance
	 * @see BaseMetadataFieldGenDatatypeHandler#toHtmlSummary(org.openmrs.customdatatype.CustomDatatype,
	 *      String)
	 */
	@Test
	public void toHtmlSummary_shouldUseTheNameInTheHtmlSummaryInstance() throws Exception {
		final String locationUuid = "some uuid";
		final String locationName = "some name";
		Location expectedLocation = new Location();
		expectedLocation.setName(locationName);
		
		MockLocationDatatype datatype = mock(MockLocationDatatype.class);
		when(datatype.deserialize(eq(locationUuid))).thenReturn(expectedLocation);
		when(datatype.getTextSummary(any(String.class))).thenCallRealMethod();
		when(datatype.doGetTextSummary(any(Location.class))).thenCallRealMethod();
		CustomDatatype.Summary summary = handler.toHtmlSummary(datatype, locationUuid);
		assertNotNull(summary);
		assertEquals(locationName, summary.getSummary());
		assertEquals(true, summary.isComplete());
	}
}

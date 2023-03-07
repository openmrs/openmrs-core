/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.resource.impl;

import io.swagger.models.Model;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetadataDelegatingCrudResourceTest {
	
	/**
	 * @verifies return a localized message if specified
	 */
	@Test
	public void getDisplayString_shouldReturnALocalizedMessageIfSpecified() throws Exception {
		String UUID = "0cbe2ed3-cd5f-4f46-9459-26127c9265ab";
		
		MessageSourceService messageSourceService = mock(MessageSourceService.class);
		when(messageSourceService.getMessage("ui.i18n.Location.name." + UUID)).thenReturn("Correct");
		ServiceContext.getInstance().setMessageSourceService(messageSourceService);
		
		Location location = new Location();
		location.setName("Incorrect");
		location.setUuid(UUID);
		
		MockLocationResource resource = new MockLocationResource();
		String display = resource.getDisplayString(location);
		
		assertThat(display, is("Correct"));
	}
	
	/**
	 * @verifies return the name property when no localized message is specified
	 */
	@Test
	public void getDisplayString_shouldReturnTheNamePropertyWhenNoLocalizedMessageIsSpecified() throws Exception {
		Location location = new Location();
		location.setName("Correct");
		
		MockLocationResource resource = new MockLocationResource();
		String display = resource.getDisplayString(location);
		
		assertThat(display, is("Correct"));
	}
	
	/**
	 * @verifies return the empty string when no localized message is specified and the name
	 *           property is null
	 */
	@Test
	public void getDisplayString_shouldReturnTheEmptyStringWhenNoLocalizedMessageIsSpecifiedAndTheNamePropertyIsNull()
	        throws Exception {
		Location location = new Location();
		location.setName(null);
		
		MockLocationResource resource = new MockLocationResource();
		String display = resource.getDisplayString(location);
		
		assertThat(display, is(""));
	}
	
	class MockLocationResource extends MetadataDelegatingCrudResource<Location> {
		
		@Override
		public Location getByUniqueId(String uniqueId) {
			return null;
		}
		
		@Override
		public Location newDelegate() {
			return new Location();
		}
		
		@Override
		public Location save(Location delegate) {
			return null;
		}
		
		@Override
		public void purge(Location delegate, RequestContext context) throws ResponseException {
		}
		
		@Override
		public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
			return null;
		}
		
		@Override
		public Model getGETModel(Representation representation) {
			return null;
		}
		
		@Override
		public Model getCREATEModel(Representation representation) {
			return null;
		}
		
		@Override
		public Model getUPDATEModel(Representation representation) {
			return null;
		}
	}
	
}

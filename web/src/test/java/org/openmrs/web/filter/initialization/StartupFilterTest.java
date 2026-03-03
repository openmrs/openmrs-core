/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.initialization;

import static org.hamcrest.MatcherAssert.assertThat;

import javax.servlet.ServletException;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.web.filter.StartupFilter;
import org.openmrs.web.filter.startuperror.StartupErrorFilter;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.hamcrest.CoreMatchers.is;


public class StartupFilterTest {
	
	private StartupFilter startupFilter;
	
	@BeforeEach
	public void setUp() {
		startupFilter = new StartupErrorFilter();
	}
	
	@Test
	public void shouldReturnServiceUnavailableIfOpenmrsNotStarted() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain mockFilterChain=new MockFilterChain();
		request.setMethod("GET");
		request.setServletPath("/health/alive");
		startupFilter.doFilter(request,response,mockFilterChain);
		assertThat(response.getStatus(), is(HttpStatus.SERVICE_UNAVAILABLE.value()));
	}
	
}

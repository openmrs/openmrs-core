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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.web.filter.StartupFilter;
import org.openmrs.web.filter.startuperror.StartupErrorFilter;
import org.openmrs.web.filter.util.FilterUtil;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

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
		MockFilterChain mockFilterChain = new MockFilterChain();
		request.setMethod("GET");
		request.setServletPath("/health/alive");
		startupFilter.doFilter(request, response, mockFilterChain);
		assertThat(response.getStatus(), is(HttpStatus.SERVICE_UNAVAILABLE.value()));
	}

	@Test
	public void renderTemplate_shouldPutTheModelFieldsExceptThePasswordsIntoTheTemplate() throws Exception {
		ModelRenderingFilter filter = new ModelRenderingFilter();
		filter.init(new MockFilterConfig(new MockServletContext()));
		filter.model.databaseName = "rendered_database";
		filter.model.databaseRootPassword = "NotRenderedSecret1";
		filter.model.createDatabasePassword = "NotRenderedSecret2";
		filter.model.createUserPassword = "NotRenderedSecret3";
		filter.model.currentDatabasePassword = "NotRenderedSecret4";
		filter.model.adminUserPassword = "NotRenderedSecret5";
		filter.model.remotePassword = "NotRenderedSecret6";

		String page = filter.render("modelfields.vm");

		assertThat(page, containsString("[rendered_database]"));
		// a constant, which simplesetup.vm shows
		assertThat(page, containsString("[" + InitializationWizardModel.ADMIN_DEFAULT_PASSWORD + "]"));
		assertThat(page, not(containsString("NotRenderedSecret")));
	}

	/**
	 * Renders a template from the test classpath with a wizard model
	 */
	static class ModelRenderingFilter extends StartupFilter {

		final InitializationWizardModel model = new InitializationWizardModel();

		String render(String templateName) throws IOException {
			Map<String, Object> referenceMap = new HashMap<>();
			referenceMap.put(FilterUtil.LOCALE_ATTRIBUTE, "en");
			MockHttpServletResponse response = new MockHttpServletResponse();
			renderTemplate(templateName, referenceMap, response);
			return response.getContentAsString();
		}

		@Override
		protected void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		}

		@Override
		protected void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		}

		@Override
		protected String getTemplatePrefix() {
			return "org/openmrs/web/filter/startupfilter/";
		}

		@Override
		protected Object getUpdateFilterModel() {
			return model;
		}

		@Override
		public boolean skipFilter(HttpServletRequest request) {
			return false;
		}
	}
}

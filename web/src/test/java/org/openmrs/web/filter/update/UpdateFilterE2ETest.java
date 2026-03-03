/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.web.filter.StartupFilter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * End-to-end tests for the database update wizard flow in {@link UpdateFilter}.
 * These tests verify the page-to-page navigation, authentication gating, and
 * model state changes without requiring a real database or Spring context.
 */
class UpdateFilterE2ETest {
	
	private TestableUpdateFilter filter;
	
	private MockHttpServletRequest request;
	
	private MockHttpServletResponse response;
	
	/**
	 * Testable subclass that overrides renderTemplate and authenticateAsSuperUser
	 * to avoid Velocity rendering and database access.
	 */
	static class TestableUpdateFilter extends UpdateFilter {
		
		String lastRenderedTemplate;
		
		Map<String, Object> lastReferenceMap;
		
		boolean authResult = false;
		
		@Override
		protected void renderTemplate(String templateName, Map<String, Object> referenceMap,
				HttpServletResponse httpResponse) throws IOException {
			this.lastRenderedTemplate = templateName;
			this.lastReferenceMap = referenceMap;
		}
		
		@Override
		protected boolean authenticateAsSuperUser(String usernameOrSystemId, String password) throws ServletException {
			return authResult;
		}
	}
	
	@BeforeEach
	void setup() throws Exception {
		filter = new TestableUpdateFilter();
		// Initialize the updateFilterModel via reflection since init() requires FilterConfig
		setUpdateFilterModel(new UpdateFilterModel(null, null) {
			{
				// Override constructor side effects - don't call database
				this.updateRequired = false;
				this.changes = null;
			}
		});
		setAuthenticatedSuccessfully(false);
		setDatabaseUpdateInProgress(false);
		UpdateFilter.setLockReleased(false);
		
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}
	
	@AfterEach
	void cleanup() throws Exception {
		setDatabaseUpdateInProgress(false);
		setAuthenticatedSuccessfully(false);
		UpdateFilter.setUpdatesRequired(true);
		UpdateFilter.setLockReleased(false);
	}
	
	// ========== GET Request ==========
	
	@Test
	void doGet_shouldRenderMaintenancePage() throws Exception {
		request.getSession().setAttribute("locale", "en");
		
		filter.doGet(request, response);
		
		assertEquals("maintenance.vm", filter.lastRenderedTemplate);
	}
	
	@Test
	void doGet_shouldPassLocaleToReferenceMap() throws Exception {
		// checkLocaleAttributesForFirstTime sets the locale from request locale,
		// so we set the request locale to English which is always available
		request.addPreferredLocale(java.util.Locale.ENGLISH);
		
		filter.doGet(request, response);
		
		assertEquals("maintenance.vm", filter.lastRenderedTemplate);
		assertNotNull(filter.lastReferenceMap);
		assertEquals("en", filter.lastReferenceMap.get("locale"));
	}
	
	// ========== Maintenance Page (authentication) ==========
	
	@Test
	void maintenancePage_shouldRenderReviewChangesOnSuccessfulAuth() throws Exception {
		filter.authResult = true;
		request.setParameter("page", "maintenance.vm");
		request.setParameter("username", "admin");
		request.setParameter("password", "Admin123");
		
		filter.doPost(request, response);
		
		assertEquals("reviewchanges.vm", filter.lastRenderedTemplate);
	}
	
	@Test
	void maintenancePage_shouldRenderMaintenancePageOnFailedAuth() throws Exception {
		filter.authResult = false;
		request.setParameter("page", "maintenance.vm");
		request.setParameter("username", "bad_user");
		request.setParameter("password", "bad_pass");
		
		filter.doPost(request, response);
		
		assertEquals("maintenance.vm", filter.lastRenderedTemplate);
		assertTrue(getErrors().containsKey("update.error.unableAuthenticate"));
	}
	
	@Test
	void maintenancePage_shouldSetAuthenticatedSuccessfullyOnValidLogin() throws Exception {
		filter.authResult = true;
		request.setParameter("page", "maintenance.vm");
		request.setParameter("username", "admin");
		request.setParameter("password", "Admin123");
		
		filter.doPost(request, response);
		
		assertTrue(getAuthenticatedSuccessfully());
	}
	
	@Test
	void maintenancePage_shouldShowProgressWhenUpdateAlreadyInProgress() throws Exception {
		filter.authResult = true;
		setDatabaseUpdateInProgress(true);
		request.setParameter("page", "maintenance.vm");
		request.setParameter("username", "admin");
		request.setParameter("password", "Admin123");
		
		filter.doPost(request, response);
		
		assertEquals("reviewchanges.vm", filter.lastRenderedTemplate);
		assertNotNull(filter.lastReferenceMap);
		assertEquals(true, filter.lastReferenceMap.get("isDatabaseUpdateInProgress"));
		assertEquals(true, filter.lastReferenceMap.get("updateJobStarted"));
	}
	
	// ========== Review Changes Page ==========
	
	@Test
	void reviewChangesPage_shouldRedirectToMaintenanceIfNotAuthenticated() throws Exception {
		setAuthenticatedSuccessfully(false);
		request.setParameter("page", "reviewchanges.vm");
		
		filter.doPost(request, response);
		
		assertEquals("maintenance.vm", filter.lastRenderedTemplate);
	}
	
	@Test
	void reviewChangesPage_shouldRenderReviewChangesWhenAuthenticated() throws Exception {
		setAuthenticatedSuccessfully(true);
		request.setParameter("page", "reviewchanges.vm");
		
		filter.doPost(request, response);
		
		assertEquals("reviewchanges.vm", filter.lastRenderedTemplate);
	}
	
	@Test
	void reviewChangesPage_shouldSetUpdateJobStartedFlag() throws Exception {
		setAuthenticatedSuccessfully(true);
		request.setParameter("page", "reviewchanges.vm");
		
		filter.doPost(request, response);
		
		assertNotNull(filter.lastReferenceMap);
		assertEquals(true, filter.lastReferenceMap.get("updateJobStarted"));
	}
	
	@Test
	void reviewChangesPage_shouldIndicateAlreadyInProgressWhenAnotherUpdateRunning() throws Exception {
		setAuthenticatedSuccessfully(true);
		setDatabaseUpdateInProgress(true);
		request.setParameter("page", "reviewchanges.vm");
		
		filter.doPost(request, response);
		
		assertEquals("reviewchanges.vm", filter.lastRenderedTemplate);
		assertEquals(true, filter.lastReferenceMap.get("isDatabaseUpdateInProgress"));
		assertEquals(true, filter.lastReferenceMap.get("updateJobStarted"));
	}
	
	// ========== AJAX Progress Endpoint ==========
	
	@Test
	void progressAjax_shouldReturnJsonContentType() throws Exception {
		// Use a real response so we can check content type and written content
		MockHttpServletResponse realResponse = new MockHttpServletResponse();
		TestableUpdateFilter ajaxFilter = new TestableUpdateFilter();
		setUpdateFilterModel(ajaxFilter, new UpdateFilterModel(null, null) {
			{
				this.updateRequired = false;
				this.changes = null;
			}
		});
		
		request.setParameter("page", "updateProgress.vm.ajaxRequest");
		
		ajaxFilter.doPost(request, realResponse);
		
		assertEquals("text/json", realResponse.getContentType());
		assertEquals("no-cache", realResponse.getHeader("Cache-Control"));
	}
	
	@Test
	void progressAjax_shouldWriteJsonResponseBody() throws Exception {
		MockHttpServletResponse realResponse = new MockHttpServletResponse();
		request.setParameter("page", "updateProgress.vm.ajaxRequest");
		
		filter.doPost(request, realResponse);
		
		// With no updateJob, the response should be an empty JSON object
		String content = realResponse.getContentAsString();
		assertNotNull(content);
		assertEquals("{}", content);
	}
	
	// ========== Full Flow: Login -> Review Changes ==========
	
	@Test
	void fullFlow_shouldNavigateFromLoginToReviewChanges() throws Exception {
		// Step 1: GET -> maintenance page
		filter.doGet(request, response);
		assertEquals("maintenance.vm", filter.lastRenderedTemplate);
		
		// Step 2: POST login with valid credentials -> review changes
		resetRequest();
		filter.authResult = true;
		request.setParameter("page", "maintenance.vm");
		request.setParameter("username", "admin");
		request.setParameter("password", "Admin123");
		clearErrors();
		filter.doPost(request, response);
		assertEquals("reviewchanges.vm", filter.lastRenderedTemplate);
		assertTrue(getAuthenticatedSuccessfully());
		
		// Step 3: POST from review changes page -> starts update, shows review with job started
		resetRequest();
		request.setParameter("page", "reviewchanges.vm");
		clearErrors();
		filter.doPost(request, response);
		assertEquals("reviewchanges.vm", filter.lastRenderedTemplate);
		assertEquals(true, filter.lastReferenceMap.get("updateJobStarted"));
	}
	
	// ========== Full Flow: Failed Login Retry ==========
	
	@Test
	void fullFlow_shouldAllowRetryAfterFailedLogin() throws Exception {
		// Step 1: Failed login
		filter.authResult = false;
		request.setParameter("page", "maintenance.vm");
		request.setParameter("username", "bad_user");
		request.setParameter("password", "bad_pass");
		filter.doPost(request, response);
		assertEquals("maintenance.vm", filter.lastRenderedTemplate);
		assertTrue(getErrors().containsKey("update.error.unableAuthenticate"));
		
		// Step 2: Successful login
		resetRequest();
		filter.authResult = true;
		request.setParameter("page", "maintenance.vm");
		request.setParameter("username", "admin");
		request.setParameter("password", "Admin123");
		clearErrors();
		filter.doPost(request, response);
		assertEquals("reviewchanges.vm", filter.lastRenderedTemplate);
	}
	
	// ========== Security: Unauthenticated Access ==========
	
	@Test
	void reviewChangesPage_shouldBlockDirectAccessWithoutLogin() throws Exception {
		// Try to go directly to review changes without authenticating
		request.setParameter("page", "reviewchanges.vm");
		
		filter.doPost(request, response);
		
		assertEquals("maintenance.vm", filter.lastRenderedTemplate);
	}
	
	@Test
	void fullFlow_shouldBlockReviewChangesAfterFailedAuth() throws Exception {
		// Failed authentication
		filter.authResult = false;
		request.setParameter("page", "maintenance.vm");
		request.setParameter("username", "bad_user");
		request.setParameter("password", "bad_pass");
		filter.doPost(request, response);
		
		// Try to access review changes
		resetRequest();
		request.setParameter("page", "reviewchanges.vm");
		clearErrors();
		filter.doPost(request, response);
		
		assertEquals("maintenance.vm", filter.lastRenderedTemplate);
	}
	
	// ========== Locale Handling ==========
	
	@Test
	void maintenancePage_shouldPassLocaleFromSessionToReferenceMap() throws Exception {
		filter.authResult = true;
		// Pre-set the locale in the session before the POST
		request.getSession().setAttribute("locale", "es");
		request.setParameter("page", "maintenance.vm");
		request.setParameter("username", "admin");
		request.setParameter("password", "Admin123");
		
		filter.doPost(request, response);
		
		assertNotNull(filter.lastReferenceMap);
		// After successful auth, FilterUtil.restoreLocale may return null which leaves session locale intact
		assertNotNull(filter.lastReferenceMap.get("locale"));
	}
	
	// ========== Multiple Users Scenario ==========
	
	@Test
	void reviewChangesPage_shouldReportInProgressWhenSecondUserTriesToRunUpdates() throws Exception {
		// First user authenticates and triggers update
		filter.authResult = true;
		request.setParameter("page", "maintenance.vm");
		request.setParameter("username", "admin");
		request.setParameter("password", "Admin123");
		filter.doPost(request, response);
		
		resetRequest();
		request.setParameter("page", "reviewchanges.vm");
		filter.doPost(request, response);
		assertEquals(true, filter.lastReferenceMap.get("updateJobStarted"));
		
		// Simulate second user authenticating while update is in progress
		setDatabaseUpdateInProgress(true);
		resetRequest();
		request.setParameter("page", "maintenance.vm");
		request.setParameter("username", "admin2");
		request.setParameter("password", "Admin456");
		clearErrors();
		filter.doPost(request, response);
		
		assertEquals("reviewchanges.vm", filter.lastRenderedTemplate);
		assertEquals(true, filter.lastReferenceMap.get("isDatabaseUpdateInProgress"));
		assertEquals(true, filter.lastReferenceMap.get("updateJobStarted"));
	}
	
	// ========== skipFilter ==========
	
	@Test
	void skipFilter_shouldReturnFalseWhenUpdatesRequired() {
		UpdateFilter.setUpdatesRequired(true);
		MockHttpServletRequest req = new MockHttpServletRequest();
		
		boolean result = filter.skipFilter(req);
		
		assertEquals(false, result);
	}
	
	@Test
	void skipFilter_shouldReturnTrueWhenNoUpdatesRequired() {
		UpdateFilter.setUpdatesRequired(false);
		MockHttpServletRequest req = new MockHttpServletRequest();
		
		boolean result = filter.skipFilter(req);
		
		assertEquals(true, result);
	}
	
	@Test
	void skipFilter_shouldReturnFalseForAjaxProgressRequestEvenWhenNoUpdatesRequired() {
		UpdateFilter.setUpdatesRequired(false);
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.setParameter("page", "updateProgress.vm.ajaxRequest");
		
		boolean result = filter.skipFilter(req);
		
		assertEquals(false, result);
	}
	
	// ========== Helper Methods ==========
	
	@SuppressWarnings("unchecked")
	private Map<String, Object[]> getErrors() throws Exception {
		Field errorsField = StartupFilter.class.getDeclaredField("errors");
		errorsField.setAccessible(true);
		return (Map<String, Object[]>) errorsField.get(filter);
	}
	
	private void clearErrors() throws Exception {
		getErrors().clear();
	}
	
	private void resetRequest() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}
	
	private void setAuthenticatedSuccessfully(boolean value) throws Exception {
		Field field = UpdateFilter.class.getDeclaredField("authenticatedSuccessfully");
		field.setAccessible(true);
		field.setBoolean(filter, value);
	}
	
	private boolean getAuthenticatedSuccessfully() throws Exception {
		Field field = UpdateFilter.class.getDeclaredField("authenticatedSuccessfully");
		field.setAccessible(true);
		return field.getBoolean(filter);
	}
	
	private void setDatabaseUpdateInProgress(boolean value) throws Exception {
		Field field = UpdateFilter.class.getDeclaredField("isDatabaseUpdateInProgress");
		field.setAccessible(true);
		field.setBoolean(null, value);
	}
	
	private void setUpdateFilterModel(UpdateFilterModel model) throws Exception {
		setUpdateFilterModel(filter, model);
	}
	
	private void setUpdateFilterModel(UpdateFilter targetFilter, UpdateFilterModel model) throws Exception {
		Field field = UpdateFilter.class.getDeclaredField("updateFilterModel");
		field.setAccessible(true);
		field.set(targetFilter, model);
	}
}

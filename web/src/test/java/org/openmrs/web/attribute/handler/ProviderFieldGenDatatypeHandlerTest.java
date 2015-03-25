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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Provider;
import org.openmrs.customdatatype.datatype.ProviderDatatype;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

/**
 * Unit tests for {@link ProviderFieldGenDatatypeHandler}
 */
public class ProviderFieldGenDatatypeHandlerTest extends
		BaseWebContextSensitiveTest {
	private ProviderFieldGenDatatypeHandler handler;
	private ProviderDatatype datatype;
	private final String uuid = "c2299800-cca9-11e0-9572-0800200c9a66"; 
	private final String formFieldName = "provider_uuid";
	private HttpServletRequest request;

	/**
	 * Initializer method executed before each test
	 */
	@Before
	public void before() {
		datatype = new ProviderDatatype();
		handler = new ProviderFieldGenDatatypeHandler();
		request = new HttpServletRequestImpl();
	}

	/**
	 * Tests using the uuid of the Super User. Expected Provider is Super User.
	 * @see ProviderFieldGenDatatypeHandler#getValue(ProviderDatatype,
	 *      HttpServletRequest, String)
	 * @throws Exception
	 */
	@Test
	@Verifies(value = "return the corresponding provider object if it exists", method = "getValue(ProviderDatatype, HttpServletRequest, String)")
	public void getValue_ShouldReturnCorrespondingProvider() throws Exception {
		Provider actualProvider = handler.getValue(datatype, request,
				formFieldName);
		Provider expectedProvider = datatype.deserialize(uuid);
		Assert.assertEquals(expectedProvider, actualProvider);
	}

	/**
	 * {@link HttpServletRequest} implementation for this test.
	 * HttpServletRequest implementation is provided by the Servlet Container
	 * (Tomcat/ Jetty). But for testing purpose, this local implementation is
	 * being used as the HttpServletRequqest object is not available in the
	 * testing environment. Please note that only the
	 * {@link HttpServletRequest#getParameter(String)} has been defined suitably
	 * for the purpose of this test.
	 * 
	 *
	 */
	private class HttpServletRequestImpl implements HttpServletRequest {

		private Map<String, String> parameters = new HashMap();

		public HttpServletRequestImpl() {
			parameters.put(formFieldName, uuid);
		}

		@Override
		public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1)
				throws IllegalStateException {
			return null;
		}

		@Override
		public AsyncContext startAsync() throws IllegalStateException {
			return null;
		}

		@Override
		public void setCharacterEncoding(String arg0)
				throws UnsupportedEncodingException {
		}

		@Override
		public void setAttribute(String arg0, Object arg1) {
		}

		@Override
		public void removeAttribute(String arg0) {
		}

		@Override
		public boolean isSecure() {
			return false;
		}

		@Override
		public boolean isAsyncSupported() {
			return false;
		}

		@Override
		public boolean isAsyncStarted() {
			return false;
		}

		@Override
		public ServletContext getServletContext() {
			return null;
		}

		@Override
		public int getServerPort() {
			return 0;
		}

		@Override
		public String getServerName() {
			return null;
		}

		@Override
		public String getScheme() {
			return null;
		}

		@Override
		public RequestDispatcher getRequestDispatcher(String arg0) {
			return null;
		}

		@Override
		public int getRemotePort() {
			return 0;
		}

		@Override
		public String getRemoteHost() {
			return null;
		}

		@Override
		public String getRemoteAddr() {
			return null;
		}

		@Override
		public String getRealPath(String arg0) {
			return null;
		}

		@Override
		public BufferedReader getReader() throws IOException {
			return null;
		}

		@Override
		public String getProtocol() {
			return null;
		}

		@Override
		public String[] getParameterValues(String arg0) {
			return null;
		}

		@Override
		public Enumeration<String> getParameterNames() {
			return null;
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			return null;
		}

		/**
		 * adds the parameter to the parameter HashMap
		 */
		@Override
		public String getParameter(String arg0) {
			if (parameters.containsKey(arg0))
				return parameters.get(arg0);
			return null;
		}

		@Override
		public Enumeration<Locale> getLocales() {
			return null;
		}

		@Override
		public Locale getLocale() {
			return null;
		}

		@Override
		public int getLocalPort() {
			return 0;
		}

		@Override
		public String getLocalName() {
			return null;
		}

		@Override
		public String getLocalAddr() {
			return null;
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			return null;
		}

		@Override
		public DispatcherType getDispatcherType() {
			return null;
		}

		@Override
		public String getContentType() {
			return null;
		}

		@Override
		public int getContentLength() {
			return 0;
		}

		@Override
		public String getCharacterEncoding() {
			return null;
		}

		@Override
		public Enumeration<String> getAttributeNames() {
			return null;
		}

		@Override
		public Object getAttribute(String arg0) {
			return null;
		}

		@Override
		public AsyncContext getAsyncContext() {
			return null;
		}

		@Override
		public void logout() throws ServletException {

		}

		@Override
		public void login(String arg0, String arg1) throws ServletException {

		}

		@Override
		public boolean isUserInRole(String arg0) {
			return false;
		}

		@Override
		public boolean isRequestedSessionIdValid() {
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromUrl() {
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromURL() {
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromCookie() {
			return false;
		}

		@Override
		public Principal getUserPrincipal() {
			return null;
		}

		@Override
		public HttpSession getSession(boolean arg0) {
			return null;
		}

		@Override
		public HttpSession getSession() {
			return null;
		}

		@Override
		public String getServletPath() {
			return null;
		}

		@Override
		public String getRequestedSessionId() {
			return null;
		}

		@Override
		public StringBuffer getRequestURL() {
			return null;
		}

		@Override
		public String getRequestURI() {
			return null;
		}

		@Override
		public String getRemoteUser() {
			return null;
		}

		@Override
		public String getQueryString() {
			return null;
		}

		@Override
		public String getPathTranslated() {
			return null;
		}

		@Override
		public String getPathInfo() {
			return null;
		}

		@Override
		public Collection<Part> getParts() throws IOException, ServletException {
			return null;
		}

		@Override
		public Part getPart(String arg0) throws IOException, ServletException {
			return null;
		}

		@Override
		public String getMethod() {
			return null;
		}

		@Override
		public int getIntHeader(String arg0) {
			return 0;
		}

		@Override
		public Enumeration<String> getHeaders(String arg0) {
			return null;
		}

		@Override
		public Enumeration<String> getHeaderNames() {
			return null;
		}

		@Override
		public String getHeader(String arg0) {
			return null;
		}

		@Override
		public long getDateHeader(String arg0) {
			return 0;
		}

		@Override
		public Cookie[] getCookies() {
			return null;
		}

		@Override
		public String getContextPath() {
			return null;
		}

		@Override
		public String getAuthType() {
			return null;
		}

		@Override
		public boolean authenticate(HttpServletResponse arg0)
				throws IOException, ServletException {
			return false;
		}
	}

}

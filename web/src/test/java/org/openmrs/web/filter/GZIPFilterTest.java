/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.GlobalPropertiesTestHelper;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.test.jupiter.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link GZIPFilter} against a real {@link org.openmrs.api.AdministrationService}. The
 * caching the filter layers on top of the global properties is covered by
 * {@link GZIPFilterCachingTest}.
 */
public class GZIPFilterTest extends BaseWebContextSensitiveTest {

	private static final String ACCEPT_PATHS = "gzip.acceptCompressedRequestsForPaths";

	private GlobalPropertiesTestHelper globalProperties;

	@BeforeEach
	public void before() {
		globalProperties = new GlobalPropertiesTestHelper(Context.getAdministrationService());
	}

	/**
	 * The transaction each test runs in is rolled back, but the global property listeners fired when
	 * the property was saved have already written to caches that are static and outlive it — notably
	 * {@link org.openmrs.util.ConfigUtil}'s. Purging the property fires the listeners again and evicts
	 * it, so the value does not leak into whatever test runs next in this JVM.
	 */
	@AfterEach
	public void purgeGlobalProperties() {
		globalProperties.purgeGlobalProperty(ACCEPT_PATHS);
		globalProperties.purgeGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_GZIP_ENABLED);
	}

	/**
	 * @see GZIPFilter#doFilterInternal(HttpServletRequest,HttpServletResponse,
	 *      jakarta.servlet.FilterChain)
	 */
	@Test
	public void zipRequestWrapperTest_shouldReturnTrueIfUnzippedContentReadFromWrapperIsTheSameAsContentBeforeZipping()
	        throws Exception {
		globalProperties.setGlobalProperty(ACCEPT_PATHS, ".*");

		MockHttpServletRequest req = new MockHttpServletRequest();
		req.setContextPath("http://gzipservletpath");
		req.addHeader("Content-encoding", "gzip");

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try (GZIPOutputStream gzOutput = new GZIPOutputStream(stream)) {
			gzOutput.write("message string".getBytes(StandardCharsets.UTF_8));
		}
		req.setContent(stream.toByteArray());

		MockHttpServletResponse resp = new MockHttpServletResponse();
		FilterChain fil = mock(FilterChain.class);
		GZIPFilter gzipFilter = new GZIPFilter();
		gzipFilter.doFilterInternal(req, resp, fil);

		final ArgumentCaptor<HttpServletRequest> argumentCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
		Mockito.verify(fil).doFilter(argumentCaptor.capture(), Mockito.any(HttpServletResponse.class));
		HttpServletRequest requestArgument = argumentCaptor.getValue();
		try {
			InputStream iStream = requestArgument.getInputStream();
			InputStreamReader iReader = new InputStreamReader(iStream);
			BufferedReader bufReader = new BufferedReader(iReader);
			String outputMessage = bufReader.readLine();

			assertThat(outputMessage, is("message string"));
		} catch (IOException e) {
			throw new RuntimeException();
		}

	}

	@Test
	public void performGZIPRequest_shouldRejectACompressedRequestForAPathThatIsNotAccepted() throws Exception {
		globalProperties.setGlobalProperty(ACCEPT_PATHS, "/openmrs/ws/rest/.*");

		GZIPFilter gzipFilter = new GZIPFilter();

		assertThrows(APIException.class, () -> gzipFilter.performGZIPRequest(compressedRequestFor("/openmrs/index.htm")));
	}

	/**
	 * The accepted paths are compiled once and held until the property changes. This checks the whole
	 * chain that makes a change visible: saving the property notifies ConfigUtil, which is a
	 * GlobalPropertyListener, and the filter then sees the new value and recompiles.
	 */
	@Test
	public void performGZIPRequest_shouldPickUpChangesToTheAcceptedPathsGlobalProperty() throws Exception {
		globalProperties.setGlobalProperty(ACCEPT_PATHS, "/openmrs/ws/rest/.*");

		GZIPFilter gzipFilter = new GZIPFilter();
		assertThrows(APIException.class, () -> gzipFilter.performGZIPRequest(compressedRequestFor("/openmrs/index.htm")));

		globalProperties.setGlobalProperty(ACCEPT_PATHS, "/openmrs/.*");

		assertThat(gzipFilter.performGZIPRequest(compressedRequestFor("/openmrs/index.htm")),
		    instanceOf(GZIPRequestWrapper.class));
	}

	private static MockHttpServletRequest compressedRequestFor(String requestURI) throws IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.setRequestURI(requestURI);
		req.addHeader("Content-encoding", "gzip");

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (GZIPOutputStream gzipStream = new GZIPOutputStream(bytes)) {
			gzipStream.write("message string".getBytes(StandardCharsets.UTF_8));
		}
		req.setContent(bytes.toByteArray());

		return req;
	}

	@Test
	public void doFilterInternal_shouldCompressResponseWhenGZIPIsSupportedAndEnabled() throws Exception {
		globalProperties.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_GZIP_ENABLED, "true");

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Accept-Encoding", "gzip");

		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain filterChain = (req, res) -> {
			res.getOutputStream().write("message string".getBytes(StandardCharsets.UTF_8));
		};

		GZIPFilter gzipFilter = new GZIPFilter();

		gzipFilter.doFilterInternal(request, response, filterChain);

		assertThat(response.getHeader("Content-Encoding"), is("gzip"));

		byte[] compressedResponse = response.getContentAsByteArray();

		try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(compressedResponse));
		        InputStreamReader reader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
		        BufferedReader bufferedReader = new BufferedReader(reader)) {

			assertThat(bufferedReader.readLine(), is("message string"));
		}
	}

}

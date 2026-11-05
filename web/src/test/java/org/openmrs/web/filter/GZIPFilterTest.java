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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
	 * @see GZIPFilter#doFilterInternal(HttpServletRequest,HttpServletResponse,
	 *      javax.servlet.FilterChain)
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
		}
		catch (IOException e) {
			throw new RuntimeException();
		}

	}

	/**
	 * Regression test for TRUNK-5978: a response larger than the stream's in-memory buffer, whose
	 * length is set with {@code setContentLengthLong} (as Tomcat's DefaultServlet does), must be
	 * delivered as a valid gzip response rather than advertising its uncompressed length.
	 */
	@Test
	public void doFilterInternal_shouldNotSendTheUncompressedLengthForALargeCompressedResponse() throws Exception {
		globalProperties.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_GZIP_ENABLED, "true");

		byte[] original = new byte[120_000];
		for (int i = 0; i < original.length; i++) {
			original[i] = (byte) ('a' + (i % 26));
		}

		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addHeader("accept-encoding", "gzip");
		MockHttpServletResponse resp = new MockHttpServletResponse();

		GZIPFilter gzipFilter = new GZIPFilter();
		gzipFilter.doFilterInternal(req, resp, (request, response) -> {
			response.setContentLengthLong(original.length);
			response.getOutputStream().write(original);
		});

		assertThat(resp.getHeader("Content-Encoding"), is("gzip"));
		assertThat(resp.getHeader("Content-Length"), nullValue());
		assertThat(gunzip(resp.getContentAsByteArray()), is(original));
	}

	@Test
	public void performGZIPRequest_shouldRejectACompressedRequestForAPathThatIsNotAccepted() throws Exception {
		globalProperties.setGlobalProperty(ACCEPT_PATHS, "/openmrs/ws/rest/.*");

		GZIPFilter gzipFilter = new GZIPFilter();

		assertThrows(APIException.class, () -> gzipFilter.performGZIPRequest(compressedRequestFor("/openmrs/index.htm")));
	}

	/**
	 * The accepted paths are compiled once and held until the property changes. This checks the whole
	 * chain that makes a change visible: saving the property evicts it from the global property cache,
	 * and the filter then sees the new value and recompiles.
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

	private static byte[] gunzip(byte[] bytes) throws IOException {
		ByteArrayOutputStream decompressed = new ByteArrayOutputStream();
		try (GZIPInputStream gzipStream = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
			byte[] buffer = new byte[8192];
			int read;
			while ((read = gzipStream.read(buffer)) != -1) {
				decompressed.write(buffer, 0, read);
			}
		}
		return decompressed.toByteArray();
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

}

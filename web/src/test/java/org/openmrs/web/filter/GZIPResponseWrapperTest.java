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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import javax.servlet.DispatcherType;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests the compression behaviour of {@link GZIPResponseWrapper} and
 * {@link GZIPResponseStream} without going through the filter, so that each acceptance criterion can
 * be exercised directly.
 */
class GZIPResponseWrapperTest {
	
	private static final byte[] LARGE_BODY = patternedBody(120_000);
	
	private static final byte[] MEDIUM_BODY = patternedBody(4_000);
	
	private static final byte[] SMALL_BODY = "hello".getBytes(StandardCharsets.UTF_8);
	
	/**
	 * Regression test for the bug that started this: Tomcat's DefaultServlet sets the length with
	 * {@code setContentLengthLong}, which the wrapper used to ignore, so a large compressed response
	 * advertised its uncompressed length and the client waited for bytes that never arrived.
	 */
	@Test
	void shouldNotSendTheUncompressedLengthWhenItIsSetWithSetContentLengthLongForALargeResponse() throws Exception {
		assertLargeResponseDoesNotCarryUncompressedLength((wrapper, length) -> wrapper.setContentLengthLong(length));
	}
	
	@Test
	void shouldNotSendTheUncompressedLengthWhenItIsSetWithSetContentLengthForALargeResponse() throws Exception {
		assertLargeResponseDoesNotCarryUncompressedLength((wrapper, length) -> wrapper.setContentLength((int) length));
	}
	
	@Test
	void shouldNotSendTheUncompressedLengthWhenItIsSetWithSetHeaderForALargeResponse() throws Exception {
		assertLargeResponseDoesNotCarryUncompressedLength(
		    (wrapper, length) -> wrapper.setHeader("Content-Length", Long.toString(length)));
	}
	
	@Test
	void shouldNotSendTheUncompressedLengthWhenItIsSetWithAddHeaderForALargeResponse() throws Exception {
		assertLargeResponseDoesNotCarryUncompressedLength(
		    (wrapper, length) -> wrapper.addHeader("Content-Length", Long.toString(length)));
	}
	
	@Test
	void shouldNotSendTheUncompressedLengthWhenItIsSetWithSetIntHeaderForALargeResponse() throws Exception {
		assertLargeResponseDoesNotCarryUncompressedLength(
		    (wrapper, length) -> wrapper.setIntHeader("Content-Length", (int) length));
	}
	
	@Test
	void shouldUseTheCompressedLengthForABufferedCompressedResponse() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		wrapper.setContentLengthLong(MEDIUM_BODY.length);
		wrapper.getOutputStream().write(MEDIUM_BODY);
		wrapper.finishResponse();
		
		assertThat(response.getHeader("Content-Encoding"), is("gzip"));
		assertThat(response.getContentLengthLong(), is((long) response.getContentAsByteArray().length));
		assertThat(response.getContentLengthLong(), not(equalTo((long) MEDIUM_BODY.length)));
		assertThat(gunzip(response.getContentAsByteArray()), equalTo(MEDIUM_BODY));
	}
	
	@Test
	void shouldAddVaryAcceptEncodingToACompressedResponse() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		wrapper.getOutputStream().write(LARGE_BODY);
		wrapper.finishResponse();
		
		assertThat(response.getHeader("Vary"), containsString("Accept-Encoding"));
	}
	
	@Test
	void shouldAddVaryAcceptEncodingToAResponseThatIsNotCompressed() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		wrapper.getOutputStream().write(SMALL_BODY);
		wrapper.finishResponse();
		
		assertThat(response.getHeader("Vary"), containsString("Accept-Encoding"));
	}
	
	@Test
	void shouldNotAddASecondVaryAcceptEncodingWhenItIsAlreadyAdvertised() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		response.addHeader("Vary", "Accept-Encoding");
		
		new GZIPResponseWrapper(request, response);
		
		assertThat(response.getHeaders("Vary"), hasSize(1));
	}
	
	@Test
	void shouldNotCompressAResponseThatAlreadyHasAContentEncoding() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		wrapper.setHeader("Content-Encoding", "br");
		wrapper.setContentLengthLong(LARGE_BODY.length);
		wrapper.getOutputStream().write(LARGE_BODY);
		wrapper.finishResponse();
		
		assertThat(response.getHeader("Content-Encoding"), is("br"));
		assertThat(response.getContentAsByteArray(), equalTo(LARGE_BODY));
		assertThat(response.getContentLengthLong(), is((long) LARGE_BODY.length));
		assertThat(response.getHeader("Vary"), containsString("Accept-Encoding"));
	}
	
	@Test
	void shouldNotCompressAPartialContentResponse() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		wrapper.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
		wrapper.setContentLengthLong(LARGE_BODY.length);
		wrapper.getOutputStream().write(LARGE_BODY);
		wrapper.finishResponse();
		
		assertThat(response.getHeader("Content-Encoding"), nullValue());
		assertThat(response.getContentAsByteArray(), equalTo(LARGE_BODY));
		assertThat(response.getContentLengthLong(), is((long) LARGE_BODY.length));
		assertThat(response.getHeader("Vary"), containsString("Accept-Encoding"));
	}
	
	@Test
	void shouldNotCompressAHeadResponse() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("HEAD");
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		wrapper.setContentLengthLong(LARGE_BODY.length);
		wrapper.getOutputStream().write(LARGE_BODY);
		wrapper.finishResponse();
		
		assertThat(response.getHeader("Content-Encoding"), nullValue());
		assertThat(response.getContentAsByteArray(), equalTo(LARGE_BODY));
		assertThat(response.getContentLengthLong(), is((long) LARGE_BODY.length));
		assertThat(response.getHeader("Vary"), containsString("Accept-Encoding"));
	}
	
	@Test
	void shouldNotCompressANoContentResponse() throws Exception {
		assertResponseWithStatusIsNotCompressed(HttpServletResponse.SC_NO_CONTENT);
	}
	
	@Test
	void shouldNotCompressANotModifiedResponse() throws Exception {
		assertResponseWithStatusIsNotCompressed(HttpServletResponse.SC_NOT_MODIFIED);
	}
	
	@Test
	void shouldDiscardBufferedOutputOnResetBuffer() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		wrapper.getOutputStream().write("discarded".getBytes(StandardCharsets.UTF_8));
		wrapper.resetBuffer();
		wrapper.getOutputStream().write("kept".getBytes(StandardCharsets.UTF_8));
		wrapper.finishResponse();
		
		assertThat(response.getContentAsByteArray(), equalTo("kept".getBytes(StandardCharsets.UTF_8)));
	}
	
	@Test
	void shouldDiscardWriterOutputOnResetBuffer() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		PrintWriter writer = wrapper.getWriter();
		writer.write("discarded");
		wrapper.resetBuffer();
		writer.write("kept");
		wrapper.finishResponse();
		
		assertThat(response.getContentAsByteArray(), equalTo("kept".getBytes(StandardCharsets.UTF_8)));
	}
	
	@Test
	void shouldDiscardBufferedOutputAndHeadersOnReset() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		wrapper.setHeader("X-Discarded", "yes");
		wrapper.getOutputStream().write("discarded".getBytes(StandardCharsets.UTF_8));
		wrapper.reset();
		wrapper.getOutputStream().write("kept".getBytes(StandardCharsets.UTF_8));
		wrapper.finishResponse();
		
		assertThat(response.getContentAsByteArray(), equalTo("kept".getBytes(StandardCharsets.UTF_8)));
		assertThat(response.getHeader("X-Discarded"), nullValue());
		assertThat(response.getHeader("Vary"), containsString("Accept-Encoding"));
	}
	
	@Test
	void shouldDeliverErrorsSentThroughSendError() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		wrapper.getOutputStream().write("discarded".getBytes(StandardCharsets.UTF_8));
		wrapper.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "boom");
		wrapper.finishResponse();
		
		assertThat(response.getStatus(), is(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
		assertThat(response.getErrorMessage(), is("boom"));
		assertThat(response.getContentAsByteArray(), equalTo(new byte[0]));
		assertThat(response.getHeader("Content-Encoding"), nullValue());
	}

	@Test
	void shouldKeepThePageWhenAnIncludedResourceSendsAnError() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		wrapper.getOutputStream().write(MEDIUM_BODY);

		request.setDispatcherType(DispatcherType.INCLUDE);
		wrapper.sendError(HttpServletResponse.SC_NOT_FOUND);
		request.setDispatcherType(DispatcherType.REQUEST);
		wrapper.finishResponse();

		assertThat(gunzip(response.getContentAsByteArray()), equalTo(MEDIUM_BODY));
	}

	@Test
	void shouldDelegateContentLengthSettersForAnIncludedResource() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		request.setDispatcherType(DispatcherType.INCLUDE);

		wrapper.setContentLength(123);
		assertThat(response.getContentLengthLong(), is(123L));
		response.reset();

		wrapper.setContentLengthLong(234L);
		assertThat(response.getContentLengthLong(), is(234L));
		response.reset();

		wrapper.setHeader("Content-Length", "345");
		assertThat(response.getContentLengthLong(), is(345L));
		response.reset();

		wrapper.addHeader("Content-Length", "456");
		assertThat(response.getContentLengthLong(), is(456L));
		response.reset();

		wrapper.setIntHeader("Content-Length", 567);
		assertThat(response.getContentLengthLong(), is(567L));
		response.reset();

		wrapper.addIntHeader("Content-Length", 678);
		assertThat(response.getContentLengthLong(), is(678L));
	}

	@Test
	void shouldReturnTheCachedWriterAfterTheResponseIsCommitted() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		response.setBufferSize(1);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);

		byte[] page = patternedBody(60_000);
		PrintWriter writer = wrapper.getWriter();
		writer.write(new String(page, StandardCharsets.UTF_8));
		writer.flush();
		wrapper.getWriter().close();

		assertThat(gunzip(response.getContentAsByteArray()), equalTo(page));
	}
	
	/**
	 * A response large enough to start streaming gzip already advertises {@code Content-Encoding:
	 * gzip}. The container writes its error page uncompressed, so that header must not survive
	 * {@code sendError}, or the client would try to gunzip plain HTML.
	 */
	@Test
	void shouldNotAdvertiseGzipForTheErrorPageAfterAStreamingResponseHasStarted() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		wrapper.getOutputStream().write(LARGE_BODY);
		assertThat(response.getHeader("Content-Encoding"), is("gzip"));
		
		wrapper.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "boom");
		wrapper.finishResponse();
		
		assertThat(response.getStatus(), is(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
		assertThat(response.getErrorMessage(), is("boom"));
		assertThat(response.getHeader("Content-Encoding"), nullValue());
		assertThat(response.getHeader("Vary"), containsString("Accept-Encoding"));
	}
	
	@Test
	void shouldKeepCookiesWhenSendErrorAbandonsAStreamingResponse() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		wrapper.addCookie(new Cookie("JSESSIONID", "abc"));
		wrapper.getOutputStream().write(LARGE_BODY);
		wrapper.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "boom");
		
		assertThat(response.getHeader("Content-Encoding"), nullValue());
		assertThat(response.getHeader("Set-Cookie"), containsString("JSESSIONID=abc"));
	}
	
	@Test
	void shouldNotCompressResponsesSmallerThanTheMinimumSize() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		wrapper.setContentLengthLong(SMALL_BODY.length);
		wrapper.getOutputStream().write(SMALL_BODY);
		wrapper.finishResponse();
		
		assertThat(response.getHeader("Content-Encoding"), nullValue());
		assertThat(response.getContentAsByteArray(), equalTo(SMALL_BODY));
		assertThat(response.getContentLengthLong(), is((long) SMALL_BODY.length));
	}
	
	@Test
	void shouldKeepTheContentLengthForAHeadResponseWithNoBody() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("HEAD");
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		wrapper.setContentLengthLong(LARGE_BODY.length);
		wrapper.finishResponse();
		
		assertThat(response.getHeader("Content-Encoding"), nullValue());
		assertThat(response.getContentLengthLong(), is((long) LARGE_BODY.length));
		assertThat(response.getContentAsByteArray(), equalTo(new byte[0]));
	}
	
	@Test
	void shouldKeepThePendingContentLengthWhenResetBufferIsCalled() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		wrapper.setContentLengthLong(1_234);
		wrapper.getOutputStream().write(SMALL_BODY);
		wrapper.resetBuffer();
		wrapper.getOutputStream().write("kept".getBytes(StandardCharsets.UTF_8));
		wrapper.finishResponse();
		
		assertThat(response.getContentAsByteArray(), equalTo("kept".getBytes(StandardCharsets.UTF_8)));
		assertThat(response.getContentLengthLong(), is(1_234L));
	}
	
	@Test
	void shouldProduceAValidReplacementAfterResettingAStreamingResponse() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		wrapper.getOutputStream().write(LARGE_BODY);
		wrapper.resetBuffer();
		wrapper.getOutputStream().write(SMALL_BODY);
		wrapper.finishResponse();
		
		assertThat(response.getHeader("Content-Encoding"), is("gzip"));
		assertThat(gunzip(response.getContentAsByteArray()), equalTo(SMALL_BODY));
	}
	
	@Test
	void shouldProduceAValidUncompressedReplacementAfterResetOfAStreamingResponse() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		wrapper.getOutputStream().write(LARGE_BODY);
		wrapper.reset();
		wrapper.getOutputStream().write(SMALL_BODY);
		wrapper.finishResponse();
		
		assertThat(response.getHeader("Content-Encoding"), nullValue());
		assertThat(response.getContentAsByteArray(), equalTo(SMALL_BODY));
	}
	
	@Test
	void shouldNotSendDiscardedWriterOutputWhenTheBufferWouldOverflow() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		StringBuilder discarded = new StringBuilder(50_001);
		for (int i = 0; i < 50_001; i++) {
			discarded.append('a');
		}
		
		PrintWriter writer = wrapper.getWriter();
		writer.write(discarded.toString());
		wrapper.resetBuffer();
		writer.write("kept");
		wrapper.finishResponse();
		
		assertThat(response.getHeader("Content-Encoding"), nullValue());
		assertThat(response.getContentAsByteArray(), equalTo("kept".getBytes(StandardCharsets.UTF_8)));
	}
	
	@Test
	void shouldDeliverAnErrorWhenTheWriterStillHoldsEnoughToOverflowTheBuffer() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		// commit as soon as anything reaches the response, as a container does once its buffer fills
		response.setBufferSize(1);
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		StringBuilder discarded = new StringBuilder(50_001);
		for (int i = 0; i < 50_001; i++) {
			discarded.append('a');
		}
		
		wrapper.getWriter().write(discarded.toString());
		wrapper.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "boom");
		wrapper.finishResponse();
		
		assertThat(response.getStatus(), is(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
		assertThat(response.getErrorMessage(), is("boom"));
		assertThat(response.getHeader("Content-Encoding"), nullValue());
	}
	
	private void assertLargeResponseDoesNotCarryUncompressedLength(ContentLengthSetter setter) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		setter.set(wrapper, LARGE_BODY.length);
		wrapper.getOutputStream().write(LARGE_BODY);
		wrapper.finishResponse();
		
		assertThat(response.getHeader("Content-Encoding"), is("gzip"));
		assertThat(response.getHeader("Content-Length"), nullValue());
		assertThat(response.getContentLengthLong(), not(equalTo((long) LARGE_BODY.length)));
		assertThat(gunzip(response.getContentAsByteArray()), equalTo(LARGE_BODY));
	}
	
	private void assertResponseWithStatusIsNotCompressed(int status) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		
		wrapper.setStatus(status);
		wrapper.getOutputStream().write(LARGE_BODY);
		wrapper.finishResponse();
		
		assertThat(response.getHeader("Content-Encoding"), nullValue());
		assertThat(response.getContentAsByteArray(), equalTo(LARGE_BODY));
		assertThat(response.getHeader("Vary"), containsString("Accept-Encoding"));
	}
	
	private static byte[] patternedBody(int length) {
		byte[] bytes = new byte[length];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) ('a' + (i % 26));
		}
		return bytes;
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
	
	@FunctionalInterface
	private interface ContentLengthSetter {
		
		void set(GZIPResponseWrapper wrapper, long length);
	}
}

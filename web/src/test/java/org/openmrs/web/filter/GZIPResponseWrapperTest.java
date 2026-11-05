/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 */
package org.openmrs.web.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GZIPResponseWrapperTest {

	@Test
	void shouldKeepThePageWhenAnIncludedResourceSendsAnError() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		byte[] body = "included parent page".getBytes(StandardCharsets.UTF_8);
		wrapper.getOutputStream().write(body);

		request.setDispatcherType(DispatcherType.INCLUDE);
		wrapper.sendError(HttpServletResponse.SC_NOT_FOUND);
		request.setDispatcherType(DispatcherType.REQUEST);
		wrapper.finishResponse();

		assertArrayEquals(body, gunzip(response.getContentAsByteArray()));
	}

	@Test
	void shouldDiscardBufferedPageWhenARequestSendsAnError() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);
		wrapper.getOutputStream().write("discard this".getBytes(StandardCharsets.UTF_8));

		wrapper.sendError(HttpServletResponse.SC_NOT_FOUND, "not found");
		wrapper.finishResponse();

		assertArrayEquals(new byte[0], gunzip(response.getContentAsByteArray()));
		assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
	}

	@Test
	void shouldIgnoreContentLengthForCompressedRequestsButDelegateItForIncludes() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);

		wrapper.setContentLength(60_000);
		assertNull(response.getHeader("Content-Length"));
		wrapper.setContentLengthLong(60_000L);
		assertNull(response.getHeader("Content-Length"));

		request.setDispatcherType(DispatcherType.INCLUDE);
		wrapper.setContentLength(60_000);
		assertEquals("60000", response.getHeader("Content-Length"));
		wrapper.setContentLengthLong(60_000L);
		assertEquals("60000", response.getHeader("Content-Length"));
	}

	@Test
	void shouldFinishTheCompressedBodyWhenTheContainerClosesTheWriterAfterTheResponseIsCommitted() throws Exception {
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

		assertArrayEquals(page, gunzip(response.getContentAsByteArray()));
	}

	private static byte[] patternedBody(int length) {
		byte[] body = new byte[length];
		for (int i = 0; i < body.length; i++) {
			body[i] = (byte) ('a' + i % 26);
		}
		return body;
	}

	private static byte[] gunzip(byte[] compressed) throws Exception {
		try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed));
		        ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			gzip.transferTo(output);
			return output.toByteArray();
		}
	}
}

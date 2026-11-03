package org.openmrs.web.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ZZScratchGzipTest {

	@Test
	void headWithContentLengthButNoBodyShouldKeepContentLength() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("HEAD");
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);

		wrapper.setContentLengthLong(120_000);
		wrapper.finishResponse();

		System.out.println("SCRATCH HEAD contentLengthLong=" + response.getContentLengthLong()
		        + " header=" + response.getHeader("Content-Length"));
		assertThat(response.getContentLengthLong(), is(120_000L));
	}

	@Test
	void getWithContentLengthButNoBodyShouldKeepContentLength() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GZIPResponseWrapper wrapper = new GZIPResponseWrapper(request, response);

		wrapper.setContentLength(0);
		wrapper.finishResponse();

		System.out.println("SCRATCH GET0 contentLengthLong=" + response.getContentLengthLong()
		        + " header=" + response.getHeader("Content-Length"));
		assertThat(response.getContentLengthLong(), is(0L));
	}
}

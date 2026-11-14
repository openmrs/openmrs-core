package org.openmrs.web.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.openmrs.GlobalProperty;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

class PseudoStaticContentControllerTest {

	private PseudoStaticContentController controller;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() {
		controller = new PseudoStaticContentController();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Test
	void handleRequest_shouldReturnSimplePath_whenNoRewritesAndNoJstl() throws Exception {
		// Given
		request.setServletPath("/scripts/app");
		request.setPathInfo(".js");
		controller.setInterpretJstl(false);

		// When
		ModelAndView modelAndView = controller.handleRequest(request, response);

		// Then
		assertEquals("/scripts/app.js", modelAndView.getViewName());
	}

	@Test
	void handleRequest_shouldApplyRewrite_whenPathMatchesRewriteMap() throws Exception {
		// Given
		request.setServletPath("/scripts/jquery/jquery-1.3.2.min.js");
		request.setPathInfo("");
		
		Map<String, String> rewrites = new HashMap<>();
		rewrites.put("/scripts/jquery/jquery-1.3.2.min.js", "/scripts/jquery/jquery.min.js");
		controller.setRewrites(rewrites);
		controller.setInterpretJstl(false);

		// When
		ModelAndView modelAndView = controller.handleRequest(request, response);

		// Then
		assertEquals("/scripts/jquery/jquery.min.js", modelAndView.getViewName());
	}

	@Test
	void handleRequest_shouldAppendWithJstl_whenInterpretJstlIsTrue() throws Exception {
		// Given
		request.setServletPath("/scripts/main");
		request.setPathInfo(".js");
		controller.setInterpretJstl(true);

		// When
		ModelAndView modelAndView = controller.handleRequest(request, response);

		// Then
		assertEquals("/scripts/main.js.withjstl", modelAndView.getViewName());
	}

	@Test
	void getLastModified_shouldReturnMinusOne_whenInterpretJstlIsFalse() {
		// Given
		controller.setInterpretJstl(false);

		// When
		long lastModified = controller.getLastModified(request);

		// Then
		assertEquals(-1, lastModified);
	}

	@Test
	void getLastModified_shouldReturnTimestamp_whenInterpretJstlIsTrue() {
		// Given
		controller.setInterpretJstl(true);

		// When
		long lastModified = controller.getLastModified(request);

		// Then
		assertTrue(lastModified > 0);
	}

	@Test
	void globalPropertyChanged_shouldUpdateLastModifiedTimestamp() {
		// Given
		controller.setInterpretJstl(true);
		long initialLastModified = controller.getLastModified(request);

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
		}

		// When
		controller.globalPropertyChanged(new GlobalProperty("some.property", "value"));
		long updatedLastModified = controller.getLastModified(request);

		// Then
		assertTrue(updatedLastModified > initialLastModified, "Timestamp should be updated on global property change");
	}

	@Test
	void globalPropertyDeleted_shouldUpdateLastModifiedTimestamp() {
		// Given
		controller.setInterpretJstl(true);
		long initialLastModified = controller.getLastModified(request);

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
		}

		// When
		controller.globalPropertyDeleted("some.property");
		long updatedLastModified = controller.getLastModified(request);

		// Then
		assertTrue(updatedLastModified > initialLastModified, "Timestamp should be updated on global property deletion");
	}

	@Test
	void supportsPropertyName_shouldAlwaysReturnTrue() {
		assertTrue(controller.supportsPropertyName("any.property.name"));
	}
}

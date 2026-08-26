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

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.ConfigUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Tests the way {@link GZIPFilter} reads the global properties that control it, and the compiled
 * accepted-path patterns it holds on to. These are plain unit tests rather than context-sensitive
 * ones so that a global property read can be made to fail on demand; the filter's behaviour against
 * a real service is covered by {@link GZIPFilterTest}.
 */
class GZIPFilterCachingTest {

	private static final String ACCEPT_PATHS = OpenmrsConstants.GLOBAL_PROPERTY_GZIP_ACCEPT_COMPRESSED_REQUESTS_FOR_PATHS;

	private static final String GZIP_ENABLED = OpenmrsConstants.GLOBAL_PROPERTY_GZIP_ENABLED;

	private MockedStatic<Context> contextMock;

	private MockedStatic<ConfigUtil> configUtilMock;

	private GZIPFilter filter;

	@BeforeEach
	void setUp() {
		contextMock = mockStatic(Context.class);
		contextMock.when(Context::isSessionOpen).thenReturn(true);
		configUtilMock = mockStatic(ConfigUtil.class);
		filter = new GZIPFilter();
	}

	@AfterEach
	void tearDown() {
		configUtilMock.close();
		contextMock.close();
	}

	@Test
	void shouldAcceptCompressedRequestForPathMatchingOneOfTheConfiguredPatterns() throws Exception {
		givenGlobalProperty(ACCEPT_PATHS, "/openmrs/ws/rest/.*,/openmrs/ms/.*");

		assertTrue(isCompressedRequestAccepted("/openmrs/ws/rest/v1/session"));
		assertTrue(isCompressedRequestAccepted("/openmrs/ms/somemodule"));
	}

	@Test
	void shouldRejectCompressedRequestForPathMatchingNoConfiguredPattern() throws Exception {
		givenGlobalProperty(ACCEPT_PATHS, "/openmrs/ws/rest/.*,/openmrs/ms/.*");

		assertFalse(isCompressedRequestAccepted("/openmrs/index.htm"));
	}

	@Test
	void shouldRejectCompressedRequestWhenPatternOnlyMatchesPartOfThePath() throws Exception {
		// the patterns have always been applied as full matches rather than as substring searches
		givenGlobalProperty(ACCEPT_PATHS, "/ws/rest/.*");

		assertFalse(isCompressedRequestAccepted("/openmrs/ws/rest/v1/session"));
	}

	@Test
	void shouldRejectCompressedRequestWhenNoPathsAreConfigured() throws Exception {
		givenGlobalProperty(ACCEPT_PATHS, null);

		assertFalse(isCompressedRequestAccepted("/openmrs/ws/rest/v1/session"));
	}

	@Test
	void shouldRejectCompressedRequestWhenThePathsPropertyCannotBeRead() throws Exception {
		configUtilMock.when(() -> ConfigUtil.getGlobalProperty(ACCEPT_PATHS))
		        .thenThrow(new APIException("service unavailable"));

		assertFalse(isCompressedRequestAccepted("/openmrs/ws/rest/v1/session"));
	}

	/**
	 * A pattern that will not compile is logged and skipped, so it costs only itself: the entries
	 * around it keep matching wherever they appear in the list. The loop this replaced threw at the bad
	 * entry and denied the path without reaching the entries after it, so one typo silently disabled
	 * everything that followed it.
	 */
	@Test
	void shouldStillAcceptPathsConfiguredBeforeAPatternThatIsNotAValidRegex() throws Exception {
		givenGlobalProperty(ACCEPT_PATHS, "/openmrs/ws/rest/.*,[unclosed,/openmrs/ms/.*");

		assertTrue(isCompressedRequestAccepted("/openmrs/ws/rest/v1/session"));
	}

	@Test
	void shouldStillAcceptPathsConfiguredAfterAPatternThatIsNotAValidRegex() throws Exception {
		givenGlobalProperty(ACCEPT_PATHS, "/openmrs/ws/rest/.*,[unclosed,/openmrs/ms/.*");

		assertTrue(isCompressedRequestAccepted("/openmrs/ms/somemodule"));
	}

	@Test
	void shouldStillAcceptPathsWhenTheFirstConfiguredPatternIsNotAValidRegex() throws Exception {
		givenGlobalProperty(ACCEPT_PATHS, "[unclosed,/openmrs/ws/rest/.*");

		assertTrue(isCompressedRequestAccepted("/openmrs/ws/rest/v1/session"));
	}

	@Test
	void shouldRejectEveryPathWhenNoConfiguredPatternIsAValidRegex() throws Exception {
		givenGlobalProperty(ACCEPT_PATHS, "[unclosed,(also bad");

		assertFalse(isCompressedRequestAccepted("/openmrs/ws/rest/v1/session"));
	}

	@Test
	void shouldRejectAPathMatchedByNoneOfTheValidPatternsAlongsideAnInvalidOne() throws Exception {
		givenGlobalProperty(ACCEPT_PATHS, "/openmrs/ws/rest/.*,[unclosed");

		assertFalse(isCompressedRequestAccepted("/openmrs/index.htm"));
	}

	/**
	 * The servlet spec says a request URI is never null, but the loop this replaced was wrapped in a
	 * catch-all that turned any surprise into a rejection rather than a 500.
	 */
	@Test
	void shouldRejectACompressedRequestWithNoRequestURI() {
		// deliberately no allowlist configured: a path that cannot be matched at all is rejected before
		// the property is consulted
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getHeader("Content-encoding")).thenReturn("gzip");
		when(request.getRequestURI()).thenReturn(null);

		assertThrows(APIException.class, () -> filter.performGZIPRequest(request));
	}

	/**
	 * This is the point of the change: the property is read on every request, because ConfigUtil
	 * answers that from its own cache, but the regexes behind it are compiled only once.
	 */
	@Test
	void shouldCompileTheConfiguredPatternsOnlyOnce() throws Exception {
		givenGlobalProperty(ACCEPT_PATHS, "/openmrs/ws/rest/.*");

		assertTrue(isCompressedRequestAccepted("/openmrs/ws/rest/v1/session"));
		Pattern[] compiled = getCompiledPatterns();
		assertThat(compiled, notNullValue());

		assertTrue(isCompressedRequestAccepted("/openmrs/ws/rest/v1/patient"));
		assertTrue(isCompressedRequestAccepted("/openmrs/ws/rest/v1/visit"));

		assertThat(getCompiledPatterns(), sameInstance(compiled));
		assertThat(getCompiledPatterns()[0], sameInstance(compiled[0]));
		configUtilMock.verify(() -> ConfigUtil.getGlobalProperty(ACCEPT_PATHS), times(3));
	}

	@Test
	void shouldRecompileThePatternsWhenThePropertyChanges() throws Exception {
		givenGlobalProperty(ACCEPT_PATHS, "/openmrs/ws/rest/.*");

		assertTrue(isCompressedRequestAccepted("/openmrs/ws/rest/v1/session"));
		Pattern[] compiled = getCompiledPatterns();

		givenGlobalProperty(ACCEPT_PATHS, "/openmrs/ms/.*");

		assertFalse(isCompressedRequestAccepted("/openmrs/ws/rest/v1/session"));
		assertTrue(isCompressedRequestAccepted("/openmrs/ms/somemodule"));
		assertThat(getCompiledPatterns(), not(sameInstance(compiled)));
	}

	@Test
	void shouldNotCompressWhenTheEnabledFlagIsFalse() throws Exception {
		givenGlobalProperty(GZIP_ENABLED, "false");

		assertFalse(isResponseCompressed());
	}

	@Test
	void shouldCompressWhenTheEnabledFlagIsTrue() throws Exception {
		givenGlobalProperty(GZIP_ENABLED, "true");

		assertTrue(isResponseCompressed());
	}

	/**
	 * Whether the response is compressed is decided by what the client says it accepts, and nothing
	 * else about the client.
	 */
	@Test
	void shouldNotCompressWhenTheClientDoesNotAdvertiseGzipSupport() throws Exception {
		givenGlobalProperty(GZIP_ENABLED, "true");

		assertFalse(isResponseCompressed(null));
		assertFalse(isResponseCompressed("deflate"));
	}

	@Test
	void shouldPickUpAChangeToTheEnabledFlag() throws Exception {
		givenGlobalProperty(GZIP_ENABLED, "false");
		assertFalse(isResponseCompressed());

		givenGlobalProperty(GZIP_ENABLED, "true");
		assertTrue(isResponseCompressed());
	}

	@Test
	void shouldNotCompressWhenTheEnabledFlagCannotBeRead() throws Exception {
		configUtilMock.when(() -> ConfigUtil.getGlobalProperty(GZIP_ENABLED))
		        .thenThrow(new APIException("service unavailable"));

		assertFalse(isResponseCompressed());
	}

	@Test
	void shouldNotReadAnyGlobalPropertyWhenNoSessionIsOpen() throws Exception {
		contextMock.when(Context::isSessionOpen).thenReturn(false);

		assertFalse(isResponseCompressed());
		assertFalse(isCompressedRequestAccepted("/openmrs/ws/rest/v1/session"));

		configUtilMock.verify(() -> ConfigUtil.getGlobalProperty(any()), times(0));
	}

	/**
	 * The filter runs for requests from clients that have not authenticated, and reading a global
	 * property is privileged, so the read has to be proxied.
	 */
	@Test
	void shouldProxyTheGetGlobalPropertiesPrivilegeAroundASuccessfulRead() throws Exception {
		givenGlobalProperty(GZIP_ENABLED, "true");

		assertTrue(isResponseCompressed());

		contextMock.verify(() -> Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES), times(1));
		contextMock.verify(() -> Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES), times(1));
	}

	/**
	 * Container threads are pooled, so a privilege left behind by a failed read would be inherited by
	 * whatever request lands on the thread next.
	 */
	@Test
	void shouldReleaseTheGetGlobalPropertiesPrivilegeWhenAReadFails() throws Exception {
		configUtilMock.when(() -> ConfigUtil.getGlobalProperty(GZIP_ENABLED))
		        .thenThrow(new APIException("service unavailable"));

		assertFalse(isResponseCompressed());

		contextMock.verify(() -> Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES), times(1));
		contextMock.verify(() -> Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES), times(1));
	}

	private void givenGlobalProperty(String propertyName, String value) {
		configUtilMock.when(() -> ConfigUtil.getGlobalProperty(propertyName)).thenReturn(value);
	}

	/**
	 * @return the patterns the filter is currently holding, or null if it has not compiled any yet.
	 *         Read reflectively because there is no other way to tell a reused set of compiled patterns
	 *         from a freshly compiled one.
	 */
	private Pattern[] getCompiledPatterns() throws Exception {
		Field acceptedPathPatterns = GZIPFilter.class.getDeclaredField("acceptedPathPatterns");
		acceptedPathPatterns.setAccessible(true);
		Object patternHolder = acceptedPathPatterns.get(filter);
		if (patternHolder == null) {
			return null;
		}

		Field patterns = patternHolder.getClass().getDeclaredField("patterns");
		patterns.setAccessible(true);
		return (Pattern[]) patterns.get(patternHolder);
	}

	/**
	 * Sends a gzipped request body for the given path through the filter.
	 *
	 * @return true if the filter passed the request on, false if it answered 415 Unsupported Media Type
	 */
	private boolean isCompressedRequestAccepted(String requestURI) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI(requestURI);
		request.addHeader("Content-encoding", "gzip");
		request.setContent(gzip("message string"));

		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilterInternal(request, response, mock(FilterChain.class));

		return response.getStatus() != HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
	}

	/**
	 * Sends a request that advertises gzip support through the filter.
	 *
	 * @return true if the filter wrapped the response for compression
	 */
	private boolean isResponseCompressed() throws Exception {
		return isResponseCompressed("gzip");
	}

	/**
	 * Sends a request through the filter, advertising the given accept-encoding, or none at all when it
	 * is null.
	 *
	 * @return true if the filter wrapped the response for compression
	 */
	private boolean isResponseCompressed(String acceptEncoding) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		if (acceptEncoding != null) {
			request.addHeader("accept-encoding", acceptEncoding);
		}

		MockHttpServletResponse response = new MockHttpServletResponse();
		RecordingFilterChain chain = new RecordingFilterChain();
		filter.doFilterInternal(request, response, chain);

		assertThat("the filter must always hand the request on", chain.wasCalled, is(true));
		return chain.responseWasWrapped;
	}

	private static byte[] gzip(String content) throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (GZIPOutputStream gzipStream = new GZIPOutputStream(bytes)) {
			gzipStream.write(content.getBytes(StandardCharsets.UTF_8));
		}
		return bytes.toByteArray();
	}

	/**
	 * Records whether the filter handed on a {@link GZIPResponseWrapper}, which is the only external
	 * sign that the filter decided compression was enabled.
	 */
	private static final class RecordingFilterChain implements FilterChain {

		private boolean wasCalled = false;

		private boolean responseWasWrapped = false;

		@Override
		public void doFilter(ServletRequest request, ServletResponse response) {
			wasCalled = true;
			responseWasWrapped = response instanceof GZIPResponseWrapper;
		}
	}
}

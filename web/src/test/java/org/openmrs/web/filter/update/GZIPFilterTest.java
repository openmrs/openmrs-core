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

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.web.filter.GZIPFilter;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests some of the methods on the {@link org.openmrs.web.filter.update.GZIPFilter}
 */
public class GZIPFilterTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see org.openmrs.web.filter.GZIPFilter#doFilterInternal(HttpServletRequest,HttpServletResponse, javax.servlet.FilterChain)
	 */
	@Test
	public void zipRequestWrapperTest_shouldReturnTrueIfUnzippedContentReadFromWrapperIsTheSameAsContentBeforeZipping()
	        throws Exception {
		GlobalProperty property = new GlobalProperty("gzip.acceptCompressedRequestsForPaths", ".*");
		
		Context.getAdministrationService().saveGlobalProperty(property);
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.setContextPath("http://gzipservletpath");
		req.addHeader("Content-encoding", "gzip");
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		GZIPOutputStream gzOutput = new GZIPOutputStream(stream);
		PrintWriter pwriter = new PrintWriter(gzOutput);
		pwriter.write("message string");
		pwriter.flush();
		gzOutput.finish();
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
			
			Assert.assertThat(outputMessage, is("message string"));
		}
		catch (IOException e) {
			throw new RuntimeException();
		}
		
	}
	
}

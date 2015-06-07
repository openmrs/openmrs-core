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

import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Wraps Request for GZipFilter
 *
 */
public class GZIPRequestWrapper extends HttpServletRequestWrapper {
	
	protected ServletInputStream stream = null;
	
	public GZIPRequestWrapper(HttpServletRequest request) throws IOException {
		super(request);
		stream = new GZIPRequestStream(request);
	}
	
	@Override
	public ServletInputStream getInputStream() throws IOException {
		return stream;
	}
	
}

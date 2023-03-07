/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.stereotype.Component;

@Component
public class BaseUriSetup {
	
	public void setup(HttpServletRequest request) {
		if (!RestConstants.URI_PREFIX.startsWith("http://") && !RestConstants.URI_PREFIX.startsWith("https://")) {
			StringBuilder uri = new StringBuilder();
			uri.append(request.getScheme()).append("://").append(request.getServerName());
			if (request.getServerPort() != 80) {
				uri.append(":").append(request.getServerPort());
			}
			if (!StringUtils.isBlank(request.getContextPath())) {
				uri.append(request.getContextPath());
			}
			
			RestConstants.URI_PREFIX = uri.toString() + RestConstants.URI_PREFIX;
		}
	}
}

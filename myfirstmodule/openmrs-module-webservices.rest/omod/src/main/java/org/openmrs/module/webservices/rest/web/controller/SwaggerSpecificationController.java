/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.controller;

import com.google.common.net.HttpHeaders;
import io.swagger.models.Scheme;
import org.openmrs.module.webservices.docs.swagger.SwaggerSpecificationCreator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller("SwaggerSpecificationController")
@RequestMapping("/module/webservices/rest/swagger.json")
public class SwaggerSpecificationController {
	
	@RequestMapping(method = RequestMethod.GET)
	public @ResponseBody
	String getSwaggerSpecification(HttpServletRequest request) throws Exception {
		
		String host = request.getHeader(HttpHeaders.HOST);
		
		String scheme = request.getHeader(HttpHeaders.X_FORWARDED_PROTO);
		if (scheme == null) {
			scheme = request.getScheme();
		}
		
		return new SwaggerSpecificationCreator()
		        .host(host)
		        .basePath(request.getContextPath() + "/ws/rest/v1")
		        .scheme(Scheme.forValue(scheme))
		        
		        .getJSON();
	}
	
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest;

import java.util.List;

import javax.servlet.ServletContext;

import org.openmrs.annotation.OpenmrsProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;

/**
 * We should not need to apply this fix in versions where TRUNK-5022 is fixed.
 */
@OpenmrsProfile(openmrsPlatformVersion = "1.9.* - 2.3.*")
public class InitPathMatcher implements ServletContextAware {
	
	@Autowired
	private AnnotationMethodHandlerAdapter handlerAdapter;
	
	@Autowired
	private List<DefaultAnnotationHandlerMapping> handlerMappings;
	
	private OpenmrsPathMatcher pathMatcher = new OpenmrsPathMatcher();
	
	@Override
	public void setServletContext(ServletContext servletContext) {
		for (DefaultAnnotationHandlerMapping handlerMapping : handlerMappings) {
			handlerMapping.setPathMatcher(pathMatcher);
		}
		
		handlerAdapter.setPathMatcher(pathMatcher);
	}
}

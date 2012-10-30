/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.web.WebModuleUtil;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator;

/**
 * A {@link RequestToViewNameTranslator} that is able to translate view names returned by modules to
 * be able to locate their jsps and other resources based on the file structure used by the module
 * engine. E.g if a module has a controller that has a mapping to
 * 'module/org.openmrs.module.mymoduleId/some.jsp' or 'module/mymoduleId/some.jsp' and has no return
 * value, this translator converts it to 'module/org/openmrs/module/mymoduleId/some.jsp'
 */
public class OpenmrsRequestToViewNameTranslator extends DefaultRequestToViewNameTranslator {
	
	protected final Log log = LogFactory.getLog(OpenmrsRequestToViewNameTranslator.class);
	
	/**
	 * @see org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator#getViewName(javax.servlet.http.HttpServletRequest)
	 * @should return the correct view name for a module url using a module id
	 * @should return the correct view name for a module url using a package name
	 * @should ignore a url in core
	 */
	@Override
	public String getViewName(HttpServletRequest request) {
		String viewName = super.getViewName(request);
		if (viewName != null && viewName.startsWith(WebModuleUtil.MODULE_VIEW_NAME_PREFIX))
			viewName = WebModuleUtil.transformModuleViewName(viewName);
		
		return viewName;
	}
}

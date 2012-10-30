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

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.web.WebModuleUtil;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * An {@link InternalResourceViewResolver} that is able to resolve view names returned by modules to
 * be able to locate their jsps and other resources based on the file structure used by the module
 * engine. E.g if a module has a controller that returns the view name
 * 'module/org.openmrs.module.mymoduleId/some.jsp' or 'module/mymoduleId/some.jsp' this resolver
 * first converts the view name to 'module/org/openmrs/module/mymoduleId/some.jsp'
 */
public class OpenmrsInternalResourceViewResolver extends InternalResourceViewResolver {
	
	protected final Log log = LogFactory.getLog(OpenmrsInternalResourceViewResolver.class);
	
	/**
	 * @see org.springframework.web.servlet.view.AbstractCachingViewResolver#resolveViewName(java.lang.String,
	 *      java.util.Locale)
	 */
	@Override
	public View resolveViewName(String viewName, Locale locale) throws Exception {
		//TODO ensure this is not called multiple times
		if (viewName != null && viewName.startsWith(WebModuleUtil.MODULE_VIEW_NAME_PREFIX)) {
			viewName = WebModuleUtil.transformModuleViewName(viewName);
		}
		
		return super.resolveViewName(viewName, locale);
	}
}

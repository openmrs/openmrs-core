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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.webservices.docs.swagger.SwaggerSpecificationCreator;
import org.openmrs.module.webservices.rest.util.ReflectionUtil;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.api.RestService;

/**
 * {@link ModuleActivator} for the webservices.rest module
 */
public class Activator extends BaseModuleActivator {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void started() {
		log.info("Started the REST Web Service module");
	}
	
	@Override
	public void stopped() {
		log.info("Stopped the REST Web Service module");
	}
	
	@Override
	public void contextRefreshed() {
		// initialize all resources and search handlers
		Context.getService(RestService.class).initialize();
		
		log.info("Clearing caches...");
		
		ConversionUtil.clearCache();
		ReflectionUtil.clearCaches();
		SwaggerSpecificationCreator.clearCache();
	}
	
}

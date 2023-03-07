/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_0;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.openmrs.OpenmrsObject;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller("webservices.rest.DbCacheController")
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/cleardbcache", method = RequestMethod.POST)
public class ClearDbCacheController2_0 extends BaseRestController {
	
	private static final Logger log = LoggerFactory.getLogger(ClearDbCacheController2_0.class);
	
	private RestService restService;
	
	@Autowired
	public ClearDbCacheController2_0(RestService restService) {
		this.restService = restService;
	}
	
	@RequestMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void clearDbCache(@RequestBody(required = false) String json) throws Exception {
		String resourceName = null;
		String subResourceName = null;
		String uuid = null;
		if (StringUtils.isNotBlank(json)) {
			SimpleObject simpleObject = new ObjectMapper().readValue(json, SimpleObject.class);
			resourceName = simpleObject.get("resource");
			subResourceName = simpleObject.get("subResource");
			uuid = simpleObject.get("uuid");
		}
		
		//TODO Replace this logic with the methods added as part of https://issues.openmrs.org/browse/TRUNK-6047
		SessionFactory sf = Context.getRegisteredComponents(SessionFactory.class).get(0);
		if (StringUtils.isBlank(resourceName)) {
			if (log.isDebugEnabled()) {
				log.debug("Clearing DB cache via REST");
			}
			
			sf.getCache().evictAllRegions();
		} else {
			if (StringUtils.isNotBlank(subResourceName)) {
				resourceName += ("/" + subResourceName);
			}
			
			Resource resource = restService.getResourceByName(buildResourceName(resourceName));
			Class<?> supportedClass = RestUtil.getSupportedClass(resource);
			if (StringUtils.isBlank(uuid)) {
				if (log.isDebugEnabled()) {
					log.debug("Clearing DB cache via REST for resource: {} ({})", resourceName, supportedClass);
				}
				
				sf.getCache().evictEntityRegion(supportedClass);
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Clearing DB cache via REST for resource: {} ({}) with uuid: {}",
					    new Object[] { resourceName, supportedClass, uuid });
				}
				
				OpenmrsObject object = (OpenmrsObject) ((BaseDelegatingResource) resource).getByUniqueId(uuid);
				if ("user".equals(resourceName)) {
					supportedClass = User.class;
				}
				
				if (object == null) {
					log.info("No {} found with uuid: {}", supportedClass.getSimpleName(), uuid);
					return;
				}
				
				sf.getCache().evictEntity(supportedClass, object.getId());
			}
			
			sf.getCache().evictCollectionRegions();
			sf.getCache().evictQueryRegions();
		}
	}
	
}

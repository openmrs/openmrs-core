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

@Controller("webservices.rest.searchIndexController2_0")
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/searchindexupdate", method = RequestMethod.POST)
public class SearchIndexController2_0 extends BaseRestController {
	
	private static final Logger log = LoggerFactory.getLogger(SearchIndexController2_0.class);
	
	@Autowired
	private RestService restService;
	
	@RequestMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateSearchIndex(@RequestBody(required = false) String json) throws Exception {
		String resourceName = null;
		String subResourceName = null;
		boolean async = false;
		String uuid = null;
		if (StringUtils.isNotBlank(json)) {
			SimpleObject simpleObject = new ObjectMapper().readValue(json, SimpleObject.class);
			resourceName = simpleObject.get("resource");
			subResourceName = simpleObject.get("subResource");
			uuid = simpleObject.get("uuid");
			if (simpleObject.get("async") != null) {
				async = simpleObject.get("async");
			}
		}
		
		if (StringUtils.isBlank(resourceName)) {
			if (log.isDebugEnabled()) {
				log.debug("Updating search index via REST" + (async ? " asynchronously" : ""));
			}
			
			if (async) {
				Context.updateSearchIndexAsync();
			} else {
				Context.updateSearchIndex();
			}
		} else {
			if (StringUtils.isNotBlank(subResourceName)) {
				resourceName += ("/" + subResourceName);
			}
			
			Resource resource = restService.getResourceByName(buildResourceName(resourceName));
			Class<?> supportedClass = RestUtil.getSupportedClass(resource);
			if (StringUtils.isBlank(uuid)) {
				log.debug("Updating search index via REST for resource: {} ({})", resourceName, supportedClass);
				
				Context.updateSearchIndexForType(supportedClass);
			} else {
				log.debug("Updating search index via REST for resource: {} with uuid: {}", resourceName, uuid);
				
				Object object = ((BaseDelegatingResource) resource).getByUniqueId(uuid);
				Context.updateSearchIndexForObject(object);
			}
		}
	}
	
}

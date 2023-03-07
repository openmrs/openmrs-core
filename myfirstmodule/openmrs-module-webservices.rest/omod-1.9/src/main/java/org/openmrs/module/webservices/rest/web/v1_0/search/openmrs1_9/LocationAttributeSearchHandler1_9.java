/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.search.openmrs1_9;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.api.SubResourceSearchHandler;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class LocationAttributeSearchHandler1_9 implements SubResourceSearchHandler {
	
	private static final SearchConfig SEARCH_CONFIG = new SearchConfig("default", RestConstants.VERSION_1
	        + "/location/attribute", Collections.singletonList("1.9.* - 2.0.*"), new SearchQuery.Builder(
	        "Allows you to find attributes by attribute type").withRequiredParameters("attributeType").build());
	
	@Override
	public SearchConfig getSearchConfig() {
		return SEARCH_CONFIG;
	}
	
	@Override
	public PageableResult search(RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("Cannot search for location attributes without parent location");
	}
	
	@Override
	public PageableResult search(String parentUuid, RequestContext context) throws ResponseException {
		String attributeType = context.getParameter("attributeType");
		
		if (StringUtils.isBlank(attributeType) || StringUtils.isBlank(parentUuid)) {
			return new EmptySearchResult();
		}
		
		Location parentLocation = Context.getLocationService().getLocationByUuid(parentUuid);
		List<LocationAttribute> results = new ArrayList<LocationAttribute>();
		for (LocationAttribute activeAttribute : parentLocation.getActiveAttributes()) {
			if (activeAttribute.getAttributeType().getUuid().equals(attributeType)) {
				results.add(activeAttribute);
			}
		}
		return new AlreadyPaged<LocationAttribute>(context, results, false, Long.valueOf(results.size()));
	}
}

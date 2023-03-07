/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.search.openmrs1_8;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.stereotype.Component;

@Component
public class LocationSearchHandler implements SearchHandler {
	
	private static final String VIEW_LOCATIONS = "View Locations";
	
	private final SearchConfig searchConfig = new SearchConfig("default", RestConstants.VERSION_1 + "/location",
			Collections.singletonList("1.8.* - 9.*"),
	        new SearchQuery.Builder(
	                "Allows you to find locations by tag uuid or tag name").withRequiredParameters("tag").build());
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.SearchHandler#getSearchConfig()
	 * <strong>Should</strong> return location by tag uuid
	 * <strong>Should</strong> return location by tag name
	 */
	@Override
	public SearchConfig getSearchConfig() {
		return searchConfig;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.SearchHandler#search(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public PageableResult search(RequestContext context) throws ResponseException {
		String tag = context.getRequest().getParameter("tag");
		
		List<Location> locations = new ArrayList<Location>();
		try {
			Context.addProxyPrivilege(VIEW_LOCATIONS); //Not using PrivilegeConstants.VIEW_LOCATIONS which was removed in platform 1.11+
			Context.addProxyPrivilege("Get Locations"); //1.11+
			
			LocationTag locationTag = Context.getLocationService().getLocationTagByUuid(tag);
			if (locationTag == null) {
				locationTag = Context.getLocationService().getLocationTagByName(tag);
			}
			
			if (locationTag != null) {
				locations = Context.getLocationService().getLocationsByTag(locationTag);
			}
		}
		finally {
			Context.removeProxyPrivilege(VIEW_LOCATIONS); //Not using PrivilegeConstants.VIEW_LOCATIONS which was removed in platform 1.11+
			Context.removeProxyPrivilege("Get Locations"); //1.11+
		}
		
		return new NeedsPaging<Location>(locations, context);
	}
}

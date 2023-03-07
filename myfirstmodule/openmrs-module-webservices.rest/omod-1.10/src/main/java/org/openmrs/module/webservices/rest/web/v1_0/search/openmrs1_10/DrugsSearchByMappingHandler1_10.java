/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.search.openmrs1_10;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptSource;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Allows finding drugs by mapping
 */
@Component
public class DrugsSearchByMappingHandler1_10 implements SearchHandler {
	
	public static final String REQUEST_PARAM_CODE = "code";
	
	public static final String REQUEST_PARAM_SOURCE = "source";
	
	public static final String REQUEST_PARAM_MAP_TYPES = "preferredMapTypes";
	
	@Autowired
	@Qualifier("conceptService")
	ConceptService conceptService;
	
	SearchQuery searchQuery = new SearchQuery.Builder(
	        "Allows you to find drugs by source, code and preferred map types(comma delimited). "
	                + "Gets the best matching drug, i.e. matching the earliest ConceptMapType passed if there are "
	                + "multiple matches for the highest-priority ConceptMapType")
	        .withRequiredParameters(REQUEST_PARAM_SOURCE)
	        .withOptionalParameters(REQUEST_PARAM_CODE, REQUEST_PARAM_MAP_TYPES).build();
	
	private final SearchConfig searchConfig = new SearchConfig("getDrugsByMapping", RestConstants.VERSION_1 + "/drug",
			Collections.singletonList("1.10.* - 9.*"), searchQuery);
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.SearchHandler#getSearchConfig()
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
		String code = context.getParameter(REQUEST_PARAM_CODE);
		String sourceUuid = context.getParameter(REQUEST_PARAM_SOURCE);
		String mapTypesUuids = context.getParameter(REQUEST_PARAM_MAP_TYPES);
		ConceptSource source = null;
		if (StringUtils.isNotBlank(sourceUuid)) {
			source = conceptService.getConceptSourceByUuid(sourceUuid);
			if (source == null) {
				throw new ObjectNotFoundException();
			}
		}
		
		List<ConceptMapType> mapTypesInOrderOfPreference = null;
		if (StringUtils.isNotBlank(mapTypesUuids)) {
			String[] uuids = StringUtils.split(mapTypesUuids, ",");
			for (String uuid : uuids) {
				ConceptMapType mapType = conceptService.getConceptMapTypeByUuid(uuid.trim());
				if (mapType == null) {
					throw new ObjectNotFoundException();
				}
				if (mapTypesInOrderOfPreference == null) {
					mapTypesInOrderOfPreference = new ArrayList<ConceptMapType>();
				}
				mapTypesInOrderOfPreference.add(mapType);
			}
		}
		
		List<Drug> drugs = conceptService.getDrugsByMapping(code, source, mapTypesInOrderOfPreference,
		    context.getIncludeAll());
		return new NeedsPaging<Drug>(drugs, context);
	}
}

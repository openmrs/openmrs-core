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

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.InvalidSearchException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.util.LocaleUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Find drugs that match the specified search phrase. The logic matches on drug names, concept names
 * of the associated concepts or the concept reference term codes of the drug reference term
 * mappings.
 */
@Component
public class DrugSearchHandler1_10 implements SearchHandler {
	
	public static final String REQUEST_PARAM_QUERY = "q";
	
	public static final String REQUEST_PARAM_LOCALE = "locale";
	
	public static final String REQUEST_PARAM_EXACT_LOCALE = "exactLocale";
	
	@Autowired
	@Qualifier("conceptService")
	ConceptService conceptService;
	
	SearchQuery searchQuery = new SearchQuery.Builder("Allows you to search for drugs, it matches on drug"
	        + " names, concept names of the associated concepts or the concept reference term codes of the"
	        + " drug reference term mappings").withRequiredParameters(REQUEST_PARAM_QUERY)
	        .withOptionalParameters(REQUEST_PARAM_LOCALE, REQUEST_PARAM_EXACT_LOCALE).build();
	
	private final SearchConfig searchConfig = new SearchConfig("default", RestConstants.VERSION_1 + "/drug",
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
		String query = context.getParameter(REQUEST_PARAM_QUERY);
		String localeString = context.getParameter(REQUEST_PARAM_LOCALE);
		String exactLocaleString = context.getParameter(REQUEST_PARAM_EXACT_LOCALE);
		Locale locale = null;
		boolean exactLocale = false;
		if (StringUtils.isNotBlank(localeString)) {
			locale = LocaleUtility.fromSpecification(localeString);
			if (locale == null) {
				throw new InvalidSearchException("Unknown locale:" + localeString);
			}
		}
		
		if (StringUtils.isNotBlank(exactLocaleString)) {
			exactLocale = Boolean.valueOf(exactLocaleString);
		}
		
		List<Drug> drugs = conceptService.getDrugs(query, locale, exactLocale, context.getIncludeAll());
		return new NeedsPaging<Drug>(drugs, context);
	}
}

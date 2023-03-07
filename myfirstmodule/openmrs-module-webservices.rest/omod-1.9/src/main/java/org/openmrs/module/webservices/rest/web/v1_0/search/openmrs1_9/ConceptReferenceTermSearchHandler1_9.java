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
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.response.InvalidSearchException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Allows you to find terms by source and code or name.
 */
@Component
public class ConceptReferenceTermSearchHandler1_9 implements SearchHandler {
	
	@Autowired
	@Qualifier("conceptService")
	ConceptService conceptService;
	
	//default search type
	private static String SEARCH_TYPE_EQUAL = "equal";
	
	private static String SEARCH_TYPE_ALIKE = "alike";
	
	private final SearchConfig searchConfig = new SearchConfig("default", RestConstants.VERSION_1 + "/conceptreferenceterm",
			Collections.singletonList("1.9.* - 9.*"),
	        new SearchQuery.Builder(
	                "Allows you to find terms by source and code or name").withOptionalParameters("source", "codeOrName",
	            "searchType").build());
	
	@Override
	public SearchConfig getSearchConfig() {
		return searchConfig;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.SearchHandler#search(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public PageableResult search(RequestContext context) throws ResponseException {
		String source = context.getParameter("source");
		String codeOrName = context.getParameter("codeOrName");
		String searchType = context.getParameter("searchType");
		
		if (searchType == null) {
			searchType = SEARCH_TYPE_EQUAL;
		}
		
		ConceptSource conceptSource = null;
		if (source != null) {
			conceptSource = conceptService.getConceptSourceByUuid(source);
			if (conceptSource == null) {
				conceptSource = conceptService.getConceptSourceByName(source);
			}
		}
		
		if (codeOrName == null) {
			List<ConceptReferenceTerm> terms = conceptService.getConceptReferenceTerms(null, conceptSource,
			    context.getStartIndex(), context.getLimit(), context.getIncludeAll());
			int count = conceptService.getCountOfConceptReferenceTerms(null, conceptSource, context.getIncludeAll());
			return new AlreadyPaged<ConceptReferenceTerm>(context, terms, context.getIncludeAll(), Long.valueOf(count));
		} else if (searchType.equals(SEARCH_TYPE_EQUAL)) {
			if (conceptSource != null) {
				ConceptReferenceTerm term = conceptService.getConceptReferenceTermByCode(codeOrName, conceptSource);
				if (term == null) {
					term = conceptService.getConceptReferenceTermByName(codeOrName, conceptSource);
				}
				
				if (term == null) {
					return new EmptySearchResult();
				} else {
					List<ConceptReferenceTerm> results = Arrays.asList(term);
					return new AlreadyPaged<ConceptReferenceTerm>(context, results, false, Long.valueOf(results.size()));
				}
			} else {
				Integer startIndex = 0;
				boolean hasMoreTerms = true;
				int termsCount = conceptService.getCountOfConceptReferenceTerms(codeOrName, conceptSource,
				    context.getIncludeAll());
				
				List<ConceptReferenceTerm> equalTerms = new ArrayList<ConceptReferenceTerm>();
				while (hasMoreTerms) {
					List<ConceptReferenceTerm> terms = conceptService.getConceptReferenceTerms(codeOrName, conceptSource,
					    startIndex, context.getLimit(), context.getIncludeAll());
					hasMoreTerms = termsCount > (startIndex + context.getLimit());
					
					for (ConceptReferenceTerm term : terms) {
						if (StringUtils.equalsIgnoreCase(codeOrName, term.getCode()) || StringUtils.equalsIgnoreCase(codeOrName, term.getName())) {
							equalTerms.add(term);
						}
					}
					
					if (!hasMoreTerms || equalTerms.size() >= context.getStartIndex() + context.getLimit()) {
						int toIndex;
						if (equalTerms.size() < context.getStartIndex() + context.getLimit()) {
							toIndex = equalTerms.size();
						} else {
							toIndex = context.getStartIndex() + context.getLimit();
						}
						
						return new AlreadyPaged<ConceptReferenceTerm>(context, equalTerms.subList(context.getStartIndex(),
						    toIndex), hasMoreTerms, Long.valueOf(termsCount));
					} else {
						startIndex += context.getLimit();
					}
				}
				
				throw new IllegalStateException("Should not have reached here");
			}
		} else if (searchType.equals(SEARCH_TYPE_ALIKE)) {
			List<ConceptReferenceTerm> terms = conceptService.getConceptReferenceTerms(codeOrName, conceptSource,
			    context.getStartIndex(), context.getLimit(), context.getIncludeAll());
			int termsCount = conceptService.getCountOfConceptReferenceTerms(codeOrName, conceptSource,
			    context.getIncludeAll());
			boolean hasMoreTerms = termsCount > (context.getStartIndex() + context.getLimit());
			
			return new AlreadyPaged<ConceptReferenceTerm>(context, terms, hasMoreTerms, Long.valueOf(termsCount));
		}
		
		throw new InvalidSearchException("Invalid searchType parameter: '" + searchType + "'. Expected '"
		        + SEARCH_TYPE_EQUAL + "' or '" + SEARCH_TYPE_ALIKE + "'");
	}
	
}

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

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSource;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.InvalidSearchException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.util.LocaleUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Allows for finding concepts by mapping or by name
 */
@Component
public class ConceptSearchHandler1_8 implements SearchHandler {
	
	@Autowired
	@Qualifier("conceptService")
	ConceptService conceptService;
	
	private final SearchConfig searchConfig = new SearchConfig("default", RestConstants.VERSION_1 + "/concept",
	        Arrays.asList("1.8.* - 9.*"),
	        Arrays.asList(
	            new SearchQuery.Builder("Allows you to find concepts by source and code").withRequiredParameters("source")
	                    .withOptionalParameters("code").build(), new SearchQuery.Builder(
	                    "Allows you to find concepts by name and class").withRequiredParameters("name")
	                    .withOptionalParameters("class", "searchType").build(), new SearchQuery.Builder(
	                    		"Allows you to find a list of concepts by passing references")
	                    .withRequiredParameters("references").build()));
	
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
		String source = context.getParameter("source");
		String code = context.getParameter("code");
		String name = context.getParameter("name");
		String conceptClass = context.getParameter("class");
		String searchType = context.getParameter("searchType");
		String conceptReferences = context.getParameter("references");
		
		List<Concept> concepts;

		if (StringUtils.isNotBlank(conceptReferences)) {
			String[] conceptReferenceStrings = conceptReferences.split(",");
			concepts = new ArrayList<Concept>(conceptReferenceStrings.length);

			for (String conceptReference : conceptReferenceStrings) {
				if (StringUtils.isBlank(conceptReference)) {
					continue;
				}
				// handle UUIDs
				if (isValidUuid(conceptReference)) {
					Concept concept = conceptService.getConceptByUuid(conceptReference);
					if (concept != null) {
						concepts.add(concept);
						continue;
					}
				}
				// handle mappings
				int idx = conceptReference.indexOf(':');
				if (idx >= 0 && idx < conceptReference.length() - 1) {
					String conceptSource = conceptReference.substring(0, idx);
					String conceptCode = conceptReference.substring(idx + 1);
					Concept concept = conceptService.getConceptByMapping(conceptCode, conceptSource, false);
					if (concept != null) {
						concepts.add(concept);
						continue;
					}
				}
			}
			if (concepts.size() == 0) {
				return new EmptySearchResult();
			}
	
			return new NeedsPaging<Concept>(concepts, context);
		}

		concepts = new ArrayList<Concept>();

		// If there's class parameter in query
		if ("fuzzy".equals(searchType)) {
			List<Locale> locales = new ArrayList<Locale>(LocaleUtility.getLocalesInOrder());
			List<ConceptClass> classes = null;
			ConceptClass responseConceptClass = conceptService.getConceptClassByUuid(conceptClass);
			
			if (responseConceptClass != null) {
				classes = Arrays.asList(responseConceptClass);
			}
			
			List<ConceptSearchResult> searchResults = conceptService.getConcepts(name, locales, context.getIncludeAll(),
			    classes, null, null, null, null, context.getStartIndex(), context.getLimit());
			List<Concept> results = new ArrayList<Concept>(searchResults.size());
			for (ConceptSearchResult csr : searchResults) {
				results.add(csr.getConcept());
			}
			return new NeedsPaging<Concept>(results, context);
		} else if (searchType == null || "equals".equals(searchType)) {
			
			if (name != null) {
				Concept concept = conceptService.getConceptByName(name);
				concepts.add(concept);
				if (concept != null) {
					boolean isPreferredOrFullySpecified = false;
					for (ConceptName conceptname : concept.getNames()) {
						if (conceptname.getName().equalsIgnoreCase(name)
						        && (conceptname.isPreferred() || conceptname.isFullySpecifiedName())) {
							isPreferredOrFullySpecified = true;
							break;
						}
					}
					if (!isPreferredOrFullySpecified) {
						throw new APIException(
						        "The concept name should be either a fully specified or locale preferred name");
					}
					
					return new NeedsPaging<Concept>(concepts, context);
				} else {
					return new EmptySearchResult();
				}
			}
		} else {
			throw new InvalidSearchException("Invalid searchType: " + searchType
			        + ". Allowed values: \"equals\" and \"fuzzy\"");
		}
		
		ConceptSource conceptSource = conceptService.getConceptSourceByUuid(source);
		if (conceptSource == null) {
			conceptSource = conceptService.getConceptSourceByName(source);
		}
		if (conceptSource == null) {
			return new EmptySearchResult();
		}
		
		if (code == null) {
			List<ConceptMap> conceptMaps = conceptService.getConceptMappingsToSource(conceptSource);
			for (ConceptMap conceptMap : conceptMaps) {
				if (!conceptMap.getConcept().isRetired() || context.getIncludeAll()) {
					concepts.add(conceptMap.getConcept());
				}
			}
			return new NeedsPaging<Concept>(concepts, context);
		} else {
			List<Concept> conceptsByMapping = conceptService.getConceptsByMapping(code, source, false);
			
			return new NeedsPaging<Concept>(conceptsByMapping, context);
		}
	}
	
	private static boolean isValidUuid(String uuid) {
		return uuid != null && (uuid.length() == 36 || uuid.length() == 38 || uuid.indexOf(' ') < 0 || uuid.indexOf('.') < 0);
	}
}

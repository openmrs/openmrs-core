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

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.ConceptResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PatientResource1_8;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The contents of this file are subject to the OpenMRS Public License Version 1.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://license.openmrs.org Software distributed under the License is distributed on an
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 * specific language governing rights and limitations under the License. Copyright (C) OpenMRS, LLC.
 * All Rights Reserved.
 */
@Component
public class ObservationSearchHandler1_8 implements SearchHandler {
	
	private final SearchConfig searchConfig = new SearchConfig("default", RestConstants.VERSION_1 + "/obs", Arrays.asList(
	    "1.8.* - 2.*"), Arrays.asList(new SearchQuery.Builder(
	        "Allows you to find Observations by patient and concept").withRequiredParameters("patient")
	        .withOptionalParameters("concept", "concepts", "answers", "groupingConcepts").build()));
	
	@Override
	public SearchConfig getSearchConfig() {
		return this.searchConfig;
	}
	
	@Override
	public PageableResult search(RequestContext context) throws ResponseException {
		
		String patientUuid = context.getRequest().getParameter("patient");
		List<String> questionConceptUuids = new ArrayList<String>();
		List<String> answerConceptUuids = new ArrayList<String>();
		List<String> groupingConceptUuids = new ArrayList<String>();
		
		if (context.getRequest().getParameter("concept") != null) {
			questionConceptUuids.add(context.getRequest().getParameter("concept"));
		}
		else if (context.getRequest().getParameter("concepts") != null) {
			questionConceptUuids.addAll(Arrays.asList(context.getRequest().getParameter("concepts").split(",")));
		}
		
		if (context.getRequest().getParameter("answers") != null) {
			answerConceptUuids.addAll(Arrays.asList(context.getRequest().getParameter("answers").split(",")));
		}
		
		if (context.getRequest().getParameter("groupingConcepts") != null) {
			groupingConceptUuids.addAll(Arrays.asList(context.getRequest().getParameter("groupingConcepts").split(",")));
		}
		
		if (patientUuid != null) {
			
			Patient patient = ((PatientResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(
			    Patient.class)).getByUniqueId(patientUuid);
			
			List<Concept> questionConcepts = new ArrayList<Concept>();
			List<Concept> answerConcepts = new ArrayList<Concept>();
			
			if (patient != null) {
				
				if (!questionConceptUuids.isEmpty()) {
					for (String conceptUuid : questionConceptUuids) {
						questionConcepts.add(((ConceptResource1_8) Context.getService(RestService.class)
						        .getResourceBySupportedClass(Concept.class)).getByUniqueId(conceptUuid));
					}
				}
				
				if (!answerConceptUuids.isEmpty()) {
					for (String conceptUuid : answerConceptUuids) {
						answerConcepts.add(((ConceptResource1_8) Context.getService(RestService.class)
						        .getResourceBySupportedClass(Concept.class)).getByUniqueId(conceptUuid));
					}
				}
				
				List<Obs> obs = Context.getObsService().getObservations(Collections.singletonList((Person) patient), null,
				    !questionConcepts.isEmpty() ? questionConcepts : null,
				    !answerConcepts.isEmpty() ? answerConcepts : null, null,
				    null, null, null, null, null, null, false);
				
				// limit by grouping concept, if present... this could potentially be a heavy call if not limiting by question or answer conept
				if (!groupingConceptUuids.isEmpty()) {
					Iterator<Obs> i = obs.iterator();
					while (i.hasNext()) {
						Obs o = i.next();
						if (o.getObsGroup() == null
						        || !groupingConceptUuids.contains(o.getObsGroup().getConcept().getUuid())) {
							i.remove();
						}
					}
				}
				
				return new NeedsPaging<Obs>(obs, context);
			}
		}
		
		return new EmptySearchResult();
	}
}

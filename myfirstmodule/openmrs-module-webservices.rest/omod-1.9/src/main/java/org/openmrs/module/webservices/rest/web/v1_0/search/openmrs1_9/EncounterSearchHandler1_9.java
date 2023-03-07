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
import org.openmrs.*;
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
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.ConceptResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PatientResource1_8;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Allow searching for encounters based on an obs, matching the input patient and concept, within
 * the encounter; optional filtering by obs numeric, text, or coded values. Sample REST requests:
 * Filter just by patient and concept value:
 * http://localhost:8080/openmrs/ws/rest/v1/encounter?s=byObs
 * &patient=98bd219e-2e3a-41be-ae4e-bbd129d943d0 &obsConcept=0dc64225-e9f6-11e4-a8a3-54ee7513a7ff
 * Filter by coded value: http://localhost:8080/openmrs/ws/rest/v1/encounter?s=byObs
 * &patient=98bd219e-2e3a-41be-ae4e-bbd129d943d0 &obsConcept=0dc64225-e9f6-11e4-a8a3-54ee7513a7ff
 * &obsValues=3cd6f600-26fe-102b-80cb-0017a47871b2 Filter by numeric values:
 * http://localhost:8080/openmrs/ws/rest/v1/encounter?s=byObs
 * &patient=98bd219e-2e3a-41be-ae4e-bbd129d943d0&obsConcept=0dc64225-e9f6-11e4-a8a3-54ee7513a7ff
 * &obsValues=150,175
 */
@Component
public class EncounterSearchHandler1_9 implements SearchHandler {
	
	private static final String REQUEST_PARAM_PATIENT = "patient";
	
	private static final String REQUEST_PARAM_CONCEPT = "obsConcept";
	
	private static final String REQUEST_PARAM_VALUES = "obsValues";
	
	SearchQuery searchQuery = new SearchQuery.Builder("Allows you to search for encounters by observation values "
	        + "given by a concept. Values are a comma delimited string of numeric, text or uuid values.")
	        .withRequiredParameters(REQUEST_PARAM_PATIENT, REQUEST_PARAM_CONCEPT)
	        .withOptionalParameters(REQUEST_PARAM_VALUES)
	        .build();
	
	private final SearchConfig searchConfig = new SearchConfig("byObs",
	        RestConstants.VERSION_1 + "/encounter",
			Collections.singletonList("1.9.* - 9.*"), searchQuery);
	
	@Override
	public SearchConfig getSearchConfig() {
		return searchConfig;
	}
	
	@Override
	public PageableResult search(RequestContext context) throws ResponseException {
		String patientUuid = context.getRequest().getParameter(REQUEST_PARAM_PATIENT);
		String conceptUuid = context.getRequest().getParameter(REQUEST_PARAM_CONCEPT);
		String values = context.getRequest().getParameter(REQUEST_PARAM_VALUES);
		
		if (StringUtils.isNotBlank(patientUuid) && StringUtils.isNotBlank(conceptUuid)) {
			Patient patient = ((PatientResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(
			    Patient.class)).getByUniqueId(patientUuid);
			
			// get all encounters matching patient and concept
			if (patient != null) {
				Concept concept = ((ConceptResource1_8) Context.getService(RestService.class)
				        .getResourceBySupportedClass(Concept.class)).getByUniqueId(conceptUuid);
				if (concept != null) {
					List<Obs> obs = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
					List<Obs> filteredObs = new ArrayList<Obs>();
					Iterator<Obs> obsIterator = obs.iterator();
					
					// return all encounters matching obs and values, if values are provided
					// filter out all non-matching obs
					if (StringUtils.isNotBlank(values)) {
						if (!StringUtils.strip(values, ",").trim().equalsIgnoreCase("")) {
							Obs currentObs;
							String[] valueArray = values.split(",");
							ConceptDatatype datatype = concept.getDatatype();
							
							if (datatype.isNumeric()) {
								while (obsIterator.hasNext()) {
									currentObs = obsIterator.next();
									if (this.isNumberInArray(currentObs.getValueNumeric(), valueArray)) {
										filteredObs.add(currentObs);
									}
								}
							} else if (datatype.isText()) {
								while (obsIterator.hasNext()) {
									currentObs = obsIterator.next();
									if (OpenmrsUtil.isStringInArray(currentObs.getValueText(), valueArray)) {
										filteredObs.add(currentObs);
									}
								}
							} else if (datatype.isCoded()) {
								while (obsIterator.hasNext()) {
									currentObs = obsIterator.next();
									if (OpenmrsUtil.isStringInArray(currentObs.getValueCoded().getUuid(), valueArray)) {
										filteredObs.add(currentObs);
									}
								}
							}
							
							// return encounters for filtered obs
							if (!filteredObs.isEmpty()) {
								List<Encounter> encounters = this.getEncountersForObs(filteredObs.iterator());
								
								return new NeedsPaging<Encounter>(encounters, context);
							}
						}
					}
					else {
						// return all encounters with obs matching concept
						List<Encounter> encounters = this.getEncountersForObs(obsIterator);
						
						return new NeedsPaging<Encounter>(encounters, context);
					}
				} else {
					throw new ObjectNotFoundException();
				}
			} else {
				throw new ObjectNotFoundException();
			}
			
		}
		
		return new EmptySearchResult();
	}
	
	private boolean isNumberInArray(double numberToCheck, String[] values) {
		boolean found = false;
		
		// first convert string to double array
		try {
			for (int i = 0; i < values.length; i++) {
				if (Double.parseDouble(values[i]) == numberToCheck) {
					found = true;
					break;
				}
			}
		}
		catch (Exception e) {
			throw new IllegalArgumentException();
		}
		
		return found;
	}
	
	private List<Encounter> getEncountersForObs(Iterator<Obs> obsIterator) {
		List<Encounter> encounters = new ArrayList<Encounter>();
		Encounter currEncounter;
		while (obsIterator.hasNext()) {
			currEncounter = obsIterator.next().getEncounter();
			if (!this.containsEncounter(encounters, currEncounter)) {
				encounters.add(currEncounter);
			}
		}
		
		return encounters;
	}
	
	private boolean containsEncounter(List<Encounter> encounters, Encounter encounter) {
		boolean containsFlag = false;
		Iterator<Encounter> encIterator = encounters.iterator();
		while (encIterator.hasNext()) {
			if (encIterator.next().getUuid().equalsIgnoreCase(encounter.getUuid())) {
				containsFlag = true;
				break;
			}
		}
		
		return containsFlag;
	}
}

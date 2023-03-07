/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.search.openmrs2_0;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
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
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.EncounterTypeResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PatientResource1_8;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.parameter.EncounterSearchCriteriaBuilder;
import org.springframework.stereotype.Component;

@Component
public class EncounterSearchHandler2_0 implements SearchHandler {
	
	private static final String DATE_FROM = "fromdate";
	
	private static final String DATE_TO = "todate";
	
	private final SearchConfig searchConfig = new SearchConfig("default", RestConstants.VERSION_1 + "/encounter",
			Collections.singletonList("2.0.* - 9.*"),
			Collections.singletonList(new SearchQuery.Builder(
					"Allows you to find Encounter by patient and encounterType (and optionally by from and to date range)")
					.withRequiredParameters("patient").withOptionalParameters("encounterType", DATE_FROM, DATE_TO, "order")
					.build()));
	
	@Override
	public SearchConfig getSearchConfig() {
		return this.searchConfig;
	}
	
	@Override
	public PageableResult search(RequestContext context) throws ResponseException {
		String patientUuid = context.getRequest().getParameter("patient");
		String encounterTypeUuid = context.getRequest().getParameter("encounterType");
		
		String dateFrom = context.getRequest().getParameter(DATE_FROM);
		String dateTo = context.getRequest().getParameter(DATE_TO);
		
		Date fromDate = dateFrom != null ? (Date) ConversionUtil.convert(dateFrom, Date.class) : null;
		Date toDate = dateTo != null ? (Date) ConversionUtil.convert(dateTo, Date.class) : null;
		
		Patient patient = ((PatientResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(
				Patient.class)).getByUniqueId(patientUuid);
		EncounterType encounterType = ((EncounterTypeResource1_8) Context.getService(RestService.class)
				.getResourceBySupportedClass(EncounterType.class)).getByUniqueId(encounterTypeUuid);
		if (patient != null) {
			EncounterSearchCriteriaBuilder encounterSearchCriteriaBuilder = new EncounterSearchCriteriaBuilder()
					.setPatient(patient).setFromDate(fromDate).setToDate(toDate).setIncludeVoided(false);
			if (encounterType != null) {
				encounterSearchCriteriaBuilder.setEncounterTypes(Arrays.asList(encounterType));
			}
			
			EncounterSearchCriteria encounterSearchCriteria = encounterSearchCriteriaBuilder.createEncounterSearchCriteria();
			
			List<Encounter> encounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);
			String order = context.getRequest().getParameter("order");
			if ("desc".equals(order)) {
				Collections.reverse(encounters);
			}
			return new NeedsPaging<Encounter>(encounters, context);
		}
		return new EmptySearchResult();
	}
}

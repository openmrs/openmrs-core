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

import org.apache.commons.lang.StringUtils;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.api.RestHelperService;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class PatientByIdentifierSearchHandler1_8 implements SearchHandler {
	
	@Autowired
	RestHelperService restHelperService;
	
	private final SearchConfig searchConfig = new SearchConfig("patientByIdentifier", RestConstants.VERSION_1 + "/patient",
			Collections.singletonList("1.8.* - 9.*"),
			Collections.singletonList(new SearchQuery.Builder("Allows you to find Patients by identifier")
					.withRequiredParameters("identifier").withOptionalParameters("searchType").build()));
	
	@Override
	public SearchConfig getSearchConfig() {
		return this.searchConfig;
	}
	
	@Override
	public PageableResult search(RequestContext context) throws ResponseException {
		
		String identifier = context.getRequest().getParameter("identifier");
		String searchType = context.getParameter("searchType");
		
		if (StringUtils.isNotBlank(identifier)) {
			if ("start".equals(searchType)) {
				List<Patient> patients = restHelperService.findPatientsByIdentifierStartingWith(identifier,
				    context.getIncludeAll());
				if (patients != null && !patients.isEmpty()) {
					return new NeedsPaging<Patient>(patients, context);
				}
			} else {
				List<Patient> patients = Context.getPatientService().getPatients(null, identifier, null, true);
				if (patients != null && patients.size() > 0) {
					return new NeedsPaging<Patient>(patients, context);
				}
			}
		}
		
		return new EmptySearchResult();
	}
	
}

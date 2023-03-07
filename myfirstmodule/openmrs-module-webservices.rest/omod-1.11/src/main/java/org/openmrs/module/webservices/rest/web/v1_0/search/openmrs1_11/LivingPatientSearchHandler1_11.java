/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.webservices.rest.web.v1_0.search.openmrs1_11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.stereotype.Component;

@Component
public class LivingPatientSearchHandler1_11 implements SearchHandler {
	
	private final SearchConfig searchConfig = new SearchConfig("default", RestConstants.VERSION_1 + "/patient",
			Collections.singletonList("1.11.* - 9.*"),
			Collections.singletonList(
					new SearchQuery.Builder("Allows you to find all patients including the dead or only living patients")
							.withRequiredParameters("q").withOptionalParameters("includeDead").build()));
	
	@Override
	public SearchConfig getSearchConfig() {
		return searchConfig;
	}
	
	@Override
	public PageableResult search(RequestContext context) throws ResponseException {
		String includeDeadStr = context.getParameter("includeDead");
		String searchPhrase = context.getParameter("q");
		Boolean includeDead = StringUtils.isNotBlank(includeDeadStr) ? Boolean.parseBoolean(includeDeadStr) : false;
		if (StringUtils.isNotBlank(searchPhrase)) {
			List<Patient> allPatients = Context.getPatientService().getPatients(searchPhrase);
			Boolean resultsExist = allPatients != null && !allPatients.isEmpty();
			if (!resultsExist) {
				return new EmptySearchResult();
			}
			if (includeDead) {
				return new NeedsPaging<Patient>(allPatients, context);
			} else {
				List<Patient> livingPatients = new ArrayList<Patient>();
				for (Patient patient : allPatients) {
					if (!patient.isDead()) {
						livingPatients.add(patient);
					}
				}
				return new NeedsPaging<Patient>(livingPatients, context);
			}
		}
		return new EmptySearchResult();
	}
}

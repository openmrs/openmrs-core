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

import java.util.Collections;
import java.util.List;

import org.openmrs.Form;
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
public class FormSearchHandler1_8 implements SearchHandler {

	private final SearchConfig searchConfig = new SearchConfig("default", RestConstants.VERSION_1 + "/form", Collections.singletonList("1.8 - 9.*"),

			new SearchQuery.Builder(
					"Allows you to filter forms by published status")
					.withRequiredParameters("published").build());

	@Override
	public SearchConfig getSearchConfig() {
		return searchConfig;
	}

	@Override
	public PageableResult search(RequestContext context) throws ResponseException {

		String publishedStatus = context.getParameter("published");
		if (publishedStatus != null && !publishedStatus.isEmpty()) {
			boolean formStatus = Boolean.parseBoolean(publishedStatus);
			List<Form> forms = Context.getFormService().getForms(null, formStatus, null, false, null, null, null);
			if (forms == null) {
				return new EmptySearchResult();
			}
			return new NeedsPaging<Form>(forms, context);
		}
		return new EmptySearchResult();
	}
}

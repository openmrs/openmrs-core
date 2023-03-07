/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.resource.impl;

import java.util.List;

import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.api.Listable;
import org.openmrs.module.webservices.rest.web.resource.api.Searchable;

/**
 * Wraps a list of search results that has not yet had any possible paging settings from the request
 * context applied. Typically this will be used by implementations of {@link Searchable} and
 * {@link Listable} that do not have a native query capable of doing a page-limited search
 * 
 * @param <T> the generic type of the list of results
 */
public class NeedsPaging<T> extends BasePageableResult<T> {
	
	private List<T> unpagedResults;
	
	public NeedsPaging(List<T> unpagedResults, RequestContext context) {
		this.unpagedResults = unpagedResults;
		this.context = context;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BasePageableResult#getPageOfResults()
	 */
	@Override
	public List<T> getPageOfResults() {
		if (context.getStartIndex() == 0 && context.getLimit() >= unpagedResults.size()) {
			return unpagedResults;
		} else {
			int endIndex = context.getStartIndex() + context.getLimit();
			if (endIndex > unpagedResults.size())
				endIndex = unpagedResults.size();
			return unpagedResults.subList(context.getStartIndex(), endIndex);
		}
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BasePageableResult#hasMoreResults()
	 */
	@Override
	public boolean hasMoreResults() {
		return unpagedResults.size() > context.getStartIndex() + context.getLimit();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BasePageableResult#getTotalCount()
	 */
	@Override
	public Long getTotalCount() {
		return (long) unpagedResults.size();
	}
}

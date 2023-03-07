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
import org.openmrs.module.webservices.rest.web.resource.api.Searchable;

/**
 * Wraps a list of search results that has already had the paging settings from the RequestContext
 * applied. This should be used by implementations of {@link Searchable} that can natively query in
 * a page-limited way.
 * 
 * @param <T> the generic type of the list of results
 */
public class AlreadyPaged<T> extends BasePageableResult<T> {
	
	private List<T> results;
	
	private boolean hasMoreResults;
	
	private Long totalCount;
	
	public AlreadyPaged(RequestContext context, List<T> results, boolean hasMoreResults) {
		this.context = context;
		this.results = results;
		this.hasMoreResults = hasMoreResults;
	}
	
	public AlreadyPaged(RequestContext context, List<T> results, boolean hasMoreResults, Long totalCount) {
		this(context, results, hasMoreResults);
		this.totalCount = totalCount;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BasePageableResult#getPageOfResults()
	 */
	@Override
	public List<T> getPageOfResults() {
		return results;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BasePageableResult#hasMoreResults()
	 */
	@Override
	public boolean hasMoreResults() {
		return hasMoreResults;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BasePageableResult#getTotalCount()
	 */
	@Override
	public Long getTotalCount() {
		return totalCount;
	}
}

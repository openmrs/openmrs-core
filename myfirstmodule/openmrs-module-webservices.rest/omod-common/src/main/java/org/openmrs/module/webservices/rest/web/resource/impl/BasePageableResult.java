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

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.Hyperlink;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.api.Converter;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * Base implementation that converts the result list to the requested representation, and adds
 * next/prev links if necessary
 * 
 * @param <T> the generic type of the list of results
 */
public abstract class BasePageableResult<T> implements PageableResult {
	
	protected RequestContext context;
	
	public abstract List<T> getPageOfResults();
	
	public abstract boolean hasMoreResults();
	
	/**
	 * Return the total number of records available for the requested resource and the applied
	 * request parameters.
	 */
	public Long getTotalCount() {
		return null;
	}
	
	/**
	 * @see PageableResult#toSimpleObject(Converter)
	 * <strong>Should</strong> add property totalCount if context contains parameter totalCount which is true
	 * <strong>Should</strong> not add property totalCount if context contains parameter totalCount which is false
	 * <strong>Should</strong> not add property totalCount if context does not contains parameter totalCount
	 */
	@Override
	public SimpleObject toSimpleObject(Converter preferredConverter) throws ResponseException {
		List<Object> results = new ArrayList<Object>();
		for (T match : getPageOfResults()) {
			results.add(ConversionUtil.convertToRepresentation(match, context.getRepresentation(), preferredConverter));
		}
		
		SimpleObject ret = new SimpleObject().add("results", results);
		boolean hasMore = hasMoreResults();
		if (context.getStartIndex() > 0 || hasMore) {
			List<Hyperlink> links = new ArrayList<Hyperlink>();
			if (hasMore)
				links.add(context.getNextLink());
			if (context.getStartIndex() > 0)
				links.add(context.getPreviousLink());
			ret.add("links", links);
		}
		if (Boolean.valueOf(context.getParameter("totalCount"))) {
			ret.add("totalCount", getTotalCount());
		}
		return ret;
	}
	
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.APIException;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;

/**
 * Holds information related to a REST web service request
 */
public class RequestContext {
	
	private HttpServletRequest request;
	
	private HttpServletResponse response;
	
	private Representation representation = new DefaultRepresentation();
	
	private Integer startIndex = 0;
	
	private Integer limit = RestUtil.getDefaultLimit();
	
	private Boolean includeAll = false;
	
	// for resources that represent class hierarchies, this allows requests for a specific type
	private String type;
	
	public RequestContext() {
	}
	
	/**
	 * @return the request
	 */
	public HttpServletRequest getRequest() {
		return request;
	}
	
	/**
	 * @param request the request to set
	 */
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
	
	/**
	 * @return the response
	 */
	public HttpServletResponse getResponse() {
		return response;
	}
	
	/**
	 * @param response the response to set
	 */
	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}
	
	/**
	 * @return the representation
	 */
	public Representation getRepresentation() {
		return representation;
	}
	
	/**
	 * @param representation the representation to set
	 */
	public void setRepresentation(Representation representation) {
		this.representation = representation;
	}
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * Should be used to determine whether voided data and/or retired metadata should be returned in
	 * <code>getAll()</code> and <code>search</code> requests.
	 * 
	 * @return the <code>Boolean</code> specifying whether or not to include voided data / retired
	 *         metadata
	 */
	public Boolean getIncludeAll() {
		return includeAll;
	}
	
	/**
	 * @param includeAll whether or not to include voided data / retired metadata
	 * @see RestUtil#getRequestContext
	 */
	public void setIncludeAll(Boolean includeAll) {
		this.includeAll = includeAll;
	}
	
	/**
	 * Should be used to limit the number of main results returned by search methods
	 * 
	 * @return the integer limit set in a request parameter
	 * @see RestUtil#getRequestContext(org.springframework.web.context.request.WebRequest)
	 * @see RestConstants#REQUEST_PROPERTY_FOR_LIMIT
	 */
	public Integer getLimit() {
		return limit;
	}
	
	/**
	 * @param limit the limit to set
	 * <strong>Should</strong> not accept a value less than one
	 * <strong>Should</strong> not accept a null value
	 */
	public void setLimit(Integer limit) {
		if (limit == null || limit <= 0)
			throw new APIException("If you specify a number of results to return, it must be >0 and not null");
		if (limit > RestUtil.getAbsoluteLimit())
			throw new APIException("Administrator has set absolute limit at " + RestUtil.getAbsoluteLimit());
		else
			this.limit = limit;
	}
	
	/**
	 * Should be used by search methods to jump results to start with this number in the list. Set
	 * by users in a request parameter
	 * 
	 * @return the integer startIndex
	 * @see RestUtil#getRequestContext(org.springframework.web.context.request.WebRequest)
	 * @see RestConstants#REQUEST_PROPERTY_FOR_START_INDEX
	 */
	public Integer getStartIndex() {
		return startIndex;
	}
	
	/**
	 * @param startIndex the startIndex to set
	 */
	public void setStartIndex(Integer startIndex) {
		this.startIndex = startIndex;
	}
	
	/**
	 * (Assumes this was a search query)
	 * 
	 * @return the hyperlink you would GET to fetch the next page of results for the query
	 */
	public Hyperlink getNextLink() {
		String query = getQueryWithoutStartIndex();
		query += RestConstants.REQUEST_PROPERTY_FOR_START_INDEX + "=" + (startIndex + limit);
		return new Hyperlink("next", request.getRequestURL().append(query).toString());
	}
	
	/**
	 * (Assumes this was a search query)
	 * 
	 * @return the hyperlink you would GET to fetch the previous page of results for the query
	 */
	public Hyperlink getPreviousLink() {
		String query = getQueryWithoutStartIndex();
		int prevStart = startIndex - limit;
		if (prevStart < 0)
			prevStart = 0;
		if (prevStart > 0)
			query += RestConstants.REQUEST_PROPERTY_FOR_START_INDEX + "=" + prevStart;
		return new Hyperlink("prev", request.getRequestURL().append(query).toString());
	}
	
	/**
	 * @return the query string from this request, with the startIndex query parameter removed if it
	 *         was present
	 */
	@SuppressWarnings("unchecked")
	private String getQueryWithoutStartIndex() {
		StringBuilder query = new StringBuilder("?");
		for (Map.Entry<String, String[]> e : ((Map<String, String[]>) (request.getParameterMap())).entrySet()) {
			String param = e.getKey();
			if (RestConstants.REQUEST_PROPERTY_FOR_START_INDEX.equals(param)) {
				continue;
			}
			for (int i = 0; i < e.getValue().length; ++i) {
				try {
					query.append(e.getKey() + "=" + URLEncoder.encode(e.getValue()[i], "UTF-8") + "&");
				}
				catch (UnsupportedEncodingException ex) {
					throw new RuntimeException("UTF-8 encoding should always be supported", ex);
				}
			}
		}
		return query.toString();
	}
	
	/**
	 * Convenience method that returns a parameter value as a string if a request parameter with the
	 * specified name exists in the associated {@link HttpServletRequest} object
	 * 
	 * @param name the request parameter name
	 * @return the value
	 * <strong>Should</strong> return the request parameter of given name if present in the request
	 * <strong>Should</strong> return null if the wanted request parameter is not present in the request
	 * <strong>Should</strong> return null if request is null
	 */
	public String getParameter(String name) {
		
		if (getRequest() == null) {
			return null;
		}
		return getRequest().getParameter(name);
	}
	
}

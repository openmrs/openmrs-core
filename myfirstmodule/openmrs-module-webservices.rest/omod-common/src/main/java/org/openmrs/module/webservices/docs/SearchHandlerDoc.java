/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.docs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;

public class SearchHandlerDoc implements Comparable<SearchHandlerDoc> {
	
	private String searchHandlerId;
	
	private String resourceURL;
	
	private String resourceName;
	
	private List<String> supportedVersions;
	
	private List<SearchQueryDoc> searchQueriesDoc;
	
	public SearchHandlerDoc(SearchHandler handler, String url) {
		this.searchHandlerId = handler.getSearchConfig().getId();
		this.resourceURL = url + "/rest/" + handler.getSearchConfig().getSupportedResource();
		this.resourceName = "";
		this.supportedVersions = new ArrayList<String>(handler.getSearchConfig().getSupportedOpenmrsVersions());
		this.searchQueriesDoc = getSearchQueryDocList(handler.getSearchConfig().getSearchQueries());
	}
	
	/**
	 * @return the searchHandlerId
	 */
	public String getSearchHandlerId() {
		return searchHandlerId;
	}
	
	/**
	 * @param searchHandlerId the searchHandlerId to set
	 */
	public void setSearchHandlerId(String searchHandlerId) {
		this.searchHandlerId = searchHandlerId;
	}
	
	/**
	 * @return the resourceURL
	 */
	public String getResourceName() {
		return this.resourceName;
	}
	
	/**
	 * @param resourceURL the resourceURL to set
	 */
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	
	/**
	 * @return the supportedVersions
	 */
	public List<String> getSupportedVersions() {
		return supportedVersions;
	}
	
	/**
	 * @param supportedVersions the supportedVersions to set
	 */
	public void setSupportedVersions(List<String> supportedVersions) {
		this.supportedVersions = supportedVersions;
	}
	
	/**
	 * @return the searchQueriesDoc
	 */
	public List<SearchQueryDoc> getSearchQueriesDoc() {
		return searchQueriesDoc;
	}
	
	/**
	 * @param searchQueriesDoc the searchQueriesDoc to set
	 */
	public void setSearchQueriesDoc(List<SearchQueryDoc> searchQueriesDoc) {
		this.searchQueriesDoc = searchQueriesDoc;
	}
	
	private List<SearchQueryDoc> getSearchQueryDocList(Set<SearchQuery> searchQuerySet) {
		
		List<SearchQueryDoc> searchQueryDocList = new ArrayList<SearchQueryDoc>();
		
		for (SearchQuery searchQuery : searchQuerySet) {
			SearchQueryDoc temp = new SearchQueryDoc(searchQuery);
			searchQueryDocList.add(temp);
		}
		
		return searchQueryDocList;
	}
	
	@Override
	public int compareTo(SearchHandlerDoc o) {
		// TODO Auto-generated method stub
		return getResourceURL().compareTo(o.getResourceName());
	}
	
	/**
	 * @return the resourceURL
	 */
	public String getResourceURL() {
		return resourceURL;
	}
	
	/**
	 * @param resourceURL the resourceURL to set
	 */
	public void setResourceURL(String resourceURL) {
		this.resourceURL = resourceURL;
	}
	
	private List<String> getVersions(Set<String> versions) {
		
		List<String> versionAsList = new ArrayList<String>();
		for (String version : versions) {
			versionAsList.add(version);
		}
		
		return versionAsList;
	}
	
}

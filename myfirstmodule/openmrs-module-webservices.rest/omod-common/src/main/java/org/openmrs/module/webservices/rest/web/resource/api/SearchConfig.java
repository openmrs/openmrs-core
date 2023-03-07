/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.resource.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;

/**
 * Stores {@link SearchHandler}'s configuration.
 */
public class SearchConfig {
	
	private final String id;
	
	private final String supportedResource;
	
	private final Set<String> supportedOpenmrsVersions;
	
	private final Set<SearchQuery> searchQueries;
	
	/**
	 * Creates an instance of SearchConfig.
	 * 
	 * @param id the id of the search config
	 * @param supportedResource the supported resource
	 * @param supportedOpenmrsVersions the supported openmrs versions
	 * @param searchQueries the search queries
	 * <strong>Should</strong> create an instance of search config
	 * <strong>Should</strong> fail if given id is null
	 * <strong>Should</strong> fail if given id is empty
	 * <strong>Should</strong> fail if given supported resource is null
	 * <strong>Should</strong> fail if given supported resource is empty
	 * <strong>Should</strong> fail if given supported openmrs versions is null
	 * <strong>Should</strong> fail if given supported openmrs versions is empty
	 * <strong>Should</strong> fail if given search queries is null
	 * <strong>Should</strong> fail if given search queries is empty
	 */
	public SearchConfig(String id, String supportedResource, Collection<String> supportedOpenmrsVersions,
	    Collection<SearchQuery> searchQueries) {
		Validate.notEmpty(id, "id must not be empty");
		Validate.notEmpty(supportedResource, "supportedResource must not be empty");
		Validate.notEmpty(supportedOpenmrsVersions, "supportedOpenmrsVersions must not be empty");
		Validate.notEmpty(searchQueries, "searchQueries must not be empty");
		
		this.id = id;
		this.supportedResource = supportedResource;
		this.supportedOpenmrsVersions = Collections.unmodifiableSet(new HashSet<String>(supportedOpenmrsVersions));
		this.searchQueries = Collections.unmodifiableSet(new HashSet<SearchQuery>(searchQueries));
	}
	
	/**
	 * Creates an instance of SearchConfig.
	 * <p>
	 * Delegates to {@link SearchConfig#SearchConfig(String, String, Collection, Collection)}.
	 * </p
	 * 
	 * @param id the id of the search config
	 * @param supportedResource the supported resource
	 * @param supportedOpenmrsVersion the supported openmrs version
	 * @param searchQuery the search query
	 * <strong>Should</strong> create an instance of search config
	 */
	public SearchConfig(String id, String supportedResource, String supportedOpenmrsVersion, SearchQuery searchQuery) {
		this(id, supportedResource, Arrays.asList(supportedOpenmrsVersion), Arrays.asList(searchQuery));
	}
	
	/**
	 * Creates an instance of SearchConfig.
	 * <p>
	 * Delegates to {@link SearchConfig#SearchConfig(String, String, Collection, Collection)}.
	 * </p>
	 * 
	 * @param id the id of the search config
	 * @param supportedResource the supported resource
	 * @param supportedOpenmrsVersions the supported openmrs versions
	 * @param searchQuery the search query
	 * <strong>Should</strong> create an instance of search config
	 */
	public SearchConfig(String id, String supportedResource, Collection<String> supportedOpenmrsVersions,
	    SearchQuery searchQuery) {
		this(id, supportedResource, supportedOpenmrsVersions, Arrays.asList(searchQuery));
	}
	
	/**
	 * Creates an instance of SearchConfig.
	 * <p>
	 * Delegates to {@link SearchConfig#SearchConfig(String, String, Collection, Collection)}.
	 * </p>
	 * 
	 * @param id the id of the search config
	 * @param supportedResource the supported resource
	 * @param supportedOpenmrsVersion the supported openmrs version
	 * @param searchQueries the search queries
	 * <strong>Should</strong> create an instance of search config
	 */
	public SearchConfig(String id, String supportedResource, String supportedOpenmrsVersion,
	    Collection<SearchQuery> searchQueries) {
		this(id, supportedResource, Arrays.asList(supportedOpenmrsVersion), searchQueries);
	}
	
	/**
	 * Get this id.
	 * 
	 * @return this id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Get this {@code supportedResource}.
	 * 
	 * @return this supported resource
	 */
	public String getSupportedResource() {
		return supportedResource;
	}
	
	/**
	 * Get this {@code supportedOpenmrsVersions}.
	 * 
	 * @return this supported openmrs versions
	 */
	public Set<String> getSupportedOpenmrsVersions() {
		return supportedOpenmrsVersions;
	}
	
	/**
	 * Get this {@code searchQueries}.
	 * 
	 * @return this search queries
	 */
	public Set<SearchQuery> getSearchQueries() {
		return searchQueries;
	}
	
	/**
	 * @see Object#hashCode()
	 * @return the hash code
	 * <strong>Should</strong> return same hashcode for equal search configs
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((supportedOpenmrsVersions == null) ? 0 : supportedOpenmrsVersions.hashCode());
		result = prime * result + ((supportedResource == null) ? 0 : supportedResource.hashCode());
		return result;
	}
	
	/**
	 * @see Object#equals(Object)
	 * @param obj the object to test for if equal to this
	 * @return true if obj is equal to this otherwise false
	 * <strong>Should</strong> return true if given this
	 * <strong>Should</strong> return true if this id and supported openmrs version and supported resource are equal
	 *         to given search configs
	 * <strong>Should</strong> be symmetric
	 * <strong>Should</strong> be transitive
	 * <strong>Should</strong> return false if given null
	 * <strong>Should</strong> return false if given an object which is not an instanceof this class
	 * <strong>Should</strong> return false if this id is not equal to the given search configs id
	 * <strong>Should</strong> return false if this supported openmrs version is not equal to given search configs
	 *         supported openmrs version
	 * <strong>Should</strong> return false if this supported resource is not equal to given search configs
	 *         supported resource
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof SearchConfig))
			return false;
		SearchConfig other = (SearchConfig) obj;
		return id.equals(other.id)
				&& supportedOpenmrsVersions.equals(other.supportedOpenmrsVersions)
				&& supportedResource.equals(other.supportedResource);
	}
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.api.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.proxy.HibernateProxy;
import org.openmrs.api.APIException;
import org.openmrs.module.ModuleUtil;
import org.openmrs.module.webservices.rest.web.OpenmrsClassScanner;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.api.RestHelperService;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchParameter;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubclassHandler;
import org.openmrs.module.webservices.rest.web.response.InvalidSearchException;
import org.openmrs.module.webservices.rest.web.response.UnknownResourceException;
import org.openmrs.util.OpenmrsConstants;

/**
 * Default implementation of the {@link RestService}
 */
public class RestServiceImpl implements RestService {
	
	volatile Map<String, ResourceDefinition> resourceDefinitionsByNames;
	
	volatile Map<Class<?>, Resource> resourcesBySupportedClasses;
	
	private volatile Map<CompositeSearchHandlerKeyValue, Set<SearchHandler>> searchHandlersByParameter;
	
	private volatile Map<CompositeSearchHandlerKeyValue, SearchHandler> searchHandlersByIds;
	
	private volatile Map<String, Set<SearchHandler>> searchHandlersByResource;
	
	private volatile List<SearchHandler> allSearchHandlers;
	
	private RestHelperService restHelperService;
	
	private OpenmrsClassScanner openmrsClassScanner;
	
	public RestHelperService getRestHelperService() {
		return restHelperService;
	}
	
	public void setRestHelperService(RestHelperService restHelperService) {
		this.restHelperService = restHelperService;
	}
	
	public OpenmrsClassScanner getOpenmrsClassScanner() {
		return openmrsClassScanner;
	}
	
	public void setOpenmrsClassScanner(OpenmrsClassScanner openmrsClassScanner) {
		this.openmrsClassScanner = openmrsClassScanner;
	}
	
	public RestServiceImpl() {
	}
	
	static class ResourceDefinition {
		
		public Resource resource;
		
		public int order;
		
		public ResourceDefinition(Resource resource, int order) {
			this.resource = resource;
			this.order = order;
		}
		
	}
	
	/**
	 * Wraps {@code Resource} name and an additional string-based key into a composite key.
	 */
	private static class CompositeSearchHandlerKeyValue {
		
		public final String supportedResource;
		
		public final String secondKey;
		
		public final String secondKeyValue;
		
		public CompositeSearchHandlerKeyValue(String supportedResource, String additionalKeyProperty) {
			this.supportedResource = supportedResource;
			this.secondKey = additionalKeyProperty;
			this.secondKeyValue = null;
		}
		
		public CompositeSearchHandlerKeyValue(String supportedResource, String additionalKeyProperty,
		    String additionalKeyPropertyValue) {
			this.supportedResource = supportedResource;
			this.secondKey = additionalKeyProperty;
			this.secondKeyValue = additionalKeyPropertyValue;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			
			CompositeSearchHandlerKeyValue that = (CompositeSearchHandlerKeyValue) o;
			
			if (!supportedResource.equals(that.supportedResource))
				return false;
			if (!secondKey.equals(that.secondKey))
				return false;
			return secondKeyValue != null ? secondKeyValue.equals(that.secondKeyValue) : that.secondKeyValue == null;
			
		}
		
		@Override
		public int hashCode() {
			int result = supportedResource.hashCode();
			result = 31 * result + secondKey.hashCode();
			result = 31 * result + (secondKeyValue != null ? secondKeyValue.hashCode() : 0);
			return result;
		}
	}
	
	private void initializeResources() {
		if (resourceDefinitionsByNames != null) {
			return;
		}
		
		Map<String, ResourceDefinition> tempResourceDefinitionsByNames = new HashMap<String, ResourceDefinition>();
		Map<Class<?>, Resource> tempResourcesBySupportedClasses = new HashMap<Class<?>, Resource>();
		
		List<Class<? extends Resource>> resources;
		try {
			resources = openmrsClassScanner.getClasses(Resource.class, true);
		}
		catch (IOException e) {
			throw new APIException("Cannot access REST resources", e);
		}
		
		for (Class<? extends Resource> resource : resources) {
			ResourceMetadata resourceMetadata = getResourceMetadata(resource);
			if (resourceMetadata == null)
				continue;
			
			if (isResourceToBeAdded(resourceMetadata, tempResourceDefinitionsByNames.get(resourceMetadata.getName()))) {
				Resource newResource = newResource(resource);
				
				tempResourceDefinitionsByNames.put(resourceMetadata.getName(), new ResourceDefinition(newResource,
				        resourceMetadata.getOrder()));
				tempResourcesBySupportedClasses.put(resourceMetadata.getSupportedClass(), newResource);
			}
		}
		
		resourcesBySupportedClasses = tempResourcesBySupportedClasses;
		resourceDefinitionsByNames = tempResourceDefinitionsByNames;
	}
	
	/**
	 * Determines whether a {@code Resource} should be added to the cache.
	 * 
	 * @param resourceMetadata the resource metadata of the resource to be added
	 * @param existingResourceDefinition the resource definition of resource
	 * @return true if the resource should be added and false otherwise
	 */
	private boolean isResourceToBeAdded(ResourceMetadata resourceMetadata, ResourceDefinition existingResourceDefinition) {
		
		if (existingResourceDefinition == null) {
			return true;
		}
		if (existingResourceDefinition.order == resourceMetadata.getOrder()) {
			throw new IllegalStateException("Two resources with the same name (" + resourceMetadata.getName()
			        + ") must not have the same order");
		}
		
		return existingResourceDefinition.order >= resourceMetadata.getOrder();
	}
	
	/**
	 * Gets {@code ResourceMetadata} from a {@code Resource} classes annotations.
	 * 
	 * @param resource the resource to get the metadata from
	 * @return the metadata of a resource
	 */
	private ResourceMetadata getResourceMetadata(Class<? extends Resource> resource) {
		ResourceMetadata resourceMetadata;
		
		org.openmrs.module.webservices.rest.web.annotation.Resource resourceAnnotation = resource
		        .getAnnotation(org.openmrs.module.webservices.rest.web.annotation.Resource.class);
		if (resourceAnnotation == null) {
			SubResource subresourceAnnotation = resource.getAnnotation(SubResource.class);
			if (subresourceAnnotation == null
			        || !isOpenmrsVersionInVersions(subresourceAnnotation.supportedOpenmrsVersions())) {
				return null;
			}
			org.openmrs.module.webservices.rest.web.annotation.Resource parentResourceAnnotation = subresourceAnnotation
			        .parent().getAnnotation(org.openmrs.module.webservices.rest.web.annotation.Resource.class);
			if (parentResourceAnnotation == null) {
				return null;
			}
			resourceMetadata = new ResourceMetadata(parentResourceAnnotation.name() + "/" + subresourceAnnotation.path(),
			        subresourceAnnotation.supportedClass(), subresourceAnnotation.order());
		} else {
			if (!isOpenmrsVersionInVersions(resourceAnnotation.supportedOpenmrsVersions())) {
				return null;
			}
			resourceMetadata = new ResourceMetadata(resourceAnnotation.name(), resourceAnnotation.supportedClass(),
			        resourceAnnotation.order());
		}
		return resourceMetadata;
	}
	
	private static class ResourceMetadata {
		
		private final String name;
		
		private final Class<?> supportedClass;
		
		private final int order;
		
		public ResourceMetadata(String name, Class<?> supportedClass, int order) {
			this.name = name;
			this.supportedClass = supportedClass;
			this.order = order;
		}
		
		public String getName() {
			return name;
		}
		
		public Class<?> getSupportedClass() {
			return supportedClass;
		}
		
		public int getOrder() {
			return order;
		}
	}
	
	/**
	 * Checks if OpenMRS version is in given array of versions.
	 * 
	 * @param versions the array of versions to be checked for the openmrs version
	 * @return true if the openmrs version is in versions and false otherwise
	 */
	private boolean isOpenmrsVersionInVersions(String[] versions) {
		
		if (versions.length == 0) {
			return false;
		}
		
		boolean result = false;
		for (String version : versions) {
			if (ModuleUtil.matchRequiredVersions(OpenmrsConstants.OPENMRS_VERSION_SHORT, version)) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	private void initializeSearchHandlers() {
		if (searchHandlersByIds != null) {
			return;
		}
		
		Map<CompositeSearchHandlerKeyValue, SearchHandler> tempSearchHandlersByIds = new HashMap<CompositeSearchHandlerKeyValue, SearchHandler>();
		Map<CompositeSearchHandlerKeyValue, Set<SearchHandler>> tempSearchHandlersByParameters = new HashMap<CompositeSearchHandlerKeyValue, Set<SearchHandler>>();
		Map<String, Set<SearchHandler>> tempSearchHandlersByResource = new HashMap<String, Set<SearchHandler>>();
		
		List<SearchHandler> allSearchHandlers = restHelperService.getRegisteredSearchHandlers();
		for (SearchHandler searchHandler : allSearchHandlers) {
			addSearchHandler(tempSearchHandlersByIds, tempSearchHandlersByParameters, tempSearchHandlersByResource,
			    searchHandler);
		}
		this.allSearchHandlers = allSearchHandlers;
		searchHandlersByParameter = tempSearchHandlersByParameters;
		searchHandlersByIds = tempSearchHandlersByIds;
		searchHandlersByResource = tempSearchHandlersByResource;
	}
	
	private void addSearchHandler(Map<CompositeSearchHandlerKeyValue, SearchHandler> tempSearchHandlersByIds,
	        Map<CompositeSearchHandlerKeyValue, Set<SearchHandler>> tempSearchHandlersByParameters,
	        Map<String, Set<SearchHandler>> tempSearchHandlersByResource, SearchHandler searchHandler) {
		for (String supportedVersion : searchHandler.getSearchConfig().getSupportedOpenmrsVersions()) {
			if (ModuleUtil.matchRequiredVersions(OpenmrsConstants.OPENMRS_VERSION_SHORT, supportedVersion)) {
				addSupportedSearchHandler(tempSearchHandlersByIds, tempSearchHandlersByParameters, searchHandler);
				addSearchHandlerToResourceMap(tempSearchHandlersByResource, searchHandler);
			}
		}
	}
	
	private void addSupportedSearchHandler(Map<CompositeSearchHandlerKeyValue, SearchHandler> tempSearchHandlersByIds,
	        Map<CompositeSearchHandlerKeyValue, Set<SearchHandler>> tempSearchHandlersByParameters,
	        SearchHandler searchHandler) {
		CompositeSearchHandlerKeyValue searchHanlderIdKey = new CompositeSearchHandlerKeyValue(searchHandler
		        .getSearchConfig().getSupportedResource(), searchHandler.getSearchConfig().getId());
		SearchHandler previousSearchHandler = tempSearchHandlersByIds.put(searchHanlderIdKey, searchHandler);
		if (previousSearchHandler != null) {
			SearchConfig config = searchHandler.getSearchConfig();
			throw new IllegalStateException("Two search handlers (" + searchHandler.getClass() + ", "
			        + previousSearchHandler.getClass() + ") for the same resource (" + config.getSupportedResource()
			        + ") must not have the same ID (" + config.getId() + ")");
		}
		
		addSearchHandlerToParametersMap(tempSearchHandlersByParameters, searchHandler);
	}
	
	private void addSearchHandlerToParametersMap(
	        Map<CompositeSearchHandlerKeyValue, Set<SearchHandler>> tempSearchHandlersByParameters,
	        SearchHandler searchHandler) {
		
		for (SearchQuery searchQueries : searchHandler.getSearchConfig().getSearchQueries()) {
			Set<SearchParameter> parameters = new HashSet<SearchParameter>(searchQueries.getRequiredParameters());
			parameters.addAll(searchQueries.getOptionalParameters());
			
			for (SearchParameter parameter : parameters) {
				CompositeSearchHandlerKeyValue parameterKey = new CompositeSearchHandlerKeyValue(searchHandler
				        .getSearchConfig().getSupportedResource(), parameter.getName(), parameter.getValue());
				Set<SearchHandler> list = tempSearchHandlersByParameters.get(parameterKey);
				if (list == null) {
					list = new HashSet<SearchHandler>();
					tempSearchHandlersByParameters.put(parameterKey, list);
				}
				list.add(searchHandler);
			}
		}
	}
	
	private void addSearchHandlerToResourceMap(Map<String, Set<SearchHandler>> tempSearchHandlersByResource,
	        SearchHandler searchHandler) {
		SearchConfig config = searchHandler.getSearchConfig();
		Set<SearchHandler> handlers = tempSearchHandlersByResource.get(config.getSupportedResource());
		if (handlers == null) {
			handlers = new HashSet<SearchHandler>();
			tempSearchHandlersByResource.put(config.getSupportedResource(), handlers);
		}
		handlers.add(searchHandler);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.api.RestService#getRepresentation(java.lang.String)
	 * <strong>Should</strong> return default representation if given null
	 * <strong>Should</strong> return default representation if given string is empty
	 * <strong>Should</strong> return reference representation if given string matches the ref representation
	 *         constant
	 * <strong>Should</strong> return default representation if given string matches the default representation
	 *         constant
	 * <strong>Should</strong> return full representation if given string matches the full representation constant
	 * <strong>Should</strong> return an instance of custom representation if given string starts with the custom
	 *         representation prefix
	 * <strong>Should</strong> return an instance of named representation for given string if it is not empty and
	 *         does not match any other case
	 */
	@Override
	public Representation getRepresentation(String requested) {
		if (StringUtils.isEmpty(requested)) {
			return Representation.DEFAULT;
		}
		
		if (RestConstants.REPRESENTATION_REF.equals(requested)) {
			return Representation.REF;
		} else if (RestConstants.REPRESENTATION_DEFAULT.equals(requested)) {
			return Representation.DEFAULT;
		} else if (RestConstants.REPRESENTATION_FULL.equals(requested)) {
			return Representation.FULL;
		} else if (requested.startsWith(RestConstants.REPRESENTATION_CUSTOM_PREFIX)) {
			return new CustomRepresentation(requested.replace(RestConstants.REPRESENTATION_CUSTOM_PREFIX, ""));
		}
		
		return new NamedRepresentation(requested);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.api.RestService#getResourceByName(String)
	 * <strong>Should</strong> return resource for given name
	 * <strong>Should</strong> return resource for given name and ignore unannotated resources
	 * <strong>Should</strong> fail if failed to get resource classes
	 * <strong>Should</strong> fail if resource for given name cannot be found
	 * <strong>Should</strong> fail if resource for given name does not support the current openmrs version
	 * <strong>Should</strong> return subresource for given name
	 * <strong>Should</strong> fail if subresource for given name does not support the current openmrs version
	 * <strong>Should</strong> fail if two resources with same name and order are found for given name
	 * <strong>Should</strong> return resource with lower order value if two resources with the same name are found
	 *         for given name
	 */
	@Override
	public Resource getResourceByName(String name) throws APIException {
		initializeResources();
		
		ResourceDefinition resourceDefinition = resourceDefinitionsByNames.get(name);
		if (resourceDefinition == null) {
			throw new UnknownResourceException("Unknown resource: " + name);
		} else {
			return resourceDefinition.resource;
		}
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.api.RestService#getResourceBySupportedClass(Class)
	 * <strong>Should</strong> return resource supporting given class and current openmrs version
	 * <strong>Should</strong> fail if no resource supporting given class and current openmrs version was found
	 * <strong>Should</strong> fail if no resource supporting given class was found
	 * <strong>Should</strong> return resource supporting superclass of given class if given class is a hibernate
	 *         proxy
	 * <strong>Should</strong> return resource supporting superclass of given class if no resource supporting given
	 *         class was found
	 * <strong>Should</strong> return resource supporting direct superclass of given class if no resource supporting
	 *         given class was found but multiple resources supporting multiple superclasses exist
	 * <strong>Should</strong> fail if failed to get resource classes
	 * <strong>Should</strong> fail if two resources with same name and order are found for given class
	 * <strong>Should</strong> return resource with lower order value if two resources with the same name are found
	 *         for given class
	 */
	@Override
	public Resource getResourceBySupportedClass(Class<?> resourceClass) throws APIException {
		initializeResources();
		
		if (HibernateProxy.class.isAssignableFrom(resourceClass)) {
			resourceClass = resourceClass.getSuperclass();
		}
		
		Resource resource = resourcesBySupportedClasses.get(resourceClass);
		
		if (resource == null) {
			Entry<Class<?>, Resource> bestResourceEntry = null;
			
			for (Entry<Class<?>, Resource> resourceEntry : resourcesBySupportedClasses.entrySet()) {
				if (resourceEntry.getKey().isAssignableFrom(resourceClass) && (bestResourceEntry == null
				        || bestResourceEntry.getKey().isAssignableFrom(resourceEntry.getKey()))) {
					bestResourceEntry = resourceEntry;
				}
			}
			
			if (bestResourceEntry != null) {
				resource = bestResourceEntry.getValue();
			}
		}
		
		if (resource == null) {
			throw new APIException("Unknown resource: " + resourceClass);
		} else {
			return resource;
		}
	}
	
	/**
	 * @throws InstantiationException
	 */
	private Resource newResource(Class<? extends Resource> resourceClass) {
		try {
			return resourceClass.newInstance();
		}
		catch (Exception ex) {
			throw new APIException("Failed to instantiate " + resourceClass, ex);
		}
	}
	
	/**
	 * Returns a search handler, which supports the given resource and the map of parameters and
	 * values.
	 * <p>
	 * A {@code SearchHandler} is selected according to following steps (in this order):
	 * <ul>
	 * <li>Lookup a {@code SearchHandler} based on its {@code id} ({@code SearchConfig#id}) if
	 * specified in given {@code parameters}. This lookup can fail if no or two
	 * {@code SearchHandler}'s is/are found for given {@code id} and {@code resourceName}.</li>
	 * <li>Lookup a {@code SearchHandler} based on given {@code parameters} if no {@code id} is
	 * specified. The lookup returns the {@code SearcHandler} supporting all requested
	 * {@code parameters} and with {@code parameters} satisfying the {@code SearchHandler}'s
	 * {@code SearchConfig}'s required parameters. This lookup can fail if more than 1
	 * {@code SearchHandler} satisfies the requirements mentioned before.</li>
	 * </ul>
	 * If no {@code SearchHandler} is found, {@code NULL} is returned.
	 * </p>
	 * 
	 * @see org.openmrs.module.webservices.rest.web.api.RestService#getSearchHandler(java.lang.String,
	 *      java.util.Map)
	 * <strong>Should</strong> return search handler matching id set in given parameters
	 * <strong>Should</strong> fail if parameters contain a search handler id which cannot be found
	 * <strong>Should</strong> fail if two search handlers for the same resource have the same id
	 * <strong>Should</strong> return null if parameters do not contain a search handler id and no other non special
	 *         request parameters
	 * <strong>Should</strong> return search handler providing all request parameters and parameters satisfying its
	 *         required parameters
	 * <strong>Should</strong> return null if given parameters are missing a parameter required by search handlers
	 *         eligible for given resource name and parameters
	 * <strong>Should</strong> fail if two search handlers match given resource and parameters and no search handler
	 *         id is specified
	 * <strong>Should</strong> return null if a non special request parameter in given parameters cannot be found in
	 *         any search handler
	 * <strong>Should</strong> return null if no search handler is found for given resource name
	 * <strong>Should</strong> return null if no search handler is found for current openmrs version
	 */
	@Override
	public SearchHandler getSearchHandler(String resourceName, Map<String, String[]> parameters) throws APIException {
		initializeSearchHandlers();
		
		Set<SearchParameter> searchParameters = new HashSet<SearchParameter>();
		
		for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
			if (!RestConstants.SPECIAL_REQUEST_PARAMETERS.contains(parameter.getKey())
			        || RestConstants.REQUEST_PROPERTY_FOR_TYPE.equals(parameter.getKey())) {
				searchParameters.add(new SearchParameter(parameter.getKey(), parameter.getValue()[0]));
			}
		}
		
		String[] searchIds = parameters.get(RestConstants.REQUEST_PROPERTY_FOR_SEARCH_ID);
		if (searchIds != null && searchIds.length > 0) {
			SearchHandler searchHandler = searchHandlersByIds.get(new CompositeSearchHandlerKeyValue(resourceName,
			        searchIds[0]));
			if (searchHandler == null) {
				throw new InvalidSearchException("The search with id '" + searchIds[0] + "' for '" + resourceName
				        + "' resource is not recognized");
			} else {
				return searchHandler;
			}
		}
		
		Set<SearchHandler> candidateSearchHandlers = null;
		for (SearchParameter param : searchParameters) {
			Set<SearchHandler> searchHandlers = searchHandlersByParameter.get(new CompositeSearchHandlerKeyValue(
			        resourceName, param.getName(), param.getValue()));
			if (searchHandlers == null) {
				searchHandlers = searchHandlersByParameter.get(new CompositeSearchHandlerKeyValue(resourceName, param
				        .getName()));
				if (searchHandlers == null)
					return null; //Missing parameter so there's no handler.
			}
			if (candidateSearchHandlers == null) {
				candidateSearchHandlers = new HashSet<SearchHandler>();
				candidateSearchHandlers.addAll(searchHandlers);
			} else {
				//Eliminate candidate search handlers that do not include all parameters
				candidateSearchHandlers.retainAll(searchHandlers);
			}
		}
		
		if (candidateSearchHandlers == null) {
			return null;
		} else {
			eliminateCandidateSearchHandlersWithMissingRequiredParameters(candidateSearchHandlers, searchParameters);
			
			if (candidateSearchHandlers.isEmpty()) {
				return null;
			} else if (candidateSearchHandlers.size() == 1) {
				return candidateSearchHandlers.iterator().next();
			} else {
				List<String> candidateSearchHandlerIds = new ArrayList<String>();
				for (SearchHandler candidateSearchHandler : candidateSearchHandlers) {
					candidateSearchHandlerIds.add(RestConstants.REQUEST_PROPERTY_FOR_SEARCH_ID + "="
					        + candidateSearchHandler.getSearchConfig().getId());
				}
				throw new InvalidSearchException("The search is ambiguous. Please specify "
				        + StringUtils.join(candidateSearchHandlerIds, " or "));
			}
		}
	}
	
	/**
	 * Eliminate search handlers with at least one required parameter that is not provided in
	 * {@code searchParameters}.
	 * 
	 * @param candidateSearchHandlers the search handlers to filter for required parameters
	 * @param searchParameters the search parameters to be checked against search handlers required
	 *            parameters
	 */
	private void eliminateCandidateSearchHandlersWithMissingRequiredParameters(Set<SearchHandler> candidateSearchHandlers,
	        Set<SearchParameter> searchParameters) {
		Iterator<SearchHandler> it = candidateSearchHandlers.iterator();
		while (it.hasNext()) {
			SearchHandler candidateSearchHandler = it.next();
			boolean remove = true;
			
			for (SearchQuery candidateSearchQueries : candidateSearchHandler.getSearchConfig().getSearchQueries()) {
				Set<SearchParameter> requiredParameters = new HashSet<SearchParameter>(
				        candidateSearchQueries.getRequiredParameters());
				
				Iterator<SearchParameter> iterator = requiredParameters.iterator();
				while (iterator.hasNext()) {
					SearchParameter requiredParameter = iterator.next();
					for (SearchParameter param : searchParameters) {
						if (requiredParameter.getValue() == null) {
							if (requiredParameter.getName().equals(param.getName())) {
								iterator.remove();
							}
						} else {
							if (requiredParameter.equals(param)) {
								iterator.remove();
							}
						}
					}
				}
				if (requiredParameters.isEmpty()) {
					remove = false;
					break;
				}
			}
			
			if (remove) {
				it.remove();
			}
		}
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.api.RestService#getResourceHandlers()
	 * <strong>Should</strong> return list of delegating resource handlers including subclass handlers
	 * <strong>Should</strong> return list with delegating resource with lower order value if two resources with the
	 *         same name are found for given name
	 * <strong>Should</strong> fail if failed to get resource classes
	 * <strong>Should</strong> fail if two resources with same name and order are found for a class
	 */
	@Override
	public List<DelegatingResourceHandler<?>> getResourceHandlers() throws APIException {
		initializeResources();
		
		List<DelegatingResourceHandler<?>> resourceHandlers = new ArrayList<DelegatingResourceHandler<?>>();
		
		for (Resource resource : resourcesBySupportedClasses.values()) {
			if (resource instanceof DelegatingResourceHandler) {
				resourceHandlers.add((DelegatingResourceHandler<?>) resource);
			}
		}
		
		List<DelegatingSubclassHandler> subclassHandlers = restHelperService.getRegisteredRegisteredSubclassHandlers();
		for (DelegatingSubclassHandler subclassHandler : subclassHandlers) {
			resourceHandlers.add(subclassHandler);
		}
		
		return resourceHandlers;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.api.RestService#getAllSearchHandlers()
	 * <strong>Should</strong> return all search handlers if search handlers have been initialized
	 * <strong>Should</strong> return null if search handlers have not been initialized
	 */
	public List<SearchHandler> getAllSearchHandlers() {
		
		return allSearchHandlers;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.api.RestService#getSearchHandlers(java.lang.String)
	 * <strong>Should</strong> return search handlers for given resource name
	 * <strong>Should</strong> return null if no search handler is found for given resource name
	 * <strong>Should</strong> return null if no search handler is found for current openmrs version
	 * <strong>Should</strong> return null given null
	 * <strong>Should</strong> fail if two search handlers for the same resource have the same id
	 */
	@Override
	public Set<SearchHandler> getSearchHandlers(String resourceName) {
		if (searchHandlersByResource == null) {
			initializeSearchHandlers();
		}
		return searchHandlersByResource.get(resourceName);
	}
	
	/**
	 * @see RestService#initialize()
	 * <strong>Should</strong> initialize resources and search handlers
	 * <strong>Should</strong> clear cached resources and search handlers and reinitialize them
	 * <strong>Should</strong> fail if failed to get resource classes
	 * <strong>Should</strong> fail if failed to instantiate a resource
	 * <strong>Should</strong> fail if two resources with same name and order are found
	 * <strong>Should</strong> fail if two search handlers for the same resource have the same id
	 */
	@Override
	public void initialize() {
		
		// first clear out any existing values
		resourceDefinitionsByNames = null;
		resourcesBySupportedClasses = null;
		searchHandlersByIds = null;
		searchHandlersByParameter = null;
		searchHandlersByResource = null;
		
		initializeResources();
		initializeSearchHandlers();
	}
}

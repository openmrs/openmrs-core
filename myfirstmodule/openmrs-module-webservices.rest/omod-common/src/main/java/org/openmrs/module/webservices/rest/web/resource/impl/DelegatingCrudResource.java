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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.CrudResource;
import org.openmrs.module.webservices.rest.web.resource.api.Listable;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.Searchable;
import org.openmrs.module.webservices.rest.web.response.IllegalPropertyException;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainSubResourceController;
import org.openmrs.module.webservices.validation.ValidateUtil;

/**
 * A base implementation of a {@link CrudResource} that delegates CRUD operations to a wrapped
 * object
 * 
 * @param <T> the class we're delegating to
 */
public abstract class DelegatingCrudResource<T> extends BaseDelegatingResource<T> implements CrudResource, Searchable, Listable {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Retrievable#retrieve(java.lang.String,
	 *      org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public Object retrieve(String uuid, RequestContext context) throws ResponseException {
		T delegate = getByUniqueId(uuid);
		if (delegate == null)
			throw new ObjectNotFoundException();
		
		SimpleObject ret = asRepresentation(delegate, context.getRepresentation());
		if (hasTypesDefined())
			ret.add(RestConstants.PROPERTY_FOR_TYPE, getTypeName(delegate));
		return ret;
	}
	
	/**
	 * Default implementation that returns REF, DEFAULT, and FULL
	 * 
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Retrievable#getAvailableRepresentations()
	 */
	@Override
	public List<Representation> getAvailableRepresentations() {
		return Arrays.asList(Representation.DEFAULT, Representation.FULL, Representation.REF);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Creatable#create(org.springframework.web.context.request.WebRequest)
	 */
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		T delegate = convert(propertiesToCreate);
		ValidateUtil.validate(delegate);
		delegate = save(delegate);
		SimpleObject ret = (SimpleObject) ConversionUtil.convertToRepresentation(delegate, context.getRepresentation());
		
		// add the 'type' discriminator if we support subclasses
		if (hasTypesDefined()) {
			ret.add(RestConstants.PROPERTY_FOR_TYPE, getTypeName(delegate));
		}
		
		return ret;
	}
	
	public T convert(SimpleObject propertiesToCreate) {
		DelegatingResourceHandler<? extends T> handler;
		if (hasTypesDefined()) {
			String type = (String) propertiesToCreate.remove(RestConstants.PROPERTY_FOR_TYPE);
			if (type == null)
				throw new IllegalArgumentException(
				        "When creating a resource that supports subclasses, you must indicate the particular subclass with a "
				                + RestConstants.PROPERTY_FOR_TYPE + " property");
			handler = getResourceHandler(type);
		} else {
			handler = this;
		}
		
		T delegate = handler.newDelegate(propertiesToCreate);
		DelegatingResourceDescription description = handler.getCreatableProperties();
		if (propertiesToCreate.containsKey("uuid")) {
			description.addProperty("uuid");
		}
		setConvertedProperties(delegate, propertiesToCreate, description, true);
		return delegate;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Updatable#update(java.lang.String,
	 *      org.openmrs.module.webservices.rest.SimpleObject)
	 */
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		T delegate = getByUniqueId(uuid);
		if (delegate == null)
			throw new ObjectNotFoundException();
		
		if (hasTypesDefined() && propertiesToUpdate.containsKey(RestConstants.PROPERTY_FOR_TYPE)) {
			// if they specify a type discriminator it must match the expected one--type can't be modified
			String type = (String) propertiesToUpdate.remove(RestConstants.PROPERTY_FOR_TYPE);
			if (!delegate.getClass().equals(getActualSubclass(type))) {
				String nameToShow = getTypeName(delegate);
				if (nameToShow == null)
					nameToShow = delegate.getClass().getName();
				throw new IllegalArgumentException("You passed " + RestConstants.PROPERTY_FOR_TYPE + "=" + type
				        + " but this instance is a " + nameToShow);
			}
		}
		
		DelegatingResourceHandler<? extends T> handler = getResourceHandler(delegate);
		
		DelegatingResourceDescription description = handler.getUpdatableProperties();
		if (isRetirable()) {
			description.addProperty("retired");
		} else if (isVoidable()) {
			description.addProperty("voided");
		}
		
		setConvertedProperties(delegate, propertiesToUpdate, description, false);
		ValidateUtil.validate(delegate);
		delegate = save(delegate);
		
		SimpleObject ret = (SimpleObject) ConversionUtil.convertToRepresentation(delegate, context.getRepresentation());
		
		// add the 'type' discriminator if we support subclasses
		if (hasTypesDefined()) {
			ret.add(RestConstants.PROPERTY_FOR_TYPE, getTypeName(delegate));
		}
		
		return ret;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Deletable#delete(java.lang.String)
	 */
	@Override
	public void delete(String uuid, String reason, RequestContext context) throws ResponseException {
		T delegate = getByUniqueId(uuid);
		if (delegate == null)
			throw new ObjectNotFoundException();
		delete(delegate, reason, context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Deletable#undelete(java.lang.String,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public Object undelete(String uuid, RequestContext context) throws ResponseException {
		T delegate = getByUniqueId(uuid);
		if (delegate == null)
			throw new ObjectNotFoundException();
		
		delegate = undelete(delegate, context);
		return (SimpleObject) ConversionUtil.convertToRepresentation(delegate, context.getRepresentation());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Purgeable#purge(java.lang.String)
	 */
	@Override
	public void purge(String uuid, RequestContext context) throws ResponseException {
		T delegate = getByUniqueId(uuid);
		if (delegate == null) {
			// HTTP DELETE is idempotent, so if we can't find the object, we assume it's already deleted and return success
			return;
		}
		purge(delegate, context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Searchable#search(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public SimpleObject search(RequestContext context) throws ResponseException {
		PageableResult result = doSearch(context);
		return result.toSimpleObject(this);
	}
	
	/**
	 * Implementations should override this method if they are actually searchable.
	 */
	protected PageableResult doSearch(RequestContext context) {
		return new EmptySearchResult();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Listable#getAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public SimpleObject getAll(RequestContext context) throws ResponseException {
		if (context.getType() != null) {
			if (!hasTypesDefined())
				throw new IllegalArgumentException(getClass() + " does not support "
				        + RestConstants.REQUEST_PROPERTY_FOR_TYPE);
			if (context.getType().equals(getResourceName()))
				throw new IllegalArgumentException("You may not specify " + RestConstants.REQUEST_PROPERTY_FOR_TYPE + "="
				        + context.getType() + " because it is the default behavior for this resource");
			DelegatingSubclassHandler<T, ? extends T> handler = getSubclassHandler(context.getType());
			if (handler == null)
				throw new IllegalArgumentException("No handler is specified for " + RestConstants.REQUEST_PROPERTY_FOR_TYPE
				        + "=" + context.getType());
			PageableResult result = handler.getAllByType(context);
			return result.toSimpleObject(this);
		} else {
			PageableResult result = doGetAll(context);
			return result.toSimpleObject(this);
		}
	}
	
	/**
	 * Implementations should override this method to return a list of all instances represented by
	 * the specified rest resource in the database. (If the resource supports subclasses, this
	 * method should return all of its documents regardless of their type/subclass.)
	 * 
	 * @throws ResponseException
	 */
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * TODO
	 * 
	 * @param delegateUuid
	 * @param subResourceName
	 * @param rep
	 * @return
	 * @throws ResponseException
	 */
	public Object listSubResource(String delegateUuid, String subResourceName, Representation rep) throws ResponseException {
		// TODO SUBCLASSHANDLER
		List<String> legal = getPropertiesToExposeAsSubResources();
		if (legal == null || !legal.contains(subResourceName))
			throw new IllegalPropertyException();
		T delegate = getByUniqueId(delegateUuid);
		if (delegate == null)
			throw new ObjectNotFoundException();
		return ConversionUtil.getPropertyWithRepresentation(delegate, subResourceName, rep);
	}
	
	/**
	 * Resources provided by this module itself are published without any particular namespace (e.g.
	 * /ws/rest/v1/concept) but when modules publish resources, they should be namespaced (e.g.
	 * /ws/rest/v1/moduleId/moduleresource).
	 * 
	 * @deprecated Since 2.x the namespace must be declared in {@link Resource}'s name,
	 *             {@link MainResourceController} and {@link MainSubResourceController}.
	 */
	@Deprecated
	protected final String getNamespacePrefix() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Determines if the resource can be retired.
	 * 
	 * @return true if can be retired, else false.
	 */
	public boolean isRetirable() {
		return false;
	}
	
	/**
	 * Determines if the resource can be voided.
	 * 
	 * @return true if can be voided, else false.
	 */
	public boolean isVoidable() {
		return false;
	}
}

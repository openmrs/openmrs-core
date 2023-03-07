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

import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Converter;
import org.openmrs.module.webservices.rest.web.resource.api.Listable;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.Retrievable;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.Arrays;
import java.util.List;

/**
 * This helper class is for resources that are backed by a Java object that they delegate to, but
 * only support read operations (e.g. because they are not straightforwardly persisted in the
 * database, or can't be edited/deleted via REST for some other reason).
 */
public abstract class BaseDelegatingReadableResource<T> extends BaseDelegatingResource<T> implements Retrievable, Listable, Converter<T> {
	
	@Override
	public List<Representation> getAvailableRepresentations() {
		return Arrays.asList(Representation.REF, Representation.DEFAULT);
	}
	
	@Override
	protected void delete(T delegate, String reason, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("read-only resource");
	}
	
	@Override
	public void purge(T delegate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("read-only resource");
	}
	
	@Override
	public T save(T delegate) {
		throw new ResourceDoesNotSupportOperationException("read-only resource");
	}
	
	@Override
	public Object retrieve(String uuid, RequestContext context) throws ResponseException {
		T delegate = getByUniqueId(uuid);
		return asRepresentation(delegate, context.getRepresentation());
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
	 * Subclasses should override this if the operation is feasible.
	 * 
	 * @param context
	 * @return
	 */
	public PageableResult doGetAll(RequestContext context) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Searchable#search(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	public SimpleObject search(RequestContext context) throws ResponseException {
		PageableResult result = doSearch(context);
		return result.toSimpleObject(this);
	}
	
	/**
	 * Implementations should override this method and implement Searchable if they are actually
	 * searchable.
	 */
	protected PageableResult doSearch(RequestContext context) {
		throw new ResourceDoesNotSupportOperationException("not searchable");
	}
}

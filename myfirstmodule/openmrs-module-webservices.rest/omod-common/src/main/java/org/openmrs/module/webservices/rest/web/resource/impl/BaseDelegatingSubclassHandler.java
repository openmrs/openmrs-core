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

import io.swagger.models.Model;
import org.openmrs.OpenmrsData;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.util.ReflectionUtil;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.RepHandler;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.HashSet;
import java.util.Set;

/**
 * Helpful base implementation of {@link DelegatingSubclassHandler}
 */
public abstract class BaseDelegatingSubclassHandler<Superclass, Subclass extends Superclass> implements DelegatingSubclassHandler<Superclass, Subclass> {
	
	/**
	 * Properties that should silently be ignored if you try to get them. Implementations should
	 * generally configure this property with a list of properties that were added to their
	 * underlying domain object after the minimum OpenMRS version required by this module. For
	 * example PatientIdentifierTypeResource will allow "locationBehavior" to be missing, since it
	 * wasn't added to PatientIdentifierType until OpenMRS 1.9. delegate class
	 */
	protected Set<String> allowedMissingProperties = new HashSet<String>();
	
	/**
	 * Uses introspection into the generic interface to determine the superclass plugged into by
	 * this handler
	 * 
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubclassHandler#getSuperclass()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<Superclass> getSuperclass() {
		return ReflectionUtil.getParameterizedTypeFromInterface(getClass(), DelegatingSubclassHandler.class, 0);
	}
	
	/**
	 * Uses introspection into the generic interface to determine the subclass handled by this
	 * handler
	 * 
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubclassHandler#getSubclassHandled()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<Subclass> getSubclassHandled() {
		return ReflectionUtil.getParameterizedTypeFromInterface(getClass(), DelegatingSubclassHandler.class, 1);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getUpdatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		return getCreatableProperties();
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return getCREATEModel(rep);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Subclass save(Subclass delegate) {
		return (Subclass) getResource().save(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(Subclass delegate, RequestContext context) throws ResponseException {
		getResource().purge(delegate, context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return RestConstants.PROPERTY_FOR_RESOURCE_VERSION_DEFAULT_VALUE;
	}
	
	/**
	 * @return the resource this handler works with
	 */
	@SuppressWarnings("unchecked")
	public DelegatingCrudResource<Superclass> getResource() {
		// get the service-managed singleton version of the resource
		Resource resource = Context.getService(RestService.class).getResourceBySupportedClass(getSuperclass());
		
		return (DelegatingCrudResource<Superclass>) resource;
	}
	
	/**
	 * Assumes we can get a "display" property
	 * 
	 * @param delegate
	 * @return standard REF representation of delegate
	 * @throws ConversionException
	 */
	@RepHandler(RefRepresentation.class)
	public SimpleObject convertToRef(Subclass delegate) throws ConversionException {
		DelegatingResourceDescription rep = new DelegatingResourceDescription();
		rep.addProperty("uuid");
		rep.addProperty("display");
		if (delegate instanceof OpenmrsData) {
			if (((OpenmrsData) delegate).isVoided())
				rep.addProperty("voided");
		} else if (delegate instanceof OpenmrsMetadata && ((OpenmrsMetadata) delegate).isRetired()) {
			rep.addProperty("retired");
		}
		rep.addSelfLink();
		return getResource().convertDelegateToRepresentation(delegate, rep);
	}
	
	/**
	 * Gets the audit information of a resource.
	 * 
	 * @param resource the resource.
	 * @return a {@link SimpleObject} with the audit information.
	 */
	@PropertyGetter("auditInfo")
	public SimpleObject getAuditInfo(Object resource) {
		return ConversionUtil.getAuditInfo(resource);
	}
	
	@Override
	public Subclass newDelegate(SimpleObject object) {
		return newDelegate();
	}
}

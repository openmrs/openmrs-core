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

import java.util.Date;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.lang.StringUtils;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.RepHandler;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * Subclass of {@link DelegatingCrudResource} with helper methods specific to
 * {@link OpenmrsMetadata}
 * 
 * @param <T>
 */
public abstract class MetadataDelegatingCrudResource<T extends OpenmrsMetadata> extends DelegatingCrudResource<T> {
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = ((ModelImpl) super.getGETModel(rep))
		        .property("uuid", new StringProperty())
		        .property("display", new StringProperty());
		if (rep instanceof FullRepresentation) {
			model
			        .property("name", new StringProperty())
			        .property("description", new StringProperty())
			        .property("retired", new BooleanProperty());
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl()
		        .property("name", new StringProperty())
		        .property("description", new StringProperty())
		        .required("name");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingConverter#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("retired");
			description.addSelfLink();
			if (rep instanceof DefaultRepresentation) {
				description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			} else {
				description.addProperty("auditInfo");
			}
			return description;
		}
		return null;
	}
	
	@RepHandler(RefRepresentation.class)
	public SimpleObject convertToRef(T delegate) throws ConversionException {
		DelegatingResourceDescription rep = new DelegatingResourceDescription();
		rep.addProperty("uuid");
		rep.addProperty("display");
		if (delegate.isRetired())
			rep.addProperty("retired");
		rep.addSelfLink();
		return convertDelegateToRepresentation(delegate, rep);
	}
	
	@RepHandler(DefaultRepresentation.class)
	public SimpleObject asDefaultRep(T delegate) throws Exception {
		DelegatingResourceDescription rep = new DelegatingResourceDescription();
		rep.addProperty("uuid");
		rep.addProperty("display");
		rep.addProperty("name");
		rep.addProperty("description");
		rep.addProperty("retired");
		rep.addSelfLink();
		rep.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
		return convertDelegateToRepresentation(delegate, rep);
	}
	
	@RepHandler(FullRepresentation.class)
	public SimpleObject asFullRep(T delegate) throws Exception {
		DelegatingResourceDescription rep = new DelegatingResourceDescription();
		rep.addProperty("uuid");
		rep.addProperty("display");
		rep.addProperty("name");
		rep.addProperty("description");
		rep.addProperty("retired");
		rep.addProperty("auditInfo");
		rep.addSelfLink();
		return convertDelegateToRepresentation(delegate, rep);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#delete(java.lang.String,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void delete(T delegate, String reason, RequestContext context) throws ResponseException {
		if (delegate.isRetired()) {
			// since DELETE should be idempotent, we return success here
			return;
		}
		delegate.setRetired(true);
		delegate.setRetiredBy(Context.getAuthenticatedUser());
		delegate.setDateRetired(new Date());
		delegate.setRetireReason(reason);
		save(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#undelete(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected T undelete(T delegate, RequestContext context) throws ResponseException {
		if (delegate.isRetired()) {
			delegate.setRetired(false);
			delegate.setRetiredBy(null);
			delegate.setDateRetired(null);
			delegate.setRetireReason(null);
			delegate.setChangedBy(Context.getAuthenticatedUser());
			delegate.setDateChanged(new Date());
			delegate = save(delegate);
		}
		return delegate;
	}
	
	/**
	 * Gets the display string, which is specific to {@link OpenmrsMetadata}
	 * 
	 * @param delegate the meta-data object.
	 * @return the display string.
	 * <strong>Should</strong> return a localized message if specified
	 * <strong>Should</strong> return the name property when no localized message is specified
	 * <strong>Should</strong> return the empty string when no localized message is specified and the name property
	 *         is null
	 */
	@PropertyGetter("display")
	public String getDisplayString(T delegate) {
		String localization = getLocalization(delegate.getClass().getSimpleName(), delegate.getUuid());
		if (localization != null) {
			return localization;
		} else {
			return StringUtils.isEmpty(delegate.getName()) ? "" : delegate.getName();
		}
	}
	
	/**
	 * This code is largely copied from the UI Framework:
	 * org.openmrs.ui.framework.FormatterImpl#format(org.openmrs.OpenmrsMetadata, java.util.Locale)
	 * 
	 * @param shortClassName
	 * @param uuid
	 * @return localization for the given metadata, from message source, in the authenticated locale
	 */
	private String getLocalization(String shortClassName, String uuid) {
		// in case this is a hibernate proxy, strip off anything after an underscore
		// ie: EncounterType_$$_javassist_26 needs to be converted to EncounterType
		int underscoreIndex = shortClassName.indexOf("_$");
		if (underscoreIndex > 0) {
			shortClassName = shortClassName.substring(0, underscoreIndex);
		}
		
		String code = "ui.i18n." + shortClassName + ".name." + uuid;
		String localization = Context.getMessageSourceService().getMessage(code);
		if (localization == null || localization.equals(code)) {
			return null;
		} else {
			return localization;
		}
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		
		description.addRequiredProperty("name");
		description.addProperty("description");
		
		return description;
	}
	
	@Override
	public boolean isRetirable() {
		return true;
	}
}
